package c3d;

import enums.TipoDato;
import java.util.*;

public class GenerarCodigoC {

    private static class Funcion {
        final String nombre;
        final List<String> parametros = new ArrayList<>();
        final List<Cuarteta> cuerpo = new ArrayList<>();
        TipoDato tipoRetorno = TipoDato.VOID;

        Funcion(String nombre) {
            this.nombre = nombre;
        }

        String nombreC() {
            return nombre.equals("main") ? "func_main" : nombre;
        }

        String firma() {
            return tipoRetorno.aTipoC() + " " + nombreC()
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
                // ADAPTA si algún día param_decl trae tipos reales de objetos (hoy: heap = int)
                actual.parametros.add("int " + q.getArg2());
                continue;
            }

            actual.cuerpo.add(q);
        }

        // 2. Lo que quedó fuera de toda función se ejecuta al inicio de main
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

        // 3. Variables, temporales y TIPOS (nuevo)
        Set<String> temporales = new TreeSet<>();
        Set<String> variables = new TreeSet<>();

        for (Cuarteta q : cuartetas) {
            recolectar(q, temporales, variables);
        }

        Map<String, TipoDato> tipos = inferirTipos(cuartetas);

        // 4. Tipo de retorno real de cada función, ahora que ya sabemos tipos
        for (Funcion f : funciones.values()) {
            f.tipoRetorno = TipoDato.VOID;
            for (Cuarteta q : f.cuerpo) {
                if ("return".equals(q.getOperador()) && q.getArg1() != null) {
                    TipoDato t = tipoDe(q.getArg1(), tipos);
                    f.tipoRetorno = t;   // si hay varios return, se queda con el último (ver nota abajo)
                }
            }
        }

        // 5. Salida
        StringBuilder sb = new StringBuilder();

        sb.append("#include <stdio.h>\n");
        sb.append("#include <stdlib.h>\n");
        sb.append("#include <string.h>\n\n");
        sb.append("int heap[100000];\n");
        sb.append("int hp = 1;   // 0 = null\n\n");
        sb.append("char* concat(const char* a, const char* b) {\n");
        sb.append("    char* buf = malloc(strlen(a) + strlen(b) + 1);\n");
        sb.append("    strcpy(buf, a);\n");
        sb.append("    strcat(buf, b);\n");
        sb.append("    return buf;\n");
        sb.append("}\n\n");

        sb.append("// ==== VARIABLES GLOBALES ====\n");
        for (String v : variables) {
            sb.append(tipoDe(v, tipos).aTipoC()).append(" ").append(v).append(";\n");
        }

