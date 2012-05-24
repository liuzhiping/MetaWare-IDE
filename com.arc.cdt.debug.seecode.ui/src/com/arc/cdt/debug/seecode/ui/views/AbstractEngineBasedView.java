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
package com.arc.cdt.debug.seecode.ui.views;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.cdt.debug.seecode.ui.UISeeCodePlugin;
import com.arc.cdt.debug.seecode.ui.Utilities;
import com.arc.seecode.display.IColorPreferences;
import com.arc.seecode.engine.EngineInterface;
import com.arc.seecode.engine.StackFrameRef;


/**
 * A view that is based on a SeeCode engine instantiation.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public abstract class AbstractEngineBasedView extends ViewPart implements IPropertyChangeListener{
    private static final String TITLE = "title";

    private static final String TIP = "tip";
    
    private IDebugEventSetListener mDebugEventListener;
    private EngineInterface mEngine = null;
    private boolean mUnwired = false;

    private StackLayout mCardLayout;

    private Composite mBlank;

    private Composite mCards;
    
    private IDebugContextListener mDebugContextListener = new IDebugContextListener(){

        @Override
        public void debugContextChanged (DebugContextEvent event) {
            if (fIsVisible)
                setSelection(event.getContext());
            
        }};

    //private ICDIStackFrame mStackFrame; // selected stack frame
    
    //private boolean mDisposing = false; // set to true when disposing

    /**
     * Maps the engine instance to the control that displays the SeeCode
     * display.
     *  
     */
    private Map<EngineInterface, Composite> mControlMap = new HashMap<EngineInterface, Composite>();

    private Font mFont = null;

    private ILaunch mLaunch = null;

    private IProject mProject = null;

    private ICDISession mSession;

    private boolean fIsVisible = false;
    
    private ICDITarget mTarget;

    private ICDIThread mThread = null;

    private IPartListener2 fPartListener = new IPartListener2(){

        @Override
        public void partActivated (IWorkbenchPartReference partRef) {
            // @todo Auto-generated method stub
            
        }

        @Override
        public void partBroughtToTop (IWorkbenchPartReference partRef) {
            // @todo Auto-generated method stub
            
        }

        @Override
        public void partClosed (IWorkbenchPartReference partRef) {
            // @todo Auto-generated method stub
            
        }

        @Override
        public void partDeactivated (IWorkbenchPartReference partRef) {
            // @todo Auto-generated method stub
            
        }

        @Override
        public void partHidden (IWorkbenchPartReference partRef) {
            if (partRef.getPart(false) == AbstractEngineBasedView.this){
                //System.out.println("HIDDEN");
                fIsVisible = false;
            }
            
        }

        @Override
        public void partInputChanged (IWorkbenchPartReference partRef) {
            // @todo Auto-generated method stub
            
        }

        @Override
        public void partOpened (IWorkbenchPartReference partRef) {
            // @todo Auto-generated method stub
            
        }

        @Override
        public void partVisible (IWorkbenchPartReference partRef) {
            if (partRef.getPart(false) == AbstractEngineBasedView.this){
                fIsVisible = true;
                //System.out.println("SHOWN");
                resetSelection();
            }
            
        }};
    
    /**
     * The constructor
     */
    public AbstractEngineBasedView() {
        super();
    }
    
    protected static ILaunch computeLaunchFrom (Object element) {
        if (element instanceof IAdaptable) {
            ICDISession session = (ICDISession) ((IAdaptable) element)
                    .getAdapter(ICDISession.class);
            if (session instanceof IAdaptable) {
                ILaunch launch = (ILaunch) ((IAdaptable) session)
                        .getAdapter(ILaunch.class);
                return launch;
            }
        }
        return null;
    }
    
    protected static ILaunch computeLaunchFrom(EngineInterface engine){
        ICDITarget target = SeeCodePlugin.getEngineTarget(engine);
        if (target != null){
            ICDISession session = target.getSession();
            if (session instanceof IAdaptable){
                return (ILaunch)((IAdaptable)session).getAdapter(ILaunch.class);
            }
        }
        return null;
    }
    
    protected static ICDISession computeSessionFrom (Object element) {
        if (element instanceof IAdaptable) {
            return (ICDISession) ((IAdaptable) element)
                    .getAdapter(ICDISession.class);
        }
        return null;
    }
    
    protected static IProject computeProjectFrom (Object element) {
        if (element instanceof IAdaptable) {
            ICDISession session = (ICDISession) ((IAdaptable) element)
                    .getAdapter(ICDISession.class);
            if (session instanceof IAdaptable) {
                IProject project = (IProject) ((IAdaptable) session)
                        .getAdapter(IProject.class);
                return project;
            }
        }
        return null;
    }
    
    protected static ICDITarget computeTargetFrom(Object element) {
        if (element instanceof IAdaptable) {
            ICDITarget t = (ICDITarget) ((IAdaptable) element)
                    .getAdapter(ICDITarget.class);
            if (t != null) return t;
            ICDISession s = computeSessionFrom(element);
            if (s instanceof IAdaptable){
                ICDITarget targets[] = s.getTargets();
                if (targets.length > 0) return targets[0];
            }
        }
        return null;
    }

 
    
    /**
     * Create a control to be displayed when no engine is selected.
     * Typically a blank panel.
     * @param parent the parent contorl
     * @return a control to be displayed if no engine is selected.
     */
    protected Composite createNoEngineControl(Composite parent){
        return new Composite(parent,0);
    }
    
    @Override
    public void init (IViewSite site, IMemento memento) throws PartInitException {
        super.init(site);
        if (memento != null) {
            String title = memento.getString(TITLE);
            String tip = memento.getString(TIP);
            if (title != null)
                setPartName(title);
            if (tip != null)
                setContentDescription(tip);
        }

        mDebugEventListener = new IDebugEventSetListener() {

            @Override
            public void handleDebugEvents (DebugEvent[] events) {
                for (int j = 0; j < events.length; j++) {
                    DebugEvent event = events[j];
                    switch (event.getKind()) {
                    case DebugEvent.TERMINATE:
                        handleTerminateEvent(event);
                        break;
                    default:
                        break;
                    }
                }

            }

        };
        DebugPlugin.getDefault().addDebugEventListener(mDebugEventListener);
        
        setFont();
        setColor();
        JFaceResources.getFontRegistry().addListener( this );
        JFaceResources.getColorRegistry().addListener( this );
    }
    
    /**
     * Called whenever there is evidence that the font for this display has
     * been changed, presumbly from the Preference dialog.
     * Subclasses can override this to do something interesting, provided that
     * they call
     */
    protected void onFontChanged(Font font){}
    
    /**
     * Called whenever the foreground color (i.e., color of text) changes.
     * @param color the new foreground color
     */
    protected void onColorChanged(IColorPreferences color){}
    
    private void setFont(){
        mFont = UISeeCodePlugin.getSeeCodeFont();
        onFontChanged(mFont);
    }
    
    private void setColor(){
        onColorChanged(UISeeCodePlugin.getColorPreferences());
    }
    
    protected Font getFont(){
        return mFont;
    }
    
    protected IColorPreferences getColor(){
        return UISeeCodePlugin.getColorPreferences();
    }
    
    /**
     * Called when engine is shutting down from the UI thread.
     * @param engine the engine being shutdown or disconnected from this
     * view.
     */
    protected void unwireEngine(EngineInterface engine){
        Composite c = mControlMap.remove(engine);
        if (c != null) {
        	 if (mCardLayout.topControl == c) {
                 mCardLayout.topControl = mBlank;
             }
            // can't dispose while in the Dispose listener of a parent!
            if (!c.isDisposed()){
                c.dispose();
            }         
        }
        if (mEngine == engine) {
            setEngineSource((EngineInterface) null);
        }
    }
    
    private void handleTerminateEvent(DebugEvent event) {
        //NOTE: this is likely not the main UI thread!
        Object src = event.getSource();
        if (!mCards.isDisposed() && src instanceof IDebugTarget) {
            ICDITarget target = (ICDITarget) ((IDebugTarget) src)
                    .getAdapter(ICDITarget.class);
            if (target != null && target instanceof IAdaptable) {
                final EngineInterface e = (EngineInterface) ((IAdaptable) target)
                        .getAdapter(EngineInterface.class);
                if (e != null) {
                    // To avoid race conditions, run this stuff
                    // in main UI thread.
                    mCards.getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            unwireEngine(e);
                            disposeViewIfTransient(mCards);
                        }
                    });

                }
            }
        }
    }

    @Override
    public void saveState (IMemento memento) {
        super.saveState(memento);
        // Don't save state of spontaneous displays...
        if (this.stateIsToBeSaved()) {
            memento.putString(TITLE, getPartName());
            memento.putString(TIP, getContentDescription());
        }
    }
    
    protected boolean stateIsToBeSaved() { return true; }

    @Override
    public void dispose() {
        getSite().getPage().removePartListener(fPartListener);
        super.dispose();   
        if (!mUnwired) {
            mUnwired = true;
            IWorkbenchWindow w = this.getViewSite().getWorkbenchWindow();
            DebugUITools.getDebugContextManager().getContextService(w).removeDebugContextListener(mDebugContextListener);
            mEngine = null;
            mControlMap.clear();
            DebugPlugin.getDefault().removeDebugEventListener(mDebugEventListener);
            JFaceResources.getFontRegistry().removeListener( this );
            JFaceResources.getColorRegistry().removeListener( this );
        }
    }

    @Override
    public void createPartControl (Composite parent) {
        mCards = new Composite(parent, 0);
        mCardLayout = new StackLayout();
        mCards.setLayout(mCardLayout);
        mBlank = createNoEngineControl(mCards);
        mCardLayout.topControl = mBlank;
        // listen to selection in debug view
        IWorkbenchWindow w = this.getViewSite().getWorkbenchWindow();
        DebugUITools.getDebugContextManager().getContextService(w).addDebugContextListener(mDebugContextListener);
        getSite().getPage().addPartListener(fPartListener);
    }
    
    private void resetSelection(){
        setSelection(getSite().getPage().getSelection(IDebugUIConstants.ID_DEBUG_VIEW));
    }

    protected void clearDisplay () {
        mCardLayout.topControl = mBlank;
        if (!mCards.isDisposed()) {
            mCards.layout();
            mCards.redraw();
        }
    }

    public void setEngineSource (EngineInterface engine) {
        if (mEngine != engine /*&& mKind != null*/) {
            mEngine = engine;
            refresh();
        }
    }

    protected void setSelection (ISelection selection) {
        if (selection instanceof IStructuredSelection){
            setEngineSource((IStructuredSelection)selection);
        }
    }
    


    protected void setEngineSource (final IStructuredSelection selection) {
        if (fIsVisible && !selection.isEmpty()) {
            // At this point, we are NOT in the UI thread.
            final Object element = selection.getFirstElement();
            final EngineInterface engine = Utilities.computeEngineFromSelection(selection);
            if (engine != null) {
                // We may be making calls into the engine to get threads, stackframes, etc.
                // If we're the UI thread and the engine hangs then the whole GUI hangs.
                // Thus, we invoke it from another thread.
                if (Thread.currentThread() == mCards.getDisplay().getThread()) {
                    engine.enqueue(new Runnable() {
                        @Override
                        public void run() {
                            setEngineSourceInternal(element,engine);
                        }
                    },this);
                    return;
                }
            }
            setEngineSourceInternal(element, engine);
        }
    }

    private void setEngineSourceInternal (Object element, final EngineInterface engine) {
        // At this point, we're in the engine enqueue thread.

        final ICDIStackFrame sf = engine != null ? Utilities.computeStackFrameFrom(element) : null;

        mLaunch = computeLaunchFrom(element);
        mSession = computeSessionFrom(element);
        mProject = computeProjectFrom(element);
        ICDITarget t = computeTargetFrom(element);
        mTarget = t;
        mThread = Utilities.computeCDIThreadFromSelection(element);

        // Now, go back into the UI thread to select the result.
        mCards.getDisplay().asyncExec(new Runnable() {
            @Override
            public void run () {
                // Discovered that as a CMPD session is terminating, terminating processes will temporarily
                // be selected before engine.isShutdown() has had time to return true. If we don't
                // catch this, the debugger will be queried to re-create a display that it just terminated.
                if (mTarget != null && !mTarget.isTerminated())
                    setEngineSelection(sf, engine);
                else {
                	mEngine = null;
                    clearDisplay();
                }
            }
        });
    }

    private void setEngineSelection (ICDIStackFrame sf, EngineInterface engine) {
        setEngineSource(engine);
        if (engine != null){           
            if (sf instanceof IAdaptable){
                StackFrameRef scref = (StackFrameRef)((IAdaptable)sf).getAdapter(StackFrameRef.class);
                this.setStackFrame(engine,scref);
                //mStackFrame = sf;   
            }
        }
        else if (mEngine != null && mEngine.isShutdown()){
            // Shouldn't get here unless the engine shutdown
            // before closing the display window.
            unwireEngine(mEngine);
        }
    }
    
    /**
     * Called when a particular stackframe is selected from the
     * Debug view.
     * @param engine the associated SeeCode engine instance.
     * @param sf SeeCode stackframe instance that was just selected.
     */
    protected void setStackFrame(EngineInterface engine, StackFrameRef sf){
        // nothing to do by default.
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        mCards.setFocus();
    }
    
    protected void refresh() {
        if (mEngine == null)
            clearDisplay();
        else {
            Composite control = mControlMap.get(mEngine);
            if (control == null && !mEngine.isShutdown()) {
                control = new Composite(mCards, 0);
                control.setLayout(new GridLayout());
                mControlMap.put(mEngine, control);

 
                final Composite container_ = control;

                control.addDisposeListener(new DisposeListener() {

                    @Override
                    public void widgetDisposed(DisposeEvent e) {
                        onDispose(container_);

                    }
                });
                
                    
                initEngineView(mEngine, control);                 
            }
            setPage(control);
        }
    }
    
    /**
     * Wire in context-sensitive help when the display is fully populated.
     * Called by subclasses.
     * @param control the composite containing the display.
     */
    protected void wireInHelp(Control control) {
        String helpID = getHelpID();
        if (helpID != null)
            PlatformUI.getWorkbench().getHelpSystem().setHelp(control,helpID);       
    }

    /**
     * Show the given page.
     * @param control the page to show.
     */
    protected void setPage (Composite control) {
        String title = computeViewTitle(mEngine);
        if (title != null)
            setPartName(title);
        mCardLayout.topControl = control;
        mCards.layout();
        mCards.redraw();
    }
    
    /**
     * Return the help ID for this display, or null if there is
     * no associated help.
     * @return the help ID for this display, or null if there is
     * no associated help.
     */
    abstract protected String getHelpID();
    
    /**
     * Compute the view title associated with the SeeCode-related display
     * that this view represents.
     * @param engine the engine instance associated with the selected
     * seecode session.
     * @return the associated view title.
     */
    abstract protected String computeViewTitle(EngineInterface engine);
    
    /**
     * Given a title that is somehow associated with the SeeCode session
     * set the part name and the content description.
     * @param title the title.
     */
    protected void setPartNameFromTitle(String title) {
        setPartName(title);
        setContentDescription("");
    }
    
    /**
     * Called when the view contents is to be created on behalf of a
     * SeeCode debug session associated with an engine instance.
     * @param engine the engine associated with the SeeCode session.
     * @param control the container in which the view is to be materialized.
     */
    protected abstract void initEngineView(EngineInterface engine, Composite control);
    
    /**
     * Called when engine connection to this view is to be terminated
     * due to the view being closed.
     * @param engine the associated SeeCode engine
     */
    protected void onDispose (EngineInterface engine) {}
    
    /**
     * Return whether or not this view is to be done away with when
     * its associated engine is disconnected. 
     * @return whether or not this view is to be done away with when
     * its associated engine is disconnected. 
     */
    protected boolean isTransientView () { return false; }
    
    /**
     * Called when view is being closed, but before it is actually closed.
     * 
     * @param container
     */
    protected void onDispose (Composite container) {
        for (Map.Entry<EngineInterface, Composite> entry : mControlMap.entrySet()) {
            if (entry.getValue() == container) {
                EngineInterface engine = entry.getKey();
                onDispose(engine);
                unwireEngine(engine);
                // If this was a transient View that was the
                // result of the engine creating a spontaneous
                // display, get rid of the view.
                disposeViewIfTransient(container);
                break;
            }
        }
    }

	private void disposeViewIfTransient(Composite container) {
		if (isTransientView() && mControlMap.size() == 0) {
		    // Can't call it directly! We're processing the
		    // dispose event of one of the children of
		    // this display. If we directly call "hideView",
		    // it will dispose of the parent before the
		    // child is unwired and cause NPE!
		    //
		    // NOTE: we occasionally get here after container is disposed.
		    // Don't know why, but we check.
		    if (!container.isDisposed()) {
		        container.getDisplay().asyncExec(new Runnable() {

		            @Override
                    public void run () {
		                getSite().getPage().hideView(AbstractEngineBasedView.this);
		            }
		        });
		    }
		}
	}
    
    /**
     * Return the SWT control that contains the widget associated with the SeeCode engine instance.
     * @param engine the seecode engine instance.
     * @return Return the SWT control that contains the widget associated with the SeeCode engine instance.
     */
    protected final Composite getControl(EngineInterface engine){
        return mControlMap.get(engine);
    }
    
    /**
     * @return the engine instance associated with the selected SeeCode
     * session.
     */
    protected final EngineInterface getEngine(){
        return mEngine;
    }
    
    /**
     * @return the selected target.
     */
    protected final ICDITarget getTarget(){
        return mTarget;
    }
    
    /**
     * Return the selected thread.
     * @return the selected thread.
     */
    protected final ICDIThread getThread(){
        return mThread ;
    }
    
    /**
     * Return the debugger launch that is currently active.
     * @return the debugger launch that is currently active.
     */
    protected final ILaunch getLaunch(){
        return mLaunch;
    }
    
    /**
     * Return the selected debug session, or null if one isn't set.
     */
    protected final ICDISession getSession(){
        return mSession;
    }
    
    /**
     * Return the project that is associated with the launch that is
     * running.
     * @return the project that is associated with the launch that is
     * running.
     */
    protected final IProject getProject(){
        return mProject;
    }

    /**
     * Called when a Font or Color property changed from a preference dialog.
     */
    @Override
    public void propertyChange (PropertyChangeEvent event) {
        String prop = event.getProperty();
        if (prop.equals(UISeeCodePlugin.SEECODE_FONT)){
            setFont();
        }
        else if (UISeeCodePlugin.isKnownColorProperty(prop)){
            setColor();
        }
    }

    /**
     * Clear the toolbar
     */
    protected void clearToolBar () {
        IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
        toolBarManager.removeAll();
        toolBarManager.update(true);
    }
    
}
