package ast.declaraciones;

import ast.expresiones.Expresion;
import ast.tipos.Tipo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DeclaracionArreglo extends Declaracion {

    private Tipo tipo;
    private String nombre;
    private List<Expresion> dimensiones;
    private List<Expresion> valorInicial;

    public DeclaracionArreglo(int linea, int columna,  Tipo tipo,  String nombre, List<Expresion> dimensiones, List<Expresion> valorInicial) {
        super(linea, columna);

        this.tipo = tipo;
        this.nombre = nombre;
        this.dimensiones = dimensiones;
        this.valorInicial = valorInicial;
    }
}
