package Ejercicios.E009DNS.excepciones;

public class ComandoIncorrectoEx extends RuntimeException {
    public ComandoIncorrectoEx() {
        super("Error 400. Peticion erronea");
    }

    public ComandoIncorrectoEx(Exception e) {
        super("Error 400." + e);
    }
}
