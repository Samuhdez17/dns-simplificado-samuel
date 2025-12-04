package excepciones;

public class ComandoIncorrectoEx extends RuntimeException {
    public ComandoIncorrectoEx() {
        super("400 Peticion erronea, usa comando help para ver la ayuda");
    }

    public ComandoIncorrectoEx(Exception e) {
        super("400 " + e);
    }
}
