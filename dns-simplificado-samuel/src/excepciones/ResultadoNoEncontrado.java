package Ejercicios.E009DNS.excepciones;

public class ResultadoNoEncontrado extends RuntimeException {
    public ResultadoNoEncontrado() {
        super("Error 404. Dominio no encontrado");
    }
}
