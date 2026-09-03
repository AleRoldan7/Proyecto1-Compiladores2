package tablas;

import estructuras.TablaHash;

public class TablaTipos {

    private final TablaHash<InformeTipo> tipos = new TablaHash<>();

    public void registrar(InformeTipo informeTipo) {
        tipos.put(informeTipo.getNombre(), informeTipo);
    }

    public boolean existeTipo(String nombre) {
        return tipos.containsKey(nombre);
    }

    public InformeTipo obtener(String nombre) {
        return tipos.get(nombre);
    }
}
