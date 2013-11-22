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
package com.arc.widgets.internal.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Control;

import com.arc.widgets.IComponent;
import com.arc.widgets.IFont;


/**
 * Wrapper for SWT Font type.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
class SWTFont implements IFont {

    private Font mFont;
    private FontData mFontData;

    SWTFont(Font font){
        mFont = font;
        mFontData = font.getFontData()[0];
    }
    
    @Override
    public Object getObject () {
        return mFont;
    }

    
    @Override
    public String getName () {
       return mFontData.getName();
    }

    
    @Override
    public int getSize () {
        return mFontData.getHeight();
    }

    
    @Override
    public boolean isItalic () {
        return (mFontData.getStyle() & SWT.ITALIC) != 0;
    }

    @Override
    public boolean isBold () {
        return (mFontData.getStyle() & SWT.BOLD) != 0;
    }

   
    @Override
    public int getPixelWidth (String x, IComponent c) {
        Control control = (Control)c.getComponent();
        GC gc = new GC(control);
        //NOTE: cr90972: gc.textExtent(x) returns a value that is too small
        // on some machines (Eclipse 3.2.x), but not all. 
        // gc.StringExtent() seems to work across the board.
        int width = gc.stringExtent(x).x;
        gc.dispose();
        return width;
        }

    @Override
    public int getPixelHeight (IComponent c) {
        return mFontData.getHeight();
    }

}
