package ast.sentencias;

import ast.NodoAST;

public abstract class Sentencia extends NodoAST {

    public Sentencia(int linea, int columna) {
        super(linea, columna);
    }
}
