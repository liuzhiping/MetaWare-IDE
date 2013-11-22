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

import java.util.List;

import com.arc.mw.util.Log;
import com.arc.mw.util.Toggle;
import com.arc.seecode.display.IColorPreferences;
import com.arc.seecode.internal.display.canvas.SquareCanvas;
import com.arc.widgets.IColor;
import com.arc.widgets.IComponent;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;
import com.arc.widgets.IFont;
import com.arc.widgets.ILabel;
import com.arc.widgets.IScrollBar;
import com.arc.widgets.IScrollPane;

/**
 * The scrollable panel that contains the text canvas and the profiling columns.
 * 
 * @author David Pickens
 */
public class SeeCodeTextView {
    private static final int MAX_NUM_PROFILE_COLUMNS = 32;

    private static final int PROFILE_COLUMN_WIDTH = 7;

    private ISquareCanvas mPrimaryCanvas;

    private boolean[] mColumnVisible = new boolean[MAX_NUM_PROFILE_COLUMNS];

    private int mNumVisibleColumns = 0;

    private ISquareCanvas[] mColumnCanvas = new ISquareCanvas[MAX_NUM_PROFILE_COLUMNS];

    private String[] mColumnHeaders = new String[MAX_NUM_PROFILE_COLUMNS];
    private ILabel[] mColumnHeaderLabels = new ILabel[MAX_NUM_PROFILE_COLUMNS];

    private IScrollBar mVertSB;

    private IScrollBar mHorzSB;

    private ISquareCanvasCallback mCallback;

    private TextDisplayModel mModel;

    private IScrollPane mScrollPane;

    private IComponentFactory mFactory;

    private IContainer mParent;

    private int mInitialLineCount;

    private int mInitialColumnCount;
    
    private IFont mHeaderFont = null;

    private ILabel mPrimaryHeader = null;

    private ViewLayout mViewLayout;

    private Object mFont = null; // null means use default

    private ProfilingColumnLayout mProfilingLayout;

    private IContainer mMainPanel;

    private IColorPreferences fColorPrefs = null;

    public SeeCodeTextView(SeeCodeTextViewer parent, TextDisplayModel model,
            boolean needInfiniteSlider,
            ISquareCanvasCallback callback,
            int preferredLineCount,
            int preferredColumnCount,
            IComponentFactory factory) {
        mCallback = callback;
        mModel = model;
        mFactory = factory;
        mHeaderFont = null;       
        mProfilingLayout = new ProfilingColumnLayout();
        mViewLayout = new ViewLayout(mProfilingLayout);
        mParent = factory.makeContainer(parent.getComponent(),mViewLayout);
        mParent.setHorizontalWeight(1.0);
        mParent.setVerticalWeight(1.0);
        mParent.setHorizontalAlignment(IComponent.FILL);
        mParent.setVerticalAlignment(IComponent.FILL);
        mParent.setMargins(0,0,0,0);
        mInitialLineCount = preferredLineCount;
        mInitialColumnCount = preferredColumnCount;
        generateWidgets(needInfiniteSlider);
    }
    
    private void generateWidgets(boolean needInfiniteSlider){
        
        mScrollPane = mFactory.makeScrollPane(mParent,true/*We manage*/);
        mScrollPane.setHorizontalWeight(1.0);
        mScrollPane.setVerticalWeight(1.0);
        mScrollPane.setHorizontalAlignment(IComponent.FILL);
        mScrollPane.setVerticalAlignment(IComponent.FILL);
        
        mMainPanel = mFactory.makeContainer(mScrollPane,mProfilingLayout);
       
        
        mPrimaryCanvas = new SquareCanvas(mMainPanel,mInitialLineCount, mInitialColumnCount,mCallback,
                mFactory,0);
        if (mFont != null) mPrimaryCanvas.setFont(mFont);
        if (fColorPrefs != null) mPrimaryCanvas.setColorPreferences(fColorPrefs);
        mParent.setFont(mPrimaryCanvas.getComponent().getFont()); // layout needs this
        mProfilingLayout.setPrimaryCanvas(mPrimaryCanvas.getComponent());
        mViewLayout.setScroller(mScrollPane);


        //mVertSB = new JScrollBar(Adjustable.VERTICAL);
        mVertSB = mScrollPane.getVerticalScrollBar();
        mHorzSB = mScrollPane.getHorizontalScrollBar();
        mVertSB.setVisible(true);
        mHorzSB.setVisible(false);
    }
    
