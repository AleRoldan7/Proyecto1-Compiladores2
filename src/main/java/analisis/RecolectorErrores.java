package analisis;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import semantico.AnalisisContexto;

/**
 * Puente entre los ErrorListener de ANTLR (lexer/parser) y el AnalisisContexto.
 * Así los errores léxicos y sintácticos quedan en la MISMA lista que los
 * errores semánticos, todos etiquetados con el archivo que estaba activo
 * en el contexto en el momento del error (contexto.setArchivoActual(...)).
 */
public class RecolectorErrores extends BaseErrorListener {

    public enum Etapa { LEXICO, SINTACTICO }

    private final AnalisisContexto contexto;
    private final Etapa etapa;

    public RecolectorErrores(AnalisisContexto contexto, Etapa etapa) {
        this.contexto = contexto;
        this.etapa = etapa;
    }

    @Override
    public void syntaxError(
            Recognizer<?, ?> recognizer,
            Object offendingSymbol,
            int linea,
            int columna,
            String mensaje,
            RecognitionException e
    ) {
        String prefijo = etapa == Etapa.LEXICO ? "Error léxico: " : "Error sintáctico: ";
        contexto.reportarError(linea, columna, prefijo + mensaje);
    }
}
