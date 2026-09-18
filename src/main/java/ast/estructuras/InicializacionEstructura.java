package ast.estructuras;

import ast.expresiones.Expresion;
import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InicializacionEstructura extends Expresion {

    private String nombreTipo;
    private List<Expresion> valores;

    public InicializacionEstructura(int linea, int columna, String nombreTipo, List<Expresion> valores) {
        super(linea, columna);
        this.nombreTipo = nombreTipo;
        this.valores = valores;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {

        // 1. Reservar un objeto temporal
        String temp = contexto.nuevoTemporal();
        contexto.agregar("new", nombreTipo, null, temp);

        // 2. Asignar cada valor a un campo del objeto.
        //    El nombre del campo se resuelve en el traductor a C a partir
        //    del orden y la definición de la estructura. Aquí usamos
        //    'field_N' como convención.
        if (valores != null) {
            for (int i = 0; i < valores.size(); i++) {
                String v = valores.get(i).generarC3D(contexto);
                contexto.agregar("field_set", temp, String.valueOf(i), v);
            }
        }

        return temp;
    }
}
