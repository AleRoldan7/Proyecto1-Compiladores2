package semantico.coordinadorsemantico;

import ast.expresiones.LlamadaFuncion;
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
        Tipo valor;


        if (esLectura(nodoAsignacion.getValor())) {
            valor = destino;
        } else {
            valor = inferirTipoCoordinador.inferir(nodoAsignacion.getValor(), analisisContexto);
        }

        if (destino == null || valor == null) {
            return destino;
        }

        if (!Tipos.asignable(destino, valor, analisisContexto)) {
            analisisContexto.reportarError(nodoAsignacion.getLinea(), nodoAsignacion.getColumna(),"No se puede asignar un valor de tipo "
                    + Tipos.describir(valor,   analisisContexto) + " a una variable de tipo " + Tipos.describir(destino, analisisContexto));
        }

        return destino;
    }


    private boolean esLectura(ast.expresiones.Expresion expresion) {
        return expresion instanceof LlamadaFuncion llamada && ("readln".equals(llamada.getNombre()) || "leer".equals(llamada.getNombre()));
    }
}
