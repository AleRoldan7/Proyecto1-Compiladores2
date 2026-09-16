package semantico.coordinadorsemantico;

import ast.expresiones.Expresion;
import ast.expresiones.LlamadaMetodo;
import ast.tipos.Tipo;
import lombok.AllArgsConstructor;
import semantico.AnalisisContexto;
import semantico.Tipos;
import semantico.interfazsemantica.InferirTipo;
import tablas.InformeTipo;
import tablas.MetodoRecord;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class InferidorLlamadaMetodo implements InferirTipo<LlamadaMetodo> {

    private final InferirTipoCoordinador inferirTipoCoordinador;

    @Override
    public Tipo inferir(LlamadaMetodo nodoMetodo, AnalisisContexto analisisContexto) {

        Tipo tipoObjeto = inferirTipoCoordinador.inferir(nodoMetodo.getObjeto(), analisisContexto);

        List<Tipo> tiposArgumentos = new ArrayList<>();

        for (Expresion argumento : nodoMetodo.getArgumentos()) {
            tiposArgumentos.add(inferirTipoCoordinador.inferir(argumento, analisisContexto));
        }

        if (tipoObjeto == null || tipoObjeto.isArreglo()) {

            analisisContexto.reportarError(nodoMetodo.getLinea(), nodoMetodo.getColumna(),
                    "No se puede invocar '" + nodoMetodo.getMetodo() + "' sobre " + Tipos.describir(tipoObjeto));
            return null;
        }

        InformeTipo tipoClase = analisisContexto.getTablaTipos().obtener(tipoObjeto.getNombre());

        if (tipoClase == null || !tipoClase.tieneMetodo(nodoMetodo.getMetodo())) {

            analisisContexto.reportarError(nodoMetodo.getLinea(), nodoMetodo.getColumna(),
                    "'" + tipoObjeto.getNombre() + "' no tiene un método '" + nodoMetodo.getMetodo() + "'");
            return null;
        }

        MetodoRecord firma = Tipos.resolverSobrecarga(tipoClase.firmasDe(nodoMetodo.getMetodo()), tiposArgumentos);

        if (firma == null) {

            analisisContexto.reportarError(nodoMetodo.getLinea(), nodoMetodo.getColumna(),
                    "No existe una versión de '" + nodoMetodo.getMetodo() + "' que reciba esos argumentos");
            return null;
        }

        return firma.tipoRetorno();
    }
}
