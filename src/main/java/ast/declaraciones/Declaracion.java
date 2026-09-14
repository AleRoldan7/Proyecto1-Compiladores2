package ast.declaraciones;

import ast.NodoAST;
import ast.sentencias.Sentencia;

public abstract class Declaracion extends NodoAST implements Sentencia {

    public Declaracion(int linea, int columna) {
        super(linea, columna);
    }
}
