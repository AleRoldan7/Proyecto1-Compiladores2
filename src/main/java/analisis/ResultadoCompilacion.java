package analisis;

import ast.NodoAST;
import semantico.AnalisisContexto;

public class ResultadoCompilacion {

    private final NodoAST ast;
    private final AnalisisContexto contexto;

    public ResultadoCompilacion(NodoAST ast, AnalisisContexto contexto) {
        this.ast = ast;
        this.contexto = contexto;
    }

    public NodoAST getAst() {
        return ast;
    }

    public AnalisisContexto getContexto() {
        return contexto;
    }
}