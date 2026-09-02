package ast.declaraciones;

import ast.sentencias.Bloque;
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
    public void generarC3D(ContextoC3D contexto) {
        cuerpoFuncion.generarC3D(contexto);
    }
}
