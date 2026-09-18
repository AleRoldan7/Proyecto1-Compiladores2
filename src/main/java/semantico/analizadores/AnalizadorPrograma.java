package semantico.analizadores;

import ast.Programa;
import ast.clases.Clase;
import ast.declaraciones.Declaracion;
import ast.declaraciones.DeclaracionFuncion;
import ast.estructuras.Estructura;
import lombok.AllArgsConstructor;
import semantico.AnalisisContexto;
import semantico.coordinadorsemantico.AnalizadorSemanticoCoordinador;
import semantico.interfazsemantica.AnalizadorSemantico;

/**
 * Raíz del análisis para Y? y Pig Latin (Zetariano entra por Clase).
 *
 * Acá es donde se aplican, vía Dialecto, las tres reglas estructurales que
 * el enunciado define por lenguaje:
 *
 *   - Y?        : no puede tener variables globales
 *   - Pig Latin : no puede DEFINIR estructuras (las importa de un .y)
 *   - solo .z   : puede definir clases
 *
 * Fijate que no hay un "AnalizadorProgramaY" y un "AnalizadorProgramaPig":
 * es el mismo recorrido preguntándole al dialecto qué está permitido.
 */
@AllArgsConstructor
public class AnalizadorPrograma implements AnalizadorSemantico<Programa> {

    private final AnalizadorSemanticoCoordinador coordinador;

    @Override
    public void analizar(Programa programa, AnalisisContexto contexto) {

        /*
         * PASADA 1 - declarar los tipos (estructuras y clases) ANTES de
         * mirar cualquier cuerpo. Sin esto, una función que recibe una
         * estructura definida más abajo en el archivo daría "tipo no
         * definido", y una estructura no podría referenciar a otra.
         */
        if (programa.getEstructuras() != null) {

            for (Estructura estructura : programa.getEstructuras()) {

                contexto.exigir(contexto.getDialecto().permiteDefinirEstructuras(),
                        estructura.getLinea(), estructura.getColumna(),
                        "no se pueden definir estructuras propias; deben importarse de un archivo .y");

                coordinador.analizar(estructura, contexto);
            }
        }

        if (programa.getClases() != null) {

            for (Clase clase : programa.getClases()) {

                contexto.exigir(contexto.getDialecto().permiteDefinirClases(),
                        clase.getLinea(), clase.getColumna(),
                        "no se pueden definir clases");

                coordinador.analizar(clase, contexto);
            }
        }

        /*
         * PASADA 2 - firmas de las funciones. Se declaran todas antes de
         * analizar sus cuerpos para que puedan llamarse entre sí en
         * cualquier orden, y para soportar recursividad.
         */
        if (programa.getFunciones() != null) {

            for (DeclaracionFuncion funcion : programa.getFunciones()) {
                coordinador.analizar(funcion, contexto);
            }
        }

        /*
         * PASADA 3 - variables globales. Solo Pig Latin las admite
         * (sección VARIABILES>); en Y? el enunciado las prohíbe.
         */
        if (programa.getDeclaraciones() != null) {

            for (Declaracion declaracion : programa.getDeclaraciones()) {

                contexto.exigir(contexto.getDialecto().permiteVariablesGlobales(),
                        declaracion.getLinea(), declaracion.getColumna(),
                        "no se pueden declarar variables globales");

                coordinador.analizar(declaracion, contexto);
            }
        }
    }
}