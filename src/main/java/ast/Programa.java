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

    @Override
    public String generarC3D(ContextoC3D contexto) {

        // 1. Estructuras: no generan cuartetas.
        //    Su descripción ya quedó en TablaTipos.

        // 2. Clases de Zetariano (u otras): cada una genera sus métodos y
        //    constructores.
        if (clases != null) {
            for (Clase c : clases) {
                c.generarC3D(contexto);
            }
        }

        // 3. Funciones globales (Y? / Pig Latin)
        if (funciones != null) {
            for (DeclaracionFuncion f : funciones) {
                f.generarC3D(contexto);
            }
        }

        // 4. Variables globales (Pig Latin: VARIABILES>)
        if (declaraciones != null) {
            for (Declaracion d : declaraciones) {
                d.generarC3D(contexto);
            }
        }

        // 5. Halt final: detiene el programa
        contexto.agregar("halt", null, null, null);

        return null;
    }
}
