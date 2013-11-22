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
package com.arc.seecode.internal.display;

import com.arc.widgets.IImageWidget;

/**
 * Maintains a displayable image from data that is received from the SeeCode
 * engine.
 * 
 * @author David Pickens
 */
class ImageManipulator {

    private IImageWidget mImageWidget;

    private int mWidth=0;

    private int mHeight=0;

    private boolean mARGB=true;

    private int[] mData = new int[0]; // encoding of image.

    private int mScaleUp = 1;

    private int mScaleDown = 1;
    
    private int mRotated = 0;
    private boolean mFlipped = false;

    /**
     * @param rgba
     *            if true, image is a 32-big ARGB; otherwise its simple
     *            8-bit greyscale.
     * @param image
     *            image widget that wraps the image.
     */
    ImageManipulator(boolean rgba,
            IImageWidget image) {
        mARGB = rgba;
        mImageWidget = image;
    }
  
    public void setScale(int scaleUp, int scaleDown){
        mScaleUp = scaleUp;
        mScaleDown = scaleDown;
        int w = mWidth*scaleUp / scaleDown;
        int h = mHeight*scaleUp / scaleDown;
        mImageWidget.setScaledSize(w,h);
        mImageWidget.apply();
    }
    
    public void setSize(int w, int h) {
        if (mWidth != w || mHeight != h) {
            mWidth = w;
            mHeight = h;
            int data[] = new int[w * h];
            System.arraycopy(mData, 0, data, 0, Math.min(mData.length,
                    data.length));
            mData = data;
            setScale(mScaleUp, mScaleDown);
        }
    }

    public void updateProperties(int width, int height, boolean rgba) {
        if (mWidth != width || mHeight != height || rgba != mARGB) {
            setSize(width, height);
            mARGB = rgba;
            refresh();
        }
    }
    
    public boolean isARGB(){
        return mARGB;
    }

    public void flip() {
        int data[] = new int[mData.length];
        for (int x = 0; x < mWidth; x++) {
            for (int y = 0; y < mHeight; y++) {
                int newX = mWidth - x - 1;
                int newY = y;
                data[mWidth * newY + newX] = mData[mWidth * y + x];
            }
        }
        mData = data;
        refresh();
        mFlipped = !mFlipped;
    }

    public void rotate(int degrees) {
        if (degrees == 0)
            return;
        int data[] = new int[mData.length];
        int newWidth = mWidth;
        int newHeight = mHeight;
        switch (degrees) {
            case 90: {
                newWidth = mHeight;
                newHeight = mWidth;
                for (int x = 0; x < mWidth; x++) {
                    for (int y = 0; y < mHeight; y++) {
                        int newX = mHeight - y-1;
                        int newY = x;
                        data[newWidth * newY + newX] = mData[mWidth * y + x];
                    }
                }
            }
                break;
            case 180 + 90:
            case -180 - 90:
            case -90: {
                newWidth = mHeight;
                newHeight = mWidth;
                for (int x = 0; x < mWidth; x++) {
                    for (int y = 0; y < mHeight; y++) {
                        int newX = y;
                        int newY = mWidth - 1 - x;
                        data[newWidth * newY + newX] = mData[mWidth * y + x];
                    }
                }

            }
                break;
            case -180:
            case +180: {
                for (int x = 0; x < mWidth; x++) {
                    for (int y = 0; y < mHeight; y++) {
                        int newX = mWidth - x - 1;
                        int newY = mHeight - y - 1;
                        data[newWidth * newY + newX] = mData[mWidth * y + x];
                    }
                }
            }
                break;
            default:
                throw new IllegalArgumentException("Invalid rotatation: "
                        + degrees);
        }
        mData = data;
        setSize(newWidth, newHeight);
        refresh();
        mRotated = (mRotated + degrees) % 360;
    }
    
    public int getRotate(){
        return mRotated;
    }
    
    public boolean isFlipped(){
        return mFlipped;
    }

    public void updateData(String data) {
        int len = data.length();
        if (mARGB) {
            if (len > mData.length * 4) {
                // NOTE: engine can send the data before updating the size!!
                // Therefore, read all of it!  CR1811
                int[] newData = new int[(len+3)/4];
                System.arraycopy(mData,0,newData,0,mData.length);
                mData = newData;
            }
            int j = 0;

            for (int i = 0; i < len; i += 4) {
                int a = data.charAt(i + 0);
                int r = data.charAt(i + 1);
                int g = data.charAt(i + 2);
                int b = data.charAt(i + 3);
                mData[j++] = (a << 24) + (r << 16) + (g << 8) + b;
            }
        } else {
            if (len > mData.length) {
                // NOTE: engine can send the data before updating the size!!
                // Therefore, read all of it!  CR1811
                int[] newData = new int[len];
                System.arraycopy(mData,0,newData,0,mData.length);
                mData = newData;
            }
            for (int i = 0; i < len; i++) {
                int c = data.charAt(i) & 0xFF;
                mData[i] = (255<<24) + (c << 16) + (c << 8) + c;
            }
        }
        mFlipped = false;
        int oldRotation = mRotated;
        mRotated = 0;
        if (oldRotation % 90 != 0 && mWidth != mHeight){
            // Undo rotation
            setSize(mHeight,mWidth);
        }
        else
            refresh();       
    }


    private void refresh() {
        mImageWidget.setImageSize(mWidth,mHeight,32);
        mImageWidget.setPixels(0, 0, mData, 0,mWidth*mHeight);
        mImageWidget.apply();
        mImageWidget.revalidate();
    }
}
