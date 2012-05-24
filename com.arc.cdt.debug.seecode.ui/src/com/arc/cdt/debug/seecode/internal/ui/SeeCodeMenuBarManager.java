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
package com.arc.cdt.debug.seecode.internal.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ITargetProperties;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchWindow;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.cdt.debug.seecode.ui.IMenuBarUpdater;
import com.arc.cdt.debug.seecode.ui.UISeeCodePlugin;
import com.arc.cdt.debug.seecode.ui.views.SeeCodeDisasmView;
import com.arc.seecode.display.MenuDescriptor;
import com.arc.seecode.engine.EngineInterface;

/**
 * An instance of this object listens for window and perspective changes. When
 * the debug perspective is activated and there is a SeeCode session running, it
 * adds the SeeCode menu to the menu bar, and any other user-created menus.
 * <P>
 * It also listens for the event to refresh the SeeCode display menu if the
 * engine informs us that there are new potential displays.
 * 
 * @author David Pickens
 */
@SuppressWarnings({ "restriction", "deprecation" })
public class SeeCodeMenuBarManager implements IPerspectiveListener,
        IWindowListener, IPageListener, IDebugEventSetListener,
        IDebugContextListener, IMenuBarUpdater {

    private IWorkbenchPage mActivePage = null;

    private Map<IWorkbenchPage, ICDITarget> mTargetMap = new HashMap<IWorkbenchPage, ICDITarget>();

    private Map<IWorkbenchWindow, IMenuBarUpdateManager> mMenuBarMap = new HashMap<IWorkbenchWindow, IMenuBarUpdateManager>();

    //The list of menus associated with an engine
    // instance. The first will be the SeeCode displays menu.
    private Map<IWorkbenchWindow,Map<EngineInterface, List<IAction>>> mMenuMap = new HashMap<IWorkbenchWindow,Map<EngineInterface, List<IAction>>>();

    private ICDITarget mTarget = null;

    // For a page running a SeeCode session, this is the listener for
    // the instruction-step mode. When the debugger enters the instruction-step
    // mode, we activate the disassembly display.
    private Map<ICDebugTarget,IPropertyChangeListener>mInstructionStepListeners = new HashMap<ICDebugTarget,IPropertyChangeListener>();

    /**
     *  
     */
    public SeeCodeMenuBarManager() {
        // Listen for debug events so that we know when
        // a seecode session is active.
        DebugPlugin.getDefault().addDebugEventListener(this);

        IWorkbench workbench = PlatformUI.getWorkbench();

        workbench.addWindowListener(this);
        IWorkbenchWindow windows[] = workbench.getWorkbenchWindows();
        for (IWorkbenchWindow w : windows) {
            wireWindow(w);
        }
        setActivePage();
    }

	private void setActivePage() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().asyncExec(new Runnable() {
			@Override
            public void run() {
				IWorkbenchWindow w = workbench.getActiveWorkbenchWindow();
				if (w != null) {
					IWorkbenchPage page = w.getActivePage();
					if (page != null)
						onPageActivated(page);
				}
			}
		});
	}

    /**
	 * Called when a page becomes active, or the active page's perspective
	 * changes.
	 * <P>
	 * If the page's perspective is Debugger and a SeeCode session is running,
	 * then active the SeeCode menu bar, as well as any RTOS-aware menubars.
	 * 
	 * @param page
	 *            the page that just became active, or whose perspective just
	 *            changed.
	 */
    private void onPageActivated(IWorkbenchPage page) {
        mActivePage = page;
        IPerspectiveDescriptor p = page.getPerspective();
        if (isDebugPerspective(p)) {
            makeOrUpdateSeeCodeMenusFor(page);
        } else
            removeSeeCodeMenus(true);
    }


    /**
     * Return true if the given perspective it the Debug perspective.
     * 
     * @param p
     *            a perspective to test.
     * @return true if the perspective is the Debug perspective.
     */
    private static boolean isDebugPerspective(IPerspectiveDescriptor p) {
        return p != null && p.getId().indexOf("DebugPerspective") >= 0;
    }

    /**
     * Given a page that just became active in the Debug perspective, either
     * insert the SeeCode menus in the menubar, or else update them w.r.t. to
     * the seecode session.
     * 
     * @param page
     */
    private void makeOrUpdateSeeCodeMenusFor(IWorkbenchPage page) {
        ICDITarget target = mTargetMap.get(page);
        setDebugTarget(target);
    }

    /**
     * Called when window is activated; we listen for page activation.
     */
    @Override
    public void windowActivated(IWorkbenchWindow window) {
        IWorkbenchPage page = window.getActivePage();
        if (page != null)
            onPageActivated(page);
    }

    @Override
    public void windowDeactivated(IWorkbenchWindow window) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
     */
    @Override
    public void windowClosed(IWorkbenchWindow window) {
        unwireWindow(window);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
     */
    @Override
    public void windowOpened(IWorkbenchWindow window) {
        wireWindow(window);

    }

    /**
     * Called to add page listener for a window so that we can take action each
     * time a page changes. In particular, we want to materialize the SeeCode
     * menu bar if page becomes active that has the Debug Perspective and a
     * SeeCode session is active.
     * 
     * @param w
     *            the workbench window to listen for.
     */
    private void wireWindow(IWorkbenchWindow w) {
        w.addPageListener(this);
        w.addPerspectiveListener(this);
        IDebugContextService contextService = DebugUITools.getDebugContextManager().getContextService(w);
        contextService.addDebugContextListener(this);
        this.setDebugViewSelection(contextService.getActiveContext());
    }

    private void unwireWindow(IWorkbenchWindow w) {
        IDebugContextService service= DebugUITools.getDebugContextManager().getContextService(w);
        if (service != null)service.removeDebugContextListener(this);
        w.removePageListener(this);
        w.removePerspectiveListener(this);
        mMenuBarMap.remove(w);
    }

    /**
     * Called when
     */
    @Override
    public void perspectiveActivated(IWorkbenchPage page,
            IPerspectiveDescriptor perspective) {
        if (page == mActivePage)
            onPageActivated(page);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPerspectiveListener#perspectiveChanged(org.eclipse.ui.IWorkbenchPage,
     *      org.eclipse.ui.IPerspectiveDescriptor, java.lang.String)
     */
    @Override
    public void perspectiveChanged(IWorkbenchPage page,
            IPerspectiveDescriptor perspective, String changeId) {
        if (page == mActivePage)
            onPageActivated(page);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPageListener#pageActivated(org.eclipse.ui.IWorkbenchPage)
     */
    @Override
    public void pageActivated(IWorkbenchPage page) {
        onPageActivated(page);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPageListener#pageClosed(org.eclipse.ui.IWorkbenchPage)
     */
    @Override
    public void pageClosed(IWorkbenchPage page) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPageListener#pageOpened(org.eclipse.ui.IWorkbenchPage)
     */
    @Override
    public void pageOpened(IWorkbenchPage page) {
        onPageActivated(page);
    }

    /**
     * Called when a debug event occurs. We listen for new debug targets being
     * created or old ones terminating.
     * <P>
     * We also listener for the special event that informs us to refresh the
     * SeeCode display menu from the engine's display selector list.
     * 
     * @param events
     */
    @Override
    public void handleDebugEvents (DebugEvent[] events) {
        for (final DebugEvent event : events) {
            switch (event.getKind()) {
                case DebugEvent.TERMINATE: {
                    Object source = event.getSource();
                    if (source instanceof IDebugTarget && source instanceof IAdaptable) {

                        final ICDebugTarget target = (ICDebugTarget) ((IAdaptable) source)
                            .getAdapter(ICDebugTarget.class);
                        if (target != null) {
                            final ICDITarget cdiTarget = (ICDITarget) ((IAdaptable) source)
                                .getAdapter(ICDITarget.class);
                            Display d = PlatformUI.getWorkbench().getDisplay();
                            // We have boundary cases in which the Display is disposed before this
                            // code executes. It means the IDE is shutting own.
                            // Presumably there's nothing significant lost in not terminating under
                            // that condition.
                            if (!d.isDisposed())
                                d.asyncExec(new Runnable() {

                                    @Override
                                    public void run () {
                                        terminateTarget(cdiTarget);
                                    }
                                });
                        }

                        // Remove the listener from the target that is terminating...
                        IPropertyChangeListener l = mInstructionStepListeners.remove(target);
                        if (l != null && target != null) {
                            target.removePropertyChangeListener(l);
                        }
                    }
                    break;
                }
                case DebugEvent.CREATE: {
                    Object source = event.getSource();
                    // We wait until target is created, not just the session.
                    // Otherwise, we may be calling back thru the engine while it is processing
                    // a "loadProgram" call do display an error. In such a case, we could dead lock.
                    // CR2140
                    if (source instanceof IAdaptable) {
                        final ICDITarget cdiTarget = (ICDITarget) ((IAdaptable) source).getAdapter(ICDITarget.class);
                        if (cdiTarget != null && source instanceof IThread) {
                            if (cdiTarget != mTarget) {
                                Display d = PlatformUI.getWorkbench().getDisplay();
                                d.asyncExec(new Runnable() {

                                    @Override
                                    public void run () {
                                        setDebugTarget(cdiTarget);
                                    }
                                });
                            }
                        }
                        if (mInstructionStepListeners.get(mActivePage) == null) {
                            final ICDebugTarget target = (ICDebugTarget) ((IAdaptable) source)
                                .getAdapter(ICDebugTarget.class);
                            // Add a listener for when the session goes into Instruction-step mode.
                            // Activate the disassembly view when it happens.
                            if (target != null) {
                                IPropertyChangeListener l = new IPropertyChangeListener() {

                                    @Override
                                    public void propertyChange (PropertyChangeEvent propEvent) {
                                        if (propEvent.getProperty()
                                            .equals(ITargetProperties.PREF_INSTRUCTION_STEPPING_MODE)) {
                                            if (((Boolean) propEvent.getNewValue()).booleanValue()) {
                                                try {
                                                    // Should never be null, but time issues can happen, I guess.
                                                    // CR2149
                                                    if (mActivePage != null)
                                                        mActivePage.showView(SeeCodeDisasmView.DISASM_VIEW_ID);
                                                }
                                                catch (PartInitException e) {
                                                    UISeeCodePlugin.getDefault().getLog().log(e.getStatus());
                                                }
                                            }

                                        }

                                    }
                                };
                                target.addPropertyChangeListener(l);
                                mInstructionStepListeners.put(target, l);
                            }
                        }
                    }
                    break;
                }
                case DebugEvent.MODEL_SPECIFIC:
                    switch (event.getDetail()) {
                        // Engine request to regenerate the
                        // display menu...
                        case SeeCodePlugin.REGEN_DISPLAY_MENU_EVENT_DETAIL: {
                            Display d = PlatformUI.getWorkbench().getDisplay();
                            d.asyncExec(new Runnable() {

                                @Override
                                public void run () {
                                    handleRegenDisplayMenuEvent(event);
                                }
                            });
                            break;
                        }
                        default:
                            break;
                    }
                    break;
            }
        }
    }

    /**
     * A change event in which the source is the {@link EngineInterface}means
     * that we need to regenerate the menu.
     * 
     * @param event
     */
    private void handleRegenDisplayMenuEvent(DebugEvent event) {
        Object src = event.getSource();
        if (src instanceof EngineInterface) {
            // Should always be true.
            EngineInterface engine = (EngineInterface) src;
            List<IAction> menuList = getMenuListFor(engine);
            if (menuList != null) {
                for (IAction a : menuList) {
                    if (a instanceof SeeCodeDisplayAction) {
                        ((SeeCodeDisplayAction) a).refresh();
                    }
                }
            }
        }
    }

    private void terminateTarget(ICDITarget target) {
        Iterator<Map.Entry<IWorkbenchPage, ICDITarget>> each = mTargetMap
                .entrySet().iterator();
        while (each.hasNext()) {
            Map.Entry<IWorkbenchPage, ICDITarget> entry = each.next();
            if (entry.getValue() == target) {
                each.remove();
            }
        }
        if (mTarget == target) {
            setDebugTarget(null);
        }
        EngineInterface engine = getEngineFor(target);
        if (engine != null) {
            mMenuMap.remove(engine);
        }

    }

    private static EngineInterface getEngineFor(ICDITarget target) {
        if (target instanceof IAdaptable) {
            return (EngineInterface) ((IAdaptable) target)
                    .getAdapter(EngineInterface.class);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
     */
    @Override
    public void debugContextChanged (DebugContextEvent event) {
        setDebugViewSelection(event.getContext());
    }

    /**
     * Called when the debug view selection changed. Refresh the SeeCode menus
     * if necessary.
     * 
     * @param selection
     */
    private void setDebugViewSelection(ISelection selection) {
        if (selection != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            Object o = ss.getFirstElement();
            if (! (o instanceof ILaunch) && ! (o instanceof IStackFrame) && 
                    ! (o instanceof IThread) && !(o instanceof IDebugTarget) &&
                    o instanceof IAdaptable)
            {
                IDebugTarget target = (IDebugTarget)((IAdaptable)o).getAdapter(IDebugTarget.class);
                if (target != null) o = target;  
                else {
                    ILaunch launch = (ILaunch)((IAdaptable)o).getAdapter(ILaunch.class);
                    if (launch != null) o = launch; 
                }
            }
            if (o instanceof ILaunch) {
                IDebugTarget targets[] = ((ILaunch) o).getDebugTargets();
                if (targets != null && targets.length > 0) {
                    // We assume all targets of a launch will have same session.
                    o = targets[0];
                }
            }
            if (o instanceof IStackFrame){
                o = ((IStackFrame)o).getThread();
            }
            if (o instanceof IThread){
                o = ((IThread)o).getDebugTarget();
            }
            //To get the seecode engine interface, we extract
            // "session" object that should have an adapter for
            // EngineInterface.
            if (o instanceof IAdaptable) {
                ICDITarget target = (ICDITarget) ((IAdaptable) o)
                        .getAdapter(ICDITarget.class);
                // We don't want to "select" a session before its target is running.
                // If the engine is in the process of loading a program and
                // attempting to display an error box. A call back into the engine would deadlock. CR2140.
                if (target != null && target.isTerminated())
                    target = null; // get rid of debug view if target terminated.
                setDebugTarget(target);
            }
            else setDebugTarget(null);
        } else {
            // NOTE: during animation, as stackframes change, the selection may go empty for a very short
            // time as an artifact of the Tree viewer. Ignore such a case by observing if the last thing
            // we were looking at is still active.
            if (mTarget == null || mTarget.isTerminated())
                setDebugTarget(null);
        }
    }

    private void setDebugTarget(ICDITarget target) {
        // Associate this target with the active page.
        if (target != null)
            mTargetMap.put(mActivePage, target);
        else
            mTargetMap.remove(mActivePage);
        if (target != mTarget) {
            mTarget = target;
            if (target instanceof IAdaptable) {
                EngineInterface engine = getEngineFor(target);
                if (engine != null) {
                    insertOrUpdateSeeCodeMenus(engine);
                } else
                    removeSeeCodeMenus(true);
            } else
                removeSeeCodeMenus(true);
        }
    }

    private void insertOrUpdateSeeCodeMenus(EngineInterface engine) {
        // KLOODGE ALERT:
        // Eclipse doesn't provide an API to dynamically
        // create menus in the menubar. Therefore, we
        // must resort to going "under the covers".
        // What you must do, you must do.
        IMenuBarUpdateManager mgr = getMenuManager();
        if (mgr == null) {
            mTarget = null; // no active session
            return; // shouldn't happen
        }
        List<IAction> menuList = getMenuListFor(engine);
        mgr.removeAll(); // remove ones from other sessions
        if (menuList != null) {  // should never be null, but just in case...
            for (IAction a : menuList) {
                mgr.insertMenu(a);
            }
        }
        mgr.update();
    }

    /**
     * Return the list of menus associated with the engine, adding the SeeCode
     * display menu if it isn't already there.
     * 
     * @param engine
     * @return list of menus associated with the engine.
     */
    private List<IAction> getMenuListFor(EngineInterface engine) {
        IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (w == null)
            return null; // should never happen, but just in case.
        Map<EngineInterface,List<IAction>> map = mMenuMap.get(w);
        if (map == null){
            map = new HashMap<EngineInterface,List<IAction>>();
            mMenuMap.put(w,map);
        }
        List<IAction> menuList = map.get(engine);
        if (menuList == null) {
            menuList = new ArrayList<IAction>();
            menuList.add(new SeeCodeDisplayAction(engine));
            map.put(engine, menuList);
        }
        return menuList;
    }

    /**
     * @return the menu manager for the active window.
     */
    private IMenuBarUpdateManager getMenuManager() {
        //KLOODGE ALERT:
        // Prior to Eclipse 3.3, there was no API to dynamically
        // create menus in the menubar. Therefore, we
        // resorted to going "under the covers".
        // What you must do, you must do.
        //
        //NOTE: as of Eclipse 3.3 there is IMenuService that is
        // retrieved by "(IMenuService)PlatformUI.getWorkbench().getService(IMenuService.class)".
        // Supposedly, it has the ability to let us generate menus in the main menu bar without
        // "going under the covers". But it doesn't seem very intuitive, and little is
        // documented on how to use it. Also, its design is questionable since is violates
        // Eclipse conventions and references hard classes instead of interfaces.
        // Therefore, for the time being, we continue to go "under the covers".
        //      
        
        IWorkbenchWindow w = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();
        // For some unknown reason, the workbenchwindow is
        // null when it shouldn't be. (Perhaps if
        // Eclipse frame loses focus?)
        // Use last active page to get it.
        if (w == null && mActivePage != null) {
            w = mActivePage.getWorkbenchWindow();
        }
        if (w != null) {
            IMenuManager mgr = ((WorkbenchWindow) w).getMenuManager();
            if (mgr != null) { // could be null if we're doing a sudden shutdown (license failure)
                IMenuBarUpdateManager mbmgr = mMenuBarMap.get(w);
                if (mbmgr == null || mbmgr.getMenuManager() != mgr) {
                    mbmgr = new MenuBarUpdateManager(mgr);
                    mMenuBarMap.put(w, mbmgr);
                }
                return mbmgr;
            }
        }
        return null;
    }

    private void removeSeeCodeMenus(boolean update) {
        mTarget = null; // no contributing session
        IMenuBarUpdateManager mgr = getMenuManager();
        if (mgr == null)
            return; //shouldn't happen
        mgr.removeAll();
        mgr.update();
    }

    /**
     * Called when engine has generated a new menu to appear in the menubar
     * (e.g., RTOS-awareness menu).
     * 
     * @param engine
     *            the engine instance to be associated with the menu.
     * @param menu
     *            a dynamically-generated menu from the engine.
     */
    @Override
    public void addMenu(EngineInterface engine, String label,
            MenuDescriptor menu) {
        DynamicEngineMenu menuEntry = new DynamicEngineMenu(label, menu);
        List<IAction> menuList = getMenuListFor(engine);
        //Can be null in the unlikely case of no active window, which probably can't
        // happen, but check anyway
        if (menuList != null) {
            menuList.add(menuEntry);
            if (mTarget != null && getEngineFor(mTarget) == engine) {
                insertOrUpdateSeeCodeMenus(engine);
            }
        }
    }

}
