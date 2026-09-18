package visitor.zetariano;

import ast.NodoAST;
import ast.clases.Atributo;
import ast.clases.Clase;
import ast.clases.Constructor;
import ast.clases.Metodo;
import ast.declaraciones.DeclaracionArreglo;
import ast.declaraciones.DeclaracionVariable;
import ast.declaraciones.Parametro;
import ast.expresiones.*;
import ast.sentencias.*;
import ast.tipos.Tipo;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.compi2.proyecto1compiladores2.GrammarZetarianoBaseVisitor;
import org.compi2.proyecto1compiladores2.GrammarZetarianoParser;

import semantico.AnalisisContexto;

import java.util.ArrayList;
import java.util.List;

public class VisitorZetariano extends GrammarZetarianoBaseVisitor<NodoAST> {

    private final AnalisisContexto analisisContexto;

    public VisitorZetariano(AnalisisContexto contexto) {
        this.analisisContexto = contexto;
    }

    /* =========================================================
       PROGRAM
       ========================================================= */

    @Override
    public NodoAST visitProgram(GrammarZetarianoParser.ProgramContext ctx) {
        return visit(ctx.creacionClase());
    }

    @Override
    public NodoAST visitCreacionClase(GrammarZetarianoParser.CreacionClaseContext ctx) {

        String nombreClase = ctx.ID().getText();

        List<Atributo> atributos = new ArrayList<>();
        List<Constructor> constructores = new ArrayList<>();
        List<Metodo> metodos = new ArrayList<>();

        for (var contenido : ctx.contenidoClase()) {

            if (contenido instanceof GrammarZetarianoParser.ContenidoAtributoContext a) {

                NodoAST nodo = visit(a.atributo());
                if (nodo instanceof Atributo atributo) atributos.add(atributo);

            } else if (contenido instanceof GrammarZetarianoParser.ContenidoConstructorContext c) {

                NodoAST nodo = visit(c.constructor());
                if (nodo instanceof Constructor constructor) constructores.add(constructor);

            } else if (contenido instanceof GrammarZetarianoParser.ContenidoMetodoContext m) {

                NodoAST nodo = visit(m.metodo());
                if (nodo instanceof Metodo metodo) metodos.add(metodo);
            }
        }

        return new Clase(linea(ctx), columna(ctx), nombreClase, atributos, constructores, metodos);
    }

    /* =========================================================
       ATRIBUTOS
       ========================================================= */

    @Override
    public NodoAST visitAtributo(GrammarZetarianoParser.AtributoContext ctx) {

        Tipo tipo = construirTipo(ctx.tipo());
        String nombre = ctx.ID().getText();

        Atributo atributo = new Atributo(linea(ctx), columna(ctx), tipo, nombre);

        if (esTipoArreglo(ctx.tipo())) {

            List<Expresion> valoresIniciales = new ArrayList<>();
            if (ctx.listaValores() != null) {
                for (var elemento : ctx.listaValores().elementoLista()) {
                    valoresIniciales.add((Expresion) visit(elemento));
                }
            }

            List<Expresion> dimensiones = new ArrayList<>();
            if (ctx.expresion() != null) {
                NodoAST inicializacion = visit(ctx.expresion());
                if (inicializacion instanceof CrearArreglo crearArreglo) {
                    dimensiones = crearArreglo.getDimensiones();
                }
            }

            if (dimensiones.isEmpty()) {
                for (int i = 0; i < cantidadDimensiones(ctx.tipo()); i++) {
                    dimensiones.add(null);
                }
            }

            atributo.setDimensiones(dimensiones);
            atributo.setValoresIniciales(valoresIniciales);

        } else if (ctx.expresion() != null) {
            atributo.setInicializacion((Expresion) visit(ctx.expresion()));
        }

        return atributo;
    }

    /* =========================================================
       CONSTRUCTOR
       ========================================================= */

    @Override
    public NodoAST visitConstructor(GrammarZetarianoParser.ConstructorContext ctx) {

        String nombreConstructor = ctx.ID().getText();
        List<Parametro> parametros = construirParametros(ctx.listaParametros());
        Bloque cuerpo = (Bloque) visit(ctx.bloque());

        return new Constructor(linea(ctx), columna(ctx), nombreConstructor, parametros, cuerpo);
    }

