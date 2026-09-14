package ast.sentencias;

import ast.NodoAST;
import c3d.ContextoC3D;

public class SentenciaContinue extends NodoAST implements Sentencia {

    public SentenciaContinue(int linea, int columna) {
        super(linea, columna);
    }

    @Override
    public void generarC3D(ContextoC3D contexto) {

    }
}
