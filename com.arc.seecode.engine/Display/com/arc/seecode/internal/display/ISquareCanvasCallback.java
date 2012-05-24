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

/**
 * Callback interface to receive action requests from
 * {@link ISquareCanvas} object.
 * @author David Pickens
 */
public interface ISquareCanvasCallback {
    /**
     * The associated {@link ISquareCanvas} object
     * has its {@linkplain ISquareCanvas#setEnableToolTips(boolean)
     * enableToolTips} property set to true, and the mouse
     * is hovering at a particular location.
     * <P>
     * If the implementor requires that a tooltip be
     * display at this position, it will presumably call
     * {@link ISquareCanvas#showToolTip}.
     * <P>
     * @param canvas the associated object that originated
     * this action.
     * @param line the line near where the mouse is hovering.
     * @param column the column near where the mouse is hovering.
     */
    public void requestToolTip(ISquareCanvas canvas,
            int line,
            int column);
    
    /**
     * Do whatever is necessary to copy the entire content of the associated
     * canvas to the clipboard. The implementation is expected to include all
     * of the backing data -- not just what is visible.
     * <P>
     * @param canvas the associated canvas.
     * @return true if successful; false if the implementation does not support
     * the operation, in which case the caller is expected to do the operation himself
     * on whatever is visible in the display.
     */
    public boolean copyAllToClipboard(ISquareCanvas canvas);
    
    /**
     * Indicate that a particular line if a {@link ISquareCanvas}
     * object was double-clicked by the user. The line number
     * is relative to the start of the document.
     * @param canvas the associated {@link ISquareCanvas} object.
     * @param line the number of the line that was double-clicked
     * (0-based, relative to start of document).
     */
    public void doubleClickOccurred(ISquareCanvas canvas, int line);
    
    /**
     * Indicate that a particular line if a {@link ISquareCanvas}
     * object was single-clicked by the user. The line number
     * is relative to the start of the document.
     * @param canvas the associated {@link ISquareCanvas} object.
     * @param line the number of the line that was clicked
     * (0-based, relative to start of document).
     */
    public void singleClickOccurred(ISquareCanvas canvas, int line);

    /**
     * Called when user selects a particular line of 
     * the canvas with a mouse click.
     * @param canvas the associated {@Link ISquareCanvas} object.
     * @param line the line number (0-based, relative to start
     * of model).
     * @param column the column number (0-based).
     */
    public void setSelectedLine(ISquareCanvas canvas, int line, int column);
    
    /**
     * Indicate that the user has highlighted text. This will be called
     * after {@link #setSelectedLine}.
     * @param canvas the associated {@Link ISquareCanvas} object.
     * @param line the line number (0-based, relative to start of model)
     * @param startColumn the column number of the start of the highlighted
     * text (0-based).
     * @param endColumn the column number immediately after the
     * highlighted text (0-based).
     * @param selection the string that is highlighted.
     */
    public void setSelection(ISquareCanvas canvas, int line, int startColumn, int endColumn, String selection);
    
    /**
     * Request implementor to set the given range of
     * lines in the associated canvas object.
     * <P>
     * @param canvas the associated {@link ISquareCanvas}object.
     * @param lineCount the number of lines to display.
     * @param width the width of the display in characters
     * (we use a fixed-width font).
     */
    public void setLines(ISquareCanvas canvas, int lineCount, int width);
    
    /**
     * A right-click of the mouse occurred; do whatever
     * is necessary (e.g., show popup menu)
     * <P>
     * @param canvas the associated canvas.
     * @param x the x pixel coordinate of the mouse cursor
     * relative to the canvas.
     * @param y the y pixel coordinate of the mouse cursor
     * relative to the canvas.
     */
    public void showPopup(ISquareCanvas canvas, int x, int y, String selection);
}
