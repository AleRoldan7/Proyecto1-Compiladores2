package tablas;

import estructuras.TablaHash;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Ambito {

    private final Ambito ambitoPadre;
    private final String nombreAmbito;
    private final TablaHash<FilaTabla> simbolos = new TablaHash();

    public Ambito(Ambito ambitoPadre, String nombreAmbito) {
        this.ambitoPadre = ambitoPadre;
        this.nombreAmbito = nombreAmbito;
    }

    public boolean existeLocalmente(String nombre) {
        return simbolos.containsKey(nombre);
    }

    public void declarar(String nombre, FilaTabla fila) {
        simbolos.put(nombre, fila);
    }

    public FilaTabla getLocalmente(String nombre) {
        return simbolos.get(nombre);
    }

}
