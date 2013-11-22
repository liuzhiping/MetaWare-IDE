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
package com.arc.seecode.internal.display.canvas;

import java.awt.Point;

import com.arc.seecode.internal.display.ISquareCanvas;
import com.arc.seecode.internal.display.ISquareCanvasCallback;
import com.arc.widgets.ITextCanvas;

/**
 * @author David Pickens
 */
class TextCanvasObserver implements ITextCanvas.IObserver {

    private ISquareCanvasCallback mCallback;
    private ISquareCanvas mSquareCanvas;
    private boolean mToolTipsEnabled = false;
    private boolean mDoubleClicksInColumn0 = true;
    
    TextCanvasObserver(ISquareCanvas sc, ISquareCanvasCallback callback){
        mCallback = callback;
        mSquareCanvas = sc;
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.ITextCanvas.IObserver#onHover(com.arc.widgets.ITextCanvas, int, int)
     */
    @Override
    public void onHover(ITextCanvas canvas, int lineNumber, int column) {
        if (mToolTipsEnabled){
            mCallback.requestToolTip(mSquareCanvas,lineNumber,column);
        }

    }
    
    void enableToolTips(boolean v){
        mToolTipsEnabled = v;
    }
    
    void enableDoubleClickInColumn0(boolean v){
        mDoubleClicksInColumn0 = v;
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.ITextCanvas.IObserver#onPopup(com.arc.widgets.ITextCanvas, int, int)
     */
    @Override
    public void onPopup(ITextCanvas canvas, int x, int y) {
        Point p = canvas.convertToScreenPoint(x,y);
        mCallback.showPopup(mSquareCanvas,p.x,p.y,
                canvas.getSelection());
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.ITextCanvas.IObserver#onResize(com.arc.widgets.ITextCanvas)
     */
    @Override
    public void onResize(ITextCanvas canvas) {
        mCallback.setLines(mSquareCanvas,canvas.getDisplayableLines(),
                canvas.getDisplayableColumns());
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.ITextCanvas.IObserver#onSelected(com.arc.widgets.ITextCanvas, int, int)
     */
    @Override
    public void onSelected(ITextCanvas canvas, int startLineNumber, int startLineColumn, int endLineNumber) {
        mCallback.setSelectedLine(mSquareCanvas,startLineNumber, startLineColumn, endLineNumber);
        if (startLineColumn < 0)
            mCallback.singleClickOccurred(mSquareCanvas,startLineNumber);
    }
    
    /* (non-Javadoc)
     * @see com.arc.widgets.ITextCanvas.IObserver#onDoubleClick(com.arc.widgets.ITextCanvas, int)
     */
    @Override
    public void onDoubleClick(ITextCanvas canvas, int lineNumber, int column) {
        if (column != 0 || mDoubleClicksInColumn0){
            mCallback.doubleClickOccurred(mSquareCanvas,lineNumber);
        }
        else {
            // E.g.A tree being expanded.
            mCallback.setSelectedLine(mSquareCanvas,lineNumber,0, lineNumber);
        }
    }
    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param canvas
     * @param firstLine
     * @param startColumn
     * @param lastLine
     * @param endColumn
     */
    @Override
    public void onHighlighted (ITextCanvas canvas, int firstLine, int startColumn, int lastLine, int endColumn) {
        mCallback.setSelection(mSquareCanvas,firstLine,startColumn,endColumn,canvas.getSelection());
    }
}
