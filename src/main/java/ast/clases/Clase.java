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
        for (Metodo m : metodos) m.generarC3D(contexto);
        for (Constructor c : constructores) c.generarC3D(contexto);
        return null;
    }

    public void registrarDisposicion(ContextoC3D contexto) {
        List<String> nombres = new ArrayList<>();
        for (var atributo : atributos) {                 // ADAPTA: nombre real de la lista de atributos
            nombres.add(atributo.getNombreAtributo());           // ADAPTA: getter real del nombre
        }
        contexto.registrarClase(nombreClase, nombres);
    }
}
