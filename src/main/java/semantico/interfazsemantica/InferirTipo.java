package semantico.interfazsemantica;

import ast.expresiones.Expresion;
import ast.tipos.Tipo;
import semantico.AnalisisContexto;

public interface InferirTipo<T extends Expresion> {

    Tipo inferir(T nodo , AnalisisContexto analisisContexto);
}
