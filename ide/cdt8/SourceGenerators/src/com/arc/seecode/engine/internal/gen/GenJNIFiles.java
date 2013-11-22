/*
 * Created on Jul 18, 2003
 *
 */


package com.arc.seecode.engine.internal.gen;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.arc.seecode.engine.gen.JNIInfo;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;
import com.sun.javadoc.Type;


/**
 * Generate the C++ files for interfacing to Java.
 * <P>
 * Three files are generated:
 * <nl>
 * <li>The C++ file that implements the native methods.
 * <li>The C++ header file for the EngineInterface class.
 * <li>A skeletal implementation of the EngineInterface class.
 * </nl>
 * @author David Pickens
 */
public class GenJNIFiles extends Doclet {

    private static final String INIT_IDS_METHOD_NAME = "initMethodAndFieldIDs";

    private ClassDoc mClassDoc;

    private String mCPPClassName; // name of parrallel C++ class

    private String mJavaInterfaceName; // full path of Java interface being C++'ized

    private String mJavaInterfaceNameOnly; // just the name part.

    private String mClassPrefix; // Fully qualified prefix of classes of interest ("com.arc.")

    private String mObjectField; // name of field that references C++ object.

    private String mInterfaceHeaderFile;

    private String mGetInterface;

    private FieldDoc mField;
    
    private boolean mNeedFactories;

    private static JNIInfo INFO = null;

    public static int generate (JNIInfo info) {
        INFO = info;
        String sourcePath = info.sourceDirectory;
        if (sourcePath == null)
            sourcePath = ".";

        checkForNull(info.rootPackage, "rootPackage");
        checkForNull(info.implementationFile, "implementationFile");
        checkForNull(info.cppClassName, "cppClassName");
        checkForNull(info.javaInterfaceName, "javaInterfaceName");
        checkForNull(info.classPrefix, "classPrefix");
        checkForNull(info.objectField, "objectField");
        checkForNull(info.interfaceHeaderFile, "interfaceHeaderFile");

        int exitCode = com.sun.tools.javadoc.Main.execute(new String[] {
                "-doclet",
                GenJNIFiles.class.getName(),
                "-source",
                "1.5",
                "-sourcepath",
                sourcePath,
                "-private",
                "-subpackages",
                info.rootPackage });
        return exitCode;
    }

    private static void checkForNull (Object o, String what) {
        if (o == null)
            throw new IllegalArgumentException(what + " is null");
    }

    public static boolean start (RootDoc root) {
        try {
            GenJNIFiles gen = new GenJNIFiles(root, INFO);
            gen.generateEngineImplementation(INFO.implementationFile);
            gen.generateInterfaceHeaderFile(INFO.interfaceHeaderFile);
            if (INFO.skeletonFile != null)
                gen.generateInterfaceSkeleton(INFO.skeletonFile);
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
        return true;
    }

    GenJNIFiles(RootDoc root, JNIInfo info) throws ClassNotFoundException {
        mClassDoc = root.classNamed(info.className);
        mCPPClassName = info.cppClassName;
        mJavaInterfaceName = info.javaInterfaceName;
        mClassPrefix = info.classPrefix;
        mObjectField = info.objectField;
        mInterfaceHeaderFile = info.interfaceHeaderFile;
        mGetInterface = "get" + mCPPClassName;
        try {
            findMethod(mClassDoc, "getJavaFactory");
            mNeedFactories = true;
        }
        catch (IllegalArgumentException x) {
            mNeedFactories = false;
        }
        int i = info.javaInterfaceName.lastIndexOf('.');
        if (i > 0) {
            mJavaInterfaceNameOnly = info.javaInterfaceName.substring(i + 1);
        }
        else
            mJavaInterfaceNameOnly = info.javaInterfaceName;
        if (mClassDoc == null)
            throw new ClassNotFoundException("Can't find " + info.className);
    }

    private void generateEngineImplementation (String cppFile) throws IOException {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(cppFile)));

        printHeader(out, "Java native method implementation");
        MethodDoc[] methods = extractNativeMethods(mClassDoc, false);
        Set<ClassDoc> classesToBeWrapped = extractClassesToBeWrapped();
        FieldDoc[] fields = extractInstanceFields(mClassDoc);
        if (fields.length == 0) {
            throw new Error(mClassDoc.name() + " has no instance field!");
        }
        mField = fields[0];

        emitJNIDefinitionPrefix(out, mField);

        defineMethodIDs(out, classesToBeWrapped);
        defineWrapperClasses(out, classesToBeWrapped);

        for (int i = 0; i < methods.length; i++) {
            emitJNIMethodDefinition(out, methods[i]);
        }

        writeExcludeEnsurePragmas(out, methods, false);

