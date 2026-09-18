package ast.clases;

import ast.NodoAST;
import ast.declaraciones.Parametro;
import ast.sentencias.Bloque;
import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Constructor extends NodoAST {

    private String nombreClase;
    private List<Parametro> parametros;
    private Bloque cuerpoConstructor;

    public Constructor(int linea, int columna, String nombreClase, List<Parametro> parametros, Bloque cuerpoConstructor) {
        super(linea, columna);
        this.nombreClase = nombreClase;
        this.parametros = parametros;
        this.cuerpoConstructor = cuerpoConstructor;
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {

        String nombreFuncion = "init_" + nombreClase;

        contexto.registrarFuncion(nombreFuncion);
        contexto.agregarEtiqueta("func_" + nombreFuncion);

        // Declarar parámetros (self incluido como parámetro 0)
        contexto.agregar("param_decl", nombreClase, "self", null);

        if (parametros != null) {
            for (Parametro p : parametros) {
                contexto.agregar("param_decl",
                        p.getTipoParametro().getNombre(),
                        p.getNombreParametro(),
                        null);
            }
        }

        // Cuerpo
        cuerpoConstructor.generarC3D(contexto);

        // Retorno implícito
        contexto.agregar("return", null, null, null);

        contexto.agregarEtiqueta("end_" + nombreFuncion);

        return null;
    }
}
