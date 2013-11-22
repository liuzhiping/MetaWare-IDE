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
package com.metaware.guihili.builder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * A composite of action listeners so that we can have many under one name.
 */
public class CompositeAction extends AbstractAction implements
        PropertyChangeListener {
    public CompositeAction(String name) {
        // mName = name;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        // System.out.println("Action " + mName + " fired");
        int cnt = _list.size();
        for (int i = 0; i < cnt; i++)
            ((ActionListener) _list.get(i)).actionPerformed(event);
    }

    public void add(ActionListener action) {
        addListener(action);
        _list.add(action);
        checkEnabled();
    }

    public void prepend(ActionListener action) {
        addListener(action);
        _list.add(0, action);
        checkEnabled();
    }

    public void remove(ActionListener action) {
        removeListener(action);
        _list.remove(action);
        checkEnabled();
    }

    private void addListener(ActionListener a) {
        if (a instanceof Action) {
            Action action = (Action) a;
            action.addPropertyChangeListener(this);
        }
    }

    private void removeListener(ActionListener a) {
        if (a instanceof Action) {
            Action action = (Action) a;
            action.removePropertyChangeListener(this);
        }
    }

    /**
     * Called when "enabled" property changes. Disable it if all constitiuents
     * are disabled.
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName().equals("enabled"))
            checkEnabled();
    }

    private void checkEnabled() {
        boolean isEnabled = false;
        int cnt = _list.size();
        for (int i = 0; i < cnt; i++) {
            ActionListener a = (ActionListener) _list.get(i);
            if (a instanceof Action) {
                if (((Action) a).isEnabled()) {
                    isEnabled = true;
                    break;
                }
            } else {
                isEnabled = true;
                break;
            }
        }
        setEnabled(isEnabled);
    }

    private ArrayList<Object> _list = new ArrayList<Object>(2);
    // private String mName;
}
