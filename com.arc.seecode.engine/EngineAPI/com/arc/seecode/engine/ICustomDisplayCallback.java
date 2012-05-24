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
package com.arc.seecode.engine;

/**
 * A callback interface by which the engine controls the contents
 * of a custom display.
 * <P>
 * NOTE: in the previous version of SeeCode, all displays were "custom"
 * in the sense that the engine was responsible for creating and
 * maintaining them. In the new scheme, only custom displays are
 * maintained by the engine. The generic displays are controlled
 * completely by the UI.
 * 
 * <i>Implementation note</i>
 * When connected to the SeeCode engine, all calls through this
 * interface originate from the engine, which is running as a separate
 * process. The engine process reads commands from the GUI
 * in a read/dispatch loop. A method in this interface could
 * invoke an engine method, which would hang because the
 * engine is busy servicing the command that is calling
 * the method! To get around this, the engine generally
 * does not directly call the methods in this interface,
 * but, instead, enqueues them to be invoke in another
 * thread. (See @[link com.arc.seecode.server.CustomDisplayCallback CustomDisplayCallback}
 * used by the engine server process. 
 * This permits methods in this interface to make calls back into
 * the engine.
 * <P>
 * However, for any method that returns a result,
 * the engine  must wait for it to return.
 * For example, the method
 * {@link #chooseFromList}. Such a method must <i>not</i>
 * make calls into the engine  -- directly or indirectly,
 * or else a deadlock will occurs.
 * 
 * @author David Pickens
 */
public interface ICustomDisplayCallback {
    /**
     * The engine calls this method to create a custom
     * display from a property list.
     *
     * It returns a display ID that can be referenced subsequently
     * by the engine.
     * <P>
     * TODO: document format of properties string.
     * <P>
     * @param properties a string of properties that can be loaded
     * by the <code>Properties.load</code> method.
     * @param id the associated window number
     * by which the engine will reference this window
     * subsequently.
     */
    void createDisplay(String properties, int id);
    /**
     * Update properties associated with a display.
     * <P>
     * TODO: document format of properties string.
     * <P>
     * @param displayID display ID
     * @param properties property string that can be loaded
     * via <code>Properties.load()</code>.
     */
    void updateComponent(int displayID, String properties);
    /**
     * Update the data that is somehow associated with the
     * toolbar panel of a display. The update won't be seen until
     * the engine subsequently calls {@link #updateComponent(int,String)}.
     * 
     * @param displayID the display ID.
     * @param componentID the name of a component in the toolbar panel.
     * @param data the data (such as an encoding of an image).
     */
    void updateComponentData(int displayID, String componentID,
            String data);
    
    /**
     * Set a line of text within a display. The display
     * need not actually change until {@link #refreshDisplay} is
     * called on this id.
     * @param id the id of the display.
     * @param lineNumber the line number being set; 0 is the first line.
     * @param line the text of the line.
     */        
    void setLine(int id, int lineNumber, String line);
    
    /**
     * Set blank lines in a custom display.
     * The display
     * need not actually change until {@link #refreshDisplay} is
     * called on this id.
     * @param id display ID
     * @param lineNumber the first line number to be blanked out.
     * @param cnt the number of lines to be blanked out.
     */
    void setBlanks(int id, int lineNumber, int cnt);
    
    /**
     * Set the highlight value of a line in a display.
     * @param id the display ID.
     * <P>
     * The display
     * need not actually change until {@link #refreshDisplay} is
     * called on this id.
     * @param lineNumber the line number be highlighted.
     * @param highlightValue the highlight value [enumerate these!].
     */
    void setHighlight(int id, int lineNumber, int highlightValue);
    
    /**
     * Remove highlights from a display.
     * <P>
     * The display
     * need not actually change until {@link #refreshDisplay} is
     * called on this id.
     * @param id the display ID.
     * @param highlight the highlight to be removed.
     */
    void removeHighlights(int id, int highlight);
    
    /**
     * Create a user-defined display and create a menu-item some
     * place to invoke it.
     * <P>
     * The display is expected to communicate with the engine by
     * invoking the {@link EngineInterface#sendValueUpdate(int,String,String)} method.
     * <P>
     * TODO: document what is passed to {@link EngineInterface#sendValueUpdate(int,String,String) sendValueUpdate}
     * method.
     * <P>
     * @param id an id number to be sent back to the engine via
     * the {@link EngineInterface#sendValueUpdate(int,String,String)} method [need more information!].
     * @param guihiliSpec guihili specification for the dialog.
     */
    void createUserDisplay(int id, String guihiliSpec);
    
