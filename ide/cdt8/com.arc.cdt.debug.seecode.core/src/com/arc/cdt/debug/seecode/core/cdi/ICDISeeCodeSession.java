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
package com.arc.cdt.debug.seecode.core.cdi;

import java.io.File;

import org.eclipse.cdt.debug.core.cdi.ICDISession;

/**
 * This interface exports stuff from the SeeCode implementation of ICDISession that
 * is required by the UI.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface ICDISeeCodeSession extends ICDISession {
    
    interface ISessionDisposeListener{
        /**
         * Called when session terminates.
         * @param session
         */
        void onSessionDisposed(ICDISeeCodeSession session);
    }
    
    /**
     * Do whatever is necessary to update things since stopping.
     */
    public void updateViews();

    /**
     * Return the path of where the engine is to
     * read the ".args" file from an Options window.
     * The string has a "%s" where the prefix is
     * to be inserted.
     * @return the path where the engine is to
     * read a file produced by an Optionw dialog window.
     */
    public String getArgsPattern ();
    
    /**
     * Return the installation directory of the SeeCode debugger
     * (e.g. "C:/ARC/MetaWare/arc"). The SeeCode driver command is expected
     * to be in the "bin" directory below the returned directory path.
     * @return the installation directory of the SeeCode debugger.
     */
    public String getSeeCodeInstallationDirectory();
    
    /**
     * Return the directory where we pass "args" back
     * to the engine.
     * @return the directory where we store per-project
     * or directory where we store the elf file for non project case
     * arguments and such.
     */
    public File getSessionDirectory();
    
    public void addSessionDisposeListener(ISessionDisposeListener listener);
    public void removeSessionDisposeListener(ISessionDisposeListener listener);
    
    /**
     * @return whether or not the associated session is terminated.
     */
    public boolean isTerminated();

	public abstract void forceEmergencyShutdown();
}
