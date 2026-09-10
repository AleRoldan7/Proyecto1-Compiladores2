grammar GrammarZetariano;

/*GRAMATICA*/

program:
    creacionClase EOF
    ;

creacionClase:
    PUBLIC CLASS ID LLAVE_ABRE contenidoClase* LLAVE_CIERRA
    ;

contenidoClase:
      atributo       # contenidoAtributo
    | constructor    # contenidoConstructor
    | metodo         # contenidoMetodo
    ;

/* FIX: se permite inicializar atributos con listaValores, igual que declaracionVariable */
atributo:
    tipo ID (IGUAL (expresion | listaValores))? PUNTO_COMA
    ;

constructor:
    PUBLIC ID PARENTESIS_ABRE listaParametros? PARENTESIS_CIERRA bloque
    ;

metodo:
    PUBLIC (tipo | VOID) ID PARENTESIS_ABRE listaParametros? PARENTESIS_CIERRA bloque
    ;

listaParametros:
    parametro (COMA parametro)*
    ;

/* FIX: ya no necesita "dimension" aparte; los corchetes ahora viven en "tipo" */
parametro:
    tipo ID
    ;

/* Tipo primitivo u objeto, sin arreglo */
tipoBase:
      INT | DOUBLE | CHAR | STRING | BOOLEAN | ID
    ;

/* FIX: arreglos al estilo Java: int[], String[], int[][], etc.
   (antes los corchetes iban después del ID, estilo Y?, lo cual no
   coincide con los ejemplos de Zetariano del enunciado) */
tipo:
    tipoBase (CORCHETE_ABRE CORCHETE_CIERRA)*
    ;

/* --------- Bloques y sentencias --------- */

/** Si el cuerpo de un if/else if/else es una sola sentencia, las llaves son opcionales
    (el enunciado lo exige explícitamente, con ejemplo de ifs anidados sin llaves) */
cuerpo:
      bloque
    | sentencia
    ;

bloque:
    LLAVE_ABRE sentencia* LLAVE_CIERRA
    ;

sentencia:
      declaracionVariable PUNTO_COMA                        # sentDeclaracionVariable
    | asignacion PUNTO_COMA                                 # sentAsignacion
    | llamadaFuncion PUNTO_COMA                              # sentLlamadaFuncion
    | llamadaMetodo PUNTO_COMA                               # sentLlamadaMetodo
    /* FIX: se quitó "ID |" porque accesoVariable ya cubre un ID solo (era redundante/ambiguo) */
    | accesoVariable (INCREMENTO | DECREMENTO) PUNTO_COMA    # sentIncrDecrPostfijo
    | (INCREMENTO | DECREMENTO) accesoVariable PUNTO_COMA    # sentIncrDecrPrefijo
    | imprimirStmt PUNTO_COMA                                # sentImprimir
    /* FIX: readln() como sentencia independiente, antes solo existía dentro de una expresión */
    | readlnExpr PUNTO_COMA                                  # sentReadln
    | RETURN expresion? PUNTO_COMA                           # sentReturn
    | BREAK PUNTO_COMA                                       # sentBreak
    | CONTINUE PUNTO_COMA                                    # sentContinue
    | condicional                                            # sentCondicional
    | cicloFor                                               # sentCicloFor
    | cicloWhile                                             # sentCicloWhile
    | cicloDoWhile PUNTO_COMA                                # sentCicloDoWhile
    | switchCase                                             # sentSwitch
    | bloque                                                 # sentBloqueAnidado
    ;

declaracionVariable:
    tipo ID (IGUAL (expresion | listaValores))?
    ;

listaValores:
    LLAVE_ABRE (elementoLista (COMA elementoLista)*)? LLAVE_CIERRA
    ;

elementoLista:
      expresion
    | listaValores
    ;

asignacion:
    accesoVariable op=(IGUAL | SUMA_ASIGNACION | RESTA_ASIGNACION | MULTI_ASIGNACION) expresion
    ;

accesoVariable:
    (THIS PUNTO)? ID (PUNTO ID | CORCHETE_ABRE expresion CORCHETE_CIERRA)*
    ;

/* --------- Condicionales --------- */

condicional:
    IF PARENTESIS_ABRE expresion PARENTESIS_CIERRA cuerpo
    (ELSE IF PARENTESIS_ABRE expresion PARENTESIS_CIERRA cuerpo)*
    (ELSE cuerpo)?
    ;

switchCase:
    SWITCH PARENTESIS_ABRE expresion PARENTESIS_CIERRA LLAVE_ABRE
        casoSwitch+
        (DEFAULT DOS_PUNTOS bloqueCaso)?
    LLAVE_CIERRA
    ;

casoSwitch:
    CASE literal DOS_PUNTOS bloqueCaso
    ;

/* El break ya no es obligatorio (se permite fall-through, como en Java/C): como BREAK ya es
   una alternativa normal de 'sentencia' (ver sentBreak), sentencia* solo ya lo cubre. */
bloqueCaso:
    sentencia*
    ;

/* --------- Ciclos --------- */

cicloFor:
    FOR PARENTESIS_ABRE (declaracionVariable | asignacion)? PUNTO_COMA
                          expresion? PUNTO_COMA
                          (asignacion | (accesoVariable (INCREMENTO | DECREMENTO)) | ((INCREMENTO | DECREMENTO) accesoVariable))?
                          PARENTESIS_CIERRA cuerpo
    ;

