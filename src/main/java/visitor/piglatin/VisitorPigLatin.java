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

/**
 * Construye el AST de Pig Latin (.pig) a partir del arbol de ANTLR.
 *
 * Pig Latin es el lenguaje de ARRANQUE: tiene imports, una seccion
 * opcional de variables globales (VARIABILES) y una seccion main
 * obligatoria (MAIOR).
 *
 * Igual que VisitorZetariano y VisitorPiton, este visitor NO hace
 * analisis semantico: no declara simbolos, no maneja ambitos, no valida
 * tipos. Las reglas propias de Pig Latin que pide el enunciado -- "ya no
 * se permite definir estructuras propias", "las funciones deben venir de
 * un .y" -- viven en DialectoPigLatin y las aplica AnalizadorPrograma en
 * la segunda pasada.
 *
 * DECISION DE MODELADO: el cuerpo del main se envuelve en una
 * DeclaracionFuncion llamada "main" con tipo de retorno void. Asi el main
 * entra por el mismo camino que cualquier funcion de Y? (mismo
 * AnalizadorFuncion, mismo generarC3D) en vez de necesitar un nodo AST y
 * un analizador propios.
 */
public class VisitorPigLatin extends GrammarPigLatinBaseVisitor<NodoAST> {

    private final AnalisisContexto analisisContexto;

    public VisitorPigLatin(AnalisisContexto contexto) {
        this.analisisContexto = contexto;
    }

    /* =========================================================
       ======================= PROGRAMA ========================
       ========================================================= */

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

