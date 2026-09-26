package ast.expresiones;

import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LlamadaMetodo extends Expresion {

    private Expresion objeto;
    private String metodo;
    private List<Expresion> argumentos;

    /** Nombre de la clase del receptor, llenado por InferidorLlamadaMetodo durante el análisis semántico. */
    private String claseReceptor;

    public LlamadaMetodo(int linea, int columna, Expresion objeto, String metodo, List<Expresion> argumentos) {
        super(linea, columna);
        this.objeto = objeto;
        this.metodo = metodo;
        this.argumentos = argumentos;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {

        String obj = objeto.generarC3D(contexto);
        contexto.agregar("param", obj, null, null);

        if (argumentos != null) {
            for (Expresion arg : argumentos) {
                String valor = arg.generarC3D(contexto);
                contexto.agregar("param", valor, null, null);
            }
        }

        String temporal = contexto.nuevoTemporal();
        int cantidadArgs = ((argumentos == null) ? 0 : argumentos.size()) + 1;

        String nombreFuncion = (claseReceptor != null)
                ? ContextoC3D.nombreFuncion(claseReceptor, metodo)
                : metodo;   // respaldo: GenerarCodigoC lo resuelve por heurística

        contexto.agregar("call", nombreFuncion, String.valueOf(cantidadArgs), temporal);

        return temporal;
    }
}