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
import java.util.Collections;
import java.util.List;

/**
 * Construye el AST de Zetariano a partir del árbol de ANTLR.
 *
 * IMPORTANTE: este visitor NO hace análisis semántico. No declara símbolos,
 * no maneja ámbitos, no valida tipos ni reporta errores semánticos — solo
 * arma los nodos NodoAST con lo que el parser reconoció. Todo el análisis
 * semántico (redeclaraciones, tipos, resolución de nombres, etc.) corre
 * después, como segunda pasada, en semantico.coordinadorsemantico.
 * AnalizadorSemanticoCoordinador (ver CompiladorArchivo), delegando en las
 * clases de semantico.analizadores / semantico.coordinadorsemantico.
 *
 * El campo analisisContexto se conserva solo por si en el futuro se
 * necesita reportar un error puramente sintáctico durante la construcción
 * del árbol (por ejemplo una combinación de tokens que el parser aceptó
 * pero que no tiene sentido armar como nodo). Hoy no se usa para nada
 * relacionado con símbolos, ámbitos o tipos.
 */
public class VisitorZetariano extends GrammarZetarianoBaseVisitor<NodoAST> {

    private final AnalisisContexto analisisContexto;

    public VisitorZetariano(AnalisisContexto contexto) {
        this.analisisContexto = contexto;
    }

    @Override
    public NodoAST visitCreacionClase(GrammarZetarianoParser.CreacionClaseContext ctx) {

        String nombreClase = ctx.ID().getText();

        /*
         * El visitor SOLO construye el árbol. Registrar el tipo, detectar
         * redefiniciones, manejar ámbitos de la tabla de símbolos y
         * arrastrar la "clase actual" es responsabilidad semántica: eso
         * ahora vive en semantico.analizadores.AnalizadorClase, que corre
         * como segunda pasada sobre este mismo árbol.
         */
        List<Atributo> atributos = new ArrayList<>();
        List<Constructor> constructores = new ArrayList<>();
        List<Metodo> metodos = new ArrayList<>();


        /*
         * Analizar contenido de la clase.
         */
        for (var contenido : ctx.contenidoClase()) {

            if (contenido instanceof
                    GrammarZetarianoParser.ContenidoAtributoContext a) {

                NodoAST nodo = visit(a.atributo());

                if (nodo instanceof Atributo atributo) {
                    atributos.add(atributo);
                }

            } else if (contenido instanceof
                    GrammarZetarianoParser.ContenidoConstructorContext c) {

                NodoAST nodo = visit(c.constructor());

                if (nodo instanceof Constructor constructor) {
                    constructores.add(constructor);
                }

            } else if (contenido instanceof
                    GrammarZetarianoParser.ContenidoMetodoContext m) {

                NodoAST nodo = visit(m.metodo());

                if (nodo instanceof Metodo metodo) {
                    metodos.add(metodo);
                }
            }
        }


        return new Clase(
                linea(ctx),
                columna(ctx),
                nombreClase,
                atributos,
                constructores,
                metodos
        );
    }


    /* =========================================================
       ======================= ATRIBUTOS ======================
       ========================================================= */

