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
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

import com.arc.widgets.ICardContainer;
import com.arc.widgets.IComponent;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;

/**
 * A builder for an SWT StackLayout container
 */

class CardContainer extends AbstractContainer implements ICardContainer {
    private StackLayout mLayout;

    CardContainer(IContainer parent, IComponentMapper mapper) {
        super(parent,mapper);
        mLayout = new StackLayout();
    }

    @Override
    protected Widget instantiate() {
        Composite c = new Composite((Composite)mParent.getComponent(), 0);
        c.setLayout(mLayout);
        return c;
    }
    /**
     * Set the name of a child.
     * @param child assign name to existing child.
     */
    @Override
    public void setCardName(Object child, String name) {
        if (child instanceof IComponent){
            child = ((IComponent)child).getComponent();
        }
        if (mLayout.topControl == null) 
            mLayout.topControl = (Control) child;
        ((Control) child).setData("card.name", name);
    }
    /**
     * Show given child.
     * @param child child component to display.
     * @exception IllegalArgumentException Thrown if child isn't in
     * the container.
     */
    @Override
    public void showCard(Object child) {
        if (child instanceof IComponent){
            child = ((IComponent)child).getComponent();
        }
        if (mLayout.topControl != child) {
            mLayout.topControl = (Control) child;
            ((Composite) getComponent()).layout();
        }
    }
    /**
     * Show child that has a given name.
     * @param name name of child component to display.
     * @exception IllegalArgumentException thrown if child isn't in
     * the container or is <code>null</code>.
     */
    @Override
    public void showCard(String name) {
        Control[] controls = ((Composite) getComponent()).getChildren();
        for (int i = 0; i < controls.length; i++) {
            if (name.equals(controls[i].getData("card.name"))) {
                showCard(controls[i]);
                return;
            }
        }
        // Look for wildcard name of "*"
        for (int i = 0; i < controls.length; i++) {
            if ("*".equals(controls[i].getData("card.name"))) {
                showCard(controls[i]);
                return;
            }
        }
        throw new IllegalArgumentException("No card named " + name);
    }

    @Override
    public void setMargins(int top, int left, int bottom, int right) {
        mLayout.marginHeight = Math.max(top, bottom);
        mLayout.marginWidth = Math.max(left, right);
    }
    
    /**
     * @see com.arc.widgets.IContainer#getContainerStyle()
     */
    @Override
    public int getContainerStyle() {
        return IComponentFactory.STACK_STYLE;
    }

    /**
     * @see com.arc.widgets.IContainer#setHorizontalSpacing(int)
     */
    @Override
    public void setHorizontalSpacing(int pixels) {
    }

    /**
     * @see com.arc.widgets.IContainer#setVerticalSpacing(int)
     */
    @Override
    public void setVerticalSpacing(int pixels) {
    }



}
