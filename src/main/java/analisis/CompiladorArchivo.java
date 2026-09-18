package analisis;

import ast.NodoAST;
import enums.TipoArchivo;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.compi2.proyecto1compiladores2.*;
import semantico.AnalisisContexto;
import semantico.coordinadorsemantico.AnalizadorSemanticoCoordinador;
import semantico.coordinadorsemantico.InferirTipoCoordinador;
import semantico.dialecto.Dialectos;
import visitor.piglatin.VisitorPigLatin;
import visitor.piton.LexerIndentacionY;
import visitor.piton.VisitorPiton;
import visitor.zetariano.VisitorZetariano;

/**
 * Compila un único archivo (.y, .z o .pig) contra un AnalisisContexto YA
 * existente y compartido (misma TablaSimbolos/TablaTipos para los 3
 * lenguajes). El llamador es responsable de hacer
 * contexto.setArchivoActual(nombreDelArchivo) ANTES de invocar analizar().
 */
public class CompiladorArchivo {

    /*
     * UN SOLO coordinador para los tres lenguajes. Lo que cambia entre
     * ellos (nombres de tipos, qué se permite declarar) lo responde el
     * Dialecto que se setea en el contexto antes de analizar cada archivo,
     * no un coordinador distinto por lenguaje.
     */
    private final InferirTipoCoordinador inferirTipoCoordinador = new InferirTipoCoordinador();
    private final AnalizadorSemanticoCoordinador analizadorSemantico =
            new AnalizadorSemanticoCoordinador(inferirTipoCoordinador);

    public NodoAST analizar(String codigo, TipoArchivo tipo, AnalisisContexto contexto) {

        if (tipo == TipoArchivo.DESCONOCIDO) {
            contexto.reportarError(0, 0,
                    "Extensión de archivo no reconocida (se esperaba .y, .z o .pig)");
            return null;
        }

        contexto.setDialecto(Dialectos.de(tipo));

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
                return analizarSemanticamente(
                        ejecutarVisitor(() -> new VisitorPiton(contexto).visit(arbol), contexto),
                        contexto);
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

                return analizarSemanticamente(
                        ejecutarVisitor(() -> visitor.visit(arbol), contexto),
                        contexto);
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
                return analizarSemanticamente(
                        ejecutarVisitor(() -> new VisitorPigLatin(contexto).visit(arbol), contexto),
                        contexto);
            }

            default:
                contexto.reportarError(0, 0,
                        "Extensión de archivo no reconocida (se esperaba .y, .z o .pig)");
                return null;
        }
    }

    /**
     * Compila un archivo con su PROPIO contexto (tablas frescas).
     * Devuelve el resultado: AST + contexto + errores.
     *
     * Úsalo cuando compilas un archivo suelto, sin imports.
     */
    public ResultadoCompilacion analizarArchivo(String codigo, TipoArchivo tipo, String nombreArchivo) {

        AnalisisContexto contexto = AnalisisContexto.paraArchivo(nombreArchivo, tipo);

        NodoAST ast = analizar(codigo, tipo, contexto);

        return new ResultadoCompilacion(ast, contexto);
    }

    /**
     * Segunda pasada: recorre el AST ya construido aplicando las reglas
     * semánticas. Es la MISMA para los tres lenguajes; lo que cambia es
     * el Dialecto que quedó seteado en el contexto.
     */
    public NodoAST analizarSemanticamente(NodoAST ast, AnalisisContexto contexto) {

        if (ast == null) {
            return null;
        }

        try {
            analizadorSemantico.analizar(ast, contexto);
        } catch (IllegalStateException e) {
            contexto.reportarError(0, 0, "Analizador semántico incompleto: " + e.getMessage());
        } catch (Exception e) {
            contexto.reportarError(0, 0, "Error durante el análisis semántico: " + e.getMessage());
        }

        return ast;
    }

    private NodoAST ejecutarVisitor(java.util.function.Supplier<NodoAST> visita, AnalisisContexto contexto) {
        try {
            return visita.get();
        } catch (Exception e) {
            contexto.reportarError(0, 0,
                    "Error construyendo el AST: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Compila un archivo SIN correr el análisis semántico. Se usa para
     * Pig Latin en la Fase 1, porque necesita que primero se resuelvan
     * sus imports.
     */
    public NodoAST analizarSinSemantica(String codigo, TipoArchivo tipo, AnalisisContexto contexto) {

        if (tipo == TipoArchivo.DESCONOCIDO) {
            contexto.reportarError(0, 0, "Extensión de archivo no reconocida");
            return null;
        }

        contexto.setDialecto(Dialectos.de(tipo));

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
                return ejecutarVisitor(() -> new VisitorPiton(contexto).visit(arbol), contexto);
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
                return ejecutarVisitor(() -> new VisitorZetariano(contexto).visit(arbol), contexto);
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
                return ejecutarVisitor(() -> new VisitorPigLatin(contexto).visit(arbol), contexto);
            }

            default:
                contexto.reportarError(0, 0, "Extensión de archivo no reconocida");
                return null;
        }
    }
}