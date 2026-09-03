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
import enums.Categoria;
import enums.TipoDato;
import org.antlr.v4.runtime.ParserRuleContext;
import org.compi2.proyecto1compiladores2.GrammarZetarianoBaseVisitor;
import org.compi2.proyecto1compiladores2.GrammarZetarianoParser;
import semantico.AnalisisContexto;
import tablas.InformeTipo;

import java.util.ArrayList;
import java.util.List;

public class VisitorZetariano extends GrammarZetarianoBaseVisitor<NodoAST> {

    private final AnalisisContexto analisisContexto;
    private InformeTipo  claseActual;

    public VisitorZetariano(AnalisisContexto contexto) {
        this.analisisContexto = contexto;
    }


    /* ==================== CLASE ==================== */

    @Override
    public NodoAST visitCreacionClase(GrammarZetarianoParser.CreacionClaseContext ctx) {
        String nombreClase = ctx.ID().getText();

        if (analisisContexto.getTablaTipos().existeTipo(nombreClase)) {
            analisisContexto.reportarError(linea(ctx), columna(ctx),"El tipo '" + nombreClase + "' ya está definido");
        }

        // Registro temprano: aunque todavía no tiene atributos/métodos, ya existe para
        // que referencias a la propia clase (recursividad) la encuentren.
        InformeTipo infoClase = new InformeTipo(nombreClase, TipoDato.OBJETO);
        analisisContexto.getTablaTipos().registrar(infoClase);

        InformeTipo claseAnterior = this.claseActual;
        this.claseActual = infoClase;

        analisisContexto.getTablaSimbolos().entrarAmbito("Clase " + nombreClase);

        List<Atributo> atributos = new ArrayList<>();
        List<Constructor> constructores = new ArrayList<>();
        List<Metodo> metodos = new ArrayList<>();

        for (var contenido : ctx.contenidoClase()) {
            if (contenido instanceof GrammarZetarianoParser.ContenidoAtributoContext a) {
                atributos.add((Atributo) visit(a.atributo()));
            } else if (contenido instanceof GrammarZetarianoParser.ContenidoConstructorContext c) {
                constructores.add((Constructor) visit(c.constructor()));
            } else if (contenido instanceof GrammarZetarianoParser.ContenidoMetodoContext m) {
                metodos.add((Metodo) visit(m.metodo()));
            }
        }

        analisisContexto.getTablaSimbolos().salirAmbito();
        this.claseActual = claseAnterior;

        return new Clase(linea(ctx), columna(ctx), nombreClase, atributos, constructores, metodos);
    }

    @Override
    public NodoAST visitAtributo(GrammarZetarianoParser.AtributoContext ctx) {
        Tipo tipo = construirTipo(ctx.tipo(), ctx.dimension());
        String nombre = ctx.ID().getText();

        if (analisisContexto.getTablaSimbolos().existeEnAmbitoActual(nombre)) {
            analisisContexto.reportarError(linea(ctx), columna(ctx),"El atributo '" + nombre + "' ya fue declarado en esta clase");
        } else {
            analisisContexto.getTablaSimbolos().declarar(nombre, Categoria.ATRIBUTO, tipo.getNombre(), "", linea(ctx));
        }

        if (claseActual != null) {
            claseActual.agregarAtributo(nombre, tipo);
        }

        return new Atributo(linea(ctx), columna(ctx), tipo, nombre);
    }

    @Override
    public NodoAST visitConstructor(GrammarZetarianoParser.ConstructorContext ctx) {
        String nombreConstructor = ctx.ID().getText();

        if (claseActual != null && !nombreConstructor.equals(claseActual.getNombre())) {
            analisisContexto.reportarError(linea(ctx), columna(ctx),"El nombre del constructor debe coincidir con el de la clase");
        }

        List<Parametro> parametros = construirParametros(ctx.listaParametros());

        analisisContexto.getTablaSimbolos().entrarAmbito("Constructor " + nombreConstructor);
        declararParametros(parametros);

        Bloque cuerpo = (Bloque) visit(ctx.bloque());

        analisisContexto.getTablaSimbolos().salirAmbito();

        return new Constructor(linea(ctx), columna(ctx), nombreConstructor, parametros, cuerpo);
    }

