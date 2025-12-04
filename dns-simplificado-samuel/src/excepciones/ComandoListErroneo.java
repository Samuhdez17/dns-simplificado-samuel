package Ejercicios.E009DNS.excepciones;

public class ComandoListErroneo extends RuntimeException {
    public ComandoListErroneo(String message) {
        super(message);
    }

    public ComandoListErroneo() {
        super("400 Comando mal formado: LIST");
    }
}
