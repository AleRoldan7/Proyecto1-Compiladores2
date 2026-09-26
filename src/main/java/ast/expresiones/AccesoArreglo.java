package ast.expresiones;

import c3d.ContextoC3D;
import enums.TipoDato;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AccesoArreglo extends Expresion {

    private Expresion arreglo;
    private List<Expresion> indicesArreglo;
    private String tipoBaseElemento;

    public AccesoArreglo(int linea, int columna, Expresion arreglo, List<Expresion> indicesArreglo) {
        super(linea, columna);
        this.arreglo = arreglo;
        this.indicesArreglo = indicesArreglo;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {

        String arr = arreglo.generarC3D(contexto);
        String indice = generarIndiceAplanado(contexto);

        boolean elementoEsEstructura =
                tipoBaseElemento != null && contexto.esEstructura(tipoBaseElemento);

        int anchoElemento = elementoEsEstructura
                ? contexto.tamanioEstructura(tipoBaseElemento) : 1;

        String offset = indice;
        if (anchoElemento != 1) {
            offset = contexto.binaria("*", indice, String.valueOf(anchoElemento), TipoDato.ENTERO);
        }

        if (elementoEsEstructura) {
            // El arreglo guarda las estructuras EN LÍNEA (memoria plana):
            // arr[i] ES la dirección del elemento, no un puntero a él.
            // Igual que AccesoAtributo con layout.esEmbebido(): devolvemos
            // la dirección calculada, SIN dereferenciar, para que el
            // siguiente eslabón de la cadena (.campo u otro [indice]) la use
            // como base directa.
            return contexto.binaria("+", arr, offset, TipoDato.ESTRUCTURA);
        }

        // Elemento escalar (entero, cadena, etc.): aquí sí queremos el VALOR.
        String temporal = contexto.nuevoTemporal();
        contexto.agregar("index_get", arr, offset, temporal, tipoResultado != null ? tipoResultado : TipoDato.ENTERO);
        return temporal;
    }

    /**
     * Aplana los índices. Para 1 índice devuelve el índice tal cual.
     * Para N índices, los combina: i0 * d1 * d2 + i1 * d2 + i2 ...
     *
     * NOTA: para el aplanado correcto se necesitan los TAMAÑOS de cada
     * dimensión. Como aún no los tenemos propagados al AST, para N > 1
     * se suman los índices (NO es correcto para arreglos reales, pero
     * evita el crash).
     */
    private String generarIndiceAplanado(ContextoC3D contexto) {

        if (indicesArreglo == null || indicesArreglo.isEmpty()) {
            return "0";
        }

        if (indicesArreglo.size() == 1) {
            return indicesArreglo.get(0).generarC3D(contexto);
        }

        // Simplificación temporal (no correcta para aplanado real)
        String acumulado = indicesArreglo.get(0).generarC3D(contexto);

        for (int i = 1; i < indicesArreglo.size(); i++) {
            String idx = indicesArreglo.get(i).generarC3D(contexto);
            acumulado = contexto.binaria("+", acumulado, idx, enums.TipoDato.ENTERO);
        }

        return acumulado;
    }
}
