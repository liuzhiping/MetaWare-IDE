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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

import com.arc.widgets.IColor;
import com.arc.widgets.ITextCanvas;
import com.arc.widgets.ITextCanvas.IObserver;

/**
 * The icon column in the {@link SWTTextCanvas}class.
 * 
 * @author David Pickens
 */
class IconColumn extends Canvas {
    // List<Image>
    private List<Image> mImages = new ArrayList<Image>();

    // List<Point>
    private List<Point> mPoints = new ArrayList<Point>();

    private int mLineHeight = 20;

    private List<IObserver> mObservers = new ArrayList<IObserver>();

    private ITextCanvas mCanvas;
    
    private int mLineOnLeft = 0;
    private Color mLineColorOnLeft = null;
    private int mLineOnRight = 0;
    private Color mLineColorOnRight = null;


    /**
     * @param parent
     * @parma lineHeight height of each line that were augmenting.
     */
    public IconColumn(Composite parent, ITextCanvas canvas) {
        super(parent, SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE);
        mCanvas = canvas;
        setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        addPaintListener(new PaintListener() {

            @Override
            public void paintControl(PaintEvent e) {
                paint(e);

            }
        });
        doMouseListener();
    }

    private void doMouseListener() {
        this.addMouseListener(new MouseListener() {
            private long _lastClickTime = 0;

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                fireEvent(DOUBLE_CLICK, e.y / mLineHeight);
            }

            @Override
            public void mouseDown(MouseEvent e) {
            }

            @Override
            public void mouseUp(MouseEvent e) {
                long now = System.currentTimeMillis();
                if (now - _lastClickTime > 300) {
                    _lastClickTime = System.currentTimeMillis();
                    fireEvent(SINGLE_CLICK, e.y / mLineHeight);
                }
            }
        });
    }

    public void addObserver(IObserver o) {
        synchronized (mObservers) {
            mObservers.add(o);
        }
    }

    public void removeObserver(IObserver o) {
        synchronized (mObservers) {
            mObservers.remove(o);
        }
    }
    private static final int DOUBLE_CLICK = 0;
    private static final int SINGLE_CLICK = 1;

    private void fireEvent(int event, int line) {
        IObserver o[];
        synchronized (mObservers) {
            o = mObservers.toArray(new IObserver[mObservers
                    .size()]);
        }
        for (int i = 0; i < o.length; i++) {
            switch(event){
                case DOUBLE_CLICK:
                    o[i].onDoubleClick(mCanvas, line, -1);
                    break;
                case SINGLE_CLICK:
                    o[i].onSelected(mCanvas,line,-1,line);
                    break;
            }
        }
    }

    void setLineHeight(int h) {
        mLineHeight = h;
    }

    public void setIcon(Image image, int x, int y) {
        mImages.add(image);
        mPoints.add(new Point(x, y));
    }

    /**
     * Clear all icons
     *  
     */
    public void clearIcons() {
        mImages.clear();
        mPoints.clear();
        redraw();
    }

    /**
     * Clear icons at a particular y position.
     * 
     * @param y
     *            the y coordinate whose icons are being cleared
     */
    public void clearIcons(int y) {

        int cnt = mPoints.size();
        for (int i = 0; i < cnt;) {
            Point p = mPoints.get(i);
            if (p.y == y) {
                mPoints.remove(i);
                Image image = mImages.remove(i);
                Rectangle size = image.getBounds();
                redraw(p.x, p.y, size.width, mLineHeight, true);
                cnt--;
            } else
                i++;
        }

    }
    
    public boolean hasIcons(){
        return mImages.size() > 0;
    }

    private void paint(PaintEvent event) {
        GC gc = event.gc;
        gc.fillRectangle(event.x, event.y, event.width, event.height);
        int cnt = mImages.size();
        for (int i = 0; i < cnt; i++) {
            Point p = mPoints.get(i);
            if (p.x >= event.x && p.x < event.x + event.width && p.y >= event.y
                    && p.y < event.y + event.height) {
                Image image =  mImages.get(i);
                Rectangle bounds = image.getBounds();
                int y = p.y + (mLineHeight - bounds.height) / 2;
                gc.drawImage(image, p.x, y);
            }
        }
        if (mLineOnLeft != 0 && mLineColorOnLeft != null){
            int save = gc.getLineWidth();
            Color saveColor = gc.getForeground();
            gc.setForeground(mLineColorOnLeft);
            gc.setLineWidth(mLineOnLeft);
            gc.drawLine(0,0,0,getSize().y);
            gc.setLineWidth(save);
            gc.setForeground(saveColor);               
        }
        if (mLineOnRight != 0 && mLineColorOnRight != null){
            int save = gc.getLineWidth();
            Color saveColor = gc.getForeground();
            gc.setForeground(mLineColorOnRight);
            gc.setLineWidth(mLineOnRight);
            gc.drawLine(getSize().x-1,0,getSize().x-1,getSize().y);
            gc.setLineWidth(save);
            gc.setForeground(saveColor);               
        }
    }

    
    @Override
    public Point computeSize (int wHint, int hHint, boolean changed) {
        int highestY = 0;
        int lowestY = Integer.MAX_VALUE;
        int maxWidth = 0;
        int cnt = mImages.size();
        for (int i = 0; i < cnt; i++) {
            Point p = mPoints.get(i);
            highestY = Math.max(p.y,highestY);
            lowestY = Math.min(p.y,lowestY);
            Rectangle bounds = mImages.get(i).getBounds();
            maxWidth = Math.max(maxWidth,bounds.width + p.x);
        }
        return new Point(maxWidth,highestY-lowestY);
    }
    
    void setVerticalLine(boolean onLeft, Object color, int thickness){
        Color realColor = color==null?null:color instanceof IColor?(Color)((IColor)color).getObject():(Color)color;
        if (onLeft){
            mLineOnLeft = thickness;
            mLineColorOnLeft = realColor;
        }
        else {
            mLineOnRight = thickness;
            mLineColorOnRight = realColor;          
        }
    }
    
    void recordState(PrintStream out){
        int cnt = mImages.size();
        for (int i = 0; i < cnt; i++){
            out.println("    <icon x=\"" + mPoints.get(i).x + "\" y=\"" +
                mPoints.get(i).y + "\">");
            Rectangle bounds = mImages.get(i).getBounds();
            out.println("        <image width=\"" + bounds.width + "\" height=\"" +
                bounds.height + "\">");
            // Only record  small subset of pixels.
            byte rgb[] = mImages.get(i).getImageData().data;
            int delta = rgb.length / 10;
            out.print("            ");
            for (int j = 0; j < rgb.length; j += delta){
                out.print(" ");
                out.print(Integer.toHexString(rgb[j] & 0xFF));
            }
            out.println("        </image>");           
        }
    }

}
