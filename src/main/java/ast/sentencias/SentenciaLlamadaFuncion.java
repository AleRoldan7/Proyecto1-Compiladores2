package ast.sentencias;

import ast.expresiones.LlamadaFuncion;
import c3d.ContextoC3D;

public class SentenciaLlamadaFuncion extends Sentencia {

    private LlamadaFuncion llamada;

    public SentenciaLlamadaFuncion(
            int linea,
            int columna,
            LlamadaFuncion llamada
    ) {
        super(linea, columna);
        this.llamada = llamada;
    }

    public LlamadaFuncion getLlamada() {
        return llamada;
    }

    @Override
    public void generarC3D(ContextoC3D contexto) {
        llamada.generarC3D(contexto);
    }
}
