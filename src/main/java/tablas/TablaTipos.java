package tablas;

import estructuras.TablaHash;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public void importarDe(TablaTipos otra) {

        if (otra == null) return;

        for (Map.Entry<String, InformeTipo> entry : otra.tipos.entradas()) {
            if (!this.tipos.containsKey(entry.getKey())) {
                this.tipos.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public List<String> listarNombres() {
        List<String> nombres = new ArrayList<>();
        for (var entry : tipos.entradas()) {
            nombres.add(entry.getKey());
        }
        return nombres;
    }
}