    @Override
    public NodoAST visitMetodo(GrammarZetarianoParser.MetodoContext ctx) {
        String nombreMetodo = ctx.ID().getText();

        Tipo tipoRetorno = ctx.VOID() != null
                ? new Tipo(linea(ctx), columna(ctx), "void", false, 0)
                : construirTipo(ctx.tipo(), null);

        List<Parametro> parametros = construirParametros(ctx.listaParametros());

        // Se declara ANTES de visitar el cuerpo: permite recursividad (metodo() llamándose a sí mismo)
        if (analisisContexto.getTablaSimbolos().existeEnAmbitoActual(nombreMetodo)) {
            analisisContexto.reportarError(linea(ctx), columna(ctx),"El método '" + nombreMetodo + "' ya fue declarado en esta clase");
        } else {
            analisisContexto.getTablaSimbolos().declarar(
                    nombreMetodo, Categoria.METODO, tipoRetorno.getNombre(),
                    parametros.size() + " parámetro(s)", linea(ctx)
            );
        }

        if (claseActual != null) {
            claseActual.agregarMetodo(new tablas.Metodo(nombreMetodo, tipoRetorno, parametros));
        }

        analisisContexto.getTablaSimbolos().entrarAmbito("Metodo " + nombreMetodo);
        declararParametros(parametros);

        Bloque cuerpo = (Bloque) visit(ctx.bloque());

        analisisContexto.getTablaSimbolos().salirAmbito();

        return new Metodo(linea(ctx), columna(ctx), nombreMetodo, tipoRetorno, parametros, cuerpo);
    }

    private void declararParametros(List<Parametro> parametros) {
        for (Parametro p : parametros) {
            if (analisisContexto.getTablaSimbolos().existeEnAmbitoActual(p.getNombreParametro())) {
                analisisContexto.reportarError(p.getLinea(), p.getColumna(),"Parámetro '" + p.getNombreParametro() + "' duplicado");
                continue;
            }
            analisisContexto.getTablaSimbolos().declarar(
                    p.getNombreParametro(), Categoria.PARAMETRO, p.getTipoParametro().getNombre(), "", p.getLinea()
            );
        }
    }

    private List<Parametro> construirParametros(GrammarZetarianoParser.ListaParametrosContext ctx) {
        List<Parametro> resultado = new ArrayList<>();
        if (ctx == null) return resultado;

        for (var p : ctx.parametro()) {
            Tipo tipo = construirTipo(p.tipo(), p.dimension());
            resultado.add(new Parametro(linea(p), columna(p), tipo, p.ID().getText(), false, p.dimension() != null));
        }
        return resultado;
    }

    private Tipo construirTipo(GrammarZetarianoParser.TipoContext ctx, GrammarZetarianoParser.DimensionContext dim) {
        return new Tipo(linea(ctx), columna(ctx), ctx.getText(), dim != null, dim != null ? dim.expresion().size() : 0);
    }

    /* ==================== BLOQUES Y SENTENCIAS ==================== */

    @Override
    public NodoAST visitBloque(
            GrammarZetarianoParser.BloqueContext ctx) {

        analisisContexto.getTablaSimbolos()
                .entrarAmbito("bloque");

        List<Sentencia> sentencias = new ArrayList<>();

        for (var s : ctx.sentencia()) {
            sentencias.add((Sentencia) visit(s));
        }

        analisisContexto.getTablaSimbolos()
                .salirAmbito();

        return new Bloque(
                linea(ctx),
                columna(ctx),
                sentencias
        );
    }

