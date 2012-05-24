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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Tracks break- and watchpoints by keeping a parrallel table with the one maintained
 * internally in the engine. This obviates the need to populate
 * {@link Breakpoint}objects from the C++-based engine.
 * <P>
 * NOTE: unless otherwise indicated, we use the term
 * <i>Breakpoint</i> to denote location breakpoints and
 * data watchpoints.
 * 
 * @author David Pickens
 */
public class BreakpointManager {

    private EngineInterface mEngine;
    // Do we remove breakpoint when "remove" event occurs?
    // If not, then the event handler will do it.
    private boolean mRemoveOnRemoveEvent = true;
    

    //private int mLastIndex = 0;
    public interface IObserver {

        /** called when breakpoint is removed. */
        public void onRemoved(Breakpoint bp);

        /** Called when new breakpoint created */
        public void onCreated(Breakpoint bp);

        /** Called when enable state changed */
        public void onStateChange(Breakpoint bp);

        /** Called when condition or hit count changed */
        public void onConditionChange(Breakpoint bp);
        
        /** Called when a thread encounters a breakpoint, after 
         * the engine's {@link IEngineObserver#processStopped} event is fired.
         * <P>
         * The command-line processor overrides this so that it can invoke
         * an "exec" command associated with the breakpoint.
         * @param bp the breakpoint that was hit.
         */
        public void onHit(Breakpoint bp);
    }

    private List<IObserver> mListeners = new ArrayList<IObserver>();

    /**
     * List <Breakpoint>list of set breakpoints
     */
    private List<Breakpoint> mBreakpoints = Collections.synchronizedList(new ArrayList<Breakpoint>());

    /**
     * 
     * @param e
     * @pre e != null
     */
    /*package*/ BreakpointManager(EngineInterface e) throws EngineException {
        mEngine = e;
        e.setBreakpointObserver(new IBreakpointObserver() {

            @Override
            public void breakpointStateChanged(int breakID, boolean enabled) {
                Breakpoint bp = getBreakpointFromID(breakID);
                assert bp != null;
                bp.setEnabledProperty(enabled);
                fireEvent(bp, STATE);
            }

            // Called when breakpoint is removed.
            // We're concerned about breakpoints that are
            // removed implicitly by engine, if that is possible.
            // NOTE: when we restart an exe that has changed, the
            // engine (as of now) deletes all breakpoints as a 
            // conservative measure. We don't want this to happen!
            // So we ignore such events when a restart is pending so that
            // we can call "reapply()" to restore them the best we can!
            @Override
            public void breakpointRemoved(int breakID) {
                if (!mEngine.isRestartPending()) {
                    Breakpoint bp = getBreakpointFromID(breakID);
                    // If already removed; then this will be null.
                    if (bp != null) {
                        bp.setDeletedByEngine();  // Don't re-issue breakpoint delete; the ID may be reassigned.
                        try {
                            // Do we remove it ourselves? or
                            // assume that an event handler will do it?
                            if (mRemoveOnRemoveEvent)
                                remove(bp);
                            else
                                // Remove from list in case the breadID is immediately re-assigned
                                // go another breakpoint, which is common when the MetaWare break display
                                // is used to "edit" a breakpoint.
                                mBreakpoints.remove(bp);
                        } catch (EngineException x) {
                        }
                        fireEvent(bp, REMOVED);
                    }
                }
            }

            @Override
            public void breakpointConditionChanged(int breakID) {
                Breakpoint bp = getBreakpointFromID(breakID);
                // If already removed; then this will be null.
                if (bp != null) {
                    fireEvent(bp, COND);
                }

            }

            //deprecated
            @Override
            public void breakpointAdded(int breakID, Location location, int hitCount, String conditional, int tid) {
                // NOTE: if engine creates a disable breakpoint, then it must emit
                // two events: this one and a breakpointStateChange
                addBreakpoint(breakID,location,hitCount,conditional,tid,0,true,true);             
            }

            // deprecated
            @Override
            public void watchpointAdded (int breakID, String var, int length, int hitCount, String condition, int flags) {
                //NOTE: as of interface 12 (engine build ID 1334), this method is
                // no longer called. watchpointAdded2 is called instead.
                addWatchpoint(breakID,var,length,condition,0,flags,true,null);               
            }

            @Override
            public void watchpointAdded2 (
                int breakID,
                String var,
                int length,
                String condition,
                int flags,
                int threadID,
                String[] attributes) {
                // if watchpoint added in disabled mode then engine will produce two events:
                // this one and a state-change event.
                addWatchpoint(breakID,var,length,condition,threadID,flags,true,attributes);               
            }

            @Override
            public void breakpointAdded2 (
                int breakID,
                Location location,
                int ignoreCount,
                String conditional,
                int tid,
                int flags,
                String[] attributes) {
                boolean enabled = (flags & IEngineAPI.BP_DISABLED) == 0;
                addBreakpoint(breakID,location,ignoreCount,conditional,tid,flags,enabled,true);       
                
            }
        });
    }

