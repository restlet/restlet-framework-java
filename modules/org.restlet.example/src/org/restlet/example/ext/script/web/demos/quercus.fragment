<?php

//
// Important!
//
// Quercus' support of the Java scripting standard is only one-way, meaning that even though
// your scripts can call Java code, your Java code cannot call PHP script functions. One
// consequence of this is that the Quercus engine does not have to store and remember functions
// between calls to eval. Unfortunately for us, this breaks container.include.
//
// Instead, we can rely on standard PHP include methods: "include", "include_once", "require",
// "require_once", etc. (The include tag uses the PHP "include" method.) Just note that this means
// that the included files are not run through Scripturian.
//
// Another consequence is that we absolutely must use the file system for our included scripts,
// unlike our reliance on the ScriptSource, which allows us to plug in our Scripturian's
// ScriptSource. This is unfortunate, because our ScriptSource can offer certain advantages.
// For example, we could implement it via a ClassLoader to load scripts from a .jar file
// or load it from a network location. This is currently impossible with Quercus. 
//

function printTriple($value) {
	print $value*3;
}

?>