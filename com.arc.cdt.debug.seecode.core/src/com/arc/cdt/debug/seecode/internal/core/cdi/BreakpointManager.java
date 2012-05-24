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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIAddressLocation;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.event.ICDIDestroyedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDISuspendedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocationBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.cdt.debug.seecode.internal.core.cdi.event.ChangedEvent;
import com.arc.cdt.debug.seecode.internal.core.cdi.event.CreatedEvent;
import com.arc.cdt.debug.seecode.internal.core.cdi.event.DestroyedEvent;
import com.arc.seecode.engine.Breakpoint;
import com.arc.seecode.engine.EngineDisconnectedException;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;
import com.arc.seecode.engine.EvaluationException;
import com.arc.seecode.engine.IEngineAPI;
import com.arc.seecode.engine.Location;
import com.arc.seecode.engine.LocationBreakpoint;
import com.arc.seecode.engine.StackFrameRef;
import com.arc.seecode.engine.Watchpoint;

/**
 * @author David Pickens
 */
class BreakpointManager extends Manager  {

    private EngineInterface mEngine;

    // The existing breakpoint manager that we wrap.
    private com.arc.seecode.engine.BreakpointManager mMgr;

    private List<CDIBreakpoint> mBreakpoints = new ArrayList<CDIBreakpoint>();

    private EventManager mEventMgr;
    
    private List<Watchpoint> fPendingWatchpoints = new ArrayList<Watchpoint>(3);
    private boolean fStartupCompleted = false;

    /**
     *  
     */
    public BreakpointManager(Target target, EventManager emgr) {
        super(target, true);
        mEventMgr = emgr;
        //emgr.addEventListener(this);  // caller does this
        mEngine = target.getEngineInterface();
        try {
            mMgr = mEngine.getBreakpointManager();
            // Tell basic breakpoint manager not to
            // delete breakpoint when engine fires breakpointRemoved
            // event. Instead, we do it ourselves to so that
            // we don't delete a temp breakpoint for the
            // stop is processed.
            mMgr.setRemoveOnRemoveEvent(false);
        } catch (EngineException e1) {
            SeeCodePlugin.log(e1);
        }
        mMgr
                .addObserver(new com.arc.seecode.engine.BreakpointManager.IObserver() {

                    @Override
                    public void onRemoved(Breakpoint bp) {
                        ICDIBreakpoint ibp = findBreakpoint(bp);
                        if (ibp != null) {
                            // to make sure we don't delete
                            // a temporary breakpoint before the
                            // corresponding stop event, we must
                            // enqueue the delete event, instead
                            // of removing outright.
                            // It is served by handleDestroyedEvent(ICDIBreakpoint}.
                            fireDestroyedEvent(ibp);
                        }
                    }

                    @Override
                    public void onCreated(Breakpoint bp) {
                        if (bp instanceof LocationBreakpoint){
                            fireBreakpointCreated((LocationBreakpoint)bp);
                        }
                        else {
                            //KLOODGE: cr21363 if the user's startup file (.scrc) contains
                            // watch command (e.g., "watch 0, len 4"), then it will
                            // be created prior to CDT restoring watchpoints. We will
                            // end up with duplicates if we don't delay the .scrc
                            // watchpoints from being recognized after
                            // startup. We already have code to prevent creation
                            // of duplicate breakpoints, but duplicate watchpoints
                            // are difficult to catch elsewhere because they depend
                            // on state of stackframe.
                            if (fStartupCompleted)
                                fireWatchpointCreated((Watchpoint)bp);
                            else {
                                synchronized(fPendingWatchpoints) {
                                    fPendingWatchpoints.add((Watchpoint)bp);
                                }
                            }
                        }
                    }

                    @Override
                    public void onStateChange(Breakpoint bp) {
                        ICDIBreakpoint ibp = findBreakpoint(bp);
                        if (ibp != null) {
                            mEventMgr.enqueueEvent(new ChangedEvent(ibp));
                        }
                    }

                    @Override
                    public void onConditionChange(Breakpoint bp) {
                        ICDIBreakpoint ibp = findBreakpoint(bp);
                        if (ibp != null) {
                            mEventMgr.enqueueEvent(new ChangedEvent(ibp));
                        }
                    }

                    @Override
                    public void onHit (Breakpoint bp) {
                        // The breakpoint hit event is enqueued by
                        // the "processStopped()" method of EngineObserver.
                        // Basically, the command-line processor overrides this
                        // to handle the "exec" command associated with a breakpoint.
                    }
                });
    }

