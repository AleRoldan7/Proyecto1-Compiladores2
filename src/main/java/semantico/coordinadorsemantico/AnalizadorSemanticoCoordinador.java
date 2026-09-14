package semantico.coordinadorsemantico;

import ast.NodoAST;
import ast.clases.Atributo;
import ast.clases.Clase;
import ast.declaraciones.DeclaracionArreglo;
import ast.declaraciones.DeclaracionVariable;
import ast.sentencias.*;
import semantico.AnalisisContexto;
import semantico.analizadores.*;
import semantico.interfazsemantica.AnalizadorSemantico;

import java.util.HashMap;
import java.util.Map;

public class AnalizadorSemanticoCoordinador {


    private final Map<Class<? extends NodoAST>, AnalizadorSemantico<? extends NodoAST>> analizadores = new HashMap<>();

    public AnalizadorSemanticoCoordinador(InferirTipoCoordinador inferirTipoCoordinador) {

        registrar(Clase.class, new AnalizadorClase(this));
        registrar(Atributo.class, new AnalizadorAtributo(inferirTipoCoordinador));

        registrar(Bloque.class, new AnalizadorBloque(this));
        registrar(CondicionIf.class, new AnalizadorCondicional(this, inferirTipoCoordinador));
        registrar(CicloWhile.class, new AnalizadorWhile(this, inferirTipoCoordinador));
        registrar(CicloDoWhile.class, new AnalizadorDoWhile(this, inferirTipoCoordinador));
        registrar(CicloFor.class, new AnalizadorCicloFor(this, inferirTipoCoordinador));
        registrar(CondicionSwitch.class, new AnalizadorSwitch(this, inferirTipoCoordinador));
        registrar(SentenciaBreak.class, new AnalizadorBreak());
        registrar(SentenciaContinue.class, new AnalizadorContinue());
        registrar(Asignacion.class, new AnalizadorAsignacion(inferirTipoCoordinador));
        registrar(DeclaracionVariable.class, new AnalizadorDeclaracionVariable(inferirTipoCoordinador));
        registrar(DeclaracionArreglo.class, new AnalizadorDeclaracionArreglo(inferirTipoCoordinador));
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
