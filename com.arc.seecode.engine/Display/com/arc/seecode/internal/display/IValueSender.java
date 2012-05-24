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

/**
 * @author David Pickens
 */
public interface IValueSender {
    /**
     * Send a message to the engine that sets a property value.
     * The action of setting the value may trigger the engine to
     * take an action.
     * @param property the property value in the engine.
     * @param value the value to be assigned.
     */
    public void sendValueUpdate(String property, String value);

    /**
     * Like {@link #sendValueUpdate(String,String)} but has
     * a timeout value, in milliseconds, if the engine doesn't
     * respond.
     * @param property the property value in the engine.
     * @param value the value to be assigned.
     * @param timeout timeout value in milliseconds.
     * @return true if successful; false if timeout occurred.
     */
    public boolean sendValueUpdate(String property, String value, int timeout);

    /**
     * Like {@link #sendValueUpdate(String,String)} but has the
     * option to save this operation so that it can be restored between
     * sessions.
     * @param property the property value in the engine.
     * @param value the value to be assigned.
     * @param record if true, record value so as to be restored
     * between sessions.
     */
    public void sendValueUpdate(String property, String value, boolean record);
}
