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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Widget;

import com.arc.widgets.IComponent;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;
import com.arc.widgets.ILayoutManager;

/**
 * A builder for an SWT container (Composite).
 */

class Container extends AbstractContainer  {
    private Layout mLayout = null;
    private int mStyle;
    private int mFlags;

    Container(IContainer parent, int style, int columns, IComponentMapper mapper) {
        super(parent,mapper);
        mStyle = style;
        mFlags = 0;
        switch (mStyle) {
            case IComponentFactory.GRID_STYLE :
                {
                    if (columns < 1)
                        throw new IllegalArgumentException("Grid width must be > 0");
                    GridLayout gl = new GridLayout(columns, false);
                    mLayout = gl;
                    gl.horizontalSpacing = 3;
                    gl.verticalSpacing = 3;
                }
                break;
            case IComponentFactory.ROW_STYLE :
                {
//                RowLayout r = new RowLayout();
//                r.wrap = false;
//                r.type = SWT.HORIZONTAL;
//                r.fill = true;
//                r.justify = true;
//                mLayout = r;

                    GridLayout gl = new GridLayout(0, false);
                    // Column count will be increased each
                    // time a component is added.
                    mLayout = gl;
                    gl.horizontalSpacing = 3;
                }
                break;
            case IComponentFactory.COLUMN_STYLE : {
//                RowLayout r = new RowLayout();
//                r.wrap = false;
//                r.type = SWT.VERTICAL;
//                r.fill = true;
//                r.justify = true;
                mLayout = new MyColumnLayout();
//                    GridLayout gl = new GridLayout(1, false);
//                    gl.verticalSpacing = 3;
//                    mLayout = gl;
                }
                break;
            case IComponentFactory.FLOW_STYLE :
                {
                    RowLayout r = new RowLayout();
                    r.wrap = true;
                    r.type = SWT.HORIZONTAL;
                    mLayout = r;
                    break;
                }
            case IComponentFactory.NO_STYLE :
            	break;
            default :
                throw new IllegalArgumentException(
                    "Unknown container style: " + mStyle);
        }
    }
    
    Container(IContainer parent, ILayoutManager layout, IComponentMapper mapper) {
        super(parent,mapper);
        mStyle = IComponentFactory.NO_STYLE;
        mFlags = 0;
        mLayout = new SWTLayout(layout,this);
    }
    
    void setFlags(int flags){
        mFlags = flags;
    }

    @Override
    protected Widget instantiate() {
        Composite c;
        if (mBorder != 0) {
            Group g;
            c = g = new Group((Composite)mParent.getComponent(), mBorder|SWT.NO_RADIO_GROUP);
            g.setForeground(g.getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE));
            if (mBorderTitle != null) {
                g.setText(mBorderTitle);
            }
        }
        else {
            c = new Composite((Composite)mParent.getComponent(), mFlags);
            if (!mMarginsSet && mLayout instanceof GridLayout){
                ((GridLayout)mLayout).marginWidth = 0;
                ((GridLayout)mLayout).marginHeight = 0;
            }
        }
        setStyleFor(c);
        return c;
    }

    /**
	 * @param c
	 */
	protected void setStyleFor(Composite c) {
		if (mLayout != null)
		    c.setLayout(mLayout);
        c.setData("style", new Integer(mStyle));
	}

	/**
     * Set the number of pixels between horizontal components in this container.
     */
    @Override
    public void setHorizontalSpacing(int pixels) {
        if (mLayout instanceof RowLayout) {
            RowLayout r = (RowLayout) mLayout;
            if (r.type == SWT.HORIZONTAL)
                r.spacing = pixels;
        }
        else if (mLayout instanceof GridLayout)
             ((GridLayout) mLayout).horizontalSpacing = pixels;
    }
    /**
     * Set the number of pixels between vertical components in this container.
     */
    @Override
    public void setVerticalSpacing(int pixels) {
        if (mLayout instanceof RowLayout) {
            RowLayout r = (RowLayout) mLayout;
            if (r.type == SWT.VERTICAL)
                r.spacing = pixels;
        }
        else if (mLayout instanceof GridLayout)
             ((GridLayout) mLayout).verticalSpacing = pixels;
        else if (mLayout instanceof MyColumnLayout)
            ((MyColumnLayout)mLayout).setVerticalSpacing(pixels);
    }

    @Override
    public void setMargins(int top, int left, int bottom, int right) {
        mMarginsSet = true;
        if (mLayout instanceof GridLayout) {
            GridLayout g = (GridLayout) mLayout;
            g.marginHeight = Math.max(top, bottom);
            g.marginWidth = Math.max(left, right);
        }
        else if (mLayout instanceof RowLayout) {
            RowLayout r = (RowLayout) mLayout;
            r.marginTop = top;
            r.marginBottom = bottom;
            r.marginLeft = left;
            r.marginRight = right;
        }
    }

    @Override
    public void setBorderTitle(String s) {
        mBorderTitle = s;
        if (mBorder == 0)
            mBorder = SWT.SHADOW_ETCHED_IN;
        if (mComponent != null)
             ((Group) mComponent).setText(s);
    }

    @Override
    public String getBorderTitle() {
        return mBorderTitle;
    }

    @Override
    public void setBorder(int style) {
        switch (style) {
            case NO_BORDER :
                mBorder = 0;
                break;
            case ETCHED_BORDER :
                mBorder = SWT.SHADOW_ETCHED_IN;
                break;
            case BEVEL_IN_BORDER :
                mBorder = SWT.SHADOW_IN;
                break;
            case BEVEL_OUT_BORDER :
                mBorder = SWT.SHADOW_OUT;
                break;
            default :
                throw new IllegalArgumentException(
                    "bad border style: " + style);
        }
    }

    @Override
    public void layout() {
        ((Composite) getComponent()).layout();
    }

    private String mBorderTitle;
    private int mBorder = IComponent.NO_BORDER;
    private boolean mMarginsSet;
    /**
     * @see com.arc.widgets.IContainer#getContainerStyle()
     */
    @Override
    public int getContainerStyle() {
        return mStyle;
    }
}

