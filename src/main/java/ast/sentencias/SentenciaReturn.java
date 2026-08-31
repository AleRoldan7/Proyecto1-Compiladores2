package ast.sentencias;

import ast.expresiones.Expresion;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SentenciaReturn extends Sentencia {

    private Expresion expresionReturn;

    public SentenciaReturn(int linea, int columna, Expresion expresionReturn) {
        super(linea, columna);
        this.expresionReturn = expresionReturn;
    }
}
