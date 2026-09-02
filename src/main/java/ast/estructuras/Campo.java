package ast.estructuras;

import ast.NodoAST;
import ast.tipos.Tipo;
import c3d.ContextoC3D;
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

    @Override
    public void generarC3D(ContextoC3D contexto) {

    }
}
