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
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIAnimatable;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIJumpToLine;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDITargetConfiguration2;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThreadStorage;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThreadStorageDescriptor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.cdt.debug.seecode.internal.core.cdi.event.ResumedEvent;
import com.arc.cdt.debug.seecode.internal.core.cdi.event.SuspendedEvent;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.Location;
import com.arc.seecode.engine.StackFrameRef;

/**
 * A thread proxy for the engine interface.
 * 
 * @author David Pickens
 */
class CDIThread implements ICDIThread, ICDIJumpToLine, ICDIAnimatable, IAdaptable {

    private Target mTarget;

    private StackFrameRef mTopFrameRef = null;
    
    private List<StackFrame> mStackFrames = new ArrayList<StackFrame>();

    private int mTID;

    private String mName;
    
    private int mPendingRunState = ICDIResumedEvent.CONTINUE;
    
    private boolean animating = false;

    CDIThread(Target target, int tid, String name) {
        mTarget = target;
        mTID = tid;
        mName = name;
    }
    
    /**
     * See the comment "KLOODGE" in {@link ThreadTable#findOrCreateThreadFor(int)}
     * to see why we would need to reset the name of a thread.
     * @param name the new name of the thread.
     */
    void setName(String name){
        mName = name;
    }
    
    /**
     * Return the id that the engine uses as a handle to this thread.
     * @return the id that the engine uses as a handle to this thread.
     */
    public int getID(){
        return mTID;
    }
    
    /**
     * Return the name associated with the thread.
     * @return the name associated with the thread.
     */
    public String getName(){
        // The SeeCode engine has a "main" thread that is the 
        // thread that currently has the CPU. Mark with "*"
        try {
            if (mTarget.getCurrentThread() == this && mTarget.getThreadCount() > 1)
                return "*" + mName;
        }
        catch (CDIException e) {
            // Error should occur again later if something is messed up with engine.
        }
        return mName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#getStackFrames()
     */
    @Override
    public ICDIStackFrame[] getStackFrames() throws CDIException {
        return getStackFrames(0, 1000);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#getStackFrames(int,
     *      int)
     */
    @Override
    public ICDIStackFrame[] getStackFrames (int lowFrame, int highFrame) throws CDIException {
        try {
            StackFrameRef sf = computeTopFrameRef();
            int index = 0;
            // Try to reuse frames so that the Variable View can display diffs
            // between steps.
            synchronized (mStackFrames) {
                while (sf != null && index <= highFrame) {
                    if (index >= mStackFrames.size() ||
                        mStackFrames.get(index) == null ||
                        mStackFrames.get(index).getSeeCodeStackFrame() != sf &&
                        !mStackFrames.get(index).updateIfTheSame(sf)) {
                        StackFrame stackframe = new StackFrame(mTarget.getEngineInterface(), this, sf, mTarget
                            .getVariableManager(), mTarget.getRegisterManager());
                        if (index >= mStackFrames.size())
                            mStackFrames.add(stackframe);
                        else
                            mStackFrames.set(index, stackframe);
                    }
                    sf = sf.getCallerFrame();
                    index++;
                }
                while (mStackFrames.size() > index)
                    mStackFrames.remove(index);
                int cnt = Math.max(0, index - lowFrame);
                ICDIStackFrame f[] = new ICDIStackFrame[cnt];
                for (int i = 0; i < cnt; i++) {
                    f[i] = mStackFrames.get(lowFrame + i);
                }
                return f;
            }
        }
        catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        }

    }

    /**
     * @return top frameref
     */
    private StackFrameRef computeTopFrameRef() throws EngineException {
        StackFrameRef current = mTarget.getEngineInterface().getTopStackFrame(mTID);
        if (mTopFrameRef != current) {
            mTopFrameRef = current;
            // The UI wants to see the same StackFrame object between
            // "step" operations if the function did not return.
            // So, if the new stack frame reference appears to be in the
            // same function, then re-use it.
            synchronized (mStackFrames) {
                if (mStackFrames.size() > 0 && mStackFrames.get(0) != null && 
                    current != null && // Apparently can be null as engine is shutting down
                    !mStackFrames.get(0).updateIfTheSame(current))
                    mStackFrames.set(0, null);
            }
        }
        return mTopFrameRef;
    }
    
