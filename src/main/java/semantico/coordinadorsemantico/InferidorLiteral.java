package semantico.coordinadorsemantico;

import ast.expresiones.Literal;
import ast.tipos.Tipo;
import semantico.AnalisisContexto;
import semantico.Tipos;
import semantico.interfazsemantica.InferirTipo;

public class InferidorLiteral implements InferirTipo<Literal> {

    @Override
    public Tipo inferir(Literal nodoLiteral, AnalisisContexto analisisContexto) {
        return Tipos.simple(nodoLiteral.getLinea(), nodoLiteral.getColumna(), nodoLiteral.getTipo());
    }
}
