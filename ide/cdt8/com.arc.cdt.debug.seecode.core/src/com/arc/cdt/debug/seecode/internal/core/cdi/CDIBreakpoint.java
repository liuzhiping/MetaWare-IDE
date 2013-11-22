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
import java.util.HashSet;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint2;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.seecode.engine.Breakpoint;
import com.arc.seecode.engine.EngineException;


/**
 * Common base class for break- and watch-points.
 * @author David Pickens
 */
class CDIBreakpoint implements ICDIBreakpoint2 {

    private Target mTarget;
    protected Breakpoint mBP;
    private boolean mCreated;
    
    CDIBreakpoint(Target target, Breakpoint bp){
        mBP = bp;
        mTarget = target;
        mCreated = false;
    }

    protected Breakpoint getSeeCodeBreakpoint() {
        return mBP;
    }

    @Override
    public boolean isHardware() {
        return mBP.isHardware();
    }
    
    void setCreated(boolean v) {
        mCreated = true;
    }
    
    /**
     * Return true if engine has fired a creation event for this breakpoint and it has
     * been processed.
     * @return whether or not the engine has fired a new breakpoint event on behalf of this
     * object.
     */
    boolean isCreated(){
        return mCreated;
    }

    @Override
    public void setCondition(ICDICondition cond) throws CDIException {
        try {
            // We have noticed race condition in which a breakpoint is removed as it is being created
            // if use triple-clicks.
            if (mBP.isRemoved() || cond == null) return;
            String expr = cond.getExpression();
            // CR96444: CDT considers empty string ("") to mean "no conditional expression".
            // The debugger engine expects null.
            if (expr != null && expr.length() == 0) expr = null;
            mBP.setCondition(expr);
            mBP.setIgnoreCount(cond.getIgnoreCount());
            String threadNames[] = cond.getThreadIds();
            int threadIDs[];
            if (threadNames == null) threadIDs = new int[0];
            else {
                HashSet<String> names = new HashSet<String>(threadNames.length);
                for (String name: threadNames){
                    names.add(name);
                }
                List<Integer> threadList = new ArrayList<Integer>(threadNames.length);
                ICDIThread allThreads[] = mTarget.getThreads();
                for (ICDIThread t: allThreads){
                    if (names.contains(((CDIThread)t).getName())){
                        threadList.add(((CDIThread)t).getID());
                    }
                }
                if (threadList.size() != threadNames.length){
                    throw new CDIException("Can't set threads: " + threadList.size() +
                            " found of " + threadNames.length);
                }
                threadIDs = new int[threadList.size()];
                for (int i = 0; i < threadIDs.length; i++){
                    threadIDs[i] = threadList.get(i).intValue();
                }
            }
            mBP.setThreads(threadIDs);
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        }
    
    }

    @Override
    public ICDITarget getTarget() {
        return mTarget;
    }

    @Override
    public boolean isEnabled() throws CDIException {
        return mBP.isEnabled();
    }

    @Override
    public void setEnabled(boolean v) throws CDIException {
        try {
            if (!mBP.isRemoved())  // check for race condition for calling engine
                mBP.setEnabled(v);
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        }
        
    }
    
    protected int getHitCount() { 
        return mBP.getIngoreCount();
    }

    @Override
    public ICDICondition getCondition() throws CDIException {
        int threads[] = mBP.getThreads();
        String threadNames[] = null;
        if (threads != null && threads.length > 0){
            List<String>threadNameList = new ArrayList<String>(threads.length);
            for (int i = 0; i < threads.length; i++){
                ICDIThread t = mTarget.lookupThreadFromID(threads[i]);
                if (t != null){ 
                    threadNameList.add(t.toString());
                }
                // else a thread must have died since the condition was made.
            }
            if (threadNameList.size() > 0)
                threadNames = threadNameList.toArray(new String[threadNameList.size()]);        
        }
        return new Condition(getHitCount(),mBP.getCondition(),threadNames);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint#isTemporary()
     */
    @Override
    public boolean isTemporary() {
        return false;
    }

    @Override
    public int getType () {
        return isHardware()?ICBreakpointType.HARDWARE:ICBreakpointType.REGULAR;
    }

}
