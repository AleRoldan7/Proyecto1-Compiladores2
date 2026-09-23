package ast.sentencias;

import ast.NodoAST;
import ast.expresiones.Expresion;
import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CondicionSwitch extends NodoAST implements Sentencia {

    private Expresion expresion;
    private List<SentenciaCase> casos;
    private Bloque bloqueDefecto;

    public CondicionSwitch(int linea, int columna, Expresion expresion, List<SentenciaCase> casos, Bloque bloqueDefecto) {
        super(linea, columna);
        this.expresion = expresion;
        this.casos = casos;
        this.bloqueDefecto = bloqueDefecto;
    }


    @Override
    public String generarC3D(ContextoC3D contexto) {

        String etqFin = contexto.nuevaEtiqueta();
        contexto.entrarSwitch(etqFin);

        String valorSwitch = expresion.generarC3D(contexto);

        java.util.Map<SentenciaCase, String> etqCaso = new java.util.HashMap<>();

        for (SentenciaCase c : casos) {
            String etq = contexto.nuevaEtiqueta();
            etqCaso.put(c, etq);

            String valorCaso = c.getValor().generarC3D(contexto);
            String cmp = contexto.binaria("==", valorSwitch, valorCaso, enums.TipoDato.BOOLEANO);

            contexto.saltoSiVerdadero(cmp, etq);
        }

        if (bloqueDefecto != null) {
            bloqueDefecto.generarC3D(contexto);
        }
        contexto.salto(etqFin);

        for (SentenciaCase c : casos) {
            contexto.agregarEtiqueta(etqCaso.get(c));
            c.getCuerpoCase().generarC3D(contexto);
            contexto.salto(etqFin);
        }

        contexto.agregarEtiqueta(etqFin);
        contexto.salirSwitch();

        return null;
    }
}