    private boolean isFirstColumnShowing(int i){
        for (int j = 0; j < i; j++){
            if (mColumnCanvas[j] != null) return false;          
        }
        return true;
    }
    
    /**
     * @param i
     */
    private void createColumnCanvas(int i) {
        int lineCnt = mPrimaryCanvas != null?mPrimaryCanvas.getLineCount():mInitialLineCount;
        mColumnCanvas[i] = new SquareCanvas(mMainPanel,lineCnt,PROFILE_COLUMN_WIDTH,mCallback,mFactory,i+1);
        if (mFont != null)
            mColumnCanvas[i].setFont(mFont);
        if (fColorPrefs != null)
            mColumnCanvas[i].setColorPreferences(fColorPrefs);
        mProfilingLayout.setColumnCanvas(mColumnCanvas[i].getComponent(),i);
        if (!isFirstColumnShowing(i)){
            mColumnCanvas[i].setVerticalLine(true,1,getLineColor());
        }
        else if (mNumVisibleColumns == 1){
            mPrimaryCanvas.setVerticalLine(true, 1, getLineColor());
        }
        if (mColumnHeaders[i] != null){
//            IContainer c = mFactory.makeContainer(mParent,IComponentFactory.COLUMN_STYLE);
//            c.setMargins(0,0,0,0);
//            c.setBorder(IComponent.BEVEL_OUT_BORDER);
            String text = mColumnHeaders[i];
//            if (false && text.length() > PROFILE_COLUMN_WIDTH+1){
//                // If label too long, then truncate it with "..."
//                text = text.substring(0,PROFILE_COLUMN_WIDTH-1) + "...";
//            }
            ILabel l = mFactory.makeLabel(mParent,text); 
            //if (mFont != null) l.setFont(mFactory.makeFont(mFont));
            if (mHeaderFont == null){
                mHeaderFont = mFactory.makeFont("Ariel",true,false,6);
            }
            l.setFont(mHeaderFont);
            //l.setWrap(true);
            l.setBorder(IComponent.BEVEL_OUT_BORDER);
            
            l.setToolTipText(mColumnHeaders[i]);
            l.setHorizontalAlignment(IComponent.CENTER);
            l.setVerticalAlignment(IComponent.END);
            l.setMargins(0,0,0,0);
            l.getComponent(); //instantiate
            mColumnHeaderLabels[i] = l;
            mViewLayout.setHeaderLabel(l,i);
        }
    }

    private IColor getLineColor () {
        return mFactory.makeColor(100,100,100);
    }

    IScrollPane getScrollPane(){
        return mScrollPane;
    }

    ISquareCanvas getPrimaryCanvas() {
        return mPrimaryCanvas;
    }

    /**
     * The display in which this view lies is being disposed. Free up all
     * reasources.
     */
    void dispose() {
        mPrimaryCanvas.dispose();
        for (int i = 0; i < mColumnCanvas.length; i++) {
            if (mColumnCanvas[i] != null) {
                mColumnCanvas[i].dispose();
                mColumnCanvas[i] = null;
            }
        }
    }

    public boolean isColumnVisible(int column) {
        return mColumnVisible[column];
    }

