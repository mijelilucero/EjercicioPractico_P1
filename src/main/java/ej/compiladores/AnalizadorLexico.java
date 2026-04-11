package ej.compiladores;

import java.util.*;

public class AnalizadorLexico {

    private static final String PALABRA_RESERVADA = "PALABRA_RESERVADA";
    private static final String IDENTIFICADOR     = "IDENTIFICADOR";
    private static final String NUMERO_ENTERO     = "NUMERO_ENTERO";
    private static final String CADENA            = "CADENA";
    private static final String OP_ARITMETICO     = "OP_ARITMETICO";
    private static final String OP_ASIGNACION     = "OP_ASIGNACION";
    private static final String OP_RELACIONAL     = "OP_RELACIONAL";
    private static final String SIMBOLO           = "SIMBOLO";
    private static final String ERROR             = "ERROR";

    //En este apartado se establecen las palabras fijas que acepta el analizador léxico.
    private static final Set<String> PALABRAS_FIJAS = new HashSet<>(
            Arrays.asList("if", "else", "for", "print", "int")
    );

    private static final Set<Character> LETRAS_ASDFG = new HashSet<>(
            Arrays.asList('a', 's', 'd', 'f', 'g')
    );

    private static boolean esCombinacionAsdfg(String lexema) {
        if (lexema.isEmpty() || lexema.length() > 5) return false; //No acepta combinaciones con letras repetidas.

        Set<Character> vistas = new HashSet<>(); //Set para evitar duplicados.
        for (char c : lexema.toCharArray()) {
            if (!LETRAS_ASDFG.contains(c)) return false; //Si contiene alguna letra incorrecta.
            if (!vistas.add(c))            return false; // Si contiene alguna letra repetida.
        }
        return true;
    }

    private final String codigoFuente;
    private int pos;
    private final List<ParTokenLexema> pares;
    private final TablaSimbolos tablaSimbolos;
    private final TablaErrores tablaErrores; //Referencia a la tabla de errores.

    //Variables que permiten el almacenamiento de las asignaciones en la tabla de simbolos.
    private String ultimoIdentificador = null;
    private boolean esperandoValor     = false;

    public AnalizadorLexico(String codigoFuente, TablaSimbolos tablaSimbolos, TablaErrores tablaErrores) {
        this.codigoFuente  = codigoFuente;
        this.pos           = 0;
        this.pares         = new ArrayList<>();
        this.tablaSimbolos = tablaSimbolos;
        this.tablaErrores  = tablaErrores;
    }

    //Helpers para navegar en el texto.
    private char actual()    { return codigoFuente.charAt(pos); }
    private char siguiente() { return (pos + 1 < codigoFuente.length()) ? codigoFuente.charAt(pos + 1) : '\0'; }
    private char consumir()  { return codigoFuente.charAt(pos++); }
    private boolean fin()    { return pos >= codigoFuente.length(); }

    /*El metodo guardar permite guardar el token recien encontrado en la lista de pares Token-lexema y en caso de
    que sea identificador tambien actualiza la tabla de simbolos*/
    private void guardar(String token, String lexema) {
        pares.add(new ParTokenLexema(token, lexema));

        if (token.equals(IDENTIFICADOR)) {
            tablaSimbolos.insertar(lexema);
            ultimoIdentificador = lexema; //Mantiene el ultimo identificador leido para asignarle valor.
            esperandoValor      = false;
            return;
        }

        if (token.equals(OP_ASIGNACION)) { //Si encuentra un := espera el valor para almacenarlo en la tabla de simbolos.
            esperandoValor = (ultimoIdentificador != null);
            return;
        }

        //Valida que tipo de valor se recibio para determinar su tipo.
        if (esperandoValor && ultimoIdentificador != null) {
            if (token.equals(NUMERO_ENTERO)) {
                tablaSimbolos.actualizar(ultimoIdentificador, "int", lexema);
                esperandoValor      = false;
                ultimoIdentificador = null;
                return;
            }
            if (token.equals(CADENA)) {
                tablaSimbolos.actualizar(ultimoIdentificador, "string", lexema);
                esperandoValor      = false;
                ultimoIdentificador = null;
                return;
            }

            //Se cancela la espera si el token no es un valor valido para asignacion.
            if (!token.equals(SIMBOLO) && !token.equals(OP_ARITMETICO)) {
                esperandoValor      = false;
                ultimoIdentificador = null;
            }
        }

        //Limpia el ultimoidentificador si no se encuentra asignacion.
        if (!token.equals(NUMERO_ENTERO) && !token.equals(CADENA) && !token.equals(OP_ASIGNACION)) {
            ultimoIdentificador = null;
        }
    }