        /*
         * Pig Latin no define estructuras ni clases propias (las importa),
         * por eso esas dos listas van vacias.
         */
        return new Programa(
                linea(ctx),
                columna(ctx),
                importaciones,
                new ArrayList<Estructura>(),
                new ArrayList<Clase>(),
                funciones,
                globales
        );
    }

    /**
     * "import carpeta.Objeto1.z" -> "carpeta.Objeto1.z"
     *
     * Se guarda la ruta tal cual: resolver el archivo y cargarlo es
     * trabajo de CompiladorProyecto, no del visitor.
     */
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

    /* =========================================================
       ===================== DECLARACIONES =====================
       ========================================================= */

    @Override
    public NodoAST visitDeclaracionVariable(GrammarPigLatinParser.DeclaracionVariableContext ctx) {

        Tipo tipo = construirTipo(ctx.tipo());
        String nombre = ctx.ID().getText();

        Expresion inicializacion = null;

        if (ctx.expresion() != null) {

            inicializacion = (Expresion) visit(ctx.expresion());

        } else if (ctx.inicializacionStruct() != null) {

            /*
             * esto ciudadano : Persona {"Valeria", 25};
             *
             * El nombre del tipo se guarda en el nodo para que el analisis
             * semantico pueda validar que los valores coinciden con los
             * campos de la estructura importada.
             */
            inicializacion = construirInicializacionStruct(
                    ctx.inicializacionStruct(), tipo.getNombre());
        }

        return new DeclaracionVariable(
                linea(ctx),
                columna(ctx),
                tipo,
                nombre,
                inicializacion
        );
    }

    @Override
    public NodoAST visitDeclaracionArreglo(GrammarPigLatinParser.DeclaracionArregloContext ctx) {

        Tipo tipoBase = construirTipo(ctx.tipo());

        Tipo tipoArreglo = new Tipo(
                linea(ctx),
                columna(ctx),
                tipoBase.getNombre(),
                true,
                1
        );

        String nombre = ctx.ID().getText();

        /*
         * series nombres[2] : textum {...};
         *
         * El tamano es un ENTERO literal en la gramatica (no una
         * expresion), asi que se envuelve en un Literal para que el resto
         * del pipeline lo trate igual que cualquier dimension.
         */
        List<Expresion> dimensiones = new ArrayList<>();

        dimensiones.add(new Literal(
                linea(ctx),
                columna(ctx),
                Integer.parseInt(ctx.ENTERO().getText()),
                "numerus"
        ));

        List<Expresion> valores = new ArrayList<>();

        if (ctx.listaValoresArreglo() != null) {

            for (var valor : ctx.listaValoresArreglo().valorArreglo()) {
                valores.add(construirValorArreglo(valor, tipoBase.getNombre()));
            }
        }

        return new DeclaracionArreglo(
                linea(ctx),
                columna(ctx),
                tipoArreglo,
                nombre,
                dimensiones,
                valores
        );
    }

    private Expresion construirValorArreglo(
            GrammarPigLatinParser.ValorArregloContext ctx, String nombreTipo) {

        if (ctx.inicializacionStruct() != null) {
            return construirInicializacionStruct(ctx.inicializacionStruct(), nombreTipo);
        }

        return (Expresion) visit(ctx.expresion());
    }

    /**
     * Estructuras anidadas: {"Valeria", 25, {"Avenida Central", 500}}
     *
     * El tipo del anidado no se conoce aca (depende del campo que ocupe),
     * asi que se deja en null: lo resuelve el analisis semantico cruzando
     * contra la definicion de la estructura en la TablaTipos.
     */
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

    /* =========================================================
       ======================== TIPOS ==========================
       ========================================================= */

    private Tipo construirTipo(GrammarPigLatinParser.TipoContext ctx) {

        if (ctx == null) {
            return null;
        }

        // tipo: tipoDato | ID   (ID = estructura u objeto importado)
        return new Tipo(linea(ctx), columna(ctx), ctx.getText(), false, 0);
    }

    /* =========================================================
       ====================== SENTENCIAS =======================
       ========================================================= */

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

        // Como SENTENCIA, prefijo o postfijo dan lo mismo: se descarta el valor.
        return new SentenciaExpresion(
                linea(ctx),
                columna(ctx),
                new ExpresionUnaria(linea(ctx), columna(ctx), operador, destino, false)
        );
    }

    @Override
    public NodoAST visitCondicional(GrammarPigLatinParser.CondicionalContext ctx) {

        /*
         * La gramatica produce:
         *
         *   si (e0) bloque0
         *   aliter (e1) bloque1     <- N ramas CON condicion
         *   aliter bloque2          <- rama final SIN condicion (opcional)
         *   finis;
         *
         * Hay una expresion por cada rama con condicion, y un bloque por
         * cada rama. Entonces: si hay mas bloques que expresiones, el
         * ultimo bloque es el 'else' suelto.
         */
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
                    null,
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

        // per (esto i : numerus 0; ...)
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

        // per (i = 0; ...)
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

        // i++ / i--
        if (ctx.INCREMENTO() != null || ctx.DECREMENTO() != null) {

            String operador = ctx.INCREMENTO() != null ? "++" : "--";

            return new ExpresionUnaria(linea(ctx), columna(ctx), operador, destino, false);
        }

        // i = i + 1
        return new Asignacion(
                linea(ctx),
                columna(ctx),
                destino,
                (Expresion) visit(ctx.expresion())
        );
    }

    /* =========================================================
       ================ ENTRADA / SALIDA =======================
       ========================================================= */

    @Override
    public NodoAST visitImprimir(GrammarPigLatinParser.ImprimirContext ctx) {

        /*
         *   >> "Hola" >> nombre;
         *
         * Imprime varias expresiones en una sola sentencia. Se modela como
         * UNA llamada a print con todos los argumentos: generarC3D de
         * LlamadaFuncion ya emite una cuarteta 'print' por argumento.
         */
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

        /*
         *   <<              -> lee y descarta
         *   mi_textum <<    -> lee y guarda
         */
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

    /**
     * perge (continue) e interrumpe (break) no tienen regla propia en la
     * gramatica: aparecen como alternativas sueltas dentro de 'sentencia',
     * asi que se detectan aca por el token.
     */
    @Override
    public NodoAST visitSentencia(GrammarPigLatinParser.SentenciaContext ctx) {

        if (ctx.PERGE() != null) {
            return new SentenciaContinue(linea(ctx), columna(ctx));
        }

        if (ctx.INTERRUMPE() != null) {
            return new SentenciaBreak(linea(ctx), columna(ctx));
        }

        /*
         * visitChildren() por defecto devuelve el resultado del ULTIMO
         * hijo, y 'sentencia' tiene un solo hijo, asi que sirve. Pero se
         * hace explicito para no depender de ese detalle.
         */
        return visit(ctx.getChild(0));
    }

    /* =========================================================
       ====================== EXPRESIONES ======================
       ========================================================= */

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

    /**
     * Para OR y AND, donde el operador es siempre el mismo.
     */
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

    /**
     * Para los niveles donde el operador varia (+ vs -, * vs / vs %...).
     *
     * El operador de cada paso se saca de los hijos del contexto: los
     * hijos en posicion impar son los tokens de operador, porque la regla
     * alterna operando / operador / operando.
     *
     * Se pliega a la IZQUIERDA para respetar la asociatividad: a - b - c
     * debe ser (a - b) - c, no a - (b - c).
     */
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

        // ID acceso* (++|--)?
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

    /* =========================================================
       ================== ACCESOS ENCADENADOS ==================
       ========================================================= */

    /**
     * Arma la cadena ID acceso* respetando el orden en que aparecen:
     *
     *   miObjeto.apellidos[0].getNombre()
     *
     *   -> LlamadaMetodo(
     *          AccesoArreglo(
     *              AccesoAtributo(Identificador(miObjeto), "apellidos"),
     *              0),
     *          "getNombre")
     *
     * La regla 'acceso' tiene tres formas: [expr] (indice), .ID (atributo)
     * y .ID(args) (metodo). Se distinguen por los tokens presentes.
     */
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

            int linea = linea(acceso);
            int columna = columna(acceso);

            // [ expresion ]
            if (acceso.CORCHETE_ABRE() != null) {

                List<Expresion> indices = new ArrayList<>();
                indices.add((Expresion) visit(acceso.expresion()));

                actual = new AccesoArreglo(linea, columna, actual, indices);
                continue;
            }

            // . ID ( args )   -> llamada a metodo
            if (acceso.PARENTESIS_ABRE() != null) {

                actual = new LlamadaMetodo(
                        linea,
                        columna,
                        actual,
                        acceso.ID().getText(),
                        construirArgumentos(acceso.listaArgumentos())
                );
                continue;
            }

            // . ID            -> atributo
            actual = new AccesoAtributo(linea, columna, actual, acceso.ID().getText());
        }

        return actual;
    }

    /* =========================================================
       ======================== APOYO ==========================
       ========================================================= */

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

        if (ctx.COMILLASSIMPLES() != null) {
            return new Literal(linea, columna, texto, "littera");
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