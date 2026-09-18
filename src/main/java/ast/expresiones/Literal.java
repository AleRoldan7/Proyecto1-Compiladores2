package ast.expresiones;

import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Literal extends Expresion {

    private Object valor;
    private String tipo;

    public Literal(int linea, int columna, Object valor, String tipo) {
        super(linea, columna);
        this.valor = valor;
        this.tipo = tipo;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {
        if (valor == null) {
            return "0";   // null → 0
        }

        if (valor instanceof String s) {
            // Registrar el string y devolver su nombre lógico
            return contexto.registrarString(s);
        }

        if (valor instanceof Boolean b) {
            return b ? "1" : "0";
        }

        return String.valueOf(valor);
    }
}
