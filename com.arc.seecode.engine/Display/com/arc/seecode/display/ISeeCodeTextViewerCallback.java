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

/**
 * Callback that the {@linkplain ISeeCodeTextViewer display view}
 * invokes actions.
 * @author David Pickens
 */
public interface ISeeCodeTextViewerCallback extends IClipboardDisplayCopier {
    /**
     * Send property update on behalf of a display.
     * 
     * @param d
     *            the associated display, if any. May be <code>null</code>.
     * @param propertyName
     *            name of property to update in engine.
     * @param value
     *            the value to assign to the property.
     */
    void sendValueUpdate(ISeeCodeTextViewer d, String propertyName, String value);

    /**
     * Send a property update, but with a timeout if there is a synchronization
     * delay with the recipient. If the timeout occurs, nothing is sent, and the
     * method returns false.
     * 
     * @param d
     *            the associated display, if any. May be <code>null</code>.
     * @param propertyName
     *            name of property to update in engine.
     * @param value
     *            the value to assign to the property.
     * @param timeout
     *            timeout value in milliseconds.
     * @return true if property change sent before timeout expired; false if
     *         timeout expired.
     */
    boolean sendValueUpdate(ISeeCodeTextViewer d, String propertyName, String value,
            int timeout);
    
    /**
     * Called when the display is being closed.
     * @param d the associated display being closed.
     */
    void onClose(ISeeCodeTextViewer d);
    
    /**
     * Create the menu if displays to be created as a new
     * tab in the given viewer's enclosing panel.
     * @param d the display showe enclosing panel will
     * contain any displays that are created.
     * @return menu descriptor.
     */
    MenuDescriptor createDisplayMenuFor(ISeeCodeTextViewer d);
    
    /**
     * Display an appropriate error box to indicate
     * an internal error of some sort.
     * @param viewer the associate viewer
     * @param message the message to be displayed
     * @param t an exception that was responsible
     */
    void internalError(ISeeCodeTextViewer viewer, String message, Throwable t);
    
    /**
     * Display an appropriate error box to indicate
     * a recoverable error
     * @param viewer the associate viewer
     * @param message the message to be displayed
     * @param title the title to appear on the erro box
     */
    void notifyError(ISeeCodeTextViewer viewer,String message, String title);
    
}