    public void addObserver(IObserver o) {
        synchronized (mListeners) {
            mListeners.add(o);
        }
    }
    
    /**
     * Indicate what is to be done if the engine fires
     * a "breakpointRemoved" event. If true, then we
     * remove the breakpoint explicitly. If false, we
     * assume that an event handler will do it.
     * @param v
     */
    public void setRemoveOnRemoveEvent(boolean v){
        mRemoveOnRemoveEvent = v;
    }

    public void removeObserver(IObserver o) {
        synchronized (mListeners) {
            mListeners.remove(o);
        }
    }
    
    /**
     * Called from {@linkplain EngineInterface engine} when breakpoint is hit.
     * @param breakID the breakpoint ID that was hit.
     */
    void fireHit(int breakID){
        Breakpoint bp = this.getBreakpointFromID(breakID);
        if (bp != null)
            fireEvent(bp,HIT);
    }

    private static final int REMOVED = 1;

    private static final int CREATED = 2;

    private static final int STATE = 3;

    private static final int COND = 4;
    
    private static final int HIT = 5;

    protected void fireEvent(Breakpoint bp, int kind) {
        IObserver observers[] = null;
        synchronized (mListeners) {
            int cnt = mListeners.size();
            if (cnt == 0) return;
            observers = mListeners.toArray(new IObserver[cnt]);
        }
        for (int i = 0; i < observers.length; i++) {
            switch (kind) {
            case REMOVED:
                observers[i].onRemoved(bp);
                break;
            case CREATED:
                observers[i].onCreated(bp);
                break;
            case STATE:
                observers[i].onStateChange(bp);
                break;
            case COND:
                observers[i].onConditionChange(bp);
                break;
            case HIT:
                observers[i].onHit(bp);
                break;
            default:
                throw new IllegalArgumentException("Bad event");
            }
        }
    }

    /**
     * Indicate that a thread just terminated. Remove all breakpoints that are
     * tied to the thread.
     * 
     * @param tid
     *            the thread ID.
     */
    public void threadTerminated(int tid) {
        assert (tid != 0);
        Iterator<Breakpoint> each = mBreakpoints.iterator();
        // Go through breakpoints and find any that are dependent on the
        // thread that is being terminated. If its the only thread tied to
        // the breakpoint, then delete the breakpoint
        while (each.hasNext()) {
            Breakpoint bp =  each.next();
            int tids[] = bp.getThreads();
            if (tids.length > 1){
                int newArray[] = new int[tids.length-1];
                int j = 0;
                for (int id: tids){
                  if (id != tid && j < newArray.length){
                      newArray[j++] = id;
                  }
                }
                try {
                    bp.setThreads(newArray);
                }
                catch (EngineException e) {
                      // what to do?
                }
            }
            else if (tids.length == 1 && tids[0] == tid){
                each.remove();
                fireEvent(bp,REMOVED);
            }
        }
    }

    /**
     * Create a global breakpoint on a given location.
     * 
     * @param loc
     * @return the new breakpoint, or null, if an error occured.
     */
    public LocationBreakpoint create(Location loc, int flags, boolean enabled) throws EngineException {
        return create(loc, 1, null, 0, flags, enabled);
    }

    /**
     * Create a breakpoint. We synchronize so that
     * the callback from the engine which indicates that
     * the breakpoint was created won't cause a race condition.
     * @param loc
     * @param hitCount
     * @param condition
     * @param tid
     * @param flags
     * @return the new breakpoint.
     * @throws EngineException
     */
    public synchronized LocationBreakpoint create(Location loc, int hitCount, String condition,
            int tid, int flags, boolean enabled) throws EngineException {
        //
        // This call may be from a listener that is responding to a
        // breakpoint-creation event that this object originated!
        // So, if we already have a breakpoint for this location, just
        // return it.
        for (Breakpoint bp: mBreakpoints){
            if (bp instanceof LocationBreakpoint &&
                    ((LocationBreakpoint)bp).getLocation().equals(loc)) {
                if (bp.isEnabled() != enabled){
                    bp.setEnabled(enabled);
                }
                return (LocationBreakpoint)bp;
            }
        }
        boolean createEnabled = enabled;
        if (!createEnabled && !mEngine.canCreateDisabledActionPoints()){
            createEnabled = true;
        }
        // A really new breakpoint
        int id = mEngine.createBreakpoint(loc, hitCount, condition, tid, flags,createEnabled);
        if (id != 0) {
            LocationBreakpoint bp = addBreakpoint(id, loc, hitCount, condition, tid, flags,createEnabled,
                !mEngine.doesEngineFireBreakpointCreationEventsWhenCreateBreakpointInvoked());
            bp.setEnabled(enabled); // fires event if to be created disabled but engine is too backlevel
            return bp;
        }
        throw new EngineException("Couldn't make breakpoint: "+ loc);
    }
    
