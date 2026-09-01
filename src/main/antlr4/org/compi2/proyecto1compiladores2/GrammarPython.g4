grammar GrammarPython;

/*GRAMATICA*/

program:
    seccionEstructuras?
    NEWLINE*
    seccionFunciones
    EOF
    ;

seccionEstructuras:
    SECCION_ESTRUCTURA NEWLINE declaracionEstructura+
    ;

declaracionEstructura:
    ESTRUCTURA ID DOS_PUNTOS NEWLINE INDENT declaracionVariable NEWLINE (declaracionVariable NEWLINE)* DEDENT
    ;

seccionFunciones:
    SECCION_FUNCION NEWLINE declaracionFuncion+
    ;

declaracionFuncion:
    DEFINIR ID PARENTESIS_ABRE listaParametros? PARENTESIS_CIERRA (TIPO_RETORNO tipo)? DOS_PUNTOS bloque
    ;

listaParametros:
    parametro (COMA parametro)*
    ;

parametro:
      CORCHETE_ABRE CORCHETE_CIERRA tipo ID   # parametroArreglo
    | LLAVE_ABRE LLAVE_CIERRA tipo ID         # parametroEstructura
    | tipo ID                                  # parametroSimple
    ;

bloque:
    NEWLINE INDENT sentencia+ DEDENT
    ;

sentencia:
      declaracionVariable NEWLINE          # sentDeclaracionVariable
    | asignacion NEWLINE                    # sentAsignacion
    | llamadaFuncion NEWLINE                 # sentLlamadaFuncion
    | imprimirStmt NEWLINE                   # sentImprimir
    | leerStmt NEWLINE                       # sentLeer
    | RETORNO expresion? NEWLINE             # sentRetorno
    | ROMPER NEWLINE                         # sentRomper
    | CONTINUAR NEWLINE                      # sentContinuar
    | declaracionEstructura                  # sentEstructuraLocal   // Y? permite declarar estructuras dentro de funciones
    | condicional                            # sentCondicional
    | cicloPara                              # sentCicloPara
    | cicloMientras                          # sentCicloMientras
    | switchCase                             # sentSwitch
    ;

/* --------- Declaraciones y asignación --------- */

declaracionVariable:
    tipo ID dimension? (IGUAL expresion)?
    ;

dimension:
    (CORCHETE_ABRE NUMERO_ENTERO CORCHETE_CIERRA)+
    ;

tipo:
      ENTERO | FLOTANTE | CARACTER | BOOL | CADENA | ID
    ;

asignacion:
    accesoVariable IGUAL expresion
    ;

accesoVariable:
    ID (PUNTO ID | CORCHETE_ABRE expresion CORCHETE_CIERRA)*
    ;

listaValores:
    LLAVE_ABRE (expresion (COMA expresion)*)? LLAVE_CIERRA
    ;

/* --------- Expresiones (con precedencia) --------- */

expresion:
      PARENTESIS_ABRE expresion PARENTESIS_CIERRA              # expParentesis
    | NEGACION expresion                                        # expNegacionLogica
    | RESTA expresion                                           # expNegativo
    | INCREMENTO ID                                             # expPreIncremento
    | DECREMENTO ID                                             # expPreDecremento
    | ID INCREMENTO                                             # expPostIncremento
    | ID DECREMENTO                                             # expPostDecremento
    | expresion op=(MULTIPLICACION|DIVISION) expresion          # expMultiplicativa
    | expresion op=(MAS|RESTA) expresion                        # expAditiva
    | expresion op=(MENOR|MAYOR|MENORIGUAL|MAYORIGUAL) expresion # expRelacional
    | expresion op=(COMPARACION|DIFERENCIA) expresion           # expIgualdad
    | expresion op=AND expresion                                 # expAnd
    | expresion op=OR expresion                                  # expOr
    | llamadaFuncion                                              # expLlamada
    | leerExpr                                                    # expLeer
    | listaValores                                                # expListaValores   // arreglo o estructura, según contexto
    | accesoVariable                                              # expAcceso
    | literal                                                     # expLiteral
    ;

