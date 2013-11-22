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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

import com.arc.widgets.IComponent;
import com.arc.widgets.IImage;
import com.arc.widgets.IMenu;

/**
 * @author David Pickens
 */
class PopupMenu extends Menu {
    private IComponent mParent;

    /**
     * @param parent
     * @param mapper
     */
    public PopupMenu(IComponent parent, IComponentMapper mapper) {
        super((IMenu)null, mapper);
        mParent = parent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.swing.SwingComponent#instantiate()
     */
    @Override
    protected Component instantiate() {
        return new JPopupMenu();
    }

    @Override
    public Object getComponent() {
        if (mComponent == null) {
            final JPopupMenu menu = (JPopupMenu) instantiate();
            Component c = (Component)mParent.getComponent();

            c.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    maybeShowPopup(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    maybeShowPopup(e);
                }

                private void maybeShowPopup(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        menu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            });
            mComponent = menu;
        }
        return mComponent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IContainer#getContainerStyle()
     */
    public int getContainerStyle() {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IContainer#setHorizontalSpacing(int)
     */
    public void setHorizontalSpacing(int pixels) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IContainer#setVerticalSpacing(int)
     */
    public void setVerticalSpacing(int pixels) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IMenu#setText(java.lang.String)
     */
    @Override
    public void setText(String txt) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IMenu#setImage(com.arc.widgets.IImage)
     */
    @Override
    public void setImage(IImage image) {

    }

}
