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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.custom.TableCursor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import com.arc.widgets.IAttributedString;
import com.arc.widgets.IColor;
import com.arc.widgets.IContainer;
import com.arc.widgets.IFont;
import com.arc.widgets.ITable;

/**
 * SWT implementation of a table.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */

class SWTTable extends AbstractContainer implements ITable {

    private Table mTable = null;
    private TableCursor mCursor = null;
    private int mColumnCount;
    private IFont[] mFonts = null; // Fonts per column
    private Color[] mColors = null;  // foreground color per column
    private ArrayList<ISelectionListener> mListeners;
    private int mRow;
    private int mColumn;
    private ControlEditor mEditor;
    private int[] mWidths;
    private Text mPendingTextInput;
    private Map<Integer,IAttributedString>[] mAttributedValues = null;
    private boolean mColumnCountChanging = false;
    private boolean mVerticalScrollEnabled = true;
    
    public SWTTable(IContainer parent, IComponentMapper mapper) {
        super(parent, mapper);
    }

    @Override
    protected Widget instantiate () {
        mTable = new Table(this.getParentComposite(),SWT.BORDER | SWT.MULTI );
        if (!mVerticalScrollEnabled)
            mTable.getVerticalBar().setVisible(false);
        mCursor = new TableCursor(mTable,SWT.NONE);
        for (int i = 0; i < mColumnCount; i++){
            makeColumn(i);
        }
        mCursor.addMouseListener(new MouseListener(){

            @Override
            public void mouseDoubleClick (MouseEvent e) {
                fireChange(mRow,mColumn,true);
                
            }

            @Override
            public void mouseDown (MouseEvent e) {
                // @todo Auto-generated method stub
                
            }

            @Override
            public void mouseUp (MouseEvent e) {
                // @todo Auto-generated method stub
                
            }});
        mCursor.addSelectionListener(new SelectionListener(){

            @Override
            public void widgetDefaultSelected (SelectionEvent e) {
                if (mPendingTextInput != null)
                    applyInput();
                mRow = mTable.indexOf(mCursor.getRow());
                mColumn = mCursor.getColumn();
                fireChange(mRow,mColumn,true);
                
            }

            @Override
            public void widgetSelected (SelectionEvent e) {
                if (mPendingTextInput != null)
                    applyInput();
                mRow = mTable.indexOf(mCursor.getRow());
                mColumn = mCursor.getColumn();
                fireChange(mRow,mColumn,false);            
            }       

            });
        
        mTable.setLinesVisible(true);
        return mTable;
    }
    
    @Override
    public void setVerticalScroll(boolean enabled){
        mVerticalScrollEnabled = enabled;
        if (mTable != null) mTable.getVerticalBar().setVisible(enabled);
    }

    private TableColumn makeColumn(final int i){
        return new TableColumn(mTable,SWT.LEFT,i);        
    }
    
    private TableColumn getColumnItem(int column){
        if (column >= mTable.getColumnCount()) {
            setColumnCount(column+1);
        }
        return mTable.getColumn(column);
        
    }
    
//    private int computeColumn(int x){
//        int cnt = mTable.getColumnCount();
//        int xPos = 0;
//        for (int i = 0; i < cnt; i++){
//            TableColumn tc = mTable.getColumn(i);
//            xPos += tc.getWidth();
//            if (xPos > x) return i;
//        }
//        return -1;
//    }
//    
//    private void select(TableItem item, int column){
//        mTable.deselectAll();
//        if ((mSelected != item || column != mSelectedColumn)){
//            if (mSelected != null&& mSelectedColumn >= 0){
//                mSelected.setBackground(mSelectedColumn,mTable.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
//                mSelected.setForeground(column,mColors != null && mColors[mSelectedColumn] != null?
//                    mColors[mSelectedColumn]:mTable.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
//            }
//            if (column >= 0) {
//                mSelected = item;
//                mSelectedColumn = column;
//                if (item != null) {
//                    item.setBackground(column,mTable.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
//                    item.setForeground(column,mTable.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
//                }
//            }
//            else mSelected = null;
//        }
//    }

