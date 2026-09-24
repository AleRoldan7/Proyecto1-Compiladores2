package c3d;

import java.util.*;

public class GenerarCodigoC {

    private static class Funcion {
        final String nombre;
        final List<String> parametros = new ArrayList<>();
        final List<Cuarteta> cuerpo = new ArrayList<>();
        boolean devuelveValor = false;

        Funcion(String nombre) {
            this.nombre = nombre;
        }

        String nombreC() {
            return nombre.equals("main") ? "func_main" : nombre;
        }

        String firma() {
            return (devuelveValor ? "int " : "void ") + nombreC()
                    + "(" + (parametros.isEmpty() ? "void" : String.join(", ", parametros)) + ")";
        }
    }

    public static String traducir(List<Cuarteta> cuartetas) {

        // 1. Separar funciones de las cuartetas sueltas
        Map<String, Funcion> funciones = new LinkedHashMap<>();
        List<Cuarteta> sueltas = new ArrayList<>();
        Funcion actual = null;

        for (Cuarteta q : cuartetas) {

            String op = q.getOperador();
            String res = q.getResultado();

            if ("label".equals(op) && res != null && res.startsWith("func_")) {
                actual = new Funcion(res.substring(5));
                funciones.put(actual.nombre, actual);
                continue;
            }

            if ("label".equals(op) && res != null && res.startsWith("end_")) {
                actual = null;
                continue;
            }

            if (actual == null) {
                sueltas.add(q);
                continue;
            }

            if ("param_decl".equals(op)) {
                actual.parametros.add("int " + q.getArg2());   // todo es int: los objetos son índices del heap
                continue;
            }

            if ("return".equals(op) && q.getArg1() != null) {
                actual.devuelveValor = true;
            }

            actual.cuerpo.add(q);
        }

        // 2. Lo que quedó fuera de toda función (VARIABILES>) se ejecuta al inicio de main
        Funcion main = funciones.get("main");
        List<Cuarteta> inicializacion = new ArrayList<>();

        for (Cuarteta q : sueltas) {
            if (!"halt".equals(q.getOperador()) && !"comment".equals(q.getOperador())) {
                inicializacion.add(q);
            }
        }

        if (main != null) {
            main.cuerpo.addAll(0, inicializacion);
        }

        Set<String> sinValor = new HashSet<>();
        for (Funcion f : funciones.values()) {
            if (!f.devuelveValor) {
                sinValor.add(f.nombre);
            }
        }

        // 3. Variables y temporales
        Set<String> temporales = new TreeSet<>();
        Set<String> variables = new TreeSet<>();

        for (Cuarteta q : cuartetas) {
            recolectar(q, temporales, variables);
        }

        // 4. Salida
        StringBuilder sb = new StringBuilder();

        sb.append("#include <stdio.h>\n");
        sb.append("#include <stdlib.h>\n\n");
        sb.append("int heap[100000];\n");
        sb.append("int hp = 1;   // 0 = null\n\n");

        sb.append("// ==== VARIABLES GLOBALES ====\n");
        if (!variables.isEmpty()) {
            sb.append("int ").append(String.join(", ", variables)).append(";\n");
        }

        sb.append("\n// ==== TEMPORALES ====\n");
        if (!temporales.isEmpty()) {
            sb.append("int ").append(String.join(", ", temporales)).append(";\n");
        }

        sb.append("\n// ==== FORWARD DECLARATIONS ====\n");
        for (Funcion f : funciones.values()) {
            sb.append(f.firma()).append(";\n");
        }

        sb.append("\n// ==== FUNCIONES ====\n");
        for (Funcion f : funciones.values()) {

            sb.append(f.firma()).append(" {\n");

            List<String> pendientes = new ArrayList<>();

            for (Cuarteta q : f.cuerpo) {
                String linea = traducirCuarteta(q, pendientes, sinValor, f.devuelveValor);
                if (!linea.isEmpty()) {
                    sb.append("    ").append(linea).append("\n");
                }
            }

            sb.append("}\n\n");
        }

        if (main == null && !inicializacion.isEmpty()) {
            sb.append("// AVISO: hay cuartetas fuera de función y no existe func_main\n");
        }

        sb.append("int main(void) {\n");
        if (main != null) {
            sb.append("    func_main();\n");
        }
        sb.append("    return 0;\n");
        sb.append("}\n");

        return sb.toString();
    }

