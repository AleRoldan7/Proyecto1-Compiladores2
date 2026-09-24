package analisis;

import ast.NodoAST;
import c3d.GeneradorC3DCompleto;
import enums.TipoArchivo;
import semantico.AnalisisContexto;
import semantico.ErrorSemantico;
import semantico.analizadores.AnalizadorClase;
import semantico.coordinadorsemantico.AnalizadorSemanticoCoordinador;
import semantico.coordinadorsemantico.InferirTipoCoordinador;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompiladorProyecto {

    private final CompiladorArchivo compiladorArchivo = new CompiladorArchivo();
    private final Map<File, List<File>> importsPorPig = new HashMap<>();
    public ResultadoProyecto compilar(
            Map<TipoArchivo, List<File>> archivosPorTipo,
            Function<File, String> proveedorContenido
    ) {

        ResultadoProyecto resultado = new ResultadoProyecto();
        importsPorPig.clear();
        // ─────── FASE 1: Y? (estructuras y funciones) ───────
        compilarY(archivosPorTipo, proveedorContenido, resultado);

        // ─────── FASE 2: Zetariano en DOS PASADAS ───────
        compilarZ(archivosPorTipo, proveedorContenido, resultado);

        // ─────── FASE 3: Pig Latin (parsear sin semántica) ───────
        compilarPigSinSemantica(archivosPorTipo, proveedorContenido, resultado);

        // ─────── FASE 4: resolver imports de Pig Latin ───────
        resolverImportsPig(archivosPorTipo, proveedorContenido, resultado);

        // ─────── FASE 5: analizar semánticamente Pig Latin ───────
        analizarPigConImports(archivosPorTipo, resultado);

        return resultado;
    }

    /* =========================================================
       FASE 1: Y?
       ========================================================= */

    private void compilarY(
            Map<TipoArchivo, List<File>> archivosPorTipo,
            Function<File, String> proveedorContenido,
            ResultadoProyecto resultado) {

        List<File> archivos = archivosPorTipo.get(TipoArchivo.Y_INTERROGACION);
        if (archivos == null || archivos.isEmpty()) return;

        for (File archivo : archivos) {
            try {
                String codigo = proveedorContenido.apply(archivo);
                AnalisisContexto ctx = AnalisisContexto.paraArchivo(
                        archivo.getName(), TipoArchivo.Y_INTERROGACION);

                NodoAST ast = compiladorArchivo.analizar(codigo, TipoArchivo.Y_INTERROGACION, ctx);

                resultado.registrarContexto(archivo, ctx);
                resultado.registrarAst(archivo, ast);

            } catch (Exception e) {
                resultado.registrarFaltante(TipoArchivo.Y_INTERROGACION,
                        "Error en " + archivo.getName() + ": " + e.getMessage());
            }
        }
    }

    /* =========================================================
       FASE 2: Zetariano en dos pasadas
       ========================================================= */

    private void compilarZ(
            Map<TipoArchivo, List<File>> archivosPorTipo,
            Function<File, String> proveedorContenido,
            ResultadoProyecto resultado) {

        List<File> archivos = archivosPorTipo.get(TipoArchivo.ZETARIANO);
        if (archivos == null || archivos.isEmpty()) return;

        System.out.println("\n========== ZETARIANO ==========");
        System.out.println("Archivos encontrados: " + archivos.size());
        for (File f : archivos) {
            System.out.println("  - " + f.getName());
        }

        // ────────── PASADA 1: parsear todos los .z ──────────
        List<RegistroZ> registros = new ArrayList<>();

        for (File archivo : archivos) {
            try {
                String codigo = proveedorContenido.apply(archivo);
                AnalisisContexto ctx = AnalisisContexto.paraArchivo(
                        archivo.getName(), TipoArchivo.ZETARIANO);

                NodoAST ast = compiladorArchivo.analizarSinSemantica(
                        codigo, TipoArchivo.ZETARIANO, ctx);

                registros.add(new RegistroZ(archivo, codigo, ctx, ast));
                resultado.registrarContexto(archivo, ctx);
                resultado.registrarAst(archivo, ast);

                System.out.println("[PASADA 1] Parseado OK: " + archivo.getName() +
                        " | clases en AST: " +
                        ((ast instanceof ast.Programa p) ? p.getClases().size() : "?"));

            } catch (Exception e) {
                resultado.registrarFaltante(TipoArchivo.ZETARIANO,
                        "Error parseando " + archivo.getName() + ": " + e.getMessage());
            }
        }

        // ────────── PASADA 2: registrar firmas ──────────
        AnalizadorSemanticoCoordinador coordinador = new AnalizadorSemanticoCoordinador(
                new InferirTipoCoordinador());

        AnalizadorClase analizadorClase = new AnalizadorClase(coordinador);

        for (RegistroZ reg : registros) {
            if (reg.ast instanceof ast.Programa programa) {
                for (ast.clases.Clase clase : programa.getClases()) {
                    analizadorClase.registrarFirma(clase, reg.contexto);
                    System.out.println("[PASADA 2] Registrada firma de '" +
                            clase.getNombreClase() + "' en contexto de " +
                            reg.archivo.getName());
                }
            }
        }

        // ────────── PASADA 3: importar firmas entre contextos ──────────
        for (RegistroZ reg : registros) {
            for (RegistroZ otro : registros) {
                if (otro != reg) {
                    reg.contexto.getTablaTipos().importarDe(otro.contexto.getTablaTipos());
                    reg.contexto.getTablaSimbolos().importarDe(otro.contexto.getTablaSimbolos());
                    System.out.println("[PASADA 3] " + reg.archivo.getName() +
                            " importó tipos de " + otro.archivo.getName());
                }
            }

            // Ver qué tipos tiene cada contexto después de importar
            System.out.println("[PASADA 3] Contexto " + reg.archivo.getName() +
                    " tiene tipos: " + reg.contexto.getTablaTipos().listarNombres());
        }

        // ────────── PASADA 4: analizar cada .z ──────────
        for (RegistroZ reg : registros) {
            if (reg.ast == null) continue;
            System.out.println("[PASADA 4] Analizando: " + reg.archivo.getName());
            compiladorArchivo.analizarSemanticamente(reg.ast, reg.contexto);
        }
    }
    /**
     * Registra SOLO la firma de la clase (nombre, atributos, métodos)
     * en la tabla de tipos. No analiza cuerpos.
     */
    private void registrarFirmaClase(NodoAST ast, AnalisisContexto contexto) {
        if (ast == null) return;

        // Asume que el AST es un Programa con una lista de clases
        if (ast instanceof ast.Programa programa) {
            for (ast.clases.Clase clase : programa.getClases()) {
                // Registrar en tabla de tipos como OBJETO
                // (nombre, categoría)
                // Aquí ya tienes tu lógica de InformeTipo
                // Si tu analizador ya lo hace, este método puede ser vacío
            }
        }
    }

    /* =========================================================
       FASE 3: Pig Latin sin semántica
       ========================================================= */

    private void compilarPigSinSemantica(
            Map<TipoArchivo, List<File>> archivosPorTipo,
            Function<File, String> proveedorContenido,
            ResultadoProyecto resultado) {

        List<File> pigs = archivosPorTipo.get(TipoArchivo.PIG_LATIN);
        if (pigs == null || pigs.isEmpty()) return;

        for (File pig : pigs) {
            try {
                String codigo = proveedorContenido.apply(pig);
                AnalisisContexto ctx = AnalisisContexto.paraArchivo(
                        pig.getName(), TipoArchivo.PIG_LATIN);

                NodoAST ast = compiladorArchivo.analizarSinSemantica(
                        codigo, TipoArchivo.PIG_LATIN, ctx);

                resultado.registrarContexto(pig, ctx);
                resultado.registrarAst(pig, ast);

            } catch (Exception e) {
                resultado.registrarFaltante(TipoArchivo.PIG_LATIN,
                        "Error parseando " + pig.getName() + ": " + e.getMessage());
            }
        }
    }

    /* =========================================================
       FASE 4: resolver imports de Pig Latin
       ========================================================= */

    private void resolverImportsPig(
            Map<TipoArchivo, List<File>> archivosPorTipo,
            Function<File, String> proveedorContenido,
            ResultadoProyecto resultado) {

        List<File> pigs = archivosPorTipo.get(TipoArchivo.PIG_LATIN);
        if (pigs == null) return;

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
                importsPorPig.computeIfAbsent(pig, k -> new ArrayList<>()).add(archivoImportado);
                ctxPig.importarDe(ctxImportado);
            }
        }
    }

    /* =========================================================
       FASE 5: analizar Pig Latin con imports
       ========================================================= */

    private void analizarPigConImports(
            Map<TipoArchivo, List<File>> archivosPorTipo,
            ResultadoProyecto resultado) {

        List<File> pigs = archivosPorTipo.get(TipoArchivo.PIG_LATIN);
        if (pigs == null) return;

        for (File pig : pigs) {

            AnalisisContexto ctxPig = resultado.getContexto(pig);
            NodoAST astPig = resultado.getAst(pig);

            if (ctxPig == null || astPig == null) continue;

            compiladorArchivo.analizarSemanticamente(astPig, ctxPig);
        }
    }

    /* =========================================================
       HELPERS
       ========================================================= */

    private static final Pattern PATRON_IMPORT = Pattern.compile(
            "import\\s+([\\w./]+\\.(z|y))\\s*;?",
            Pattern.MULTILINE
    );

    private List<String> extraerImports(String codigoPig) {
        List<String> rutas = new ArrayList<>();
        Matcher m = PATRON_IMPORT.matcher(codigoPig);
        while (m.find()) rutas.add(m.group(1));
        return rutas;
    }

    private String extraerNombreBase(String ruta) {
        int ultimoPunto = ruta.lastIndexOf('.');
        if (ultimoPunto < 0) return ruta;
        String sinExtension = ruta.substring(0, ultimoPunto);
        String extension = ruta.substring(ultimoPunto);
        String normalizada = sinExtension.replace('\\', '/').replace('.', '/');
        int idx = normalizada.lastIndexOf('/');
        String nombreArchivo = idx >= 0 ? normalizada.substring(idx + 1) : normalizada;
        return nombreArchivo + extension;
    }

    private static class RegistroZ {
        final File archivo;
        final String codigo;
        final AnalisisContexto contexto;
        final NodoAST ast;

        RegistroZ(File archivo, String codigo, AnalisisContexto contexto, NodoAST ast) {
            this.archivo = archivo;
            this.codigo = codigo;
            this.contexto = contexto;
            this.ast = ast;
        }
    }

    /** Compilado con éxito = todos los archivos tienen AST y ningún error semántico/sintáctico. */
    public boolean proyectoSinErrores(Map<TipoArchivo, List<File>> archivosPorTipo, ResultadoProyecto resultado) {

        boolean hayArchivos = false;

        for (TipoArchivo tipo : List.of(TipoArchivo.Y_INTERROGACION, TipoArchivo.ZETARIANO, TipoArchivo.PIG_LATIN)) {
            for (File archivo : archivosPorTipo.getOrDefault(tipo, List.of())) {
                hayArchivos = true;
                if (!compiladoSinErrores(archivo, resultado)) {
                    return false;
                }
            }
        }

        return hayArchivos;
    }

    /**
     * Arma la lista de archivos para el C3D en el orden que necesita el generador:
     * los .y que importa el .pig, todos los .z (se ven entre sí) y al final el .pig principal.
     */
    public List<GeneradorC3DCompleto.ArchivoFuente> archivosParaC3D(
            File pigPrincipal,
            Map<TipoArchivo, List<File>> archivosPorTipo,
            ResultadoProyecto resultado) {

        List<File> importados = importsPorPig.getOrDefault(pigPrincipal, List.of());
        List<File> ordenados = new ArrayList<>();

        for (File y : archivosPorTipo.getOrDefault(TipoArchivo.Y_INTERROGACION, List.of())) {
            if (importados.stream().anyMatch(i -> i.getName().equals(y.getName()))) {
                ordenados.add(y);
            }
        }

        ordenados.addAll(archivosPorTipo.getOrDefault(TipoArchivo.ZETARIANO, List.of()));
        ordenados.add(pigPrincipal);

        List<GeneradorC3DCompleto.ArchivoFuente> fuentes = new ArrayList<>();

        for (File archivo : ordenados) {
            if (!compiladoSinErrores(archivo, resultado)) {
                throw new IllegalStateException(
                        "'" + archivo.getName() + "' tiene errores. Corrígelos y compila de nuevo.");
            }
            fuentes.add(new GeneradorC3DCompleto.ArchivoFuente(
                    archivo.getName(),
                    resultado.getAst(archivo),
                    archivo.equals(pigPrincipal)));
        }

        return fuentes;
    }

    private boolean compiladoSinErrores(File archivo, ResultadoProyecto resultado) {
        AnalisisContexto contexto = resultado.getContexto(archivo);
        return resultado.getAst(archivo) != null && contexto != null && !contexto.tieneErrores();
    }
}