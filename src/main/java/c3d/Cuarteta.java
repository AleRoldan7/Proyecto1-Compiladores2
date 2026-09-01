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

            case "label" -> resultado + ":";
            case "goto" -> "goto" + resultado;
            case "if_false" -> "if_false" + arg1 + "goto" + resultado;
            case "=" -> resultado + "=" + arg1;
            case "call" -> resultado + "= call" + arg1 + ", " + arg2;
            case "param" -> "param" + arg1;
            default -> resultado + "=" + arg1 + " " + operador + " " + arg2;
        };
    }
}
