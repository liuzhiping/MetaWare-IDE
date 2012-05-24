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

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;

import javax.swing.JPanel;

import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;
import com.arc.widgets.ILayoutManager;

class SwingContainer extends AbstractContainer {

    private int mStyle;
    private int mHGap;
    private int mVGap;
    private int mColumns;
    private ILayoutManager mLayout = null;

    SwingContainer(IContainer parent, int style, int columns, IComponentMapper mapper) {
        super(parent,mapper);
        mStyle = style;
        mColumns = columns;
        if (style == IComponentFactory.ROW_STYLE) mHGap = 3;
    }
    
    SwingContainer(IContainer parent, ILayoutManager layout, IComponentMapper mapper) {
        super(parent,mapper);
        mStyle = IComponentFactory.NO_STYLE;
        mLayout = layout;
    }

    int getStyle() {
        return mStyle;
    }
    @Override
    public int getContainerStyle() {
        return mStyle;
    }

    /**
     * A Row panel that can have gaps.
     */
    static class RowPanel extends JPanel {
        private int mGap;
        RowPanel(int gap) {
            super(new GridBagLayout());
            mGap = gap;
        }
        @Override
        public void add(Component c, Object constraint) {
            GridBagConstraints con = (GridBagConstraints) constraint;
            con.gridx = GridBagConstraints.RELATIVE;
            con.gridy = 0;
            if (getComponentCount() > 0) {
                con.insets.left += mGap;
                super.add(c, con);
                con.insets.left -= mGap;
            }
            else
                super.add(c, constraint);
        }
    }
    /**
     * A column panel that can have gaps.
     */
    static class ColumnPanel extends JPanel {
        private int mGap;
        ColumnPanel(int gap) {
            super(new GridBagLayout());
            mGap = gap;
        }
        @Override
        public void add(Component c, Object constraint) {
            GridBagConstraints con = (GridBagConstraints) constraint;
            con.gridy = GridBagConstraints.RELATIVE;
            con.gridx = 0;
            con.weightx = 1.0;
            if (c instanceof JPanel && ((JPanel)c).getBorder() != null){
                 con.fill = GridBagConstraints.HORIZONTAL;
            }
            if (getComponentCount() > 0) {
                con.insets.top += mGap;
                super.add(c, con);
                con.insets.top -= mGap;
            }
            else
                super.add(c, constraint);
        }
    }
    /**
     * A grid panel that can have gaps.
     */
    static class GridPanel extends JPanel {
        private int _HGap;
        private int _VGap;
        private int _columns;
        GridPanel(int columns, int hgap, int vgap) {
            super(new GridBagLayout());
            _HGap = hgap;
            _VGap = vgap;
            _columns = columns;
        }
        @Override
        public void add(Component c, Object constraint) {
            GridBagConstraints con = (GridBagConstraints) constraint;
            int position = getComponentCount();
            con.gridy = position / _columns;
            con.gridx = position % _columns;
            int saveTop = con.insets.top;
            int saveLeft = con.insets.left;
            if (con.gridy > 0) {
                con.insets.top += _VGap;
            }
            if (con.gridx > 0) {
                con.insets.left += _HGap;
            }
            super.add(c, con);
            con.insets.top = saveTop;
            con.insets.left = saveLeft;
        }
    }

    @Override
    protected Component instantiate() {
        LayoutManager l = null;
        JPanel c = null;
        switch (mStyle) {
            case IComponentFactory.GRID_STYLE :
                c = new GridPanel(mColumns, mHGap, mVGap);
                break;
            case IComponentFactory.ROW_STYLE :
                c = new RowPanel(mHGap);
                break;
            case IComponentFactory.COLUMN_STYLE :
                c = new ColumnPanel(mVGap);
                break;
            case IComponentFactory.FLOW_STYLE :
                c = new JPanel();
                l =
                    new FlowLayout(
                       FlowLayout.LEFT,
                        mHGap,
                        mVGap);
                c.setLayout(l);
                break;
            case IComponentFactory.NO_STYLE :
                if (mLayout != null){
                    l = new SwingLayoutManager(mLayout,this);
                    c = new JPanel(l);
                }
        }
        return c;
    }

    @Override
    public void setHorizontalSpacing(int pixels) {
        mHGap = pixels;
    }
    @Override
    public void setVerticalSpacing(int pixels) {
        mVGap = pixels;
    }
}
