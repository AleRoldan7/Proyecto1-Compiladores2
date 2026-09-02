package ast.expresiones;

import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpresionUnaria extends Expresion {

    private String operador;
    private Expresion expresion;
    private Boolean prefijo;

    public ExpresionUnaria(int linea, int columna, String operador, Expresion expresion, Boolean prefijo) {
        super(linea, columna);
        this.operador = operador;
        this.expresion = expresion;
        this.prefijo = prefijo;
    }

    @Override
    public void generarC3D(ContextoC3D contexto) {

    }
}