    public void setColumnVisible(int column, boolean visible) {
        trace("setColumnVisible " + column + " " + visible);
        // A column can be referenced that doesn't exist
        // during debugger startup. The previous application
        // may have had a DLL that defined more columns than
        // we currently have. So, ignore column references for
        // which there is no colum header.
        if (mColumnHeaders[column] != null) {
            if (mColumnVisible[column] != visible) {
                mColumnVisible[column] = visible;
                if (visible) {
                    mNumVisibleColumns++;
                    createColumnCanvas(column);
                }
                else {
                    mNumVisibleColumns--;
                    if (mColumnCanvas[column] != null){
                        mColumnCanvas[column].dispose();
                        mColumnCanvas[column] = null;
                    }
                    if (mColumnHeaderLabels[column] != null){
                        mColumnHeaderLabels[column].dispose();
                        mColumnHeaderLabels[column] = null;
                    }
                    if (mNumVisibleColumns == 0){
                        mPrimaryCanvas.setVerticalLine(true,0,null);
                    }
                }               
                mParent.layout();
                this.mMainPanel.layout();
                refresh();
            }
        }
    }

    public void setVerticalScrollerEnabled(boolean enabled) {
        mVertSB.setVisible(enabled);
    }

    public int getNumColumns() {
        return mNumVisibleColumns + 1;
    }

    public int getVisibleColumnBitmap() {
        int bits = 0;
        if (mNumVisibleColumns > 0) {
            for (int i = 0; i < MAX_NUM_PROFILE_COLUMNS; i++) {
                if (mColumnVisible[i]) {
                    bits |= (1 << i);
                }
            }
        }
        return bits;

    }
    
    public int getMaxProfileColumns(){
        return MAX_NUM_PROFILE_COLUMNS;
    }

    public boolean toggleColumn(int column) {
        trace("toggle column " + column);
        boolean visible = !mColumnVisible[column];
        setColumnVisible(column, visible);
        return visible;
    }

   public void setColumnHeaders(List<String> headers) {
        int i = 0;
        for (String label: headers) {
            if (i < mColumnHeaders.length) {// should always be true
                mColumnHeaders[i] = label;
                if (this.mColumnHeaderLabels[i] != null){
                    mColumnHeaderLabels[i].setText(label);
                }
                i++;
            }
        }
    }

   /**
     * Set the title string of the Primary Canvas Header.
     */
    public void setPrimaryHeader(String title) {
        if (title != null && title.length() == 0){
            title = null;
        }
        if (title != null) {
            if (mPrimaryHeader != null)
                mPrimaryHeader.setText(title);
            else {
                mPrimaryHeader = mFactory.makeLabel(mParent,title);
                mViewLayout.setPrimaryHeader(mPrimaryHeader);
            }
        }
        else {
            mPrimaryHeader.dispose();
        }        
    }

//    /**
//     * set the background and foreground color of the header labels. This is
//     * handy for colorizing Thread Locking.
//     */
//    public void setHeadingColorScheme(int scheme) {
//        if (scheme > ColorMgmt.getColorCount())
//            scheme = 0;
//
//        setHeaderColorScheme(mPrimaryHeader, scheme);
//        for (int i = 0; i < mColumnHeaders.length; i++) {
//            ILabel label = mColumnHeaders[i];
//            if (label != null)
//                setHeaderColorScheme(label, scheme);
//        }
//    }
//
//    private void setHeaderColorScheme(ILabel l, int scheme) {
//        if (scheme == 0) {;
//            l.setBackground(UIManager.getColor("Label.background"));
//            l.setForeground(UIManager.getColor("Label.foreground"));
//        } else {
//            int index = scheme - 1;
//            l.setBackground(ColorMgmt.getBackgroundColor(index));
//            l.setForeground(ColorMgmt.getForegroundColor(index));
//        }
//    }




    /**
     * This is called after the DynamicTextView has been fully sized and layed
     * out. Its now ready to be populated from the lines that were cached in the
     * model.
     *  
     */
    void initialize() {
        mPrimaryCanvas.set(mModel.getLines());
    }
    
