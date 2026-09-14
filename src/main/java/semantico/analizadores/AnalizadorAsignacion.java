package semantico.analizadores;

import ast.sentencias.Asignacion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import semantico.AnalisisContexto;
import semantico.coordinadorsemantico.InferirTipoCoordinador;
import semantico.interfazsemantica.AnalizadorSemantico;

@Getter
@Setter
@AllArgsConstructor
public class AnalizadorAsignacion implements AnalizadorSemantico<Asignacion> {

    private final InferirTipoCoordinador  inferirTipoCoordinador;

    @Override
    public void analizar(Asignacion nodoAsignacion, AnalisisContexto analisisContexto) {

        inferirTipoCoordinador.inferir(nodoAsignacion, analisisContexto);
    }
}