cicloWhile:
    WHILE PARENTESIS_ABRE expresion PARENTESIS_CIERRA cuerpo
    ;

cicloDoWhile:
    DO bloque WHILE PARENTESIS_ABRE expresion PARENTESIS_CIERRA
    ;

/* --------- Funciones especiales --------- */

imprimirStmt:
      PRINTLN PARENTESIS_ABRE expresion? PARENTESIS_CIERRA
    | PRINT PARENTESIS_ABRE expresion PARENTESIS_CIERRA
    ;

readlnExpr:
    READLN PARENTESIS_ABRE PARENTESIS_CIERRA
    ;

/* --------- Expresiones (con precedencia) --------- */

expresion:
      PARENTESIS_ABRE expresion PARENTESIS_CIERRA                     # expParentesis
    | NEGACION expresion                                               # expNegacionLogica
    | RESTA expresion                                                  # expNegativo
    | (INCREMENTO | DECREMENTO) accesoVariable                         # expPreIncrDecr
    | accesoVariable (INCREMENTO | DECREMENTO)                         # expPostIncrDecr
    | NEW ID PARENTESIS_ABRE listaArgumentos? PARENTESIS_CIERRA         # expCrearObjeto
    /* FIX: nueva alternativa para crear arreglos, p.ej. new int[5], new int[3][3] */
    | NEW tipoBase (CORCHETE_ABRE expresion CORCHETE_CIERRA)+           # expCrearArreglo
    | expresion op=(MULTIPLICACION | DIVISION | MODULO) expresion      # expMultiplicativa
    | expresion op=(MAS | RESTA) expresion                             # expAditiva
    | expresion op=(MENOR | MAYOR | MENORIGUAL | MAYORIGUAL) expresion # expRelacional
    | expresion op=(COMPARACION | DIFERENCIA) expresion                # expIgualdad
    | expresion op=AND expresion                                       # expAnd
    | expresion op=OR expresion                                        # expOr
    | expresion TERNARIO expresion DOS_PUNTOS expresion                # expTernaria
    | readlnExpr                                                       # expReadln
    | llamadaMetodo                                                    # expLlamadaMetodo
    | llamadaFuncion                                                   # expLlamadaFuncion
    | accesoVariable                                                   # expAcceso
    | literal                                                          # expLiteral
    | THIS                                                             # expThis
    ;

llamadaFuncion:
    ID PARENTESIS_ABRE listaArgumentos? PARENTESIS_CIERRA
    ;

llamadaMetodo:
    (THIS | ID) (PUNTO ID)* PUNTO ID PARENTESIS_ABRE listaArgumentos? PARENTESIS_CIERRA
    ;

listaArgumentos:
    expresion (COMA expresion)*
    ;

/* FIX: se agrega NULL, usado en el enunciado (p1 == null) */
literal:
    ENTERO | DECIMAL | COMILLAS | COMILLASSIMPLES | TRUE | FALSE | NULL
    ;


/*LEXICO*/

WS:  [ \n\r\t]+ -> skip;

COMENTARIO_LINEA: '//' ~[\r\n]* -> skip;
COMENTARIO_BLOQUE: '/*' .*? '*/' -> skip;

INCREMENTO: '++';
DECREMENTO: '--';

SUMA_ASIGNACION: '+=';
RESTA_ASIGNACION: '-=';
MULTI_ASIGNACION: '*=';

MAS: '+';
RESTA: '-';
MULTIPLICACION: '*';
DIVISION: '/';
MODULO: '%';

COMILLAS: '"' (ESC | ~["\\])* '"';
fragment ESC: '\\' . ;
COMILLASSIMPLES: '\'' ~['\r\n] '\'';

MAYORIGUAL: '>=';
MENORIGUAL: '<=';
COMPARACION: '==';
DIFERENCIA: '!=';
MENOR: '<';
MAYOR: '>';

AND: '&&';
OR: '||';
NEGACION: '!';

INT: 'int';
DOUBLE: 'double';
CHAR: 'char';
BOOLEAN: 'boolean';
STRING: 'String';
NEW: 'new';
THIS: 'this';

TRUE: 'true';
FALSE: 'false';
NULL: 'null';

IF: 'if';
ELSE: 'else';

SWITCH: 'switch';
CASE: 'case';
BREAK: 'break';
DEFAULT: 'default';

FOR: 'for';
WHILE: 'while';
DO: 'do';
CONTINUE: 'continue';

PRINTLN: 'println';
PRINT: 'print';
READLN: 'readln';

PUBLIC: 'public';
CLASS: 'class';

VOID: 'void';
RETURN: 'return';

ID: [a-zA-Z_][a-zA-Z_0-9]*;
ENTERO: [0-9]+;
DECIMAL: [0-9]+ '.' [0-9]+;

LLAVE_ABRE: '{';
LLAVE_CIERRA: '}';
CORCHETE_ABRE: '[';
CORCHETE_CIERRA: ']';
PARENTESIS_ABRE: '(';
PARENTESIS_CIERRA: ')';
PUNTO_COMA: ';';
PUNTO: '.';
COMA: ',';
DOS_PUNTOS: ':';
IGUAL: '=';
TERNARIO: '?';
