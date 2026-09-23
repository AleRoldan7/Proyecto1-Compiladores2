package semantico.coordinadorsemantico;

import ast.expresiones.Expresion;
import ast.expresiones.LlamadaFuncion;
import ast.tipos.Tipo;
import enums.TipoDato;
import lombok.AllArgsConstructor;
import semantico.AnalisisContexto;
import semantico.Tipos;
import semantico.interfazsemantica.InferirTipo;
import tablas.InformeTipo;
import tablas.MetodoRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class InferidorLlamadaFuncion implements InferirTipo<LlamadaFuncion> {

    private final InferirTipoCoordinador inferirTipoCoordinador;

    @Override
    public Tipo inferir(LlamadaFuncion nodoLlamada, AnalisisContexto analisisContexto) {

        // FIX: guarda defensiva contra argumentos null
        List<Expresion> argumentos = nodoLlamada.getArgumentos() == null ? Collections.emptyList() : nodoLlamada.getArgumentos();

        List<Tipo> tiposArgumentos = new ArrayList<>();

        for (Expresion argumento : argumentos) {
            tiposArgumentos.add(inferirTipoCoordinador.inferir(argumento, analisisContexto));
        }

        /*
         * FIX: usar el dialecto para nombrar los tipos de retorno.
         * Antes se devolvía Tipos.STRING ("String"), lo cual rompía
         * en Pig Latin donde el tipo se llama "textum".
         */
        String nombreTexto = analisisContexto.getDialecto().nombrarTipo(TipoDato.TEXTO);

        if ("readln".equals(nodoLlamada.getNombre())) {
            return Tipos.simple(nodoLlamada.getLinea(), nodoLlamada.getColumna(), nombreTexto);
        }

        if ("leer".equals(nodoLlamada.getNombre())) {
            return Tipos.simple(nodoLlamada.getLinea(), nodoLlamada.getColumna(), nombreTexto);
        }

        if ("print".equals(nodoLlamada.getNombre()) || "println".equals(nodoLlamada.getNombre()) || "imprimir".equals(nodoLlamada.getNombre())) {

            // FIX: si el dialecto no tiene NIHIL (Pig Latin), devolver null
            //      (sin tipo de retorno). El llamador debe tolerar null.

            return Tipos.simple(nodoLlamada.getLinea(), nodoLlamada.getColumna(), Tipos.VOID);
        }

        InformeTipo claseActual = analisisContexto.getClaseActual();

        if (claseActual == null || !claseActual.tieneMetodo(nodoLlamada.getNombre())) {

            analisisContexto.reportarError(nodoLlamada.getLinea(), nodoLlamada.getColumna(),
                    "Método '" + nodoLlamada.getNombre() + "' no declarado en la clase");
            return null;
        }

        MetodoRecord firma = Tipos.resolverSobrecarga(
                claseActual.firmasDe(nodoLlamada.getNombre()),
                tiposArgumentos);

        if (firma == null) {

            analisisContexto.reportarError(nodoLlamada.getLinea(), nodoLlamada.getColumna(),
                    "No existe una versión de '" + nodoLlamada.getNombre() + "' que reciba esos argumentos");
            return null;
        }

        return firma.tipoRetorno();
    }
}