    /**
     * Arrange for a "update timer" to invoke
     * {@link EngineInterface#sendValueUpdate}
     * with property "update_timer" every so-many milliseconds
     * on behalf of a display.
     * <P>
     * @param id the id number of a display, or 0 if
     * there is no associated display.
     * @param millisec the number of milliseconds between
     * updates; <= 0 will kill updates altogether.
     */
    void setTimerUpdate(int id, int millisec);
    
    /**
     * Bring up a dialog box to change an element of
     * a display. 
     * <P>
     * The result (if not canceled) is sent back to
     * the engine by calling the 
     * {@link IEngineAPI#invokeCommand}
     * with with the value of <code>cmd</code> augmented
     * as follows: Any occurance of "%s" is substituted with
     * new value. If there is no "%s", then the value
     * will be appended to the cmd.
     * <P>
     * @param id the id number of the display
     * @param name the nameo the element (e.g., "register")
     * @param old the current value of the element.
     * @param cmd the value to be sent back via the
     * {@link IEngineAPI#invokeCommand}.
     */
    void doChangeDialog(int id, String name,String old,String cmd);
    
    /**
     * Scroll the display by the given amount, based
     * on the lines that are being tracked via
     * {@link #setScrollBar} values.
     * @param id the id number of the display to be scrolled.
     * @param amount the number of lines to scroll; >0 means scroll
     * down; <0 means scroll up.
     * @param displaySize the number of lines showing.
     */
    void scroll(int id, int amount, int displaySize);
    
    /**
     * Set the vertical scrollbar attributes for a
     * display.
     * @param id the id number of the display.
     * @param current the number of the line to be shown at the top of 
     * the display.
     * @param displaySize the number of lines in the display.
     * @param first the line number of the first line
     * to be buffered (must be <= <code>current</code>).
     * @param last the line number of the last line
     * to be buffered (must be >= <code>current+displaySize</code>)
     * @param blockIncrement amount to scrool when white part of
     * scrollbar clicked.
     */
    void setScrollBar(int id, int current, int displaySize, int first, int last,
            int blockIncrement);
    
    /**
     * Set the status field on behalf of a display window.
     * @param id the display window id number.
     * @param msg the message to display, or <code>null</code>
     * if the status is to be cleared.
     */
    void setStatus(int id, String msg);
    
    /**
     * Display an error box (modal?) on behalf of a display.
     * @param id the display window id number.
     * @param msg the message to appear in the message box.
     */
    void displayErrorBox(int id, String msg);
    
    /**
     * Clear the given display window.
     * @param id the id number of the window to be cleared.
     */
    void clear(int id);
    
    /**
     * Create a progress monitor display. And display
     * an appropriate amount of time if it isn't
     * destroyed in the mean time.
     * <P>
     * The display is constructed from a property
     * string with the following values:
     * <P>
     * <dl>
     * <dt><code>title</code>
     * <dd><i>(optional)</i> the title of the
     * progress monitor. If omitted, "Progress" is assumed.
     * <dt><code>cancel</code>
     * <dd><i>(optional)</i> if present, and is not
     * equal to "0", it causes a "Cancel" button to
     * appear on the progress bar.
     * <dt><code>delay</code>
     * <dd><i>(optional)</i>the number of milliseconds
     * to elapsed before the progress monitor is to
     * appear. If it is destroyed within this time it
     * won't appear at all. If this property is omitted,
     * an appropriate default is used (e.g,. 100).
     * </dl>
     * <P>
     * If the progress display has "Cancel" button and
     * the button is pressed, then the 
     * method {@link IEngineAPI#progressDisplayCanceled}
     * will be called.
     * <P>
     * @param id the number by which this progress display
     * will be referenced subsequently (e.g.,{@link #destroyProgressDisplay}).
     * @param properties a property string that defines
     * the attributes of the progress display.
     */
    void createProgressDisplay(int id, String properties);
    
    /**
     * Destroy the progress monitor with the given ID.
     * If the progress display hasn't yet appeared, it won't.
     * And if it is being displayed, it is disposed of.
     * @param id the progress display id number.
     */
    void destroyProgressDisplay(int id);
    