    /* =========================================================
       METODO
       ========================================================= */

    @Override
    public NodoAST visitMetodo(GrammarZetarianoParser.MetodoContext ctx) {

        String nombreMetodo = ctx.ID().getText();

        Tipo tipoRetorno;
        if (ctx.VOID() != null) {
            tipoRetorno = new Tipo(linea(ctx), columna(ctx), "void", false, 0);
        } else {
            tipoRetorno = construirTipo(ctx.tipo());
        }

        List<Parametro> parametros = construirParametros(ctx.listaParametros());
        Bloque cuerpo = (Bloque) visit(ctx.bloque());

        return new Metodo(linea(ctx), columna(ctx), nombreMetodo, tipoRetorno, parametros, cuerpo);
    }

    /* =========================================================
       PARAMETROS
       ========================================================= */

    private List<Parametro> construirParametros(GrammarZetarianoParser.ListaParametrosContext ctx) {

        List<Parametro> resultado = new ArrayList<>();
        if (ctx == null) return resultado;

        for (var p : ctx.parametro()) {

            Tipo tipo = construirTipo(p.tipo());
            boolean esArreglo = esTipoArreglo(p.tipo());

            resultado.add(new Parametro(
                    linea(p), columna(p),
                    tipo,
                    p.ID().getText(),
                    false,
                    esArreglo
            ));
        }

        return resultado;
    }

    /* =========================================================
       TIPOS
       ========================================================= */

    private Tipo construirTipo(GrammarZetarianoParser.TipoContext ctx) {

        if (ctx == null) return null;

        String tipoBase = ctx.tipoBase().getText();
        boolean esArreglo = !ctx.CORCHETE_ABRE().isEmpty();
        int dimensiones = ctx.CORCHETE_ABRE().size();

        return new Tipo(linea(ctx), columna(ctx), tipoBase, esArreglo, dimensiones);
    }

    private boolean esTipoArreglo(GrammarZetarianoParser.TipoContext ctx) {
        return ctx != null && !ctx.CORCHETE_ABRE().isEmpty();
    }

    private int cantidadDimensiones(GrammarZetarianoParser.TipoContext ctx) {
        return ctx == null ? 0 : ctx.CORCHETE_ABRE().size();
    }

    /* =========================================================
       BLOQUE
       ========================================================= */

    @Override
    public NodoAST visitBloque(GrammarZetarianoParser.BloqueContext ctx) {

        List<Sentencia> sentencias = new ArrayList<>();

        for (var s : ctx.sentencia()) {
            NodoAST nodo = visit(s);
            if (nodo instanceof Sentencia sentencia) sentencias.add(sentencia);
        }

        return new Bloque(linea(ctx), columna(ctx), sentencias);
    }

    /* =========================================================
       DECLARACION VARIABLE / ARREGLO
       ========================================================= */

    @Override
    public NodoAST visitDeclaracionVariable(GrammarZetarianoParser.DeclaracionVariableContext ctx) {

        Tipo tipo = construirTipo(ctx.tipo());
        String nombre = ctx.ID().getText();

        if (esTipoArreglo(ctx.tipo())) {

            List<Expresion> valoresIniciales = new ArrayList<>();
            if (ctx.listaValores() != null) {
                for (var expresion : ctx.listaValores().elementoLista()) {
                    valoresIniciales.add((Expresion) visit(expresion));
                }
            }

            List<Expresion> dimensiones = new ArrayList<>();
            if (ctx.expresion() != null) {
                NodoAST inicializacion = visit(ctx.expresion());
                if (inicializacion instanceof CrearArreglo crearArreglo) {
                    dimensiones = crearArreglo.getDimensiones();
                }
            }

            if (dimensiones.isEmpty()) {
                for (int i = 0; i < cantidadDimensiones(ctx.tipo()); i++) {
                    dimensiones.add(null);
                }
            }

            return new DeclaracionArreglo(linea(ctx), columna(ctx), tipo, nombre, dimensiones, valoresIniciales);
        }

        Expresion inicializacion = null;
        if (ctx.expresion() != null) {
            inicializacion = (Expresion) visit(ctx.expresion());
        }

        return new DeclaracionVariable(linea(ctx), columna(ctx), tipo, nombre, inicializacion);
    }

    /* =========================================================
       ASIGNACION
       ========================================================= */

