package ast.sentencias;

import ast.NodoAST;
import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Bloque extends NodoAST implements Sentencia {

    private List<Sentencia> sentencias;

    public Bloque(int linea, int columna, List<Sentencia> sentencias) {
        super(linea, columna);
        this.sentencias = sentencias;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {
        for (Sentencia s : sentencias) {
            s.generarC3D(contexto);
        }
        return null;
    }
}
