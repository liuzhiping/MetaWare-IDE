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
package com.arc.cdt.debug.seecode.internal.ui;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Callback methods that the Guihili processing needs to read and write properties.
 * An instance of this interface exists for ordinary debugger launch configuration, and
 * another for CMPD stuff.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface IGuihiliCallback {
    /**
     * 
     * @return the launch name.
     */
    public String getLaunchName();
    
    /**
     * Return the map of all property values that have been persistently stored.
     * @return the map of all poperty values.
     */
    public Properties getProperties();
    
    /**
     * Assign a new set of properties.
     * @param props the new set of properties.
     */
    public void setProperties(Properties props);
    
    /**
     * Return the previously computed "swahili" arguments or <code>null<code>.
     * The swahili arguments are what are passed to the SeeCode driver command to configure the
     * debugger.
     * @return the previously computed <i>swahili</i> arguments or <code>null</code>.
     */
    public List<String>getSwahiliArguments();
    
    /**
     * Set the <i>swahili</i> arguments based on the current value of the properties.
     * @param args the new <i>swahili</i> arguments.
     */
    public void setSwahiliArguments(List<String> args);

    /**
     * Return associated project, if known; otherwise returns <code>null</code>.
     * @return associated project or <code>null</code>.
     */
    public IProject getProject ();
    
    /**
     * @return a short string the indicates the target CPU type (e.g., "ac", "arm", "vc", etc.).
     */
    public String getTargetCPU();
    
    /**
     * @return the overriding OS environment, or <code>null</code> if the client's
     * environment is being used.
     */
    public String[] getEnvironment();
    
    /**
     * @return the relavent working directory.
     */
    public File getWorkingDirectory();
    
    /**
     * @return the total number of processes (or "cores").
     */
    public int getProcessCount();
    
    public ILaunchConfiguration getLaunchConfiguration ();

}
