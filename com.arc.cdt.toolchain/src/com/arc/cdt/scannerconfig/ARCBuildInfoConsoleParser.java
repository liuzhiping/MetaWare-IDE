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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import com.arc.mw.util.Cast;


/**
 * Scans build output looking for explicit "-D" and "-I" to contribute to include paths and symbols.
 */
@SuppressWarnings("restriction")
public class ARCBuildInfoConsoleParser implements IScannerInfoConsoleParser {

    private IProject fProject = null;

    private ScannerInfoConsoleParserUtility fUtil = null;

    private IScannerInfoCollector fCollector = null;

    // private IMarkerGenerator fMarkerGenerator;

    public void startup (
        IProject project,
        IPath workingDirectory,
        IScannerInfoCollector collector,
        IMarkerGenerator markerGenerator) {
        fProject = project;
        fCollector = collector;

        fUtil = (project != null && workingDirectory != null && markerGenerator != null) ? new ScannerInfoConsoleParserUtility(
            project, workingDirectory, markerGenerator) : null;

    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.ScannerInfoConsoleParserUtility#processLine(java.lang.String)
     */
    public boolean processLine (String line) {
        TraceUtil.outputTrace("ARCScannerInfoConsoleParser parsing line:", TraceUtil.EOL, line); //$NON-NLS-1$ 
        // We're only interested in the first line to the High C front end.
        // Such lines have "-def" and "-dir" entries.
        if (line.indexOf("-I") < 0 && line.indexOf("-D") < 0)
            return false;

        ArrayList<String> allTokens = new ArrayList<String>(Arrays.asList(ScannerConfigUtil.tokenizeStringWithQuotes(
            line,
            "\"")));
        if (allTokens.size() <= 1)
            return false;
        Iterator<String> I = allTokens.iterator();

        List<String> includes = new ArrayList<String>();
        // Look for
        // -Dfoo or -Dfoo=1
        // and
        // -Ifoo or "-I foo"
  

        List<String> translatedIncludes = new ArrayList<String>();
        List<String> symbols = new ArrayList<String>();

        // List targetSpecificOptions = new ArrayList();

        String fileName = null;
        String token = null;
        String cashedToken = null;
        while (I.hasNext()) {
            if (cashedToken == null) {
                token = I.next();
            }
            else {
                token = cashedToken;
                cashedToken = null;
            }
            if (token.length() == 0) {
                continue;
            }
            if (token.startsWith("-D")) {//$NON-NLS-1$
                String define = token.substring(2);
                if (define.indexOf('=') < 0) {
                    define = define + "=1";
                }
               
                if (!symbols.contains(define))
                    symbols.add(define);
            }
            else if (token.startsWith("-I") &&( token.length() > 2 || I.hasNext())) {//$NON-NLS-1$

                String iPath = token.length()>2?token.substring(2):I.next();

                if (iPath.length() == 0 || iPath.charAt(0) == '-') {
                    cashedToken = iPath;
                    continue;
                }
                if (!includes.contains(iPath))
                    includes.add(iPath);
            }
            else {
                String possibleFileName = token.toLowerCase();
                if (possibleFileName.startsWith("..") || //$NON-NLS-1$
                    possibleFileName.startsWith(".") || //$NON-NLS-1$
                    possibleFileName.startsWith("/") || //$NON-NLS-1$
                    possibleFileName.endsWith(".c") || //$NON-NLS-1$
                    possibleFileName.endsWith(".cpp") || //$NON-NLS-1$
                    possibleFileName.endsWith(".cc") || //$NON-NLS-1$
                    possibleFileName.endsWith(".cxx")) { //$NON-NLS-1$

                    fileName = token;
                }
            }
        }

        if (includes.size() > 0 || symbols.size() > 0) {
            IFile file = null;
            IProject project = fProject;
            translatedIncludes = includes;
            if (includes.size() > 0 && fUtil != null) {
                if (fileName != null) {
                    file = fUtil.findFile(fileName);
                    if (file != null) {
                        project = file.getProject();
                        translatedIncludes = Cast.toType(fUtil.translateRelativePaths(file, fileName, includes));
                    }
                }
                else {
                    TraceUtil.outputError("Unable to find file name: ", line); //$NON-NLS-1$
                    fUtil.generateMarker(fProject, -1, "Unable to find file name: " + line, //$NON-NLS-1$
                        IMarkerGenerator.SEVERITY_ERROR_RESOURCE,
                        null);
                }
            }
            if (fCollector != null) {
                Map<ScannerInfoTypes, List<String>> scannerInfo = new HashMap<ScannerInfoTypes, List<String>>();
                scannerInfo.put(ScannerInfoTypes.INCLUDE_PATHS, translatedIncludes);
                scannerInfo.put(ScannerInfoTypes.SYMBOL_DEFINITIONS, symbols);
                if (fCollector instanceof PerProjectSICollector) {
                    ((PerProjectSICollector) fCollector).contributeToScannerConfig(project, scannerInfo, true);
                }
                else {
                    fCollector.contributeToScannerConfig(fProject, scannerInfo);
                }
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser#shutdown()
     */
    public void shutdown () {
        if (fUtil != null) {
            fUtil.reportProblems();
        }
    }

}
