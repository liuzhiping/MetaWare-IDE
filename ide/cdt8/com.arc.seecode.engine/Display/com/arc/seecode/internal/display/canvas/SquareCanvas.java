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

import com.arc.seecode.display.IColorPreferences;
import com.arc.seecode.internal.display.ISquareCanvas;
import com.arc.seecode.internal.display.ISquareCanvasCallback;
import com.arc.seecode.internal.display.Line;
import com.arc.widgets.IAttributedString;
import com.arc.widgets.IColor;
import com.arc.widgets.IComponent;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;
import com.arc.widgets.ICopyToClipboard;
import com.arc.widgets.IFont;
import com.arc.widgets.IImage;
import com.arc.widgets.ITextCanvas;

/**
 * @author David Pickens
 */
public class SquareCanvas implements ISquareCanvas {
    private static final int COLUMN_MARGIN = 2;

    private static final int BKPT_IMAGE_OFFSET = COLUMN_MARGIN;

    private static final int BKPT_IMAGE_WIDTH = 15;

    private static final int PC_IMAGE_OFFSET = BKPT_IMAGE_OFFSET
            + BKPT_IMAGE_WIDTH + COLUMN_MARGIN;

    private static final int PC_IMAGE_WIDTH = 15;

    private static final int TEXT_OFFSET = PC_IMAGE_OFFSET + PC_IMAGE_WIDTH
            + COLUMN_MARGIN;

    private ITextCanvas mCanvas;

    private TextCanvasObserver mTextCanvasObserver;

    private Line[] mCurrentLines = null;
    
    private IComponentFactory mFactory;

    private int mWhichAmI;

    private IFont fProportionalFont;
    
    private IColorPreferences fPrefs = null;

    private ISquareCanvasCallback mCallback;

