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
package com.metaware.guihili;

import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.Action;

/**
 * An interface for setting action listeners.
 *
 * @author David Pickens
 * @version May 20, 2002
 */
public interface IActionMap {
    /**
     * Add a listener to be invoked when a named action is fired.
     * @param name the name of the action to listen for.
     * @param l the listener.
     * @return composite action that will be fired.
     */
    public Action addAction(String name, ActionListener l);
    /**
     * Return action with a particular name.
     * @param name the name of the action we're requesting.
     * @return the action associated with the name, or null.
     */
    public Action getAction(String name);

    /**
     * Remove a listener that was previously added.
     * @param name the name of the action to listen for.
     * @param l the listener.
     */
    public void removeAction(String name, ActionListener l);

    /**
     * return collection of all action names.
     * Each element of the returned collection is a string.
     */
    public Collection<String> getActionNames();
    }
