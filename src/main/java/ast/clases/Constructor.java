package ast.clases;

import ast.NodoAST;
import ast.declaraciones.Parametro;
import ast.sentencias.Bloque;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Constructor extends NodoAST {

    private String nombreClase;
    private List<Parametro> parametros;
    private Bloque cuerpoConstructor;

    public Constructor(int linea, int columna, String nombreClase, List<Parametro> parametros, Bloque cuerpoConstructor) {
        super(linea, columna);
        this.nombreClase = nombreClase;
        this.parametros = parametros;
        this.cuerpoConstructor = cuerpoConstructor;
    }
}
