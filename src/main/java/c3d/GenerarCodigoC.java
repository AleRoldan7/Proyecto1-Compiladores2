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
                actual.parametros.add("int " + q.getArg2());
                continue;
            }

            actual.cuerpo.add(q);
        }

        // Mapa nombre-corto -> nombre-completo (ej. "getDato" -> "Nodo_getDato"),
        // por si alguna cuarteta "call" llega sin el prefijo de clase resuelto.
        Map<String, String> nombreCompleto = new HashMap<>();
        for (String nombre : funciones.keySet()) {
            nombreCompleto.putIfAbsent(nombre, nombre);
            int idx = nombre.lastIndexOf('_');
            if (idx > 0 && idx < nombre.length() - 1) {
                String corto = nombre.substring(idx + 1);
                nombreCompleto.putIfAbsent(corto, nombre);
            }
        }

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

        Set<String> temporales = new TreeSet<>();
        Set<String> variables = new TreeSet<>();

        for (Funcion f : funciones.values()) {
            Set<String> parametrosDeEstaFuncion = new HashSet<>();
            for (String p : f.parametros) {
                parametrosDeEstaFuncion.add(p.substring(p.lastIndexOf(' ') + 1));
            }
            for (Cuarteta q : f.cuerpo) {
                recolectar(q, temporales, variables, parametrosDeEstaFuncion);
            }
        }

        if (main == null) {
            for (Cuarteta q : inicializacion) {
                recolectar(q, temporales, variables, Set.of());
            }
        }

        Map<String, TipoDato> tipos = inferirTiposYRetornos(cuartetas, funciones, nombreCompleto);

        StringBuilder sb = new StringBuilder();
        sb.append("// ★★★ GENERADO CON inferirTiposYRetornos — build de prueba ★★★\n\n");
        sb.append("#include <stdio.h>\n");
        sb.append("#include <stdlib.h>\n");
        sb.append("#include <string.h>\n\n");
        sb.append("#include <stdint.h>\n");
        sb.append("uintptr_t heap[100000];\n");
        sb.append("int hp = 1;   // 0 = null\n\n");
        sb.append("char* concat(const char* a, const char* b) { \n");
        sb.append("    char* buf = malloc(strlen(a) + strlen(b) + 1);\n");
        sb.append("    strcpy(buf, a);\n");
        sb.append("    strcat(buf, b);\n");
        sb.append("    return buf;\n");
        sb.append("}\n\n");
        sb.append("char* numAtexto(int n) {\n");
        sb.append("    char* buf = malloc(32);\n");
        sb.append("    snprintf(buf, 32, \"%d\", n);\n");
        sb.append("    return buf;\n");
        sb.append("}\n\n");

        sb.append("// ==== VARIABLES GLOBALES ====\n");
        for (String v : variables) {
            System.out.println("VARIABLES: " + v + "Tipos: " + tipos.get(v));
            sb.append(tipoDe(v, tipos).aTipoC()).append(" ").append(v).append(";\n");
        }

        sb.append("\n// ==== TEMPORALES ====\n");
        for (String t : temporales) {
            TipoDato tipoT = tipos.get(t);
            if (tipoT == null || tipoT == TipoDato.DESCONOCIDO) {
                // Resultado descartado (ej. llamada a método void) — nunca se lee, el tipo es irrelevante.
                tipoT = TipoDato.ENTERO;
            }
            System.out.println("TEMPORALES: " + t + "Tipos: " + tipos.get(t));
            sb.append(tipoT.aTipoC()).append(" ").append(t).append(";\n");
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
                String linea = traducirCuarteta(q, pendientes, funciones, f.tipoRetorno, tipos, nombreCompleto);
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

    private static void recolectar(Cuarteta q, Set<String> temporales, Set<String> variables,
                                   Set<String> parametrosLocales) {

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
            if (parametrosLocales.contains(arg)) continue;   // parámetro de ESTA función, no va como global
            if (arg.matches("t\\d+")) {
                temporales.add(arg);
            } else if (arg.matches("[A-Za-z_]\\w*") && !arg.matches("L\\d+")) {
                variables.add(arg);
            }
        }
    }

    /** Literal de C3D: "texto", número entero, número decimal, o nombre de variable/temporal ya tipado. */
    private static TipoDato tipoDe(String valor, Map<String, TipoDato> tipos) {

        if (valor == null) {
            return TipoDato.DESCONOCIDO;
        }

        if (valor.startsWith("\"")) {
            return TipoDato.TEXTO;
        }

        if (valor.matches("-?\\d+")) {
            return TipoDato.ENTERO;
        }

        if (valor.matches("-?\\d+\\.\\d+")) {
            return TipoDato.DECIMAL;
        }

        TipoDato tipo = tipos.get(valor);

        if (tipo != null) {
            return tipo;
        }

        return TipoDato.DESCONOCIDO;
    }
    /** Convierte un operando de '+' a char* para concat(), si no lo es ya. */
    private static String aTexto(String valor, Map<String, TipoDato> tipos) {
        return tipoDe(valor, tipos) == TipoDato.TEXTO ? valor : "numAtexto(" + valor + ")";
    }
    /**
     * Punto fijo combinado: tipos de variables/temporales Y tipos de retorno de
     * funciones se recalculan juntos, iterando hasta que ninguno de los dos cambie.
     * Necesario porque tipar un 'call' requiere saber el retorno de la función, y
     * saber el retorno de la función requiere que su 'return' ya esté tipado.
     * Al final deja Funcion.tipoRetorno ya resuelto (side effect intencional).
     * nombreCompleto resuelve nombres cortos de call (ej. "getDato") al nombre real
     * de la función (ej. "Nodo_getDato") por si alguna cuarteta llega sin prefijo.
     */
    private static Map<String, TipoDato> inferirTiposYRetornos(
            List<Cuarteta> cuartetas,
            Map<String, Funcion> funciones,
            Map<String, String> nombreCompleto) {

        Map<String, TipoDato> tipos = new HashMap<>();

        Map<String, TipoDato> retornosFuncion = new HashMap<>();

        for (String nombre : funciones.keySet()) {
            retornosFuncion.put(nombre, TipoDato.VOID);
        }

        for (int pasada = 0; pasada < 10; pasada++) {

            boolean cambio = false;

            for (Cuarteta q : cuartetas) {

                String op = q.getOperador();
                String res = q.getResultado();

                /*
                 * Si la cuarteta ya trae el tipo desde el AST/semántica,
                 * ese tipo tiene prioridad.
                 */
                if (q.getTipoDeclarado() != null
                        && res != null
                        && !res.equals("self")) {

                    TipoDato anterior = tipos.put(
                            res,
                            q.getTipoDeclarado()
                    );

                    if (anterior != q.getTipoDeclarado()) {
                        cambio = true;
                    }

                    continue;
                }

                switch (op) {

                    case "=" -> {

                        if (res == null) {
                            break;
                        }

                        TipoDato tipo = tipoDe(
                                q.getArg1(),
                                tipos
                        );

                        TipoDato anterior = tipos.put(
                                res,
                                tipo
                        );

                        if (anterior != tipo) {
                            cambio = true;
                        }
                    }

                    case "read" -> {

                        if (res == null) {
                            break;
                        }

                        TipoDato anterior = tipos.put(
                                res,
                                TipoDato.ENTERO
                        );

                        if (anterior != TipoDato.ENTERO) {
                            cambio = true;
                        }
                    }

                    case "new" -> {

                        if (res == null) {
                            break;
                        }

                        /*
                         * new crea una referencia al heap.
                         */
                        TipoDato anterior = tipos.put(
                                res,
                                TipoDato.OBJETO
                        );

                        if (anterior != TipoDato.OBJETO) {
                            cambio = true;
                        }
                    }

                    case "new_array" -> {

                        if (res == null) {
                            break;
                        }

                        /*
                         * La referencia al arreglo también es una
                         * dirección dentro del heap.
                         */
                        TipoDato anterior = tipos.put(
                                res,
                                TipoDato.OBJETO
                        );

                        if (anterior != TipoDato.OBJETO) {
                            cambio = true;
                        }
                    }

                    case "attr_get", "index_get" -> {

                        if (res == null) {
                            break;
                        }

                        /*
                         * NO ponemos ENTERO por defecto aquí.
                         *
                         * Si la cuarteta tiene tipoDeclarado ya se
                         * procesó arriba.
                         *
                         * Si no lo tiene, esperamos a que otra regla
                         * pueda determinarlo.
                         */
                        TipoDato actual = tipos.get(res);

                        if (actual == null) {
                            tipos.put(res, TipoDato.DESCONOCIDO);
                            cambio = true;
                        }
                    }

                    case "call" -> {

                        if (res == null) {
                            break;
                        }

                        String nombreReal =
                                nombreCompleto.getOrDefault(
                                        q.getArg1(),
                                        q.getArg1()
                                );

                        TipoDato tipoRetorno =
                                retornosFuncion.get(
                                        nombreReal
                                );

                        if (tipoRetorno != null
                                && tipoRetorno != TipoDato.VOID) {

                            TipoDato anterior =
                                    tipos.put(
                                            res,
                                            tipoRetorno
                                    );

                            if (anterior != tipoRetorno) {
                                cambio = true;
                            }
                        }
                    }

                    default -> {

                        if (res == null || op.startsWith("if_")) {
                            break;
                        }

                        TipoDato tipoIzquierda =
                                tipoDe(q.getArg1(), tipos);

                        TipoDato tipoDerecha =
                                q.getArg2() != null
                                        ? tipoDe(q.getArg2(), tipos)
                                        : tipoIzquierda;

                        TipoDato resultado;

                        if ("+".equals(op)
                                && (tipoIzquierda == TipoDato.TEXTO
                                || tipoDerecha == TipoDato.TEXTO)) {

                            resultado = TipoDato.TEXTO;

                        } else if (tipoIzquierda == TipoDato.DECIMAL
                                || tipoDerecha == TipoDato.DECIMAL) {

                            resultado = TipoDato.DECIMAL;

                        } else if (tipoIzquierda == TipoDato.DESCONOCIDO
                                || tipoDerecha == TipoDato.DESCONOCIDO) {

                            resultado = TipoDato.DESCONOCIDO;

                        } else {

                            resultado = TipoDato.ENTERO;
                        }

                        TipoDato anterior =
                                tipos.put(res, resultado);

                        if (anterior != resultado) {
                            cambio = true;
                        }
                    }
                }
            }

            /*
             * Resolver retornos de funciones.
             */
            for (Funcion f : funciones.values()) {

                TipoDato retorno = TipoDato.VOID;

                for (Cuarteta q : f.cuerpo) {

                    if (!"return".equals(q.getOperador())) {
                        continue;
                    }

                    if (q.getArg1() == null) {
                        retorno = TipoDato.VOID;
                        continue;
                    }

                    retorno = tipoDe(
                            q.getArg1(),
                            tipos
                    );

                    if (retorno == TipoDato.DESCONOCIDO) {
                        retorno = TipoDato.ENTERO;
                    }
                }

                TipoDato anterior =
                        retornosFuncion.put(
                                f.nombre,
                                retorno
                        );

                if (anterior != retorno) {
                    cambio = true;
                }
            }

            if (!cambio) {
                break;
            }
        }

        /*
         * Guardar los tipos finales de las funciones.
         */
        for (Funcion f : funciones.values()) {

            f.tipoRetorno =
                    retornosFuncion.getOrDefault(
                            f.nombre,
                            TipoDato.VOID
                    );
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
                                           Map<String, TipoDato> tipos, Map<String, String> nombreCompleto) {

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
                String nombreReal = nombreCompleto.getOrDefault(a1, a1);
                String llamada = nombreReal + "(" + String.join(", ", pendientes) + ")";
                pendientes.clear();
                Funcion destino = funciones.get(nombreReal);
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
            case "attr_get" -> {

                TipoDato tipoResultado =
                        tipoDe(res, tipos);

                String cast = "";

                if (tipoResultado == TipoDato.TEXTO) {
                    cast = "(char*)";
                }

                yield res
                        + " = "
                        + cast
                        + "heap["
                        + a1
                        + " + "
                        + numero(a2, op)
                        + "];";
            }
            case "field_set" -> {

                TipoDato tipoValor =
                        tipoDe(a2, tipos);

                String valor = a2;

                if (tipoValor == TipoDato.TEXTO
                        || tipoValor == TipoDato.OBJETO
                        || tipoValor == TipoDato.ESTRUCTURA) {

                    valor = "(uintptr_t)" + a2;
                }

                yield "heap["
                        + res
                        + " + "
                        + numero(a1, op)
                        + "] = "
                        + valor
                        + ";";
            }
            case "index_get" -> {

                TipoDato tipoResultado =
                        tipoDe(res, tipos);

                String cast = "";

                if (tipoResultado == TipoDato.TEXTO) {
                    cast = "(char*)";
                }

                yield res
                        + " = "
                        + cast
                        + "heap["
                        + a1
                        + " + "
                        + a2
                        + "];";
            }
            case "index_set" -> "heap[" + res + " + " + a1 + "] = " + a2 + ";";

            default -> {
                if ("+".equals(op) && tipoDe(res, tipos) == TipoDato.TEXTO) {
                    String argA = aTexto(a1, tipos);
                    String argB = aTexto(a2, tipos);
                    yield res + " = concat(" + argA + ", " + argB + ");";
                }
                yield op.startsWith("if_")
                        ? "if (" + a1 + " " + op.substring(3) + " " + a2 + ") goto " + res + ";"
                        : res + " = " + a1 + " " + op + " " + a2 + ";";
            }
        };
    }
}