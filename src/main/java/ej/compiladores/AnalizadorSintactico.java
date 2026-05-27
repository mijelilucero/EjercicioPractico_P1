package ej.compiladores;

import java.util.ArrayList;
import java.util.List;

public class AnalizadorSintactico {

    private final List<ParTokenLexema> tokens;
    private int pos;
    private final List<String> errores;

    public AnalizadorSintactico(List<ParTokenLexema> tokens) {
        this.tokens  = tokens;
        this.pos     = 0;
        this.errores = new ArrayList<>();
    }

    //Helpers para navegar en la lista de tokens.
    private ParTokenLexema actual()   { return pos < tokens.size() ? tokens.get(pos) : new ParTokenLexema("EOF", "$"); }
    private String tipoActual()       { return actual().token; }
    private String lexemaActual()     { return actual().lexema; }
    private ParTokenLexema consumir() { return tokens.get(pos++); }
    private boolean fin()             { return pos >= tokens.size(); }

    //Verifica si el token actual coincide con tipo y lexema esperados.
    private boolean esToken(String tipo, String lexema) {
        return tipoActual().equals(tipo) && lexemaActual().equals(lexema);
    }

    //Verifica si el token actual coincide solo con el tipo.
    private boolean esTipo(String tipo) {
        return tipoActual().equals(tipo);
    }

    //Registra un error sintáctico indicando qué se esperaba y qué se encontró.
    private NodoArbol error(String esperado) {
        String msg = "Error sintáctico: se esperaba " + esperado +
                " pero se encontró '" + lexemaActual() + "' (" + tipoActual() + ")";
        errores.add(msg);
        if (!fin()) consumir(); //Recuperación de pánico: avanza para no quedar atascado.
        return new NodoArbol("ERROR: " + msg, true);
    }

    public boolean hayErrores()   { return !errores.isEmpty(); }

    public void imprimirErrores() {
        for (String e : errores) {
            System.out.println("  " + e);
        }
    }

    //Inicia el análisis desde la regla raíz <programa>.
    public NodoArbol parsear() {
        NodoArbol raiz = parsearPrograma();
        if (!fin()) error("fin de programa");
        return raiz;
    }

    //Los metodos siguientes implementan cada regla de la BNF como un metodo recursivo:

    //<programa> ::= <sentencia> | <sentencia> <programa>
    private NodoArbol parsearPrograma() {
        NodoArbol nodo = new NodoArbol("<programa>", false);

        while (!fin()) {
            if (esToken("SIMBOLO", "}")) break; //Fin de bloque, detiene el parseo del programa actual.
            nodo.agregarHijo(parsearSentencia());
        }
        return nodo;
    }

    //<sentencia> ::= <declaracion> | <asignacion> | <sentencia-if> | <sentencia-for> | <sentencia-print>
    private NodoArbol parsearSentencia() {
        NodoArbol nodo = new NodoArbol("<sentencia>", false);

        if (esToken("PALABRA_RESERVADA", "int")) {
            nodo.agregarHijo(parsearDeclaracion());

        } else if (esToken("PALABRA_RESERVADA", "if")) {
            nodo.agregarHijo(parsearIf());

        } else if (esToken("PALABRA_RESERVADA", "for")) {
            nodo.agregarHijo(parsearFor());

        } else if (esToken("PALABRA_RESERVADA", "print")) {
            nodo.agregarHijo(parsearPrint());

        } else if (esTipo("IDENTIFICADOR")) {
            nodo.agregarHijo(parsearAsignacion());

        } else {
            nodo.agregarHijo(error("inicio de sentencia (int, if, for, print o identificador)"));
        }
        return nodo;
    }

    //<declaracion> ::= int <identificador> ; | int <identificador> := <valor> ;
    private NodoArbol parsearDeclaracion() {
        NodoArbol nodo = new NodoArbol("<declaracion>", false);

        nodo.agregarHijo(new NodoArbol(consumir().lexema, true)); //Consume 'int'.

        if (esTipo("IDENTIFICADOR")) {
            nodo.agregarHijo(parsearIdentificador());
        } else {
            nodo.agregarHijo(error("identificador después de 'int'"));
            return nodo;
        }

        if (esTipo("OP_ASIGNACION")) { //La asignación en una declaración es opcional.
            nodo.agregarHijo(new NodoArbol(consumir().lexema, true)); // :=
            nodo.agregarHijo(parsearValor());
        }

        if (esToken("SIMBOLO", ";")) {
            nodo.agregarHijo(new NodoArbol(consumir().lexema, true));
        } else {
            nodo.agregarHijo(error("';' al final de la declaración"));
        }
        return nodo;
    }

