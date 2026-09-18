package analisis;

import ast.NodoAST;
import enums.TipoArchivo;
import semantico.AnalisisContexto;
import semantico.ErrorSemantico;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompiladorProyecto {

    private final CompiladorArchivo compiladorArchivo = new CompiladorArchivo();

    /* =========================================================
       COMPILACIÓN
       ========================================================= */

    /**
     * Compila todos los archivos del proyecto.
     *
     * Orden:
     *   1. Y?         (define estructuras y funciones)
     *   2. Zetariano  (define clases; puede haber varios .z)
     *   3. Pig Latin  (importa de los anteriores)
     *
     * Cada archivo tiene su PROPIO contexto (tablas frescas). Los
     * imports se resuelven explícitamente en la Fase 2.
     */
    public ResultadoProyecto compilar(
            Map<TipoArchivo, List<File>> archivosPorTipo,
            Function<File, String> proveedorContenido
    ) {

        ResultadoProyecto resultado = new ResultadoProyecto();

        List<TipoArchivo> orden = List.of(
                TipoArchivo.Y_INTERROGACION,
                TipoArchivo.ZETARIANO,
                TipoArchivo.PIG_LATIN
        );

        // ─────── FASE 1: compilar cada archivo con su propio contexto ───────

        for (TipoArchivo tipo : orden) {

            List<File> archivos = archivosPorTipo.get(tipo);

            if (archivos == null || archivos.isEmpty()) {
                resultado.registrarFaltante(tipo,
                        "No se encontró ningún archivo " + tipo.getExtension()
                                + " (" + tipo.getNombreLegible() + ")");
                continue;
            }

            for (File archivo : archivos) {

                String codigo;
                try {
                    codigo = proveedorContenido.apply(archivo);
                } catch (Exception e) {
                    // No hay contexto aún, así que no podemos reportar error
                    // en tabla. Lo dejamos como faltante.
                    resultado.registrarFaltante(tipo,
                            "No se pudo leer " + archivo.getName() + ": " + e.getMessage());
                    continue;
                }

                AnalisisContexto contexto = AnalisisContexto.paraArchivo(
                        archivo.getName(), tipo);

                NodoAST ast;

                // Pig Latin no analiza semántica en Fase 1 (necesita imports)
                if (tipo == TipoArchivo.PIG_LATIN) {
                    ast = compiladorArchivo.analizarSinSemantica(codigo, tipo, contexto);
                } else {
                    ast = compiladorArchivo.analizar(codigo, tipo, contexto);
                }

                resultado.registrarContexto(archivo, contexto);
                resultado.registrarAst(archivo, ast);
            }
        }

        // ─────── FASE 2: resolver imports de Pig Latin ───────

        List<File> pigs = archivosPorTipo.get(TipoArchivo.PIG_LATIN);

        if (pigs != null) {
            for (File pig : pigs) {

                AnalisisContexto ctxPig = resultado.getContexto(pig);
                if (ctxPig == null) continue;

                String codigo = proveedorContenido.apply(pig);
                for (String rutaImport : extraerImports(codigo)) {

                    File archivoImportado = resultado.buscarPorNombre(
                            extraerNombreBase(rutaImport));

                    if (archivoImportado == null) {
                        ctxPig.reportarError(0, 0,
                                "No se encontró el archivo importado: " + rutaImport);
                        continue;
                    }

                    AnalisisContexto ctxImportado = resultado.getContexto(archivoImportado);
                    if (ctxImportado == null) {
                        ctxPig.reportarError(0, 0,
                                "El archivo importado no se compiló: " + rutaImport);
                        continue;
                    }

                    ctxPig.importarDe(ctxImportado);
                }
            }
        }

        // ─────── FASE 3: re-analizar Pig Latin con imports cargados ───────

        if (pigs != null) {
            for (File pig : pigs) {

                AnalisisContexto ctxPig = resultado.getContexto(pig);
                NodoAST astPig = resultado.getAst(pig);

                if (ctxPig == null || astPig == null) continue;

                compiladorArchivo.analizarSemanticamente(astPig, ctxPig);
            }
        }

        return resultado;
    }

    /* =========================================================
       HELPERS DE IMPORTS
       ========================================================= */

    private static final Pattern PATRON_IMPORT = Pattern.compile(
            "import\\s+([\\w./]+\\.(z|y))\\s*;?",
            Pattern.MULTILINE
    );

    private List<String> extraerImports(String codigoPig) {

        List<String> rutas = new ArrayList<>();
        Matcher m = PATRON_IMPORT.matcher(codigoPig);

        while (m.find()) {
            rutas.add(m.group(1));
        }

        return rutas;
    }

    private String extraerNombreBase(String ruta) {

        // 1. Separar la extensión (.z o .y)
        int ultimoPunto = ruta.lastIndexOf('.');
        if (ultimoPunto < 0) return ruta;

        String sinExtension = ruta.substring(0, ultimoPunto);   // "testY"
        String extension = ruta.substring(ultimoPunto);         // ".y"

        // 2. En la parte sin extensión, el último segmento (separado por
        //    punto o barra) es el nombre del archivo.
        String normalizada = sinExtension.replace('\\', '/').replace('.', '/');
        int idx = normalizada.lastIndexOf('/');

        String nombreArchivo = idx >= 0
                ? normalizada.substring(idx + 1)
                : normalizada;

        return nombreArchivo + extension;
    }
}