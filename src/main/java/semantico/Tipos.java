package semantico;

import ast.tipos.Tipo;
import tablas.MetodoRecord;

import java.util.Set;

public class Tipos {

    public static final String INT = "int";
    public static final String DOUBLE = "double";
    public static final String CHAR = "char";
    public static final String BOOLEAN = "boolean";
    public static final String STRING = "String";
    public static final String VOID = "void";
    public static final String NULL = "null";

    private static final Set<String> NUMERICOS = Set.of(INT, DOUBLE);

    private Tipos() {
    }

    public static Tipo simple(int linea, int columna, String nombre) {
        return new Tipo(linea, columna, nombre, false, 0);
    }

    public static Tipo entero(int linea, int columna) {
        return simple(linea, columna, INT);
    }

    public static Tipo decimal(int linea, int columna) {
        return simple(linea, columna, DOUBLE);
    }

    public static Tipo caracter(int linea, int columna) {
        return simple(linea, columna, CHAR);
    }

    public static Tipo booleano(int linea, int columna) {
        return simple(linea, columna, BOOLEAN);
    }

    public static Tipo cadena(int linea, int columna) {
        return simple(linea, columna, STRING);
    }

    public static boolean esNumerico(Tipo tipo) {
        return tipo != null && !tipo.isArreglo() && NUMERICOS.contains(tipo.getNombre());
    }

    public static boolean esBooleano(Tipo tipo) {
        return tipo != null && !tipo.isArreglo() && BOOLEAN.equals(tipo.getNombre());
    }

    public static boolean esNull(Tipo tipo) {
        return tipo != null && NULL.equals(tipo.getNombre());
    }

    /**
     * ¿Se puede usar 'real' en un lugar que espera 'esperado'?
     *
     * Reglas:
     *  - Mismo nombre, mismo arreglo, misma cantidad de dimensiones -> sí.
     *  - Promoción numérica: int encaja donde se espera double.
     *  - null encaja en cualquier tipo objeto/arreglo (no en primitivos).
     */
    public static boolean sonCompatibles(Tipo esperado, Tipo real) {

        if (esperado == null || real == null) {
            return false;
        }

        if (esNull(real)) {
            return esperado.isArreglo() || !esPrimitivo(esperado);
        }

        if (esperado.isArreglo() != real.isArreglo()) {
            return false;
        }

        if (esperado.isArreglo() && esperado.getDimensiones() != real.getDimensiones()) {
            return false;
        }

        if (esperado.getNombre().equals(real.getNombre())) {
            return true;
        }

        // int -> double, solo para tipo simple (no arreglo)
        return !esperado.isArreglo() && DOUBLE.equals(esperado.getNombre()) && INT.equals(real.getNombre());
    }

    private static boolean esPrimitivo(Tipo tipo) {
        String n = tipo.getNombre();
        return INT.equals(n) || DOUBLE.equals(n) || CHAR.equals(n) || BOOLEAN.equals(n);
    }

    public static String base(Tipo tipo) {

        if (tipo == null) {
            return null;
        }

        int idx = tipo.getNombre().indexOf('[');

        return idx == -1 ? tipo.getNombre() : tipo.getNombre().substring(0, idx);
    }

    public static String describir(Tipo tipo) {

        if (tipo == null) {
            return "?";
        }

        StringBuilder stringBuilder = new StringBuilder(tipo.getNombre());

        for (int i = 0; i < tipo.getDimensiones(); i++) {
            stringBuilder.append("[]");
        }

        return stringBuilder.toString();
    }

    public static Tipo desdeTexto(int linea, int columna, String nombreCompleto) {

        if (nombreCompleto == null) {
            return null;
        }

        int dimensiones = 0;
        String base = nombreCompleto;

        while (base.endsWith("[]")) {
            base = base.substring(0, base.length() - 2);
            dimensiones++;
        }

        return new Tipo(linea, columna, nombreCompleto, dimensiones > 0, dimensiones);
    }


    /**
     * Construye el Tipo de un arreglo a partir del tipo base y la
     * cantidad de dimensiones (usado por CrearArreglo y por
     * AccesoArreglo cuando quedan dimensiones sin indexar).
     */
    public static Tipo arreglo(int linea, int columna, String tipoBase, int dimensiones) {

        StringBuilder nombre = new StringBuilder(tipoBase);

        for (int i = 0; i < dimensiones; i++) {
            nombre.append("[]");
        }

        return new Tipo(linea, columna, nombre.toString(), true, dimensiones);
    }

    public static MetodoRecord resolverSobrecarga(
            java.util.List<MetodoRecord> candidatos, java.util.List<Tipo> tiposArgumentos) {

        for (tablas.MetodoRecord candidato : candidatos) {

            if (candidato.parametros().size() != tiposArgumentos.size()) {
                continue;
            }

            boolean todosCompatibles = true;

            for (int i = 0; i < tiposArgumentos.size(); i++) {

                Tipo esperado = candidato.parametros().get(i).getTipoParametro();
                Tipo real = tiposArgumentos.get(i);

                if (!sonCompatibles(esperado, real)) {
                    todosCompatibles = false;
                    break;
                }
            }

            if (todosCompatibles) {
                return candidato;
            }
        }

        return null; // ninguna firma matchea
    }
}
