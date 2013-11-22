/*
 * JNIInfo
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

/**
 * Information needed to generate JNI files.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class JNIInfo {
    /**
     * Path to directory where generate files will be emitted.
     * If <code>null</code>, defaults to "."
     */
    public String sourceDirectory;
    
    /**
     * Full nanme of root package (e.g., "com.arc.seecode.engine").
     * Must not be <code>null</code>.
     */
    public String rootPackage;
    
    /**
     * Implementation file (e.g., "com_arc_seecode_engine_EngineAPI.cc").
     * Must not be <code>null</code>.
     */
    public String implementationFile;
    
    /**
     * The fully-qualified name of the class for which JNI functions are to be emitted.
     */
    public String className;
    
    /**
     * Name of parallel C++ class. For example "EngineInterface".
     */
    public String cppClassName;
    
    /**
     * The name of the Java class that corresponds to the C++ class. For example, "IEngineAPI".
     */
    public String javaInterfaceName;
    /**
     * Prefix of fully-qualified classes that we are interested in generated JNI information for.
     * For example, "com.arc".
     */
    public String classPrefix;
    
    /**
     * Name of method that initializes the interface. (E.g., "initInterface").
     */
    public String initInterfaceMethodName;
    
    /**
     * Interface header file; e.g., "EngineInterface.h"
     */
    public String interfaceHeaderFile;
    
    /**
     * Name of field that referneces the C++ object; e.g. "id_CPP_HANDLE".
     */
    public String objectField;
    /**
     * Name of file for a skeleton implementation of C++ (E.g. "EngineInterface.skeleton"
     */
    public String skeletonFile;
   
}
