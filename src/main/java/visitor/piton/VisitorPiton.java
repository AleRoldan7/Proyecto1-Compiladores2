package visitor.piton;

import ast.NodoAST;
import ast.Programa;
import ast.clases.Clase;
import ast.declaraciones.*;
import ast.estructuras.Campo;
import ast.estructuras.Estructura;
import ast.estructuras.InicializacionEstructura;
import ast.expresiones.*;
import ast.sentencias.*;
import ast.tipos.Tipo;
import enums.Categoria;
import org.antlr.v4.runtime.ParserRuleContext;
import org.compi2.proyecto1compiladores2.GrammarPythonBaseVisitor;
import org.compi2.proyecto1compiladores2.GrammarPythonParser;
import semantico.AnalisisContexto;

import java.util.ArrayList;
import java.util.List;


public class VisitorPiton extends GrammarPythonBaseVisitor<NodoAST> {

    private final AnalisisContexto contexto;

    public VisitorPiton(AnalisisContexto contexto) {
        this.contexto = contexto;
    }

    /**
     * true si se pudo declarar (no estaba repetido en este ámbito)
     */
    private boolean declarar(String nombre, Categoria categoria, String tipo, int linea, int columna) {
        if (contexto.getTablaSimbolos().existeEnAmbitoActual(nombre)) {
            contexto.reportarError(linea, columna, "'" + nombre + "' ya fue declarado en este ámbito");
            return false;
        }
        contexto.getTablaSimbolos().declarar(nombre, categoria, tipo, "", linea);
        return true;
    }

