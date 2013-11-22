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
package com.arc.widgets.internal.swing;

import java.awt.Container;
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;

import com.arc.widgets.IButton;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;
import com.arc.widgets.IImage;
import com.arc.widgets.IWindow;

class Dialog extends JDialog implements IWindow, WindowListener {
    private static final long serialVersionUID = 1L;
    private List<IObserver> mObservers = new ArrayList<IObserver>();
    Dialog(Frame f, boolean modal, IComponentFactory fact) {
        super(f, modal);
        mFactory = fact;
        init();
    }

    Dialog(java.awt.Dialog d, boolean modal) {
        super(d, modal);
        init();
    }
    
    private void init(){
        this.addWindowListener(this);
    }

    @Override
    public void open() {
        setVisible(true);
    }
    
    @Override
    public void setDefaultButton(IButton button){
        this.getRootPane().setDefaultButton((JButton)button.getComponent());
    }

    @Override
    public void setPosition(int x, int y) {
        setLocation(x, y);
    }

    @Override
    public Object getComponent() {
        return this;
    }

    @Override
    public IContainer getContents() {
        return mFactory.wrapContainer(this.getContentPane(),
                IComponentFactory.GRID_STYLE);
    }

    @Override
    public void setContents(IContainer container) {
        this.setContentPane((Container) container.getComponent());
    }
    
    private IComponentFactory mFactory;
    private boolean mDisposed;

    /* (non-Javadoc)
     * @see com.arc.widgets.IWindow#addWindowObserver(com.arc.widgets.IWindow.IObserver)
     */
    @Override
    public  void addWindowObserver(IObserver l) {
        synchronized(mObservers){
            mObservers.add(l);
        }
        
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IWindow#removeWindowObserver(com.arc.widgets.IWindow.IObserver)
     */
    @Override
    public void removeWindowObserver(IObserver l) {
        synchronized(mObservers){
            mObservers.remove(l);
        }
        
    }
    
    private void fireEvent (int id) {
        IObserver[] observers;
        synchronized (mObservers) {
            int cnt = mObservers.size();
            if (cnt == 0)
                return;
            observers = mObservers.toArray(new IObserver[cnt]);
        }
        for (int i = 0; i < observers.length; i++) {
            IObserver o = observers[i];
            switch (id) {
                case WindowEvent.WINDOW_CLOSING:
                    o.windowClosing(this);
                    break;
                case WindowEvent.WINDOW_CLOSED:
                    o.windowClosed(this);
                    break;
                case WindowEvent.WINDOW_ICONIFIED:
                    o.windowIconified(this);
                    break;
                case WindowEvent.WINDOW_DEICONIFIED:
                    o.windowDeiconified(this);
                    break;
                case WindowEvent.WINDOW_ACTIVATED:
                    o.windowActivated(this);
                    break;
                case WindowEvent.WINDOW_DEACTIVATED:
                    o.windowDeactivated(this);
                    break;
                default:
                    break;
            }
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
     */
    @Override
    public void windowActivated(WindowEvent e) {
        fireEvent(e.getID());       
    }

    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
     */
    @Override
    public void windowClosed(WindowEvent e) {
        mDisposed = true;
        fireEvent(e.getID()); 
    }

    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
     */
    @Override
    public void windowClosing(WindowEvent e) {
        if (mObservers == null || mObservers.size() == 0)
            dispose();
        else
            fireEvent(e.getID()); 
    }

    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
     */
    @Override
    public void windowDeactivated(WindowEvent e) {
        fireEvent(e.getID()); 
    }

    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
     */
    @Override
    public void windowDeiconified(WindowEvent e) {
        fireEvent(e.getID()); 
    }

    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
     */
    @Override
    public void windowIconified(WindowEvent e) {
        fireEvent(e.getID()); 
    }

    /* (non-Javadoc)
     * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
     */
    @Override
    public void windowOpened(WindowEvent e) {
        fireEvent(e.getID()); 
        
    }

    /* (non-Javadoc)
     * @see com.arc.widgets.IWindow#isDisposed()
     */
    @Override
    public boolean isDisposed() {
        return mDisposed;
    }
    
    @Override
    public void setImage (IImage image) {
        //this.setIconImage((Image)image.getObject());
        
    }
}
