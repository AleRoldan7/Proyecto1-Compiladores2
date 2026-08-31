package ast.expresiones;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Literal extends Expresion {

    private Object valor;
    private String tipo;

    public Literal(int linea, int columna, Object valor, String tipo) {
        super(linea, columna);
        this.valor = valor;
        this.tipo = tipo;
    }
}
