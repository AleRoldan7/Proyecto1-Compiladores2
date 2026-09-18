package tablas;

import enums.Categoria;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TablaSimbolos {

    private Ambito ambitoActual;
    private final List<FilaTabla> historialTabla = new ArrayList<>();

    public TablaSimbolos() {
        this.ambitoActual = new Ambito(null, "global");
    }

    public void entrarAmbito(String nombreAmbito) {
        ambitoActual = new Ambito(ambitoActual, nombreAmbito);
    }

    public void salirAmbito() {
        if (ambitoActual.getAmbitoPadre() == null) {
            throw new IllegalStateException("No se puede salir del ámbito global");
        }
        ambitoActual = ambitoActual.getAmbitoPadre();
    }

    public boolean existeEnAmbitoActual(String nombre) {
        return ambitoActual.existeLocalmente(nombre);
    }

    public void declarar(String nombre, Categoria categoria, String tipo, String detalle, int linea) {
        FilaTabla fila = new FilaTabla(nombre, categoria, tipo, detalle, ambitoActual.getNombreAmbito(), linea);
        ambitoActual.declarar(nombre, fila);
        historialTabla.add(fila);
    }

    public FilaTabla buscar(String nombre) {
        Ambito ambito = ambitoActual;
        while (ambito != null) {
            FilaTabla fila = ambito.getLocalmente(nombre);
            if (fila != null) return fila;
            ambito = ambito.getAmbitoPadre();
        }
        return null;
    }

    public List<FilaTabla> getTablaCompleta() {
        return historialTabla;
    }

    /**
     * Devuelve el ámbito global (raíz) desde el ámbito actual.
     */
    public Ambito getAmbitoRaiz() {
        Ambito a = ambitoActual;
        while (a.getAmbitoPadre() != null) {
            a = a.getAmbitoPadre();
        }
        return a;
    }

    /**
     * Copia los símbolos GLOBALES de otra tabla a esta. Se usa para
     * importaciones: Pig Latin importa un .y y necesita llamar a sus
     * funciones, o importa un .z y necesita ver sus clases.
     *
     * Solo se copian los símbolos del ámbito GLOBAL y de categoría
     * FUNCION o CLASE. Los parámetros, variables locales, atributos y
     * variables globales NO se importan.
     */
    public void importarDe(TablaSimbolos otra) {

        if (otra == null) return;

        Ambito globalOtra = otra.getAmbitoRaiz();
        Ambito globalLocal = this.getAmbitoRaiz();

        if (globalOtra == null || globalLocal == null) return;

        for (FilaTabla fila : globalOtra.getSimbolosLocales()) {

            if (fila.getCategoria() != Categoria.FUNCION
                    && fila.getCategoria() != Categoria.CLASE) {
                continue;
            }

            if (!globalLocal.existeLocalmente(fila.getNombre())) {
                globalLocal.declarar(fila.getNombre(), fila);
                this.historialTabla.add(fila);
            }
        }
    }
}
