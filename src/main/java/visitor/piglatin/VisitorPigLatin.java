package visitor.piglatin;

import ast.NodoAST;
import ast.Programa;
import ast.clases.Clase;
import ast.declaraciones.Declaracion;
import ast.declaraciones.DeclaracionArreglo;
import ast.declaraciones.DeclaracionFuncion;
import ast.declaraciones.DeclaracionVariable;
import ast.estructuras.Estructura;
import ast.estructuras.InicializacionEstructura;
import ast.expresiones.*;
import ast.sentencias.*;
import ast.tipos.Tipo;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.compi2.proyecto1compiladores2.GrammarPigLatinBaseVisitor;
import org.compi2.proyecto1compiladores2.GrammarPigLatinParser;
import semantico.AnalisisContexto;

import java.util.ArrayList;
import java.util.List;


public class VisitorPigLatin extends GrammarPigLatinBaseVisitor<NodoAST> {

    private final AnalisisContexto analisisContexto;

    public VisitorPigLatin(AnalisisContexto contexto) {
        this.analisisContexto = contexto;
    }

    @Override
    public NodoAST visitProgram(GrammarPigLatinParser.ProgramContext ctx) {

        List<String> importaciones = new ArrayList<>();

        for (var imp : ctx.seccionImport()) {
            importaciones.add(construirRutaImport(imp));
        }

        List<Declaracion> globales = new ArrayList<>();

        if (ctx.seccionVariables() != null) {

            for (var decl : ctx.seccionVariables().declaracion()) {

                NodoAST nodo = visit(decl);

                if (nodo instanceof Declaracion declaracion) {
                    globales.add(declaracion);
                }
            }
        }

        List<DeclaracionFuncion> funciones = new ArrayList<>();

        if (ctx.seccionMain() != null) {

            NodoAST main = visit(ctx.seccionMain());

            if (main instanceof DeclaracionFuncion funcionMain) {
                funciones.add(funcionMain);
            }
        }

        return new Programa(linea(ctx), columna(ctx), importaciones, new ArrayList<Estructura>(), new ArrayList<Clase>(), funciones, globales);
    }

