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
package com.arc.seecode.display;

import com.arc.seecode.engine.IEngineAPI;
import com.arc.widgets.IComponent;

/**
 * Callback to get information that a display or associated dialog might need.
 *
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface IContext {
    interface IThread {
        /**
         * Name of the thread as the user would expect to see it.
         * @return User-friendly thread name.
         */
        String getName();
        
        /**
         * Index that is used to identify the thread do the debugger.
         * @return Index that is used to identify the thread do the debugger.
         */
        int getIndex();
    }
    
    /**
     * Return threads.
     * @return array of thread descriptors.
     */
    IThread[] getThreads();
    
    /**
     * Return whether or not a watchpoint dialog should display mask and value.
     * @return whether or not a watchpoint dialog should display mask and value.
     */
    boolean supportsWatchpointMask();
    
    /**
     * Called by watchpoint dialog to set a watchpoint.
     * 
     * @param expr the watchpoint expression.
     * @param len the length, in bytes, if the size isn't inherent in the expression.
     * @param condition if not <code>null</code>, the booleancondition that
     * must evaluate to true before the watchpoint is to be hit.
     * 
     * @param threadID associated thread ID, or 0 to denote that the watchpoint
     * applies to all threads.
     * @param flags watchpoint flags as defined in {@link IEngineAPI}.
     * @param mask the masking value, if supported; otherwise it will be -1.
     * @param value the value to be compared under the mask.
     */
    void setWatchpoint(String expr, int len, String condition,int threadID, int flags,
        long mask, long value);
    
    /**
     * Called by watchpoint dialog to set a watchpoint on a register.
     * 
     * @param regName the register name.
     * @param len the length, in bytes, if the size isn't inherent in the expression.
     * @param condition if not <code>null</code>, the booleancondition that
     * must evaluate to true before the watchpoint is to be hit.
     * 
     * @param threadID associated thread ID, or 0 to denote that the watchpoint
     * applies to all threads.
     * @param flags watchpoint flags as defined in {@link IEngineAPI}.
     * @param mask the masking value, if supported; otherwise it will be -1.
     * @param value the value to be compared under the mask.
     * 
     * @throws IllegalArgumentException if regName is not a valid register name.
     */
    void setWatchpointReg(String regName, String condition, int threadID, int flags,
        long mask, long value) throws IllegalArgumentException;
    
    /**
     * Display error dialog to indicate that there is a problem with the values
     * in a dialog.
     * @param msg the message.
     * @param title error box title.
     */
    void displayError(String msg, String title);
    
    /**
     * Do whatever is necessary to associate a Help ID to a widget.
     * @param widget the widget (e.g., SWT Shell or Swing JComponent)
     * @param helpID a string denoting a help identifier.
     */
    void setHelpID(IComponent widget, String helpID);
    
    /**
     * Return the main shell of the application. Under Eclipse, it will be a Shell
     * object; under Swing, will be a Frame.
     */
    Object getShell();
    
    /**
     * Return the engine build ID, in case we need to base some functionality
     * on it.
     * @return the engine build ID.
     */
    String getEngineBuildID();

}
