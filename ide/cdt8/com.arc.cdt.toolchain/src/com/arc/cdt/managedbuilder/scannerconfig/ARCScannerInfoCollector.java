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
package com.arc.cdt.managedbuilder.scannerconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.cdt.managedbuilder.scannerconfig.IManagedScannerInfoCollector;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * @author Hurai Rody Created on Jun 10, 2004
 * 
 * A custom ARC Scanner Info Collector.
 * 
 * This class is used for generating header files for source files, probably
 * among other things.
 */
@SuppressWarnings("restriction")
public class ARCScannerInfoCollector implements IManagedScannerInfoCollector {
	protected Map<String,String> definedSymbols;
	protected static final String EQUALS = "="; //$NON-NLS-1$
	protected List<String> includePaths;
	protected IProject project;

	/**
	 *  
	 */
	public ARCScannerInfoCollector() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector#contributeToScannerConfig(org.eclipse.core.resources.IResource,
	 *      java.util.List, java.util.List, java.util.Map)
	 */ 
	@Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void contributeToScannerConfig(Object resource,  Map/*<? extends Object,List<String>>*/ scannerInfo) {
        List<String> includes = (List<String>)scannerInfo.get(ScannerInfoTypes.INCLUDE_PATHS);
        List<String> symbols =  (List<String>)scannerInfo.get(ScannerInfoTypes.SYMBOL_DEFINITIONS);
		// This method will be called by the parser each time there is a new
		// value
        for (String path: includes) {
			getIncludePaths().add(path);
		}

		// Now add the macros
        for (String symbol: symbols){
			// See if it has an equals
			String[] macroTokens = symbol.split(EQUALS);
			String macro = macroTokens[0].trim();
			String value = (macroTokens.length > 1)
					? macroTokens[1].trim()
					: new String();
			getDefinedSymbols().put(macro, value);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.core.parser.IScannerInfo#getDefinedSymbols()
	 */
	@Override
    public Map<String,String> getDefinedSymbols() {
		if (definedSymbols == null) {
			definedSymbols = new HashMap<String,String>();
		}
		return definedSymbols;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.managedbuilder.scannerconfig.IManagedScannerInfoCollector#getIncludePaths()
	 */
	@Override
    public List<String> getIncludePaths() {
		if (includePaths == null) {
			includePaths = new ArrayList<String>();
		}
		return includePaths;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.managedbuilder.scannerconfig.IManagedScannerInfoCollector#setProject(org.eclipse.core.resources.IProject)
	 */
	@Override
    public void setProject(IProject project) {
		this.project = project;

	}

    @Override
    public List<String> getCollectedScannerInfo (Object resource, ScannerInfoTypes type) {
        List<String> rv = null;
        String errorMessage = null;
        if (resource == null) {
            errorMessage = "resource is null";//$NON-NLS-1$
        } 
        else if (!(resource instanceof IResource)) {
            errorMessage = "resource is not an IResource";//$NON-NLS-1$
        }
        else if (((IResource) resource).getProject() == null) {
            errorMessage = "project is null";//$NON-NLS-1$
        }
        else if (((IResource) resource).getProject() != project) {
            errorMessage = "wrong project";//$NON-NLS-1$
        }
        
        if (errorMessage != null) {
            TraceUtil.outputError("DefaultGCCScannerInfoCollector.getCollectedScannerInfo : ", errorMessage); //$NON-NLS-1$
        }
        else if (type.equals(ScannerInfoTypes.INCLUDE_PATHS)) {
            rv = getIncludePaths();
        }
        else if (type.equals(ScannerInfoTypes.SYMBOL_DEFINITIONS)) {
            rv = new ArrayList<String>();
            Map<String,String> symbols = getDefinedSymbols();
            for (Iterator<String> i = symbols.keySet().iterator(); i.hasNext(); ) {
                String macro =  i.next();
                String value =  symbols.get(macro);
                if (value.length() > 0) {
                    rv.add(macro + EQUALS + value);
                }
                else {
                    rv.add(macro);
                }
            }
        }
        else {
            rv = new ArrayList<String>();
        }
        return rv;
    }

}
