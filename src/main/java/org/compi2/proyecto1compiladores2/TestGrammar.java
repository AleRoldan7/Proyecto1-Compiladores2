package org.compi2.proyecto1compiladores2;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class TestGrammar {

    public static void main(String[] args) {

        String codigo = """
           %estructuras
           si(edad > 18)
            """;



        CharStream input = CharStreams.fromString(codigo);
        //GrammarZetarianoLexer lexer = new GrammarZetarianoLexer(input);
        GrammarPythonLexer lexer = new GrammarPythonLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        //GrammarZetarianoParser parser = new GrammarZetarianoParser(tokens);
        GrammarPythonParser parser = new GrammarPythonParser(tokens);
        ParseTree tree = parser.program();
        System.out.println(tree.toStringTree(parser));
    }
}
