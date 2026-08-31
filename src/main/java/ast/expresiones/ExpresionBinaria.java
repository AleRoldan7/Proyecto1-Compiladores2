package ast.expresiones;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpresionBinaria extends Expresion {


    private Expresion izquierda;
    private String operacion;
    private Expresion derecha;

    public ExpresionBinaria(int linea, int columna, Expresion izquierda, String operacion, Expresion derecha) {
        super(linea, columna);
        this.izquierda = izquierda;
        this.operacion = operacion;
        this.derecha = derecha;
    }
}
