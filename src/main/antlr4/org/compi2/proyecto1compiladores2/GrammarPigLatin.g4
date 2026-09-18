grammar GrammarPigLatin;

/* =========================================================
   ======================= PARSER ==========================
   ========================================================= */

/* PROGRAMA */

program:
    seccionImport+
    seccionVariables?
    seccionMain
    EOF
    ;

/* =========================================================
   IMPORTACIONES
   ========================================================= */

seccionImport:
    IMPORT ID (PUNTO ID)* PUNTO ID PUNTO_COMA?
    ;

/* =========================================================
   SECCION DE VARIABLES
   ========================================================= */

seccionVariables:
    SECCIONVARIABLE MAYOR declaracion+
    ;

declaracion:
    declaracionVariable
    | declaracionArreglo
    ;

/* =========================================================
   DECLARACION DE VARIABLES
   ========================================================= */

declaracionVariable:
    ESTO ID DOSPUNTOS tipo
    (expresion | inicializacionStruct)?
    PUNTO_COMA?
    ;

/* =========================================================
   TIPOS
   ========================================================= */

tipo:
    tipoDato
    | ID
    ;

tipoDato:
    NUMEROS
    | DECIMALIS
    | TEXTUM
    | LITTERA
    | BOOL
    ;

/* =========================================================
   DECLARACION DE ARREGLOS
   ========================================================= */

declaracionArreglo:
    SERIES ID
    CORCHETE_ABRE ENTERO CORCHETE_CIERRA
    DOSPUNTOS tipo
    (
        LLAVE_ABRE listaValoresArreglo? LLAVE_CIERRA
    )?
    PUNTO_COMA
    ;

listaValoresArreglo:
    valorArreglo (COMA valorArreglo)*
    ;

valorArreglo:
    expresion
    | inicializacionStruct
    ;

/* =========================================================
   INICIALIZACION DE ESTRUCTURAS
   ========================================================= */

/*
   Ejemplo:

   esto ciudadano : Persona {
       "Valeria",
       25,
       {
           "Avenida Central",
           500
       }
   };
*/

inicializacionStruct:
    LLAVE_ABRE listaValoresInicializacion? LLAVE_CIERRA
    ;

listaValoresInicializacion:
    valorInicializacion (COMA valorInicializacion)*
    ;

valorInicializacion:
    expresion
    | inicializacionStruct
    ;

/* =========================================================
   SECCION MAIN
   ========================================================= */

seccionMain:
    SECCIONMAIN MAYOR
    sentencia*
    FINIS PUNTO_COMA
    ;

/* =========================================================
   SENTENCIAS
   ========================================================= */

sentencia:
    declaracionVariable
    | declaracionArreglo
    | asignacion
    | condicional
    | cicloDum
    | cicloFacere
    | cicloPer
    | imprimir
    | leer
    | llamadaFuncionSentencia
    | incrementoDecremento
    | PERGE PUNTO_COMA
    | INTERRUMPE PUNTO_COMA
    ;

/* =========================================================
   INCREMENTO / DECREMENTO
   ========================================================= */

incrementoDecremento:
    ID acceso*
    (INCREMENTO | DECREMENTO)
    PUNTO_COMA
    ;

/* =========================================================
   ASIGNACIONES
   ========================================================= */

asignacion:
    ID acceso*
    IGUAL
    (expresion | inicializacionStruct)
    PUNTO_COMA
    ;

/* =========================================================
   CONDICIONALES
   ========================================================= */

condicional:
    SI
    PARENTESIS_ABRE expresion PARENTESIS_CIERRA
    bloquePig

    (
        ALITER
        PARENTESIS_ABRE expresion PARENTESIS_CIERRA
        bloquePig
    )*

    (
        ALITER
        bloquePig
    )?

    FINIS PUNTO_COMA
    ;

/** { sentencia* } como sub-regla propia: así cada rama (si / aliter / aliter) tiene
    su PROPIO contexto en el árbol de parseo. Si se deja inline, ANTLR aplana las
    sentencias de TODAS las ramas en una sola lista y se pierde a cuál rama pertenece
    cada una — rompe la construcción del AST del condicional. */
bloquePig:
    LLAVE_ABRE sentencia* LLAVE_CIERRA
    ;

/* =========================================================
   CICLO DUM - WHILE
   ========================================================= */

