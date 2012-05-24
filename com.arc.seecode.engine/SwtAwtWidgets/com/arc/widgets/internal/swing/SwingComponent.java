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

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import com.arc.widgets.IColor;
import com.arc.widgets.IComponent;
import com.arc.widgets.IComponentObserver;
import com.arc.widgets.IContainer;
import com.arc.widgets.IFont;

abstract class SwingComponent implements IComponent {


    private IContainer mParent;

    protected Component mComponent;

    private GridBagConstraints mConstraints;

    private String mName;

    private Border mBorder;

    private String mBorderTitle;

    private Color mForeground;

    private Color mBackground;

    private Dimension mPreferredSize;

    private boolean mEnabled = true;

    private boolean mVisible = true;

    private IFont mFont;

    private String mToolTip;

    private int mVertAlign = CENTER;

    private int mHorizAlign = BEGINNING;

    private Border mEmptyBorder;

    private IComponentMapper mMapper;

    SwingComponent(IContainer parent, IComponentMapper mapper) {
        mParent = parent;
        mMapper = mapper;
        mConstraints = new GridBagConstraints();
    }

    protected IComponentMapper getComponentMapper() {
        return mMapper;
    }

    @Override
    public IContainer getParent() {
        return mParent;
    }

    private static byte sAnchor[][] = new byte[4][4];
    static {
        sAnchor[BEGINNING][BEGINNING] = GridBagConstraints.NORTHWEST;
        sAnchor[BEGINNING][CENTER] = GridBagConstraints.WEST;
        sAnchor[BEGINNING][END] = GridBagConstraints.SOUTHWEST;
        sAnchor[BEGINNING][FILL] = GridBagConstraints.WEST;
        sAnchor[CENTER][BEGINNING] = GridBagConstraints.NORTH;
        sAnchor[CENTER][CENTER] = GridBagConstraints.CENTER;
        sAnchor[CENTER][END] = GridBagConstraints.SOUTH;
        sAnchor[CENTER][FILL] = GridBagConstraints.CENTER;
        sAnchor[END][BEGINNING] = GridBagConstraints.NORTHEAST;
        sAnchor[END][CENTER] = GridBagConstraints.EAST;
        sAnchor[END][END] = GridBagConstraints.SOUTHEAST;
        sAnchor[END][FILL] = GridBagConstraints.SOUTH;
        sAnchor[FILL][BEGINNING] = GridBagConstraints.NORTHWEST;
        sAnchor[FILL][CENTER] = GridBagConstraints.WEST;
        sAnchor[FILL][END] = GridBagConstraints.SOUTHWEST;
        sAnchor[FILL][FILL] = GridBagConstraints.WEST;
    }

    @Override
    public Object getComponent() {
        if (mComponent == null) {
            mComponent = instantiate();
            mMapper.mapComponent(mComponent, this);
            doToolTip();
            doBorder();
            doFont();
            doForeground();
            doBackground();
            doPreferredSize();
            doEnabled();
            doVisible();
            doName();
            if (mParent != null) {
                Container parent = (Container) mParent.getComponent();
                if (parent.getLayout() instanceof GridBagLayout) {
                    mConstraints.anchor = sAnchor[mHorizAlign][mVertAlign];
                    if (mHorizAlign == FILL) {
                        mConstraints.fill = mVertAlign == FILL ? GridBagConstraints.BOTH
                                : GridBagConstraints.HORIZONTAL;
                    } else if (mVertAlign == FILL)
                        mConstraints.fill = GridBagConstraints.VERTICAL;
                    parent.add(mComponent, mConstraints);
                } else if (parent.getLayout() instanceof CardLayout) {
                    parent.add(mComponent, mComponent.getName());
                } else
                    parent.add(mComponent);
            }
        }
        return mComponent;
    }

    /**
     * Instantiate the component.
     */
    abstract protected Component instantiate();

    @Override
    public void setBorderTitle(String title) {
        mBorderTitle = title;
        doBorder();
    }

