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
package com.arc.cdt.debug.seecode.internal.core.cdi;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;

/**
 * The event manager. All engine events are propagated by calling
 * {@link #enqueueEvent(ICDIEvent)}.
 * <P>
 * The logic above the CDI layer (particularly the breakpoint manager) requires
 * that events be serviced in a separate thread than the originator of those
 * events. (Otherwise, issues that are solved with locks don't work.)
 * <P>
 * Also, multiple events can be serviced at once. So we queue them up.
 * <P>
 * We make it public because "value" package fires
 * array element changes.
 * 
 * @author David Pickens
 */
public class EventManager implements ICDIEventManager, Runnable {

    private List<ICDIEventListener> mListeners = new ArrayList<ICDIEventListener>();

    private Thread mThread;

    private boolean mTerminated;

    private List<ICDIEvent> mQueue = new ArrayList<ICDIEvent>();

    private ICDISession fSession;

    /**
     * @param session
     */
    public EventManager(ICDISession session) {
        fSession = session;
        mThread = new Thread(this, "SeeCodeEventManager");
        mThread.setDaemon(true);
        mThread.start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDIEventManager#addEventListener(org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener)
     */
    @Override
    public void addEventListener(ICDIEventListener listener) {
        synchronized (mListeners) {
            mListeners.add(listener);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDIEventManager#removeEventListener(org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener)
     */
    @Override
    public void removeEventListener(ICDIEventListener listener) {
        synchronized (mListeners) {
            mListeners.remove(listener);
        }
    }

    public void shutdown () {
        synchronized (mQueue) {
            mTerminated = true;
        }

        if (mThread != null && mThread != Thread.currentThread()) {           
            synchronized (this) {
                try {
                    // Wait for thread to exit
                    // in case the caller is
                    // from the plugin's "stop()"
                    // method.
                    if (mThread != null) {
                       mThread.interrupt();
                       wait(5000);
                    }
                }
                catch (InterruptedException e) {
                }
            }
        }
    }

    @Override
    public void run() {
        if (mThread != Thread.currentThread()) return;
        try {
            while (true) {
                try {
                    ICDIEvent events[];
                    synchronized (mQueue) {
                        while (mQueue.isEmpty()) {
                            if (!mTerminated)
                                mQueue.wait();
                            else 
                                return;
                        }
                        events =  mQueue.toArray(new ICDIEvent[mQueue
                                .size()]);
                        mQueue.clear();
                    }
                    fireEvents(events);
                } catch (InterruptedException e) {
                    //Presumably being shutdown.
                }
            }
        } finally {
            synchronized(this){
                mThread = null;
                //Let listeners know we terminated
                notifyAll();
            }
        }

    }

    /**
     * This method is mainly called by the SeeCode engine observer.
     * 
     * @param e
     *            the event.
     */
    public void enqueueEvent(ICDIEvent e) {
        if (e == null)
                throw new IllegalArgumentException("Event can't be null");
        synchronized (mQueue) {
            // Don't add anything if we've shutdown.
            if (!mTerminated) {
                mQueue.add(e);
                mQueue.notifyAll();
            }
        }
    }

    /**
     * This method is called when several events are occurring simultaneously;
     * e.g., register changes.
     * 
     * @param e
     *            the event.
     */
    public void enqueueEvents(ICDIEvent[] e) {
        if (e == null)
                throw new IllegalArgumentException("Event can't be null");
        synchronized (mQueue) {
            if (!mTerminated) {
                for (int i = 0; i < e.length; i++) {
                    mQueue.add(e[i]);
                }
                mQueue.notifyAll();
            }
        }
    }

    /**
     * Called from the event thread to fire the actual events.
     * 
     * @param e
     */
    private void fireEvents(ICDIEvent[] e) {
        ICDIEventListener listeners[];
        synchronized (mListeners) {
            int cnt = mListeners.size();
            if (cnt == 0) return;
            listeners =  mListeners
                    .toArray(new ICDIEventListener[mListeners.size()]);
        }
        for (int i = 0; i < listeners.length; i++) {
            try {
                listeners[i].handleDebugEvents(e);
            }
            catch(Throwable t){
                SeeCodePlugin.log(t);
            }
        }
    }

    @Override
    public ICDISession getSession () {
        return fSession;
    }
}
