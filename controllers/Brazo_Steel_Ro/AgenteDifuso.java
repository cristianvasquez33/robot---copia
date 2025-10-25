import com.cyberbotics.webots.controller.CameraRecognitionObject;

public class AgenteDifuso {

    private double[] pA;
    private Diagnostico dx, dy, ds;
    private ControladorDifusoPD controladorHorizontal;
    private ControladorDifusoPD controladorProfundidad;
    private ControladorDifusoPD controladorVertical;
    private VentanaReglas ventana;

    public AgenteDifuso() {
        pA = new double[7];

        dx = new Diagnostico(114); // horizontal
        dy = new Diagnostico(55);  // vertical
        ds = new Diagnostico(80);  // profundidad

        ventana = new VentanaReglas();

        // Matriz FAM con respuesta gradual
        String[][] fam = {
            {"NS", "NS", "Z",  "PS", "PS"},
            {"NS", "Z",  "Z",  "PS", "PM"},
            {"Z",  "Z",  "Z",  "PS", "PM"},
            {"Z",  "Z",  "PS", "PM", "PM"},
            {"PS", "PS", "PM", "PM", "PB"}
        };

        controladorHorizontal = new ControladorDifusoPD(fam, 0.035, ventana);
        controladorProfundidad = new ControladorDifusoPD(fam, 0.035, ventana);
        controladorVertical   = new ControladorDifusoPD(fam, 0.0233, ventana);
    }

    public void Razonamiento(CameraRecognitionObject[] objData) {
        // Posición inicial del brazo
        pA[0] = 0.07; pA[1] = 0.6; pA[2] = -1.65;
        pA[3] = 1.17; pA[4] = 1.5; pA[5] = -1.3; pA[6] = -1.1;

        if (objData.length > 0) {
            double ancho = objData[0].getSize_on_image()[0];
            double alto  = objData[0].getSize_on_image()[1];
            double size  = objData[0].getSize_on_image()[0];

            dx.actualizar(ancho);
            dy.actualizar(alto);
            ds.actualizar(size);

            double x1 = controladorHorizontal.escalar(dx.errorPresente);
            double x2 = controladorHorizontal.escalar(dx.derivada);
            double eS = controladorProfundidad.escalar(ds.errorPresente);
            double dS = controladorProfundidad.escalar(ds.derivada);
            double eY = controladorVertical.escalar(dy.errorPresente);
            double dY = controladorVertical.escalar(dy.derivada);

            ventana.limpiar();

            double yHor  = controladorHorizontal.inferir(x1, x2);
            double yProf = controladorProfundidad.inferir(eS, dS);
            double yVert = controladorVertical.inferir(eY, dY);

            // Zona muerta cerca del objetivo
            if (Math.abs(dx.errorPresente) < 3 && Math.abs(dx.derivada) < 1) yHor = 0;
            if (Math.abs(ds.errorPresente) < 3 && Math.abs(ds.derivada) < 1) yProf = 0;
            if (Math.abs(dy.errorPresente) < 3 && Math.abs(dy.derivada) < 1) yVert = 0;

            // Diagnóstico por ciclo
            dx.imprimir("Horizontal");
            ds.imprimir("Profundidad");
            dy.imprimir("Vertical");
            System.out.printf("→ yHor=%.2f yProf=%.2f yVert=%.2f\n", yHor, yProf, yVert);

            // Aplicación de correcciones suaves
            pA[0] += 0.05 * yHor;
            pA[1] -= 0.08 * yProf;
            pA[2] -= 0.10 * yProf;
            pA[3] += 0.08 * yProf;
            pA[4] += 0.05 * yVert;

            limitarRangos();
        } else {
            System.out.println("No se detectó objeto.");
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
