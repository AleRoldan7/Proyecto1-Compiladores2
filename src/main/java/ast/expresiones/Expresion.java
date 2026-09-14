package ast.expresiones;

import ast.NodoAST;
import ast.sentencias.Sentencia;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Expresion  extends NodoAST implements Sentencia {


    protected String resultado;

    public Expresion(int linea, int columna) {
        super(linea, columna);
    }
}
