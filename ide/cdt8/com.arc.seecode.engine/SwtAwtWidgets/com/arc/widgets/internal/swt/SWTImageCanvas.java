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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

/**
 * A widget for drawing an image and scaling it appropriately.
 * <P>
 * <B>NOTE:</B> this class is made public so that the GUI tester can reference it.
 * 
 * @author David Pickens
 */
public class SWTImageCanvas extends Canvas implements PaintListener, DisposeListener{

    private Image mImage;

    private int mWidth;

    private int mHeight;

    private boolean mRenderSizeSet = false;

    /**
     * @param parent
     */
    public SWTImageCanvas(Composite parent) {
        super(parent, SWT.NO_BACKGROUND | SWT.DOUBLE_BUFFERED);
        addPaintListener(this);
        addDisposeListener(this);
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
            gc.drawImage(mImage, 0, 0, bounds.width, bounds.height, 0, 0,
                    mWidth, mHeight);
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
        return new Point(mWidth, mHeight);
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
