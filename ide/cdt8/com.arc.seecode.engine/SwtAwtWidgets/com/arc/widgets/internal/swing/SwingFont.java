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
package com.arc.widgets.internal.swing;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;

import com.arc.widgets.IComponent;
import com.arc.widgets.IFont;


/**
 * Our font wrapper.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
class SwingFont implements IFont {

    private Font mFont;

    SwingFont(Font font){
        mFont = font;
    }
    
    @Override
    public Object getObject () {
        return mFont;
    }

    @Override
    public String getName () {
        return mFont.getFamily();
    }

    @Override
    public int getSize () {
        return mFont.getSize();
    }

    
    @Override
    public boolean isItalic () {
       return mFont.isItalic();
    }

    @Override
    public boolean isBold () {
        return mFont.isBold();
    }

   
    @Override
    public int getPixelWidth (String x, IComponent c) {
        FontMetrics fm = ((Component)c.getComponent()).getFontMetrics(mFont);
        char[] chars = x.toCharArray();
        return fm.charsWidth(chars,0,chars.length);
    }

   
    @Override
    public int getPixelHeight (IComponent c) {
        FontMetrics fm = ((Component)c.getComponent()).getFontMetrics(mFont);
        return fm.getHeight();
    }

}
