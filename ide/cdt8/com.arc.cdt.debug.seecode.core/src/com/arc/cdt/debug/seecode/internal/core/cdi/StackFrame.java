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

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocator;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgument;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgumentDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.core.runtime.IAdaptable;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.seecode.engine.EngineDisconnectedException;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;
import com.arc.seecode.engine.Location;
import com.arc.seecode.engine.StackFrameRef;
import com.arc.seecode.engine.Variable;

/**
 * The stackframe implementation. It wraps the
 * seecode {@Link StackFrameRef}.
 * @author David Pickens
 */
class StackFrame implements ICDIStackFrame, IAdaptable {

    private CDIThread mThread;

    private StackFrameRef mSF; // SeeCode version

    private EngineInterface mEngine;
    
    private VariableManager mVariableManager;
    
    private ICDILocator mLocation = null;
    private long mPC = -1;
    
    //private ICDIVariableManager mVarMgr;

    StackFrame(EngineInterface engine, CDIThread thread, StackFrameRef sf, VariableManager vmgr,
            RegisterManager rmgr) {
        mThread = thread;
        mSF = sf;
        mEngine = engine;
        mVariableManager = vmgr;
    }
    
    StackFrameRef getSeeCodeStackFrame(){
        return mSF;
    }
    
    /**
     * If "sf" handle looks like it references the same frame as we're
     * currently referencing, allowing for a different PC, then 
     * update this stack frame to reference the new handle.
     * 
     * The UI will highlight changed variables in red between steps
     * if the stackframe hasn't changed.
     * 
     * @param sf the new handle.
     * @return true if update was successful; false if stack frame handle
     * looks like it corresponds to a different stackframe.
     */
    boolean updateIfTheSame(StackFrameRef sf){
        if (sf == mSF) { return true; }
        if (mSF.isEqual(sf)) {
            StackFrameRef oldSF = mSF;
            mSF = sf;
            //cr99138: the frames don't match if the underlying source file changed!
            ICDILocator savedLoc = mLocation;
            
            ICDILocator newLocator = getLocator();
            if (savedLoc != null && newLocator != null &&
                savedLoc.getFile() != null && 
                !savedLoc.getFile().equals(newLocator.getFile())){
                // undo the update
                mSF = oldSF;
                mLocation = savedLoc;
                return false;
            }

            return true;
        }
        return false;       
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getLocation()
     */
    @Override
    public ICDILocator getLocator() {
        
        try {
            long pc = mSF.getPC();
            if (pc != mPC || mLocation == null){
//              Cache the result. CDT calls this method repeatedly in succession
                Location loc = mEngine.computeLocation(pc);
                mPC = pc;
                String file = loc.getSource();
                // Make the source reference be relative so as not to be long absolute name
                if (file != null){
                	file = ((Target)getTarget()).computeRelativeSourcePath(file);
                }
                mLocation = new CDILocation(loc, file);
            }
            return mLocation;
        } catch (EngineDisconnectedException e){
            try {
                mThread.getTarget().disconnect();
            } catch (CDIException e1) {
            }
            return new CDILocation(0xDeadbeef);
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            return new CDILocation(0xdeadbeef);
        }
    }
    

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getLocalVariables()
     */
    @Override
    public ICDILocalVariableDescriptor[] getLocalVariableDescriptors() throws CDIException {
        return (ICDILocalVariableDescriptor[])extractVars(Variable.AUTO);
    }

    /**
     * @return array of variables that are either local
     * or parameters.
     */
    private ICDIVariableDescriptor[] extractVars(int kind) {
        Variable[] vars;
        try {
            vars = mSF.getLocals();
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            vars = new Variable[0];
        }
        ArrayList<ICDIVariableDescriptor> list = new ArrayList<ICDIVariableDescriptor>(vars.length);
        for (int i = 0; i < vars.length; i++){
            Variable v = vars[i];
            if (/*v.getKind() == kind*/ (kind==Variable.PARM) == (v.getKind()==Variable.PARM)){
                list.add(kind==Variable.PARM?new CDIArgumentDescriptor((Target)getTarget(),v,this,mVariableManager):new CDILocalVariableDescriptor((Target)getTarget(),v,this,mVariableManager));
            }
        }
        ICDIVariableDescriptor[] result = kind==Variable.PARM?new ICDIArgumentDescriptor[list.size()]:
                        new ICDILocalVariableDescriptor[list.size()];
        
        return  list.toArray(result);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getArguments()
     */
    @Override
    public ICDIArgumentDescriptor[] getArgumentDescriptors() throws CDIException {
        return (ICDIArgumentDescriptor[])extractVars(Variable.PARM);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getThread()
     */
    @Override
    public ICDIThread getThread() {
        return mThread;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getLevel()
     */
    @Override
    public int getLevel() {
        return mSF.getLevel();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#equals(org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame)
     */
    @Override
    public boolean equals(ICDIStackFrame stackframe) {
        if (!(stackframe instanceof StackFrame)) return false;
        if (! ((StackFrame) stackframe).mSF.isEqual(mSF) ) return false;
        //cr99138: make sure the two frames reference the same source file
        ICDILocator loc1 = getLocator();
        ICDILocator loc2 = stackframe.getLocator();
        if (loc1 == null) return loc2 == null; // should not happen
        if (loc2 == null) return false; // should not happen
        if (loc1.getFile() == null) return loc2.getFile() == null;
        return loc1.getFile().equals(loc2.getFile());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIObject#getTarget()
     */
    @Override
    public ICDITarget getTarget() {
        return mThread.getTarget();
    }
    
    

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStepReturn#stepReturn()
     */
    @Override
    public void stepReturn() throws CDIException {
        mThread.stepReturn();
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStepReturn#stepReturn(org.eclipse.cdt.debug.core.cdi.model.ICDIValue)
     */
    @Override
    public void stepReturn(ICDIValue value) throws CDIException {
        // TODO Auto-generated method stub
        throw new CDIException("Operation not (yet) implemented");
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter.isInstance(this))
            return this;
        if (adapter.equals(StackFrameRef.class)){
            return this.getSeeCodeStackFrame();
        }
        if (adapter.equals(EngineInterface.class)){
            return this.mEngine;
        }
        return mThread.getAdapter(adapter);
    }

    @Override
    public ICDIArgument createArgument (ICDIArgumentDescriptor varDesc) throws CDIException {
        return (ICDIArgument)mVariableManager.createVariable((CDIVariableDescriptor)varDesc);
    }

    @Override
    public ICDILocalVariable createLocalVariable (ICDILocalVariableDescriptor varDesc) throws CDIException {
        return (ICDILocalVariable)mVariableManager.createVariable((ISeeCodeVariableDescriptor)varDesc);
    }
    
    @Override
    public String toString(){
        return "CDIStackFrame(thread=" + mThread + ",sf=" + mSF + ")";
    }

 

}
