package c3d;

import enums.TipoDato;

import java.util.*;

public class GenerarCodigoC {

    private static final Set<String> TIPOS = Set.of(
            "int", "double", "char", "boolean", "String", "void",
            "entero", "flotante", "caracter", "bool", "cadena",
            "numerus", "textum", "decimalis", "littera"
    );

    public static String traducir(List<Cuarteta> cuartetas) {

        StringBuilder sb = new StringBuilder();

        sb.append("#include <stdio.h>\n");
        sb.append("#include <stdlib.h>\n\n");

        // 1. Recolectar variables y temporales
        Set<String> temporales = new TreeSet<>();
        Set<String> variables  = new TreeSet<>();

        for (Cuarteta q : cuartetas) {
            recolectarIdentificadores(q, temporales, variables);
        }

        // 2. Parámetros (para no declararlos como globales)
        Set<String> parametros = new HashSet<>();
        for (Cuarteta q : cuartetas) {
            if ("param_decl".equals(q.getOperador())) {
                parametros.add(q.getArg2());
            }
        }

        // 3. Variables globales
        sb.append("// ==== VARIABLES GLOBALES ====\n");
        for (String v : variables) {
            if (!parametros.contains(v)) {
                sb.append("int ").append(v).append(";\n");
            }
        }

        // 4. Temporales
        sb.append("\n// ==== TEMPORALES ====\n");
        if (!temporales.isEmpty()) {
            sb.append("int ");
            sb.append(String.join(", ", temporales));
            sb.append(";\n");
        }

        // 5. Forward declarations
        sb.append("\n// ==== FORWARD DECLARATIONS ====\n");
        for (Cuarteta q : cuartetas) {
            if ("label".equals(q.getOperador()) && q.getResultado().startsWith("func_")) {
                String nombre = q.getResultado().substring(5);
                String tipoRetorno = inferirTipoRetorno(nombre, cuartetas);
                String nombreFinal = nombre.equals("main") ? "func_main" : nombre;
                sb.append(tipoRetorno).append(" ").append(nombreFinal).append("();\n");
            }
        }

        // 6. Funciones
        sb.append("\n// ==== FUNCIONES ====\n");

        boolean dentroDeFuncion = false;
        StringBuilder cuerpoFuncion = new StringBuilder();
        String nombreFuncionActual = null;
        String tipoRetornoFuncion = "void";
        List<String> parametrosFuncion = new ArrayList<>();

        for (Cuarteta q : cuartetas) {

            if ("label".equals(q.getOperador()) && q.getResultado().startsWith("func_")) {
                nombreFuncionActual = q.getResultado().substring(5);
                tipoRetornoFuncion = inferirTipoRetorno(nombreFuncionActual, cuartetas);
                parametrosFuncion.clear();
                cuerpoFuncion.setLength(0);
                dentroDeFuncion = true;
                continue;
            }

            if ("label".equals(q.getOperador()) && q.getResultado().startsWith("end_")) {

                String nombreFinal = nombreFuncionActual.equals("main")
                        ? "func_main"
                        : nombreFuncionActual;

                sb.append(tipoRetornoFuncion).append(" ").append(nombreFinal).append("(");

                for (int i = 0; i < parametrosFuncion.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(parametrosFuncion.get(i));
                }

                sb.append(") {\n");
                sb.append(cuerpoFuncion);
                sb.append("}\n\n");

                dentroDeFuncion = false;
                continue;
            }

            if (dentroDeFuncion) {
                if ("param_decl".equals(q.getOperador())) {
                    parametrosFuncion.add(q.getArg1() + " " + q.getArg2());
                    continue;
                }
                if ("return_decl".equals(q.getOperador())) continue;
                cuerpoFuncion.append("    ").append(traducirCuarteta(q)).append("\n");
            } else {
                // Ignorar halt y cualquier cosa fuera de función
                if (!"halt".equals(q.getOperador())) {
                    sb.append(traducirCuarteta(q)).append("\n");
                }
            }
        }

        // 7. ✅ SIEMPRE emitir el main al final
        sb.append("\nint main() {\n");
        sb.append("    func_main();\n");
        sb.append("    return 0;\n");
        sb.append("}\n");

        return sb.toString();
    }

    /* =========================================================
       HELPERS
       ========================================================= */

    private static void recolectarIdentificadores(Cuarteta q,
                                                  Set<String> temporales,
                                                  Set<String> variables) {

        if ("param_decl".equals(q.getOperador())) {
            String nombre = q.getArg2();
            if (nombre != null && !TIPOS.contains(nombre)) {
                variables.add(nombre);
            }
            return;
        }

        for (String arg : new String[]{q.getArg1(), q.getArg2(), q.getResultado()}) {

            if (arg == null) continue;
            if (arg.matches("-?\\d+(\\.\\d+)?")) continue;
            if (arg.startsWith("L")) continue;
            if (arg.startsWith("func_") || arg.startsWith("end_")) continue;
            if (TIPOS.contains(arg)) continue;

            if (arg.matches("t\\d+")) {
                temporales.add(arg);
            } else if (!arg.contains(" ") && !arg.contains("[")
                    && !arg.contains(".") && !arg.contains("->")) {
                variables.add(arg);
            }
        }
    }

    private static String inferirTipoRetorno(String nombreFuncion, List<Cuarteta> cuartetas) {

        boolean dentro = false;

        for (Cuarteta q : cuartetas) {

            if ("label".equals(q.getOperador())
                    && q.getResultado().equals("func_" + nombreFuncion)) {
                dentro = true;
                continue;
            }

            if ("label".equals(q.getOperador())
                    && q.getResultado().equals("end_" + nombreFuncion)) {
                break;
            }

            if (dentro && "return".equals(q.getOperador()) && q.getArg1() != null) {
                return q.getArg1().contains(".") ? "double" : "int";
            }
        }

        return "void";
    }

    private static String traducirCuarteta(Cuarteta q) {

        return switch (q.getOperador()) {

            case "label"    -> q.getResultado() + ":;";
            case "goto"     -> "goto " + q.getResultado() + ";";
            case "if_true"  -> "if (" + q.getArg1() + ") goto " + q.getResultado() + ";";
            case "if_false" -> "if (!" + q.getArg1() + ") goto " + q.getResultado() + ";";
            case "="        -> q.getResultado() + " = " + q.getArg1() + ";";
            case "call"     -> {
                if (q.getResultado() == null) {
                    yield q.getArg1() + "();";
                }
                yield q.getResultado() + " = " + q.getArg1() + "();";
            }
            case "return"   -> q.getArg1() == null ? "return;" : "return " + q.getArg1() + ";";
            case "print"    -> "printf(\"%d\\n\", " + q.getArg1() + ");";
            case "halt"     -> "return 0;";

            case "neg"      -> q.getResultado() + " = -" + q.getArg1() + ";";
            case "not"      -> q.getResultado() + " = !" + q.getArg1() + ";";
            case "comment"  -> "// " + q.getResultado();

            case "index_get" -> q.getResultado() + " = " + q.getArg1() + "[" + q.getArg2() + "];";
            case "index_set" -> q.getResultado() + "[" + q.getArg1() + "] = " + q.getArg2() + ";";

            default -> {

                if (q.getOperador().startsWith("if_")) {
                    String op = q.getOperador().substring(3);
                    yield "if (" + q.getArg1() + " " + op + " " + q.getArg2()
                            + ") goto " + q.getResultado() + ";";
                }

                yield q.getResultado() + " = " + q.getArg1()
                        + " " + q.getOperador() + " " + q.getArg2() + ";";
            }
        };
    }
}
