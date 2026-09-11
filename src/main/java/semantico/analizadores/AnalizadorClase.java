package semantico.analizadores;

import ast.clases.Clase;
import enums.TipoDato;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import semantico.AnalisisContexto;
import semantico.interfazsemantica.AnalizadorSemantico;
import semantico.coordinadorsemantico.AnalizadorSemanticoCoordinador;
import tablas.InformeTipo;

@Getter
@Setter
@AllArgsConstructor
public class AnalizadorClase implements AnalizadorSemantico<Clase> {

    private final AnalizadorSemanticoCoordinador coordinador;


    @Override
    public void analizar(Clase nodo, AnalisisContexto analisisContexto) {

        String nombreClase = nodo.getNombreClase();

        if (analisisContexto.getTablaTipos().existeTipo(nombreClase)) {

            analisisContexto.reportarError(nodo.getLinea(), nodo.getColumna(), "El tipo '" + nombreClase + "' ya esta definido");
        }

        InformeTipo infoClase = new InformeTipo(nombreClase, TipoDato.OBJETO);
        analisisContexto.getTablaTipos().registrar(infoClase);

        InformeTipo claseAnterior = analisisContexto.getClaseActual();
        analisisContexto.setClaseActual(claseAnterior);

        analisisContexto.getTablaSimbolos().entrarAmbito("Clase " + nombreClase);

        nodo.getAtributos().forEach(atributo -> coordinador.analizar(atributo, analisisContexto));
        nodo.getConstructores().forEach(constructor -> coordinador.analizar(constructor, analisisContexto));
        nodo.getMetodos().forEach(metodo ->  coordinador.analizar(metodo, analisisContexto));

        analisisContexto.getTablaSimbolos().salirAmbito();
        analisisContexto.setClaseActual(claseAnterior);
    }
}
