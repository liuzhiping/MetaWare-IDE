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
package com.arc.cdt.debug.seecode.ui.display;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIAnimatable;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.SubStatusLineManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.cdt.debug.seecode.core.cdi.ICDISeeCodeSession;
import com.arc.cdt.debug.seecode.ui.UISeeCodePlugin;
import com.arc.cdt.debug.seecode.ui.Utilities;
import com.arc.cdt.debug.seecode.ui.views.AbstractEngineBasedView;
import com.arc.cdt.debug.seecode.ui.views.IContextHelpIds;
import com.arc.cdt.debug.seecode.ui.views.SeeCodeCustomView;
import com.arc.seecode.display.ISeeCodeTextViewer;
import com.arc.seecode.display.ISeeCodeTextViewerCallback;
import com.arc.seecode.display.MenuDescriptor;
import com.arc.seecode.engine.EngineInterface;
import com.arc.seecode.engine.EngineTimeoutException;
import com.arc.seecode.engine.StackFrameRef;
import com.arc.seecode.engine.display.AbstractCustomDisplayCallback;
import com.arc.seecode.engine.display.IArgsFileLocator;
import com.arc.seecode.engine.display.IDisplayCreator;
import com.arc.seecode.engine.display.SeeCodeTextViewerCallback;
import com.arc.widgets.IComponent;
import com.arc.widgets.IContainer;
import com.arc.widgets.IImage;
import com.arc.widgets.IWindow;

/**
 * This is the callback through which the SeeCode engine controls its own
 * displays.
 * <P>
 * Thus, the call is from the engine (at least conceptually).
 * <P>
 * <B>NOTE: </B> all methods are assumed invoked from the main GUI thread.
 * 
 * @author David Pickens
 */
