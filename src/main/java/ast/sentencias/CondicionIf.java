package ast.sentencias;

import ast.expresiones.Expresion;
import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CondicionIf extends Sentencia {

    private Expresion condicion;
    private Bloque bloqueEntonces;
    private List<CondicionIf> listaSiNoSi;
    private Bloque bloqueSiNo;

    public CondicionIf(int linea, int columna, Expresion condicion, Bloque bloqueEntonces, List<CondicionIf> listaSiNoSi, Bloque bloqueSiNo) {
        super(linea, columna);
        this.condicion = condicion;
        this.bloqueEntonces = bloqueEntonces;
        this.listaSiNoSi = listaSiNoSi;
        this.bloqueSiNo = bloqueSiNo;
    }

    @Override
    public void generarC3D(ContextoC3D contexto) {

    }
}
