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
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.event.ICDICreatedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIDestroyedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIRestartedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;

import com.arc.cdt.debug.seecode.internal.core.cdi.event.ChangedEvent;
import com.arc.cdt.debug.seecode.internal.core.cdi.event.CreatedEvent;
import com.arc.cdt.debug.seecode.internal.core.cdi.event.DestroyedEvent;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;
import com.arc.seecode.engine.Variable;

/**
 * Variable manager for SeeCode
 * 
 * @author David Pickens
 */
class VariableManager extends Manager {

    private Map<ISeeCodeVariableDescriptor,ISeeCodeVariable> mVarMap = new HashMap<ISeeCodeVariableDescriptor,ISeeCodeVariable>();
    private Map<String,ICDIGlobalVariableDescriptor> mGlobalsMap = new HashMap<String,ICDIGlobalVariableDescriptor>();

    /**
     * @param target the associated target.
     */
    public VariableManager(Target target) {
        super(target, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#getGlobalVariableObject(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public ICDIGlobalVariableDescriptor getGlobalVariableDescriptor(String filename,
            String function, String name) throws CDIException {
        CDIGlobalVariableDescriptor var;
        synchronized(mGlobalsMap){
            var = (CDIGlobalVariableDescriptor) mGlobalsMap.get(name);
        }
        if (var == null) {
            EngineInterface e = getTarget().getEngineInterface();
            try {
                //TODO: need to add filename and function info...
                Variable v = e.lookupGlobalVariable(name);
                if (v == null)
                    throw new CDIException("Can't find \"" + name + "\"");
                var = new CDIGlobalVariableDescriptor(getTarget(), v, null,this);
                synchronized(mGlobalsMap) {
                    mGlobalsMap.put(name, var);
                }
            } catch (EngineException e1) {
                throw new CDIException(e1.getMessage());
            }
        }
        return var;
    }
    
    /**
     * Return whether or not a particular variable is active.
     * The UI seems to reference stale global variables so we need to know
     * when to resurrect them.
     * @param d
     * @return true if active.
     */
    public boolean isActive(ISeeCodeVariable d){
        return mVarMap.get(d.getDescriptor()) == d;
    }
    
    /**
     * This method is required because the UI seems to reference stale global variables
     * even after we have destroyed them. If so, reconnect them.
     * @param v a stale global variable that the UI is referencing; it will be resurrected.
     * @throws CDIException 
     */
    public void reconnect(ISeeCodeVariable v) throws CDIException{
        ISeeCodeVariable v1 = mVarMap.get(v.getDescriptor());
        if (v1 != v) {
            if (v1 != null)
				destroyVariable(v1); // shouldn't happen
            synchronized(mVarMap){
                mVarMap.put(v.getDescriptor(),v);
            }
            EventManager emgr = (EventManager)getSession().getEventManager();
            emgr.enqueueEvent(new CreatedEvent(v));
            update((Target)v.getTarget()); // so that its value will be current
        }
    }
    
    ICDIVariable createVariable(ISeeCodeVariableDescriptor v) throws CDIException{
        ISeeCodeVariable result;
        synchronized(mVarMap){
            result = mVarMap.get(v);
        }
        if (result != null){
            result.setSeeCodeCookie(v.getSeeCodeCookie());
        }
        else {
            result = v.allocateVariable();
            synchronized(mVarMap) {
                mVarMap.put(v,result);
            }
            EventManager emgr = (EventManager) getSession().getEventManager();
            emgr.enqueueEvent(new CreatedEvent(result));
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableManager#destroyVariable(org.eclipse.cdt.debug.core.cdi.model.ICDIVariable)
     */
    public void destroyVariable(ICDIVariable var) {
        
        synchronized (mVarMap) {
            Iterator<Map.Entry<ISeeCodeVariableDescriptor,ISeeCodeVariable>>each = mVarMap.entrySet().iterator();
            while (each.hasNext()){
                Map.Entry<ISeeCodeVariableDescriptor,ISeeCodeVariable> entry = each.next();
                if (entry.getValue() == var){
                    each.remove();
                    EventManager emgr = (EventManager) getSession().getEventManager();
                    emgr.enqueueEvent(new DestroyedEvent(var));
                    break;
                }
            }
        }
        
    }
    
    /**
     * Called immediately when we're notified that the process is destroyed
     * so that the Variable display doesn't attempt to refresh with
     * stale handles.
     * However, the UI seems to re-use global variables across restarts. So, don't
     * kill the globals.
     */
    private void destroyAllNonGlobals () {
        ArrayList<ICDIEvent> events = new ArrayList<ICDIEvent>();
        synchronized (mVarMap) {         
            if (mVarMap.size() > 0) {
                Iterator<Map.Entry<ISeeCodeVariableDescriptor,ISeeCodeVariable>> each = mVarMap.entrySet().iterator();
                while (each.hasNext()){
                    Map.Entry<ISeeCodeVariableDescriptor,ISeeCodeVariable> entry = each.next();
                    if (! (entry.getKey() instanceof ICDIGlobalVariableDescriptor)) {
                        events.add(new DestroyedEvent(entry.getValue()));
                        each.remove();
                    }
                }
                
                if (events.size() > 0) {
                    EventManager emgr = (EventManager) getSession().getEventManager();
                    emgr.enqueueEvents(events.toArray(new ICDIEvent[events.size()]));
                }
            }
        }
    }

    /*
     * Synchronized because it is called from main UI thread and
     * the "hanldDebugEvent" is called from the EventQueue thread.
     */
    @Override
    public void update(Target target) throws CDIException {
        ArrayList<ICDIObject> changes = new ArrayList<ICDIObject>();
        ArrayList<ICDIVariable> tobeDeleted = new ArrayList<ICDIVariable>();
        Collection<ISeeCodeVariable> values;
        synchronized(mVarMap){
            values = mVarMap.values();
        }
        // Must make copy of values before iterating. If we lock it, we could get
        // deadlock. 
        ISeeCodeVariable vars[] = values.toArray(new ISeeCodeVariable[values.size()]);
        for (ISeeCodeVariable v : vars) {
            v.refresh(changes);
            // Above call may have set it inactive (out of scope)
            if (v.isOutOfScope()) {
                tobeDeleted.add(v);
            }
        }

        if (changes.size() > 0 || tobeDeleted.size() > 0) {
            changes.removeAll(tobeDeleted);
            ArrayList<ICDIEvent> events = new ArrayList<ICDIEvent>();
            for (ICDIObject v: changes){
                if (v instanceof ICDIVariable) {
                    events.add(new ChangedEvent((ICDIVariable)v));
                }
            }
            for (ICDIVariable v: tobeDeleted) {
                destroyVariable(v);
                events.add(new DestroyedEvent(v));
            }
            ICDIEvent ev[] = events.toArray(new ICDIEvent[events.size()]);
            EventManager emgr = (EventManager) getSession().getEventManager();
            emgr.enqueueEvents(ev);
        }
    }
    /* 
     * Synchronized because update() is called from the UI thread
     * and this method is called from the EventQueue thread.
     */
    @Override
    public void handleDebugEvents(ICDIEvent[] e) {
        // If process is being destroyed (or created, in 
        // case we lost the destroyed event), then
        // clear everything so that there are no 
        // stale objects. The engine may have fits if
        // asked to update a stale aggregate element.
        for (int i = 0; i < e.length; i++){
            ICDIEvent event = e[i];
            if (event instanceof ICDIDestroyedEvent ||
                event instanceof ICDICreatedEvent ||
                event instanceof ICDIRestartedEvent){
                if (event.getSource() instanceof ICDITarget){
                    destroyAllNonGlobals();
                }
            }
            // If there are globals left, then force them to be "changed" so that
            // the UI will refresh. Don't know why this doesn't happen automatically.
            if (event instanceof ICDIRestartedEvent && mVarMap.size() > 0){
                ArrayList<ICDIEvent> changeEvents = new ArrayList<ICDIEvent>();
                for (ISeeCodeVariable v: mVarMap.values()){
                    changeEvents.add(new ChangedEvent(v));
                }
                EventManager emgr = (EventManager) getSession().getEventManager();
                emgr.enqueueEvents(changeEvents.toArray(new ICDIEvent[changeEvents.size()]));
            }
        }
        super.handleDebugEvents(e);
    }
}