    @Override
    public NodoAST visitAsignacion(GrammarZetarianoParser.AsignacionContext ctx) {

        Expresion destino = construirAccesoVariable(ctx.accesoVariable());
        Expresion valor = (Expresion) visit(ctx.expresion());
        String operador = ctx.op.getText();

        if (operador.equals("=")) {
            return new Asignacion(linea(ctx), columna(ctx), destino, valor);
        }

        String operadorBinario;
        switch (operador) {
            case "+=": operadorBinario = "+"; break;
            case "-=": operadorBinario = "-"; break;
            case "*=": operadorBinario = "*"; break;
            default:   operadorBinario = operador;
        }

        Expresion combinada = new ExpresionBinaria(linea(ctx), columna(ctx), destino, operadorBinario, valor);
        return new Asignacion(linea(ctx), columna(ctx), destino, combinada);
    }

    /* =========================================================
       INCREMENTOS
       ========================================================= */

    @Override
    public NodoAST visitSentIncrDecrPostfijo(GrammarZetarianoParser.SentIncrDecrPostfijoContext ctx) {
        Expresion destino = construirAccesoVariable(ctx.accesoVariable());
        String operador = ctx.INCREMENTO() != null ? "++" : "--";
        ExpresionUnaria inc = new ExpresionUnaria(linea(ctx), columna(ctx), operador, destino, false);
        return new SentenciaExpresion(linea(ctx), columna(ctx), inc);
    }

    @Override
    public NodoAST visitSentIncrDecrPrefijo(GrammarZetarianoParser.SentIncrDecrPrefijoContext ctx) {
        Expresion destino = construirAccesoVariable(ctx.accesoVariable());
        String operador = ctx.INCREMENTO() != null ? "++" : "--";
        ExpresionUnaria inc = new ExpresionUnaria(linea(ctx), columna(ctx), operador, destino, true);
        return new SentenciaExpresion(linea(ctx), columna(ctx), inc);
    }

    /* =========================================================
       SENTENCIAS DELEGADAS
       ========================================================= */

    @Override
    public NodoAST visitSentDeclaracionVariable(GrammarZetarianoParser.SentDeclaracionVariableContext ctx) {
        return visit(ctx.declaracionVariable());
    }

    @Override
    public NodoAST visitSentAsignacion(GrammarZetarianoParser.SentAsignacionContext ctx) {
        return visit(ctx.asignacion());
    }

    @Override
    public NodoAST visitSentLlamadaFuncion(GrammarZetarianoParser.SentLlamadaFuncionContext ctx) {
        return new SentenciaExpresion(linea(ctx), columna(ctx), (Expresion) visit(ctx.llamadaFuncion()));
    }

    @Override
    public NodoAST visitSentLlamadaMetodo(GrammarZetarianoParser.SentLlamadaMetodoContext ctx) {
        return new SentenciaExpresion(linea(ctx), columna(ctx), (Expresion) visit(ctx.llamadaMetodo()));
    }

    @Override
    public NodoAST visitSentImprimir(GrammarZetarianoParser.SentImprimirContext ctx) {
        return visit(ctx.imprimirStmt());
    }

    @Override
    public NodoAST visitSentReadln(GrammarZetarianoParser.SentReadlnContext ctx) {
        return new SentenciaExpresion(linea(ctx), columna(ctx),
                new LlamadaFuncion(linea(ctx), columna(ctx), "readln", List.of()));
    }

    @Override
    public NodoAST visitSentReturn(GrammarZetarianoParser.SentReturnContext ctx) {
        Expresion valor = ctx.expresion() != null ? (Expresion) visit(ctx.expresion()) : null;
        return new SentenciaReturn(linea(ctx), columna(ctx), valor);
    }

    @Override
    public NodoAST visitSentBreak(GrammarZetarianoParser.SentBreakContext ctx) {
        return new SentenciaBreak(linea(ctx), columna(ctx));
    }

    @Override
    public NodoAST visitSentContinue(GrammarZetarianoParser.SentContinueContext ctx) {
        return new SentenciaContinue(linea(ctx), columna(ctx));
    }

    @Override
    public NodoAST visitSentCondicional(GrammarZetarianoParser.SentCondicionalContext ctx) {
        return visit(ctx.condicional());
    }

