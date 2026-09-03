grammar GrammarPigLatin;

/*GRAMATICA*/

program:
    seccionImport
    seccionVariables?
    seccionMain?
    EOF
    ;

/*SINTAXIS SECCION IMPORT*/
seccionImport:
    IMPORT ID PUNTO ID (PUNTO ID)* PUNTO ID
    ;

/*SINTAXIS SECCION VARIABLES*/
seccionVariables:
    SECCIONVARIABLE MAYOR declaracion+
    ;


declaracion:
    declaracionVariable
    | declaracionArreglo
    | declaracionStruct

    ;

declaracionVariable:
    ESTO ID DOSPUNTOS tipo (expresion | inicializacionStruct)? PUNTO_COMA?
    ;

declaracionStruct:
    ESTRUCTURA ID LLAVE_ABRE contenidoStruct ((COMA | PUNTO_COMA) contenidoStruct)* (COMA | PUNTO_COMA)? LLAVE_CIERRA FINIS PUNTO_COMA
    ;

contenidoStruct:
    ESTO ID DOSPUNTOS tipo
    | SERIES ID DOSPUNTOS tipo
    ;

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


declaracionArreglo:
    SERIES ID CORCHETE_ABRE ENTERO CORCHETE_CIERRA DOSPUNTOS tipo (LLAVE_ABRE listaValoresArreglo? LLAVE_CIERRA)? PUNTO_COMA
    ;

listaValoresArreglo:
    valorArreglo (COMA valorArreglo)*
    ;

valorArreglo:
    expresion
    | inicializacionStruct
    ;


inicializacionStruct:
    LLAVE_ABRE asignacionCampo (COMA asignacionCampo)* LLAVE_CIERRA
    ;

asignacionCampo:
    ID DOSPUNTOS (expresion | inicializacionStruct)
    ;

/*SINTAXIS SECCION MAIN*/
seccionMain:
    SECCIONMAIN MAYOR sentencia* FINIS PUNTO_COMA
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
    | incrementoDecremento
    | PERGE PUNTO_COMA
    | INTERRUMPE PUNTO_COMA
    ;

incrementoDecremento:
    ID acceso* (INCREMENTO | DECREMENTO) PUNTO_COMA
    ;


asignacion:
    ID acceso* IGUAL (expresion | inicializacionStruct) PUNTO_COMA
    ;

condicional:
    SI PARENTESIS_ABRE expresion PARENTESIS_CIERRA LLAVE_ABRE sentencia* LLAVE_CIERRA
    (ALITER PARENTESIS_ABRE expresion PARENTESIS_CIERRA LLAVE_ABRE sentencia* LLAVE_CIERRA)*
    (ALITER LLAVE_ABRE sentencia* LLAVE_CIERRA)?
    FINIS PUNTO_COMA
    ;

cicloDum:
    DUM PARENTESIS_ABRE expresion PARENTESIS_CIERRA LLAVE_ABRE sentencia* LLAVE_CIERRA FINIS PUNTO_COMA
    ;

cicloFacere:
    FACERE LLAVE_ABRE sentencia* LLAVE_CIERRA DUM PARENTESIS_ABRE expresion PARENTESIS_CIERRA PUNTO_COMA
    ;

cicloPer:
     PER PARENTESIS_ABRE inicializacionPer expresion PUNTO_COMA actualizacionPer PARENTESIS_CIERRA
     LLAVE_ABRE sentencia* LLAVE_CIERRA
    ;

inicializacionPer:
    declaracionVariable
    | ID acceso* IGUAL expresion PUNTO_COMA
    ;

actualizacionPer:
    ID acceso* IGUAL expresion
    | expresion
    ;

imprimir:
    MAYORIMPRIMIR expresion (MAYORIMPRIMIR expresion)* PUNTO_COMA
    ;

leer:
    (ID acceso*)? MENORLEER
    ;


llamadaFuncionSentencia:
    llamadaFuncion PUNTO_COMA
    ;


/*EXPRESION UTILIZANDO LA PRECEDENCIA*/
expresion:
    expresionOr
    ;

expresionOr:
    expresionAnd (OR expresionAnd)*
    ;

expresionAnd:
    expresionIgualdad (AND expresionIgualdad)*
    ;

