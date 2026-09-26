package semantico.coordinadorsemantico;

import ast.expresiones.AccesoArreglo;
import ast.expresiones.Expresion;
import ast.expresiones.ExpresionUnaria;
import ast.expresiones.Literal;
import ast.tipos.Tipo;
import enums.TipoDato;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import semantico.AnalisisContexto;
import semantico.Tipos;
import semantico.interfazsemantica.InferirTipo;

@Getter
@Setter
@AllArgsConstructor
public class InferidorAccesoArreglo implements InferirTipo<AccesoArreglo> {

    private final InferirTipoCoordinador inferirTipoCoordinador;

    @Override
    public Tipo inferir(AccesoArreglo nodoAcceso, AnalisisContexto analisisContexto) {

        Tipo tipoArreglo = inferirTipoCoordinador.inferir(nodoAcceso.getArreglo(), analisisContexto);

        for (Expresion indice : nodoAcceso.getIndicesArreglo()) {

            Tipo tipoIndice = inferirTipoCoordinador.inferir(indice, analisisContexto);

            if (tipoIndice != null && Tipos.canonico(tipoIndice, analisisContexto) != TipoDato.ENTERO) {

                analisisContexto.reportarError(indice.getLinea(), indice.getColumna(), "El índice de un arreglo debe ser entero, se encontró "
                        + Tipos.describir(tipoIndice, analisisContexto));
                continue;
            }


            if (indice instanceof Literal literal) {

                Object valor = literal.getValor();

                if (valor instanceof Integer && (Integer) valor < 0) {
                    analisisContexto.reportarError(indice.getLinea(), indice.getColumna(),
                            "El índice de un arreglo no puede ser negativo, se encontró " + valor);
                }
            }

            if (indice instanceof ExpresionUnaria unaria) {

                if ("-".equals(unaria.getOperador()) && unaria.getExpresion() instanceof Literal literal) {

                    Object valor = literal.getValor();

                    if (valor instanceof Integer) {

                        int valorNegativo = -(Integer) valor;

                        analisisContexto.reportarError(indice.getLinea(), indice.getColumna(),
                                "El índice de un arreglo no puede ser negativo, se encontró " + valorNegativo);
                    }
                }
            }
        }

        if (tipoArreglo == null) {
            return null;
        }

        if (!tipoArreglo.isArreglo()) {

            analisisContexto.reportarError(nodoAcceso.getLinea(), nodoAcceso.getColumna(),
                    "'" + Tipos.describir(tipoArreglo, analisisContexto) + "' no es un arreglo, no se puede indexar");

            return null;
        }

        int dimensionesRestantes = tipoArreglo.getDimensiones() - nodoAcceso.getIndicesArreglo().size();

        if (dimensionesRestantes < 0) {

            analisisContexto.reportarError(nodoAcceso.getLinea(), nodoAcceso.getColumna(), "Demasiados índices para un arreglo de "
                    + tipoArreglo.getDimensiones() + " dimensión(es)");

            dimensionesRestantes = 0;
        }

        String base = Tipos.base(tipoArreglo);

        // FIX: sin esto, tipoBaseElemento queda siempre null y el generador de
        // C3D (AccesoArreglo/Almacenamiento) nunca puede detectar que el
        // elemento indexado es una estructura -> arr[i] se trataba como
        // escalar y dereferenciaba en vez de calcular la dirección.
        nodoAcceso.setTipoBaseElemento(base);

        StringBuilder nombre = new StringBuilder(base);

        for (int i = 0; i < dimensionesRestantes; i++) {
            nombre.append("[]");
        }

        return new Tipo(nodoAcceso.getLinea(), nodoAcceso.getColumna(), nombre.toString(), dimensionesRestantes > 0, dimensionesRestantes);
    }
}