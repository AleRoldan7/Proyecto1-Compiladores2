package ast.sentencias;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Bloque extends Sentencia {

    private List<Sentencia> sentencias;

    public Bloque(int linea, int columna, List<Sentencia> sentencias) {
        super(linea, columna);
        this.sentencias = sentencias;
    }
}