    @Override
    public NodoAST visitProgram(GrammarPythonParser.ProgramContext ctx) {

        List<Estructura> estructuras = new ArrayList<>();
        List<Clase> clases = new ArrayList<>(); // Asumo que esto lo tenías en List.of()
        List<DeclaracionFuncion> funciones = new ArrayList<>();
        List<Declaracion> declaracionesGlobales = new ArrayList<>();

        // Iteramos sobre todos los hijos directos del programa
        for (var hijo : ctx.children) {

            // 1. Procesar sección de estructuras
            if (hijo instanceof GrammarPythonParser.SeccionEstructurasContext) {
                GrammarPythonParser.SeccionEstructurasContext ctxEstructuras = (GrammarPythonParser.SeccionEstructurasContext) hijo;
                for (var e : ctxEstructuras.declaracionEstructura()) {
                    estructuras.add((Estructura) visit(e));
                }
            }

            // 2. Procesar sección de funciones
            else if (hijo instanceof GrammarPythonParser.SeccionFuncionesContext) {
                GrammarPythonParser.SeccionFuncionesContext ctxFunciones = (GrammarPythonParser.SeccionFuncionesContext) hijo;
                for (var f : ctxFunciones.declaracionFuncion()) {
                    funciones.add((DeclaracionFuncion) visit(f));
                }
            }

            // 3. Procesar sentencias sueltas (variables, asignaciones, ciclos, etc.)
            else if (hijo instanceof GrammarPythonParser.SentenciaContext) {
                NodoAST nodoVisitado = visit(hijo);

                // Clasificamos el nodo visitado según su tipo real para meterlo en su lista
                if (nodoVisitado instanceof Estructura) {
                    estructuras.add((Estructura) nodoVisitado);
                } else if (nodoVisitado instanceof DeclaracionFuncion) {
                    funciones.add((DeclaracionFuncion) nodoVisitado);
                } else if (nodoVisitado instanceof Clase) {
                    clases.add((Clase) nodoVisitado);
                } else if (nodoVisitado instanceof Declaracion) {
                    declaracionesGlobales.add((Declaracion) nodoVisitado);
                }
                // Si es otro tipo de nodo (como una asignación o un ciclo que no hereda de Declaracion),
                // deberías agregarlo a la lista que corresponda. Si no existe, se ignora.
            }
        }

        // Retornamos el Programa con sus listas llenas
        return new Programa(
                linea(ctx),
                columna(ctx),
                List.of(), // Lista de Strings (¿Imports? o algo similar, se mantiene vacía)
                estructuras,
                clases,
                funciones,
                declaracionesGlobales
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
            tipoRetorno = construirTipo(ctx.tipo());
        }

        // Se declara ANTES de entrar a su propio ámbito: permite recursividad,
        // y en Y? todas las funciones viven en el ámbito global.
        declarar(nombreFuncion, Categoria.FUNCION,
                tipoRetorno != null ? tipoRetorno.getNombre() : "void", linea(ctx), columna(ctx));

        contexto.getTablaSimbolos().entrarAmbito("Funcion " + nombreFuncion);

        // Parámetros
        List<Parametro> parametros = new ArrayList<>();

        if (ctx.listaParametros() != null) {

            for (var p : ctx.listaParametros().parametro()) {

                if (p instanceof GrammarPythonParser.ParametroSimpleContext simple) {

                    Tipo tipo = construirTipo(simple.tipo());

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
                    declarar(simple.ID().getText(), Categoria.PARAMETRO, tipo.getNombre(), linea(simple), columna(simple));

                } else if (p instanceof GrammarPythonParser.ParametroArregloContext arreglo) {

                    // Para arreglos: [ ] tipo id — igual, sin tocar el nombre del tipo
                    Tipo tipo = construirTipo(arreglo.tipo());

                    Tipo tipoArreglo = new Tipo(
                            linea(arreglo),
                            columna(arreglo),
                            tipo.getNombre(),
                            true,
                            1
                    );

                    parametros.add(
                            new Parametro(
                                    linea(arreglo),
                                    columna(arreglo),
                                    tipoArreglo,
                                    arreglo.ID().getText(),
                                    false,
                                    true
                            )
                    );
                    declarar(arreglo.ID().getText(), Categoria.PARAMETRO, tipo.getNombre(), linea(arreglo), columna(arreglo));

                } else if (p instanceof GrammarPythonParser.ParametroEstructuraContext estructura) {

                    // Para estructuras: { } tipo id
                    Tipo tipo = construirTipo(estructura.tipo());

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
                    declarar(estructura.ID().getText(), Categoria.PARAMETRO, tipo.getNombre(), linea(estructura), columna(estructura));
                }
            }
        }

        // Cuerpo de la función
        Bloque cuerpoFuncion = (Bloque) visit(ctx.bloque());

        contexto.getTablaSimbolos().salirAmbito();

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

        // Recorrer los campos de la estructura
        for (var campo : ctx.campoEstructura()) {
            Tipo tipo = construirTipo(campo.tipo());

            // Verificar si tiene dimensión (tamaño constante, ver GrammarPython.g4: campoEstructura usa 'dimension')
            if (campo.dimension() != null) {
                // Es un arreglo con tamaño constante — el nombre del tipo NO lleva "[]"
                int dimensiones = campo.dimension().NUMERO_ENTERO().size();
                Tipo tipoArreglo = new Tipo(
                        linea(campo),
                        columna(campo),
                        tipo.getNombre(),
                        true,
                        dimensiones
                );
                campos.add(new Campo(
                        linea(campo),
                        columna(campo),
                        tipoArreglo,
                        campo.ID().getText()
                ));
            } else {
                campos.add(new Campo(
                        linea(campo),
                        columna(campo),
                        tipo,
                        campo.ID().getText()
                ));
            }
        }

        return new Estructura(
                linea(ctx),
                columna(ctx),
                ctx.ID().getText(),
                campos
        );
    }

