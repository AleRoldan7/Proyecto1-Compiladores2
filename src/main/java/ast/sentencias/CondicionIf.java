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

        String etiquetaFin = contexto.nuevaEtiqueta();

        generarRama(contexto, getCondicion(), getBloqueEntonces(), etiquetaFin);            // ADAPTA los getters

        if (getListaSiNoSi() != null) {
            for (CondicionIf rama : getListaSiNoSi()) {
                generarRama(contexto, rama.getCondicion(), rama.getBloqueEntonces(), etiquetaFin);
            }
        }

        if (getBloqueSiNo() != null) {
            getBloqueSiNo().generarC3D(contexto);
        }

        contexto.agregarEtiqueta(etiquetaFin);
        return null;
    }

    private void generarRama(ContextoC3D contexto, Expresion condicion, Bloque bloque, String etiquetaFin) {

        String etiquetaSiguiente = contexto.nuevaEtiqueta();

        String valor = condicion.generarC3D(contexto);
        contexto.saltoSiFalso(valor, etiquetaSiguiente);

        bloque.generarC3D(contexto);
        contexto.salto(etiquetaFin);

        contexto.agregarEtiqueta(etiquetaSiguiente);
    }

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