    @Override
    public NodoAST visitSentCicloFor(GrammarZetarianoParser.SentCicloForContext ctx) {
        return visit(ctx.cicloFor());
    }

    @Override
    public NodoAST visitSentCicloWhile(GrammarZetarianoParser.SentCicloWhileContext ctx) {
        return visit(ctx.cicloWhile());
    }

    @Override
    public NodoAST visitSentCicloDoWhile(GrammarZetarianoParser.SentCicloDoWhileContext ctx) {
        return visit(ctx.cicloDoWhile());
    }

    @Override
    public NodoAST visitSentSwitch(GrammarZetarianoParser.SentSwitchContext ctx) {
        return visit(ctx.switchCase());
    }

    @Override
    public NodoAST visitSentBloqueAnidado(GrammarZetarianoParser.SentBloqueAnidadoContext ctx) {
        return visit(ctx.bloque());
    }

    /* =========================================================
       PRINTLN / PRINT
       ========================================================= */

    @Override
    public NodoAST visitImprimirStmt(GrammarZetarianoParser.ImprimirStmtContext ctx) {

        String nombreFuncion = ctx.PRINTLN() != null ? "println" : "print";

        List<Expresion> argumentos = new ArrayList<>();
        if (ctx.expresion() != null) {
            argumentos.add((Expresion) visit(ctx.expresion()));
        }

        LlamadaFuncion llamada = new LlamadaFuncion(linea(ctx), columna(ctx), nombreFuncion, argumentos);
        return new SentenciaExpresion(linea(ctx), columna(ctx), llamada);
    }

    /* =========================================================
       CONDICIONAL
       ========================================================= */

    @Override
    public NodoAST visitCondicional(GrammarZetarianoParser.CondicionalContext ctx) {

        Expresion condicion = (Expresion) visit(ctx.expresion(0));
        Bloque bloqueEntonces = (Bloque) visit(ctx.cuerpo(0));

        int cantidadExpresiones = ctx.expresion().size();
        int cantidadBloques = ctx.cuerpo().size();

        boolean tieneElse = cantidadBloques > cantidadExpresiones;
        int cantidadElseIf = cantidadExpresiones - 1;

        List<CondicionIf> listaElseIf = new ArrayList<>();

        for (int i = 0; i < cantidadElseIf; i++) {
            Expresion condicionElseIf = (Expresion) visit(ctx.expresion(i + 1));
            Bloque bloqueElseIf = (Bloque) visit(ctx.cuerpo(i + 1));

            listaElseIf.add(new CondicionIf(
                    linea(ctx), columna(ctx),
                    condicionElseIf, bloqueElseIf, List.of(), null
            ));
        }

        Bloque bloqueElse = null;
        if (tieneElse) {
            bloqueElse = (Bloque) visit(ctx.cuerpo(cantidadBloques - 1));
        }

        return new CondicionIf(linea(ctx), columna(ctx), condicion, bloqueEntonces, listaElseIf, bloqueElse);
    }

    /* =========================================================
       CICLOS
       ========================================================= */

    @Override
    public NodoAST visitCicloWhile(GrammarZetarianoParser.CicloWhileContext ctx) {
        Expresion condicion = (Expresion) visit(ctx.expresion());
        Bloque cuerpo = (Bloque) visit(ctx.cuerpo());
        return new CicloWhile(linea(ctx), columna(ctx), condicion, cuerpo);
    }

    @Override
    public NodoAST visitCicloDoWhile(GrammarZetarianoParser.CicloDoWhileContext ctx) {
        Bloque cuerpo = (Bloque) visit(ctx.bloque());
        Expresion condicion = (Expresion) visit(ctx.expresion());
        return new CicloDoWhile(linea(ctx), columna(ctx), cuerpo, condicion);
    }