    public void setColorPreferences(IColorPreferences prefs){
        fColorPrefs = prefs;
        IColor foreground = prefs.getForegroundColor();
        mParent.setForeground(foreground);
        if (mPrimaryCanvas != null)
            mPrimaryCanvas.setColorPreferences(prefs);
        for (ISquareCanvas canvas: this.mColumnCanvas){
            if (canvas != null) canvas.setColorPreferences(prefs);
        }
        if (mPrimaryHeader != null)
            mPrimaryHeader.setForeground(foreground);
        for (IComponent labelContainer: mColumnHeaderLabels){
            if (labelContainer != null){
                labelContainer.setForeground(foreground);
                if (labelContainer instanceof IContainer){
                    IComponent children[] = ((IContainer)labelContainer).getChildren();
                    for (IComponent label: children){
                        label.setForeground(foreground);
                    }
                }
            }
        }
        mParent.layout();       
    }
    
    public void setFont(Object font){
        IFont ifont = mFactory.makeFont(font);
        mParent.setFont(ifont);
        mFont = font;
        mPrimaryCanvas.setFont(font);
        for (ISquareCanvas canvas: this.mColumnCanvas){
            if (canvas != null) canvas.setFont(font);
        }
//        if (mPrimaryHeader != null)
//            mPrimaryHeader.setFont(ifont);
//        for (IComponent labelContainer: mColumnHeaderLabels){
//            if (labelContainer != null){
//                labelContainer.setFont(ifont);
//                if (labelContainer instanceof IContainer){
//                    IComponent children[] = ((IContainer)labelContainer).getChildren();
//                    for (IComponent label: children){
//                        label.setFont(ifont);
//                    }
//                }
//            }
//        }
        mParent.layout();
        
    }
    
    /**
     * Set name for benefit of the GUI tester so that it can find the
     * underlying components.
     * @param name the name to be assigned to the underlying components.
     */
    public void setName(String name){
        mPrimaryCanvas.setName(name);        
    }

    public void refresh() {
        trace("dynamicTextModelChanged");
        Line newLines[] = mModel.getLines();

        // display primary data
        mPrimaryCanvas.set(newLines);

        // display profile data
        if (mNumVisibleColumns > 0) {
            trace("dynamicTextModelChanged");
            for (int i = 0; i < mColumnCanvas.length; i++) {
                trace("dynamicTextModelChanged, i=" + i);
                if (mColumnVisible[i]) {
                    Line data[] = mModel.getColumnData(i);
                    if (data != null) {
                        ISquareCanvas canvas = mColumnCanvas[i];
                        if (canvas != null) { // should always be true
                            canvas.set(data);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Set a range of text to be highlighted as "selected".
     * @param line1 starting line, 0 based.
     * @param col1 starting column in line, 0 based.
     * @param line2 end line, inclusive, 0 based.
     * @param col2 end column, inclusive, 0 based.
     */
    public void setSelection(int line1, int col1, int line2, int col2) {
        mPrimaryCanvas.setSelection(line1, col1, line2, col2);
    }
    
    public String getSelection() {
        return mPrimaryCanvas.getSelection();
    }

    private static Toggle sTrace = Toggle.define("TRACE_DYNAMIC_TEXT_VIEW",
            false);

    private boolean tracing() {
        return sTrace.on();
    }

    private void trace(String msg) {
        if (tracing())
            Log.log("DynamicTextView", msg);
    }
    
//    static class LabelLayout implements ILayoutManager{
//
//        
//        public Dimension computeSize (IContainer c, int wHint, int hHint) {
//            IComponent children[] = c.getChildren();
//            int height = 0;
//            int width = 0;
//            for (IComponent kid: children){
//                IFont f = kid.getFont();
//                height = Math.max(f.getSize()+3,height);
//                width += kid.computeSize(wHint,hHint).getWidth();
//            }
//            if (hHint != DEFAULT_SIZE){
//                height = Math.max(hHint,height);
//            }
//            return new Dimension(width,height);
//        }
//
//        public void layout (IContainer c) {
//            IComponent children[] = c.getChildren();
//            Rectangle rect = c.getClientArea();
//            for (IComponent kid: children){               
//                kid.setBounds(rect.x,rect.y,rect.width,rect.height);
//            }
//            
//        }
//        
//    }
}

