package visitor.piton;

import ast.NodoAST;
import ast.Programa;
import ast.declaraciones.DeclaracionArreglo;
import ast.declaraciones.DeclaracionFuncion;
import ast.declaraciones.DeclaracionVariable;
import ast.declaraciones.Parametro;
import ast.estructuras.Campo;
import ast.estructuras.Estructura;
import ast.estructuras.InicializacionEstructura;
import ast.expresiones.Expresion;
import ast.expresiones.ExpresionBinaria;
import ast.expresiones.Literal;
import ast.sentencias.Bloque;
import ast.sentencias.CondicionIf;
import ast.sentencias.Sentencia;
import ast.tipos.Tipo;
import org.antlr.v4.runtime.ParserRuleContext;
import org.compi2.proyecto1compiladores2.GrammarPythonBaseVisitor;
import org.compi2.proyecto1compiladores2.GrammarPythonParser;

import java.util.ArrayList;
import java.util.List;


public class VisitorPiton extends GrammarPythonBaseVisitor<NodoAST> {

    @Override
    public NodoAST visitProgram(GrammarPythonParser.ProgramContext ctx) {

        List<Estructura> estructuras = new ArrayList<>();

        if (ctx.seccionEstructuras() != null) {

            for (var e : ctx.seccionEstructuras().declaracionEstructura()) {
                estructuras.add((Estructura) visit(e));
            }
        }

        List<DeclaracionFuncion> funciones = new ArrayList<>();

        if (ctx.seccionFunciones() != null) {

            for (var f : ctx.seccionFunciones().declaracionFuncion()) {
                funciones.add((DeclaracionFuncion) visit(f));
            }
        }

        return new Programa(
                linea(ctx),
                columna(ctx),
                List.of(),
                estructuras,
                List.of(),
                funciones,
                List.of()
        );
    }

    @Override
    public NodoAST visitDeclaracionFuncion(
            GrammarPythonParser.DeclaracionFuncionContext ctx) {

        // Nombre de la función
        String nombreFuncion = ctx.ID().getText();

        // Tipo de retorno
        Tipo tipoRetorno = null;

        if (ctx.tipo() != null) {
            tipoRetorno = construirTipo(ctx.tipo(), null);
        }

        // Parámetros
        List<Parametro> parametros = new ArrayList<>();

        if (ctx.listaParametros() != null) {

            for (var p : ctx.listaParametros().parametro()) {

                if (p instanceof GrammarPythonParser.ParametroSimpleContext simple) {

                    Tipo tipo = construirTipo(simple.tipo(), null);

                    parametros.add(
                            new Parametro(
                                    linea(simple),
                                    columna(simple),
                                    tipo,
                                    simple.ID().getText(),
                                    false,
                                    false
                            )
                    );

                } else if (p instanceof GrammarPythonParser.ParametroArregloContext arreglo) {

                    Tipo tipo = construirTipo(arreglo.tipo(), null);

                    parametros.add(
                            new Parametro(
                                    linea(arreglo),
                                    columna(arreglo),
                                    tipo,
                                    arreglo.ID().getText(),
                                    false,
                                    true
                            )
                    );

                } else if (p instanceof GrammarPythonParser.ParametroEstructuraContext estructura) {

                    Tipo tipo = construirTipo(estructura.tipo(), null);

                    parametros.add(
                            new Parametro(
                                    linea(estructura),
                                    columna(estructura),
                                    tipo,
                                    estructura.ID().getText(),
                                    false,
                                    false
                            )
                    );
                }
            }
        }

        // Cuerpo de la función
        Bloque cuerpoFuncion = (Bloque) visit(ctx.bloque());

        return new DeclaracionFuncion(
                linea(ctx),
                columna(ctx),
                nombreFuncion,
                tipoRetorno,
                parametros,
                cuerpoFuncion
        );
    }