    @Override
    public NodoAST visitDeclaracionVariable(GrammarZetarianoParser.DeclaracionVariableContext ctx) {
        Tipo tipo = construirTipo(ctx.tipo(), ctx.dimension());
        String nombre = ctx.ID().getText();

        if (analisisContexto.getTablaSimbolos().existeEnAmbitoActual(nombre)) {
            analisisContexto.reportarError(linea(ctx), columna(ctx),"'" + nombre + "' ya fue declarado en este ámbito");
        } else {
            analisisContexto.getTablaSimbolos().declarar(nombre, Categoria.VARIABLE, tipo.getNombre(), "", linea(ctx));
        }

        if (ctx.dimension() != null) {
            List<Expresion> dimensiones = new ArrayList<>();
            for (var dimExpr : ctx.dimension().expresion()) {
                dimensiones.add((Expresion) visit(dimExpr));
            }
            List<Expresion> valoresIniciales = ctx.listaValores() != null
                    ? ctx.listaValores().expresion().stream().map(e -> (Expresion) visit(e)).toList()
                    : List.of();
            return new DeclaracionArreglo(linea(ctx), columna(ctx), tipo, nombre, dimensiones, valoresIniciales);
        }

        Expresion inicializacion = ctx.expresion() != null ? (Expresion) visit(ctx.expresion()) : null;
        return new DeclaracionVariable(linea(ctx), columna(ctx), tipo, nombre, inicializacion);
    }

    @Override
    public NodoAST visitAsignacion(GrammarZetarianoParser.AsignacionContext ctx) {
        Expresion destino = construirAccesoVariable(ctx.accesoVariable());
        Expresion valor = (Expresion) visit(ctx.expresion());

        if (ctx.op.getText().equals("=")) {
            return new Asignacion(linea(ctx), columna(ctx), destino, valor);
        }

        // a += b  =>  a = a + b   (misma idea para -= y *=)
        String operador = ctx.op.getText().substring(0, 1);
        Expresion combinada = new ExpresionBinaria(linea(ctx), columna(ctx), destino, operador, valor);
        return new Asignacion(linea(ctx), columna(ctx), destino, combinada);
    }

    @Override
    public NodoAST visitImprimirStmt(
            GrammarZetarianoParser.ImprimirStmtContext ctx) {

        String nombreFuncion =
                ctx.PRINTLN() != null ? "println" : "print";

        List<Expresion> argumentos = new ArrayList<>();

        if (ctx.expresion() != null) {
            argumentos.add((Expresion) visit(ctx.expresion()));
        }

        LlamadaFuncion llamada = new LlamadaFuncion(
                linea(ctx),
                columna(ctx),
                nombreFuncion,
                argumentos
        );

        return new SentenciaExpresion(
                linea(ctx),
                columna(ctx),
                llamada
        );
    }

    @Override
    public NodoAST visitSentIncrDecrPostfijo(GrammarZetarianoParser.SentIncrDecrPostfijoContext ctx) {
        Expresion destino = ctx.accesoVariable() != null
                ? construirAccesoVariable(ctx.accesoVariable())
                : new Identificador(linea(ctx), columna(ctx), ctx.ID().getText());

        String operador = ctx.INCREMENTO() != null ? "++" : "--";
        ExpresionUnaria incremento = new ExpresionUnaria(linea(ctx), columna(ctx), operador, destino, false);
        return new SentenciaExpresion(linea(ctx), columna(ctx), incremento);
    }

    @Override
    public NodoAST visitSentIncrDecrPrefijo(GrammarZetarianoParser.SentIncrDecrPrefijoContext ctx) {
        Expresion destino = construirAccesoVariable(ctx.accesoVariable());
        String operador = ctx.INCREMENTO() != null ? "++" : "--";
        ExpresionUnaria incremento = new ExpresionUnaria(linea(ctx), columna(ctx), operador, destino, true);
        return new SentenciaExpresion(linea(ctx), columna(ctx), incremento);
    }

