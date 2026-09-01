package utils.generadorC;

import c3d.Cuarteta;
import enums.TipoDato;

import java.util.List;

public class GeneradorCodigoC {

    public String generar(List<Cuarteta> cuartetas) {
        StringBuilder codigo = new StringBuilder();
        codigo.append("#include <stdio.h>\n#include <stdlib.h>\n#include <string.h>\n\n");
        codigo.append("int main() {\n");

        for (Cuarteta q : cuartetas) {
            codigo.append("    ").append(traducir(q)).append('\n');
        }

        codigo.append("    return 0;\n}\n");
        return codigo.toString();
    }

    private String traducir(Cuarteta q) {
        return switch (q.getOperador()) {
            case "label"     -> q.getResultado() + ":;";
            case "goto"      -> "goto " + q.getResultado() + ";";
            case "if_false"  -> "if (!(" + q.getArg1() + ")) goto " + q.getResultado() + ";";
            case "="         -> q.getResultado() + " = " + q.getArg1() + ";";
            case "+", "-", "*", "/", "==", "!=", "<", ">", "&&", "||" ->
                    prefijoTipo(q) + q.getResultado() + " = " + q.getArg1() + " " + q.getOperador() + " " + q.getArg2() + ";";
            default -> "// operador aún no soportado en C: " + q.getOperador();
        };
    }

    private String prefijoTipo(Cuarteta cuarteta) {
        if (cuarteta.getTipoDeclarado() == null) {
            return "";
        }
        return mapearTipoC(cuarteta.getTipoDeclarado()) + " ";
    }

    private String mapearTipoC(TipoDato tipo) {
        return switch (tipo) {
            case ENTERO -> "int";
            case DECIMAL -> "double";
            case CARACTER -> "char";
            case BOOLEANO -> "int";
            case TEXTO -> "char*";
            default -> "void*";
        };
    }
}
