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

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Widget;

import com.arc.widgets.IAttributedString;
import com.arc.widgets.IColor;
import com.arc.widgets.IContainer;
import com.arc.widgets.ICopyToClipboard;
import com.arc.widgets.IFont;
import com.arc.widgets.IImage;
import com.arc.widgets.ITextCanvas;

/**
 * This implements the SeeCode "SquareCanvas" stuff.
 * <P>
 * @author David Pickens
 */
class SWTTextCanvas extends Component implements ITextCanvas {

    private int mLeftMargin;
    private IconColumn mIconColumn = null;
    private TextColumn mTextColumn = null;
    private boolean mIncludeIconColumn;
    private boolean mIconsPresent = false;
    private String mName = null;
    /**
     * WE maintain list of icons as <image,x-position,linenumber> so that
     * we can regenerate them if the font size changes.
     */
    private List<Object[]> mIconList = new ArrayList<Object[]>();
    private boolean mSelectable;
    private ICopyToClipboard fClipboard = null;

    /**
     * @param parent
     * @param mapper
     */
    SWTTextCanvas(IContainer parent, boolean includeIconColumn, boolean selectable, IComponentMapper mapper) {
        super(parent, mapper);
        mIncludeIconColumn = includeIconColumn;
        mSelectable = selectable;

    }
    /* (non-Javadoc)
     * @see com.arc.widgets.swt.Component#instantiate()
     */
    @Override
    protected Widget instantiate() {
        Composite container = new Composite(this.getParentComposite(),0);
        

        if (mIncludeIconColumn){
            container.setLayout(new MyTextCanvasLayout());
            mIconColumn = new IconColumn(container,this);
        }
        else {
            GridLayout gl = new GridLayout(1,false);
            gl.marginHeight = 0;
            gl.marginWidth = 0;
            container.setLayout(gl);
        }
        mTextColumn = new TextColumn(container,this,mSelectable);
        if (fClipboard != null) mTextColumn.setCopyToClipboard(fClipboard);
        if (mIconColumn != null)
            mIconColumn.setLineHeight(mTextColumn.getLineHeight());
        else {
            mTextColumn.setLayoutData(new GridData(GridData.FILL_BOTH));
        }
        if (mName != null) setName(mName);
        return container;
    }
    
    @Override
    public void setName(String name){
        // For GUI tester
        mName = name;
        if (mTextColumn != null) mTextColumn.setData("name",name);
    }
    
