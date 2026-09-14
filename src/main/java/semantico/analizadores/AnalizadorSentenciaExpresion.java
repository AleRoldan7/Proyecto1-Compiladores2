package semantico.analizadores;

import ast.sentencias.SentenciaExpresion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import semantico.AnalisisContexto;
import semantico.coordinadorsemantico.InferirTipoCoordinador;
import semantico.interfazsemantica.AnalizadorSemantico;

@Getter
@Setter
@AllArgsConstructor
public class AnalizadorSentenciaExpresion implements AnalizadorSemantico<SentenciaExpresion> {

    private final InferirTipoCoordinador inferirTipoCoordinador;

    @Override
    public void analizar(SentenciaExpresion nodoSentencia, AnalisisContexto analisisContexto) {
        inferirTipoCoordinador.inferir(nodoSentencia.getExpresion(), analisisContexto);
    }
}
