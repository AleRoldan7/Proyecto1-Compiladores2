package semantico;

import enums.TipoErrorSemantico;

public record ErrorSemantico(String archivo, int linea, int columna, String mensaje, TipoErrorSemantico tipoError) {

    public static ErrorSemantico error(String archivo, int linea, int columna, String mensaje) {
        return new ErrorSemantico(archivo, linea, columna, mensaje, TipoErrorSemantico.ERROR);
    }

    public static ErrorSemantico advertencia(String archivo, int linea, int columna, String mensaje) {
        return new ErrorSemantico(archivo, linea, columna, mensaje, TipoErrorSemantico.ADVERTENCIA);
    }

    @Override
    public String toString() {
        return "[" + tipoError + "] " + archivo + ":" + linea + ":" + columna + "->" + mensaje;
    }
}