    //<asignacion> ::= <identificador> := <valor> ;
    private NodoArbol parsearAsignacion() {
        NodoArbol nodo = new NodoArbol("<asignacion>", false);

        nodo.agregarHijo(parsearIdentificador());

        if (esTipo("OP_ASIGNACION")) {
            nodo.agregarHijo(new NodoArbol(consumir().lexema, true)); // :=
        } else {
            nodo.agregarHijo(error("':=' en asignación"));
            return nodo;
        }

        nodo.agregarHijo(parsearValor());

        if (esToken("SIMBOLO", ";")) {
            nodo.agregarHijo(new NodoArbol(consumir().lexema, true));
        } else {
            nodo.agregarHijo(error("';' al final de la asignación"));
        }
        return nodo;
    }

    //<sentencia-if> ::= if ( <condicion> ) { <programa> } | if ( <condicion> ) { <programa> } else { <programa> }
    private NodoArbol parsearIf() {
        NodoArbol nodo = new NodoArbol("<sentencia-if>", false);

        nodo.agregarHijo(new NodoArbol(consumir().lexema, true)); //Consume 'if'.

        if (esToken("SIMBOLO", "(")) {
            nodo.agregarHijo(new NodoArbol(consumir().lexema, true));
        } else {
            nodo.agregarHijo(error("'(' después de 'if'"));
        }

        nodo.agregarHijo(parsearCondicion());

        if (esToken("SIMBOLO", ")")) {
            nodo.agregarHijo(new NodoArbol(consumir().lexema, true));
        } else {
            nodo.agregarHijo(error("')' después de condición"));
        }

        if (esToken("SIMBOLO", "{")) {
            nodo.agregarHijo(new NodoArbol(consumir().lexema, true));
        } else {
            nodo.agregarHijo(error("'{' para abrir bloque if"));
        }

        nodo.agregarHijo(parsearPrograma()); //Bloque then.

        if (esToken("SIMBOLO", "}")) {
            nodo.agregarHijo(new NodoArbol(consumir().lexema, true));
        } else {
            nodo.agregarHijo(error("'}' para cerrar bloque if"));
        }

        if (esToken("PALABRA_RESERVADA", "else")) { //El bloque else es opcional.
            nodo.agregarHijo(new NodoArbol(consumir().lexema, true)); //Consume 'else'.

            if (esToken("SIMBOLO", "{")) {
                nodo.agregarHijo(new NodoArbol(consumir().lexema, true));
            } else {
                nodo.agregarHijo(error("'{' para abrir bloque else"));
            }

            nodo.agregarHijo(parsearPrograma()); //Bloque else.

            if (esToken("SIMBOLO", "}")) {
                nodo.agregarHijo(new NodoArbol(consumir().lexema, true));
            } else {
                nodo.agregarHijo(error("'}' para cerrar bloque else"));
            }
        }
        return nodo;
    }

    //<sentencia-for> ::= for ( <asignacion> <condicion> ; <asignacion> ) { <programa> }
    private NodoArbol parsearFor() {
        NodoArbol nodo = new NodoArbol("<sentencia-for>", false);

        nodo.agregarHijo(new NodoArbol(consumir().lexema, true)); //Consume 'for'.

        if (esToken("SIMBOLO", "(")) {
            nodo.agregarHijo(new NodoArbol(consumir().lexema, true));
        } else {
            nodo.agregarHijo(error("'(' después de 'for'"));
        }

        nodo.agregarHijo(parsearAsignacion());  //Inicialización.
        nodo.agregarHijo(parsearCondicion());   //Condición de parada.

        if (esToken("SIMBOLO", ";")) {
            nodo.agregarHijo(new NodoArbol(consumir().lexema, true));
        } else {
            nodo.agregarHijo(error("';' después de condición en for"));
        }

        nodo.agregarHijo(parsearAsignacion());  //Actualización.

        if (esToken("SIMBOLO", ")")) {
            nodo.agregarHijo(new NodoArbol(consumir().lexema, true));
        } else {
            nodo.agregarHijo(error("')' para cerrar encabezado for"));
        }

        if (esToken("SIMBOLO", "{")) {
            nodo.agregarHijo(new NodoArbol(consumir().lexema, true));
        } else {
            nodo.agregarHijo(error("'{' para abrir bloque for"));
        }

        nodo.agregarHijo(parsearPrograma()); //Cuerpo del for.

        if (esToken("SIMBOLO", "}")) {
            nodo.agregarHijo(new NodoArbol(consumir().lexema, true));
        } else {
            nodo.agregarHijo(error("'}' para cerrar bloque for"));
        }
        return nodo;
    }

