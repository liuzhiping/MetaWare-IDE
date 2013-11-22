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


/**
 * A GUI component that is a choice combo box. The factory determines whether or not it is writable.
 */
public interface IChoice extends IComponent {

    interface IObserver {

        public void selectionChanged (IChoice choice);
    }

    interface ITextObserver {

        public void textChanged (IChoice choice, String text);
    }

    /**
     * Add observer when value of field changes, whether by user or programmatically.
     * @param o the observer.
     */
    void addObserver (IObserver o);

    /**
     * Remove a previously added observer.
     * @param o the observer to remove.
     */
    void removeObserver (IObserver o);

    void setItems (String[] items);

    int addItem (String text);

    String getText ();

    /**
     * Remove all items.
     */
    void clear ();

    /**
     * Return selected item index, or -1.
     */
    int getSelectionIndex ();

    void setSelection (String text);

    int getItemCount ();

    void removeAllItems ();

    Object getItemAt (int i);

    /**
     * Remove item from combo box
     * @param i index of item to be removed.
     */
    void removeItemAt (int i);

    /**
     * Set the selection index.
     */
    void setSelectionIndex (int i);

    /**
     * Add action listener when something happens. For SWT implementation, we contrive the ActionEvent
     */
    void addActionListener (ActionListener listener);

    void removeActionListener (ActionListener listener);

    /**
     * Set approximate minimum size in terms of character positions.
     */
    void setColumns (int col);

    /**
     * Add an observer that is notified each time the text is latered in the text field of the combobox. Each keystroke
     * will cause an event to be fired.
     * @param observer
     */
    void addTextObserver (ITextObserver observer);

    /**
     * Remove text observer.
     * @param observer
     */
    void removeTextObserver (ITextObserver observer);

    /**
     * Return whether or not this combobox is editable.
     * @return whether not this combobox is editable.
     */
    boolean isEditable ();
}
