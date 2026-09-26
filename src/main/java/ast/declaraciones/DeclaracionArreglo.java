package ast.declaraciones;

import ast.expresiones.Expresion;
import ast.tipos.Tipo;
import c3d.ContextoC3D;
import enums.TipoDato;
import lombok.Getter;
import lombok.Setter;

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

        // 2. Reservar en heap: si el tipo base es una estructura, cada elemento
        //    ocupa varias celdas (su ancho), no una sola.
        int anchoElemento = contexto.esEstructura(tipo.getNombre())
                ? contexto.tamanioEstructura(tipo.getNombre())
                : 1;

        String totalCeldas = total;
        if (anchoElemento != 1) {
            totalCeldas = contexto.binaria("*", total, String.valueOf(anchoElemento), TipoDato.ENTERO);
        }

        contexto.agregar("new_array", tipo.getNombre(), totalCeldas, nombre);

        // 3. Inicializar con valores si los hay
        if (valorInicial != null) {
            for (int i = 0; i < valorInicial.size(); i++) {
                String valor = valorInicial.get(i).generarC3D(contexto);
                contexto.agregar("index_set", String.valueOf(i), valor, nombre);
            }
        }

        return nombre;
    }
}
