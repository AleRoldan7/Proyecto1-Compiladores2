package ast.expresiones;

import c3d.ContextoC3D;
import enums.TipoDato;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpresionUnaria extends Expresion {

    private String operador;
    private Expresion expresion;
    private Boolean prefijo;

    public ExpresionUnaria(int linea, int columna, String operador, Expresion expresion, Boolean prefijo) {
        super(linea, columna);
        this.operador = operador;
        this.expresion = expresion;
        this.prefijo = prefijo;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {

        String operando = expresion.generarC3D(contexto);

        switch (operador) {

            case "-":
                return contexto.unaria("neg", operando, TipoDato.ENTERO);

            case "!":
                return contexto.unaria("not", operando, TipoDato.BOOLEANO);

            case "++": {
                String res = contexto.binaria("+", operando, "1", TipoDato.ENTERO);
                contexto.asignar(operando, res);
                return operando;
            }

            case "--": {
                String res = contexto.binaria("-", operando, "1", TipoDato.ENTERO);
                contexto.asignar(operando, res);
                return operando;
            }

            default:
                return operando;
        }
    }
}