    //El metodo guardarError registra el error en la lista de pares y en la tabla de errores simultaneamente.
    private void guardarError(TablaErrores.TipoError tipo, String lexema, String descripcion) {
        pares.add(new ParTokenLexema(ERROR, lexema));
        tablaErrores.registrar(tipo, lexema, descripcion);
        ultimoIdentificador = null; //Cualquier error cancela la espera de valor.
        esperandoValor      = false;
    }

    public List<ParTokenLexema> escanear() {
        while (!fin()) {
            char c = actual();

            if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                consumir(); //Se ignoran.
            } else if (Character.isLetter(c)) {
                leerPalabraOIdentificador();
            } else if (Character.isDigit(c)) {
                leerNumero();
            } else if (c == '"') {
                leerCadena();
            } else if (c == ':') {
                leerAsignacion();
            } else if (c == '<') {
                leerMenor();
            } else if (c == '>') {
                leerMayor();
            } else if (c == '.') {
                leerPunto();
            } else if (c == '+' || c == '-' || c == '*' || c == '/') {
                String lexema = String.valueOf(consumir());
                if (Regex.coincide(Regex.OP_ARITMETICO, lexema)) {
                    guardar(OP_ARITMETICO, lexema);
                }
            } else if (c == '=') {
                String lexema = String.valueOf(consumir());
                if (Regex.coincide(Regex.OP_RELACIONAL, lexema)) {
                    guardar(OP_RELACIONAL, lexema);
                }
            } else if (c == '{' || c == '}' || c == '[' || c == ']' ||
                    c == '(' || c == ')' || c == ',' || c == ';') {
                String lexema = String.valueOf(consumir());
                if (Regex.coincide(Regex.SIMBOLO, lexema)) {
                    guardar(SIMBOLO, lexema);
                }
            } else {
                //Cualquier caracter fuera del alfabeto se registra como error.
                guardarError(
                        TablaErrores.TipoError.CARACTER_NO_RECONOCIDO,
                        String.valueOf(consumir()),
                        "El carácter no pertenece al alfabeto permitido"
                );
            }
        }
        return pares;
    }

    //Los metodos siguientes permiten realizar las validaciones correspondientes al token leido.

    private void leerPalabraOIdentificador() {
        StringBuilder acumulado = new StringBuilder();
        while (!fin() && Character.isLetterOrDigit(actual())) {
            acumulado.append(consumir());
        }
        String lexema = acumulado.toString();

        if (lexema.length() > 10) { //Si tiene longitud mayor a 10 le asigna ERROR.
            guardarError(
                    TablaErrores.TipoError.IDENTIFICADOR_LARGO,
                    lexema,
                    "Supera los 10 caracteres permitidos"
            );
            return;
        }

        String lexemaLower = lexema.toLowerCase();

        //Condicionales para saber si es palabra reservada.
        if (PALABRAS_FIJAS.contains(lexemaLower)) {
            guardar(PALABRA_RESERVADA, lexemaLower);
            return;
        }
        if (esCombinacionAsdfg(lexemaLower)) {
            guardar(PALABRA_RESERVADA, lexemaLower);
            return;
        }

        //Validar con regex antes de guardar como identificador.
        if (Regex.coincide(Regex.IDENTIFICADOR, lexema)) {
            guardar(IDENTIFICADOR, lexema); //Si no es palabra reservada lo guarda como identificador.
        } else {
            guardarError(
                    TablaErrores.TipoError.CARACTER_NO_RECONOCIDO,
                    lexema,
                    "'" + lexema + "' no es un identificador válido"
            );
        }
    }

    private void leerNumero() {
        StringBuilder acumulado = new StringBuilder();
        while (!fin() && Character.isDigit(actual())) {
            acumulado.append(consumir());
        }
        String lexema = acumulado.toString();

        //Validar formato numérico con regex antes de verificar el rango.
        if (!Regex.coincide(Regex.NUMERO, lexema)) {
            guardarError(
                    TablaErrores.TipoError.NUMERO_FUERA_RANGO,
                    lexema,
                    "'" + lexema + "' no tiene formato numérico válido"
            );
            return;
        }

        if (Integer.parseInt(lexema) > 100) { //Validación para saber si está dentro del rango deseado.
            guardarError(
                    TablaErrores.TipoError.NUMERO_FUERA_RANGO,
                    lexema,
                    "El valor " + lexema + " supera el máximo permitido"
            );
        } else {
            guardar(NUMERO_ENTERO, lexema);
        }
    }

    private void leerCadena() {
        StringBuilder acumulado = new StringBuilder();
        acumulado.append(consumir()); // "
        while (!fin() && actual() != '"') {
            acumulado.append(consumir());
        }
        if (fin()) {
            guardarError(
                    TablaErrores.TipoError.CADENA_SIN_CERRAR,
                    acumulado.toString(),
                    "La cadena no tiene comilla de cierre"
            ); //Si la cadena no finaliza con comillas.
            return;
        }
        acumulado.append(consumir()); // "
        String lexema    = acumulado.toString();
        String contenido = lexema.substring(1, lexema.length() - 1);

        //Lo guarda como cadena válida solo si tiene asdfg, validando con regex.
        if (Regex.coincide(Regex.CADENA_VALIDA, lexema)) {
            guardar(CADENA, lexema);
        } else if (Regex.coincide(Regex.CADENA_INVALIDA, lexema)) {
            guardarError(
                    TablaErrores.TipoError.CADENA_SIN_ASDFG,
                    lexema,
                    "La cadena no contiene la secuencia 'asdfg'"
            );
        }
    }

    //Lookahead para validar operador de asignacion.
    private void leerAsignacion() {
        StringBuilder acumulado = new StringBuilder();
        acumulado.append(consumir());

        if (!fin() && actual() == '=') {
            acumulado.append(consumir());
            guardar(OP_ASIGNACION, acumulado.toString());
        } else {
            guardarError(
                    TablaErrores.TipoError.ASIGNACION_INVALIDA,
                    acumulado.toString(),
                    "Se encontró ':' solo — se esperaba ':='"
            );
        }
    }

    //Lookahead para validar operadores relacionales que inician con <.
    private void leerMenor() {
        StringBuilder acumulado = new StringBuilder();
        acumulado.append(consumir());
        if (!fin() && actual() == '=') {
            acumulado.append(consumir());
            guardar(OP_RELACIONAL, acumulado.toString());
        } else if (!fin() && actual() == '>') {
            acumulado.append(consumir());
            guardar(OP_RELACIONAL, acumulado.toString());
        } else {
            guardar(OP_RELACIONAL, acumulado.toString());
        }
    }

    //Lookahead para validar operadores relacionales que inician con >.
    private void leerMayor() {
        StringBuilder acumulado = new StringBuilder();
        acumulado.append(consumir());
        if (!fin() && actual() == '=') {
            acumulado.append(consumir());
            guardar(OP_RELACIONAL, acumulado.toString());
        } else {
            guardar(OP_RELACIONAL, acumulado.toString());
        }
    }

    //Lookahead para validar que el operador .. este completo.
    private void leerPunto() {
        StringBuilder acumulado = new StringBuilder();
        acumulado.append(consumir());
        if (!fin() && actual() == '.') {
            acumulado.append(consumir());
            guardar(SIMBOLO, acumulado.toString());
        } else {
            guardarError(
                    TablaErrores.TipoError.PUNTO_INVALIDO,
                    acumulado.toString(),
                    "Se encontró '.' solo — se esperaba '..'"
            );
        }
    }
}