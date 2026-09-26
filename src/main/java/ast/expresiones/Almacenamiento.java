package ast.expresiones;

import c3d.ContextoC3D;
import enums.TipoDato;

import java.util.List;

public final class Almacenamiento {

    private Almacenamiento() {}

    public static void guardar(Expresion destino, String valor, ContextoC3D contexto) {

        if (destino instanceof AccesoAtributo acceso) {

            String basePlace = acceso.getBase().generarC3D(contexto);

            if (acceso.isContenedorEsEstructura()) {

                ContextoC3D.CampoLayout layout = contexto.layoutEstructura(acceso.getTipoContenedor(), acceso.getAtributo());

                if (layout.esEmbebido()) {
                    throw new IllegalStateException("No se puede asignar un struct completo de un solo golpe ('"
                            + acceso.getAtributo() + "'); asigna campo por campo.");
                }

                contexto.agregar("field_set", String.valueOf(layout.offset()), valor, basePlace);

            } else {
                contexto.agregar("field_set",
                        String.valueOf(contexto.desplazamiento(acceso.getTipoContenedor(), acceso.getAtributo())), valor, basePlace);
            }
            return;
        }

        if (destino instanceof AccesoArreglo acceso) {
            String arreglo = acceso.getArreglo().generarC3D(contexto);
            String indice = acceso.getIndicesArreglo().get(0).generarC3D(contexto);

            boolean elementoEsEstructura = acceso.getTipoBaseElemento() != null
                    && contexto.esEstructura(acceso.getTipoBaseElemento());

            int anchoBase = elementoEsEstructura
                    ? contexto.tamanioEstructura(acceso.getTipoBaseElemento()) : 1;

            // Igual que en AccesoArreglo.generarC3D(): si esta asignación es a
            // una dimensión intermedia de un arreglo multidimensional de
            // primitivos (ej. matriz[i][j] = 5, aquí escribiendo el índice j
            // de la fila i), el paso de 'i' avanza el tamaño de una fila
            // completa, no de una celda.
            int anchoElemento = anchoBase;
            List<Integer> tamanios = AccesoArreglo.tamaniosDeclarados(contexto, acceso.getArreglo());
            int nivel = AccesoArreglo.nivelDeIndices(acceso.getArreglo());

            if (tamanios != null && nivel < tamanios.size()) {
                int dimensionesRestantesTrasEste = tamanios.size() - nivel - 1;
                if (dimensionesRestantesTrasEste > 0) {
                    for (int d = nivel + 1; d < tamanios.size(); d++) {
                        anchoElemento *= tamanios.get(d);
                    }
                }
            }

            String offset = indice;
            if (anchoElemento != 1) {
                offset = contexto.binaria("*", indice, String.valueOf(anchoElemento), TipoDato.ENTERO);
            }

            contexto.agregar("index_set", offset, valor, arreglo);
            return;
        }

        if (destino instanceof Identificador id) {
            String nombre = id.getNombreIdentificador();

            if (!contexto.esLocal(nombre) && contexto.esAtributo(nombre)) {
                contexto.agregar("field_set",
                        String.valueOf(contexto.desplazamiento(contexto.getClaseActual(), nombre)), valor, "self");
            } else {
                contexto.asignar(nombre, valor);
            }
            return;
        }

        throw new IllegalStateException("Destino de asignación no soportado: " + destino.getClass().getSimpleName());
    }
}