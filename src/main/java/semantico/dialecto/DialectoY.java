package semantico.dialecto;

import enums.TipoDato;

import java.util.Map;

/**
 * Y? (.y) — sintaxis tipo Python, indentación significativa.
 *
 * Reglas del enunciado:
 *  - SOLO se pueden definir estructuras y funciones.
 *  - NO se pueden definir variables globales (por eso
 *    permiteVariablesGlobales() es false: un analizador compartido lo
 *    consulta y reporta el error sin necesidad de un analizador aparte).
 *  - Los arreglos y estructuras se pasan por referencia; los primitivos
 *    por valor.
 *  - Case sensitive.
 */
public class DialectoY extends DialectoBase {

    private static final Map<String, TipoDato> TIPOS = Map.of(
            "entero",   TipoDato.ENTERO,
            "flotante", TipoDato.DECIMAL,
            "cadena",   TipoDato.TEXTO,
            "caracter", TipoDato.CARACTER,
            "bool",     TipoDato.BOOLEANO,
            "vacio",    TipoDato.VOID
    );

    private static final Map<TipoDato, String> NOMBRES = Map.of(
            TipoDato.ENTERO,   "entero",
            TipoDato.DECIMAL,  "flotante",
            TipoDato.TEXTO,    "cadena",
            TipoDato.CARACTER, "caracter",
            TipoDato.BOOLEANO, "bool",
            TipoDato.VOID,     "vacio"
    );

    @Override protected Map<String, TipoDato> diccionarioDeTipos() { return TIPOS; }
    @Override protected Map<TipoDato, String> nombresDeTipos()     { return NOMBRES; }

    @Override public String  nombre()                        { return "Y?"; }
    @Override public boolean permiteVariablesGlobales()       { return false; }
    @Override public boolean permiteDefinirEstructuras()      { return true; }
    @Override public boolean permiteDefinirClases()           { return false; }
    @Override public boolean archivoDebeCoincidirConClase()   { return false; }
}