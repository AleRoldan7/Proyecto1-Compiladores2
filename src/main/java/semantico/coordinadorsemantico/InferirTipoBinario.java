package semantico.coordinadorsemantico;

import ast.expresiones.Expresion;
import ast.expresiones.ExpresionBinaria;
import ast.tipos.Tipo;
import enums.TipoDato;
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

        Tipo resultado = switch (tipoOperador) {
            case ARITMETICO -> inferirAritmetico(nodoBinario, izquierdo, derecho, operacion, analisisContexto);
            case RELACIONAL -> inferirRelacional(nodoBinario, izquierdo, derecho, operacion, analisisContexto);
            case LOGICO     -> inferirLogico(nodoBinario, izquierdo, derecho, operacion, analisisContexto);
            case IGUALDAD   -> inferirIgualdad(nodoBinario, izquierdo, derecho, operacion, analisisContexto);
        };

        // Guardamos el tipo ya resuelto en el nodo, para que generarC3D no tenga que adivinarlo
        if (resultado != null) {
            nodoBinario.setTipoResuelto(Tipos.canonico(resultado, analisisContexto));
        }

        return resultado;
    }


    private Tipo inferirAritmetico(ExpresionBinaria nodoBinario, Tipo izquierdo, Tipo derecho, String operador, AnalisisContexto analisisContexto) {

        if ("+".equals(operador) && (Tipos.esTexto(izquierdo, analisisContexto) || Tipos.esTexto(derecho,   analisisContexto))) {

            return Tipos.simple(nodoBinario.getLinea(), nodoBinario.getColumna(), analisisContexto.getDialecto().nombrarTipo(TipoDato.TEXTO));
        }

        if (!Tipos.esNumerico(izquierdo, analisisContexto) || !Tipos.esNumerico(derecho,   analisisContexto)) {

            error(nodoBinario, analisisContexto, operador, izquierdo, derecho, "operandos numéricos");
            return null;
        }

        TipoDato canonIzq = Tipos.canonico(izquierdo, analisisContexto);
        TipoDato canonDer = Tipos.canonico(derecho,   analisisContexto);

        TipoDato resultado = (canonIzq == TipoDato.DECIMAL || canonDer == TipoDato.DECIMAL) ? TipoDato.DECIMAL : TipoDato.ENTERO;

        return Tipos.simple(nodoBinario.getLinea(), nodoBinario.getColumna(), analisisContexto.getDialecto().nombrarTipo(resultado));
    }


    private Tipo inferirRelacional(ExpresionBinaria nodoBinario, Tipo izquierdo, Tipo derecho, String operador, AnalisisContexto analisisContexto) {

        if (!Tipos.esNumerico(izquierdo, analisisContexto) || !Tipos.esNumerico(derecho,   analisisContexto)) {

            error(nodoBinario, analisisContexto, operador, izquierdo, derecho, "operandos numéricos");
            return null;
        }

        return Tipos.simple(nodoBinario.getLinea(), nodoBinario.getColumna(), analisisContexto.getDialecto().nombrarTipo(TipoDato.BOOLEANO));

    }

    private Tipo inferirIgualdad(ExpresionBinaria nodoBinario, Tipo izquierdo, Tipo derecho, String operador, AnalisisContexto analisisContexto) {

        if (!Tipos.asignable(izquierdo, derecho, analisisContexto) && !Tipos.asignable(derecho,   izquierdo, analisisContexto)) {

            analisisContexto.reportarError(nodoBinario.getLinea(), nodoBinario.getColumna(), "No se puede comparar "
                    + Tipos.describir(izquierdo, analisisContexto) + " con " + Tipos.describir(derecho,   analisisContexto));
        }

        return Tipos.simple(nodoBinario.getLinea(), nodoBinario.getColumna(), analisisContexto.getDialecto().nombrarTipo(TipoDato.BOOLEANO));
    }

    private Tipo inferirLogico(ExpresionBinaria nodoBinario, Tipo izquierdo, Tipo derecho, String operador, AnalisisContexto analisisContexto) {

        if (!Tipos.esBooleano(izquierdo, analisisContexto) || !Tipos.esBooleano(derecho,   analisisContexto)) {

            error(nodoBinario, analisisContexto, operador, izquierdo, derecho, "operandos booleanos");
            return null;
        }
        return Tipos.simple(nodoBinario.getLinea(), nodoBinario.getColumna(), analisisContexto.getDialecto().nombrarTipo(TipoDato.BOOLEANO));
    }

    private boolean esString(Tipo tipo) {
        return tipo != null && Tipos.STRING.equals(tipo.getNombre());
    }

    private void error(ExpresionBinaria nodoBinario, AnalisisContexto analisisContexto, String operador, Tipo izquierdo, Tipo derecho, String requisito) {

        analisisContexto.reportarError(nodoBinario.getLinea(), nodoBinario.getColumna(), "El operador '" + operador + "' requiere " + requisito
                + ", se encontró " + Tipos.describir(izquierdo, analisisContexto) + " y " + Tipos.describir(derecho,   analisisContexto));
    }
}
