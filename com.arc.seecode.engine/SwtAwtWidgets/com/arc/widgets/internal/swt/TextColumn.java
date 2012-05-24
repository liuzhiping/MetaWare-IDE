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
package com.arc.widgets.internal.swt;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Composite;

import com.arc.widgets.IAttributedString;
import com.arc.widgets.IColor;
import com.arc.widgets.IContainer;
import com.arc.widgets.ICopyToClipboard;
import com.arc.widgets.IScrollBar;
import com.arc.widgets.IScrollPane;
import com.arc.widgets.ITextCanvas;
import com.arc.widgets.ITextCanvas.IObserver;

/**
 * The text portion of the {@link SWTTextCanvas}class.
 * <P>
 * <B>NOTE:</B> this class is made public because the GUI tester needs to be able to
 * reference it.
 * 
 * @author David Pickens
 */
public class TextColumn extends Canvas {
    private int mLineHeight = 1; // will be reset by "setFont"

    private int mFirstColumn = 0;

    private int mSelectedLineStart = -1;
    private int mSelectedLineEnd = -2;

    private List<IObserver> mObservers = new ArrayList<IObserver>();
    
    private int mLineOnLeft = 0;
    private Color mLineColorOnLeft = null;
    private int mLineOnRight = 0;
    private Color mLineColorOnRight = null;
    private MyMouseListener mMouseListener = new MyMouseListener();
    private boolean fSelectAllPending = false;

    static class TextLine {
        TextLine() {
            treePrefix = "";
            dirty = true;
            background = null;
        }
        
        Color background; // non null if special highlight

        String treePrefix;

        IAttributedString content[];

        boolean dirty;
    }

    /**
     * The initial number of lines until we know what are final size is.
     * We initially used 0, but SWT has peculiarities in that we
     * sometimes miss "resize" events during construction and the caller
     * thinks we have more lines than we do, and starts calling 
     * {@link #setLine} to set them. When we finally see the "resize"
     * event, the caller thinks the previously rendered lines are clean
     * and doesn't refresh them. So, we now permit a significant number of
     * lines to be cached at the beginning.
     */
    private static final int INIT_LINE_COUNT = 30;
    private TextLine[] mLines = new TextLine[INIT_LINE_COUNT];
    
    private int[] mStartPosition = new int[INIT_LINE_COUNT];

    private int mSelectedStartColumn = -1;

    private int mSelectedEndColumn = -1;

    private int mCharWidth = 10; // to be set in setFont

    private SWTTextCanvas mParent;

    private int mMouseX = 0;

    private int mMouseY = 0;

    private int mWidth = 0; // set by resize

    private int mLineWidth;

    private Caret mCaret;

    private Image mDoubleBuffer;
    
    private static int sInstanceCount = 0;
    private static Image sPlusImage = null;
    private static Image sMinusImage = null;

    private GC _gc; // gc when doing selection highlighting

    private int mVisibleLines;

    TextColumn(Composite parent, SWTTextCanvas canvas, boolean selectable) {
        super(parent, SWT.NO_BACKGROUND|SWT.NO_REDRAW_RESIZE);
        sInstanceCount++;
        mParent = canvas;
        mCaret = new Caret(this, 0);
        setCaret(mCaret);
        mDoubleBuffer = null;
        setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        //TODO: make font configurable some way.
        Font font = new Font(getDisplay(), "Courier New", 10, 0);
        setFont(font);
        resize();
        doDisposeListener(font);
        doResizeListener();
        if (selectable) {
            doMouseListener();
            doMouseMoveListener();
            doMouseHoverListener();
        }
        parentScroller = null;
        IContainer p = mParent.getParent();
        while (p != null) {
            if (p instanceof IScrollPane) {
                parentScroller = (IScrollPane) p;
                break;
            }
            p = p.getParent();
        }
        doKeyListener();
        doPaintListener();
    }

    private void doPaintListener() {
        this.addPaintListener(new PaintListener() {

            @Override
            public void paintControl(PaintEvent e) {
                paint(e);
            }
        });
    }
    
