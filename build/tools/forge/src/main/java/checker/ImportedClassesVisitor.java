package checker;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ASM4;
import static org.objectweb.asm.Opcodes.ASM7;

public class ImportedClassesVisitor extends ClassVisitor {

    private static class AnnotationChecker extends AnnotationVisitor {

        private Set<String> classes;

        private AnnotationChecker(Set<String> jdkClasses) {
            super(ASM7);
            this.classes = jdkClasses;
        }

        public void visit(String name, Object value) {

        }

        public AnnotationVisitor visitAnnotation(String name, String desc) {
            try {
                checkDescription(classes, desc);
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage()
                        + " [annotation] " + name);
            }
            return new AnnotationChecker(classes);
        }

        public AnnotationVisitor visitArray(String name) {
            return new AnnotationChecker(classes);
        }

        public void visitEnd() {
        }

        public void visitEnum(String name, String desc, String value) {
            try {
                checkDescription(classes, desc);
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage()
                        + " [annotation] " + name + " [enum] " + value);
            }
        }
    }

    private static class FieldChecker extends FieldVisitor {

        private Set<String> classes;

        private String className;

        private String fieldName;

        public FieldChecker(Set<String> jdkClasses, String className,
                String fieldName) {
            super(ASM7);
            this.classes = jdkClasses;
            this.className = className;
            this.fieldName = fieldName;
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return new AnnotationChecker(classes);
        }

        public void visitAttribute(Attribute attr) {
        }

        public void visitEnd() {
        }

    }

    private static class MethodChecker extends MethodVisitor {

        /** La liste des classes. */
        private Set<String> classes;

        private String className;

        private String method;

        public MethodChecker(Set<String> classes, String className,
                String method) {
            super(ASM7);
            this.method = method;
            this.className = className;
            this.classes = classes;
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return new AnnotationChecker(classes);
        }

        public AnnotationVisitor visitAnnotationDefault() {
            return new AnnotationChecker(classes);
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
                check(classes, owner);
                check(classes, desc);
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage() + " [class] "
                        + className + " [method] " + method);
            }
        }

        public void visitFrame(int type, int nLocal, Object[] local,
                int nStack, Object[] stack) {
            for (Object object : stack) {
                checkObject(classes, object);
            }
            for (Object object : local) {
                checkObject(classes, object);
            }
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
            checkObject(classes, cst);
        }

        public void visitLineNumber(int line, Label start) {
        }

        public void visitLocalVariable(String name, String desc,
                String signature, Label start, Label end, int index) {
            try {
                checkDescription(classes, desc);
                checkSignature(classes, signature);
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
                check(classes, owner);
                checkDescription(classes, desc);
                checkMethod(classes, owner, name, desc);
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage() + " [class] "
                        + className + " [method] " + method);
            }
        }

        public void visitMultiANewArrayInsn(String desc, int dims) {
            try {
                checkDescription(classes, desc);
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage() + " [class] "
                        + className + " [method] " + method);
            }
        }

        public AnnotationVisitor visitParameterAnnotation(int parameter,
                String desc, boolean visible) {
            try {
                checkDescription(classes, desc);
            } catch (Exception e) {
                throw new IllegalArgumentException(e.getMessage() + " [class] "
                        + className + " [method] " + method);
            }
            return new AnnotationChecker(classes);
        }

        public void visitTableSwitchInsn(int min, int max, Label dflt,
                Label[] labels) {
        }

        public void visitTryCatchBlock(Label start, Label end, Label handler,
                String type) {
        }

        public void visitTypeInsn(int opCpde, String type) {
            addClassesName(classes, type);
        }

        public void visitVarInsn(int opCode, int var) {
        }

    }

    private static void addClassesName(Set<String> classes, String name) {
        if (name != null) {
            name = name.replace("/", ".").replace("(", "").replace(")", "")
                    .replace("[", "").replace("]", "").replace("<", ";")
                    .replace(">", ";");
            int index = name.indexOf("$");
            if (index != -1) {
                name = name.substring(0, index);
            }
            if (name.endsWith(";V")) {
                name = name.substring(0, name.length() - 2);
            }
            // In case of generic, or signature
            String[] str = name.split(";");
            if (str.length == 1) {
                for (int i = 0; i < str[0].length(); i++) {
                    if (str[0].contains(".") && !str[0].startsWith(".")) {
                        if (Character.isLowerCase(str[0].charAt(i))) {
                            classes.add(str[0].substring(i));
                            break;
                        }
                    }
                }
            } else {
                for (String string : str) {
                    if (string.contains(".")) {
                        addClassesName(classes, string);
                    }
                }
            }

        }
    }

    private static void check(Set<String> classes, String name)
            throws Exception {
        addClassesName(classes, name);
    }

    private static void checkDescription(Set<String> classes, String name)
            throws Exception {
        addClassesName(classes, name);
    }

    private static void checkMethod(Set<String> classes, String className,
            String methodName, String desc) throws Exception {
        addClassesName(classes, className);
    }

    private static void checkObject(Set<String> classes, Object o) {
        if (o instanceof org.objectweb.asm.Type) {
            try {
                check(classes, ((org.objectweb.asm.Type) o).getClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void checkSignature(Set<String> classes, String name)
            throws Exception {
        addClassesName(classes, name);
    }

    public static void main(String[] args) {
        File file = new File("/home/thboileau/forge/restlet/2.2/modules");
        // checkModule(file, "org.restlet");
        String module = "org.restlet.ext.jdbc";
        Set<String> classes = checkModule(file, module);
        for (String string : classes) {
            if (!(string.startsWith("java.") || string.startsWith(module)
                    || string.startsWith("com.sun.")
                    || string.startsWith("org.xml.") || string
                        .startsWith("org.w3c."))) {
                System.out.println(string);
            }
        }
    }

    public static Set<String> checkModule(File rootdir, String module) {
        Set<String> classes = new HashSet<String>();
        checkModulePackage(new File(rootdir, module + "/bin"), module, classes);
        return classes;
    }

    private static void checkModulePackage(File packageDir, String module,
            Set<String> classes) {
        File[] tab = packageDir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory()
                        || pathname.getName().endsWith(".class");
            }

        });
        if (tab != null) {
            for (File file : tab) {
                if (file.isDirectory()) {
                    checkModulePackage(file, module, classes);
                } else {
                    checkClass(file, classes);
                }
            }
        } else {
            System.out.println("Could you check ? " + packageDir);
        }
    }

    private static void checkClass(File file, Set<String> classes) {
        ImportedClassesVisitor checker = new ImportedClassesVisitor(classes,
                file.getName());
        FileInputStream fis = null;
        ClassReader reader = null;
        try {
            fis = new FileInputStream(file);
            reader = new ClassReader(fis);
        } catch (FileNotFoundException e) {
            throw new BuildException("Cannot find " + file.getAbsolutePath()
                    + " due to " + e.getMessage());
        } catch (IOException e) {
            throw new BuildException("Cannot read " + file.getAbsolutePath()
                    + " due to " + e.getMessage());
        } catch (Throwable e) {
            throw new BuildException("Cannot read " + file.getAbsolutePath()
                    + " due to " + e.getMessage());
        }
        try {
            reader.accept(checker, 0);
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /** La liste des classes. */
    private Set<String> classes;

    private String className;

    public ImportedClassesVisitor(Set<String> classes, String className) {
        super(ASM7);
        this.classes = classes;
        if (className.endsWith(".class")) {
            this.className = className.replace(".class", "");
        } else {
            this.className = className;
        }
    }

    public void visit(int version, int access, String name, String signature,
            String superName, String[] interfaces) {
        try {
            check(classes, superName);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage() + " [class] "
                    + className);
        }

        if (interfaces != null) {
            for (String i : interfaces) {
                try {
                    check(classes, i);
                } catch (Exception e) {
                    throw new IllegalArgumentException(e.getMessage()
                            + " [class] " + className);
                }
            }
        }
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        try {
            checkDescription(classes, desc);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage() + " [class] "
                    + className);
        }
        return new AnnotationChecker(classes);
    }

    public void visitAttribute(Attribute attr) {
    }

    public void visitEnd() {
    }

    public FieldVisitor visitField(int access, String name, String desc,
            String signature, Object value) {
        try {
            checkDescription(classes, desc);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage() + " [class] "
                    + className);
        }
        return new FieldChecker(classes, className, name);
    }

    public void visitInnerClass(String name, String outerName,
            String innerName, int access) {
    }

    public MethodVisitor visitMethod(int access, String name, String desc,
            String signature, String[] exceptions) {
        try {
            checkSignature(classes, signature);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage() + " [class] "
                    + className);
        }
        return new MethodChecker(classes, className, name);
    }

    public void visitOuterClass(String owner, String name, String desc) {
    }

    public void visitSource(String source, String debug) {
    }

}
