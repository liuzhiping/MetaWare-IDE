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
package com.arc.seecode.display;

import java.io.File;
import java.util.Map;

import com.arc.widgets.IContainer;
import com.arc.widgets.IToolBarBuilder;

/**
 * 
 * @author David Pickens
 */
public interface ISeeCodeTextViewer {
    /**
     * Return the underlying GUI widget
     * @return the underlying GUI widget
     */
    IContainer getComponent();
    /**
     * To be called when this view is going away.
     * I.e., the pane in which it is rendered is 
     * being disposed.
     *
     */
    void dispose();
    
    /**
     * This method is called when the widget that contains
     * this object is suddenly being disposed of spontaneously,
     * (e.g., when IDE is being shutdown).
     * It closes the display, if not already, and
     * then immediately disposes of it before the engine
     * has a chance to acknowledge the closing. This means
     * that the engine may be in the process of sending
     * updates and won't be notified of the closing until
     * later. Thus, we must prepare to send the updates to the "bit bucket".
     * <P>
     * This prevents repaint operations from being sent
     * to a disposed widget.
     *
     */
    void forceDispose();
    
    /**
     * Return whether or not this display has been disposed.
     * @return whether or not this display has been disposed.
     */
    boolean isDisposed();

    /**
     * Return associated title as it would appear, say, on tab.
     * @return associated title.
     */
    String getTitle();

    /**
     * Do whatever is necessary to close this display.
     * Whatever action to be taken should eventually
     * cause {@link #dispose()} to be called.
     *
     */
    void close();
    /**
     * Returns true if {@link #close()} has been called on this
     * display. It may not necessarily be disposed, however.
     * @return whether or not this display is closing.
     */
    boolean isClosing();

    /**
     * Return the kind of display this is. The returned string
     * is used when saving the state of this display so as to
     * be able to reconstruct it in a later session. Thus, it
     * must be recognized by the underlying debugger engine.
     * @return the kind of display as would be required for
     * saving the state.
     */
    String getDisplayKind();

    /**
     * Clear the content of this display.
     */
    void clear();
    
    /**
     * Set the base title for this display.
     * @param title the base title for this display.
     */
    void setTitle(String title);
    
    /**
     * Return the ID number of the display. Each display has
     * a unique ID number.
     * @return the ID number of this display.
     */
    int getDisplayID();
    /**
     * Record a property update on behalf of this display.
     * {@link #getValueUpdates()} can be called to retrieve all of
     * the pending update values.
     * @param property the name of the property.
     * @param value the value.
     */
    void addValueUpdate(String property, String value);
    
    /**
     * Remove a property that may have been previously recorded. It is not
     * to be restored in the next session.
     * @param property name of property to be removed.
     */
    void removeValueUpdate(String property);
    
    /**
     * Send a "value update" to the engine on behalf of this display.
     * This method is called when restoring the state of a display
     * during initialization. It is expected to call
     * {@link #addValueUpdate} as a side-effect.
     *
     * @param property the name of the property.
     * @param value the value.
     */
    void sendValueUpdate(String property, String value);
    
    /**
     * Return the map of {@linkplain #addValueUpdate(String,String)
     * "value updates"} for this display. We retrieve them
     * so that they can be re-applied between sessions.
     * @return the map of value updates.
     */
    Map<String,String> getValueUpdates();
       
    /**
     * Return whether or not this display's state is to be saved
     * between debug sessions.
     * @return whether or not this display's state is to be saved
     * between debug sessions.
     */
    boolean isPersistent();
    
    
    /**
     * If this display contains profiling data, return a bit string
     * that identifies which columns are present.
     * Otherwise, returns 0.
     * <P>
     * The least-significant-bit corresponds to column 0; the
     * most-significant bit corresponds to column 31.
     * <P>
     * This method is called to save state.
     * 
     * @return a bit string to determine which profiling columns
     * are present.
     */
    int getVisibleColumnBitmap();
    
