package ast.sentencias;

import ast.NodoAST;
import ast.expresiones.Expresion;
import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CicloWhile extends NodoAST implements Sentencia {

    private Expresion condicionWhile;
    private Bloque bloqueWhile;

    public CicloWhile(int linea, int columna, Expresion condicionWhile, Bloque bloqueWhile) {
        super(linea, columna);
        this.condicionWhile = condicionWhile;
        this.bloqueWhile = bloqueWhile;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {

        String etqInicio = contexto.nuevaEtiqueta();
        String etqCuerpo = contexto.nuevaEtiqueta();
        String etqFin = contexto.nuevaEtiqueta();

        // continue → etqInicio, break → etqFin
        contexto.entrarCiclo(etqInicio, etqFin);

        contexto.agregarEtiqueta(etqInicio);

        CondicionIf.generarCondicion(condicionWhile, contexto, etqCuerpo, etqFin);

        contexto.agregarEtiqueta(etqCuerpo);
        bloqueWhile.generarC3D(contexto);
        contexto.salto(etqInicio);

        contexto.agregarEtiqueta(etqFin);

        contexto.salirCiclo();
        return null;
    }
}
