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

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;

import com.arc.widgets.IColor;
import com.arc.widgets.IComponent;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IComponentObserver;
import com.arc.widgets.IContainer;
import com.arc.widgets.IFont;

/**
 * Common base class for all SWT widget builders
 */
abstract class Component implements IComponent {

    private IFont mFont;

    private Color mForeground;

    private Color mBackground;

    private GridData mGridData; // if parent uses GridLayout

    private Point mSize; // preferred size

    protected IContainer mParent;

    protected Widget mComponent;

    private boolean mVisible = true;

    private boolean mEnabled = true;

    private String mToolTip;

    private String mName;

    private IComponentMapper mMapper;

    private Object mLayoutData = null;

    private List<IComponentObserver> mObservers;

    Component(IContainer parent, IComponentMapper mapper) {
        mParent = parent;
        mMapper = mapper;
        if (parent != null) {
            Composite p = (Composite) parent.getComponent();
            Layout layout = p.getLayout();
            Integer style = (Integer) p.getData("style");
            if (layout instanceof GridLayout || style != null && style.intValue() == IComponentFactory.COLUMN_STYLE) {
                mGridData = new GridData(/* GridData.FILL_BOTH */);          
                if (style != null) {
                    switch (style.intValue()) {
                        case IComponentFactory.COLUMN_STYLE:
                            mGridData.horizontalAlignment = GridData.FILL;
                            mGridData.verticalAlignment = GridData.BEGINNING;
                            mGridData.grabExcessHorizontalSpace = true;
                            mGridData.grabExcessVerticalSpace = false;
                            break;
                        case IComponentFactory.ROW_STYLE:
                            if (layout instanceof GridLayout)
                                ((GridLayout) layout).numColumns++;
                            break;
                        case IComponentFactory.GRID_STYLE:
                            if (layout instanceof GridLayout)
                                if (((GridLayout) layout).numColumns == 1) {
                                    // If this is really a column, then
                                    // fill things; Eclipse dialogs don't look
                                    // right otherwise.
                                    mGridData.horizontalAlignment = GridData.FILL;
                                    mGridData.grabExcessHorizontalSpace = true;
                                }
                    }
                }
            }
        }
    }

    /**
     * Hook to set GridData if we're wrapping an existing control.
     * @param data
     */
    void setGridData(GridData data) {
        mGridData = data;
    }

    @Override
    public void dispose() {
        if (mComponent != null) {
            Composite parent = null;
            if (mComponent instanceof Control && !mComponent.isDisposed()){
                parent = ((Control)mComponent).getParent();
                mComponent.dispose();
            }
            mComponent = null;
            if (parent != null) {
                parent.layout(true);
            }
        }
    }

    public void build() {
        getComponent();
    }

    @Override
    public IContainer getParent() {
        return mParent;
    }

    protected abstract Widget instantiate();

    protected IComponentMapper getComponentMapper() {
        return mMapper;
    }

    protected Composite getParentComposite() {
        if (mParent instanceof AbstractContainer)
            return ((AbstractContainer)mParent).getComposite();
        return mParent != null?(Composite)mParent.getComponent():null;
    }

    @Override
    public Object getComponent() {
        if (mComponent == null) {
            mComponent = instantiate();
            // If parent is scroll pane then set this as the scroll content
            if (getParentComposite() instanceof ScrolledComposite && mComponent instanceof Control) {
                ((ScrolledComposite)getParentComposite()).setContent((Control)mComponent);
            }
            mMapper.mapComponent(mComponent, this);
            if (mComponent instanceof Control) {
                Control control = (Control) mComponent;
                if (mGridData != null)
                    control.setLayoutData(mGridData);
                if (mFont != null)
                    control.setFont((Font)mFont.getObject());
                if (mBackground != null)
                    control.setBackground(mBackground);
                if (mForeground != null)
                    control.setForeground(mForeground);
                if (!mVisible) {
                    // NOTE: don't set visibility if the initial state is visible.
                    // This will mess up the GUI tester in that it cannot disambiguate
                    // like-named displays in a stack layout. All but the top are invisible.
                    control.setVisible(false);
                }
                control.setEnabled(mEnabled);
                control.setToolTipText(mToolTip);
                if (mSize != null)
                    control.setSize(mSize);
            }
            if (mName != null)
                setName(mName);
        }
        return mComponent;
    }

    @Override
    public void setBorderTitle(String title) {
        throw new IllegalArgumentException("Can't put border on non-container");
    }

    @Override
    public String getBorderTitle() {
        return null;
    }

    /**
     * Set tooltip text for the underlying GUI component.
     * 
     * @param tip
     *            the tooltip text.
     */
    @Override
    public void setToolTipText(String tip) {
        mToolTip = tip;
        if (mComponent instanceof Control)
            ((Control)mComponent).setToolTipText(tip);
    }