    private void doKeyListener () {
       
        if (parentScroller != null) {
            final IScrollBar vb = parentScroller.getVerticalScrollBar();
            final IScrollBar hb = parentScroller.getHorizontalScrollBar();
            addKeyListener(new KeyListener() {

                @Override
                public void keyPressed (KeyEvent e) {
                    switch (e.keyCode) {
                        case SWT.HOME:
                            vb.setValue(vb.getMinimum());
                            hb.setValue(hb.getMinimum());
                            e.doit = true;
                            break;
                        case SWT.END:
                            //System.out.printf("min=%d, max=%d, port=%d, value=%d\n",vb.getMinimum(),vb.getMaximum(),vb.getPortSize(),vb.getValue());
                            //System.out.println("SetValue to " + vb.getMaximum());
                            vb.setValue(vb.getMaximum());
                            hb.setValue(hb.getMinimum());
                            e.doit = true;
                            break;
                        case SWT.ARROW_UP:
                        case SWT.PAGE_UP:
                        case SWT.PAGE_DOWN:
                        case SWT.ARROW_DOWN: {
                            int incr = e.keyCode == SWT.ARROW_UP||e.keyCode==SWT.ARROW_DOWN?vb.getIncrementAmount():vb.getPageAmount();
                            int value = vb.getValue();
                            if (e.keyCode == SWT.ARROW_DOWN || e.keyCode == SWT.PAGE_DOWN) {
                                if (value + 1 < vb.getMaximum()) {
                                    vb.setValue(Math.min(value + incr, vb.getMaximum() - 1));
                                }
                            }
                            else if (value > vb.getMinimum()) {
                                vb.setValue(Math.max(value - incr, vb.getMinimum()));
                            }
                            e.doit = true;
                        }
                            break;
                        case SWT.ARROW_LEFT:
                        case SWT.ARROW_RIGHT: {
                            int incr = hb.getIncrementAmount();
                            int value = hb.getValue();
                            if (e.keyCode == SWT.ARROW_RIGHT) {
                                if (value + 1 < vb.getMaximum()) {
                                    hb.setValue(Math.min(value + incr, vb.getMaximum() - 1));
                                }
                            }
                            else if (value > vb.getMinimum()) {
                                hb.setValue(Math.max(value - incr, vb.getMinimum()));
                            }
                            e.doit = true;
                            break;
                        }
                        case 'C':
                        case 'c':
                        case SWT.INSERT:
                            if ((e.stateMask & (SWT.CTRL|SWT.SHIFT)) == SWT.CTRL) {
                                copyToClipboard();
                            }
                            break;
                        case 'a':
                        case 'A':
                            if ((e.stateMask & (SWT.CTRL|SWT.SHIFT)) == SWT.CTRL && mVisibleLines > 0) {
                                fSelectAllPending = true;
                                mSelectedLineStart = 0;
                                mSelectedLineEnd = mVisibleLines;
                                mSelectedStartColumn = 0;
                                mSelectedEndColumn = mLineWidth;
                                for (int i = 0; i < mVisibleLines; i++){
                                    mLines[i].dirty = true;
                                }
                                TextColumn.this.redraw();
                            }
                            break;
                    }
                }

                @Override
                public void keyReleased (KeyEvent e) {
                    // @todo Auto-generated method stub

                }
            });
        }
    }

    /**
     * Set up mouse-hover listener
     */
    private void doMouseHoverListener() {
        this.addMouseTrackListener(new MouseTrackAdapter() {
            @Override
            public void mouseHover(MouseEvent e) {
                mMouseX = e.x;
                mMouseY = e.y;
                notifyObservers(HOVER);

            }
        });
    }

    /**
     * Set up mouse-hover listener
     */
    private void doMouseMoveListener() {
        this.addMouseMoveListener(mMouseListener);
    }

    public void repaintDirtyLines() {
        int cnt = mVisibleLines;
        int firstDirty = -1;
        for (int i = 0; i < cnt; i++) {
            if (mLines[i] != null && mLines[i].dirty) {
                if (firstDirty == -1) {
                    firstDirty = i;
                }
            } else {
                if (firstDirty >= 0) {
                    redrawLines(firstDirty, i - 1);
                }
            }
        }
        if (firstDirty >= 0) {
            redrawLines(firstDirty, cnt - 1);
        }
    }

    /**
     * Arrange to redraw a range of lines.
     * 
     * @param first
     * @param last
     */
    private void redrawLines(int first, int last) {
        if (last < first) { // shouldn't happen
            int tmp = first;
            first = last;
            last = tmp;
        }
        this.redraw(0, first * mLineHeight, getSize().x, (last - first + 1)
                * mLineHeight, false);
    }

    private void redrawLine(int line) {
        redrawLines(line, line);
    }
    
    int getLineHeight(){
        return mLineHeight;
    }
    
    void setLineHeight(int height){
        if (height != mLineHeight){
            mLineHeight = height;
            resize();
        }
    }

    /**
     * Paint the canvas according to the clipping that is in the event argument.
     * 
     * @param event
     */
    private void paint(PaintEvent event) {
//      Call to force allocation of backing buffer and handle size if changed.
        resize();
        if (mDoubleBuffer == null) {
            // happens if size is transiently of size 0
            return;
        }
        GC gc = event.gc;
        int firstLine = event.y / mLineHeight;
        int lastLine = (event.y + event.height + mLineHeight - 1) / mLineHeight;
        GC lineGC = new GC(mDoubleBuffer);
        try {
            lineGC.setFont(gc.getFont());
            lineGC.setForeground(gc.getForeground());
            lineGC.setBackground(gc.getBackground());
            

            lastLine = Math.min(lastLine, mVisibleLines-1);
            for (int i = firstLine; i <= lastLine; i++) {
                paintLineAndTreeChars(lineGC, i);
            }
            gc.drawImage(mDoubleBuffer,0,0);
            if (mLineOnLeft != 0 && mLineColorOnLeft != null){
                int save = gc.getLineWidth();
                Color saveColor = gc.getForeground();
                gc.setForeground(mLineColorOnLeft);
                gc.setLineWidth(mLineOnLeft);
                gc.drawLine(0,0,0,getSize().y);
                gc.setLineWidth(save);
                gc.setForeground(saveColor);               
            }
            if (mLineOnRight != 0 && mLineColorOnRight != null){
                int save = gc.getLineWidth();
                Color saveColor = gc.getForeground();
                gc.setForeground(mLineColorOnRight);
                gc.setLineWidth(mLineOnRight);
                gc.drawLine(getSize().x-1,0,getSize().x-1,getSize().y);
                gc.setLineWidth(save);
                gc.setForeground(saveColor);               
            }
//            gc.drawImage(mDoubleBuffer, event.x, event.y, event.width,
//                    event.height, event.x, event.y, event.width, event.height);
        } finally {
            lineGC.dispose();
        }
    }
    
