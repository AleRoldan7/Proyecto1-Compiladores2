package semantico.dialecto;

import enums.TipoDato;

import java.util.Map;

/**
 * Pig Latin (.pig) — lenguaje de ARRANQUE del programa.
 *
 * Reglas del enunciado:
 *  - Es el único con sección de variables GLOBALES (VARIABILES>).
 *  - Ya NO puede definir estructuras propias: las importa de un .y.
 *  - Las funciones no ligadas a un objeto deben venir de un .y.
 *  - Contiene la función principal obligatoria (MAIOR>).
 */
public class DialectoPigLatin extends DialectoBase {

    private static final Map<String, TipoDato> TIPOS = Map.of(
            "numerus",   TipoDato.ENTERO,
            "decimalis", TipoDato.DECIMAL,
            "textum",    TipoDato.TEXTO,
            "littera",   TipoDato.CARACTER,
            "bool",      TipoDato.BOOLEANO
    );

    private static final Map<TipoDato, String> NOMBRES = Map.of(
            TipoDato.ENTERO,   "numerus",
            TipoDato.DECIMAL,  "decimalis",
            TipoDato.TEXTO,    "textum",
            TipoDato.CARACTER, "littera",
            TipoDato.BOOLEANO, "bool"
    );

    @Override protected Map<String, TipoDato> diccionarioDeTipos() { return TIPOS; }
    @Override protected Map<TipoDato, String> nombresDeTipos()     { return NOMBRES; }

    @Override public String  nombre()                        { return "Pig Latin"; }
    @Override public boolean permiteVariablesGlobales()       { return true; }
    @Override public boolean permiteDefinirEstructuras()      { return false; }
    @Override public boolean permiteDefinirClases()           { return false; }
    @Override public boolean archivoDebeCoincidirConClase()   { return false; }
}