    //<sentencia-print> ::= print ( <valor> ) ;
    private NodoArbol parsearPrint() {
        NodoArbol nodo = new NodoArbol("<sentencia-print>", false);

        nodo.agregarHijo(new NodoArbol(consumir().lexema, true)); //Consume 'print'.

        if (esToken("SIMBOLO", "(")) {
            nodo.agregarHijo(new NodoArbol(consumir().lexema, true));
        } else {
            nodo.agregarHijo(error("'(' después de 'print'"));
        }

        nodo.agregarHijo(parsearValor());

        if (esToken("SIMBOLO", ")")) {
            nodo.agregarHijo(new NodoArbol(consumir().lexema, true));
        } else {
            nodo.agregarHijo(error("')' después del valor en print"));
        }

        if (esToken("SIMBOLO", ";")) {
            nodo.agregarHijo(new NodoArbol(consumir().lexema, true));
        } else {
            nodo.agregarHijo(error("';' al final de print"));
        }
        return nodo;
    }

    //<condicion> ::= <expresion> <op-relacional> <expresion>
    private NodoArbol parsearCondicion() {
        NodoArbol nodo = new NodoArbol("<condicion>", false);

        nodo.agregarHijo(parsearExpresion());

        if (esTipo("OP_RELACIONAL")) {
            nodo.agregarHijo(new NodoArbol(consumir().lexema, true));
        } else {
            nodo.agregarHijo(error("operador relacional en condición"));
        }

        nodo.agregarHijo(parsearExpresion());
        return nodo;
    }

    //<expresion> ::= <termino> | <termino> <op-aritmetico> <expresion>
    private NodoArbol parsearExpresion() {
        NodoArbol nodo = new NodoArbol("<expresion>", false);

        nodo.agregarHijo(parsearTermino());

        if (esTipo("OP_ARITMETICO")) { //Si continúa con operador aritmético se expande recursivamente.
            nodo.agregarHijo(new NodoArbol(consumir().lexema, true));
            nodo.agregarHijo(parsearExpresion());
        }
        return nodo;
    }

    //<termino> ::= <numero> | <identificador> | ( <expresion> )
    private NodoArbol parsearTermino() {
        NodoArbol nodo = new NodoArbol("<termino>", false);

        if (esTipo("NUMERO_ENTERO")) {
            nodo.agregarHijo(new NodoArbol(consumir().lexema, true));

        } else if (esTipo("IDENTIFICADOR")) {
            nodo.agregarHijo(new NodoArbol(consumir().lexema, true));

        } else if (esToken("SIMBOLO", "(")) {
            nodo.agregarHijo(new NodoArbol(consumir().lexema, true)); // (
            nodo.agregarHijo(parsearExpresion());
            if (esToken("SIMBOLO", ")")) {
                nodo.agregarHijo(new NodoArbol(consumir().lexema, true)); // )
            } else {
                nodo.agregarHijo(error("')' para cerrar expresión entre paréntesis"));
            }
        } else {
            nodo.agregarHijo(error("número, identificador o '(' en término"));
        }
        return nodo;
    }

    //<valor> ::= <numero> | <cadena> | <expresion>
    private NodoArbol parsearValor() {
        NodoArbol nodo = new NodoArbol("<valor>", false);

        if (esTipo("CADENA")) {
            nodo.agregarHijo(new NodoArbol(consumir().lexema, true)); //Las cadenas se guardan directamente como terminal.

        } else if (esTipo("NUMERO_ENTERO") || esTipo("IDENTIFICADOR")) {
            nodo.agregarHijo(parsearExpresion()); //Números e identificadores se expanden como expresión.

        } else {
            nodo.agregarHijo(error("número, cadena o identificador como valor"));
        }
        return nodo;
    }

    //<identificador> ::= token de tipo IDENTIFICADOR
    private NodoArbol parsearIdentificador() {
        NodoArbol nodo = new NodoArbol("<identificador>", false);
        if (esTipo("IDENTIFICADOR")) {
            nodo.agregarHijo(new NodoArbol(consumir().lexema, true));
        } else {
            nodo.agregarHijo(error("identificador"));
        }
        return nodo;
    }
}