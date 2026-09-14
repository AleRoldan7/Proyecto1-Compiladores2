package semantico.analizadores;

import ast.clases.Metodo;
import ast.declaraciones.Parametro;
import enums.Categoria;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import semantico.AnalisisContexto;
import semantico.coordinadorsemantico.AnalizadorSemanticoCoordinador;
import semantico.interfazsemantica.AnalizadorSemantico;

@Getter
@Setter
@AllArgsConstructor
public class AnalizadorMetodo implements AnalizadorSemantico<Metodo> {

    private final AnalizadorSemanticoCoordinador analizadorSemanticoCoordinador;

    @Override
    public void analizar(Metodo nodoMetodo, AnalisisContexto analisisContexto) {

        analisisContexto.getTablaSimbolos().entrarAmbito("Metodo " + nodoMetodo.getNombreMetodo());

        for (Parametro parametro : nodoMetodo.getParametros()) {

            if (analisisContexto.getTablaSimbolos().existeEnAmbitoActual(parametro.getNombreParametro())) {

                analisisContexto.reportarError(nodoMetodo.getLinea(), nodoMetodo.getColumna(), "El parámetro '"
                        + parametro.getNombreParametro() + "' está duplicado");

            } else {

                analisisContexto.getTablaSimbolos().declarar(parametro.getNombreParametro(), Categoria.PARAMETRO, parametro.getTipoParametro().getNombre(),
                        "", nodoMetodo.getLinea());
            }
        }

        var tipoRetornoAnterior = analisisContexto.getTipoRetorno();

        analisisContexto.setTipoRetorno(nodoMetodo.getTipoRetorno());

        analizadorSemanticoCoordinador.analizar(nodoMetodo.getCuerpoMetodo(), analisisContexto);

        analisisContexto.setTipoRetorno(tipoRetornoAnterior);

        analisisContexto.getTablaSimbolos().salirAmbito();
    }
}
