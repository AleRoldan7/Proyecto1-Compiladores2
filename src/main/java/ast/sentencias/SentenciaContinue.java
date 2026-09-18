package ast.sentencias;

import ast.NodoAST;
import c3d.ContextoC3D;

public class SentenciaContinue extends NodoAST implements Sentencia {

    public SentenciaContinue(int linea, int columna) {
        super(linea, columna);
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {
        String etq = contexto.etiquetaContinue();
        if (etq != null) {
            contexto.salto(etq);
        }
        return null;
    }
}
