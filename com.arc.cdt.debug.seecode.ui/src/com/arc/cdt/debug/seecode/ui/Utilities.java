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
import java.net.URISyntaxException;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.service.datalocation.Location;

import com.arc.seecode.engine.EngineInterface;
import com.arc.seecode.engine.StackFrameRef;

/**
 * Miscellaneous utility methods required by various debugger UI classes.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class Utilities {

    /**
     * Element could be a selection from the Launch view, or an object of a selection. Compute the
     * corresponding EngineInterface.
     * @param element selection from Launch View, or the object that the selection references.
     * @return the corresponding <code>EngineInterface</code> or <code>null</code>.
     */
    public static EngineInterface computeEngineFromSelection (Object element) {
        if (element instanceof IStructuredSelection){
            element = ((IStructuredSelection)element).getFirstElement();
        }
        if (element instanceof IAdaptable) {
            if (element instanceof IStackFrame){
                element = ((IStackFrame)element).getThread();
            }
            IThread thread = (IThread) ((IAdaptable) element)
                    .getAdapter(IThread.class);
            IDebugTarget target = null;
            if (thread != null) {
                target = thread.getDebugTarget();
            }
            if (target == null) {
                target = (IDebugTarget)((IAdaptable)element).getAdapter(IDebugTarget.class);
                if (target == null){
                    ILaunch launch = (ILaunch)((IAdaptable)element).getAdapter(ILaunch.class);
                    if (launch != null){
                        target = launch.getDebugTarget();
                    }
                }
            }
            if (target != null) {
                element = target.getAdapter(ICDITarget.class);
            }
            if (element instanceof IAdaptable) {
                EngineInterface engine = (EngineInterface) ((IAdaptable) element)
                        .getAdapter(EngineInterface.class);
                return engine;
            }
        }
        return null;
    }
    
    /**
     * Element could be a selection from the Launch view, or an object of a selection. Compute the
     * corresponding CDI target.
     * @param element selection from Launch View, or the object that the selection references.
     * @return the corresponding <code>ICDITarget</code> or <code>null</code>.
     */
    public static ICDITarget computeCDITargetFromSelection (Object element) {
        IDebugTarget target = computeTargetFromSelection(element);
        if (target != null){
            ICDITarget t = (ICDITarget)target.getAdapter(ICDITarget.class);
            return t;
        }
        return null;
    }
    
    /**
     * Element could be a selection from the Launch view, or an object of a selection. Compute the
     * corresponding thread.
     * @param element selection from Launch View, or the object that the selection references.
     * @param chooseIfNecessary if true, choose the first thread if only the target is selected.
     * @return the corresponding <code>Thread</code> or <code>null</code>.
     */
    public static IThread computeThreadFromSelection (Object element, boolean chooseIfNecessary) {
        if (element instanceof IStructuredSelection) {
            element = ((IStructuredSelection) element).getFirstElement();
        }
        if (element instanceof IAdaptable) {
            if (element instanceof IStackFrame) {
                element = ((IStackFrame) element).getThread();
            }
            IThread thread = (IThread) ((IAdaptable) element).getAdapter(IThread.class);
            if (thread == null && chooseIfNecessary) {
                IDebugTarget target = (IDebugTarget) ((IAdaptable) element).getAdapter(IDebugTarget.class);
                if (target != null) {
                    thread = (IThread) target.getAdapter(IThread.class);
                    if (thread == null) {
                        IThread threads[];
                        try {
                            threads = target.getThreads();
                        }
                        catch (DebugException e) {
                            return null;
                        }
                        if (threads.length > 0)
                            thread = threads[0];
                    }
                }
            }
            return thread;
        }
        return null;
    }
    
    /**
     * Element could be a selection from the Launch view, or an object of a selection. Compute the
     * corresponding CDI thread.
     * @param element selection from Launch View, or the object that the selection references.
     * @return the corresponding <code>Thread</code> or <code>null</code>.
     */
    public static ICDIThread computeCDIThreadFromSelection (Object element) {
        IThread thread = computeThreadFromSelection(element,true);
        if (thread != null){
            return (ICDIThread)thread.getAdapter(ICDIThread.class);
        }
        return null;
    }
    
    /**
     * Return the currenlty selected CDI thread.
     */
    public static ICDIThread getSelectedCDIThread(){
        IAdaptable context = DebugUITools.getDebugContext();
        return computeCDIThreadFromSelection(context);
    }
    
    /**
     * Return the currenlty selected CDI target.
     */
    public static ICDITarget getSelectedCDITarget(){
        IAdaptable context = DebugUITools.getDebugContext();
        return computeCDITargetFromSelection(context);
    }
    
    /**
     * Element could be a selection from the Launch view, or an object of a selection. Compute the
     * corresponding target.
     * @param element selection from Launch View, or the object that the selection references.
     * @return the corresponding <code>Target</code> or <code>null</code>.
     */
    public static IDebugTarget computeTargetFromSelection (Object element) {
        if (element instanceof IStructuredSelection){
            element = ((IStructuredSelection)element).getFirstElement();
        }
        if (element instanceof IAdaptable) {
            if (element instanceof IStackFrame){
                element = ((IStackFrame)element).getThread();
            }
            IThread thread = (IThread) ((IAdaptable) element)
                    .getAdapter(IThread.class);
            IDebugTarget target = null;
            if (thread != null) {
                target = thread.getDebugTarget();
            }
            if (target == null) {
                target = (IDebugTarget)((IAdaptable)element).getAdapter(IDebugTarget.class);
                if (target == null){
                    ILaunch launch = (ILaunch)((IAdaptable)element).getAdapter(ILaunch.class);
                    if (launch != null){
                        target = launch.getDebugTarget();
                    }
                }
            }
            return target;
        }
        return null;
    }

    /**
     * Given a selected object in the debug view, return the associated stackframe.
     * @param element the selected element in the Debug View.
     * @return the associated stackframe or <code>null</code> if there is no
     * associated stackframe.
     */
    public static ICDIStackFrame computeStackFrameFrom (Object element) {
        if (element instanceof IAdaptable) {
            ICDIStackFrame sf = (ICDIStackFrame)((IAdaptable) element).getAdapter(ICDIStackFrame.class);
            if (sf != null){
                return sf;
            }
            ICStackFrame csf = (ICStackFrame) ((IAdaptable) element)
            .getAdapter(ICStackFrame.class);
            if (csf != null){
                return (ICDIStackFrame)((IAdaptable) csf).getAdapter(ICDIStackFrame.class);
            }
            ICDITarget target = (ICDITarget) ((IAdaptable) element)
                    .getAdapter(ICDITarget.class);
            if (target instanceof IAdaptable){
                sf = (ICDIStackFrame)((IAdaptable) target).getAdapter(ICDIStackFrame.class);
                if (sf != null)
                    return sf;
            }
            ICDISession session = (ICDISession) ((IAdaptable) element)
            .getAdapter(ICDISession.class);
            if (session instanceof IAdaptable) {
                ICDITarget t[] = session.getTargets();
                if (t.length > 0 && t[0] instanceof IAdaptable){
                    sf = (ICDIStackFrame)((IAdaptable) t[0]).getAdapter(ICDIStackFrame.class);
                    if (sf != null)
                        return sf;
                }
            }
        }
        return null;
    }

    /**
     * Return the current stackframe selection in the debugger view.
     * @return current selection in Debugger view.
     */
    public static StackFrameRef getSelectedStackFrame(){
        IAdaptable context = DebugUITools.getDebugContext();
        ICDIStackFrame sf = computeStackFrameFrom(context);
        if (sf instanceof IAdaptable) return (StackFrameRef)((IAdaptable)sf).getAdapter(StackFrameRef.class);
        return null;
    }

    public static String getWorkspacePath() {
        Location loc = Platform.getInstanceLocation();
        if (loc != null) {
            try {
                File f = new File(loc.getURL().toURI());
                return f.getPath();
            }
            catch (URISyntaxException e) {
                //shouldn't happen
            }
        }
        return ".";
    }
    
    public static File getWorkingDirectory (ILaunchConfiguration config) {
        IPath path;
        try {
            path = CDebugUtils.getWorkingDirectoryPath(config);
        }
        catch (CoreException e) {
            path = null;
        }
        if (path != null) {
            path.toFile();
        }
        IProject p = getProject(config);
        if (p != null && p.getLocation() != null){
            return new File(p.getLocation().toOSString());
        }
        return new File("."); // shouldn't get here.
    }
    
    public static IProject getProject(ILaunchConfiguration config){
        String name = getProjectName(config);
        if (name != null && name.length() > 0)
            return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
        return null;       
    }
    
    public static String getProjectName(ILaunchConfiguration configuration) {
        try {
            return configuration.getAttribute(
                    ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME,
                    (String) null);
        } catch (CoreException e) {
            return null;
        }
    }

}
