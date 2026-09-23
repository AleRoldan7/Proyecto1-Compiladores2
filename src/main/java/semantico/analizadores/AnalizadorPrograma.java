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

@AllArgsConstructor
public class AnalizadorPrograma implements AnalizadorSemantico<Programa> {

    private final AnalizadorSemanticoCoordinador coordinador;

    @Override
    public void analizar(Programa programa, AnalisisContexto contexto) {

        // PASADA 1: estructuras y clases
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

        // FIX: PASADA 2 - variables globales ANTES que las funciones
        if (programa.getDeclaraciones() != null) {
            for (Declaracion declaracion : programa.getDeclaraciones()) {
                contexto.exigir(contexto.getDialecto().permiteVariablesGlobales(),
                        declaracion.getLinea(), declaracion.getColumna(),
                        "no se pueden declarar variables globales");
                coordinador.analizar(declaracion, contexto);
            }
        }

        // PASADA 3: funciones (incluye main)
        if (programa.getFunciones() != null) {
            for (DeclaracionFuncion funcion : programa.getFunciones()) {
                coordinador.analizar(funcion, contexto);
            }
        }
    }
}