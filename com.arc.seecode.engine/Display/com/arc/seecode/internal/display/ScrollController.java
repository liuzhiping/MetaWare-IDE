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

import com.arc.widgets.IScrollBar;
import com.arc.widgets.IScrollPane;

/**
 * Handles keyboard and mouse events on behalf of a text viewer.
 * 
 * @author David Pickens
 */
class ScrollController {
    private ITextScrollObserver mViewer;

    private IScrollBar mVertical = null;

    private IScrollBar mHorizontal = null;

	private long mTimeOfLastHorizVisibilityChange = 0;

    /**
     * 
     * @param scrollObserver
     *            the viewer that is the source of the mouse and keyboard
     *            events.
     */
    ScrollController(ITextScrollObserver scrollObserver, IScrollPane scroller) {
        mViewer = scrollObserver;

        IScrollBar v = scroller.getVerticalScrollBar();
        IScrollBar h = scroller.getHorizontalScrollBar();
        v.addObserver(new IScrollBar.IObserver() {

            @Override
            public void scrollBarChanged(IScrollBar sb) {
                onVerticalScroll(sb);

            }
        });
        h.addObserver(new IScrollBar.IObserver() {

            @Override
            public void scrollBarChanged(IScrollBar sb) {
                onHorizontalScroll(sb);

            }
        });
        mVertical = v;
        mHorizontal = h;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IScrollBar.IObserver#scrollBarChanged(com.arc.widgets.IScrollBar)
     */
    private void onVerticalScroll(IScrollBar sb) {
        mViewer.onScrollToLine(sb.getValue());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IScrollBar.IObserver#scrollBarChanged(com.arc.widgets.IScrollBar)
     */
    private void onHorizontalScroll(IScrollBar sb) {
        mViewer.onScrollToColumn(sb.getValue());
    }

    public void setVerticalScroller(int value, int portSize, int min, int max,
            int blockIncr) {
        setScrollBarAttributes(mVertical, min, max, value, portSize, blockIncr,true);
    }
    
    public int getHorizontalScrollPosition(){
        return mHorizontal.getValue();
    }

    public void setHorizontalScroller(int value, int portSize, int min, int max,
            int incr) {
    	long thisTime = System.currentTimeMillis();
    	boolean permitInvisibility = thisTime - mTimeOfLastHorizVisibilityChange > 1000;
        if (setScrollBarAttributes(mHorizontal, min, max, value, portSize, incr,
        		permitInvisibility)) {
        	mTimeOfLastHorizVisibilityChange = thisTime;
        }
    }

    /**
     * @param min
     * @param max
     * @param value
     * @param portSize
     * @param permitInvisibility
     * @return whether or not the scrollbar visibility changed.
     */
    private static boolean setScrollBarAttributes(IScrollBar sb, int min, int max,
            int value, int portSize, int blockIncr, boolean permitInvisibility) {
        // By making a scrollbar invisible, we may causing one
        // additional row (or column) to be show, which could trigger
        // the need for the scrollbar. This can cause a loop. Therefore,
        // we make sure a scroll operation from other scroller hasn't happened recently before change
        // things.
    	boolean prevVisibility = sb.isVisible();
        if (max - min + 1 <= portSize && (!sb.isVisible() || permitInvisibility)) {
             sb.setVisible(false);      
        } else {       
            sb.setVisible(true);
            sb.setMinimum(min);
            sb.setMaximum(max+1); // must be 1 beyond maximum
            sb.setCurrentValue(value);
            sb.setPortSize(portSize);
        }
        sb.setPageAmount(blockIncr);
        return sb.isVisible() != prevVisibility;
    }

}
