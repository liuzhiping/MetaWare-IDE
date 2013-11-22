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
package com.arc.seecode.internal.display;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import com.arc.mw.util.ITimedUpdate;
import com.arc.mw.util.StringUtil;
import com.arc.mw.util.UpdateTimer;
import com.arc.seecode.display.IColorPreferences;
import com.arc.seecode.display.ISeeCodeTextViewer;
import com.arc.seecode.display.ISeeCodeTextViewerCallback;
import com.arc.seecode.display.MenuDescriptor;
import com.arc.seecode.display.icons.LabelsAndIcons;
import com.arc.seecode.internal.display.panels.ExtensionsPanel;
import com.arc.widgets.IChoice;
import com.arc.widgets.IColor;
import com.arc.widgets.IComponent;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;
import com.arc.widgets.IFileChooser;
import com.arc.widgets.IImageWidget;
import com.arc.widgets.ITextField;
import com.arc.widgets.IToolBarBuilder;
import com.arc.widgets.IToolItem;
import com.arc.widgets.IWidget;

/**
 * @author David Pickens
 */
class SeeCodeTextViewer implements ISeeCodeTextViewer, ITimedUpdate,
        ITextScrollObserver {
    private ISquareCanvas mCanvas;

    private ISeeCodeTextViewerCallback mCallback;

    private String mTitle = "<title>";

    private boolean mIsClosing = false;

    private int mID;

    private UpdateTimer mUpdateTimer = null;

    private String mKind;
    
    private File mDefaultDirectory = new File(".");

    private Map<String,String> mValues = new HashMap<String,String>();

    private boolean mPersistent;

    private int mColumnCount = 0;

    private String mName = null;

    private String mHelpID = null;
    private String mDefaultHelpID = null;
    
    private TextDisplayModel mModel;

    private boolean mNeedInfiniteSlider;

    private ScrollController mController;

    private int mLastLine;

    private ExtensionsPanel mExtraPanel;

    private IContainer mContainer;

    private SeeCodeTextViewerFactory mFactory;

    private SeeCodeTextView mView = null;

    private SquareCanvasCallback mCanvasCallback;

    //private MenuDescriptor mToolBarMenu;

    // private MenuDescriptor mUserMenu = null;

    private int mDisplayableColumns;

    private IComponentFactory mWidgetFactory;

    private IFileChooser mSaveFileDialog = null;

    private boolean mDeleted = false;

    private IToolBarBuilder mToolBarBuilder;
    
    private IColorPreferences fColorPrefs = null;

    private final IValueSender profilingValueUpdate = new IValueSender(){
    
                                @Override
                                public void sendValueUpdate(String property, String value) {
                                    //Kloodge city:
                                    //For a profiling display, the value will be:
                                    // "{1|0}{column_number} ugui N"
                                    // All we care about is the column numbe whichi
                                    // is enabled or disabled.
                                    if (value.length() > 1 && Character.isDigit(value.charAt(1))){
                                        StringTokenizer ST = new StringTokenizer(value.substring(1));
                                        int val = Integer.parseInt(ST.nextToken());
                                        boolean enabled = value.charAt(0) != '0';
                                        setProfile(val,enabled);
                                        refresh();
                                    }
                                    else {
                                        // Otherwise its the form:
                                        // "<value> ugui N"
                                        int index = value.indexOf("ugui");
                                        if (index >= 0){
                                            value = value.substring(0,index);
                                        }
                                        mCallback.sendValueUpdate(SeeCodeTextViewer.this,value,"");
                                    }
                                }
    
                                @Override
                                public boolean sendValueUpdate(String property, String value, int timeout) {
                                    sendValueUpdate(property,value);
                                    return true;
                                }
    
                                @Override
                                public void sendValueUpdate(String property, String value, boolean record) {
                                    sendValueUpdate(property,value);
                                    
                                }
    
                               };

    SeeCodeTextViewer(int id, String kindOfDisplay, IContainer parent,
            ISeeCodeTextViewerCallback callback,
            IComponentFactory widgetFactory,
            SeeCodeTextViewerFactory factory) {
        mCallback = callback;
        mID = id;
        mKind = kindOfDisplay;
        mModel = new TextDisplayModel();
        mContainer = parent; /*factory.getWidgetFactory().makeGridContainer(parent, 1);*/
        mContainer.setHorizontalWeight(1.0);
        mContainer.setVerticalWeight(1.0);
        mContainer.setVerticalAlignment(IComponent.FILL);
        mContainer.setHorizontalAlignment(IComponent.FILL);
        mFactory = factory;
        mWidgetFactory = widgetFactory;

    }

    @Override
    public IContainer getComponent() {
        return mContainer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ITextDisplayView#dispose()
     */
    @Override
    public void dispose() {
        if (!this.mIsClosing){
            close();
        }
        if (mCanvas != null) {
            mCanvas.dispose();
            mCanvas = null;           
        }
        mFactory.remove(this);
    }
    
    @Override
    public void forceDispose(){
        dispose();
        // NOw, create a dummy viewer that will intercept
        // updates that the engine may be in the process
        // of sending.
        mFactory.add(new DummyViewer(mID));      
    }

    /**
     * Must be called to finish constructing this object after the "extraPanel"
     * is constructed.
     * 
     * @param props
     * @param extraPanel
     * @param kind
     * @param f
     *            the widget factory.
     */
    void init(Properties props, ExtensionsPanel extraPanel, 
            IComponentFactory f, IToolBarBuilder tbBuilder) {
        //mKind = props.getProperty("kind", "unknown"); // already set in constructor
        mToolBarBuilder = tbBuilder;
        // get display geometry
        int lines = Integer.parseInt(props.getProperty("lines", "10"));
        int width = Integer.parseInt(props.getProperty("width", "0"));

        //mThreadLockable = (props.getProperty("threadlock", "0").charAt(0) ==
        // '1');
        //mConnectable = (props.getProperty("connectable", "0").charAt(0) !=
        // '0');

        String infinite = props.getProperty("infinite_slider");
        if (infinite != null) {
            mNeedInfiniteSlider = infinite.equals("1");
        }
        mCanvasCallback = new SquareCanvasCallback(new ValueSender(this,
                mCallback), this, mCallback, f);
        // create output data model
        mModel = new TextDisplayModel();

        // get profiling properties
        String profilingProps = props.getProperty("profiling");

        mView = new SeeCodeTextView(this, mModel, mNeedInfiniteSlider,
                mCanvasCallback, lines, width, mFactory.getWidgetFactory());
        // create controller
        mController = new ScrollController(this, mView.getScrollPane());

        // create guts of our DynamicTextDisplay
        mExtraPanel = extraPanel;

        mModel.setSize(lines);

        mCanvas = mView.getPrimaryCanvas();

        if (extraPanel != null) {
            mCanvas.setPermitDoubleClickInColumn0(extraPanel
                    .getPermitsDoubleClickInColumn0());
        }
        
        if (fColorPrefs != null) mCanvas.setColorPreferences(fColorPrefs);
        
        if (profilingProps != null){
            processProfilingStuff(profilingProps);
        }
        
        mHelpID = props.getProperty("help_id");
        mDefaultHelpID = props.getProperty("help_id_default");

        //createToolbarMenu(profilingProps);

        //!mView.setColumnHeaders(mProfileTitles);

        //setupHelpBroker(kind);

    }
    
    private void processProfilingStuff(String propString){
        List<String> props = SeeCodeTextViewerFactory.extract_properties_from_property_sequence(propString);
        List<String> columnHeaders = new ArrayList<String>();
        // NOTE; these properties define toolbar buttons.
        // But we forgo them for the time being because we
        // don't have room for such in Eclipse.
        // So, we only look for the "user_gui" that creates
        // the menu items.
        for (String s: props){
            if (s.indexOf("user_gui") >= 0){
                mFactory.makeWidgetFromPropertyString(
                        getExtensionsPanel(),
                        s,
                        profilingValueUpdate,
                        mCallback,
                        new CheckBoxRecorder(SeeCodeTextViewer.this)
                        );
            }
            else {
                Properties buttonProps = SeeCodeTextViewerFactory.loadProperties(s);
                String label = buttonProps.getProperty("label");
                if (label != null){
                    columnHeaders.add(label);
                }
            }
        }
        mView.setColumnHeaders(columnHeaders);
    }

    ExtensionsPanel getExtensionsPanel() {
        return mExtraPanel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ITextDisplayView#isDisposed()
     */
    @Override
    public boolean isDisposed() {
        return mCanvas == null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ITextDisplayView#getTitle()
     */
    @Override
    public String getTitle() {
        return mTitle;
    }

    /**
     * Prepare to close this window.
     */
    private void prepareToClose() {
        if (!mIsClosing) {
            mIsClosing = true; //suppress "set_lines" calls as panels are
            // resized
            // If we're being closed, prevent the timer from
            // sending a message to a non-existing entity.
            killUpdateTimer();
        }
    }

    /**
     * Remove this panel from the enclosing frame. We actually tell the engine
     * to do the work. Called when "close" popup menu-item is selected.
     */
    @Override
    public void close() {
        if (!mIsClosing && !isDisposed()) {
            prepareToClose();
            //NOTE: setDeleted() will have been called if the engine
            // implicitly deleted this display as a side-effect of deleting
            // a parent display. Thus, we don't want to re-close it or else the
            // engine will complain with "invalid display id" messages.
            if (!mDeleted) {
                mCanvasCallback.setClosing(true);
                // NOTE: this action is assume to dispose of this
                // display! But there may be a delay, so we can't
                // confirm it here.
                mCallback.onClose(this);
            }
        }
    }
    
    @Override
    public boolean isClosing(){
        return mIsClosing || isDisposed();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ITextDisplayView#getDisplayKind()
     */
    @Override
    public String getDisplayKind() {
        return mKind;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ITextDisplayView#clear()
     */
    @Override
    public void clear() {
        mModel.clearAll();

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ITextDisplayView#setTitle(java.lang.String)
     */
    @Override
    public void setTitle(String title) {
        mTitle = title;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ITextDisplayView#getDisplayID()
     */
    @Override
    public int getDisplayID() {
        return mID;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ITextDisplayView#addValueUpdate(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void addValueUpdate(String property, String value) {
        if (property == null)
            throw new IllegalArgumentException("property is null");
        if (value == null) value = "";
        mValues.put(property, value);
    }
    
    @Override
    public void removeValueUpdate(String property){
    	if (property == null)
            throw new IllegalArgumentException("property is null");
    	mValues.remove(property);
    }
    
    @Override
    public void sendValueUpdate(String property, String value){  
        if (property == null) return; // Old corrupted memento files.
        IWidget C = mExtraPanel.findComponent(property);
        boolean consumed = false;
        if (C != null){
            consumed = true;
            if (C instanceof IToolItem){
                ((IToolItem)C).setSelected(!value.equals("0"));
            }
            else if (C instanceof ITextField){
                ((ITextField)C).setText(value);
            }
            else if (C instanceof IChoice){
                ((IChoice)C).setSelection(value);
            }
            else consumed = false;
        }
        else {
            // Set the checkboxes appropriately for the profiling columns
            if (mExtraPanel.getStaticMenuDescriptor().setCheckBoxValue(property,!"0".equals(value))){
                consumed = true;
                if (!"0".equals(value)){
                    addValueUpdate(property,"1"); // preserve it for next session
                }
            }
        }
        if (!consumed){
            mCallback.sendValueUpdate(this,property,value);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ITextDisplayView#getValueUpdates()
     */
    @Override
    public Map<String,String> getValueUpdates() {
        return mValues;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ITextDisplayView#isPersistent()
     */
    @Override
    public boolean isPersistent() {
        return mPersistent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ITextDisplayView#getVisibleColumnBitmap()
     */
    @Override
    public int getVisibleColumnBitmap() {
        return mView != null?mView.getVisibleColumnBitmap():0;
    }
    
    @Override
    public void setVisibleColumnBitmap(int bitmap) {
        if (mView == null) return; // shouldn't happen
        boolean change = false;
        for (int column = 0; column < 32; column++){
            boolean enabled =((1<<column) & bitmap) != 0;
            if (mView.isColumnVisible(column) != enabled){
                mView.setColumnVisible(column,enabled);
                change = true;
            }
        }
        if (change){
            mCallback.sendValueUpdate(this,"profile_columns_enabled",""+bitmap);
        }
    }

    /**
     * 
     */
    private void setProfilingMenuSelection() {
        MenuDescriptor mainMenu = mExtraPanel.getStaticMenuDescriptor();
        mainMenu.eachTopLevelMenu(new MenuDescriptor.ITopLevelCallback(){

            @Override
            public void doMenu (String label, MenuDescriptor menu) {
                if ("Profiling".equals(label)){
                    setProfilingMenuSelection(menu);
                }
                
            }});
    }
        

    private void setProfilingMenuSelection (final MenuDescriptor profMenu) {
        int position = 0;
        String items[] = profMenu.getItemNames();
        for (int i = 0; i < items.length; i++) {
            if (mView.isColumnVisible(i)) {
                position++;
                // set the profiling sub-menu items selected based on saved
                // state
                profMenu.setCheckBoxValue(items[i], true);
//                if (false) { // No way to undo the suffix when unchecked!
//                    String label = profMenu.getLabel(items[i]);
//                    if (label != null && label.indexOf('[') < 0) {
//                        profMenu.setLabel(items[i], label + " [" + position + "]");
//                    }
//                }
            }
        }
    }
    /*
     * (non-Javadoc)
     * @see com.arc.seecode.display.ITextDisplayView#getNumColumns()
     */
    @Override
    public int getNumColumns() {
        return mColumnCount;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ITextDisplayView#getToolbarsStatus()
     */
    @Override
    public boolean[] getToolbarsStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    //    /* (non-Javadoc)
    //     * @see
    // com.arc.seecode.display.ITextDisplayView#setTitlePrefix(java.lang.String)
    //     */
    //    public void setTitlePrefix(String prefix) {
    //        // Auto-generated method stub
    //
    //    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ITextDisplayView#setTimerUpdate(int)
     */
    @Override
    public void setTimerUpdate(int millis) {
        if (mUpdateTimer == null) {
            mUpdateTimer = new UpdateTimer(this);
        }
        mUpdateTimer.setTimerInterval(millis);
    }

    private void killUpdateTimer() {
        if (mUpdateTimer != null) {
            mUpdateTimer.killTimer();
            mUpdateTimer = null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ITextDisplayView#removeHighlights(int)
     */
    @Override
    public void removeHighlights(int which) {
        mModel.removeHighlights(which);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ITextDisplayView#setViewPort(int, int, int,
     *      int, int)
     */
    @Override
    public void setVerticalScroller(int firstLineInView, int linesInView,
            int firstLineInModel, int lastLineInModel, int blockIncr) {
        mController.setVerticalScroller(firstLineInView, linesInView,
                firstLineInModel, lastLineInModel, blockIncr);
        mLastLine = firstLineInView;

    }

    void setDisplayableColumns(int width) {
        mDisplayableColumns = width;
        int longestLine = mModel.getLongestLineWidth();
        int position = mController.getHorizontalScrollPosition();
        mController.setHorizontalScroller(position, width, 0, longestLine,1);
    }

    void setDisplayableLines(int lines) {
        mModel.setSize(lines);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ITextDisplayView#scroll(int, int)
     */
    @Override
    public void scroll(int lineCount, int displaySize) {
        mModel.scroll(lineCount, displaySize);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ITextDisplayView#updateComponent(java.lang.String)
     */
    @Override
    public void updateComponent(String props_string) {
        // Globals.SOP("Update component "+props_string);
        Properties P = SeeCodeTextViewerFactory.loadProperties(props_string);
        String kind = P.getProperty("kind");
        if (kind != null) {
            if (kind.equals("user_gui")) {
                updateUserGui(P);
                return;
            }
            if (kind.equals("profiling_columns")){
                updateProfilingColumns(P.getProperty("profiling_columns"));
                return;
            }
        }

        String id = P.getProperty("id");
        if (id == null) {
            return;
        }

        IWidget C = mExtraPanel.findComponent(id);
        MenuDescriptor md = mExtraPanel.getMenuDescriptor();
        if (md != null){
            String able = P.getProperty("able");
            if (able != null) md.setEnabled(id,able.equals("1"));
            String state = P.getProperty("state");
            if (state != null) md.setCheckBoxValue(id,state.equals("1"));
            String label = P.getProperty("name");
            if (label != null) md.setLabel(id,label);
            if (C == null) return;
        }
        else if (C == null) {
            mCallback.internalError(this, "Couldn't find id " + id, new Error());
            return;
        }


        String able = P.getProperty("able");
        boolean abled = able != null;
        if (abled) {
            if (Integer.parseInt(able) == 1) {
                C.setEnabled(true);
            } else {
                C.setEnabled(false);
            }
        }

        if (mExtraPanel == null) {
            return;
        }

        String tip = P.getProperty("tip");
        String state = P.getProperty("state");

        if (kind == null) {
            // Newer way of doing things: no need to specify kind
            if (tip != null) {
                C.setToolTipText(tip);
            }
            String label = P.getProperty("label");
            if (C instanceof IToolItem) {
                if (label != null) {
                    LabelsAndIcons.setButtonAttributes(label,(IToolItem)C,mWidgetFactory,mFactory.getDebuggerInstallPath());
                }
                if (state != null)
                    ((IToolItem) C).setSelected(state.charAt(0) != '0');
            } else if (C instanceof ITextField){
                if (label != null)
                    ((ITextField)C).setText(label);
                // Assume old "record" updates that do nothing
                //else mCallback.internalError(this,"Can't update text field",null);
            } else if (C instanceof IChoice){
                if (label != null){
                    ((IChoice)C).setSelection(label);
                }
            } else if (C instanceof IImageWidget) {
                SeeCodeImage si = mFactory.getSeeCodeImageFor((IImageWidget)C);
                if (si != null){
                    si.updateProperties(P);
                }
                else mCallback.internalError(this,"Can't locate image widget!",null);

                //          else if (C instanceof IChoice
                //                    && P.getProperty("record") != null) {
                //                IChoice combo = (IChoice) C;
                //                Object item = combo.getSelection();
                //                int cnt = combo.getItemCount();
                //                boolean found = false;
                //                for (int i = 0; i < cnt; i++) {
                //                    if (combo.getItemAt(i).equals(item)) {
                //                        found = true;
                //                        break;
                //                    }
                //                }
                //                if (!found) combo.addItem(item);
            } else if (able == null)
                mCallback.internalError(this, "Can't set properties for "
                        + C.getClass().getName(), new Error());
        } else if (kind.equals("text")
                && (C instanceof ITextField || C instanceof IChoice)) {
            String value = P.getProperty("value");
            if (value != null) {
                if (C instanceof ITextField)
                    ((ITextField) C).setText(value);
                else
                    ((IChoice) C).setSelection(value);
            }
            if (tip != null) {
                C.setToolTipText(tip);
            }
            // Can't set the label, because the RC was part of
            // Arbitrary_text_choice, which we don't see when we find
            // a component this way.
        }

        else if (kind.equals("choice") && C instanceof IChoice) {
            String choices = P.getProperty("choices");
            IChoice RC = (IChoice) C;
            if (choices != null) {
                int oldix = RC.getSelectionIndex();
                String[] array = StringUtil.pathToArray(choices, ",");
                RC.setItems(array);
                if (oldix < array.length) {
                    RC.setSelectionIndex(oldix);
                }
                RC.revalidate(); // size may have changed
            }
            String which = P.getProperty("init_choice");
            if (which != null) {
                RC.setSelectionIndex(Integer.parseInt(which));
            }
        }

        else if (kind.equals("LED") && C instanceof IImageWidget) {
            IImageWidget led = (IImageWidget) C;
            String color = P.getProperty("color");
            if (color != null) {
                IColor ledColor = mWidgetFactory.makeColor(color);
                led.setColor(new IColor[]{ledColor});
                led.apply();
            }
            if (tip != null) {
                led.setToolTipText(tip);
            }
        }

        else if (!abled) {
            mCallback.internalError(this, "Can't update component kind=" + kind
                    + "; class=" + C.getClass().getName(), new Error());
            System.out.println("Can't update component: " + C);
            System.out.println("Update component was passed: " + props_string);
        }

    }

    private void updateUserGui(Properties P) {
        String userGui = P.getProperty("user_gui");
        //mUserMenu = new MenuDescriptor();
//      If we're replacing the Profiling menu, must add the special
        // value_update action for the same.
        String profiling = P.getProperty("profiling_menu");
        if (profiling != null) {
            //Hack for when updating profiling menus. They go through a different path for
            // hysterical reasons.
            mFactory.addUserGui(0, userGui, mExtraPanel.getStaticMenuDescriptor(), mCallback, this, this.profilingValueUpdate);
            setProfilingMenuSelection();
        }
        else
           mFactory.addUserGUI(0, userGui, mExtraPanel.getStaticMenuDescriptor(), mCallback,this);
    }
    
    private void updateProfilingColumns(String props){
        this.processProfilingStuff(props);
        mView.refresh();
    }

    /**
     * Called when user is scrolling vertically.
     * 
     * @param line
     *            the new line position.
     */
    @Override
    public void onScrollToLine(int line) {
        if (line != mLastLine) {
            int delta = line - mLastLine;
            mLastLine = line;
            mCallback.sendValueUpdate(this, "scroll", Integer.toString(delta));
        }
    }

    /**
     * Called when user is scrolling horizontally.
     * 
     * @param column
     *            the zero-based column to scroll to horizontally.
     */
    @Override
    public void onScrollToColumn(int column) {
        mCanvas.setHorizontalScrollValue(column);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ITextDisplayView#updateComponentData(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public void updateComponentData(String id, String data) {
        IWidget C = mExtraPanel.findComponent(id);
        if (C instanceof IImageWidget){
            SeeCodeImage si = mFactory.getSeeCodeImageFor((IImageWidget)C);
            if (si != null){
                si.updateData(data);
            }
        }
        else
            mCallback.internalError(this,"unknown updateComponentData call for id=" + id + " kind=" + getDisplayKind(),null);

    }


    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ITextDisplayView#setHighlight(int, int)
     */
    @Override
    public void setHighlight(int ix, int engineHighlightCode) {
        mModel.setHighlight(ix, engineHighlightCode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ITextDisplayView#setToolbar(int, boolean)
     */
    @Override
    public void setToolbar(int which, boolean visible) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ITextDisplayView#setProfile(int, boolean)
     */
    @Override
    public void setProfile(int column, boolean enabled) {
        mView.setColumnVisible(column,enabled);
        int bits = mView.getVisibleColumnBitmap();
        mCallback.sendValueUpdate(this,"profile_columns_enabled",""+bits);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ITextDisplayView#setLine(int,
     *      java.lang.String)
     */
    @Override
    public void setLine(int lineNumber, String content) {
        mModel.setLine(lineNumber, content);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ITextDisplayView#setBlankLines(int, int)
     */
    @Override
    public void setBlankLines(int line, int cnt) {
        mModel.clearLines(line, cnt);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ITextDisplayView#inputText(java.lang.String,
     *      java.lang.String, boolean)
     */
    @Override
    public void inputText(String component, String text, boolean type_tail) {
        IWidget c = mExtraPanel.findComponent(component);
        if (c == null){
            throw new IllegalArgumentException("Can't find component \"" + component + "\" in " + getDisplayKind() + " display");
        }
        if (type_tail && text.length() > 0){
            // If tail is true, then only replace tail.
            // Example:
            //  Existing text:  *(int*)0x10000
            //  New text: )0x10100
            //  Resulting text:  *(int*)0x10100
            String s = null;
            if (c instanceof ITextField){
                s = ((ITextField)c).getText();
            }
            else if (c instanceof IChoice){
                s = ((IChoice)c).getText();
            }
            if (s != null){
                int i = s.lastIndexOf(text.charAt(0));
                if (i >= 0){
                    text = s.substring(0,i) + text;
                }
                else text = text.substring(1); // first char doesn't match
            }
        }
        if (c instanceof ITextField){
            ((ITextField)c).setText(text);
        }
        else if (c instanceof IChoice){
            //Hmmm. Does the engine think this is really a text field and
            // supplying text? Or does it think its a Combobox with a
            // selection index? We assume the latter if it isn't editable.
            IChoice combo = (IChoice)c;
            if (combo.isEditable())
                combo.setSelection(text);
            else {
                try {
                    int index = Integer.parseInt(text);
                    combo.setSelectionIndex(index);
                } catch(NumberFormatException x){
                    combo.setSelection(text);
                }
            }
        }
        else {
             throw new IllegalArgumentException("Unrecognized component \"" + component + "\" in " + getDisplayKind() + " display: class is " + c.getClass().getName());
        }
    }

    

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ITextDisplayView#populate()
     */
    @Override
    public void populate() {
        // TODO Auto-generated method stub

    }

    /**
     * Create popup menu.
     * 
     * @param selection
     *            selected text, if any, to be copied if "Copy" menu item is
     *            selected.
     * @return popup menu description
     */
    MenuDescriptor getPopupMenu(final String selection) {

        MenuDescriptor menu = new MenuDescriptor();
        if (mExtraPanel != null) {
            MenuDescriptor md = mExtraPanel.getMenuDescriptor();
            menu.addAllMenuItems(md);

        }
        boolean hasFileSave = (mExtraPanel != null)
                && mExtraPanel.getSaveToFileAction() != null;
        if (hasFileSave) {
            menu.addMenuItem("saveToFile", "Save to File",
                    new MenuDescriptor.IActionObserver() {

                        @Override
                        public void actionPerformed(String name) {
                            onSaveToFile();
                        }
                    });
            menu.addSeparator();
        }
//        if (mUserMenu != null) {
//            menu.addAllMenuItems(mUserMenu);
//            menu.addSeparator();
//        }
        if (selection != null && selection.length() > 0) {
            menu.addMenuItem("copy", "Copy", new MenuDescriptor.IActionObserver() {

                @Override
                public void actionPerformed(String name) {
                    SeeCodeTextViewer.this.copyToClipboard();

                }
            });
            menu.addSeparator();
        }
        //MenuDescriptor displayMenu = mCallback.createDisplayMenuFor(this);
        // menu.addSubMenu("displays", "Displays", displayMenu);
        return menu;
    }
    
    /**
     * Copy selected text to clipboard, honoring "Select All" if pending.
     */
    protected void copyToClipboard(){
        mCanvas.copyToClipboard();
    }

    private void onSaveToFile() {
        if (mSaveFileDialog == null){
            mSaveFileDialog = mWidgetFactory.makeFileChooser(mContainer,IComponentFactory.FILE_SAVE);
            mSaveFileDialog.setTitle("Save display contents");
            mSaveFileDialog.setDirectory(mDefaultDirectory);
        }
        File file = mSaveFileDialog.open();
        if (file != null){
            mExtraPanel.transmitFileSaveRequest(file.toString());           
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ITextDisplayView#refresh()
     */
    @Override
    public void refresh() {
        // Make horizontal scrollbar go visible or invisible
        setDisplayableColumns(mDisplayableColumns);
        mView.refresh();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ITextDisplayView#setEnableToolTips(boolean)
     */
    @Override
    public void setEnableToolTips(boolean v) {
        mCanvas.setEnableToolTips(v);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ITextDisplayView#showToolTip(java.lang.String,
     *      int, int)
     */
    @Override
    public void showToolTip(String tip, int line, int column) {
        mCanvas.showToolTip(tip, line, column);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ITextDisplayView#clickButton(java.lang.String)
     */
    @Override
    public void clickButton(String buttonName) {
        // TODO Auto-generated method stub

    }

    //! private void createToolbarMenu(String profile_properties) {
    //        mToolBarMenu = new MenuDescriptor(mFactory.getWidgetFactory());
    //
    //        int toolbar_cnt = 0;
    //
    //        // // Add font buttons to the top.
    //        // // Don't put the lock/snap toolbar by default. Let him ask for it.
    //        // // It's not as useful with the "show-all-windows" button now
    //        // // on the status line.
    //        // // The process choice number is on the lock/snap toolbar, so we leave
    //        // // it
    //        // // there for cmpd until we can find another place for it. O/wise
    //        // // we don't have the aa toolbar.
    //        //
    //        // // USED to add the lock snap toolbar if the session is CMPD, this is
    //        // no
    //        // // longer
    //        // //mToolbar = Globals.cmpd ? makeLockSnapToolbar() : null;
    //        //
    //        // mLockSnapMenuItem = new Responsible_Checkbox_with_panel_MI(
    //        // LockSnapToolbar.MenuName(), mToolbar, toolbar_cnt++) {
    //        //
    //        // void doit(boolean v) {
    //        // if (mLockSnapToolbar == null) {
    //        // if (v)
    //        // setupLockSnapToolbar();
    //        // else
    //        // return;
    //        // }
    //        // mLockSnapToolbar.setVisible(v);
    //        // }
    //        // };
    //        //
    //        // addToolbarItem(mLockSnapMenuItem);
    //
    //        if (mExtraPanel != null) {
    //            mToolbar = Combine_two_panels.Combine(mToolbar, mExtraPanel);
    //            setToolbar(mToolbar);
    //
    //            options_item = new Responsible_Checkbox_with_panel_MI(mExtraPanel
    //                    .getMenuName(), mExtraPanel, toolbar_cnt++) {
    //
    //                void doit(boolean v) {
    //                    mExtraPanel.setVisible(v);
    //                    // this toggles the toolbar
    //                    DynamicTextDisplay.this.revalidate();
    //                    DynamicTextDisplay.this.repaint();
    //                }
    //            };
    //            options_item.setName("Options"); // Use this name for property
    //            addToolbarItem(options_item);
    //        }
    //
    //        IValueUpdate user_gui_VU = null;
    //        if (profile_properties != null) {
    //            final generic_extra prof_panel = new generic_extra();
    //            prof_panel.init(mValueUpdate);
    //            // Probably irrelevant.
    //            List V = DisplayFactory
    //                    .extract_properties_from_property_sequence(profile_properties);
    //            boolean added = false;
    //            int profcnt = 0;
    //            for (int i = 0; i < V.size(); i++) {
    //                Component C = mDisplayFactory.make_one_GUI_component(
    //                        mValueUpdate, (String) V.get(i), mCallback);
    //                // Globals.SOP("made component "+C);
    //                prof_panel.add(C, false);
    //                added = true;
    //                if (C instanceof ValueUpdateButton) {
    //                    ValueUpdateButton B = (ValueUpdateButton) C;
    //                    // Is this a id=profile button? If so, we intercept
    //                    // to bring up the profile column.
    //                    if (B.getProperty().equals("profile")) {
    //                        Local_VU local_vu = new Local_VU(profcnt);
    //                        if (mProfileButtons == null) {
    //                            mProfileButtons = new ArrayList();
    //                        }
    //                        mProfileButtons.add(B);
    //                        // Change the label to the string he had, plus [x].
    //                        String S = DynamicTextDisplay.not_selected_prof;
    //                        trace("profile title added: " + B.getText());
    //                        mProfileTitles.add(B.getText());
    //                        B.setText(B.getText() + S);
    //                        // Special button gets special action that brings up
    //                        // a profile column.
    //                        B.setValueUpdate(new ValueSender(this, local_vu));
    //                        profcnt++;
    //                    }
    //                } else if (mUserGui == null
    //                        && C instanceof DisplayFactory.UG_component) {
    //                    mUserGui = ((DisplayFactory.UG_component) C).s;
    //                    user_gui_VU = new Menu_profiling_local_VU();
    //                }
    //            }
    //            if (added) {
    //                prof_panel.finish();
    //                mToolbarMenu.add(new Responsible_Checkbox_with_panel_MI(
    //                        "Profiling", prof_panel, toolbar_cnt++) {
    //
    //                    void doit(boolean selected) {
    //                        prof_panel.setVisible(selected);
    //                        DynamicTextDisplay.this.revalidate();
    //                    }
    //                });
    //
    //                mToolbar = Combine_two_panels.Combine(mToolbar, prof_panel);
    //                setToolbar(mToolbar);
    //                prof_panel.setVisible(false);
    //                // Profile titles.
    //                /*
    //                 * JPanel profile_titles = new Profile_titles(titles); toolbar =
    //                 * Combine_two_panels.Combine(toolbar,profile_titles);
    //                 */
    //                // profile_titles.hide();
    //            }
    //        }
    //
    //        if (mUserGui != null) {
    //            mDisplayFactory.addUserGUI(0, mUserGui, null, mCallback,
    //                    user_gui_VU, this, false);
    //        }
    //
    //        //
    //        // if (mLockSnapMenuItem != null) {
    //        // mLockSnapMenuItem.set_state();
    //        // }
    //        //
    //        // if (options_item != null) {
    //        // options_item.set_state();
    //        // }
    //
    //        // show_status_item.set_state();
    //    }

    void setPersistent(boolean v) {
        mPersistent = v;
    }

    @Override
    public void setName(String name) {
        mName = name;
        mView.setName(name); // for GUI tester
    }

    String getName() {
        return mName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.mw.util.ITimedUpdate#timedUpdate()
     */
    @Override
    public void timedUpdate() {
        mCallback.sendValueUpdate(this, "timer_update", "1", mUpdateTimer
                .getTimerInterval());

    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#setProfileColumns(int, java.lang.String[])
     */
    @Override
    public void setProfileColumns(int column, String[] lines) {
        mModel.setColumnData(column,lines);
        mView.refresh();
    }

    /**
     * @return the number of canvases (profiling columns+primary).
     */
    public int getCanvasCount() {
        return mView.getNumColumns();
    }
    
    @Override
    public void show(){
        // Don't know what to do
    }

    
    @Override
    public void setFont (Object font) {
        mView.setFont(font);
        
    }
    
    @Override
    public void setDefaultDirectory(File directory){
        mDefaultDirectory = directory;
    }

    @Override
    public void setDeleted () {
        mDeleted = true;      
    }

    @Override
    public IToolBarBuilder getToolBarBuilder() {
        return mToolBarBuilder;
    }

    @Override
    public String getDebuggerInstallPath () {
        return this.mFactory.getDebuggerInstallPath();
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#getDefaultHelpID()
     */
    @Override
    public String getDefaultHelpID () {
        return mDefaultHelpID;
    }

    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewer#getHelpID()
     */
    @Override
    public String getHelpID () {
        return mHelpID;
    }

    @Override
    public void setColorPreferences (IColorPreferences prefs) {
        fColorPrefs = prefs;
        if (this.mView != null)
            this.mView.setColorPreferences(prefs);    
    }

    @Override
    public String getSelection () {
        return this.mView.getSelection();
    }
    
    /**
     * Set a range of text to be highlighted as "selected".
     * @param line1 starting line, 0 based.
     * @param col1 starting column in line, 0 based.
     * @param line2 end line, inclusive, 0 based.
     * @param col2 end column, inclusive, 0 based.
     */
    @Override
    public void setSelection(int line1, int col1, int line2, int col2){
    	this.mView.setSelection(line1,col1,line2,col2);
    }

}
