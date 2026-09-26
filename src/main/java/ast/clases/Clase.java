package ast.clases;

import ast.NodoAST;
import c3d.ContextoC3D;
import enums.TipoDato;
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
            if (constructores == null || constructores.isEmpty()) {
                generarConstructorPorDefecto(contexto);
            } else {
                for (Constructor c : constructores) c.generarC3D(contexto);
            }
        } finally {
            contexto.setClaseActual(null);
        }
        return null;
    }


    public void registrarDisposicion(ContextoC3D contexto) {

        List<String> nombres = new ArrayList<>();
        List<TipoDato> tipos = new ArrayList<>();
        for (var atributo : atributos) {                 // ADAPTA
            nombres.add(atributo.getNombreAtributo());            // ADAPTA
            String tipoNombre = atributo.getTipo() != null ? atributo.getTipo().getNombre() : null;
            tipos.add(convertirTipoDatoDesdeNombre(tipoNombre));
        }
        //contexto.registrarClase(nombreClase, nombres);
        contexto.registrarClase(nombreClase, nombres, tipos);

        if (constructores != null) {                        // ADAPTA: tu campo/getter del constructor
            contexto.registrarFuncion(ContextoC3D.nombreConstructor(nombreClase));
        }
    }

    private static TipoDato convertirTipoDatoDesdeNombre(String nombreTipo) {
        if (nombreTipo == null) {
            return TipoDato.DESCONOCIDO;
        }

        return switch (nombreTipo.toLowerCase()) {
            case "entero", "int" -> TipoDato.ENTERO;
            case "decimal", "double", "float" -> TipoDato.DECIMAL;
            case "texto", "string", "cadena" -> TipoDato.TEXTO;
            case "caracter", "char" -> TipoDato.CARACTER;
            case "booleano", "bool", "boolean" -> TipoDato.BOOLEANO;
            case "void" -> TipoDato.VOID;
            default -> TipoDato.OBJETO;
        };
    }

    private void generarConstructorPorDefecto(ContextoC3D contexto) {

        String nombreFuncion = ContextoC3D.nombreConstructor(nombreClase);

        contexto.registrarFuncion(nombreFuncion);
        contexto.agregarEtiqueta("func_" + nombreFuncion);
        contexto.agregar("param_decl", nombreClase, "self", null);
        contexto.agregar("return", null, null, null);
        contexto.agregarEtiqueta("end_" + nombreFuncion);
    }
}
