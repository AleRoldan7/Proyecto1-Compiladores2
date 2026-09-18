package ast.sentencias;

import ast.NodoAST;
import ast.expresiones.Expresion;
import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CicloDoWhile extends NodoAST implements Sentencia {

    private Bloque bloqueDoWhile;
    private Expresion expresionDoWhile;

    public CicloDoWhile(int linea, int columna, Bloque bloqueDoWhile, Expresion expresionDoWhile) {
        super(linea, columna);
        this.bloqueDoWhile = bloqueDoWhile;
        this.expresionDoWhile = expresionDoWhile;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {

        String etqInicio = contexto.nuevaEtiqueta();
        String etqCuerpo = contexto.nuevaEtiqueta();
        String etqFin = contexto.nuevaEtiqueta();

        // continue → etqCuerpo (donde se evalúa la condición), break → etqFin
        contexto.entrarCiclo(etqCuerpo, etqFin);

        contexto.agregarEtiqueta(etqInicio);
        bloqueDoWhile.generarC3D(contexto);

        // Evaluar la condición: si verdadera, repetir; si falsa, salir
        contexto.agregarEtiqueta(etqCuerpo);
        CondicionIf.generarCondicion(expresionDoWhile, contexto, etqInicio, etqFin);

        contexto.agregarEtiqueta(etqFin);

        contexto.salirCiclo();
        return null;
    }
}
