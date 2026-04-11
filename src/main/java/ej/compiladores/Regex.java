package ej.compiladores;

import java.util.regex.Pattern;

public class Regex {

    //Identificadores.
    public static final Pattern IDENTIFICADOR =
            Pattern.compile("[a-zA-Z][a-zA-Z0-9]{0,9}");

    //Identificador invalido porque tiene mas de 10 caracteres.
    public static final Pattern IDENTIFICADOR_LARGO =
            Pattern.compile("[a-zA-Z][a-zA-Z0-9]{10,}");

    //Numeros.
    public static final Pattern NUMERO =
            Pattern.compile("[0-9]+");

    //Cadena válida que contiene asdfg.
    public static final Pattern CADENA_VALIDA =
            Pattern.compile("\"[^\"]*asdfg[^\"]*\"");

    //Cadena inválida porque no tiene asdfg.
    public static final Pattern CADENA_INVALIDA =
            Pattern.compile("\"[^\"]*\"");

    //Operadores aritmeticos.
    public static final Pattern OP_ARITMETICO =
            Pattern.compile("[+\\-*/]");

    //Operador de asignacion.
    public static final Pattern OP_ASIGNACION =
            Pattern.compile(":=");

    //Operadores relacionales.
    public static final Pattern OP_RELACIONAL =
            Pattern.compile(">=|<=|<>|>|<|=");

    //Simbolo de dos puntos.
    public static final Pattern PUNTO_PUNTO =
            Pattern.compile("\\.\\.");

    //Simbolos simples.
    public static final Pattern SIMBOLO =
            Pattern.compile("[{}\\[\\](),;]");

    //Solo letras asdfg.
    public static final Pattern SOLO_LETRAS_ASDFG =
            Pattern.compile("[asdfg]+");

    //Este metodo verifica la coincidencia de las regex con los lexemas.
    public static boolean coincide(Pattern patron, String lexema) {
        return patron.matcher(lexema).matches();
    }
}