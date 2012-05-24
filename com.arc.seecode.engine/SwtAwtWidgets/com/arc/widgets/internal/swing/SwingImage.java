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

import javax.swing.ImageIcon;

import com.arc.widgets.IImage;

/**
 * @author David Pickens
 */
class SwingImage implements IImage {

    private ImageIcon mIcon;

    public SwingImage(ImageIcon icon){
        mIcon = icon;
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.IImage#getObject()
     */
    @Override
    public Object getObject() {
        return mIcon;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IImage#getWidth()
     */
    @Override
    public int getWidth() {
        return mIcon.getIconWidth();
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IImage#getHeight()
     */
    @Override
    public int getHeight() {
        return mIcon.getIconHeight();
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IImage#getPixel(int, int)
     */
    public int getPixel(int x, int y) {
        return 0;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IImage#setDepth(int)
     */
    public void setDepth(int bits) {
       throw new IllegalArgumentException("setDepth not supported");

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IImage#setPixel(int, int, int)
     */
    public void setPixel(int x, int y, int value) {
        throw new IllegalArgumentException("setPixel not supported");

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IImage#setAlpha(int, int, int)
     */
    public void setAlpha(int x, int y, int alpha) {
        throw new IllegalArgumentException("setAlpha not supported");

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IImage#setPixels(int, int, int[], int, int)
     */
    public void setPixels(int x, int y, int[] data, int startIndex, int count) {
        throw new IllegalArgumentException("setPixel not supported");

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IImage#dispose()
     */
    @Override
    public void dispose() {

    }

}
