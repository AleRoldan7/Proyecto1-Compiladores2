package tablas;

import ast.declaraciones.Parametro;
import ast.tipos.Tipo;

import java.util.List;

public record MetodoRecord(String nombre, Tipo tipoRetorno, List<Parametro> parametros) {
}
