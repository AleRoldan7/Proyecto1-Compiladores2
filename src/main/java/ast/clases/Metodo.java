package ast.clases;

import ast.NodoAST;
import ast.declaraciones.Parametro;
import ast.sentencias.Bloque;
import ast.tipos.Tipo;
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
}
