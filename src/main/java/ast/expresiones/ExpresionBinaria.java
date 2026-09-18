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

        TipoDato tipo = inferirTipo(izq, der);

        // Relacionales y lógicos devuelven booleano (0/1)
        if (esRelacional(operacion) || esLogico(operacion)) {
            tipo = TipoDato.BOOLEANO;
        }

        return contexto.binaria(operacion, izq, der, tipo);
    }

    private TipoDato inferirTipo(String izq, String der) {
        // Si algún operando es decimal, el resultado es decimal
        if (esDecimal(izq) || esDecimal(der)) {
            return TipoDato.DECIMAL;
        }
        return TipoDato.ENTERO;
    }

    private boolean esDecimal(String s) {
        return s != null && s.contains(".");
    }

    private boolean esRelacional(String op) {
        return op.equals("<") || op.equals(">") ||
                op.equals("<=") || op.equals(">=") ||
                op.equals("==") || op.equals("!=");
    }

    private boolean esLogico(String op) {
        return op.equals("&&") || op.equals("||");
    }
}
