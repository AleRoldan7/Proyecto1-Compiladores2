package ast.expresiones;

import c3d.ContextoC3D;
import enums.TipoDato;

public final class Almacenamiento {

    private Almacenamiento() {}

    /** Escribe 'valor' en lo que representa 'destino'. */
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

            int anchoElemento = (acceso.getTipoBaseElemento() != null && contexto.esEstructura(acceso.getTipoBaseElemento()))
                    ? contexto.tamanioEstructura(acceso.getTipoBaseElemento()) : 1;

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