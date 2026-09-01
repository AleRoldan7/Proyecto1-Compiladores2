package org.compi2.proyecto1compiladores2;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import visitor.piton.LexerIndentacionY;
import visitor.piton.VisitorPiton;

public class TestGrammar {

    public static void main(String[] args) {

        String codigo = """
                %estructuras
                estructura Punto:
                	entero x
                	entero y

                %funciones
                definir sumar(entero a, entero b) -> entero:
                	retorno a + b
                """;



        GrammarPythonLexer lexerBase = new GrammarPythonLexer(CharStreams.fromString(codigo));
        LexerIndentacionY lexer = new LexerIndentacionY(lexerBase);
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        tokens.fill();

        for (Token token : tokens.getTokens()) {
            System.out.println(
                    token.getLine() + ":" +
                            token.getCharPositionInLine() +
                            " -> " +
                            token.getType() + " -> [" +
                            token.getText() + "]"
            );
        }

        GrammarPythonParser parser = new GrammarPythonParser(tokens);

        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> r, Object offendingSymbol, int linea, int col, String msg, RecognitionException e) {
                System.err.println("Error sintáctico [" + linea + ":" + col + "] " + msg);
            }
        });



        var arbol = parser.program();
        System.out.println(arbol.toStringTree(parser));   // primero valida el parse tree "crudo"

        var ast = new VisitorPiton().visit(arbol);
        System.out.println(ast);   // te conviene sobreescribir toString() en tus clases de ast/ para inspeccionar visualmente
    }
}
