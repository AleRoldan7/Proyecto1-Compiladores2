package semantico.analizadores;

import ast.clases.Constructor;
import ast.declaraciones.Parametro;
import enums.Categoria;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import semantico.AnalisisContexto;
import semantico.Tipos;
import semantico.coordinadorsemantico.AnalizadorSemanticoCoordinador;
import semantico.interfazsemantica.AnalizadorSemantico;

@Getter
@Setter
@AllArgsConstructor
public class AnalizadorConstructor implements AnalizadorSemantico<Constructor> {

    private final AnalizadorSemanticoCoordinador analizadorSemanticoCoordinador;

    @Override
    public void analizar(Constructor nodoConstructor, AnalisisContexto analisisContexto) {

        analisisContexto.getTablaSimbolos().entrarAmbito("Constructor");

        for (Parametro parametro : nodoConstructor.getParametros()) {

            if (analisisContexto.getTablaSimbolos().existeEnAmbitoActual(parametro.getNombreParametro())) {

                analisisContexto.reportarError(nodoConstructor.getLinea(), nodoConstructor.getColumna(), "El parametro '" +
                        parametro.getNombreParametro() + "' esta duplicado");

            } else {

                analisisContexto.getTablaSimbolos().declarar(parametro.getNombreParametro(), Categoria.PARAMETRO, parametro.getTipoParametro().getNombre(),
                        "", nodoConstructor.getLinea());
            }
        }

        var tipoRetornoAntes = analisisContexto.getTipoRetorno();

        analisisContexto.setTipoRetorno(Tipos.simple(nodoConstructor.getLinea(), nodoConstructor.getColumna(), Tipos.VOID));

        analizadorSemanticoCoordinador.analizar(nodoConstructor.getCuerpoConstructor(), analisisContexto);

        analisisContexto.setTipoRetorno(tipoRetornoAntes);

        analisisContexto.getTablaSimbolos().salirAmbito();
    }
}
