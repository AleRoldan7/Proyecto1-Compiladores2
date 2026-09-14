package semantico.analizadores;

import ast.sentencias.SentenciaContinue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import semantico.AnalisisContexto;
import semantico.interfazsemantica.AnalizadorSemantico;

@Getter
@Setter
@AllArgsConstructor
public class AnalizadorContinue implements AnalizadorSemantico<SentenciaContinue> {

    @Override
    public void analizar(SentenciaContinue nodoContinue, AnalisisContexto analisisContexto) {

        if (!analisisContexto.dentroDeCiclo()) {

            analisisContexto.reportarError(nodoContinue.getLinea(), nodoContinue.getColumna(), "continue solo se puede usar dentro de un ciclo");
        }
    }
}
