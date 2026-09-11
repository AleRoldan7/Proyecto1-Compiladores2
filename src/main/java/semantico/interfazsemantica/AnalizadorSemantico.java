package semantico.interfazsemantica;

import ast.NodoAST;
import semantico.AnalisisContexto;

public interface AnalizadorSemantico<T extends NodoAST> {

    void analizar(T nodo, AnalisisContexto analisisContexto);
}
