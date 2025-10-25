import java.util.*;

public class ControladorDifusoPD {

    private static final String[] ETIQUETAS = {"NM", "NP", "Z", "PP", "PM"};
    private static final Map<String, Double> CENTROIDES = Map.of(
        "NB", -2.0, "NS", -1.0, "Z", 0.0, "PS", 1.0, "PB", 2.0
    );

    private String[][] fam;
    private trapesoidal[] conjuntos;
    private VentanaReglas ventana;
    private double escala;

    public ControladorDifusoPD(String[][] matrizFAM, double escala, VentanaReglas ventana) {
        this.fam = matrizFAM;
        this.escala = escala;
        this.ventana = ventana;
        conjuntos = new trapesoidal[] {
            new trapesoidal(-2.8, -2.5, -1.8, -1.2), // NM
            new trapesoidal(-2.5, -1.2, -0.9, -0.4), // NP
            new trapesoidal(-1.8, -0.9, 0.1, 0.6),   // Z
            new trapesoidal(-1.2, -0.4, 0.6, 1.8),   // PP
            new trapesoidal( 0.4,  1.2,  1.8, 2.5)   // PM
        };
    }

    private double[] evaluar(double valor) {
        double[] grados = new double[conjuntos.length];
        for (int i = 0; i < conjuntos.length; i++) {
            grados[i] = conjuntos[i].pertenencia(valor);
        }
        return grados;
    }

    public double inferir(double entrada1, double entrada2) {
        double[] m1 = evaluar(entrada1);
        double[] m2 = evaluar(entrada2);
        double num = 0.0, den = 0.0;

        for (int i = 0; i < m1.length; i++) {
            for (int j = 0; j < m2.length; j++) {
                double w = Math.min(m1[i], m2[j]);
                String etiqueta = fam[i][j];
                double centroide = CENTROIDES.getOrDefault(etiqueta, 0.0);
                if (w > 0) {
                    ventana.mostrarRegla(String.format("Si x1 es %s y x2 es %s → y1 = %s (peso=%.2f)",
                        ETIQUETAS[i], ETIQUETAS[j], etiqueta, w));
                }
                num += w * centroide;
                den += w;
            }
        }
        return (den == 0) ? 0.0 : num / den;
    }

    public double escalar(double valorReal) {
        return valorReal * escala;
    }
}
