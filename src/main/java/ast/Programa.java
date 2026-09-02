package ast;

import ast.clases.Clase;
import ast.declaraciones.Declaracion;
import ast.declaraciones.DeclaracionFuncion;
import ast.estructuras.Estructura;
import c3d.ContextoC3D;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Programa extends NodoAST {

    private List<String> importaciones;
    private List<Estructura> estructuras;
    private List<Clase> clases;
    private List<DeclaracionFuncion> funciones;
    private List<Declaracion> declaraciones;

    public Programa(int linea, int columna, List<String> importaciones, List<Estructura> estructuras,
                    List<Clase> clases, List<DeclaracionFuncion> funciones, List<Declaracion> declaraciones) {

        super(linea, columna);

        this.importaciones = importaciones;
        this.estructuras = estructuras;
        this.clases = clases;
        this.funciones = funciones;
        this.declaraciones = declaraciones;
    }

    @Override
    public String toString() {
        return "Programa{" +
                "estructuras=" + estructuras +
                ", funciones=" + funciones +
                '}';
    }

    // Programa.java
    @Override
    public void generarC3D(ContextoC3D contexto) {
        // estructuras y clases: no generan cuartetas, solo describen tipos
        // (su información ya quedó en TablaTipos durante el análisis semántico)

        if (declaraciones != null) {
            for (Declaracion d : declaraciones) {
                d.generarC3D(contexto); // variables globales (Pig Latin: sección VARIABILES>)
            }
        }

        if (funciones != null) {
            for (DeclaracionFuncion f : funciones) {
                f.generarC3D(contexto);
            }
        }
    }
}
