package semantico.analizadores;

import ast.expresiones.Expresion;
import ast.sentencias.CondicionIf;
import ast.tipos.Tipo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import semantico.AnalisisContexto;
import semantico.Tipos;
import semantico.coordinadorsemantico.AnalizadorSemanticoCoordinador;
import semantico.coordinadorsemantico.InferirTipoCoordinador;
import semantico.interfazsemantica.AnalizadorSemantico;

@Getter
@Setter
@AllArgsConstructor
public class AnalizadorCondicional implements AnalizadorSemantico<CondicionIf> {

    private final AnalizadorSemanticoCoordinador analizadorSemanticoCoordinador;
    private final InferirTipoCoordinador inferirTipoCoordinador;


    @Override
    public void analizar(CondicionIf nodoIf, AnalisisContexto analisisContexto) {

        validarCondicionBooleana(nodoIf.getCondicion(), analisisContexto, "if");

        analizadorSemanticoCoordinador.analizar(nodoIf.getBloqueEntonces(), analisisContexto);

        for (CondicionIf condicionIf : nodoIf.getListaSiNoSi()) {
            analizadorSemanticoCoordinador.analizar(condicionIf, analisisContexto);
        }

        if (nodoIf.getBloqueSiNo() != null) {
            analizadorSemanticoCoordinador.analizar(nodoIf.getBloqueSiNo(), analisisContexto);
        }
    }

    private void validarCondicionBooleana(Expresion condicion, AnalisisContexto analisisContexto, String etiqueta) {

        Tipo tipoCondicion = inferirTipoCoordinador.inferir(condicion, analisisContexto);

        if (!Tipos.esBooleano(tipoCondicion, analisisContexto)) {

            analisisContexto.reportarError(condicion.getLinea(), condicion.getColumna(), "La condición del " + etiqueta + " debe ser boolean, se encontró "
                    + Tipos.describir(tipoCondicion, analisisContexto));
        }
    }
}
