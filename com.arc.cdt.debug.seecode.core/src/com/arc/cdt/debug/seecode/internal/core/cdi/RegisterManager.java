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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIAnimatable;
import org.eclipse.cdt.debug.core.cdi.ICDIEventManager;
import org.eclipse.cdt.debug.core.cdi.event.ICDIDestroyedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegister;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterGroup;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.cdt.debug.seecode.internal.core.cdi.event.ChangedEvent;
import com.arc.seecode.engine.EngineDisconnectedException;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;
import com.arc.seecode.engine.RegisterContent;
import com.arc.seecode.engine.StackFrameRef;

/**
 * The manager of registers being displayed. This version is lame in that it
 * doesn't recognize register-bank groupings.
 * 
 * @author David Pickens
 */
class RegisterManager implements IUpdatable, ICDIEventListener {

    private RegisterDescriptor mRegs[] = null;
    // Map<int,Register>
    private Map<Integer,RegisterDescriptor> mRegDescriptorMap = new HashMap<Integer,RegisterDescriptor>();
    
    private RegisterBank mBanks[] = null;
    //private StackFrameRef mFrame = null;
    private Target mTarget;
    
    private Map<ICDIThread,Map<Integer,Register>> mRegMap = new HashMap<ICDIThread,Map<Integer,Register>>();
    private ICDIEventManager mEventManager;
    
    static class RegisterBank implements ICDIRegisterGroup {
        private ICDITarget _target;
        private String _name;
        private RegisterDescriptor[] _regs;

