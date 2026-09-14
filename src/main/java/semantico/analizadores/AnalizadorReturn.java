package semantico.analizadores;

import ast.sentencias.SentenciaReturn;
import ast.tipos.Tipo;
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
public class AnalizadorReturn implements AnalizadorSemantico<SentenciaReturn> {

    private final InferirTipoCoordinador inferirTipoCoordinador;

    @Override
    public void analizar(SentenciaReturn nodoReturn, AnalisisContexto analisisContexto) {

        Tipo tipoEsperado = analisisContexto.getTipoRetorno();

        if (tipoEsperado == null) {
            analisisContexto.reportarError(nodoReturn.getLinea(), nodoReturn.getColumna(),"'return' solo puede usarse dentro de un método");
            return;
        }

        boolean esVoid = Tipos.VOID.equals(tipoEsperado.getNombre());

        if (nodoReturn.getExpresionReturn() == null) {

            if (!esVoid) {

                analisisContexto.reportarError(nodoReturn.getLinea(), nodoReturn.getColumna(),"El método debe devolver un valor de tipo " +
                        Tipos.describir(tipoEsperado));
            }
            return;
        }

        if (esVoid) {

            analisisContexto.reportarError(nodoReturn.getLinea(), nodoReturn.getColumna(), "Un método void no puede devolver un valor");
            return;
        }

        Tipo tipoValor = inferirTipoCoordinador.inferir(nodoReturn.getExpresionReturn(), analisisContexto);

        if (!Tipos.sonCompatibles(tipoEsperado, tipoValor)) {

            analisisContexto.reportarError(nodoReturn.getLinea(), nodoReturn.getColumna(), "El método declara retornar " + Tipos.describir(tipoEsperado)
                    + " pero se devuelve " + Tipos.describir(tipoValor));
        }
    }
}
