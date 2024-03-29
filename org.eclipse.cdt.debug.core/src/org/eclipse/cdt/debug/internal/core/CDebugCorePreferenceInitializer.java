/*******************************************************************************
 * Copyright (c) 2004, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Ken Ryall (Nokia) - 207675
 * Synopsys -- MetaWare debugger integration
*******************************************************************************/
package org.eclipse.cdt.debug.internal.core; 

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.core.cdi.ICDIFormat;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
 
/**
 * Default preference value initializer for <code>CDebugCorePlugin</code>.
 */
public class CDebugCorePreferenceInitializer extends AbstractPreferenceInitializer {

	/** 
	 * Constructor for CDebugCorePreferenceInitializer. 
	 */
	public CDebugCorePreferenceInitializer() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		CDebugCorePlugin.getDefault().getPluginPreferences().setDefault( ICDebugConstants.PREF_MAX_NUMBER_OF_INSTRUCTIONS, ICDebugConstants.DEF_NUMBER_OF_INSTRUCTIONS );
		CDebugCorePlugin.getDefault().getPluginPreferences().setDefault( ICDebugConstants.PREF_DEFAULT_VARIABLE_FORMAT, ICDIFormat.NATURAL );
		CDebugCorePlugin.getDefault().getPluginPreferences().setDefault( ICDebugConstants.PREF_DEFAULT_EXPRESSION_FORMAT, ICDIFormat.NATURAL );
		CDebugCorePlugin.getDefault().getPluginPreferences().setDefault( ICDebugConstants.PREF_DEFAULT_REGISTER_FORMAT, ICDIFormat.HEXADECIMAL ); //CUSTOMIZATION: HEXADECIMAL
		CDebugCorePlugin.getDefault().getPluginPreferences().setDefault( ICDebugConstants.PREF_CHARSET, ICDebugConstants.DEF_CHARSET );
		CDebugCorePlugin.getDefault().getPluginPreferences().setDefault( ICDebugConstants.PREF_INSTRUCTION_STEP_MODE_ON, false );
	}
}
