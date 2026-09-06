package enums;

import java.io.File;
import java.util.Locale;

/**
 * Identifica a cuál de los 3 lenguajes de la resistencia pertenece un archivo,
 * a partir de su extensión.
 */
public enum TipoArchivo {

    Y_INTERROGACION(".y", "Y?"),
    ZETARIANO(".z", "Zetariano"),
    PIG_LATIN(".pig", "Pig Latin"),
    DESCONOCIDO("", "Desconocido");

    private final String extension;
    private final String nombreLegible;

    TipoArchivo(String extension, String nombreLegible) {
        this.extension = extension;
        this.nombreLegible = nombreLegible;
    }

    public String getExtension() {
        return extension;
    }

    public String getNombreLegible() {
        return nombreLegible;
    }

    public static TipoArchivo porArchivo(File archivo) {
        return porNombre(archivo == null ? null : archivo.getName());
    }

    public static TipoArchivo porNombre(String nombreArchivo) {
        if (nombreArchivo == null) return DESCONOCIDO;

        String nombre = nombreArchivo.toLowerCase(Locale.ROOT);

        if (nombre.endsWith(".y")) return Y_INTERROGACION;
        if (nombre.endsWith(".z")) return ZETARIANO;
        if (nombre.endsWith(".pig")) return PIG_LATIN;

        return DESCONOCIDO;
    }
}
