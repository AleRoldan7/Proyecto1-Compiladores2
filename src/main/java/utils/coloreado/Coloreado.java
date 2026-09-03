package utils.coloreado;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.compi2.proyecto1compiladores2.GrammarZetarianoLexer;

import java.util.ArrayList;
import java.util.List;

public class Coloreado {

    public List<TokenColor> colorear(String codigo) {

        List<TokenColor> colorear = new ArrayList<>();

        CharStream input = CharStreams.fromString(codigo);
        GrammarZetarianoLexer lexer = new GrammarZetarianoLexer(input);
        Token token;

        while ((token = lexer.nextToken()).getType() != Token.EOF) {

            String tipo = obtenerColor(token.getType());

            colorear.add(new TokenColor(token.getStartIndex(), token.getStopIndex() - token.getStartIndex() + 1, tipo));
        }

        return colorear;
    }

    private String obtenerColor(int tipo){

        switch(tipo){

            case GrammarZetarianoLexer.CLASS:
            case GrammarZetarianoLexer.PUBLIC:
                return "seccion";


            case GrammarZetarianoLexer.INT:
            case GrammarZetarianoLexer.DOUBLE:
            case GrammarZetarianoLexer.CHAR:
            case GrammarZetarianoLexer.STRING:
            case GrammarZetarianoLexer.BOOLEAN:
                return "tipo";

            case GrammarZetarianoLexer.TRUE:
            case GrammarZetarianoLexer.FALSE:
                return "boolean";

            case GrammarZetarianoLexer.VOID:
            case GrammarZetarianoLexer.RETURN:
            case GrammarZetarianoLexer.CASE:
            case GrammarZetarianoLexer.BREAK:
                return "funcion";

            case GrammarZetarianoLexer.FOR:
            case GrammarZetarianoLexer.WHILE:
            case GrammarZetarianoLexer.DO:
                return "ciclo";

            case GrammarZetarianoLexer.IF:
            case GrammarZetarianoLexer.ELSE:
                return "condicional";

            case GrammarZetarianoLexer.ENTERO:
            case GrammarZetarianoLexer.DECIMAL:
                return "numero";

            case GrammarZetarianoLexer.COMILLAS:
            case GrammarZetarianoLexer.COMILLASSIMPLES:
                return "cadena";


            case GrammarZetarianoLexer.MAS:
            case GrammarZetarianoLexer.RESTA:
            case GrammarZetarianoLexer.MULTIPLICACION:
            case GrammarZetarianoLexer.DIVISION:
                return "operador";

            default:
                return "default";

        }

    }
}
