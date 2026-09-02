package ast.tipos;

import ast.NodoAST;
import c3d.ContextoC3D;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Tipo extends NodoAST {

    private String nombre;
    private boolean arreglo;
    private int dimensiones;


    public Tipo(int linea, int columna, String nombre, boolean arreglo, int dimensiones) {
        super(linea, columna);

        this.nombre = nombre;
        this.arreglo = arreglo;
        this.dimensiones = dimensiones;
    }

    @Override
    public void generarC3D(ContextoC3D contexto) {

    }
}