    @Override
    public NodoAST visitDeclaracionVariable(GrammarPythonParser.DeclaracionVariableContext ctx) {
        Tipo tipo = construirTipo(ctx.tipo());
        Expresion valorInicial = ctx.expresion() != null ? (Expresion) visit(ctx.expresion()) : null;

        // Verificar si es un arreglo con dimensiones
        if (ctx.dimension() != null) {
            // FIX: 'dimension' ahora es (CORCHETE_ABRE NUMERO_ENTERO CORCHETE_CIERRA)+ (tamaño
            // siempre constante, exigido por el enunciado) — ya no tiene expresion(), solo NUMERO_ENTERO()
            List<Expresion> dims = ctx.dimension().NUMERO_ENTERO().stream()
                    .map(n -> (Expresion) new Literal(linea(ctx), columna(ctx), Integer.parseInt(n.getText()), "entero"))
                    .toList();

            // Construir el tipo del arreglo SIN tocar el nombre base
            // (tipo.getNombre() debe seguir siendo "entero", no "entero[]",
            // o se rompe cualquier comparación de tipos más adelante)
            int numDims = ctx.dimension().CORCHETE_ABRE().size();
            Tipo tipoArreglo = new Tipo(
                    linea(ctx),
                    columna(ctx),
                    tipo.getNombre(),
                    true,
                    numDims
            );

            List<Expresion> valores = new ArrayList<>();
            if (valorInicial instanceof InicializacionEstructura ie) {
                valores = ie.getValores();
            } else if (valorInicial != null) {
                // Si es un solo valor, podría ser inicialización de arreglo con lista
                valores.add(valorInicial);
            }

            return new DeclaracionArreglo(
                    linea(ctx),
                    columna(ctx),
                    tipoArreglo,
                    ctx.ID().getText(),
                    dims,
                    valores
            );
        }

        return new DeclaracionVariable(
                linea(ctx),
                columna(ctx),
                tipo,
                ctx.ID().getText(),
                valorInicial
        );
    }

    @Override
    public NodoAST visitExpAditiva(GrammarPythonParser.ExpAditivaContext ctx) {
        return new ExpresionBinaria(
                linea(ctx),
                columna(ctx),
                (Expresion) visit(ctx.expresion(0)),
                ctx.op.getText(),
                (Expresion) visit(ctx.expresion(1))
        );
    }

    @Override
    public NodoAST visitExpMultiplicativa(GrammarPythonParser.ExpMultiplicativaContext ctx) {
        return new ExpresionBinaria(
                linea(ctx),
                columna(ctx),
                (Expresion) visit(ctx.expresion(0)),
                ctx.op.getText(),
                (Expresion) visit(ctx.expresion(1))
        );
    }

    @Override
    public NodoAST visitExpLiteral(GrammarPythonParser.ExpLiteralContext ctx) {
        return construirLiteral(ctx.literal());
    }

    /**
     * FIX: antes visitCasoElegir hacía visit(ctx.literal()) directo, pero no había ningún
     * visitLiteral(...) sobrescrito — ANTLR caía al 'visitChildren' por defecto, que para
     * un token suelto devuelve null. Cada 'caso' del switch terminaba con valor = null.
     */
    private Literal construirLiteral(GrammarPythonParser.LiteralContext lit) {
        if (lit.NUMERO_ENTERO() != null) {
            return new Literal(linea(lit), columna(lit), Integer.parseInt(lit.getText()), "entero");
        }
        if (lit.DECIMAL() != null) {
            return new Literal(linea(lit), columna(lit), Double.parseDouble(lit.getText()), "flotante");
        }
        if (lit.VERDADERO() != null || lit.FALSO() != null) {
            return new Literal(linea(lit), columna(lit), lit.VERDADERO() != null, "bool");
        }
        return new Literal(linea(lit), columna(lit), lit.getText(), "cadena");
    }

    @Override
    public NodoAST visitExpNegacionLogica(GrammarPythonParser.ExpNegacionLogicaContext ctx) {
        return new ExpresionUnaria(
                linea(ctx),
                columna(ctx),
                "!",
                (Expresion) visit(ctx.expresion()),
                true
        );
    }

    @Override
    public NodoAST visitExpNegativo(GrammarPythonParser.ExpNegativoContext ctx) {
        return new ExpresionUnaria(
                linea(ctx),
                columna(ctx),
                "-",
                (Expresion) visit(ctx.expresion()),
                true
        );
    }

