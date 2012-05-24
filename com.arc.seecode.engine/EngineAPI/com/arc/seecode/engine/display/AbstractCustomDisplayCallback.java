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
package com.arc.seecode.engine.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.arc.mw.util.ITimedUpdate;
import com.arc.mw.util.StringUtil;
import com.arc.mw.util.UpdateTimer;
import com.arc.seecode.command.CommandFactory;
import com.arc.seecode.command.ICommandProcessor;
import com.arc.seecode.display.Factory;
import com.arc.seecode.display.IContext;
import com.arc.seecode.display.ISeeCodeTextViewer;
import com.arc.seecode.display.ISeeCodeTextViewerCallback;
import com.arc.seecode.display.ISeeCodeTextViewerFactory;
import com.arc.seecode.display.IToolBarBuilderFactory;
import com.arc.seecode.display.MenuDescriptor;
import com.arc.seecode.display.MenuDescriptor.ITopLevelCallback;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;
import com.arc.seecode.engine.EvaluationException;
import com.arc.seecode.engine.ICustomDisplayCallback;
import com.arc.seecode.engine.StackFrameRef;
import com.arc.widgets.IButton;
import com.arc.widgets.IComponent;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;
import com.arc.widgets.IFileChooser;
import com.arc.widgets.IImage;
import com.arc.widgets.ILabel;
import com.arc.widgets.IList;
import com.arc.widgets.ITextField;
import com.arc.widgets.IWindow;

/**
 * A convenient base class for the {@link ICustomDisplayCallback}interface by
 * which the SeeCode engine sends display-related directives to the user
 * interface.
 * 
 * @author David Pickens
 */
