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
import java.awt.event.TextListener;
/**
 * A GUI component that is a text field.
 */
public interface ITextField extends IComponent {
    public interface IObserver {
        public void selectionChanged(ITextField tf);
    }
    
    /**
     * Add observer when value of field changes,
     * whether by user or programmatically.
     * @param o the observer.
     */
    void addObserver(IObserver o);
    /**
     * Remove a previously added observer.
     * @param o the observer to remove.
     */
    void removeObserver(IObserver o);
    void setText(String text);
    String getText();
    /**
     * Add action listener when enter key is pressed, or focus is lost after
     * something is typed.
     * For SWT implementation, we contrive the ActionEvent
     */
    void addActionListener(ActionListener listener);

    /**
     * Set approximate minimum size in terms of character positions.
     */
    void setColumns(int col);

    /**
     * Add listener for each time a key is typed in this text field.
     * For SWT implementation, we contrive the TextEvent.
     */
    void addTextListener(TextListener listener);
    
    /**
     * Set whether or not the text field is editable.
     * If not editable, it is essentially a label that can
     * be dynamically changed.
     */
    void setEditable(boolean v);
    
    /**
     * Select a range for change
     * @param start start index (inclusive)
     * @param end ending index (exclusive)
     */
    void setSelection(int start, int end);
    
    /**
     * Indicate whether or not an action event is to be fired when
     * the focus is lost after changing the text. If not, then the action
     * event is only fired if the enter key is pressed while the text has
     * focus.
     * <P>
     * The default is true.
     * @param v if true then fire an action event when the focus is lost and the text
     * has changed. If false, then do nothing when the focus is lost.
     */
    void setFireActionWhenFocusLost(boolean v);
    }
