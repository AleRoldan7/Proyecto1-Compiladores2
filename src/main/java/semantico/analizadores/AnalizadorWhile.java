package semantico.analizadores;

import ast.sentencias.CicloWhile;
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
public class AnalizadorWhile implements AnalizadorSemantico<CicloWhile> {

    private final AnalizadorSemanticoCoordinador analizadorSemanticoCoordinador;
    private final InferirTipoCoordinador  inferirTipoCoordinador;

    @Override
    public void analizar(CicloWhile nodoWhile, AnalisisContexto analisisContexto) {

        Tipo tipoCondicion = inferirTipoCoordinador.inferir(nodoWhile.getCondicionWhile(), analisisContexto);

        if (!Tipos.esBooleano(tipoCondicion)) {

            analisisContexto.reportarError(nodoWhile.getCondicionWhile().getLinea(), nodoWhile.getCondicionWhile().getColumna(),
                    "La condición del while debe ser boolean, se encontró " + Tipos.describir(tipoCondicion));
        }

        analisisContexto.entrarCiclo();

        analizadorSemanticoCoordinador.analizar(nodoWhile.getBloqueWhile(), analisisContexto);

        analisisContexto.salirCiclo();
    }

}
