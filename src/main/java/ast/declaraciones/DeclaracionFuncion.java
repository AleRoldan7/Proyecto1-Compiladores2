package ast.declaraciones;

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
public class DeclaracionFuncion extends Declaracion {

    private String nombreFuncion;
    private Tipo tipoRetorno;
    private List<Parametro> parametros;
    private Bloque cuerpoFuncion;

    public DeclaracionFuncion(int linea, int columna, String nombreFuncion, Tipo tipoRetorno, List<Parametro> parametros, Bloque cuerpoFuncion) {
        super(linea, columna);
        this.nombreFuncion = nombreFuncion;
        this.tipoRetorno = tipoRetorno;
        this.parametros = parametros;
        this.cuerpoFuncion = cuerpoFuncion;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {

        contexto.registrarFuncion(nombreFuncion);
        contexto.agregarEtiqueta("func_" + nombreFuncion);

        // Declarar parámetros
        if (parametros != null) {
            for (Parametro p : parametros) {
                contexto.agregar("param_decl",
                        p.getTipoParametro().getNombre(),
                        p.getNombreParametro(),
                        null);
            }
        }

        // Cuerpo
        cuerpoFuncion.generarC3D(contexto);

        // Return implícito si es void y no tiene return explícito
        if (esVoid() && !tieneReturnExplicito(cuerpoFuncion)) {
            contexto.agregar("return", null, null, null);
        }

        contexto.agregarEtiqueta("end_" + nombreFuncion);

        return null;
    }

    private boolean esVoid() {
        return tipoRetorno == null
                || "void".equals(tipoRetorno.getNombre())
                || tipoRetorno.getNombre() == null;
    }

    private boolean tieneReturnExplicito(Bloque bloque) {
        if (bloque == null || bloque.getSentencias() == null) return false;
        for (Sentencia s : bloque.getSentencias()) {
            if (s instanceof SentenciaReturn) return true;
        }
        return false;
    }
}
