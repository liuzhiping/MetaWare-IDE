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

import java.util.Properties;

import com.arc.widgets.IContainer;



/**
 * An object for making {@link ISeeCodeTextViewer text display view}
 * objects.
 * @author David Pickens
 */
public interface ISeeCodeTextViewerFactory {
    /**
     * Given a property description, create a display.
     * @param id the unique ID number to be assigned to the display.
     * @param props the description of the display.
     * @param parent the parent component in which the
     * display view is to be inserted. Under SWT, it will be
     * a <code>Composite</code> of some sort. Under Swing it
     * will be a <code>Container</code>.
     * @param callback callback interface to respond to
     * display-related events.
     * @return the new display.
     * @pre props != null  && callback != null && updates != null
     */
    ISeeCodeTextViewer createDisplay(int id, Properties props,
            IContainer parent, ISeeCodeTextViewerCallback callback);
    
    /**
     * Given an ID number, return the corresponding display.
     * @param idNumber the id number of the display.
     * @return the display corresponding to the ID number.
     * @exception IllegalArgumentException if there is no display
     * associated with the ID number, or the display is disposed.
     */
    ISeeCodeTextViewer getDisplay(int idNumber)  throws IllegalArgumentException;
    
    /**
     * Add a custom user-defined GUI from a "guihili" specification.
     * @param ugui_number a number to be sent back in the
     * <code>ISeeCodeTextViewerCallback.sendValueUpdate()</code>
     * call. 
     * @param specification the "guihili" specification.
     * <P><i> menubar</i> where new menus to be placed if the
     * specification defines a menu.
     * @param callback callback to receive engine property updates as
     * events are fired.
     * @param viewer the associated viewer display, or null.
     */
    public void addUserGUI(
            int ugui_number,
            String specification,
            MenuDescriptor menubar,
            ISeeCodeTextViewerCallback callback,
            ISeeCodeTextViewer viewer) ;
    
    /**
     * Return the debugger's installation path (e.g., "C:/arc/metaware/arc").
     * @return the debugger's installation path.
     */
    public String getDebuggerInstallPath();
}
