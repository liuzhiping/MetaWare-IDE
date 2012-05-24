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

import com.arc.seecode.display.ISeeCodeTextViewer;
import com.arc.seecode.display.ISeeCodeTextViewerCallback;

/**
 * A wrapper to send "value updates" on behalf of a display.
 * @author David Pickens
 */
public class ValueSender implements IValueSender {
    private ISeeCodeTextViewerCallback mCallback;
    private ISeeCodeTextViewer mDisplay;
    ValueSender(ISeeCodeTextViewer d, ISeeCodeTextViewerCallback callback){
        assert callback != null;
        mDisplay = d;
        mCallback = callback;
    }
    
    public ISeeCodeTextViewerCallback getCallback(){
        return mCallback;
    }
    
    @Override
    public void sendValueUpdate(String property, String value){
        mCallback.sendValueUpdate(mDisplay,property,value);
    }
    
    @Override
    public boolean sendValueUpdate(String property, String value, int timeout){
        return mCallback.sendValueUpdate(mDisplay,property,value,timeout);
    }
    
    @Override
    public void sendValueUpdate(String property, String value, boolean record){
        sendValueUpdate(property,value);
        if (record && mDisplay != null){
            mDisplay.addValueUpdate(property,value);
        }
    }
}
