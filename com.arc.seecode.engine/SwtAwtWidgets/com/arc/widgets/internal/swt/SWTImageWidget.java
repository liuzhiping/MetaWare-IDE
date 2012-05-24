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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

import com.arc.widgets.IColor;
import com.arc.widgets.IContainer;
import com.arc.widgets.IImageWidget;

/**
 * @author David Pickens
 */
class SWTImageWidget extends Component implements IImageWidget {
    private ImageData mImageData = new ImageData(10, 10, 32, new PaletteData(
            0xFF0000, 0xFF00, 0xFF));
    
    private int mWidth = 0;
    private int mHeight = 0;
    
    List<IObserver> mObservers = null;

    //private boolean mResized;

    /**
     * @param parent
     * @param mapper
     */
    public SWTImageWidget(IContainer parent, SWTImage image,
            IComponentMapper mapper) {
        super(parent, mapper);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.swt.Component#instantiate()
     */
    @Override
    protected Widget instantiate() {
        Control w = new SWTImageCanvas(this.getParentComposite());
        w.addMouseListener(new MouseListener(){

            @Override
            public void mouseDoubleClick (MouseEvent e) {
               
                
            }

            @Override
            public void mouseDown (MouseEvent e) {
                
                
            }

            @Override
            public void mouseUp (MouseEvent e) {
               fireMousePressed();
                
            }});
        return w;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IImageWidget#setSize(int, int)
     */
    @Override
    public void setImageSize(int width, int height, int depth) {
        ImageData i = new ImageData(width,height,depth,mImageData.palette);
        System.arraycopy(mImageData.data,0,i.data,0,Math.min(mImageData.data.length,i.data.length));
        mImageData = i;  
    }
    
    @Override
    public void setScaledSize(int width, int height) {
        if (width != mWidth || height != mHeight){
            //mResized = true;
            mWidth = width;
            mHeight = height;
        }
        SWTImageCanvas c = (SWTImageCanvas) getComponent();
        c.setRenderingSize(width, height);
    }
    
    @Override
    public void setColor(IColor color[]){
        mImageData.depth = color.length;
        mImageData.palette.isDirect = false;
        RGB colors[] = new RGB[color.length];
        for (int i = 0; i < colors.length; i++){
            colors[i] = ((Color)color[i].getObject()).getRGB();
        }
        mImageData.palette.colors = colors;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IImageWidget#getPixel(int, int)
     */
    @Override
    public int getPixel(int x, int y) {
        return mImageData.getPixel(x, y);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IImageWidget#setPixel(int, int, int)
     */
    @Override
    public void setPixel(int x, int y, int value) {
        mImageData.setPixel(x,y,value);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IImageWidget#setAlpha(int, int, int)
     */
    @Override
    public void setAlpha(int x, int y, int alpha) {
        mImageData.setAlpha(x,y,alpha);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IImageWidget#setPixels(int, int, int[], int, int)
     */
    @Override
    public void setPixels(int x, int y, int[] data, int startIndex, int count) {
        mImageData.setPixels(x,y,count,data,startIndex);
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IImageWidget#apply()
     */
    @Override
    public void apply() {
        SWTImageCanvas c = (SWTImageCanvas)getComponent();
        c.setImageData(mImageData); 
        revalidate();
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IImageWidget#getImageWidht()
     */
    @Override
    public int getImageWidth() {
        return mImageData.width;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IImageWidget#getImageHeight()
     */
    @Override
    public int getImageHeight() {
        return mImageData.height;
    }

    @Override
    public void addObserver (IObserver observer) {
        if (mObservers == null){
            mObservers = new ArrayList<IObserver>();
        }
        mObservers.add(observer);
    }

    @Override
    public void removeObserver (IObserver observer) {
        if (mObservers != null)
            mObservers.remove(observer);
        
    }
    
    /**
     * Called when the mouse is pressed over this image.
     */
    private void fireMousePressed(){
        if (mObservers != null && mObservers.size() > 0){
            IObserver[] observers = mObservers.toArray(new IObserver[mObservers.size()]);
            for (IObserver o: observers){
                o.onMousePressed(this);
            }
        }
    }
}
