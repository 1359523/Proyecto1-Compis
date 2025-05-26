package proyecto;

public class Variable {
    private String tipo;
    private String valor;

    public Variable(String tipo, String valor) {
        this.tipo = tipo;
        this.valor = valor;
    }

    public String getTipo() {
        return tipo;
    }

    public String getValor() {
        return valor;
    }
}