public class CustomDisplayCallback extends AbstractCustomDisplayCallback
        implements ICDISeeCodeSession.ISessionDisposeListener {
    

    public interface IObserver {
        void onCreated(ISeeCodeTextViewer viewer, EngineInterface engine);
        void onTitleChanged(ISeeCodeTextViewer viewer, EngineInterface engine);
        void onDeleted(ISeeCodeTextViewer viewer, EngineInterface engine);
    }

    static final String PLUGIN_ID = "com.arc.cdt.debug.seecode.ui";

    //Map<int,IWindow>
    private Map<IContainer,Integer> mContainerMap = new HashMap<IContainer,Integer>();

    private Display mDisplay;

    private ICDISeeCodeSession mSession;
    
    /**
     * The display that sent the last "sendValueUpdate" to
     * the engine. This is used in case the engine spontaneously
     * creates a display and we can figure out where it originated
     * in contriving a "secondary id" for the new View.
     */
    private ISeeCodeTextViewer mDisplayThatSentLastValueUpdate = null;
    /**
     * The update property that was last changed.
     * We cache this so that if the engine spontaneously creates
     * a display, we can use this to contrive the
     * "secondary id" for it.
     */
    private String mLastValueUpdate = "";

    private Map<Integer,IContainer>mIdToContainerMap = new HashMap<Integer,IContainer>();
    
    private List<IObserver> mObservers = new ArrayList<IObserver>();

    private UpdateViewsThread mUpdateViewsThread;

    /**
     * @param target the associated target.
     */
    public CustomDisplayCallback(ICDITarget target) {
        super(UISeeCodePlugin.getWidgetFactory(), new ViewToolBarBuilderFactory(),
                (EngineInterface)((IAdaptable)target).getAdapter(EngineInterface.class),
        		((ICDISeeCodeSession)target.getSession()).getSeeCodeInstallationDirectory());
        //Force SeeCode menu manager to be created if it isn't already.
        // We cannot do this at plugin startup because it requires that the workbench
        // be fully instantiated.
        UISeeCodePlugin.getDefault().getMenuBarUpdater();
        
        mDisplay = PlatformUI.getWorkbench().getDisplay();
        mSession = (ICDISeeCodeSession)target.getSession();
        mUpdateViewsThread = new UpdateViewsThread();
        mUpdateViewsThread.start();
        mSession.addSessionDisposeListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.display.AbstractCustomDisplayCallback#getContainerForNewDisplay(int)
     */
    @Override
    protected IContainer getContainerForNewDisplay(final int id) {
        IContainer parent = mIdToContainerMap.get(new Integer(id));
        return parent;
        
    }
    /**
     * SEt the container that the next display is to
     * be created in. Called from the
     * SeeCodeCustomView when it invoking
     * {@link EngineInterface#createDisplay}.
     * @param container
     */
    public void setContainer(int id, IContainer container){
        mIdToContainerMap.put(new Integer(id),container);
        mContainerMap.put(container, new Integer(id));
    }
    
    /**
     * Called by SeeCodeCustomView to determine which display
     * it is viewing.
     * @param container
     * @return the display being viewed in the container.
     */
    public ISeeCodeTextViewer getDisplayFor(IContainer container){
        Integer i = mContainerMap.get(container);
        if (i != null){
            return getDisplay(i.intValue());
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.display.AbstractCustomDisplayCallback#internalError(com.arc.seecode.display.ISeeCodeTextViewer,
     *      java.lang.String, java.lang.Throwable)
     */
    @Override
    public void internalError(ISeeCodeTextViewer viewer, String message,
            Throwable t) {
        final IStatus status = new Status(IStatus.ERROR, PLUGIN_ID,
                IStatus.ERROR, message == null?"Exception":message, t);
        SeeCodePlugin.log(status);

        //Under some circumstances, we may not be in the UI thread.
        
        Runnable run = new Runnable(){
            @Override
            public void run() {
                ErrorDialog.openError(mDisplay.getActiveShell(), "Internal error",
                    "An internal error occurred:\n"
                        + "See the error log for more details\n", status);
            }
        };
        if (mDisplay.getThread() == Thread.currentThread())
            run.run();
        else
            mDisplay.asyncExec(run);
        //
        // If we got a timeout, then the engine is hung, which means the GUI is hung.
        // Don't know what else to do but forceably kill it.
        if (t instanceof EngineTimeoutException) {
            try {
                mSession.getSessionProcess().destroy();
            }
            catch (CDIException e) {
                internalError(viewer,"Error occurred while forceably terminating",e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.display.AbstractCustomDisplayCallback#notifyError(com.arc.seecode.display.ISeeCodeTextViewer,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void notifyError(ISeeCodeTextViewer viewer, final String message,
            final String title) {

        MessageBox box = new MessageBox(mDisplay.getActiveShell(), SWT.OK
                | SWT.ICON_ERROR);
        box.setMessage(message);
        box.setText(title);
        box.open();

    }
    
    static class ProgressMonitorDescriptor{
        public IProgressMonitor progressMonitor;
        public boolean started = false;
        public String title;
        public int value;
        public Runnable ifCanceled;
        ProgressMonitorDescriptor(IProgressMonitor p, String title, Runnable ifCanceled){
            this.progressMonitor = p;
            this.title = title;
            this.ifCanceled = ifCanceled;
            this.value = 0;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.display.AbstractCustomDisplayCallback#createProgressDisplay(java.lang.String,
     *      int, java.lang.Runnable)
     */
    @Override
	protected Object createProgressDisplay(String title, int delay, Runnable ifCanceled) {
		IActionBars actionBars = getActionBars(mDisplayThatSentLastValueUpdate);
		IStatusLineManager statusLine = null;
		if (actionBars != null) statusLine = actionBars.getStatusLineManager();
		if (statusLine != null) {
			IProgressMonitor p = statusLine.getProgressMonitor();
			return new ProgressMonitorDescriptor(p, title, ifCanceled);
		}
		// shouldn't get here
		return new Integer(0); // anything but null
	}

    /*
	 * (non-Javadoc)
	 * 
	 * @see com.arc.seecode.engine.display.AbstractCustomDisplayCallback#disposeProgressDisplay(java.lang.Object)
	 */
    @Override
    protected void disposeProgressDisplay(Object p) {
        //The following will always be true unless an
        // error occurred in creating the progress monitor.
        if (p instanceof ProgressMonitorDescriptor) {
            ProgressMonitorDescriptor pd = (ProgressMonitorDescriptor) p;
            if (pd.started) {
                pd.progressMonitor.done();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.display.AbstractCustomDisplayCallback#setProgressPercentage(java.lang.Object,
     *      int)
     */
    @Override
    protected void setProgressPercentage(Object handle, int percentage) {
        //The following will always be true unless an
        // error occurred in creating the progress monitor.
        if (handle instanceof ProgressMonitorDescriptor) {
            ProgressMonitorDescriptor pd = (ProgressMonitorDescriptor) handle;
            if (!pd.started) {
                pd.started = true;
                pd.progressMonitor.beginTask(pd.title, 100);
            }
            if (pd.progressMonitor.isCanceled()){
                if (pd.ifCanceled != null) {
                    pd.ifCanceled.run();
                }
            }
            else if (percentage > pd.value) {
                pd.progressMonitor.worked(percentage-pd.value);
                pd.value = percentage;
            }
        }
    }
    
    private IActionBars getActionBars(int id) {
        ISeeCodeTextViewer v = getDisplay(id);
        return getActionBars(v);
    }
    
    /**
     * Given a view, try to locate its status line manager. Or else, find that of the debug view.
     * @param v the view whose status line manager is desired, or null if there is no associated view.
     * @return status line manager associated with view or with Debug View.
     */
    private IActionBars getActionBars (ISeeCodeTextViewer v) {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        IViewPart view = null;
        if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                if (v != null) {
                    IViewReference vr = page.findViewReference(SeeCodeCustomView.VIEW_ID, v.getDisplayKind());
                    view = vr != null ? vr.getView(true) : null;
                }
                if (view == null) {
                    view = page.findView(IDebugUIConstants.ID_DEBUG_VIEW);
                }
                if (view != null) {
                    return view.getViewSite().getActionBars();
                }
            }          
        }
        return null;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see com.arc.seecode.engine.ICustomDisplayCallback#setStatus(int,
	 *      java.lang.String)
	 */
    @Override
    public void setStatus(int id, final String msg) {
        IActionBars actionBars = getActionBars(id);
        if (actionBars != null) {
            IStatusLineManager statusLine = actionBars.getStatusLineManager();
            if (statusLine != null) {
                if (statusLine instanceof SubStatusLineManager && msg != null && msg.length() > 0){
                    ((SubStatusLineManager)statusLine).setVisible(true);
                }
                statusLine.setMessage(msg);
                actionBars.updateActionBars();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.ICustomDisplayCallback#displayErrorBox(int,
     *      java.lang.String)
     */
    @Override
    public void displayErrorBox(int id, String msg) {
        ISeeCodeTextViewer v = id == 0?null:getDisplay(id);
        notifyError(v, msg, "Error");

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.ICustomDisplayCallback#refreshDisplaysMenu()
     * @deprecated
     */
    @Override
    public void refreshDisplaysMenu() {
        // We cause the SeeCode displays menu to
        // be regenerated by firing a MODEL_SPECIFIC event
        // with the EngineInterface object as its source.
        // See com.arc.cdt.debug.seecode.internal.ui.SeeCodeDisplaysMenuDelegate
        getEngine().invalidateDisplaySelectorCache();
        DebugPlugin.getDefault().fireDebugEventSet(
                new DebugEvent[]{new DebugEvent(getEngine(),DebugEvent.MODEL_SPECIFIC,SeeCodePlugin.REGEN_DISPLAY_MENU_EVENT_DETAIL)});
    }
    
    @Override
    public void setDisplaySelectors(String selectors[]){
        // We cause the SeeCode displays menu to
        // be regenerated by firing a MODEL_SPECIFIC event
        // with the EngineInterface object as its source.
        // See com.arc.cdt.debug.seecode.internal.ui.SeeCodeDisplaysMenuDelegate
        super.setDisplaySelectors(selectors);
        DebugPlugin.getDefault().fireDebugEventSet(
                new DebugEvent[]{new DebugEvent(getEngine(),DebugEvent.MODEL_SPECIFIC,SeeCodePlugin.REGEN_DISPLAY_MENU_EVENT_DETAIL)});
        
    }
    
    private static final int DELETE = 1;
    private static final int TITLE = 2;
    private static final int CREATED = 3;
    
    private void notifyObservers(int event, int id){
        ISeeCodeTextViewer viewer = getDisplay(id);
        // Need to make copy because observers removed by observer themselves.
        EngineInterface engine = getEngine();
        for (IObserver observer: new ArrayList<IObserver>(mObservers)){
            switch(event){
                case DELETE: observer.onDeleted(viewer,engine); break;
                case TITLE: observer.onTitleChanged(viewer,engine); break;
                case CREATED: observer.onCreated(viewer,engine); break;
            }
        }
    }
    
    public void addObserver(IObserver observer){
        mObservers.add(observer);
    }
    
    public void removeObserver(IObserver observer){
        mObservers.remove(observer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.ICustomDisplayCallback#deleteDisplay(int)
     */
    @Override
    public void deleteDisplay(int id) {
        notifyObservers(DELETE,id);
        //DebugPlugin.getDefault().removeDebugEventListener(this);
        super.deleteDisplay(id);
        mContainerMap.remove(new Integer(id));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.ICustomDisplayCallback#createDisplay(java.lang.String,
     *      int)
     */
    @Override
    public void createDisplay (final String properties, final int id) {
        try {
            // This method is called from the engine to
            // create a display with the given properties.
            // The engine does this from two difference responses:
            // 1) The user explicitly asked for the display
            // to be created. In such a case, we'll have
            // a SeeCodeCustomView object ready to
            // received the display. The parent widget
            // can be retrieved by calling
            // getContainerForNewDisplay.

            // 2) The engine could be creating a display
            // as a response to, say, clicking the
            // "disassembly" button on Source display
            // or by clicking on buttons in the History
            // display. Such displays have an ID that is
            // > 64000 and there is no view to receive
            // the display. So, what do we do?
            // We create a view on-the-fly to place
            // the new display in.

            if (getContainerForNewDisplay(id) != null) {
                super.createDisplay(properties, id);
                notifyObservers(CREATED, id);
            }
            else {
                createViewForId(id);
                if (getContainerForNewDisplay(id) == null)
                    internalError(null, "Could not make spontaneous view for ID = " + id, null);
                else {
                    createDisplay(properties, id);
                }
            }
        }
        catch (RuntimeException e) {
            internalError(null, "Exception in creating display", e);
        }
    }
    
    /**
     * The engine is creating a display spontaneously. The events data is the display ID.
     * <P>
     * Create the view and forceably populate it.
     */
    private void createViewForId(int id) {
        // Get display from which the request to create this view originated.
        ISeeCodeTextViewer src = mDisplayThatSentLastValueUpdate;
        // We previously cached the "value update" that caused the engine
        // to request a new display; we encode it in the name.
        String updateProperty = mLastValueUpdate;
            // We contrive the secondary ID from the originating
            // display followed by ID_SEPARATOR followed by
            // the kind
        String srckind = src == null ? "UNK" : src.getDisplayKind();

        String id2 = srckind + SeeCodeCustomView.ID_SEPARATOR
                    + updateProperty;
        SeeCodeCustomView view = createView(id2);
        // If there is already a view for this engine,
        // create another one! That's what the engine expects!   
        int cnt = 0;
        while (view != null && view.getDisplayIdFor(getEngine()) != 0 &&
                view.getDisplayIdFor(getEngine()) != id)
        {
            view = createView(id2+"." + (++cnt));
            if (cnt == 10) break; // prevent things getting out of hand
        }
        if (view != null) {
            view.setPreassignedDisplayID(getEngine(),id);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.ICustomDisplayCallback#setTitle(int,
     *      java.lang.String)
     */
    @Override
    public void setTitle(int id, String title) {
        super.setTitle(id, title);
        notifyObservers(TITLE,id);
    }

//    /**
//     * Return the engine interface object corresponding to the target, if known.
//     * 
//     * @param target
//     * @return the engine interface object corresponding to the target, if
//     *         known.
//     */
//    private static EngineInterface getEngineFor(ICDITarget target) {
//        if (target instanceof IAdaptable) {
//            EngineInterface engine = (EngineInterface) ((IAdaptable) target)
//                    .getAdapter(EngineInterface.class);
//            return engine;
//        }
//        return null;
//    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.display.AbstractCustomDisplayCallback#getSelectedDisplay()
     */
    @Override
    protected ISeeCodeTextViewer getSelectedDisplay() {
        IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null) return null;
        IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        if (window == null) return null;
        IWorkbenchPage page = window.getActivePage();
        if (page == null) return null;
        IWorkbenchPart part = page.getActivePart();
        if (part instanceof AbstractEngineBasedView){
            SeeCodeCustomView v = (SeeCodeCustomView)part;
            return v.getViewer();
        }
        return null;
    }
    

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.ICustomDisplayCallback#updateViews()
     */
    @Override
    public void updateViews() {
        // We don't want to update the SeeCode displays from the UI thread because
        // the action can be relatively time consuming as the engine is queried.
        // It can make the UI seems sluggish.
        // So, we have another thread do it.
        // 
        mUpdateViewsThread.doit();
    }
    
    /**
     * This thread merely waits in a loop to be notified to update the
     * SeeCode debugger views. The method {@link #updateViews} causes it
     * to do something.
     */
    class UpdateViewsThread extends Thread {
        private boolean _doit;
        private boolean terminated = false;
        UpdateViewsThread(){
            super("UpdateViews");
            setDaemon(true);
        }
        @Override
        public void run() {
            while (!terminated) {
                boolean doIt;
                synchronized(this) {
                    try {
                        wait();
                    }
                    catch (InterruptedException e) {
                       if (terminated) break;
                    }
                    doIt = _doit;
                    _doit = false;
                }
                if (doIt) {
                    getEngine().invalidateCache();
                    mSession.updateViews();
                }
            }
        }
        
        public void terminate(){
            terminated = true;
            interrupt();
        }
        
        public synchronized void doit(){
            _doit = true;
            notifyAll();
        }
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.display.AbstractCustomDisplayCallback#getDisplayCreator()
     */
    @Override
    protected IDisplayCreator getDisplayCreator() {
        return this;
    }

    /**
     * @inheritDoc
     * <P>
     * Called when a Displays popup menu is selected
     * to create another display.
     * @param kind
     * @param viewer
     */
    @Override
    public void createDisplay(String kind, ISeeCodeTextViewer viewer) {
        createView(kind); 
    }

    @Override
    public void activateDisplay(String kind){
    	createView(kind);
    }
    /**
     * Dynamically create a new SeeCode view with the given "secondary" ID.
     * @param id the kind of display ("disasm", "source", etc.).
     */
	private SeeCodeCustomView createView(String id) {
		IViewPart v = UISeeCodePlugin.getDefault().createDisplay(getEngine(),
				id);
		if (v instanceof SeeCodeCustomView) {
			return (SeeCodeCustomView) v;
		}
		// Its a view that we've replaced with a custom GUI version (e.g.,
		// RegDisplay)
		return null;
	}
    /* (non-Javadoc)
     * @see com.arc.seecode.engine.display.AbstractCustomDisplayCallback#makeViewerCallback()
     */
    @Override
    protected ISeeCodeTextViewerCallback makeViewerCallback() {
        // When the engine spontaneously creates displays in
        // response to a user, say, clicking the "disasm" button
        // of a Source display, we need to contrive the
        // view from its source display and the id of the button
        // that caused it.
        // So, we override the "sendValueUpdate" (which is
        // called by the GUI when an engine-originated button
        // is pressed), so that we can cache the update property.
        return new SeeCodeTextViewerCallback(getEngine(),this,getDisplayCreator()){
            @Override
            public void sendValueUpdate(ISeeCodeTextViewer d, String propertyName,
                    String value){
                mDisplayThatSentLastValueUpdate = d;
                mLastValueUpdate = propertyName;
                super.sendValueUpdate(d,propertyName,value);
                
            }
        };
    }

    @Override
    protected IArgsFileLocator getArgsFileLocator() {
        return new IArgsFileLocator(){

            @Override
            public File computeArgsFile(String prefix) {
                String s = mSession.getArgsPattern();
                s = s.replaceAll("%s",prefix);
                return new File(s);
            }

            @Override
            public File computePropertiesFile(String prefix) {
                File d = mSession.getProjectDirectory();
                return new File(d,"." + prefix + ".properties");
            }
            
        };
    }

    /**
     * Called to insert the user-defined menu into
     * the Eclipse menubar.
     */
    @Override
    protected void setUserDefinedMenu(int id, String label, MenuDescriptor menu) {
        UISeeCodePlugin.getDefault().getMenuBarUpdater().addMenu(getEngine(),label,menu);       
    }
    
    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param id
     */
    @Override
    public void show (int id) {
        super.show(id);
        ISeeCodeTextViewer d = getDisplay(id);
        AbstractEngineBasedView view = SeeCodeCustomView.getDisplayFor(d);
        if (view != null){
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().activate(view);
        }
    }
   
    @Override
    protected void setHelp (IComponent widget, String helpID) {
        PlatformUI.getWorkbench().getHelpSystem().setHelp((Control)widget.getComponent(),IContextHelpIds.PREFIX + helpID);
    }

    @Override
    protected Object getDialogOwner() {
        return mDisplay.getActiveShell();
    }

    @Override
    public void associateHelp(IWindow dialog, String id) {
        Shell shell = (Shell)dialog.getComponent();
        PlatformUI.getWorkbench().getHelpSystem().setHelp(shell,IContextHelpIds.PREFIX + id);       
    }

    @Override
    public void onSessionDisposed (ICDISeeCodeSession session) {
        if (session == mSession){ // should always be true
            mUpdateViewsThread.terminate();
            session.removeSessionDisposeListener(this);
        }       
    }

    @Override
    protected StackFrameRef getStackFrameRef() {
        // This is called from watchpoint dialog to determine the stackframe
        // context in which to evaluate a watchpoint expression.
        return Utilities.getSelectedStackFrame();    
    }
    
    @Override
    public void animate(int tid, String command){
        try {
            ICDIThread thread = null;
            if (tid == 0){
                thread = Utilities.getSelectedCDIThread();
                if (thread == null){
                    ICDITarget target = Utilities.getSelectedCDITarget();
                    if (target != null){
                        thread = target.getCurrentThread();
                    }
                }
                if (thread == null){
                    displayErrorBox(0,"Need to select a thread to animate");
                    return;
                }
            }
            else {
                ICDITarget target = Utilities.getSelectedCDITarget();
                ICDIThread threads[] = target.getThreads();
                for (ICDIThread t: threads) {
                    if (t instanceof IAdaptable){
                        Integer threadID = (Integer)((IAdaptable)t).getAdapter(Integer.class);
                        if (threadID != null && threadID.intValue() == tid){
                            thread = t;
                            break;
                        }
                    }
                }
                if (thread == null) {
                    displayErrorBox(0,"Thread " + tid + " does not exist");
                    return;
                }
            }
            if (thread instanceof ICDIAnimatable){
                if (!thread.isSuspended() || ((ICDIAnimatable)thread).isAnimating()) {
                    displayErrorBox(0,"Animation failed: thread must be suspended.");
                }
                else{
                    int stepType = lookupStepType(command);
                    if (stepType <= 0){
                        displayErrorBox(0,"Unsupported animation command: " + command);
                    }
                    ((ICDIAnimatable)thread).animate(stepType);
                }
            }
            else {
                displayErrorBox(0,"Selected debugger session does not support animation");
            }
        }
        catch (CDIException e) {
            displayErrorBox(0,"Animation failed: " + e.getMessage());
        }
    }
    
    private int lookupStepType(String cmd){
        cmd = cmd.toLowerCase();
        if (cmd.equals("ssi")) return ICDIAnimatable.ANIMATE_STATEMENT_STEP_INTO;
        if (cmd.equals("sso")) return ICDIAnimatable.ANIMATE_STATEMENT_STEP_OVER;
        if (cmd.equals("isi")) return ICDIAnimatable.ANIMATE_INSTR_STEP_INTO;
        if (cmd.equals("iso")) return ICDIAnimatable.ANIMATE_INSTR_STEP_OVER;
        return -1;
    }
    
    static class LicenseWaitJob extends Job {
        private int timeout;
        private boolean fDone = false;
        public LicenseWaitJob(int millis) {
            super("Acquiring debugger license");
            timeout = millis;
        }

        @Override
        protected IStatus run (IProgressMonitor monitor) {
            monitor.beginTask("Waiting for license server",timeout);
            long endTime = System.currentTimeMillis() + timeout;
            synchronized(this) {
                while (!fDone && System.currentTimeMillis() < endTime){
                    try {
                        this.wait(1000);
                        monitor.worked(1000);
                    }
                    catch (InterruptedException e) {
                        
                    }              
                }
            }
            return Status.OK_STATUS;
            
        }
        
        void done(){
            synchronized(this){
                fDone = true;
                this.notifyAll();
            }
        }
        
    }
    
    private LicenseWaitJob pendingLicenseWait = null;

    @Override
    public void onLicenseRequestEnd (boolean granted) {
        if (pendingLicenseWait != null){
            pendingLicenseWait.done();
            pendingLicenseWait = null;
        }      
    }

    @Override
    public void onLicenseRequestStart (int timeoutMillis) {
       IProgressService service = PlatformUI.getWorkbench().getProgressService();
       pendingLicenseWait = new LicenseWaitJob(timeoutMillis);
       service.showInDialog(null, pendingLicenseWait);
       pendingLicenseWait.schedule();       
    }
    
    static class Text {
        public Text(int x,int y, String text){
            this.x = x;
            this.y = y;
            this.text = text;
        }
        public int x,y;
        public String text;
    }
    
    static class SplashScreen implements PaintListener {
        public SplashScreen(Image image){
            this.image = image;
            Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
            shell = new Shell(parent,SWT.NO_TRIM|(image!=null?SWT.NO_BACKGROUND:0));
            shell.addPaintListener(this);
            shell.addMouseListener(new MouseListener(){

                @Override
                public void mouseDoubleClick (MouseEvent e) {  }

                @Override
                public void mouseDown (MouseEvent e) {
                    shell.dispose();             
                }

                @Override
                public void mouseUp (MouseEvent e) {}});
            if (image != null){
                Rectangle bounds = image.getBounds();
                shell.setSize(bounds.width,bounds.height);
            }
            else {
                shell.setSize(500,400);
            }
            Rectangle parentBounds = parent.getBounds();
            shell.setLocation(parentBounds.x + (parentBounds.width-shell.getSize().x)/2,
                              parentBounds.y + (parentBounds.height-shell.getSize().y)/2);
        }
        @Override
        public void paintControl (PaintEvent e) {
            GC gc = e.gc;
            gc.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
            Font font = new Font(shell.getDisplay(), "Ariel", 11, 0);
            Font saveFont = gc.getFont();
            gc.setFont(font);
            try {
                if (image != null)
                    gc.drawImage(image, 0, 0);
                for (Text t : list) {
                    String lines[] = t.text.split("\n");
                    int y = t.y;
                    for (String line: lines){
                        gc.drawText(line, t.x, y,SWT.DRAW_TRANSPARENT);
                        y += 20;
                    }
                }
            }
            finally {
                gc.setFont(saveFont);
            }
        }
        public void add(String text, int x, int y){
            list.add(new Text(x,y,text));
        }
        public void show(){
            shell.open();
        }
        private Shell shell;
        private Image image;
        private List<Text> list = new ArrayList<Text>();
            
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.display.AbstractCustomDisplayCallback#makeSplash(com.arc.widgets.IImage)
     */
    @Override
    protected Object makeSplash (IImage image) {
        return new SplashScreen(image!=null?(Image)image.getObject():null);
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.display.AbstractCustomDisplayCallback#setSplashText(java.lang.Object, int, int, java.lang.String)
     */
    @Override
    protected void setSplashText (Object dialog, int x, int y, String text) {
        ((SplashScreen)dialog).add(text,x,y);
        
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.display.AbstractCustomDisplayCallback#showSplashScreen(java.lang.Object)
     */
    @Override
    protected void showSplashScreen (Object dialog) {
        ((SplashScreen)dialog).show();
        
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.engine.display.AbstractCustomDisplayCallback#showConfirmDialog(java.lang.String)
     */
    @Override
    protected boolean showConfirmDialog (String msg) {
        return MessageDialog.openQuestion(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Confirm", msg);
    }
    
    @Override
    protected void showHelpForID(String id){
        IContext context = HelpSystem.getContext(id);
        if (context == null){
            UISeeCodePlugin.showError("Missing Help file","Help context ID \"" + id + "\" not recognized.");
        }
        else
            PlatformUI.getWorkbench().getHelpSystem().displayHelp(context);
    }

    @Override
    public void selectStackframe (int threadID, int stacklevel) {
        // TODO Auto-generated method stub
    	//What to do?
        
    }

    @Override
    public void recordInitState (int id, String state) {
        ISeeCodeTextViewer d = getDisplay(id);
        AbstractEngineBasedView view = SeeCodeCustomView.getDisplayFor(d);
        if (view instanceof SeeCodeCustomView){
            ((SeeCodeCustomView)view).recordInitState(state);
        }       
    }
}
