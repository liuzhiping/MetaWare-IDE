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
 * A widget for representing lines of text with attributes.
 * Icons can appear in a margin on the left of the text.
 * <P>
 * This interface is somewhat biased in implementing the
 * "square canvas" framework required by the SeeCode
 * engine.
 * @author David Pickens
 */
public interface ITextCanvas extends IComponent {
    /**
     * Return the maximum number of lines that can
     * be displayed, given the size of the canvas.
     * @return the maximum number of lines that can
     * be displayed.
     */
    int getDisplayableLines();
    
//    /**
//     * Scroll down or up by some number of lines.
//     * @param amount number of lines to scroll; > 0 scrolls
//     * down; < 0 scrolls up.
//     */
//    void scrollVertically(int amount);
    
    /**
     * Draw line. The line will not necessarily be
     * drawn until @{link #repaint}is called.
     * @param line the number of the line relative to the
     * top of the canvas; the first is 0.
     * @param strings the contents of the line, presumably at different tab stops
     * @param background the background color or <code>null</code> if default is used.
     */
    void setLine(int line, IAttributedString string[], IColor background);

    /**
     * Clear icons on a particular line, if any.
     * <P>
     * This will not necessarily take affect until
     * {@link #repaint} is invoked.
     * @param line the number of the line whose icons
     * are to be removed.
     */
    void clearIcons(int line);
    
    /**
     * Unselect any selected line.
     */
    void unselect();
    
    /**
     * Set the selected line.
     * @param line the number of the line to be selected.
     */
    void setSelectedLine(int line);
    
    /**
     * Set a range of text to be highlighted as "selected".
     * @param line1 starting line, 0 based.
     * @param col1 starting column in line, 0 based.
     * @param line2 end line, inclusive, 0 based.
     * @param col2 end column, inclusive, 0 based.
     */
    void setSelection(int line1, int col1, int line2, int col2);
    
    /**
     * Blank out the given line. I.e., set it to blank.
     * Also clears any icons on that line.
     * <P>
     * This will not necessarily take affect until
     * {@link #repaint} is invoked.
     * @param line the line number (zero-based) that is
     * to be cleared.
     */
    void clearLine(int line);
    
    /**
     * Repaint the canvas with the current set of lines.
     *
     */
    @Override
    void repaint();
    
    /**
     * Draw an icon, typically in the margin.
     * <P>
     * If <code>x</code> is less than the
     * {@linkplain #setLeftMargin(int) margin} width, then
     * the icon will appear in the margin and not by
     * subject to {@linkplain #scrollHorizontally(int)
     * horizontal scrolling}.
     * @param image the icon to be drawn.
     * @param x the number of pixels from the left edge.
     * @param lineNumber the line number from which the
     * y position for the image position is to be derived.
     */
    void setIcon(IImage image, int x, int lineNumber);
    
    /**
     * A collapsed tree symbol.
     * <pre>
     *   |
     *   + ----
     *   |
     * </pre>
     */
    public static final int TREE_COLLAPSED = 0;
    /**
     * A collapsed tree symbol at end of list
     * <pre>
     *   |
     *   + ----
     * </pre>
     */
    public static final int TREE_COLLAPSED_ELBOW = 1;
    /**
     * An expanded tree symbol for something that is empty(?)
     * <pre>
     *   |
     *   - ----
     *   |
     * </pre>
     */
    public static final int TREE_EXPANDED = 2;
    /**
     * An expanded tree symbol.
     * <pre>
     *   |
     *   - ----
     *   
     * </pre>
     */
    public static final int TREE_EXPANDED_ELBOW = 3;
    /**
     * A branch tree symbol (e.g., reference to "leaf").
     * <pre>
     *   |
     *   |----
     *   |
     * </pre>
     */
    public static final int TREE_BRANCH = 4;
    /**
     * A branch tree symbol at end of list(e.g.., reference to "leaf").
     * <pre>
     *   |
     *   |----
     * </pre>
     */
    public static final int TREE_BRANCH_ELBOW = 5;
    /**
     * A blank for indentation.
     */
    public static final int TREE_BLANK = 6;
    /**
     * A vertical bar.
     * <pre>
     *   |
     *   |
     *   |
     * </pre>
     */
    public static final int  TREE_BAR = 7;
    /**
     * Draw a tree indicator as the first character of a line and
     * shift the content of the line over accordingly.
     * <P>
     * The indicator is coded in the characters of
     * a string and  is placed as the first character
     * <i>after</i> the left margin, and is thus
     * subject to horizontal scrolling.
     * <P>
     * The tree indicator characters can be one of the following
     * manifest constants:
     * <dl>
     * <dt> {@link #TREE_COLLAPSED}
     * <dt> {@link #TREE_COLLAPSED_ELBOW}
     * <dt> {@link #TREE_EXPANDED}
     * <dt> {@link #TREE_EXPANDED_ELBOW}
     * <dt> {@link #TREE_BRANCH}
     * <dt> {@link #TREE_BRANCH_ELBOW}
     * <dt> {@link #TREE_BLANK}
     * <dt> {@link #TREE_BAR}
     * </dl>
     * @param line the number of the line being affected.
     * @param treePrefix one of the manifest constant to identify
     * what the tree node is to look like.
     */
    void setTreeIndicators(int line, String treePrefix);
    
    /**
     * Return the number of lines being displayed, which
     * may be less than the {@linkplain #getDisplayableLines
     * maximum} line count.
     * @return the number of lines being displayed
     */
    int getLineCount();
    
