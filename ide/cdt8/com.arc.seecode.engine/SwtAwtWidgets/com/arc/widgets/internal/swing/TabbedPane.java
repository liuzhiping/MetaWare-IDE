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

import javax.swing.Icon;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import com.arc.widgets.IComponent;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;
import com.arc.widgets.ITabItem;
import com.arc.widgets.ITabbedPane;

class TabbedPane extends AbstractContainer implements ITabbedPane {
    private int mStyle;

    TabbedPane(IContainer parent, int style, IComponentMapper mapper) {
        super(parent, mapper);
        mStyle = style;
    }

    @Override
    protected Component instantiate() {
        int t = SwingConstants.TOP;
        switch (mStyle) {
            case IComponentFactory.TABS_ON_TOP:
                t = SwingConstants.TOP;
                break;
            case IComponentFactory.TABS_ON_BOTTOM:
                t = SwingConstants.BOTTOM;
                break;
            case IComponentFactory.TABS_ON_LEFT:
                t = SwingConstants.LEFT;
                break;
            case IComponentFactory.TABS_ON_RIGHT:
                t = SwingConstants.RIGHT;
                break;
        }
        return new JTabbedPane(t) {
            // Suppress adding from child's constructor
            @Override
            public void add(Component c, Object constraint) {
            }

            @Override
            public Component add(Component c) {
                return c;
            }
        };
    }

    @Override
    public void setSelectedItem(ITabItem item) {
        ((JTabbedPane) getComponent()).setSelectedComponent((Component) item
                .getComponent());
    }

    @Override
    public ITabItem addTab(IComponent component, String title) {
        JTabbedPane p = (JTabbedPane) getComponent();
        p.addTab(title, (Component) component.getComponent());
        return new TabItem(p, (Component) component.getComponent());
    }

    class TabItem implements ITabItem {
        private JTabbedPane _pane;

        private Component _component;

        TabItem(JTabbedPane pane, Component c) {
            _pane = pane;
            _component = c;
        }

        @Override
        public String getText() {
            int i = _pane.indexOfComponent(_component);
            if (i < 0)
                return null;
            return _pane.getTitleAt(i);
        }

        @Override
        public void setText(String txt) {
            int i = _pane.indexOfComponent(_component);
            if (i >= 0)
                _pane.setTitleAt(i, txt);
        }

        @Override
        public Object getImage() {
            int i = _pane.indexOfComponent(_component);
            if (i < 0)
                return null;
            return _pane.getIconAt(i);
        }

        @Override
        public void setImage(Object image) {
            int i = _pane.indexOfComponent(_component);
            if (i >= 0)
                _pane.setIconAt(i, (Icon) image);
        }

        @Override
        public ITabbedPane getParent() {
            return TabbedPane.this;
        }

        @Override
        public void setToolTipText(String tip) {
            int i = _pane.indexOfComponent(_component);
            if (i >= 0)
                _pane.setToolTipTextAt(i, tip);
        }

        @Override
        public String getToolTipText() {
            int i = _pane.indexOfComponent(_component);
            if (i < 0)
                return null;
            return _pane.getToolTipTextAt(i);
        }

        /**
         * Get component occupying the pane.
         */
        @Override
        public Object getComponent() {
            return _component;
        }
    }

    /* override */
    @Override
    public int getContainerStyle() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* override */
    @Override
    public void setHorizontalSpacing(int pixels) {
        // TODO Auto-generated method stub

    }

    /* override */
    @Override
    public void setVerticalSpacing(int pixels) {
        // TODO Auto-generated method stub

    }
}
