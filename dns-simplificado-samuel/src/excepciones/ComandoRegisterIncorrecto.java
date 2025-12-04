package excepciones;

public class ComandoRegisterIncorrecto extends RuntimeException {
    public ComandoRegisterIncorrecto(String message) {
        super(message);
    }

    public  ComandoRegisterIncorrecto() {
        super("400 Comando mal formado: REGISTER <dominio> <tipo> -> (A, MX, CNAME) <valor>");
    }
}
