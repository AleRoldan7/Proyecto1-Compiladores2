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
}
