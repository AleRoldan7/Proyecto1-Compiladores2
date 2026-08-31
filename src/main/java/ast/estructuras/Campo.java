package ast.estructuras;

import ast.NodoAST;
import ast.tipos.Tipo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Campo extends NodoAST {

    private Tipo tipo;
    private String nombre;

    public Campo(int linea, int columna, Tipo tipo, String nombre) {
        super(linea, columna);
        this.tipo = tipo;
        this.nombre = nombre;
    }
}