    /**
     * Set the progress value on a progress display.
     * A value of -1 means "indetermanent".
     * @param id the id of the progress display.
     * @param percent  a value between 0 and 100 to set
     * the progress to, or -1 to indicate unknown progress.
     */
    void setProgressPercentage(int id, int percent);
    
//    /**
//     * Return whether or not a progress display is
//     * canceled.
//     * <P>
//     * <i>Implementation caveat</i><p>
//     * When connecting to the SeeCode engine, the
//     * caller of this method will be in a different
//     * process and ordinarily doesn't wait for the result
//     * so as to avoid deadlock (see note for this class).
//     * But the result of this method is required immediately,
//     * so the engine will hang to wait for the result.
//     * It must <i>not</i> invoke an engine method, or else
//     * a deadlock <i><u>will</u></i> occur.
//     * <P>
//     * Thus, there is a slight chance that it could be called
//     * before {@link #createProgressDisplay} (i.e., while
//     * a call to the latter is enqueued). Thus, it should
//     * return false if the progress display id is not
//     * yet recognized.
//     * @param id the progress display id that was
//     * previously passed to {@link #createProgressDisplay},
//     * but may not yet be known (see note above).
//     * @return true if the progress display is canceled;
//     * false if not canceled or not yet recognized.
//     */
//    boolean isProgressDisplayCanceled(int id);
    
    /**
     * Display a dynamically-generated "guihili" dialog
     * that saves its output in an argument file whose name
     * is supplied in the properties.
     * <P>
     * The dialog is described by a property stream:
     * <dl>
     * <dt><code>on_push</code>
     * <dd>one of the substrings to be passed back to
     * the engine via {@link IEngineAPI#sendValueUpdate}.
     * <dt><code>options_code</code>
     * <dd>the <i>guihili</i> specification of the dialog.
     * <dt><code>file_prefix</code>
     * <dd><i>(required)</i> a file prefix from which
     * the generated argument file is named. The name
     * is derived in a manner that the engine and
     * GUI presumably agree upon.
     * <dt><code>title</code>
     * <dd> <i>(required)</i>the title to appear in the title bar of the
     * generated dialog.
     * <dt><code>options_name</code>
     * <dd>a synonym for <code>title</code>.
     * </dl>
     * <P>
     * When the "OK" button is pressed for the dialog,
     * {@link IEngineAPI#sendValueUpdate(int,String,String)} is called with
     * the following two arguments:
     * <pre>
     *         "user_gui_selection"
     *         "<i>on_push</i> ugui <i>gui_id</i>"
     * </pre>
     * @param displayID the associated display window id,or 0.
     * back in the call to {@link IEngineAPI#sendValueUpdate}.
     * @param gui_id the <i>gui_id</i> parameter send back to 
     * the engine.
     * @param props properties from which the dialog is built.
     */
    void doOptionsWindow(int displayID, int gui_id, String props);
    
    /**
     * Set the titlebar of the display.
     * @param id the id number of the window display.
     * @param title the new associted title.
     */
    void setTitle(int id, String title);
    
    /**
     * Force the "Displays" menu for
     * creating SeeCode displays to be regenerated
     * because new display types were added or removed.
     * @deprecated as of interface version 9, {@link #setDisplaySelectors} is now called by
     * the engine to set the selectors. 
     *
     */
    @Deprecated
	void refreshDisplaysMenu();
    
    /**
     * A register, variable, or memory location
     * has (possibly) been changed. Cause all
     * related views of these things to update themselves
     * if they are in auto-update mode.
     */
    void updateViews();
    
    /**
     * Bring up a modal dialog box containing a list
     * with a top most label that has the given
     * prompt (e.g. "Select from following list of symbols...".
     * <P>
     * The method returns the index of the item that the
     * user selects, or -1 if the user canceled the selection.
     * <P>
     * <i>Caveat:</i>
     * The engine hangs while waiting for the result of this
     * call. This method most <i>not</i> make a call back into
     * the engine or else it will deadlock (the engine runs
     * in a separate process and handles calls from the
     * GUI in a read/dispatch loop.). See the note for 
     * this interface above.
     * <P>
     * <i>Caveat#2:</i>
     * Under Eclipse the thread that invokes the engine is typically
     * the main UI thread. Thus, if this method is going to display a
     * list, it will deadlock! To get around this, the 
     * {@linkplain com.arc.seecode.server.Server server} side that
     * talks directly to the engine doesn't wait for the
     * engine if it is calling a method that returns void.
     * In other words, any engine method that returns void is
     * invoked ascynchronously w.r.t. the to the GUI thread.
     * <P>
     * @param prompt the prompt to appear
     * @param list list of strings that the user
     * is to selected from.
     * @return the index of the item selected or -1 if the
     * user canceled the selection.
     */
    int chooseFromList(String prompt, String list[]);
    