    /* =========================================================
       HELPERS
       ========================================================= */

    private static void recolectar(Cuarteta q, Set<String> temporales, Set<String> variables) {

        String[] candidatos = switch (q.getOperador()) {
            case "label", "goto", "halt", "comment", "param_decl" -> new String[0];
            case "call", "new", "read"                            -> new String[]{q.getResultado()};  // arg1 = función/clase
            case "new_array"                                      -> new String[]{q.getArg2(), q.getResultado()};
            case "attr_get"                                       -> new String[]{q.getArg1(), q.getResultado()};
            case "field_set"                                      -> new String[]{q.getResultado(), q.getArg2()};
            case "param", "print", "return", "if_true", "if_false" -> new String[]{q.getArg1()};
            default                                               -> new String[]{q.getArg1(), q.getArg2(), q.getResultado()};
        };

        for (String arg : candidatos) {

            if (arg == null || arg.equals("self")) continue;

            if (arg.matches("t\\d+")) {
                temporales.add(arg);
            } else if (arg.matches("[A-Za-z_]\\w*") && !arg.matches("L\\d+")) {
                variables.add(arg);          // números, textos y etiquetas no pasan este filtro
            }
        }
    }

    private static String numero(String valor, String operador) {

        if (valor == null || !valor.matches("\\d+")) {
            throw new IllegalStateException("La cuarteta '" + operador + "' necesita un desplazamiento numérico y recibió '"
                    + valor + "'. Revisa AccesoAtributo y Asignacion.");
        }

        return valor;
    }

    private static String traducirCuarteta(Cuarteta q, List<String> pendientes,
                                           Set<String> sinValor, boolean funcionDevuelve) {

        String op = q.getOperador();
        String a1 = q.getArg1();
        String a2 = q.getArg2();
        String res = q.getResultado();

        return switch (op) {

            case "label"    -> res + ":;";
            case "goto"     -> "goto " + res + ";";
            case "if_true"  -> "if (" + a1 + ") goto " + res + ";";
            case "if_false" -> "if (!" + a1 + ") goto " + res + ";";
            case "="        -> res + " = " + a1 + ";";
            case "neg"      -> res + " = -" + a1 + ";";
            case "not"      -> res + " = !" + a1 + ";";
            case "comment"  -> "// " + String.valueOf(res).replace('\n', ' ');
            case "halt"     -> "";

            case "param" -> {
                pendientes.add(a1);
                yield "";
            }

            case "call" -> {
                String llamada = a1 + "(" + String.join(", ", pendientes) + ")";
                pendientes.clear();
                boolean descartar = (res == null) || sinValor.contains(a1);
                yield (descartar ? llamada : res + " = " + llamada) + ";";
            }

            case "return" -> a1 != null
                    ? "return " + a1 + ";"
                    : (funcionDevuelve ? "return 0;" : "return;");

            case "print" -> a1.startsWith("\"")
                    ? "printf(\"%s\", " + a1 + ");"
                    : "printf(\"%d\\n\", " + a1 + ");";

            case "read" -> "if (scanf(\"%d\", &" + res + ") != 1) { int c; while ((c = getchar()) != '\\n' && c != EOF) {} "
                    + res + " = 0; }";

            case "new" -> {
                if (a2 == null || !a2.matches("\\d+")) {
                    throw new IllegalStateException("La cuarteta 'new' no trae el tamaño del objeto. "
                            + "Usa el CrearObjeto que llama a contexto.tamanio(clase).");
                }
                yield res + " = hp; hp = hp + " + a2 + ";";
            }

            case "new_array" -> res + " = hp; hp = hp + " + a2 + ";";
            case "attr_get"  -> res + " = heap[" + a1 + " + " + numero(a2, op) + "];";
            case "field_set" -> "heap[" + res + " + " + numero(a1, op) + "] = " + a2 + ";";
            case "index_get" -> res + " = heap[" + a1 + " + " + a2 + "];";
            case "index_set" -> "heap[" + res + " + " + a1 + "] = " + a2 + ";";

            default -> op.startsWith("if_")
                    ? "if (" + a1 + " " + op.substring(3) + " " + a2 + ") goto " + res + ";"
                    : res + " = " + a1 + " " + op + " " + a2 + ";";
        };
    }
}