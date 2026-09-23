package ast.sentencias;

import ast.NodoAST;
import ast.expresiones.Expresion;
import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CicloFor extends NodoAST implements Sentencia {

    private Sentencia inicializacion;
    private Expresion condicionFor;
    private Expresion incremento;
    private Bloque bloqueFor;

    public CicloFor(int linea, int columna, Sentencia inicializacion, Expresion condicionFor, Expresion incremento, Bloque bloqueFor) {
        super(linea, columna);
        this.inicializacion = inicializacion;
        this.condicionFor = condicionFor;
        this.incremento = incremento;
        this.bloqueFor = bloqueFor;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {

        String etqInicio = contexto.nuevaEtiqueta();
        String etqCuerpo = contexto.nuevaEtiqueta();
        String etqIncremento = contexto.nuevaEtiqueta();
        String etqFin = contexto.nuevaEtiqueta();

        contexto.entrarCiclo(etqIncremento, etqFin);

        if (inicializacion != null) {
            inicializacion.generarC3D(contexto);
        }

        contexto.agregarEtiqueta(etqInicio);

        if (condicionFor != null) {
            CondicionIf.generarCondicion(condicionFor, contexto, etqCuerpo, etqFin);
        } else {
            contexto.salto(etqCuerpo);
        }

        contexto.agregarEtiqueta(etqCuerpo);
        bloqueFor.generarC3D(contexto);

        contexto.agregarEtiqueta(etqIncremento);
        if (incremento != null) {
            incremento.generarC3D(contexto);
        }

        contexto.salto(etqInicio);
        contexto.agregarEtiqueta(etqFin);

        contexto.salirCiclo();
        return null;
    }
}