    @Override
    public NodoAST visitExpPreIncremento(GrammarPythonParser.ExpPreIncrementoContext ctx) {
        return new ExpresionUnaria(
                linea(ctx),
                columna(ctx),
                "++",
                new Identificador(linea(ctx), columna(ctx), ctx.ID().getText()),
                true
        );
    }

    @Override
    public NodoAST visitExpPreDecremento(GrammarPythonParser.ExpPreDecrementoContext ctx) {
        return new ExpresionUnaria(
                linea(ctx),
                columna(ctx),
                "--",
                new Identificador(linea(ctx), columna(ctx), ctx.ID().getText()),
                true
        );
    }

    @Override
    public NodoAST visitExpPostIncremento(GrammarPythonParser.ExpPostIncrementoContext ctx) {
        return new ExpresionUnaria(
                linea(ctx),
                columna(ctx),
                "++",
                new Identificador(linea(ctx), columna(ctx), ctx.ID().getText()),
                false
        );
    }

    @Override
    public NodoAST visitExpPostDecremento(GrammarPythonParser.ExpPostDecrementoContext ctx) {
        return new ExpresionUnaria(
                linea(ctx),
                columna(ctx),
                "--",
                new Identificador(linea(ctx), columna(ctx), ctx.ID().getText()),
                false
        );
    }

    @Override
    public NodoAST visitExpRelacional(GrammarPythonParser.ExpRelacionalContext ctx) {
        return new ExpresionBinaria(
                linea(ctx),
                columna(ctx),
                (Expresion) visit(ctx.expresion(0)),
                ctx.op.getText(),
                (Expresion) visit(ctx.expresion(1))
        );
    }

    @Override
    public NodoAST visitExpIgualdad(GrammarPythonParser.ExpIgualdadContext ctx) {
        return new ExpresionBinaria(
                linea(ctx),
                columna(ctx),
                (Expresion) visit(ctx.expresion(0)),
                ctx.op.getText(),
                (Expresion) visit(ctx.expresion(1))
        );
    }

    @Override
    public NodoAST visitExpAnd(GrammarPythonParser.ExpAndContext ctx) {
        return new ExpresionBinaria(
                linea(ctx),
                columna(ctx),
                (Expresion) visit(ctx.expresion(0)),
                "&&",
                (Expresion) visit(ctx.expresion(1))
        );
    }

    @Override
    public NodoAST visitExpOr(GrammarPythonParser.ExpOrContext ctx) {
        return new ExpresionBinaria(
                linea(ctx),
                columna(ctx),
                (Expresion) visit(ctx.expresion(0)),
                "||",
                (Expresion) visit(ctx.expresion(1))
        );
    }

    @Override
    public NodoAST visitExpParentesis(GrammarPythonParser.ExpParentesisContext ctx) {
        return visit(ctx.expresion());
    }

