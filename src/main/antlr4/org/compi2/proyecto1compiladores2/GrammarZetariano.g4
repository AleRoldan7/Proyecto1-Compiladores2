grammar GrammarZetariano;

/*GRAMATICA*/






/*LEXICO*/

WS:  [ \n\r\t]+ -> skip;

/*COMENTARIOS*/
COMENTARIO_LINEA: '//' ~[\r\n]* -> skip;
COMENTARIO_BLOQUE: '/*' .*? '*/' -> skip;

