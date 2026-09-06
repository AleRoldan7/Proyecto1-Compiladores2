package analisis;

import ast.NodoAST;
import enums.TipoArchivo;
import lombok.Getter;
import semantico.AnalisisContexto;
import semantico.ErrorSemantico;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ResultadoProyecto {

    /** Contexto compartido: misma TablaSimbolos / TablaTipos para los 3 lenguajes. */
    private final AnalisisContexto contexto;

    /** AST generado por cada archivo que sí pudo compilarse. */
    private final Map<File, NodoAST> astPorArchivo = new LinkedHashMap<>();

    /** Archivos que se buscaron pero no se encontraron en el árbol de trabajo. */
    private final Map<TipoArchivo, String> faltantes = new LinkedHashMap<>();

    public ResultadoProyecto(AnalisisContexto contexto) {
        this.contexto = contexto;
    }

    public void registrarAst(File archivo, NodoAST ast) {
        astPorArchivo.put(archivo, ast);
    }

    public void registrarFaltante(TipoArchivo tipo, String motivo) {
        faltantes.put(tipo, motivo);
    }

    public List<ErrorSemantico> getErrores() {
        return contexto.getErrores();
    }

    public boolean isCorrecto() {
        return !contexto.tieneErrores();
    }
}
