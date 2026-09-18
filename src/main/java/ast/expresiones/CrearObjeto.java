package ast.expresiones;

import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CrearObjeto extends Expresion {

    private String nombreClase;
    private List<Expresion> argumentos;

    public CrearObjeto(int linea, int columna, String nombreClase, List<Expresion> argumentos) {
        super(linea, columna);
        this.nombreClase = nombreClase;
        this.argumentos = argumentos;
    }
    @Override
    public String generarC3D(ContextoC3D contexto) {

        // 1. Reservar en heap
        String temporal = contexto.nuevoTemporal();
        contexto.agregar("new", nombreClase, null, temporal);

        // 2. Evaluar argumentos para el constructor
        if (argumentos != null) {
            for (Expresion arg : argumentos) {
                String valor = arg.generarC3D(contexto);
                contexto.agregar("param", valor, null, null);
            }
        }

        // 3. Pasar el objeto como self
        contexto.agregar("param", temporal, null, null);

        // 4. Llamar al constructor
        int cantidadArgs = ((argumentos == null) ? 0 : argumentos.size()) + 1;
        contexto.agregar("call", "init_" + nombreClase, String.valueOf(cantidadArgs), null);

        return temporal;
    }
}
