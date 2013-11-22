/*
 * Created on Jul 18, 2003
 *
 */
package com.arc.seecode.engine.gen;

import com.arc.seecode.engine.internal.gen.GenJNIFiles;
import com.sun.javadoc.Doclet;

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
public class GenEngineFiles extends Doclet {
    private static final String INIT_INTERFACE = "initInterface";
    private static final String ROOT_PACKAGE = "com.arc.seecode.engine";
    private static final String PACKAGE = "com.arc.seecode.engine.internal.";
    //private static final String INIT_IDS_METHOD_NAME = "initMethodAndFieldIDs";
    private static final String OBJECT_FIELD = "id_CPP_HANDLE";
    static final String ENGINE_INTERFACE_NAME = "EngineAPI";
    static final String ENGINE_CLASS =
        PACKAGE + ENGINE_INTERFACE_NAME;
    static final String IMPLEMENTATION_FILE =
        ENGINE_CLASS.replace('.', '_') + ".cc";
    static final String CPP_CLASS = "EngineInterface"; 
    static final String INTERFACE_HEADER_FILE = "EngineInterface.h";
    static final String INTERFACE_SKELETON =
        ENGINE_INTERFACE_NAME + ".skeleton";
    static final String INTERFACE_THREAD = "EngineInterfaceThread";

   
    public static void main(String[] args) {
        JNIInfo info = new JNIInfo();
        info.className = ENGINE_CLASS;
        info.classPrefix = "com.arc.";
        info.cppClassName = CPP_CLASS;
        info.implementationFile = IMPLEMENTATION_FILE;
        info.initInterfaceMethodName = INIT_INTERFACE;
        info.interfaceHeaderFile = INTERFACE_HEADER_FILE;
        info.javaInterfaceName = ENGINE_CLASS;
        info.objectField = OBJECT_FIELD;
        info.rootPackage = ROOT_PACKAGE;
        info.skeletonFile = INTERFACE_SKELETON;
        String sourcePath = ".";
        if (args.length > 0) {
            sourcePath = args[0];
        }
        info.sourceDirectory = sourcePath;
        GenJNIFiles.generate(info);
    }
}
