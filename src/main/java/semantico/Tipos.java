package semantico;

import ast.tipos.Tipo;

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
}
