package ast.sentencias;

import ast.NodoAST;
import ast.expresiones.Expresion;
import ast.expresiones.ExpresionBinaria;
import ast.expresiones.ExpresionUnaria;
import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CondicionIf extends NodoAST implements Sentencia {

    private Expresion condicion;
    private Bloque bloqueEntonces;
    private List<CondicionIf> listaSiNoSi;
    private Bloque bloqueSiNo;

    public CondicionIf(int linea, int columna, Expresion condicion, Bloque bloqueEntonces, List<CondicionIf> listaSiNoSi, Bloque bloqueSiNo) {
        super(linea, columna);
        this.condicion = condicion;
        this.bloqueEntonces = bloqueEntonces;
        this.listaSiNoSi = listaSiNoSi;
        this.bloqueSiNo = bloqueSiNo;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {

        // if
        String etqVerdadero = contexto.nuevaEtiqueta();
        String etqFalso     = contexto.nuevaEtiqueta();
        String etqFin       = contexto.nuevaEtiqueta();

        generarCondicion(condicion, contexto, etqVerdadero, etqFalso);

        contexto.agregarEtiqueta(etqVerdadero);
        bloqueEntonces.generarC3D(contexto);
        contexto.salto(etqFin);

        // else ifs
        String etqFalsoActual = etqFalso;

        if (listaSiNoSi != null) {
            for (CondicionIf c : listaSiNoSi) {

                contexto.agregarEtiqueta(etqFalsoActual);

                String etqV = contexto.nuevaEtiqueta();
                String etqF = contexto.nuevaEtiqueta();

                generarCondicion(c.getCondicion(), contexto, etqV, etqF);

                contexto.agregarEtiqueta(etqV);
                c.getBloqueEntonces().generarC3D(contexto);
                contexto.salto(etqFin);

                etqFalsoActual = etqF;
            }
        }

        contexto.agregarEtiqueta(etqFalso);
        if (bloqueSiNo != null) bloqueSiNo.generarC3D(contexto);

        contexto.agregarEtiqueta(etqFin);

        return null;
    }

    /**
     * Genera el código de una condición booleana con dos etiquetas:
     * etqV (a donde saltar si es verdadera), etqF (si es falsa).
     *
     * Maneja AND, OR, NOT y comparaciones simples.
     */
    public static void generarCondicion(Expresion cond, ContextoC3D ctx,
                                        String etqV, String etqF) {

        if (cond instanceof ExpresionBinaria bin) {

            String op = bin.getOperacion();

            if (op.equals("&&")) {
                String etqIntermedia = ctx.nuevaEtiqueta();
                generarCondicion(bin.getIzquierda(), ctx, etqIntermedia, etqF);
                ctx.agregarEtiqueta(etqIntermedia);
                generarCondicion(bin.getDerecha(), ctx, etqV, etqF);
                return;
            }

            if (op.equals("||")) {
                String etqIntermedia = ctx.nuevaEtiqueta();
                generarCondicion(bin.getIzquierda(), ctx, etqV, etqIntermedia);
                ctx.agregarEtiqueta(etqIntermedia);
                generarCondicion(bin.getDerecha(), ctx, etqV, etqF);
                return;
            }

            if (esRelacional(op)) {
                String izq = bin.getIzquierda().generarC3D(ctx);
                String der = bin.getDerecha().generarC3D(ctx);
                ctx.agregar("if_" + op, izq, der, etqV);
                ctx.salto(etqF);
                return;
            }
        }

        if (cond instanceof ExpresionUnaria un && "!".equals(un.getOperador())) {
            generarCondicion(un.getExpresion(), ctx, etqF, etqV);
            return;
        }

        // Condición simple
        String valor = cond.generarC3D(ctx);
        ctx.saltoSiVerdadero(valor, etqV);
        ctx.salto(etqF);
    }

    private static boolean esRelacional(String op) {
        return op.equals("<") || op.equals(">") ||
                op.equals("<=") || op.equals(">=") ||
                op.equals("==") || op.equals("!=");
    }
}