expresionIgualdad:
    expresionRelacional ((COMPARACION | DIFERENCIA) expresionRelacional)*
    ;

expresionRelacional:
    expresionAditiva ((MENOR | MAYOR | MAYORIGUAL | MENORIGUAL) expresionAditiva)*
    ;

expresionAditiva:
    expresionMultiplicativa ((MAS | RESTA) expresionMultiplicativa)*
    ;

expresionMultiplicativa:
    expresionUnaria ((MULTIPLICACION | DIVISION) expresionUnaria)*
    ;

expresionUnaria:
    (NEGACION | RESTA) expresionUnaria
    | expresionPrimaria
    ;

expresionPrimaria:
    literal
    | llamadaFuncion
    | ID acceso* (INCREMENTO | DECREMENTO)?
    | PARENTESIS_ABRE expresion PARENTESIS_CIERRA
    ;

acceso:
    CORCHETE_ABRE expresion CORCHETE_CIERRA
    | PUNTO ID
    ;

literal:
    ENTERO
    | DECIMAL
    | COMILLAS
    | COMILLASSIMPLES
    | VERUM
    | FALSUS
    ;

llamadaFuncion:
    ID PARENTESIS_ABRE listaArgumentos? PARENTESIS_CIERRA
    ;

listaArgumentos:
    expresion (COMA expresion)*
    ;


/*LEXER*/

WS: [ \t\n\r]+ -> skip;

/*COMENTARIOS*/
COMENTARIO_LINEA: '//' ~[\r\n]* -> skip;
COMENTARIO_BLOQUE: '/*' .*? '*/' -> skip;

/*INCREMENTO Y DECREMENTO*/
INCREMENTO: '++';
DECREMENTO: '--';

/*ARITMETICOS*/
MAS: '+';
RESTA: '-';
MULTIPLICACION: '*';
DIVISION: '/';

/*SIGNOS ESPECIALES*/
MENORLEER: '<<';
MAYORIMPRIMIR: '>>';
COMILLAS: '"' (ESC | ~["\\])* '"';
fragment ESC: '\\' . ;
COMILLASSIMPLES: '\'' ~['\r\n] '\'';

/*RELACIONALES*/
MAYORIGUAL: '>=';
MENORIGUAL: '<=';
COMPARACION: '==';
DIFERENCIA: '!=';
MENOR: '<';
MAYOR: '>';

/*LOGICOS*/
AND: '&&';
OR: '||';
NEGACION: 'non';

/*TIPOS DE DATOS*/
NUMEROS: 'numerus';
TEXTUM: 'textum';
DECIMALIS: 'decimalis';
LITTERA: 'littera';
BOOL: 'bool';

/*BOOLEANOS*/
VERUM: 'verum';
FALSUS: 'falsus';

/*SECCIONES*/
SECCIONVARIABLE: 'VARIABILES';
SECCIONMAIN: 'MAIOR';

/*STRUCT*/
ESTRUCTURA: 'estructura';
FINIS: 'finis' | 'FINIS';

/*CONDICIONALES*/
SI: 'si';
ALITER: 'aliter';

/*CICLOS*/
DUM: 'dum'; /*CICLO SIMPLE*/
FACERE: 'facere'; /*CICLO DO-WHILE*/
PER: 'per'; /*CICLO CON ITERADOR*/
PERGE: 'perge'; /*CONTINUE*/
INTERRUMPE: 'interrumpe'; /*BREAK*/

/*IMPORTACIONES Y CREACION DE OBJETOS*/
IMPORT: 'import';
NOVUS: 'novus';

/*VARIABLES*/
ESTO: 'esto';
SERIES: 'series';
DOSPUNTOS: ':';
ID: [a-zA-Z_][a-zA-Z_0-9]*;
ENTERO: [0-9]+;
DECIMAL: [0-9]+ '.' [0-9]+;
IGUAL: '=';

/*SIGNOS*/
LLAVE_ABRE: '{';
LLAVE_CIERRA: '}';
CORCHETE_ABRE: '[';
CORCHETE_CIERRA: ']';
PARENTESIS_ABRE: '(';
PARENTESIS_CIERRA: ')';
PUNTO_COMA: ';';
PUNTO: '.';
COMA: ',';