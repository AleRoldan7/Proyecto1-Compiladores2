package semantico;

import ast.tipos.Tipo;
import enums.TipoArchivo;
import semantico.dialecto.Dialecto;
import semantico.dialecto.Dialectos;
import tablas.MetodoRecord;
import enums.TipoDato;

import java.util.ArrayList;
import java.util.List;
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
    private static List<Dialecto> dialectosValidos;

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

        return new Tipo(linea, columna, base, dimensiones > 0, dimensiones);

    }



    public static Tipo arreglo(int linea, int columna, String tipoBase, int dimensiones) {

        StringBuilder nombre = new StringBuilder(tipoBase);

        for (int i = 0; i < dimensiones; i++) {
            nombre.append("[]");
        }

        return new Tipo(linea, columna, nombre.toString(), true, dimensiones);
    }

    public static MetodoRecord resolverSobrecarga(
            List<MetodoRecord> candidatos, List<Tipo> tiposArgumentos, AnalisisContexto contexto) {

        for (MetodoRecord candidato : candidatos) {

            if (candidato.parametros().size() != tiposArgumentos.size()) {
                continue;
            }

            boolean todosCompatibles = true;

            for (int i = 0; i < tiposArgumentos.size(); i++) {

                Tipo esperado = candidato.parametros().get(i).getTipoParametro();

                if (!asignable(esperado, tiposArgumentos.get(i), contexto)) {
                    todosCompatibles = false;
                    break;
                }
            }

            if (todosCompatibles) {
                return candidato;
            }
        }

        return null;
    }


    public static TipoDato canonico(Tipo tipo, AnalisisContexto contexto) {

        if (tipo == null) {
            return null;
        }

        String nombre = base(tipo);

        // 1. Nombres del dialecto del archivo que se analiza ahora (numerus en .pig)
        TipoDato primitivo = contexto.getDialecto().tipoPrimitivo(nombre);
        if (primitivo != null) {
            return primitivo;
        }

        // 2. Nombres que vienen de otro archivo importado (Pila.z devuelve "int")
        primitivo = primitivoDeCualquierDialecto(nombre);
        if (primitivo != null) {
            return primitivo;
        }

        // 3. Clases
        return contexto.getTablaTipos().existeTipo(nombre) ? TipoDato.OBJETO : null;
    }

    private static List<Dialecto> dialectosValidos() {

        if (dialectosValidos == null) {

            List<Dialecto> lista = new ArrayList<>();

            for (TipoArchivo archivo : TipoArchivo.values()) {
                try {
                    lista.add(Dialectos.de(archivo));
                } catch (RuntimeException e) {
                    // Tipo de archivo sin dialecto (DESCONOCIDO): se omite
                }
            }

            dialectosValidos = lista;
        }

        return dialectosValidos;
    }

    private static TipoDato primitivoDeCualquierDialecto(String nombre) {

        for (Dialecto dialecto : dialectosValidos()) {
            TipoDato tipoDato = dialecto.tipoPrimitivo(nombre);
            if (tipoDato != null) {
                return tipoDato;
            }
        }

        return null;
    }

    public static boolean esNumerico(Tipo tipo, AnalisisContexto contexto) {

        if (tipo == null || tipo.isArreglo()) {
            return false;
        }

        TipoDato tipoDato = canonico(tipo, contexto);

        return tipoDato != null && tipoDato.esNumero();
    }

    public static boolean esBooleano(Tipo tipo, AnalisisContexto contexto) {
        return !esArreglo(tipo) && canonico(tipo, contexto) == TipoDato.BOOLEANO;
    }

    public static boolean esTexto(Tipo tipo, AnalisisContexto contexto) {
        return !esArreglo(tipo) && canonico(tipo, contexto) == TipoDato.TEXTO;
    }

    private static boolean esArreglo(Tipo tipo) {
        return tipo == null || tipo.isArreglo();
    }


    public static boolean asignable(Tipo esperado, Tipo real, AnalisisContexto contexto) {

        if (esperado == null || real == null) {
            return false;
        }

        if (esNull(real)) {
            return esperado.isArreglo() || canonico(esperado, contexto) == TipoDato.OBJETO;
        }

        if (esperado.isArreglo() != real.isArreglo()) {
            return false;
        }

        if (esperado.isArreglo()) {
            return esperado.getDimensiones() == real.getDimensiones()
                    && base(esperado).equals(base(real));
        }

        if (base(esperado).equals(base(real))) {
            return true;
        }

        return contexto.getDialecto().asignable(
                canonico(esperado, contexto),
                canonico(real, contexto)
        );
    }


    public static Tipo resultadoBinario(String operador, Tipo izquierda, Tipo derecha,
                                        int linea, int columna, AnalisisContexto contexto) {

        if (izquierda == null || derecha == null || izquierda.isArreglo() || derecha.isArreglo()) {
            return null;
        }

        TipoDato resultado = contexto.getDialecto().resultadoDe(
                operador,
                canonico(izquierda, contexto),
                canonico(derecha, contexto)
        );

        if (resultado == null) {
            return null;
        }

        return simple(linea, columna, contexto.getDialecto().nombrarTipo(resultado));
    }

    public static String describir(Tipo tipo, AnalisisContexto contexto) {

        if (tipo == null) {
            return "?";
        }

        TipoDato tipoDato = canonico(tipo, contexto);

        String base = tipoDato == null || tipoDato == TipoDato.OBJETO
                ? base(tipo)
                : contexto.getDialecto().nombrarTipo(tipoDato);

        return base + "[]".repeat(Math.max(0, tipo.getDimensiones()));
    }
}