    @Override
    public String getName(){
        return mName;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.ITextCanvas#getDisplayableLines()
     */
    @Override
    public int getDisplayableLines() {
        if (mTextColumn != null){
            return mTextColumn.getMaxLines();
        }
        return 0;
    }

//    /* (non-Javadoc)
//     * @see com.arc.widgets.ITextCanvas#scrollVertically(int)
//     */
//    public void scrollVertically(int amount) {
//        // TODO Auto-generated method stub
//        
//    }

    /* (non-Javadoc)
     * @see com.arc.widgets.ITextCanvas#setLine(int, com.arc.widgets.IAttributedString)
     */
    @Override
    public void setLine(int line, IAttributedString strings[], IColor backgroundColor) {
        if (mTextColumn == null) getComponent();
        mTextColumn.setLine(line,strings);
        mTextColumn.setLineColor(line,backgroundColor != null?(Color)backgroundColor.getObject():null);       
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.ITextCanvas#clearIcons(int)
     */
    @Override
    public void clearIcons(int line) {
        if (mIconColumn != null){
            mIconColumn.clearIcons(mTextColumn.getLinePosition(line));
            Iterator<Object[]> each = mIconList.iterator();
            while (each.hasNext()) {
                int eline = (Integer)each.next()[2];
                if (eline == line){
                    each.remove();
                }
            }
        }      
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.ITextCanvas#clearLine(int)
     */
    @Override
    public void clearLine(int line) {
        if (mTextColumn != null){
            mTextColumn.clearLine(line);
        }
        
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.ITextCanvas#repaint()
     */
    @Override
    public void repaint() {
        if (mIconColumn != null)
            mIconColumn.redraw();
        mTextColumn.repaintDirtyLines();       
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.ITextCanvas#setIcon(com.arc.widgets.IImage, int, int)
     */
    @Override
    public void setIcon(IImage image, int x, int lineNumber) {
        Composite container = (Composite)getComponent(); // force allocation if not already
        if (mIconColumn != null) {
            boolean found = false;
            for (Object[] e: mIconList){
                int ex = (Integer)e[1];
                int eline = (Integer)e[2];
                if (ex == x && eline == lineNumber){
                    e[0] = image;
                    found = true; 
                    break;
                }
            }
            if (!found)
                mIconList.add(new Object[]{image,x,lineNumber});
            mIconColumn.setIcon((Image)image.getObject(),x,
                mTextColumn.getLinePosition(lineNumber));
            if (!mIconsPresent){
                mIconsPresent = true;
                container.layout(true); // create room for icon column
            }
        }
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.ITextCanvas#addTreeIndicator(int, int)
     */
    @Override
    public void setTreeIndicators(int line, String treePrefix) {
        getComponent(); // force construction
        mTextColumn.setTreeIndicators(line,treePrefix);       
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.ITextCanvas#getLineCount()
     */
    @Override
    public int getLineCount() {
        getComponent(); // force construction
        return mTextColumn.getLineCount();
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.ITextCanvas#setLeftMargin(int)
     */
    @Override
    public void setLeftMargin(int amount) {
        mLeftMargin = amount;
        if (mComponent != null){
            Composite c = (Composite)getComponent();
            c.layout();
            c.redraw();
        }
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.ITextCanvas#getDisplayableColumns()
     */
    @Override
    public int getDisplayableColumns() {
        getComponent(); // force construction
        return mTextColumn.getMaxColumns();
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.ITextCanvas#showToolTip(java.lang.String, int, int)
     */
    @Override
    public void showToolTip(String tooltip, int line, int column) {
        getComponent();
        mTextColumn.showToolTip(tooltip,line,column);       
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.ITextCanvas#scrollHorizontally(int)
     */
    @Override
    public void scrollHorizontally(int column) {
        getComponent();
        mTextColumn.scrollHorizontally(column);
        
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.ITextCanvas#addObserver(com.arc.widgets.ITextCanvas.IObserver)
     */
    @Override
    public void addObserver(IObserver observer) {
        getComponent();
        mTextColumn.addObserver(observer);
        if (mIconColumn != null)
            mIconColumn.addObserver(observer);
        
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.ITextCanvas#removeObserver(com.arc.widgets.ITextCanvas.IObserver)
     */
    @Override
    public void removeObserver(IObserver observer) {
        mTextColumn.removeObserver(observer);
        if (mIconColumn != null)
            mIconColumn.removeObserver(observer);
        
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.ITextCanvas#getSelectedLine()
     */
    @Override
    public int getSelectedLine() {
        if (mTextColumn != null)
            return mTextColumn.getSelectedLine();
        return -1;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.ITextCanvas#getSelectedStartColumn()
     */
    @Override
    public int getSelectedStartColumn() {
        if (mTextColumn != null)
            return mTextColumn.getSelectedStartColumn();
        return -1;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.ITextCanvas#getSelectedEndColumn()
     */
    @Override
    public int getSelectedEndColumn() {
        if (mTextColumn != null)
            return mTextColumn.getSelectedEndColumn();
        return -1;
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.ITextCanvas#getSelection()
     */
    @Override
    public String getSelection() {
        if (mTextColumn != null){
            return mTextColumn.getSelection();
        }
        return null;
    }
    
    class MyTextCanvasLayout extends Layout{

        /* (non-Javadoc)
         * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite, int, int, boolean)
         */
        @Override
        protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
            Control[] kids = composite.getChildren();
            if (kids.length == 2){
                // Should always be true.
                // The first is the icon column the
                // second is the text canvas.
                // NOTE: always show margin. Debugger seems to assume this when
                // prepending profiling columns, etc.
                if (true /*|| mIconsPresent || mIconColumn.hasIcons()*/){
                    Point p1 = kids[0].computeSize(SWT.DEFAULT,hHint,flushCache);
                    int widthHint = wHint == SWT.DEFAULT?SWT.DEFAULT:mLeftMargin-p1.x;
                    if (widthHint <= 0) widthHint = SWT.DEFAULT;
                    Point p2 = kids[1].computeSize(widthHint,hHint,flushCache);
                    return new Point(mLeftMargin+p2.x,Math.max(p1.y,p2.y));
                }
//                else
//                    return kids[1].computeSize(wHint,hHint,flushCache);
            }
            // No icon column
            return kids[0].computeSize(wHint,hHint,flushCache);
        }

        /* (non-Javadoc)
         * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite, boolean)
         */
        @Override
        protected void layout(Composite composite, boolean flushCache) {
            Control[] kids = composite.getChildren();
            Rectangle area = composite.getClientArea();
            if (kids.length == 2){  
                // NOTE: always show margin. Debugger seems to assume this when
                // prepending profiling columns, etc.
                if (true  /*|| mIconsPresent || mIconColumn.hasIcons()*/){
                    mIconsPresent = true;
                    Rectangle r = new Rectangle(area.x,area.y,mLeftMargin,area.height);
                    kids[0].setBounds(r);
                    r = new Rectangle(mLeftMargin,0,area.width-mLeftMargin,area.height);
                    kids[1].setBounds(r);
                }
//                else
//                    kids[1].setBounds(area); // no icons seen yet
            }
            else {
                // No icon column
                kids[0].setBounds(area);
            }
            
        }
        
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.ITextCanvas#unselect()
     */
    @Override
    public void unselect() {
        mTextColumn.unselect();      
    }
    /* (non-Javadoc)
     * @see com.arc.widgets.ITextCanvas#setSelectedLine(int)
     */
    @Override
    public void setSelectedLine(int line) {
        mTextColumn.setSelectedLine(line);
        
    }
 
    @Override
    protected Font getActualFont () {
        return mTextColumn.getFont();
    }
    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param font
     */
    @Override
    public void setFont (IFont font) {
        super.setFont(font);
        mTextColumn.setFont((Font)font.getObject());
        // Since the font size may have changed, regenerate the Icon positions.
        if (mIconColumn != null){
        	mIconColumn.setLineHeight(mTextColumn.getLineHeight());
            mIconColumn.clearIcons();
            for (Object[] e: mIconList){
                int x = (Integer)e[1];
                int line = (Integer)e[2];
                IImage image = (IImage)e[0];
                setIcon(image,x,line);
            }
        }           
    }
    
    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param color
     */
    @Override
    public void setForeground (IColor color) {
        super.setForeground(color);
        mTextColumn.setForeground((Color)color.getObject());        
    }
    
    @Override
    public void setBackground (IColor color) {
        super.setBackground(color);
        mTextColumn.setBackground((Color)color.getObject());        
    }
    
    @Override
    public int getLineHeight () {
        if (mTextColumn == null){
            getComponent(); // force instantiation
        }
        return mTextColumn.getLineHeight();
    }
    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param height
     */
    @Override
    public void setLineHeight (int height) {
        mTextColumn.setLineHeight(height);    
    }
    
    @Override
    public void setVerticalLine(boolean onLeft, int thickness, Object color){
        if (onLeft && this.mIconColumn != null){
            this.mIconColumn.setVerticalLine(onLeft,color,thickness);
        }
        else
            this.mTextColumn.setVerticalLine(onLeft,color,thickness);
    }
    
    public void recordState(OutputStream outStream){
        PrintStream out = outStream instanceof PrintStream?(PrintStream)outStream:new PrintStream(outStream);
        out.println("<TextCanvas>");
        mTextColumn.recordState(out);
        if (mIconColumn != null){
            mIconColumn.recordState(out);
        }
        out.println("</TextCanvas>");
    }
    @Override
    public void setCopyToClipboard (ICopyToClipboard callback) {
        fClipboard  = callback;
        if (mTextColumn != null)
            mTextColumn.setCopyToClipboard(callback);
        
    }
    @Override
    public void copyToClipboard () {
       mTextColumn.copyToClipboard();     
    }
    @Override
    public void setSelection (int line1, int col1, int line2, int col2) {
        this.mTextColumn.setSelection(line1,col1,line2,col2);      
    }

}
