package c3d;

import ast.Programa;
import org.antlr.v4.runtime.*;
import org.compi2.proyecto1compiladores2.GrammarPythonLexer;
import org.compi2.proyecto1compiladores2.GrammarPythonParser;
import org.testng.annotations.Test;
import utils.generadorC.GeneradorCodigoC;
import visitor.piton.LexerIndentacionY;
import visitor.piton.VisitorPiton;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

public class GeneradorC3DTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        String codigo = """
                %funciones
                definir sumar():
                	entero a = 5
                	entero b = 3
                	entero total = a + b
                """;

        //System.out.println("===== CÓDIGO FUENTE =====");
        System.out.println(codigo);

        // ---------- 1. Léxico + sintáctico ----------
        List<String> erroresSintacticos = new ArrayList<>();

        GrammarPythonLexer lexerBase = new GrammarPythonLexer(CharStreams.fromString(codigo));
        LexerIndentacionY lexer = new LexerIndentacionY(lexerBase);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        GrammarPythonParser parser = new GrammarPythonParser(tokens);

        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> r, Object o, int linea, int col, String msg, RecognitionException e) {
                erroresSintacticos.add("[" + linea + ":" + col + "] " + msg);
            }
        });

        var arbol = parser.program();

        if (!erroresSintacticos.isEmpty()) {
            System.out.println("===== ERRORES SINTÁCTICOS =====");
            erroresSintacticos.forEach(System.out::println);
            return; // no tiene sentido seguir si el parser falló
        }
       // System.out.println("===== PARSE TREE (crudo) =====");
        System.out.println(arbol.toStringTree(parser));

        // ---------- 2. Construcción del AST ----------
        Programa programa;
        try {
            programa = (Programa) new VisitorPiton().visit(arbol);
        } catch (Exception e) {
            ///System.out.println("===== ERROR CONSTRUYENDO EL AST =====");
            e.printStackTrace();
            return;
        }
        //System.out.println("\n===== AST =====");
        System.out.println(programa);

        // ---------- 3. Generación de C3D ----------
        ContextoC3D contexto = new ContextoC3D();
        try {
            programa.generarC3D(contexto);
        } catch (Exception e) {
            //System.out.println("===== ERROR GENERANDO C3D =====");
            e.printStackTrace();
            return;
        }

        System.out.println("PRUEBA CUARTETA");
        if (contexto.getCuartetas().isEmpty()) {
            System.out.println("(vacío — revisa si el nodo correspondiente ya implementa generarC3D())");
        }
        for (Cuarteta c : contexto.getCuartetas()) {
            System.out.println(c);
        }

        // al final de PruebaC3D.main(), después de imprimir las cuartetas:

        String codigoC = new GeneradorCodigoC().generar(contexto.getCuartetas());
        System.out.println("\n===== CÓDIGO C GENERADO =====");
        System.out.println(codigoC);
        codigoC = codigoC.replace("return 0;", "printf(\"total = %d\\n\", total);\n    return 0;");
        Path archivoC = Path.of("salida_prueba.c");
        Files.writeString(archivoC, codigoC);

        Process compilacion = new ProcessBuilder("gcc", archivoC.toString(), "-o", "salida_prueba")
                .redirectErrorStream(true)
                .start();
        String salidaGcc = new String(compilacion.getInputStream().readAllBytes());
        int codigoSalidaGcc = compilacion.waitFor();

        System.out.println("\n===== SALIDA DE GCC =====");
        System.out.println(salidaGcc.isBlank() ? "(sin errores) ✅ compiló" : salidaGcc);

        if (codigoSalidaGcc == 0) {
            Process ejecucion = new ProcessBuilder("./salida_prueba").redirectErrorStream(true).start();
            String salidaPrograma = new String(ejecucion.getInputStream().readAllBytes());
            ejecucion.waitFor();
            System.out.println("\n===== SALIDA AL EJECUTAR =====");
            System.out.println(salidaPrograma.isBlank() ? "(sin salida — normal, tu .y de prueba no tiene imprimir())" : salidaPrograma);
        } else {
            System.out.println("gcc falló. Revisa " + archivoC.toAbsolutePath());
        }
        // ---------- 4. Comparación manual contra lo esperado ----------
        List<String> esperado = List.of(
                "a=5",
                "b=3",
                "tmp0=a + b",
                "total=tmp0"
        );
        List<String> obtenido = contexto.getCuartetas().stream().map(Cuarteta::toString).toList();

        /*
        System.out.println("\n===== RESULTADO =====");
        System.out.println(obtenido.equals(esperado) ? "COINCIDE con lo esperado" : "❌ NO coincide");
        if (!obtenido.equals(esperado)) {
            System.out.println("Esperado: " + esperado);
            System.out.println("Obtenido: " + obtenido);
        }

         */
    }
}