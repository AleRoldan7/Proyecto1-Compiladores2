package ast.expresiones;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpresionTernaria extends Expresion {

    private Expresion condicionTernaria;
    private Expresion verdaderoTernaria;
    private Expresion falsoTernaria;

    public ExpresionTernaria(int linea, int columna, Expresion condicionTernaria, Expresion verdaderoTernaria, Expresion falsoTernaria) {
        super(linea, columna);
        this.condicionTernaria = condicionTernaria;
        this.verdaderoTernaria = verdaderoTernaria;
        this.falsoTernaria = falsoTernaria;
    }
}
