package semantico.dialecto;

import enums.TipoDato;

import java.util.Map;

/**
 * Zetariano (.z) — orientado a objetos, basado en Java.
 *
 * Reglas del enunciado:
 *  - El archivo debe llamarse como la clase definida en él.
 *  - Los objetos viven en el heap.
 *  - Sin herencia / polimorfismo / encapsulamiento en esta entrega.
 *  - Soporta sobrecarga de métodos y constructores, y recursividad.
 *  - Es el único de los tres con operador módulo (%).
 */
public class DialectoZetariano extends DialectoBase {

    private static final Map<String, TipoDato> TIPOS = Map.of(
            "int",     TipoDato.ENTERO,
            "double",  TipoDato.DECIMAL,
            "String",  TipoDato.TEXTO,
            "char",    TipoDato.CARACTER,
            "boolean", TipoDato.BOOLEANO,
            "void",    TipoDato.VOID
    );

    private static final Map<TipoDato, String> NOMBRES = Map.of(
            TipoDato.ENTERO,   "int",
            TipoDato.DECIMAL,  "double",
            TipoDato.TEXTO,    "String",
            TipoDato.CARACTER, "char",
            TipoDato.BOOLEANO, "boolean",
            TipoDato.VOID,     "void"
    );

    @Override protected Map<String, TipoDato> diccionarioDeTipos() { return TIPOS; }
    @Override protected Map<TipoDato, String> nombresDeTipos()     { return NOMBRES; }

    @Override public String  nombre()                        { return "Zetariano"; }
    @Override public boolean permiteVariablesGlobales()       { return false; }
    @Override public boolean permiteDefinirEstructuras()      { return false; }
    @Override public boolean permiteDefinirClases()           { return true; }
    @Override public boolean archivoDebeCoincidirConClase()   { return true; }
    @Override public boolean permiteModulo()                  { return true; }
}