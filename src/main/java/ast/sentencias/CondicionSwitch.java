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

        // Evaluar la expresión del switch
        String valorSwitch = expresion.generarC3D(contexto);

        // Para cada caso, comparar y saltar a su etiqueta
        java.util.Map<SentenciaCase, String> etqCaso = new java.util.HashMap<>();

        for (SentenciaCase c : casos) {
            String etq = contexto.nuevaEtiqueta();
            etqCaso.put(c, etq);

            String valorCaso = c.getValor().generarC3D(contexto);
            String cmp = contexto.binaria("==", valorSwitch, valorCaso, enums.TipoDato.BOOLEANO);

            contexto.saltoSiVerdadero(cmp, etq);
        }

        // Si ninguno matcheó → ir a default (o fin)
        if (bloqueDefecto != null) {
            bloqueDefecto.generarC3D(contexto);
        }
        contexto.salto(etqFin);

        // Cuerpos de cada caso
        for (SentenciaCase c : casos) {
            contexto.agregarEtiqueta(etqCaso.get(c));
            c.getCuerpoCase().generarC3D(contexto);
            contexto.salto(etqFin);   // fall-through → salta al fin (no hay fall-through)
        }

        contexto.agregarEtiqueta(etqFin);
        contexto.salirSwitch();

        return null;
    }
}
