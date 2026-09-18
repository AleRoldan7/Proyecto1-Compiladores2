package semantico.analizadores;

import ast.clases.Clase;
import ast.clases.Constructor;
import ast.clases.Metodo;
import ast.declaraciones.Parametro;
import ast.tipos.Tipo;
import enums.TipoDato;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import semantico.AnalisisContexto;
import semantico.Tipos;
import semantico.coordinadorsemantico.AnalizadorSemanticoCoordinador;
import semantico.interfazsemantica.AnalizadorSemantico;
import tablas.InformeTipo;
import tablas.MetodoRecord;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class AnalizadorClase implements AnalizadorSemantico<Clase> {

    private final AnalizadorSemanticoCoordinador coordinador;

    @Override
    public void analizar(Clase nodoClase, AnalisisContexto analisisContexto) {

        String nombreClase = nodoClase.getNombreClase();

        if (analisisContexto.getTablaTipos().existeTipo(nombreClase)) {
            analisisContexto.reportarError(nodoClase.getLinea(), nodoClase.getColumna(),
                    "El tipo '" + nombreClase + "' ya esta definido");
        }

        InformeTipo infoClase = new InformeTipo(nombreClase, TipoDato.OBJETO);
        analisisContexto.getTablaTipos().registrar(infoClase);

        InformeTipo claseAnterior = analisisContexto.getClaseActual();
        analisisContexto.setClaseActual(infoClase);

        analisisContexto.getTablaSimbolos().entrarAmbito("Clase " + nombreClase);
        validarMetodosDuplicados(nodoClase.getMetodos(), analisisContexto);
        validarConstructoresDuplicados(nodoClase.getConstructores(), nombreClase, analisisContexto);


        for (Metodo metodo : nodoClase.getMetodos()) {
            infoClase.agregarMetodo(new MetodoRecord(metodo.getNombreMetodo(), metodo.getTipoRetorno(), metodo.getParametros()));
        }

        for (Constructor constructor : nodoClase.getConstructores()) {
            infoClase.agregarConstructor(new MetodoRecord(nombreClase, null, constructor.getParametros()));
        }

        nodoClase.getAtributos().forEach(a -> coordinador.analizar(a, analisisContexto));
        nodoClase.getConstructores().forEach(c -> coordinador.analizar(c, analisisContexto));
        nodoClase.getMetodos().forEach(m -> coordinador.analizar(m, analisisContexto));

        analisisContexto.getTablaSimbolos().salirAmbito();
        analisisContexto.setClaseActual(claseAnterior);
    }


    private void validarMetodosDuplicados(List<Metodo> metodos, AnalisisContexto analisisContexto) {

        Set<String> firmasVistas = new HashSet<>();

        for (Metodo metodo : metodos) {

            String firma = construirFirma(metodo.getNombreMetodo(), metodo.getParametros());

            if (firmasVistas.contains(firma)) {

                analisisContexto.reportarError(metodo.getLinea(), metodo.getColumna(), "El método '" + metodo.getNombreMetodo() +
                        "' ya fue declarado con la misma firma");

            } else {
                firmasVistas.add(firma);
            }
        }
    }



    private void validarConstructoresDuplicados(List<Constructor> constructores, String nombreClase, AnalisisContexto analisisContexto) {

        Set<String> firmasVistas = new HashSet<>();

        for (Constructor constructor : constructores) {

            String firma = construirFirma(nombreClase, constructor.getParametros());

            if (firmasVistas.contains(firma)) {

                analisisContexto.reportarError(constructor.getLinea(), constructor.getColumna(), "El constructor ya fue declarado con la misma firma");

            } else {
                firmasVistas.add(firma);
            }
        }
    }

    private String construirFirma(String nombre, List<Parametro> parametros) {

        StringBuilder sb = new StringBuilder(nombre);
        sb.append("(");

        for (int i = 0; i < parametros.size(); i++) {

            if (i > 0) sb.append(",");

            Parametro p = parametros.get(i);
            Tipo tipo = p.getTipoParametro();

            sb.append(Tipos.describir(tipo));
        }

        sb.append(")");

        return sb.toString();
    }
}