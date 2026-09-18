package ast.clases;

import ast.NodoAST;
import ast.expresiones.Expresion;
import ast.tipos.Tipo;
import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Atributo extends NodoAST {

    private Tipo tipo;
    private String nombreAtributo;

    private Expresion inicializacion;

    private List<Expresion> dimensiones;

    private List<Expresion> valoresIniciales;


    public Atributo(int linea, int columna, Tipo tipo, String nombreAtributo) {
        super(linea, columna);
        this.tipo = tipo;
        this.nombreAtributo = nombreAtributo;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {
        // Un atributo no emite cuartetas por sí solo. La reserva de memoria
        // del objeto se hace en 'new', y la inicialización en el constructor.
        return null;
    }
}
