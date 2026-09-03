package ast.sentencias;

import ast.expresiones.Expresion;
import c3d.ContextoC3D;

public class SentenciaExpresion extends Sentencia {

    private Expresion expresion;

    public SentenciaExpresion(int linea, int columna, Expresion expresion) {
        super(linea, columna);
        this.expresion = expresion;
    }

    @Override
    public void generarC3D(ContextoC3D contexto) {
        expresion.generarC3D(contexto);
    }
}
