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
package com.arc.widgets;

/**
 * A widget that consists of an image suitably scaled to fit
 * the size of the widget.
 * @author David Pickens
 */
public interface IImageWidget extends IComponent {
    
    public interface IObserver {
        /**
         * Invoked when mouse pressed on image:
         * @param widget the associated widget.
         */
        void onMousePressed(IImageWidget widget);
    }
    
    public void addObserver(IObserver observer);
    public void removeObserver(IObserver observer);
    
    /**
     * Set the size that the image.
     * @param width the number of pixels in width.
     * @param height the height in pixels.
     * @param depth the number of bits per pixel.
     */
    public void setImageSize(int width, int height, int depth);
    
    /**
     * Set the size that the image is to be scaled to.
     */
    public void setScaledSize(int width, int height);
    
    /**
     * Return the pixel at the given position.
     * @param x
     * @param y
     * @return the pixel value at the given position.
     */
    int getPixel(int x, int y);
    
    /**
     * Set value of a pixel.
     * @param x x coordinate
     * @param y y coordinate
     * @param value value of the pixel; as interpreted
     * from depth of image.
     */
    void setPixel(int x, int y, int value);
    
    /**
     * Set alpha value of a pixel: 0 means invisible
     * and 255 means totally opaque.
     * @param x
     * @param y
     * @param alpha
     */
    void setAlpha(int x, int y, int alpha);
    
    /**
     * Set all of the pixels
     * @param x the x position to start.
     * @param y the y position to start.
     * @param count the number of pixels of the data to use.
     */
    void setPixels(int x, int y, int data[], int startIndex, int count);
    
    int getImageWidth();
    
    int getImageHeight();
    
    /**
     * Assuming a modest depth (e.g., 1) redefine the color.
     */
    void setColor(IColor color[]);
    
    /**
     * Apply the pixel updates to the actual image.
     *
     */
    void apply();

}
