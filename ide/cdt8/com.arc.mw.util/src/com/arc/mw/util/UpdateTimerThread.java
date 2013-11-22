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
package com.arc.mw.util;

/**
 * A thread that fires a timer at regular intervals.
 *
 *@author     David Pickens
 *@created    March 12,2002
 */
class UpdateTimerThread extends Thread {

    UpdateTimerThread(ITimedUpdate update, int millis) {
        super("UpdateTimer");
        _target = update;
        setInterval(millis);
        setDaemon(true);
    }

    private ITimedUpdate _target;

    private int _interval;

    void setInterval(int millis) {
        _interval = millis;
    }

    public void kill() {
        _target = null;
        _interval = 0;
        interrupt();
    }

    @Override
    public void run() {
        long timeAtEndOfLastSleep = System.currentTimeMillis();
        while (_target != null && _interval != 0) {
            long elapsed = System.currentTimeMillis() - timeAtEndOfLastSleep;
            try {
                if (_interval > elapsed)
                    Thread.sleep(_interval - elapsed);
            } catch (InterruptedException x) {
            }
            if (_interval == 0 || _target == null) {
                return;
            }
            timeAtEndOfLastSleep = System.currentTimeMillis();
            _target.timedUpdate();
        }
    }
}