    /* override */
    public ICDIBreakpoint[] getBreakpoints () {
        synchronized (mBreakpoints) {
            return mBreakpoints.toArray(new ICDIBreakpoint[mBreakpoints.size()]);
        }
    }
    
    void fireEarlyWatchpoints(){
        synchronized(fPendingWatchpoints){
            for (Watchpoint wp: fPendingWatchpoints){
                fireWatchpointCreated(wp);
            }
            fPendingWatchpoints.clear();
        }
    }
    
    //Called at end of Target.start() to indicate that we can now handle watchpoint creation
    // events that are implicit from user's .scrc file.
    void setStartupCompleted(){
        fStartupCompleted = true;
    }

    /* override */
    public void deleteBreakpoint(ICDIBreakpoint bp) throws CDIException {
        removeBreakpoint(bp);
        fireDestroyedEvent(bp);
    }

    /**
     * @param bp
     */
    private void fireDestroyedEvent(ICDIBreakpoint bp) {
        mEventMgr.enqueueEvent(new DestroyedEvent(bp));
    }

    /**
     * Remove a breakpoint, but don't leave it to the
     * caller to fire the {@link DestroyedEvent}.
     * @param bp
     * @throws CDIException
     */
    private void removeBreakpoint(ICDIBreakpoint bp) throws CDIException {
        Breakpoint scbp = ((CDIBreakpoint)bp).getSeeCodeBreakpoint();
        synchronized(mBreakpoints){
            mBreakpoints.remove(bp);
        }
        try {
            mMgr.remove(scbp);
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        }
    }

    /* override */
    public void deleteBreakpoints(ICDIBreakpoint[] bps) throws CDIException {
        for (int i = 0; i < bps.length; i++) {
            deleteBreakpoint(bps[i]);
        }
    }

    /* override */
    public void deleteAllBreakpoints() throws CDIException {
        ICDIBreakpoint bp[] = getBreakpoints();
        try {
            mMgr.removeAll();
        } catch (EngineDisconnectedException x){
            // The engine shudown while the GUI is
            // trying to cleanup breakpoints. Don't complain because
            // it is a timing issue.
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        }
        synchronized(mBreakpoints){
            mBreakpoints.clear();
        }
        for (int i = 0; i < bp.length; i++) {
            mEventMgr.enqueueEvent(new DestroyedEvent(bp[i]));
        }
    }

    /* override */
    private LocationBreakpoint setBreakpoint(int type,
            ICDILocation location, ICDICondition condition, 
            boolean deferred, boolean enabled) throws CDIException {
        Location loc = ((CDILocation) location).getLocation();
        if (!loc.isValid()){
            // If we supported deferred breakpoints, this would be deferred.
            throw new CDIException("Breakpoint target not found.");
        }
        String cond = null;
        int hitCount = 0;
        if (condition != null) {
            cond = condition.getExpression();
            if (cond != null && cond.trim().length() == 0)
                cond = null; // no empty conditionls
            hitCount = condition.getIgnoreCount();
        }
        int flags = type; // the flags match!
//        assert ICBreakpointType.HARDWARE == IEngineAPI.BP_HARDWARE
//                && ICBreakpointType.REGULAR == IEngineAPI.BP_REGULAR
//                && ICBreakpointType.TEMPORARY == IEngineAPI.BP_TEMPORARY;
        // CDT currently doesn't handle thread-specific breakpoints!
        LocationBreakpoint bp;
        try {
            bp = mMgr.create(loc, hitCount, cond, /* tid */0, flags,enabled);
        } catch (EngineException e) {
            // May be shutting down debugger before it finished initializing.
            if (!(e instanceof EngineDisconnectedException))
                SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        }
        return bp;       
    }
        