        sb.append("\n// ==== TEMPORALES ====\n");
        for (String t : temporales) {
            sb.append(tipoDe(t, tipos).aTipoC()).append(" ").append(t).append(";\n");
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
                String linea = traducirCuarteta(q, pendientes, funciones, f.tipoRetorno, tipos);
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
            case "call", "new", "read"                            -> new String[]{q.getResultado()};
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
                variables.add(arg);
            }
        }
    }

    /** Literal de C3D: "texto", número entero, número decimal, o nombre de variable/temporal ya tipado. */
    private static TipoDato tipoDe(String valor, Map<String, TipoDato> tipos) {
        if (valor == null) return TipoDato.ENTERO;
        if (valor.startsWith("\"")) return TipoDato.TEXTO;
        if (valor.matches("-?\\d+")) return TipoDato.ENTERO;
        if (valor.matches("-?\\d+\\.\\d+")) return TipoDato.DECIMAL;
        return tipos.getOrDefault(valor, TipoDato.ENTERO);   // default: heap/entero, como antes
    }

    /**
     * Inferencia por propagación: recorre las cuartetas varias veces hasta que
     * el mapa de tipos deje de cambiar (dataflow de punto fijo, cota de seguridad).
     * NOTA: "call" queda en ENTERO salvo que la propia cuarteta traiga tipoDeclarado
     * (hoy ContextoC3D no lo hace para llamadas — ver comentario al final).
     */
    private static Map<String, TipoDato> inferirTipos(List<Cuarteta> cuartetas) {

        Map<String, TipoDato> tipos = new HashMap<>();

        for (int pasada = 0; pasada < 10; pasada++) {
            boolean cambio = false;

            for (Cuarteta q : cuartetas) {
                String op = q.getOperador();
                String res = q.getResultado();

                // Si la propia cuarteta ya trae el tipo (unaria/binaria con tipoDeclarado), úsalo.
                if (q.getTipoDeclarado() != null && res != null && !res.equals("self")) {
                    cambio |= tipos.put(res, q.getTipoDeclarado()) != q.getTipoDeclarado();
                    continue;
                }

                switch (op) {
                    case "=" -> {
                        TipoDato t = tipoDe(q.getArg1(), tipos);
                        if (res != null) cambio |= tipos.put(res, t) != t;
                    }
                    case "read" -> {
                        if (res != null) cambio |= tipos.put(res, TipoDato.ENTERO) != TipoDato.ENTERO;
                    }
                    case "attr_get", "index_get", "new", "new_array" -> {
                        if (res != null) cambio |= tipos.putIfAbsent(res, TipoDato.ENTERO) == null;
                    }
                    default -> {
                        if (op.startsWith("if_") || res == null) break;
                        // operador binario genérico (+, -, *, ...) sin tipoDeclarado:
                        // hereda TEXTO solo si es "+" y algún operando ya es TEXTO; si no, ENTERO/DECIMAL.
                        TipoDato t1 = tipoDe(q.getArg1(), tipos);
                        TipoDato t2 = q.getArg2() != null ? tipoDe(q.getArg2(), tipos) : t1;
                        TipoDato t = ("+".equals(op) && (t1 == TipoDato.TEXTO || t2 == TipoDato.TEXTO))
                                ? TipoDato.TEXTO
                                : (t1 == TipoDato.DECIMAL || t2 == TipoDato.DECIMAL ? TipoDato.DECIMAL : TipoDato.ENTERO);
                        cambio |= tipos.put(res, t) != t;
                    }
                }
            }

            if (!cambio) break;
        }

        return tipos;
    }

    private static String numero(String valor, String operador) {
        if (valor == null || !valor.matches("\\d+")) {
            throw new IllegalStateException("La cuarteta '" + operador + "' necesita un desplazamiento numérico y recibió '"
                    + valor + "'. Revisa AccesoAtributo y Asignacion.");
        }
        return valor;
    }

    private static String traducirCuarteta(Cuarteta q, List<String> pendientes,
                                           Map<String, Funcion> funciones, TipoDato tipoRetornoFuncion,
                                           Map<String, TipoDato> tipos) {

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
                Funcion destino = funciones.get(a1);
                boolean descartar = (res == null) || destino == null || destino.tipoRetorno == TipoDato.VOID;
                yield (descartar ? llamada : res + " = " + llamada) + ";";
            }

            case "return" -> a1 != null
                    ? "return " + a1 + ";"
                    : (tipoRetornoFuncion == TipoDato.VOID ? "return;" : "return 0;");

            case "print" -> switch (tipoDe(a1, tipos)) {
                case TEXTO   -> "printf(\"%s\", " + a1 + ");";
                case DECIMAL -> "printf(\"%f\\n\", " + a1 + ");";
                default      -> "printf(\"%d\\n\", " + a1 + ");";
            };

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

            default -> {
                if ("+".equals(op) && tipoDe(res, tipos) == TipoDato.TEXTO) {
                    yield res + " = concat(" + a1 + ", " + a2 + ");";
                }
                yield op.startsWith("if_")
                        ? "if (" + a1 + " " + op.substring(3) + " " + a2 + ") goto " + res + ";"
                        : res + " = " + a1 + " " + op + " " + a2 + ";";
            }
        };
    }
}