cicloDum:
    DUM
    PARENTESIS_ABRE expresion PARENTESIS_CIERRA
    LLAVE_ABRE sentencia* LLAVE_CIERRA
    FINIS PUNTO_COMA
    ;

/* =========================================================
   CICLO FACERE - DO WHILE
   ========================================================= */

cicloFacere:
    FACERE
    LLAVE_ABRE sentencia* LLAVE_CIERRA
    DUM
    PARENTESIS_ABRE expresion PARENTESIS_CIERRA
    PUNTO_COMA
    ;

/* =========================================================
   CICLO PER - FOR
   ========================================================= */

cicloPer:
    PER
    PARENTESIS_ABRE

    inicializacionPer
    PUNTO_COMA

    expresion
    PUNTO_COMA

    actualizacionPer

    PARENTESIS_CIERRA

    LLAVE_ABRE sentencia* LLAVE_CIERRA
    ;

/* =========================================================
   INICIALIZACION DEL FOR
   ========================================================= */

inicializacionPer:
    ESTO ID DOSPUNTOS tipo expresion?
    | ID acceso* IGUAL expresion
    ;

/* =========================================================
   ACTUALIZACION DEL FOR
   ========================================================= */

actualizacionPer:
    ID acceso*
    (INCREMENTO | DECREMENTO)

    | ID acceso* IGUAL expresion
    ;

/* =========================================================
   IMPRESION
   ========================================================= */

imprimir:
    MAYORIMPRIMIR
    expresion
    (MAYORIMPRIMIR expresion)*
    PUNTO_COMA
    ;

/* =========================================================
   LECTURA
   ========================================================= */

leer:
    (ID acceso*)?
    MENORLEER
    PUNTO_COMA?
    ;

/* =========================================================
   LLAMADA A FUNCION COMO SENTENCIA
   ========================================================= */

llamadaFuncionSentencia:
    llamadaFuncion PUNTO_COMA
    ;

/* =========================================================
   ====================== EXPRESIONES ======================
   ========================================================= */

/*
   Precedencia:

   OR
       AND
           igualdad
               relacional
                   aditiva
                       multiplicativa
                           unaria
                               primaria
*/

expresion:
    expresionOr
    ;

expresionOr:
    expresionAnd
    (OR expresionAnd)*
    ;

expresionAnd:
    expresionIgualdad
    (AND expresionIgualdad)*
    ;

expresionIgualdad:
    expresionRelacional
    ((COMPARACION | DIFERENCIA) expresionRelacional)*
    ;

expresionRelacional:
    expresionAditiva
    (
        (MENOR | MAYOR | MAYORIGUAL | MENORIGUAL)
        expresionAditiva
    )*
    ;

expresionAditiva:
    expresionMultiplicativa
    ((MAS | RESTA) expresionMultiplicativa)*
    ;

expresionMultiplicativa:
    expresionUnaria
    (
        (MULTIPLICACION | DIVISION | MODULO)
        expresionUnaria
    )*
    ;

expresionUnaria:
    (NEGACION | RESTA) expresionUnaria
    | expresionPrimaria
    ;

/* =========================================================
   EXPRESION PRIMARIA
   ========================================================= */

expresionPrimaria:
    literal
    | creacionObjeto
    | llamadaFuncion
    | ID acceso* (INCREMENTO | DECREMENTO)?
    | PARENTESIS_ABRE expresion PARENTESIS_CIERRA
    ;

/* =========================================================
   CREACION DE OBJETOS
   ========================================================= */

creacionObjeto:
    NOVUS
    ID
    PARENTESIS_ABRE
    listaArgumentos?
    PARENTESIS_CIERRA
    ;

/* =========================================================
   ACCESO A CAMPOS, ARREGLOS Y METODOS
   ========================================================= */

acceso:
    CORCHETE_ABRE expresion CORCHETE_CIERRA
    | PUNTO ID
    | PUNTO ID
      PARENTESIS_ABRE listaArgumentos? PARENTESIS_CIERRA
    ;

/* =========================================================
   LITERALES
   ========================================================= */

literal:
    ENTERO
    | DECIMAL
    | COMILLAS
    | COMILLASSIMPLES
    | VERUM
    | FALSUS
    ;

/* =========================================================
   LLAMADA A FUNCION
   ========================================================= */

llamadaFuncion:
    ID
    PARENTESIS_ABRE
    listaArgumentos?
    PARENTESIS_CIERRA
    ;

listaArgumentos:
    expresion
    (COMA expresion)*
    ;


