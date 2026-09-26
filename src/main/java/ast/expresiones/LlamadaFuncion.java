package ast.expresiones;

import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class LlamadaFuncion extends Expresion {

    private String nombre;
    private List<Expresion> argumentos;
    private boolean metodoDeClase;

    public LlamadaFuncion(int linea, int columna, String nombre, List<Expresion> argumentos) {
        super(linea, columna);
        this.nombre = nombre;
        this.argumentos = argumentos;
    }


    @Override
    public String generarC3D(ContextoC3D contexto) {

        List<String> lugares = new ArrayList<>();
        if (getArgumentos() != null) {
            for (Expresion argumento : getArgumentos()) {
                lugares.add(argumento.generarC3D(contexto));
            }
        }

        if (ContextoC3D.esImpresion(getNombre())) {
            for (String lugar : lugares) {
                contexto.agregar("print", lugar, null, null);
            }
            return null;
        }

        if (ContextoC3D.esLectura(getNombre())) {
            String leido = contexto.nuevoTemporal();
            contexto.agregar("read", null, null, leido);
            return leido;
        }

        if (metodoDeClase) {
            contexto.agregar("param", "self", null, null);
        }

        for (String lugar : lugares) {
            contexto.agregar("param", lugar, null, null);
        }

        // NUEVO: resolver nombre completo
        String nombreReal = metodoDeClase
                ? ContextoC3D.nombreFuncion(contexto.getClaseActual(), getNombre())
                : getNombre();

        String resultado = contexto.nuevoTemporal();
        int cantidad = lugares.size() + (metodoDeClase ? 1 : 0);

        contexto.agregar("call", nombreReal, String.valueOf(cantidad), resultado);
        return resultado;
    }
}
