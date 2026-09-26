package ast.expresiones;

import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;
import semantico.Tipos;

import java.util.List;

@Getter
@Setter
public class CrearArreglo extends Expresion {

    private String tipoBase;
    private List<Expresion> dimensiones;
    private List<Expresion> valoresIniciales;

    public CrearArreglo(int linea, int columna, String tipoBase,
                        List<Expresion> dimensiones, List<Expresion> valoresIniciales) {
        super(linea, columna);
        this.tipoBase = tipoBase;
        this.dimensiones = dimensiones;
        this.valoresIniciales = valoresIniciales;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {

        //Calcular el tamaño total
        String total = null;

        if (dimensiones != null && !dimensiones.isEmpty()) {
            for (Expresion dim : dimensiones) {
                String d = dim.generarC3D(contexto);
                if (total == null) {
                    total = d;
                } else {
                    total = contexto.binaria("*", total, d, enums.TipoDato.ENTERO);
                }
            }
        }

        if (total == null) total = "0";

        int anchoElemento = contexto.esEstructura(tipoBase) ? contexto.tamanioEstructura(tipoBase) : 1;

        String totalCeldas = total;
        if (anchoElemento != 1) {
            totalCeldas = contexto.binaria("*", total, String.valueOf(anchoElemento), enums.TipoDato.ENTERO);
        }

        String temporal = contexto.nuevoTemporal();
        contexto.agregar("new_array", tipoBase, totalCeldas, temporal);

        //Inicializar con valores si los hay
        if (valoresIniciales != null) {
            for (int i = 0; i < valoresIniciales.size(); i++) {
                String valor = valoresIniciales.get(i).generarC3D(contexto);
                contexto.agregar("index_set", temporal, String.valueOf(i), valor);
            }
        }

        return temporal;
    }

}