    @Override
    public void setColumnCount (int n) {
        if (mTable.getDisplay().getThread() != Thread.currentThread()){
            throw new IllegalStateException("Not UI thread");
        }
        try {
            mColumnCountChanging  = true; // Suppress size-change events.
            if (mTable != null) {
                int columns = mTable.getColumnCount();
                if (columns < n) {
                    for (int i = columns; i < n; i++) {
                        makeColumn(i);
                    }
                }
                else if (columns > n) {
                    for (int i = columns - 1; i >= n; i--) {
                        TableColumn c = mTable.getColumn(i);
                        c.dispose();
                    }
                }
            }
            mColumnCount = n;
        }
        finally {
            mColumnCountChanging = false;
        }
    }

    @Override
    public void setColumnFont (int column, IFont font) {
        if (column < 0 || column >= mColumnCount)
            throw new IllegalArgumentException("Column out of range");
        if (mFonts == null || mFonts.length < mColumnCount){
            IFont newFonts[] = new IFont[mColumnCount];
            if (mFonts != null){
                System.arraycopy(mFonts,0,newFonts,0,mFonts.length);
            }
            mFonts = newFonts;
        }
        mFonts[column] = font;
        if (mTable != null) {
            TableItem items[] = mTable.getItems();
            IFont f = mFonts[column];
            Font actual;
            if (f == null) actual = mTable.getFont();
            else actual = (Font)f.getObject();
            for (TableItem ti: items){
                ti.setFont(column,actual);
            }
        }
    }
    
    @Override
    public IFont getColumnFont(int column){
        if (column < 0 || column >= mColumnCount)
            throw new IllegalArgumentException("Column out of range");
        if (mFonts != null && column < mFonts.length && mFonts[column] != null)
            return mFonts[column];
        return getFont();       
    }

    @Override
    public void setColumnForeground (int column, IColor color) {
        if (column < 0 || column >= mColumnCount)
            throw new IllegalArgumentException("Column out of range");
        if (mColors == null || mColors.length < mColumnCount){
            Color newColors[] = new Color[mColumnCount];
            if (mColors != null){
                System.arraycopy(mColors,0,newColors,0,mColors.length);
            }
            mColors = newColors;
        }
        mColors[column] = color != null?(Color)color.getObject():null;
        if (mTable != null) {
            TableItem items[] = mTable.getItems();
            Color c = mColors[column];
            if (c == null) c = mTable.getForeground();
            for (TableItem ti: items){
                ti.setForeground(column,c);
            }
        }
    }

    @Override
    public void setColumnHeaders (String[] headers) {
        getComponent();
        if (headers == null) {
            mTable.setHeaderVisible(false);
        }
        else {
            mTable.setHeaderVisible(true);
            int cnt = Math.min(mTable.getColumnCount(), headers.length);
            for (int i = 0; i < cnt; i++) {
                mTable.getColumn(i).setText(headers[i]);
            }
        }
    }
    
    @Override
    public void pack(){
        if (mTable != null) {
            int cnt = mTable.getColumnCount();
          
            for (int i = 1; i < cnt; i++ ){
                mTable.getColumn(i).pack();
            }
            // BUG: Eclipse 3.3: 0'th column doesn't get set to reasonable value, at least
            // under Windows.
            if (true){
                GC gc = new GC(mTable);
                try {
                    int charWidth = gc.getCharWidth('W');
                    int charCount = 0;
                    int rowCount = mTable.getItemCount();
                    for (int i = 0; i < rowCount; i++){
                        charCount = Math.max(charCount,this.getItem(i,0).length());
                    }
                    mTable.getColumn(0).setWidth((charCount+2) * charWidth);
                }
                finally {
                    gc.dispose();
                }
            }
        }
    }

