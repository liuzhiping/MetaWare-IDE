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
package com.arc.cdt.debug.seecode.ui.internal.display;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import com.arc.widgets.IColor;
import com.arc.widgets.IImageWidget;

/**
 * Used to build "LED" image that is required by SeeCode.
 */
public class DeferredImage extends DeferredComponent implements IImageWidget, IToolBarItem {
    private SWTImageCanvas mImageCanvas = null;
    private ImageData mImageData = null;
    private PaletteData mPalette;
    private String mID;
    private List<IObserver> mObservers = null;
    private static int sItem = 0;
    
    DeferredImage(int width, int height, int depth){
        mPalette = new PaletteData(0xFF,0xFF00,0xFF0000);
        mID = "Image" + ++sItem;
        setImageSize(width,height,depth);
    }
    @Override
    protected Composite getParentComposite () {
        // @todo Auto-generated method stub
        return null;
    }

    @Override
    public void setImageSize (int width, int height, int depth) {
        mImageData = new ImageData(width,height,depth,mPalette);
    }

    @Override
    public void setScaledSize (int width, int height) {
        // Not used
    }

    @Override
    public int getPixel (int x, int y) {
        if (mImageData != null){
            return mImageData.getPixel(x,y);
        }
        return 0;
    }

    @Override
    public void setPixel (int x, int y, int value) {
        if (mImageData != null){
            mImageData.setPixel(x,y,value);
        }

    }

    @Override
    public void setAlpha (int x, int y, int alpha) {
        if (mImageData != null){
            mImageData.setAlpha(x,y,alpha);
        }

    }

    @Override
    public void setPixels (int x, int y, int[] data, int startIndex, int count) {
        if (mImageData != null){
            mImageData.setPixels(x,y,count,data,startIndex);
        }
    }

    @Override
    public int getImageWidth () {
        if (mImageData != null) return mImageData.width;
        return 0;
    }

    @Override
    public int getImageHeight () {
        if (mImageData != null) return mImageData.height;
        return 0;
    }

    @Override
    public void setColor (IColor[] color) {
        if (mImageData != null) {
            mPalette.isDirect = false;
            RGB rgb[] = new RGB[color.length];
            for (int i = 0; i < color.length; i++){
                rgb[i] = ((Color)color[i].getObject()).getRGB();
            }
            mPalette.colors = rgb;
        }
    }
    
    @Override
    public void setToolTipText(String tip){
        super.setToolTipText(tip);
        if (mImageCanvas != null){
            mImageCanvas.setToolTipText(tip);
        }
    }

    @Override
    public void apply () {
        if (mImageCanvas != null && mImageData != null){
            mImageCanvas.setImageData(mImageData);
        }
    }

    @Override
    public void addToToolBar (IToolBarManager manager) {
        if (mImageData != null){
            ControlContribution c = new MyContributionItem(mID);
            manager.add(c);
        }      
    }
    
    class MyContributionItem extends ControlContribution {

        protected MyContributionItem(String id) {
            super(id);
        }

        @Override
        protected Control createControl(Composite parent) {
            //NOTE: we must use "CCombo" instead of "Combo" because
            // the latter doesn't render correctly under Windows
            // when the toolbar wraps.
            SWTImageCanvas c = new SWTImageCanvas(parent);
            DeferredImage.this.mImageCanvas = c;
            c.setImageData(mImageData);

            c.addDisposeListener(new DisposeListener(){

                @Override
                public void widgetDisposed(DisposeEvent e) {
                    DeferredImage.this.mImageCanvas = null;
                    
                }});
            c.setEnabled(isEnabled());

            if (getToolTipText() != null){
                c.setToolTipText(getToolTipText());
            }
            return c;
        }
        
        @Override
        public boolean isDynamic(){
            return false;
        }
        
        @Override
        public void dispose(){
            if (mImageCanvas != null){
                mImageCanvas.dispose();
            }
        }
            
        @Override
        protected int computeWidth(Control control) {
            return control.computeSize(SWT.DEFAULT,SWT.DEFAULT,true).x;
        }
    }
    
    /**
     * A widget for drawing an image and scaling it appropriately.
     * <P>
     * <B>NOTE:</B> this class is made public so that the GUI tester can reference it.
     * @author David Pickens
     */
    public class SWTImageCanvas extends Canvas implements PaintListener, DisposeListener{
        private static final int HORIZONTAL_INSET = 3;
        private static final int VERTICAL_INSET = 3;
        private Image mImage;

        private int mWidth;

        private int mHeight;

        private boolean mRenderSizeSet = false;

        /**
         * @param parent
         */
        public SWTImageCanvas(Composite parent) {
            super(parent, SWT.NO_BACKGROUND);
            addPaintListener(this);
            addDisposeListener(this);
            addMouseListener(new MouseListener(){

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
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
         */
        @Override
        public void paintControl(PaintEvent e) {
            if (mImage != null) {
                GC gc = e.gc;
                Rectangle entire = getBounds();
                gc.fillRectangle(0,0,entire.width,entire.height);
                Rectangle bounds = mImage.getBounds();
                int x = Math.max(0,(entire.width-bounds.width)/2);
                int y = Math.max(0,(entire.height-bounds.height)/2);
                gc.drawImage(mImage, 0, 0, bounds.width, bounds.height, x, y,
                        mWidth, mHeight);
                gc.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
                gc.drawRectangle(x,y,mWidth,mHeight);
            }
        }

        public void setRenderingSize(int width, int height) {
            mWidth = width;
            mHeight = height;
            mRenderSizeSet = true;
            redraw();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.swt.widgets.Control#computeSize(int, int)
         */
        @Override
        public Point computeSize(int wHint, int hHint, boolean changed) {
            return new Point(mWidth+HORIZONTAL_INSET*2, mHeight+VERTICAL_INSET*2);
        }

        public void setImageData(ImageData imageData) {
            if (mImage != null) {
                mImage.dispose();
            }
            mImage = new Image(getDisplay(), imageData);
            if (!mRenderSizeSet){
                mWidth = imageData.width;
                mHeight = imageData.height;
            }
            redraw();
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
         */
        @Override
        public void widgetDisposed(DisposeEvent e) {
            if (mImage != null){
                mImage.dispose();
                mImage = null;
            }
            
        }
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
        if (mObservers != null) {
            mObservers.remove(observer);
        }
        
    }
    
    private void fireMousePressed(){
        if (mObservers != null && mObservers.size() > 0){
            IObserver[] observers = mObservers.toArray(new IObserver[mObservers.size()]);
            for (IObserver o: observers){
                o.onMousePressed(this);
            }
        }
    }
    @Override
    public void setName (String name) {
        super.setName(name);
        mID = name;
    }
}
