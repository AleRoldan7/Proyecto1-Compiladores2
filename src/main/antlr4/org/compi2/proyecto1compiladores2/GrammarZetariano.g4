grammar GrammarZetariano;

/*GRAMATICA*/

program:
    creacionClase
    EOF
    ;


creacionClase:
    ;




/*LEXICO*/

WS:  [ \n\r\t]+ -> skip;

/*COMENTARIOS*/
COMENTARIO_LINEA: '//' ~[\r\n]* -> skip;
COMENTARIO_BLOQUE: '/*' .*? '*/' -> skip;

/*INCREMENTO Y DECREMENTO*/
INCREMENTO: '++';
DECREMENTO: '--';

/*SIGNOS COMPUESTOS*/
SUMA_ASIGNACION: '+=';
RESTA_ASIGNACION: '-=';
MULTI_ASIGNACION: '*=';

/*ARITMETICOS*/
MAS: '+';
RESTA: '-';
MULTIPLICACION: '*';
DIVISION: '/';
MODULO: '%';

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
NEGACION: '!';

/*TIPOS DE DATOS*/
INT: 'int';
DOUBLE: 'double';
CHAR: 'char';
BOOLEAN: 'boolean';
STRING: 'String';
NEW: 'new';

/*TIPOS BOOLEANOS*/
TRUE: 'true';
FALSE: 'false';

/*CONDICIONALES*/
IF: 'if';
ELSE: 'else';

/*SWITCH*/
SWITCH: 'switch';
CASE: 'case';
BREAK: 'break';
DEFAULT: 'default';

/*CICLOS*/
FOR: 'for';
WHILE: 'while';
DO: 'do';
CONTINUE: 'continue';

/*FUNCIONES ESPECIALES*/
PRINTLN: 'println';
PRINT: 'print';
READLN: 'readln';

/*ESTRUCTURA DE CLASES*/
PUBLIC: 'public';
CLASS: 'class';

/*FUNCIONES*/
VOID: 'void';
RETURN: 'return';

/*COMPLEMENTOS LEXICOS*/
ID: [a-zA-Z_][a-zA-Z_0-9]*;
ENTERO: [0-9]+;
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
TERNARIO: '?';