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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.help.HelpSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

import com.arc.cdt.debug.seecode.core.ISeeCodeLaunchConfigurationConstants;
import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.cdt.debug.seecode.ui.UISeeCodePlugin;
import com.arc.cdt.debug.seecode.ui.Utilities;
import com.arc.cdt.debug.seecode.ui.display.CustomDisplayCallback;
import com.arc.cdt.debug.seecode.ui.display.IViewToolBarBuilder;
import com.arc.mw.util.Cast;
import com.arc.seecode.display.IColorPreferences;
import com.arc.seecode.display.ISeeCodeTextViewer;
import com.arc.seecode.engine.EngineDisconnectedException;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;
import com.arc.seecode.engine.StackFrameRef;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;

/**
 * This view is to display a SeeCode custom display (e.g., History display).
 * There is one instance of this class for each kind of display. That is, it is
 * not possible to have more than one "Reg" display open.
 * <P>
 * Actually, the above statement isn't quite true. The engine can create
 * displays "on-the-fly" when, say, the user clicks the "disasm" button on a
 * Source display. When this happens, we create an instance of this view
 * dynamically by contriving a "secondaryID" that is the concatenation of the
 * display type and the "value-update" that caused the spontaneous display to appear, separated by a separator character.
 * This will force Eclipse to create a new instance instead of trying to
 * reactivate an old one.
 * <P>
 * Thus, there are two kinds of secondary Ids. See the Javadoc for
 * {@link #init(IViewSite,IMemento)}for details.
 * <P>
 * The contents of this display depends on which session is active in the Debug
 * View. So implement it with a "StackLayout" and select between which ever
 * session is selected.
 * <p>
 */

