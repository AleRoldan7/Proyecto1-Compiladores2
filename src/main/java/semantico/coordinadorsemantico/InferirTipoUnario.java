package semantico.coordinadorsemantico;

import ast.expresiones.ExpresionUnaria;
import ast.tipos.Tipo;
import enums.TipoDato;
import lombok.AllArgsConstructor;
import lombok.Getter;
import semantico.AnalisisContexto;
import semantico.Tipos;
import semantico.interfazsemantica.InferirTipo;

import java.util.Set;

@Getter
@AllArgsConstructor
public class InferirTipoUnario implements InferirTipo<ExpresionUnaria> {

    private static final Set<String> INCREMENTO = Set.of("++", "--");

    private final InferirTipoCoordinador inferirTipoCoordinador;

    @Override
    public Tipo inferir(ExpresionUnaria nodoUnario, AnalisisContexto analisisContexto) {

        int linea = nodoUnario.getLinea();
        int columna = nodoUnario.getColumna();
        String operador = nodoUnario.getOperador();

        Tipo tipoOperando = inferirTipoCoordinador.inferir(nodoUnario.getExpresion(), analisisContexto);

        if ("!".equals(operador)) {

            if (tipoOperando != null && !Tipos.esBooleano(tipoOperando, analisisContexto)) {
                analisisContexto.reportarError(linea, columna,
                        "El operador '!' requiere un operando booleano, se encontró "
                                + Tipos.describir(tipoOperando, analisisContexto));
            }

            return Tipos.simple(linea, columna,
                    analisisContexto.getDialecto().nombrarTipo(TipoDato.BOOLEANO));
        }

        if ("-".equals(operador) || "+".equals(operador) || INCREMENTO.contains(operador)) {

            if (tipoOperando == null) {
                return null;
            }

            if (!Tipos.esNumerico(tipoOperando, analisisContexto)) {
                analisisContexto.reportarError(linea, columna,
                        "El operador '" + operador + "' requiere un operando numérico, se encontró "
                                + Tipos.describir(tipoOperando, analisisContexto));
            }

            return tipoOperando;
        }

        analisisContexto.reportarError(linea, columna, "Operador unario no reconocido: '" + operador + "'");

        return null;
    }
}