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
package com.arc.mw.util;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * This is an interface by which the GUI tester can
 * query a display of SeeCode.
 * <P>
 * This interface should go in some other package that
 * is visible to both SeeCode and the GUI testing framework.
 * But, for the time being, this package is the only thing
 * that is shared between the two frameworks.
 * 
 * @author David Pickens
 */
public interface ISeeCodeDisplay {
    /**
     * Return the number of lines that are showing in the
     * display. That is, the number actually painted.
     * @return the number of lines showing.
     */
    int getDisplayLineCount();
    /**
     * Return the visible portion of a particular line in 
     * the display that is showing, given its line number (0
     * is the first line).
     * @param lineNumber the line number of the visible line
     * to be retrieved, the first being 0.
     * @return the text of the line that is showing.
     * 
     * @pre lineNumber >= 0 && lineNumber < getDisplayLineCount()
     */
    String getDisplayLine(int lineNumber);
    
    /**
     * Return the coordinates in the display where the
     * character in a particular line is located.
     * @param lineNumber the line number, origined at 0.
     * @param column character position, starting at 0.
     * @param length the number of characters.
     * @return the point corresponding to the line and column
     * @pre lineNumber >= 0 && lineNumber < getDisplayLineCount
     */
    Rectangle getDisplayBounds(int lineNumber, int column, int length);

    /**
     * Return the coordinate of where the tree-expansion icon
     * is located on a given line. If there is no tree expansion
     * point, the affects are undefined.
     * @param line line number prefixed with tree icon.
     * @return point of the tree icon at that line.
     */
    Point getTreeImagePoint(int line);
    
    /**
     * Return coordinates of the highlighted region, or
     * the cursor position. Returns null if there is no selection.
     * The result is 4 int array: [lineNumberStart, startColumn, endColumn,
     * lineNumberEnd]
     * @return selected line or -1.
     */
    int[] getHighlight();
    
    /**
     * Return the associated component.
     * @return the associated component.
     */
    Component getComponent();
}
