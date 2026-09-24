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

        List<Expresion> argumentos = nodoMetodo.getArgumentos() == null
                ? List.of() : nodoMetodo.getArgumentos();

        List<Tipo> tiposArgumentos = new ArrayList<>();
        boolean argumentosValidos = true;

        for (Expresion argumento : argumentos) {
            Tipo tipo = inferirTipoCoordinador.inferir(argumento, analisisContexto);
            if (tipo == null) {
                argumentosValidos = false;
            }
            tiposArgumentos.add(tipo);
        }

        // Si el objeto no tiene tipo, el error ya se reportó al inferirlo: no lo duplicamos
        if (tipoObjeto == null) {
            return null;
        }

        if (tipoObjeto.isArreglo()) {
            analisisContexto.reportarError(nodoMetodo.getLinea(), nodoMetodo.getColumna(),
                    "No se puede invocar '" + nodoMetodo.getMetodo() + "' sobre un arreglo de "
                            + Tipos.describir(tipoObjeto, analisisContexto));
            return null;
        }

        InformeTipo tipoClase = analisisContexto.getTablaTipos().obtener(tipoObjeto.getNombre());

        if (tipoClase == null) {
            analisisContexto.reportarError(nodoMetodo.getLinea(), nodoMetodo.getColumna(),
                    "'" + tipoObjeto.getNombre() + "' no es una clase declarada (¿falta el import?)");
            return null;
        }

        if (!tipoClase.tieneMetodo(nodoMetodo.getMetodo())) {
            analisisContexto.reportarError(nodoMetodo.getLinea(), nodoMetodo.getColumna(),
                    "'" + tipoObjeto.getNombre() + "' no tiene un método '" + nodoMetodo.getMetodo() + "'");
            return null;
        }

        if (!argumentosValidos) {
            return null;
        }

        MetodoRecord firma = Tipos.resolverSobrecarga(tipoClase.firmasDe(nodoMetodo.getMetodo()), tiposArgumentos, analisisContexto);

        if (firma == null) {
            analisisContexto.reportarError(nodoMetodo.getLinea(), nodoMetodo.getColumna(),
                    "No existe una versión de '" + nodoMetodo.getMetodo() + "' que reciba esos argumentos");
            return null;
        }

        Tipo retorno = firma.tipoRetorno();
        return retorno != null
                ? retorno
                : Tipos.simple(nodoMetodo.getLinea(), nodoMetodo.getColumna(), Tipos.VOID);
    }
}