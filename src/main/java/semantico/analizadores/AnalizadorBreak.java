package semantico.analizadores;

import ast.sentencias.SentenciaBreak;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import semantico.AnalisisContexto;
import semantico.interfazsemantica.AnalizadorSemantico;

@Getter
@Setter
@AllArgsConstructor
public class AnalizadorBreak implements AnalizadorSemantico<SentenciaBreak> {

    @Override
    public void analizar(SentenciaBreak nodoBreak, AnalisisContexto analisisContexto) {

        if (!analisisContexto.dentroDeCiclo() && !analisisContexto.dentroDeSwitch()) {
            analisisContexto.reportarError(nodoBreak.getLinea(), nodoBreak.getColumna(), "break solo se usa dentro de un ciclo o un switch");
        }
    }
}
