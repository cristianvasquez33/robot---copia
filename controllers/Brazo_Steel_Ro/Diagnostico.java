public class Diagnostico {
    public double deseado;
    public double sensadoPresente;
    public double sensadoPasado;
    public double errorPresente;
    public double errorPasado;
    public double derivada;

    public Diagnostico(double deseadoInicial) {
        this.deseado = deseadoInicial;
        this.sensadoPresente = deseadoInicial;
        this.sensadoPasado = deseadoInicial;
    }

    public void actualizar(double nuevoSensado) {
        sensadoPasado = sensadoPresente;
        errorPasado = errorPresente;
        sensadoPresente = nuevoSensado;
        errorPresente = deseado - sensadoPresente;
        derivada = sensadoPresente - sensadoPasado;
    }

    public void imprimir(String nombre) {
        System.out.printf("[%s] Deseado=%.2f Sensado=%.2f Error=%.2f Derivada=%.2f\n",
                          nombre, deseado, sensadoPresente, errorPresente, derivada);
    }
}