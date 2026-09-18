package analisis;

import ast.NodoAST;
import enums.TipoArchivo;
import enums.TipoErrorSemantico;
import lombok.Getter;
import semantico.AnalisisContexto;
import semantico.ErrorSemantico;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ResultadoProyecto {

    /** Un contexto POR ARCHIVO (con sus propias tablas). */
    private final Map<File, AnalisisContexto> contextos = new LinkedHashMap<>();

    /** Un AST por archivo compilado. */
    private final Map<File, NodoAST> astPorArchivo = new LinkedHashMap<>();

    /** Archivos que se buscaron pero no se encontraron. */
    private final Map<TipoArchivo, String> faltantes = new LinkedHashMap<>();

    public void registrarContexto(File archivo, AnalisisContexto contexto) {
        contextos.put(archivo, contexto);
    }

    public void registrarAst(File archivo, NodoAST ast) {
        astPorArchivo.put(archivo, ast);
    }

    public void registrarFaltante(TipoArchivo tipo, String motivo) {
        faltantes.put(tipo, motivo);
    }

    public AnalisisContexto getContexto(File archivo) {
        return contextos.get(archivo);
    }

    public NodoAST getAst(File archivo) {
        return astPorArchivo.get(archivo);
    }

    public List<ErrorSemantico> getErrores() {

        List<ErrorSemantico> todos = new ArrayList<>();

        for (AnalisisContexto ctx : contextos.values()) {
            todos.addAll(ctx.getErrores());
        }

        // Faltantes también cuentan como errores del proyecto
        for (var entry : faltantes.entrySet()) {
            todos.add(ErrorSemantico.error("proyecto", 0, 0, entry.getValue()));
        }

        return todos;
    }

    public boolean isCorrecto() {
        if (!faltantes.isEmpty()) return false;
        return getErrores().stream()
                .noneMatch(e -> e.tipoError() == TipoErrorSemantico.ERROR);
    }

    /** Busca un archivo por su nombre base (ej. "Persona.z"). */
    public File buscarPorNombre(String nombre) {
        for (File f : contextos.keySet()) {
            if (f.getName().equals(nombre)) return f;
        }
        return null;
    }

    public Map<File, AnalisisContexto> getContextos() {
        return contextos;
    }
}