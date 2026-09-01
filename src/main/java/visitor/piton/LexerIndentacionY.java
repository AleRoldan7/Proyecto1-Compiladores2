package visitor.piton;

import org.antlr.v4.runtime.*;
import org.compi2.proyecto1compiladores2.GrammarPythonLexer;

import java.util.*;

public class LexerIndentacionY implements TokenSource {

    private final GrammarPythonLexer lexerBase;

    private final Deque<Integer> pilaIndentacion =
            new ArrayDeque<>(List.of(0));

    private final Queue<Token> colaTokens =
            new LinkedList<>();

    public LexerIndentacionY(GrammarPythonLexer lexerBase) {
        this.lexerBase = lexerBase;
    }

    @Override
    public Token nextToken() {

        // Primero devolver tokens pendientes
        if (!colaTokens.isEmpty()) {
            return colaTokens.poll();
        }

        Token token = lexerBase.nextToken();

        // ==========================================
        // NEWLINE
        // ==========================================
        if (token.getType() == GrammarPythonLexer.NEWLINE) {

            // IMPORTANTE:
            // El NEWLINE NO se debe perder.
            colaTokens.add(token);

            int nivel = contarTabs(token.getText());

            procesarIndentacion(nivel, token);

            return colaTokens.poll();
        }

        // ==========================================
        // EOF
        // ==========================================
        if (token.getType() == Token.EOF) {

            // Cerrar todos los niveles pendientes
            while (pilaIndentacion.peek() > 0) {

                pilaIndentacion.pop();

                colaTokens.add(
                        crearTokenSintetico(
                                GrammarPythonLexer.DEDENT,
                                token
                        )
                );
            }

            colaTokens.add(token);

            return colaTokens.poll();
        }

        return token;
    }

    private void procesarIndentacion(int nivel, Token origen) {

        int actual = pilaIndentacion.peek();

        // ==========================================
        // AUMENTA INDENTACIÓN
        // ==========================================
        if (nivel > actual) {

            pilaIndentacion.push(nivel);

            colaTokens.add(
                    crearTokenSintetico(
                            GrammarPythonLexer.INDENT,
                            origen
                    )
            );

        }

        // ==========================================
        // DISMINUYE INDENTACIÓN
        // ==========================================
        else {

            while (nivel < pilaIndentacion.peek()) {

                pilaIndentacion.pop();

                colaTokens.add(
                        crearTokenSintetico(
                                GrammarPythonLexer.DEDENT,
                                origen
                        )
                );
            }
        }
    }

    private int contarTabs(String textoNewline) {

        int tabs = 0;

        for (char c : textoNewline.toCharArray()) {

            if (c == '\t') {
                tabs++;
            }
        }

        return tabs;
    }

    private CommonToken crearTokenSintetico(
            int tipo,
            Token origen) {

        CommonToken t = new CommonToken(origen);

        t.setType(tipo);

        if (tipo == GrammarPythonLexer.INDENT) {
            t.setText("<INDENT>");
        } else {
            t.setText("<DEDENT>");
        }

        return t;
    }

    @Override
    public int getLine() {
        return lexerBase.getLine();
    }

    @Override
    public int getCharPositionInLine() {
        return lexerBase.getCharPositionInLine();
    }

    @Override
    public CharStream getInputStream() {
        return lexerBase.getInputStream();
    }

    @Override
    public String getSourceName() {
        return lexerBase.getSourceName();
    }

    @Override
    public void setTokenFactory(TokenFactory<?> factory) {
        lexerBase.setTokenFactory(factory);
    }

    @Override
    public TokenFactory<?> getTokenFactory() {
        return lexerBase.getTokenFactory();
    }
}