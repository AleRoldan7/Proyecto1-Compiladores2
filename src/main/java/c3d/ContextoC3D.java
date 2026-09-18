package c3d;

import enums.TipoDato;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Getter
@Setter
public class ContextoC3D {

    private final List<Cuarteta> cuartetas = new ArrayList<>();
    private int contadorTemporales = 0;
    private int contadorEtiquetas = 0;

    /*
     * ============================================================
     * Pilas de salto para break / continue.
     * ============================================================
     *
     * Al entrar a un ciclo se apilan dos etiquetas:
     *   - etiquetaSalida    -> a donde salta un 'break'
     *   - etiquetaContinuar -> a donde salta un 'continue'
     *
     * Son PILAS y no variables sueltas porque los ciclos se anidan: un
     * break dentro de un for que esta dentro de un while tiene que salir
     * del for (el mas cercano), no del while.
     *
     * El switch apila SOLO salida: un 'continue' dentro de un switch que
     * esta dentro de un ciclo debe seguir yendo al ciclo. Por eso las dos
     * pilas son independientes.
     */
    private final Deque<String> etiquetasSalida = new ArrayDeque<>();
    private final Deque<String> etiquetasContinuar = new ArrayDeque<>();

    public String nuevoTemporal() {
        return "tmp" + contadorTemporales++;
    }

    public String nuevaEtiqueta() {
        return "etq" + contadorEtiquetas++;
    }

    /* ================== EMISION BASICA =================== */

    public void agregar(String operador, String arg1, String arg2, String resultado) {
        cuartetas.add(new Cuarteta(operador, arg1, arg2, resultado, null));
    }

    public void agregarConTipo(String operador, String arg1, String arg2, String resultado, TipoDato tipo) {
        cuartetas.add(new Cuarteta(operador, arg1, arg2, resultado, tipo));
    }

    public void agregarEtiqueta(String etiqueta) {
        cuartetas.add(new Cuarteta("label", null, null, etiqueta, null));
    }

    /* ============ HELPERS DE ALTO NIVEL ================== */

    /** destino = origen */
    public void asignar(String destino, String origen) {
        agregar("=", origen, null, destino);
    }

    /** goto etiqueta */
    public void salto(String etiqueta) {
        agregar("goto", null, null, etiqueta);
    }

    /** if_false condicion goto etiqueta */
    public void saltoSiFalso(String condicion, String etiqueta) {
        agregar("if_false", condicion, null, etiqueta);
    }

    /** if condicion goto etiqueta */
    public void saltoSiVerdadero(String condicion, String etiqueta) {
        agregar("if_true", condicion, null, etiqueta);
    }

    /**
     * Emite una operacion binaria en un temporal nuevo y devuelve el
     * nombre del temporal: la forma mas comun de generar C3D de una
     * expresion.
     */
    public String binaria(String operador, String izquierda, String derecha, TipoDato tipo) {

        String temporal = nuevoTemporal();
        agregarConTipo(operador, izquierda, derecha, temporal, tipo);

        return temporal;
    }

    /* ================= CONTROL DE CICLOS ================= */

    public void entrarCiclo(String etiquetaContinuar, String etiquetaSalida) {
        etiquetasContinuar.push(etiquetaContinuar);
        etiquetasSalida.push(etiquetaSalida);
    }

    public void salirCiclo() {
        etiquetasContinuar.pop();
        etiquetasSalida.pop();
    }

    /** El switch solo interrumpe con break, no participa del continue. */
    public void entrarSwitch(String etiquetaSalida) {
        etiquetasSalida.push(etiquetaSalida);
    }

    public void salirSwitch() {
        etiquetasSalida.pop();
    }

    /**
     * Etiqueta a la que debe saltar un 'break'. Devuelve null si no hay
     * ciclo ni switch activo: ese caso ya lo rechazo el analisis semantico
     * (AnalizadorBreak), asi que aca simplemente no se emite nada.
     */
    public String etiquetaBreak() {
        return etiquetasSalida.peek();
    }

    /** Etiqueta a la que debe saltar un 'continue'. */
    public String etiquetaContinue() {
        return etiquetasContinuar.peek();
    }

    /* ======================= SALIDA ====================== */

    /** El C3D completo, una cuarteta por linea. */
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