    public ICDILocationBreakpoint setLineBreakpoint(int type,
                ICDILineLocation location, ICDICondition condition, 
                boolean deferred, boolean enabled) throws CDIException {
        LocationBreakpoint bp = setBreakpoint(type,location,condition,deferred, enabled);
        synchronized (mBreakpoints) {
            CDIBreakpoint cdiBP = findBreakpoint(bp);
            if (cdiBP == null) {
                cdiBP = new CDILineBreakpoint(getTarget(),bp);
                mBreakpoints.add(cdiBP);
            }
            return (ICDILocationBreakpoint)cdiBP;
        }
    }

    public ICDILocationBreakpoint setFunctionBreakpoint (
        int type,
        ICDIFunctionLocation location,
        ICDICondition condition,
        boolean deferred,
        boolean enabled) throws CDIException {
        LocationBreakpoint bp = setBreakpoint(type, location, condition, deferred,enabled);
        synchronized (mBreakpoints) {
            CDIBreakpoint cdiBP = findBreakpoint(bp);
            if (cdiBP == null) {
                cdiBP = new CDIFunctionBreakpoint(getTarget(), bp);
                mBreakpoints.add(cdiBP);
            }
            return (ICDILocationBreakpoint) cdiBP;
        }
    }
    
    public ICDILocationBreakpoint setAddressBreakpoint (
        int type,
        ICDIAddressLocation location,
        ICDICondition condition,
        boolean deferred,
        boolean enabled) throws CDIException {
        LocationBreakpoint bp = setBreakpoint(type, location, condition, deferred,enabled);
        synchronized (mBreakpoints) {
            CDIBreakpoint cdiBP = findBreakpoint(bp);
            if (cdiBP == null) {
                cdiBP = new CDIAddressBreakpoint(getTarget(), bp);
                mBreakpoints.add(cdiBP);
            }
            else if (cdiBP.isEnabled() != enabled){
                cdiBP.setEnabled(enabled);
            }
            return (ICDILocationBreakpoint) cdiBP;
        }
    }

    private void fireBreakpointCreated(LocationBreakpoint bp) {
        CDIBreakpoint cdiBP;
        synchronized (mBreakpoints) {            
            cdiBP = findBreakpoint(bp);
            if (cdiBP == null) {
                Location location = bp.getLocation();
                if (location.getSource() != null){
                     cdiBP = new CDILineBreakpoint(getTarget(), bp);
                }
                else if (location.getFunction() != null){
                     cdiBP = new CDIFunctionBreakpoint(getTarget(), bp);
                }
                else cdiBP = new CDIAddressBreakpoint(getTarget(),bp);
                mBreakpoints.add(cdiBP);
            }
        }
        if (!cdiBP.isCreated()) {
            cdiBP.setCreated(true);
            mEventMgr.enqueueEvent(new CreatedEvent(cdiBP));
        }
    }
    
    private void fireWatchpointCreated(Watchpoint bp) {
        CDIBreakpoint cdiBP;
        synchronized (mBreakpoints) {            
            cdiBP = findBreakpoint(bp);
            if (cdiBP == null) {
                cdiBP = new CDIWatchpoint(getTarget(), bp);
                mBreakpoints.add(cdiBP);
            }
        }
        if (!cdiBP.isCreated()) {
            cdiBP.setCreated(true);
            mEventMgr.enqueueEvent(new CreatedEvent(cdiBP));
        }
    }

    /**
     * Given a seecode engine breakpoint object, return the CDI breakpoint that
     * wraps it, or null.
     * 
     * @param bp
     *            the seecode breakpoint object.
     * @return the corresponding CDI breakpoint object, or null.
     */
    CDIBreakpoint findBreakpoint(Breakpoint bp) {
        synchronized (mBreakpoints) {
            for (CDIBreakpoint cdiBP: mBreakpoints) {
                if (cdiBP.getSeeCodeBreakpoint() == bp)
                    return cdiBP;
            }
        }
        return null;
    }