    StackFrame getTopFrame () throws CDIException {
        try {
            StackFrameRef sf = computeTopFrameRef();
            // When engine is shutdown, or is sick, we can get back a null stack frame
            if (sf == null)
                throw new CDIException("Engine not able to compute stackframe");
            synchronized (mStackFrames) {
                if (mStackFrames.size() == 0 || mStackFrames.get(0) == null) {
                    StackFrame s = new StackFrame(mTarget.getEngineInterface(), this, sf, mTarget.getVariableManager(),
                        mTarget.getRegisterManager());
                    if (mStackFrames.size() == 0)
                        mStackFrames.add(s);
                    else
                        mStackFrames.set(0, s);
                }
            }
        }
        catch (EngineException e) {
            throw new CDIException(e.getMessage());
        }

        return mStackFrames.get(0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#getStackFrameCount()
     */
    @Override
    public int getStackFrameCount() throws CDIException {
        try {
            StackFrameRef sf = computeTopFrameRef();
            int cnt = 0;
            while (sf != null) {
                cnt++;
                sf = sf.getCallerFrame();
            }
            return cnt;
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        }
    }



    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#isSuspended()
     */
    @Override
    public boolean isSuspended() {
        return mTarget.isSuspended(mTID);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#resume()
     */
    @Override
    public void resume() throws CDIException {
        mPendingRunState = ICDIResumedEvent.CONTINUE;
        animating = false;
        mTarget.resume(mTID);

    }
    
    @Override
    public void animate (int stepType) throws CDIException {
        mPendingRunState = ICDIResumedEvent.CONTINUE;
        mTarget.animate(stepType,mTID);
        animating = true;      
    }
    
    @Override
    public boolean isAnimating(){
        return animating;
    }
    
    @Override
    public void setAnimateStepDelay(int milliseconds){
        mTarget.setAnimateStepDelay(milliseconds);
    }
    
    /**
     * Called when a suspend event is serviced. Basically, it just turns off the "animating"
     * flag.
     */
    void onSuspended(){
        animating = false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#suspend()
     */
    @Override
    public void suspend() throws CDIException {
        mTarget.suspend(mTID);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#stepReturn()
     */
    @Override
    public void stepReturn() throws CDIException {
        mTarget.stepReturn(mTID);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#stepOver()
     */
    @Override
    public void stepOver(int count) throws CDIException {
        mPendingRunState = ICDIResumedEvent.STEP_OVER;
        mTarget.stepOver(mTID,count);
    }
    
    /**
     * Return the kind of "resume" that last ran this thread.
     */
    int getPendingRunState(){
        return mPendingRunState;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#stepInto()
     */
    @Override
    public void stepInto(int count) throws CDIException {
        mPendingRunState = ICDIResumedEvent.STEP_INTO;
        mTarget.stepInto(mTID,count);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#stepOverInstruction()
     */
    @Override
    public void stepOverInstruction(int count) throws CDIException {
        mPendingRunState = ICDIResumedEvent.STEP_OVER_INSTRUCTION;
        mTarget.stepOverInstruction(mTID,count);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#stepIntoInstruction()
     */
    @Override
    public void stepIntoInstruction(int count) throws CDIException {
        mPendingRunState = ICDIResumedEvent.STEP_INTO_INSTRUCTION;
        mTarget.stepIntoInstruction(mTID,count);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#runUntil(org.eclipse.cdt.debug.core.cdi.ICDILocation)
     */
    @Override
    public void runUntil(ICDILocation location) throws CDIException {
        mPendingRunState = ICDIResumedEvent.STEP_RETURN;
        mTarget.runUntil(location, mTID);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#jump(org.eclipse.cdt.debug.core.cdi.ICDILocation)
     */
    @Override
    public void jump(ICDILocation location) throws CDIException {
        throw new CDIException("jump not (yet) supported");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#signal()
     */
    @Override
    public void signal() throws CDIException {
        throw new CDIException("signal not (yet) supported");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#signal(org.eclipse.cdt.debug.core.cdi.model.ICDISignal)
     */
    @Override
    public void signal(ICDISignal signal) throws CDIException {
        throw new CDIException("signal not (yet) supported");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#equals(org.eclipse.cdt.debug.core.cdi.model.ICDIThread)
     */
    @Override
    public boolean equals(ICDIThread thread) {
        return thread == this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIObject#getTarget()
     */
    @Override
    public ICDITarget getTarget() {
        return mTarget;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getName();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#stepOver()
     */
    @Override
    public void stepOver() throws CDIException {
        stepOver(1);
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#stepInto()
     */
    @Override
    public void stepInto() throws CDIException {
        stepInto(1);
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#stepOverInstruction()
     */
    @Override
    public void stepOverInstruction() throws CDIException {
        stepOverInstruction(1);
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#stepIntoInstruction()
     */
    @Override
    public void stepIntoInstruction() throws CDIException {
        stepIntoInstruction(1);
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStep#stepUntil(org.eclipse.cdt.debug.core.cdi.ICDILocation)
     */
    @Override
    public void stepUntil(ICDILocation location) throws CDIException {
        runUntil(location);
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteResume#resume(boolean)
     */
    @Override
    public void resume(boolean passSignal) throws CDIException {
        //TODO fix
        resume();
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteResume#resume(org.eclipse.cdt.debug.core.cdi.ICDILocation)
     */
    @Override
    public void resume(ICDILocation location) throws CDIException {
        Target target = (Target)getTarget();
        if (target.getEngineInterface().doesEngineHandleSetPC()){
            Location loc = target.createLocation(location);
            if (!loc.isValid()){
                SeeCodePlugin.getDefault().displayError("Debugger Operation Failure",
                      "Could not resolve source line to a machine address.");
                return;
            }
            if (loc.isAmbiguous()){
                SeeCodePlugin.getDefault().displayNote("Warning",
                    "The source line was apparently inlined at more than one place\n"+
                    "and resolves to more than one machine address. One of these addresses\n"+
                    "was chosen arbitrarily.");
            }
            try {
                ResumedEvent resume;
                SuspendedEvent suspend;
                if (((ICDITargetConfiguration2)target.getConfiguration()).supportsThreadControl()) {
                    resume = new ResumedEvent(this,ICDIResumedEvent.CONTINUE);
                    suspend = new SuspendedEvent(this,(Session)target.getSession());
                }
                else {
                    resume = new ResumedEvent(target,ICDIResumedEvent.CONTINUE);
                    suspend = new SuspendedEvent(target,(Session)target.getSession());
                }
                ((EventManager)target.getSession().getEventManager()).enqueueEvent(resume);
                target.getEngineInterface().setPC(this.getID(),loc);  // resume/stop event now done implicitly from engine.
                target.getRegisterManager().update(target);             
                ((EventManager)target.getSession().getEventManager()).enqueueEvent(suspend);
            }
            catch (EngineException e) {
                throw new CDIException(e);
            }
        }
        else {
            throw new CDIException("Jump not (yet) supported");
        }
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteResume#resume(org.eclipse.cdt.debug.core.cdi.model.ICDISignal)
     */
    @Override
    public void resume(ICDISignal signal) throws CDIException {
        // TODO fix
        throw new CDIException("Operation not (yet) implemented");
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter.equals(ICDITarget.class)){
            return mTarget;
        }
        if (adapter.equals(ICDIStackFrame.class)){
            try {
                return getTopFrame();
            } catch (CDIException e) {
               //ignore
            }
        }
        if (adapter.equals(StackFrameRef.class)){
            try {
                return computeTopFrameRef();
            } catch (EngineException e) {
                //ignore
            }
        }
        if (adapter.equals(ICDISession.class)){
            return mTarget.getSession();
        }
        if (adapter.isInstance(this))
            return this;
        // If adapting to Integer, assume caller wants the thread ID.
        if (adapter.equals(Integer.class)){
            return new Integer(getID());
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIThread#getThreadStorageDescriptors()
     */
    @Override
    public ICDIThreadStorageDescriptor[] getThreadStorageDescriptors() throws CDIException {
        //TODO: fixme
        return new ICDIThreadStorageDescriptor[0];
    }

    @Override
    public ICDIThreadStorage createThreadStorage (ICDIThreadStorageDescriptor varDesc) throws CDIException {
        assert false; //shouldn't get here.
        return null;
    }

    public void clearPendingRunState () {
        mPendingRunState = ICDIResumedEvent.CONTINUE;       
    }

    @Override
    public boolean canJumpToLine (IFile file, int line) {
        return ((Target)getTarget()).getEngineInterface().doesEngineHandleSetPC();
    }

    @Override
    public boolean canJumpToLine (String file, int line) {
        return ((Target)getTarget()).getEngineInterface().doesEngineHandleSetPC();
    }

    @Override
    public int getAnimationCount () {
        return ((ICDIAnimatable)this.getTarget()).getAnimationCount();
    }

    @Override
    public void resetAnimationCounter () {
        ((ICDIAnimatable)this.getTarget()).resetAnimationCounter();     
    }

    @Override
    public int getActualAnimateStepDelay () {
        return this.mTarget.getActualAnimateStepDelay();
    }

    @Override
    public int getAnimateStepDelay () {
        return this.mTarget.getAnimateStepDelay();
    }
}
