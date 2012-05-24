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
package com.arc.widgets;

import java.awt.event.ActionListener;
//import java.awt.event.ItemListener;
/**
 * A GUI component that is a button.
 */
public interface IButton extends IComponent, IToolItem {
    String getText();
    @Override
    void setText(String txt);
    @Override
    void setImage(IImage image);
    IImage getImage();

    /**
     * For a checkbox, toggle, or radio button, select or unselect it.
     */
    @Override
    void setSelected(boolean v);

    /**
     * Set a control character that will enable this button.
     */
    void setMnemonic(char c);

    public static final int PUSH = 0;
    public static final int CHECKBOX = 1;
    public static final int TOGGLE = 2;
    public static final int RADIO = 3;

    /**
     * Return one of PUSH, CHECKBOX, TOGGLE, or RADIO to indicate
     * which kind of button this is.
     */
    int getButtonKind();

    /**
     * Add action listener for when button's state changes.
     * <b>Note:</b> for the SWT implementation, we contrive an ActionEvent from
     * the underlying <code>org.eclipse.swt.events.SelectionEvent</code>.
     */
    void addActionListener(ActionListener listener);
    
    /**
     * Remove action listener.
     */
    void removeActionListener(ActionListener listener);

    /**
     * Return state of this button if it is a toggle button, checkbox, or
     * radio button. For push-style of button, this method returns null.
     */
    @Override
    boolean isSelected();
    }

