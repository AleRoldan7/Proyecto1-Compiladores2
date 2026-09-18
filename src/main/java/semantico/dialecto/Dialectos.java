package semantico.dialecto;

import enums.TipoArchivo;

/** Punto único donde se elige el Dialecto según la extensión del archivo. */
public final class Dialectos {

    public static final Dialecto Y         = new DialectoY();
    public static final Dialecto ZETARIANO = new DialectoZetariano();
    public static final Dialecto PIG_LATIN = new DialectoPigLatin();

    private Dialectos() {
    }

    public static Dialecto de(TipoArchivo tipoArchivo) {

        return switch (tipoArchivo) {
            case Y_INTERROGACION -> Y;
            case ZETARIANO       -> ZETARIANO;
            case PIG_LATIN       -> PIG_LATIN;
            case DESCONOCIDO     -> throw new IllegalArgumentException(
                    "No hay dialecto para un archivo de tipo desconocido");
        };
    }
}