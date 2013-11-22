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

import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;

import com.arc.widgets.IWindow;

/**
 * @author pickens
 *
 * A wrapper to convert a Shell event to a Window Event
 * 
 */
class WindowChangeListener implements ShellListener {

    private IWindow mWindow;
    private static final int ACTIVATED = 1;
    private static final int DEACTIVATED = 2;
    private static final int ICONIFIED = 3;
    private static final int DEICONIFIED = 4;
    private static final int CLOSING = 5;
    private static final int CLOSED = 6;
    WindowChangeListener(IWindow window){
        mWindow = window;
    }
    /**
     * @see org.eclipse.swt.events.ShellListener#shellActivated(ShellEvent)
     */
    @Override
    public void shellActivated(ShellEvent e) {
        doit(ACTIVATED,e);
    }

    public void add(IWindow.IObserver l){
	synchronized(mListeners){
	    mListeners.add(l);
	    }
	}
    public void remove(IWindow.IObserver l){
	synchronized(mListeners){
	    mListeners.remove(l);
	    }
	}

    /**
     * @see org.eclipse.swt.events.ShellListener#shellClosed(ShellEvent)
     */
    @Override
    public void shellClosed(ShellEvent e) {
        if (mWindow.isDisposed())
            doit(CLOSED,e);
        else {
            e.doit = false;
            doit(CLOSING,e);
        }
    }

    /**
     * @see org.eclipse.swt.events.ShellListener#shellDeactivated(ShellEvent)
     */
    @Override
    public void shellDeactivated(ShellEvent e) {
        doit(DEACTIVATED,e);
    }

    /**
     * @see org.eclipse.swt.events.ShellListener#shellDeiconified(ShellEvent)
     */
    @Override
    public void shellDeiconified(ShellEvent e) {
        doit(DEICONIFIED,e);
    }

    /**
     * @see org.eclipse.swt.events.ShellListener#shellIconified(ShellEvent)
     */
    @Override
    public void shellIconified(ShellEvent e) {
        doit(ICONIFIED,e);
    }
    private void doit(int event, ShellEvent e){
        IWindow.IObserver[] listeners = null;
        int cnt = 0;
        synchronized(mListeners){
            cnt = mListeners.size();
            if (cnt == 0) return;
            listeners = mListeners.toArray(new IWindow.IObserver[cnt]);
        }
        for (int i = 0; i < cnt; i++){
            IWindow.IObserver w = listeners[i];
            switch(event){
                case ICONIFIED: w.windowIconified(mWindow); break;
                case DEICONIFIED: w.windowDeiconified(mWindow); break;
                case ACTIVATED: w.windowActivated(mWindow); break;
                case DEACTIVATED: w.windowDeactivated(mWindow); break;
                case CLOSED: w.windowClosed(mWindow); break;
                case CLOSING: w.windowClosing(mWindow); break;
		default: throw new IllegalArgumentException("Bad window event id: " + e);
            }
        }
    }
    private List<IWindow.IObserver>mListeners = new ArrayList<IWindow.IObserver>();

}
