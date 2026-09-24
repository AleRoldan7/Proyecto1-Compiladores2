package ast.expresiones;

import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
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

        List<String> lugares = new ArrayList<>();

        if (getArgumentos() != null) {
            for (Expresion argumento : getArgumentos()) {
                lugares.add(argumento.generarC3D(contexto));
            }
        }

        String objeto = contexto.nuevoTemporal();
        contexto.agregar("new", getNombreClase(), String.valueOf(contexto.tamanio(getNombreClase())), objeto);

        String constructor = ContextoC3D.nombreConstructor(getNombreClase());

        if (contexto.existeFuncion(constructor)) {
            contexto.agregar("param", objeto, null, null);
            for (String lugar : lugares) {
                contexto.agregar("param", lugar, null, null);
            }
            contexto.agregar("call", constructor, String.valueOf(lugares.size() + 1), null);
        }

        return objeto;
    }
}
