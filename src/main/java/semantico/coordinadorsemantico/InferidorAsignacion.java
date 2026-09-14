package semantico.coordinadorsemantico;

import ast.sentencias.Asignacion;
import ast.tipos.Tipo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import semantico.AnalisisContexto;
import semantico.Tipos;
import semantico.interfazsemantica.InferirTipo;

@Getter
@Setter
@AllArgsConstructor
public class InferidorAsignacion implements InferirTipo<Asignacion> {

    private final InferirTipoCoordinador inferirTipoCoordinador;

    @Override
    public Tipo inferir(Asignacion nodoAsignacion, AnalisisContexto analisisContexto) {

        Tipo destino = inferirTipoCoordinador.inferir(nodoAsignacion.getDestino(), analisisContexto);
        Tipo valor = inferirTipoCoordinador.inferir(nodoAsignacion.getValor(), analisisContexto);

        if (!Tipos.sonCompatibles(destino, valor)) {

            analisisContexto.reportarError(nodoAsignacion.getLinea(), nodoAsignacion.getColumna(),
                    "No se puede asignar un valor de tipo " + Tipos.describir(valor) + " a una variable tipo " + Tipos.describir(destino));
        }
        return destino;
    }
}