public class SeeCodeCustomView extends AbstractEngineBasedView implements
CustomDisplayCallback.IObserver {
    private static final String INIT_STATE = "initState";


    public static final String VIEW_ID = "com.arc.cdt.debug.seecode.ui.views.SeeCodeCustomView";
    
    public static final String DISASM_KIND = "disasm";
    
    public static final String SOURCE_KIND = "source";
    /**
     * For those displays that have more than one instance (e.g., Memory display), we
     * append a "#<em>ordinal</em>" to the secondary ID.
     */
    public static final String INSTANCE_SEPARATOR = "#";

    /**
     * When we're creating a spontaneous display, we encode it as an window ID
     * followed by unique junk so as to get a separate View instance. The junk
     * is separated by this character. (Can't use ":" because it is reserved
     * internally).
     */
    public static final String ID_SEPARATOR = "!";

    /**
     * To be set the kind of display by
     * 
     * {@link #init(IViewSite,IMemento) init()}. E.g. "history"
     */
    private String mKind = null;
    
    /**
     * If this is display kind for which there can be multiple instances (e.g., "mem"),
     * then this is an instance number that we may want to appear in the title.
     */
    private String mInstance = null;
    
    /**
     * Context help ID or <code>null</code> if the default derivation from the display "kind" is used.
     */
    private String mHelpID = null;

    /**
     * Maps the engine instance to the display ID
     */
    private Map<EngineInterface, Integer> mEngineToIdMap = new HashMap<EngineInterface, Integer>();

    /**
     * Maps the engine object to the display object.
     */
    private Map<EngineInterface, ISeeCodeTextViewer> mEngineToViewerMap = new HashMap<EngineInterface, ISeeCodeTextViewer>();

    private int mPreassignedID = 0;

    private static int sLastWindowID = 0;
    
    private static Map<ISeeCodeTextViewer,SeeCodeCustomView>sGlobalDisplayMap = new HashMap<ISeeCodeTextViewer,SeeCodeCustomView>();
    
    private String initState = null;

    /**
     * The constructor.
     */
    public SeeCodeCustomView() {
    }

    /**
     * The secondary ID in our case has two encodings:
     * <dl>
     * <dt><i>kind </i>
     * <dd>The kind of the display that this view instance is to show. The
     * engine's {@link EngineInterface#createDisplay createDisplay}method must
     * be invoked with this ID to actually materialize the contents.
     * <dt><i>id </i>: <i>unique-junk </i>
     * <dd>this corresponds to a display that the engine has created
     * spontaneously (e.g., by clicking "disasm" button on a Source display).
     * Thus, this display already has a pre-assigned ID, and the engine is
     * expecting it to be populated right away, which will be done by the
     * caller. We append "junk" so that we get a new View instance each time.
     *
     * </dl>
     */
    @Override
    public void init (IViewSite site, IMemento memento) throws PartInitException {
        super.init(site,memento);

        if (memento != null){
            initState = memento.getString(INIT_STATE);
        }
        String type = site.getSecondaryId();
        // If no secondary, this was invoked from View Menu
        if (type != null)
            setType(type);
    }
    
    protected void setType(String type){
        int i = type.indexOf(INSTANCE_SEPARATOR);
        if (i > 0) {
            mKind = type.substring(0,i);
            mInstance = type.substring(i+1);
        }
        else
            mKind = type;
        i = mKind.indexOf(ID_SEPARATOR);
        if (i > 0) {
            mNeedsPreassignedID = true;
            mPreassignedID = 0;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite)
     */
    @Override
    public void init(IViewSite site) throws PartInitException {
        init(site, null);
    }

    /**
     * Create a control to be displayed when no engine is selected.
     * Typically a blank panel.
     * @param parent the parent contorl
     * @return a control to be displayed if no engine is selected.
     */
    @Override
    protected Composite createNoEngineControl(Composite parent){
        Composite result = new Composite(parent,0);
        if (mKind == null){
            result.setLayout(new FillLayout());
            Label l = new Label(result,SWT.CENTER);
            l.setText("Use Debugger menu in the menubar\nto create debugger displays");
        }
        return result;      
    }

    private boolean mNeedsPreassignedID = false;

    private static final String VISIBLE_COLUMNS_KEY = "profilingColumns";
    private static final String VALUE_UPDATES_KEY = "valueUpdates";
    
    /**
     * Given an engine instance associated with a SeeCode session,
     * return the title associated with this view, or null, if there
     * is no title.
     * @param engine
     * @return the title associated with the view that is associated with
     * the SeeCode session using the engine instance.
     */
    @Override
    protected String computeViewTitle(EngineInterface engine){
        ISeeCodeTextViewer viewer = getViewer(engine);
        if (viewer != null) {
            String t = viewer.getTitle();
            if (mInstance != null) t = t + " #" + mInstance;
            return t;
        }
        return null;
    }
    
    /**
     * Called when the view contents is to be created on behalf of a SeeCode debug session associated with an engine
     * instance.
     * @param engine the engine associated with the SeeCode session.
     * @param control the container in which the view is to be materialized.
     */
    @Override
    protected void initEngineView (final EngineInterface engine, final Composite control) {

        CustomDisplayCallback callback = (CustomDisplayCallback) engine.getCustomDisplayCallback();
        callback.addObserver(this);

        if (!mNeedsPreassignedID) {
            final int id = ++sLastWindowID;
            wireDisplayID(id, engine, control);
            // Since implementing CMPD, we have a problem of deadlocks if the UI thread calls back into the
            // engine. The engine may be attempting a callback on behalf of another CMPD process, which
            // needs the UI freed up.
            // Thus, we invoke the engine initialization from a worker thread.
            Job job = new Job("Init " + getTitle() + " Display") {

                @Override
                protected IStatus run (IProgressMonitor monitor) {
                    try {
                        engine.createDisplay(id, mKind);
                        if (initState != null && engine.supportsDisplayInitStates()){
                            engine.sendValueUpdate(id,"init_state", initState);
                            initState = null; // Let engine regenerate it if it wants to.
                        }
                    }
                    catch (EngineDisconnectedException x) {
                        // Ignore; happens too often to diagnose
                    }
                    catch (EngineException x) {
                        SeeCodePlugin.log(x);
                    }
                    StackFrameRef sf = Utilities.getSelectedStackFrame();
                    if (sf != null && sf.getEngine() == engine) {
                        setStackFrame(engine, sf);
                    }
                    return Status.OK_STATUS;
                }
            };
            job.schedule();
        }
    }
    
    /**
     * Called when a particular stackframe is selected from the Debug view.
     * @param engine the associated SeeCode engine instance.
     * @param sf SeeCode stackframe instance that was just selected.
     */
    @Override
    protected void setStackFrame(EngineInterface engine, StackFrameRef sf){
        try {
            engine.setStackFrame(getDisplayIdFor(engine),sf);
        }
        catch (EngineDisconnectedException e){
            // engine shutdown while display being updated.
            // Consider it benign.
        }
        catch (EngineException e) {
            SeeCodePlugin.log(e);
        }
    }

    private void wireDisplayID(int displayID, EngineInterface engine, Composite control) {
        CustomDisplayCallback callback = (CustomDisplayCallback) engine
                .getCustomDisplayCallback();
        mEngineToIdMap.put(engine, new Integer(displayID));
        IContainer container = UISeeCodePlugin.getWidgetFactory().wrapContainer(
                control, IComponentFactory.COLUMN_STYLE);
        //When SeeCode engine calls through the callback
        // to create the display, we'll create it here.
        callback.setContainer(displayID, container);
    }

    /**
     * This is called for the case where this is a display that the engine has
     * spontaneously created as the result of, say, the user clicking the
     * "disasm" button on the Source display.
     * <P>
     * Its secondary ID is contrived so that it knows that it must wait for the
     * method to be called to receive the ID that is to correspond to this view.
     * <P>
     * The container for the display will be set in the
     * {@link CustomDisplayCallback#setContainer}method so that it will know
     * where to render the display.
     * 
     * @param engine 
     *            the associated engine.
     * @param id
     *            the preassigned display.
     */
    public void setPreassignedDisplayID(EngineInterface engine, int id) {
        if (id == 0)
            throw new IllegalArgumentException("id is zero");
        this.mPreassignedID = id;
        Integer idObj = mEngineToIdMap.get(engine);
        if (idObj != null) {
            if (idObj.intValue() != id) {
                //Shouldn't happen.
                SeeCodePlugin.log("Setting preassigned id " + id
                        + " on top of " + idObj);
            }
        }
        mEngineToIdMap.put(engine,new Integer(id));
        setEngineSource(engine);
        wireDisplayID(id, engine, getControl(engine));

    }
    
    @Override
    public String getHelpID(){
        if (mHelpID != null) return mHelpID;
        if (mKind != null) return IContextHelpIds.PREFIX + mKind + "_window";
        return null;
    }
    
    /**
     * Return the display ID number associated with
     * a particular engine instance.
     * @param engine
     * @return the display ID associated with engine, or 0
     * if not yet set.
     */
    public int getDisplayIdFor(EngineInterface engine){
        Integer idObj = mEngineToIdMap.get(engine);
        if (idObj == null) return 0;
        return idObj.intValue();       
    }
    
    private String computeConfigKey(String key){
        return "SeeCodeDisplay." + mKind + "." + key;
    }
    
    /**
     * Called to remove all references to an engine
     */
    @Override
    protected void unwireEngine(EngineInterface engine) {
        ISeeCodeTextViewer d = mEngineToViewerMap.get(engine);
        //Save the state of the display within the launch dialog so that
        // we can restore when it is restarted.
        ILaunch launch = computeLaunchFrom(engine);
        if (d != null && launch != null && launch.getLaunchConfiguration() != null){

            try {
                ILaunchConfigurationWorkingCopy config = launch.getLaunchConfiguration().getWorkingCopy();
                int visibleColumns = d.getVisibleColumnBitmap();
                //ARS0098415
                //config.setAttribute(computeConfigKey(VISIBLE_COLUMNS_KEY),visibleColumns);
                setConfigVisibleColumnAttribute(config, visibleColumns);
                Map<String,String> map = d.getValueUpdates();
                config.setAttribute(computeConfigKey(VALUE_UPDATES_KEY),map);
                config.doSave();
            }
            catch (CoreException e) {
                //couldn't save seecode states
            }
            
        }
        if (getEngine() == engine || mEngineToViewerMap.size() == 0 || getEngine() == null){
            clearToolBar(); // should blank it out
        }
        super.unwireEngine(engine);

        CustomDisplayCallback callback = (CustomDisplayCallback) engine
                .getCustomDisplayCallback();
        callback.removeObserver(this);
        ISeeCodeTextViewer viewer = mEngineToViewerMap.remove(engine);
        if (viewer != null && !viewer.isDisposed()){
            viewer.forceDispose();
        }
        mEngineToIdMap.remove(engine);
        
    }
    
    
    
    public int  getVisibleColumnAttribute(ILaunchConfiguration config) throws CoreException{
    	if(mKind == null)
    		return 0;

    	if(mKind.equals(DISASM_KIND) || mKind.equals(SOURCE_KIND))
    		return getVisibleColumnAttributeFilterByTarget(config);
    	return getVisibleColumnAttributeNoFilter(config);

    }

    public int  getVisibleColumnAttributeNoFilter(ILaunchConfiguration config) throws CoreException{
    	String value =  config.getAttribute(computeConfigKey(VISIBLE_COLUMNS_KEY), "0");
    	try{
    		return Integer.parseInt(value);
    	}catch (NumberFormatException ex){
    		return 0;
    	}

    }



    @SuppressWarnings("rawtypes")
    public int getVisibleColumnAttributeFilterByTarget(ILaunchConfiguration config) throws CoreException{
    	String name =  config.getAttribute(computeConfigKey(VISIBLE_COLUMNS_KEY), "0");
    	int index = name.indexOf(".");
    	if(name.indexOf(".") >0){
    		String columnIdxStr = name.substring(0, index);
    		String which_arc = name.substring(index+1);
    		if(which_arc!=null){
    			Map empty = new HashMap();
    			Map values = (Map)config.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_GUIHILI_PROPERTIES , empty);
    			String arcInfo = (String)values.get("which_arc");
    			if(which_arc.equals(arcInfo)){
    				try{
    					return Integer.parseInt(columnIdxStr);
    				}catch(NumberFormatException ex){
    					return 0;
    				}

    			}

    		}

    	}
    	// This is for the case of launching old launch profile with new code - we are still able to show
    	return getVisibleColumnAttributeNoFilter(config);
    }

    protected void setConfigVisibleColumnAttribute(ILaunchConfigurationWorkingCopy config, int visibleColumns) throws CoreException{

    	if(mKind == null)
    		return;

    	if(mKind.equals(DISASM_KIND) || mKind.equals(SOURCE_KIND))
    		setConfigVisibleColumnAttributeFilterByTarget(config, visibleColumns);
    	else
    		setConfigVisibleColumnAttributeNoFilter(config, visibleColumns);


    }

    protected void setConfigVisibleColumnAttributeNoFilter(ILaunchConfigurationWorkingCopy config, int visibleColumns) throws CoreException{
    	config.setAttribute(computeConfigKey(VISIBLE_COLUMNS_KEY),""+visibleColumns);
    }

    @SuppressWarnings("rawtypes")
    protected void setConfigVisibleColumnAttributeFilterByTarget(ILaunchConfigurationWorkingCopy config, int visibleColumns) throws CoreException{

    	Map empty = new HashMap();
    	Map values = (Map)config.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_GUIHILI_PROPERTIES , empty);
    	String arcInfo = (String)values.get("which_arc");
    	String columnStr = ""+visibleColumns;
    	if(arcInfo != null){
    		columnStr = visibleColumns + "." + arcInfo;
    	}
    	config.setAttribute(computeConfigKey(VISIBLE_COLUMNS_KEY),columnStr);
    	
    	

    }


    /**
     * Return whether or not this view is to be done away with when
     * its associated engine is disconnected. 
     * @return whether or not this view is to be done away with when
     * its associated engine is disconnected. 
     */
    @Override
    protected boolean isTransientView () {
        return mPreassignedID != 0;
    } 

    /**
     * Called when engine connection to this view is to be terminated
     * due to the view being closed.
     * @param engine the associated SeeCode engine
     */
    @Override
    protected void onDispose (EngineInterface engine) {
        ISeeCodeTextViewer v = mEngineToViewerMap.get(engine);
        if (v != null) {
            // Forceably close the display and handle
            // matters if the engine is in the process of
            // updating it...
            // NOTE: it will cause onDeleted() to
            // be called on behalf of the display,
            // the mDisposing setting prevents it from
            // deleting things underneath us.
            v.forceDispose();
        }
    }

    @Override
    public void onTitleChanged(ISeeCodeTextViewer viewer, EngineInterface engine) {
        if (getViewer(getEngine()) == viewer) {
            String title = viewer.getTitle();
            setPartNameFromTitle(title);
        }

    }

    /**
     * @param title
     */
    @Override
    protected void setPartNameFromTitle(String title) {
        int i = title.indexOf(':');
        if (i > 0) {
            String t = title.substring(0,i);
            if (mInstance != null)
                t = t + " #" + mInstance;
            setPartName(t);
            setContentDescription(title.substring(i + 1));
        } else {
            if (mInstance != null)
                title = title + " #" + mInstance;
            setPartName(title);
            setContentDescription("");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.cdt.debug.seecode.display.CustomDisplayCallback.IObserver#onDeleted(com.arc.seecode.display.ISeeCodeTextViewer)
     */
    @Override
    public void onDeleted(ISeeCodeTextViewer viewer, EngineInterface engine) {
        //If the display is being deleted implicitly by the engine, say, as
        // in the response to deleting a parent display, then we must set
        // it as "deleted" so that it knows that a "close" operation isn't
        // necessary. Otherwise, we'd get "invalid display id" errors from the
        // engine when we attempt to close a deleted frame.
        viewer.setDeleted();
        sGlobalDisplayMap.remove(viewer);
        // If this event is fired as a result of
        // forceably disposing of the viewer as a result
        // of eclipse shutdown, then avoid disposing
        // of anything. We're being called from the DisposeListener
        // of this view!!!
        if (getViewer(engine) == viewer) {
            Composite c = getControl(engine);
            if (!c.isDisposed()) {
                c.dispose();
            }
           
        }
    }
    
	private void setHelpStuff(ISeeCodeTextViewer viewer) {
		String helpID = viewer.getHelpID();
		if (helpID != null) {
			// Canonicalize...
			if (helpID.indexOf('.') < 0)
				helpID = UISeeCodePlugin.PLUGIN_ID + "." + helpID;
			String id2 = viewer.getDefaultHelpID();
			if (id2 != null && HelpSystem.getContext(helpID) == null)
				helpID = UISeeCodePlugin.PLUGIN_ID + "." + id2;
			this.setHelpID(helpID);
		}
	}

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.cdt.debug.seecode.display.CustomDisplayCallback.IObserver#onCreated(com.arc.seecode.display.ISeeCodeTextViewer)
     */
    @Override
    public void onCreated(ISeeCodeTextViewer viewer, EngineInterface engine) {
        Integer idObject = mEngineToIdMap.get(engine);
        if (idObject != null && idObject.intValue() == viewer.getDisplayID()) { // should always be true
            setPartName(viewer.getTitle());
            if (getFont() != null)
                viewer.setFont(getFont());
            if (getColor() != null) {
                IColorPreferences prefs = getColor();
                viewer.setColorPreferences(prefs);
            }
            IProject project = getProject();
            if (project != null)
                viewer.setDefaultDirectory(project.getLocation().toFile());
            mEngineToViewerMap.put(engine, viewer);
            restoreAttributes(viewer);
            Composite c = getControl(engine);
            c.layout(true);
            c.redraw();
            refreshToolBars();
            //For the sake of the GUI tester, we need to name the underlaying
            // "text canvas" widget, so that it can find it.
            viewer.setName(mInstance != null?mKind+"#" + mInstance:mKind);
            sGlobalDisplayMap.put(viewer,this);
            setHelpStuff(viewer);
            wireInHelp(c);
        }
    }
    
    private void restoreAttributes (ISeeCodeTextViewer viewer) {
        ILaunch launch = getLaunch();
        if (launch == null) return; // shouldn't happen
        ILaunchConfiguration config = launch.getLaunchConfiguration();
        try {
        	////ARS0098415
            //int visibleColumnBitMap = config.getAttribute(computeConfigKey(VISIBLE_COLUMNS_KEY),0);
        	int visibleColumnBitMap = getVisibleColumnAttribute(config);
        	viewer.setVisibleColumnBitmap(visibleColumnBitMap);
        	//ignore the old cache for the below cases
            if( (mKind.equals(DISASM_KIND) || mKind.equals(SOURCE_KIND)) && visibleColumnBitMap == 0) 
            	return;
            	
            Map<String,String> map = Cast.toType(config.getAttribute(computeConfigKey(VALUE_UPDATES_KEY),(Map<String,String>)null));
            if (map != null) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    viewer.sendValueUpdate(entry.getKey(), entry.getValue());
                    try {
                        viewer.inputText(entry.getKey(),entry.getValue(),false);
                    } catch(IllegalArgumentException x){
                        // If for some reason we cannot update the widget with the
                        // saved value, don't worry about it. It didn't use to do 
                        // it at all prior to cr22380.
                    }
                }
            }
        }
        catch (CoreException e) {
            //shouldn't happen
        }
    }
       
    
    /**
     * Given a seecode display, return the view in which it is located.
     * @todo davidp needs to add a method comment.
     * @param v the seecode display.
     * @return the view in which the given seecode display is located.
     */
    public static AbstractEngineBasedView getDisplayFor(ISeeCodeTextViewer v){
        return sGlobalDisplayMap.get(v);       
    }

    private ISeeCodeTextViewer getViewer(EngineInterface engine) {
        return mEngineToViewerMap.get(engine);
    }
    
    public ISeeCodeTextViewer getViewer(){
        if (getEngine() != null) return getViewer(getEngine());
        return null;
    }

 

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        mEngineToIdMap.clear();
        mEngineToViewerMap.clear();
    }
    
    @Override
    protected boolean stateIsToBeSaved(){
        return mPreassignedID == 0;
    }
    
    /**
     * Called when the font has changed.
     */
    @Override
    protected void onFontChanged (Font font) {
        super.onFontChanged(font);
        for (ISeeCodeTextViewer d: mEngineToViewerMap.values()){
            d.setFont(font);
        }
    }
    
    @Override
    protected void onColorChanged (IColorPreferences prefs) {
        super.onColorChanged(prefs);
        for (ISeeCodeTextViewer d: mEngineToViewerMap.values()){
            d.setColorPreferences(prefs);
        }
    }

    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     */
    @Override
    protected void refresh() {
        super.refresh();
        refreshToolBars();
    }

    /**
     * Regenerate the toolbar for this view. It is called when the
     * view is first attached to a debugger display, and when selecting between
     * displays associated with diffenent sessions.
     */
    private void refreshToolBars() {
        ISeeCodeTextViewer tv = getViewer();
        if (tv != null) {
            IViewToolBarBuilder tb = (IViewToolBarBuilder) tv
                    .getToolBarBuilder();
            if (tb != null) {
                tb.populateView(this);
            }
        }
        else {
            clearToolBar();
        }
    }
    
    protected void setHelpID(String id){
        mHelpID = id;
    }

    @Override
    public void saveState (IMemento memento) {
        super.saveState(memento);
        if (this.stateIsToBeSaved() && initState != null){
            memento.putString(INIT_STATE, initState);
        }
    }
    public String getInitState() { return initState; }
    
    public void recordInitState(String state){
        initState = state;
    }
}
