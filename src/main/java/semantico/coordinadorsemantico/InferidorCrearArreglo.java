package semantico.coordinadorsemantico;

import ast.expresiones.CrearArreglo;
import ast.expresiones.Expresion;
import ast.tipos.Tipo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import semantico.AnalisisContexto;
import semantico.Tipos;
import semantico.interfazsemantica.InferirTipo;

@Getter
@Setter
@AllArgsConstructor
public class InferidorCrearArreglo implements InferirTipo<CrearArreglo> {

    private final InferirTipoCoordinador inferirTipoCoordinador;

    @Override
    public Tipo inferir(CrearArreglo nodoArreglo, AnalisisContexto analisisContexto) {

        for (Expresion dimension : nodoArreglo.getDimensiones()) {

            if (dimension == null) {
                continue; // new int[3][] : dimensión sin tamaño explícito
            }

            Tipo tipoDimension = inferirTipoCoordinador.inferir(dimension, analisisContexto);

            if (tipoDimension != null && !Tipos.INT.equals(tipoDimension.getNombre())) {

                analisisContexto.reportarError(dimension.getLinea(), dimension.getColumna(), "La dimensión de un arreglo debe ser int, se encontró "
                        + Tipos.describir(tipoDimension));
            }
        }

        if (nodoArreglo.getValoresIniciales() != null) {

            String nombre = Tipos.base(Tipos.simple(nodoArreglo.getLinea(), nodoArreglo.getColumna(), nodoArreglo.getTipoBase()));
            Tipo tipoBase = Tipos.simple(nodoArreglo.getLinea(), nodoArreglo.getColumna(), nombre);

            for (Expresion elemento : nodoArreglo.getValoresIniciales()) {

                Tipo tipoElemento = inferirTipoCoordinador.inferir(elemento, analisisContexto);

                if (!Tipos.sonCompatibles(tipoBase, tipoElemento)) {
                    System.out.println("TIPO: " + Tipos.describir(tipoElemento));
                    analisisContexto.reportarError(elemento.getLinea(), elemento.getColumna(), "Elemento de tipo " + Tipos.describir(tipoElemento)
                            + " no es compatible con el arreglo de tipo " + nodoArreglo.getTipoBase());
                }
            }
        }

        int dimensiones = nodoArreglo.getDimensiones().size();

        StringBuilder nombre = new StringBuilder(nodoArreglo.getTipoBase());
        for (int i = 0; i < dimensiones; i++) {
            nombre.append("[]");
        }

        return new Tipo(nodoArreglo.getLinea(), nodoArreglo.getColumna(), nombre.toString(), dimensiones > 0, dimensiones);

    }
}
