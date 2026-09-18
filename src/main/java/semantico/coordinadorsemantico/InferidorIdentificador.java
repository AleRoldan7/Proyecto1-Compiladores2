package semantico.coordinadorsemantico;

import ast.expresiones.Identificador;
import ast.tipos.Tipo;
import semantico.AnalisisContexto;
import semantico.Tipos;
import semantico.interfazsemantica.InferirTipo;
import tablas.FilaTabla;

public class InferidorIdentificador implements InferirTipo<Identificador> {

    @Override
    public Tipo inferir(Identificador nodoIdentificador, AnalisisContexto analisisContexto) {

        if ("this".equals(nodoIdentificador.getNombreIdentificador())) {

            if (analisisContexto.getClaseActual() == null) {

                analisisContexto.reportarError(nodoIdentificador.getLinea(), nodoIdentificador.getColumna(),
                        "'this' solo puede usarse dentro de una clase");
                return null;
            }

            return Tipos.simple(nodoIdentificador.getLinea(), nodoIdentificador.getColumna(),
                    analisisContexto.getClaseActual().getNombre());
        }

        FilaTabla filaTabla = analisisContexto.getTablaSimbolos().buscar(nodoIdentificador.getNombreIdentificador());

        if  (filaTabla == null) {

            analisisContexto.reportarError(nodoIdentificador.getLinea(), nodoIdentificador.getColumna(), "La variabre '" +
                    nodoIdentificador.getNombreIdentificador() + "' no ha sido declarada");

            return null;
        }
        System.out.println("IDENTIFICADOR: " + nodoIdentificador.getNombreIdentificador()
                + " -> tipoEnTabla=[" + filaTabla.getTipo() + "]");
        return Tipos.desdeTexto(nodoIdentificador.getLinea(), nodoIdentificador.getColumna(), filaTabla.getTipo());
    }
}