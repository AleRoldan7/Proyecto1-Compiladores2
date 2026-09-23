package semantico.coordinadorsemantico;

import ast.expresiones.CrearArreglo;
import ast.expresiones.Expresion;
import ast.tipos.Tipo;
import enums.TipoDato;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import semantico.AnalisisContexto;
import semantico.Tipos;
import semantico.interfazsemantica.InferirTipo;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class InferidorCrearArreglo implements InferirTipo<CrearArreglo> {

    private final InferirTipoCoordinador inferirTipoCoordinador;

    @Override
    public Tipo inferir(CrearArreglo nodoArreglo, AnalisisContexto analisisContexto) {

        // FIX: evitar NullPointerException si getDimensiones() es null
        List<Expresion> dimensiones = nodoArreglo.getDimensiones() == null ? Collections.emptyList() : nodoArreglo.getDimensiones();

        for (Expresion dimension : dimensiones) {

            if (dimension == null) {
                continue;
            }

            Tipo tipoDimension = inferirTipoCoordinador.inferir(dimension, analisisContexto);

            // FIX: usar el dialecto para saber si es entero
            if (tipoDimension != null && Tipos.canonico(tipoDimension, analisisContexto) != TipoDato.ENTERO) {

                analisisContexto.reportarError(dimension.getLinea(), dimension.getColumna(),"La dimensión de un arreglo debe ser entero, se encontró "
                        + Tipos.describir(tipoDimension, analisisContexto));
            }
        }

        // FIX: evitar NullPointerException si getValoresIniciales() es null
        List<Expresion> valoresIniciales = nodoArreglo.getValoresIniciales() == null ? Collections.emptyList() : nodoArreglo.getValoresIniciales();

        if (!valoresIniciales.isEmpty()) {

            Tipo tipoBase = Tipos.simple(nodoArreglo.getLinea(), nodoArreglo.getColumna(), Tipos.base(Tipos.simple(nodoArreglo.getLinea(),
                    nodoArreglo.getColumna(), nodoArreglo.getTipoBase())));

            for (Expresion elemento : valoresIniciales) {

                Tipo tipoElemento = inferirTipoCoordinador.inferir(elemento, analisisContexto);

                // FIX: usar asignable(.., contexto)
                if (!Tipos.asignable(tipoBase, tipoElemento, analisisContexto)) {
                    analisisContexto.reportarError(elemento.getLinea(), elemento.getColumna(),"Elemento de tipo "
                            + Tipos.describir(tipoElemento, analisisContexto) + " no es compatible con el arreglo de tipo "
                            + Tipos.describir(tipoBase, analisisContexto));
                }
            }
        }

        // FIX: usar la lista ya calculada
        int dims = dimensiones.size();

        StringBuilder nombre = new StringBuilder(nodoArreglo.getTipoBase());
        for (int i = 0; i < dims; i++) {
            nombre.append("[]");
        }

        return new Tipo(nodoArreglo.getLinea(), nodoArreglo.getColumna(), nombre.toString(), dims > 0, dims);
    }
}
