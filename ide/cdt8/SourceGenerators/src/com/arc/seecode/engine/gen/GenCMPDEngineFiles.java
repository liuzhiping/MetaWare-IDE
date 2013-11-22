/*
 * GenCMPDEngineFiles
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

import com.arc.seecode.engine.internal.gen.GenJNIFiles;


/**
 * Generate C++ JNI code for accessing CMPD interface of the engine.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class GenCMPDEngineFiles {

    private static final String INIT_INTERFACE = "initInterface";
    private static final String ROOT_PACKAGE = "com.arc.seecode";
    private static final String PACKAGE = "com.arc.seecode.internal.cmpd.";
    //private static final String INIT_IDS_METHOD_NAME = "initMethodAndFieldIDs";
    private static final String OBJECT_FIELD = "id_CPP_HANDLE";
    static final String ENGINE_INTERFACE_NAME = "CMPDController";
    static final String ENGINE_CLASS =
        PACKAGE + ENGINE_INTERFACE_NAME;
    static final String IMPLEMENTATION_FILE =
        ENGINE_CLASS.replace('.', '_') + ".cc";
    static final String CPP_CLASS = "CMPDController"; 
    static final String INTERFACE_HEADER_FILE = "CMPDController.h";
    static final String INTERFACE_SKELETON =
        "CMPDController.skeleton";

   
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
