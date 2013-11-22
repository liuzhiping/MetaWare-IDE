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
package com.arc.cdt.debug.seecode.internal.ui.action;

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget3;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;


public class RefreshActionDelegate extends AbstractDebugActionDelegate {

    private ICDITarget3 getTargetFromSelection(Object element){
        IDebugTarget target = null;
        if (element instanceof IDebugTarget){
            target = (IDebugTarget)element;
        }
        if (target == null){
            if (element instanceof IThread){
                target = ((IThread)element).getDebugTarget();
            }
        }
        if (target == null){
            if (element instanceof IStackFrame){
                target = ((IStackFrame)element).getDebugTarget();
            }
        }
        if (target == null){
            ILaunch launch = null;
            if (element instanceof ILaunch){
                 launch = (ILaunch)element;            
            }
            else if (element instanceof IAdaptable){
                launch = (ILaunch)((IAdaptable)element).getAdapter(ILaunch.class);
            }
            if (launch != null){
                target = launch.getDebugTarget();
            }
        }
        if (target != null){
            ICDITarget cdiTarget = (ICDITarget)target.getAdapter(ICDITarget.class);
            if (cdiTarget instanceof ICDITarget3)
                return (ICDITarget3)cdiTarget;
        }
        return null;
    }
    @Override
    protected boolean isEnabledFor(Object element) {
        return getTargetFromSelection(element) != null;
    }

    @Override
    protected boolean isRunInBackground() {
        return false;
    }

    @Override
    protected void doAction (Object element) throws DebugException {
        ICDITarget3 target = getTargetFromSelection(element);
        if (target != null && !target.isTerminated()){
            target.refreshViews();
            getAction().setEnabled(true);
        }   
    }

}
