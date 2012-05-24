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
 * A scroll bar as appears on a {@link IScrollPane}
 * object.
 * @author David Pickens
 */
public interface IScrollBar {
    /**
     * Observer interface.
     * See {@link IScrollBar#addObserver addObserver()}.
     * @author David Pickens
     */
    public interface IObserver {
        public void scrollBarChanged(IScrollBar sb);
    }
    /**
     * Return the value of the scroll position. It will
     * be some number getween the {@link #getMinimum() minumum}
     * and {@link #getMaximum() maximum} inclusive.
     * 
     * @return the value of the scroll position.
     */
    public int getValue();
    
    /**
     * Set the value. If the value is less then
     * {@link #getMinimum minimum}, it is set to
     * the minimum.
     * <P>
     * If the value is greater than the 
     * {@link #getMaximum maximum}, it is set to the
     * maximum.
     * <P>
     * <B>NOTE:</B> if the new value differs from the old,
     * then the observers will be notified.
     * @param value the new value to set.
     */
    public void setValue(int value);
    
    /**
     * Sets the current value of the scroller, but
     * does <i>not</i> notify scrollers. This is called
     * if things are being rescaled, or something like that.
     * <P>
     * See {@link #setValue}.
     * @param value the scroller value.
     */
    public void setCurrentValue(int value);
    
    /**
     * Return the minimum position of the scroller.
     * @return the minimum position of the scroller.
     */
    public int getMinimum();
    
    /**
     * Return the maximum position of the scroller.
     * @return the maximum position of the scroller.
     */
    public int getMaximum();
    /**
     * Return the amount by which the scroll value 
     * increments/decrements when arrow keys are pressed.
     * @return the increment amount.
     */
    public int getIncrementAmount();
    /**
     * Return the amount by which the scroll value 
     * increments/decrements when page-up/page-down
     * key is pressed.
     * @return the page amount.
     */
    public int getPageAmount();
    
    /**
     * Set the minimum position of the scroller.
     * If the {@link #getValue value} of the scroll bar
     * is less than this value, it will be
     * set to the new minimum. 
     * @param v the new minimum value.
     */
    public void setMinimum(int v);
    
    /**
     * Set the maximum position of the scroller.
     * If the {@link #getValue value} of the scroll bar
     * is greater than this value, it will be
     * set to the new maximum. 
     * @param v the new maximum value.
     */
    public void setMaximum(int v);
    
    /**
     * Set the amount that the value is incremented or
     * decremented when the ends of the scrollbar are
     * clicked, or when arrow keys are pressed.
     * @param amount the new incremetn amount.
     */
    public void setIncrementAmount(int amount);
    
    /**
     * Set the amount that the value is incremented or
     * decremented when the "page up" or "page down"
     * key is selected.
     * @param amount the new page increment amount.
     */
    public void setPageAmount(int amount);
    
    /**
     * Return the size of the port relative to
     * the minimum and maxiumum value. 
     * @return the relative size of the thumb.
     */
    public int getPortSize();
    
    /**
     * Set the size of the port relative to
     * the minimum and maxiumum value. 
     * @param size the relative size of the thumb.
     */
    public void setPortSize(int size);
    
    /**
     * Return whether or not the scrollbar is visible.
     * It is invisible if it isn't required.
     * @return whether or not the scrollbar is visible.
     */
    public boolean isVisible();
    
    /**
     * Set the visibility of the scrollbar.
     * @param v
     */
    public void setVisible(boolean v);

    /**
     * Add an observer for this scroller
     * 
     * @param observer
     */
    public void addObserver(IObserver observer);
    /**
     * Remove an observer for this scroller.
     * @param observer
     */
    public void removeObserver(IObserver observer);
}
