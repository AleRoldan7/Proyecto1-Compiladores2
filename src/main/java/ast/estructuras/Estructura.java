package ast.estructuras;

import ast.NodoAST;
import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Estructura extends NodoAST {

    private String nombre;
    private List<Campo> campos;


    public Estructura(int linea, int columna, String nombre, List<Campo> campos) {

        super(linea, columna);

        this.nombre = nombre;
        this.campos = campos;
    }

    @Override
    public void generarC3D(ContextoC3D contexto) {

    }
}
