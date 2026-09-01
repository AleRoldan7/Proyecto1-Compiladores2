package c3d;

import enums.TipoDato;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ContextoC3D {

    private final List<Cuarteta> cuartetas = new ArrayList<>();
    private int contadorTemporales = 0;
    private int contadorEtiquetas = 0;

    public String nuevoTemporal() {
        return "tmp" + contadorTemporales++;
    }

    public String nuevaEtiqueta() {
        return "etq" + contadorEtiquetas++;
    }

    public void agregar(String operador, String arg1, String arg2, String resultado) {
        cuartetas.add(new Cuarteta(operador, arg1, arg2, resultado, null));
    }

    public void agregarConTipo(String operador, String arg1, String arg2, String resultado, TipoDato tipo) {
        cuartetas.add(new Cuarteta(operador, arg1, arg2, resultado, tipo));
    }

    public void agregarEtiqueta(String etiqueta) {
        cuartetas.add(new Cuarteta("label", null, null, etiqueta, null));
    }


}
