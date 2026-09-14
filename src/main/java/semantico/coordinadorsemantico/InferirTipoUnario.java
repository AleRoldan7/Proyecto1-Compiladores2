package semantico.coordinadorsemantico;

import ast.expresiones.ExpresionUnaria;
import ast.tipos.Tipo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import semantico.AnalisisContexto;
import semantico.Tipos;
import semantico.interfazsemantica.InferirTipo;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class InferirTipoUnario implements InferirTipo<ExpresionUnaria> {

    private static final Set<String> INCREMENTO = Set.of("++", "--");
    private final InferirTipoCoordinador inferirTipoCoordinador;

    @Override
    public Tipo inferir(ExpresionUnaria nodoUnario, AnalisisContexto analisisContexto) {

        Tipo tipoOperador = inferirTipoCoordinador.inferir(nodoUnario.getExpresion(), analisisContexto);
        String operador = nodoUnario.getOperador();

        if ("!".equals(operador)) {

            if (!Tipos.esBooleano(tipoOperador)) {

                analisisContexto.reportarError(nodoUnario.getLinea(), nodoUnario.getColumna(), "El operador '!' requiere un operando boolean, se encontró "
                        + Tipos.describir(tipoOperador));
            }

            return Tipos.booleano(nodoUnario.getLinea(), nodoUnario.getColumna());
        }

        if ("-".equals(operador) || "+".equals(operador) || INCREMENTO.contains(operador)) {

            if (!Tipos.esNumerico(tipoOperador)) {

                analisisContexto.reportarError(nodoUnario.getLinea(), nodoUnario.getColumna(), "El operador '" +
                        operador + "' requiere un operando numérico, se encontró " + Tipos.describir(tipoOperador));
            }

            return tipoOperador;
        }

        analisisContexto.reportarError(nodoUnario.getLinea(), nodoUnario.getColumna(), "Operador unario no reconocido: '" + operador + "'");

        return null;
    }
}
