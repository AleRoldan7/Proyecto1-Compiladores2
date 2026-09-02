package ast.sentencias;

import ast.expresiones.Expresion;
import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Asignacion extends Sentencia {

    private Expresion destino;
    private Expresion valor;

    public Asignacion(int linea, int columna, Expresion destino, Expresion valor) {
        super(linea, columna);
        this.destino = destino;
        this.valor = valor;
    }

    @Override
    public void generarC3D(ContextoC3D contexto) {

    }
}