    @Override
    public String getToolTipText() {
        if (mComponent instanceof Control)
            return ((Control)mComponent).getToolTipText();
        return mToolTip;
    }

    @Override
    public void setBorder(int border) {
        throw new IllegalArgumentException("Can't put border on non-container");
    }

    // See javadoc in interface
    @Override
    public void setFont(IFont font) {
        mFont = font;
        if (mComponent instanceof Control)
            ((Control)mComponent).setFont((Font)mFont.getObject());
    }
    
    @Override
    public IFont getFont(){
        if (mFont == null){
            mFont = this.getParent() != null?this.getParent().getFont():null;
            if (mFont == null) {
                getComponent();
                Font f = getActualFont();
                if (f != null){
                    mFont = new SWTFont(f);
                }
            }
        }
        return mFont;
    }
    
    protected Font getActualFont(){
        if (mComponent instanceof Control){
            return ((Control)mComponent).getFont();
        }
        return null;
    }

    // See javadoc in interface
    @Override
    public void setForeground(IColor color) {
        mForeground = (Color) color.getObject();
        if (mComponent instanceof Control)
            ((Control)mComponent).setForeground(mForeground);
    }

    // See javadoc in interface
    @Override
    public void setBackground(IColor color) {
        mBackground = (Color) color.getObject();
        if (mComponent instanceof Control)
            ((Control)mComponent).setBackground(mBackground);
    }

    /**
     * Set a name for the component.
     * 
     * @param name
     *            the name.
     */
    @Override
    public void setName(String name) {
        mName = name;
        if (mComponent != null)
            mComponent.setData("name", name);
    }

    /**
     * Return the name of the component.
     */
    @Override
    public String getName() {
        if (mComponent != null)
            return (String) mComponent.getData("name");
        return mName;
    }

    /**
     * Set "enabled" property.
     */
    @Override
    public void setEnabled(boolean v) {
        mEnabled = v;
        if (mComponent instanceof Control)
            ((Control)mComponent).setEnabled(v);
        else if (mComponent instanceof MenuItem){
            ((MenuItem)mComponent).setEnabled(v);
        }
    }

    /**
     * Return whether or not the underlying component is enabled.
     */
    @Override
    public boolean isEnabled() {
        if (mComponent instanceof Control)
            return ((Control)mComponent).getEnabled();
        if (mComponent instanceof MenuItem)
            return ((MenuItem)mComponent).getEnabled();
        return mEnabled;
    }

    /**
     * Return whether or not the underlying component is visible.
     */
    @Override
    public boolean isVisible() {
        if (mComponent instanceof Control)
            return ((Control)mComponent).getVisible();
        return mVisible;
    }

    /**
     * Set "visible" property.
     */
    @Override
    public void setVisible(boolean v) {
        mVisible = v;
        if (mComponent instanceof Control)
            ((Control)mComponent).setVisible(v);
    }

    /**
     * Set the grid position for a grid container. into its grid-based
     * container.
     * 
     * @param rowSpan
     *            the number of cells occupied vertically (typically 1)
     * @param colSpan
     *            the number of cells occupied horizontally (typically 1)
     */
    @Override
    public void setGridSpan(int rowSpan, int colSpan) {
        if (mGridData != null) {
            mGridData.horizontalSpan = colSpan;
            mGridData.verticalSpan = rowSpan;
        } else if (rowSpan != 1 || colSpan != 1)
            throw new IllegalArgumentException("Not a grid layout");
    }

    /**
     * A value between 0 and 1 that determines how the space for component is
     * stretched vertically when container is lengthened. If 0, it doesn't
     * stretch at all. If 1 it stretches maximally. NOTE: a component's space
     * can stretch but not necessarily fill.
     */
    @Override
    public void setVerticalWeight(double weight) {
        if (weight > 0) {
            if (mGridData != null)
                mGridData.grabExcessVerticalSpace = true;
        }
    }

    /**
     * A value between 0 and 1 that determines how the space for component is
     * stretched horizontally when container is lengthened. If 0, it doesn't
     * stretch at all. If 1 it stretches maximally. NOTE: a component's space
     * can stretch but not necessarily fill.
     */
    @Override
    public void setHorizontalWeight(double weight) {
        if (weight > 0) {
            if (mGridData != null)
                mGridData.grabExcessHorizontalSpace = true;
        }
    }

    @Override
    public void setHorizontalAlignment(int align) {
        if (mGridData != null) {
            switch (align) {
                case CENTER:
                    mGridData.horizontalAlignment = GridData.CENTER;
                    break;
                case BEGINNING:
                    mGridData.horizontalAlignment = GridData.BEGINNING;
                    break;
                case END:
                    mGridData.horizontalAlignment = GridData.END;
                    break;
                case FILL:
                    mGridData.horizontalAlignment = GridData.FILL;
                    break;
            }
        }
    }

