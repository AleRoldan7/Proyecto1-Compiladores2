package org.compi2.proyecto1compiladores2;

import ast.NodoAST;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import semantico.AnalisisContexto;
import tablas.TablaSimbolos;
import tablas.TablaTipos;
import visitor.piton.LexerIndentacionY;
import visitor.piton.VisitorPiton;
import visitor.zetariano.VisitorZetariano;

public class TestGrammar {

    public static void main(String[] args) {

        String codigo = """
            public class Persona {
                String nombre;
                int edad;
                int dato = 10;
                double modulo = a % b;
                
                
                public Persona(String nombreParametro, int edadParametro) {
                    nombre = nombreParametro;
                    edad = edadParametro;
                }
                
                public Persona() {
                    nombre = "Sin nombre";
                    edad = 0;
                }
                
                public void saludar() {
                    println("¡Hola! Me llamo " + nombre + " y tengo " + edad + " años.");
                }
                
                public int calcularAnioNacimiento(int anioActual) {
                    return anioActual - edad;
                }
                
                Persona p = new Persona("hola", 25);
                
            }
            """;



        //GrammarPythonLexer lexerBase = new GrammarPythonLexer(CharStreams.fromString(codigo));
        //LexerIndentacionY lexer = new LexerIndentacionY(lexerBase);
        //GrammarPigLatinLexer lexerBase = new GrammarPigLatinLexer(CharStreams.fromString(codigo));
        GrammarZetarianoLexer lexerBase = new GrammarZetarianoLexer(CharStreams.fromString(codigo));
        CommonTokenStream tokens = new CommonTokenStream(lexerBase);

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

        //GrammarPythonParser parser = new GrammarPythonParser(tokens);

        //GrammarPigLatinParser parser = new GrammarPigLatinParser(tokens);
        GrammarZetarianoParser parser = new GrammarZetarianoParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> r, Object offendingSymbol, int linea, int col, String msg, RecognitionException e) {
                System.err.println("Error sintáctico [" + linea + ":" + col + "] " + msg);
            }
        });


        /*
        var arbol = parser.program();
        System.out.println(arbol.toStringTree(parser));   // primero valida el parse tree "crudo"

        var ast = new VisitorPiton().visit(arbol);
        System.out.println(ast);   // te conviene sobreescribir toString() en tus clases de ast/ para inspeccionar visualmente
         */

        // 4. ANALIZAR Y OBTENER ÁRBOL
        System.out.println("\n=== ÁRBOL DE ANÁLISIS SINTÁCTICO ===");
        var arbol = parser.program();
        System.out.println(arbol.toStringTree(parser));

        // 5. CREAR CONTEXTO DE ANÁLISIS
        TablaSimbolos tablaSimbolos = new  TablaSimbolos();
        TablaTipos tablaTipos = new  TablaTipos();
        AnalisisContexto contexto = new AnalisisContexto(tablaSimbolos, tablaTipos);

        // 6. CREAR Y EJECUTAR VISITOR
        System.out.println("\n=== VISITANDO EL ÁRBOL ===");
        VisitorZetariano visitor = new VisitorZetariano(contexto);
        NodoAST ast = visitor.visit(arbol);

        // 7. MOSTRAR RESULTADOS
        System.out.println("\n=== AST GENERADO ===");
        System.out.println(ast != null ? ast.toString() : "AST nulo");

        // 8. MOSTRAR TABLA DE SÍMBOLOS
        System.out.println("\n=== TABLA DE SÍMBOLOS ===");
        contexto.getTablaSimbolos().getHistorialTabla();

        // 9. MOSTRAR TABLA DE TIPOS
        System.out.println("\n=== TABLA DE TIPOS ===");
        contexto.getTablaTipos().toString();

        // 10. MOSTRAR ERRORES
        System.out.println("\n=== ERRORES DE COMPILACIÓN ===");
        if (contexto.getErrores().isEmpty()) {
            System.out.println("Sin errores");
        } else {
            for (var error : contexto.getErrores()) {
                System.out.println(error);
            }
        }

    }
}