    @Override
    public void setToolTipText(String tip) {
        mToolTip = tip;
        doToolTip();
    }

    @Override
    public String getToolTipText() {
        if (mComponent instanceof JComponent)
            return ((JComponent) mComponent).getToolTipText();
        return mToolTip;

    }

    @Override
    public void dispose() {
        if (mComponent != null) {
            mComponent.getParent().remove(mComponent);
            mMapper.unmapComponent(mComponent);
        }
    }

    private static final Border sEtchedBorder = new EtchedBorder();

    private static final Border sLoweredBevelBorder = new BevelBorder(
            BevelBorder.LOWERED);

    private static final Border sRaisedBevelBorder = new BevelBorder(
            BevelBorder.RAISED);

    private Object mLayoutData;

    private ArrayList<IComponentObserver> mObservers;

    @Override
    public void setBorder(int border) {
        Border b = null;
        switch (border) {
            case NO_BORDER:
                break;
            case ETCHED_BORDER:
                b = sEtchedBorder;
                break;
            case BEVEL_IN_BORDER:
                b = sLoweredBevelBorder;
                break;
            case BEVEL_OUT_BORDER:
                b = sRaisedBevelBorder;
                break;
            default:
                throw new IllegalArgumentException("Bad border style");
        }
        if (mBorder != null) // Empty border exists?
            mBorder = new CompoundBorder(b, mBorder);
        else
            mBorder = b;
        doBorder();
    }

    @Override
    public void setMargins(int top, int left, int bottom, int right) {
        if (top != 0 || left != 0 || bottom != 0 || right != 0)
            mEmptyBorder = new EmptyBorder(top, left, bottom, right);
    }

    @Override
    public String getBorderTitle() {
        return mBorderTitle;
    }

    @Override
    public void setFont(IFont font) {
        mFont = font;
        doFont();
    }

    @Override
    public void setForeground(IColor color) {
        mForeground = (Color) color.getObject();
        doForeground();
    }

    @Override
    public void setBackground(IColor color) {
        mBackground = (Color) color.getObject();
        doBackground();
    }

    @Override
    public void setPreferredSize(int width, int height) {
        mPreferredSize = new Dimension(width, height);
        doPreferredSize();
    }

    @Override
    public void setName(String name) {
        mName = name;
        doName();
    }

    /**
     * Return the name of the component.
     */
    @Override
    public String getName() {
        if (mComponent != null)
            return mComponent.getName();
        return mName;
    }

    @Override
    public void setEnabled(boolean v) {
        mEnabled = v;
        doEnabled();
    }

    @Override
    public boolean isEnabled() {
        if (mComponent != null)
            return mComponent.isEnabled();
        return mEnabled;
    }

    @Override
    public boolean isVisible() {
        if (mComponent != null)
            return mComponent.isVisible();
        return mVisible;
    }

    @Override
    public void setVisible(boolean v) {
        mVisible = true;
        doVisible();
    }

    /**
     * Set the grid position for a grid container. into its grid-based
     * container.
     * 
     * @param rowSpan
     *            the number of cells occupied horizonally (typically 1)
     * @param colSpan
     *            the number of cells occupied vertically (typically 1)
     */
    @Override
    public void setGridSpan(int rowSpan, int colSpan) {
        mConstraints.gridwidth = rowSpan;
        mConstraints.gridheight = colSpan;
    }

    @Override
    public void setVerticalWeight(double weight) {
        mConstraints.weighty = weight;
    }

    @Override
    public void setHorizontalWeight(double weight) {
        mConstraints.weightx = weight;
    }

    @Override
    public void setHorizontalAlignment(int position) {
        mHorizAlign = position;
    }

    @Override
    public void setVerticalAlignment(int position) {
        mVertAlign = position;
    }

    @Override
    public Dimension computeSize(int wHint, int hHint) {
        if (mComponent != null)
            return mComponent.getPreferredSize();
        return mPreferredSize;
    }
    
