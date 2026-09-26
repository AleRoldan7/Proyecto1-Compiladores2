package ast.expresiones;

import c3d.ContextoC3D;
import enums.TipoDato;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpresionBinaria extends Expresion {

    private Expresion izquierda;
    private String operacion;
    private Expresion derecha;

    /** Tipo del RESULTADO ya resuelto por el análisis semántico (InferirTipoBinario). */
    private TipoDato tipoResuelto;

    public ExpresionBinaria(int linea, int columna, Expresion izquierda, String operacion, Expresion derecha) {
        super(linea, columna);
        this.izquierda = izquierda;
        this.operacion = operacion;
        this.derecha = derecha;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {

        String izq = izquierda.generarC3D(contexto);
        String der = derecha.generarC3D(contexto);

        TipoDato tipo = (tipoResuelto != null) ? tipoResuelto : inferirTipoRespaldo(izq, der);

        return contexto.binaria(operacion, izq, der, tipo);
    }

    /** Solo por si algún ExpresionBinaria se genera sin haber pasado análisis semántico. */
    private TipoDato inferirTipoRespaldo(String izq, String der) {

        if (esRelacional(operacion) || esLogico(operacion)) {
            return TipoDato.BOOLEANO;
        }

        if ("+".equals(operacion) && (esTexto(izq) || esTexto(der))) {
            return TipoDato.TEXTO;
        }

        if (esDecimal(izq) || esDecimal(der)) {
            return TipoDato.DECIMAL;
        }

        return TipoDato.ENTERO;
    }

    private boolean esTexto(String s) {
        return s != null && s.startsWith("\"");
    }

    private boolean esDecimal(String s) {
        return s != null && s.matches("-?\\d+\\.\\d+");
    }

    private boolean esRelacional(String op) {
        return op.equals("<") || op.equals(">") || op.equals("<=") || op.equals(">=")
                || op.equals("==") || op.equals("!=");
    }

    private boolean esLogico(String op) {
        return op.equals("&&") || op.equals("||");
    }
}