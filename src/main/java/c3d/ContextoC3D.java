package c3d;

import enums.TipoDato;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class ContextoC3D {

    private final List<Cuarteta> cuartetas = new ArrayList<>();

    private int contadorTemporales = 0;
    private int contadorEtiquetas = 0;
    private int contadorStrings = 0;

    private final Deque<String> etiquetasSalida = new ArrayDeque<>();
    private final Deque<String> etiquetasContinuar = new ArrayDeque<>();

    /* =========================================================
       REGISTRO DE STRINGS (para C)
       ========================================================= */

    /**
     * Los strings literales no se pueden meter como operandos directos
     * en C3D que luego se va a traducir a C, porque en C los strings
     * son arrays de char. Se guardan en una tabla y se referencian por
     * nombre.
     */
    private final Map<String, String> tablaStrings = new LinkedHashMap<>();

    public String registrarString(String valor) {
        for (Map.Entry<String, String> e : tablaStrings.entrySet()) {
            if (e.getValue().equals(valor)) return e.getKey();
        }
        String nombre = "str" + contadorStrings++;
        tablaStrings.put(nombre, valor);
        return nombre;
    }

    public Map<String, String> getTablaStrings() {
        return tablaStrings;
    }

    /* =========================================================
       FUNCIONES GENERADAS (para forward declarations en C)
       ========================================================= */

    private final Set<String> funcionesGeneradas = new LinkedHashSet<>();

    public void registrarFuncion(String nombre) {
        funcionesGeneradas.add(nombre);
    }

    public Set<String> getFuncionesGeneradas() {
        return funcionesGeneradas;
    }

    /* =========================================================
       TEMPORALES Y ETIQUETAS
       ========================================================= */

    public String nuevoTemporal() {
        return "t" + contadorTemporales++;
    }

    public String nuevaEtiqueta() {
        return "L" + contadorEtiquetas++;
    }

    /* =========================================================
       EMISIÓN
       ========================================================= */

    public void agregar(String operador, String arg1, String arg2, String resultado) {
        cuartetas.add(new Cuarteta(operador, arg1, arg2, resultado, null));
    }

    public void agregarConTipo(String operador, String arg1, String arg2,
                               String resultado, TipoDato tipo) {
        cuartetas.add(new Cuarteta(operador, arg1, arg2, resultado, tipo));
    }

    public void agregarEtiqueta(String etiqueta) {
        cuartetas.add(new Cuarteta("label", null, null, etiqueta, null));
    }

    public void asignar(String destino, String origen) {
        agregar("=", origen, null, destino);
    }

    public void salto(String etiqueta) {
        agregar("goto", null, null, etiqueta);
    }

    public void saltoSiFalso(String condicion, String etiqueta) {
        agregar("if_false", condicion, null, etiqueta);
    }

    public void saltoSiVerdadero(String condicion, String etiqueta) {
        agregar("if_true", condicion, null, etiqueta);
    }

    public String binaria(String operador, String izquierda, String derecha, TipoDato tipo) {
        String temporal = nuevoTemporal();
        agregarConTipo(operador, izquierda, derecha, temporal, tipo);
        return temporal;
    }

    public String unaria(String operador, String operando, TipoDato tipo) {
        String temporal = nuevoTemporal();
        agregarConTipo(operador, operando, null, temporal, tipo);
        return temporal;
    }

    /* =========================================================
       CONTROL DE CICLOS / SWITCH
       ========================================================= */

    public void entrarCiclo(String etiquetaContinuar, String etiquetaSalida) {
        etiquetasContinuar.push(etiquetaContinuar);
        etiquetasSalida.push(etiquetaSalida);
    }

    public void salirCiclo() {
        etiquetasContinuar.pop();
        etiquetasSalida.pop();
    }

    public void entrarSwitch(String etiquetaSalida) {
        etiquetasSalida.push(etiquetaSalida);
    }

    public void salirSwitch() {
        etiquetasSalida.pop();
    }

    public String etiquetaBreak() {
        return etiquetasSalida.peek();
    }

    public String etiquetaContinue() {
        return etiquetasContinuar.peek();
    }

    /* =========================================================
       SALIDA
       ========================================================= */

    public String comoTexto() {

        StringBuilder texto = new StringBuilder();

        for (Cuarteta cuarteta : cuartetas) {

            if (!"label".equals(cuarteta.getOperador())) {
                texto.append("    ");
            }

            texto.append(cuarteta).append('\n');
        }

        return texto.toString();
    }
}