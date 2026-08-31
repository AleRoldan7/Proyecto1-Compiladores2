package ast.declaraciones;

import ast.NodoAST;
import ast.tipos.Tipo;
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
}
