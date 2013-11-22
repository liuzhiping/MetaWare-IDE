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
 * Support code for windows that need to respond to update timer.
 * @author David Pickens
 * @created March 12, 2002
 */
public class UpdateTimer {

    private int _interval = Integer.MAX_VALUE;

    /**
     * Set up a timer that will call update.timedUpdate()
     */
    public UpdateTimer(ITimedUpdate update) {
        _update = update;
    }

    /**
     * Arrange for update timer to fire every "cnt" milliseconds.
     * @param millis the interval in milliseconds
     */
    public void setTimerInterval (int millis) {
        if (millis != _interval) {
            _interval = millis;
            if (millis != 0) {
                if (_timer != null)
                    _timer.setInterval(millis);
                else {
                    _timer = new UpdateTimerThread(_update, millis);
                    _timer.start();
                }
            }
            else {
                if (_timer != null) {
                    _timer.kill();
                    _timer = null;
                }
            }
        }
    }

    /**
     * Return the current interval time in milliseconds.
     * @return the current interval time in milliseconds.
     */
    public int getTimerInterval () {
        return _interval;
    }

    /**
     * Stop the timer.
     */
    public void killTimer () {
        if (_timer != null) {
            _timer.kill();
            _timer = null;
            _interval = Integer.MAX_VALUE;
        }
    }

    private UpdateTimerThread _timer;

    private ITimedUpdate _update;

}
