package ast.expresiones;

import ast.NodoAST;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Expresion  extends NodoAST {


    protected String resultado;

    public Expresion(int linea, int columna) {
        super(linea, columna);
    }
}
