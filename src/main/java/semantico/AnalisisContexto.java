package semantico;

import enums.TipoErrorSemantico;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tablas.TablaSimbolos;
import tablas.TablaTipos;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AnalisisContexto {

    private  TablaSimbolos tablaSimbolos;
    private  TablaTipos tablaTipos;
    private int sizeCiclo = 0;
    private int sizeSwitch = 0;
    private String archivoActual = "desconocido";
    private final List<ErrorSemantico> errores = new ArrayList<>();

    public AnalisisContexto(TablaSimbolos tablaSimbolos, TablaTipos tablaTipos) {
        this.tablaSimbolos = tablaSimbolos;
        this.tablaTipos = tablaTipos;
    }




    public void entrarCiclo() {
        sizeCiclo++;
    }

    public void salirCiclo() {
        sizeCiclo--;
    }

    public boolean dentroDeCiclo() {
        return sizeCiclo > 0;
    }

    public void entrarSwitch() {
        sizeSwitch++;
    }

    public void salirSwitch() {
        sizeSwitch--;
    }

    public boolean dentroDeSwitch() {
        return sizeSwitch > 0;
    }

    public void reportarError(int linea, int columna, String mensaje) {
        errores.add(ErrorSemantico.error(archivoActual, linea, columna, mensaje));
    }

    public void reportarAdvertencia(int linea, String mensaje) {
        errores.add(ErrorSemantico.advertencia(archivoActual, linea, 0, mensaje));
    }

    public boolean tieneErrores() {
        return errores.stream().anyMatch(e -> e.tipoError() == TipoErrorSemantico.ERROR);
    }
}
