package ast.expresiones;

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
}
