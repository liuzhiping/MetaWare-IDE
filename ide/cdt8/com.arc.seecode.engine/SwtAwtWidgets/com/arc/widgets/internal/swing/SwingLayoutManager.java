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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

import com.arc.widgets.IContainer;
import com.arc.widgets.ILayoutManager;

/**
 * @author David Pickens
 */
class SwingLayoutManager implements LayoutManager {

    private ILayoutManager mLayout;
    private IContainer mContainer;

    public SwingLayoutManager(ILayoutManager layout, IContainer c){
        mLayout = layout;
        mContainer = c;
    }


    /* (non-Javadoc)
     * @see java.awt.LayoutManager#addLayoutComponent(java.lang.String, java.awt.Component)
     */
    @Override
    public void addLayoutComponent(String name, Component comp) {

    }

    /* (non-Javadoc)
     * @see java.awt.LayoutManager#removeLayoutComponent(java.awt.Component)
     */
    @Override
    public void removeLayoutComponent(Component comp) {
        

    }

    /* (non-Javadoc)
     * @see java.awt.LayoutManager#preferredLayoutSize(java.awt.Container)
     */
    @Override
    public Dimension preferredLayoutSize(Container parent) {
        return mLayout.computeSize(mContainer,ILayoutManager.DEFAULT_SIZE,ILayoutManager.DEFAULT_SIZE);
    }

    /* (non-Javadoc)
     * @see java.awt.LayoutManager#minimumLayoutSize(java.awt.Container)
     */
    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return preferredLayoutSize(parent);
    }

    /* (non-Javadoc)
     * @see java.awt.LayoutManager#layoutContainer(java.awt.Container)
     */
    @Override
    public void layoutContainer(Container parent) {
        mLayout.layout(mContainer);

    }

}
