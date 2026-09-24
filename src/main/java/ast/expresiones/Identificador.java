package ast.expresiones;

import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Identificador extends Expresion {

    private String nombreIdentificador;

    public Identificador(int linea, int columna, String nombreIdentificador) {
        super(linea, columna);
        this.nombreIdentificador = nombreIdentificador;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {

        String nombre = getNombreIdentificador();

        if ("this".equals(nombre)) {
            return "self";
        }

        if (!contexto.esLocal(nombre) && contexto.esAtributo(nombre)) {
            String temporal = contexto.nuevoTemporal();
            contexto.agregar("attr_get", "self",
                    String.valueOf(contexto.desplazamiento(contexto.getClaseActual(), nombre)), temporal);
            return temporal;
        }

        return nombre;
    }
}
