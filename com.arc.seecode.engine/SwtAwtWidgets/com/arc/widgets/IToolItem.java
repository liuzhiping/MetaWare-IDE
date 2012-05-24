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

/**
 * An item that can appear in a {@link IToolBar}.
 * <P>
 * Notice that is does not extends {@link IComponent}
 * because the SWT implementation is not a control.
 * @author David Pickens
 */
public interface IToolItem extends IWidget{
    public interface IObserver {
        public void itemChanged(IToolItem item);
    }
    
    /**
     * For a {@link #CHECK}- or {@link #RADIO}-style
     * item, this indicates whether or not it is selected.
     * It has no meaning otherwise.
     * @return whether or not a check- or ratio-style item
     * is selected.
     */
    boolean isSelected();
    
    /**
     * Set the image to be displayed.
     * @param image the image to be displayed.
     */
    void setImage(IImage image);
    /**
     * The label to appear on the item.
     * @param text the label on the item.
     */
    void setText(String text);
    
    void addObserver(IObserver observer);
    
    void removeObserver(IObserver observer);
    
    public static final int PUSH = 1;
    public static final int CHECK = 2;
    public static final int RADIO = 3;
    public static final int TOGGLE = 4;
    public static final int PULLDOWN = 5;
    /**
     * Return the style of this item:
     * <dl>
     * <dt>{@link #PUSH}
     * <dd> simply responds to a push operation, but
     * holds no state.
     * <dt>{@link #CHECK}
     * <dd> a checkbox that has a selection of true or false.
     * <dt>{@link #RADIO}
     * <dd> a radiobutton that has a selection of true or false.
     * <dt>{@link #PULLDOWN}
     * <dd> a pulldown-style that has a submenu.
     * <dt>{@link #TOGGLE}
     * <dd> a push-in/push-out button
     * </dl>
     * @return the style of this item.
     */
    int getStyle();
    
    void setSelected(boolean v);

}