    @Override
    public NodoAST visitAtributo(
            GrammarZetarianoParser.AtributoContext ctx) {

        Tipo tipo = construirTipo(ctx.tipo());

        String nombre = ctx.ID().getText();

        /*
         * Duplicados y registro en la tabla de tipos: responsabilidad de
         * semantico.analizadores.AnalizadorAtributo.
         */
        Atributo atributo = new Atributo(
                linea(ctx),
                columna(ctx),
                tipo,
                nombre
        );


        /*
         * FIX: el atributo ahora admite inicialización, igual que
         * declaracionVariable:
         *
         * private int contador = 0;
         * private int[] numeros = {1, 2, 3};
         * private int[3][3] matriz = new int[3][3];
         */
        if (esTipoArreglo(ctx.tipo())) {

            List<Expresion> valoresIniciales = new ArrayList<>();

            if (ctx.listaValores() != null) {

                for (var elemento : ctx.listaValores().elementoLista()) {

                    valoresIniciales.add(
                            (Expresion) visit(elemento)
                    );
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

            atributo.setInicializacion(
                    (Expresion) visit(ctx.expresion())
            );
        }


        return atributo;
    }


    /* =========================================================
       ===================== CONSTRUCTOR ======================
       ========================================================= */

    @Override
    public NodoAST visitConstructor(
            GrammarZetarianoParser.ConstructorContext ctx) {

        String nombreConstructor = ctx.ID().getText();

        /*
         * La regla "el constructor debe llamarse igual que la clase" ahora
         * se valida en semantico.analizadores.AnalizadorConstructor, que
         * tiene acceso a la clase actual vía AnalisisContexto.
         */
        List<Parametro> parametros =
                construirParametros(ctx.listaParametros());

        /*
         * Declarar los parámetros en la tabla de símbolos y manejar el
         * ámbito del constructor también se movió a AnalizadorConstructor
         * (entra/sale de ámbito antes y después de analizar el cuerpo).
         */
        Bloque cuerpo =
                (Bloque) visit(ctx.bloque());


        return new Constructor(
                linea(ctx),
                columna(ctx),
                nombreConstructor,
                parametros,
                cuerpo
        );
    }


    /* =========================================================
       ======================== METODOS =======================
       ========================================================= */

    @Override
    public NodoAST visitMetodo(
            GrammarZetarianoParser.MetodoContext ctx) {

        String nombreMetodo = ctx.ID().getText();


        Tipo tipoRetorno;

        if (ctx.VOID() != null) {

            tipoRetorno =
                    new Tipo(
                            linea(ctx),
                            columna(ctx),
                            "void",
                            false,
                            0
                    );

        } else {

            tipoRetorno =
                    construirTipo(ctx.tipo());
        }


        List<Parametro> parametros =
                construirParametros(ctx.listaParametros());

        /*
         * Duplicados dentro de la clase, registrar la firma para
         * sobrecarga/recursividad, ámbito de parámetros y del cuerpo:
         * todo eso vive ahora en AnalizadorClase (firma) y
         * AnalizadorMetodo (ámbito + parámetros), corriendo sobre este
         * mismo nodo Metodo en la segunda pasada.
         */
        Bloque cuerpo =
                (Bloque) visit(ctx.bloque());


        return new Metodo(
                linea(ctx),
                columna(ctx),
                nombreMetodo,
                tipoRetorno,
                parametros,
                cuerpo
        );
    }


    /* =========================================================
       ======================= PARAMETROS ======================
       ========================================================= */

    private List<Parametro> construirParametros(
            GrammarZetarianoParser.ListaParametrosContext ctx) {

        List<Parametro> resultado = new ArrayList<>();


        if (ctx == null) {
            return resultado;
        }


        for (var p : ctx.parametro()) {

            Tipo tipo = construirTipo(p.tipo());

            boolean esArreglo = esTipoArreglo(p.tipo());

            int dimensiones = cantidadDimensiones(p.tipo());


            resultado.add(
                    new Parametro(
                            linea(p),
                            columna(p),
                            tipo,
                            p.ID().getText(),
                            false,
                            esArreglo
                    )
            );
        }


        return resultado;
    }


    /*
     * declararParametros() se eliminó: la declaración de parámetros en la
     * tabla de símbolos (con su chequeo de duplicados) ahora la hacen
     * AnalizadorMetodo y AnalizadorConstructor sobre el nodo ya construido.
     */


    /* =========================================================
       ========================= TIPOS ========================
       ========================================================= */

    private Tipo construirTipo(
            GrammarZetarianoParser.TipoContext ctx) {

        if (ctx == null) {
            return null;
        }

        // Obtener el nombre del tipo base (int, double, String, etc.)
        String tipoBase = ctx.tipoBase().getText();

        boolean esArreglo =
                !ctx.CORCHETE_ABRE().isEmpty();

        int dimensiones =
                ctx.CORCHETE_ABRE().size();

        // Construir el nombre completo del tipo (ej: "int[]", "String[][]")
        StringBuilder nombreCompleto = new StringBuilder(tipoBase);
        for (int i = 0; i < dimensiones; i++) {
            nombreCompleto.append("[]");
        }

        return new Tipo(
                linea(ctx),
                columna(ctx),
                nombreCompleto.toString(),
                esArreglo,
                dimensiones
        );
    }


    private boolean esTipoArreglo(
            GrammarZetarianoParser.TipoContext ctx) {

        return ctx != null
                && !ctx.CORCHETE_ABRE().isEmpty();
    }


    private int cantidadDimensiones(
            GrammarZetarianoParser.TipoContext ctx) {

        if (ctx == null) {
            return 0;
        }

        return ctx.CORCHETE_ABRE().size();
    }


    /* =========================================================
       ======================== BLOQUES =======================
       ========================================================= */

    @Override
    public NodoAST visitBloque(
            GrammarZetarianoParser.BloqueContext ctx) {

        // Ámbito del bloque: lo maneja AnalizadorBloque en la 2da pasada.
        List<Sentencia> sentencias =
                new ArrayList<>();


        for (var s : ctx.sentencia()) {

            NodoAST nodo = visit(s);

            if (nodo instanceof Sentencia sentencia) {
                sentencias.add(sentencia);
            }
        }


        return new Bloque(
                linea(ctx),
                columna(ctx),
                sentencias
        );
    }


    /* =========================================================
       ================= DECLARACIONES VARIABLES ==============
       ========================================================= */

    @Override
    public NodoAST visitDeclaracionVariable(
            GrammarZetarianoParser.DeclaracionVariableContext ctx) {

        Tipo tipo =
                construirTipo(ctx.tipo());

        String nombre =
                ctx.ID().getText();

        // Duplicados y registro en tabla de símbolos: AnalizadorDeclaracionVariable / AnalizadorDeclaracionArreglo.

        /*
         * Si es un arreglo:
         *
         * int[] numeros = {1,2,3};
         * int[5] numeros = new int[5];
         * int[3][3] matriz = new int[3][3];
         */
        if (esTipoArreglo(ctx.tipo())) {

            List<Expresion> valoresIniciales =
                    new ArrayList<>();


            if (ctx.listaValores() != null) {

                for (var expresion :
                        ctx.listaValores().elementoLista()) {

                    valoresIniciales.add(
                            (Expresion) visit(expresion)
                    );
                }
            }


            /*
             * Las dimensiones del tipo indican cuántos corchetes tiene.
             *
             * Ejemplo:
             *
             * int[]      -> 1 dimensión (pero sin tamaño específico en la declaración)
             * int[][]    -> 2 dimensiones
             * int[5]     -> 1 dimensión con tamaño 5
             * int[3][3]  -> 2 dimensiones con tamaños 3 y 3
             */
            List<Expresion> dimensiones =
                    new ArrayList<>();

            // Si hay una expresión de inicialización que es un CrearArreglo
            if (ctx.expresion() != null) {
                NodoAST inicializacion = visit(ctx.expresion());
                if (inicializacion instanceof CrearArreglo crearArreglo) {
                    // Tomar las dimensiones del CrearArreglo
                    dimensiones = crearArreglo.getDimensiones();
                }
            }

            // Si no hay dimensiones de la inicialización, usar null o lista vacía
            // (se determinarán en tiempo de ejecución o en análisis semántico)
            if (dimensiones.isEmpty()) {
                for (int i = 0; i < cantidadDimensiones(ctx.tipo()); i++) {
                    dimensiones.add(null);
                }
            }

            return new DeclaracionArreglo(
                    linea(ctx),
                    columna(ctx),
                    tipo,
                    nombre,
                    dimensiones,
                    valoresIniciales
            );
        }


        Expresion inicializacion = null;


        if (ctx.expresion() != null) {

            inicializacion =
                    (Expresion) visit(ctx.expresion());
        }


        return new DeclaracionVariable(
                linea(ctx),
                columna(ctx),
                tipo,
                nombre,
                inicializacion
        );
    }


    /* =========================================================
       ======================= ASIGNACION =====================
       ========================================================= */

    @Override
    public NodoAST visitAsignacion(
            GrammarZetarianoParser.AsignacionContext ctx) {

        Expresion destino =
                construirAccesoVariable(
                        ctx.accesoVariable()
                );


        Expresion valor =
                (Expresion) visit(ctx.expresion());


        String operador =
                ctx.op.getText();


        /*
         * Asignación normal:
         *
         * a = b
         */
        if (operador.equals("=")) {

            return new Asignacion(
                    linea(ctx),
                    columna(ctx),
                    destino,
                    valor
            );
        }


        /*
         * Asignaciones compuestas:
         *
         * a += b
         * a -= b
         * a *= b
         *
         * Se transforman en:
         *
         * a = a + b
         * a = a - b
         * a = a * b
         */
        String operadorBinario;


        switch (operador) {

            case "+=":
                operadorBinario = "+";
                break;

            case "-=":
                operadorBinario = "-";
                break;

            case "*=":
                operadorBinario = "*";
                break;

            default:
                operadorBinario = operador;
        }


        Expresion combinada =
                new ExpresionBinaria(
                        linea(ctx),
                        columna(ctx),
                        destino,
                        operadorBinario,
                        valor
                );


        return new Asignacion(
                linea(ctx),
                columna(ctx),
                destino,
                combinada
        );
    }


    /* =========================================================
       ======================== INCREMENTOS ===================
       ========================================================= */

    @Override
    public NodoAST visitSentIncrDecrPostfijo(
            GrammarZetarianoParser.SentIncrDecrPostfijoContext ctx) {

        Expresion destino =
                construirAccesoVariable(
                        ctx.accesoVariable()
                );


        String operador =
                ctx.INCREMENTO() != null
                        ? "++"
                        : "--";


        ExpresionUnaria incremento =
                new ExpresionUnaria(
                        linea(ctx),
                        columna(ctx),
                        operador,
                        destino,
                        false
                );


        return new SentenciaExpresion(
                linea(ctx),
                columna(ctx),
                incremento
        );
    }


    @Override
    public NodoAST visitSentIncrDecrPrefijo(
            GrammarZetarianoParser.SentIncrDecrPrefijoContext ctx) {

        Expresion destino =
                construirAccesoVariable(
                        ctx.accesoVariable()
                );


        String operador =
                ctx.INCREMENTO() != null
                        ? "++"
                        : "--";


        ExpresionUnaria incremento =
                new ExpresionUnaria(
                        linea(ctx),
                        columna(ctx),
                        operador,
                        destino,
                        true
                );


        return new SentenciaExpresion(
                linea(ctx),
                columna(ctx),
                incremento
        );
    }


    /* =========================================================
       ================= SENTENCIAS DELEGADAS =================
       ========================================================= */

    @Override
    public NodoAST visitSentDeclaracionVariable(
            GrammarZetarianoParser.SentDeclaracionVariableContext ctx) {

        return visit(ctx.declaracionVariable());
    }


    @Override
    public NodoAST visitSentAsignacion(
            GrammarZetarianoParser.SentAsignacionContext ctx) {

        return visit(ctx.asignacion());
    }


    @Override
    public NodoAST visitSentLlamadaFuncion(
            GrammarZetarianoParser.SentLlamadaFuncionContext ctx) {

        return new SentenciaExpresion(
                linea(ctx),
                columna(ctx),
                (Expresion) visit(ctx.llamadaFuncion())
        );
    }


    @Override
    public NodoAST visitSentLlamadaMetodo(
            GrammarZetarianoParser.SentLlamadaMetodoContext ctx) {

        return new SentenciaExpresion(
                linea(ctx),
                columna(ctx),
                (Expresion) visit(ctx.llamadaMetodo())
        );
    }


    @Override
    public NodoAST visitSentImprimir(
            GrammarZetarianoParser.SentImprimirContext ctx) {

        return visit(ctx.imprimirStmt());
    }


    @Override
    public NodoAST visitSentReadln(
            GrammarZetarianoParser.SentReadlnContext ctx) {

        return new SentenciaExpresion(
                linea(ctx),
                columna(ctx),
                new LlamadaFuncion(
                        linea(ctx),
                        columna(ctx),
                        "readln",
                        List.of()
                )
        );
    }


    @Override
    public NodoAST visitSentReturn(
            GrammarZetarianoParser.SentReturnContext ctx) {

        Expresion valor =
                ctx.expresion() != null
                        ? (Expresion) visit(ctx.expresion())
                        : null;


        return new SentenciaReturn(
                linea(ctx),
                columna(ctx),
                valor
        );
    }


    @Override
    public NodoAST visitSentBreak(
            GrammarZetarianoParser.SentBreakContext ctx) {

        return new SentenciaBreak(
                linea(ctx),
                columna(ctx)
        );
    }


    @Override
    public NodoAST visitSentContinue(
            GrammarZetarianoParser.SentContinueContext ctx) {

        return new SentenciaContinue(
                linea(ctx),
                columna(ctx)
        );
    }


    @Override
    public NodoAST visitSentCondicional(
            GrammarZetarianoParser.SentCondicionalContext ctx) {

        return visit(ctx.condicional());
    }


    @Override
    public NodoAST visitSentCicloFor(
            GrammarZetarianoParser.SentCicloForContext ctx) {

        return visit(ctx.cicloFor());
    }


    @Override
    public NodoAST visitSentCicloWhile(
            GrammarZetarianoParser.SentCicloWhileContext ctx) {

        return visit(ctx.cicloWhile());
    }


    @Override
    public NodoAST visitSentCicloDoWhile(
            GrammarZetarianoParser.SentCicloDoWhileContext ctx) {

        return visit(ctx.cicloDoWhile());
    }


    @Override
    public NodoAST visitSentSwitch(
            GrammarZetarianoParser.SentSwitchContext ctx) {

        return visit(ctx.switchCase());
    }


    @Override
    public NodoAST visitSentBloqueAnidado(
            GrammarZetarianoParser.SentBloqueAnidadoContext ctx) {

        return visit(ctx.bloque());
    }


    /* =========================================================
       ======================== PRINT =========================
       ========================================================= */

    @Override
    public NodoAST visitImprimirStmt(
            GrammarZetarianoParser.ImprimirStmtContext ctx) {

        String nombreFuncion =
                ctx.PRINTLN() != null
                        ? "println"
                        : "print";


        List<Expresion> argumentos =
                new ArrayList<>();


        if (ctx.expresion() != null) {

            argumentos.add(
                    (Expresion) visit(ctx.expresion())
            );
        }


        LlamadaFuncion llamada =
                new LlamadaFuncion(
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


    /* =========================================================
       ===================== CONDICIONALES ====================
       ========================================================= */

    @Override
    public NodoAST visitCondicional(
            GrammarZetarianoParser.CondicionalContext ctx) {

        /*
         * if
         */
        Expresion condicion =
                (Expresion) visit(ctx.expresion(0));


        Bloque bloqueEntonces = (Bloque) visit(ctx.cuerpo(0));


        /*
         * Número de else-if.
         *
         * Hay un bloque por cada:
         *
         * if
         * else if
         * else
         */
        int cantidadExpresiones =
                ctx.expresion().size();


        int cantidadBloques =
                ctx.cuerpo().size();


        boolean tieneElse =
                cantidadBloques > cantidadExpresiones;


        int cantidadElseIf =
                cantidadExpresiones - 1;


        List<CondicionIf> listaElseIf =
                new ArrayList<>();


        for (int i = 0;
             i < cantidadElseIf;
             i++) {

            Expresion condicionElseIf =
                    (Expresion) visit(
                            ctx.expresion(i + 1)
                    );


            Bloque bloqueElseIf =
                    (Bloque) visit(
                            ctx.cuerpo(i + 1)
                    );


            listaElseIf.add(
                    new CondicionIf(
                            linea(ctx),
                            columna(ctx),
                            condicionElseIf,
                            bloqueElseIf,
                            List.of(),
                            null
                    )
            );
        }


        Bloque bloqueElse = null;


        if (tieneElse) {

            bloqueElse =
                    (Bloque) visit(
                            ctx.cuerpo(cantidadBloques - 1)
                    );
        }


        return new CondicionIf(
                linea(ctx),
                columna(ctx),
                condicion,
                bloqueEntonces,
                listaElseIf,
                bloqueElse
        );
    }


    /* =========================================================
       ========================== WHILE =======================
       ========================================================= */

    @Override
    public NodoAST visitCicloWhile(
            GrammarZetarianoParser.CicloWhileContext ctx) {

        Expresion condicion =
                (Expresion) visit(ctx.expresion());

        // dentro-de-ciclo (para break/continue) lo controla AnalizadorWhile.
        Bloque cuerpo =
                (Bloque) visit(ctx.cuerpo());


        return new CicloWhile(
                linea(ctx),
                columna(ctx),
                condicion,
                cuerpo
        );
    }


    /* =========================================================
       ======================== DO WHILE ======================
       ========================================================= */

    @Override
    public NodoAST visitCicloDoWhile(
            GrammarZetarianoParser.CicloDoWhileContext ctx) {

        Bloque cuerpo =
                (Bloque) visit(ctx.bloque());

        Expresion condicion =
                (Expresion) visit(ctx.expresion());


        return new CicloDoWhile(
                linea(ctx),
                columna(ctx),
                cuerpo,
                condicion
        );
    }


    /* =========================================================
       =========================== FOR ========================
       ========================================================= */

    @Override
    public NodoAST visitCicloFor(
            GrammarZetarianoParser.CicloForContext ctx) {

        // El ámbito propio del for lo maneja AnalizadorCicloFor.
        Sentencia inicializacion = null;


        /*
         * for (int i = 0; ...)
         */
        if (ctx.declaracionVariable() != null) {

            NodoAST nodo =
                    visit(ctx.declaracionVariable());

            if (nodo instanceof Sentencia sentencia) {
                inicializacion = sentencia;
            }
        }


        /*
         * for (i = 0; ...)
         */
        else if (!ctx.asignacion().isEmpty()) {

            NodoAST nodo =
                    visit(ctx.asignacion(0));

            if (nodo instanceof Sentencia sentencia) {
                inicializacion = sentencia;
            }
        }


        /*
         * Condición.
         */
        Expresion condicion =
                ctx.expresion() != null
                        ? (Expresion) visit(ctx.expresion())
                        : null;


        /*
         * La asignación del for puede aparecer
         * en ctx.asignacion(1).
         */
        Expresion incremento = null;


        if (ctx.asignacion().size() > 1) {

            NodoAST nodo =
                    visit(ctx.asignacion(1));

            if (nodo instanceof Expresion expresion) {
                incremento = expresion;
            }
        }


        /*
         * i++
         */
        if (ctx.accesoVariable() != null) {

            Expresion destino =
                    construirAccesoVariable(
                            ctx.accesoVariable()
                    );


            String operador =
                    ctx.INCREMENTO() != null
                            ? "++"
                            : "--";


            incremento =
                    new ExpresionUnaria(
                            linea(ctx),
                            columna(ctx),
                            operador,
                            destino,
                            ctx.INCREMENTO() != null
                    );
        }


        Bloque cuerpo =
                (Bloque) visit(ctx.cuerpo());

        return new CicloFor(
                linea(ctx),
                columna(ctx),
                inicializacion,
                condicion,
                incremento,
                cuerpo
        );
    }


    /* =========================================================
       ========================== SWITCH ======================
       ========================================================= */

    @Override
    public NodoAST visitSwitchCase(
            GrammarZetarianoParser.SwitchCaseContext ctx) {

        Expresion expresionSwitch =
                (Expresion) visit(ctx.expresion());

        // dentro-de-switch (para break) lo controla AnalizadorSwitch.
        List<SentenciaCase> casos =
                new ArrayList<>();


        for (var caso : ctx.casoSwitch()) {

            NodoAST nodo = visit(caso);

            if (nodo instanceof SentenciaCase sentenciaCase) {
                casos.add(sentenciaCase);
            }
        }


        Bloque bloqueDefault = null;


        if (ctx.DEFAULT() != null) {

            // El default está al final, así que tomamos el último bloqueCaso
            List<GrammarZetarianoParser.BloqueCasoContext> bloquesCaso = Collections.singletonList(ctx.bloqueCaso());
            int cantidadBloquesCaso = bloquesCaso.size();

            // El default es el último bloqueCaso
            GrammarZetarianoParser.BloqueCasoContext bloqueCasoDefault =
                    bloquesCaso.get(cantidadBloquesCaso - 1);

            bloqueDefault =
                    (Bloque) visit(bloqueCasoDefault);
        }

        return new CondicionSwitch(
                linea(ctx),
                columna(ctx),
                expresionSwitch,
                casos,
                bloqueDefault
        );
    }


    /* =========================================================
       ======================= CASE ===========================
       ========================================================= */

    @Override
    public NodoAST visitCasoSwitch(
            GrammarZetarianoParser.CasoSwitchContext ctx) {

        Expresion valor =
                construirLiteral(ctx.literal());


        Bloque cuerpo =
                (Bloque) visit(ctx.bloqueCaso());


        return new SentenciaCase(
                linea(ctx),
                columna(ctx),
                valor,
                cuerpo
        );
    }


    /* =========================================================
       ===================== BLOQUE CASE ======================
       ========================================================= */

    @Override
    public NodoAST visitBloqueCaso(
            GrammarZetarianoParser.BloqueCasoContext ctx) {

        // Ámbito del case: lo maneja AnalizadorBloque al recorrer el cuerpo del case.
        List<Sentencia> sentencias =
                new ArrayList<>();


        for (var s : ctx.sentencia()) {

            NodoAST nodo = visit(s);

            if (nodo instanceof Sentencia sentencia) {
                sentencias.add(sentencia);
            }
        }

        return new Bloque(
                linea(ctx),
                columna(ctx),
                sentencias
        );
    }


    /* =========================================================
       ======================== EXPRESIONES ===================
       ========================================================= */

    @Override
    public NodoAST visitExpParentesis(
            GrammarZetarianoParser.ExpParentesisContext ctx) {

        return visit(ctx.expresion());
    }


    @Override
    public NodoAST visitExpNegacionLogica(
            GrammarZetarianoParser.ExpNegacionLogicaContext ctx) {

        return new ExpresionUnaria(
                linea(ctx),
                columna(ctx),
                "!",
                (Expresion) visit(ctx.expresion()),
                true
        );
    }


    @Override
    public NodoAST visitExpNegativo(
            GrammarZetarianoParser.ExpNegativoContext ctx) {

        return new ExpresionUnaria(
                linea(ctx),
                columna(ctx),
                "-",
                (Expresion) visit(ctx.expresion()),
                true
        );
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

        // "¿existe la clase?" lo valida InferidorCrearObjeto en la 2da pasada.
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
    public NodoAST visitExpMultiplicativa(GrammarZetarianoParser.ExpMultiplicativaContext ctx) {

        return binaria(ctx, ctx.expresion(0), ctx.op.getText(), ctx.expresion(1));
    }


    @Override
    public NodoAST visitExpAditiva(GrammarZetarianoParser.ExpAditivaContext ctx) {

        return binaria(ctx, ctx.expresion(0), ctx.op.getText(), ctx.expresion(1));
    }

    @Override
    public NodoAST visitExpRelacional(GrammarZetarianoParser.ExpRelacionalContext ctx) {

        return binaria(ctx, ctx.expresion(0), ctx.op.getText(), ctx.expresion(1));
    }

    @Override
    public NodoAST visitExpIgualdad(GrammarZetarianoParser.ExpIgualdadContext ctx) {

        return binaria(ctx, ctx.expresion(0), ctx.op.getText(), ctx.expresion(1));
    }

    @Override
    public NodoAST visitExpAnd(GrammarZetarianoParser.ExpAndContext ctx) {

        return binaria(ctx, ctx.expresion(0), "&&", ctx.expresion(1));
    }

    @Override
    public NodoAST visitExpOr(GrammarZetarianoParser.ExpOrContext ctx) {

        return binaria(ctx, ctx.expresion(0), "||", ctx.expresion(1));
    }

    private NodoAST binaria(ParserRuleContext ctx, GrammarZetarianoParser.ExpresionContext izquierda, String operador,
                            GrammarZetarianoParser.ExpresionContext derecha) {

        return new ExpresionBinaria(linea(ctx), columna(ctx), (Expresion) visit(izquierda), operador, (Expresion) visit(derecha));
    }

    @Override
    public NodoAST visitExpTernaria(GrammarZetarianoParser.ExpTernariaContext ctx) {

        return new ExpresionTernaria(
                linea(ctx),
                columna(ctx),
                (Expresion) visit(ctx.expresion(0)),
                (Expresion) visit(ctx.expresion(1)),
                (Expresion) visit(ctx.expresion(2))
        );
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

    @Override
    public NodoAST visitLlamadaFuncion(GrammarZetarianoParser.LlamadaFuncionContext ctx) {

        String nombre = ctx.ID().getText();

        // "¿existe el método?" lo valida InferidorLlamadaFuncion en la 2da pasada.
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

            // "¿existe la variable?" lo valida InferidorIdentificador en la 2da pasada.
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

            // "¿existe la variable?" lo valida InferidorIdentificador en la 2da pasada.
            actual = new Identificador(linea(ctx), columna(ctx), nombre);
        }

        for (int i = 0; i < ctx.getChildCount(); i++) {

            String texto = ctx.getChild(i).getText();


            if (texto.equals(".") && cursorId < ids.size()) {

                actual = new AccesoAtributo(linea(ctx), columna(ctx), actual, ids.get(cursorId++).getText());
            }

            else if (texto.equals("[") && cursorIndice < indices.size()) {

                Expresion indice = (Expresion) visit(indices.get(cursorIndice++));


                actual = new AccesoArreglo(linea(ctx), columna(ctx), actual, List.of(indice));
            }
        }


        return actual;
    }



    private List<Expresion> construirArgumentos(GrammarZetarianoParser.ListaArgumentosContext ctx) {

        List<Expresion> argumentos = new ArrayList<>();


        if (ctx == null) {
            return argumentos;
        }


        for (var expresion :
                ctx.expresion()) {

            argumentos.add((Expresion) visit(expresion));
        }


        return argumentos;
    }


    private Literal construirLiteral(
            GrammarZetarianoParser.LiteralContext ctx) {

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

    private int linea(ParserRuleContext ctx) {

        return ctx.getStart().getLine();
    }


    private int columna(ParserRuleContext ctx) {

        return ctx.getStart()
                .getCharPositionInLine();
    }
}