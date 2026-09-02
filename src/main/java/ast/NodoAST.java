package ast;

import c3d.ContextoC3D;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public abstract class NodoAST {
    private int linea;
    private int columna;

    // Programa.java
    public abstract void generarC3D(ContextoC3D contexto);
}
