package semantico.analizadores;

import ast.clases.Clase;
import ast.clases.Constructor;
import ast.clases.Metodo;
import enums.TipoDato;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import semantico.AnalisisContexto;
import semantico.interfazsemantica.AnalizadorSemantico;
import semantico.coordinadorsemantico.AnalizadorSemanticoCoordinador;
import tablas.InformeTipo;
import tablas.MetodoRecord;

@Getter
@Setter
@AllArgsConstructor
public class AnalizadorClase implements AnalizadorSemantico<Clase> {

    private final AnalizadorSemanticoCoordinador coordinador;


    @Override
    public void analizar(Clase nodoClase, AnalisisContexto analisisContexto) {

        String nombreClase = nodoClase.getNombreClase();

        if (analisisContexto.getTablaTipos().existeTipo(nombreClase)) {

            analisisContexto.reportarError(nodoClase.getLinea(), nodoClase.getColumna(), "El tipo '" + nombreClase + "' ya esta definido");
        }

        InformeTipo infoClase = new InformeTipo(nombreClase, TipoDato.OBJETO);
        analisisContexto.getTablaTipos().registrar(infoClase);

        InformeTipo claseAnterior = analisisContexto.getClaseActual();
        analisisContexto.setClaseActual(claseAnterior);

        analisisContexto.getTablaSimbolos().entrarAmbito("Clase " + nombreClase);


        for (Metodo metodo : nodoClase.getMetodos()) {
            infoClase.agregarMetodo(new MetodoRecord(metodo.getNombreMetodo(), metodo.getTipoRetorno(), metodo.getParametros()));
        }

        for (Constructor constructor : nodoClase.getConstructores()) {
            infoClase.agregarConstructor(new MetodoRecord(nombreClase,
                    null, // un constructor no tiene tipo de retorno propio
                    constructor.getParametros()
            ));
        }

        nodoClase.getAtributos().forEach(atributo -> coordinador.analizar(atributo, analisisContexto));
        nodoClase.getConstructores().forEach(constructor -> coordinador.analizar(constructor, analisisContexto));
        nodoClase.getMetodos().forEach(metodo ->  coordinador.analizar(metodo, analisisContexto));

        analisisContexto.getTablaSimbolos().salirAmbito();
        analisisContexto.setClaseActual(claseAnterior);
    }
}
