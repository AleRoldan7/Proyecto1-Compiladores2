package semantico.coordinadorsemantico;

import ast.expresiones.AccesoArreglo;
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
public class InferidorAccesoArreglo implements InferirTipo<AccesoArreglo> {

    private final InferirTipoCoordinador inferirTipoCoordinador;

    @Override
    public Tipo inferir(AccesoArreglo nodoAcceso, AnalisisContexto analisisContexto) {

        Tipo tipoArreglo = inferirTipoCoordinador.inferir(nodoAcceso.getArreglo(), analisisContexto);

        for (Expresion indice : nodoAcceso.getIndicesArreglo()) {

            Tipo tipoIndice = inferirTipoCoordinador.inferir(indice, analisisContexto);

            if (tipoIndice != null && !Tipos.INT.equals(tipoIndice.getNombre())) {

                analisisContexto.reportarError(indice.getLinea(), indice.getColumna(), "El índice de un arreglo debe ser int, se encontró "
                        + Tipos.describir(tipoIndice));
            }
        }

        if (tipoArreglo == null || !tipoArreglo.isArreglo()) {

            analisisContexto.reportarError(nodoAcceso.getLinea(), nodoAcceso.getColumna(), "'" + Tipos.describir(tipoArreglo)
                    + "' no es un arreglo, no se puede indexar");

            return null;
        }

        int dimensionesRestantes = tipoArreglo.getDimensiones() - nodoAcceso.getIndicesArreglo().size();

        if (dimensionesRestantes < 0) {

            analisisContexto.reportarError(nodoAcceso.getLinea(), nodoAcceso.getColumna(), "Demasiados índices para un arreglo de "
                    + tipoArreglo.getDimensiones() + " dimensión(es)");

            dimensionesRestantes = 0;
        }

        String base = Tipos.base(tipoArreglo);

        StringBuilder nombre = new StringBuilder(base);
        for (int i = 0; i < dimensionesRestantes; i++) {
            nombre.append("[]");
        }

        return new Tipo(nodoAcceso.getLinea(), nodoAcceso.getColumna(), nombre.toString(), dimensionesRestantes > 0, dimensionesRestantes);

    }
}
