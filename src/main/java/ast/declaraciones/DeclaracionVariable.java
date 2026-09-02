package ast.declaraciones;

import ast.expresiones.Expresion;
import ast.tipos.Tipo;
import c3d.ContextoC3D;
import enums.TipoDato;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeclaracionVariable extends Declaracion {

    private Tipo tipo;
    private String nombre;
    private Expresion inicializacion;


    public DeclaracionVariable(int linea, int columna, Tipo tipo, String nombre, Expresion inicializacion) {
        super(linea, columna);

        this.tipo = tipo;
        this.nombre = nombre;
        this.inicializacion = inicializacion;
    }

    @Override
    public void generarC3D(ContextoC3D contexto) {

        if (inicializacion == null) {
            return;
        }
        inicializacion.generarC3D(contexto);
        contexto.agregarConTipo("=", inicializacion.getResultado(), null, nombre, mapeoRapido(tipo.getNombre()));
    }

    private TipoDato mapeoRapido(String nombreTipo) {
        return switch (nombreTipo) {
            case "entero" -> TipoDato.ENTERO;
            case "flotante" -> TipoDato.DECIMAL;
            case "cadena" -> TipoDato.TEXTO;
            case "caracter" -> TipoDato.CARACTER;
            case "bool" -> TipoDato.BOOLEANO;
            default -> TipoDato.DESCONOCIDO;
        };
    }
}
