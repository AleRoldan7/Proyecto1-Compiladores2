package ast.estructuras;

import ast.expresiones.Expresion;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InicializacionEstructura extends Expresion {

    private String nombreTipo;
    private List<Expresion> valores;

    public InicializacionEstructura(int linea, int columna, String nombreTipo, List<Expresion> valores) {
        super(linea, columna);
        this.nombreTipo = nombreTipo;
        this.valores = valores;
    }
}
