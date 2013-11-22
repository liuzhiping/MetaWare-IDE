/*
 * GenCMPDClient
 * $Revision$
 * $Date$
 *
 * CONFIDENTIAL AND PROPRIETARY INFORMATION.
 * Copyright 2008 ARC International (Unpublished).
 * All Rights Reserved.
 * This document, material and/or software contains confidential and
 * proprietary information of ARC International and is protected by copyright,
 * trade secret and other state, federal, and international laws, and may be
 * embodied in patents issued or pending. Its receipt or possession does not
 * convey any rights to use, reproduce, disclose its contents, or to
 * manufacture, or sell anything it may describe.  Reverse engineering is
 * prohibited, and reproduction, disclosure or use without specific written
 * authorization of ARC International is strictly forbidden.  ARC and the ARC
 * logotype are trademarks of ARC International.
 */
package com.arc.seecode.engine.gen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Type;


public class GenCMPDClient {
    private PrintWriter mOut;

    private ClassDoc mClassDoc;

    private static String sOutputDir;

    public GenCMPDClient(RootDoc root, File outFile) throws ClassNotFoundException,
            IOException {
        mClassDoc = root.classNamed("com.arc.seecode.cmpd.ICMPDController");
        if (mClassDoc == null)
                throw new ClassNotFoundException("Can't find ICMPDController");
        FileWriter writer = new FileWriter(outFile);
        mOut = new PrintWriter(writer);
    }

    private void printPrefix () {
        mOut.println("/*");
        mOut.println(" * WARNING!");
        mOut.println(" * This file was auto-generated from " + GenCMPDClient.class.getName());
        mOut.println(" * DO NOT MODIFY BY HAND!");
        mOut.println(" */");
        mOut.println();
    }

    private void printPackageAndImports(String packageName) {
        mOut.println("package " + packageName + ";");
        mOut.println();
        mOut.println("import java.lang.reflect.Method;");

        mOut.println("import com.arc.seecode.cmpd.ICMPDController;");
        mOut.println("import com.arc.seecode.connect.IConnection;");
        mOut.println("import com.arc.seecode.engine.EngineException;");
        mOut.println("import com.arc.seecode.serialize.IMethodFilter;");
        mOut.println("import com.arc.seecode.serialize.IMethodSerializer;");
        mOut.println("import com.arc.seecode.serialize.MethodSerializationHandler;");
        mOut.println();
    }

    private void printClassDefinition(String className) {
        mOut.println("public class " + className + " extends AbstractRemoteCmpdController {");
        mOut.println();
        mOut.println("    /**");
        mOut.println("     */");
        mOut.println("    public " + className
                + "(IConnection connection, int timeout) {");
        mOut.println("        super(connection,timeout);");
        MethodDoc[] methods = getMethodsToEmit(mClassDoc);
        mOut
                .println("        MethodSerializationHandler ms = new MethodSerializationHandler(this,ICMPDController.class,new MyMethodFilter(),null,null);");
        for (int i = 0; i < methods.length; i++) {
            MethodDoc m = methods[i];
            mOut.println("        " + methodSerializerName(m)
                    + " = ms.getSerializerFor(\"" + m.name() + "\");");
        }

        mOut.println("    }");
        for (int i = 0; i < methods.length; i++) {
            printMethodDefinition(methods[i]);
        }
        
        mOut.println();
        mOut.println("    static class MyMethodFilter implements IMethodFilter {");
        mOut.println("        public boolean includeMethod(Method m){");
        mOut.println("             return");
        Method[] finalMethods = com.arc.seecode.client.AbstractClient.class.getDeclaredMethods();
        for (int i = 0; i < finalMethods.length; i++){
            Method m = finalMethods[i];
            if (i > 0) mOut.print("  &&");
            mOut.println();
            mOut.print("              ");
            mOut.print("!m.getName().equals(\"" +
                    m.getName() + "\")");
        }
        mOut.println(";");
        mOut.println("        }");
        mOut.println("    }");
        

        mOut.println();
        for (int i = 0; i < methods.length; i++) {
            mOut.println("    private IMethodSerializer "
                    + methodSerializerName(methods[i]) + ";");
        }
        mOut.println("}");
    }
    
    private static MethodDoc[] getMethodsToEmit(ClassDoc c){
        // Filter out the "set...Observer" methods. They are
        // handled in base class. In fact, filter out all
        // methods that are defined in base class.
        Method[] methods = com.arc.seecode.internal.cmpd.AbstractRemoteCmpdController.class.getDeclaredMethods();
        ArrayList<MethodDoc> list = new ArrayList<MethodDoc>();
        MethodDoc[] methodDocs = c.methods();
        for (int i = 0; i < methodDocs.length; i++){
            MethodDoc m = methodDocs[i];
            String name = m.name();
            boolean found = false;
            for (int j = 0; j < methods.length; j++){
                if (name.equals(methods[j].getName())){
                    found = true;
                    break;
                }
            }
            if (!found){
                list.add(m);
            }
        }
        
        return list.toArray(new MethodDoc[list.size()]);
    }

