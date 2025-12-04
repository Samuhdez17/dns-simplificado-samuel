package Ejercicios.E009DNS.excepciones;

public class ComandoLookupErroneo extends RuntimeException {
    public ComandoLookupErroneo(String message) {
        super(message);
    }

    public ComandoLookupErroneo() {
        super("400 Comando mal formado: LOOKUP <tipo> <dominio>");
    }
}
