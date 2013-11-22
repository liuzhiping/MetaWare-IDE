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
package com.arc.cdt.scannerconfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.internal.core.scannerconfig.ScannerConfigUtil;
import org.eclipse.cdt.make.internal.core.scannerconfig.gnu.ScannerInfoConsoleParserUtility;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.cdt.make.internal.core.scannerconfig2.PerProjectSICollector;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * 
 * Parses the output of clang when "-v -E -dM" is specified to get all of the
 * macros and header files defined.
 * 
 * @author David Pickens
 * 
 */
@SuppressWarnings("restriction")
public class ClangScannerInfoConsoleParser implements IScannerInfoConsoleParser {

    private IProject fProject = null;

    private ScannerInfoConsoleParserUtility fUtil = null;

    private IScannerInfoCollector fCollector = null;
    private List<String> includes = new ArrayList<String>();
    private List<String> symbols = new ArrayList<String>();

    // private IMarkerGenerator fMarkerGenerator;

    @Override
    public void startup(IProject project, IPath workingDirectory, IScannerInfoCollector collector,
            IMarkerGenerator markerGenerator) {
        fProject = project;
        fCollector = collector;
        // if (collector instanceof IScannerInfoCollectorCleaner){
        // ((IScannerInfoCollectorCleaner)collector).deleteAll(project);
        // }
        // fMarkerGenerator = markerGenerator;
        fUtil = (project != null && workingDirectory != null && markerGenerator != null) ? new ScannerInfoConsoleParserUtility(
                project, workingDirectory, markerGenerator) : null;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.cdt.make.core.scannerconfig.ScannerInfoConsoleParserUtility
     * #processLine(java.lang.String)
     */
    @Override
    public boolean processLine(String line) {
        TraceUtil.outputTrace("ClangScannerInfoConsoleParser parsing line:", TraceUtil.EOL, line); //$NON-NLS-1$ 
        // "ccac -v -dM -E" output:
        //
        //#define _ARC 1
        //#define _ARCOMPACT 1
        //#define _ARCVER 0x50
        //#define _ARC_LE 1
        //#define _ASM(x) __asm(x)
        //#define _ATT4 1
        //#define _CC(x) __attribute__((x))
        //#define _CRTIMP 
        //#define _DIV_T_DEFINED 
        //#define _Exception __attribute__((__exception__))
        //...
        // clangac ... -internal-isystem <include-path>

        boolean result = false;
        if (line.startsWith("#define")) {
            int i =  7;
            while (i < line.length() && Character.isWhitespace(line.charAt(i))){
                i++;
            }
            if (i < line.length()){
                int symStart = i;
                while (i < line.length() && !Character.isWhitespace(line.charAt(i))){
                    i++;
                }
                int symEnd = i;
                while (i < line.length() && Character.isWhitespace(line.charAt(i))){
                    i++;
                }
                String symDef = line.substring(symStart,symEnd) + "=" + line.substring(i);
                if (!symbols.contains(symDef)) {
                    symbols.add(symDef);
                    result = true;
                }
            }
        }
        else if (line.indexOf("clang") >= 0) {     
            ArrayList<String> allTokens = new ArrayList<String>(Arrays.asList(ScannerConfigUtil
                    .tokenizeStringWithQuotes(line, "\"")));
            if (allTokens.size() <= 1)
                return false;
            Iterator<String> I = allTokens.iterator();
            String token = null;
            while (I.hasNext()) {
               token = I.next();
               if (token.equals("-internal-isystem") && I.hasNext()) {//$NON-NLS-1$:   MetaWare case and clang case
                    String iPath = I.next();
                    if (!includes.contains(iPath)) {
                        includes.add(iPath);
                        result = true;
                    }
                } 
            }
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser#shutdown
     * ()
     */
    @Override
    public void shutdown() {
        if (fUtil != null) {
            fUtil.reportProblems();
        }
        if (includes.size() > 0 || symbols.size() > 0) {
            Map<ScannerInfoTypes, List<String>> scannerInfo = new HashMap<ScannerInfoTypes, List<String>>();
            scannerInfo.put(ScannerInfoTypes.INCLUDE_PATHS, includes);
            scannerInfo.put(ScannerInfoTypes.SYMBOL_DEFINITIONS, symbols);
            if (fCollector != null) {
                if (fCollector instanceof PerProjectSICollector) {
                    ((PerProjectSICollector) fCollector).contributeToScannerConfig(fProject,
                            scannerInfo, true);
                } else {
                    fCollector.contributeToScannerConfig(fProject, scannerInfo);
                }
            }
            TraceUtil
                    .outputTrace(
                            "Scanner info from \'specs\' file", //$NON-NLS-1$
                            "Include paths", includes, new ArrayList<String>(0), "Defined symbols", symbols); //$NON-NLS-1$ //$NON-NLS-2$);
        }
    }
}
