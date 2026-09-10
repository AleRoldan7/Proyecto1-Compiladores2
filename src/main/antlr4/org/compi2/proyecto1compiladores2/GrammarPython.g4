grammar GrammarPython;

/*GRAMATICA*/

program:
    NEWLINE*
    seccionEstructuras?
    NEWLINE*
    seccionFunciones
    NEWLINE*
    EOF
    ;

seccionEstructuras:
    SECCION_ESTRUCTURA NEWLINE NEWLINE*
    declaracionEstructura
    (NEWLINE* declaracionEstructura)*
    ;

/* FIX: los campos de una estructura ya no reutilizan declaracionVariable
   completa (que permite inicializadores y tamaño de arreglo dinámico);
   ahora usan campoEstructura, que obliga a que el tamaño del arreglo
   sea una constante, tal como lo exige el enunciado
   ("la expresión para definir arreglos obligatoriamente debe ser
   constante, únicamente dentro de la definición de una estructura") */
declaracionEstructura:
    ESTRUCTURA ID DOS_PUNTOS NEWLINE
    INDENT
    campoEstructura NEWLINE
    (campoEstructura NEWLINE)*
    DEDENT
    ;

campoEstructura:
    tipo ID dimension?
    ;

seccionFunciones:
    SECCION_FUNCION NEWLINE NEWLINE* declaracionFuncion+
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
    NEWLINE INDENT (sentencia | NEWLINE)* DEDENT
    ;

sentencia:
      declaracionVariable NEWLINE          # sentDeclaracionVariable
    | asignacion NEWLINE                    # sentAsignacion
    | llamadaFuncion NEWLINE                 # sentLlamadaFuncion
    /* FIX: incremento/decremento como sentencia independiente
       (el enunciado usa "contador++;", "intentos++" como línea suelta,
       lo cual antes solo era válido dentro de una expresión) */
    | accesoVariable (INCREMENTO | DECREMENTO) NEWLINE   # sentIncrDecrPostfijo
    | (INCREMENTO | DECREMENTO) accesoVariable NEWLINE   # sentIncrDecrPrefijo
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

/* Tamaño de arreglo: siempre constante (ningún ejemplo del enunciado usa tamaño dinámico,
   ni dentro ni fuera de una estructura) */
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
      /* FIX: se quitó el DOS_PUNTOS extra después de HACER; el enunciado
         muestra "mientras(contador < 5) hacer" seguido directo del bloque
         indentado, sin dos puntos (a diferencia de "hacer:" del do-while,
         donde HACER sí abre el bloque como palabra líder) */
      MIENTRAS PARENTESIS_ABRE expresion PARENTESIS_CIERRA HACER bloque   # cicloWhile
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
RETORNO: 'retornar';
TIPO_RETORNO: '->';

ID: [a-zA-Z_][a-zA-Z_0-9]*;

DECIMAL: [0-9]+ '.' [0-9]+;
NUMERO_ENTERO: [0-9]+;

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