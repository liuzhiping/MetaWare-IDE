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

import com.arc.widgets.IColor;


/**
 * Interface for accessing colors required for drawing the text
 * of a MetaWare debugger display.
 * @author dpickens
 *
 */
public interface IColorPreferences {
    /**
     * Return the color of the text.
     * @return the color of the text, or <code>null</code> if an appropriate default is to be used.
     */
    public IColor getForegroundColor();
    /**
     * Return the default color of the background if the text is not associated
     * with an overlay, read-only memory, or misaligned, memory etc.
     * @return the default color of the background, or <code>null</code> if an appropriate default is to be used.
     */
    public IColor getBackgroundColor();
    /**
     * Return the color used to highlight lines in the source and disassembly displays that
     * correspond to overlays.
     * @return the color used to highlight lines in the source and disassembly displays that
     * correspond to overlays, <code>null</code> if no highlighting is to be done.
     */
    public IColor getOverlayBackgroundColor();
    
    /**
     * @return the color used to highlight lines in the source, disassembly, and memory displays that
     * correspond to read-only memory, <code>null</code> if no highlighting is to be done.
     */
    public IColor getReadonlyBackgroundColor();
    
    /**
     * @return the color used to highlight lines in the  memory and examine displays that
     * correspond to misaligned memory, <code>null</code> if no highlighting is to be done.
     */
    public IColor getMisalignedBackgroundColor();
    
    /**
     * Return the color of the background of the debugger error log.
     * @return the color of the background, or <code>null</code> if an appropriate default is to be used.
     */
    public IColor getErrorLogBackgroundColor();
    /**
     * Return the color of the text of the debugger error log.
     * @return the color of the text, or <code>null</code> if an appropriate default is to be used.
     */
    public IColor getErrorLogForegroundColor();
}
