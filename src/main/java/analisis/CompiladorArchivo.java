package analisis;

import ast.NodoAST;
import enums.TipoArchivo;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.compi2.proyecto1compiladores2.*;
import semantico.AnalisisContexto;
import visitor.piglatin.VisitorPigLatin;
import visitor.piton.LexerIndentacionY;
import visitor.piton.VisitorPiton;
import visitor.zetariano.VisitorZetariano;

/**
 * Compila un único archivo (.y, .z o .pig) contra un AnalisisContexto YA
 * existente y compartido (misma TablaSimbolos/TablaTipos para los 3
 * lenguajes). El llamador es responsable de hacer
 * contexto.setArchivoActual(nombreDelArchivo) ANTES de invocar analizar(),
 * para que cada error quede correctamente etiquetado con su archivo de
 * origen.
 */
public class CompiladorArchivo {

    /**
     * @param codigo   contenido fuente del archivo
     * @param tipo     lenguaje del archivo (según su extensión)
     * @param contexto contexto de análisis compartido entre los 3 archivos
     * @return el NodoAST raíz generado (puede ser null si hubo errores graves)
     */
    public NodoAST analizar(String codigo, TipoArchivo tipo, AnalisisContexto contexto) {

        switch (tipo) {

            case Y_INTERROGACION: {
                GrammarPythonLexer lexerBase = new GrammarPythonLexer(CharStreams.fromString(codigo));
                lexerBase.removeErrorListeners();
                lexerBase.addErrorListener(new RecolectorErrores(contexto, RecolectorErrores.Etapa.LEXICO));

                LexerIndentacionY lexer = new LexerIndentacionY(lexerBase);
                CommonTokenStream tokens = new CommonTokenStream(lexer);

                GrammarPythonParser parser = new GrammarPythonParser(tokens);
                parser.removeErrorListeners();
                parser.addErrorListener(new RecolectorErrores(contexto, RecolectorErrores.Etapa.SINTACTICO));

                var arbol = parser.program();
                return ejecutarVisitor(() -> new VisitorPiton().visit(arbol), contexto);
            }

            case ZETARIANO: {
                GrammarZetarianoLexer lexer = new GrammarZetarianoLexer(CharStreams.fromString(codigo));
                lexer.removeErrorListeners();
                lexer.addErrorListener(new RecolectorErrores(contexto, RecolectorErrores.Etapa.LEXICO));

                CommonTokenStream tokens = new CommonTokenStream(lexer);

                GrammarZetarianoParser parser = new GrammarZetarianoParser(tokens);
                parser.removeErrorListeners();
                parser.addErrorListener(new RecolectorErrores(contexto, RecolectorErrores.Etapa.SINTACTICO));

                var arbol = parser.program();
                VisitorZetariano visitor = new VisitorZetariano(contexto);
                return ejecutarVisitor(() -> visitor.visit(arbol), contexto);
            }

            case PIG_LATIN: {
                GrammarPigLatinLexer lexer = new GrammarPigLatinLexer(CharStreams.fromString(codigo));
                lexer.removeErrorListeners();
                lexer.addErrorListener(new RecolectorErrores(contexto, RecolectorErrores.Etapa.LEXICO));

                CommonTokenStream tokens = new CommonTokenStream(lexer);

                GrammarPigLatinParser parser = new GrammarPigLatinParser(tokens);
                parser.removeErrorListeners();
                parser.addErrorListener(new RecolectorErrores(contexto, RecolectorErrores.Etapa.SINTACTICO));

                var arbol = parser.program();
                return ejecutarVisitor(() -> new VisitorPigLatin().visit(arbol), contexto);
            }

            default:
                contexto.reportarError(0, 0, "Extensión de archivo no reconocida (se esperaba .y, .z o .pig)");
                return null;
        }
    }

    private NodoAST ejecutarVisitor(java.util.function.Supplier<NodoAST> visita, AnalisisContexto contexto) {
        try {
            return visita.get();
        } catch (Exception e) {
            contexto.reportarError(0, 0, "Error durante el análisis semántico: " + e.getMessage());
            return null;
        }
    }
}
