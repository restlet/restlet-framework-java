package checker;

import java.util.List;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ASM7;

public class WhiteListeClassChecker extends ClassVisitor {

    public static class AnnotationChecker extends AnnotationVisitor {

        private List<String> jdkClasses;

        public AnnotationChecker(List<String> jdkClasses) {
            super(ASM7);
            this.jdkClasses = jdkClasses;
        }

        public void visit(String name, Object value) {
        }

        public AnnotationVisitor visitAnnotation(String name, String desc) {
            try {
                checkDescription(jdkClasses, desc);
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage()
                        + " [annotation] " + name);
            }
            return new AnnotationChecker(jdkClasses);
        }

        public AnnotationVisitor visitArray(String name) {
            return new AnnotationChecker(jdkClasses);
        }

        public void visitEnd() {
        }

        public void visitEnum(String name, String desc, String value) {
            try {
                checkDescription(jdkClasses, desc);
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage()
                        + " [annotation] " + name + " [enum] " + value);
            }
        }
    }

    public static class FieldChecker extends FieldVisitor {

        private String className;

        private String fieldName;

        private List<String> jdkClasses;

        public FieldChecker(List<String> jdkClasses, String className,
                String fieldName) {
            super(ASM7);
            this.jdkClasses = jdkClasses;
            this.className = className;
            this.fieldName = fieldName;
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return new AnnotationChecker(jdkClasses);
        }

        public void visitAttribute(Attribute attr) {
        }

        public void visitEnd() {
        }

    }

    public static class MethodChecker extends MethodVisitor {

        /** Liste des méthodes formellement autorisées par classe. */
        private Map<String, List<String>> authorizedMethods;

        private String className;

        /** Liste des méthodes formellement interdites par classe. */
        private Map<String, List<String>> forbiddenMethods;

        /** La liste des classes du JDK autorisées. */
        private List<String> jdkClasses;

        private String method;

        public MethodChecker(List<String> jdkClasses,
                Map<String, List<String>> authorizedMethods,
                Map<String, List<String>> forbiddenMethods, String className,
                String method) {
            super(ASM7);
            this.method = method;
            this.className = className;
            this.jdkClasses = jdkClasses;
            this.authorizedMethods = authorizedMethods;
            this.forbiddenMethods = forbiddenMethods;
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return new AnnotationChecker(jdkClasses);
        }

        public AnnotationVisitor visitAnnotationDefault() {
            return new AnnotationChecker(jdkClasses);
        }

        public void visitAttribute(Attribute attr) {
        }

        public void visitCode() {
        }

        public void visitEnd() {
        }

        public void visitFieldInsn(int opCode, String owner, String name,
                String desc) {
            try {
                check(jdkClasses, owner);
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage() + " [class] "
                        + className + " [method] " + method);
            }
        }

        public void visitFrame(int type, int nLocal, Object[] local,
                int nStack, Object[] stack) {
        }

        public void visitIincInsn(int var, int increment) {
        }

        public void visitInsn(int opCode) {
        }

        public void visitIntInsn(int opCode, int operand) {
        }

        public void visitJumpInsn(int opCode, Label label) {
        }

        public void visitLabel(Label label) {
        }

        public void visitLdcInsn(Object cst) {
        }

        public void visitLineNumber(int line, Label start) {
        }

        public void visitLocalVariable(String name, String desc,
                String signature, Label start, Label end, int index) {
            try {
                checkDescription(jdkClasses, desc);
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage() + " [class] "
                        + className + " [method] " + method);
            }
        }

        public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        }

        public void visitMaxs(int maxStacks, int maxLocals) {
        }

        public void visitMethodInsn(int opCode, String owner, String name,
                String desc) {
            try {
                check(jdkClasses, owner);
                checkDescription(jdkClasses, desc);
                checkMethod(authorizedMethods, forbiddenMethods, owner, name,
                        desc);
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage() + " [class] "
                        + className + " [method] " + method);
            }
        }

        public void visitMultiANewArrayInsn(String desc, int dims) {
            try {
                checkDescription(jdkClasses, desc);
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage() + " [class] "
                        + className + " [method] " + method);
            }
        }

        public AnnotationVisitor visitParameterAnnotation(int parameter,
                String desc, boolean visible) {
            try {
                checkDescription(jdkClasses, desc);
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage() + " [class] "
                        + className + " [method] " + method);
            }
            return new AnnotationChecker(jdkClasses);
        }

        public void visitTableSwitchInsn(int min, int max, Label dflt,
                Label[] labels) {
        }

        public void visitTryCatchBlock(Label start, Label end, Label handler,
                String type) {
        }

        public void visitTypeInsn(int opCpde, String type) {
        }

        public void visitVarInsn(int opCode, int var) {
        }

    }

    public static void check(List<String> jdkClasses, String name)
            throws Exception {
        // TODO check owner org/restlet/client/engine/io/IoBuffer
        if (name != null) {
            name = getClassName(name);
            if (name.startsWith("java.") || name.startsWith("javax.")
                    || name.startsWith("org.w3c.dom.")
                    || name.startsWith("org.xml.sax.")) {
                if (!jdkClasses.contains(name)) {
                    boolean found = false;
                    // Vérifier alors qu'un package n'est pas marqué.
                    int index = name.indexOf('.', 0);
                    while (index != -1 && !found) {
                        found = jdkClasses.contains(name.substring(0, index)
                                + ".*");
                        index = name.indexOf('.', index + 1);

                    }

                    if (!found) {
                        throw new Exception("[banned class] " + name);
                    }
                }
            }
        }
    }

    public static void checkDescription(List<String> jdkClasses, String name)
            throws Exception {
        // TODO check desc
        // (Ljava/nio/ByteBuffer;Lorg/restlet/client/engine/io/BufferState;)
    }

    public static void checkMethod(Map<String, List<String>> authorizedMethods,
            Map<String, List<String>> forbiddenMethods, String className,
            String methodName, String desc) throws Exception {
        if (className != null) {
            className = getClassName(className);
            List<String> forbidden = forbiddenMethods.get(className);
            if (forbidden != null && forbidden.contains(methodName + desc)) {
                throw new Exception("[banned method] " + methodName + desc);
            }
            List<String> au = authorizedMethods.get(className);
            if (au != null && !au.contains(methodName + desc)) {
                throw new Exception("[banned method] " + methodName + desc);
            }
        }
    }

    public static void checkSignature(List<String> jdkClasses, String name)
            throws Exception {
        // TODO check desc
        // (Ljava/nio/ByteBuffer;Lorg/restlet/client/engine/io/BufferState;)
    }

    private static String getClassName(String name) {
        name = name.replace("/", ".");
        int index = name.indexOf("$");
        if (index != -1) {
            name = name.substring(0, index);
        }
        return name;
    }

    /** Liste des méthodes formellement autorisées par classe. */
    private Map<String, List<String>> authorizedMethods;

    private String className;

    /** Liste des méthodes formellement interdites par classe. */
    private Map<String, List<String>> forbiddenMethods;

    /** La liste des classes du JDK autorisées. */
    private List<String> jdkClasses;

    public WhiteListeClassChecker(List<String> jdkClasses,
            Map<String, List<String>> authorizedMethods,
            Map<String, List<String>> forbiddenMethods, String className) {
        super(ASM7);
        this.jdkClasses = jdkClasses;
        if (className.endsWith(".class")) {
            this.className = className.replace(".class", "");
        } else {
            this.className = className;
        }
        this.authorizedMethods = authorizedMethods;
        this.forbiddenMethods = forbiddenMethods;
    }

    public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {
        try {
            check(jdkClasses, superName);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage() + " [class] "
                    + className);
        }

        if (interfaces != null) {
            for (String i : interfaces) {
                try {
                    check(jdkClasses, i);
                } catch (Exception e) {
                    throw new IllegalArgumentException(e.getMessage()
                            + " [class] " + className);
                }
            }
        }
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        try {
            checkDescription(jdkClasses, desc);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage() + " [class] "
                    + className);
        }
        return new AnnotationChecker(jdkClasses);
    }

    public void visitAttribute(Attribute attr) {
    }

    public void visitEnd() {
    }

    public FieldVisitor visitField(int access, String name, String desc,
            String signature, Object value) {
        try {
            checkDescription(jdkClasses, desc);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage() + " [class] "
                    + className);
        }
        return new FieldChecker(jdkClasses, className, name);
    }

    public void visitInnerClass(String name, String outerName,
            String innerName, int access) {
    }

    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {
        try {
            checkSignature(jdkClasses, signature);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage() + " [class] "
                    + className);
        }
        return new MethodChecker(jdkClasses, authorizedMethods,
                forbiddenMethods, className, name);
    }

    public void visitOuterClass(String owner, String name, String desc) {
    }

    public void visitSource(String source, String debug) {
    }

}
