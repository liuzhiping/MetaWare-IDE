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
 * A scrollable list of strings that can be selected.
 * @author David Pickens
 */
public interface IList extends IComponent {
    public interface IObserver {
        /**
         * Called each time the selection changes.
         * @param list the applicable widget.
         */
        public void onSelected(IList list);
        /**
         * Called when a row is double-clicked.
         * @param list the applicable widget.
         * @param index the row that was double-clicked.
         */
        public void onDoubleClicked(IList list, int index);
    }
    void setItems(String list[]);
    
    void addObserver(IObserver observer);
    void removeObserver(IObserver observer);
    
    /**
     * Clear any existing selection, and select
     * the given index.
     * @param index the index of the line to be
     * exclusively selected, or -1 if nothing is
     * to be selected.
     */
    void setSelection(int index);
    
    /**
     * Programmatically set multiple selections.
     * @param index array of rows to be selected.
     */
    void setSelections(int index[]);
    
    /**
     * Return the first selected row, or -1 if
     * nothing selected.
     * @return the first selected row, or -1 if
     * nothing selected.
     */
    int getSelectionIndex();
    
    /**
     * Return the array of all selected lines.
     * If nothing is selected, an empty array is
     * returned.
     * @return the array of all selected lines.
     */
    int[] getSelectionIndices();
    
    /**
     * Return the contents of the first selected
     * line.
     * @return the contents of the first selected
     * line.
     */
    String getSelection();
}
