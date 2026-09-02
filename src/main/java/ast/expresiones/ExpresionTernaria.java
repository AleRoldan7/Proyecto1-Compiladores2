package ast.expresiones;

import c3d.ContextoC3D;
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

    @Override
    public void generarC3D(ContextoC3D contexto) {

    }
}
