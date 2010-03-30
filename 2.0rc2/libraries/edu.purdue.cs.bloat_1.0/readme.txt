---------------------------------------------------------
Purdue BLOAT - Java bytecode optimizer and class rewriter
---------------------------------------------------------

"BLOAT, the Bytecode-Level Optimizer and Analysis Tools, is a Java classfile 
optimizer that is written entirely in Java. BLOAT was designed and developed by
Nate Nystrom in 1998 and performs a number of intraprocedural optimizations on 
Java bytecode:
    * Control flow graph construction
    * Conversion to static single assignment (SSA) form
    * Constant and copy propagation
    * Dead code elimination
    * Partial redundency elimination of expressions and access paths (e.g. array and fieldreferences)
    * Efficient "register" (JVM local variables) allocation
    * Java bytecode peephole optimizations "

For more information:
http://www.cs.purdue.edu/s3/projects/bloat/