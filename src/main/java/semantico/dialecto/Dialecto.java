package semantico.dialecto;

import enums.TipoDato;

/**
 * Un Dialecto encapsula TODO lo que difiere entre los tres lenguajes del
 * proyecto (Y?, Zetariano, Pig Latin).
 *
 * La idea central: el AST, la TablaSimbolos, la TablaTipos y los
 * Analizador* / Inferidor* son COMPARTIDOS. Lo único que cambia entre
 * lenguajes es:
 *
 *   1. Cómo se ESCRIBEN los tipos    ("entero" vs "int" vs "numerus")
 *   2. Qué se PERMITE declarar        (Y? no tiene variables globales,
 *                                      Pig Latin no define estructuras)
 *   3. Las palabras de los mensajes   (para que el error salga en el
 *                                      idioma del lenguaje que falló)
 *
 * Así, en vez de tener tres copias del analizador semántico, tenés UNA
 * sola y tres Dialectos. Cuando el enunciado diga "en Y? no se pueden
 * declarar variables globales", eso es UNA línea en DialectoY, no un
 * if regado por diez analizadores.
 */
public interface Dialecto {

    /** Nombre legible del lenguaje, para los mensajes de error. */
    String nombre();

    /* =====================================================
       ================ SISTEMA DE TIPOS ===================
       ===================================================== */

    /**
     * Traduce el nombre de tipo TAL COMO SE ESCRIBE en este lenguaje a la
     * representación canónica interna.
     *
     * "entero"/"int"/"numerus"  -> TipoDato.ENTERO
     * "cadena"/"String"/"textum" -> TipoDato.TEXTO
     *
     * Devuelve null si el nombre no es un tipo primitivo del lenguaje
     * (entonces es una estructura/clase definida por el usuario, y se
     * resuelve contra la TablaTipos).
     */
    TipoDato tipoPrimitivo(String nombreEnElLenguaje);

    /**
     * El camino inverso: cómo se escribe un tipo canónico en este
     * lenguaje. Se usa para que los mensajes de error hablen el idioma
     * del archivo que falló ("se esperaba numerus", no "se esperaba int").
     */
    String nombrarTipo(TipoDato tipoDato);

    /* =====================================================
       ============= REGLAS PROPIAS DEL LENGUAJE ===========
       ===================================================== */

    /** Y? -> false (el enunciado lo prohíbe explícitamente). */
    boolean permiteVariablesGlobales();

    /** Pig Latin -> false (debe importarlas de un .y). */
    boolean permiteDefinirEstructuras();

    /** Solo Zetariano define clases. */
    boolean permiteDefinirClases();

    /** Solo Zetariano: el archivo debe llamarse como la clase. */
    boolean archivoDebeCoincidirConClase();

    /** Solo Zetariano soporta el operador módulo (%) según el enunciado. */
    boolean permiteModulo();

    /* =====================================================
       ============ COMPATIBILIDAD DE TIPOS ================
       ===================================================== */

    /**
     * Tabla de compatibilidad: ¿qué tipo resulta de aplicar 'operador'
     * entre 'izquierda' y 'derecha'? Devuelve null si la operación NO es
     * válida en este lenguaje.
     *
     * Este es exactamente el entregable "Tabla de compatibilidad de tipos"
     * que pide la documentación técnica — implementarlo acá significa que
     * la tabla del manual y el comportamiento del compilador no se pueden
     * desincronizar.
     */
    TipoDato resultadoDe(String operador, TipoDato izquierda, TipoDato derecha);

    /**
     * ¿Se puede guardar un valor de tipo 'real' en algo declarado como
     * 'esperado'? Contempla la promoción implícita (ENTERO -> DECIMAL).
     */
    boolean asignable(TipoDato esperado, TipoDato real);
}