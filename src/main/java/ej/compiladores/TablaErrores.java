package ej.compiladores;

import java.util.ArrayList;
import java.util.List;

public class TablaErrores {

    public enum TipoError { //Todos los tipos de errores posibles.
        IDENTIFICADOR_LARGO,
        NUMERO_FUERA_RANGO,
        CADENA_SIN_ASDFG,
        CADENA_SIN_CERRAR,
        ASIGNACION_INVALIDA,
        PUNTO_INVALIDO,
        CARACTER_NO_RECONOCIDO
    }

    private static class EntradaError { //Formato de entrada a la tabla de errores.
        int numero;
        TipoError tipo;
        String lexema;
        String descripcion;

        EntradaError(int numero, TipoError tipo, String lexema, String descripcion) {
            this.numero      = numero;
            this.tipo        = tipo;
            this.lexema      = lexema;
            this.descripcion = descripcion;
        }

        @Override
        public String toString() {
            return String.format("%-5d %-30s %-25s %s",
                    numero, tipo, lexema, descripcion);
        }
    }

    private final List<EntradaError> errores = new ArrayList<>();
    private int contador = 1; //Para poder añadirle el numero de error a la tabla.

    public void registrar(TipoError tipo, String lexema, String descripcion) {
        errores.add(new EntradaError(contador++, tipo, lexema, descripcion));
    }

    public boolean hayErrores() {
        return !errores.isEmpty();
    }

    public void imprimir() {
        System.out.println("\n\nTABLA DE ERRORES:");
        System.out.println("───────────────────────\n");
        if (!hayErrores()) {
            System.out.println("No se encontraron errores.");
            return;
        }
        System.out.printf("%-5s %-30s %-25s %s%n", "Num", "Tipo", "Lexema", "Descripción");
        System.out.println("-".repeat(90));
        for (EntradaError e : errores) {
            System.out.println(e);
        }
    }
}