/* =========================================================
   ======================== LEXER ==========================
   ========================================================= */

/* =========================================================
   ESPACIOS
   ========================================================= */

WS: [ \t\n\r]+ -> skip;

/* =========================================================
   COMENTARIOS
   ========================================================= */
COMENTARIO_LINEA:'//' ~[\r\n]* -> skip;
COMENTARIO_BLOQUE:'/*' .*? '*/' -> skip;

/* =========================================================
   INCREMENTO Y DECREMENTO
   ========================================================= */
INCREMENTO:'++';
DECREMENTO:'--';

/* =========================================================
   OPERADORES ARITMETICOS
   ========================================================= */
MAS:'+';
RESTA:'-';
MULTIPLICACION:'*';
DIVISION:'/';
MODULO:'%';

/* =========================================================
   SIGNOS ESPECIALES
   ========================================================= */

MENORLEER:'<<';
MAYORIMPRIMIR:'>>';
COMILLAS:'"' (ESC | ~["\\])* '"';
fragment ESC:'\\' .;
COMILLASSIMPLES:'\'' ~['\r\n] '\'';

/* =========================================================
   OPERADORES RELACIONALES
   ========================================================= */
MAYORIGUAL:'>=';
MENORIGUAL:'<=';
COMPARACION:'==';
DIFERENCIA:'!=';
MENOR:'<';
MAYOR:'>';

/* =========================================================
   OPERADORES LOGICOS
   ========================================================= */

AND:
    '&&'
    ;

OR:
    '||'
    ;

NEGACION:
    'non'
    ;

/* =========================================================
   TIPOS DE DATOS
   ========================================================= */

NUMEROS:
    'numerus'
    ;

TEXTUM:
    'textum'
    ;

DECIMALIS:
    'decimalis'
    ;

LITTERA:
    'littera'
    ;

BOOL:
    'bool'
    ;

/* =========================================================
   BOOLEANOS
   ========================================================= */

VERUM:
    'verum'
    ;

FALSUS:
    'falsus'
    ;

/* =========================================================
   SECCIONES
   ========================================================= */

SECCIONVARIABLE:
    'VARIABILES'
    ;

SECCIONMAIN:
    'MAIOR'
    ;

/* =========================================================
   ESTRUCTURAS
   ========================================================= */

/*
   Las estructuras NO se declaran en Pig Latin.
   Se importan desde archivos .y.
*/

/* =========================================================
   FINAL DE BLOQUES
   ========================================================= */

FINIS:
    'finis'
    ;

/* =========================================================
   CONDICIONALES
   ========================================================= */

SI:
    'si'
    ;

ALITER:
    'aliter'
    ;

/* =========================================================
   CICLOS
   ========================================================= */

DUM:
    'dum'
    ;

FACERE:
    'facere'
    ;

PER:
    'per'
    ;

PERGE:
    'perge'
    ;

INTERRUMPE:
    'interrumpe'
    ;

/* =========================================================
   IMPORTACIONES Y OBJETOS
   ========================================================= */

IMPORT:
    'import'
    ;

NOVUS:
    'novus'
    ;

/* =========================================================
   VARIABLES
   ========================================================= */

ESTO:
    'esto'
    ;

SERIES:
    'series'
    ;

/* =========================================================
   IDENTIFICADORES
   ========================================================= */

ID:
    [a-zA-Z_] [a-zA-Z_0-9]*
    ;

/* =========================================================
   NUMEROS
   ========================================================= */

/*
   DECIMAL debe aparecer antes que ENTERO para
   mantener clara la intención del lexer.
*/

DECIMAL:
    [0-9]+ '.' [0-9]+
    ;

ENTERO:
    [0-9]+
    ;

/* =========================================================
   ASIGNACION
   ========================================================= */

IGUAL:
    '='
    ;

/* =========================================================
   SIGNOS DE ESTRUCTURA
   ========================================================= */

DOSPUNTOS:
    ':'
    ;

LLAVE_ABRE:
    '{'
    ;

LLAVE_CIERRA:
    '}'
    ;

CORCHETE_ABRE:
    '['
    ;

CORCHETE_CIERRA:
    ']'
    ;

PARENTESIS_ABRE:
    '('
    ;

PARENTESIS_CIERRA:
    ')'
    ;

PUNTO_COMA:
    ';'
    ;

PUNTO:
    '.'
    ;

COMA:
    ','
    ;