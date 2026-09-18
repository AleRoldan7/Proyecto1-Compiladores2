package ast.clases;

import ast.NodoAST;
import ast.declaraciones.Parametro;
import ast.sentencias.Bloque;
import ast.sentencias.Sentencia;
import ast.sentencias.SentenciaReturn;
import ast.tipos.Tipo;
import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Metodo extends NodoAST {

    private String nombreMetodo;
    private Tipo tipoRetorno;
    private List<Parametro> parametros;
    private Bloque cuerpoMetodo;

    public Metodo(int linea, int columna, String nombreMetodo, Tipo tipoRetorno, List<Parametro> parametros, Bloque cuerpoMetodo) {
        super(linea, columna);
        this.nombreMetodo = nombreMetodo;
        this.tipoRetorno = tipoRetorno;
        this.parametros = parametros;
        this.cuerpoMetodo = cuerpoMetodo;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {

        // 1. Registrar la función para forward declarations en C
        contexto.registrarFuncion(nombreMetodo);

        // 2. Etiqueta de inicio de función
        contexto.agregarEtiqueta("func_" + nombreMetodo);

        // 3. Declarar parámetros (el generador de C los mapea a argumentos)
        if (parametros != null) {
            for (Parametro p : parametros) {
                contexto.agregar("param_decl", p.getTipoParametro().getNombre(),
                        p.getNombreParametro(), null);
            }
        }

        // 4. Cuerpo
        cuerpoMetodo.generarC3D(contexto);

        // 5. Return implícito SOLO si es void y no tiene return explícito
        if (esVoid() && !tieneReturnExplicito(cuerpoMetodo)) {
            contexto.agregar("return", null, null, null);
        }

        // 6. Etiqueta de fin de función (útil para el traductor a C)
        contexto.agregarEtiqueta("end_" + nombreMetodo);

        return null;
    }

    /* =========================================================
       HELPERS
       ========================================================= */

    private boolean esVoid() {
        return tipoRetorno == null
                || "void".equals(tipoRetorno.getNombre())
                || tipoRetorno.getNombre() == null;
    }

    /**
     * Recorre el cuerpo buscando un 'return' explícito.
     * Es recursivo porque puede estar dentro de un if, while, for...
     */
    private boolean tieneReturnExplicito(Bloque bloque) {

        if (bloque == null || bloque.getSentencias() == null) {
            return false;
        }

        for (Sentencia s : bloque.getSentencias()) {
            if (contieneReturn(s)) return true;
        }

        return false;
    }

    private boolean contieneReturn(Object nodo) {

        if (nodo instanceof SentenciaReturn) {
            return true;
        }

        // Aquí puedes extender para if/while/for/switch:
        //   if (nodo instanceof CondicionIf c) {
        //       return tieneReturnExplicito(c.getBloqueEntonces())
        //           || tieneReturnExplicito(c.getBloqueElse())
        //           || c.getListaElseIf().stream().anyMatch(e -> tieneReturnExplicito(e.getBloque()));
        //   }
        //   ...
        //
        // Por ahora basta con detectar los return directos.

        return false;
    }
}