        RegisterBank(ICDITarget target, String name, RegisterDescriptor regs[]){
            _target = target;
            _name = name;
            _regs = regs;
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterGroup#getName()
         */
        @Override
        public String getName() {
            return _name;
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.debug.core.cdi.model.ICDIObject#getTarget()
         */
        @Override
        public ICDITarget getTarget() {
            return _target;
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterGroup#getRegisterDescriptors()
         */
        @Override
        public ICDIRegisterDescriptor[] getRegisterDescriptors() throws CDIException {
            return _regs;
        }

        /**
         * {@inheritDoc} Implementation of overridden (or abstract) method.
         * @return if has any registers.
         * @throws CDIException
         */
        @Override
        public boolean hasRegisters () throws CDIException {
            return _regs.length > 0;
        }
        
    }

    /**
     * @param target
     */
    public RegisterManager(Target target, ICDIEventManager emgr) {
        mTarget = target;
        mEventManager = emgr;
        emgr.addEventListener(this);
    }

    private void init() throws CDIException {
        try {
            EngineInterface engine = mTarget.getEngineInterface();
            int bankCount = engine.getRegisterBankCount();
            ArrayList<RegisterDescriptor> allList = new ArrayList<RegisterDescriptor>();
            mBanks = new RegisterBank[bankCount];
            for (int bank = 0; bank < bankCount; bank++) {
                int regs[] = engine.getRegisterIDsFromBank(bank);               
                RegisterDescriptor bankRegs[] = new RegisterDescriptor[regs.length];
                for (int i = 0; i < regs.length; i++) {
                    
                    RegisterDescriptor r = new RegisterDescriptor(mTarget, engine.getRegisterName(regs[i]),
                            regs[i]);
                    bankRegs[i] = r;
                    allList.add(r);
                }
                mBanks[bank] = new RegisterBank(mTarget,engine.getRegisterBankName(bank),bankRegs);
            }
            mRegs = allList.toArray(new RegisterDescriptor[allList.size()]);
            for (int i = 0; i < mRegs.length; i++){
                mRegDescriptorMap.put(new Integer(mRegs[i].getID()),mRegs[i]);
            }
        } catch (EngineDisconnectedException x){
            // Debugger being shut down before initialization complete (?)
            // Don't complain.
            mBanks = new RegisterBank[0]; // prevent caller from having NPE
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        }
    }
    
    RegisterDescriptor getReg(int id){
        RegisterDescriptor r = mRegDescriptorMap.get(new Integer(id));
        if (r == null) throw new IllegalArgumentException("Bad reg id: " + id);
        return r;
    }
    /*
     * Make sure this register is being monitored for change events.
     */
    void record(Register r, ICDIThread thread){
        Map<Integer,Register>map = mRegMap.get(thread);
        if (map == null) {
            map = new HashMap<Integer,Register>();
            mRegMap.put(thread,map);
        }
        map.put(new Integer(r.getID()),r);
    }

 
    @Override
    public void update (Target target) throws CDIException {
        if (mRegMap.size() == 0)
            return;
        ArrayList<ICDIEvent> events = new ArrayList<ICDIEvent>();
        for (Map.Entry<ICDIThread, Map<Integer, Register>> entry : mRegMap.entrySet()) {
            ICDIThread thread = entry.getKey();
            Map<Integer, Register> map = entry.getValue();
            try {
                if (thread.isSuspended() || ((ICDIAnimatable)thread).isAnimating()) {
                    StackFrame sframe = ((CDIThread) thread).getTopFrame();
                    StackFrameRef sf = sframe.getSeeCodeStackFrame();
                    Collection<Register> regs = map.values();
                    int regIDS[] = new int[regs.size()];
                    int i = 0;
                    for (Register reg: regs){
                        regIDS[i++] = reg.getID();
                    }
                    RegisterContent[] contents = sf.getRegisterContent(regIDS);
                    for (RegisterContent content : contents) {
                        Register reg = map.get(content.getRegister());
                        if (reg != null && reg.update(sf,content)) {
                            events.add(new ChangedEvent((ICDIRegister)reg));
                        }
                    }
                }
            }
            catch (CDIException e) {
                // bad stackframe; don't refresh
            }
            catch (EngineException e) {
                // Engine isn't it correct sttae; don't update.
                // Presumably, this problem will manifest later with an appropriate diagnostic.
            }
        }
        if (events.size() > 0) {
            ICDIEvent e[] = events.toArray(new ICDIEvent[events.size()]);
            fireEvents(e);
        }
    }

    /**
     * Fire the register-change events through the event manager.
     * @param events register-change events
     */
    private void fireEvents(ICDIEvent events[]) {
        EventManager emgr = (EventManager) ((Session)mTarget.getSession()).getEventManager();
        emgr.enqueueEvents(events);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.cdi.ICDIRegisterManager#getRegisterGroups()
     */
    public ICDIRegisterGroup[] getRegisterGroups() throws CDIException {
        if (mBanks == null) {
            init();
            if (mBanks == null) throw new CDIException("Engine not responding to register queries");
        }
        return mBanks;       
    }

    @Override
    public boolean isAutoUpdate () {
       return true;
    }
    
    private void handleTargetTerminationEvent(ICDIDestroyedEvent e){
        if (e.getSource() == mTarget)
            mEventManager.removeEventListener(this);
    }
    
    private void handleThreadTerminationEvent(ICDIDestroyedEvent e){
        ICDIThread t = (ICDIThread)e.getSource();
        mRegMap.remove(t); // thread terminated; flush registers
    }
    
    private void handleResumedEvent (ICDIResumedEvent e) {
        switch (e.getType()) {
            case ICDIResumedEvent.CONTINUE:
                if (e.getSource() == mTarget) {
                    // If target is resuming but not stepping
                    // then UI will refresh all registers, so we
                    // don't need to track them.
                    mRegMap.clear();
                }
                else if (e.getSource() instanceof ICDIThread) {
                    mRegMap.remove(e.getSource());
                }
                break;
            default:
        }
    }
    
    /**
     * called when a CDI debugger event occurs.
     */
    @Override
    public void handleDebugEvents (ICDIEvent[] events) {
        for (ICDIEvent e: events){
            if (e instanceof ICDIDestroyedEvent){
                if (e.getSource() == mTarget){
                    handleTargetTerminationEvent((ICDIDestroyedEvent)e);
                }   
                else if (e.getSource() instanceof ICDIThread){
                    handleThreadTerminationEvent((ICDIDestroyedEvent)e);
                }
            }
            else if (e instanceof ICDIResumedEvent){
                handleResumedEvent((ICDIResumedEvent)e);             
            }           
        }       
    }
}
