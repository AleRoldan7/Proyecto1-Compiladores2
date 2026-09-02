package ast.expresiones;

import c3d.ContextoC3D;
import enums.TipoDato;
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

    @Override
    public void generarC3D(ContextoC3D contexto) {
        izquierda.generarC3D(contexto);
        derecha.generarC3D(contexto);

        String temporal = contexto.nuevoTemporal();
        contexto.agregarConTipo(operacion, izquierda.getResultado(), derecha.getResultado(), temporal, TipoDato.ENTERO); // parche temporal
        this.resultado = temporal;
    }
}
