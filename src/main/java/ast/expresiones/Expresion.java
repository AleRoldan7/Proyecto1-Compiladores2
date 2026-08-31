package ast.expresiones;

import ast.NodoAST;

public abstract class Expresion  extends NodoAST {


    public Expresion(int linea, int columna) {
        super(linea, columna);
    }
}
