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

        // Si tipoBaseElemento es null, obtenerlo del contexto
        String tipoBase = tipoBaseElemento;
        if (tipoBase == null) {
            Expresion base = arreglo;
            while (base instanceof AccesoArreglo aa) {
                base = aa.getArreglo();
            }
            if (base instanceof Identificador id) {
                tipoBase = contexto.tipoBaseDeArreglo(id.getNombreIdentificador());
            }
        }

        boolean elementoEsEstructura =
                tipoBase != null && contexto.esEstructura(tipoBase);

        int anchoElementoFinal = elementoEsEstructura
                ? contexto.tamanioEstructura(tipoBase) : 1;

        int nivel = nivelDeIndices(arreglo);
        List<Integer> tamanios = tamaniosDeclarados(contexto, arreglo);

        boolean quedanDimensiones = false;
        int anchoElemento = anchoElementoFinal;

        if (tamanios != null && nivel < tamanios.size()) {
            int dimensionesRestantesTrasEste = tamanios.size() - nivel - 1;
            if (dimensionesRestantesTrasEste > 0) {
                quedanDimensiones = true;
                int ancho = anchoElementoFinal;
                for (int d = nivel + 1; d < tamanios.size(); d++) {
                    ancho *= tamanios.get(d);
                }
                anchoElemento = ancho;
            }
        }

        boolean esCompuesto = elementoEsEstructura || quedanDimensiones;

        String offset = indice;
        if (anchoElemento != 1) {
            offset = contexto.binaria("*", indice, String.valueOf(anchoElemento), TipoDato.ENTERO);
        }

        if (esCompuesto) {
            return contexto.binaria("+", arr, offset, TipoDato.ESTRUCTURA);
        }

        String temporal = contexto.nuevoTemporal();
        contexto.agregar("index_get", arr, offset, temporal);
        return temporal;
    }

    /**
     * Cuántos accesos [indice] ya se aplicaron por debajo de 'expr' (0 = expr
     * es la base). Visibilidad de paquete: también la usa Almacenamiento
     * para mantener el mismo cálculo de ancho en el camino de escritura.
     */
    static int nivelDeIndices(Expresion expr) {
        if (expr instanceof AccesoArreglo aa) {
            return 1 + nivelDeIndices(aa.getArreglo());
        }
        return 0;
    }

    /**
     * Tamaños por dimensión declarados para el arreglo del que cuelga 'expr',
     * si 'expr' desciende de una variable con tamaño fijo conocido (ver
     * DeclaracionArreglo.generarC3D / ContextoC3D.registrarTamaniosArreglo).
     * Devuelve null si la cadena no llega a un identificador simple (ej. es
     * un campo de estructura) o si esa variable no tiene tamaños registrados.
     */
    static List<Integer> tamaniosDeclarados(ContextoC3D contexto, Expresion expr) {

        Expresion base = expr;
        while (base instanceof AccesoArreglo aa) {
            base = aa.getArreglo();
        }

        if (base instanceof Identificador id) {
            return contexto.tamaniosDeArreglo(id.getNombreIdentificador());
        }

        return null;
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