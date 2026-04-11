package ej.compiladores;

public class ParTokenLexema {
    public String token;
    public String lexema;

    public ParTokenLexema(String token, String lexema) {
        this.token = token;
        this.lexema = lexema;
    }

    @Override //Sobrescribe el metodo .toString para mejorar la visualización de los tokens.
    public String toString() {
        return String.format("%-20s %s", token, lexema);
    }
}
