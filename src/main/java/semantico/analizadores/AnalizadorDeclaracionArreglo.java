package semantico.analizadores;

import ast.declaraciones.DeclaracionArreglo;
import ast.expresiones.Expresion;
import ast.tipos.Tipo;
import enums.Categoria;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import semantico.AnalisisContexto;
import semantico.Tipos;
import semantico.coordinadorsemantico.InferirTipoCoordinador;
import semantico.interfazsemantica.AnalizadorSemantico;

@Getter
@Setter
@AllArgsConstructor
public class AnalizadorDeclaracionArreglo implements AnalizadorSemantico<DeclaracionArreglo> {

    private final InferirTipoCoordinador inferirTipoCoordinador;

    @Override
    public void analizar(DeclaracionArreglo nodoArreglo, AnalisisContexto analisisContexto) {

        String nombre = nodoArreglo.getNombre();

        if (analisisContexto.getTablaSimbolos().existeEnAmbitoActual(nombre)) {

            analisisContexto.reportarError(nodoArreglo.getLinea(), nodoArreglo.getColumna(), "'" + nombre + "' ya fue declarado");

        } else {

            analisisContexto.getTablaSimbolos().declarar(nombre, Categoria.VARIABLE, Tipos.describir(nodoArreglo.getTipo()), "", nodoArreglo.getLinea());
        }

        if (nodoArreglo.getValorInicial() != null) {

            Tipo tipoBase = Tipos.simple(nodoArreglo.getLinea(), nodoArreglo.getColumna(), Tipos.base(nodoArreglo.getTipo()));

            for (Expresion elemento : nodoArreglo.getValorInicial()) {

                Tipo tipoElemento = inferirTipoCoordinador.inferir(elemento, analisisContexto);

                if (!Tipos.asignable(tipoBase, tipoElemento, analisisContexto)) {

                    analisisContexto.reportarError(elemento.getLinea(), elemento.getColumna(), "Elemento de tipo " +
                            Tipos.describir(tipoElemento) + " no es compatible  con el arreglo de tipo " + Tipos.describir(nodoArreglo.getTipo()));
                }
            }
        }

        if (nodoArreglo.getDimensiones() != null) {

            for (Expresion dimension : nodoArreglo.getDimensiones()) {

                if (dimension == null) {
                    continue;
                }

                Tipo tipoDimension = inferirTipoCoordinador.inferir(dimension, analisisContexto);

                if (tipoDimension != null && !Tipos.INT.equals(tipoDimension.getNombre())) {

                    analisisContexto.reportarError(dimension.getLinea(), dimension.getColumna(), "La dimensión del arreglo " +
                            "debe de ser un entero(int), se enontró " + Tipos.describir(tipoDimension));
                }
            }

        }
    }
}