    /**
     * Return the contents of the given line. Used by the GUI tester.
     * @param lineNo zero-based line number.
     * @return the contents of the give line.
     * @throws IndexOutOfBoundsException
     */
    public String getLine(int lineNo) throws IndexOutOfBoundsException{
        if (lineNo >= 0 && lineNo < mVisibleLines){
            if (mLines[lineNo].content.length == 1)
                return mLines[lineNo].content[0].getString();
            StringBuilder buf = new StringBuilder();
            for (IAttributedString as: mLines[lineNo].content){
                if (buf.length() > 0) buf.append('\t');
                buf.append(as.getString());
            }
            return buf.toString();
        }
        throw new IndexOutOfBoundsException("Line number out of bounds: " + lineNo);
    }
    
    public void setLineColor(int lineNumber, Color color){
        if (lineNumber >= 0 && lineNumber < mLines.length){
            mLines[lineNumber].background = color;
        }
    }

    private void paintLineAndTreeChars(GC gc, int lineNumber) {
        TextLine tl = mLines[lineNumber];
        int y = getLinePosition(lineNumber);
        int x = 0;
        Color background = gc.getBackground();
        Color savedBackground = gc.getBackground();
        //Highlight selected line, and selected text.
        if (lineNumber >= mSelectedLineStart && lineNumber <= mSelectedLineEnd &&
            mSelectedLineStart == mSelectedLineEnd && this.mSelectedStartColumn == this.mSelectedEndColumn) {
            background = getDisplay().getSystemColor(
                    SWT.COLOR_WIDGET_LIGHT_SHADOW);
            gc.setBackground(background);
            gc.fillRectangle(0, getLinePosition(lineNumber), mWidth,
                    mLineHeight);
        } else {
            if (tl != null && tl.background != null) gc.setBackground(tl.background);
            gc.fillRectangle(0, getLinePosition(lineNumber), mWidth,
                    mLineHeight);
        }
        if (tl != null) {
            if (tl.treePrefix.length() > mFirstColumn) {
                x += paintTreeIndicators(gc, tl.treePrefix.substring(mFirstColumn),
                        0, y);
            }
            mStartPosition[lineNumber] = x;
            if (tl.content != null) {
                for (IAttributedString as: tl.content) {
                    paintAttributedString(gc, (SWTAttributedString)as, mFirstColumn, x, y,
                        lineNumber);
                }
            }
        }
        gc.setBackground(savedBackground);
    }
    
    private int getTreeIndicatorWidth(){
        return mCharWidth*2;
    }

//    /**
//     * Return X-coodinate of column
//     * 
//     * @param column
//     * @return
//     */
//    private int getX(int column) {
//        return (column - mFirstColumn) * mCharWidth;
//    }

    private void paintAttributedString(GC gc, SWTAttributedString as,
            int startingIndex, int x, int y, int lineNumber) {
    	//System.out.println(">>>paintline " + lineNumber + ": selected=" + mSelectedLineStart +"," + mSelectedLineEnd + "," + mSelectedStartColumn + "," +mSelectedEndColumn+")");
        if (startingIndex < 0) { // Should never happen, but it happens
            x += (-startingIndex * mCharWidth);
            startingIndex = 0;
        }
        int tabStop = as.getIndentation();
        if (tabStop >= startingIndex) {
            tabStop -= startingIndex;
            startingIndex = 0;
        }
        else if (tabStop < startingIndex){
            startingIndex -= tabStop;
            tabStop = 0;
        }
        x += tabStop*mCharWidth;
        String s = as.getString();
        // Check if horizontal scroll is so far to the right as to
        // bypass the string.
        if (startingIndex >= s.length())
            return;
        as.draw(gc,x,y,startingIndex,startingIndex,s.length(),false);
        if (lineNumber >= mSelectedLineStart && lineNumber <= mSelectedLineEnd) {
        	int start = Math.max(0,getSelectedStartColumn()-tabStop);
            int end = getSelectedEndColumn()-tabStop;
        	if (lineNumber > mSelectedLineStart) {
        		if (lineNumber < mSelectedLineEnd){
        			highlight(gc,as,x,y,startingIndex,startingIndex,s.length());
        		}
        		else{
        			if (end >= startingIndex)
        			    highlight(gc,as,x,y,startingIndex,startingIndex,end);
        		}
        	}
        	else 
    		if (lineNumber < mSelectedLineEnd){
    			highlight(gc,as,x,y,startingIndex,start,s.length());
    		}
    		else if (end > start){
    			highlight(gc,as,x,y,startingIndex,start,end);
    		}          
        }
    }

