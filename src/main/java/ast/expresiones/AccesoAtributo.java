package ast.expresiones;

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
}