    private String construirRutaImport(GrammarPigLatinParser.SeccionImportContext ctx) {

        StringBuilder ruta = new StringBuilder();
        List<TerminalNode> ids = ctx.ID();

        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                ruta.append('.');
            }
            ruta.append(ids.get(i).getText());
        }

        return ruta.toString();
    }

    @Override
    public NodoAST visitSeccionMain(GrammarPigLatinParser.SeccionMainContext ctx) {

        Bloque cuerpo = construirBloque(ctx, ctx.sentencia());

        return new DeclaracionFuncion(
                linea(ctx),
                columna(ctx),
                "main",
                new Tipo(linea(ctx), columna(ctx), "nihil", false, 0),
                new ArrayList<>(),
                cuerpo
        );
    }


    @Override
    public NodoAST visitDeclaracionVariable(GrammarPigLatinParser.DeclaracionVariableContext ctx) {

        Tipo tipo;
        Expresion inicializacion = null;

        if (ctx.tipo().NOVUS() != null) {

            String nombreClase = ctx.tipo().ID().getText();

            tipo = new Tipo(linea(ctx), columna(ctx), nombreClase, false, 0);

            List<Expresion> argumentos = construirArgumentos(ctx.tipo().listaArgumentos());

            inicializacion = new CrearObjeto(
                    linea(ctx), columna(ctx),
                    nombreClase,
                    argumentos
            );
        }
        else {
            tipo = construirTipo(ctx.tipo());

            if (ctx.expresion() != null) {
                inicializacion = (Expresion) visit(ctx.expresion());
            } else if (ctx.inicializacionStruct() != null) {
                inicializacion = construirInicializacionStruct(
                        ctx.inicializacionStruct(), tipo.getNombre());
            }
        }

        return new DeclaracionVariable(
                linea(ctx), columna(ctx),
                tipo,
                ctx.ID().getText(),
                inicializacion
        );
    }

    @Override
    public NodoAST visitDeclaracionArreglo(GrammarPigLatinParser.DeclaracionArregloContext ctx) {

        Tipo tipoBase = construirTipo(ctx.tipo());


        int dimensiones = ctx.CORCHETE_ABRE().size();

        Tipo tipoArreglo = new Tipo(linea(ctx), columna(ctx), tipoBase.getNombre(), true, dimensiones);

        String nombre = ctx.ID().getText();


        List<Expresion> dimensionesExpr = new ArrayList<>();

        for (var entero : ctx.ENTERO()) {
            dimensionesExpr.add(new Literal(
                    linea(ctx),
                    columna(ctx),
                    Integer.parseInt(entero.getText()),
                    "numerus"
            ));
        }

        List<Expresion> valores = new ArrayList<>();

        if (ctx.listaValoresArreglo() != null) {

            for (var valor : ctx.listaValoresArreglo().valorArreglo()) {
                valores.add(construirValorArreglo(valor, tipoBase.getNombre()));
            }
        }

        return new DeclaracionArreglo(linea(ctx), columna(ctx), tipoArreglo, nombre, dimensionesExpr, valores);
    }

    private Expresion construirValorArreglo(
            GrammarPigLatinParser.ValorArregloContext ctx, String nombreTipo) {

        if (ctx.inicializacionStruct() != null) {
            return construirInicializacionStruct(ctx.inicializacionStruct(), nombreTipo);
        }

        return (Expresion) visit(ctx.expresion());
    }

    private InicializacionEstructura construirInicializacionStruct(
            GrammarPigLatinParser.InicializacionStructContext ctx, String nombreTipo) {

        List<Expresion> valores = new ArrayList<>();

        if (ctx.listaValoresInicializacion() != null) {

            for (var valor : ctx.listaValoresInicializacion().valorInicializacion()) {

                if (valor.inicializacionStruct() != null) {
                    valores.add(construirInicializacionStruct(valor.inicializacionStruct(), null));
                } else {
                    valores.add((Expresion) visit(valor.expresion()));
                }
            }
        }

        return new InicializacionEstructura(
                linea(ctx),
                columna(ctx),
                nombreTipo,
                valores
        );
    }


    private Tipo construirTipo(GrammarPigLatinParser.TipoContext ctx) {

        if (ctx == null) return null;

        if (ctx.NOVUS() != null) {
            return new Tipo(linea(ctx), columna(ctx), ctx.ID().getText(), false, 0);
        }

        return new Tipo(linea(ctx), columna(ctx), ctx.getText(), false, 0);
    }


    @Override
    public NodoAST visitAsignacion(GrammarPigLatinParser.AsignacionContext ctx) {

        Expresion destino = construirAcceso(ctx, ctx.ID(), ctx.acceso());

        Expresion valor;

        if (ctx.inicializacionStruct() != null) {
            valor = construirInicializacionStruct(ctx.inicializacionStruct(), null);
        } else {
            valor = (Expresion) visit(ctx.expresion());
        }

        return new Asignacion(linea(ctx), columna(ctx), destino, valor);
    }

    @Override
    public NodoAST visitIncrementoDecremento(GrammarPigLatinParser.IncrementoDecrementoContext ctx) {

        Expresion destino = construirAcceso(ctx, ctx.ID(), ctx.acceso());

        String operador = ctx.INCREMENTO() != null ? "++" : "--";

        return new SentenciaExpresion(
                linea(ctx),
                columna(ctx),
                new ExpresionUnaria(linea(ctx), columna(ctx), operador, destino, false)
        );
    }

    @Override
    public NodoAST visitCondicional(GrammarPigLatinParser.CondicionalContext ctx) {

        List<GrammarPigLatinParser.ExpresionContext> condiciones = ctx.expresion();
        List<GrammarPigLatinParser.BloquePigContext> bloques = ctx.bloquePig();

        Expresion condicionPrincipal = (Expresion) visit(condiciones.get(0));
        Bloque bloquePrincipal = (Bloque) visit(bloques.get(0));

        List<CondicionIf> ramasIntermedias = new ArrayList<>();

        for (int i = 1; i < condiciones.size(); i++) {

            ramasIntermedias.add(new CondicionIf(
                    linea(condiciones.get(i)),
                    columna(condiciones.get(i)),
                    (Expresion) visit(condiciones.get(i)),
                    (Bloque) visit(bloques.get(i)),
                    new ArrayList<>(),
                    null
            ));
        }

        Bloque bloqueElse = null;

        if (bloques.size() > condiciones.size()) {
            bloqueElse = (Bloque) visit(bloques.get(bloques.size() - 1));
        }

        return new CondicionIf(
                linea(ctx),
                columna(ctx),
                condicionPrincipal,
                bloquePrincipal,
                ramasIntermedias,
                bloqueElse
        );
    }

    @Override
    public NodoAST visitBloquePig(GrammarPigLatinParser.BloquePigContext ctx) {
        return construirBloque(ctx, ctx.sentencia());
    }

    @Override
    public NodoAST visitCicloDum(GrammarPigLatinParser.CicloDumContext ctx) {

        return new CicloWhile(
                linea(ctx),
                columna(ctx),
                (Expresion) visit(ctx.expresion()),
                construirBloque(ctx, ctx.sentencia())
        );
    }

    @Override
    public NodoAST visitCicloFacere(GrammarPigLatinParser.CicloFacereContext ctx) {

        return new CicloDoWhile(
                linea(ctx),
                columna(ctx),
                construirBloque(ctx, ctx.sentencia()),
                (Expresion) visit(ctx.expresion())
        );
    }

    @Override
    public NodoAST visitCicloPer(GrammarPigLatinParser.CicloPerContext ctx) {

        Sentencia inicializacion = null;

        NodoAST nodoInicial = visit(ctx.inicializacionPer());

        if (nodoInicial instanceof Sentencia sentencia) {
            inicializacion = sentencia;
        }

        Expresion condicion = (Expresion) visit(ctx.expresion());

        Expresion actualizacion = null;

        NodoAST nodoActualizacion = visit(ctx.actualizacionPer());

        if (nodoActualizacion instanceof Expresion expresion) {
            actualizacion = expresion;
        }

        return new CicloFor(
                linea(ctx),
                columna(ctx),
                inicializacion,
                condicion,
                actualizacion,
                construirBloque(ctx, ctx.sentencia())
        );
    }

    @Override
    public NodoAST visitInicializacionPer(GrammarPigLatinParser.InicializacionPerContext ctx) {

        if (ctx.ESTO() != null) {

            Expresion valorInicial = ctx.expresion() == null
                    ? null
                    : (Expresion) visit(ctx.expresion());

            return new DeclaracionVariable(
                    linea(ctx),
                    columna(ctx),
                    construirTipo(ctx.tipo()),
                    ctx.ID().getText(),
                    valorInicial
            );
        }

        return new Asignacion(
                linea(ctx),
                columna(ctx),
                construirAcceso(ctx, ctx.ID(), ctx.acceso()),
                (Expresion) visit(ctx.expresion())
        );
    }

    @Override
    public NodoAST visitActualizacionPer(GrammarPigLatinParser.ActualizacionPerContext ctx) {

        Expresion destino = construirAcceso(ctx, ctx.ID(), ctx.acceso());

        if (ctx.INCREMENTO() != null || ctx.DECREMENTO() != null) {

            String operador = ctx.INCREMENTO() != null ? "++" : "--";

            return new ExpresionUnaria(linea(ctx), columna(ctx), operador, destino, false);
        }

        return new Asignacion(
                linea(ctx),
                columna(ctx),
                destino,
                (Expresion) visit(ctx.expresion())
        );
    }

    @Override
    public NodoAST visitImprimir(GrammarPigLatinParser.ImprimirContext ctx) {

        List<Expresion> argumentos = new ArrayList<>();

        for (var expresion : ctx.expresion()) {
            argumentos.add((Expresion) visit(expresion));
        }

        return new SentenciaExpresion(
                linea(ctx),
                columna(ctx),
                new LlamadaFuncion(linea(ctx), columna(ctx), "print", argumentos)
        );
    }

    @Override
    public NodoAST visitLeer(GrammarPigLatinParser.LeerContext ctx) {

        LlamadaFuncion lectura =
                new LlamadaFuncion(linea(ctx), columna(ctx), "readln", new ArrayList<>());

        if (ctx.ID() == null) {
            return new SentenciaExpresion(linea(ctx), columna(ctx), lectura);
        }

        return new Asignacion(
                linea(ctx),
                columna(ctx),
                construirAcceso(ctx, ctx.ID(), ctx.acceso()),
                lectura
        );
    }

    @Override
    public NodoAST visitLlamadaFuncionSentencia(
            GrammarPigLatinParser.LlamadaFuncionSentenciaContext ctx) {

        return new SentenciaExpresion(
                linea(ctx),
                columna(ctx),
                (Expresion) visit(ctx.llamadaFuncion())
        );
    }


    @Override
    public NodoAST visitLlamadaMetodoSentencia(
            GrammarPigLatinParser.LlamadaMetodoSentenciaContext ctx) {

        Expresion receptor = new Identificador(linea(ctx), columna(ctx), ctx.ID(0).getText());

        List<GrammarPigLatinParser.AccesoContext> accesos = ctx.acceso();
        for (GrammarPigLatinParser.AccesoContext acceso : accesos) {
            receptor = aplicarAcceso(receptor, acceso);
        }

        LlamadaMetodo llamada = new LlamadaMetodo(linea(ctx), columna(ctx), receptor, ctx.ID(ctx.ID().size() - 1).getText(),
                construirArgumentos(ctx.listaArgumentos()));

        return new SentenciaExpresion(linea(ctx), columna(ctx), llamada);
    }

    @Override
    public NodoAST visitSentencia(GrammarPigLatinParser.SentenciaContext ctx) {

        if (ctx.PERGE() != null) {
            return new SentenciaContinue(linea(ctx), columna(ctx));
        }

        if (ctx.INTERRUMPE() != null) {
            return new SentenciaBreak(linea(ctx), columna(ctx));
        }

        return visit(ctx.getChild(0));
    }


    @Override
    public NodoAST visitExpresion(GrammarPigLatinParser.ExpresionContext ctx) {
        return visit(ctx.expresionOr());
    }

    @Override
    public NodoAST visitExpresionOr(GrammarPigLatinParser.ExpresionOrContext ctx) {
        return plegarIzquierda(ctx, ctx.expresionAnd(), "||");
    }

    @Override
    public NodoAST visitExpresionAnd(GrammarPigLatinParser.ExpresionAndContext ctx) {
        return plegarIzquierda(ctx, ctx.expresionIgualdad(), "&&");
    }

    @Override
    public NodoAST visitExpresionIgualdad(GrammarPigLatinParser.ExpresionIgualdadContext ctx) {
        return plegarConOperadores(ctx, ctx.expresionRelacional());
    }

    @Override
    public NodoAST visitExpresionRelacional(GrammarPigLatinParser.ExpresionRelacionalContext ctx) {
        return plegarConOperadores(ctx, ctx.expresionAditiva());
    }

    @Override
    public NodoAST visitExpresionAditiva(GrammarPigLatinParser.ExpresionAditivaContext ctx) {
        return plegarConOperadores(ctx, ctx.expresionMultiplicativa());
    }

    @Override
    public NodoAST visitExpresionMultiplicativa(
            GrammarPigLatinParser.ExpresionMultiplicativaContext ctx) {

        return plegarConOperadores(ctx, ctx.expresionUnaria());
    }

    private NodoAST plegarIzquierda(
            ParserRuleContext ctx, List<? extends ParserRuleContext> operandos, String operador) {

        Expresion izquierda = (Expresion) visit(operandos.get(0));

        for (int i = 1; i < operandos.size(); i++) {

            izquierda = new ExpresionBinaria(
                    linea(ctx),
                    columna(ctx),
                    izquierda,
                    operador,
                    (Expresion) visit(operandos.get(i))
            );
        }

        return izquierda;
    }

    private NodoAST plegarConOperadores(
            ParserRuleContext ctx, List<? extends ParserRuleContext> operandos) {

        Expresion izquierda = (Expresion) visit(operandos.get(0));

        for (int i = 1; i < operandos.size(); i++) {

            String operador = ctx.getChild(2 * i - 1).getText();

            izquierda = new ExpresionBinaria(
                    linea(ctx),
                    columna(ctx),
                    izquierda,
                    operador,
                    (Expresion) visit(operandos.get(i))
            );
        }

        return izquierda;
    }

    @Override
    public NodoAST visitExpresionUnaria(GrammarPigLatinParser.ExpresionUnariaContext ctx) {

        if (ctx.expresionPrimaria() != null) {
            return visit(ctx.expresionPrimaria());
        }

        String operador = ctx.NEGACION() != null ? "!" : "-";

        return new ExpresionUnaria(
                linea(ctx),
                columna(ctx),
                operador,
                (Expresion) visit(ctx.expresionUnaria()),
                true
        );
    }

    @Override
    public NodoAST visitExpresionPrimaria(GrammarPigLatinParser.ExpresionPrimariaContext ctx) {

        if (ctx.literal() != null) {
            return construirLiteral(ctx.literal());
        }

        if (ctx.creacionObjeto() != null) {
            return visit(ctx.creacionObjeto());
        }

        if (ctx.llamadaFuncion() != null) {
            return visit(ctx.llamadaFuncion());
        }

        if (ctx.expresion() != null) {
            return visit(ctx.expresion());
        }

        Expresion base = construirAcceso(ctx, ctx.ID(), ctx.acceso());

        if (ctx.INCREMENTO() != null || ctx.DECREMENTO() != null) {

            String operador = ctx.INCREMENTO() != null ? "++" : "--";

            return new ExpresionUnaria(linea(ctx), columna(ctx), operador, base, false);
        }

        return base;
    }

    @Override
    public NodoAST visitCreacionObjeto(GrammarPigLatinParser.CreacionObjetoContext ctx) {

        return new CrearObjeto(
                linea(ctx),
                columna(ctx),
                ctx.ID().getText(),
                construirArgumentos(ctx.listaArgumentos())
        );
    }

    @Override
    public NodoAST visitLlamadaFuncion(GrammarPigLatinParser.LlamadaFuncionContext ctx) {

        return new LlamadaFuncion(
                linea(ctx),
                columna(ctx),
                ctx.ID().getText(),
                construirArgumentos(ctx.listaArgumentos())
        );
    }



    private Expresion construirAcceso(
            ParserRuleContext ctx,
            TerminalNode identificadorBase,
            List<GrammarPigLatinParser.AccesoContext> accesos) {

        Expresion actual = new Identificador(
                linea(ctx),
                columna(ctx),
                identificadorBase.getText()
        );

        if (accesos == null) {
            return actual;
        }

        for (GrammarPigLatinParser.AccesoContext acceso : accesos) {
            actual = aplicarAcceso(actual, acceso);
        }

        return actual;
    }


    private Expresion aplicarAcceso(
            Expresion base,
            GrammarPigLatinParser.AccesoContext acceso) {

        int linea = linea(acceso);
        int columna = columna(acceso);

        if (acceso.CORCHETE_ABRE() != null) {

            List<Expresion> indices = new ArrayList<>();
            indices.add((Expresion) visit(acceso.expresion()));

            return new AccesoArreglo(linea, columna, base, indices);
        }

        if (acceso.PARENTESIS_ABRE() != null) {

            return new LlamadaMetodo(
                    linea,
                    columna,
                    base,
                    acceso.ID().getText(),
                    construirArgumentos(acceso.listaArgumentos())
            );
        }

        return new AccesoAtributo(linea, columna, base, acceso.ID().getText());
    }


    private Bloque construirBloque(
            ParserRuleContext ctx, List<GrammarPigLatinParser.SentenciaContext> sentencias) {

        List<Sentencia> lista = new ArrayList<>();

        if (sentencias != null) {

            for (var contextoSentencia : sentencias) {

                NodoAST nodo = visit(contextoSentencia);

                if (nodo instanceof Sentencia sentencia) {
                    lista.add(sentencia);
                }
            }
        }

        return new Bloque(linea(ctx), columna(ctx), lista);
    }

    private List<Expresion> construirArgumentos(
            GrammarPigLatinParser.ListaArgumentosContext ctx) {

        List<Expresion> argumentos = new ArrayList<>();

        if (ctx == null) {
            return argumentos;
        }

        for (var expresion : ctx.expresion()) {
            argumentos.add((Expresion) visit(expresion));
        }

        return argumentos;
    }

    private Literal construirLiteral(GrammarPigLatinParser.LiteralContext ctx) {

        int linea = linea(ctx);
        int columna = columna(ctx);
        String texto = ctx.getText();

        if (ctx.ENTERO() != null) {
            return new Literal(linea, columna, Integer.parseInt(texto), "numerus");
        }

        if (ctx.DECIMAL() != null) {
            return new Literal(linea, columna, Double.parseDouble(texto), "decimalis");
        }

        if (ctx.VERUM() != null) {
            return new Literal(linea, columna, true, "bool");
        }

        if (ctx.FALSUS() != null) {
            return new Literal(linea, columna, false, "bool");
        }

        if (ctx.NULL() != null) {
            return new Literal(linea, columna, null, "null");
        }

        if (ctx.COMILLASSIMPLES() != null) {
            return new Literal(linea, columna, texto, "littera");
        }

        if (ctx.COMILLAS() != null) {
            return new Literal(linea, columna, texto, "textum");
        }
        return new Literal(linea, columna, texto, "textum");
    }

    private int linea(ParserRuleContext ctx) {
        return ctx.getStart().getLine();
    }

    private int columna(ParserRuleContext ctx) {
        return ctx.getStart().getCharPositionInLine();
    }
}