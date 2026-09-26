package semantico.analizadores;

import ast.declaraciones.DeclaracionFuncion;
import ast.declaraciones.Parametro;
import enums.Categoria;
import lombok.AllArgsConstructor;
import semantico.AnalisisContexto;
import semantico.Tipos;
import semantico.coordinadorsemantico.AnalizadorSemanticoCoordinador;
import semantico.interfazsemantica.AnalizadorSemantico;

import java.util.HashSet;
import java.util.Set;

/**
 * Funciones globales de Y? (%funciones). Es el análogo de AnalizadorMetodo,
 * pero la función vive en el ámbito global en vez de dentro de una clase,
 * así que Pig Latin puede llamarla después de importar el .y.
 */
@AllArgsConstructor
public class AnalizadorFuncion implements AnalizadorSemantico<DeclaracionFuncion> {

    private final AnalizadorSemanticoCoordinador coordinador;

    @Override
    public void analizar(DeclaracionFuncion funcion, AnalisisContexto contexto) {

        /*
         * La firma se declara en el ámbito GLOBAL (el que está activo al
         * entrar), antes de abrir el ámbito del cuerpo. Eso es lo que
         * habilita la recursividad: cuando se analice el cuerpo y aparezca
         * una llamada a sí misma, el nombre ya está en la tabla.
         */
        if (contexto.getTablaSimbolos().existeEnAmbitoActual(funcion.getNombreFuncion())) {

            contexto.reportarError(funcion.getLinea(), funcion.getColumna(),
                    "La función '" + funcion.getNombreFuncion() + "' ya fue declarada");

        } else {

            contexto.getTablaSimbolos().declarar(
                    funcion.getNombreFuncion(),
                    Categoria.FUNCION,
                    funcion.getTipoRetorno() == null
                            ? "void"
                            : funcion.getTipoRetorno().getNombre(),
                    (funcion.getParametros() == null ? 0 : funcion.getParametros().size()) + " parámetro(s)",
                    funcion.getLinea()
            );
        }

        contexto.getTablaSimbolos().entrarAmbito("Funcion " + funcion.getNombreFuncion());

        Set<String> vistos = new HashSet<>();

        if (funcion.getParametros() != null) {

            for (Parametro parametro : funcion.getParametros()) {

                if (!vistos.add(parametro.getNombreParametro())) {

                    contexto.reportarError(parametro.getLinea(), parametro.getColumna(),
                            "El parámetro '" + parametro.getNombreParametro() + "' está duplicado");
                    continue;
                }

                if (Tipos.canonico(parametro.getTipoParametro(), contexto) == null) {

                    contexto.reportarError(parametro.getLinea(), parametro.getColumna(),
                            "Tipo desconocido '" + Tipos.base(parametro.getTipoParametro())
                                    + "' en el parámetro '" + parametro.getNombreParametro() + "'");
                }

                contexto.getTablaSimbolos().declarar(
                        parametro.getNombreParametro(),
                        Categoria.PARAMETRO,
                        Tipos.describir(parametro.getTipoParametro()),   // antes: parametro.getTipoParametro().getNombre()
                        parametro.isReferencia() ? "por referencia" : "por valor",
                        parametro.getLinea()
                );
            }
        }

        var retornoAnterior = contexto.getTipoRetorno();
        contexto.setTipoRetorno(funcion.getTipoRetorno());

        coordinador.analizar(funcion.getCuerpoFuncion(), contexto);

        contexto.setTipoRetorno(retornoAnterior);

        contexto.getTablaSimbolos().salirAmbito();
    }
}