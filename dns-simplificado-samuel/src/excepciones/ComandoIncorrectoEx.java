package Ejercicios.E009DNS.excepciones;

public class ComandoIncorrectoEx extends RuntimeException {
    public ComandoIncorrectoEx() {
        super("400 Peticion erronea");
    }

    public ComandoIncorrectoEx(Exception e) {
        super("400 " + e);
    }
}
