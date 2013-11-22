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
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;

import com.arc.seecode.engine.EngineException;

/**
 * Maps threads between the engine and CDI.
 * @author David Pickens
 */
class ThreadTable {
	static class Pair {
		Pair(int tid, CDIThread thread){
			this.tid = tid;
			this.thread = thread;
		}
		int tid;
		CDIThread thread;
	}

	private List<Pair> mList = new ArrayList<Pair>();
	private Target mTarget;
	
	
	ThreadTable(Target target){
		mTarget = target;		
	}
	
	synchronized int getThreadID(ICDIThread t) throws CDIException{
        for (Pair p: mList){
			if ( p.thread == t){
				return p.tid;
			}
		}
		throw new CDIException("Can't find thread!");
	}
	
	synchronized CDIThread lookupThreadFor(int tid){
		for (Pair p: mList){
			if (p.tid == tid){
				return p.thread;
			}
		}
		return null;
	}
    
    /**
     * @return true if all threads have been successfully suspended by the debugger.
     */
    boolean isAllSuspended(){
        for (Pair p: mList) {
            if (!p.thread.isSuspended()) return false;
        }
        return true;
    }
    
    /**
     * Prepare to resume a process. Clear any "pending" states from each thread.
     */
    void prepareForProcessResume(){
        for (Pair p: mList) {
            p.thread.clearPendingRunState();
        }
    }
    
    /**
     * @return true if all threads have been successfully resumed by the debugger.
     */
    boolean isAllResumed(){
        for (Pair p: mList) {
            if (p.thread.isSuspended()) return false;
        }
        return mList.size() > 0;
    }
	
    /*
     * Find or create thread object for an id.
     */
	CDIThread findOrCreateThreadFor(int tid){
	    CDIThread thread = lookupThreadFor(tid);
	    if (thread == null){
            String name;
            // Take care not to invoke engine while sync lock is owned.
            // It could cause deadlock as the engine calls back into
            // the GUI.
            try {
                name = mTarget.getEngineInterface().getThreadName(tid);
            }
            catch (EngineException e) {
                name = tid + "?";
            }
            synchronized(this){
                thread = lookupThreadFor(tid);
                if (thread == null){ // In case of race condition
                    thread = new CDIThread(mTarget,tid,name);
		            mList.add(new Pair(tid,thread));
                }
            }
            
            //KLOODGE:
            // The SeeCode engine always creates a phantom thread
            // at startup that has an ID if "NO_TID" and is
            // given the name "main task" or "main thread". It is made
            // to be a clone of the "current thread".
            // Our interface into the engine gets rid of this phantom
            // thread in a multi-threaded application by replacing
            // it with the first thread that the application
            // creates. That being the case, we need to refresh
            // the name of that thread. So, when the number of threads
            // goes to 2, we force the first thread's name to be
            // recomputed.
            if (mList.size() == 2){
                String threadName = null;
                try {                   
                    threadName = mTarget.getEngineInterface().getThreadName(mList.get(0).tid);
                }
                catch (EngineException e) {
                    // Just ignore; thread may have gone away
                }
                synchronized(this){
                    // Beware of changes in engine callback tread
                    if (threadName != null && mList.size() > 0){
                        mList.get(0).thread.setName(threadName);
                    }
                }
            }
	    }
		return thread;
	}
    
    synchronized int getThreadCount(){
        return mList.size();
    }
    
    synchronized ICDIThread[] getThreads(){
        ICDIThread threads[] = new ICDIThread[mList.size()];
        int i = 0;
        for (Pair p: mList){
            threads[i++] = p.thread;
        }
        return threads;
    }
	
	synchronized void disposeThread(int tid){
		Iterator<Pair> each = mList.iterator();
		while (each.hasNext()){
			Pair p = each.next();
			if (p.tid == tid){
				each.remove();
				return;
			}
		}
	}
	
	synchronized void disposeThread(ICDIThread t){
		Iterator<Pair> each = mList.iterator();
		while (each.hasNext()){
			Pair p = each.next();
			if (p.thread == t){
				each.remove();
				return;
			}
		}
	}
}
