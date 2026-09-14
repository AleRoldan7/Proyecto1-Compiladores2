package semantico.coordinadorsemantico;

import ast.expresiones.Expresion;
import ast.expresiones.ExpresionBinaria;
import ast.tipos.Tipo;
import enums.TipoOperador;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import semantico.AnalisisContexto;
import semantico.Tipos;
import semantico.interfazsemantica.InferirTipo;

@Getter
@Setter
@AllArgsConstructor
public class InferirTipoBinario implements InferirTipo<ExpresionBinaria> {

    private final InferirTipoCoordinador inferirTipoCoordinador;

    @Override
    public Tipo inferir(ExpresionBinaria nodoBinario, AnalisisContexto analisisContexto) {

        Tipo izquierdo = inferirTipoCoordinador.inferir(nodoBinario.getIzquierda(), analisisContexto);
        Tipo derecho = inferirTipoCoordinador.inferir(nodoBinario.getDerecha(), analisisContexto);
        String operacion = nodoBinario.getOperacion();

        TipoOperador tipoOperador = TipoOperador.obtenerTipoOperador(operacion);

        if (tipoOperador == null) {
            analisisContexto.reportarError(nodoBinario.getLinea(), nodoBinario.getColumna(), "Operador binario no reconocido '" + operacion + "'");

            return null;
        }

        return switch (tipoOperador) {

            case ARITMETICO -> inferirAritmetico(nodoBinario, izquierdo, derecho, operacion, analisisContexto);

            case RELACIONAL -> inferirRelacional(nodoBinario, izquierdo, derecho, operacion, analisisContexto);

            case LOGICO -> inferirLogico(nodoBinario, izquierdo, derecho, operacion, analisisContexto);

            case IGUALDAD -> inferirIgualdad(nodoBinario, izquierdo, derecho, operacion, analisisContexto);

        };
    }


    private Tipo inferirAritmetico(ExpresionBinaria nodoBinario, Tipo izquierdo, Tipo derecho, String operador, AnalisisContexto analisisContexto) {

        if ("+".equals(operador) && (esString(izquierdo) || esString(derecho))) {

            return Tipos.cadena(nodoBinario.getLinea(), nodoBinario.getColumna());
        }

        if (!Tipos.esNumerico(izquierdo) || !Tipos.esNumerico(derecho)) {
            error(nodoBinario, analisisContexto, operador, izquierdo, derecho, "Operandos numericos");
            return null;
        }

        boolean esDouble = Tipos.DOUBLE.equals(izquierdo.getNombre()) || Tipos.DOUBLE.equals(derecho.getNombre());

        return esDouble ? Tipos.decimal(nodoBinario.getLinea(), nodoBinario.getColumna()) : Tipos.entero(nodoBinario.getLinea(), nodoBinario.getColumna());
    }

    private Tipo inferirRelacional(ExpresionBinaria nodoBinario, Tipo izquiedo, Tipo derecho, String operador, AnalisisContexto analisisContexto) {

        if (!Tipos.esNumerico(izquiedo) || !Tipos.esNumerico(derecho)) {

            error(nodoBinario, analisisContexto, operador, izquiedo, derecho, "operandos numéricos");
        }

        return Tipos.booleano(nodoBinario.getLinea(), nodoBinario.getColumna());
    }

    private Tipo inferirIgualdad(ExpresionBinaria nodoBinario, Tipo izquierdo, Tipo derecho, String operador, AnalisisContexto analisisContexto) {

        if (!Tipos.sonCompatibles(izquierdo, derecho) && !Tipos.sonCompatibles(derecho, izquierdo)) {

            analisisContexto.reportarError(nodoBinario.getLinea(), nodoBinario.getColumna(), "No se puede comparar " + Tipos.describir(izquierdo) +
                    " con " + Tipos.describir(derecho));
        }

        return Tipos.booleano(nodoBinario.getLinea(), nodoBinario.getColumna());
    }

    private Tipo inferirLogico(ExpresionBinaria nodoBinario, Tipo izquierdo, Tipo derecho, String operador, AnalisisContexto analisisContexto) {

        if (!Tipos.esBooleano(izquierdo) || !Tipos.esBooleano(derecho)) {

            error(nodoBinario, analisisContexto, operador, izquierdo, derecho, "operandos booleanos");
        }

        return Tipos.booleano(nodoBinario.getLinea(), nodoBinario.getColumna());
    }

    private boolean esString(Tipo tipo) {
        return tipo != null && Tipos.STRING.equals(tipo.getNombre());
    }

    private void error(ExpresionBinaria nodoBinario, AnalisisContexto analisisContexto, String operador, Tipo izquierdo, Tipo derecho, String requisito) {

        analisisContexto.reportarError(nodoBinario.getLinea(), nodoBinario.getColumna(), "El operador '" + operador + "' requiere "
                + requisito + ", se encontró " + Tipos.describir(izquierdo) + " y " + Tipos.describir(derecho));
    }
}
