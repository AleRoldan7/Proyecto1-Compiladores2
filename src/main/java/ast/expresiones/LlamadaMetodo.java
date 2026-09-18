package ast.expresiones;

import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LlamadaMetodo extends Expresion {

    private Expresion objeto;
    private String metodo;
    private List<Expresion> argumentos;

    public LlamadaMetodo(int linea, int columna, Expresion objeto, String metodo, List<Expresion> argumentos) {
        super(linea, columna);
        this.objeto = objeto;
        this.metodo = metodo;
        this.argumentos = argumentos;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {

        // 1. Evaluar el objeto (self)
        String obj = objeto.generarC3D(contexto);

        // 2. El objeto se pasa como primer parámetro (self)
        contexto.agregar("param", obj, null, null);

        // 3. Evaluar los argumentos
        if (argumentos != null) {
            for (Expresion arg : argumentos) {
                String valor = arg.generarC3D(contexto);
                contexto.agregar("param", valor, null, null);
            }
        }

        // 4. Llamar al método
        String temporal = contexto.nuevoTemporal();
        int cantidadArgs = ((argumentos == null) ? 0 : argumentos.size()) + 1;

        contexto.agregar("call", metodo, String.valueOf(cantidadArgs), temporal);

        return temporal;
    }
}
