package ast.declaraciones;

import ast.NodoAST;

public abstract class Declaracion extends NodoAST {

    public Declaracion(int linea, int columna) {
        super(linea, columna);
    }
}
