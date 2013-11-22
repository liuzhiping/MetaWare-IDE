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
package com.arc.seecode.engine;


/**
 * Information pertaining to a watchpoint hit.
 * It is filled in by the seecode engine and returned
 * by {@link IEngineAPI#getWatchpointHits}.
 * @author David Pickens
 */
public class WatchpointHit {
    private int mWatchpoint;
    private long mAddress; //address that was hit.
    private String mOldValue;
    private String mNewValue;
    /**
     * Return the watchpoint ID.
     * @return the watchpoint ID.
     */
    public int getWatchpointID(){
        return mWatchpoint;        
    }
    
    public void setWatchpointID(int id){
        mWatchpoint = id;
    }
    
    public long getAddress(){
        return mAddress;
    }
    
    public void setAddress(long addr){
        mAddress = addr;
    }
    
    public String getOldValue(){
        return mOldValue;
    }
    
    public void setOldValue(String oldValue){
        mOldValue = oldValue;
    }
    
    public String getNewValue(){
        return mNewValue;
    }
    
    public void setNewValue(String newValue){
        mNewValue = newValue;
    }

}
