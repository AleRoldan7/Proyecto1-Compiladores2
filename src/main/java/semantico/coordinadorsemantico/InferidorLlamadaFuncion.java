package semantico.coordinadorsemantico;

import ast.expresiones.Expresion;
import ast.expresiones.LlamadaFuncion;
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
public class InferidorLlamadaFuncion implements InferirTipo<LlamadaFuncion> {

    private final InferirTipoCoordinador inferirTipoCoordinador;

    @Override
    public Tipo inferir(LlamadaFuncion nodoLlamada, AnalisisContexto analisisContexto) {

        List<Tipo> tiposArgumentos = new ArrayList<>();

        for (Expresion argumento : nodoLlamada.getArgumentos()) {
            tiposArgumentos.add(inferirTipoCoordinador.inferir(argumento, analisisContexto));
        }

        if ("readln".equals(nodoLlamada.getNombre())) {
            return Tipos.simple(nodoLlamada.getLinea(), nodoLlamada.getColumna(), Tipos.STRING);
        }

        if ("print".equals(nodoLlamada.getNombre()) || "println".equals(nodoLlamada.getNombre())) {
            return Tipos.simple(nodoLlamada.getLinea(), nodoLlamada.getColumna(), Tipos.VOID);
        }

        if ("imprimir".equals(nodoLlamada.getNombre())) {
            return Tipos.simple(nodoLlamada.getLinea(), nodoLlamada.getColumna(), Tipos.VOID);
        }

        if ("leer".equals(nodoLlamada.getNombre())) {
            return Tipos.simple(nodoLlamada.getLinea(), nodoLlamada.getColumna(), Tipos.STRING);
        }

        InformeTipo claseActual = analisisContexto.getClaseActual();

        if (claseActual == null || !claseActual.tieneMetodo(nodoLlamada.getNombre())) {

            analisisContexto.reportarError(nodoLlamada.getLinea(), nodoLlamada.getColumna(),
                    "Método '" + nodoLlamada.getNombre() + "' no declarado en la clase");
            return null;
        }

        MetodoRecord firma = Tipos.resolverSobrecarga(claseActual.firmasDe(nodoLlamada.getNombre()), tiposArgumentos);

        if (firma == null) {

            analisisContexto.reportarError(nodoLlamada.getLinea(), nodoLlamada.getColumna(),
                    "No existe una versión de '" + nodoLlamada.getNombre() + "' que reciba esos argumentos");
            return null;
        }

        return firma.tipoRetorno();
    }
}
