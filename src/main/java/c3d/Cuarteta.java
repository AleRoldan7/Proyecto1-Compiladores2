package c3d;

import enums.TipoDato;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Cuarteta {

    private final String operador;
    private final String arg1;
    private final String arg2;
    private final String resultado;
    private final TipoDato tipoDeclarado;

    public Cuarteta(String operador, String arg1, String arg2, String resultado, TipoDato tipoDeclarado) {
        this.operador = operador;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.resultado = resultado;
        this.tipoDeclarado = tipoDeclarado;
    }

    @Override
    public String toString() {
        return switch (operador) {

            /*
             * FIX: faltaban espacios en casi todos los casos, así que el
             * C3D salía como "gotoetq0" / "if_falsetmp1gotoetq2" y era
             * ilegible (y no se podía volver a parsear).
             */
            case "label"    -> resultado + ":";
            case "goto"     -> "goto " + resultado;
            case "if_true"  -> "if " + arg1 + " goto " + resultado;
            case "if_false" -> "if_false " + arg1 + " goto " + resultado;
            case "="        -> resultado + " = " + arg1;
            case "call"     -> resultado == null
                    ? "call " + arg1 + ", " + arg2
                    : resultado + " = call " + arg1 + ", " + arg2;
            case "param"    -> "param " + arg1;
            case "return"   -> arg1 == null ? "return" : "return " + arg1;
            case "print"    -> "print " + arg1;
            case "read"     -> resultado + " = read";
            case "halt"     -> "halt";

            // Unarios: resultado = op arg1   (ej: t1 = - t0, t2 = ! t1)
            case "neg"      -> resultado + " = -" + arg1;
            case "not"      -> resultado + " = !" + arg1;

            // Arreglos: t = base[indice]  /  base[indice] = valor
            case "index_get" -> resultado + " = " + arg1 + "[" + arg2 + "]";
            case "index_set" -> resultado + "[" + arg1 + "] = " + arg2;

            // Heap: reserva de objetos (el enunciado exige heap para objetos)
            case "new"      -> resultado + " = new " + arg1;

            default -> resultado + " = " + arg1 + " " + operador + " " + arg2;
        };
    }
}