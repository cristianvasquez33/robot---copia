import com.cyberbotics.webots.controller.CameraRecognitionObject;

public class AgenteDifuso {

    private double[] pA;
    private Diagnostico dx, ds; // Solo horizontal y profundidad
    private ControladorDifusoPD controladorHorizontal;
    private ControladorDifusoPD controladorProfundidad;
    private VentanaReglas ventana;
    private boolean inicializado = false; // Para inicializar posiciones solo una vez

    public AgenteDifuso() {
        // Inicializar con las posiciones iniciales que Webots carga por defecto
        // Estas son las posiciones del archivo Brazo_Steel_Ro.java líneas 69-75
        pA = new double[]{0.07, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

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
        // Inicializar posiciones solo la primera vez (usa posición actual de Webots)
        if (!inicializado) {
            // En el primer ciclo, pA[] mantiene los valores que tiene el robot
            // No hacemos nada, dejamos que se usen las posiciones iniciales
            inicializado = true;
            System.out.println("✓ Controlador difuso inicializado desde posición actual del robot");
        }

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

            // Inferencia difusa con identificación de controlador
            ventana.mostrarRegla("╔═══════════════════════════════════╗");
            ventana.mostrarRegla("║   CONTROLADOR HORIZONTAL          ║");
            ventana.mostrarRegla("╚═══════════════════════════════════╝");
            double yHor  = controladorHorizontal.inferir(errorH, derivadaH);

            ventana.mostrarRegla("\n╔═══════════════════════════════════╗");
            ventana.mostrarRegla("║   CONTROLADOR PROFUNDIDAD         ║");
            ventana.mostrarRegla("╚═══════════════════════════════════╝");
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
            // ESTRATEGIA SIMPLIFICADA: Alcance frontal coordinado
            pA[0] += 0.050 * yHor;   // Hombro horizontal (giro izq/der para centrar)
            pA[1] -= 0.100 * yProf;  // Hombro vertical (bajar para acercar)
            pA[2] -= 0.120 * yProf;  // Brazo superior (extender hacia adelante)
            pA[3] += 0.120 * yProf;  // Codo (extender brazo)
            pA[4] += 0.080 * yProf;  // Brazo inferior (ajuste fino)

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
