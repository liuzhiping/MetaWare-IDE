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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * For CMPD, each ICDITarget instance also implements this interface.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface ICMPDTarget {
    /** 
     * Return the assigned process name. There may be multiple instance per process.
     * @return the assigned process name.
     */
    public String getProcessName();
    
    /**
     * Return the ID of the process. Typically a small integer.
     * @return the process ID.
     */
    public int getProcessId();
    
    /**
     * Return the total number of CMPD processes with this {@link #getProcessName() name}.
     * @return the total number of CMPD processes with this {@link #getProcessName() name}.
     */
    public int getProcessInstanceTotal();
    
    /**
     * @return the path to the associated exe.
     */
    public IPath getExePath();
    
    /**
     * Return the associated project, if any.
     * @return the associated project, or <code>null</code> if there is no associated project.
     */
    public IProject getProject();

}
