package ast.sentencias;

import ast.expresiones.Expresion;
import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CicloFor extends Sentencia {

    private Sentencia inicializacion;
    private Expresion condicionFor;
    private Expresion incremento;
    private Bloque bloqueFor;

    public CicloFor(int linea, int columna, Sentencia inicializacion, Expresion condicionFor, Expresion incremento, Bloque bloqueFor) {
        super(linea, columna);
        this.inicializacion = inicializacion;
        this.condicionFor = condicionFor;
        this.incremento = incremento;
        this.bloqueFor = bloqueFor;
    }

    @Override
    public void generarC3D(ContextoC3D contexto) {

    }
}
