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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import com.arc.widgets.IComponent;

/**
 * A SelectionListener implementation that wraps an action listener.
 */
class SelectionActionListener implements SelectionListener {
    private List<ActionListener> mListeners;
    private boolean mDefaultOnly;
    private IComponent mSource;
    SelectionActionListener(boolean defaultOnly, IComponent source) {
        mSource = source;
        mListeners = new ArrayList<ActionListener>();
        mDefaultOnly = defaultOnly;
    }
    void addActionListener(ActionListener l) {
    	if (l == null) throw new IllegalArgumentException("Action listener is null");
        mListeners.add(l);
    }
    void removeActionListener(ActionListener l){
        mListeners.remove(l);
    }
    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        int cnt = mListeners.size();
        if (cnt == 0)
            return;
        
        
        // We permit event to be null if called from
        // a programmatic "setSelection()".
        ActionEvent ae =
            new ActionEvent(mSource, ActionEvent.ACTION_PERFORMED, "selected");
        for (ActionListener a: mListeners){
            a.actionPerformed(ae);
        }
    }
    @Override
    public void widgetSelected(SelectionEvent e) {
        if (!mDefaultOnly)
            widgetDefaultSelected(e);
    }
}
