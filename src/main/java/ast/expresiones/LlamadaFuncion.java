package ast.expresiones;

import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LlamadaFuncion extends Expresion {

    private String nombre;
    private List<Expresion> argumentos;

    public LlamadaFuncion(int linea, int columna, String nombre, List<Expresion> argumentos) {
        super(linea, columna);
        this.nombre = nombre;
        this.argumentos = argumentos;
    }

    @Override
    public void generarC3D(ContextoC3D contexto) {

    }
}
