package ast.sentencias;

import ast.expresiones.Expresion;
import c3d.ContextoC3D;
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

    @Override
    public void generarC3D(ContextoC3D contexto) {

    }
}