    /**
     * Given an engine breakpoint id, return the corresponding CDI breakpoint
     * wrapper, or null.
     * <P>
     * This is called when the engine fires a
     * {@linkplain EngineObserver#processStopped(EngineInterface) breakpoint stop event}.
     * 
     * @param breakID
     *            the breakpoint id.
     * @return corresponding CDI breakpoint or null.
     */
    ICDIBreakpoint findBreakpoint(int breakID) {
        Breakpoint bp = mMgr.getBreakpointFromID(breakID);
        if (bp != null) return findBreakpoint(bp);
        return null;
    }

	/**
	 * Sets a watchpoint for the given expression.
	 * @param type - a combination of TEMPORARY and HARDWARE or 0
	 * @param watchType - a combination of READ and WRITE
	 * @param expression - the expression to watch
	 * @param condition - the condition or <code>null</code>
	 * @param enabled - if false, create it disabled.
	 * @return a watchpoint
	 * @throws CDIException on failure. Reasons include:
	 */
	public ICDIWatchpoint setWatchpoint(
		int type,
		int watchType,
		String expression,
		int length,
		ICDICondition condition,
		boolean enabled)
		throws CDIException{

        String cond = null;
        if (condition != null) {
            cond = condition.getExpression();
            if (cond != null && cond.trim().length() == 0)
                cond = null; // no empty conditionls
        }
        int flags = 0;
        if ((type & ICBreakpointType.HARDWARE) != 0){
            flags |= IEngineAPI.WP_HARDWARE;
        }
        if ((type & ICBreakpointType.TEMPORARY) != 0){
            flags |= IEngineAPI.BP_TEMPORARY;
        }
        if ((watchType & ICDIWatchpoint.READ) != 0){
            flags |= IEngineAPI.WP_READ;
            if ((watchType & ICDIWatchpoint.WRITE) != 0){
                flags |= IEngineAPI.WP_WRITE;
            }
        }
        else flags |= IEngineAPI.WP_WRITE;
        // CDT currently doesn't handle thread-specific breakpoints!
        Watchpoint bp;
        try {
            StackFrame sf = getTarget().getThread().getTopFrame();
            StackFrameRef sfr = sf==null?null:sf.getSeeCodeStackFrame();
            bp = mMgr.create(expression, length, cond, /* tid */0, flags,sfr,enabled);
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        } catch (EvaluationException e) {
            throw new CDIException(e.getMessage());
        } 
        // new breakpoint observer presumably set it!
        synchronized (mBreakpoints) {
            CDIBreakpoint cdiBP = findBreakpoint(bp);
            if (cdiBP == null) {
                cdiBP =  new CDIWatchpoint(getTarget(), bp);
                mBreakpoints.add(cdiBP);
            }
            return (ICDIWatchpoint)cdiBP;
        }
    }

//    /* override */
//    public ICDICatchpoint setCatchpoint(int arg0, ICDICatchEvent arg1,
//            String arg2, ICDICondition arg3) throws CDIException {
//        throw new CDIException("Don't support catch points");
//    }

    /* override */
    public void allowProgramInterruption(boolean v) {
        // ??? what to do here?
    }

    /* override */
    public ICDICondition createCondition(int ignoreCount, String expression, String threadIDs[]) {
        return new Condition(ignoreCount, expression, threadIDs);
    }
    
    private Location findLocation(String file, String function, int line, int byteOffset) throws EngineDisconnectedException {
        Location loc = null;
        try {
            //We sometimes get source with 0 line number. Don't know how, but it has happened.
            if (file != null && file.length() > 0 && (line > 0 || line == 0 && function == null)) {
                loc = mEngine.lookupSource(file, line);
                if (byteOffset > 0 && loc != null) {
                    loc = loc.clone(); // Must make copy because original may be used as table key.
                    loc.setSourceLine(loc.getSourceLine(),byteOffset);
                    loc.setAddress(loc.getAddress() + byteOffset);
                }
            } else if (function != null) {
                String s = function;
                if (byteOffset != 0){
                    s += "+"+byteOffset;
                }
                loc = mEngine.evaluateLocation(s, 0);
               
            }
        }
        catch (EngineDisconnectedException x){
            // Debugger being shut down before initialization complete (?)
            // Don't complain.
            throw x;
        }
        catch (EngineException e) {
            SeeCodePlugin.log(e);
        }
        catch (EvaluationException e) {  
        } 
        return loc;
    }

