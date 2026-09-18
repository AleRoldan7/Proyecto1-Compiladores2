package ast.expresiones;

import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LlamadaFuncion extends Expresion {

    private String nombre;
    private List<Expresion> argumentos;

    public LlamadaFuncion(int linea, int columna, String nombre, List<Expresion> argumentos) {
        super(linea, columna);
        this.nombre = nombre;
        this.argumentos = argumentos;
    }


    @Override
    public String generarC3D(ContextoC3D contexto) {

        // 1. Evaluar cada argumento y emitir 'param' en orden
        if (argumentos != null) {
            for (Expresion arg : argumentos) {
                String valor = arg.generarC3D(contexto);
                contexto.agregar("param", valor, null, null);
            }
        }

        // 2. Llamar a la función y guardar el resultado en un temporal
        String temporal = contexto.nuevoTemporal();
        int cantidadArgs = (argumentos == null) ? 0 : argumentos.size();

        contexto.agregar("call", nombre, String.valueOf(cantidadArgs), temporal);

        return temporal;
    }
}