    /**
     * Add a newly created breakpoint, or respond to
     * a breakpoint that was implicitly created from the
     * engine.
     * <P>
     * Because the way the engine reports breakpoints,
     * we may get two creating notifications: once
     * when we created, and again when the engine 
     * notifies us of the creation. We distinguish the two
     * by the "define" argument.
     * @param id
     * @param loc
     * @param hitCount
     * @param condition
     * @param tid
     * @param flags
     * @param define if true, the engine has responded that it has set the breakpoint; if false,
     * we create a place-holder only.
     * @return the new breakpoint
     */
    private synchronized LocationBreakpoint addBreakpoint(int id, Location loc, int hitCount, String condition, int tid, int flags,
            boolean enabled, boolean define) {
        // When a breakpoint is created, this method is called twice. Once from "create()" method,
        // and once from the breakpoint-created observer. Either one could be first
        LocationBreakpoint bp = (LocationBreakpoint)getBreakpointFromID(id);
        if (bp == null) {
            // First time. "define" will be false, unless we're talking to an older
            //  debugger engine that doesn't fire bkpt-create event as side-effect.
            bp = allocateBreakpoint(loc, hitCount, condition, tid,
                id, flags, enabled);
            mBreakpoints.add(bp);
        }
        else if (define) {
            int oldFlags = bp.getFlags();
            bp.setFlags(flags);
            if (!mEngine.doesEngineFireBreakpointCreationEventsWhenCreateBreakpointInvoked()) {
                define = false; // already created, but attributes may be different.
                if (oldFlags != flags){
                    fireEvent(bp,STATE);
                }
            }
        }
        if (define)
            fireEvent(bp, CREATED);
        return bp;
    }

    /**
     * Create a watchpoint.
     * @param var the variable being watched.
     * @param length number of bytes to watch, or 0 if
     * the natural size of the variable is to be used.
     * @param condition if not null, a condition to guard
     * this watchpoint.
     * @param tid if not zero, then watchpoint is to be
     * tied to a thread.
     * @param flags One of {@link IEngineAPI#BP_HARDWARE BP_HARDWARE} or
     * {@link IEngineAPI#BP_REGULAR BP_REGULAR}.
     * @param frame the stackframe context into which
     * the variable is to be evaluated, or <code>null</code>
     * if the variable is not tied to a stackframe.
     * @param enabled if false, then create in disabled state.
     * @return watchpoint ID.
     * @throws EngineException couldn't make the watchpoint.
     */
    public Watchpoint create(String var, int length,  String condition,
        int tid, int flags, StackFrameRef frame, boolean enabled) throws EngineException, EvaluationException {
        return create(var,length,condition,tid,flags,frame,enabled,null);
    }
    /**
     * Create a watchpoint.
     * @param var the variable being watched.
     * @param length number of bytes to watch, or 0 if
     * the natural size of the variable is to be used.
     * @param condition if not null, a condition to guard
     * this watchpoint.
     * @param tid if not zero, then watchpoint is to be
     * tied to a thread.
     * @param flags One of {@link IEngineAPI#BP_HARDWARE BP_HARDWARE} or
     * {@link IEngineAPI#BP_REGULAR BP_REGULAR}.
     * @param frame the stackframe context into which
     * the variable is to be evaluated, or <code>null</code>
     * if the variable is not tied to a stackframe.
     * @param enabled if false then watchpoint is to be created in a disabled state.
     * @param attributes esoteric attributes (e.g., "mask=0x...")
     * @return watchpoint ID.
     * @throws EngineException couldn't make the watchpoint.
     */
    public Watchpoint create(String var, int length, String condition,
            int tid, int flags, StackFrameRef frame, 
            boolean enabled,
            String[] attributes) throws EngineException, EvaluationException {
        
        int frameID = frame != null?frame.getFrameID():0;
        boolean createEnabled = enabled;
        if (!createEnabled){
            if (!mEngine.canCreateDisabledActionPoints()){
                createEnabled = true;
            }
        }
        int id = mEngine.createWatchpoint(var, length, condition, tid, flags,frameID,createEnabled,attributes);
        if (id != 0) {
            Watchpoint wp = addWatchpoint(id,var, length, condition, tid, flags,createEnabled,attributes);
            wp.setEnabled(enabled); // fires event if to be created disabled but engine is too backlevel
            return wp;
        }
        mEngine.evaluationError();
        // shouldn't get here
        throw new EngineException("Couldn't make watchpoint: " + var);
    }

