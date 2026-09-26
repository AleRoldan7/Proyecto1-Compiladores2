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

        String total = "1";

        if (dimensiones != null) {
            for (Expresion dim : dimensiones) {
                if (dim == null) continue;

                String d = dim.generarC3D(contexto);

                total = contexto.binaria(
                        "*",
                        total,
                        d,
                        TipoDato.ENTERO
                );
            }
        }

        String tipoBase = tipo.getNombre();

        if (tipoBase.contains("[")) {
            tipoBase = tipoBase.substring(
                    0,
                    tipoBase.indexOf("[")
            );
        }

        boolean esEstructura =
                contexto.esEstructura(tipoBase);

        int anchoElemento = esEstructura
                ? contexto.tamanioEstructura(tipoBase)
                : 1;

        contexto.registrarTipoBaseArreglo(
                nombre,
                tipoBase
        );

        String totalCeldas = total;

        if (anchoElemento != 1) {
            totalCeldas = contexto.binaria(
                    "*",
                    total,
                    String.valueOf(anchoElemento),
                    TipoDato.ENTERO
            );
        }

        contexto.agregar(
                "new_array",
                tipoBase,
                totalCeldas,
                nombre
        );

        /*
         * Si es un arreglo de estructuras, cada estructura puede
         * tener campos que son arreglos.
         *
         * Ejemplo:
         *
         * Persona:
         *   nombre
         *   edad
         *   notas[3]
         *
         * Persona[3]:
         *   se crean 3 arreglos de notas.
         */
        if (esEstructura) {

            List<ContextoC3D.CampoDef> campos =
                    contexto.getDefinicionesEstructura()
                            .get(tipoBase);

            if (campos != null) {

                int cantidadElementos = 0;

                if (dimensiones != null
                        && !dimensiones.isEmpty()
                        && dimensiones.get(0) instanceof Literal lit
                        && lit.getValor() instanceof Integer) {

                    cantidadElementos =
                            (Integer) lit.getValor();
                }

                for (int i = 0; i < cantidadElementos; i++) {

                    String desplazamiento;

                    if (i == 0) {
                        desplazamiento = "0";
                    } else {
                        desplazamiento = contexto.binaria(
                                "*",
                                String.valueOf(i),
                                String.valueOf(anchoElemento),
                                TipoDato.ENTERO
                        );
                    }

                    String estructura =
                            contexto.binaria(
                                    "+",
                                    nombre,
                                    desplazamiento,
                                    TipoDato.ESTRUCTURA
                            );

                    for (ContextoC3D.CampoDef campo : campos) {

                        if (campo.anchoDeclarado() > 1) {

                            String arregloCampo =
                                    contexto.nuevoTemporal();

                            contexto.agregar(
                                    "new_array",
                                    campo.tipo().name().toLowerCase(),
                                    String.valueOf(campo.anchoDeclarado()),
                                    arregloCampo
                            );

                            ContextoC3D.CampoLayout layout =
                                    contexto.layoutEstructura(
                                            tipoBase,
                                            campo.nombre()
                                    );

                            contexto.agregar(
                                    "field_set",
                                    String.valueOf(layout.offset()),
                                    arregloCampo,
                                    estructura
                            );
                        }
                    }
                }
            }
        }

        if (valorInicial != null) {

            for (int i = 0;
                 i < valorInicial.size();
                 i++) {

                String valor =
                        valorInicial.get(i)
                                .generarC3D(contexto);

                String offset =
                        String.valueOf(i);

                if (anchoElemento != 1) {

                    offset = contexto.binaria(
                            "*",
                            String.valueOf(i),
                            String.valueOf(anchoElemento),
                            TipoDato.ENTERO
                    );
                }

                contexto.agregar(
                        "index_set",
                        offset,
                        valor,
                        nombre
                );
            }
        }

        List<Integer> tamaniosInt =
                new ArrayList<>();

        if (dimensiones != null) {

            for (Expresion dim : dimensiones) {

                if (dim instanceof Literal lit
                        && lit.getValor() instanceof Integer) {

                    tamaniosInt.add(
                            (Integer) lit.getValor()
                    );
                }
            }
        }

        if (!tamaniosInt.isEmpty()) {

            contexto.registrarTamaniosArreglo(
                    nombre,
                    tamaniosInt
            );
        }

        return nombre;
    }
}
