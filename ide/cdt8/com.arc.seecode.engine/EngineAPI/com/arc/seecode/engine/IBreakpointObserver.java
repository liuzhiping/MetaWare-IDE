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
 * An observer for breakpoint state changes.
 * An instance of this is registered with the 
 * {@linkplain EngineInterface engine interface} by means of 
 * {@link EngineInterface#setBreakpointObserver(IBreakpointObserver)
 * setBreakpointObserver()}.
 * @author David Pickens
 */
public interface IBreakpointObserver {
    /**
     * Called when a breakpoint enabled state changes.
     * <P>
     * This method is called by the engine when a (visible) breakpoint
     * enable-state changes -- even though it is usually the result
     * of a UI-specific action (e.g. {@link EngineInterface#setBreakpointEnabled(int,boolean)}).
     * Thus, the GUI management of this property can be completely
     * controlled here.
     * 
     * @param breakID the breakpoint ID.
     * @param enabled true if being enabled; false if being disabled.
     */
    void breakpointStateChanged(int breakID, boolean enabled);
    
    /**
     * Indicate that a breakpoint is being removed.
     * <P>
     * This method is called by the engine when a (visible) breakpoint
     * is removed -- even though it is usually the result
     * of a UI-specific action (e.g. {@link EngineInterface#removeBreakpoint(int)}).
     * Thus, the GUI breakpoint display can be easily updated from
     * this method.
     * <P>
     * After this call, the breakpoint ID is no longer valid.
     * @param breakID the breakpoint ID.
     */
    void breakpointRemoved(int breakID);
    
    /**
     * Indicate that a breakpoint's condition or
     * hit count or thread filters has been changed. 
     * @param breakID the breakpoint ID.
     */
    void breakpointConditionChanged(int breakID);
    
    /**
     * Indicate that a new breakpoint has been created
     * by the engine (non-temporary).
     * @param breakID the ID of the new breakpoint.
     * @param location the location of the breakpoint
     * @param ignoreCount  the number of hits before the
     * breakpoint takes.
     * @param conditional if not null, an expression to be evaluated
     * to true before breakpoint takes.
     * @param tid the thread ID if the breakpoint is to be tied to
     * a thread, or 0 if breakpoint is to be applied globally
     * @deprecated as of version 16. Replaced with @{link breakpointAdded2}.
     */
    @Deprecated
	void breakpointAdded(int breakID,Location location, int ignoreCount,
            String conditional, int tid);
    /**
     * Indicate that a new breakpoint has been created
     * by the engine.
     * @param breakID the ID of the new breakpoint.
     * @param location the location of the breakpoint
     * @param ignoreCount  the number of hits before the
     * breakpoint takes.
     * @param conditional if not null, an expression to be evaluated
     * to true before breakpoint takes.
     * @param tid the thread ID if the breakpoint is to be tied to
     * a thread, or 0 if breakpoint is to be applied globally
     * @param flags indicates the actual type of the breakpoint. See {@link IEngineAPI#createBreakpoint(Location, int, String, int, int)}.
     * @new
     */
    void breakpointAdded2(int breakID,Location location, int ignoreCount,
        String conditional, int tid, int flags, String attributes[]);

    /**
     * Indicate that a new watchpoint has been created by the engine.
     * @param breakID the ID of the new breakpoint (i.e., watchpoint)
     * @param var a string that denotes the location.
     * @param length number of consecutive bytes to watch.
     * @param flags related flags. See {@link IEngineAPI#createWatchpoint}.
     * @param condition the associated conditional expression, or <code>null</code>.
     * @param hitCount if > 1, the number of hits before the watchpoint takes.
     * <P>
     * @deprecated as of version 12. Replaced with @{link #watchpointAdded2}
     * @new
     */
    @Deprecated
	void watchpointAdded(int breakID, String var, int length, int hitCount,
            String condition,
            int flags);
    
    /**
     * Indicate that a new watchpoint has been created by the engine.
     * @param breakID the ID of the new breakpoint (i.e., watchpoint)
     * @param var a string that denotes the location.
     * @param length number of consecutive bytes to watch.    
     * @param condition the associated conditional expression, or <code>null</code>.
     * @param flags related flags. See {@link IEngineAPI#createWatchpoint}.
     * @param threadID the thread ID if the watchpoint is  tied to
     * a thread, or 0 if watchpoint is applied globally.
     * @param attributes if not null, esoteric attributes (e.g., "mask=0x...")
     * <P>
     * <B>NOTE:</B> this method as added in interface version 12 as a replacement
     * for {@link #watchpointAdded}.
     * @new
     */
    void watchpointAdded2(int breakID, String var, int length, 
        String condition,
        int flags, int threadID, String[] attributes);
}
