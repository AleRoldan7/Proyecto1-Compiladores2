package analisis;

import enums.TipoArchivo;
import semantico.AnalisisContexto;
import tablas.TablaSimbolos;
import tablas.TablaTipos;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Compila LOS TRES lenguajes del proyecto a la vez, compartiendo una sola
 * TablaSimbolos/TablaTipos (así el Pig Latin puede "ver" lo que Y? y
 * Zetariano definieron). El orden de compilación es fijo:
 *
 *   1) Y?        (define estructuras y funciones, sin variables globales)
 *   2) Zetariano (clases; puede haber varios archivos .z)
 *   3) Pig Latin (importa de los anteriores; siempre al final)
 *
 * Cada archivo se compila con contexto.setArchivoActual(nombreDelArchivo),
 * así que cualquier error (léxico, sintáctico o semántico) queda etiquetado
 * con el archivo exacto que falló.
 */
public class CompiladorProyecto {

    private final CompiladorArchivo compiladorArchivo = new CompiladorArchivo();

    /**
     * @param archivosPorTipo  archivos detectados en el árbol de trabajo, agrupados por lenguaje
     * @param proveedorContenido  cómo obtener el texto de un archivo (desde disco o desde una pestaña
     *                             abierta sin guardar todavía); normalmente:
     *                             archivo -> Files.readString(archivo.toPath())
     */
    public ResultadoProyecto compilar(
            Map<TipoArchivo, List<File>> archivosPorTipo,
            Function<File, String> proveedorContenido
    ) {
        AnalisisContexto contexto = new AnalisisContexto(new TablaSimbolos(), new TablaTipos());
        ResultadoProyecto resultado = new ResultadoProyecto(contexto);

        List<TipoArchivo> orden = List.of(
                TipoArchivo.Y_INTERROGACION,
                TipoArchivo.ZETARIANO,
                TipoArchivo.PIG_LATIN
        );

        for (TipoArchivo tipo : orden) {
            List<File> archivos = archivosPorTipo.get(tipo);

            if (archivos == null || archivos.isEmpty()) {
                resultado.registrarFaltante(tipo, "No se encontró ningún archivo " + tipo.getExtension()
                        + " (" + tipo.getNombreLegible() + ") en el árbol de trabajo");
                continue;
            }

            for (File archivo : archivos) {
                contexto.setArchivoActual(archivo.getName());
                try {
                    String codigo = proveedorContenido.apply(archivo);
                    var ast = compiladorArchivo.analizar(codigo, tipo, contexto);
                    resultado.registrarAst(archivo, ast);
                } catch (Exception e) {
                    contexto.reportarError(0, 0, "No se pudo leer/compilar el archivo: " + e.getMessage());
                }
            }
        }

        return resultado;
    }
}
