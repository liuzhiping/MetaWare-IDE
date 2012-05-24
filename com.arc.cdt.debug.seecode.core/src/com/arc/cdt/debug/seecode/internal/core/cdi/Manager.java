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

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;



/**
 * @author David Pickens
 */
public abstract class Manager implements IUpdatable, ICDIEventListener {
    private Target fTarget;
    private boolean fAutoUpdate;
    /**
     * 
     */
    public Manager(Target target, boolean autoUpdate) {
        fTarget = target;
        fAutoUpdate = autoUpdate;
    }

    public void setAutoUpdate(boolean v) {
        fAutoUpdate = v;
    }

    @Override
    public boolean isAutoUpdate() {
        return fAutoUpdate;
    }

    @Override
    public void update(Target target) throws CDIException {
    }

    @Override
    public void handleDebugEvents(ICDIEvent[] events) {

    }

    public ICDISession getSession() {
        return fTarget.getSession();
    }

    /**
     * @return associated target object.
     */
    protected Target getTarget() {
        return fTarget;
    }

}
