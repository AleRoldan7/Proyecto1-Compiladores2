package ast;

import ast.clases.Clase;
import ast.declaraciones.Declaracion;
import ast.declaraciones.DeclaracionFuncion;
import ast.estructuras.Estructura;
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
}
