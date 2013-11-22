/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.textcanvas;


import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

/**
 * A cell oriented Canvas. Maintains a list of "cells".
 * It can either be vertically or horizontally scrolled.
 * The CellRenderer is responsible for painting the cell.
 * 
 * CUSTOMIZATION: make implement adaptable.
 */
public class TextCanvas extends GridCanvas implements IAdaptable {
	protected final ITextCanvasModel fCellCanvasModel;
	/** Renders the cells */
	private final ILinelRenderer fCellRenderer;
	private boolean fScrollLock;
	private Point fDraggingStart;
	private Point fDraggingEnd;
	private boolean fHasSelection;
	private ResizeListener fResizeListener;
	private int fMinColumns=20;
	private int fMinLines=4;
	/**
	 * Create a new CellCanvas with the given SWT style bits.
	 * (SWT.H_SCROLL and SWT.V_SCROLL are automatically added).
	 */
	public TextCanvas(Composite parent, ITextCanvasModel model, int style,ILinelRenderer cellRenderer) {
		super(parent, style | SWT.H_SCROLL | SWT.V_SCROLL);
		fCellRenderer=cellRenderer;
		setCellWidth(fCellRenderer.getCellWidth());
		setCellHeight(fCellRenderer.getCellHeight());
		fCellCanvasModel=model;
		fCellCanvasModel.addCellCanvasModelListener(new ITextCanvasModelListener(){
			public void rangeChanged(int col, int line, int width, int height) {
				repaintRange(col,line,width,height);
			}
			public void dimensionsChanged(int cols, int rows) {
				setVirtualExtend(cols+getCellWidth(), rows+getCellHeight());
				calculateGrid();
			}
			public void terminalDataChanged() {
				if(isDisposed())
					return;
				scrollToEnd();
			}
		});
		addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent e) {
				fCellCanvasModel.setCursorEnabled(true);
			}
			public void focusLost(FocusEvent e) {
				fCellCanvasModel.setCursorEnabled(false);
			}});
		addMouseListener(new MouseListener(){
			public void mouseDoubleClick(MouseEvent e) {
			}
			public void mouseDown(MouseEvent e) {
				if(e.button==1) { // left button
					fDraggingStart=screenPointToCell(e.x, e.y);
					fHasSelection=false;
					if((e.stateMask&SWT.SHIFT)!=0) {
						Point anchor=fCellCanvasModel.getSelectionAnchor();
						if(anchor!=null)
							fDraggingStart=anchor;
					} else {
						fCellCanvasModel.setSelectionAnchor(fDraggingStart);
					}
					fDraggingEnd=null;
				}
			}
			public void mouseUp(MouseEvent e) {
				if(e.button==1) { // left button
					updateHasSelection(e);
					if(fHasSelection)
						setSelection(screenPointToCell(e.x, e.y));
					else
						fCellCanvasModel.setSelection(-1,-1,-1,-1);
					fDraggingStart=null;
				}
			}
		});
		addMouseMoveListener(new MouseMoveListener() {

			public void mouseMove(MouseEvent e) {
				if (fDraggingStart != null) {
					updateHasSelection(e);
					setSelection(screenPointToCell(e.x, e.y));
				}
			}
		});
		serVerticalBarVisible(true);
		setHorizontalBarVisible(false);
	}

	/**
	 * The user has to drag the mouse to at least one character to make a selection.
	 * Once this is done, even a one char selection is OK.
	 *
	 * @param e
	 */
	private void updateHasSelection(MouseEvent e) {
		if(fDraggingStart!=null) {
			Point p=screenPointToCell(e.x, e.y);
			if(fDraggingStart.x!=p.x||fDraggingStart.y!=p.y)
				fHasSelection=true;
		}
	}

	void setSelection(Point p) {
		if (fDraggingStart !=null && !p.equals(fDraggingEnd)) {
			fDraggingEnd = p;
			if (compare(p, fDraggingStart) < 0) {
				fCellCanvasModel.setSelection(p.y, fDraggingStart.y, p.x, fDraggingStart.x);
			} else {
				fCellCanvasModel.setSelection(fDraggingStart.y, p.y, fDraggingStart.x, p.x);

			}
		}
	}

	int compare(Point p1, Point p2) {
		if (p1.equals(p2))
			return 0;
		if (p1.y == p2.y) {
			if (p1.x > p2.x)
				return 1;
			else
				return -1;
		}
		if (p1.y > p2.y) {
			return 1;
		} else {
			return -1;
		}
	}
	public ILinelRenderer getCellRenderer() {
		return fCellRenderer;
	}

	public int getMinColumns() {
		return fMinColumns;
	}

	public void setMinColumns(int minColumns) {
		fMinColumns = minColumns;
	}

	public int getMinLines() {
		return fMinLines;
	}

	public void setMinLines(int minLines) {
		fMinLines = minLines;
	}

	protected void onResize() {
		if(fResizeListener!=null) {
			Rectangle bonds=getClientArea();
			int lines=bonds.height/getCellHeight();
			int columns=bonds.width/getCellWidth();
			// when the view is minimised, its size is set to 0
			// we don't sent this to the terminal!
			if(lines>0 && columns>0) {
				if(columns<fMinColumns) {
					if(!isHorizontalBarVisble()) {
						setHorizontalBarVisible(true);
						bonds=getClientArea();
						lines=bonds.height/getCellHeight();
					}
					columns=fMinColumns;
				} else if(columns>=fMinColumns && isHorizontalBarVisble()) {
					setHorizontalBarVisible(false);
					bonds=getClientArea();
					lines=bonds.height/getCellHeight();
					columns=bonds.width/getCellWidth();

				}
				if(lines<fMinLines)
					lines=fMinLines;
				fResizeListener.sizeChanged(lines, columns);
			}
		}
		super.onResize();
		calculateGrid();
	}

	private void calculateGrid() {
		setVirtualExtend(getCols()*getCellWidth(),getRows()*getCellHeight());
		// scroll to end
		scrollToEnd();
		// make sure the scroll area is correct:
		scrollY(getVerticalBar());
		scrollX(getHorizontalBar());

		getParent().layout();
		redraw();
	}
	void scrollToEnd() {
		if(!fScrollLock) {
			int y=-(getRows()*getCellHeight()-getClientArea().height);
			Rectangle v=getViewRectangle();
			if(v.y!=y) {
				setVirtualOrigin(v.x,y);
			}
		}
	}
	/**
	 *
	 * @return true if the cursor should be shown on output....
	 */
	public boolean isScrollLock() {
		return fScrollLock;
	}
	/**
	 * If set then if the size changes
	 * @param scrollLock
	 */
	public void setScrollLock(boolean scrollLock) {
		fScrollLock=scrollLock;
	}
	protected void repaintRange(int col, int line, int width, int height) {
		Point origin=cellToOriginOnScreen(col,line);
		Rectangle r=new Rectangle(origin.x,origin.y,width*getCellWidth(),height*getCellHeight());
		repaint(r);
	}
	protected void drawLine(GC gc, int line, int x, int y, int colFirst, int colLast) {
		fCellRenderer.drawLine(fCellCanvasModel, gc,line,x,y,colFirst, colLast);

	}
	protected void visibleCellRectangleChanged(int x, int y, int width, int height) {
		fCellCanvasModel.setVisibleRectangle(y,x,height,width);
		update();
	}
	protected int getCols() {
		return fCellCanvasModel.getTerminalText().getWidth();
	}
	protected int getRows() {
		return fCellCanvasModel.getTerminalText().getHeight();
	}
	public String getSelectionText() {
		// TODO -- create a hasSelectionMethod!
		return fCellCanvasModel.getSelectedText();
	}
	public void copy() {
		Clipboard clipboard = new Clipboard(getDisplay());
		clipboard.setContents(new Object[] { getSelectionText() }, new Transfer[] { TextTransfer.getInstance() });
		clipboard.dispose();
	}
	public void selectAll() {
		fCellCanvasModel.setSelection(0, fCellCanvasModel.getTerminalText().getHeight(), 0, fCellCanvasModel.getTerminalText().getWidth());
		fCellCanvasModel.setSelectionAnchor(new Point(0,0));
	}
	public boolean isEmpty() {
		return false;
	}
	/**
	 * Gets notified when the visible size of the terminal changes.
	 * This should update the model!
	 *
	 */
	public interface ResizeListener {
		void sizeChanged(int lines, int columns);
	}
	/**
	 * @param listener this listener gets notified, when the size of
	 * the widget changed. It should change the dimensions of the underlying
	 * terminaldata
	 */
	public void addResizeHandler(ResizeListener listener) {
		if(fResizeListener!=null)
			throw new IllegalArgumentException("There can be at most one listener at the moment!"); //$NON-NLS-1$
		fResizeListener=listener;
	}

	public void onFontChange() {
		fCellRenderer.onFontChange();
		setCellWidth(fCellRenderer.getCellWidth());
		setCellHeight(fCellRenderer.getCellHeight());
		calculateGrid();
	}

	public void setInvertedColors(boolean invert) {
		fCellRenderer.setInvertedColors(invert);
		redraw();
	}

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     * CUSTOMIZATION
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter (Class adapter) {
        if (adapter == ITextCanvasModel.class){
            return fCellCanvasModel;
        }
        return null;
    }

}

