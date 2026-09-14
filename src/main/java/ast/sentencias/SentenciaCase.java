package ast.sentencias;

import ast.NodoAST;
import ast.expresiones.Expresion;
import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SentenciaCase extends NodoAST implements Sentencia {

    private Expresion valor;
    private Bloque cuerpoCase;

    public SentenciaCase(int linea, int columna, Expresion valor, Bloque cuerpoCase) {
        super(linea, columna);
        this.valor = valor;
        this.cuerpoCase = cuerpoCase;
    }

    @Override
    public void generarC3D(ContextoC3D contexto) {

    }
}
