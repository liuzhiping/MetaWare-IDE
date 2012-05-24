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

import java.io.File;
import java.util.Map;

import com.arc.seecode.display.IColorPreferences;
import com.arc.seecode.display.ISeeCodeTextViewer;
import com.arc.widgets.IContainer;
import com.arc.widgets.IToolBarBuilder;

/**
 * This is a dummy display viewer that we create to
 * replace one that was forceably closed by the GUI
 * system. It receives any pending updates from the engine
 * that it may be transmitting before being made aware that
 * the display was closed.
 * @author David Pickens
 */
class DummyViewer implements ISeeCodeTextViewer {

    private int mID;

    /**
     * 
     */
    public DummyViewer(int id) {
        mID = id;
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#getComponent()
     */
    @Override
    public IContainer getComponent() {
        return null;
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#dispose()
     */
    @Override
    public void dispose() {

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#forceDispose()
     */
    @Override
    public void forceDispose() {

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#isDisposed()
     */
    @Override
    public boolean isDisposed() {
        return true;
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#getTitle()
     */
    @Override
    public String getTitle() {
        return "";
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#close()
     */
    @Override
    public void close() {
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#isClosing()
     */
    @Override
    public boolean isClosing() {
        return true;
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#getDisplayKind()
     */
    @Override
    public String getDisplayKind() {
        return "";
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#clear()
     */
    @Override
    public void clear() {
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#setTitle(java.lang.String)
     */
    @Override
    public void setTitle(String title) {
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#getDisplayID()
     */
    @Override
    public int getDisplayID() {
        return mID;
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#addValueUpdate(java.lang.String, java.lang.String)
     */
    @Override
    public void addValueUpdate(String property, String value) {
    }
    
    @Override
    public void removeValueUpdate(String property){}

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#getValueUpdates()
     */
    @Override
    public Map<String,String> getValueUpdates() {
        return null;
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#isPersistent()
     */
    @Override
    public boolean isPersistent() {
        return false;
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#getVisibleColumnBitmap()
     */
    @Override
    public int getVisibleColumnBitmap() {
        return 0;
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#getNumColumns()
     */
    @Override
    public int getNumColumns() {
        return 0;
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#getToolbarsStatus()
     */
    @Override
    public boolean[] getToolbarsStatus() {
        return null;
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#setTimerUpdate(int)
     */
    @Override
    public void setTimerUpdate(int millis) {
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#removeHighlights(int)
     */
    @Override
    public void removeHighlights(int which) {


    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#setVerticalScroller(int, int, int, int, int)
     */
    @Override
    public void setVerticalScroller(int firstLineInView, int linesInView,
            int firstLineInModel, int lastLineInModel, int viewIncr) {

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#scroll(int, int)
     */
    @Override
    public void scroll(int lineCount, int displaySize) {

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#updateComponent(java.lang.String)
     */
    @Override
    public void updateComponent(String props_string) {
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#updateComponentData(java.lang.String, java.lang.String)
     */
    @Override
    public void updateComponentData(String id, String data) {
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#setHighlight(int, int)
     */
    @Override
    public void setHighlight(int ix, int engineHighlightCode) {

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#setToolbar(int, boolean)
     */
    @Override
    public void setToolbar(int which, boolean visible) {
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#setProfile(int, boolean)
     */
    @Override
    public void setProfile(int column, boolean enabled) {

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#setLine(int, java.lang.String)
     */
    @Override
    public void setLine(int line, String content) {

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#setBlankLines(int, int)
     */
    @Override
    public void setBlankLines(int line, int cnt) {
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#inputText(java.lang.String, java.lang.String, boolean)
     */
    @Override
    public void inputText(String component, String text, boolean type_tail) {
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#populate()
     */
    @Override
    public void populate() {

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#refresh()
     */
    @Override
    public void refresh() {

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#setEnableToolTips(boolean)
     */
    @Override
    public void setEnableToolTips(boolean v) {

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#showToolTip(java.lang.String, int, int)
     */
    @Override
    public void showToolTip(String tip, int line, int column) {

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#clickButton(java.lang.String)
     */
    @Override
    public void clickButton(String buttonName) {

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#setProfileColumns(int, java.lang.String[])
     */
    @Override
    public void setProfileColumns(int column, String[] lines) {
    
    }

    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * 
     */
    @Override
    public void show () {
        
    }

    
    @Override
    public void setVisibleColumnBitmap (int bitmap) {
      
    }


    @Override
    public void sendValueUpdate (String property, String value) {     
    }

   
    @Override
    public void setFont (Object font) {       
    }


    @Override
    public void setDefaultDirectory (File directory) {
        
    }

    @Override
    public void setDeleted () {
        
    }

    @Override
    public IToolBarBuilder getToolBarBuilder() {
        return null;
    }

    @Override
    public void setColorPreferences (IColorPreferences prefs) {
        
    }

    @Override
    public String getDebuggerInstallPath () {
        return null;
    }

    @Override
    public void setName (String name) {
        // @todo Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#getDefaultHelpID()
     */
    @Override
    public String getDefaultHelpID () {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#getHelpID()
     */
    @Override
    public String getHelpID () {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSelection () {
        return "";
    }

    @Override
    public void setSelection (int line1, int col1, int line2, int col2) {
        // TODO Auto-generated method stub
        
    }

}
