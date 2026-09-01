package enums;

public enum TipoDato {

    ENTERO,
    DECIMAL,
    TEXTO,
    CARACTER,
    BOOLEANO,
    VOID,
    ESTRUCTURA,
    OBJETO,
    DESCONOCIDO;

    public boolean esNumero() {
        return this == ENTERO || this == DECIMAL;
    }

    public boolean esPrimitivo() {
        return this == ENTERO || this == DECIMAL || this == TEXTO || this == CARACTER || this == BOOLEANO;
    }

    public boolean esReferencia() {
        return this == ESTRUCTURA || this == OBJETO;
    }

    public String aTipoC() {
        return switch (this) {
            case ENTERO -> "int";
            case DECIMAL -> "double";
            case CARACTER -> "char";
            case BOOLEANO -> "int";
            case TEXTO -> "char*";
            case ESTRUCTURA, OBJETO -> "void*";
            case VOID -> "void";
            case DESCONOCIDO -> throw new IllegalStateException("Un tipo DESCONOCIDO no genera código C");
        };
    }
}