    @Override
    public NodoAST visitDeclaracionEstructura(GrammarPythonParser.DeclaracionEstructuraContext ctx) {
        List<Campo> campos = new ArrayList<>();
        for (var v : ctx.declaracionVariable()) {
            var dv = (DeclaracionVariable) visit(v);
            campos.add(new Campo(dv.getLinea(), dv.getColumna(), dv.getTipo(), dv.getNombre()));
        }
        return new Estructura(linea(ctx), columna(ctx), ctx.ID().getText(), campos);
    }

    @Override
    public NodoAST visitDeclaracionVariable(GrammarPythonParser.DeclaracionVariableContext ctx) {
        Tipo tipo = construirTipo(ctx.tipo(), ctx.dimension());
        Expresion valorInicial = ctx.expresion() != null ? (Expresion) visit(ctx.expresion()) : null;

        if (ctx.dimension() != null) {
            List<Expresion> dims = ctx.dimension().NUMERO_ENTERO().stream()
                    .map(n -> (Expresion) new Literal(linea(ctx), columna(ctx), Integer.parseInt(n.getText()), "entero"))
                    .toList();
            List<Expresion> valores = (valorInicial instanceof InicializacionEstructura ie)
                    ? ie.getValores() : List.of();
            return new DeclaracionArreglo(linea(ctx), columna(ctx), tipo, ctx.ID().getText(), dims, valores);
        }

        return new DeclaracionVariable(linea(ctx), columna(ctx), tipo, ctx.ID().getText(), valorInicial);
    }

    @Override
    public NodoAST visitExpAditiva(GrammarPythonParser.ExpAditivaContext ctx) {
        return new ExpresionBinaria(linea(ctx), columna(ctx),
                (Expresion) visit(ctx.expresion(0)), ctx.op.getText(), (Expresion) visit(ctx.expresion(1)));
    }

    @Override
    public NodoAST visitExpMultiplicativa(GrammarPythonParser.ExpMultiplicativaContext ctx) {
        return new ExpresionBinaria(linea(ctx), columna(ctx),
                (Expresion) visit(ctx.expresion(0)), ctx.op.getText(), (Expresion) visit(ctx.expresion(1)));
    }

    @Override
    public NodoAST visitExpLiteral(GrammarPythonParser.ExpLiteralContext ctx) {
        var lit = ctx.literal();
        if (lit.NUMERO_ENTERO() != null) return new Literal(linea(ctx), columna(ctx), Integer.parseInt(lit.getText()), "entero");
        if (lit.DECIMAL() != null) return new Literal(linea(ctx), columna(ctx), Double.parseDouble(lit.getText()), "flotante");
        if (lit.VERDADERO() != null || lit.FALSO() != null) return new Literal(linea(ctx), columna(ctx), lit.VERDADERO() != null, "bool");
        return new Literal(linea(ctx), columna(ctx), lit.getText(), "cadena");
    }

    @Override
    public NodoAST visitCondicional(GrammarPythonParser.CondicionalContext ctx) {
        Expresion condicion = (Expresion) visit(ctx.expresion(0));
        Bloque bloqueEntonces = (Bloque) visit(ctx.bloque(0));
        // sino-si y contrario se arman igual, recorriendo los índices restantes de ctx
        return new CondicionIf(linea(ctx), columna(ctx), condicion, bloqueEntonces, List.of(), null);
    }

    @Override
    public NodoAST visitBloque(GrammarPythonParser.BloqueContext ctx) {
        List<Sentencia> sentencias = new ArrayList<>();
        for (var s : ctx.sentencia()) {
            sentencias.add((Sentencia) visit(s));
        }
        return new Bloque(linea(ctx), columna(ctx), sentencias);
    }

    private Tipo construirTipo(GrammarPythonParser.TipoContext ctx, GrammarPythonParser.DimensionContext dim) {
        return new Tipo(linea(ctx), columna(ctx), ctx.getText(), dim != null, dim != null ? dim.NUMERO_ENTERO().size() : 0);
    }

    private int linea(ParserRuleContext ctx) {
        return ctx.getStart().getLine();
    }

    private int columna(ParserRuleContext ctx) {
        return ctx.getStart().getCharPositionInLine();
    }
}