    public SquareCanvas(IContainer parent, int preferredLineCount,
            int preferredColumnCount, ISquareCanvasCallback callback,
            IComponentFactory factory,
            int whichAmI) {
        mCallback = callback;
        mTextCanvasObserver = new TextCanvasObserver(this, callback);
        Images.init(factory); // only executes first time.
        mFactory = factory;
        mWhichAmI = whichAmI;
        createWidget(parent);
    }
    private void createWidget(IContainer parent){
        mCanvas = mFactory.makeTextCanvas(parent,mWhichAmI == 0,mWhichAmI==0);
        mCanvas.setHorizontalAlignment(IComponent.FILL);
        mCanvas.setVerticalAlignment(IComponent.FILL);
        mCanvas.setHorizontalWeight(1.0);
        mCanvas.setVerticalWeight(1.0);
        
        mCanvas.setCopyToClipboard(new ICopyToClipboard(){

            @Override
            public boolean copyToClipboard (ITextCanvas canvas, boolean all) {
                if (all) {
                    return mCallback.copyAllToClipboard(SquareCanvas.this);
                }
                return false;
            }});

        mCanvas.setLeftMargin(TEXT_OFFSET);
//        if (mWhichAmI != 0) {
//            int lineHeight = mCanvas.getLineHeight();
//            IFont font = mCanvas.getFont();
//            // Column need a small font.
//            if (font != null && font.getSize() > 8) {
//                mCanvas.setFont(mFactory.makeFont(font.getName(), false, false, 8));
//                mCanvas.setLineHeight(lineHeight); //don't change line height!
//            }
//        }
        
        mCanvas.addObserver(mTextCanvasObserver);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.internal.display.ISquareCanvas#dispose()
     */
    @Override
    public void dispose() {
        mCanvas.dispose();

    }
    
    @Override
    public void copyToClipboard(){
        mCanvas.copyToClipboard();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.internal.display.ISquareCanvas#setEnableToolTips(boolean)
     */
    @Override
    public void setEnableToolTips(boolean v) {
        mTextCanvasObserver.enableToolTips(v);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.internal.display.ISquareCanvas#setPermitDoubleClickInColumn0(boolean)
     */
    @Override
    public void setPermitDoubleClickInColumn0(boolean v) {
        mTextCanvasObserver.enableDoubleClickInColumn0(v);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.internal.display.ISquareCanvas#showToolTip(java.lang.String,
     *      int, int)
     */
    @Override
    public void showToolTip(String tip, int line, int column) {
        mCanvas.showToolTip(tip, line, column);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.internal.display.ISquareCanvas#set(com.arc.seecode.internal.display.Line[])
     */
    @Override
    public void set(Line[] newLines) {
        for (int i = 0; i < newLines.length; i++) {
            Line line = newLines[i];
            if (mCurrentLines == null || i >= mCurrentLines.length
                    || !mCurrentLines[i].equals(line)
                    || mCurrentLines[i].isDirty()) {
                IAttributedString as[] = line.getAttributedStrings(mFactory,mCanvas.getFont(),fProportionalFont);
                mCanvas.clearIcons(i);
                String treeChars = line.getTreeChars();
                setTreePrefix(i,treeChars);
                mCanvas.setLine(i, as,line.getBackgroundColor(fPrefs));
                setIcons(i, line);
                line.setDirty(false);
            }
        }
        int max = mCanvas.getLineCount();
        for (int i = newLines.length; i < max; i++){
            mCanvas.clearLine(i);
        }
        mCurrentLines = new Line[newLines.length];
        System.arraycopy(newLines, 0, mCurrentLines, 0, newLines.length);
        mCanvas.unselect(); // conventional way of acting
        mCanvas.repaint();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.internal.display.ISquareCanvas#setHorizontalScrollValue(int)
     */
    @Override
    public void setHorizontalScrollValue(int column) {
        mCanvas.scrollHorizontally(column);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.internal.display.ISquareCanvas#getComponent()
     */
    @Override
    public IComponent getComponent() {
        return mCanvas;
    }

    private void setIcons(int lineNumber, Line line) {
        IImage image;

        // Indicators at mPcImageOffset
        if (line.isHighlightOn(Line.HL_PC | Line.HL_OLD_PC | Line.HL_WPT | Line.HL_WPT_DISABLED | 
                               Line.HL_LOCK | Line.HL_FOCUS | Line.HL_EXPAND |
                               Line.HL_CONTRACT | Line.HL_MARKED | Line.HL_FOUND)) {

            image = null;

            if (line.isHighlightOn(Line.HL_PC))
                image = Images.PC_ARROW_IMAGE;
            
            else if (line.isHighlightOn(Line.HL_OLD_PC))
                image = Images.OLDPC_ARROW_IMAGE;
            
            else if (line.isHighlightOn(Line.HL_WPT))
                image = Images.WATCH_IMAGE;
            
            else if (line.isHighlightOn(Line.HL_WPT_DISABLED))
                image = Images.WATCH_DISABLED_IMAGE;
            
            else if (line.isHighlightOn(Line.HL_LOCK))
                image = Images.LOCK_IMAGE;
            
            else if (line.isHighlightOn(Line.HL_FOCUS))
                image = Images.FOCUS_IMAGE;
            
            else if (line.isHighlightOn(Line.HL_EXPAND) && !line.isExpandContractIndented())
                image = Images.EXPAND_IMAGE;
            
            else if (line.isHighlightOn(Line.HL_CONTRACT) && !line.isExpandContractIndented())
                image = Images.CONTRACT_IMAGE;

            else if (line.isHighlightOn(Line.HL_MARKED))
                image = Images.MARKED_IMAGE;

            else if (line.isHighlightOn(Line.HL_FOUND))
                image = Images.FOUND_IMAGE;
            if (image != null)
                mCanvas.setIcon(image,PC_IMAGE_OFFSET,lineNumber);
            }


        // Indicators at mBkptImageOffset
        image = null;

        if (line.isHighlightOn(Line.HL_BKPT | Line.HL_BKPT_DISABLED | Line.HL_BKPT_MULTIPLE |
                                    Line.HL_BKPT_SAME_FUNC | Line.HL_BKPT_SAME_FUNC_DISABLED |
                                    Line.HL_BKPT_THREAD_SPECIFIC | Line.HL_BKPT_OTHER_THREAD | 
                                    Line.HL_BKPT_HARDWARE | Line.HL_CAN_SET_BKPT |
                                    Line.HL_PC_UNINDENTED)) {
            
            if (line.isHighlightOn(Line.HL_BKPT_DISABLED))
                image = Images.BKPT_DISABLED_IMAGE;
                
            else if (line.isHighlightOn(Line.HL_BKPT_MULTIPLE))
                image = Images.BKPT_MULTIPLE_IMAGE;
                
            else if (line.isHighlightOn(Line.HL_BKPT_SAME_FUNC))
                image = Images.BKPT_SAME_FUNC_IMAGE;
                
            else if (line.isHighlightOn(Line.HL_BKPT_SAME_FUNC_DISABLED))
                image = Images.BKPT_SAME_FUNC_DISABLED_IMAGE;

            else if (line.isHighlightOn(Line.HL_BKPT_HARDWARE)){
                image = Images.BKPT_HARDWARE_IMAGE;      
            }
            else if (line.isHighlightOn(Line.HL_BKPT_THREAD_SPECIFIC))
                image = Images.BKPT_THREAD_SPECIFIC_IMAGE;

            else if (line.isHighlightOn(Line.HL_BKPT_OTHER_THREAD))
                image = Images.BKPT_OTHER_THREAD_IMAGE;
            else if (line.isHighlightOn(Line.HL_BKPT))
                image = Images.BKPT_IMAGE;
            else if (line.isHighlightOn(Line.HL_CAN_SET_BKPT)) {
                image = Images.BKPT_CAN_SET; 
            }
            else if (line.isHighlightOn(Line.HL_PC_UNINDENTED)){
                image = Images.PC_ARROW_IMAGE;
            }
        }

        // check if we have an image to draw.
        if (image != null) {
            mCanvas.setIcon(image, BKPT_IMAGE_OFFSET, lineNumber);
            }
    }
    

    private void setTreePrefix(int line, String treeChars) {
        int len = treeChars.length();
        if (len == 0) {
            mCanvas.setTreeIndicators(line,"");
        }
        StringBuffer treeIndicators = new StringBuffer();
        for (int i = 0; i < len; i++) {
            char c = treeChars.charAt(i);
            int prefix;
            switch(c){
                case Line.CONTROL_CODE_BAR_BLANK:
                    prefix = ITextCanvas.TREE_BLANK;
                break;
                case Line.CONTROL_CODE_BAR_ELBOW:
                    prefix = ITextCanvas.TREE_BRANCH_ELBOW;
                break;
                case Line.CONTROL_CODE_BAR_ELBOW_CONTRACT:
                    prefix = ITextCanvas.TREE_COLLAPSED_ELBOW;
                break;
                case Line.CONTROL_CODE_BAR_ELBOW_EXPAND:
                    prefix = ITextCanvas.TREE_EXPANDED_ELBOW;
                break;
                case Line.CONTROL_CODE_BAR:
                    prefix = ITextCanvas.TREE_BAR;
                break;
                case Line.CONTROL_CODE_BAR_TAIL:
                    prefix = ITextCanvas.TREE_BRANCH;
                break;
                case Line.CONTROL_CODE_BAR_TAIL_CONTRACT:
                    prefix = ITextCanvas.TREE_COLLAPSED;
                break;
                case Line.CONTROL_CODE_BAR_TAIL_EXPAND:
                    prefix = ITextCanvas.TREE_EXPANDED;
                break;
                default:
                    throw new IllegalArgumentException("Unknown tree char: 0x" + Integer.toHexString(c));
            }
            treeIndicators.append((char)prefix);
        }
        mCanvas.setTreeIndicators(line,treeIndicators.toString());       
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.internal.display.ISquareCanvas#reparent(com.arc.widgets.IContainer)
     */
    @Override
    public void reparent(IContainer parent) {
        mCanvas.dispose();
        createWidget(parent);
        repopulate();       
    }
    
    private void repopulate(){
        for (Line line: mCurrentLines){
            line.setDirty(true);
        }
        set(mCurrentLines);
    }
    /* (non-Javadoc)
     * @see com.arc.seecode.internal.display.ISquareCanvas#getLineCount()
     */
    @Override
    public int getLineCount() {
        if (mCurrentLines == null)
            return 0;
        return mCurrentLines.length;
    }
    /* (non-Javadoc)
     * @see com.arc.seecode.internal.display.ISquareCanvas#getWhich()
     */
    @Override
    public int getWhich() {
        return mWhichAmI;
    }
    
    @Override
    public void setFont (Object font) {
       mCanvas.setFont(font instanceof IFont?(IFont)font:mFactory.makeFont(font));
       IFont iFont = mCanvas.getFont();
       fProportionalFont = mFactory.makeFont("dialog",false,false,iFont.getSize());
    }
    
    @Override
    public void setColorPreferences (IColorPreferences prefs) {
        mCanvas.setForeground(prefs.getForegroundColor());
        IColor background = prefs.getBackgroundColor();
        if (background != null)
            mCanvas.setBackground(background);
        fPrefs = prefs;
        if (mCurrentLines != null) {
            for (Line line : mCurrentLines) {
                line.setDirty(true);
            }
            set(mCurrentLines); // redraw with new stuff
        }
    }
    
    @Override
    public void setVerticalLine (boolean onLeft, int thickness, Object color) {
        mCanvas.setVerticalLine(onLeft,thickness,color);        
    }
    
    @Override
    public void setName(String name){
        mCanvas.setName(name);
    }
    @Override
    public String getSelection () {
        return mCanvas.getSelection();
    }
    
    /**
     * Set a range of text to be highlighted as "selected".
     * @param line1 starting line, 0 based.
     * @param col1 starting column in line, 0 based.
     * @param line2 end line, inclusive, 0 based.
     * @param col2 end column, inclusive, 0 based.
     */
    @Override
    public void setSelection(int line1, int col1, int line2, int col2){
    	mCanvas.setSelection(line1,col1,line2,col2);
    }
}
