package ast.tipos;

import ast.NodoAST;
import c3d.ContextoC3D;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Tipo extends NodoAST {

    private String nombre;
    private boolean arreglo;
    private int dimensiones;
    private List<Integer> size;


    public Tipo(int linea, int columna, String nombre, boolean arreglo, int dimensiones) {
        super(linea, columna);
        this.nombre = nombre;
        this.arreglo = arreglo;
        this.dimensiones = dimensiones;
    }

    public Tipo(int linea, int columna, String nombre, boolean arreglo,
                int dimensiones, List<Integer> size) {
        super(linea, columna);

        this.nombre = nombre;
        this.arreglo = arreglo;
        this.dimensiones = dimensiones;
        this.size = size != null ? size : new ArrayList<>();
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {
        return null;
    }
}