    /**
     * Add a newly-defined watchpoint to the table.
     * @param breakID the new ID of the watchpoint.
     * @param var the watchpoint expression.
     * @param length
     * @param condition
     * @param tid
     * @param flags
     * @param enabled if false, then watchpoint was created in disabled state.
     * @param attributes esoteric attributes such as "mask=0x..."
     * @return new watchpoint
     */
    private Watchpoint addWatchpoint (
        int breakID,
        String var,
        int length,
        String condition,
        int tid,
        int flags,
        boolean enabled,
        String[] attributes) {
        // cr21363: may be redefining an existing breakpoint. Check for this and allow it.
        Breakpoint bp = this.getBreakpointFromID(breakID);
        if (bp != null && bp instanceof Watchpoint) {
            try {
                bp.setCondition(condition);
            }
            catch (EngineException e) {
                //Will be diagnosed later
            }
            return (Watchpoint) bp;
        }
        else {
            Watchpoint wp = allocateWatchpoint(var, length, condition, tid, breakID, flags, enabled, attributes);
            mBreakpoints.add(wp);
            fireEvent(wp, CREATED);
            return wp;
        }
    }

    /**
     * This class actually allocates the breakpoint object. It can be overridden
     * if the breakpoint object must confirm to some other interface (as is
     * required by Eclipse/CDT)
     * 
     * @param loc
     *            the location of the breakpoint.
     * @param hitCount
     *            the number of hits before the breakpoint takes affect.
     * @param condition
     *            the condition if conditional; otherwise null.
     * @param tid
     *            if thread-specific, the thread ID; or 0.
     * @param id
     *            the ID number that the engine know this breakpoint by.
     * @param enabled
     *            indicates whether or not the breakpoint is enabled or disabled.
     * @return the new breakpoint object.
     */
    protected LocationBreakpoint allocateBreakpoint(Location loc, int hitCount,
            String condition, int tid, int id, int flags, boolean enabled) {
        LocationBreakpoint bp = new LocationBreakpoint(mEngine, loc, hitCount, condition, tid,
                id, flags, enabled);
        return bp;
    }
    
    /**
     * This class actually allocates the breakpoint object. It can be overridden
     * if the breakpoint object must confirm to some other interface (as is
     * required by Eclipse/CDT)
     * 
     * @param var
     *            the variable being watched.
     * @param length
     *            the number of bytes to watch; or 0 if the
     * 			  natural size of the variable is to be used.
     * @param condition
     *            the condition if conditional; otherwise null.
     * @param tid
     *            if thread-specific, the thread ID; or 0.
     * @param id
     *            the ID number that the engine know this breakpoint by.
     * @return the watchpoint object.
     */
    protected Watchpoint allocateWatchpoint(String var, int length, 
            String condition, int tid, int id, int flags,
            boolean enabled,
            String[] attributes) {
        Watchpoint bp = new Watchpoint(mEngine, var, length, condition, tid,
                id, flags, enabled, attributes);
        return bp;
    }

    public synchronized void remove(Breakpoint p) throws EngineException {
        //It may have already been removed and this call
        // is from the breakpoint-destroyed event of CDI.
        if (!p.isRemoved()) {
            removeIt(p);
            assert p.isRemoved();
        }

    }

    /**
     * @param p
     */
    private void removeIt(Breakpoint p) throws EngineException {
        if (!p.isRemoved()){
            mBreakpoints.remove(p);
            try {
                // Don't re-delete if the engine has already issued a breakpoint-delete event.
                if (!p.isDeletedByEngine())
                    mEngine.removeBreakpoint(p.getBreakID());
            }
            catch (EngineDisconnectedException e) {
                //We've noticed that we can be removing breakpoints after the engine has
                // terminated. Just ignore it.
            }
            p.setRemoved();
            fireEvent(p, REMOVED);
        }
    }

    public synchronized void removeAll() throws EngineException {
        Breakpoint[] bps = getBreakpoints();
        for (int i = 0; i < bps.length; i++) {
            removeIt(bps[i]);
        }
    }