    @Override
    public NodoAST visitCicloFor(GrammarZetarianoParser.CicloForContext ctx) {

        Sentencia inicializacion = null;

        if (ctx.declaracionVariable() != null) {
            NodoAST nodo = visit(ctx.declaracionVariable());
            if (nodo instanceof Sentencia sentencia) inicializacion = sentencia;
        } else if (!ctx.asignacion().isEmpty()) {
            NodoAST nodo = visit(ctx.asignacion(0));
            if (nodo instanceof Sentencia sentencia) inicializacion = sentencia;
        }

        Expresion condicion = ctx.expresion() != null ? (Expresion) visit(ctx.expresion()) : null;

        Expresion incremento = null;

        if (ctx.asignacion().size() > 1) {
            NodoAST nodo = visit(ctx.asignacion(1));
            if (nodo instanceof Expresion expresion) incremento = expresion;
        }

        if (ctx.accesoVariable() != null) {
            Expresion destino = construirAccesoVariable(ctx.accesoVariable());
            String operador = ctx.INCREMENTO() != null ? "++" : "--";
            incremento = new ExpresionUnaria(linea(ctx), columna(ctx), operador, destino, ctx.INCREMENTO() != null);
        }

        Bloque cuerpo = (Bloque) visit(ctx.cuerpo());

        return new CicloFor(linea(ctx), columna(ctx), inicializacion, condicion, incremento, cuerpo);
    }

    /* =========================================================
       SWITCH
       ========================================================= */

    @Override
    public NodoAST visitSwitchCase(GrammarZetarianoParser.SwitchCaseContext ctx) {

        Expresion expresionSwitch = (Expresion) visit(ctx.expresion());

        List<SentenciaCase> casos = new ArrayList<>();
        Bloque bloqueDefault = null;

        for (var caso : ctx.casoSwitch()) {
            NodoAST nodo = visit(caso);
            if (nodo instanceof SentenciaCase sentenciaCase) casos.add(sentenciaCase);
        }

        for (var def : ctx.casoDefault()) {
            // Solo debería haber uno, pero por si acaso nos quedamos con el último
            bloqueDefault = visitarCuerpoDefault(def);
        }

        return new CondicionSwitch(linea(ctx), columna(ctx), expresionSwitch, casos, bloqueDefault);
    }

    private Bloque visitarCuerpoDefault(GrammarZetarianoParser.CasoDefaultContext ctx) {
        List<Sentencia> sentencias = new ArrayList<>();
        for (var s : ctx.sentencia()) {
            NodoAST nodo = visit(s);
            if (nodo instanceof Sentencia sentencia) sentencias.add(sentencia);
        }
        return new Bloque(linea(ctx), columna(ctx), sentencias);
    }

    @Override
    public NodoAST visitCasoSwitch(GrammarZetarianoParser.CasoSwitchContext ctx) {

        Expresion valor = construirLiteral(ctx.literal());

        List<Sentencia> sentencias = new ArrayList<>();
        for (var s : ctx.sentencia()) {
            NodoAST nodo = visit(s);
            if (nodo instanceof Sentencia sentencia) sentencias.add(sentencia);
        }

        Bloque cuerpo = new Bloque(linea(ctx), columna(ctx), sentencias);

        return new SentenciaCase(linea(ctx), columna(ctx), valor, cuerpo);
    }

    /* =========================================================
       EXPRESIONES
       ========================================================= */

    @Override
    public NodoAST visitExpParentesis(GrammarZetarianoParser.ExpParentesisContext ctx) {
        return visit(ctx.expresion());
    }

    /** Unaria: -x, !x, ++x, --x */
    @Override
    public NodoAST visitExpUnaria(GrammarZetarianoParser.ExpUnariaContext ctx) {

        String operador;

        if (ctx.NEGACION() != null)      operador = "!";
        else if (ctx.RESTA() != null)    operador = "-";
        else if (ctx.INCREMENTO() != null) operador = "++";
        else                             operador = "--";

        boolean prefijo = !operador.equals("!") && !operador.equals("-");

        return new ExpresionUnaria(
                linea(ctx), columna(ctx),
                operador,
                (Expresion) visit(ctx.expresionUnaria()),
                prefijo || operador.equals("!") || operador.equals("-")
        );
    }

    /** Sin unaria: pasa al postfijo. */
    @Override
    public NodoAST visitExpSinUnaria(GrammarZetarianoParser.ExpSinUnariaContext ctx) {
        return visit(ctx.expresionPostfija());
    }

    /** Postfijo: a++ o a-- */
    @Override
    public NodoAST visitExpPostfija(GrammarZetarianoParser.ExpPostfijaContext ctx) {

        Expresion base = (Expresion) visit(ctx.expresionPrimaria());

        if (ctx.INCREMENTO() == null && ctx.DECREMENTO() == null) {
            return base;
        }

        String operador = ctx.INCREMENTO() != null ? "++" : "--";
        return new ExpresionUnaria(linea(ctx), columna(ctx), operador, base, false);
    }

