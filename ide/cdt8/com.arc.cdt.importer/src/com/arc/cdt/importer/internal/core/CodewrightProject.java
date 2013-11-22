/*******************************************************************************
 * Copyright (c) 2005-2012 Synopsys, Incorporated
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Synopsys, Inc - Initial implementation 
 *******************************************************************************/
package com.arc.cdt.importer.internal.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.arc.cdt.importer.core.ICodewrightProject;
import com.arc.cdt.importer.core.ICodewrightProjectSpace;
import com.arc.cdt.importer.core.PSPException;
import com.arc.mw.util.StringUtil;

/**
 * A Codewright project description.
 * 
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
class CodewrightProject implements ICodewrightProject {
    private ICodewrightProjectSpace mProjectSpace;

    private File mPjtFile;

    private File mElxFile;

    private Set<File> mSourceFiles = new HashSet<File>();

    private boolean mCPlusPlus = false;

    private File mPjtFileDir;

    private ElxFileReader mElxReader;

    CodewrightProject(ICodewrightProjectSpace psp, File projectFile,
            IProgressMonitor monitor) throws IOException, PSPException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        monitor.beginTask("Extracting project from project space", 5);
        mPjtFileDir = projectFile.getParentFile();
        mProjectSpace = psp;
        mPjtFile = projectFile;

        monitor.subTask("Getting .elx file");
        mElxFile = computeElxFileFrom(projectFile);
        monitor.worked(1);
        monitor.subTask("Reading .elx file");
        mElxReader = new ElxFileReader(mElxFile);
        monitor.worked(1);

        monitor.subTask("Reading explicit source files");
        readSourceFilesFromProjectFile(mPjtFile);
        monitor.worked(1);

        File root = computeRoot();

        monitor.subTask("Reading implicit assembly include files");
        readAssemblyIncludeFiles(mSourceFiles, new SubProgressMonitor(monitor,
                1));

        monitor.subTask("Reading implicit C include files");
        readExtraCIncludeFiles(mSourceFiles, root, new SubProgressMonitor(
                monitor, 1));

    }

    /**
     * Compute the root directory of the source files.
     * 
     * @return the root directory of the source files.
     */
    private File computeRoot() {
        File d = null;
        for (File src : mSourceFiles) {
            if (d == null) {
                d = src.getParentFile();
            } else {
                d = commonPortion(d, src.getParentFile());
            }
        }
        return d;
    }

    private static File commonPortion(File d1, File d2) {
        IPath path1 = new Path(d1.toString());
        IPath path2 = new Path(d2.toString());
        if (path1.getDevice() != null
                && !path1.getDevice().equals(path2.getDevice()))
            return null;
        if (path1.getDevice() == null && path2.getDevice() != null)
            return null;
        int cnt = path1.matchingFirstSegments(path2);
        if (cnt == 0)
            return null;
        return new File(path1.uptoSegment(cnt).toOSString());

    }

    /**
     * Given a project file (.pjt), return the corresponding ".elx" file.
     * 
     * @param f
     *            the project file (.pjt).
     * @return the corresponding ".elx" file.
     */
    static File computeElxFileFrom(File f) {
        String fn = f.getPath();
        int i = fn.lastIndexOf('.');
        if (i > 0) {
            fn = fn.substring(0, i) + ".elx";
        } else
            fn += ".elx";
        return new File(fn);
    }

    public ICodewrightProjectSpace getProjectSpace() {
        return mProjectSpace;
    }

    public File[] getSourceFiles() {
        return mSourceFiles.toArray(new File[mSourceFiles.size()]);
    }

    public String getName() {
        String name = mElxReader.getAttribute(TARGET_SECTION,
                TARGET_PROJECT_NAME);
        if (name == null || name.trim().length() == 0) {
            name = mPjtFile.getName();
            int index = name.lastIndexOf('.');
            if (index > 0) {
                name = name.substring(0, index);
            }
        }
        return name;
    }

    public File getPjtFile() {
        return mPjtFile;
    }

    public File getElxFile() {
        return mElxFile;
    }

    public String getTarget() {
        String v = mElxReader.getAttribute(TARGET_SECTION,
                TARGET_PROCESSOR_FAMILY);
        if (v == null)
            v = "??";
        return v.toLowerCase();
    }

    private final int[] COMP_OPTS = { COMPILER_OPT_ADDITIONAL,
            COMPILER_OPT_OPTIMIZE, COMPILER_OPT_COPYRIGHT,
            COMPILER_OPT_TOGGLES, COMPILER_OPT_PRAGMAS, COMPILER_OPT_INLINE,
            COMPILER_OPT_PROFILING,
            TARGET_PROCESSOR_OPTION};

    public String[] getCompilerOptions() {
        StringBuffer buf = new StringBuffer();
        for (int o : COMP_OPTS) {
            String opts = mElxReader.getAttribute(COMPILER_SECTION, o);
            if (opts != null && opts.length() > 0) {
                if (buf.length() > 0)
                    buf.append(' ');
                buf.append(opts);
            }
        }
        appendEndianOption(buf);
        inferARCompactProcessor(buf);
        return buf.toString().split("\\s");
    }

    /**
     * Given a string of compiler or linker options, and we're targeting
     * to an ARC 600 or ARC 700, make sure the appropriate target
     * processor is specified (e.g., "-a6" or "-a7").
     * @param buf argument list that is being constructed and is to be
     * appended to.
     */
    private void inferARCompactProcessor (StringBuffer buf) {
        // For ARCompact, the processor version is implicit with the
        // target name (e.g., "ARC 600") We must manufacture the appropriate
        // compiler option if it isn't already specified.
        String target = getTarget();
        if (target.toLowerCase().indexOf("arc") >= 0){
            if (target.indexOf("600") >= 0){
                if (buf.toString().indexOf("-a6") < 0 &&
                    buf.toString().indexOf("-arc600") < 0)
                {
                    buf.append(" -arc600");
                }
            }
            else if (target.indexOf("700") >= 0){
                if (buf.toString().indexOf("-a7") < 0 &&
                        buf.toString().indexOf("-arc700") < 0)
                    {
                        buf.append(" -arc700");
                    }             
            }
        }
    }

    /**
     * @todo davidp needs to add a method comment.
     * @param buf
     */
    private void appendEndianOption(StringBuffer buf) {
        String endian = mElxReader.getAttribute(TARGET_SECTION, TARGET_ENDIAN);
        boolean littleDefault = isLittleEndianByDefault();
        if (endian != null
                && (endian.equalsIgnoreCase("b") && littleDefault || endian
                        .equalsIgnoreCase("l")
                        && !littleDefault))
            buf.append(" -H" + endian.toUpperCase());
    }

    private boolean isLittleEndianByDefault() {
        String target = getTarget();
        if (target.indexOf("arc") >= 0)
            return true;
        if (target.indexOf("arm") >= 0)
            return true;
        if (target.indexOf("vc") >= 0)
            return true;
        return false;
    }

    public String[] getAssemblerOptions() {
        StringBuffer buf = new StringBuffer();
        String opts = mElxReader.getAttribute(ASSEMBLER_SECTION,
                ASSEMBLER_OPT_ADDITIONAL);
        if (opts != null) {
            buf.append(opts);
        }
        if (isFlagSet(ASSEMBLER_SECTION, ASM_GEN_DEBUGINFO))
            buf.append(" -Hasopt=-g");
        if (isFlagSet(ASSEMBLER_SECTION, ASM_GEN_NOCOPYR))
            buf.append(" -Hnocopyr");
        if (isFlagSet(ASSEMBLER_SECTION, ASM_GEN_PRIVATELABEL))
            buf.append(" -Hasopt=-L");
        if (isFlagSet(ASSEMBLER_SECTION, ASM_ERR_NOWARNING))
            buf.append(" -Hasopt=-w");
        appendEndianOption(buf);
        String core = mElxReader.getAttribute(ASSEMBLER_SECTION,
                ASM_PROC_ARC_COREVER);
        if (core != null) {
            buf.append(' ');
            if (!core.startsWith("-"))
                core = "-" + core;
            buf.append(core);
        }
        else
            inferARCompactProcessor(buf);
        return buf.toString().split("\\s");
    }

    private boolean isFlagSet(int sectionID, int attrID) {
        String v = mElxReader.getAttribute(sectionID, attrID);
        if (v != null) {
            return v.equalsIgnoreCase("true");
        }
        return false;
    }

    public String[] getLinkerOptions() {
        StringBuffer buf = new StringBuffer();
        String opts = mElxReader
                .getAttribute(LINK_SECTION, LINK_OPT_ADDITIONAL);
        if (opts != null) {
            buf.append(opts);
        }
        // if ARC 600 or ARC 700, make sure "-a6" or "-a7" is being passed.
        inferARCompactProcessor(buf);
        // if (isFlagSet(LINK_SECTION, LINK_USE_SVR3_CMDFILES )){
        // String s = mElxReader.getAttribute(LINK_SECTION,LINK_SVR3_CMDFILES);
        // if (s != null && s.length() > 0){
        // String cmdFiles[] = s.split(";");
        // buf.append(StringUtil.arrayToArgString(cmdFiles));
        // }
        // }
        return buf.toString().split("\\s");
    }

    public File[] getSvr3CommandFiles() {
        if (isFlagSet(LINK_SECTION, LINK_USE_SVR3_CMDFILES)) {
            String s = mElxReader
                    .getAttribute(LINK_SECTION, LINK_SVR3_CMDFILES);
            if (s != null && s.length() > 0) {
                String names[] = s.split(";");
                return makeFileList(names);
            }
        }
        return EMPTY_FILE_ARRAY;
    }

    public File[] getSvr4CommandFiles() {
        if (isFlagSet(LINK_SECTION, LINK_USE_SVR4_CMDFILES)) {
            String s = mElxReader
                    .getAttribute(LINK_SECTION, LINK_SVR4_CMDFILES);
            if (s != null && s.length() > 0) {
                return makeFileList(s.split(";"));
            }
        }
        return EMPTY_FILE_ARRAY;
    }

    public File[] getObjectFilesAndLibraries() {
        List<String> list = new ArrayList<String>();
        String opts = mElxReader.getAttribute(LINK_SECTION, LINK_OBJ_FILES);
        if (opts != null && opts.length() > 0) {
            list.addAll(Arrays.asList(opts.split(";")));
        }
        opts = mElxReader.getAttribute(LINK_SECTION, LINK_LIBRARIES);
        if (opts != null && opts.length() > 0) {
            list.addAll(Arrays.asList(opts.split(";")));
        }
        return makeFileList(list);
    }

    private File[] makeFileList(List<String> list) {
        File files[] = new File[list.size()];
        int i = 0;
        for (String fn : list) {
            files[i++] = formSourceFile(fn);
        }
        return files;
    }

    private File[] makeFileList(String[] names) {
        return makeFileList(Arrays.asList(names));
    }

    /**
     * Maps Guihili property names to their corresponding location in the Debug
     * section of old MetaDeveloper project.
     */
    private Object sKeyValue[] = { /**/
            "Program_arguments", DEBUG_TARGET_ARGUMENTS,/**/
            "ARC_parallel_port_address", DEBUG_CONN_PORT, /**/
            "ARC_Timeout", DEBUG_CONN_TIMEOUT,/**/
            "ARC_blast_filename", BLAST_XBF_FILE,/**/
            "ARC_blast_spec", DEBUG_FPGA_BLAST, /**/
            "ARC_fujitsu_if", DEBUG_CONN_FUJITSU, /**/
            "parallel_jtag", PARALLEL_PORT_IN_JTAG_MODE,/**/
    };

    public Map<String, String> getSeeCodeOptions() {
        // NOTE the map keys are the "property_name" attributes from Guihili.
        // Make sure
        // they state in sync.
        HashMap<String, String> map = new HashMap<String, String>();

        for (int i = 0; i < sKeyValue.length; i += 2) {
            String value = mElxReader.getAttribute(DEBUG_SECTION,
                    ((Integer) sKeyValue[i + 1]).intValue());
            if (value != null && value.length() > 0) {
                String property_name = (String) sKeyValue[i];
                map.put(property_name, value);
            }
        }
        // 
        // Now compute target: simulator, CAS, or hardware
        //
        String target = mElxReader.getAttribute(DEBUG_SECTION, DEBUG_MODE);
        if (target != null) {
            target = target.toLowerCase();
            String mode = null;
            if (target.startsWith("sim")) {
                mode = "ARC_simulator";
            } else if (target.startsWith("cas") || target.startsWith("cycle")) {
                mode = "ARC_CAS";
            } else
                mode = "ARC_hardware";
            map.put("ARC_target", mode);
        }
        //
        // Now derive things from "additional options":
        // -Xtimer0, -Xtimer1, -simext=xxx, -mem=
        //
        String extra = mElxReader.getAttribute(DEBUG_SECTION,
                ADDITIONAL_OPTIONS);
        if (extra != null) {
            String args[] = extra.split("\\s");
            List<String> remaining = new ArrayList<String>();
            int simextpCnt = 0;
            int memextCnt = 0;
            int semIntCnt = 0;
            for (String arg : args) {
                if (arg.startsWith("-mem=")) {
                    map.put("ARC_memsize", arg.substring(5));
                } else if (arg.startsWith("-simextp=") && simextpCnt < 4) {
                    map.put("ARC_ExtDLL" + (++simextpCnt), arg.substring(9));
                } else if (arg.startsWith("-simext=") && simextpCnt < 4){
                    String libs[] = arg.substring(8).split(",");
                    for (String lib: libs){
                        if (simextpCnt < 4)
                            map.put("ARC_ExtDLL" + (++simextpCnt), lib);
                        else remaining.add("-simextp=" + lib);
                    }
                    
                } else if (arg.startsWith("-memext=")) {
                    String mem[] = arg.substring(8).split(",");
                    // -memext=lo,hi
                    if (mem.length == 2) {
                        map.put("ARC_ExtAdrLo" + (++memextCnt), mem[0]);
                        map.put("ARC_ExtAdrHi" + (++memextCnt), mem[1]);
                    } else
                        remaining.add(arg); // can't figure it out!
                } else if (arg.startsWith("-semint=") && semIntCnt < 4) {
                    map.put("SIDLL" + (++semIntCnt), arg.substring(8));

                } else
                    remaining.add(arg);
            }
            if (remaining.size() > 0) {
                StringBuilder buf = new StringBuilder();
                for (String s : remaining) {
                    if (buf.length() > 0)
                        buf.append(' ');
                    buf.append(s);
                }
                map.put("cmd_line_option",buf.toString());
            }
        }

        return map;
    }

    public boolean isCPlusPlus() {
        return mCPlusPlus;
    }

    public String getOutputFile() {
        return mElxReader.getAttribute(LINK_SECTION, LINK_OUTPUT_FILE);
    }

    private static String EMPTY_ARRAY[] = new String[0];

    private static File EMPTY_FILE_ARRAY[] = new File[0];

    public File[] getCompilerIncludes() {
        String v = mElxReader.getAttribute(COMPILER_SECTION,
                COMPILER_OPT_INCLUDE);
        if (v == null || v.length() == 0)
            return EMPTY_FILE_ARRAY;
        String[] paths = StringUtil.pathToArray(v, ";");
        File files[] = new File[paths.length];
        for (int i = 0; i < paths.length; i++) {
            files[i] = formSourceFile(paths[i]);
        }
        return files;
    }

    /**
     * Return the include directories that are within a root directory.
     * 
     * @param dir
     *            the root directory.
     * @return the array of include directories within the root directory.
     */
    private File[] getCompilerIncludesWithin(File dir) {
        if (dir == null)
            return new File[0];
        List<File> files = new ArrayList<File>();
        for (File f : getCompilerIncludes()) {
            if (isWithinDir(dir, f)) {
                files.add(f);
            }
        }
        return files.toArray(new File[files.size()]);
    }

    private static boolean isWithinDir(File dir, File f) {
        if (dir.equals(f))
            return true;
        if (f.getParentFile() != null) {
            return isWithinDir(dir, f.getParentFile());
        }
        return false;
    }

    public String[] getCompilerDefines() {
        String v = mElxReader.getAttribute(COMPILER_SECTION,
                COMPILER_OPT_DEFINE);
        if (v == null)
            return EMPTY_ARRAY;
        String[] defs = StringUtil.pathToArray(v, ",");
        return defs;
    }

    public String[] getCompilerUndefines() {
        String v = mElxReader.getAttribute(COMPILER_SECTION,
                COMPILER_OPT_UNDEFINE);
        if (v == null)
            return EMPTY_ARRAY;
        String[] defs = StringUtil.pathToArray(v, ",");
        return defs;
    }

    public File[] getAssemblerIncludes() {
        String v = mElxReader.getAttribute(ASSEMBLER_SECTION,
                ASM_PP_INCLUDEDIRS);
        List<File> files = new ArrayList<File>();
        if (v != null && v.length() > 0) {
            String[] paths = StringUtil.pathToArray(v, ";");

            for (int i = 0; i < paths.length; i++) {
                files.add(formSourceFile(paths[i]));
            }
        }
        // For some strange reason, MQX has included .s files in the
        // assembler's output directory!
        String output = mElxReader.getAttribute(ASSEMBLER_SECTION,
                OUTPUT_DIRECTORY);
        if (output != null && output.length() > 0) {
            files.add(formSourceFile(output));
        }
        return files.toArray(new File[files.size()]);
    }

    public String[] getAssemblerDefines() {
        String v = mElxReader.getAttribute(ASSEMBLER_SECTION, ASM_PP_DEFINES);
        if (v == null)
            return EMPTY_ARRAY;
        String[] defs = StringUtil.pathToArray(v, ",");
        return defs;
    }

    public String[] getAssemblerUndefines() {
        String v = mElxReader.getAttribute(ASSEMBLER_SECTION, ASM_PP_UNDEFINES);
        if (v == null)
            return EMPTY_ARRAY;
        String[] defs = StringUtil.pathToArray(v, ",");
        return defs;
    }

    /**
     * Read the source file list from the .pjt file. We permit a project to have
     * no source files, though we're not sure what that means.
     * 
     * @param f
     *            the .pjt file.
     * @throws IOException
     *             if an I/O error occurs.
     */
    private void readSourceFilesFromProjectFile(File f) throws IOException {
        FileReader reader = new FileReader(f);
        BufferedReader input = new BufferedReader(reader);
        try {
            String line = input.readLine();
            while (line != null) {
                if (line.equals("[Files]")) {
                    line = input.readLine();
                    if (line != null)
                        line = line.trim();
                    while (line != null && line.length() > 0
                            && line.charAt(0) != '[') {
                        if (line.toLowerCase().endsWith(".cc")
                                || line.toLowerCase().endsWith(".cpp")) {
                            mCPlusPlus = true;
                        }
                        mSourceFiles.add(formSourceFile(line));
                        line = input.readLine();
                        if (line != null)
                            line = line.trim();
                    }
                } else
                    line = input.readLine();
            }
        } finally {
            input.close();
        }
    }

    /**
     * Given a set of source files, looks for C/C++ or .h files and scan them
     * for other include files that we may be missing.
     * 
     * @param files
     *            the set of files of which a subset of .c, .cc, or .h files.
     * @param rootSrc
     *            the root of the sources so that we don't read includes in
     *            other libraries.
     */
    private void readExtraCIncludeFiles(Collection<File> files, File rootSrc,
            IProgressMonitor monitor) {
        Set<File> cFiles = new HashSet<File>();
        for (File f : files) {
            if (!f.getName().endsWith(".s")) {
                cFiles.add(f);
            }
        }
        if (cFiles.size() > 0) {
            scanCFilesForIncludes(cFiles, rootSrc, monitor);
        }
    }

    /**
     * GIven a collection of C and .h files, scan them for any include files
     * that are not explicitly referenced from the project, and add them.
     * 
     * @param files
     *            collection of C and .h files.
     * @param monitor
     */
    private void scanCFilesForIncludes(Collection<File> files, File rootSrc,
            IProgressMonitor monitor) {
        Set<File> newFilesToScan = new HashSet<File>();
        monitor.beginTask("Scanning for implicit include files",
                files.size() + 1);
        try {
            for (File f : files) {
                monitor.subTask(f.getPath());
                Collection<File> includes = extractCIncludeFilesFrom(f, rootSrc);
                monitor.worked(1);
                for (File include : includes) {
                    if (mSourceFiles.add(include)) {
                        newFilesToScan.add(include);
                    }
                }
            }
            if (newFilesToScan.size() > 0) {
                scanCFilesForIncludes(newFilesToScan, rootSrc,
                        new SubProgressMonitor(monitor, 1));
            }
        } finally {
            monitor.done();
        }
    }

    /**
     * Given a set of source files, looks for assembly files, and scan them for
     * "include" directives. Append the include files to the source file list.
     * 
     * @param files
     *            the set of files of which a subset may be assembly files.
     */
    private void readAssemblyIncludeFiles(Collection<File> files,
            IProgressMonitor monitor) {
        Set<File> assemblyFiles = new HashSet<File>();
        for (File f : files) {
            if (f.getName().endsWith(".s")) {
                assemblyFiles.add(f);
            }
        }
        if (assemblyFiles.size() > 0) {
            scanAssemblyFilesForIncludes(assemblyFiles, monitor);
        }
    }

    private void scanAssemblyFilesForIncludes(Collection<File> files,
            IProgressMonitor monitor) {
        Set<File> newFilesToScan = new HashSet<File>();
        monitor.beginTask("Scanning assembly files for new includes", files
                .size() + 1);
        try {
            for (File f : files) {
                monitor.subTask(f.toString());
                Collection<File> includes = extractAsmIncludeFilesFrom(f);
                monitor.worked(1);
                for (File include : includes) {
                    if (mSourceFiles.add(include)) {
                        newFilesToScan.add(include);
                    }
                }
            }
            if (newFilesToScan.size() > 0) {
                scanAssemblyFilesForIncludes(newFilesToScan,
                        new SubProgressMonitor(monitor, 1));
            }
        } finally {
            monitor.done();
        }
    }

    /**
     * Read the given assembly file and look for "include foo.met" and stuff
     * like that. Return all the "foo.met" instances that exist.
     * 
     * @param f
     *            the assembly file or an assembly include file that may have
     *            more includes.
     * @return collection of includes.
     */
    private Collection<File> extractAsmIncludeFilesFrom(File f) {
        BufferedReader input = null;
        Set<File> set = new HashSet<File>();
        try {
            FileReader reader = new FileReader(f);
            input = new BufferedReader(reader);
            String line = input.readLine();
            while (line != null) {
                line = line.trim();
                if (line.startsWith("include") || line.startsWith(".include")) {
                    line = line.substring(line.charAt(0) == '.' ? 8 : 7).trim();
                    String fileName = null;
                    if (line.startsWith("\"")) {
                        // .include "foo.bar"
                        int i = line.indexOf('"', 1);
                        if (i >= 0) {
                            fileName = line.substring(1, i);
                        }
                    } else {
                        int i = line.indexOf('#');
                        if (i < 0)
                            i = line.indexOf(";");
                        if (i < 0)
                            i = line.indexOf(" //");
                        if (i < 0)
                            i = line.indexOf("\t//");
                        if (i >= 0) {
                            fileName = line.substring(0, i);
                        } else
                            fileName = line;
                    }
                    if (fileName != null) {
                        File include = formSourceFile(f.getParentFile(),
                                fileName);
                        if (include.exists()) {
                            set.add(include);
                        }
                    }
                }
                line = input.readLine();
            }
        } catch (IOException x) {
            // Don't bother with files we can't read.
            // Presumably, the problem will be diagnosed as we
            // attempt to copy them.
        } finally {
            if (input != null)
                try {
                    input.close();
                } catch (IOException e) {
                }
        }
        return set;
    }

    /**
     * Read the given C or .h file and look for [#include "foo.h"]. Return all
     * the "foo.h" instances that exist.
     * 
     * @param f
     *            the C or .h file
     * @param rootSrc
     *            the root of the sources or <code>null</code> if we're not
     *            able to compute it.
     * @return collection of includes.
     */
    private Collection<File> extractCIncludeFilesFrom(File f, File rootSrc) {
        BufferedReader input = null;
        Set<File> set = new HashSet<File>();
        File[] includeDirs = getCompilerIncludesWithin(rootSrc);
        try {
            FileReader reader = new FileReader(f);
            input = new BufferedReader(reader);
            String line = input.readLine();
            while (line != null) {
                line = line.trim();
                if (line.startsWith("#")) {
                    line = line.substring(1).trim();
                }
                if (line.startsWith("include")) {
                    line = line.substring(7).trim();
                    String fileName = null;
                    if (line.startsWith("\"")) {
                        // .include "foo.bar"
                        int i = line.indexOf('"', 1);
                        if (i >= 0) {
                            fileName = line.substring(1, i);
                        }
                    }

                    if (fileName != null) {
                        File include = formSourceFile(f.getParentFile(),
                                fileName);
                        if (include.exists()) {
                            set.add(include);
                        }
                        if (new File(fileName).getName().equals(fileName)) {
                            for (File dir : includeDirs) {
                                File df = new File(dir, fileName);
                                if (df.exists()) {
                                    set.add(formSourceFile(dir, fileName));
                                    break;
                                }
                            }
                        }
                    }
                }
                line = input.readLine();
            }
        } catch (IOException x) {
            // Don't bother with files we can't read.
            // Presumably, the problem will be diagnosed as we
            // attempt to copy them.
        } finally {
            if (input != null)
                try {
                    input.close();
                } catch (IOException e) {
                }
        }
        return set;
    }

    /**
     * Given a file specified relative to the .pjt directory, compute the File
     * object that references it.
     * 
     * @param fn
     *            the file name, possibly relative.
     * @return the corresponding File object.
     */
    private File formSourceFile(String fn) {
        return formSourceFile(mPjtFileDir, fn);
    }

    /**
     * Given a file specified relative to a directory directory, compute the
     * File object that references it.
     * 
     * @param dir
     *            the directory that it is relative to.
     * @param fn
     *            the file name, possibly relative.
     * @return the corresponding File object.
     */
    private File formSourceFile(File dir, String fn) {
        File f = new File(fn);
        if (f.isAbsolute())
            return f;
        f = new File(dir, fn);
        try {
            return f.getCanonicalFile();
        } catch (IOException e) {
            return f; // shouldn't get here
        }
    }

    // Target Section
    private static final int TARGET_SECTION = 0x0001;

    // private static final int TARGET_VERSION = 0x01;
    // private static final int TARGET_DESIGNER = 0x02;
    private static final int TARGET_PROJECT_NAME = 0x03;

    // private static final int TARGET_PROJECT_ROOT_DIR = 0x04;
    private static final int TARGET_PROCESSOR_FAMILY = 0x05;

    private static final int TARGET_ENDIAN = 0x06;

    // MQX Section
    // private static final int MQX_SECTION = 0x0002;
    // private static final int MQX_VERSION = 0x01;
    // private static final int MQX_ROOT_DIR = 0x02;
    // private static final int MQX_RTCS_ROOT_DIR = 0x03;
    // private static final int MQX_PSP_NAME = 0x04;
    // private static final int MQX_BSP_NAME = 0x05;
    //
    //
    // // MQX Builder Section
    // private static final int BUILDER_SECTION = 0x0003;
    // private static final int BUILDER_VERSION = 0x01;
    // private static final int BUILDER_EXE = 0x02;
    // private static final int BUILDER_PROJ_FILE = 0x03;
    // private static final int BUILDER_DATA_FILE = 0x04;
    // private static final int BUILDER_CMD_LINE = 0x05;
    // private static final int BUILDER_CMD_ARGS = 0x06;
    //
    //
    // // MQX Design Tool Section
    // private static final int DESIGNTOOL_SECTION = 0x0004;
    // private static final int DESIGN_VERSION = 0x01;
    // private static final int DESIGN_EXE = 0x02;
    // private static final int DESIGN_PROJ_FILE = 0x03;
    // private static final int DESIGN_CMD_LINE = 0x04;
    // private static final int DESIGN_CMD_ARGS = 0x05;
    //
    //
    // // EDS Client section
    // private static final int EDSCLIENT_SECTION = 0x0005;
    // private static final int EDSCLIENT_VERSION = 0x01;
    // private static final int EDSCLIENT_EXE = 0x02;
    //
    //
    // // Performance Tool Section
    // private static final int PERFORMANCE_SECTION = 0x0006;
    // private static final int PERFORMANCE_VERSION = 0x01;
    // private static final int PERFORMANCE_EXE = 0x02;
    // private static final int PERFORMANCE_PROJ_FILE = 0x03;
    // private static final int PERFORMANCE_DATA_FILE = 0x04;
    // private static final int PERFORMANCE_CMD_LINE = 0x05;
    // private static final int PERFORMANCE_CMD_ARGS = 0x06;

    // Compiling tools section
    private static final int COMPILER_SECTION = 0x0007;

    // private static final int COMPILER_SYMBOL = 0x01;
    // private static final int COMPILER_ROOT = 0x02;
    // private static final int COMPILER_C_EXTENSION = 0x03;
    // private static final int COMPILER_OBJ_EXTENSION = 0x04;
    // private static final int COMPILER_CPP_EXTENSION = 0x05;
    // private static final int COMPILER_CPP_OBJ_EXTENSION = 0x06;
    private static final int COMPILER_OPT_OPTIMIZE = 0x07;

    private static final int COMPILER_OPT_COPYRIGHT = 0x08;

    private static final int COMPILER_OPT_INLINE = 0x09;

    private static final int COMPILER_OPT_PROFILING = 0x0A;

    private static final int COMPILER_OPT_DEFINE = 0x0B;

    private static final int COMPILER_OPT_UNDEFINE = 0x0C;

    private static final int COMPILER_OPT_INCLUDE = 0x0D;

    private static final int COMPILER_OPT_PRAGMAS = 0x0E;

    private static final int COMPILER_OPT_TOGGLES = 0x0F;

    private static final int COMPILER_OPT_ADDITIONAL = 0x10;

    // private static final int COMPILER_OUTPUT_DIRECTORY = 0x11;
    // private static final int COMPILER_CPP_LEVEL = 0x12;
    // private static final int COMPILER_ENABLE_EOC = 0x13;
    // private static final int COMPILER_EOC_EXCEPTION = 0x14;
    // private static final int COMPILER_EOC_RTTI = 0x15;
    // private static final int COMPILER_VERSION = 0x16;
    //
    // // command line for Processor related options, used by the ARC Tangent A4
    // plug-in
    private static final int TARGET_PROCESSOR_OPTION = 0xa0;

    // Assembler section
    private static final int ASSEMBLER_SECTION = 0x0008;

    // private static final int ASSEMBLER_SOURCE_EXTENSION = 0x01;
    // private static final int ASSEMBLER_OBJECT_EXTENSION = 0x02;
    private static final int OUTPUT_DIRECTORY = 0x03;

    // private static final int DELETE_OBJECT_FILE = 0x04;
    // private static final int C_STYLE_DIRECTIVES = 0x05;
    private static final int ASSEMBLER_OPT_ADDITIONAL = 0x06;

    // private static final int ASSEMBLER_ASSEMBLER_EXE = 0x07;
    // private static final int ASSEMBLER_VERSION = 0x08;

    // ////// New Assembler Data ////////////
    // General Page
    private static final int ASM_GEN_DEBUGINFO = 0x10;

    private static final int ASM_GEN_PRIVATELABEL = 0x11;

    // private static final int ASM_GEN_VERINOBJ = 0x12;
    private static final int ASM_GEN_NOCOPYR = 0x13;

    // Processor Page
    private static final int ASM_PROC_ARC_COREVER = 0x20;

    // private static final int ASM_PROC_ARC_32BITS = 0x21;
    // private static final int ASM_PROC_ARC_NOCFA = 0x22;
    // // private static final int ASM_PROC_ARC_OPTIONS = 0x23;
    // private static final int ASM_PROC_DRIVER_OPTIONS = 0x23;
    // private static final int ASM_PROC_OPTIONS = 0x2F;
    // PreProcessor Page
    private static final int ASM_PP_DEFINES = 0x30;

    private static final int ASM_PP_UNDEFINES = 0x31;

    // private static final int ASM_PP_REG_PERCENT = 0x32;
    // private static final int ASM_PP_NOBACKSLASHESC = 0x33;
    private static final int ASM_PP_INCLUDEDIRS = 0x34;

    // Listings Page
    // private static final int ASM_LST_SUMMST = 0x40;
    // private static final int ASM_LST_LISTINGS = 0x41;
    // private static final int ASM_LST_FLAGS = 0x44 ;
    // // Error and Warning Page
    // private static final int ASM_ERR_LIMIT = 0x50;
    private static final int ASM_ERR_NOWARNING = 0x51;

    // private static final int ASM_ERR_BADINST = 0x52;
    // private static final int ASM_ERR_UUSYMBOL = 0x53 ;

    // Linker section
    private static final int LINK_SECTION = 0x0009;

    // private static final int LINK_VERSION = 0x01;
    private static final int LINK_TYPE = 0x02;

    // private static final int LINK_OUTPUT_DIR = 0x03;
    private static final int LINK_OUTPUT_FILE = 0x04;

    // private static final int LINK_HEAP_SIZE = 0x05;
    private static final int LINK_LIBRARIES = 0x06;

    private static final int LINK_OBJ_FILES = 0x07;

    private static final int LINK_OPT_ADDITIONAL = 0x08;

    // private static final int LINK_LINKER_EXE = 0x09;

    // New Linker options for Command Files
    private static final int LINK_USE_SVR3_CMDFILES = 0x10;

    private static final int LINK_USE_SVR4_CMDFILES = 0x11;

    private static final int LINK_SVR3_CMDFILES = 0x12;

    private static final int LINK_SVR4_CMDFILES = 0x13;

    // New Linker Option added by MD 1.05
    // private static final int LINK_DEF_SYSLIB = 0x14;

    public boolean isLibrary() {
        String output = this.getOutputFile();
        if (output != null) {
            return output.endsWith(".a") || output.endsWith(".lib");
        }
        String type = mElxReader.getAttribute(LINK_SECTION, LINK_TYPE);
        if (type != null) {
            return type.toLowerCase().indexOf("lib") >= 0;
        }
        return false;
    }

    // TAD section
    private static final int DEBUG_SECTION = 0x000A;

    // private static final int TASK_AWARE_DEBUG = 0x01;
    // private static final int TAD_DLL = 0x02;
    private static final int ADDITIONAL_OPTIONS = 0x03;

    private static final int DEBUG_MODE = 0x04;

    // private static final int CONNECTION_TYPE = 0x05;
    // private static final int CONN_CMD_LINE = 0x06;
    // private static final int HAVE_CHIP_INIT = 0x07;
    // private static final int CHIP_INIT_FILE = 0x08;
    // private static final int DEBUG_VERSION = 0x09;
    // private static final int DEBUG_STARTIN_DIRECTORY = 0x0A;
    private static final int BLAST_XBF_FILE = 0x0B;

    // private static final int DEBUG_PPC_CHIP_TYPE = 0x0C;
    // private static final int CHIP_TYPE = 0x0D;
    // private static final int SPEED_TYPE = 0x0E;
    // private static final int BAUD_TYPE = 0x0F;
    // private static final int PARA_PORT_TYPE = 0x10;
    // private static final int SERIAL_PORT_TYPE = 0x11;
    // private static final int DEBUG_IP_ADDRESS = 0x14;
    // private static final int ENABLE_ALTIVEC = 0x15;
    // private static final int FIRST_MEM_ADDR1 = 0x16;
    // private static final int FIRST_MEM_ADDR2 = 0x17;
    // private static final int LAST_MEM_ADDR1 = 0x18;
    // private static final int LAST_MEM_ADDR2 = 0x19;
    // private static final int EXTENSION_DLL1 = 0x1A;
    // private static final int EXTENSION_DLL2 = 0x1B;
    // private static final int TAP_CONTROLLER = 0x1C;
    // private static final int SIM_HW_MEM_SIZE = 0x1D;
    // private static final int SPR_REG_VALUE = 0x1E;
    // private static final int DEBUG_USE_MD_EDITOR = 0x1F;
    // private static final int DEBUG_EXECUTE_MAIN = 0x26;
    // private static final int DEBUG_DATA_CACHE = 0x27;
    // private static final int DEBUG_HOST_LINK = 0x28;
    // private static final int DEBUG_SEECODE_UNCONFIG = 0x2B;
    // private static final int ARMANGEL_DLL_PATH = 0x2D;
    //
    // private static final int CONN_ENET_MODE = 0x12;
    // private static final int CONN_SERIAL_MODE = 0x13;
    //
    // ///////////////// Debugger option for ARC
    private static final int DEBUG_CONN_PORT = 0x20;

    private static final int DEBUG_CONN_TIMEOUT = 0x21;

    private static final int DEBUG_CONN_FUJITSU = 0x22;

    // private static final int DEBUG_ARC_SIM_XYMEM = 0x23;
    // private static final int DEBUG_ARC_OPTIONS = 0x24;
    private static final int DEBUG_FPGA_BLAST = 0x25;

    // private static final int DEBUG_XMEM_BASE_ADDR = 0x29;
    // private static final int DEBUG_YMEM_BASE_ADDR = 0x2A;
    // private static final int ADDITIONAL_CONN_OPTIONS = 0x2C;
    private static final int PARALLEL_PORT_IN_JTAG_MODE = 0x2D;

    //
    // private static final int DEBUG_CONN_ARM_RESET_RESTART = 0x2E;
    //
    // // Target arguments
    private static final int DEBUG_TARGET_ARGUMENTS = 0x30;
    //
    // // Toolchain section
    // private static final int TOOLCHAIN_SECTION = 0X000B;
    // private static final int TOOLCHAIN_METAWARE = 0x01;
    // private static final int TOOLCHAIN_RTOS = 0x02;
    // private static final int TOOLCHAIN_MQX = 0x03;
    // private static final int TOOLCHAIN_SEECODE = 0x04;
    // private static final int TOOLCHAIN_MET_ROOT = 0x05;
    // private static final int TOOLCHAIN_MET_COMP = 0x06;
    // private static final int TOOLCHAIN_OPTSET = 0x07;
    // private static final int TOOLCHAIN_MET_DEBUG = 0x08;
    // private static final int TOOLCHAIN_RTCS = 0x09;
    // private static final int TOOLCHAIN_USB = 0X0A; // USB indicator in
    // ToolChain Section;
    //
    // // IDE Build section
    // private static final int IDEBUILD_SECTION = 0x000C;
    // private static final int IDEBUILD_NOT_REFRESH_MAKE = 0x01;
    // private static final int IDEBUILD_KEEP_DEPENDENCIES = 0x02;
    //
    // // USB section
    // private static final int USB_SECTION = 0X000D;
    // private static final int USB_IN_USE = 0X01;
    // private static final int USB_ROOT_DIR = 0X02;
    // private static final int USB_MODE = 0X03;
    // private static final int USB_PACK = 0X04;
    // private static final int USB_COMPILER_OPT = 0X05;
    // private static final int USB_LINKER_OPT = 0X06;
    // private static final int USB_LIB_DIR = 0X07;
    // //////////////////// USB Class
    // private static final int USB_CLASS_NAME = 0x08;
    // private static final int USB_CLASS_ROOT_DIR = 0x09;
    // private static final int USB_CLASS_SUBDIR = 0x0a;
    // private static final int USB_CLASS_LIB_PATH = 0x0b;
    //
    // private static final int USB_VERSION = 0X10;
    //
    // // Integrated Profiler section
    // private static final int INTEGRATED_PROFILER_SECTION = 0X000E;
    // private static final int PROFILER_VERSION = 0X01;
    // private static final int PROFILER_EXE_PATH = 0X02;
    // private static final int PROFILER_JAVA_PATH = 0X03;
    // private static final int PROFILER_SET_JAVA_MEM = 0X04;
    // private static final int PROFILER_JAVA_MEM = 0X05;
    //
    // private static final int PROFILER_PERFORMANCE_DATAFILE = 0X10;
    //
    // private static final int PROFILER_EDSCONN_TYPE = 0X20;
    //
    // private static final int PROFILER_EDS_DATAFILE = 0X21;
    //
    // private static final int PROFILER_EDSCONN_ENET_ADDR = 0X22;
    // private static final int PROFILER_EDSCONN_ENET_PORT = 0X23;
    // private static final int PROFILER_EDSCONN_ENET_SOCKET_TYPE = 0X24;
    // private static final int PROFILER_EDSCONN_ENET_ADDR_TYPE = 0X25;
    //
    // private static final int PROFILER_EDSCONN_SERIAL_COM = 0X28;
    // private static final int PROFILER_EDSCONN_SERIAL_BAUD = 0X29;
    // private static final int PROFILER_EDSCONN_SERIAL_PARITY = 0X2A;
    // private static final int PROFILER_EDSCONN_SERIAL_DATABITS = 0X2B;
    // private static final int PROFILER_EDSCONN_SERIAL_STOPBITS = 0X2C;
    // private static final int PROFILER_EDSCONN_SERIAL_FLOW = 0X2D;
    // private static final int PROFILER_EDSCONN_SERIAL_TIMEOUT = 0X2E;
    //
    // // OS changer section
    // private static final int OSCHANGER_SECTION = 0X000F;
    // private static final int OSC_ENABLED = 0x01;
    // private static final int OSC_ROOT = 0x02;
    // private static final int OSC_ORIGIN_OSNAME = 0x03;
    // private static final int OSC_ORIGIN_OSROOT = 0x04;
    // private static final int OSC_INCLUDE_DIRS = 0x05;
    // private static final int OSC_DEFINES = 0x06;
    // private static final int OSC_COMP_OPTION = 0x07;
    // private static final int OSC_LIBS = 0x08;
    //
    // // Other section
    // private static final int OTHER_SECTION = 0x0100;
    // private static final int TARGET_MODIFIED = 0x01;
    // private static final int DEBUGGER_RUNNING = 0x02;
    // private static final int CRITICAL_SETTING_MODIFIED = 0x03;
    // private static final int MODIFIED_FILE_LIST = 0x04;
    // private static final int INSTALLATION_DIRECTORY = 0x06;
    // // This flag will indicate the project is for Precise/MQX own product
    // private static final int MQX_LIBRARIES = 0x05;
    // private static final int OVERWRITE_MAKEFILE = 0x07;
    // private static final int DE_CMD_ARGS = 0x0102;
    //
    // private static final int MAX_NUMBER_CRITICAL = 64;

}
