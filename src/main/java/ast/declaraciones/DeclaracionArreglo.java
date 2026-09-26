package ast.declaraciones;

import ast.expresiones.Expresion;
import ast.expresiones.Literal;
import ast.tipos.Tipo;
import c3d.ContextoC3D;
import enums.TipoDato;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DeclaracionArreglo extends Declaracion {

    private Tipo tipo;
    private String nombre;
    private List<Expresion> dimensiones;
    private List<Expresion> valorInicial;

    public DeclaracionArreglo(int linea, int columna,  Tipo tipo,  String nombre, List<Expresion> dimensiones, List<Expresion> valorInicial) {
        super(linea, columna);

        this.tipo = tipo;
        this.nombre = nombre;
        this.dimensiones = dimensiones;
        this.valorInicial = valorInicial;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {

        // 1. Calcular el tamaño total (producto de las dimensiones)
        String total = "1";

        if (dimensiones != null) {
            for (Expresion dim : dimensiones) {
                if (dim == null) continue;
                String d = dim.generarC3D(contexto);
                total = contexto.binaria("*", total, d, TipoDato.ENTERO);
            }
        }

        // 2. Determinar el tipo base (quitar corchetes si los hay)
        String tipoBase = tipo.getNombre();
        if (tipoBase.contains("[")) {
            tipoBase = tipoBase.substring(0, tipoBase.indexOf("["));
        }

        // 3. Determinar el ancho de cada elemento
        boolean esEstructura = contexto.esEstructura(tipoBase);
        int anchoElemento = esEstructura
                ? contexto.tamanioEstructura(tipoBase)
                : 1;

        // 4. Registrar el tipo base del arreglo en el contexto
        contexto.registrarTipoBaseArreglo(nombre, tipoBase);

        // 5. Calcular el total de celdas (numElementos * anchoElemento)
        String totalCeldas = total;
        if (anchoElemento != 1) {
            totalCeldas = contexto.binaria("*", total, String.valueOf(anchoElemento), TipoDato.ENTERO);
        }

        // 6. Emitir new_array
        contexto.agregar("new_array", tipoBase, totalCeldas, nombre);

        // 7. Inicializar con valores si los hay
        if (valorInicial != null) {
            for (int i = 0; i < valorInicial.size(); i++) {
                String valor = valorInicial.get(i).generarC3D(contexto);

                // Si el elemento es una estructura, el offset debe ser i * anchoElemento
                String offset = String.valueOf(i);
                if (anchoElemento != 1) {
                    offset = contexto.binaria("*", String.valueOf(i), String.valueOf(anchoElemento), TipoDato.ENTERO);
                }

                contexto.agregar("index_set", offset, valor, nombre);
            }
        }

        // 8. Registrar tamaños de dimensiones (para arreglos multidimensionales)
        List<Integer> tamaniosInt = new ArrayList<>();
        if (dimensiones != null) {
            for (Expresion dim : dimensiones) {
                if (dim instanceof Literal lit && lit.getValor() instanceof Integer) {
                    tamaniosInt.add((Integer) lit.getValor());
                }
            }
        }
        if (!tamaniosInt.isEmpty()) {
            contexto.registrarTamaniosArreglo(nombre, tamaniosInt);
        }

        return nombre;
    }
}