    /*TERNARIO*/
    @Override
    public NodoAST visitExpTernaria(GrammarZetarianoParser.ExpTernariaContext ctx) {
        return new ExpresionTernaria(
                linea(ctx), columna(ctx),
                (Expresion) visit(ctx.expresionOr()),  // condición
                (Expresion) visit(ctx.expresion(0)),   // rama verdadera
                (Expresion) visit(ctx.expresion(1))    // rama falsa
        );
    }

    /** Sin ternario: pasa a OR. */
    @Override
    public NodoAST visitExpSinTernaria(GrammarZetarianoParser.ExpSinTernariaContext ctx) {
        return visit(ctx.expresionOr());
    }
/* =========================================================
   NIVELES INTERMEDIOS SIN LABEL
   ========================================================= */

    @Override
    public NodoAST visitExpresionOr(GrammarZetarianoParser.ExpresionOrContext ctx) {

        Expresion acumulada = (Expresion) visit(ctx.expresionAnd(0));

        for (int i = 0; i < ctx.OR().size(); i++) {

            TerminalNode op = ctx.OR(i);
            Expresion derecha = (Expresion) visit(ctx.expresionAnd(i + 1));

            acumulada = new ExpresionBinaria(
                    linea(op), columna(op),
                    acumulada, "||", derecha
            );
        }

        return acumulada;
    }

    @Override
    public NodoAST visitExpresionAnd(GrammarZetarianoParser.ExpresionAndContext ctx) {

        Expresion acumulada = (Expresion) visit(ctx.expresionIgualdad(0));

        for (int i = 0; i < ctx.AND().size(); i++) {

            TerminalNode op = ctx.AND(i);
            Expresion derecha = (Expresion) visit(ctx.expresionIgualdad(i + 1));

            acumulada = new ExpresionBinaria(
                    linea(op), columna(op),
                    acumulada, "&&", derecha
            );
        }

        return acumulada;
    }

    @Override
    public NodoAST visitExpresionIgualdad(GrammarZetarianoParser.ExpresionIgualdadContext ctx) {

        Expresion acumulada = (Expresion) visit(ctx.expresionRelacional(0));

        /*
         * Los operadores de igualdad vienen mezclados (== y !=) en el
         * orden en que aparecen. Como ANTLR los agrupa en dos listas
         * separadas (ctx.COMPARACION() y ctx.DIFERENCIA()), no podemos
         * saber el orden real solo con las listas. Recorremos los hijos
         * del contexto para obtener el operador correcto en cada paso.
         */
        int indiceOperando = 1;

        for (int i = 1; i < ctx.getChildCount(); i += 2) {

            TerminalNode op = (TerminalNode) ctx.getChild(i);
            Expresion derecha = (Expresion) visit(ctx.expresionRelacional(indiceOperando++));

            acumulada = new ExpresionBinaria(
                    linea(op), columna(op),
                    acumulada, op.getText(), derecha
            );
        }

        return acumulada;
    }

    @Override
    public NodoAST visitExpresionRelacional(GrammarZetarianoParser.ExpresionRelacionalContext ctx) {

        Expresion acumulada = (Expresion) visit(ctx.expresionAditiva(0));

        int indiceOperando = 1;

        for (int i = 1; i < ctx.getChildCount(); i += 2) {

            TerminalNode op = (TerminalNode) ctx.getChild(i);
            Expresion derecha = (Expresion) visit(ctx.expresionAditiva(indiceOperando++));

            acumulada = new ExpresionBinaria(
                    linea(op), columna(op),
                    acumulada, op.getText(), derecha
            );
        }

        return acumulada;
    }

    @Override
    public NodoAST visitExpresionAditiva(GrammarZetarianoParser.ExpresionAditivaContext ctx) {

        Expresion acumulada = (Expresion) visit(ctx.expresionMultiplicativa(0));

        int indiceOperando = 1;

        for (int i = 1; i < ctx.getChildCount(); i += 2) {

            TerminalNode op = (TerminalNode) ctx.getChild(i);
            Expresion derecha = (Expresion) visit(ctx.expresionMultiplicativa(indiceOperando++));

            acumulada = new ExpresionBinaria(
                    linea(op), columna(op),
                    acumulada, op.getText(), derecha
            );
        }

        return acumulada;
    }

