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

        String codigoFuente = codigo.toString(); //Convierte el StringBuilder a String para poder ser evaluado.

        // ── Análisis léxico ───────────────────────────────────────────────────
        TablaSimbolos tablaSimbolos = new TablaSimbolos();
        TablaErrores tablaErrores   = new TablaErrores(); //Registra los errores encontrados durante el escaneo.
        AnalizadorLexico lexer = new AnalizadorLexico(codigoFuente, tablaSimbolos, tablaErrores);
        List<ParTokenLexema> pares = lexer.escanear();

        System.out.println("\n\nTOKENS DETECTADOS:");
        System.out.println("───────────────────────\n");
        System.out.printf("%-20s %s%n", "TOKEN", "LEXEMA");
        System.out.println("-".repeat(40));
        for (ParTokenLexema par : pares) {
            System.out.println(par);
        }

        tablaSimbolos.imprimir();
        tablaErrores.imprimir();

        // ── Análisis sintáctico ───────────────────────────────────────────────
        System.out.println("\n\nANÁLISIS SINTÁCTICO:");
        System.out.println("───────────────────────────────────────────────────────────────────\n");

        if (tablaErrores.hayErrores()) {
            //Si hay errores léxicos no tiene sentido continuar con el análisis sintáctico.
            System.out.println("No se puede realizar el análisis sintáctico porque existen errores léxicos.");
        } else {
            AnalizadorSintactico parser = new AnalizadorSintactico(pares);
            NodoArbol arbol = parser.parsear();

            System.out.println("ÁRBOL DE DERIVACIÓN:");
            System.out.println("-".repeat(40));
            arbol.imprimir("");  //Imprime el árbol con indentación.

            if (parser.hayErrores()) {
                System.out.println("\nERRORES SINTÁCTICOS:");
                System.out.println("-".repeat(40));
                parser.imprimirErrores();
            } else {
                System.out.println("\nAnálisis sintáctico completado sin errores.");
            }
        }
    }
}