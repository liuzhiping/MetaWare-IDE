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
package com.arc.cdt.debug.seecode.ui;

import java.io.File;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.debug.core.ICDIDebugger2;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.arc.cdt.debug.seecode.core.cdi.IEngineErrorLog;
import com.arc.cdt.debug.seecode.core.cdi.IEngineErrorLogContainer;
import com.arc.cdt.debug.seecode.ui.views.SeeCodeErrorLogView;
import com.arc.seecode.engine.EngineInterface;

/**
 * Logically, this class belongs in the seecode.core plugin. But to launch the seecode engine,
 * we require that UI callbacks be registered. 
 * <P>
 * Formerly (Eclipse 3.1 and earlier), we did have this class in seecode.core, and explicitly
 * started the UI plugin before launching. But as of Eclipse 3.2, this behavior had the unfortunate
 * side-effect of flagging this plugin as "persistent". It would be started during startup
 * immediately, instead of lazily. This, in turn, caused all dependent plugins to be started
 * immediately. Eventually, the org.eclipse.core.resource plugin was forced to start prior
 * to being prompted for the workspace. Thus, the default workspace would be used. (CR1892).
 * <P>
 * To get around this problem, we move CDebugger extension to this UI plugin and delegate things
 * to the seecode.core plugin. Thus, we guarantee that this plugin is started lazily.
 * <P>
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */

public class SeeCodeDebugger implements ICDIDebugger2{
    private ICDIDebugger2 _delegate;

    public SeeCodeDebugger(){
        _delegate = new com.arc.cdt.debug.seecode.core.SeeCodeDebugger();
    }

    @Override
    public ICDISession createSession (ILaunch launch, File executable, IProgressMonitor monitor) throws CoreException {
        ICDISession session = _delegate.createSession(launch,executable,monitor);
        // Arrange for the engine to write into the Debugger Error Log view.
        for (ICDITarget target: session.getTargets()) {
            IEngineErrorLogContainer lc = (IEngineErrorLogContainer)((IAdaptable)target).getAdapter(IEngineErrorLogContainer.class);
            if (lc != null) {
                lc.setErrorLog(new IEngineErrorLog(){
                    private SeeCodeErrorLogView view = null;
                    @Override
                    public void write (EngineInterface engine, String message) {
                        if (view == null) {
                            Display d = PlatformUI.getWorkbench().getDisplay();
                            d.syncExec(new Runnable(){

                                @Override
                                public void run () {
                                    try {
                                        view = (SeeCodeErrorLogView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(SeeCodeErrorLogView.VIEW_ID);
                                    }
                                    catch (PartInitException e) {
                                        UISeeCodePlugin.log(e);
                                    }
                                    
                                }});
                        }
                        view.write(engine,message);
                        
                    }});
            }
        }
        return session;
    }

    @Override
    public ICDISession createDebuggerSession (ILaunch launch, IBinaryObject exe, IProgressMonitor monitor) throws CoreException {
        throw new IllegalStateException("Obsolete method called");
    }

}
