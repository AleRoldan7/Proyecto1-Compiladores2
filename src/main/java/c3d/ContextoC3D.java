package c3d;

import enums.TipoDato;

import java.util.*;

public class ContextoC3D {

    private final List<Cuarteta> cuartetas = new ArrayList<>();
    private final Set<String> funciones = new LinkedHashSet<>();
    private final List<String> cadenas = new ArrayList<>();
    private final Map<String, Map<String, Integer>> disposiciones = new HashMap<>();   // clase -> atributo -> desplazamiento
    private final Deque<String[]> destinos = new ArrayDeque<>();                        // {continue, break}; continue = null en un switch

    private int contadorTemporales = 0;
    private int contadorEtiquetas = 0;
    private String claseActual;

    // ---------- Temporales y etiquetas ----------

    public String nuevoTemporal() {
        return "t" + contadorTemporales++;
    }

    public String nuevaEtiqueta() {
        return "L" + contadorEtiquetas++;
    }

    // ---------- Emisión de cuartetas ----------

    /** (operador, arg1, arg2, resultado) */
    public void agregar(String operador, String arg1, String arg2, String resultado) {
        cuartetas.add(new Cuarteta(operador, arg1, arg2, resultado, null));
    }

    public void emitir(String operador, String arg1, String arg2, String resultado) {
        agregar(operador, arg1, arg2, resultado);
    }

    public void agregarEtiqueta(String etiqueta) {
        agregar("label", null, null, etiqueta);
    }

    public void salto(String etiqueta) {
        agregar("goto", null, null, etiqueta);
    }

    public void saltoSiVerdadero(String condicion, String etiqueta) {
        agregar("if_true", condicion, null, etiqueta);
    }

    public void saltoSiFalso(String condicion, String etiqueta) {
        agregar("if_false", condicion, null, etiqueta);
    }

    public void emitirComentario(String texto) {
        agregar("comment", null, null, texto);
    }

    /** t = operador operando */
    public String unaria(String operador, String operando, TipoDato tipo) {
        String temporal = nuevoTemporal();
        cuartetas.add(new Cuarteta(operador, operando, null, temporal, tipo));
        return temporal;
    }

    /** t = izquierda operador derecha */
    public String binaria(String operador, String izquierda, String derecha, TipoDato tipo) {
        String temporal = nuevoTemporal();
        cuartetas.add(new Cuarteta(operador, izquierda, derecha, temporal, tipo));
        return temporal;
    }

    /** destino = valor */
    public void asignar(String destino, String valor) {
        cuartetas.add(new Cuarteta("=", valor, null, destino, null));
    }

    public List<Cuarteta> getCuartetas() {
        return cuartetas;
    }

    // ---------- Funciones ----------

    /** Funciones globales (.y) y main: se llaman por su nombre. Métodos (.z): Clase_metodo. */
    public static String nombreFuncion(String clase, String metodo) {
        return clase == null ? metodo : clase + "_" + metodo;
    }

    /** Igual que en tu nodo Constructor: init_Clase */
    public static String nombreConstructor(String clase) {
        return "init_" + clase;
    }

    public void registrarFuncion(String nombre) {
        funciones.add(nombre);
    }

    public boolean existeFuncion(String nombre) {
        return funciones.contains(nombre);
    }

    public Set<String> getFunciones() {
        return funciones;
    }

    public void abrirFuncion(String nombre, List<String> parametros) {
        registrarFuncion(nombre);
        agregarEtiqueta("func_" + nombre);
        for (String parametro : parametros) {
            agregar("param_decl", "int", parametro, null);
        }
    }

    public void cerrarFuncion(String nombre) {
        agregarEtiqueta("end_" + nombre);
    }

    // ---------- Cadenas ----------

    /**
     * Devuelve el literal ya listo para C. GenerarCodigoC lo detecta porque empieza
     * con comillas y lo imprime con %s. Si tu lexer deja comillas sin escapar dentro
     * del texto, escápalas aquí.
     */


