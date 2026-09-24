grammar GrammarPigLatin;

/* =========================================================
   ======================= PARSER ==========================
   ========================================================= */

program:
    seccionImport+
    seccionVariables?
    seccionMain
    EOF
    ;

seccionImport:
    IMPORT ID (PUNTO ID)* PUNTO ID PUNTO_COMA?
    ;

seccionVariables:
    SECCIONVARIABLE MAYOR declaracion+
    ;

declaracion:
    declaracionVariable
    | declaracionArreglo
    ;

declaracionVariable:
    ESTO ID DOSPUNTOS tipo
    (expresion | inicializacionStruct)?
    PUNTO_COMA?
    ;

tipo:
    tipoDato
    | ID
    | NOVUS ID PARENTESIS_ABRE listaArgumentos? PARENTESIS_CIERRA
    ;

tipoDato:
    NUMEROS
    | DECIMALIS
    | TEXTUM
    | LITTERA
    | BOOL
    | NIHIL
    ;

declaracionArreglo:
    SERIES ID
    (CORCHETE_ABRE ENTERO CORCHETE_CIERRA)+
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

seccionMain:
    SECCIONMAIN MAYOR
    sentencia*
    FINIS PUNTO_COMA
    ;

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
    | llamadaMetodoSentencia
    | incrementoDecremento
    | PERGE PUNTO_COMA
    | INTERRUMPE PUNTO_COMA
    ;

incrementoDecremento:
    ID acceso*
    (INCREMENTO | DECREMENTO)
    PUNTO_COMA
    ;

asignacion:
    ID acceso*
    IGUAL
    (expresion | inicializacionStruct)
    PUNTO_COMA
    ;

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

bloquePig:
    LLAVE_ABRE sentencia* LLAVE_CIERRA
    ;

cicloDum:
    DUM
    PARENTESIS_ABRE expresion PARENTESIS_CIERRA
    LLAVE_ABRE sentencia* LLAVE_CIERRA
    FINIS PUNTO_COMA
    ;

cicloFacere:
    FACERE
    LLAVE_ABRE sentencia* LLAVE_CIERRA
    DUM
    PARENTESIS_ABRE expresion PARENTESIS_CIERRA
    PUNTO_COMA
    ;

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

inicializacionPer:
    ESTO ID DOSPUNTOS tipo expresion?
    | ID acceso* IGUAL expresion
    ;

actualizacionPer:
    ID acceso*
    (INCREMENTO | DECREMENTO)
    | ID acceso* IGUAL expresion
    ;

imprimir:
    MAYORIMPRIMIR
    expresion
    (MAYORIMPRIMIR expresion)*
    PUNTO_COMA
    ;

leer:
    (ID acceso*)?
    MENORLEER
    PUNTO_COMA?
    ;

llamadaFuncionSentencia:
    llamadaFuncion PUNTO_COMA
    ;

/* FIX: Corregido para capturar misObjetos[9].hablar(...) */
llamadaMetodoSentencia:
    ID acceso* PUNTO ID PARENTESIS_ABRE listaArgumentos? PARENTESIS_CIERRA PUNTO_COMA
    ;

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

expresionPrimaria:
    literal
    | creacionObjeto
    | llamadaFuncion
    | ID acceso* (INCREMENTO | DECREMENTO)?
    | PARENTESIS_ABRE expresion PARENTESIS_CIERRA
    ;

creacionObjeto:
    NOVUS
    ID
    PARENTESIS_ABRE
    listaArgumentos?
    PARENTESIS_CIERRA
    ;

acceso:
    CORCHETE_ABRE expresion CORCHETE_CIERRA
    | PUNTO ID
    | PUNTO ID
      PARENTESIS_ABRE listaArgumentos? PARENTESIS_CIERRA
    ;

literal:
    ENTERO
    | DECIMAL
    | COMILLAS
    | COMILLASSIMPLES
    | VERUM
    | FALSUS
    | NULL
    ;

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

WS: [ \t\n\r]+ -> skip;

/* FIX: Comentarios ## ... ## */
COMENTARIO_BLOQUE_HASH: '##' .*? '##' -> skip;
COMENTARIO_LINEA_HASH: '##' ~[\r\n]* -> skip;

COMENTARIO_LINEA:'//' ~[\r\n]* -> skip;
COMENTARIO_BLOQUE:'/*' .*? '*/' -> skip;

INCREMENTO:'++';
DECREMENTO:'--';

MAS:'+';
RESTA:'-';
MULTIPLICACION:'*';
DIVISION:'/';
MODULO:'%';

MENORLEER:'<<';
MAYORIMPRIMIR:'>>';
COMILLAS:'"' (ESC | ~["\\])* '"';
fragment ESC:'\\' .;
COMILLASSIMPLES:'\'' ~['\r\n] '\'';

MAYORIGUAL:'>=';
MENORIGUAL:'<=';
COMPARACION:'==';
DIFERENCIA:'!=';
MENOR:'<';
MAYOR:'>';

AND: '&&';
OR: '||';
NEGACION: '!';

NUMEROS: 'numerus';
TEXTUM: 'textum';
DECIMALIS: 'decimalis';
LITTERA: 'littera';
BOOL: 'bool';
NIHIL: 'nihil';

VERUM: 'verum';
FALSUS: 'falsus';
NULL: 'null';

SECCIONVARIABLE: 'VARIABILES';
SECCIONMAIN: 'MAIOR';

FINIS: 'finis';
SI: 'si';
ALITER: 'aliter';
DUM: 'dum';
FACERE: 'facere';
PER: 'per';
PERGE: 'perge';
INTERRUMPE: 'interrumpe';

IMPORT: 'import';
NOVUS: 'novus';
ESTO: 'esto';
SERIES: 'series';

ID: [a-zA-Z_] [a-zA-Z_0-9]*;

DECIMAL: [0-9]+ '.' [0-9]+;
ENTERO: [0-9]+;

IGUAL: '=';
DOSPUNTOS: ':';
LLAVE_ABRE: '{';
LLAVE_CIERRA: '}';
CORCHETE_ABRE: '[';
CORCHETE_CIERRA: ']';
PARENTESIS_ABRE: '(';
PARENTESIS_CIERRA: ')';
PUNTO_COMA: ';';
PUNTO: '.';
COMA: ',';