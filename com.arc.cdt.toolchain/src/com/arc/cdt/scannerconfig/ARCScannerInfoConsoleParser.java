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
 * @author hurair Created on Jun 11, 2004
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
@SuppressWarnings("restriction")
public class ARCScannerInfoConsoleParser implements IScannerInfoConsoleParser {

    private IProject fProject = null;

	private ScannerInfoConsoleParserUtility fUtil = null;

    private IScannerInfoCollector fCollector = null;
    private List<String> includes = new ArrayList<String>();
    private List<String> symbols = new ArrayList<String>();

    //private IMarkerGenerator fMarkerGenerator;

 
	public void startup (IProject project, IPath workingDirectory, IScannerInfoCollector collector, IMarkerGenerator markerGenerator) {
        fProject = project;
        fCollector = collector;
//        if (collector instanceof IScannerInfoCollectorCleaner){
//            ((IScannerInfoCollectorCleaner)collector).deleteAll(project);
//        }
        //fMarkerGenerator = markerGenerator;
        fUtil = (project != null && workingDirectory != null && markerGenerator != null) ?
                new ScannerInfoConsoleParserUtility(project, workingDirectory, markerGenerator) : null;
        
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.make.core.scannerconfig.ScannerInfoConsoleParserUtility#processLine(java.lang.String)
     */
	public boolean processLine(String line) {
        TraceUtil
                .outputTrace(
                        "ARCScannerInfoConsoleParser parsing line:", TraceUtil.EOL, line); //$NON-NLS-1$ 
        // We're only interested in the first line to the High C front end.
        // Such lines have "-def" and "-dir" entries.
        if (line.indexOf("-def") < 0) return false;

        ArrayList<String> allTokens = new ArrayList<String>(Arrays.asList(ScannerConfigUtil
                .tokenizeStringWithQuotes(line,"\"")));
        if (allTokens.size() <= 1) return false;
        Iterator<String> I = allTokens.iterator();
        
        // Look for
        //    -def "foo 1"
        // and
        //    -dir "directory"
        // Recognized gcc or g++ compiler invocation
       
        symbols.add("__HIGHC__"); // TODO: somehow don't define this if -Hansi specified!
        //List targetSpecificOptions = new ArrayList();
        String token = null;
        String cashedToken = null;
        while (I.hasNext()) {
            if (cashedToken == null) {
                token = I.next();
            } else {
                token = cashedToken;
                cashedToken = null;
            }
            if (token.length() == 0) {
                continue;
            }
            if (token.equals("-def") && I.hasNext()) {//$NON-NLS-1$
                String define = I.next();

                if (define.length() > 0 && define.charAt(0) == '-') {
                    cashedToken = define;
                    continue;
                }
                int spaceIndex = define.indexOf(' ');
                String symbol;
                if (spaceIndex < 0) {
                    symbol = define;
                } else {
                    symbol = define.substring(0, spaceIndex) + '='
                            + define.substring(spaceIndex + 1);
                }

                if (!symbols.contains(symbol)) symbols.add(symbol);
            } else if (token.equals("-dir") && I.hasNext()) {//$NON-NLS-1$

                String iPath = I.next();

                if (iPath.length() == 0 || iPath.charAt(0) == '-') {
                    cashedToken = iPath;
                    continue;
                }
//                String nPath = fUtil.normalizePath(iPath);
//                if (!includes.contains(nPath)) includes.add(nPath);
                if (!includes.contains(iPath)) includes.add(iPath);
            } 
        }

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser#shutdown()
     */
    public void shutdown () {
        if (fUtil != null) {
            fUtil.reportProblems();
        }
        if (includes.size() > 0 || symbols.size() > 0) {
            Map<ScannerInfoTypes, List<String>> scannerInfo = new HashMap<ScannerInfoTypes, List<String>>();
            scannerInfo.put(ScannerInfoTypes.INCLUDE_PATHS, includes);
            scannerInfo.put(ScannerInfoTypes.SYMBOL_DEFINITIONS, symbols);
            if (fCollector != null) {
                if (fCollector instanceof PerProjectSICollector) {
                    ((PerProjectSICollector) fCollector).contributeToScannerConfig(fProject, scannerInfo, true);
                }
                else {
                    fCollector.contributeToScannerConfig(fProject, scannerInfo);
                }
            }
            TraceUtil.outputTrace("Scanner info from \'specs\' file", //$NON-NLS-1$
                "Include paths", includes, new ArrayList<String>(0), "Defined symbols", symbols); //$NON-NLS-1$ //$NON-NLS-2$);
        }
    }

 

//    private int getDirectoryLevel(String line) {
//        int s = line.indexOf('[');
//        int num = 0;
//        if (s != -1) {
//            int e = line.indexOf(']');
//            String number = line.substring(s + 1, e).trim();
//            try {
//                num = Integer.parseInt(number);
//            } catch (NumberFormatException exc) {
//            }
//        }
//        return num;
//    }

}
