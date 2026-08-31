package ast.expresiones;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AccesoArreglo extends Expresion {

    private Expresion arreglo;
    private List<Expresion> indicesArreglo;

    public AccesoArreglo(int linea, int columna, Expresion arreglo, List<Expresion> indicesArreglo) {
        super(linea, columna);
        this.arreglo = arreglo;
        this.indicesArreglo = indicesArreglo;
    }
}
