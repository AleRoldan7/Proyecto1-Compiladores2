package semantico.coordinadorsemantico;

import ast.expresiones.Expresion;
import ast.expresiones.LlamadaFuncion;
import ast.sentencias.Asignacion;
import ast.tipos.Tipo;
import lombok.AllArgsConstructor;
import semantico.AnalisisContexto;
import semantico.Tipos;
import semantico.interfazsemantica.InferirTipo;

@AllArgsConstructor
public class InferidorAsignacion implements InferirTipo<Asignacion> {

    private final InferirTipoCoordinador inferirTipoCoordinador;

    @Override
    public Tipo inferir(Asignacion nodoAsignacion, AnalisisContexto analisisContexto) {

        Tipo destino = inferirTipoCoordinador.inferir(nodoAsignacion.getDestino(), analisisContexto);

        // "x <<" lee del teclado y convierte al tipo del destino
        Tipo valor = esLectura(nodoAsignacion.getValor())
                ? destino
                : inferirTipoCoordinador.inferir(nodoAsignacion.getValor(), analisisContexto);

        // Si algo ya falló, el error está reportado: no encadenamos otro
        if (destino == null || valor == null) {
            return destino;
        }

        if (!Tipos.asignable(destino, valor, analisisContexto)) {
            analisisContexto.reportarError(nodoAsignacion.getLinea(), nodoAsignacion.getColumna(),
                    "No se puede asignar un valor de tipo " + Tipos.describir(valor, analisisContexto)
                            + " a una variable de tipo " + Tipos.describir(destino, analisisContexto));
        }

        return destino;
    }

    private boolean esLectura(Expresion expresion) {
        return expresion instanceof LlamadaFuncion llamada && InferidorLlamadaFuncion.esLectura(llamada.getNombre());
    }
}