    	/**
    	 * Return all break- and watchpoints.
    	 * @return all break- and watchpoints.
    	 */
    public Breakpoint[] getBreakpoints() {
        Breakpoint b[] = mBreakpoints.toArray(new Breakpoint[mBreakpoints.size()]);
        // Sort by breakID:
        Arrays.sort(b,new Comparator<Breakpoint>(){

            @Override
            public int compare (Breakpoint o1, Breakpoint o2) {
                return o1.getBreakID() - o2.getBreakID();
            }});
        return b;
    }
    
    /**
     * Return those breakpoints that are data watchpoints.
     */
    public Watchpoint[] getWatchpoints(){
        ArrayList<Watchpoint> list = new ArrayList<Watchpoint>();
        Breakpoint all[] = getBreakpoints();
        for (int i = 0; i < all.length; i++){
            if (all[i] instanceof Watchpoint)
                list.add((Watchpoint)all[i]);
        }
        return  list.toArray(new Watchpoint[list.size()]);    
    }
    
    /**
     * Return those breakpoints that are location breakpoints.
     */
    public LocationBreakpoint[] getLocationBreakpoints(){
        ArrayList<LocationBreakpoint> list = new ArrayList<LocationBreakpoint>();
        Breakpoint all[] = getBreakpoints();
        for (int i = 0; i < all.length; i++){
            if (all[i] instanceof LocationBreakpoint)
                list.add((LocationBreakpoint)all[i]);
        }
        return list.toArray(new LocationBreakpoint[list.size()]);    
    }

    /**
     * Return the breakpoint corresponding to an ID.
     * 
     * @param breakID
     *            the id number of the breakpoint being sought.
     * @return the breakpoint corresponding to the id number or null if not
     *         existing.
     */
    public Breakpoint getBreakpointFromID(int breakID) {
        for (int i = 0; i < mBreakpoints.size(); i++) {
            Breakpoint p =  mBreakpoints.get(i);
            if (p.getBreakID() == breakID) return p;
        }
        return null;

    }

    //    /**
    //     * Return the breakpoint corresponding to an ID.
    //     * @param index the index of the breakpoint being sought.
    //     * @return the breakpoint corresponding to the id number or null
    //     * if not existing.
    //     */
    //    public Breakpoint getBreakpointFromIndex(int index){
    //        for (int i = 0; i < mBreakpoints.size(); i++){
    //            Breakpoint p = (Breakpoint)mBreakpoints.get(i);
    //            if (p.getIndex() == index) return p;
    //        }
    //        return null;
    //        
    //    }

    /**
     * Remove breakpoint given its ID.
     * 
     * @param breakID
     *            breakpoint ID to remove.
     * @return true if breakpoint successfully removed; false if bogus break id.
     */
    public boolean removeID(int breakID) throws EngineException {
        Breakpoint p = getBreakpointFromID(breakID);
        if (p != null) {
            remove(p);
            return true;
        }
        return false;
    }
    
    /**
     * When we restart the program, the engine doesn't handle
     * things very well if the program changed.
     * So, we call this method to re-apply the breakpoints that
     * the engine may have dropped.
     * <p>
     * <b>Note:</b> may become unnecessary after engine is overhauled.
     * @throws EngineException
     */
    void reapply() throws EngineException {
        Breakpoint[] breakpoints = getBreakpoints();
        for (int i = 0; i < breakpoints.length; i++){
            Breakpoint bp = breakpoints[i];
            if (!mEngine.isValidBreakpoint(bp.getBreakID())){
                if (!bp.reapply()){
                    //could not be regenerated
                    if (mRemoveOnRemoveEvent){
                        remove(bp);
                    }
                    fireEvent(bp,REMOVED);
                }
            }
        }
    }

    /**
     * Resync with the engine by removing any breakpoint that the engine doesn't
     * know about.
     */
    public void resync() throws EngineException {
        // Remove any breakpoints that the engine no longer has.
        Breakpoint[] breakpoints = getBreakpoints();
        for (int i = 0; i < breakpoints.length; i++){
            Breakpoint bp = breakpoints[i];
            if (!mEngine.isValidBreakpoint(bp.getBreakID())) {
                if (mRemoveOnRemoveEvent){
                    remove(bp);
                }//Otherwise, an event handler will remove it.
                fireEvent(bp, REMOVED);
            }
        }
    }

    //    /**
    //     * Remove breakpoint given its index.
    //     * @param index breakpoint ID to remove.
    //     * @return true if breakpoint successfully removed; false if
    //     * bogus break id.
    //     */
    //    public boolean removeIndex(int index){
    //        Breakpoint p = getBreakpointFromIndex(index);
    //        if (p != null) {
    //            remove(p);
    //            return true;
    //        }
    //        return false;
    //    }

}
