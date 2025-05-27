grammar Expr;

@header {
        package proyecto.antlr;
}

programa: lista_declaraciones lista_funciones lista_sentencias EOF ;

lista_declaraciones: declaracion* ;

declaracion: INT_VAR IDENT '=' expresion ';'
            | FLOAT_VAR IDENT '=' expresion ';'
            | STRING_VAR IDENT '=' STRING ';'
            | BOOL_VAR IDENT '=' booleano ';' ;

expresion: expresion_suma ;

expresion_suma: expresion_multiplicacion (('+' | '-') expresion_multiplicacion)* ;
expresion_multiplicacion: expresion_parentesis (('*' | '/' | '%') expresion_parentesis)* ;
expresion_parentesis: '(' expresion ')' 
                    | NUMERO 
                    | IDENT ;

booleano: 'True'
        | 'False'
        | IDENT (EQ | NEQ | GT | LT) IDENT
        | NUMERO (EQ | NEQ | GT | LT) NUMERO;

lista_funciones: funcion* ;

funcion: 'def' IDENT '(' parametros* ')' bloque ;
parametros: (INT_VAR | FLOAT_VAR | STRING_VAR | BOOL_VAR) IDENT (',' (INT_VAR | FLOAT_VAR | STRING_VAR | BOOL_VAR) IDENT)* ;  // Simplificado
bloque: '{' lista_sentencias '}' ;

lista_sentencias: sentencia+ ;
sentencia: asignacion
         | estructura_control
         | entrada_salida
         | llamada_funcion ;

asignacion: IDENT '=' expresion ';'
           | IDENT '=' STRING ';'
           | IDENT '=' booleano ';' ;

estructura_control: 'if' '(' booleano ')' bloque ('else' bloque)?
                  | 'while' '(' booleano ')' bloque ;

entrada_salida: 'read' '(' STRING ')' ';'
              | 'write' '(' contenido ',' STRING ')' ';'  // write(dato, "ruta");
              | 'print' '(' contenido ')' ';' 
              | 'input' '(' IDENT ')' ';' ;

contenido: STRING | IDENT ;

llamada_funcion: IDENT '(' (IDENT (',' IDENT)*)? ')' ';' ;

// Tokens (orden correcto), se cambio el orden por la procedencia que tiene ANTLR
INT_VAR: 'int' ;
FLOAT_VAR: 'float';
STRING_VAR: 'string';
BOOL_VAR: 'bool';
IDENT: [a-zA-Z_][a-zA-Z0-9_]* ;
NUMERO: [0-9]+ ('.' [0-9]+)? ;
STRING: '"' .*? '"' ;

EQ: '==' ;
NEQ: '!=' ;
GT: '>' ;
LT: '<' ;

MOD: '%' ;
LBRACE: '{' ;
RBRACE: '}' ;
LPAREN: '(' ;
RPAREN: ')' ;
SEMI: ';' ;
ASSIGN: '=' ;
COMMA: ',' ;

WS: [ \t\r\n]+ -> skip ;