    @Override
    public Dimension getSize(){
        if (mComponent != null){
            return mComponent.getSize();
        }
        return new Dimension(0,0);
    }
    
    /* (non-Javadoc)
     * @see com.arc.widgets.IComponent#convertToScreenPoint(int, int)
     */
    @Override
    public Point convertToScreenPoint(int x, int y) {
        Point p = new Point(x,y);
        SwingUtilities.convertPointToScreen(p,(Component)getComponent());
        return p;
    }
    
    @Override
    public int getWidth(){
        if (mComponent != null){
            return mComponent.getWidth();
        }
        return 0;
    }
    
    @Override
    public int getHeight(){
        if (mComponent != null){
            return mComponent.getHeight();
        }
        return 0;
    }

    private void doToolTip() {
        if (mToolTip != null && mComponent instanceof JComponent)
            ((JComponent) mComponent).setToolTipText(mToolTip);
    }

    private void doBorder() {
        if (mBorder == null && mBorderTitle != null && mComponent != null)
            mBorder = sEtchedBorder;
        if (mBorder != null && mComponent instanceof JComponent) {
            Border b = mBorder;
            if (mBorderTitle != null)
                b = new TitledBorder(b, mBorderTitle);
            if (mEmptyBorder != null)
                b = new CompoundBorder(b, mEmptyBorder);
            ((JComponent) mComponent).setBorder(b);
        }
    }

    private void doForeground() {
        if (mForeground != null && mComponent != null)
            mComponent.setForeground(mForeground);
    }

    private void doBackground() {
        if (mBackground != null && mComponent != null)
            mComponent.setBackground(mBackground);
    }

    private void doName() {
        if (mName != null && mComponent != null)
            mComponent.setName(mName);
    }

    private void doVisible() {
        if (mComponent != null)
            mComponent.setVisible(mVisible);
    }

    private void doEnabled() {
        if (mComponent != null)
            mComponent.setEnabled(mEnabled);
    }

    private void doFont() {
        if (mFont != null && mComponent != null)
            mComponent.setFont((Font)mFont.getObject());
    }

    private void doPreferredSize() {
        if (mPreferredSize != null && mComponent instanceof JComponent)
            ((JComponent) mComponent).setPreferredSize(mPreferredSize);
    }
    
    @Override
    public void repaint(){
        Component c = (Component)getComponent();
        c.repaint();
    }
    
    @Override
    public void revalidate(){
        Component c = (Component)getComponent();
        c.invalidate();
        c.validate();
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
        Component c = (Component)getComponent();
        c.setBounds(x,y,width,height);

    }
    /* (non-Javadoc)
     * @see com.arc.widgets.IComponent#setLayoutData(java.lang.Object)
     */
    @Override
    public void setLayoutData(Object object) {
        mLayoutData = object;

    }
    
    @Override
    public IFont getFont () {
        if (mFont == null) {
            Component c = (Component) getComponent();
            final Font f = c.getFont();
            if (f != null) {
                mFont = new SwingFont(f);
            }
        }
        return mFont;
    }

    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param observer
     */
    @Override
    public synchronized void addObserver (IComponentObserver observer) {
        if (mObservers == null) {
            mObservers = new ArrayList<IComponentObserver>();
            Component c = (Component)getComponent();
            c.addComponentListener(new ComponentListener(){

                @Override
                public void componentHidden (ComponentEvent e) {
                    // @todo Auto-generated method stub
                    
                }

                @Override
                public void componentMoved (ComponentEvent e) {
                    // @todo Auto-generated method stub
                    
                }

                @Override
                public void componentResized (ComponentEvent e) {
                    synchronized(SwingComponent.this){
                        for (IComponentObserver o: mObservers){
                            o.onSizeChange(SwingComponent.this);
                        }
                    }
                    
                }

                @Override
                public void componentShown (ComponentEvent e) {
                    // @todo Auto-generated method stub
                    
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
        // @todo Auto-generated method stub
        
    }
}
