package ast.expresiones;

import c3d.ContextoC3D;
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

    @Override
    public void generarC3D(ContextoC3D contexto) {
        this.resultado = String.valueOf(valor);
    }
}
