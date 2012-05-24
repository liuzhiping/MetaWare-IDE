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
import java.awt.Component;
import java.awt.Container;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import com.arc.widgets.ICardContainer;
import com.arc.widgets.IComponent;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;


class CardContainer extends AbstractContainer implements ICardContainer {

    private CardLayout mLayout;

    private Map<Component,String> mMap;

    CardContainer(IContainer parent, IComponentMapper mapper) {
        super(parent, mapper);
        mLayout = new CardLayout();
    }

    class MyCardPanel extends JPanel {

        MyCardPanel(CardLayout layout) {
            super(layout);
        }

        // Delay adding them until "setChildName" called
        @Override
        public void add (Component c, Object name) {
            if (mMap == null)
                mMap = new HashMap<Component,String>();
            mMap.put(c, (String)name);
        }

        void addCard (Component c, String name) {
            super.add(c, name);
        }
    }

    @Override
    protected Component instantiate () {
        MyCardPanel p = new MyCardPanel(mLayout);
        if (mMap != null) {
            for (Map.Entry<Component,String> e: mMap.entrySet()){
                p.addCard(e.getKey(), e.getValue());
            }
            mMap = null;
        }
        return p;
    }

    @Override
    public void setCardName (Object child, String name) {
        if (child instanceof IComponent) {
            child = ((IComponent) child).getComponent();
        }
        if (mComponent == null) {
            if (mMap == null)
                mMap = new HashMap<Component,String>();
            mMap.put((Component)child, name);
        }
        else
            ((MyCardPanel) mComponent).addCard((Component) child, name);
    }

    @Override
    public void showCard (Object child) {
        if (child instanceof IComponent) {
            child = ((IComponent) child).getComponent();
        }
        mLayout.show((Container) getComponent(), ((Component) child).getName());
    }

    @Override
    public void showCard (String name) {
        mLayout.show((Container) getComponent(), name);
    }

    /**
     * @see com.arc.widgets.IContainer#getContainerStyle()
     */
    @Override
    public int getContainerStyle () {
        return IComponentFactory.STACK_STYLE;
    }

    /**
     * @see com.arc.widgets.IContainer#setHorizontalSpacing(int)
     */
    @Override
    public void setHorizontalSpacing (int pixels) {
    }

    /**
     * @see com.arc.widgets.IContainer#setVerticalSpacing(int)
     */
    @Override
    public void setVerticalSpacing (int pixels) {
    }

}