llamadaFuncion:
    ID PARENTESIS_ABRE listaArgumentos? PARENTESIS_CIERRA
    ;

listaArgumentos:
    expresion (COMA expresion)*
    ;

literal:
      NUMERO_ENTERO | DECIMAL | COMILLAS | COMILLASSIMPLES | VERDADERO | FALSO
    ;

/* --------- Condicionales --------- */

condicional:
    SI PARENTESIS_ABRE expresion PARENTESIS_CIERRA ENTONCES bloque
    (SINO PARENTESIS_ABRE expresion PARENTESIS_CIERRA ENTONCES bloque)*
    (CONTRARIO bloque)?
    ;

switchCase:
    ELEGIR PARENTESIS_ABRE expresion PARENTESIS_CIERRA DOS_PUNTOS NEWLINE INDENT
        casoElegir+
        (SIEMPRE DOS_PUNTOS bloqueCaso)?
    DEDENT
    ;

casoElegir:
    CASO literal DOS_PUNTOS bloqueCaso
    ;

bloqueCaso:
    NEWLINE INDENT sentencia+ ROMPER NEWLINE DEDENT
    ;

/* --------- Ciclos --------- */

cicloPara:
    PARA PARENTESIS_ABRE (declaracionVariable | asignacion)? PUNTO_COMA
                          expresion? PUNTO_COMA
                          expresion? PARENTESIS_CIERRA DOS_PUNTOS bloque
    ;

cicloMientras:
      MIENTRAS PARENTESIS_ABRE expresion PARENTESIS_CIERRA HACER DOS_PUNTOS bloque   # cicloWhile
    | HACER DOS_PUNTOS bloque MIENTRAS PARENTESIS_ABRE expresion PARENTESIS_CIERRA NEWLINE  # cicloDoWhile
    ;

/* --------- Funciones especiales --------- */

imprimirStmt:
    IMPRIMIR PARENTESIS_ABRE expresion PARENTESIS_CIERRA
    ;

leerStmt:
    leerExpr
    ;

leerExpr:
    LEER PARENTESIS_ABRE PARENTESIS_CIERRA
    ;

/*LEXICO*/

WS: [ ]+ -> skip;
NEWLINE: ('\r'? '\n' | '\r') [ \t]*;

COMENTARIO_LINEA: '//' ~[\r\n]* -> skip;
COMENTARIO_BLOQUE: '/*' .*? '*/' -> skip;

INCREMENTO: '++';
DECREMENTO: '--';

MAS: '+';
RESTA: '-';
MULTIPLICACION: '*';
DIVISION: '/';

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

ENTERO: 'entero';
FLOTANTE: 'flotante';
CARACTER: 'caracter';
BOOL: 'bool';
CADENA: 'cadena';

VERDADERO: 'verdadero';
FALSO: 'falso';

SI: 'si';
ENTONCES: 'entonces';
SINO: 'sino';
CONTRARIO: 'contrario';

ELEGIR: 'elegir';
CASO: 'caso';
ROMPER: 'romper';
SIEMPRE: 'siempre';

PARA: 'para';
CONTINUAR: 'continuar';
MIENTRAS: 'mientras';
HACER: 'hacer';

IMPRIMIR: 'imprimir';
LEER: 'leer';

SECCION_ESTRUCTURA: '%estructuras';
ESTRUCTURA: 'estructura';

SECCION_FUNCION: '%funciones';
DEFINIR: 'definir';
RETORNO: 'retorno';
TIPO_RETORNO: '->';

ID: [a-zA-Z_][a-zA-Z_0-9]*;
NUMERO_ENTERO: [0-9]+;
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

INDENT: '\u0002INDENT_NUNCA_COINCIDE\u0002';
DEDENT: '\u0002DEDENT_NUNCA_COINCIDE\u0002';