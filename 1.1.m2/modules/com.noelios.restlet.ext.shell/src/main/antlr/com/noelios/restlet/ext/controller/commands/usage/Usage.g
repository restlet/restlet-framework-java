grammar Usage;

options {
    output=AST;
}

tokens {
    OPT; 
    REQ; 
    MULTI; 
} 

@parser::header {
package com.noelios.restlet.ext.shell.controller.commands.usage;
}
   
@lexer::header {
package com.noelios.restlet.ext.shell.controller.commands.usage;
}

usage 
    : 
    | command 
    | command requiredParameter+ 
    | command optionalParameter+ 
    | command requiredParameter+ optionalParameter+
;

// TODO: this generate a useless node
command 
    : ID 
;

requiredParameter 
    : parameter -> ^(REQ parameter) 
;

optionalParameter 
    : '[' parameter ']' -> ^(OPT parameter)
;

parameter 
    : simpleParameter 
    | multiParameter 
;

simpleParameter
    : ID
;

multiParameter
    : ID ( '|' ID )+  -> ^(MULTI ID+)
;

ID 
    : LETTER+ 
;

fragment LETTER 
    : 'a' .. 'z'
;

WS 
    : (' ' | '\t' )+ {
        $channel = HIDDEN;
    } 
;
