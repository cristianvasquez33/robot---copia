import com.cyberbotics.webots.controller.CameraRecognitionObject;

public class AgenteDifuso {

    private double[] pA;
    private Diagnostico dx, ds; // Solo horizontal y profundidad
    private ControladorDifusoPD controladorHorizontal;
    private ControladorDifusoPD controladorProfundidad;
    private VentanaReglas ventana;

    public AgenteDifuso() {
        pA = new double[7];

        dx = new Diagnostico(114); // horizontal - centro de imagen en X
        ds = new Diagnostico(80);  // profundidad - tamaño objetivo del objeto

        ventana = new VentanaReglas();

        // Matriz FAM optimizada para acercamiento rápido y estable
        // Error\Derivada: NM    NP    Z     PP    PM
        String[][] fam = {
            {"NB", "NS", "NS", "Z",  "PS"}, // NM: lejos negativo
            {"NS", "NS", "Z",  "PS", "PS"}, // NP: cerca negativo
            {"NS", "Z",  "Z",  "Z",  "PS"}, // Z:  centrado
            {"PS", "PS", "Z",  "PS", "PS"}, // PP: cerca positivo
            {"PS", "Z",  "PS", "PS", "PB"}  // PM: lejos positivo
        };

        // Factores de escala optimizados (más sensibles)
        controladorHorizontal = new ControladorDifusoPD(fam, 0.040, ventana);
        controladorProfundidad = new ControladorDifusoPD(fam, 0.040, ventana);
    }

    public void Razonamiento(CameraRecognitionObject[] objData) {
        // Posición inicial del brazo (configuración de acercamiento)
        pA[0] = 0.07; pA[1] = 0.6; pA[2] = -1.65;
        pA[3] = 1.17; pA[4] = 1.5; pA[5] = -1.3; pA[6] = -1.1;

        if (objData.length > 0) {
            // Obtener posición horizontal (ancho) y tamaño del objeto
            double posicionX = objData[0].getPosition_on_image()[0]; // Posición X en imagen
            double tamano = objData[0].getSize_on_image()[0]; // Ancho del objeto

            // Actualizar diagnósticos
            dx.actualizar(posicionX);
            ds.actualizar(tamano);

            // Escalar entradas para el controlador difuso
            double errorH = controladorHorizontal.escalar(dx.errorPresente);
            double derivadaH = controladorHorizontal.escalar(dx.derivada);
            double errorP = controladorProfundidad.escalar(ds.errorPresente);
            double derivadaP = controladorProfundidad.escalar(ds.derivada);

            ventana.limpiar();

            // Inferencia difusa
            double yHor  = controladorHorizontal.inferir(errorH, derivadaH);
            double yProf = controladorProfundidad.inferir(errorP, derivadaP);

            // Zona muerta ampliada para evitar oscilaciones
            if (Math.abs(dx.errorPresente) < 15 && Math.abs(dx.derivada) < 3) yHor = 0;
            if (Math.abs(ds.errorPresente) < 10 && Math.abs(ds.derivada) < 3) yProf = 0;

            // Diagnóstico por ciclo
            dx.imprimir("Horizontal");
            ds.imprimir("Profundidad");
            System.out.printf("→ Salidas: yHor=%.3f yProf=%.3f\n", yHor, yProf);

            // Mostrar estado de acercamiento
            if (Math.abs(dx.errorPresente) < 15 && Math.abs(ds.errorPresente) < 10) {
                System.out.println("✓ Objetivo alcanzado - Robot estabilizado");
            }

            // Aplicación de correcciones optimizadas
            pA[0] += 0.045 * yHor;   // Hombro horizontal (giro izq/der)
            pA[1] -= 0.080 * yProf;  // Hombro vertical (acercamiento)
            pA[2] -= 0.100 * yProf;  // Brazo superior (acercamiento)
            pA[3] += 0.100 * yProf;  // Codo (extensión)
            pA[4] += 0.060 * yProf;  // Brazo inferior (extensión adicional)

            limitarRangos();
        } else {
            System.out.println("⚠ No se detectó objeto.");
        }
    }

    private void limitarRangos() {
        double[] min = {-2.5, -1.5, -3.0, -0.5, -2.0, -1.4, -2.0};
        double[] max = { 2.6,  1.2,  1.5,  2.3,  2.1,  1.4,  2.0};
        for (int i = 0; i < pA.length; i++) {
            pA[i] = Math.max(min[i], Math.min(max[i], pA[i]));
        }
    }

    public double[] getArm() {
        return pA;
    }
}