        out.close();

    }

    /**
     * @param out
     * @param classesToBeWrapped
     */
    private void defineMethodIDs (PrintWriter out, Collection<ClassDoc> classesToBeWrapped) {
        for (ClassDoc c : classesToBeWrapped) {
            MethodDoc methods[] = c.methods();
            out.println("static jclass " + classIDNameFor(c) + "; // " + c.qualifiedName());
            for (int i = 0; i < methods.length; i++) {
                if (isMethodToBeAccessedFromC(methods[i]))
                    out.println("static jmethodID " + methodIDNameFor(methods[i]) + ';');
            }
        }
        out.println();
        out.println("static void error(const char *fmt,...){");
        out.println("    va_list ap;");
        out.println("    va_start(ap,fmt);");
        out.println("    static FILE *errout = 0;\n"
            + "    if (errout == 0){\n"
            + "        errout = fopen(\"debugger_error.log\",\"w\");\n"
            + "        if (errout == 0) errout = stderr;\n"
            + "    }\n"
            + "    vfprintf(errout,fmt,ap); fflush(errout); \n"
            + "    va_end(ap);\n"
            + "}");
        out.println();
        out.println("static jclass findClass(JNIEnv *env, const char *className, bool abortIfMissing){");
        out.println("    jclass c = env->FindClass(className);");
        out.println("    if ( c == 0) {\n" + "        if (abortIfMissing){");
        out.println("            error(\">>>Can't find %s\\n\",className);");
        out.println("            exit(5);");
        out.println("        }");
        out.println("        env->ExceptionClear();");
        out.println("        return 0;");
        out.println("    }");
        out.println("    return (jclass) env->NewGlobalRef((jobject)c);");
        out.println("}");

        out.println();
        out
            .println("static jmethodID findMethod(JNIEnv *env, jclass clazz, const char *name, const char *sig, const char*cls, bool complainIfMissing){");
        out.println("    jmethodID methodID = env->GetMethodID(clazz,name,sig);");
        out.println("    if (methodID == 0) {");
        out.println("        env->ExceptionClear();");
        out.println("        if (complainIfMissing) {");

        out.println("            error(\">>>Can't find method \\\"%s\\\" \\\"%s\\\" in %s\\n\",");
        out.println("               name,sig,cls);");
        out.println("        }");
        out.println("    }");
        out.println("    return methodID;");
        out.println("}");
        out.println();
        out.println("static void " + INIT_IDS_METHOD_NAME + "(JNIEnv *env) { ");
       
        boolean first = true;
        for (ClassDoc c : classesToBeWrapped) {
            MethodDoc[] methods = c.methods();
            if (methods.length > 0) {
                if (first) {
                    first = false;
                    out.println("    jclass clazz;");
                    out.println("    const char *className;");
                }
                out.println("    className = \"" + c.qualifiedName().replace('.', '/') + "\";");
                out.println("    clazz = findClass(env,className," + isRequiredClass(c) + ");");
                out.println("    if (clazz != 0) {");
                out.println("        " + classIDNameFor(c) + " = clazz;");

                for (int i = 0; i < methods.length; i++) {
                    if (isMethodToBeAccessedFromC(methods[i])) {
                        out.println("        " +
                            methodIDNameFor(methods[i]) +
                            " = findMethod(env,clazz,\"" +
                            methods[i].name() +
                            "\",\"" +
                            signatureFor(methods[i]) +
                            "\",className," +
                            isRequiredMethod(methods[i]) +
                            ");");
                    }
                }
            }
            out.println("        }");
        }
        out.println("    }");
    }

    /**
     * @param m the method whose signature is required.
     * @return the signature of a method.
     */
    private static String signatureFor (MethodDoc m) {
        StringBuffer buf = new StringBuffer();
        buf.append("(");
        Parameter[] parms = m.parameters();
        for (int i = 0; i < parms.length; i++) {
            Parameter p = parms[i];
            buf.append(signatureFor(p.type()));
        }
        buf.append(")");
        buf.append(signatureFor(m.returnType()));
        return buf.toString();
    }

    private static String signatureFor (Type type) {
        if (isObjectArrayType(type)) {
            return "[L" + type.qualifiedTypeName().replace('.', '/') + ';';
        }
        if (isPrimitiveArrayType(type)) {
            String baseType = type.typeName();
            if (baseType.equals("byte"))
                return "[B";
            if (baseType.indexOf("short") >= 0)
                return "[S";
            if (baseType.indexOf("long") >= 0)
                return "[J";
            if (baseType.indexOf("char") >= 0)
                return "[C";
            return "[I";
        }

        if (isVoidType(type))
            return "V";
        if (isBoolean(type))
            return "Z";
        if (isLong(type))
            return "J";
        if (isObjectType(type)) {
            return "L" + type.qualifiedTypeName().replace('.', '/') + ';';
        }
        return "I";
    }

    /**
     * @param doc
     * @return the method ID for a method.
     */
    private static String methodIDNameFor (MethodDoc doc) {
        ClassDoc cd = doc.containingClass();
        String name = doc.name();
        MethodDoc md[] = cd.methods();
        // If name isn't unique, the suffix an index to it.
        int whichOne = -1;
        boolean isUnique = true;
        for (int i = 0; i < md.length; i++) {
            if (md[i] == doc)
                whichOne = i;
            else if (md[i].name().equals(name)) {
                isUnique = false;
            }
        }
        if (!isUnique) {
            name = name + "_" + whichOne;
        }
        return "id_" + doc.containingClass().name() + "_" + name;
    }

    /**
     * For each interface of a collection of interfaces, define a wrapper class so that we can hide the
     * Java-dependencies from C++.
     * @param out
     * @param classesToBeWrapped
     */
    private void defineWrapperClasses (PrintWriter out, Collection<ClassDoc> classesToBeWrapped) {
        for (ClassDoc c : classesToBeWrapped) {
            defineWrapperClass(out, c, mCPPClassName);
        }

    }

    /**
     * @param out
     * @param c
     */
    private void defineWrapperClass (PrintWriter out, ClassDoc c, String cppClassName) {
        out.println("class " + wrapperNameFor(className(c)) + ": public " + className(c) + " {");
        out.println("    private:");
        out.println("    " + cppClassName + " *mEngine;    // So as to access latest Java environment pointer");
        out.println();
        out.println("    public:");
        out.println("    " +
            wrapperNameFor(className(c)) +
            "(" +
            cppClassName +
            " *engine, jobject object): " +
            c.name() +
            "(object){ ");
        out.println("        mEngine = engine;");
        out.println("        }");
        out.println();

        if (isListenerClass(c)) {
            out.println("    /* Make sure the global java object is deleted */");
            out.println("    ~" + wrapperNameFor(c.name()) + "(){");
            out.println("        mEngine->getJNIEnv()->DeleteGlobalRef(getJavaObject());");
            out.println("        }");
            out.println();
        }

        MethodDoc methods[] = getMethodsToWrap(c);
        for (int i = 0; i < methods.length; i++) {
            out.println();
            if (isMethodToBeAccessedFromC(methods[i]))
                defineWrapperMethod(out, methods[i], isListenerClass(c));
        }
        out.println("};");
    }

    /**
     * New methods are tagged as "@new" and are not required to be defined. This allows newer debuggers to talk to older
     * IDEs.
     * @param doc the method being tested.
     * @return true if the method is not required to be defined in the IDE.
     */
    private static boolean isRequiredMethod (MethodDoc doc) {
        Tag[] tags = doc.tags("@new");
        return tags == null || tags.length == 0;
    }

    /**
     * New classes are tagged as "@new" and are not required to be defined. This allows newer debuggers to talk to older
     * IDEs.
     * @param doc the method being tested.
     * @return true if the method is not required to be defined in the IDE.
     */
    private static boolean isRequiredClass (ClassDoc doc) {
        Tag[] tags = doc.tags("@new");
        return tags == null || tags.length == 0;
    }

    /**
     * If the tag "@nojni" appears, then do not emit the wrapper for the method. It won't be called from C.
     * @param doc
     * @return whether or not the method is to be accessed from C.
     */
    private static boolean isMethodToBeAccessedFromC (MethodDoc doc) {
        if (doc.isSynthetic())
            return false;
        Tag[] tags = doc.tags("@nojni");
        return !doc.isStatic() && (tags == null || tags.length == 0);
    }

    /**
     * @param c
     * @return whether or not this is a callback class; i.e., C++ will be calling back through it.
     */
    private static boolean isListenerClass (ClassDoc c) {
        return c.name().equals("ICustomDisplayCallback") || c.name().equals("IEngineAPIObserver");
    }

    /**
     * Return an array of methods that we want to wrap in a class for which a wrapper is being defined. We don't bother
     * with "get" methods that return objects because the engine has no need to access them, and we don't know when to
     * dispose of them.
     * @param c the class whose methods we want to wrap.
     * @return array of methods to wrap.
     */
    private static MethodDoc[] getMethodsToWrap (ClassDoc c) {
        ArrayList<MethodDoc> list = new ArrayList<MethodDoc>();
        MethodDoc methods[] = c.methods();
        for (int i = 0; i < methods.length; i++) {
            MethodDoc m = methods[i];
            if (shouldBeWrapped(m))
                list.add(m);
        }
        return list.toArray(new MethodDoc[list.size()]);
    }

    private static boolean shouldBeWrapped (MethodDoc m) {
        // Value.stringize is used internally only.
        if (m.isPrivate())
            return false;
        if (!m.name().startsWith("get"))
            return true;
        if (m.isStatic())
            return false;
        return !isObjectType(m.returnType());
    }

    private void defineWrapperMethod (PrintWriter out, MethodDoc m, boolean isEngineCallback) {
        out.print("    _Override " + cppFullType(m.returnType()));
        out.print(" " + m.name() + "(");
        Parameter parms[] = m.parameters();
        for (int i = 0; i < parms.length; i++) {
            if (i > 0)
                out.print(",");
            out.print(cppFullType(parms[i].type()) + " " + parms[i].name());

        }
        out.println(")");
        out.println("        {");
        if (!isRequiredMethod(m)) {
            out
                .println("        //This method was added in later versions of IDE and isn't required to be defined at startup");
            out.println("        if (" + methodIDNameFor(m) + " == 0) {");
            out.println("            error(\"Undefined method called: " +
                m.containingClass().name() +
                "::" +
                m.name() +
                "\");");
            out.println("            fprintf(stderr,\"Undefined method " +
                m.containingClass().name() +
                "::" +
                m.name() +
                "\");");
            if (!isVoidType(m.returnType())) {
                out.println("            return 0;");
            }
            else
                out.println("            return;");
            out.println("        }");
        }
        String env = "env";
        out.println("        JNIEnv *" + env + " = mEngine->getJNIEnv();");
        String parmNames[] = new String[parms.length];
        for (int i = 0; i < parms.length; i++) {
            Parameter p = parms[i];
            parmNames[i] = p.name();
            if (isObjectArrayType(p.type())) {
                parmNames[i] = p.name() + "Array";
                out.println("        " + cppType(p.type()) + " " + parmNames[i] + " = 0;");
                out.println("        if (" + p.name() + " != 0) {");
                out.println("            " +
                    parmNames[i] +
                    " = " +
                    env +
                    "->NewObjectArray(" +
                    p.name() +
                    "->count()," +
                    classIDNameFor(p.type().asClassDoc()) +
                    ",0);");
                out.println("            for (int i = 0; i < " + p.name() + "->count(); i++) {");
                out.print("                " + env + "->SetObjectArrayElement(" + parmNames[i] + ",i,");
                if (isStringType(p.type())) {
                    out.println(env + "->NewStringUTF((*" + p.name() + ")[i]));");
                }
                else
                    out.println("(*" + p.name() + ")[i].getJavaObject());");
                out.println("                }");
                out.println("        }");
            }
            else if (isPrimitiveArrayType(p.type())) {
                parmNames[i] = p.name() + "Array";
                out.println("        " + cppType(p.type()) + " " + parmNames[i] + " = 0;");
                out.println("        if (" + p.name() + " != 0) {");
                String tn = p.type().typeName();
                tn = tn.substring(0, 1).toUpperCase() + tn.substring(1);
                out.println("            " +
                    parmNames[i] +
                    " = " +
                    env +
                    "->New" +
                    tn +
                    "Array(" +
                    p.name() +
                    "->count());");
                out.println("            " +
                    env +
                    "->Set" +
                    tn +
                    "ArrayRegion(" +
                    parmNames[i] +
                    ",0," +
                    p.name() +
                    "->count()," +
                    p.name() +
                    "->data());");
                out.println("        }");
            }
            else if (isStringType(p.type())) {
                parmNames[i] = p.name() + "String";
                out.println("        jstring " +
                    parmNames[i] +
                    " = " +
                    p.name() +
                    "?" +
                    env +
                    "->NewStringUTF(" +
                    p.name() +
                    "):0;");
            }
            else if (isObjectType(p.type())) {
                parmNames[i] = p.name() + "Object";
                out.println("        jobject " +
                    parmNames[i] +
                    " = " +
                    p.name() +
                    "?" +
                    p.name() +
                    "->getJavaObject():0;");
            }
        }
        out.print("        ");
        if (!isVoidType(m.returnType())) {
            out.print(cppType(m.returnType()) + " result = (" + cppType(m.returnType()) + ")");
        }

        out.print(env + "->Call" + jniType(m.returnType()) + "Method(getJavaObject()," + methodIDNameFor(m));

        for (int i = 0; i < parms.length; i++) {
            out.print("," + parmNames[i]);
        }
        out.println(");");
        if (!isVoidType(m.returnType())) {
            if (isStringType(m.returnType())) {
                out.println("        if (result == 0) return 0;");
                out.println("        //NOTE: possible memory leak!");
                out.println("        const char *string = " + env + "->GetStringUTFChars(result,0);");
                out.println("        char *resultString = strdup(string);");
                out.println("        env->ReleaseStringUTFChars(result,string);");
                out.println("        return resultString;");
            }
            else if (isObjectType(m.returnType())) {
                out.println("        return new " + wrapperNameFor(m.returnType()) + "( mEngine, result);");
            }
            else
                out.println("        return result;");
        }
        out.println("        }");

    }

    /**
     * @param type
     * @return the equivalent JNI name for a Java type.
     */
    private static String jniType (Type type) {
        if (isObjectType(type))
            return "Object";
        String name = type.typeName();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * @param type
     * @return the equivalent primitive C++ type for a Java type.
     */
    private static String cppType (Type type) {

        if (isObjectArrayType(type))
            return "jobjectArray";
        if (isStringType(type))
            return "jstring";
        if (isPrimitiveArrayType(type))
            return "j" + type.typeName() + "Array";
        if (isObjectType(type))
            return "jobject";
        if (isBoolean(type))
            return "jboolean";
        if (isLong(type))
            return "jlong";
        if (type.typeName().equals("int"))
            return "jint";
        return type.typeName();
    }

    /**
     * @param type
     * @return whether or not the type is an array-of-object type.
     */
    private static boolean isObjectArrayType (Type type) {
        if (type.qualifiedTypeName().indexOf('.') > 0) {
            String dim = type.dimension();
            if (dim != null && dim.length() > 0)
                return true;
        }
        return false;
    }

    private static boolean isStringArrayType (Type type) {
        return type.qualifiedTypeName().equals("java.lang.String") && isObjectArrayType(type);
    }

    private static boolean isArrayType (Type type) {
        String dim = type.dimension();
        return dim != null && dim.length() > 0;
    }

    /**
     * @param type
     * @return whether or not the type is an array of a primitive type.
     */
    private static boolean isPrimitiveArrayType (Type type) {
        if (type.qualifiedTypeName().indexOf('.') == -1) {
            String dim = type.dimension();
            if (dim != null && dim.length() > 0)
                return true;
        }
        return false;
    }

    /**
     * @param type
     * @return the full C++ equivelent of a Jav atype.
     */
    private String cppFullType (Type type) {
        if (isStringArrayType(type))
            return "Vector_list<const char*>*";
        if (isStringType(type))
            return "const char *";
        if (isBoolean(type))
            return "bool";
        if (isObjectArrayType(type))
            return "Vector_list<" + type.typeName() + "*>*";
        if (isPrimitiveArrayType(type))
            return "Vector_list<j" + type.typeName() + ">*";
        if (isLong(type))
            return "jlong";
        if (isObjectType(type)) {
            // EngineAPI or IEngineAPI must map to C++ class.
            if (type.typeName().endsWith(mJavaInterfaceNameOnly))
                return mCPPClassName + " *";
            return type.typeName() + " *";
        }
        return type.typeName();
    }

    private String cppFullTypeClass (Type type) {
        String t = cppFullType(type);
        if (t.endsWith("*"))
            t = t.substring(0, t.length() - 1);
        return t;
    }

    /**
     * Find all Java interfaces referenced from methods.
     * @return set of classes to wrap.
     */
    private Set<ClassDoc> extractClassesToBeWrapped () {
        MethodDoc methods[] = extractNativeMethods(mClassDoc, false);
        // Use linked hash set so that the JavaFactory is put out last
        // since it references other classes.
        Set<ClassDoc> set = new LinkedHashSet<ClassDoc>();
        extractClassesRefdFromMethods(methods, set);
        //
        // Also include JavaFactory and Typefactory
        //
        if (mNeedFactories){
            MethodDoc jf = findMethod(mClassDoc, "getJavaFactory");
            set.add(jf.returnType().asClassDoc());
            MethodDoc tf = findMethod(mClassDoc, "getTypeFactory");
            set.add(tf.returnType().asClassDoc());
        }
        return set;
    }

    private boolean isEngineAPI (Type t) {
        return t.typeName().equals(mJavaInterfaceNameOnly) || t.typeName().equals("I" + mJavaInterfaceNameOnly);

    }

    /**
     * Any class tagged as "@nojni" won't be emitted.
     * @param doc
     * @return true if to be emitted.
     */
    private static boolean isAccessibleFromC (ClassDoc doc) {
        Tag[] tags = doc.tags("@nojni");
        return tags == null || tags.length == 0;
    }

    private void extractClassesRefdFromMethods (MethodDoc methods[], Set<ClassDoc> set) {
        for (int i = 0; i < methods.length; i++) {
            MethodDoc m = methods[i];
            if (isMethodToBeAccessedFromC(m)) {
                Type returnType = m.returnType();
                if (isObjectType(returnType) && !isStringType(returnType) && isAccessibleFromC(returnType.asClassDoc()))
                    addClassToSet(returnType.asClassDoc(), set);
                Parameter[] parms = m.parameters();
                for (int j = 0; j < parms.length; j++) {
                    Type t = parms[j].type();
                    if (isObjectType(t) && !isStringType(t) && !isEngineAPI(t) && isAccessibleFromC(t.asClassDoc()))
                        addClassToSet(t.asClassDoc(), set);
                }
            }
        }
    }

    private void addClassToSet (ClassDoc c, Set<ClassDoc> set) {
        if (!set.contains(c) &&
            c.qualifiedName().startsWith(mClassPrefix) &&
            !c.qualifiedName().equals(mJavaInterfaceName)) {
            set.add(c);
            MethodDoc methods[] = c.methods();
            extractClassesRefdFromMethods(methods, set);
        }
    }

    private void emitJNIDefinitionPrefix (PrintWriter out, FieldDoc field) {
        out.println("#include \"" + mJavaInterfaceName.replace('.', '_') + ".h\"");
        out.println("#include \"" + mInterfaceHeaderFile + "\"");
        out.println("#include <stdio.h>    // fprintf(stderr,...)");
        out.println("#include <stdlib.h>   // exit()");
        out.println("#include <stdarg.h>");
        out.println("#include <string.h>");
        out.println();
        out.println("#pragma on(for_dcln_local)");
        out.println("#pragma offwarn(127)   // Missing vtable seat");
        out.println();
        out.println("static jfieldID " + mObjectField + ";");
        out.println("static jclass " + classIDNameFor("String") + ";  // java.lang.String");
        out.println();
        out.println("static " + mCPPClassName + " *" + mGetInterface + "(JNIEnv *env, jobject this_){");
        out.println("    " +
            mCPPClassName +
            "*e = (" +
            mCPPClassName +
            "*)env->GetIntField(this_," +
            mObjectField +
            ");");
        out.println("    e->setJNIEnv(env); // callbacks need this");
        out.println("    return e;");
        out.println("}");
        out.println();
        out.println("static const char *getStringUTFChars(JNIEnv *env, jstring s) {");
        out.println("    if (s == 0) return 0;");
        out.println("    return env->GetStringUTFChars(s,0);");
        out.println("}");
        out.println();
        out.println("static void releaseStringUTFChars(JNIEnv *env, jstring s, const char *t){");
        out.println("    if (s == 0) return;");
        out.println("    env->ReleaseStringUTFChars(s,t);");
        out.println("}");
    }

    // /**
    // * @param field
    // * @return
    // */
    // private static String fieldIDNameFor(FieldDoc field) {
    // return "id_" + field.name();
    // }

    private String getJNIName (MethodDoc m) {
        return "Java_" + m.containingClass().qualifiedName().replace('.', '_') + "_" + m.name();
    }

    private void emitJNIMethodDefinition (PrintWriter out, MethodDoc m) {

        out.println();
        out.println("/*");
        out.println(" * Class:  " + m.containingClass().qualifiedName());
        out.println(" * Method: " + m.name());
        out.println(" * Signature:  " + m.flatSignature());
        out.println(" * Returns: " + m.returnType().toString());
        out.println(" */");
        out.println("extern \"C\" JNIEXPORT " + cppType(m.returnType()) + " JNICALL " + getJNIName(m));
        out.print("    (JNIEnv *env, ");
        if (m.isStatic())
            out.print("jclass klass");
        else
            out.print("jobject this_");

        Parameter[] parms = m.parameters();
        for (int i = 0; i < parms.length; i++) {
            Parameter p = parms[i];
            out.println(",");
            out.print("\t" + cppType(p.type()) + " " + p.name());
        }
        out.println(")");
        out.println("    {");

        /* emit declarations for each parameter... */

        if (m.name().equals(INFO.initInterfaceMethodName))
            emitJNIInitBody(out, mField.name());
        else {
            String[] parmNames = new String[parms.length];

            for (int i = 0; i < parms.length; i++) {
                Parameter p = parms[i];
                parmNames[i] = p.name();
                if (isArrayType(p.type())) {
                    parmNames[i] = p.name() + "Array";
                    out.println("    " + cppFullType(p.type()) + " " + parmNames[i] + " = 0;");
                    String lengthName = p.name() + "_length";
                    out.println("    int " + lengthName + " = 0;");
                    out.println("    if ( " + p.name() + " != 0 ) {");
                    out.println("        " + lengthName + " = env->GetArrayLength(" + p.name() + ");");
                    out.println("        " +
                        parmNames[i] +
                        " = " +
                        "new " +
                        cppFullTypeClass(p.type()) +
                        "(" +
                        lengthName +
                        ");");

                    if (!isObjectArrayType(p.type())) {
                        out.println("        j" +
                            p.type().typeName() +
                            " *" +
                            parmNames[i] +
                            "Elements = env->Get" +
                            jniType(p.type()) +
                            "ArrayElements(" +
                            p.name() +
                            ",0);");
                        out.println("        for (int i = 0; i < " + p.name() + "_length; i++){");
                        out.println("            " + parmNames[i] + "->add(" + parmNames[i] + "Elements[i]);");
                        out.println("            }");
                        out.println("        env->Release" +
                            jniType(p.type()) +
                            "ArrayElements(" +
                            p.name() +
                            "," +
                            parmNames[i] +
                            "Elements,JNI_ABORT);");
                    }
                    else {
                        out.println("        for (int i = 0; i < " + p.name() + "_length; i++){");
                        out.print("            " + parmNames[i] + "->add(");
                        if (isStringArrayType(p.type())) {
                            out.println("getStringUTFChars(env,(jstring)env->GetObjectArrayElement(" +
                                p.name() +
                                ",i)));");
                        }
                        else {
                            out.println(wrapperNameFor(p.type()) +
                                "( env, " +
                                "env->GetObjectArrayElement(" +
                                p.name() +
                                ",i)));");
                        }
                        out.println("            }");
                    }
                    out.println("        }");
                }
                else if (isStringType(p.type())) {
                    parmNames[i] = p.name() + "String";
                    out.println("    const char *" +
                        parmNames[i] +
                        " = " +
                        p.name() +
                        "?" +
                        "getStringUTFChars(env," +
                        p.name() +
                        "):0;");
                }
                else if (isObjectType(p.type())) {
                    parmNames[i] = p.name() + "Wrapper";
                    if (isSpecialSetMethod(m))
                        out.println("    " +
                            p.name() +
                            " = " +
                            p.name() +
                            "?env->NewGlobalRef((jobject)" +
                            p.name() +
                            "):0;");

                    out.println("    " +
                        p.type().typeName() +
                        " *" +
                        parmNames[i] +
                        " = " +
                        p.name() +
                        "?new " +
                        wrapperNameFor(p.type()) +
                        "( " +
                        mGetInterface +
                        "(env,this_), " +
                        p.name() +
                        "):0;");
                }
            }
            if (!isVoidType(m.returnType())) {
                out.print("    " + cppFullType(m.returnType()) + " result = ");
            }
            else
                out.print("    ");
            if (m.isStatic()) {
                out.print(mCPPClassName + "::");
            }
            else {
                out.print("" + mGetInterface + "(env,this_)->");
            }
            out.print(m.name() + "(");
            for (int i = 0; i < parmNames.length; i++) {
                if (i > 0)
                    out.print(",");
                out.print(parmNames[i]);
            }
            out.println(");");
            // dispose of strings and objects
            for (int i = 0; i < parms.length; i++) {
                Parameter p = parms[i];
                if (isObjectArrayType(p.type())) {
                    out.println("    if (" + parmNames[i] + " != 0) {");
                    out.println("        for(int i = 0; i < " + p.name() + "_length; i++){");
                    if (isStringArrayType(p.type())) {
                        out.println("            releaseStringUTFChars(env," +
                            "(jstring)env->GetObjectArrayElement(" +
                            p.name() +
                            ",i),(*" +
                            parmNames[i] +
                            ")[i]);");
                    }
                    else {
                        out.println("            delete (*" + parmNames[i] + ")[i];");
                    }
                    out.println("        }");
                    out.println("        delete " + parmNames[i] + ";");
                    out.println("    }");
                }
                else if (isArrayType(p.type())) {
                    out.println("    if (" + parmNames[i] + " != 0) {");
                    out.println("        delete " + parmNames[i] + ";");
                    out.println("    }");
                }
                else if (isStringParm(p)) {
                    out.println("    if (" + parmNames[i] + " != 0) {");
                    out.println("        releaseStringUTFChars(env," + p.name() + "," + parmNames[i] + ");");
                    out.println("    }");
                }
                else if (isObjectParm(p)) {
                    if (!isSpecialSetMethod(m)) {
                        out.println("    if (" + parmNames[i] + " != 0) {");
                        out.println("        delete " + parmNames[i] + ";");
                        out.println("        }");
                    }
                }
            }
            if (!isVoidType(m.returnType())) {

                if (isObjectArrayType(m.returnType())) {
                    out.println("    if (result == 0) return 0;");
                    out.println("    jobjectArray resultArray = " +
                        "env->NewObjectArray(result->count()," +
                        classIDNameFor(m.returnType().typeName()) +
                        ",0);");
                    out.println("    for (int i = 0; i < result->count(); i++){");
                    out.print("        env->SetObjectArrayElement(resultArray,i,");
                    if (isStringArrayType(m.returnType()))
                        out.println("env->NewStringUTF((*result)[i]));");
                    else {
                        out.println("(*result)[i]->getJavaObject());");
                        out.println("        delete (*result)[i];");
                    }

                    out.println("        }");
                    out.println("    delete result;");
                    out.println("    return resultArray;");
                }
                else if (isPrimitiveArrayType(m.returnType())) {
                    out.println("    if (result == 0) return 0;");
                    String tn = m.returnType().typeName();
                    tn = tn.substring(0, 1).toUpperCase() + tn.substring(1);
                    out.println("    " +
                        cppType(m.returnType()) +
                        " resultArray = " +
                        "env->New" +
                        tn +
                        "Array(result->count());");
                    out.println("    env->Set" + tn + "ArrayRegion(resultArray,0,result->count(),result->data());");
                    out.println("    delete result;");
                    out.println("    return resultArray;");
                }
                else if (isStringType(m.returnType())) {
                    out.println("    if (result == 0) return 0;");
                    out.println("    return env->NewStringUTF(result);");
                }
                else if (isObjectType(m.returnType())) {
                    out.println("    if (result == 0) return 0;");
                    out.println("    jobject resultObject = result->getJavaObject();");
                    out.println("    delete result;");
                    out.println("    return resultObject;");
                }
                else
                    out.println("    return result;");
            }
        }
        out.println("    }");

    }

    /**
     * @param m
     * @return if the method is somehow is used to set callbacks.
     */
    private static boolean isSpecialSetMethod (MethodDoc m) {
        return m.name().endsWith("Observer") || m.name().equals("setCustomDisplayCallback");
    }

    /**
     * @param string name of class.
     * @return the C++ variable name by which we store the classes ID name.
     */
    private static String classIDNameFor (String string) {
        return "id_" + string + "Class";
    }

    /**
     * @param c
     * @return the C++ variable name by which we will store the class ID of the java class.
     */
    private String classIDNameFor (ClassDoc c) {
        return classIDNameFor(className(c));
    }

    /**
     * @param type
     * @return the C++ name of the class that wraps the given type.
     */
    private static String wrapperNameFor (Type type) {
        String name = type.typeName();
        return wrapperNameFor(name);
    }

    private static String wrapperNameFor (String name) {
        if (name.charAt(0) == 'I')
            name = name.substring(1);
        return name + "Wrapper";
    }

    /**
     * @param type
     * @return whether or not the type is "boolean".
     */
    private static boolean isBoolean (Type type) {
        return type.typeName().equals("boolean");
    }

    private static boolean isLong (Type type) {
        return type.typeName().equals("long");
    }

    /**
     * @param type
     * @return wither or not the type is a class.
     */
    private static boolean isObjectType (Type type) {
        return type.qualifiedTypeName().indexOf('.') > 0;
    }

    /**
     * @param type
     * @return whether or not the type is String.
     */
    private static boolean isStringType (Type type) {
        return type.qualifiedTypeName().equals("java.lang.String");
    }

    /**
     * @param type
     * @return whether or not the type is "void".
     */
    private static boolean isVoidType (Type type) {
        return type.typeName().equals("void");
    }

    /**
     * @param out
     */
    private void emitJNIInitBody (PrintWriter out, String objectFieldName) {
        out.println("    jclass klass = env->GetObjectClass(this_);");
        out.println("    if (" + mObjectField + " == 0 ) {");
        out.println("        " + INIT_IDS_METHOD_NAME + "(env);");

        out.println("        " + mObjectField + " = env->GetFieldID(klass,\"" + objectFieldName + "\",\"I\");");
        out.println("        if (" + mObjectField + " == 0) {");
        out.println("           error(\">>>Can't find " + objectFieldName + "\\n\");");
        out.println("           exit(1);");
        out.println("           }");
        out.println("        }");
        // findMethod(mClassDoc, "getJavaFactory"); // make sure its there.
        out.println("    " + classIDNameFor("String") + " = findClass(env,\"java/lang/String\",true);");
        if (mNeedFactories) {
            out
                .println("    jmethodID getJavaFactoryID = findMethod(env,klass,\"getJavaFactory\",\"()Lcom/arc/seecode/engine/JavaFactory;\",\"EngineInterface\",false);");
            out.println("    if (getJavaFactoryID == 0) {");
            out.println("        error(\">>>Can't find JavaFactory\\n\");");
            out.println("        exit(2);");
            out.println("        }");
            out
                .println("    jmethodID getTypeFactoryID = findMethod(env,klass,\"getTypeFactory\",\"()Lcom/arc/seecode/engine/type/ITypeFactory;\",\"EngineInterface\",false);");
            out.println("    if (getTypeFactoryID == 0) {");
            out.println("        error(\">>>Can't find ITypeFactory\\n\");");
            out.println("        exit(3);");
            out.println("        }");
            out.println("    jobject jfactory = env->CallObjectMethod(this_,getJavaFactoryID);");
            out.println("    jobject jtypeFactory = env->CallObjectMethod(this_,getTypeFactoryID);");
        }
        out.println("    " + mCPPClassName + " *object = ::create" + mCPPClassName + "(env->NewGlobalRef(this_));");
        if (mNeedFactories) {
            out.println("    object->initInterface(new JavaFactoryWrapper(object,env->NewGlobalRef(jfactory)),");
            out.println("           new TypeFactoryWrapper(object,env->NewGlobalRef(jtypeFactory)));");
        }
        out.println("    env->SetIntField(this_," + mObjectField + ",(int)object);");
    }

    /**
     * @param classDoc
     * @param name
     * @return the method with the specified name within the class.
     */
    private static MethodDoc findMethod (ClassDoc classDoc, String name) {
        MethodDoc methods[] = classDoc.methods();
        for (int i = 0; i < methods.length; i++) {
            MethodDoc m = methods[i];
            if (m.name().equals(name))
                return m;
        }
        throw new IllegalArgumentException("Can't find " + name);
    }

    private static boolean isStringParm (Parameter p) {
        return p.type().qualifiedTypeName().equals("java.lang.String");
    }

    private static boolean isObjectParm (Parameter p) {
        return p.type().qualifiedTypeName().indexOf('.') > 0;
    }

    private static MethodDoc[] extractNativeMethods (ClassDoc c, boolean excludeInit) {
        List<MethodDoc> list = new ArrayList<MethodDoc>();
        MethodDoc[] methods = c.methods();
        for (int i = 0; i < methods.length; i++) {
            MethodDoc m = methods[i];
            if (m.isNative() && (!excludeInit || !m.name().equals(INFO.initInterfaceMethodName)))
                list.add(m);
        }
        return list.toArray(new MethodDoc[list.size()]);
    }

    private static FieldDoc[] extractInstanceFields (ClassDoc c) {
        List<FieldDoc> list = new ArrayList<FieldDoc>();
        FieldDoc[] fields = c.fields();
        for (int i = 0; i < fields.length; i++) {
            FieldDoc f = fields[i];
            if (!f.isStatic())
                list.add(f);
        }
        return list.toArray(new FieldDoc[list.size()]);
    }

    private static FieldDoc[] extractConstantFields (ClassDoc c) {
        List<FieldDoc> list = new ArrayList<FieldDoc>();
        extractConstants(c, list);
        ClassDoc[] interfaces = c.interfaces();
        for (int i = 0; i < interfaces.length; i++) {
            extractConstants(interfaces[i], list);
        }
        return list.toArray(new FieldDoc[list.size()]);
    }

    /**
     * @param c
     * @param list
     */
    private static void extractConstants (ClassDoc c, List<FieldDoc> list) {
        FieldDoc[] fields = c.fields();
        for (int i = 0; i < fields.length; i++) {
            FieldDoc f = fields[i];
            if (f.isStatic() &&
                f.isFinal() &&
                (f.type().typeName().equals("int") || f.type().typeName().indexOf("String") >= 0))
                list.add(f);
        }
    }

    private void printHeader (PrintWriter out, String identity) {
        out.println("/*");
        out.println(" * " + identity);
        out.println(" *");
        out.println(" * THIS FILE IS AUTO-GENERATED. DO NOT MODIFY!");
        out.println(" *");
        out.println(" */");
    }

    private void emitConstant (PrintWriter out, FieldDoc f) {
        out.println("    /**");
        writeCommentBlock(out, f.commentText(), 4);
        out.println("     */");

        if (f.type().typeName().equals("int")) {
            out.println("     static const int " + f.name() + " = " + f.constantValueExpression() + ';');
        }
        else {
            out.println("    #define " + f.name() + " " + f.constantValueExpression());
        }

    }

    void generateInterfaceHeaderFile (String file) throws IOException {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
        printHeader(out, "C++ EngineInterface Interface Declaration");

        out.println();
        out.println("#include \"jni.h\"     // jobject");
        out.println("#include <tools/veclist.h>  // Vector_list");

        emitExtraInterfaceDeclarations(out);

        out.println();
        out.println("/**");
        out.println(" * This class parrallels the Java class by the name " + mClassDoc.name() + ".");
        out.println(" * The \"glue\" code to convert between the Java and C++ domains");
        out.println(" * is automatically generated.");
        out.println(" *");
        out.println(" * Except for the java object pointer that is cached here,");
        out.println(" * The java-centric logic should be magically transparent.");
        out.println(" */");
        out.println("class " + mCPPClassName + " {");
        FieldDoc[] fields = extractConstantFields(mClassDoc);
        if (fields.length > 0) {
            out.println("    public:");
            for (int i = 0; i < fields.length; i++) {
                emitConstant(out, fields[i]);
            }
        }
        out.println("    private:");
        out.println("    jobject mJavaObject; // corresponding Java object");
        out.println("    JNIEnv *mEnv;   // callbacks need this");
        out.println();
        out.println("    protected:");
        if (mNeedFactories) {
            out.println("    JavaFactory *mFactory;");
            out.println("    ITypeFactory *mTypeFactory;");
        }
        out.println("    " + mCPPClassName + "(jobject javaObject){");
        out.println("        mJavaObject = javaObject;");

        out.println("        }");
        out.println();
        out.println("    public:");
        out.println("    virtual ~" + mCPPClassName + "() { ");
        if (mNeedFactories) {
            out.println("          delete mFactory;");
        }
        out.println("    }");
        if (mNeedFactories) {
            out.println("    void initInterface(JavaFactory *factory, ITypeFactory *typeFact){");
            out.println("        mFactory = factory;");
            out.println("        mTypeFactory = typeFact;");
            out.println("    }");
        }
        out.println();
        out.println("    jobject getJavaObject() const { return mJavaObject; }");
        out.println("    JNIEnv *getJNIEnv() const { return mEnv; }");
        out.println("    void setJNIEnv(JNIEnv *env) { mEnv = env; }");
        out.println();

        MethodDoc[] methods = extractNativeMethods(mClassDoc, true);
        for (int i = 0; i < methods.length; i++) {
            out.println();
            emitInterfaceMethod(out, methods[i]);
        }
        out.println("    }; // " + mClassDoc.name());

        out.println();
        out.println("// Create the interface by which Java communicates with the engine");
        out.println("extern " + mCPPClassName + " *create" + mCPPClassName + "(jobject javaObject);");

        writeExcludeEnsurePragmas(out, methods, true);

        out.close();

    }

    private static void writeExcludeEnsurePragmas (PrintWriter out, MethodDoc[] methods, boolean exclude) {
        Collection<String> vecTypes = extractVectorTypes(methods);
        out.println();
        for (String vecType : vecTypes) {
            if (Character.isUpperCase(vecType.charAt(0)) && vecType.endsWith("*")) {
                out.println("class " + vecType.substring(0, vecType.length() - 1) + ";");
            }
        }
        String which = exclude ? "exclude" : "ensure";
        for (String vecType : vecTypes) {
            out.println("#pragma " + which + "_instantiation(Vector_list<" + vecType + ">)");
            out.println("#pragma " + which + "_instantiation(Array<" + vecType + ">)");
        }
    }

    /**
     * Extract the names of all types that are to be represented by the C++ "Vector_list".
     * @param methods
     * @return a collection
     */
    private static Collection<String> extractVectorTypes (MethodDoc methods[]) {
        Set<String> set = new HashSet<String>();
        Set<String> seen = new HashSet<String>();
        addVectorTypes(set, seen, methods);
        return set;
    }

    private static void addVectorTypes (Set<String> vectorTypes, Set<String> seen, MethodDoc methods[]) {
        for (int i = 0; i < methods.length; i++) {
            MethodDoc m = methods[i];
            if (isMethodToBeAccessedFromC(m)) {
                checkForVectorType(vectorTypes, seen, m.returnType());
                Parameter parms[] = m.parameters();
                for (int j = 0; j < parms.length; j++) {
                    checkForVectorType(vectorTypes, seen, parms[j].type());
                }
            }
        }
    }

    private static boolean isRuntimeClass (ClassDoc c) {
        return c.qualifiedName().startsWith("java.") || c.qualifiedName().startsWith("com.sun");
    }

    private static void checkForVectorType (Set<String> vectorTypes, Set<String> seen, Type type) {
        if (isArrayType(type))
            vectorTypes.add(cppBaseName(type));

        if (seen.add(type.typeName())) {

            ClassDoc c = type.asClassDoc();
            if (c != null && !isRuntimeClass(c)) {
                MethodDoc methods[] = c.methods();
                addVectorTypes(vectorTypes, seen, methods);
            }
        }
    }

    private static String cppBaseName (Type type) {
        if (isStringArrayType(type))
            return "const char*";
        if (type.qualifiedTypeName().indexOf('.') > 0)
            return type.typeName() + "*";
        return "j" + type.typeName();
    }

    /**
     * @param out
     */
    private void emitExtraInterfaceDeclarations (PrintWriter out) {
        Collection<ClassDoc> classes = extractClassesToBeWrapped();

        for (ClassDoc c : classes) {
            out.println("class " + className(c) + ";");
        }
        out.println("class " + mCPPClassName + ";");
        out.println("typedef signed char byte;");
        for (ClassDoc c : classes) {
            emitInterfaceDeclaration(out, c);
        }
    }

    /**
     * @param c the class
     * @return the name by which we are to refer to a class from C++.
     */
    private String className (ClassDoc c) {
        // For historical reasons, the C++ name for the main
        // interface differs from Java.
        String name = c.name();
        if (name.equals(mJavaInterfaceNameOnly))
            return mCPPClassName;
        return name;
    }

    /**
     * @param out
     * @param c
     */
    private void emitInterfaceDeclaration (PrintWriter out, ClassDoc c) {
        out.println();
        out.println("/**");
        writeCommentBlock(out, c.commentText(), 0);
        out.println(" */");
        out.println("class " + c.name() + " {");
        out.println("    private:");
        out.println("        jobject mJavaObject;");
        out.println("    public:");
        out.println("        " + c.name() + "(jobject javaObject){");
        out.println("            mJavaObject = javaObject;");
        out.println("        }");
        out.println("        jobject getJavaObject() { return mJavaObject;}");
        FieldDoc constFields[] = extractConstantFields(c);
        for (int i = 0; i < constFields.length; i++) {
            emitConstant(out, constFields[i]);
        }
        MethodDoc methods[] = getMethodsToWrap(c);
        for (int i = 0; i < methods.length; i++) {
            if (isMethodToBeAccessedFromC(methods[i]))
                emitInterfaceMethod(out, methods[i]);
        }
        out.println("};");

    }

    /**
     * @param string
     */
    private static void writeCommentBlock (PrintWriter out, String string, int indentBy) {
        indent(out, indentBy);
        out.print(" * ");
        int slen = string.length();
        for (int i = 0; i < slen; i++) {
            out.print(string.charAt(i));
            if (string.charAt(i) == '\n') {
                indent(out, indentBy);
                out.print(" * ");
            }
        }
        out.println();
    }

    private static void indent (PrintWriter out, int indentBy) {
        for (int j = 0; j < indentBy; j++) {
            out.print(' ');
        }
    }

    /**
     * @param out
     * @param m the method being emitted.
     */
    private void emitInterfaceMethod (PrintWriter out, MethodDoc m) {
        writeMethodCommentBlock(out, m);
        Parameter parms[] = m.parameters();
        if (!m.isStatic()) {
            out.print("    virtual ");
        }
        else {
            out.print("    static ");
        }
        out.print(cppFullType(m.returnType()) + " " + m.name() + "(");
        if (parms.length > 0) {
            out.println();
            out.print("        ");
        }
        for (int i = 0; i < parms.length; i++) {
            Parameter p = parms[i];
            out.print(cppFullType(p.type()) + " " + p.name());
            if (i + 1 < parms.length) {
                out.println(",");
                out.print("        ");
            }
        }
        if (!m.isStatic())
            out.println(") = 0;");
        else
            out.println(");");
    }

    static MethodDoc findMatchingMethodInClass (ClassDoc parent, MethodDoc m) {
        MethodDoc methods[] = parent.methods();
        for (MethodDoc method : methods) {
            if (method.name().equals(m.name()) && method.flatSignature().equals(m.flatSignature())) {
                return method;
            }
        }
        return null;
    }

    private static String getCommentText (MethodDoc m) {
        String s = m.commentText();
        ClassDoc c = m.containingClass();
        ClassDoc parent = c.containingClass();
        if (s.length() == 0) {
            MethodDoc parentMethod = null;
            if (parent != null) {
                parentMethod = findMatchingMethodInClass(parent, m);
            }
            if (parentMethod == null) {
                for (ClassDoc iface : c.interfaces()) {
                    parentMethod = findMatchingMethodInClass(iface, m);
                    if (parentMethod != null && parentMethod.commentText().length() > 0)
                        break;
                }
            }
            if (parentMethod != null) {
                s = getCommentText(parentMethod);
            }
        }
        return s;
    }

    private static void writeMethodCommentBlock (PrintWriter out, MethodDoc m) {
        out.println("        /**");
        writeCommentBlock(out, getCommentText(m), 8);
        out.println("         *");
        ParamTag parmTags[] = m.paramTags();
        if (parmTags.length > 0) {
            out.println("         * Parameters:");
            out.println("         *");
        }

        for (int i = 0; i < parmTags.length; i++) {
            ParamTag p = parmTags[i];
            out.println("         *    " + p.parameterName() + "\t" + p.parameterComment());
        }
        if (isPrimitiveArrayType(m.returnType()) || isObjectArrayType(m.returnType())) {
            out.println("        *");
            out.println("        * NOTE: the result must be allocated on the heap;");
            out.println("        *       It will be deleted by the caller!");
        }
        else if (isStringType(m.returnType())) {
            out.println("        *");
            out.println("        * NOTE: the memory management of the resulting string");
            out.println("        *       must be handled by this method. The caller copies its");
            out.println("        *       conents, but does not delete it");
        }
        else if (isObjectType(m.returnType())) {
            out.println("        *");
            out.println("        * NOTE: the resulting object actually wraps a Java-based object.");
            out.println("        *      The factory interface passed to the constructor must be ");
            out.println("        *      used to allocate it. The caller will delete it after the ");
            out.println("        *      wrapped Java object is extracted.");
        }
        out.println("         */");
    }

    void generateInterfaceSkeleton (String file) throws IOException {
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
        printHeader(out, "C++ " + mCPPClassName + " Interface Skeletal implementation");
        out.println("#include \"" + mInterfaceHeaderFile + "\"");
        out.println("#include <stdio.h>   // fprintf");
        out.println();
        String myClassName = "My" + mCPPClassName;
        out.println("class " + myClassName + ": public " + mCPPClassName + " {");
        out.println("    public:");
        out.println("    " + myClassName + "(jobject javaObject, JavaFactory *f);");
        MethodDoc methods[] = extractNativeMethods(mClassDoc, true);
        for (int i = 0; i < methods.length; i++) {
            MethodDoc m = methods[i];
            out.print("    _Override ");
            out.print(cppFullType(m.returnType()) + " " + m.name());
            printMethodParms(out, m);
            out.println(";");
        }
        out.println("};");
        out.println();
        out.println(mCPPClassName + "*create" + mCPPClassName +"(jobject javaObject, JavaFactory *f, ITypeFactory *tf) {");
        out.println("    return new " + myClassName + "(javaObject,f,tf);");
        out.println("    }");

        out.println("static void notYetImplemented(const char *fcn){");
        out.println("    fprintf(stderr,\">>>Called unimplemented function %s\\n\",fcn);");
        out.println("    }");
        out.println();
        out.println(myClassName + "::" + myClassName + "(jobject javaObject, JavaFactory *f):");
        out.println("    " + mCPPClassName + "(javaObject,f) {");
        out.println("    }");

        for (int i = 0; i < methods.length; i++) {
            out.println();
            MethodDoc m = methods[i];
            writeMethodCommentBlock(out, m);
            out.print(cppFullType(m.returnType()) + " " + myClassName + "::" + m.name());
            printMethodParms(out, m);
            out.println("{");
            out.println("    notYetImplemented(\"" + m.name() + "\");");
            if (!isVoidType(m.returnType())) {
                out.println("    return 0;");
            }
            out.println("    }");
        }

        out.close();
    }

    private void printMethodParms (PrintWriter out, MethodDoc m) {
        out.print("(");
        Parameter[] parms = m.parameters();
        for (int j = 0; j < parms.length; j++) {
            if (j > 0)
                out.print(",");
            out.print(cppFullType(parms[j].type()) + " " + parms[j].name());
        }
        out.print(")");
    }

}
