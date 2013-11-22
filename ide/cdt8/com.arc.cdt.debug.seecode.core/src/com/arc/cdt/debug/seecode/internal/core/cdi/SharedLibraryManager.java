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
import org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary;

import com.arc.cdt.debug.seecode.internal.core.cdi.event.CreatedEvent;
import com.arc.cdt.debug.seecode.internal.core.cdi.event.DestroyedEvent;

/**
 * Manages the shared library that are displayed.
 * 
 * @author David Pickens
 */
class SharedLibraryManager extends Manager  {

    private List<SharedLibrary> mList = new ArrayList<SharedLibrary>();

    /**
     * @param target the associated target.
     */
    public SharedLibraryManager(Target target) {
        super(target, false);
    }

    void addLibrary(int moduleID) {
        // When entering the main module, the engine
        // may, or may have not, trigger new-module event.
        // So check is needed.
        if (lookupModule(moduleID) == null) {

            SharedLibrary library = new SharedLibrary(getTarget(), moduleID);
            mList.add(library);
            EventManager emgr = (EventManager) getSession().getEventManager();
            emgr.enqueueEvent(new CreatedEvent(library));
        }
    }

    SharedLibrary lookupModule(int moduleID) {
        for (SharedLibrary lib: mList){
            if (lib.getModuleID() == moduleID) { return lib; }
        }
        return null;
    }

    void clear() {
        Iterator<SharedLibrary> each = mList.iterator();
        while (each.hasNext()) {
            SharedLibrary lib = each.next();
            each.remove();
            EventManager emgr = (EventManager) getSession().getEventManager();
            emgr.enqueueEvent(new DestroyedEvent(lib));
        }
    }

    boolean removeLibrary(int moduleID) {
        Iterator<SharedLibrary> each = mList.iterator();
        while (each.hasNext()) {
            SharedLibrary lib = each.next();
            if (lib.getModuleID() == moduleID) {
                each.remove();
                EventManager emgr = (EventManager) getSession()
                        .getEventManager();
                emgr.enqueueEvent(new DestroyedEvent(lib));
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryManager#getSharedLibraries()
     */
    public ICDISharedLibrary[] getSharedLibraries() {
        return  mList.toArray(new ICDISharedLibrary[mList.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryManager#loadSymbols(org.eclipse.cdt.debug.core.cdi.model.ICDISharedLibrary[])
     */
    public void loadSymbols(ICDISharedLibrary[] libs) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryManager#loadSymbols()
     */
    public void loadSymbols() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryManager#getSharedLibraryPaths()
     */
    public String[] getSharedLibraryPaths() {
        return new String[0];
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryManager#setSharedLibraryPaths(java.lang.String[])
     */
    public void setSharedLibraryPaths(String[] libpaths) throws CDIException {
        throw new CDIException("SharedLibraryPaths not supported");
    }
}
