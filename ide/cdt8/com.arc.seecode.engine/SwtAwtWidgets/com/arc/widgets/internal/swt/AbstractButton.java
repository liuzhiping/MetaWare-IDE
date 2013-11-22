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

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;

import com.arc.widgets.IButton;
import com.arc.widgets.IColor;
import com.arc.widgets.IContainer;
import com.arc.widgets.IImage;

/**
 * Common base class for buttons and menu items.
 * 
 * @author David Pickens
 */
abstract class AbstractButton extends Component implements IButton {
    private Image mColorImage = null;

    private List<IObserver> mObservers = null;

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IComponent#setBackground(com.arc.widgets.IColor)
     */
    @Override
    public void setBackground(IColor color) {
        // Setting the background color for a button
        // doesn't work (at least under windows).
        // Thus, we create a square image and fill it with
        // the color.
        if (mImage != null)
            super.setBackground(color);
        else {
            boolean firstTime = true;
            if (mColorImage != null) {
                mColorImage.dispose();
                firstTime = false;
            }
            Control c = (Control) getComponent();
            int WIDTH = 16;
            int HEIGHT = 20;
            Image image = new Image(c.getDisplay(), WIDTH, HEIGHT);
            GC gc = new GC(image);
            gc.setForeground((Color) color.getObject());
            gc.setBackground((Color) color.getObject());
            gc.fillRectangle(0, 0, WIDTH, HEIGHT);
            gc.dispose();
            mColorImage = image;
            applyImage(image);
            if (firstTime) {
                c.addDisposeListener(new DisposeListener() {

                    @Override
                    public void widgetDisposed(DisposeEvent e) {
                        if (mColorImage != null) {
                            mColorImage.dispose();
                            mColorImage = null;
                        }

                    }
                });
            }
        }
    }

    /**
     * @param parent
     * @param mapper
     */
    public AbstractButton(IContainer parent, IComponentMapper mapper, int style) {
        super(parent, mapper);
        mStyle = style;
    }

    private SelectionActionListener mActionListener;

    @Override
    public int getButtonKind() {
        switch (mStyle) {
            case SWT.PUSH:
                return PUSH;
            case SWT.CHECK:
                return CHECKBOX;
            case SWT.RADIO:
                return RADIO;
            default:
                return PUSH; // ??
        }
    }

    @Override
    public void setMnemonic(char c) {
    }

    protected void fireAction() {
        if (mActionListener != null) {
            Event e = new Event();
            e.widget = (Widget) getComponent();
            SelectionEvent se = new SelectionEvent(e);
            se.widget = e.widget;
            mActionListener.widgetSelected(se);
        }
    }

    /**
     * Add action listener for PUSH-type button. <b>Note: </b> for the SWT
     * implementation, we contrive an ActionEvent from the underlying
     * <code>org.eclipse.swt.events.SelectionEvent</code>.
     */
    @Override
    public void addActionListener(ActionListener listener) {
        if (mActionListener == null) {
            mActionListener = new SelectionActionListener(false,this);
            Widget w = (Widget) getComponent();
            if (w instanceof Button)
                ((Button) w).addSelectionListener(mActionListener);
            else
                ((MenuItem) w).addSelectionListener(mActionListener);
        }
        mActionListener.addActionListener(listener);
    }

    @Override
    public void removeActionListener(ActionListener listener) {
        if (mActionListener != null)
            mActionListener.removeActionListener(listener);
    }

    protected int mStyle;

    protected String mText;

    protected IImage mImage;

    @Override
    public String getText() {
        //    	if (mComponent != null)
        //    		return ((Button) mComponent).getText();
        return mText;
    }

    @Override
    public IImage getImage() {
        return mImage;
    }

    protected boolean mSelected;

    @Override
    public boolean isSelected() {
        if (mComponent != null) {
            if (mComponent instanceof Button) {
                return ((Button) mComponent).getSelection();
            } else if (mComponent instanceof MenuItem) {
                return ((MenuItem) mComponent).getSelection();
            }
        }
        return mSelected;
    }

    @Override
    public void setSelected(boolean v) {
        mSelected = v;
        if (mComponent != null) {
            if (isSelected() != v) {
                if (mComponent instanceof Button)
                    ((Button) mComponent).setSelection(v);
                else if (mComponent instanceof MenuItem) {
                    ((MenuItem) mComponent).setSelection(v);
                }
                // Programmatically setting button does
                // not fire selection event!
                fireAction();
                notifyObservers();
            }
        }
    }

    @Override
    public void setText(String txt) {
        mText = txt;
        if (mComponent != null) {
            if (mComponent instanceof Button) {
                ((Button) mComponent).setText(txt);
            } else if (mComponent instanceof MenuItem) {
                ((MenuItem) mComponent).setText(txt);
            }
        }
    }

    @Override
    public void setImage(IImage image) {

        mImage = image;
        if (mComponent != null) {
            Image i = (Image) image.getObject();
            applyImage(i);
        }
        if (mColorImage != null) {
            mColorImage.dispose();
            mColorImage = null;
        }
    }

    /**
     * @param i
     */
    private void applyImage(Image i) {
        if (mComponent instanceof Button) {
            ((Button) mComponent).setImage(i);
        } else if (mComponent instanceof MenuItem) {
            ((MenuItem) mComponent).setImage(i);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IToolItem#addObserver(com.arc.widgets.IToolItem.IObserver)
     */
    @Override
    public synchronized void addObserver(IObserver observer) {
        if (mObservers == null) {
            mObservers = new ArrayList<IObserver>();
            Widget w = (Widget) getComponent();
            SelectionListener l = new SelectionListener() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    notifyObservers();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    widgetSelected(e);

                }
            };
            if (w instanceof Button){
                ((Button)w).addSelectionListener(l);
            }
            else {
                ((MenuItem)w).addSelectionListener(l);
            }
        }
        mObservers.add(observer);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.IToolItem#removeObserver(com.arc.widgets.IToolItem.IObserver)
     */
    @Override
    public synchronized void removeObserver(IObserver observer) {
        if (mObservers != null)
            mObservers.remove(observer);
    }

    private void notifyObservers() {
        IObserver observers[];
        synchronized (this) {
            if (mObservers == null) return;
            observers = mObservers.toArray(new IObserver[mObservers.size()]);
        }
        for (IObserver o : observers) {
            o.itemChanged(this);
        }
    }
    
    @Override
    public int getStyle(){
        return getButtonKind();
    }

}
