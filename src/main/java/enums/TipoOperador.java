package enums;

import java.util.Arrays;

public enum TipoOperador {

    ARITMETICO("+", "-", "/", "*", "%"),
    RELACIONAL("<", ">", "<=", ">="),
    LOGICO("&&", "||"),
    IGUALDAD("==", "!=");

    private final String[] operadores;

    TipoOperador(String... operadores) {
        this.operadores = operadores;
    }

    public boolean contieneOperador(String operador) {
        return Arrays.asList(operadores).contains(operador);
    }

    public static TipoOperador obtenerTipoOperador(String operador) {

        for (TipoOperador tipoOperador : TipoOperador.values()) {
            if (tipoOperador.contieneOperador(operador)) {
                return tipoOperador;
            }
        }

        return null;
    }
}