    @Override
    public NodoAST visitExpresionMultiplicativa(GrammarZetarianoParser.ExpresionMultiplicativaContext ctx) {

        Expresion acumulada = (Expresion) visit(ctx.expresionUnaria(0));

        int indiceOperando = 1;

        for (int i = 1; i < ctx.getChildCount(); i += 2) {

            TerminalNode op = (TerminalNode) ctx.getChild(i);
            Expresion derecha = (Expresion) visit(ctx.expresionUnaria(indiceOperando++));

            acumulada = new ExpresionBinaria(
                    linea(op), columna(op),
                    acumulada, op.getText(), derecha
            );
        }

        return acumulada;
    }

    /* =========================================================
       PRIMARIAS
       ========================================================= */

    @Override
    public NodoAST visitExpCrearObjeto(GrammarZetarianoParser.ExpCrearObjetoContext ctx) {
        String nombreClase = ctx.ID().getText();
        List<Expresion> argumentos = construirArgumentos(ctx.listaArgumentos());
        return new CrearObjeto(linea(ctx), columna(ctx), nombreClase, argumentos);
    }

    @Override
    public NodoAST visitExpCrearArreglo(GrammarZetarianoParser.ExpCrearArregloContext ctx) {

        String tipoBase = ctx.tipoBase().getText();

        List<Expresion> dimensiones = new ArrayList<>();
        for (var expresion : ctx.expresion()) {
            dimensiones.add((Expresion) visit(expresion));
        }

        return new CrearArreglo(linea(ctx), columna(ctx), tipoBase, dimensiones, null);
    }

    @Override
    public NodoAST visitExpReadln(GrammarZetarianoParser.ExpReadlnContext ctx) {
        return new LlamadaFuncion(linea(ctx), columna(ctx), "readln", List.of());
    }

    @Override
    public NodoAST visitExpLlamadaMetodo(GrammarZetarianoParser.ExpLlamadaMetodoContext ctx) {
        return visit(ctx.llamadaMetodo());
    }

    @Override
    public NodoAST visitExpLlamadaFuncion(GrammarZetarianoParser.ExpLlamadaFuncionContext ctx) {
        return visit(ctx.llamadaFuncion());
    }

    @Override
    public NodoAST visitExpAcceso(GrammarZetarianoParser.ExpAccesoContext ctx) {
        return construirAccesoVariable(ctx.accesoVariable());
    }

    @Override
    public NodoAST visitExpLiteral(GrammarZetarianoParser.ExpLiteralContext ctx) {
        return construirLiteral(ctx.literal());
    }

    @Override
    public NodoAST visitExpThis(GrammarZetarianoParser.ExpThisContext ctx) {
        return new Identificador(linea(ctx), columna(ctx), "this");
    }

    /* =========================================================
       LLAMADAS
       ========================================================= */

    @Override
    public NodoAST visitLlamadaFuncion(GrammarZetarianoParser.LlamadaFuncionContext ctx) {
        String nombre = ctx.ID().getText();
        List<Expresion> argumentos = construirArgumentos(ctx.listaArgumentos());
        return new LlamadaFuncion(linea(ctx), columna(ctx), nombre, argumentos);
    }

    @Override
    public NodoAST visitLlamadaMetodo(GrammarZetarianoParser.LlamadaMetodoContext ctx) {

        List<TerminalNode> ids = ctx.ID();
        int totalIds = ids.size();

        Expresion objeto;
        int siguienteId;

        if (ctx.THIS() != null) {
            objeto = new Identificador(linea(ctx), columna(ctx), "this");
            siguienteId = 0;
        } else {
            String nombreBase = ids.get(0).getText();
            objeto = new Identificador(linea(ctx), columna(ctx), nombreBase);
            siguienteId = 1;
        }

        while (siguienteId < totalIds - 1) {
            objeto = new AccesoAtributo(linea(ctx), columna(ctx), objeto, ids.get(siguienteId++).getText());
        }

        String nombreMetodo = ids.get(totalIds - 1).getText();
        List<Expresion> argumentos = construirArgumentos(ctx.listaArgumentos());

        return new LlamadaMetodo(linea(ctx), columna(ctx), objeto, nombreMetodo, argumentos);
    }