    /**
     * Given a bitmap of columns that was returned by
     * {@link #getVisibleColumnBitmap()} in a previous session,
     * restore the state appropriately by sending messages to the
     * seecode engine.
     * @param bitmap the visible columns to be restored.
     */
    void setVisibleColumnBitmap(int bitmap);

    /**
     * Return the number of profiling columns that this display
     * contains. Or else returns 0 if there is no profiling stuff.
     * <P>
     * This method is called to save state.
     * @return the number of profiling columns.
     */
    int getNumColumns();

    /**
     * Return the state of toolbar buttons.
     * Used to save the state of this display.
     * @return the state of associated toolbar buttons.
     */
    boolean[] getToolbarsStatus();
    
//    /**
//     * Set a prefix to be prepended to this display's title.
//     * SeeCode prefixes displays with a pane number (e.g. "10-").
//     * @param prefix a prefix to be prepended to the pane title.
//     */
//    void setTitlePrefix(String prefix);
    
    /**
     * Arrange for this display to send back "timer_update" updates
     * every so many milliseconds.
     * <P>
     * If the <code>millis</code> is 0, then the timer is stopped.
     * @param millis the interval at which "timer_update"
     * messages are to be sent from this display.
     * @throws IllegalStateException if this kind of display
     * doesn't support this.
     *
     */
    void setTimerUpdate(int millis);
    
    /**
     * Remove the highlights from this display.
     * @param which identity of highlights.
     */
    void removeHighlights(int which);
    
    /**
     * Set the view port scroll position.
     * @param firstLineInView first line in view.
     * @param linesInView number of lines in view.
     * @param firstLineInModel first line in model which must be
     * at least the value of firstLineInView.
     * @param lastLineInModel the last line in the model, which
     * must be at least the value of firstLineInView+linesInView
     * @param viewIncr at each scroll increment, the amount to move.
     */
    public void setVerticalScroller(int firstLineInView,
            int linesInView,
            int firstLineInModel,
            int lastLineInModel,
            int viewIncr);
    
    /**
     * Scroll the lines that are in the "model"; i.e.,
     * without querying the engine for more.
     *
     * @param lineCount number of lines to scroll, if lineCount > 0
     *                  then scroll forward, else scroll back.
     * @param displaySize number of lines in the model being scrolled
     */
    public void scroll(int lineCount, int displaySize);
    
    
    /**
     * Update attributes of this display from a properties string.
     * @param props_string a property string as acceptable as input
     * to <code>Properties.load()</code>.
     */
    public void updateComponent(String props_string);
    
    /**
     * Associate arbitrary data to this display using "id" as a key.
     * @param id the key for the data.
     * @param data string-representation of the data.
     */
    public void updateComponentData(String id, String data);
 
    
    /**
     * Set or reset the highlight of a line at a given index.
     *
     * @param ix index of line to modify
     * @param engineHighlightCode highlight to set or reset  
     */
    public void setHighlight(int ix, int engineHighlightCode);
    
    /**
     * Set a particular toolbar as visible or invisible
     * @param which which toolbar.
     * @param visible visibility of toolbar.
     */
    public void setToolbar(int which, boolean visible); 
    
    /**
     * Enable or disable a profiling column.
     * @param column the column number (starting with 0)
     * @param enabled whether or not to enable or disable.
     */
    public void setProfile(int column, boolean enabled) ;
    
    /**
     * Set a line to a particular value.
     * @param line the line number.
     * @param content the new contents of the line.
     */
    public void setLine(int line, String content);
    
    /**
     * Clear a range of lines.
     * @param line the first line to clear.
     * @param cnt the number of lines to clear.
     */
    public void setBlankLines(int line,int cnt);
    
    /**
     * An input event is one in which a text field, combobox,
     * or button is programmatically filled in or selected.
     * The engine does such things as part of its processing of Examine
     * window. Perhaps elsewhere also.
     * @param component the name of a component being modified.
     * @param text associated text, or combobox selection.
     * @param type_tail if true, modify the tail of existing text only.
     * @throws IllegalArgumentException if "component" could not be found.
     */
    public void inputText(
        String component,
        String text,
        boolean type_tail) throws IllegalArgumentException;
    