/**
 * A column layout that doesn't have the anomilies of GridLayout or
 * RowLayout.
 * 
 *<P>
 * Since guihili can be very large, there is a performance bottleneck. So we cache stuff readily.
 * For this reason, we assume that each layout instance applies to a single container.
 */
class MyColumnLayout extends Layout {
    private static int DEFAULT_VERTICAL_SPACING = 3;
    
    private int verticalSpacing = DEFAULT_VERTICAL_SPACING;
    
    private Composite fComposite = null;
    private Point[] fChildSizeCache = null; // for each child.
    private Point fSizeCache = null;
    private int fCachedWHint = SWT.DEFAULT;
    private boolean fHeightDependsOnWidth = false;
    
    MyColumnLayout(){
        this(DEFAULT_VERTICAL_SPACING);
    }
    
    MyColumnLayout(int verticalSpacing){
        this.verticalSpacing = verticalSpacing;
    }
    
    public void setVerticalSpacing(int v){
        verticalSpacing = v;
    }
    
    @Override
    protected Point computeSize (Composite composite, int wHint, int hHint, boolean flushCache) {
        // The Eclipse Launch config dialog is killing us in performance because it redraws
        // the universe when anything changes. Ignore the "flushCache" unless the number of
        // children changed.
        Control children[] = composite.getChildren();

        if (!flushCache) {
            if (composite != fComposite || fSizeCache == null) {
                fComposite = composite;
                flushCache = true;
            }
        }
        if (!flushCache) {
       
            if (fChildSizeCache.length != children.length) {
                flushCache = true;
            }
            if (wHint != fCachedWHint && fHeightDependsOnWidth) {
                flushCache = true;
            }
        }           
        if (!flushCache) {
            return fSizeCache;
        }
        
        if (fChildSizeCache == null || fChildSizeCache.length != children.length)
            fChildSizeCache = new Point[children.length];
        int width = 0;
        int height = 0;
        boolean wrappingControls = false;
        fHeightDependsOnWidth = false;
        //<HACK>cr93684
        //For some reason, "Group" doesn't pass the actual client area width, but rather the
        // width of the entire component. We discovered it to off by 6 pixels. So we correct it here.
        // Otherwise, wrapped text doesn't render correctly.
        if (wHint != SWT.DEFAULT && wHint > 6 && composite instanceof Group) wHint -= 6;
        //</HACK>
        for (int i = 0; i < children.length; i++) {
            Point size = children[i].computeSize(wHint,SWT.DEFAULT,false/*flushCache*/);
            fChildSizeCache[i] = size;
            // If widget is a label that is to "wrap", then don't use
            // its width unless it is the only one.
            if (!isHeightDependentOnWidth(children[i]) /*|| children.length == 1*/) {
                width = Math.max(width,size.x);
            }
            else {
                fHeightDependsOnWidth = true;
                wrappingControls = true;
            }
            height += size.y;
        }
        if (children.length > 1) {
            height += verticalSpacing*(children.length-1);
        }
        if (wrappingControls){
            // Some of the controls wrap, so their height depends on their
            // width. But if we have a panel that consist of nothing but wrappable string, then
        	// the width will be narrow. So, arbitrary choose 200.
            if (wHint == SWT.DEFAULT){
                return computeSize(composite,Math.max(width,200),hHint,flushCache);
            }
            width = Math.max(width,wHint);
        }
        fSizeCache =  new Point(width,height);
        this.fCachedWHint = wHint;
        return fSizeCache;
    }