    /**
     * Create a CDI location given a file/line/byteoffset, or function/byteoffset
     * @param file the source file or null.
     * @param function the name of the function, or null.
     * @param line the line number if > 0
     * @param byteOffset byte offset from the line number or function.
     * @return the new CDI location.
     */
    public ICDILocation createLocation (String file, String function, int line, int byteOffset) {
        try {
            Location loc = findLocation(file, function, line, byteOffset);
            if (loc == null) {
                // The source/line or function was not found.
                // CR92083: the workspace may have moved with a
                // breakpoint set and the executable hasn't been
                // rebuilt. Convert the source to relative to
                // workspace and see if we can compute a result.
                // If that doesn't work, remove "../foo" from the prefix.
                // E.g. if "../library/foo.c" fails, try "library/foo.c", then "foo.c"
                if (new File(file).isAbsolute()) {
                    file = getTarget().computeRelativeSourcePath(file);
                    File f = new File(file);
                    if (!f.isAbsolute()) {
                        loc = findLocation(file, function, line, byteOffset);
                        if (loc == null && file.startsWith("..")) {
                            loc = findLocation(file.substring(3), function, line, byteOffset);
                            if (loc == null) {
                                int i = file.indexOf('/',3);
                                if (i < 0) i = file.indexOf('\\',3);
                                if (i >= 0) loc = findLocation(file.substring(i+1),function,line,byteOffset);
                            }
                        }
                    }
                }
            }

            if (loc == null) {
                // We may be invoked prior to the engine is completely initialized.
                loc = new Location();
                loc.setFunction(function, byteOffset);
                loc.setValid(false);
            }

            return new CDILocation(loc);
        }
        catch (EngineDisconnectedException x) {
            // Debugger being shut down before initialization complete (?)
            // Don't complain.
            return new CDILocation(0);
        }
    }

    /* override */
    public ICDILocation createLocation(long address) {
        Location loc;
        try {
            loc = mEngine.computeLocation(address);
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            return new CDILocation(address);
        }
        return new CDILocation(loc);
    }

    public void update () throws CDIException {
        try {
            // Just in case the engine somehow got out of sync
            // with us.
            mMgr.resync();
        }
        catch (EngineException e) {
            SeeCodePlugin.log(e);
            throw new CDIException(e.getMessage());
        }
        synchronized (mBreakpoints) {
            Iterator<CDIBreakpoint> each = mBreakpoints.iterator();
            while (each.hasNext()) {
                CDIBreakpoint bp = each.next();
                if (bp.getSeeCodeBreakpoint().isRemoved()) {
                    each.remove();
                }
            }
        }
    }
    
    /**
     * This method is called when a temporary breakpoint is
     * deleted by the engine after it is hit. The
     * {@link com.arc.seecode.engine.BreakpointManager.IObserver#onRemoved(Breakpoint)
     * onRemoved(Breakpoint)} method is called. We then enqueue
     * a {@Link DestroyedEvent} so that we don't delete it
     * before the {@link ICDISuspendedEvent} event is seen.
     * This method then serves that event.
     * @param e the event that indicates that a breakpoint
     * is being destroyed.
     */
    private void handleDestroyedEvent(ICDIDestroyedEvent e){
        ICDIBreakpoint bp = (ICDIBreakpoint)e.getSource();
        try {
            removeBreakpoint(bp);
        } catch (CDIException x) {
            SeeCodePlugin.log(x);
        }
        
    }
    /**
     * Look for breakpoint delete event and remove it.
     */
    @Override
    public void handleDebugEvents(ICDIEvent[] events) {    
        super.handleDebugEvents(events);
        for (int i = 0; i < events.length; i++){
            ICDIEvent e = events[i];
            if (e instanceof ICDIDestroyedEvent &&
                    e.getSource() instanceof ICDIBreakpoint &&
                    ((ICDIBreakpoint)e.getSource()).getTarget() == getTarget())
            {
                handleDestroyedEvent((ICDIDestroyedEvent)e);
            }
        }
    }
}
