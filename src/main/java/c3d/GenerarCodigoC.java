package c3d;

import enums.TipoDato;

import java.util.List;

public class GenerarCodigoC {

    public String generar(List<Cuarteta> cuartetas) {
        StringBuilder c = new StringBuilder();
        c.append("#include <stdio.h>\n#include <stdlib.h>\n#include <string.h>\n\n");
        c.append("int main() {\n");

        for (Cuarteta q : cuartetas) {
            c.append("    ").append(traducir(q)).append('\n');
        }

        c.append("    return 0;\n}\n");
        return c.toString();
    }

    private String traducir(Cuarteta q) {
        return switch (q.getOperador()) {
            case "label"    -> q.getResultado() + ":;";
            case "goto"     -> "goto " + q.getResultado() + ";";
            case "if_false" -> "if (!(" + q.getArg1() + ")) goto " + q.getResultado() + ";";
            case "="        -> prefijoTipo(q) + q.getResultado() + " = " + q.getArg1() + ";";
            case "+", "-", "*", "/", "==", "!=", "<", ">", "&&", "||" ->
                    prefijoTipo(q) + q.getResultado() + " = " + q.getArg1() + " " + q.getOperador() + " " + q.getArg2() + ";";
            default -> "// operador aún no soportado en C: " + q.getOperador();
        };
    }

    private String prefijoTipo(Cuarteta q) {
        if (q.getTipoDeclarado() == null) {
            return ""; // ya es una variable existente, no se re-declara
        }
        return mapearTipoC(q.getTipoDeclarado()) + " ";
    }

    private String mapearTipoC(TipoDato tipo) {
        return switch (tipo) {
            case ENTERO -> "int";
            case DECIMAL -> "double";
            case CARACTER -> "char";
            case BOOLEANO -> "int";
            case TEXTO -> "char*";
            default -> "int"; // fallback temporal, hasta que conectes el resolutor de tipos real
        };
    }
}
