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
package com.arc.cdt.debug.seecode.internal.core;

import java.util.Map;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.IProcessFactory;
import org.eclipse.debug.core.model.IProcess;

import com.arc.cdt.debug.seecode.internal.core.cdi.SeeCodeEngineProcess;


/**
 * This class implements the <code>org.eclipse.debug.core.processFactories</code>
 * extension so that we can define our own implementation
 * of <code>IProcess</code> for representing the SeeCode
 * engine process. The default one has the very undesirable
 * behavour of killing off the engine when a "terminate"
 * operation is performed before the we have a chance to
 * cleanly shut it down.
 * <p>
 * We also override the implementation of "RuntimeProcess"
 * because it has bugs that cause deadlocks if the engine
 * should abort: specifically, it invokes listeners
 * from a synchronized method. No other threads can
 * then access the "isTerminated()" method because it
 * is also synchronized! Bogus!
 * @author David Pickens
 */
public class ProcessFactory implements IProcessFactory {

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.IProcessFactory#newProcess(org.eclipse.debug.core.ILaunch, java.lang.Process, java.lang.String, java.util.Map)
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public IProcess newProcess(ILaunch launch, Process process, String label,
            Map attributes) {
        if (process instanceof SeeCodeEngineProcess)
            return new SeeCodeProcess(launch,process,label,attributes);
        return new ImprovedRuntimeProcess(launch,process,label,attributes);
    }

}
