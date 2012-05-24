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
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;


/**
 * A ModifyListener implementation that wraps a key listener.
 */
class ModifyTextListener implements ModifyListener {

    private List <TextListener> mListeners;

    ModifyTextListener() {
        mListeners = new ArrayList<TextListener>();
    }

    void add (TextListener l) {
        mListeners.add(l);
    }

    @Override
    public void modifyText (ModifyEvent e) {
        int cnt = mListeners.size();
        if (cnt == 0)
            return;
        TextEvent te = new TextEvent(e.widget, TextEvent.TEXT_VALUE_CHANGED);
        for (int i = 0; i < cnt; i++) {
            TextListener t =  mListeners.get(i);
            t.textValueChanged(te);
        }
    }
}
