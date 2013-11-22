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
package com.arc.seecode.internal.display;

import com.arc.seecode.display.IColorPreferences;
import com.arc.widgets.IComponent;
import com.arc.widgets.IContainer;


/**
 * This is the text line viewer of a SeeCode custom display. 
 * It is called "Square Canvas"  for obscure
 * historical reasons.
 * @author David Pickens
 */
public interface ISquareCanvas {
    /**
     * Dispose of all resources consumed by this object.
     * The behavior of all methods in this object is
     * undefined after this method is invoked.
     *
     */
    public void dispose();
    /**
     * Indicate whether or not tooltip requests are to
     * be sent back to the engine (via
     * {@link ISquareCanvasCallback#requestToolTip}).
     * @param v if true, send back tooltip requests.
     */
    public void setEnableToolTips(boolean v);
    
    /**
     * Indicate whether or not double-click in column 0
     * is to be supported. If not, then column 0 denotes a tree node
     * that is expanded/contracted.
     * <P>
     * Double clicks are communicated to the engine by the
     * method {@link ISquareCanvasCallback#doubleClickOccurred}.
     * @param v whether or not double-clicks permitted in column 0
     */
    void setPermitDoubleClickInColumn0(boolean v);
    /**
     * Display a tooltip near a line and column position
     * in this display.
     * <P>
     * This method is called when the {@linkplain #setEnableToolTips enableToolTips}
     * property and the engine has received an appropriate
     * tooltip request via the {@link ISquareCanvasCallback#requestToolTip}
     * method.
     * @param tip the tooltip message
     * @param line the line number near where the tooltip is to be display (relative
     * to start of underlying document).
     * @param column the column near where the tooltip is to appear.
     */
    public void showToolTip(String tip, int line, int column);
    
    
    /**
     * Populate the canvas with a list of lines. Only those
     * marked "dirty" have changed.
     * @param newLines the new lines to populate this display with
     */
    public void set(Line[] newLines);
    
    /**
     * Scroll horizontally such that the given zero-based
     * column is on the left.
     * @param column the column that is to appear on
     * the left.
     */
    public void setHorizontalScrollValue(int column);
    
    /**
     * Return underlying widget for this canvas.
     * @return underlying widget for this canvas.
     */
    public IComponent getComponent();
    
    /**
     * Do whatever is necessary to reparent the
     * widget. If the old one is disposed, then
     * completely regenerate it. If it isn't disposed,
     * then either move its parent, or regenerate it
     * and dispose of the old one.
     * <P>
     * <B>NOTE:</B> this is a concession to our
     * generic GUI wrappers that have quite limited
     * layout capability. So if we need to, say, 
     * add a column, we must regenerate the entire
     * container.
     * @param parent the new parent.
     */
    public void reparent(IContainer parent);
    
    /**
     * Return number of lines showing in canvas.
     * @return number of lines showing in canvas.
     */
    public int getLineCount();
    
    /**
     * Returns which of a multi-canvas view that this
     * canvas represents. 0 is the primary canvas. NOn-zero
     * denotes a (profiling) column.
     * @return which of a multi-canvas view this canvas
     * represents.
     */
    public int getWhich();
    
    /**
     * Set the font that the canvas will use. We don't presume a type for the
     * font so that this interface can be used in a SWT or Swing environment.
     * @param font the new font.
     */
    public void setFont(Object font);
    
    /**
     * Set the color preferences. This method will be called when the user changes
     * the preferences and the implementor needs to be able to repaint to reflect
     * the changes.
     * @param prefs the color preferences.
     */
    public void setColorPreferences(IColorPreferences prefs);
    
    /**
     * Draw a vertical line of a particular thickness and color on the left or right. 
     * @param onLeft if true, draw the line on the left; otherwise right.
     * @param thickness thickness of the line; 0 means no line at all.
     * @param color the color of the line; null means no line at all.
     */
    public void setVerticalLine(boolean onLeft, int thickness, Object color);
    
    /**
     * Set the name of the underlying "ITextCanvas" so that the GUI tester
     * can find it.
     * @param name name to be assigned to underlying widget.
     */
    public void setName(String name);
    
    /**
     * Return whatever is selected, or empty string if nothing.
     *
     */
    public String getSelection();
    
    /**
     * Copy selected text to the clipboard, honoring "Select All" status.
     */
    public void copyToClipboard();
    
    /**
     * Set a range of text to be highlighted as "selected".
     * @param line1 starting line, 0 based.
     * @param col1 starting column in line, 0 based.
     * @param line2 end line, inclusive, 0 based.
     * @param col2 end column, inclusive, 0 based.
     */
    void setSelection(int line1, int col1, int line2, int col2);
}
