package semantico.coordinadorsemantico;

import ast.NodoAST;
import ast.clases.Atributo;
import ast.clases.Clase;
import ast.expresiones.Expresion;
import ast.tipos.Tipo;
import semantico.AnalisisContexto;
import semantico.analizadores.AnalizadorAtributo;
import semantico.analizadores.AnalizadorClase;
import semantico.interfazsemantica.AnalizadorSemantico;
import semantico.interfazsemantica.InferirTipo;

import java.util.HashMap;
import java.util.Map;

public class InferirTipoCoordinador {

    private final Map<Class<? extends Expresion>, InferirTipo<? extends Expresion>> inferidores = new HashMap<>();


    public InferirTipoCoordinador() {

        // registrar(Literal.class, new InferidorLiteral());
        // registrar(Identificador.class, new InferidorIdentificador());
        // registrar(AccesoAtributo.class, new InferidorAccesoAtributo(this));
        // registrar(AccesoArreglo.class, new InferidorAccesoArreglo(this));
        // registrar(OperacionBinaria.class, new InferidorOperacionBinaria(this));
        // registrar(LlamadaFuncion.class, new InferidorLlamadaFuncion(this));
        // registrar(LlamadaMetodo.class, new InferidorLlamadaMetodo(this));
        // registrar(CrearArreglo.class, new InferidorCrearArreglo(this));
        // registrar(CrearObjeto.class, new InferidorCrearObjeto(this));
        // ... uno por cada subclase de Expresion
    }

    private <T extends Expresion> void registrar(Class<T> tipo, InferirTipo<T> inferidor) {
        inferidores.put(tipo, inferidor);
    }

    @SuppressWarnings("unchecked")
    public Tipo inferir(Expresion nodo, AnalisisContexto contexto) {

        if (nodo == null) {
            return null;
        }

        InferirTipo<Expresion> inferidor = (InferirTipo<Expresion>) inferidores.get(nodo.getClass());

        if (inferidor == null) {
            throw new IllegalStateException("No hay InferidorTipo registrado para " + nodo.getClass().getSimpleName());
        }

        return inferidor.inferir(nodo, contexto);
    }
}
