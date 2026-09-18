package semantico.analizadores;

import ast.declaraciones.DeclaracionVariable;
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
public class AnalizadorDeclaracionVariable implements AnalizadorSemantico<DeclaracionVariable> {

    private final InferirTipoCoordinador inferirTipoCoordinador;

    @Override
    public void analizar(DeclaracionVariable nodoVariable, AnalisisContexto analisisContexto) {

        String nombre =  nodoVariable.getNombre();

        if (analisisContexto.getTablaSimbolos().existeEnAmbitoActual(nombre)) {

            analisisContexto.reportarError(nodoVariable.getLinea(), nodoVariable.getColumna(), "'" + nombre + "' ya fue declarado");

        } else {

            analisisContexto.getTablaSimbolos().declarar(nombre, Categoria.VARIABLE, Tipos.describir(nodoVariable.getTipo()), "", nodoVariable.getLinea());
        }

        Expresion inicializacion = nodoVariable.getInicializacion();

        if (inicializacion != null) {

            Tipo inicio = inferirTipoCoordinador.inferir(inicializacion, analisisContexto);

            if (!Tipos.asignable(nodoVariable.getTipo(), inicio, analisisContexto)) {

                analisisContexto.reportarError(nodoVariable.getLinea(), nodoVariable.getColumna(),  "No se puede inicializar '" + nombre + "' de tipo "
                        + Tipos.describir(nodoVariable.getTipo()) + " con un valor de tipo " + Tipos.describir(inicio));
            }
        }
    }
}
