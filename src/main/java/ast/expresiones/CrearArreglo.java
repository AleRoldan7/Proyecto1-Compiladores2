package ast.expresiones;

import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CrearArreglo extends Expresion {

    private String tipoBase;
    private List<Expresion> dimensiones;
    private List<Expresion> valoresIniciales;

    public CrearArreglo(int linea, int columna, String tipoBase,
                        List<Expresion> dimensiones, List<Expresion> valoresIniciales) {
        super(linea, columna);
        this.tipoBase = tipoBase;
        this.dimensiones = dimensiones;
        this.valoresIniciales = valoresIniciales;
    }

    @Override
    public void generarC3D(ContextoC3D contexto) {
        // Implementar generación de código C3D para creación de arreglos
    }

}
