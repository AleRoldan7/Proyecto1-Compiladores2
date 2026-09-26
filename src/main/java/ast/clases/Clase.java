package ast.clases;

import ast.NodoAST;
import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Clase extends NodoAST {

    private String nombreClase;
    private List<Atributo> atributos;
    private List<Constructor> constructores;
    private List<Metodo> metodos;

    public Clase(int linea, int columna, String nombreClase, List<Atributo> atributos, List<Constructor> constructores, List<Metodo> metodos) {
        super(linea, columna);
        this.nombreClase = nombreClase;
        this.atributos = atributos;
        this.constructores = constructores;
        this.metodos = metodos;
    }


    @Override
    public String generarC3D(ContextoC3D contexto) {
        contexto.setClaseActual(nombreClase);
        try {
            for (Metodo m : metodos) m.generarC3D(contexto);
            for (Constructor c : constructores) c.generarC3D(contexto);
        } finally {
            contexto.setClaseActual(null);
        }
        return null;
    }

    public void registrarDisposicion(ContextoC3D contexto) {

        List<String> nombres = new ArrayList<>();
        for (var atributo : atributos) {                 // ADAPTA
            nombres.add(atributo.getNombreAtributo());            // ADAPTA
        }
        contexto.registrarClase(nombreClase, nombres);

        if (constructores != null) {                        // ADAPTA: tu campo/getter del constructor
            contexto.registrarFuncion(ContextoC3D.nombreConstructor(nombreClase));
        }
    }
}
