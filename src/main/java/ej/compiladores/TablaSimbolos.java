package ej.compiladores;

public class TablaSimbolos {

    //Clase para los nodos de la lista en cada índice del hashmap.
    static class Entrada {
        String nombre;
        String tipo;
        String valor;
        Entrada siguiente;

        Entrada(String nombre) {
            this.nombre   = nombre;
            this.tipo     = "desconocido";
            this.valor    = "-";
            this.siguiente = null;
        }
    }

    private static final int CAPACIDAD = 11; //Número par para disminuir las colisiones.
    private final Entrada[] tabla;

    public TablaSimbolos() {
        this.tabla = new Entrada[CAPACIDAD];
    }

    private int hash(String nombre) { //Función hash para determinar el indice de cada símbolo.
        int suma = 0;
        for (char c : nombre.toCharArray()) suma += c;
        return suma % CAPACIDAD;
    }

    public void insertar(String nombre) {
        if (buscar(nombre) != null) return; //Si no lo encuentra sale del metodo.

        int indice = hash(nombre);
        Entrada nueva = new Entrada(nombre);
        nueva.siguiente = tabla[indice]; //Coloca la nueva entrada al inicio de la fila.
        tabla[indice] = nueva;
    }

    public Entrada buscar(String nombre) {
        int indice = hash(nombre);
        Entrada actual = tabla[indice];
        while (actual != null) {
            if (actual.nombre.equals(nombre)) return actual;
            actual = actual.siguiente;
        }
        return null;
    }

    /*El metodo actualizar fue creado porque primero se almacena el nombre con los demás campos vacios y luego
    cuando se encuentra la asignación se completa el tipo y valor.*/
    public void actualizar(String nombre, String tipo, String valor) {
        Entrada entrada = buscar(nombre);
        if (entrada != null) {
            entrada.tipo  = tipo;
            entrada.valor = valor;
        }
    }

    public void imprimir() {
        System.out.println("\n\nTABLA DE SIMBOLOS:");
        System.out.println("───────────────────────\n");
        System.out.printf("%-5s %-15s %-15s %-15s%n", "Slot", "Nombre", "Tipo", "Valor");
        System.out.println("-".repeat(52));
        for (int i = 0; i < CAPACIDAD; i++) {
            Entrada actual = tabla[i];
            if (actual == null) {
                System.out.printf("%-5d %-15s%n", i, "(vacío)");
            } else {
                while (actual != null) {
                    System.out.printf("%-5d %-15s %-15s %-15s%n",
                            i, actual.nombre, actual.tipo, actual.valor);
                    actual = actual.siguiente;
                }
            }
        }
    }
}