    private String fullName(MethodDoc m) {
        StringBuffer buf = new StringBuffer();
        buf.append(m.name());
        buf.append(m.flatSignature());
        return buf.toString();
    }
    
    private String typeName(Type type){
        return type.typeName() + type.dimension();
    }

    private void printMethodDefinition(MethodDoc m) {
        mOut.println();
        mOut.println("    /**");
        mOut
                .println("     * Implements {@link ICMPDController#" + fullName(m)
                        + "}");
        mOut
                .println("     *  by serializing the arguments, sending a command to the engine process,");
        mOut
                .println("     *  waiting for the reply, deserialize the result, and then return it.");
        mOut.println("     */");
        mOut.print("    public ");
        mOut.print(typeName(m.returnType()));
        mOut.print(" " + m.name() + "(");
        Parameter[] parms = m.parameters();
        for (int i = 0; i < parms.length; i++) {
            Parameter p = parms[i];
            if (i > 0) mOut.print(", ");
            mOut.print(typeName(p.type()) + " " + p.name());
        }
        mOut.println(") throws EngineException {");
        mOut.print("        Object[] argsArray = new Object[]{");
        for (int i = 0; i < parms.length; i++) {
            if (i > 0) mOut.print(",");
            mOut.println();
            mOut.print("                ");
            mOut.print(objectizedParm(parms[i]));
        }
        mOut.println("};");
        mOut.print("        ");
        Type t = m.returnType();
        if (!isVoid(t)) {
            mOut.print("Object result = ");
        }
        mOut.print("this.invokeRemoteMethod(" + methodSerializerName(m)
                + ", argsArray");
        // loadProgram needs its own larger timeout value and a possible callback to
        // extend it.
        if (m.name().equals("loadProgram"))
            mOut.print(",mLoadTimeout,mLoadTimeoutCallback");
        mOut.println(");");
        if (!isVoid(t)) {
            mOut.print("        return ");
            if (!isPrimitive(t)) {
                mOut.println("(" + typeName(t) + ")result;");
            } else {
                mOut.println("((" + boxedTypeFor(t) + ")result)."
                        + extractFunctionFor(t) + "();");

            }
        }
        mOut.println("    }");
    }

    private static String objectizedParm(Parameter p) {
        if (isPrimitive(p.type())) { return "new " + boxedTypeFor(p.type())
                + "(" + p.name() + ")"; }
        return p.name();
    }

    private static boolean isVoid(Type t) {
        return "void".equals(t.typeName());
    }

    private static boolean isPrimitive(Type t) {
        if (t.dimension() != null && t.dimension().length()!=0) return false;
        return Character.isLowerCase(t.typeName().charAt(0));
    }

    private static String boxedTypeFor(Type t) {
        if ("int".equals(t.toString())) { return "Integer"; }
        return capitalize(t.typeName());
    }

    private static String extractFunctionFor(Type t) {
        return t.typeName() + "Value";
    }

    private static String capitalize(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    private static String methodSerializerName(MethodDoc m) {
        return "m" + capitalize(m.name());
    }

    private void generate(String packageName, String className) {
        printPrefix();
        printPackageAndImports(packageName);
        printClassDefinition(className);
        mOut.flush();
    }

    public static boolean start(RootDoc root) {
        try {
            GenCMPDClient gen = new GenCMPDClient(root, new File(sOutputDir,
                    "RemoteCmpdController.java"));
            gen.generate("com.arc.seecode.internal.cmpd", "RemoteCmpdController");
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
        return true;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Args are: <path-to-ICMPController> <output-dir>");
            System.exit(1);
        }
        String sourcePath = args[0];
        sOutputDir = args[1];

        File f = new File(sourcePath);
        if (!f.isDirectory())
                throw new IllegalArgumentException("Not a directory: "
                        + sourcePath);
        if (!new File(f, "com/arc/seecode/cmpd/ICMPDController.java").exists()) { throw new IllegalArgumentException(
                "Can't find source file"); }
        com.sun.tools.javadoc.Main.main(new String[] { "-docletpath", ".",
                "-doclet", GenCMPDClient.class.getName(), "-source", "1.5",
                "-verbose", "-sourcepath", sourcePath, "-private",
                "com.arc.seecode.cmpd"});
    }

}
