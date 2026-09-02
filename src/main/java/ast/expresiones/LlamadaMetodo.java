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
    public void generarC3D(ContextoC3D contexto) {

    }
}
