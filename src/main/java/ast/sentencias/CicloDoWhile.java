package ast.sentencias;

import ast.expresiones.Expresion;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CicloDoWhile extends Sentencia {

    private Bloque bloqueDoWhile;
    private Expresion expresionDoWhile;

    public CicloDoWhile(int linea, int columna, Bloque bloqueDoWhile, Expresion expresionDoWhile) {
        super(linea, columna);
        this.bloqueDoWhile = bloqueDoWhile;
        this.expresionDoWhile = expresionDoWhile;
    }
}
