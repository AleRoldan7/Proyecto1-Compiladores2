package analisis;

import ast.NodoAST;
import org.antlr.v4.runtime.*;
import org.compi2.proyecto1compiladores2.GrammarZetarianoLexer;
import org.compi2.proyecto1compiladores2.GrammarZetarianoParser;
import semantico.AnalisisContexto;
import semantico.coordinadorsemantico.AnalizadorSemanticoCoordinador;
import semantico.coordinadorsemantico.InferirTipoCoordinador;
import tablas.TablaSimbolos;
import tablas.TablaTipos;
import visitor.zetariano.VisitorZetariano;

import java.util.ArrayList;
import java.util.List;

public class CompiladorCodigo {

    public ResultadoAnalisis analizar(String codigo) {

        List<String> errores = new ArrayList<>();


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




        CommonTokenStream tokens =
                new CommonTokenStream(lexer);



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



        var arbol = parser.program();




        TablaSimbolos tablaSimbolos =
                new TablaSimbolos();

        TablaTipos tablaTipos =
                new TablaTipos();



        AnalisisContexto contexto = new AnalisisContexto(tablaSimbolos, tablaTipos);



        VisitorZetariano visitor =
                new VisitorZetariano(contexto);

        NodoAST ast = null;

        try {

            ast = visitor.visit(arbol);

        } catch (Exception e) {

            errores.add(
                    "Error durante la construcción del AST: "
                            + e.getMessage()
            );
        }


        /*
         * FIX: análisis semántico conectado. Corre solo si el AST se
         * construyó bien (si el visitor ya truena, no tiene sentido
         * analizar un AST a medio construir o null).
         *
         * OJO: el visitor TODAVÍA hace sus propias validaciones al
         * construir el AST (existeEnAmbitoActual, reportarError, etc.),
         * así que vas a ver errores de "ya fue declarado" duplicados
         * por ahora — uno del visitor, otro del coordinador. Es
         * esperado mientras no saquemos esa lógica del visitor.
         */
        if (ast != null) {

            try {

                InferirTipoCoordinador inferirTipoCoordinador = new InferirTipoCoordinador();
                AnalizadorSemanticoCoordinador analizadorSemantico =
                        new AnalizadorSemanticoCoordinador(inferirTipoCoordinador);

                analizadorSemantico.analizar(ast, contexto);

            } catch (Exception e) {

                errores.add(
                        "Error durante el análisis semántico: "
                                + e.getMessage()
                );
            }
        }


        contexto.getErrores().forEach(error -> errores.add(error.toString()));


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
