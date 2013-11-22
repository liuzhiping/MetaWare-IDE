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
 * A Table widget.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface ITable extends IComponent{
    
    interface ISelectionListener {
        void onTableItemSelected(int row, int column);
        void onTableItemDoubleClicked(int row, int column);
        /**
         * Invoked when user enters a new value of an editable cell. Returns the string to 
         * be written into the cell if the
         * new value is to be accepted. (It is typically the same as the new value).
         * Returns null if the new value is to be rejected.
         * Returns false if the new value is to be rejected.
         * @param row
         * @param column
         * @param value new value.
         * @return the new value to write into the cell, or null if reject the value.
         */
        String onNewValueEntered(int row, int column, String value);
    }
    /**
     * Set the number of columns.
     * @param n the number of columns.
     */
    public void setColumnCount(int n);
    
    /**
     * Set the column headers.
     * If this method isn't called, then the table will have no headers.
     * @param headers the headers, each element corresponds to a column.
     */
    public void setColumnHeaders(String[]headers);
    
    /**
     * Sets the column widths in pixels.
     * A width of 0 means that the column is to be "packed" so that it has the
     * minimal width to represent all of its contants. This is the default.
     * @param widths width of each column, in pixels.
     */
    public void setColumnWidths(int[] widths);
    
    /**
     * Set the number or rows to "n". Any rows beyond are deleted.
     * @param n number of rows to reset the table to.
     */
    public void setRowCount(int n);
    
    /**
     * Return the number of rows in the table.
     * @return the number of rows in the table.
     */
    public int getRowCount();
    
    /**
     * Set the foreground color of the items in a particular column.
     * @param column the zero-based collumn
     * @param color the foreground color of the text.
     */
    public void setColumnForeground(int column, IColor color);
    
    public void setColumnFont(int column, IFont font);
    
    public IFont getColumnFont(int column);
    
    /**
     * Indicate whether or not the items in a column are selectable (i.e., produce
     * a selection event.)
     * @param column
     * @param v
     */
    public void setColumnSelectable(int column, boolean v);
    
    public void addSelectionListener(ISelectionListener l);
    public void removeSelectionListener(ISelectionListener l);
    
    /**
     * Set the foreground color of a particular item, overriding the
     * column foreground color.
     * <P>
     * A color of <code>null</code> will restore the foreground to the
     * default value.
     * @param row
     * @param column
     * @param color the foreground color or <code>null</code>.
     */
    public void setForeground(int row, int column, IColor color);
    
    /**
     * Set the background color of a particular item, overriding the default.
     * @param row
     * @param column
     * @param color the background color, or <code>null</code> to restore the default.
     */
    public void setBackground(int row, int column, IColor color);
    
    /**
     * Set the item to a value without attributes.
     * @param row
     * @param column
     * @param value
     */
    public void setItem(int row, int column, String value);
    
    /**
     * Return the content of the given cell (stripped of any attribute information).
     * @param row Row of the cell (zero based).
     * @param column Column of the cell (zero based).
     * @return the string at that cell.
     * @exception IllegalArgumentException if there is no cell at specified location.
     */
    public String getItem(int row, int column);
    
    /**
     * Set the item to a value with attributes.
     * @param row
     * @param column
     * @param value
     */
    public void setItem(int row, int column, IAttributedString value);
    
    public void setSelection(int row, int column);
    
    enum Alignment { LEFT, RIGHT, @SuppressWarnings("hiding") CENTER }
    
    public void setColumnAlignment(int column, Alignment alignment);
    
    /**
     * Set a cell to a mode for receiving input.
     * When the new input is entered, the {@link ISelectionListener#onNewValueEntered} method
     * is invoked.
     * @param row
     * @param column
     */
    public void setInput(int row, int column);
    
    /**
     * Make the columns wide enought to hold the current data.
     */
    public void pack();
    
    /**
     * Set whether or not vertical scroll is permitted.
     * @param enabled
     */
    public void setVerticalScroll(boolean enabled);

}
