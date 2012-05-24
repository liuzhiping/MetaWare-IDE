/*
 * ICDITarget3
 * $Revision$
 * $Date$
 *
 * CONFIDENTIAL AND PROPRIETARY INFORMATION.
 * Copyright 2007 ARC International (Unpublished).
 * All Rights Reserved.
 * This document, material and/or software contains confidential and
 * proprietary information of ARC International and is protected by copyright,
 * trade secret and other state, federal, and international laws, and may be
 * embodied in patents issued or pending. Its receipt or possession does not
 * convey any rights to use, reproduce, disclose its contents, or to
 * manufacture, or sell anything it may describe.  Reverse engineering is
 * prohibited, and reproduction, disclosure or use without specific written
 * authorization of ARC International is strictly forbidden.  ARC and the ARC
 * logotype are trademarks of ARC International.
 */
package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.core.runtime.IPath;

/**
 * Extra methods required by ARC toolset integration.
 * <P>
 * CUSTOMIZATION
 * 
 * 
 * @author davidp David Pickens
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface ICDITarget3 extends ICDITarget2 {
    
    /**
     * This method is adds the ability to access a byte from a source line.
     */
    ICDILineLocation createLineLocation(String file, int line, int byteOffsetFromLine);
    
    /**
     * Creates a function location with a byte offset from a function.
     */
    ICDIFunctionLocation createFunctionLocation(String file, String function, int byteOffsetFromFunction);
    
    /**
     * Called when ready to load the target program for debugging.
     * @throws CDIException if something messes up.
     */
    void start() throws CDIException;
    
    /**
     * Called after {@link #start} and after breakpoints have been restored.
     * This gives the target the opportunity to, say, set watchpoints that
     * were implicit in a debugger startup file.
     * @throws CDIException
     */
    void onBreakpointsRestored() throws CDIException;
    
    /**
     * Returns whether or not the target has a custom disassembly view. If so, it does
     * not use the generic CDT disassembly view.
     * @return where or not the target has a custom disassembly view.
     */
    boolean hasCustomDisassemblyView();
    
    /**
     * Refresh the views from the engine, even though the target process
     * appears to have been suspended since the last update.
     */
    void refreshViews();
    
    /**
     * @return the path to the associated exe, or <code>null</code> if not known, or not relavent.
     */
    public IPath getExePath();
    
    /**
     * Restart with different command-line arguments.
     * @param args new command-line arguments, or <code>null</code> if current arguments are to be
     * re-used.
     */
    public void restart(String args[]) throws CDIException;
    
    /**
     * Set the directory translation paths. path[n] is a compiler source directory
     * and path[n+1] is where it is to be mapped to.
     * @param paths list of pairs of directories: compile directory, local directory.
     * @throws CDIException
     */
    public void setDirectoryTranslation(String paths[])throws CDIException;

}
