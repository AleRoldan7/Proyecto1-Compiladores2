package semantico.coordinadorsemantico;

import ast.expresiones.AccesoAtributo;
import ast.tipos.Tipo;
import enums.TipoDato;
import lombok.AllArgsConstructor;
import semantico.AnalisisContexto;
import semantico.Tipos;
import semantico.interfazsemantica.InferirTipo;
import tablas.InformeTipo;

@AllArgsConstructor
public class InferidorAccesoAtributo
        implements InferirTipo<AccesoAtributo> {

    private final InferirTipoCoordinador inferirTipoCoordinador;

    @Override
    public Tipo inferir(
            AccesoAtributo nodoAcceso,
            AnalisisContexto analisisContexto) {

        Tipo tipoObjeto =
                inferirTipoCoordinador.inferir(
                        nodoAcceso.getBase(),
                        analisisContexto
                );

        if (tipoObjeto == null
                || tipoObjeto.isArreglo()) {

            analisisContexto.reportarError(
                    nodoAcceso.getLinea(),
                    nodoAcceso.getColumna(),
                    "No se puede acceder al atributo '"
                            + nodoAcceso.getAtributo()
                            + "' de "
                            + Tipos.describir(tipoObjeto)
            );

            return null;
        }

        InformeTipo tipoContenedor =
                analisisContexto.getTablaTipos()
                        .obtener(tipoObjeto.getNombre());

        if (tipoContenedor == null
                || !tipoContenedor.tieneAtributo(
                nodoAcceso.getAtributo())) {

            analisisContexto.reportarError(
                    nodoAcceso.getLinea(),
                    nodoAcceso.getColumna(),
                    "'"
                            + tipoObjeto.getNombre()
                            + "' no tiene un atributo '"
                            + nodoAcceso.getAtributo()
                            + "'"
            );

            return null;
        }

        nodoAcceso.setTipoContenedor(
                tipoObjeto.getNombre()
        );

        nodoAcceso.setContenedorEsEstructura(
                tipoContenedor.getCategoria()
                        == TipoDato.ESTRUCTURA
        );

        Tipo tipoResultado =
                tipoContenedor.tipoDeAtributo(
                        nodoAcceso.getAtributo()
                );

        TipoDato tipoC =
                convertirTipoDato(
                        tipoResultado,
                        analisisContexto
                );

        nodoAcceso.setTipoResultado(tipoC);

        return tipoResultado;
    }

    private TipoDato convertirTipoDato(
            Tipo tipo,
            AnalisisContexto analisisContexto) {

        if (tipo == null
                || tipo.getNombre() == null) {

            return TipoDato.DESCONOCIDO;
        }

        String nombre =
                tipo.getNombre().toLowerCase();

        return switch (nombre) {

            case "entero", "int" ->
                    TipoDato.ENTERO;

            case "decimal", "double", "float" ->
                    TipoDato.DECIMAL;

            case "texto", "string", "cadena" ->
                    TipoDato.TEXTO;

            case "caracter", "char" ->
                    TipoDato.CARACTER;

            case "booleano", "bool", "boolean" ->
                    TipoDato.BOOLEANO;

            case "void" ->
                    TipoDato.VOID;

            default -> {

                InformeTipo informe =
                        analisisContexto
                                .getTablaTipos()
                                .obtener(tipo.getNombre());

                if (informe != null) {
                    yield informe.getCategoria();
                }

                yield TipoDato.DESCONOCIDO;
            }
        };
    }
}