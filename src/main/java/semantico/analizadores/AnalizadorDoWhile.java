package semantico.analizadores;

import ast.sentencias.CicloDoWhile;
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
public class AnalizadorDoWhile implements AnalizadorSemantico<CicloDoWhile> {

    private final AnalizadorSemanticoCoordinador analizadorSemanticoCoordinador;
    private final InferirTipoCoordinador inferirTipoCoordinador;

    @Override
    public void analizar(CicloDoWhile nodoDoWhile, AnalisisContexto analisisContexto) {

        analisisContexto.entrarCiclo();

        analizadorSemanticoCoordinador.analizar(nodoDoWhile.getBloqueDoWhile(), analisisContexto);

        analisisContexto.salirCiclo();

        Tipo tipoCondicon = inferirTipoCoordinador.inferir(nodoDoWhile.getExpresionDoWhile(), analisisContexto);

        if (!Tipos.esBooleano(tipoCondicon)) {

            analisisContexto.reportarError(nodoDoWhile.getExpresionDoWhile().getLinea(), nodoDoWhile.getExpresionDoWhile().getColumna(),
                    "La condición del ciclo do-while debe ser boolanea pero se encontro " + Tipos.describir(tipoCondicon));
        }
    }
}
