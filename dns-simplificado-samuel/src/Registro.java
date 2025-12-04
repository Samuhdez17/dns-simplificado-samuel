package Ejercicios.E009DNS;

public class Registro {
    private final String dominio;
    private final String tipo;
    private final String valor;

    public Registro(String[] registro) {
        this.dominio = registro[0];
        this.tipo = registro[1];
        this.valor = registro[2];
    }

    public String getDominio() {
        return dominio;
    }

    public String getTipo() {
        return tipo;
    }

    public String getValor() {
        return valor;
    }

    @Override
    public String toString() {
        return dominio + " " + tipo + " " + valor;
    }
}
