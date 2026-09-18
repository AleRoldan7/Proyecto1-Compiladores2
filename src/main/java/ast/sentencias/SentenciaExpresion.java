package ast.sentencias;

import ast.NodoAST;
import ast.expresiones.Expresion;
import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SentenciaExpresion extends NodoAST implements Sentencia {

    private Expresion expresion;

    public SentenciaExpresion(int linea, int columna, Expresion expresion) {
        super(linea, columna);
        this.expresion = expresion;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {
        expresion.generarC3D(contexto);
        return null;
    }
}
