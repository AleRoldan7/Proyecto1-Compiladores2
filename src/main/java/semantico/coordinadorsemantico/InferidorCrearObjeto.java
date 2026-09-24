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

        List<Expresion> argumentos = nodo.getArgumentos() == null
                ? List.of() : nodo.getArgumentos();

        List<Tipo> tiposArgumentos = new ArrayList<>();
        boolean argumentosValidos = true;

        for (Expresion argumento : argumentos) {
            Tipo tipo = inferirTipoCoordinador.inferir(argumento, contexto);
            if (tipo == null) {
                argumentosValidos = false;   // el error ya se reportó
            }
            tiposArgumentos.add(tipo);
        }

        InformeTipo tipoClase = contexto.getTablaTipos().obtener(nodo.getNombreClase());

        if (tipoClase == null) {
            contexto.reportarError(nodo.getLinea(), nodo.getColumna(),
                    "La clase '" + nodo.getNombreClase() + "' no está definida (¿falta el import?)");
            return null;
        }

        List<MetodoRecord> constructores = tipoClase.getConstructores() == null
                ? List.of() : tipoClase.getConstructores();

        if (argumentosValidos && !constructores.isEmpty()) {

            MetodoRecord constructor = Tipos.resolverSobrecarga(constructores, tiposArgumentos, contexto);

            if (constructor == null) {
                contexto.reportarError(nodo.getLinea(), nodo.getColumna(),
                        "No existe un constructor de '" + nodo.getNombreClase() + "' que reciba esos argumentos");
            }
        }

        return Tipos.simple(nodo.getLinea(), nodo.getColumna(), nodo.getNombreClase());
    }
}