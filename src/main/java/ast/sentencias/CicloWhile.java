package ast.sentencias;

import ast.expresiones.Expresion;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CicloWhile extends Sentencia {

    private Expresion condicionWhile;
    private Bloque bloqueWhile;

    public CicloWhile(int linea, int columna, Expresion condicionWhile, Bloque bloqueWhile) {
        super(linea, columna);
        this.condicionWhile = condicionWhile;
        this.bloqueWhile = bloqueWhile;
    }
}