    /**
     * Set the left margin where text begins.
     * Icons are displayed in the margin.
     * @param amount
     */
    void setLeftMargin(int amount);
    
    /**
     * Return the maximum number of characters that can
     * appear in a line, given the width of the display
     * and the current {@linkplain #setLeftMargin margin
     * settings}.
     * @return the maximum length of a line that can
     * be displayed.
     */
    int getDisplayableColumns();
    
    /**
     * Show a tooltip near the given line and column.
     * The tooltip will show for some implementation-dependent
     * length of time.
     * @param tooltip the message to appear in the tooltip.
     * @param line the line number (zero-based) near where the tooltip is
     * to be displayed.
     * @param column the zero-based column number near where
     * the tooltip is to appear.
     */
    void showToolTip(String tooltip, int line, int column);
    
    /**
     * Scroll horizontally such that the given column
     * is on the left edge. The margin is not affected
     * by a scroll; it always appears.
     * @param column the 0-based column number to appear
     * on the left edge.
     */
    void scrollHorizontally(int column);
    
    /**
     * Register an observer to received events pertaining
     * to this canvas.
     * @param observer
     */
    void addObserver(IObserver observer);
    /**
     * Remove a previously-registered observer.
     * @param observer
     */
    void removeObserver(IObserver observer);
    
    /**
     * @return the number of the selected line.
     */
    int getSelectedLine();
    
    /**
     * 
     * @return the starting column of what is highlighted or
     * -1 if nothing is highlighted.
     */
    int getSelectedStartColumn();
    
    /**
     * @return the ending column of what is highlighted, or
     * -1 if nothing is highlighted.
     */
    int getSelectedEndColumn();
    
    /**
     * The text that is highlighted.
     * @return the text that is highlighted.
     */
    String getSelection();
    
    /**
     * Return the line height in pixels. It will be based on the
     * font unless {@link #setLineHeight} is called.
     * @return the line height in pixels.
     */
    int getLineHeight();
    
    /**
     * Set the line height, overriding the natural font height.
     * @param height the new line height in pixels.
     */
    void setLineHeight(int height);
    
    /**
     * Pass in a callback method to service a copy-to-clipboard request.
     * If such a callback is not provided, then the implementation is expected
     * to copy the selected text to the clipboard.
     * <P>
     * The callback is provided to handle the case of "Select All" followed by
     * "Copy" when the backing "model" may include more lines than the implementing
     * widget sees.
     * <P>
     * The callback is to be used when the user clicks the Ctrl-C key, or if the
     * {@link #copyToClipboard} method is invoked.
     * @param callback an interface that services a copy-to-clipboard request.
     */
    void setCopyToClipboard(ICopyToClipboard callback);
    
    /**
     * Copy the selected text to the clipboard. If there is a {@link #setCopyToClipboard ICopyToClipboard callback} provided, 
     * then use that to perform the operation. The implementation is to be cognitive of the fact that there
     * is a "Select All" operation pending when calling through the callback object.
     * <P>
     * When a Ctrl-C is typed, the implementation is expected to perform the copy-to-clipboard operation 
     * implicitly. This method is provided to handle a "Copy" operation in a right-click context menu.
     */
    void copyToClipboard();
    
    /**
     * Draw a vertical line of a particular thickness and color on the left or right. 
     * @param onLeft if true, draw the line on the left; otherwise right.
     * @param thickness thickness of the line; 0 means no line at all.
     * @param color the color of the line; null means no line at all.
     */
    public void setVerticalLine(boolean onLeft, int thickness, Object color);

    interface IObserver {
        /**
         * Called when the mouse is clicked on a line.
         * @param canvas
         * @param lineNumber the 0-based line number that
         * was clicked.
         * @param column the 0-based column that was clicked.
         */
        void onSelected(ITextCanvas canvas, int lineNumber, int column);
        
        /**
         * Called when the the user has highlighted text with the mouse.
         * @param canvas the relavent canvs.
         * @param firstLine the 0-based line of where the highlighting begins.
         * @param startColumn the 0-based column of where the highlighting begins in the
         * first line.
         * @param lastLine the 0-based line of where the highlighting ends. If hightlighting
         * text within a single line, this value will be the same as <code>firstLine</code>.
         * @param endColumn the column immediately after the last one highlighted.
         */
        void onHighlighted(ITextCanvas canvas, int firstLine, int startColumn, int lastLine, int endColumn);
        
        /**
         * Called when the mouse is hovering at 
         * particular position for some length of time.
         * The implementor may want to show a tool tip.
         * @param canvas
         * @param lineNumber the 0-based line number where
         * there hovering is taking place.
         * @param column the column (0-based) near where
         * the mouse is hovering.
         */
        void onHover(ITextCanvas canvas, int lineNumber, int column);
        
        /**
         * Called when a right-click popup requested.
         * @param canvas
         * @param x the x pixel relative to the canvas
         * where the mouse is.
         * @param y the y pixel relative to the canvas
         * where the mouse is.
         */
        void onPopup(ITextCanvas canvas, int x, int y);
        
        /**
         * Canvas was resized.
         * @param canvas the canvas that was resized.
         */
        void onResize(ITextCanvas canvas);
        
        /**
         * Indicate that a line was double-clicked.
         * @param canvas
         * @param lineNumber the number of the line that
         * was double-clicked.
         * @param column the column where the double-click
         * occurred.
         */
        void onDoubleClick(ITextCanvas canvas, int lineNumber, int column);
        
    }
}
