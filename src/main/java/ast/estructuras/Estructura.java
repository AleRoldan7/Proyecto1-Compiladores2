package ast.estructuras;

import ast.NodoAST;
import c3d.ContextoC3D;
import enums.TipoDato;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Estructura extends NodoAST {

    private String nombre;
    private List<Campo> campos;

    public Estructura(int linea, int columna, String nombre, List<Campo> campos) {
        super(linea, columna);
        this.nombre = nombre;
        this.campos = campos;
    }

    /**
     * Registra el layout de esta estructura en el heap. Debe llamarse en el
     * orden en que aparecen en el .y — una estructura que anida otra necesita
     * que la anidada ya esté registrada (igual que exige AnalizadorEstructura).
     */
    public void registrarDisposicion(ContextoC3D contexto) {

        List<ContextoC3D.CampoDef> defs = new ArrayList<>();

        for (Campo campo : campos) {
            String tipoCampo = campo.getTipo().getNombre();
            boolean esEmbebido = contexto.esEstructura(tipoCampo);

            TipoDato tipoDato = esEmbebido
                    ? TipoDato.ESTRUCTURA
                    : convertirTipoDatoDesdeNombre(tipoCampo);

            defs.add(new ContextoC3D.CampoDef(
                    campo.getNombre(),
                    esEmbebido,
                    esEmbebido ? tipoCampo : null,
                    tipoDato
            ));
        }

        contexto.registrarEstructura(nombre, defs);
    }

    @Override
    public String generarC3D(ContextoC3D contexto) {
        return null;   // una declaración de estructura no genera código, solo registra su forma
    }

    private static TipoDato convertirTipoDatoDesdeNombre(String nombreTipo) {

        if (nombreTipo == null) {
            return TipoDato.DESCONOCIDO;
        }

        return switch (nombreTipo.toLowerCase()) {
            case "entero", "int"               -> TipoDato.ENTERO;
            case "decimal", "double", "float"  -> TipoDato.DECIMAL;
            case "texto", "string", "cadena"   -> TipoDato.TEXTO;
            case "caracter", "char"            -> TipoDato.CARACTER;
            case "booleano", "bool", "boolean" -> TipoDato.BOOLEANO;
            case "void"                        -> TipoDato.VOID;
            default                            -> TipoDato.OBJETO;
        };
    }
}