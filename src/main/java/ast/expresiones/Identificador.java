package ast.expresiones;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Identificador extends Expresion {

    private String nombreIdentificador;

    public Identificador(int linea, int columna, String nombreIdentificador) {
        super(linea, columna);
        this.nombreIdentificador = nombreIdentificador;
    }
}