    public List<String> getCadenas() {
        return cadenas;
    }

    // ---------- Objetos en el heap ----------

    public void registrarClase(String clase, List<String> atributos) {
        Map<String, Integer> mapa = new LinkedHashMap<>();
        int desplazamiento = 0;
        for (String atributo : atributos) {
            mapa.put(atributo, desplazamiento++);
        }
        disposiciones.put(clase, mapa);
    }

    private Map<String, Integer> disposicion(String clase) {
        Map<String, Integer> mapa = disposiciones.get(clase);
        if (mapa == null) {
            throw new IllegalStateException("La clase '" + clase + "' no tiene disposición en el heap (¿se generó antes su archivo?)");
        }
        return mapa;
    }



    public int tamanio(String clase) {
        return disposicion(clase).size();
    }

    public String getClaseActual() {
        return claseActual;
    }

    public void setClaseActual(String claseActual) {
        this.claseActual = claseActual;
    }

    public boolean esAtributo(String nombre) {
        return claseActual != null && disposicion(claseActual).containsKey(nombre);
    }

    // ---------- break / continue (ciclos y switch comparten la pila) ----------

    public void entrarCiclo(String etiquetaContinue, String etiquetaBreak) {
        destinos.push(new String[]{etiquetaContinue, etiquetaBreak});
    }

    public void salirCiclo() {
        destinos.pop();
    }

    /** Un switch admite break pero no continue: el continue sigue apuntando al ciclo de afuera. */
    public void entrarSwitch(String etiquetaBreak) {
        destinos.push(new String[]{null, etiquetaBreak});
    }

    public void salirSwitch() {
        destinos.pop();
    }

    public String etiquetaBreak() {
        return destinos.isEmpty() ? null : destinos.peek()[1];
    }

    public String etiquetaContinue() {
        for (String[] destino : destinos) {          // recorre desde el más interno
            if (destino[0] != null) {
                return destino[0];
            }
        }
        return null;
    }

    private final Set<String> locales = new HashSet<>();


    public void declararLocal(String nombre) {
        locales.add(nombre);
    }

    public boolean esLocal(String nombre) {
        return locales.contains(nombre);
    }

    public static boolean esImpresion(String nombre) {
        return Set.of("print", "println", "imprimir").contains(nombre);
    }

    public static boolean esLectura(String nombre) {
        return Set.of("readln", "leer").contains(nombre);
    }

    /** El texto del lexer ya trae sus comillas: no se duplican. */
    public String registrarString(String texto) {
        cadenas.add(texto);
        String contenido = texto.replace("\r", "").replace("\n", "\\n").replace("\t", "\\t");
        boolean yaTieneComillas = contenido.length() >= 2 && contenido.startsWith("\"") && contenido.endsWith("\"");
        return yaTieneComillas ? contenido : "\"" + contenido + "\"";
    }

    public int desplazamiento(String clase, String atributo) {
        Integer d = disposicion(clase).get(atributo);
        if (d == null) {
            throw new IllegalStateException("La clase '" + clase + "' no tiene el atributo '" + atributo + "'");
        }
        return d;
    }

    /** Si no se conoce la clase del objeto, se busca en todas; falla si el nombre es ambiguo. */
    public int desplazamientoDe(String clase, String atributo) {

        if (clase != null) {
            return desplazamiento(clase, atributo);
        }

        Integer encontrado = null;

        for (Map<String, Integer> mapa : disposiciones.values()) {
            Integer d = mapa.get(atributo);
            if (d == null) continue;
            if (encontrado != null && !encontrado.equals(d)) {
                throw new IllegalStateException("El atributo '" + atributo
                        + "' está en varias clases con distinta posición: falta conocer la clase del objeto");
            }
            encontrado = d;
        }

        if (encontrado == null) {
            throw new IllegalStateException("Ninguna clase tiene el atributo '" + atributo + "'");
        }

        return encontrado;
    }
}