    /**
     * Does the given control's height somehow depend on its width?
     * Returns true for, say, a wrappable label, or a composite that has a wrappable label.
     * @param c the control
     * @return whether or not the height is dependent on the width.
     */
    private boolean isHeightDependentOnWidth (Control c) {
        if  ((c.getStyle() & SWT.WRAP) != 0) return true;
        if (c instanceof Composite){
            Composite composite = (Composite)c;
            if (composite.getLayout() instanceof MyColumnLayout) {
                Control kids[] = composite.getChildren();
                for (Control kid: kids){
                    if (isHeightDependentOnWidth(kid)) return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void layout (Composite composite, boolean flushCache) {
        //System.out.println("MyColumnLayout::layout(flushCache=" + flushCache + ")");
        boolean resetCache = flushCache;
        Control children[] = composite.getChildren();
        if (children.length == 0) return;
        if (fChildSizeCache == null || fChildSizeCache.length != children.length || fComposite != composite) {
            resetCache = true;
            fChildSizeCache = new Point[children.length];
            fComposite = composite;
        }
        Rectangle rect = composite.getClientArea();
        int y = rect.y;
        int lastY = y + rect.height;
        List<Control> stretchedControls = new ArrayList<Control>();
        int widths[] = new int[children.length];
        int heights[] = new int[children.length];
       
        for (int i = 0; i < children.length; i++) {
            Control c = children[i];
            if (c.getLayoutData() instanceof GridData){
                GridData gd = (GridData)c.getLayoutData();
                if (gd.grabExcessVerticalSpace || gd.verticalAlignment == SWT.FILL) 
                    stretchedControls.add(c);
            }
            Point size;
            if (resetCache) {
                size = c.computeSize(rect.width,SWT.DEFAULT,false);
                fChildSizeCache[i] = size;
            }
            else {
                size = fChildSizeCache[i];
            }
            heights[i] = size.y;
            widths[i] = size.x;
            y += size.y;
        }
        y += (children.length-1)* verticalSpacing;
        
        int stretchAmountPerControl = 0;
        if (y < rect.y + rect.height && stretchedControls.size() > 0){
            stretchAmountPerControl = (rect.y+rect.height-y) / stretchedControls.size();        
        }
        
        y = rect.y;
        
        for (int i = 0; i < children.length; i++) {
            Control c = children[i];
            // We use the GridData...
            int hAlign = GridData.BEGINNING;
            int vAlign = GridData.BEGINNING;
            if (c.getLayoutData() instanceof GridData){
                GridData gd = (GridData)c.getLayoutData();
                hAlign = gd.horizontalAlignment;
                vAlign = gd.verticalAlignment;
            }
            int h = heights[i];
            if (vAlign == GridData.FILL)
                h += stretchAmountPerControl;
            else if (vAlign == GridData.END && stretchedControls.contains(c)){
                y += stretchAmountPerControl; 
            }
            else if (vAlign == GridData.CENTER && stretchedControls.contains(c)){
                y += stretchAmountPerControl/2;    
                h += stretchAmountPerControl/2;
            }
            int x = rect.x;
            int width = widths[i];
            if (hAlign == GridData.END){
                int xx = rect.x + rect.height - widths[i];
                if (xx > x) x = xx;
            }
            else if (hAlign == GridData.CENTER){
                if (widths[i] < rect.width){
                    x = rect.x + (rect.width-widths[i])/2;
                }
            }
            else if (hAlign == GridData.FILL){
                width = rect.width;
            }
            if (y + h >= lastY){
                h = lastY - y;
                if (h > 0)
                    c.setBounds(x,y,width,h);
                break;
            }
            c.setBounds(x,y,width,h);
            y += h + verticalSpacing;
        }       
    }

    @Override
    protected boolean flushCache (Control control) {
        this.fSizeCache = null;
        return true;
    }   
}