    /* =========================================================
       HELPERS DE EXPRESIONES BINARIAS
       ========================================================= */

    private Expresion recorrerBinariaIzquierda(
            List<? extends GrammarZetarianoParser.ExpresionAndContext> operandos,
            List<TerminalNode> operadores,
            String simbolo) {

        Expresion acumulada = (Expresion) visit(operandos.get(0));

        for (int i = 0; i < operadores.size(); i++) {
            Expresion derecha = (Expresion) visit(operandos.get(i + 1));
            acumulada = new ExpresionBinaria(linea(operadores.get(i)), columna(operadores.get(i)), acumulada, simbolo, derecha);
        }

        return acumulada;
    }

    private Expresion recorrerBinariaMixta(
            List<? extends GrammarZetarianoParser.ExpresionMultiplicativaContext> operandos,
            List<TerminalNode> ops1,
            List<TerminalNode> ops2,
            String simbolo1, String simbolo2) {

        // No se usa en la práctica, se deja por compatibilidad
        return null;
    }

    /* =========================================================
       ACCESO VARIABLE
       ========================================================= */

    private Expresion construirAccesoVariable(GrammarZetarianoParser.AccesoVariableContext ctx) {

        List<TerminalNode> ids = ctx.ID();
        List<GrammarZetarianoParser.ExpresionContext> indices = ctx.expresion();

        int cursorId = 0;
        int cursorIndice = 0;

        Expresion actual;

        if (ctx.THIS() != null) {
            actual = new Identificador(linea(ctx), columna(ctx), "this");
        } else {
            String nombre = ids.get(cursorId++).getText();
            actual = new Identificador(linea(ctx), columna(ctx), nombre);
        }

        for (int i = 0; i < ctx.getChildCount(); i++) {

            String texto = ctx.getChild(i).getText();

            if (texto.equals(".") && cursorId < ids.size()) {
                actual = new AccesoAtributo(linea(ctx), columna(ctx), actual, ids.get(cursorId++).getText());
            } else if (texto.equals("[") && cursorIndice < indices.size()) {
                Expresion indice = (Expresion) visit(indices.get(cursorIndice++));
                actual = new AccesoArreglo(linea(ctx), columna(ctx), actual, List.of(indice));
            }
        }

        return actual;
    }

    private List<Expresion> construirArgumentos(GrammarZetarianoParser.ListaArgumentosContext ctx) {

        List<Expresion> argumentos = new ArrayList<>();
        if (ctx == null) return argumentos;

        for (var expresion : ctx.expresion()) {
            argumentos.add((Expresion) visit(expresion));
        }

        return argumentos;
    }

    /* =========================================================
       LITERALES
       ========================================================= */

    private Literal construirLiteral(GrammarZetarianoParser.LiteralContext ctx) {

        if (ctx.ENTERO() != null) {
            return new Literal(linea(ctx), columna(ctx), Integer.parseInt(ctx.getText()), "int");
        }
        if (ctx.DECIMAL() != null) {
            return new Literal(linea(ctx), columna(ctx), Double.parseDouble(ctx.getText()), "double");
        }
        if (ctx.TRUE() != null) {
            return new Literal(linea(ctx), columna(ctx), true, "boolean");
        }
        if (ctx.FALSE() != null) {
            return new Literal(linea(ctx), columna(ctx), false, "boolean");
        }
        if (ctx.NULL() != null) {
            return new Literal(linea(ctx), columna(ctx), null, "null");
        }
        if (ctx.COMILLASSIMPLES() != null) {
            return new Literal(linea(ctx), columna(ctx), ctx.getText(), "char");
        }

        return new Literal(linea(ctx), columna(ctx), ctx.getText(), "String");
    }

    /* =========================================================
       UTILIDADES
       ========================================================= */

    private int linea(ParserRuleContext ctx) {
        return ctx.getStart().getLine();
    }

    private int columna(ParserRuleContext ctx) {
        return ctx.getStart().getCharPositionInLine();
    }

    private int linea(TerminalNode nodo) {
        return nodo.getSymbol().getLine();
    }

    private int columna(TerminalNode nodo) {
        return nodo.getSymbol().getCharPositionInLine();
    }
}