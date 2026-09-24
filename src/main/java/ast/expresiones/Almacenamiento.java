package ast.expresiones;

import c3d.ContextoC3D;

public final class Almacenamiento {

    private Almacenamiento() {}

    /** Escribe 'valor' en lo que representa 'destino'. */
    public static void guardar(Expresion destino, String valor, ContextoC3D contexto) {

        if (destino instanceof AccesoAtributo acceso) {
            String objeto = acceso.getObjeto().generarC3D(contexto);            // ADAPTA: getter de la base
            contexto.agregar("field_set", String.valueOf(acceso.desplazamiento(contexto)), valor, objeto);
            return;
        }

        if (destino instanceof AccesoArreglo acceso) {
            String arreglo = acceso.getArreglo().generarC3D(contexto);
            String indice = acceso.getIndicesArreglo().get(0).generarC3D(contexto);   // 1 dimensión; para N ver generarIndiceAplanado
            contexto.agregar("index_set", indice, valor, arreglo);
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