grammar GrammarPython;

/*GRAMATICA*/

program:
    seccionEstructuras?
    seccionFunciones
    EOF
    ;

seccionEstructuras:
    SECCION_ESTRUCTURA declaracionEstructura+
    | SECCION_ESTRUCTURA condicional
    ;

declaracionEstructura:
    ESTRUCTURA ID DOS_PUNTOS declaracionVariable+
    ;

/*Declaración de variables*/
declaracionVariable:
    tipo ID  dimension? (IGUAL expresion)?
    ;

dimension:
    (CORCHETE_ABRE NUMERO_ENTERO CORCHETE_CIERRA)+
    ;

tipo:
    ENTERO
    | FLOTANTE
    | CARACTER
    | BOOL
    | CADENA
    | ID
    ;

expresion:
    literal
    | accesoVariable
    ;


literal:
    NUMERO_ENTERO
    | DECIMAL
    | COMILLAS
    | COMILLASSIMPLES
    | VERDADERO
    | FALSO
    ;

asignacion:
    accesoVariable IGUAL expresion
    ;

accesoVariable:
    ID (PUNTO ID | CORCHETE_ABRE expresion CORCHETE_CIERRA)*
    ;


/*Conicional Si*/
condicional:
    SI (condicion)+
    ;

condicion:
    PARENTESIS_ABRE ID tipoRelacional NUMERO_ENTERO (tipoLogico ID tipoRelacional NUMERO_ENTERO)* PARENTESIS_CIERRA
    ;

tipoRelacional:
    MAYOR
    | MENOR
    | MAYORIGUAL
    | MENORIGUAL
    | COMPARACION
    | DIFERENCIA
    ;

tipoLogico:
    AND
    | NOT
    ;

seccionFunciones:
    ;



/*LEXICO*/

WS:  [ \n\r\t]+ -> skip;

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
NOT: '||';
NEGACION: '!';

/*TIPOS DE DATOS*/
ENTERO: 'entero';
FLOTANTE: 'flotante';
CARACTER: 'caracter';
BOOL: 'bool';
CADENA: 'cadena';

/*BOOLEANOS*/
VERDADERO: 'verdadero';
FALSO: 'falso';

/*CONDICIONALES SI*/
SI: 'si';
ENTONCES: 'entonces';
SINO: 'sino';
CONTRARIO: 'contrario';

/*SWITCH*/
ELEGIR: 'elegir';
CASO: 'caso';
ROMPER: 'romper';
SIEMPRE: 'siempre';

/*CICLOS*/
PARA: 'para';
CONTINUAR: 'continuar';
MIENTRAS: 'mientras';
HACER: 'hacer';

/*FUNCIONES ESPECIALES*/
IMPRIMIR: 'imprimir';
LEER: 'leer';

/*SECCION ESTRUCTURAS*/
SECCION_ESTRUCTURA: '%estructuras';
ESTRUCTURA: 'estructura';

/*SECCION FUNCIONES*/
SECCION_FUNCION: '%funciones';
DEFINIR: 'definir';
RETORNO: 'retorno';
TIPO_RETORNO: '->';

/*COMPLEMENTOS LEXICOS*/
ID: [a-zA-Z_][a-zA-Z_0-9]*;
NUMERO_ENTERO: [0-9]+;
DECIMAL: [0-9]+ '.' [0-9]+;

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
DOS_PUNTOS: ':';
IGUAL: '=';