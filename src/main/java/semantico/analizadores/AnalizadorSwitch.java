package semantico.analizadores;

import ast.sentencias.CondicionSwitch;
import ast.sentencias.SentenciaCase;
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
public class AnalizadorSwitch implements AnalizadorSemantico<CondicionSwitch> {

    private final AnalizadorSemanticoCoordinador analizadorSemanticoCoordinador;
    private final InferirTipoCoordinador inferirTipoCoordinador;

    @Override
    public void analizar(CondicionSwitch nodoSwitch, AnalisisContexto analisisContexto) {

        Tipo tipoSwitch = inferirTipoCoordinador.inferir(nodoSwitch.getExpresion(), analisisContexto);

        analisisContexto.entrarSwitch();

        for (SentenciaCase caso : nodoSwitch.getCasos()) {

            Tipo tipoCaso = inferirTipoCoordinador.inferir(caso.getValor(), analisisContexto);

            if (!Tipos.sonCompatibles(tipoSwitch, tipoCaso)) {

                analisisContexto.reportarError(caso.getValor().getLinea(), caso.getValor().getColumna(),
                        "El 'case' es de tipo " + Tipos.describir(tipoCaso) + ", no compatible con el switch de tipo " + Tipos.describir(tipoSwitch));
            }

            analizadorSemanticoCoordinador.analizar(caso.getCuerpoCase(), analisisContexto);
        }

        if (nodoSwitch.getBloqueDefecto() != null) {
            analizadorSemanticoCoordinador.analizar(nodoSwitch.getBloqueDefecto(), analisisContexto);
        }

        analisisContexto.salirSwitch();

    }
}
