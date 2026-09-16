package semantico.coordinadorsemantico;

import ast.expresiones.AccesoAtributo;
import ast.tipos.Tipo;
import lombok.AllArgsConstructor;
import semantico.AnalisisContexto;
import semantico.Tipos;
import semantico.interfazsemantica.InferirTipo;
import tablas.InformeTipo;

@AllArgsConstructor
public class InferidorAccesoAtributo implements InferirTipo<AccesoAtributo> {

    private final InferirTipoCoordinador inferirTipoCoordinador;

    @Override
    public Tipo inferir(AccesoAtributo nodoAcceso, AnalisisContexto analisisContexto) {

        Tipo tipoObjeto = inferirTipoCoordinador.inferir(nodoAcceso.getObjeto(), analisisContexto);

        if (tipoObjeto == null || tipoObjeto.isArreglo()) {

            analisisContexto.reportarError(nodoAcceso.getLinea(), nodoAcceso.getColumna(),
                    "No se puede acceder al atributo '" + nodoAcceso.getAtributo() + "' de " + Tipos.describir(tipoObjeto));
            return null;
        }

        InformeTipo tipoClase = analisisContexto.getTablaTipos().obtener(tipoObjeto.getNombre());

        if (tipoClase == null || !tipoClase.tieneAtributo(nodoAcceso.getAtributo())) {

            analisisContexto.reportarError(nodoAcceso.getLinea(), nodoAcceso.getColumna(),
                    "'" + tipoObjeto.getNombre() + "' no tiene un atributo '" + nodoAcceso.getAtributo() + "'");
            return null;
        }

        return tipoClase.tipoDeAtributo(nodoAcceso.getAtributo());
    }
}