    /**
     * Dispose of the display with the given ID number.
     * This method is called from the engine in response
     * to the {@link IEngineAPI#closeDisplay} call.
     * @param id the display to be deleted.
     */
    void deleteDisplay(int id);
    
    /**
     * Refresh the contents of the given display.
     * This is called after a series of {@link #setLine}
     * calls to tell the display to update itself.
     * @param id the display to be refreshed.
     */
    void refreshDisplay(int id);
    
    /**
     * This method sets the profiling columns on
     * a display that contains Source.
     * <p>
     * The data is a string of tokens separated by
     * whitespace. Each token occupies one line in the
     * column.
     * @param id the display ID.
     * @param column the column number affected.
     * @param data a sequence of tokens separated by whitespace,
     * one per line.
     */
    void setProfileData(int id, int column, String data);
    
    /**
     * Do whatever is required to make sure the given display is
     * visible. If it is in a tabbed frame, it moves it to the top.
     * 
     * @param id the display ID.
     */
    void show(int id);
    
    /**
     * Type text into a text field or an (editable) combobox.
     * If <code>tail</code> is set, then only the tail portion of the
     * existing text is to be changed. The first character of the next
     * text is used to match that where the new text is to be appended.

     * @param id the display ID.
     * @param componentName the name of the text field or combobox.
     * @param text the new text.
     * @param tail if true, then replace on the tail portion of the existing
     * contents.
     */
    void typeText(int id, String componentName, String text, boolean tail);
    
    /**
     * Execute the given command sometime in the near future.
     * <P>
     * Actually, in practice, the engine doesn't seem to ever call it.
     * But we leave it because eventually we may move the command processor 
     * out of the engine altogether, and we'll need a hook to implement
     * the "exec" expression of a breakpoint.
     * <P>
     * @param command the command to be enqueued for execution.
     */
    void queueCommand(String command);
    
    /**
     * Set the display selectors. Each selector corresponds to an engine-generated display.
     * The UI directs the engine to create a display by calling {@link EngineInterface#createDisplay}.
     * <P>
     * This method will also be called when the selectors change due to user's DLL registering new ones.
     * <P>
     * NOTE: this method was added in engine interface version 9.
     * @param selectors the array of display selectors that the engine recognizes.
     */
    void setDisplaySelectors(String[] selectors);
    

    /**
     * A recognize property that the debugger engine can set by calling
     * @{link {@link #setProperty(String, String)}}.
     * <P>
     * It indicates that the watchpoint dialog should have an option for a mask
     * and value.
     */
    public static final String WATCHPOINT_VALUE_MASK = "watchpoint_value_mask"; 
    
    /**
     * This is a hook by which the debugger can set esoteric properties pertaining
     * to the UI. For example, the ARC target can set "watchpoint_value" to "1" to
     * indicate that the watchpoint dialog supports mask and value.
     * @param key property name
     * @param value property value.
     * <P>
     * NOTE: this method was added in interface version 12.
     * @new
     */
    void setProperty(String key, String value);
    
    /**
     * Start animating the debugger by invoking the given comman repeatedly so-many-times
     * per minute. If the command is not recognized, or is not supported for animation,
     * or if the debugger is not in a state to start animating, then an error box will 
     * pop up to notify the user of that fact.
     * <P>
     * NOTE: a subsequent stop event will terminate animation.
     * @param tid the ID of the thread to be animated, or 0, if it is to apply to the
     * selected or "current" thread.
     * @param cmd the command to animate: "ssi", "sso", "isi", or "iso".
     * <P>
     * Added in version 14 of the interface.
     * @new
     */
    void animate(int tid, String cmd);
    
    /**
     * Indicate that the debugger is about to request a license. It should take no more time than indicated by
     * the timeout argument. When the license is granted or denied, the method {@link #onLicenseRequestEnd} will be
     * called.
     * <P>
     * The implementation is expected to pop up some sort of progress bar if the license server doesn't respond in, say,
     * 3 seconds.
     * @param timeoutMillis the maximum number of milliseconds that the  engine is expected to take in waiting from
     * a response from the license server.
     * <P>
     * Added in version 19 of the interface.
     * @new
     */
    void onLicenseRequestStart(int timeoutMillis);
    
