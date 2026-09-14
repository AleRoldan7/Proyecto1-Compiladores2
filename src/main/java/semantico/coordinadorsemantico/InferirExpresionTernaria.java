package semantico.coordinadorsemantico;

import ast.expresiones.ExpresionTernaria;
import ast.tipos.Tipo;
import lombok.AllArgsConstructor;
import semantico.AnalisisContexto;
import semantico.Tipos;
import semantico.interfazsemantica.InferirTipo;

@AllArgsConstructor
public class InferirExpresionTernaria implements InferirTipo<ExpresionTernaria> {

    private final InferirTipoCoordinador inferirTipoCoordinador;

    @Override
    public Tipo inferir(ExpresionTernaria nodoTernario, AnalisisContexto analisisContexto) {

        Tipo tipoCondicion = inferirTipoCoordinador.inferir(nodoTernario.getCondicionTernaria(), analisisContexto);

        if (!Tipos.esBooleano(tipoCondicion)) {

            analisisContexto.reportarError(nodoTernario.getLinea(), nodoTernario.getColumna(),
                    "La condición del operador ternario debe ser boolean, se encontró " + Tipos.describir(tipoCondicion));
        }

        Tipo tipoVerdadero = inferirTipoCoordinador.inferir(nodoTernario.getVerdaderoTernaria(), analisisContexto);
        Tipo tipoFalso = inferirTipoCoordinador.inferir(nodoTernario.getFalsoTernaria(), analisisContexto);

        if (Tipos.sonCompatibles(tipoVerdadero, tipoFalso)) {
            return tipoVerdadero;
        }

        if (Tipos.sonCompatibles(tipoFalso, tipoVerdadero)) {
            return tipoFalso;
        }

        analisisContexto.reportarError(nodoTernario.getLinea(), nodoTernario.getColumna(), "Las ramas del operador ternario tienen tipos incompatibles: "
                + Tipos.describir(tipoVerdadero) + " y " + Tipos.describir(tipoFalso));

        return tipoVerdadero;
    }
}
