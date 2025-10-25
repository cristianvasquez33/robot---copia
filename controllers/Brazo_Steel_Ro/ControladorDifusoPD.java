import java.util.*;
public class ControladorDifusoPD {
    private trapesoidal[] conjuntosError;
    private trapesoidal[] conjuntosDerivada;
    private trapesoidal[] conjuntosSalida;
    private String[][] fam;
    private double escala;
    private VentanaReglas ventana;
    public ControladorDifusoPD(String[][] fam, double escala, VentanaReglas ventana) {
        this.fam = fam;
        this.escala = escala;
        this.ventana = ventana;
        // Funciones de membresía para error
        conjuntosError = new trapesoidal[] {
            new trapesoidal(-3.2, -2.8, -2.0, -1.4), // NM
            new trapesoidal(-2.5, -1.8, -1.0, -0.3), // NP
            new trapesoidal(-1.2, -0.4,  0.4,  1.2), // Z
            new trapesoidal( 0.3,  1.0,  1.8,  2.5), // PP
            new trapesoidal( 1.4,  2.0,  2.8,  3.2)  // PM
        };
        // Funciones de membresía para derivada
        conjuntosDerivada = new trapesoidal[] {
            new trapesoidal(-2.5, -2.0, -1.5, -1.0), // NM
            new trapesoidal(-1.2, -0.8, -0.3,  0.0), // NP
            new trapesoidal(-0.4, -0.1,  0.1,  0.4), // Z
            new trapesoidal( 0.0,  0.3,  0.8,  1.2), // PP
            new trapesoidal( 1.0,  1.5,  2.0,  2.5)  // PM
        };
        // Funciones de membresía para salida
        conjuntosSalida = new trapesoidal[] {
            new trapesoidal(-2.5, -2.0, -1.5, -1.0), // NB
            new trapesoidal(-1.2, -0.8, -0.3,  0.0), // NS
            new trapesoidal(-0.4, -0.1,  0.1,  0.4), // Z
            new trapesoidal( 0.0,  0.3,  0.8,  1.2), // PS
            new trapesoidal( 1.0,  1.5,  2.0,  2.5)  // PB
        };
    }
    public double escalar(double valor) {
        return escala * valor;
    }
    public double inferir(double error, double derivada) {
        double[] pertenenciaError = new double[5];
        double[] pertenenciaDerivada = new double[5];
        double[] salida = new double[5];
        // Calcula grados de pertenencia
        for (int i = 0; i < 5; i++) {
            pertenenciaError[i] = conjuntosError[i].pertenencia(error);
            pertenenciaDerivada[i] = conjuntosDerivada[i].pertenencia(derivada);
        }
        // Aplica reglas FAM
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                double activacion = Math.min(pertenenciaError[i], pertenenciaDerivada[j]);
                if (activacion > 0) {
                    String etiqueta = fam[i][j];
                    // ventana.activar(etiqueta); // Comentado: método no existe en VentanaReglas
                    int indice = switch (etiqueta) {
                        case "NB" -> 0;
                        case "NS" -> 1;
                        case "Z"  -> 2;
                        case "PS" -> 3;
                        default   -> 4; // PB
                    };
                    salida[indice] = Math.max(salida[indice], activacion);
                }
            }
        }
        // Defuzzificación por centroide
        double[] centros = {-2.0, -1.0, 0.0, 1.0, 2.0}; // NB, NS, Z, PS, PB
        double numerador = 0, denominador = 0;
        for (int i = 0; i < 5; i++) {
            numerador += salida[i] * centros[i];
            denominador += salida[i];
        }
        double salidaFinal = (denominador == 0) ? 0 : numerador / denominador;
        return Math.max(-2.0, Math.min(2.0, salidaFinal)); // límite suave
    }
}
