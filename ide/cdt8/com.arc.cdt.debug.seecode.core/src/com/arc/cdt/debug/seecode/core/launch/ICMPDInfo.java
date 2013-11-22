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
package com.arc.cdt.debug.seecode.core.launch;


import java.util.Map;

import org.eclipse.core.resources.IProject;

import com.arc.seecode.engine.ProcessIdList;

/**
 * A description of a CMPD session.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface ICMPDInfo {

    /**
     * A description of a CMPD process or class of processes.
     * @author davidp
     * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
     * @version $Revision$
     * @lastModified $Date$
     * @lastModifiedBy $Author$
     * @reviewed 0 $Revision:1$
     */
    public interface IProcess {

        /**
         * Return the path to the executable, together with its arguments.
         * @return the path to the executable, together with its arguments.
         */
        public String[] getCommand ();

        /**
         * @return the so-called <i>swahili</i> arguments that are passed to the SeeCode driver to configuration a
         * debug session.
         */
        public String[] getSwahiliArgs ();

        /**
         * If this is a CMPD process, return the user-defined name for it. Otherwise, the results are undefined.
         * @return the user-defined name for the case of a CMPD process.
         */
        public String getProcessName ();

        /**
         * @return the associated project, if known; otherwise <code>null</code>.
         */
        public IProject getProject ();

        /**
         * Return the number of instances to be invoked.
         * @return the number of instances to be invoked.
         */
        public int getInstanceCount ();
        
        /**
         * Return the list of CMPD IDs that are configured by this class of processes.
         * @return the list of CMPD IDs that are configured by this class of processes.
         */
        public ProcessIdList getIDList();
        
        /**
         * Return any additional "guihili" properties from which we set stuff in 
         * the debugger options tab on the Launch Configuration dialog.
         * Most are set by reverse-engineering the "swahili" arguments.
         * @return extra guihili arguments.
         */
        public Map<String,String> getGuihiliProperties();
    }

    /**
     * Return list of CMPD processes to be configured.
     * @return list of CMPD processes to be configured.
     */
    public IProcess[] getProcesses ();
    
    /**
     * Return list of additional launch argument for the CMPD session.
     * @return list of additional launch argument for the CMPD session.
     */
    public String[] getLaunchArgs ();
    
    /**
     * Return a list of commands to be executed on behalf of all processes when the last
     * CMPD process has been loaded. Returns empty array if there are no such commands.
     * @return a list of commands to be executed on behalf of all processes when the last
     * CMPD process has been loaded.
     */
    public String[] getStartupCommands();
}
