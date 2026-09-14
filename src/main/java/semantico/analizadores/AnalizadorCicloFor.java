package semantico.analizadores;

import ast.NodoAST;
import ast.sentencias.CicloFor;
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
public class AnalizadorCicloFor implements AnalizadorSemantico<CicloFor> {

    private final AnalizadorSemanticoCoordinador analizadorSemanticoCoordinador;
    private final InferirTipoCoordinador inferirTipoCoordinador;

    @Override
    public void analizar(CicloFor nodoFor, AnalisisContexto analisisContexto) {

        analisisContexto.getTablaSimbolos().entrarAmbito("for");

        if (nodoFor.getInicializacion() != null) {
            analizadorSemanticoCoordinador.analizar((NodoAST) nodoFor.getInicializacion(), analisisContexto);
        }

        if (nodoFor.getCondicionFor() != null) {

            Tipo tipoCondicion = inferirTipoCoordinador.inferir(nodoFor.getCondicionFor(), analisisContexto);

            if (!Tipos.esBooleano(tipoCondicion)) {

                analisisContexto.reportarError(nodoFor.getCondicionFor().getLinea(), nodoFor.getCondicionFor().getColumna(),
                        "La condición del for debe ser boolean, se encontró " + Tipos.describir(tipoCondicion));
            }
        }

        if (nodoFor.getIncremento() != null) {
            // Solo para validar que sea una expresión válida (variable
            // declarada, tipos consistentes, etc.); no se usa el tipo.
            inferirTipoCoordinador.inferir(nodoFor.getIncremento(), analisisContexto);
        }

        analisisContexto.entrarCiclo();

        analizadorSemanticoCoordinador.analizar(nodoFor.getBloqueFor(), analisisContexto);

        analisisContexto.salirCiclo();

        analisisContexto.getTablaSimbolos().salirAmbito();
    }
}
