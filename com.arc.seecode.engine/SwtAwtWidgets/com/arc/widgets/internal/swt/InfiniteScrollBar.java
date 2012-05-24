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
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;

import com.arc.mw.util.ITimedUpdate;
import com.arc.mw.util.UpdateTimer;
import com.arc.widgets.IScrollBar;

/**
 * A vertical scrollbar that is being applied to something that is infinite in
 * length. There is no positional thumb, just scroll up/scroll down buttons.
 * <P>
 * NOTE: this class was made public so that the GUI tester can access it.
 * 
 * @author David Pickens
 */
public class InfiniteScrollBar extends Composite implements IScrollBar, ITimedUpdate,
        MouseListener {
    private static final int MINIMUM_REPEAT_INTERVAL = 40; // 25 per second

    private static final int INITIAL_REPEAT_DELAY = 500; // half second

    private static final int STARTING_REPEAT_INTERVAL = 200; //3 per second

    private int mIncrAmount = 1;

    private int mPageAmount = 2; // arbitrary default

    private int mValue = 0;

    private int mPortSize;

    private List<IObserver> mObservers = new ArrayList<IObserver>();

    private UpdateTimer mRepeatTimer = null;

    private Button mActiveButton = null;

    private transient boolean mRepeatPending = false;
    
    private static Image sUpArrow = null;
    private static Image sUpUpArrow = null;
    private static Image sDownArrow = null;
    private static Image sDownDownArrow = null;
    
    /**
     * We maintain the number of instance of
     * this class. When it goes to zero, we
     * dispose of the images.
     */
    private static int sInstanceCount = 0;
    private static void allocImages(Display d){
        if (sUpArrow != null)
            throw new Error("image already created!");
        sUpArrow = allocImage(d,"xpup");
        sUpUpArrow = allocImage(d,"xpupup");
        sDownArrow = allocImage(d,"xpdown");
        sDownDownArrow = allocImage(d,"xpdowndown");
    }
    
    private static void freeImages(){
        sUpArrow.dispose();
        sUpArrow = null;
        
        sUpUpArrow.dispose();
        sUpUpArrow = null;     
        
        sDownArrow.dispose();
        sDownArrow = null;
             
        sDownDownArrow.dispose();
        sDownDownArrow = null;
    }
    
    private static Image allocImage(Display d,String name){
        return new Image(d,InfiniteScrollBar.class.getResourceAsStream(name + ".gif"));
    }

    /**
     * @param parent
     */
    public InfiniteScrollBar(Composite parent) {
        super(parent, 0);
        setLayout(new MyLayout());
        synchronized(this.getClass()){
            if (sInstanceCount == 0)
                allocImages(getDisplay());
            sInstanceCount++;
        }
        addDisposeListener(new DisposeListener(){

            @Override
            public void widgetDisposed(DisposeEvent e) {
                synchronized(InfiniteScrollBar.this.getClass()){
                sInstanceCount--;
                if (sInstanceCount == 0)
                    freeImages();
                }               
            }});
        
        Button incrUp = new Button(this, SWT.PUSH);
        incrUp.setImage(sUpArrow);
        incrUp.setLayoutData(MyLayout.UP);
        incrUp.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                onUpEvent();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });

        Button pageUp = new Button(this, SWT.PUSH);
        pageUp.setImage(sUpUpArrow);
        pageUp.setLayoutData(MyLayout.PAGE_UP);
        pageUp.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                onPageUpEvent();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });

        Button incrDown = new Button(this, SWT.PUSH|SWT.ARROW|SWT.DOWN|SWT.FLAT);
        incrDown.setImage(sDownArrow);
        incrDown.setLayoutData(MyLayout.DOWN);
        incrDown.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                onDownEvent();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });

        Button pageDown = new Button(this, SWT.PUSH);
        pageDown.setImage(sDownDownArrow);
        pageDown.setLayoutData(MyLayout.PAGE_DOWN);
        pageDown.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                onPageDownEvent();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });

        incrUp.addMouseListener(this);
        pageUp.addMouseListener(this);
        incrDown.addMouseListener(this);
        pageDown.addMouseListener(this);

        mRepeatTimer = new UpdateTimer(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IScrollBar#getValue()
     */
    @Override
    public int getValue() {
        return mValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IScrollBar#setValue(int)
     */
    @Override
    public void setValue(int value) {
        if (value != mValue) {
            mValue = value;
            notifyObservers();
        }
    }

    @Override
    public void setCurrentValue(int value) {
        mValue = value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IScrollBar#getMinimum()
     */
    @Override
    public int getMinimum() {
        return Integer.MIN_VALUE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IScrollBar#getMaximum()
     */
    @Override
    public int getMaximum() {
        return Integer.MAX_VALUE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IScrollBar#getIncrementAmount()
     */
    @Override
    public int getIncrementAmount() {
        return mIncrAmount;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IScrollBar#getPageAmount()
     */
    @Override
    public int getPageAmount() {
        return mPageAmount;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IScrollBar#setMinimum(int)
     */
    @Override
    public void setMinimum(int v) {
        //        if (v != getMinimum())
        //            throw new IllegalArgumentException("minimum cannot be set");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IScrollBar#setMaximum(int)
     */
    @Override
    public void setMaximum(int v) {
        //        if (v != getMaximum())
        //            throw new IllegalArgumentException("maximum cannot be set");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IScrollBar#setIncrementAmount(int)
     */
    @Override
    public void setIncrementAmount(int amount) {
        mIncrAmount = amount;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IScrollBar#setPageAmount(int)
     */
    @Override
    public void setPageAmount(int amount) {
        mPageAmount = amount;

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IScrollBar#getPortSize()
     */
    @Override
    public int getPortSize() {
        return mPortSize;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IScrollBar#setPortSize(int)
     */
    @Override
    public void setPortSize(int size) {
        mPortSize = size;

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IScrollBar#addObserver(com.arc.widgets.IScrollBar.IObserver)
     */
    @Override
    public void addObserver(IObserver observer) {
        synchronized (mObservers) {
            mObservers.add(observer);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IScrollBar#removeObserver(com.arc.widgets.IScrollBar.IObserver)
     */
    @Override
    public void removeObserver(IObserver observer) {
        synchronized (mObservers) {
            mObservers.remove(observer);
        }
    }

    private void notifyObservers() {
        IObserver[] observers;
        synchronized (mObservers) {
            if (mObservers.size() == 0)
                return;
            observers = mObservers.toArray(new IObserver[mObservers.size()]);
        }
        for (IObserver o : observers) {
            o.scrollBarChanged(this);
        }
    }

    static class MyLayout extends Layout {
        // enum Position { UP, PAGE_UP, DOWN, PAGE_DOWN };
        final static String UP = "up";

        final static String PAGE_UP = "page_up";

        final static String DOWN = "down";

        final static String PAGE_DOWN = "page_down";

        @Override
        protected Point computeSize(Composite composite, int wHint, int hHint,
                boolean flushCache) {
            int width = 0;
            int height = 0;
            Control kids[] = composite.getChildren();
            for (int i = 0; i < kids.length; i++) {
                Point size = kids[i].computeSize(wHint, SWT.DEFAULT);
                width = Math.max(width, size.x);
                height += size.y;
            }
            return new Point(width, height);
        }

        private Control find(Control[] kids, String p) {
            for (Control k : kids) {
                if (k.getLayoutData() == p)
                    return k;
            }
            throw new IllegalArgumentException("can't find " + p);
        }

        @Override
        protected void layout(Composite composite, boolean flushCache) {
            Control kids[] = composite.getChildren();
            Point size = composite.getSize();
            Control up = find(kids, UP);
            Point upSize = up.computeSize(size.x, SWT.DEFAULT);

            Control pageUp = find(kids, PAGE_UP);
            Point pageUpSize = pageUp.computeSize(size.x, SWT.DEFAULT);

            Control down = find(kids, DOWN);
            Point downSize = down.computeSize(size.x, SWT.DEFAULT);

            Control pageDown = find(kids, PAGE_DOWN);
            Point pageDownSize = pageDown.computeSize(size.x, SWT.DEFAULT);

            up.setBounds(0, 0, size.x, upSize.y);

            pageUp.setBounds(0, upSize.y, size.x, pageUpSize.y);

            down.setBounds(0, size.y - downSize.y, size.x, downSize.y);

            pageDown.setBounds(0, size.y - downSize.y - pageDownSize.y, size.x,
                    pageDownSize.y);
        }

    }

    private void onUpEvent() {
        setValue(getValue() - mIncrAmount);
    }

    private void onDownEvent() {
        setValue(getValue() + mIncrAmount);
    }

    private void onPageUpEvent() {
        setValue(getValue() - mPageAmount);
    }

    private void onPageDownEvent() {
        setValue(getValue() + mPageAmount);
    }

    /**
     * Called when a button is helddown to cause it to repeat.
     *  
     */
    @Override
    public void timedUpdate() {
        if (mActiveButton != null && !mRepeatPending) {
            mRepeatPending = true;
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        String data = (String) mActiveButton.getLayoutData();
                        if (data == MyLayout.UP) {
                            onUpEvent();
                        } else if (data == MyLayout.DOWN) {
                            onDownEvent();
                        } else if (data == MyLayout.PAGE_UP) {
                            onPageUpEvent();
                        } else if (data == MyLayout.PAGE_DOWN) {
                            onPageDownEvent();
                        }
                    } finally {
                        mRepeatPending = false;
                    }
                }
            };
            getDisplay().asyncExec(run);
            int i = mRepeatTimer.getTimerInterval();
            if (i == INITIAL_REPEAT_DELAY)
                mRepeatTimer.setTimerInterval(STARTING_REPEAT_INTERVAL);
            else if (i > MINIMUM_REPEAT_INTERVAL) {
                mRepeatTimer.setTimerInterval(i - 10);
            }
        }
        else if (mRepeatPending){
            // We're going a little too fast; slow down a bit.
            int i = mRepeatTimer.getTimerInterval();
            if (i < STARTING_REPEAT_INTERVAL)
                mRepeatTimer.setTimerInterval(i+10);
            
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
     */
    @Override
    public void mouseDoubleClick(MouseEvent e) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
     */
    @Override
    public void mouseDown(MouseEvent e) {
        mActiveButton = (Button) e.getSource();
        mRepeatTimer.setTimerInterval(INITIAL_REPEAT_DELAY);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
     */
    @Override
    public void mouseUp(MouseEvent e) {
        mRepeatTimer.killTimer();

    }

}
