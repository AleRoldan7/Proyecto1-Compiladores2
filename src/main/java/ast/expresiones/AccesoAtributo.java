package ast.expresiones;

import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccesoAtributo extends Expresion {

    private Expresion objeto;
    private String atributo;

    public AccesoAtributo(int linea, int columna, Expresion objeto, String atributo) {
        super(linea, columna);
        this.objeto = objeto;
        this.atributo = atributo;
    }

    @Override
    public void generarC3D(ContextoC3D contexto) {

    }
}