    @Override
    public NodoAST visitCondicional(GrammarPythonParser.CondicionalContext ctx) {
        // Obtener la condición del if
        Expresion condicion = (Expresion) visit(ctx.expresion(0));
        Bloque bloqueEntonces = (Bloque) visit(ctx.bloque(0));

        // Procesar else-if
        List<CondicionIf> listaElseIf = new ArrayList<>();
        int cantidadElseIf = ctx.expresion().size() - 1;

        for (int i = 0; i < cantidadElseIf; i++) {
            Expresion condElseIf = (Expresion) visit(ctx.expresion(i + 1));
            Bloque bloqueElseIf = (Bloque) visit(ctx.bloque(i + 1));
            listaElseIf.add(new CondicionIf(
                    linea(ctx),
                    columna(ctx),
                    condElseIf,
                    bloqueElseIf,
                    List.of(),
                    null
            ));
        }

        // Procesar else (contrario)
        Bloque bloqueElse = null;
        int cantidadBloques = ctx.bloque().size();
        int cantidadExpresiones = ctx.expresion().size();

        if (cantidadBloques > cantidadExpresiones) {
            bloqueElse = (Bloque) visit(ctx.bloque(cantidadBloques - 1));
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

    @Override
    public NodoAST visitSwitchCase(GrammarPythonParser.SwitchCaseContext ctx) {
        Expresion expresionSwitch = (Expresion) visit(ctx.expresion());

        List<SentenciaCase> casos = new ArrayList<>();

        for (var caso : ctx.casoElegir()) {
            NodoAST nodo = visit(caso);
            if (nodo instanceof SentenciaCase sentenciaCase) {
                casos.add(sentenciaCase);
            }
        }

        Bloque bloqueDefault = null;
        if (ctx.SIEMPRE() != null) {
            // ctx.bloqueCaso() devuelve un solo BloqueCasoContext (el del default)
            bloqueDefault = (Bloque) visit(ctx.bloqueCaso());
        }

        return new CondicionSwitch(
                linea(ctx),
                columna(ctx),
                expresionSwitch,
                casos,
                bloqueDefault
        );
    }

    @Override
    public NodoAST visitCasoElegir(GrammarPythonParser.CasoElegirContext ctx) {
        Expresion valor = construirLiteral(ctx.literal());
        Bloque cuerpo = (Bloque) visit(ctx.bloqueCaso());

        return new SentenciaCase(
                linea(ctx),
                columna(ctx),
                valor,
                cuerpo
        );
    }

    @Override
    public NodoAST visitBloqueCaso(GrammarPythonParser.BloqueCasoContext ctx) {
        List<Sentencia> sentencias = new ArrayList<>();

        for (var s : ctx.sentencia()) {
            NodoAST nodo = visit(s);
            if (nodo instanceof Sentencia sentencia) {
                sentencias.add(sentencia);
            }
        }

        // El break está en la gramática pero no se incluye en el AST
        // porque ya está como parte del bloque

        return new Bloque(
                linea(ctx),
                columna(ctx),
                sentencias
        );
    }

    @Override
    public NodoAST visitBloque(GrammarPythonParser.BloqueContext ctx) {
        List<Sentencia> sentencias = new ArrayList<>();
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

    @Override
    public NodoAST visitSentDeclaracionVariable(GrammarPythonParser.SentDeclaracionVariableContext ctx) {
        return visit(ctx.declaracionVariable());
    }

    @Override
    public NodoAST visitSentAsignacion(GrammarPythonParser.SentAsignacionContext ctx) {
        return visit(ctx.asignacion());
    }

    @Override
    public NodoAST visitAsignacion(GrammarPythonParser.AsignacionContext ctx) {
        Expresion destino = (Expresion) visit(ctx.accesoVariable());
        Expresion valor = (Expresion) visit(ctx.expresion());

        return new Asignacion(
                linea(ctx),
                columna(ctx),
                destino,
                valor
        );
    }

    @Override
    public NodoAST visitSentLlamadaFuncion(GrammarPythonParser.SentLlamadaFuncionContext ctx) {
        return new SentenciaExpresion(
                linea(ctx),
                columna(ctx),
                (Expresion) visit(ctx.llamadaFuncion())
        );
    }

    @Override
    public NodoAST visitLlamadaFuncion(GrammarPythonParser.LlamadaFuncionContext ctx) {
        String nombre = ctx.ID().getText();
        List<Expresion> argumentos = new ArrayList<>();

        if (ctx.listaArgumentos() != null) {
            for (var expr : ctx.listaArgumentos().expresion()) {
                argumentos.add((Expresion) visit(expr));
            }
        }

        return new LlamadaFuncion(
                linea(ctx),
                columna(ctx),
                nombre,
                argumentos
        );
    }

    @Override
    public NodoAST visitSentIncrDecrPostfijo(GrammarPythonParser.SentIncrDecrPostfijoContext ctx) {
        Expresion destino = (Expresion) visit(ctx.accesoVariable());
        String operador = ctx.INCREMENTO() != null ? "++" : "--";

        ExpresionUnaria incremento = new ExpresionUnaria(
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
    public NodoAST visitSentIncrDecrPrefijo(GrammarPythonParser.SentIncrDecrPrefijoContext ctx) {
        Expresion destino = (Expresion) visit(ctx.accesoVariable());
        String operador = ctx.INCREMENTO() != null ? "++" : "--";

        ExpresionUnaria incremento = new ExpresionUnaria(
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

    @Override
    public NodoAST visitSentImprimir(GrammarPythonParser.SentImprimirContext ctx) {
        return visit(ctx.imprimirStmt());
    }

    @Override
    public NodoAST visitImprimirStmt(GrammarPythonParser.ImprimirStmtContext ctx) {
        List<Expresion> argumentos = new ArrayList<>();
        argumentos.add((Expresion) visit(ctx.expresion()));

        return new SentenciaExpresion(
                linea(ctx),
                columna(ctx),
                new LlamadaFuncion(
                        linea(ctx),
                        columna(ctx),
                        "imprimir",
                        argumentos
                )
        );
    }

    @Override
    public NodoAST visitSentLeer(GrammarPythonParser.SentLeerContext ctx) {
        return visit(ctx.leerStmt());
    }

    @Override
    public NodoAST visitLeerStmt(GrammarPythonParser.LeerStmtContext ctx) {
        return new SentenciaExpresion(
                linea(ctx),
                columna(ctx),
                new LlamadaFuncion(
                        linea(ctx),
                        columna(ctx),
                        "leer",
                        List.of()
                )
        );
    }

    @Override
    public NodoAST visitExpLeer(GrammarPythonParser.ExpLeerContext ctx) {
        return new LlamadaFuncion(
                linea(ctx),
                columna(ctx),
                "leer",
                List.of()
        );
    }

    @Override
    public NodoAST visitSentRetorno(GrammarPythonParser.SentRetornoContext ctx) {
        Expresion valor = ctx.expresion() != null
                ? (Expresion) visit(ctx.expresion())
                : null;

        return new SentenciaReturn(
                linea(ctx),
                columna(ctx),
                valor
        );
    }

    @Override
    public NodoAST visitSentRomper(GrammarPythonParser.SentRomperContext ctx) {
        return new SentenciaBreak(
                linea(ctx),
                columna(ctx)
        );
    }

    @Override
    public NodoAST visitSentContinuar(GrammarPythonParser.SentContinuarContext ctx) {
        return new SentenciaContinue(
                linea(ctx),
                columna(ctx)
        );
    }

    @Override
    public NodoAST visitSentCondicional(GrammarPythonParser.SentCondicionalContext ctx) {
        return visit(ctx.condicional());
    }

    @Override
    public NodoAST visitSentSwitch(GrammarPythonParser.SentSwitchContext ctx) {
        return visit(ctx.switchCase());
    }

    @Override
    public NodoAST visitSentCicloPara(GrammarPythonParser.SentCicloParaContext ctx) {
        return visit(ctx.cicloPara());
    }

    @Override
    public NodoAST visitCicloPara(GrammarPythonParser.CicloParaContext ctx) {
        Sentencia inicializacion = null;

        // Inicialización
        if (ctx.declaracionVariable() != null) {
            NodoAST nodo = visit(ctx.declaracionVariable());
            if (nodo instanceof Sentencia sentencia) {
                inicializacion = sentencia;
            }
        } else if (ctx.asignacion() != null) {
            NodoAST nodo = visit(ctx.asignacion());
            if (nodo instanceof Sentencia sentencia) {
                inicializacion = sentencia;
            }
        }

        // Condición
        Expresion condicion = null;
        if (!ctx.expresion().isEmpty()) {
            condicion = (Expresion) visit(ctx.expresion().get(0));
        }

        // Incremento
        Expresion incremento = null;
        if (ctx.expresion().size() > 1) {
            incremento = (Expresion) visit(ctx.expresion().get(1));
        }

        // Cuerpo
        Bloque cuerpo = (Bloque) visit(ctx.bloque());

        return new CicloFor(
                linea(ctx),
                columna(ctx),
                inicializacion,
                condicion,
                incremento,
                cuerpo
        );
    }

    @Override
    public NodoAST visitSentCicloMientras(GrammarPythonParser.SentCicloMientrasContext ctx) {
        return visit(ctx.cicloMientras());
    }

    @Override
    public NodoAST visitCicloWhile(GrammarPythonParser.CicloWhileContext ctx) {
        Expresion condicion = (Expresion) visit(ctx.expresion());
        Bloque cuerpo = (Bloque) visit(ctx.bloque());

        return new CicloWhile(
                linea(ctx),
                columna(ctx),
                condicion,
                cuerpo
        );
    }

    @Override
    public NodoAST visitCicloDoWhile(GrammarPythonParser.CicloDoWhileContext ctx) {
        Bloque cuerpo = (Bloque) visit(ctx.bloque());
        Expresion condicion = (Expresion) visit(ctx.expresion());

        return new CicloDoWhile(
                linea(ctx),
                columna(ctx),
                cuerpo,
                condicion
        );
    }

    @Override
    public NodoAST visitSentEstructuraLocal(GrammarPythonParser.SentEstructuraLocalContext ctx) {
        return visit(ctx.declaracionEstructura());
    }

    @Override
    public NodoAST visitExpAcceso(GrammarPythonParser.ExpAccesoContext ctx) {
        return visit(ctx.accesoVariable());
    }

    @Override
    public NodoAST visitAccesoVariable(GrammarPythonParser.AccesoVariableContext ctx) {
        // Procesar acceso a variable con posibles puntos e índices
        List<String> ids = new ArrayList<>();
        for (var id : ctx.ID()) {
            ids.add(id.getText());
        }

        // Si solo hay un ID, es acceso simple
        if (ids.size() == 1 && ctx.expresion().isEmpty()) {
            return new Identificador(
                    linea(ctx),
                    columna(ctx),
                    ids.get(0)
            );
        }

        // Construir acceso compuesto
        Expresion actual = new Identificador(
                linea(ctx),
                columna(ctx),
                ids.get(0)
        );

        int idIndex = 1;
        int exprIndex = 0;

        for (int i = 1; i < ctx.getChildCount(); i++) {
            String texto = ctx.getChild(i).getText();

            if (texto.equals(".") && idIndex < ids.size()) {
                actual = new AccesoAtributo(
                        linea(ctx),
                        columna(ctx),
                        actual,
                        ids.get(idIndex++)
                );
            } else if (texto.equals("[") && exprIndex < ctx.expresion().size()) {
                Expresion indice = (Expresion) visit(ctx.expresion().get(exprIndex++));
                actual = new AccesoArreglo(
                        linea(ctx),
                        columna(ctx),
                        actual,
                        List.of(indice)
                );
            }
        }

        return actual;
    }

    @Override
    public NodoAST visitExpListaValores(GrammarPythonParser.ExpListaValoresContext ctx) {
        List<Expresion> valores = new ArrayList<>();

        // ctx.listaValores() devuelve el contexto de la lista de valores
        GrammarPythonParser.ListaValoresContext listaValores = ctx.listaValores();

        if (listaValores != null) {
            // listaValores.expresion() devuelve la lista de expresiones
            for (var expr : listaValores.expresion()) {
                valores.add((Expresion) visit(expr));
            }
        }

        return new InicializacionEstructura(
                linea(ctx),
                columna(ctx),
                null,  // El nombre del tipo se determinará en análisis semántico
                valores
        );
    }

    private Tipo construirTipo(GrammarPythonParser.TipoContext ctx) {
        if (ctx == null) {
            return null;
        }
        return new Tipo(
                linea(ctx),
                columna(ctx),
                ctx.getText(),
                false,
                0
        );
    }

    private int linea(ParserRuleContext ctx) {
        return ctx.getStart().getLine();
    }

    private int columna(ParserRuleContext ctx) {
        return ctx.getStart().getCharPositionInLine();
    }
}