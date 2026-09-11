package semantico.coordinadorsemantico;

import ast.NodoAST;
import ast.clases.Atributo;
import ast.clases.Clase;
import semantico.AnalisisContexto;
import semantico.analizadores.AnalizadorAtributo;
import semantico.analizadores.AnalizadorClase;
import semantico.interfazsemantica.AnalizadorSemantico;

import java.util.HashMap;
import java.util.Map;

public class AnalizadorSemanticoCoordinador {


    private final Map<Class<? extends NodoAST>, AnalizadorSemantico<? extends NodoAST>> analizadores = new HashMap<>();

    public AnalizadorSemanticoCoordinador(InferirTipoCoordinador inferirTipoCoordinador) {

        registrar(Clase.class, new AnalizadorClase(this));
        registrar(Atributo.class, new AnalizadorAtributo(inferirTipoCoordinador));
    }

    private <T extends NodoAST> void registrar(Class<T> tipo, AnalizadorSemantico<T> analizador) {
        analizadores.put(tipo, analizador);
    }

    @SuppressWarnings("unchecked")
    public void analizar(NodoAST nodo, AnalisisContexto contexto) {

        if (nodo == null) {
            return;
        }

        AnalizadorSemantico<NodoAST> analizador = (AnalizadorSemantico<NodoAST>) analizadores.get(nodo.getClass());

        if (analizador == null) {
            throw new IllegalStateException("No hay AnalizadorSemantico registrado para " + nodo.getClass().getSimpleName());
        }

        analizador.analizar(nodo, contexto);
    }
}
