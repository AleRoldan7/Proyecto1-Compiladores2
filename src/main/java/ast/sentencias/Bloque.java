package ast.sentencias;

import c3d.ContextoC3D;
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

    @Override
    public void generarC3D(ContextoC3D contexto) {
        for (Sentencia sentencia : sentencias) {
            sentencia.generarC3D(contexto);
        }
    }
}