    // ---- alternativas de "sentencia" que solo delegan a su subregla ----
    @Override public NodoAST visitSentDeclaracionVariable(GrammarZetarianoParser.SentDeclaracionVariableContext ctx) { return visit(ctx.declaracionVariable()); }
    @Override public NodoAST visitSentAsignacion(GrammarZetarianoParser.SentAsignacionContext ctx) { return visit(ctx.asignacion()); }
    @Override public NodoAST visitSentImprimir(GrammarZetarianoParser.SentImprimirContext ctx) { return visit(ctx.imprimirStmt()); }
    @Override public NodoAST visitSentCondicional(GrammarZetarianoParser.SentCondicionalContext ctx) { return visit(ctx.condicional()); }
    @Override public NodoAST visitSentCicloFor(GrammarZetarianoParser.SentCicloForContext ctx) { return visit(ctx.cicloFor()); }
    @Override public NodoAST visitSentCicloWhile(GrammarZetarianoParser.SentCicloWhileContext ctx) { return visit(ctx.cicloWhile()); }
    @Override public NodoAST visitSentCicloDoWhile(GrammarZetarianoParser.SentCicloDoWhileContext ctx) { return visit(ctx.cicloDoWhile()); }
    @Override public NodoAST visitSentSwitch(GrammarZetarianoParser.SentSwitchContext ctx) { return visit(ctx.switchCase()); }
    @Override public NodoAST visitSentBloqueAnidado(GrammarZetarianoParser.SentBloqueAnidadoContext ctx) { return visit(ctx.bloque()); }

    @Override
    public NodoAST visitSentLlamadaFuncion(GrammarZetarianoParser.SentLlamadaFuncionContext ctx) {
        return new SentenciaExpresion(linea(ctx), columna(ctx), (Expresion) visit(ctx.llamadaFuncion()));
    }

