/*
 * Created on Sep 21, 2004
 *
 */
package com.arc.seecode.engine.gen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Type;

/**
 * @author David Pickens
 */
public class GenDisplayCallback {

    private PrintWriter mOut;

    private ClassDoc mClassDoc;

    private static String sOutputDir;

    public GenDisplayCallback(RootDoc root, File outFile)
            throws ClassNotFoundException, IOException {
        mClassDoc = root
                .classNamed("com.arc.seecode.engine.ICustomDisplayCallback");
        if (mClassDoc == null)
                throw new ClassNotFoundException(
                        "Can't find ICustomDisplayCallback");
        FileWriter writer = new FileWriter(outFile);
        mOut = new PrintWriter(writer);
    }

    private void printPrefix() {
        mOut.println("/*");
        mOut.println(" * WARNING!");
        mOut.println(" * This file was auto-generated from "
                + GenDisplayCallback.class.getName());
        mOut.println(" * DO NOT MODIFY BY HAND!");
        mOut.println(" */");
        mOut.println();
    }

    private void printPackageAndImports(String packageName) {
        mOut.println("package " + packageName + ";");
        mOut.println();
        mOut.println("import com.arc.seecode.engine.ICustomDisplayCallback;");
        mOut.println("import com.arc.seecode.scwp.ScwpCommandPacket;");
        mOut.println("import com.arc.seecode.scwp.ScwpReplyPacket;");
        mOut.println("import java.io.IOException;");
        mOut.println("import com.arc.seecode.connect.IConnection;");
        mOut.println("import com.arc.seecode.connect.TimeoutException;");
        mOut.println("import com.arc.seecode.connect.VMDisconnectedException;");
        mOut.println();
    }

    private void printClassDefinition(String className) {
        mOut
                .println("public class "
                        + className
                        + " extends AbstractObserver implements ICustomDisplayCallback {");
        mOut.println();
        mOut.println("    private IConnection mConnection;");
        mOut.println();
        mOut.println("    /**");
        mOut.println("     */");
        mOut.println("    public " + className + "(CallbackThread thread, IConnection connection, int pid) { ");
        mOut
                .println("        super(thread,pid*ScwpCommandPacket.REQUIRED_CHANNELS+ScwpCommandPacket.CUSTOM_DISPLAY_MONITOR);");
        mOut.println("        mConnection = connection;");
        mOut.println("    }");
        MethodDoc[] methods = getMethodsToEmit(mClassDoc);

        for (int i = 0; i < methods.length; i++) {
            printMethodDefinition(methods[i]);
        }

        mOut.println("}");

    }

    private static MethodDoc[] getMethodsToEmit(ClassDoc c) {
        MethodDoc[] methodDocs = c.methods();
        return methodDocs;
    }

    private String typeName(Type type) {
        return type.typeName() + type.dimension();
    }
    
    private static boolean isStringType(Type type){
        return type.typeName().equals("String");
    }

    private void printMethodDefinition(MethodDoc m) {
        mOut.println();
        mOut.println("    @Override");
        mOut.print("    public ");
        mOut.print(typeName(m.returnType()));
        mOut.print(" " + m.name() + "(");
        Parameter[] parms = m.parameters();
        for (int i = 0; i < parms.length; i++) {
            Parameter p = parms[i];
            if (i > 0) mOut.print(", ");
            mOut.print(typeName(p.type()) + " " + p.name());
        }
        mOut.println(") {");
        mOut.print("        Object[] args = new Object[]{");
        for (int i = 0; i < parms.length; i++) {
            if (i > 0) mOut.print(",");
            mOut.println();
            mOut.print("                ");
            mOut.print(objectizedParm(parms[i]));
        }
        mOut.println("};");
        Type t = m.returnType();
        if (isVoid(m.returnType())) {
            mOut.println("        this.dispatch(\"" + m.name()
                    + "\",args);");
        }
        else {
            mOut.println("        ScwpCommandPacket cmd = this.makePacket(\"" + m.name() + "\",args);");
            mOut.println("        try {");
            mOut.println("            ScwpReplyPacket result = mConnection.sendCommand(cmd);");
            mOut.print("            return ");
            if (isStringType(t)) {
                mOut.println("result.dataInStream().readUTF();");
            }
            else
            if (!isPrimitive(t)) {
                mOut.println("(" + typeName(t) + ")result;");
            } else {
                mOut.println("result.dataInStream().read"
                        + capitalize(t.typeName()) + "();");

            }
            String badReturn = "            return -1;";
            if (!isPrimitive(t))
                badReturn = "            return null;";
            mOut.println("        }");
            mOut.println("        catch(TimeoutException e){");
            mOut.println(badReturn);
            mOut.println("        }");
            mOut.println("        catch(VMDisconnectedException e){");
            mOut.println(badReturn);
            mOut.println("        }");
            mOut.println("        catch(IOException e){");
            mOut.println(badReturn);
            mOut.println("        }");
        }
        mOut.println("    }");
    }
    
    private static boolean isVoid(Type t) {
        return "void".equals(t.typeName());
    }
    
//    private static String extractFunctionFor(Type t) {
//        return t.typeName() + "Value";
//    }

    private static String objectizedParm(Parameter p) {
        if (isPrimitive(p.type())) { return "new " + boxedTypeFor(p.type())
                + "(" + p.name() + ")"; }
        return p.name();
    }

    private static boolean isPrimitive(Type t) {
        if (t.dimension() != null && t.dimension().length() != 0) return false;
        return Character.isLowerCase(t.typeName().charAt(0));
    }

    private static String boxedTypeFor(Type t) {
        if ("int".equals(t.toString())) { return "Integer"; }
        return capitalize(t.typeName());
    }

    private static String capitalize(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private void generate(String packageName, String className) {
        printPrefix();
        printPackageAndImports(packageName);
        printClassDefinition(className);
        mOut.flush();
    }

    public static boolean start(RootDoc root) {
        try {
            GenDisplayCallback gen = new GenDisplayCallback(root, new File(
                    sOutputDir, "CustomDisplayCallback.java"));
            gen.generate("com.arc.seecode.server", "CustomDisplayCallback");
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
        return true;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Args are: <path-to-IEngineAPI> <output-dir>");
            System.exit(1);
        }
        String sourcePath = args[0];
        sOutputDir = args[1];

        File f = new File(sourcePath);
        if (!f.isDirectory())
                throw new IllegalArgumentException("Not a directory: "
                        + sourcePath);
        if (!new File(f, "com/arc/seecode/engine/ICustomDisplayCallback.java")
                .exists()) { throw new IllegalArgumentException(
                "Can't find source file"); }
        if (!new File("com/arc/seecode/engine/gen").isDirectory())
                throw new IllegalArgumentException(
                        "not proper working directory");
        com.sun.tools.javadoc.Main.main(new String[] { "-docletpath", ".",
                "-doclet", GenDisplayCallback.class.getName(), "-source",
                "1.5", "-verbose", "-sourcepath", sourcePath, "-private",
                "com.arc.seecode.engine" });
    }
}