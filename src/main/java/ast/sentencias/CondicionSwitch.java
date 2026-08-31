package ast.sentencias;

import ast.expresiones.Expresion;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CondicionSwitch extends Sentencia {

    private Expresion expresion;
    private List<SentenciaCase> casos;
    private Bloque bloqueDefecto;

    public CondicionSwitch(int linea, int columna, Expresion expresion, List<SentenciaCase> casos, Bloque bloqueDefecto) {
        super(linea, columna);
        this.expresion = expresion;
        this.casos = casos;
        this.bloqueDefecto = bloqueDefecto;
    }
}
