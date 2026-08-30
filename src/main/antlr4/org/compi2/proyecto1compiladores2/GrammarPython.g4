grammar GrammarPython;

/*GRAMATICA*/

program:
    seccionEstructuras?
    seccionFunciones
    EOF
    ;

seccionEstructuras:
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
OR: '||';
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