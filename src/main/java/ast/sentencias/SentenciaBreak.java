package ast.sentencias;

import ast.NodoAST;
import c3d.ContextoC3D;

public class SentenciaBreak extends NodoAST implements Sentencia {

    public SentenciaBreak(int linea, int columna) {
        super(linea, columna);
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {
        String etq = contexto.etiquetaBreak();
        if (etq != null) {
            contexto.salto(etq);
        }
        return null;
    }
}
