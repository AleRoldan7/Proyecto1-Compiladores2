package ast.expresiones;

import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CrearObjeto extends Expresion {

    private String nombreClase;
    private List<Expresion> argumentos;

    public CrearObjeto(int linea, int columna, String nombreClase, List<Expresion> argumentos) {
        super(linea, columna);
        this.nombreClase = nombreClase;
        this.argumentos = argumentos;
    }

    @Override
    public void generarC3D(ContextoC3D contexto) {

    }
}
