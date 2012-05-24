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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.RuntimeProcess;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.cdt.debug.seecode.internal.core.cdi.SeeCodeEngineProcess;

/**
 * This class is used to implement the <code>IProcess</code> object for the
 * SeeCode engine process.
 * <P>
 * It is materialized from {@link ProcessFactory}, which implements an
 * extension to <code>org.eclipse.debug.core.processFactories</code>.
 * <P>
 * We had to override the {@link #terminate()}method because the default
 * version kills off the engine process before it can be safely stutdown.
 * Bummer.
 * 
 * @author David Pickens
 */
class SeeCodeProcess extends RuntimeProcess {

    private Thread mWaitThread = null;

    /**
     * @param launch
     * @param process
     * @param name
     * @param attributes
     */
    public SeeCodeProcess(ILaunch launch, Process process, String name,
            Map<String,String> attributes) {
        super(launch, process, name, attributes);
        if (!(process instanceof SeeCodeEngineProcess))
                throw new IllegalArgumentException(
                        "Only handles MetaWare Debugger processes");
    }

    protected void killIt() throws DebugException {
        super.terminate();
    }

    /**
     * This is the method that we must override and is responsible for us having
     * to supply our own subclass.
     * <P>
     * The default implementation kills the engine process outright without
     * giving us a chance to shut it down.
     * 
     * @see org.eclipse.debug.core.model.ITerminate#terminate()
     */
    @Override
    public void terminate() throws DebugException {
        final SeeCodeEngineProcess sp = (SeeCodeEngineProcess) this
                .getSystemProcess();
        // the system process will be null if it already terminated.
        if (sp != null && !sp.isShutdown()) {
            sp.shutdown();
            if (mWaitThread == null) {
                Runnable r = new Runnable() {

                    @Override
                    public void run() {
                        if (!sp.waitForProcessTermination(30 * 1000)) {
                            SeeCodePlugin
                                    .log("Engine didn't shutdown in reasonable time; it will be forced");
                        }
                        try {
                            killIt();
                        } catch (DebugException e) {
                            SeeCodePlugin.log(e);
                        }
                        mWaitThread = null;
                    }
                };
                mWaitThread = new Thread(r, "WaitForShutdown");
                mWaitThread.start();
            }
        } else
            killIt();
    }

	@SuppressWarnings({ "rawtypes" })
	@Override
	public Object getAdapter(Class adapter) {
		Process p = getSystemProcess();
		if (p instanceof IAdaptable){
		    Object result = ((IAdaptable)p).getAdapter(adapter);
		    if (result != null) return result;
		}
		return super.getAdapter(adapter);
	}
}
