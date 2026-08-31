package ast.declaraciones;

import ast.expresiones.Expresion;
import ast.tipos.Tipo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeclaracionVariable extends Declaracion {

    private Tipo tipo;
    private String nombre;
    private Expresion inicializacion;


    public DeclaracionVariable(int linea, int columna, Tipo tipo, String nombre, Expresion inicializacion) {
        super(linea, columna);

        this.tipo = tipo;
        this.nombre = nombre;
        this.inicializacion = inicializacion;
    }
}
