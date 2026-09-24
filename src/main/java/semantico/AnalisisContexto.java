package semantico;

import ast.tipos.Tipo;
import enums.TipoArchivo;
import enums.TipoDato;
import enums.TipoErrorSemantico;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tablas.InformeTipo;
import tablas.TablaSimbolos;
import tablas.TablaTipos;
import semantico.dialecto.Dialecto;
import semantico.dialecto.Dialectos;

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
    private InformeTipo claseActual;
    private Tipo tipoRetorno;

    /*
     * Dialecto del archivo que se está analizando AHORA. Cambia con cada
     * archivo del proyecto (lo setea CompiladorArchivo), mientras que la
     * TablaSimbolos/TablaTipos siguen siendo compartidas entre los tres
     * lenguajes. Así un mismo analizador atiende a los tres, y le pregunta
     * al dialecto lo que es específico del lenguaje.
     */
    private Dialecto dialecto = Dialectos.ZETARIANO;

    public AnalisisContexto(TablaSimbolos tablaSimbolos, TablaTipos tablaTipos) {
        this.tablaSimbolos = tablaSimbolos;
        this.tablaTipos = tablaTipos;
    }

    /**
     * Crea un contexto NUEVO con sus PROPIAS tablas de símbolos y tipos.
     *
     * Cada archivo (.z, .y, .pig) vive en su propio espacio de nombres,
     * así que dos archivos pueden definir una clase 'Persona' sin que
     * eso sea un error.
     */
    public static AnalisisContexto paraArchivo(String nombreArchivo, TipoArchivo tipo) {

        AnalisisContexto ctx = new AnalisisContexto();
        ctx.setArchivoActual(nombreArchivo);
        ctx.setDialecto(Dialectos.de(tipo));
        ctx.setTablaSimbolos(new TablaSimbolos());
        ctx.setTablaTipos(new TablaTipos());

        return ctx;
    }

    /**
     * Importa los TIPOS PÚBLICOS de otro archivo a este contexto.
     *
     * Se usa cuando Pig Latin hace:
     *     import carpeta.Objeto1.z
     *     import carpeta.Funciones.y
     *
     * Solo se copian:
     *   - De un .z: la clase pública (con sus métodos y atributos públicos).
     *   - De un .y: las estructuras y las funciones.
     */
    public void importarDe(AnalisisContexto otro) {

        if (otro == null || otro.getTablaTipos() == null) {
            return;
        }

        // Copiar los tipos públicos (estructuras, clases)
        this.tablaTipos.importarDe(otro.getTablaTipos());

        // Copiar las funciones globales (categoría FUNCION)
        if (otro.getTablaSimbolos() != null) {
            this.tablaSimbolos.importarDe(otro.getTablaSimbolos());
        }
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

    /**
     * Regla del lenguaje actual: reporta el error y devuelve false si la
     * construcción no está permitida en este dialecto.
     *
     * Uso típico dentro de un analizador compartido:
     *
     *   if (!contexto.exigir(contexto.getDialecto().permiteVariablesGlobales(),
     *           nodo, "no se pueden declarar variables globales")) return;
     */
    public boolean exigir(boolean condicion, int linea, int columna, String queEstaMal) {

        if (!condicion) {
            reportarError(linea, columna, "En " + dialecto.nombre() + " " + queEstaMal);
        }

        return condicion;
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

    /** Convierte un nombre de tipo de CUALQUIER lenguaje (int, numerus...) a su TipoDato; null si es una clase. */
    public static TipoDato clasificar(String nombre) {
        if (nombre == null) return null;

        for (TipoArchivo archivo : TipoArchivo.values()) {
            Dialecto dialecto = Dialectos.de(archivo);
            for (TipoDato tipoDato : TipoDato.values()) {
                if (nombre.equals(dialecto.nombrarTipo(tipoDato))) {
                    return tipoDato;
                }
            }
        }
        return null;
    }
}