public abstract class AbstractCustomDisplayCallback implements
		IDisplayCreator,ICustomDisplayCallback, IHelpAssociator {
   
    private ISeeCodeTextViewerFactory mFactory;

    protected ISeeCodeTextViewerCallback mSeeCodeTextViewerCallback;

    private UpdateTimer mUpdateTimer = null;

    private int mTimerValue;

    private EngineInterface mEngine;

    private Map<Integer,Object> mProgressDisplays = null;

    private IComponentFactory mWidgetFactory;
    
    private ICommandProcessor mCommandProcessor = null;

	private String mSCDIR;
	
	/**
	 * Indicate whether or not the debugger engine can handle watchpoints with
	 * mask and value. This determines whether or not mask and value fields will
	 * appear in the watchpoint dialog that is generated from the watchpoint display.
	 */
	private boolean mWatchpointValuesSupported = false;



    /**
     * 
     * @param f
     *            component factory for making displays
     * @param tbFactory a factory for creating toolbar builder, or <code>null</code>
     * if default is to be sued.
     * @param engine
     *            the associated engine connection.
     * @param scdir the path to the associated SeeCode installation directory (so that we can
     * read guihili files from there).
     * @param displayCreator callback for activating displays by kind.
     */
    protected AbstractCustomDisplayCallback(IComponentFactory f,
            IToolBarBuilderFactory tbFactory,
            EngineInterface engine,
            String scdir) {
        if (f == null || tbFactory == null || engine == null || scdir == null){
            throw new IllegalArgumentException("Arguments must not be null");
        }
        mFactory = Factory.createTextDisplayViewFactory(f,tbFactory, new MyContext(engine),scdir,
        		this);
        mEngine = engine;
        mWidgetFactory = f;
        mSeeCodeTextViewerCallback = makeViewerCallback();
        mSCDIR = scdir;
    }
    
    class MyContext implements IContext {
        private EngineInterface fEngine;
        private HashMap<String, Integer> fRegMap = null;
        MyContext(EngineInterface engine){
            fEngine = engine;
        }
        @Override
        public IThread[] getThreads () {
            final int[] threadIDs;
            try {
                threadIDs = fEngine.getThreads();
            }
            catch (EngineException e) {
                return new IThread[0];
            }
            IThread[] threads = new IThread[threadIDs.length];
            for (int i = 0; i < threads.length; i++){
                final int index = threadIDs[i];
                threads[i] = new IThread() {

                    @Override
                    public int getIndex () {
                        return index;
                    }

                    @Override
                    public String getName () {
                        try {
                            return fEngine.getThreadName(index);
                        }
                        catch (EngineException e) {
                            return "thread " + getIndex();
                        }
                    }
                    
                };
            }
            return threads; 
        }
        @Override
        public void setWatchpoint (String expr, int len, String condition, int threadID, 
            int flags, long mask, long value) {
            String[] attributes = convertToAttributes(mask, value);
            try {
                mEngine.getBreakpointManager().create(expr, len, condition, threadID, flags, getStackFrameRef(), true, attributes);
            }
            catch (EngineException e) {
                internalError(null,"While setting watchpoint: " + e.getMessage(),e);
            }
            catch (EvaluationException e) {
                notifyError(null,e.getMessage(),"Watchpoint creation failure");
            }          
        }
        private String[] convertToAttributes (long mask, long value) {
            String attributes[] = null;
            if (mask != 0){
                attributes = new String[]{"mask=0x" + Long.toHexString(mask),"value=0x"+Long.toHexString(value)};
            }
            return attributes;
        }
        
        @Override
        public void setWatchpointReg (String regName, String condition, int threadID, 
            int flags, long mask, long value) {          
            int regID = lookupRegName(regName);
            if (regID == -1) throw new IllegalArgumentException("Unrecognized register name: " + regName);
            String[] attributes = convertToAttributes(mask, value);
            
            try {
                mEngine.createWatchpointReg(regID,condition, threadID, flags, attributes);
            }
            catch (EngineException e) {
                internalError(null,"While setting watchpoint: " + e.getMessage(),e);
            }          
        }
        @Override
        public boolean supportsWatchpointMask () {
            return mWatchpointValuesSupported;
        }    
        
        @Override
        public void displayError(String msg, String title){
            notifyError(null,msg,"Watchpoint set error");
        }
        
        /**
         * Lookup register name and return its corresponding register ID.
         * Returns -1 if the name is not valid.
         * @param regName register name to lookup.
         * @return the register ID or -1.
         */
        protected int lookupRegName(String regName){
            if (fRegMap == null){
                fRegMap = new HashMap<String,Integer>();
                try {
                    int bankCount = mEngine.getRegisterBankCount();
                    for (int bank = 0; bank < bankCount; bank++){
                        int regs[] = mEngine.getRegisterIDsFromBank(bank);
                        for (int r: regs){
                            String name = mEngine.getRegisterName(r);
                            if (name.startsWith("%")) name = name.substring(1);
                            fRegMap.put(name,new Integer(r));
                        }
                    }
                }
                catch (EngineException e) {
                    return -1;
                }
            }
            if (regName.startsWith("%")) regName = regName.substring(1);
            Integer r = fRegMap.get(regName);
            return r != null?r.intValue():-1;
            
        }
        @Override
        public void setHelpID (IComponent widget, String helpID) {
            AbstractCustomDisplayCallback.this.setHelp(widget,helpID);         
        }
        @Override
        public Object getShell () {
            return getDialogOwner();
        }
        @Override
        public String getEngineBuildID () {
            try {
                return mEngine.getEngineVersionStrings()[2];
            }
            catch (EngineException e) {
               return "1000"; // shouldn't get here
            }
        }
    }
    
 
    
    /**
     * Return the current stackframe. Subclasses should actually return the
     * stack frame being referenced from the debug view.
     * @return the current stackframe that watch- or break-point expressions are
     * to be evaluated in; or else 0 if no stackframe context can be determined.
     */
    protected StackFrameRef getStackFrameRef(){
        int tid = mEngine.getCurrentThread();
        if (tid != 0) {
            try {
                return mEngine.getTopStackFrame(tid);
            }
            catch (EngineException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * Override if a different callback is to be used.
     * @return the viewer callback object.
     */
    protected ISeeCodeTextViewerCallback makeViewerCallback(){
        return new SeeCodeTextViewerCallback(mEngine, this, getDisplayCreator());
    }
    
    protected EngineInterface getEngine(){
        return mEngine;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.ICustomDisplayCallback#createDisplay(java.lang.String,
     *      int)
     */
    @Override
    public void createDisplay(String properties, int id) {
        ByteArrayInputStream input = new ByteArrayInputStream(properties
                .getBytes());
        Properties props = new Properties();
        try {
            props.load(input);
        } catch (IOException e) {
            mSeeCodeTextViewerCallback.internalError(null, e.getMessage(), e);
            return;
        }
        try {
            mFactory.createDisplay(id, props, getContainerForNewDisplay(id),
                    mSeeCodeTextViewerCallback);
        } catch (RuntimeException e1) {
            internalError(null,e1.getMessage(),e1);
        }
    }

    /**
     * Return the container that is appropriate to place the new display in.
     * 
     * @param id
     *            the id number of the display to be created.
     * @return the container in which to place the display, ir <code>null</code>
     * if it isn't yet know.
     */
    protected abstract IContainer getContainerForNewDisplay(int id);

    /**
     * Return the viewer coresponding to id.
     * 
     * @param id
     * @return corresponding display.
     */
    public ISeeCodeTextViewer getDisplay(int id) {
        try {
            return mFactory.getDisplay(id);
        } catch (IllegalArgumentException e) {
            mSeeCodeTextViewerCallback.internalError(null, "Display " + id
                    + " is invalid", e);
            return null;
        }
    }

    @Override
    public void setSelection (int id, int line1, int col1, int line2, int col2) {
       getDisplay(id).setSelection(line1,col1,line2,col2);      
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.ICustomDisplayCallback#updateComponent(int,
     *      java.lang.String)
     */
    @Override
    public void updateComponent(int displayID, String properties) {
        ISeeCodeTextViewer d = getDisplay(displayID);
        d.updateComponent(properties);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.ICustomDisplayCallback#updateComponentData(int,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void updateComponentData(int displayID, String componentID,
            String data) {
        ISeeCodeTextViewer d = getDisplay(displayID);
        d.updateComponentData(componentID, data);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.ICustomDisplayCallback#setLine(int, int,
     *      java.lang.String)
     */
    @Override
    public void setLine(int id, int lineNumber, String line) {
        ISeeCodeTextViewer d = getDisplay(id);
        d.setLine(lineNumber, line);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.ICustomDisplayCallback#setBlanks(int, int,
     *      int)
     */
    @Override
    public void setBlanks(int id, int lineNumber, int cnt) {
        ISeeCodeTextViewer d = getDisplay(id);
        d.setBlankLines(lineNumber, cnt);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.ICustomDisplayCallback#setHighlight(int, int,
     *      int)
     */
    @Override
    public void setHighlight(int id, int lineNumber, int highlightValue) {
        ISeeCodeTextViewer d = getDisplay(id);
        d.setHighlight(lineNumber, highlightValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.ICustomDisplayCallback#removeHighlights(int,
     *      int)
     */
    @Override
    public void removeHighlights(int id, int highlight) {
        ISeeCodeTextViewer d = getDisplay(id);
        d.removeHighlights(highlight);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.ICustomDisplayCallback#createUserDisplay(int,
     *      java.lang.String)
     */
    @Override
    public void createUserDisplay(final int id, String guihiliSpec) {
        MenuDescriptor menu = new MenuDescriptor();
        mFactory.addUserGUI(id, guihiliSpec, menu, mSeeCodeTextViewerCallback,null);
        //We want the the top most menu
        menu.eachTopLevelMenu(new ITopLevelCallback(){

            @Override
            public void doMenu(String label, MenuDescriptor m) {
               setUserDefinedMenu(id,label,m);
                
            }});
    }
    
    /**
     * Called by {@link #createUserDisplay} to render
     * the menu that was dynamically created.
     * @param id the id passed to {@link #createUserDisplay}.
     * @param label the label for the menu.
     * @param menu the menu that was generated.
     */
    protected abstract void setUserDefinedMenu(int id, String label, MenuDescriptor menu);

    @Override
    public void refreshDisplay(int id) {
        ISeeCodeTextViewer d = getDisplay(id);
        // If we refresh a display that is in the process
        // of closing, we may be updating widgets that
        // have been disposed!
        if (!d.isClosing())
            d.refresh();
    }
    
    @Override
    public void show(int id) {
        ISeeCodeTextViewer d = getDisplay(id);
        d.show(); // bring to top.
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.ICustomDisplayCallback#setTimerUpdate(int,
     *      int)
     */
    @Override
    public void setTimerUpdate(int id, int millisec) {
        ISeeCodeTextViewer d = id == 0 ? null : getDisplay(id);
        if (d != null) {
            d.setTimerUpdate(millisec);
        } else {
            mTimerValue = millisec;
            if (mUpdateTimer == null) {
                mUpdateTimer = new UpdateTimer(new ITimedUpdate() {

                    @Override
                    public void timedUpdate() {
                        mSeeCodeTextViewerCallback.sendValueUpdate(null,
                                "timer_update", "", mTimerValue);

                    }
                });
            }
            if (millisec <= 0)
                mUpdateTimer.killTimer();
            else
                mUpdateTimer.setTimerInterval(millisec);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.ICustomDisplayCallback#scroll(int, int, int)
     */
    @Override
    public void scroll(int id, int amount, int displaySize) {
        ISeeCodeTextViewer d = getDisplay(id);
        d.scroll(amount, displaySize);
    }

    /*
     * (non-Javadoc)
     * @see com.arc.seecode.engine.ICustomDisplayCallback#setScrollBar(int, int, int, int, int, int)
     */
    @Override
    public void setScrollBar (int id, int current, int displaySize, int first, int last, int blockIncrement) {
        ISeeCodeTextViewer d = getDisplay(id);
        d.setVerticalScroller(current, displaySize, first, last, blockIncrement);
    }

    /*
     * (non-Javadoc)
     * @see com.arc.seecode.engine.ICustomDisplayCallback#clear(int)
     */
    @Override
    public void clear(int id) {
        ISeeCodeTextViewer d = getDisplay(id);
        d.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.ICustomDisplayCallback#createProgressDisplay(int,
     *      java.lang.String)
     */
    @Override
    public void createProgressDisplay(final int id, String propString) {
        ByteArrayInputStream input = new ByteArrayInputStream(propString
                .getBytes());
        Properties props = new Properties();
        try {
            props.load(input);
        } catch (IOException e) {
            internalError(null, e.getMessage(), e);
            return;
        }
        String title = props.getProperty("title", "Progress");
        String cancel = props.getProperty("cancel", "0");
        String delay = props.getProperty("delay", "300");

        Runnable cancelAction = null;
        if (!cancel.equals("0")) {
            cancelAction = new Runnable() {
                @Override
                public void run() {
                    try {
                        mEngine.progressDisplayCanceled(id);
                    } catch (EngineException e) {
                        internalError(null,e.getMessage(),e);
                    }
                }
            };
        }
        Object p = createProgressDisplay(title, Integer
                .parseInt(delay), cancelAction);
        if (mProgressDisplays == null) {
            mProgressDisplays = new HashMap<Integer,Object>();
        }
        mProgressDisplays.put(new Integer(id), p);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.ICustomDisplayCallback#destroyProgressDisplay(int)
     */
    @Override
    public void destroyProgressDisplay(int id) {
        Object p = null;
        if (mProgressDisplays != null) {
            p = mProgressDisplays.remove(new Integer(id));
        }
        if (p != null) {
            disposeProgressDisplay(p);
        } else
            internalError(null, "Unrecognized progress display id: " + id, new IllegalStateException("Unrecognized progress display id: " + id));

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.ICustomDisplayCallback#setProgressPercentage(int,
     *      int)
     */
    @Override
    public void setProgressPercentage(int id, int percent) {
        Object p = null;
        if (mProgressDisplays != null) {
            p = mProgressDisplays.get(new Integer(id));
        }
        if (p != null) {
            setProgressPercentage(p, percent);
        } else
            internalError(null, "Unrecognized progress id: " + id, new IllegalStateException("Unrecognized progress id: " + id));

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.ICustomDisplayCallback#doOptionsWindow(int,
     *      java.lang.String)
     */
    @Override
    public void doOptionsWindow(final int displayID, final int guiID, String props) {
        ByteArrayInputStream input = new ByteArrayInputStream(props.getBytes());
        Properties propMap = new Properties();
        try {
            propMap.load(input);
        } catch (IOException e) {
            internalError(null,"Corrupt properties string",e);
            return;
        }
        final String onPush = propMap.getProperty("on_push","XXX");
        OptionsWindow ow = new OptionsWindow(mWidgetFactory,getDialogOwner(),getArgsFileLocator(),mSCDIR);
        try {
            ow.showDialog(propMap,
                    new Runnable(){
                @Override
                public void run(){
                    try {
                        mEngine.sendValueUpdate(displayID,"user_gui_selection",
                                onPush + " ugui " + guiID);
                    } catch (EngineException e) {
                        internalError(null,"Engine communication failure",e);
                    }
                }
                
            },this);
        } catch (Exception x) {
            internalError(null,x.getMessage(),x);
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
        ISeeCodeTextViewer d = getDisplay(id);
        d.setTitle(title);
    }

    @Override
    public void deleteDisplay(int id) {
        ISeeCodeTextViewer d = getDisplay(id);
        d.dispose();
    }
    
    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param id
     * @param componentName
     * @param text
     * @param tail
     */
    @Override
    public void typeText (int id, String componentName, String text, boolean tail) {
        ISeeCodeTextViewer d = getDisplay(id);
        try {
            d.inputText(componentName,text,tail);
        } catch(IllegalArgumentException x){
            internalError(d,x.getMessage(),x);
        }
    }
 

    /**
     * Deal with an internal error (e.g., writing to a log, or popping up an
     * error box).
     * <P>
     * Called from {@link SeeCodeTextViewerCallback#internalError}.
     * 
     * @param viewer
     *            the associated viewer or null.
     * @param message
     *            the message
     * @param t
     *            the exception that caused the problem, or null.
     */
    public abstract void internalError(ISeeCodeTextViewer viewer, String message,
            Throwable t);

    /**
     * Display an appropriate error box to indicate a recoverable error.
     * <P>
     * Called from {@link SeeCodeTextViewerCallback#notifyError}.
     * 
     * @param viewer
     *            the associate viewer
     * @param message
     *            the message to be displayed
     * @param title
     *            the title to appear on the erro box
     */
    public abstract void notifyError(ISeeCodeTextViewer viewer, String message,
            String title);
    
    /* (non-Javadoc)
     * @see com.arc.seecode.engine.ICustomDisplayCallback#doChangeDialog(int, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void doChangeDialog(int id, String name, String old, final String cmd) {
        ISeeCodeTextViewer v = id == 0?null:getDisplay(id);
        IComponent parent = v == null?null:v.getComponent();
        final IWindow dialog = mWidgetFactory.makeDialog(parent,true);
        dialog.setTitle("Change " + name);
        IContainer panel = mWidgetFactory.makeContainer(dialog.getContents(),IComponentFactory.COLUMN_STYLE);
        IContainer promptPanel = mWidgetFactory.makeContainer(panel,IComponentFactory.ROW_STYLE);
        mWidgetFactory.makeLabel(promptPanel,"New value for " +name+":");
        final ITextField tf = mWidgetFactory.makeTextField(promptPanel);
        tf.setFireActionWhenFocusLost(false); // Only fire change when Enter key is pressed or OK button pressed
        tf.setHorizontalAlignment(IComponent.FILL);
        tf.setHorizontalWeight(1.0);
        tf.setText(old);
        tf.setSelection(0,old.length());
        ActionListener okAction = new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                applyChange(cmd,tf.getText()); 
                dialog.dispose();
            }};
        tf.addActionListener(okAction);
        IContainer buttonPanel = mWidgetFactory.makeContainer(panel,IComponentFactory.ROW_STYLE);
        ILabel space = mWidgetFactory.makeLabel(buttonPanel,"");
        space.setHorizontalAlignment(IComponent.FILL);
        space.setHorizontalWeight(1.0);
        IButton okButton = mWidgetFactory.makeButton(buttonPanel);
        okButton.setText("OK");
        okButton.addActionListener(okAction);
        IButton cancelButton = mWidgetFactory.makeButton(buttonPanel);
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();       
            }});
        dialog.addWindowObserver(new IWindow.IObserver(){

            @Override
            public void windowActivated (IWindow w) {            
            }

            @Override
            public void windowClosed (IWindow w) {
            }

            @Override
            public void windowClosing (IWindow w) {
                dialog.dispose();
            }

            @Override
            public void windowDeactivated (IWindow w) {
            }

            @Override
            public void windowDeiconified (IWindow w) {
            }

            @Override
            public void windowIconified (IWindow w) {
            }});
        dialog.pack();
        setHelp(dialog.getContents(),"change_window");
        dialog.open();
    }
    
    /**
     * Override if a help system is supported (based on F1 key).
     * @param widget the widget to be associated with help.
     * @param helpID the help ID.
     */
    protected void setHelp(IComponent widget, String helpID){
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.engine.ICustomDisplayCallback#chooseFromList(java.lang.String,
     *      java.lang.String[])
     */
    @Override
    public int chooseFromList(String prompt, String[] listItems) {
        ISeeCodeTextViewer d = getSelectedDisplay();
        IComponent c = d == null?null: d.getComponent();
        final IWindow dialog = mWidgetFactory.makeDialog(c,true);
        dialog.setTitle("Select one, please...");
        IContainer panel = dialog.getContents();
        mWidgetFactory.makeLabel(panel,prompt);
        final IList list = mWidgetFactory.makeList(panel,false);
        list.setItems(listItems);
        list.setHorizontalWeight(1.0);
        list.setVerticalWeight(1.0);
        list.setHorizontalAlignment(IComponent.FILL);
        list.setVerticalAlignment(IComponent.FILL);
        IContainer buttonPanel = mWidgetFactory.makeContainer(panel,IComponentFactory.ROW_STYLE);
        ILabel space = mWidgetFactory.makeLabel(buttonPanel,"");
        space.setHorizontalAlignment(IComponent.FILL);
        space.setHorizontalWeight(1.0);
        IButton okButton = mWidgetFactory.makeButton(buttonPanel);
        okButton.setText("OK");
        final int result[] = new int[1];
        result[0] = -1;
        okButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                result[0] = list.getSelectionIndex();
                dialog.dispose();
            }});
        IButton cancelButton = mWidgetFactory.makeButton(buttonPanel);
        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();                
            }});
        dialog.addWindowObserver(new IWindow.IObserver(){

            @Override
            public void windowClosing (IWindow w) {
                dialog.dispose();               
            }

            @Override
            public void windowClosed (IWindow w) {
            }

            @Override
            public void windowActivated (IWindow w) {
            }

            @Override
            public void windowDeactivated (IWindow w) {
            }

            @Override
            public void windowIconified (IWindow w) {
            }

            @Override
            public void windowDeiconified (IWindow w) {
            }});
        list.addObserver(new IList.IObserver(){

            @Override
            public void onSelected(IList listWidget) {
            }

            @Override
            public void onDoubleClicked(IList listWidget, int index) {
                 result[0] = index;
                 dialog.dispose();
            }});
        dialog.pack();
        int height = dialog.getHeight();
        if (height > 600){
            //Don't allow too tall of dialog.
            dialog.setSize(dialog.getWidth(),600);
            dialog.layout();
        }
        dialog.open();
        return result[0];
    }
    
    /* (non-Javadoc)
     * @see com.arc.seecode.engine.ICustomDisplayCallback#setProfileData(int, int, java.lang.String)
     */
    @Override
    public void setProfileData(int id, int column, String data) {
       String s[] = StringUtil.stringToArray(data);
       ISeeCodeTextViewer d = getDisplay(id);
       d.setProfileColumns(column,s);
    }
    /**
     * Apply the Change request that was generated by
     * the Change dialog.
     * @param cmd the command to send back to the engine.
     * @param value value to be appended or inserted.
     */
    protected void applyChange(String cmd, String value){
        int i = cmd.indexOf("%s");
        if (i >= 0){
            cmd = cmd.substring(0,i) + value + cmd.substring(i+2);
        }
        else cmd = cmd + ' ' + value;
        try {
            mEngine.invokeCommand(cmd);
        } catch (EngineException e) {
            internalError(null,e.getMessage(),e);
        }
        
    }

    /**
     * Create a progress display.
     * 
     * @param title
     *            the title to appear its its title bar.
     * @param delay
     *            the number of milliseconds to wait before it is to appear (if
     *            not canceled).
     * @param ifCanceled
     *            a callback to invoke if progress display is canceled; or null
     *            if no cancel button is to appear.
     * @return a handle representing the progress display.
     */
    protected abstract Object createProgressDisplay(String title, int delay,
            Runnable ifCanceled);
    /**
     * Dispose of a progress display.
     * @param p a handle that was returned by {@link #createProgressDisplay(String,int,Runnable)}
     */
    protected abstract void disposeProgressDisplay(Object p);
    
    /**
     * Set the value on a progress bar.
     * @param handle a handle returned from by {@link #createProgressDisplay(String,int,Runnable)}
     * @param percentage a value of 0 to 100 to denote a percentage
     * of completion for the progress bar, or -1 to denote unknown completion.
     */
    protected abstract void setProgressPercentage(Object handle, int percentage);
    
    /**
     * Return the display that is currently selected.
     * Used by the {@link #chooseFromList} method to
     * figure out who the requester is.
     * @return the selected display.
     */
    protected abstract ISeeCodeTextViewer getSelectedDisplay();
    
    /**
     * Should be overridden to define the object that
     * creates displays. For Eclipse, it must instantiate
     * a view.
     * @return display creator.
     */
    protected abstract IDisplayCreator getDisplayCreator();
    /**
     * Return an object from which the "args" and properties
     * file can be computed as required by the
     * {@link #doOptionsWindow} invocation.
     * @return an object for computing the args and
     * properties files associated with an options window.
     */
    protected abstract IArgsFileLocator getArgsFileLocator();
    
    /**
     * When a dialog needs to be generated, return an object that 
     * will act as an "owner" of the dialog. Under Swing, its typically an
     * instance of Window; under SWT, its an instance of Shell.
     * @return the owner of any dialog that needs to be generated.
     */
    protected abstract Object getDialogOwner();

    @Override
    public void queueCommand (String command) {
        if (mCommandProcessor == null) {
            try {
                mCommandProcessor = CommandFactory.createCommandProcessor(mEngine, mEngine.getOutputStream(), mEngine
                    .getErrorStream());
            }
            catch (EngineException e1) {
                internalError(null, e1.getMessage(), e1);
                return;
            }
        }
        try {
            mCommandProcessor.processCommand(command);
        }
        catch (Exception x){
            if (x instanceof EngineException ||
                x instanceof IllegalArgumentException){
//              Unrecognized command?
                notifyError(null,x.getMessage() + "\n(in command: \"" + command + "\")","Queued Command Error");
            }
            else 
                internalError(null,x.getMessage(),x);
        }
    }

    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param dialog
     * @param id
     */
    @Override
    public void associateHelp (IWindow dialog, String id) {
        // @todo Auto-generated method stub
        
    }

    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param selectors
     */
    @Override
    public void setDisplaySelectors (String[] selectors) {
        mEngine.setDisplaySelectors(selectors);      
    }
    
    @Override
    public void setProperty(String key, String value){
        if (WATCHPOINT_VALUE_MASK.equals(key)){
            mWatchpointValuesSupported = !"0".equals(value);
        }
    }
    
    @Override
    public void animate(int tid, String command){
        this.displayErrorBox(0, "Animation is not supported in this environment");
    }
    
    /**
     * Given the image, create a splash dialog, but don't yet display.
     * Return the dialog object.
     * @param image
     * @return the dialog object.
     */
    protected abstract Object makeSplash(IImage image);
    
    /**
     * Given a previously created splash screen returned from {@link #makeSplash}, set
     * text to the given x,y coordinate. The text may have newlines.
     * @param dialog object previously returned from {@link #makeSplash}.
     * @param x the x coordinate relative to the splash dialog where the text is to be placed.
     * @param y the y coordinate relative to the splash dialog where the text is to be placed.
     * @param text the text to be written in the splash screen at x,y position.
     */
    protected abstract void setSplashText(Object dialog, int x, int y, String text);
    
    /**
     * Show the splash screen that was previously created from {@link #makeSplash} and augmented by calls to
     * {@link #setSplashText}.
     * @param dialog the splash dialog to show.
     */
    protected abstract void showSplashScreen(Object dialog);
    
    @Override
    public void showSplash(String imagePath, String propString){
        IImage image = null;
        Properties props = extractProperties(propString);
        File ifile = new File(imagePath);
        if (!ifile.exists()){
            ifile = new File(this.mSCDIR + "/mdb/pix/swing/" + imagePath);
        }
        try {
            image = mWidgetFactory.makeImage(ifile.toURI().toURL());
        }
        catch (MalformedURLException e) {
        }
               
        if (image == null) {
            String text = props.getProperty("text",null);
            // Image couldn't be found. Just do a note:
            if (text != null)
                mEngine.getObserver().displayNote(mEngine,text);
        }
        else {
            Object splash = makeSplash(image);
            for (int i = 0; i < 10; i++){
                String suffix = i==0?"":""+i;
                String text = props.getProperty("text"+suffix,null);
                if (text == null) break;
                int x = Integer.parseInt(props.getProperty("x"+ suffix,"-1"));
                int y = Integer.parseInt(props.getProperty("y"+ suffix,"-1"));
                if (x >= 0 && y>=0){
                    setSplashText(splash,x,y,text);
                }
            }
            showSplashScreen(splash);
        }
    }

    @Override
    public String promptForFile (String propString) {
        Properties props = extractProperties(propString);
        String title = props.getProperty("title", "Choose file");
        String type = props.getProperty("type", "file");
        String output = props.getProperty("output", "1");
        String dir = props.getProperty("dir",".");
        String file = props.getProperty("file","");
        String ext = props.getProperty("ext",null);
        String desc = props.getProperty("description","");
        int style = 0;
        if (output.equals("0")) style |= IComponentFactory.FILE_OPEN;
        else style |= IComponentFactory.FILE_SAVE;
        if (type.equals("dir")) style |= IComponentFactory.FILE_DIRS;
        
        IFileChooser dialog = this.mWidgetFactory.makeFileChooser(getDialogOwner(), style);
        dialog.setTitle(title);
        if (ext != null){
            if (ext.indexOf("*")<0) {
                String x[] = ext.split("[,;]");
            
                for (int i = 0; i < x.length; i++){
                    if (x[i].startsWith(".") && x[i].indexOf("*") < 0) x[i] = "*" + x[i];
                }
                StringBuilder b = new StringBuilder();
                for (String s: x){
                    if (b.length() > 0) b.append(';');
                    b.append(s);
                }
                ext = b.toString();
            }
            dialog.addFilter(ext,desc);
        }
        dialog.addFilter("*", "All files (*.*)");
        dialog.setDirectory(new File(dir));
        if (file.length() > 0) dialog.setFile(new File(dir,file));
        while (true) {
            File f = dialog.open();
            if (f != null) {
                if (f.exists() && !output.equals("0")) {
                    if (!showConfirmDialog("Overwrite existing file \"" + f + "\"?")) {
                        continue;
                    }
                }
                return f.getPath();
            }
            return null;
        }
    }
    
    abstract protected boolean showConfirmDialog(String msg);

    private Properties extractProperties (String propString) {
        ByteArrayInputStream input = new ByteArrayInputStream(propString.getBytes());
        Properties props = new Properties();
        try {
            props.load(input);
        }
        catch (IOException e) {
            internalError(null, e.getMessage(), e);
        }
        return props;
    }

    abstract protected void showHelpForID(String id);
    
    @Override
    public void showHelp(String props){
        String id = extractProperties(props).getProperty("id",null);
        if (id == null) {
            internalError(null,"Internal error: help \"id\" property missing\nProperties: " + props,null);
        }
        else showHelpForID(id);
    }

    @Override
    public void copyToClipboard (String s) {
        mWidgetFactory.copyToClipBoard(s);       
    }

    @Override
    public void copyVisibleToClipboard (int id) {
        mWidgetFactory.copyToClipBoard(mFactory.getDisplay(id).getSelection());      
    }
}