    @Override
    public void setColumnWidths (int[] widths) {
        getComponent();
        int cnt = Math.min(mTable.getColumnCount(), widths.length);
        mWidths = new int[cnt];
        System.arraycopy(widths, 0, mWidths, 0, cnt);
        GC gc = new GC(mTable);
        try {
            int charWidth = gc.getCharWidth('X');
            for (int i = 0; i < cnt; i++) {
                if (widths[i] == 0)
                    mTable.getColumn(i).pack();
                else
                    mTable.getColumn(i).setWidth(widths[i] * charWidth);
            }
        }
        finally {
            gc.dispose();
        }
    }
    
    private void setFontAndColor(TableItem item){
        if (mFonts != null){
            int cnt = mTable.getColumnCount();
            for (int i = 0; i < cnt; i++){
                if (mFonts[i] != null){
                    item.setFont(i,(Font)mFonts[i].getObject());
                }
            }
        }
        if (mColors != null){
            int cnt = mTable.getColumnCount();
            for (int i = 0; i < cnt; i++){
                if (mColors[i] != null){
                    item.setForeground(i,mColors[i]);
                }
            }
        }
    }

    /**
     * Return the item corresponding to a row, creating it if necessary.
     * @param row
     * @return the item corresponding to a row.
     */
    private TableItem grabItem (int row) {
        getComponent();
        int rowCnt = mTable.getItemCount();
        for (int i = rowCnt; i < row; i++){
            TableItem ti = new TableItem(mTable,0);
            setFontAndColor(ti);
        }
        if (mTable.getItemCount() <= row){
            setFontAndColor(new TableItem(mTable,0));
        }
        TableItem item = mTable.getItem(row);
        return item;
    }

    @Override
    public void setItem (int row, int column, String value) {
        getComponent();
        if (column >= mColumnCount){
            setColumnCount(column+1);
            assert column < mColumnCount;
        }
        setAttributedValue(row,column,null); // no longer attributed
        TableItem item = grabItem(row);
        item.setText(column,value);
        if (mWidths == null || column >= mWidths.length || mWidths[column] == 0)
            mTable.getColumn(column).pack();
    }
    
    @Override
    public int getRowCount() {
        return mTable.getItemCount();
    }
    
    @Override
    public void setRowCount(int rowCount){
        if (mTable != null){
            int itemCount = mTable.getItemCount();
            mTable.setItemCount(rowCount);
            for (int i = itemCount; i < rowCount; i++){
                setFontAndColor(mTable.getItem(i));
            }
        }
    }
    
    @Override
    public String getItem (int row, int column) {
        if (column >= mColumnCount) throw new IllegalArgumentException("Non-existent column: " + column);
        TableItem item = grabItem(row);
        if (item != null) return item.getText(column);
        throw new IllegalArgumentException("Missing row: " + row);
    }

    @Override
    public int getContainerStyle () {
        // @todo Auto-generated method stub
        return 0;
    }

    @Override
    public void setHorizontalSpacing (int pixels) {
        // @todo Auto-generated method stub

    }

    @Override
    public void setVerticalSpacing (int pixels) {
        // @todo Auto-generated method stub

    }

    @Override
    public void setBackground (int row, int column, IColor color) {
        getComponent();
        TableItem item = grabItem(row);
        Color c = color != null?(Color)color.getObject():mTable.getBackground();
        item.setBackground(column,c);       
    }

    @Override
    public void setForeground (int row, int column, IColor color) {
        getComponent();
        TableItem item = grabItem(row);
        Color c;
        if (color == null){
            if (mColors != null && mColors[column] != null)
                c = mColors[column];
            else
                c = mTable.getForeground();
        }
        else c = (Color)color.getObject();
        item.setForeground(column,c);             
    }

    @Override
    public void addSelectionListener (ISelectionListener l) {
        if (mListeners == null) mListeners = new ArrayList<ISelectionListener>();
        mListeners.add(l);    
    }
    
    protected void fireChange(int row, int column,boolean doubleClicked){
        if (mListeners != null){
            for (ISelectionListener s: mListeners){
                if (doubleClicked) s.onTableItemDoubleClicked(row, column);
                else s.onTableItemSelected(row, column);
            }
        }
    }