    /**
     * Called after {@link #onLicenseRequestStart} to indicate that the wait for a license is over.
     * If the UI popped up a progress monitor, it can now be canceled.
     * <P>
     * If the license was not granted, a subsequent call to <code>IEngineAPIObserver.licensingFailure()</code> will be
     * called subsequently.
     * @param granted whether or not the license was granted.
     *  <P>
     * Added in version 19 of the interface.
     * @new
     */
    void onLicenseRequestEnd(boolean granted);
    
    /**
     * Prompt for a file path and return it.
     * Returns <code>null</code> if the chooser dialog was canceled.
     * <P>
     * <i>Caveat:</i>
     * The engine hangs while waiting for the result of this
     * call. This method most <i>not</i> make a call back into
     * the engine or else it will deadlock (the engine runs
     * in a separate process and handles calls from the
     * GUI in a read/dispatch loop.). See the note for 
     * this interface above.
     * <P>
     * Recognized properties:
     * <dl>
     * <dt>title
     * <dd>(string) Title for the file chooser dialog.
     * <dt>output
     * <dd>(boolean) if 1, the file is to be written; otherwise read.
     * <dt>type
     * <dd>(string) if "dir", then a directory path is being selected. If "file", a file path. Any other type
     * is not recognized.
     * <dt> dir
     * <dd>(string) the initial directory to choose from.
     * <dt>file
     * <dd>(string) the initial file to select.
     * <dt>ext
     * <dd> the file extension to be sought.
     * <dt>description
     * <dd>a description of the file.
     * </dl>
     * @param props
     * @return the selected file or <codeE>null</code> if canceled.
     * <p>
     * Added in version 25 of the interface.
     * @new
     */
    String promptForFile(String props);
    /**
     * Show a "splash" screen or "About box".
     * <P>
     * Recognized properties:
     * <dl>
     * <dt>text
     * <dd>(string) text to be displayed. Newlines are permitted and do what is expected.
     * <dt>x
     * <dd> (integer) the X coordinate, in pixels, relative to the image where the text is to be written.
     * <dt>y
     * <dd> (integer) the Y coordinate, in pixels, relative to the image where the text is to be written.
     * </dl>
     * @param imagePath the image for the splash screen. The size of the box will be the size of the image.
     * @param props properties for setting text.
     * <p>
     * Added in version 25 of the interface.
     * @new
     */
    void showSplash(String imagePath, String props);
    
    /**
     * Display help page.
     * @param props the only property of interest is "id".
     * @new
     */
    void showHelp(String props);
    
    /**
     * Copy the given string to the clipboard. This method is invoked by the engine
     * to perform a {@link IEngineAPI#copyAllToClipboard} operation.
     * <P>
     * The string could be an arbitrary number of lines and could be quite large
     * (E.g., History display).
     * <P>
     * This method was added in version 27 of the API interface.
     * 
     * @param s the string to be copied to the clip board.
     * @new
     */
    void copyToClipboard(String s);
    
    /**
     * Copy the visible content of the associated display to the clip board.
     * This method is called by the engine to perform a {@link IEngineAPI#copyAllToClipboard}
     * when the implementation of the display does not support a copy-all operation (e.g.,
     * disassembly and memory displays).
     * <P>
     * This method was added in version 27 of the API interface.
     * 
     * @param id the associated display ID.
     * @new
     */
    void copyVisibleToClipboard(int id);
    
    /**
     * Highlight a selection of text in the given display.
     * <p>
     * This method was added in version 30 of the API interface.
     * <P>
     * @param id the display ID.
     * @param line1 the line of the start of the selection (0 based).
     * @param col1 the column of the start of the selection (0 based).
     * @param line2 the line of the end of the selection (0 based).
     * @param col2 the column of the end of the selection (inclusive, 0 based).
     * @new
     */
    void setSelection(int id, int line1, int col1, int line2, int col2);
    
    /**
     * Do whatever is necessary to "select" the given stack frame of a thread.
     * The Debug view is to show the selection appropriately.
     * <P>
     * This method was added in version 30 of the API interface.
     * <P>
     * @param threadID the ID of the thread.
     * @param stacklevel the stack level to be highlighted, where 0 is the top; 1 is the next level down, etc.
     * @new
     */
    void selectStackframe(int threadID, int stacklevel);
    
    /**
     *  Send a state string from which the given display can be restored
     *  when the debugger session is restored.
     *  <P>
     *  The restoration is done by calling {@link EngineInterface#sentValueUpdate} with
     *  property "init_state" on behalf of the display.
     * @param id the ID of the display.
     * @param state an arbitrary string that has meaning to the engine for
     * restoring the state of this string.
     * @new
     */
   
    void recordInitState(int id, String state);
    
}
