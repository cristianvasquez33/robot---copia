import com.cyberbotics.webots.controller.CameraRecognitionObject;

public class AgenteDifuso {

    private double[] pA;
    private Diagnostico dx, dy, ds;
    private ControladorDifusoPD controladorProfundidad;
    private ControladorDifusoPD controladorVertical;
    private VentanaReglas ventana;

    public AgenteDifuso() {
        pA = new double[7];
        dx = new Diagnostico(112);  // posición horizontal deseada
        dy = new Diagnostico(112);  // posición vertical deseada
        ds = new Diagnostico(80);   // tamaño deseado

        ventana = new VentanaReglas();

        String[][] fam = {
            {"NB", "NS", "Z", "PS", "PB"},
            {"NS", "Z",  "Z", "PS", "PB"},
            {"NS", "Z",  "Z", "PS", "PS"},
            {"Z",  "Z",  "PS", "PS", "PB"},
            {"Z",  "PS", "PS", "PB", "PB"}
        };

        controladorProfundidad = new ControladorDifusoPD(fam, 0.03125, ventana);
        controladorVertical = new ControladorDifusoPD(fam, 0.00125, ventana);
    }

    public void Razonamiento(CameraRecognitionObject[] objData) {
        pA[0] = 0.07; pA[1] = 0.6; pA[2] = -1.65;
        pA[3] = 1.17; pA[4] = 1.5; pA[5] = -1.3; pA[6] = -1.1;

        if (objData.length > 0) {
            double x = objData[0].getPosition_on_image()[0];
            double y = objData[0].getPosition_on_image()[1];
            double size = objData[0].getSize_on_image()[0];

            dx.actualizar(x);
            dy.actualizar(y);
            ds.actualizar(size);

            double x1 = controladorProfundidad.escalar(dx.errorPresente);
            double x2 = controladorProfundidad.escalar(dx.derivada);
            double eS = controladorProfundidad.escalar(ds.errorPresente);
            double dS = controladorProfundidad.escalar(ds.derivada);
            double eY = controladorVertical.escalar(dy.errorPresente);
            double dY = controladorVertical.escalar(dy.derivada);

            ventana.limpiar();

            double yHor = controladorProfundidad.inferir(x1, x2);
            double yProf = controladorProfundidad.inferir(eS, dS);
            double yVert = controladorVertical.inferir(eY, dY);

            dx.imprimir("Horizontal");
            ds.imprimir("Profundidad");
            dy.imprimir("Vertical");

            pA[0] += 0.1 * yHor;
            pA[1] -= 0.2 * yProf;
            pA[2] -= 0.3 * yProf;
            pA[3] += 0.25 * yProf;
            pA[4] += 0.1 * yVert;

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