package ej.compiladores;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        StringBuilder codigo = new StringBuilder();

        System.out.println("\nANALIZADOR LEXICO");
        System.out.println("Ingrese el código fuente (escribe 'FIN' para terminar el ingreso):");
        System.out.println("───────────────────────────────────────────────────────────────────\n");

        String linea;
        while (scanner.hasNextLine()) { //Lectura de caracter por caracter para construir el StringBuilder.
            linea = scanner.nextLine();
            if (linea.equals("FIN")) break;
            codigo.append(linea).append("\n");
        }

        scanner.close();

        String codigoFuente = codigo.toString(); //Convierte el StringBuilser a String para poder ser evaluado.

        TablaSimbolos tablaSimbolos = new TablaSimbolos();
        AnalizadorLexico lexer = new AnalizadorLexico(codigoFuente, tablaSimbolos);
        List<ParTokenLexema> pares = lexer.escanear();

        System.out.println("\n\nTOKENS DETECTADOS:");
        System.out.println("───────────────────────\n");
        System.out.printf("%-20s %s%n", "TOKEN", "LEXEMA");
        System.out.println("-".repeat(40));
        for (ParTokenLexema par : pares) {
            System.out.println(par);
        }

        tablaSimbolos.imprimir();
    }
}