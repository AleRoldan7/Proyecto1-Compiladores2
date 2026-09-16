package semantico.coordinadorsemantico;

import ast.expresiones.CrearObjeto;
import ast.expresiones.Expresion;
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
public class InferidorCrearObjeto implements InferirTipo<CrearObjeto> {

    private final InferirTipoCoordinador inferirTipoCoordinador;

    @Override
    public Tipo inferir(CrearObjeto nodo, AnalisisContexto contexto) {

        List<Tipo> tiposArgumentos = new ArrayList<>();

        for (Expresion argumento : nodo.getArgumentos()) {
            tiposArgumentos.add(inferirTipoCoordinador.inferir(argumento, contexto));
        }

        InformeTipo tipoClase = contexto.getTablaTipos().obtener(nodo.getNombreClase());

        if (tipoClase == null) {

            contexto.reportarError(nodo.getLinea(), nodo.getColumna(),
                    "La clase '" + nodo.getNombreClase() + "' no está definida");
            return null;
        }

        MetodoRecord constructor = Tipos.resolverSobrecarga(tipoClase.getConstructores(), tiposArgumentos);

        if (constructor == null && !tipoClase.getConstructores().isEmpty()) {

            contexto.reportarError(nodo.getLinea(), nodo.getColumna(),
                    "No existe un constructor de '" + nodo.getNombreClase() + "' que reciba esos argumentos");
        }

        return Tipos.simple(nodo.getLinea(), nodo.getColumna(), nodo.getNombreClase());
    }
}