    /**
     * Do whatever is necessary to populate this display.
     *
     */
    public void populate();
    
    
    /**
     * Refresh the contents of this display.
     */
    public void refresh();
    
    /**
     * Enable or disable tooltips. When enabled, a hover is suppose to send a
     * "hover" value update to the engine, which then replies with a
     * show_tooltip command.
     * 
     * @param v
     *                if true, enable tooltips
     */
    public void setEnableToolTips(boolean v);
 
    /**
     * Display tooltip "tip" near line "line" and column "col". The engine
     * requests this as a reply to the "hover" command.
     * 
     * @param tip
     *                the tool tip
     * @param line
     *                the line near where the tooltip is to be displayed
     * @param column
     *                the column
     */
    public void showToolTip(String tip, int line, int column);
    
    /**
     * Programmatically click a button with the given name within the toolbar
     * of this display.
     * @param buttonName name of button.
     * 
     */
    public void clickButton(String buttonName);
    
    /**
     * 
     * Set the data for a profiling column. Displays can have
     * columns on the left in which arbitrary text can be seen.
     *
     * @param column the column to be set.
     * @param lines the lines of the column.
     */
    public void setProfileColumns(int column, String[]lines);
    
    /**
     * Make this display visible. If it is a panel within a tabbed
     * pane, then bring the tab containing this display to the top.
     *
     */
    public void show();
    
    /**
     * Set the background colors to denote overlays, read-only memory, and misaligned memory.
     * This method may be called when the user changes preference settings to it must be
     * responsible for for repainting the display to reflect the changes.
     * <P>
     * If this method is not called with a non-null value, then such things will not
     * be highlighted.
     * @param prefs interface to retrieve background colors.
     */
    public void setColorPreferences(IColorPreferences prefs);
    
    /**
     * Set the font of the display. We don't presume on the type of the Font
     * object so that this interface can work for SWT and Swing environments.
     * @param font the new font to be set.
     */
    public void setFont(Object font);
    
    /**
     * Set the default directory that is to be used for, say, save-to-file
     * dialogs.  Under Eclipse, it is the path of the project.
     * @param directory the directory to be used as the default for any
     * dialogs that need to be rendered.
     */
    public void setDefaultDirectory(File directory);
    
    /**
     * Indicate that the engine has deleted this display, either as a result of an
     * explicit user action, or implicitly as a side-effect of deleting a parent display.
     * The engine is not expected to have knowledge of this display afterward.
     */
    public void setDeleted();
    
    /**
     * Get associated toolbar builder. We make it externally available for the
     * benefit of the Eclipse implementation that must access the information
     * therein to populate the IToolBarManager object.
     * @return the associated toolbar builder.
     */
    public IToolBarBuilder getToolBarBuilder();
    
    /**
     * Return the debugger's installation path (e.g., "C:/arc/metaware/arc").
     * @return the debugger's installation path.
     */
    public String getDebuggerInstallPath();
    
    /**
     * A name to be assigned to underlying "ITextCanvas" widget so that
     * the GUI tester can access it. WindowTester's window search mechanism
     * has issues finding stuff if it isn't very explicit.
     * @param name name to be assigned underlying widget.
     */
    public void setName(String name);
    
    /**
     * Return the help context ID.
     * @return the help context ID.
     */
    public String getHelpID();
    
    /**
     * If the context ID from {@link #getHelpID} is not known, then this one is used.
     * Typically, this one will show the first page of online help.
     * @return a context help ID if that of {@link #getHelpID} is not recognized.
     */
    public String getDefaultHelpID();
    
    /**
     * Return the selected text.
     *
     * @return the selected text or empty string.
     */
    public String getSelection();
    
    /**
     * Set a range of text to be highlighted as "selected".
     * @param line1 starting line, 0 based.
     * @param col1 starting column in line, 0 based.
     * @param line2 end line, inclusive, 0 based.
     * @param col2 end column, inclusive, 0 based.
     */
    void setSelection(int line1, int col1, int line2, int col2);
     
}