    @Override
    public void removeSelectionListener (ISelectionListener l) {
        if (mListeners != null) mListeners.remove(l);
        
    }

    @Override
    public void setColumnSelectable (int column, boolean v) {
        // @todo Auto-generated method stub
        
    }

    @Override
    public void setSelection (int row, int column) {
        getComponent();
        if (mPendingTextInput != null)
            applyInput();
        mRow = row;
        mColumn = column;
        mCursor.setSelection(row,column);
        
    }

    @Override
    public void setInput (final int row, final int column) {
        getComponent();
        mCursor.setSelection(row,column);
        if (mEditor == null){
            mEditor = new ControlEditor(mCursor);
            mEditor.grabHorizontal = true;
            mEditor.grabVertical = true;
        }
        final TableItem rowItem = mTable.getItem(row);
        final Text text = new Text(mCursor,SWT.BORDER);
        text.setFont(rowItem.getFont(column));
        mPendingTextInput = text;
        text.setText(rowItem.getText(column));
        text.selectAll();
        text.addKeyListener(new KeyAdapter() {
              @Override
            public void keyPressed(KeyEvent e) {
                  // close the text editor and copy the data over 
                  // when the user hits "ENTER"
                  if (e.character == SWT.CR) {
                      applyInput();
                  }
                  // close the text editor when the user hits "ESC"
                  if (e.character == SWT.ESC) {
                      text.dispose();
                  }
              }
        });
        mEditor.setEditor(text);
        text.setFocus();       
    }
    
    /**
     * The user has just hit ENTER key on entering text, or he's clicked outside of the cell.
     */
    private void applyInput () {
        Text text = mPendingTextInput;
        mPendingTextInput = null;
        String value = text.getText();
        String  apply = null;
        if (mListeners != null) {
            for (ISelectionListener o : mListeners) {
                apply = o.onNewValueEntered(mRow, mColumn, value);
            }
        }
        if (apply != null) {
            mTable.getItem(mRow).setText(mColumn, apply);
            if (mWidths == null || mColumn >= mWidths.length || mWidths[mColumn] == 0)
                mTable.getColumn(mColumn).pack();
        }
        text.dispose();
    }
    
    @SuppressWarnings("unchecked")
    private void setAttributedValue(int row, int column, IAttributedString value){
        if (mAttributedValues == null || column >= mAttributedValues.length){
            if (value == null) return;
            Map<Integer,IAttributedString>list[] = new Map[mColumnCount];
            if (mAttributedValues != null)
                System.arraycopy(mAttributedValues,0,list,0,mAttributedValues.length);
            mAttributedValues = list;
        }
        if (mAttributedValues[column] == null){
            if (value == null) return;
            mAttributedValues[column] = new HashMap<Integer,IAttributedString>();
        }
        if (value != null)
            mAttributedValues[column].put(row,value);
        else
            mAttributedValues[column].remove(row);
    }

    @Override
    public void setItem (int row, int column, IAttributedString value) {
//        getComponent();
//        if (column >= mColumnCount){
//            setColumnCount(column+1);
//        }
//        setAttributedValue(row,column,value);
//        if (mPaintListener == null){
//            mTable.addListener(SWT.EraseItem, new Listener(){
//
//                public void handleEvent (Event event) {
//                    // @todo Auto-generated method stub
//                    
//                }});
//        }
    }

    @Override
    public void setColumnAlignment (int column, Alignment alignment) {
        getComponent();
        TableColumn c = getColumnItem(column);
        int a = SWT.LEFT;
        switch (alignment) {
        case LEFT:
            a = SWT.LEFT;
            break;
        case CENTER:
            a = SWT.CENTER;
            break;
        case RIGHT:
            a = SWT.RIGHT;
            break;
        }
        c.setAlignment(a);
    }
    
    @Override
    protected void notifyObserversOfSizeChange(){
        if (!this.mColumnCountChanging){
            //Only issue resize event if columns are not getting programmatically reset.
            super.notifyObserversOfSizeChange();
        }
    }
}
