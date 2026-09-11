package semantico.analizadores;

import ast.clases.Atributo;
import ast.expresiones.Expresion;
import ast.tipos.Tipo;
import enums.Categoria;
import enums.TipoErrorSemantico;
import lombok.AllArgsConstructor;
import semantico.AnalisisContexto;
import semantico.Tipos;
import semantico.coordinadorsemantico.InferirTipoCoordinador;
import semantico.interfazsemantica.AnalizadorSemantico;

@AllArgsConstructor
public class AnalizadorAtributo implements AnalizadorSemantico<Atributo> {

    private final InferirTipoCoordinador inferirTipoCoordinador;

    @Override
    public void analizar(Atributo nodo, AnalisisContexto contexto) {

        String nombre = nodo.getNombreAtributo();

        if (contexto.getTablaSimbolos().existeEnAmbitoActual(nombre)) {

            contexto.reportarError(nodo.getLinea(), nodo.getColumna(), "El atributo '" + nombre + "' ya fue declarado en esta clase");

        } else {

            contexto.getTablaSimbolos().declarar(nombre, Categoria.ATRIBUTO, nodo.getTipo().getNombre(), "", nodo.getLinea());
        }

        if (contexto.getClaseActual() != null) {
            contexto.getClaseActual().agregarAtributo(nombre, nodo.getTipo());
        }

        /*
         * Validar compatibilidad de la inicialización, si la hay.
         *
         * Caso tipo simple: private int contador = 5 + 3;
         */
        Expresion inicializacion = nodo.getInicializacion();

        if (inicializacion != null) {

            Tipo tipoInicial = inferirTipoCoordinador.inferir(inicializacion, contexto);

            if (!Tipos.sonCompatibles(nodo.getTipo(), tipoInicial)) {

                contexto.reportarError(nodo.getLinea(), nodo.getColumna(), "No se puede inicializar '" + nombre + "' de tipo "
                        + Tipos.describir(nodo.getTipo()) + " con un valor de tipo " + Tipos.describir(tipoInicial));
            }
        }

        /*
         * Caso arreglo: private int[] numeros = {1, 2, 3};
         *
         * Cada elemento de valoresIniciales debe ser compatible con el
         * tipo base (mismo nombre, sin el flag de arreglo).
         */
        if (nodo.getValoresIniciales() != null) {

            Tipo tipoBase = Tipos.simple(
                    nodo.getLinea(), nodo.getColumna(), nodo.getTipo().getNombre());

            for (Expresion elemento : nodo.getValoresIniciales()) {

                Tipo tipoElemento = inferirTipoCoordinador.inferir(elemento, contexto);

                if (!Tipos.sonCompatibles(tipoBase, tipoElemento)) {

                    contexto.reportarError(elemento.getLinea(), elemento.getColumna(), "Elemento de tipo " + Tipos.describir(tipoElemento)
                            + " no es compatible con el arreglo de tipo " + Tipos.describir(nodo.getTipo()));
                }
            }
        }
    }
}
