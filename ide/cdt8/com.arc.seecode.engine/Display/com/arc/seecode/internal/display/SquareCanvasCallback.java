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

import com.arc.seecode.display.IClipboardDisplayCopier;
import com.arc.seecode.display.MenuBuilder;
import com.arc.seecode.display.MenuDescriptor;
import com.arc.seecode.internal.display.panels.ExtensionsPanel;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IMenu;

/**
 * The callback for the square canvas widget.
 * @author David Pickens
 */
class SquareCanvasCallback implements ISquareCanvasCallback {
    private IValueSender mSender;
    private boolean mIsClosing;
    private SeeCodeTextViewer mViewer;
    private IComponentFactory mFactory;
    private int mLastLineCount = -1;
    private IClipboardDisplayCopier fClipboard;
    /**
     * 
     * @param s where engine "value update" messages sent.
     * @param viewer associated viewer in which the 
     * associated canvas is located.
     * @param factory factory which creates a popup menu.
     */
    SquareCanvasCallback(IValueSender s, SeeCodeTextViewer viewer,
            IClipboardDisplayCopier clipboard,
            IComponentFactory factory){
        if (s == null || viewer == null || clipboard == null || factory == null)
            throw new IllegalArgumentException("null argument");
        mSender = s;
        mViewer = viewer;
        mFactory = factory;
        fClipboard = clipboard;
    }
    
    @Override
    public boolean copyAllToClipboard(ISquareCanvas canvas){
        return fClipboard.copyAllToClipboard(mViewer);
    }

    /**
     * Indicate that associated viewer is shutting down
     * and avoid processing any residual commands.
     * @param v if true, indicate associated viewer is closing.
     */
    void setClosing(boolean v){
        mIsClosing = v;
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.internal.display.ISquareCanvasCallback#requestToolTip(com.arc.seecode.internal.display.ISquareCanvas, int, int)
     */
    @Override
    public void requestToolTip(ISquareCanvas canvas, int line, int column) {
        ExtensionsPanel p = mViewer.getExtensionsPanel();
        if (canvas.getWhich() == 0 && p != null && !mIsClosing) {
            if (column < 0) column = 0;
            else column++;  // engine's columns are 1 based
            mSender.sendValueUpdate("hover", "" + column + " " + line);
            p.doToolTipsClick();
        }

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.internal.display.ISquareCanvasCallback#doubleClickOccurred(com.arc.seecode.internal.display.ISquareCanvas, int)
     */
    @Override
    public void doubleClickOccurred(ISquareCanvas canvas, int line) {
        ExtensionsPanel p = mViewer.getExtensionsPanel();
        if (p != null && !mIsClosing){
            p.doDoubleClick();
        }

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.internal.display.ISquareCanvasCallback#singleClickOccurred(com.arc.seecode.internal.display.ISquareCanvas, int)
     */
    @Override
    public void singleClickOccurred(ISquareCanvas canvas, int line) {
        ExtensionsPanel p = mViewer.getExtensionsPanel();
        if (p != null && !mIsClosing){
            p.doColumn0Click();
        }

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.internal.display.ISquareCanvasCallback#setSelectedLine(com.arc.seecode.internal.display.ISquareCanvas, int)
     */
    @Override
    public void setSelectedLine(ISquareCanvas canvas, int line, int column, int endLine) {
        boolean isDrag = false;
        if (!mIsClosing){
            ExtensionsPanel p = mViewer.getExtensionsPanel();
            if (p != null)
                p.setSelection(line,column,column,"");
            int which = canvas.getWhich();
            if (which == 0){
                // Engine's column position is 1 based
                if (column < 0) column = 0;
                else column++;

                if(endLine > line) isDrag = true;
                mSender.sendValueUpdate("sel_line", line+ " " + column + " " + isDrag);
            }
        }
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.internal.display.ISquareCanvasCallback#setLines(com.arc.seecode.internal.display.ISquareCanvas, int, int)
     */
    @Override
    public void setLines(ISquareCanvas canvas, int lineCount, int width) {
        if (!mIsClosing && canvas.getWhich() == 0) {       
            mViewer.setDisplayableColumns(width);
            if (lineCount != mLastLineCount){
                mLastLineCount = lineCount;
                mViewer.setDisplayableLines(lineCount);
                mSender.sendValueUpdate("lines", "" + lineCount 
                    + " " + width, false);
            }
        }

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.internal.display.ISquareCanvasCallback#showPopup(com.arc.seecode.internal.display.ISquareCanvas, int, int, java.lang.String)
     */
    @Override
    public void showPopup(ISquareCanvas canvas, int x, int y, String selection) {
        IMenu menu = mFactory.makePopupMenu(canvas.getComponent());
        MenuDescriptor md = mViewer.getPopupMenu(selection);
        MenuBuilder builder = new MenuBuilder(mFactory);
        md.generate(menu,builder);
        menu.addMenuObserver(new IMenu.IObserver(){

            @Override
            public void menuShown(IMenu m) {              
            }

            @Override
            public void menuHidden(IMenu m) {
                m.removeMenuObserver(this);               
                m.dispose();
                
            }});
        menu.show(x,y);
    }


    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param canvas
     * @param line
     * @param startColumn
     * @param endColumn
     * @param selection
     */
    @Override
    public void setSelection (ISquareCanvas canvas, int line, int startColumn, int endColumn, String selection) {
        ExtensionsPanel p = mViewer.getExtensionsPanel();
        if (p != null)
            p.setSelection(line,startColumn,endColumn,selection);
    }

}
