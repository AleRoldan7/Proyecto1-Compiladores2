package ast.sentencias;

import ast.NodoAST;
import ast.expresiones.Expresion;
import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SentenciaReturn extends NodoAST implements Sentencia {

    private Expresion expresionReturn;

    public SentenciaReturn(int linea, int columna, Expresion expresionReturn) {
        super(linea, columna);
        this.expresionReturn = expresionReturn;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {

        if (expresionReturn != null) {
            String valor = expresionReturn.generarC3D(contexto);
            contexto.agregar("return", valor, null, null);
        } else {
            contexto.agregar("return", null, null, null);
        }

        return null;
    }
}
