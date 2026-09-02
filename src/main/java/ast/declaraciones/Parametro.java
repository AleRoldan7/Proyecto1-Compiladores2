package ast.declaraciones;

import ast.NodoAST;
import ast.tipos.Tipo;
import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Parametro extends NodoAST {

    private Tipo tipoParametro;
    private String nombreParametro;
    private boolean referencia;
    private boolean arreglo;

    public Parametro(int linea, int columna, Tipo tipoParametro, String nombreParametro, boolean referencia, boolean arreglo) {
        super(linea, columna);
        this.tipoParametro = tipoParametro;
        this.nombreParametro = nombreParametro;
        this.referencia = referencia;
        this.arreglo = arreglo;
    }

    @Override
    public void generarC3D(ContextoC3D contexto) {

    }
}