    @Override
    public NodoAST visitSentLlamadaMetodo(GrammarZetarianoParser.SentLlamadaMetodoContext ctx) {
        return new SentenciaExpresion(linea(ctx), columna(ctx), (Expresion) visit(ctx.llamadaMetodo()));
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

    /* ==================== CONDICIONALES ==================== */

    @Override
    public NodoAST visitCondicional(GrammarZetarianoParser.CondicionalContext ctx) {
        Expresion condicion = (Expresion) visit(ctx.expresion(0));
        Bloque bloqueEntonces = (Bloque) visit(ctx.bloque(0));

        boolean tieneElseFinal = ctx.bloque().size() > ctx.expresion().size();
        int totalElseIf = ctx.bloque().size() - 1 - (tieneElseFinal ? 1 : 0);

        List<CondicionIf> listaSiNoSi = new ArrayList<>();
        for (int i = 1; i <= totalElseIf; i++) {
            Expresion condicionElseIf = (Expresion) visit(ctx.expresion(i));
            Bloque bloqueElseIf = (Bloque) visit(ctx.bloque(i));
            listaSiNoSi.add(new CondicionIf(linea(ctx), columna(ctx), condicionElseIf, bloqueElseIf, List.of(), null));
        }

        Bloque bloqueSiNo = tieneElseFinal ? (Bloque) visit(ctx.bloque(ctx.bloque().size() - 1)) : null;

        return new CondicionIf(linea(ctx), columna(ctx), condicion, bloqueEntonces, listaSiNoSi, bloqueSiNo);
    }

    /* ==================== CICLOS ==================== */

    @Override
    public NodoAST visitCicloWhile(GrammarZetarianoParser.CicloWhileContext ctx) {
        Expresion condicion = (Expresion) visit(ctx.expresion());

        analisisContexto.entrarCiclo();
        Bloque cuerpo = (Bloque) visit(ctx.bloque());
        analisisContexto.salirCiclo();

        return new CicloWhile(linea(ctx), columna(ctx), condicion, cuerpo);
    }

    @Override
    public NodoAST visitCicloDoWhile(GrammarZetarianoParser.CicloDoWhileContext ctx) {
        analisisContexto.entrarCiclo();
        Bloque cuerpo = (Bloque) visit(ctx.bloque());
        analisisContexto.salirCiclo();

        Expresion condicion = (Expresion) visit(ctx.expresion());
        return new CicloDoWhile(linea(ctx), columna(ctx), cuerpo, condicion);
    }

    @Override
    public NodoAST visitCicloFor(GrammarZetarianoParser.CicloForContext ctx) {
        analisisContexto.getTablaSimbolos().entrarAmbito("for"); // envuelve init + condición + actualización + cuerpo

        Sentencia inicializacion = null;
        if (ctx.declaracionVariable() != null) {
            inicializacion = (Sentencia) visit(ctx.declaracionVariable());
        } else if (!ctx.asignacion().isEmpty()) {
            inicializacion = (Sentencia) visit(ctx.asignacion(0));
        }

        Expresion condicion = ctx.expresion() != null ? (Expresion) visit(ctx.expresion()) : null;

        // TODO: si la actualización usa "i = i + 1" (asignacion), CicloFor.incremento (tipado Expresion)
        // no la puede recibir tal cual — de momento solo se soporta i++ / --i aquí.
        Expresion incremento = null;

        analisisContexto.entrarCiclo();
        Bloque cuerpo = (Bloque) visit(ctx.bloque());
        analisisContexto.salirCiclo();

        analisisContexto.getTablaSimbolos().salirAmbito();

        return new CicloFor(linea(ctx), columna(ctx), inicializacion, condicion, incremento, cuerpo);
    }

    /* ==================== SWITCH ==================== */

    @Override
    public NodoAST visitSwitchCase(GrammarZetarianoParser.SwitchCaseContext ctx) {
        Expresion expresionSwitch = (Expresion) visit(ctx.expresion());

        analisisContexto.entrarSwitch();

        List<SentenciaCase> casos = new ArrayList<>();
        for (var c : ctx.casoSwitch()) {
            casos.add((SentenciaCase) visit(c));
        }

        Bloque bloqueDefecto = ctx.bloqueCaso() != null ? (Bloque) visit(ctx.bloqueCaso()) : null;

        analisisContexto.salirSwitch();

        return new CondicionSwitch(linea(ctx), columna(ctx), expresionSwitch, casos, bloqueDefecto);
    }

    @Override
    public NodoAST visitCasoSwitch(GrammarZetarianoParser.CasoSwitchContext ctx) {
        Expresion valor = construirLiteral(ctx.literal());
        Bloque cuerpo = (Bloque) visit(ctx.bloqueCaso());
        return new SentenciaCase(linea(ctx), columna(ctx), valor, cuerpo);
    }

    @Override
    public NodoAST visitBloqueCaso(GrammarZetarianoParser.BloqueCasoContext ctx) {
        analisisContexto.getTablaSimbolos().entrarAmbito("case");

        List<Sentencia> sentencias = new ArrayList<>();
        for (var s : ctx.sentencia()) {
            sentencias.add((Sentencia) visit(s));
        }

        analisisContexto.getTablaSimbolos().salirAmbito();
        return new Bloque(linea(ctx), columna(ctx), sentencias);
    }

    /* ==================== EXPRESIONES ==================== */

    @Override public NodoAST visitExpParentesis(GrammarZetarianoParser.ExpParentesisContext ctx) { return visit(ctx.expresion()); }

    @Override
    public NodoAST visitExpNegacionLogica(GrammarZetarianoParser.ExpNegacionLogicaContext ctx) {
        return new ExpresionUnaria(linea(ctx), columna(ctx), "!", (Expresion) visit(ctx.expresion()), true);
    }

    @Override
    public NodoAST visitExpNegativo(GrammarZetarianoParser.ExpNegativoContext ctx) {
        return new ExpresionUnaria(linea(ctx), columna(ctx), "-", (Expresion) visit(ctx.expresion()), true);
    }

    @Override
    public NodoAST visitExpPreIncrDecr(GrammarZetarianoParser.ExpPreIncrDecrContext ctx) {
        Expresion operando = construirAccesoVariable(ctx.accesoVariable());
        String operador = ctx.INCREMENTO() != null ? "++" : "--";
        return new ExpresionUnaria(linea(ctx), columna(ctx), operador, operando, true);
    }

    @Override
    public NodoAST visitExpPostIncrDecr(GrammarZetarianoParser.ExpPostIncrDecrContext ctx) {
        Expresion operando = construirAccesoVariable(ctx.accesoVariable());
        String operador = ctx.INCREMENTO() != null ? "++" : "--";
        return new ExpresionUnaria(linea(ctx), columna(ctx), operador, operando, false);
    }

    @Override
    public NodoAST visitExpCrearObjeto(GrammarZetarianoParser.ExpCrearObjetoContext ctx) {
        String nombreClase = ctx.ID().getText();

        if (!analisisContexto.getTablaTipos().existeTipo(nombreClase)) {
            analisisContexto.reportarError(linea(ctx), columna(ctx),"La clase '" + nombreClase + "' no está definida");
        }

        List<Expresion> argumentos = new ArrayList<>();
        if (ctx.listaArgumentos() != null) {
            for (var e : ctx.listaArgumentos().expresion()) {
                argumentos.add((Expresion) visit(e));
            }
        }
        return new CrearObjeto(linea(ctx), columna(ctx), nombreClase, argumentos);
    }

    @Override public NodoAST visitExpMultiplicativa(GrammarZetarianoParser.ExpMultiplicativaContext ctx) { return binaria(ctx, ctx.expresion(0), ctx.op.getText(), ctx.expresion(1)); }
    @Override public NodoAST visitExpAditiva(GrammarZetarianoParser.ExpAditivaContext ctx) { return binaria(ctx, ctx.expresion(0), ctx.op.getText(), ctx.expresion(1)); }
    @Override public NodoAST visitExpRelacional(GrammarZetarianoParser.ExpRelacionalContext ctx) { return binaria(ctx, ctx.expresion(0), ctx.op.getText(), ctx.expresion(1)); }
    @Override public NodoAST visitExpIgualdad(GrammarZetarianoParser.ExpIgualdadContext ctx) { return binaria(ctx, ctx.expresion(0), ctx.op.getText(), ctx.expresion(1)); }
    @Override public NodoAST visitExpAnd(GrammarZetarianoParser.ExpAndContext ctx) { return binaria(ctx, ctx.expresion(0), "&&", ctx.expresion(1)); }
    @Override public NodoAST visitExpOr(GrammarZetarianoParser.ExpOrContext ctx) { return binaria(ctx, ctx.expresion(0), "||", ctx.expresion(1)); }

    private NodoAST binaria(ParserRuleContext ctx, GrammarZetarianoParser.ExpresionContext izq, String op, GrammarZetarianoParser.ExpresionContext der) {
        return new ExpresionBinaria(linea(ctx), columna(ctx), (Expresion) visit(izq), op, (Expresion) visit(der));
    }

    @Override
    public NodoAST visitExpTernaria(GrammarZetarianoParser.ExpTernariaContext ctx) {
        return new ExpresionTernaria(linea(ctx), columna(ctx),
                (Expresion) visit(ctx.expresion(0)), (Expresion) visit(ctx.expresion(1)), (Expresion) visit(ctx.expresion(2)));
    }

    @Override public NodoAST visitExpReadln(GrammarZetarianoParser.ExpReadlnContext ctx) { return new LlamadaFuncion(linea(ctx), columna(ctx), "readln", List.of()); }
    @Override public NodoAST visitExpLlamadaMetodo(GrammarZetarianoParser.ExpLlamadaMetodoContext ctx) { return visit(ctx.llamadaMetodo()); }
    @Override public NodoAST visitExpLlamadaFuncion(GrammarZetarianoParser.ExpLlamadaFuncionContext ctx) { return visit(ctx.llamadaFuncion()); }
    @Override public NodoAST visitExpAcceso(GrammarZetarianoParser.ExpAccesoContext ctx) { return construirAccesoVariable(ctx.accesoVariable()); }
    @Override public NodoAST visitExpLiteral(GrammarZetarianoParser.ExpLiteralContext ctx) { return construirLiteral(ctx.literal()); }
    @Override public NodoAST visitExpThis(GrammarZetarianoParser.ExpThisContext ctx) { return new Identificador(linea(ctx), columna(ctx), "this"); }

    @Override
    public NodoAST visitLlamadaFuncion(GrammarZetarianoParser.LlamadaFuncionContext ctx) {
        String nombre = ctx.ID().getText();
        if (analisisContexto.getTablaSimbolos().buscar(nombre) == null) {
            analisisContexto.reportarError(linea(ctx), columna(ctx),"Método '" + nombre + "' no declarado en la clase");
        }
        List<Expresion> argumentos = new ArrayList<>();
        if (ctx.listaArgumentos() != null) {
            for (var e : ctx.listaArgumentos().expresion()) {
                argumentos.add((Expresion) visit(e));
            }
        }
        return new LlamadaFuncion(linea(ctx), columna(ctx), nombre, argumentos);
    }

    @Override
    public NodoAST visitLlamadaMetodo(GrammarZetarianoParser.LlamadaMetodoContext ctx) {
        List<org.antlr.v4.runtime.tree.TerminalNode> ids = ctx.ID();
        int totalIds = ids.size();

        Expresion objeto;
        int siguienteId;

        if (ctx.THIS() != null) {
            objeto = new Identificador(linea(ctx), columna(ctx), "this");
            siguienteId = 0;
        } else {
            String nombreBase = ids.get(0).getText();
            if (analisisContexto.getTablaSimbolos().buscar(nombreBase) == null) {
                analisisContexto.reportarError(linea(ctx), columna(ctx),"Variable '" + nombreBase + "' no declarada");
            }
            objeto = new Identificador(linea(ctx), columna(ctx), nombreBase);
            siguienteId = 1;
        }

        while (siguienteId < totalIds - 1) {
            objeto = new AccesoAtributo(linea(ctx), columna(ctx), objeto, ids.get(siguienteId).getText());
            siguienteId++;
        }

        String nombreMetodo = ids.get(totalIds - 1).getText();

        List<Expresion> argumentos = new ArrayList<>();
        if (ctx.listaArgumentos() != null) {
            for (var e : ctx.listaArgumentos().expresion()) {
                argumentos.add((Expresion) visit(e));
            }
        }
        return new LlamadaMetodo(linea(ctx), columna(ctx), objeto, nombreMetodo, argumentos);
    }

    /** (THIS.)?ID(.ID | [expr])*  →  encadena Identificador/AccesoAtributo/AccesoArreglo en orden real del árbol */
    private Expresion construirAccesoVariable(GrammarZetarianoParser.AccesoVariableContext ctx) {
        List<org.antlr.v4.runtime.tree.TerminalNode> ids = ctx.ID();
        List<GrammarZetarianoParser.ExpresionContext> indices = ctx.expresion();

        int cursorId = 0;
        int cursorIndice = 0;
        Expresion actual;

        if (ctx.THIS() != null) {
            actual = new Identificador(linea(ctx), columna(ctx), "this");
        } else {
            String nombre = ids.get(cursorId++).getText();
            if (analisisContexto.getTablaSimbolos().buscar(nombre) == null) {
                analisisContexto.reportarError(linea(ctx), columna(ctx),"Variable '" + nombre + "' no declarada");
            }
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

    private Literal construirLiteral(GrammarZetarianoParser.LiteralContext ctx) {
        if (ctx.ENTERO() != null) return new Literal(linea(ctx), columna(ctx), Integer.parseInt(ctx.getText()), "int");
        if (ctx.DECIMAL() != null) return new Literal(linea(ctx), columna(ctx), Double.parseDouble(ctx.getText()), "double");
        if (ctx.TRUE() != null || ctx.FALSE() != null) return new Literal(linea(ctx), columna(ctx), ctx.TRUE() != null, "boolean");
        if (ctx.COMILLASSIMPLES() != null) return new Literal(linea(ctx), columna(ctx), ctx.getText(), "char");
        return new Literal(linea(ctx), columna(ctx), ctx.getText(), "String");
    }

    private int linea(ParserRuleContext ctx) { return ctx.getStart().getLine(); }
    private int columna(ParserRuleContext ctx) { return ctx.getStart().getCharPositionInLine(); }
}
