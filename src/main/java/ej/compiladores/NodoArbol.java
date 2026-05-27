package ej.compiladores;

import java.util.ArrayList;
import java.util.List;

public class NodoArbol {
    public String etiqueta;
    public boolean esTerminal;
    public List<NodoArbol> hijos;  //Hijos del nodo en el árbol.

    public NodoArbol(String etiqueta, boolean esTerminal) {
        this.etiqueta   = etiqueta;
        this.esTerminal = esTerminal;
        this.hijos      = new ArrayList<>();
    }

    //Añade un hijo a este nodo.
    public void agregarHijo(NodoArbol hijo) {
        hijos.add(hijo);
    }

    //Imprime el árbol con indentación para simular la estructura jerárquica.
    public void imprimir(String prefijo) {
        String tipo = esTerminal ? "[T]  " : "[NT] ";
        System.out.println(prefijo + tipo + etiqueta);
        for (NodoArbol hijo : hijos) {
            hijo.imprimir(prefijo + "    ");
        }
    }
}