	private void setHighlightColors(GC gc) {
		gc.setBackground(getDisplay().getSystemColor(
		        SWT.COLOR_LIST_SELECTION));
		gc.setForeground(getDisplay().getSystemColor(
		        SWT.COLOR_LIST_SELECTION_TEXT));
	}
	
	private void highlight(GC gc, SWTAttributedString as, int x, int y, int indexAtX, int start, int end){
		Color savedBackground = gc.getBackground();
        Color savedForeground = gc.getForeground();
        setHighlightColors(gc);
        as.draw(gc,x,y,indexAtX,start,end,true);
        gc.setBackground(savedBackground);
        gc.setForeground(savedForeground);
	}
    
    private Image getImage(String file){
        URL url = getClass().getResource(file);
        try {
            return new Image(getDisplay(),url.openStream());
        }
        catch (IOException e) {
            return null;
        }
    }
    
    private Image getPlusImage(){
        if (sPlusImage == null){
            sPlusImage = getImage("plus.gif");
        }
        return sPlusImage;
    }
    private Image getMinusImage(){
        if (sMinusImage == null){
            sMinusImage = getImage("minus.gif");
        }
        return sMinusImage;
    }

    /**
     * Paint tree indicators and return their width in pixels.
     * @param gc
     * @param codes
     * @param x
     * @param y
     * @return width painted in pixels.
     */
    private int paintTreeIndicators (GC gc, String codes, int x, int y) {
        int len = codes.length();
        int iWidth = getTreeIndicatorWidth();
        Color savedForeground = gc.getForeground();
        int result = 3; // Need a little margin for +/- icons to fit when
                        // centered.
        x += result;
        try {
            gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE));
            for (int i = 0; i < len; i++) {
                switch (codes.charAt(i)) {
                case ITextCanvas.TREE_BLANK:
                    break;
                case ITextCanvas.TREE_BRANCH:
                    gc.drawLine(x + mCharWidth / 2, y + mLineHeight / 2, x + iWidth, y + mLineHeight / 2);
                    gc.drawLine(x + mCharWidth / 2, y, x + mCharWidth / 2, y + mLineHeight);
                    break;
                case ITextCanvas.TREE_BAR:
                    gc.drawLine(x + mCharWidth / 2, y, x + mCharWidth / 2, y + mLineHeight);
                    break;
                case ITextCanvas.TREE_BRANCH_ELBOW:
                    gc.drawLine(x + mCharWidth / 2, y + mLineHeight / 2, x + iWidth, y + mLineHeight / 2);
                    gc.drawLine(x + mCharWidth / 2, y, x + mCharWidth / 2, y + mLineHeight / 2);
                    break;
                case ITextCanvas.TREE_COLLAPSED:
                case ITextCanvas.TREE_COLLAPSED_ELBOW: {
                    Image image = getMinusImage();
                    drawImageCentered(gc, x, y, image);
                    break;
                }
                case ITextCanvas.TREE_EXPANDED:
                case ITextCanvas.TREE_EXPANDED_ELBOW: {
                    Image image = getPlusImage();
                    drawImageCentered(gc, x, y, image);
                    break;
                }
                default:
                    break;
                }
                x += iWidth;
                result += iWidth;
            }
        }
        finally {
            gc.setForeground(savedForeground);
        }
        return result;
    }

    /**
     * Draw image centered, as tree indicator.
     * @param gc
     * @param x
     * @param y
     * @param image
     */
    private void drawImageCentered (GC gc, int x, int y, Image image) {
        Rectangle b = image.getBounds();
        int xDelta = (mCharWidth-b.width)/2;
        int yDelta = (mLineHeight-b.height)/2;
        gc.drawImage(image,x+xDelta,y+yDelta);
    }

    /**
     * Set mouse listener
     */
    private void doMouseListener() {
        this.addMouseListener(mMouseListener);
    }

    /**
     * Set resize listener
     */
    private void doResizeListener() {
        // Track resizing so that we can adjust
        // number of lines.
        this.addControlListener(new ControlListener() {

            @Override
            public void controlMoved(ControlEvent e) {
            }

            @Override
            public void controlResized(ControlEvent e) {
                resize();
            }
        });
    }

    /**
     * @param font
     */
    private void doDisposeListener(final Font font) {
        // Dispose of font when this widget is disposed
        this.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                sInstanceCount--;
                if (mDoubleBuffer != null) {
                    mDoubleBuffer.dispose();
                    mDoubleBuffer = null;
                }
                if (_gc != null) {
                    _gc.dispose();
                    _gc = null;
                }
                font.dispose();
                if (sInstanceCount == 0){
                    if (sMinusImage != null){
                        sMinusImage.dispose();
                        sMinusImage = null;
                    }
                    if (sPlusImage != null){
                        sPlusImage.dispose();
                        sPlusImage = null;
                    }
                }
                
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.widgets.Control#setFont(org.eclipse.swt.graphics.Font)
     */
    @Override
    public void setFont(Font font) {
        super.setFont(font);
        GC gc = new GC(getDisplay());
        gc.setFont(font);
        Point size = gc.stringExtent("XXX");
        gc.dispose();
        int height = size.y;
        int charWidth = size.x/3;
        if (height != mLineHeight || charWidth != mCharWidth) {
            mLineHeight = height;
            mCharWidth = charWidth;
            getCaret().setSize(2,mLineHeight); // 2 pixels wide to make it dark
            if (mLines.length > 0)
                resize();
        }
    }

    public int getMaxColumns() {
        return mWidth / mCharWidth;
    }

    public int getMaxLines() {
        return mVisibleLines;
    }

    /**
     * Return the y coordinate of the top of the line.
     * 
     * @param line
     *            the line number.
     * @return the y coordindate of the top of the line.
     */
    public int getLinePosition(int line) {
        return line * mLineHeight;
    }
    
    private int computeLine(int y){
    	return y/mLineHeight;
    }

    private void setSelection(int x, int y) {
        int line = computeLine(y);
        setSelectedLine(line);
        mSelectedStartColumn = computeCharPosition(line,x);
        setCaret(line,mSelectedStartColumn);
        mSelectedEndColumn = mSelectedStartColumn;
    }
    
    private void setCaret(int line, int column){
    	getCaret().setLocation((column-mFirstColumn)*mCharWidth,line*mLineHeight);
    }

    private int computeCharPosition(int line, int x) {
        return (x - getPositionOfFirstChar(line))/ mCharWidth + mFirstColumn;
    }
    
    /**
     * Return the x-pixel position where the first character starts.
     * @param line
     * @return the x-pixel position where the first character starts.
     */
    private int getPositionOfFirstChar(int line){
        if (line >= 0 && line < mStartPosition.length){
            return mStartPosition[line];
        }
        return 0;     
    }

    /**
     * Add tree indicator.
     * 
     * @param line
     * @param codes
     *            array of codes, one per character, as defined in
     *            {@link ITextCanvas}.
     */
    public void setTreeIndicators(int line, String codes) {
        if (line >= 0 && line < mLines.length) {
            if (mLines[line] == null)
                mLines[line] = new TextLine();
            if (!mLines[line].treePrefix.equals(codes)) {
                mLines[line].treePrefix = codes;
                mLines[line].dirty = true;
            }
        }
    }
    
    public void unselect(){
        if (mSelectedLineEnd >= 0){
            int line1= Math.max(0,mSelectedLineStart);
            int line2 = mSelectedLineEnd;
            mSelectedLineStart = -1;
            mSelectedLineEnd = -2;
            for (int line = line1; line <= line2; line++){
               redrawLine(line);
            }
        }
    }
    
    public void setSelectedLine(int line){
        if (line != mSelectedLineStart || mSelectedLineStart != mSelectedLineEnd) {
            int oldSelectedLineStart = mSelectedLineStart;
            int oldSelectedLineEnd = mSelectedLineEnd;
            if (line < mLines.length) {
                mSelectedLineStart = line;
                mSelectedLineEnd = line;
            } else {
                mSelectedLineStart = -1;
                mSelectedLineEnd = -1;
            }
            if (oldSelectedLineEnd >= 0) {
                for (int l = oldSelectedLineStart; l <= oldSelectedLineEnd; l++){
            	    redrawLine(l);
                }
            }
            if (line >= 0)
                redrawLine(line);
        }        
    }
    
    private static boolean compare(IAttributedString s1[], IAttributedString s2[]){
        if (s1 == null) return s2 == null;
        if (s2 == null) return false;
        if (s1.length != s2.length) return false;
        for (int i = 0; i < s1.length; i++){
            if (!s1[i].equals(s2[i])) return false;
        }
        return true;
    }

    public void setLine(int line, IAttributedString as[]) {
        if (line < 0)
            throw new IllegalArgumentException("Line number is negative");
        if (line < mLines.length) {
            if (mLines[line] == null)
                mLines[line] = new TextLine();
            if (!compare(as,mLines[line].content)) {
                mLines[line].dirty = true;
                mLines[line].content = as;
            }
        }
    }

    /**
     * Return actual number of lines.
     * 
     * @return actual number of lines
     */
    public int getLineCount() {
        int cnt = mVisibleLines;
        while (cnt > 0 && mLines[cnt - 1] == null)
            cnt--;
        return cnt;
    }

    public void clearLine(int line) {
        if (line < 0)
            throw new IllegalArgumentException("Line number is negative");
        if (line < mLines.length && mLines[line] != null
                && mLines[line].content != null) {
            mLines[line].content = null;
            mLines[line].dirty = true;
        }
    }

    public int getSelectedLine() {
        return mSelectedLineStart;
    }

    public void scrollHorizontally(int column) {
        if (column < 0)
            throw new IllegalArgumentException("column index is negative");
        if (mFirstColumn != column) {
        	int delta = column-mFirstColumn;
        	mSelectedStartColumn -= delta;
        	mSelectedEndColumn -= delta;
            mFirstColumn = column;
            if (mSelectedEndColumn == mSelectedStartColumn &&
            		mSelectedLineStart == mSelectedLineEnd){
            	setCaret(mSelectedLineStart,mSelectedStartColumn);
            }
            redraw();
        }
    }
    
    private void confirmBuffer () {
        Point size = getSize();
        if (isVisible() && size.x > 0 && size.y > 0) {
            if (mDoubleBuffer != null
                    && (mDoubleBuffer.getBounds().height != size.x || mDoubleBuffer.getBounds().width != size.y)) {
                mDoubleBuffer.dispose();
                mDoubleBuffer = null;
            }
            if (mDoubleBuffer == null)
                mDoubleBuffer = new Image(getDisplay(), size.x, size.y);
        }
    }

    private void resize () {
        Rectangle bounds = getClientArea();
        boolean widthChanged = false;
        // A quirk of Windows is that when this column is being resized,
        // it gets a resize event with size of 0 or even less than 0. 
        // Skip this because
        // it can cause all sorts of unnecessary cache flushing in
        // our calling code, or IllegalArgumentExceptions.
        // It also gets resize events when not visible! Don't know why.
        if (!isVisible() || bounds.width <= 0 || bounds.height <= 0) {
            notifyObservers(RESIZE);
            return;
        }
        confirmBuffer();
        mWidth = bounds.width;
        // To avoid what appears to be blank line at the bottom, go ahead
        // and permit a line if it is 50% visible
        int lineCnt = (bounds.height+mLineHeight/2) / mLineHeight;
        int lineWidth = mWidth / mCharWidth;
        if (lineCnt > mLines.length) {
            TextLine tl[] = new TextLine[lineCnt];
            int startPosition[] = new int[lineCnt];
            System.arraycopy(mLines, 0, tl, 0,  mLines.length);
            System.arraycopy(mStartPosition,0,startPosition,0,mStartPosition.length);
//            //If the height is reduced, we may have a line at the bottom
//            // that won't get cleared because we repaint only dirty regions.
//            // Force it to be cleared if this is the case.
//            if (lineCnt < mLines.length){
//                redrawLine(lineCnt); // makes the appropriate region dirty
//            }
            mLines = tl;
            mStartPosition = startPosition;
            mLineWidth = lineWidth;
        }
        else if (lineWidth != mLineWidth) {
            mLineWidth = lineWidth;
            widthChanged = true;
        }
        
        int oldVisible = mVisibleLines;
        mVisibleLines = lineCnt;
        if (lineCnt > oldVisible){
            //We may have shrunk then regrown. Repaint any lines
            // we already have cached.
            int first = oldVisible;
            int second;
            for (second = first;  second < lineCnt && mLines[second] != null; second++){}
            if (second > first)
                redrawLines(first,second-1);
        }
        if (oldVisible != lineCnt || widthChanged){
            notifyObservers(RESIZE);
        }
    }

    /**
     * @return the start column
     */
    public int getSelectedStartColumn() {
        return mSelectedStartColumn;
    }

    /**
     * @return the end column
     */
    public int getSelectedEndColumn() {
        return mSelectedEndColumn;
    }
    
    private String getLineContent(int line,int startIndex, int endIndex){
    	if (line >= 0 && line < mLines.length && mLines[line] != null &&
    			mLines[line].content != null){
    		if (mLines[line].content.length == 1){
    			SWTAttributedString as = (SWTAttributedString)mLines[line].content[0];
    			int tabStop = as.getIndentation();
    			int start = Math.max(0,startIndex-tabStop);
    			int end = endIndex-tabStop;
    			String s = as.getString();
    			end = Math.min(end,s.length());
    			if (start < end)
    			    return s.substring(start,end);
    			return "";
    		}
    		StringBuilder buf = new StringBuilder();
    		for (IAttributedString as: mLines[line].content){
    			int tabStop = ((SWTAttributedString)as).getIndentation();
    			if (tabStop > endIndex) break;
    			int start = startIndex-tabStop;
    			int end = endIndex-tabStop;
    			if (start < 0){
    				buf.append('\t');
    				start = 0;
    			}
    			else if (buf.length() > 0)
    				buf.append('\t');
    			String s = as.getString();
    			end = Math.min(end,s.length());
    			if (start < end){
    				buf.append(s.substring(start, end));
    			}
    			
    		}
    		return buf.toString();
    	}
    	return "";
    }
    
    private static final String LINE_SEPARATOR = System.getProperty("line.separator","\n");

    public String getSelection() {
        int start = getSelectedStartColumn();
        int end = getSelectedEndColumn();
        if (mSelectedLineStart >= 0 && mSelectedLineStart < mLines.length &&
        		mSelectedLineStart <= mSelectedLineEnd) {
        	StringBuilder buf = new StringBuilder();
        	for (int l = mSelectedLineStart; l <= mSelectedLineEnd;l++) {
        		int firstPos = l==mSelectedLineStart?start:0;
        		int lastPos = l==mSelectedLineEnd?end:1000;
        		if (buf.length() > 0) buf.append(LINE_SEPARATOR);
        		buf.append(getLineContent(l,firstPos,lastPos));       		
        	}
            return buf.toString();
        }
        return null;
    }

    /**
     * @param tooltip
     * @param line
     * @param column
     */
    public void showToolTip(String tooltip, int line, int column) {
        //TODO

    }

    public void addObserver(IObserver o) {
        synchronized (mObservers) {
            mObservers.add(o);
        }
    }

    public void removeObserver(IObserver o) {
        synchronized (mObservers) {
            mObservers.remove(o);
        }
    }
    
 
    
    /**
     * Compute the column position relative to the
     * start of text -- skipping over the tree prefix
     * characters.
     * @param line the 0-based line number.
     * @param col column relative left-most edge.
     * @return the text-relative column
     */
    private int computeColumn(int line, int col){
        if (line >= 0 && line < mLines.length && mLines[line] != null){
            return col - mLines[line].treePrefix.length();
        }
        return col;
        
    }

    private static final int RESIZE = 1;

    private static final int SELECTED = 2;

    private static final int DOUBLECLICK = 3;

    private static final int HOVER = 4;

    private static final int POPUP = 5;
    
    private static final int HIGHLIGHT = 6;

    private IScrollPane parentScroller;

    private ICopyToClipboard fClipboard = null;

    protected void notifyObservers(int event) {
        IObserver o[];
        synchronized (mObservers) {
            o = mObservers.toArray(new IObserver[mObservers
                    .size()]);
        }
        for (int i = 0; i < o.length; i++) {
            switch (event) {
                case RESIZE:
                    o[i].onResize(mParent);
                    break;
                case SELECTED: 
                    o[i].onSelected(mParent, mSelectedLineStart,
                            computeColumn(mSelectedLineStart,mSelectedStartColumn));
                    break;
                case DOUBLECLICK:
                    o[i].onDoubleClick(mParent, mSelectedLineStart,
                            computeColumn(mSelectedLineStart,mSelectedStartColumn));
                    break;
                case HOVER:
                    o[i].onHover(mParent, mMouseY / mLineHeight, computeColumn(mMouseY/mLineHeight,mMouseX
                            / mCharWidth));
                    break;
                case POPUP:
                    o[i].onPopup(mParent, mMouseX, mMouseY);
                    break;
                case HIGHLIGHT:
                    o[i].onHighlighted(mParent,mSelectedLineStart,mSelectedStartColumn,mSelectedLineEnd,mSelectedEndColumn);
                    break;
            }
        }
    }

    @Override
    public Point computeSize (int wHint, int hHint, boolean changed) {
        return new Point(getMaxColumns()*mCharWidth,getMaxLines()*mLineHeight);
    }
    
    void setVerticalLine(boolean onLeft, Object color, int thickness){
        Color realColor = color==null?null:color instanceof IColor?(Color)((IColor)color).getObject():(Color)color;
        if (onLeft){
            mLineOnLeft = thickness;
            mLineColorOnLeft = realColor;
        }
        else {
            mLineOnRight = thickness;
            mLineColorOnRight = realColor;          
        }
    }
    
    /**
     * Called from GUI tester to record the state of the
     * parent ITextCanvas.
     * @param out the output stream into which the state is to be written.
     */
    public void recordState(OutputStream out){
        mParent.recordState(out);
    }
    
    /**
     * Record state for benefit of GUI tester. Called from 
     * {@link SWTTextCanvas.recordState}.
     * @param out where to write state information.
     */
    void recordState(PrintStream out){
        // We make it XML-like
        out.print("    <text lineCount=\"" + getLineCount() + "\" selection=\"" +
            getSelectedLine() +'"');
        if (mFirstColumn > 0) {
            out.print(" hscroll=\""+ mFirstColumn + "\"");
        }
        out.println(">");
        for (int i = 0; i < getLineCount(); i++){
            out.print("        <line");
            if (mLines[i].treePrefix.length() > 0) {
                out.print(" treePrefix=\"" + mLines[i].treePrefix + "\"");
            }
            out.println(" content=\"" + mLines[i].content[0].getString() + "\">");
            ((SWTAttributedString)mLines[i].content[0]).recordState(out,12);     
        }      
        out.println("    </text>");
    }
    
    void setCopyToClipboard(ICopyToClipboard callback){
        fClipboard = callback;
    }
    
    void setSelection (int line1, int col1, int line2, int col2) {
        int oldSelectedLineStart = mSelectedLineStart;
        int oldSelectedLineEnd = mSelectedLineEnd;
       
        this.mSelectedLineStart = line1;
        this.mSelectedLineEnd = line2;
        this.mSelectedStartColumn = col1;
        this.mSelectedEndColumn = col2;
        if (oldSelectedLineEnd >= 0) {
            for (int l = oldSelectedLineStart; l <= oldSelectedLineEnd; l++){
                redrawLine(l);
            }
        }
        if (line1 >= 0){
            for (int l = mSelectedLineStart; l <= mSelectedLineEnd; l++){
                redrawLine(l);
            }
        }
    }

    
    void copyToClipboard () {
        if (fClipboard != null && fSelectAllPending){
            if (fClipboard.copyToClipboard(mParent, true)){
                return;
            }
        }
        String selection = getSelection();
        if (selection != null && selection.length() > 0) {
            Clipboard cb = new Clipboard(getDisplay());
            TextTransfer textTransfer = TextTransfer.getInstance();
            cb.setContents(new Object[]{selection}, new Transfer[]{textTransfer});
            cb.dispose();
        }
    }

    class MyMouseListener implements MouseListener, MouseMoveListener{
        private long _doubleClickTime = 0;
        private boolean _button3ClickPending = false;
        private int _startLine = -1;
        private int _endLine = -1;
        private int _startColumn = -1;
        private int _endColumn = -1;
        @Override
        public void mouseDoubleClick(MouseEvent e) {
            setSelection(e.x, e.y);
            _doubleClickTime = System.currentTimeMillis();
            notifyObservers(DOUBLECLICK);              
        }

        @Override
        public void mouseDown(MouseEvent e) {
            forceFocus(); // so that mouse wheel will work!!!
            mMouseX = e.x;
            mMouseY = e.y;
            _button3ClickPending = false;
            if (e.button == 1) {
                //setSelection(e.x, e.y);
                _startLine = computeLine(e.y);
                _startColumn = computeCharPosition(_startLine,e.x);
                _endLine = _startLine;
                _endColumn = _startColumn;
                if (_startLine >= 0) {
                    if (_gc == null)
                        _gc = new GC(TextColumn.this);
                    export();
                }
            }
            else if (e.button == 3 && mSelectedStartColumn == mSelectedEndColumn) {
                // The third button will cause a new selection unless there
                // is a highlight pending. This is how old SeeCode worked.
                setSelection(e.x,e.y);
                _button3ClickPending = true;
            }
        }
        
        private void export(){
        	int oldStart = mSelectedLineStart;
        	int oldEnd = mSelectedLineEnd;
        	if (_endLine < _startLine || _endLine==_startLine && _endColumn < _startColumn){
        		mSelectedStartColumn = _endColumn;
            	mSelectedEndColumn = _startColumn;
                mSelectedLineStart = _endLine;
            	mSelectedLineEnd = _startLine;         	          	
            }
            else {
            	mSelectedStartColumn = _startColumn;
            	mSelectedEndColumn = _endColumn;
            	mSelectedLineStart = _startLine;
                mSelectedLineEnd = _endLine;            	
            }
        	int line1 = Math.min(oldStart,mSelectedLineStart);
        	int line2 = Math.max(oldStart,mSelectedLineStart);
        	if (line1 >= 0) {
        	    for (int l = line1; l <= line2; l++){
        		    if (_gc != null) paintLineAndTreeChars(_gc,l);
        	    }
        	}
        	fSelectAllPending = false;
        	int line3 = Math.min(oldEnd,mSelectedLineEnd);
        	int line4 = Math.max(oldEnd,mSelectedLineEnd);
        	if (line3 == line2) line3++;
        	if (line4 == line2) line4--;
        	if (line3 >= 0) {
	        	for (int l = line3; l <= line4; l++){
	        		if (_gc != null) paintLineAndTreeChars(_gc,l);
	        	}
        	}
        }

        @Override
        public void mouseUp(MouseEvent e) {
            if (e.button == 1) {
                // Don't send SELECTED event if this is a doubleclick event.
                if (System.currentTimeMillis() - _doubleClickTime > 200)
                    notifyObservers(SELECTED);
                _endLine = computeLine(e.y);
                _endColumn = computeCharPosition(_endLine,e.x);

                if (_gc != null) {
                    export();
                	_gc.dispose();
                	_gc = null;
                }
                else if (_endLine >= 0) {
                	_startLine = _endLine;
                	_startColumn = _endColumn;
                	export();
                    redrawLine(_endLine);
                }
                notifyObservers(HIGHLIGHT);
            } else if (e.button == 3) {
                if (_button3ClickPending)
                    notifyObservers(SELECTED);
                notifyObservers(POPUP);
            }
        }
        @Override
        public void mouseMove(MouseEvent e) {
            // NOTE: the e.button field isn't set!
            // If _gc defined, then we know that the
            // left button is pressed.
            if (_gc != null) {
                mMouseX = e.x;
                mMouseY = e.y;
                _endLine = computeLine(e.y);
                _endColumn = computeCharPosition(_endLine,e.x);
                export();
            }
        }
    }
}
