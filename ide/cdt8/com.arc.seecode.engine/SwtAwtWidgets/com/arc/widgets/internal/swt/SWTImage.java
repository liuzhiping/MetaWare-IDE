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

import org.eclipse.swt.graphics.Image;

import com.arc.widgets.IImage;

/**
 * Wrapper for SWT image.
 * @author David Pickens
 */
class SWTImage implements IImage {
    private Image mImage;
    /**
     * @param image the image being wrapped.
     */
    public SWTImage(Image image) {
        mImage = image;       
    }

 

    /* (non-Javadoc)
     * @see com.arc.widgets.IImage#getObject()
     */
    @Override
    public Object getObject() {
        return mImage;
    }
    
    public Image getImage(){
        return mImage;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IImage#getPixel(int, int)
     */
    public int getPixel(int x, int y) {
        return mImage.getImageData().getPixel(x,y);
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IImage#setSize(int, int)
     */
    public void setSize(int width, int height) {
        mImage.getImageData().width = width;
        mImage.getImageData().height = height;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IImage#setDepth(int)
     */
    public void setDepth(int bits) {
        mImage.getImageData().depth = bits;

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IImage#setPixel(int, int, int)
     */
    public void setPixel(int x, int y, int value) {
        mImage.getImageData().setPixel(x,y,value);

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IImage#setAlpha(int, int, int)
     */
    public void setAlpha(int x, int y, int alpha) {
        mImage.getImageData().setAlpha(x,y,alpha);

    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IImage#setPixels(int, int, int, int[], int)
     */
    public void setPixels(int x, int y, int[] data, int startIndex, int count) {
        mImage.getImageData().setPixels(x,y,Math.min(count,data.length-startIndex),data,startIndex);
    }



    /* (non-Javadoc)
     * @see com.arc.widgets.IImage#getWidth()
     */
    @Override
    public int getWidth() {
        return mImage.getBounds().width;
    }



    /* (non-Javadoc)
     * @see com.arc.widgets.IImage#getHeight()
     */
    @Override
    public int getHeight() {
        return mImage.getBounds().height;
    }



    /* (non-Javadoc)
     * @see com.arc.widgets.IImage#dispose()
     */
    @Override
    public void dispose() {
        if (!mImage.isDisposed()){
            mImage.dispose();
        }
        
    }

}
