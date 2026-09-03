package analisis;

import ast.NodoAST;
import org.antlr.v4.runtime.*;
import org.compi2.proyecto1compiladores2.GrammarZetarianoLexer;
import org.compi2.proyecto1compiladores2.GrammarZetarianoParser;
import semantico.AnalisisContexto;
import tablas.TablaSimbolos;
import tablas.TablaTipos;
import visitor.zetariano.VisitorZetariano;

import java.util.ArrayList;
import java.util.List;

public class CompiladorCodigo {

    public ResultadoAnalisis analizar(String codigo) {

        List<String> errores = new ArrayList<>();

        // =====================================================
        // 1. LEXER
        // =====================================================

        GrammarZetarianoLexer lexer =
                new GrammarZetarianoLexer(
                        CharStreams.fromString(codigo)
                );

        lexer.removeErrorListeners();

        lexer.addErrorListener(new BaseErrorListener() {

            @Override
            public void syntaxError(
                    Recognizer<?, ?> recognizer,
                    Object offendingSymbol,
                    int linea,
                    int columna,
                    String mensaje,
                    RecognitionException e
            ) {

                errores.add(
                        "Error léxico [" +
                                linea + ":" +
                                columna +
                                "] " +
                                mensaje
                );
            }
        });


        // =====================================================
        // 2. TOKENS
        // =====================================================

        CommonTokenStream tokens =
                new CommonTokenStream(lexer);


        // =====================================================
        // 3. PARSER
        // =====================================================

        GrammarZetarianoParser parser =
                new GrammarZetarianoParser(tokens);

        parser.removeErrorListeners();

        parser.addErrorListener(new BaseErrorListener() {

            @Override
            public void syntaxError(
                    Recognizer<?, ?> recognizer,
                    Object offendingSymbol,
                    int linea,
                    int columna,
                    String mensaje,
                    RecognitionException e
            ) {

                errores.add(
                        "Error sintáctico [" +
                                linea + ":" +
                                columna +
                                "] " +
                                mensaje
                );
            }
        });


        // =====================================================
        // 4. PARSE TREE
        // =====================================================

        var arbol = parser.program();


        // =====================================================
        // 5. TABLAS
        // =====================================================

        TablaSimbolos tablaSimbolos =
                new TablaSimbolos();

        TablaTipos tablaTipos =
                new TablaTipos();


        // =====================================================
        // 6. CONTEXTO
        // =====================================================

        AnalisisContexto contexto =
                new AnalisisContexto(
                        tablaSimbolos,
                        tablaTipos
                );


        // =====================================================
        // 7. VISITOR
        // =====================================================

        VisitorZetariano visitor =
                new VisitorZetariano(contexto);

        NodoAST ast = null;

        try {

            ast = visitor.visit(arbol);

        } catch (Exception e) {

            errores.add(
                    "Error durante el análisis semántico: "
                            + e.getMessage()
            );
        }


        // =====================================================
        // 8. ERRORES SEMÁNTICOS
        // =====================================================

        //errores.addAll(contexto.getErrores());


        // =====================================================
        // 9. RESULTADO
        // =====================================================

        boolean correcto = errores.isEmpty();

        return new ResultadoAnalisis(
                correcto,
                ast,
                contexto.getTablaSimbolos(),
                contexto.getTablaTipos(),
                errores
        );
    }
}
