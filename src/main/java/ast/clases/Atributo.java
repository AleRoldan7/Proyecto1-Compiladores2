package ast.clases;

import ast.NodoAST;
import ast.tipos.Tipo;
import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Atributo extends NodoAST {

    private Tipo tipo;
    private String nombreAtributo;

    public Atributo(int linea, int columna, Tipo tipo, String nombreAtributo) {
        super(linea, columna);
        this.tipo = tipo;
        this.nombreAtributo = nombreAtributo;
    }

    @Override
    public void generarC3D(ContextoC3D contexto) {

    }
}