    @Override
    public void setVerticalAlignment(int align) {
        if (mGridData != null) {
            switch (align) {
                case CENTER:
                    mGridData.verticalAlignment = GridData.CENTER;
                    break;
                case BEGINNING:
                    mGridData.verticalAlignment = GridData.BEGINNING;
                    break;
                case END:
                    mGridData.verticalAlignment = GridData.END;
                    break;
                case FILL:
                    mGridData.verticalAlignment = GridData.FILL;
                    break;
            }
        }
    }

    /**
     * Given hints of width and height, compute the preferred width and height
     * of this component.
     * <P>
     * More specifically, given a width, what is the preferred height? And,
     * given a height, what is the preferred width?
     * <P>
     * If there are no constraints, then the hints should be zero.
     * 
     * @param wHint
     *            the constrained width, or 0.
     * @param hHint
     *            the constrained height, or 0.
     * @return the preferred size
     */
    @Override
    public Dimension computeSize(int wHint, int hHint) {
        if (wHint <= 0)
            wHint = SWT.DEFAULT;
        if (hHint <= 0)
            hHint = SWT.DEFAULT;
        Point p = ((Control) getComponent()).computeSize(wHint, hHint);
        Dimension d = new Dimension(p.x, p.y);
        return d;
    }
    
    @Override
    public Dimension getSize(){
        Point p = ((Control)getComponent()).getSize();
        return new Dimension(p.x,p.y);
    }

    /**
     * Set preferred size.
     * 
     * @param width
     *            the preferred width
     * @param height
     *            the preferred height
     */
    @Override
    public void setPreferredSize(int width, int height) {
        mSize = new Point(width, height);
        if (mComponent instanceof Control) {
            ((Control)mComponent).setSize(mSize);
        }
        if (mGridData != null) {
            mGridData.widthHint = width;
            mGridData.heightHint = height;
        }
    }

    /**
     * Set margins around this component. A margin is the number of pixels of
     * space that is appear around this component.
     * 
     * @param top
     *            the number of pixels to appear at top.
     * @param left
     *            the number of pixels to appear at left.
     * @param bottom
     *            the number of pixels to appear at bottom.
     * @param right
     *            the number of pixels to appear at right.
     */
    @Override
    public void setMargins(int top, int left, int bottom, int right) {
        if (mGridData != null) {
            mGridData.horizontalIndent = left;
        }
    }
    
    /* (non-Javadoc)
     * @see com.arc.widgets.IComponent#convertToScreenPoint(int, int)
     */
    @Override
    public java.awt.Point convertToScreenPoint(int x, int y) {
        Control w = (Control)getComponent();
        Point p = w.toDisplay(x,y);
        return new java.awt.Point(p.x,p.y);
    }
    
    @Override
    public int getWidth(){
        if (mComponent instanceof Control){
            Point size = ((Control)mComponent).getSize();
            return size.x;
        }
        return 0;
    }
    
    @Override
    public int getHeight(){
        if (mComponent instanceof Control){
            Point size = ((Control)mComponent).getSize();
            return size.y;
        }
        return 0;
    }
    
    @Override
    public void repaint(){
        ((Control)getComponent()).redraw();
    }
    
    private static void revalidate(Control c){

        Composite parent = c.getParent();
        
        if (parent != null){
            Point actual = c.getSize();
            Point preferredSize = c.computeSize(SWT.DEFAULT,SWT.DEFAULT,true);
            if (!preferredSize.equals(actual)){
                revalidate(parent);
                parent.layout(true);
            }
        }
    }
    
    @Override
    public void revalidate(){
        Control c = (Control)getComponent();
        revalidate(c);
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.IComponent#getLayoutData()
     */
    @Override
    public Object getLayoutData() {
        return mLayoutData;
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.IComponent#setBounds(int, int, int, int)
     */
    @Override
    public void setBounds(int x, int y, int width, int height) {
        Control c = (Control)getComponent();
        c.setBounds(x,y,width,height);
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.IComponent#setLayoutData(java.lang.Object)
     */
    @Override
    public void setLayoutData(Object object) {
        mLayoutData = object;

    }

    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param observer
     */
    @Override
    public void addObserver (IComponentObserver observer) {
        if (mObservers == null){
            mObservers = new ArrayList<IComponentObserver>();
            Control c = (Control)getComponent();
            c.addControlListener(new ControlListener(){

                @Override
                public void controlMoved (ControlEvent e) {
                }

                @Override
                public void controlResized (ControlEvent e) {
                    notifyObserversOfSizeChange();
                    
                }});
        }
        mObservers.add(observer);     
    }

    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param observer
     */
    @Override
    public void removeObserver (IComponentObserver observer) {
        if (mObservers != null){
            mObservers.remove(observer);
        }
        
    }

    /**
     * Notify observers of size change
     */
    protected void notifyObserversOfSizeChange () {
        for (IComponentObserver o: mObservers){
            o.onSizeChange(Component.this);
        }
    }
}
