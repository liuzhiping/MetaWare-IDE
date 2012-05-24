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
package com.arc.seecode.internal.display.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import com.arc.seecode.display.ISeeCodeTextViewer;
import com.arc.seecode.display.IContext;
import com.arc.seecode.display.MenuDescriptor;
import com.arc.seecode.display.icons.LabelsAndIcons;
import com.arc.seecode.internal.display.IValueSender;
import com.arc.widgets.IChoice;
import com.arc.widgets.IColor;
import com.arc.widgets.IComponent;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;
import com.arc.widgets.IImageWidget;
import com.arc.widgets.ILabel;
import com.arc.widgets.IToolBarBuilder;
import com.arc.widgets.IToolItem;
import com.arc.widgets.IWidget;
import com.arc.widgets.IImageWidget.IObserver;

/**
 * @author David Pickens
 */
public class ExtensionsPanel {

    private String mSaveToFileAction = null;

    private String mWantsToolTipAction = null;

    private String mColumn0ClickAction = null;

    private String mDoubleClickAction = null;

    private MenuDescriptor mStaticPopupMenu = null;

    protected IValueSender mSender = null;

    private ISeeCodeTextViewer mView;
    
    /**
     * Given a value-update property, find the widget that
     * triggers it.
     */
    private Map<String,IWidget> mValueMap = null;

    private IToolBarBuilder mToolBarBuilder;

    private IComponentFactory mWidgetFactory;

    private IContainer mContainer;

    /**
     * This is a dummy constructor. The method {@link #init}must be called to
     * do the real initialization. This is so that each subclass, which is
     * instantiated by reflection doesn't need arguments in its construction.
     *  
     */
    public ExtensionsPanel() {
        // Real constructor is "init()" but we don't
        // want every subclass to have to implement this
        // signature.
    }

    /**
     * Set the callback for sending messages back to the debugger engine.
     * <P>
     * It is accessed to do double-click and column0 clicks, as well as when
     * adding custom buttons.
     * 
     * @param sender
     *            callback for sending message back to engine.
     */
    public void init(ISeeCodeTextViewer view, IValueSender sender,
            IToolBarBuilder tb,
            IComponentFactory widgetFactory,
            IContext context) {
        setContext(context);
        mView = view;
        mToolBarBuilder = tb;
        mSender = sender;
        mWidgetFactory = widgetFactory;
        addStaticComponents();
    }
    
    /**
     * Called from {@link #init} in case a subclass needs this information.
     * @param context callback to get context information.
     */
    protected void setContext(IContext context){
        /*Let subclasses override this if they need list of threads */
    }

    /**
     * Add static component that are esoteric to this panel's display viewer.
     *  
     */
    protected void addStaticComponents() {

    }

    public ISeeCodeTextViewer getViewer() {
        return mView;
    }

    /**
     * Indicate that subsequent widgets are to be added on the next line.
     *  
     */
    public void newLine() {
       mToolBarBuilder.makeRowSeparator();
    }

    public IWidget findComponent(String id) {
        IWidget w = null;
        if (mValueMap != null){
            w = mValueMap.get(id);
        }
        return w;
    }

    /**
     * Set the widget that, when selected, causes the contents of the display to
     * be written to a file.
     * 
     * @param actionName
     */
    public void setSaveToFileAction(String actionName) {
        mSaveToFileAction = actionName;
    }

    public String getSaveToFileAction() {
        return mSaveToFileAction;
    }

    /**
     * Set the action name to be sent back to the engine when the user
     * double-clicks a line.
     * 
     * @param s
     *            the name of the action.
     */
    public void setDoubleClickAction(String s) {
        mDoubleClickAction = s;
    }

    /**
     * Set the action name to be sent back to the engine when the user clicks on
     * column 0 of a line.
     * 
     * @param s
     *            the name of the action.
     */
    public void setColumn0ClickAction(String s) {
        mColumn0ClickAction = s;
    }

    /**
     * Set the action name to be sent back to the engine when a tooltip is
     * requested.
     * 
     * @param s
     *            the name of the action.
     */
    public void setWantsToolTipAction(String s) {
        mWantsToolTipAction = s;
    }
    
    public MenuDescriptor getMenuDescriptor(){
        MenuDescriptor md = new MenuDescriptor();
        addDynamicPopupMenu(md);
        md.addSeparator();
        if (mStaticPopupMenu != null)
            md.addAllMenuItems(mStaticPopupMenu);
        return md;
    }
    
    /**
     * Called to fill in menu items that are subject to change
     * depending on selection, etc.
     * @param md the menu to be appended to.
     */
    protected void addDynamicPopupMenu(MenuDescriptor md){
        //by default, there is no dynamic menu items.
    }

    /**
     * Return the menu descriptor that is static. As items are added to
     * it, they remain.
     * @return the static-portion of the popup menu.
     */
    public MenuDescriptor getStaticMenuDescriptor() {
        if (mStaticPopupMenu == null) {
            mStaticPopupMenu = new MenuDescriptor();
        }
        return mStaticPopupMenu;
    }

    public void doDoubleClick() {
        if (mDoubleClickAction != null)
            mSender.sendValueUpdate(mDoubleClickAction, "");
    }

    public void doColumn0Click() {
        if (mColumn0ClickAction != null)
            mSender.sendValueUpdate(mColumn0ClickAction, "");
    }

    public void doToolTipsClick() {
        if (mWantsToolTipAction != null)
            mSender.sendValueUpdate(mWantsToolTipAction, "");
    }

     protected IChoice makeChoiceWidget(String label, String name, String tip) {       
        IChoice choice = mToolBarBuilder.makeComboBox(label, true);
        choice.setColumns(10);
        getValueMap().put(name,choice);
        choice.setToolTipText(tip);
        return choice;
    }
    
    private Map<String,IWidget>getValueMap(){
        if (mValueMap == null) mValueMap = new HashMap<String,IWidget>();
        return mValueMap;
    }
    
    /**
     * Register a widget so that it can be found by {@link #findComponent}.
     * @param widget the widget.
     * @param id the name associated with the widget.
     */
    public void registerToolBarItem(IWidget widget, String id){
        getValueMap().put(id,widget);
    }
    
    public IChoice makeChoiceWidget(final String label, final String name,
            String tip, final String valueProperty, final boolean saveBetweenSessions){
        IChoice choice = makeChoiceWidget(label,name,tip);
        // Assign a unique name for the benefit of the GUI tester...
        choice.setName(mView.getDisplayKind() + ".combo." + valueProperty);
        // "name" doesn't ever seem to be used. Engine addresses widget from value property
        getValueMap().put(valueProperty,choice);
        choice.addObserver(new IChoice.IObserver(){
                @Override
                public void selectionChanged(IChoice combo){
                    String s = combo.getText();
                    if (s == null) s = "";
                    //By convention, don't send blank to the engine. It will stupidly interpret as
                    // an expression. So, we interpret it to mean not to restore anything.
                    if (s.trim().length() > 0)
                        mSender.sendValueUpdate(valueProperty,combo.getText(), saveBetweenSessions);
                    else 
                    	mView.removeValueUpdate(valueProperty);
                }
            });
        return choice;
    }
    
    public IChoice makeChoiceWidget(String label, String name,
            String tip, String valueProperty){
        return makeChoiceWidget(label,name,tip,valueProperty,false);
    
    }

    /**
     * Called when construction is completed.
     *  
     */
    public void finish() {

    }
    
    protected void addSeparator(){
        mToolBarBuilder.makeSeparator();
    }
    
    /**
     * Return the path to the debugger installation so that we can access icons, etc.
     * @return the path to the debugger installation.
     */
    protected String getSCDir(){
        return getViewer().getDebuggerInstallPath();
    }

    public IToolItem makeButton(String labelKey, final String valueUpdate,
            String tip) {
        
        IToolItem b = mToolBarBuilder.makeButton();
        
        // Assign a unique name for the benefit of the GUI tester...
        b.setName(mView.getDisplayKind() + ".button." + valueUpdate);
        
        b.setToolTipText(tip);
        getValueMap().put(valueUpdate,b);
        LabelsAndIcons.setButtonAttributes(labelKey, b, mWidgetFactory,
            getSCDir());
        b.addObserver(new IToolItem.IObserver() {

            @Override
            public void itemChanged(IToolItem item) {
                mSender.sendValueUpdate(valueUpdate, "");

            }        });
        return b;
    }
    
    public IImageWidget makeLED(IColor color, final String value_id){
        IImageWidget w = mToolBarBuilder.makeImage(8,20,1);
        // Assign a unique name for the benefit of the GUI tester...
        w.setName(mView.getDisplayKind() + ".led." + value_id);
        w.setColor(new IColor[]{color});
        if (value_id != null){
            w.addObserver(new IObserver(){

                @Override
                public void onMousePressed (IImageWidget widget) {
                    mSender.sendValueUpdate(value_id,"");
                    
                }});
            getValueMap().put(value_id,w);
        }
        return w;
    }
    
    public IContainer getControl(){
        if (mContainer == null){
            mContainer = mWidgetFactory.makeContainer(mView.getComponent(),IComponentFactory.COLUMN_STYLE);
        }
        return mContainer;
    }
    
    public void makeMenuItem(String labelKey, String valueUpdate, MenuDescriptor.IActionObserver observer){
        MenuDescriptor md = getStaticMenuDescriptor();
        String label = LabelsAndIcons.getButtonLabel(labelKey);
        md.addMenuItem(valueUpdate, label, observer);
    }

    public void makeMenuItem(String labelKey, final String valueUpdate,
            String tip) {
        makeMenuItem(labelKey,valueUpdate, new MenuDescriptor.IActionObserver() {

            @Override
            public void actionPerformed(String name) {
                mSender.sendValueUpdate(valueUpdate, "");
            }
        });
    }
    
    /**
     * Set the thing selected during a right-click pop-up operation.
     * @param line the line number being selected (0-based).
     * @param startColumn the 0-based starting column of the selection.
     * @param endColumn the 0-based column number immediately following the selection.
     * @param selection the selected text, or empty string, or <code>null</code>.
     */
    public void setSelection(int line, int startColumn, int endColumn, String selection){
        // Not relavent to most panels.
    }
    
    public void addMenuSeparator(){
        MenuDescriptor md = getStaticMenuDescriptor();
        md.addSeparator();
    }
    
    protected void makeButtonAndMenuItem(String labelKey, String valueupdate, String tip){
       makeButtonAndMenuItem(labelKey,labelKey,valueupdate,tip);
    }
    
    protected void makeButtonAndMenuItem(String buttonLabelKey, String menuItemLabel, String valueupdate, String tip){
        makeButton(buttonLabelKey,valueupdate,tip);
        makeMenuItem(menuItemLabel,valueupdate,tip);
    }
    
    /**
     * Create a toggle button in the header.
     * @param valueID the engine property to be modified by this button.
     * @param initValue initial setting of the button.
     * @param label1 label when the button is not pressed
     * @param label2 label when the button is pressed.
     * @param tip1 the tooltip when not pressed.
     * @param tip2 the tooltip when pressed.
     * @param saveStateBetweenSessions whether or not the state of
     * this button is saved between sessions.
     * @return the toggle button.
     */
    public IToolItem makeBooleanToggle( final String valueID,
            boolean initValue, final String label1, final String label2,
            final String tip1, final String tip2,
            final boolean saveStateBetweenSessions) {
 
        IToolItem b = mToolBarBuilder.makeToggleButton();
        b.setName(getViewer().getDisplayKind() + ".button." + valueID);
        
        b.setSelected(initValue);
        getValueMap().put(valueID,b);
        setBooleanToggleAttributes(b, label1, label2, tip1, tip2);

        b.addObserver(new IToolItem.IObserver() {

            @Override
            public void itemChanged(IToolItem item) {
                mSender.sendValueUpdate(valueID, item.isSelected() ? "1" : "0",saveStateBetweenSessions);
                setBooleanToggleAttributes(item, label1, label2, tip1, tip2);
            }
        });
        return b;
    }
    
    private  void setBooleanToggleAttributes(IToolItem toggle,
            String label1, String label2, String tip1, String tip2) {
        if (!toggle.isSelected()) {
            LabelsAndIcons.setButtonAttributes(label1, toggle, mWidgetFactory,getSCDir());
            toggle.setToolTipText(tip1);
        } else {
            LabelsAndIcons.setButtonAttributes(label2, toggle, mWidgetFactory,getSCDir());
            toggle.setToolTipText(tip2);
        }
    }


    /**
     * Return whether or not column 0 is special in regard to double-clicks. If
     * it is a tree to be expanded, then it may not be double-clicked to select
     * a line.
     * 
     * @return whether or not double-clicks permitted from column 0.
     */
    public boolean getPermitsDoubleClickInColumn0() {
        return mColumn0ClickAction == null;
    }

    /**
     * A method required by several subclasses to create a combobox that
     * accepts a regular expression.
     *
     */
    protected void makeNameChoice () {
        makeChoiceWidget("Name: ","Name:","Name or regular expression",
                "regex_name");
    }
    
    public void transmitFileSaveRequest(String file){
        mSender.sendValueUpdate(this.getSaveToFileAction(),file.toString());
    }
    
    public void makeSeparator(){
        mToolBarBuilder.makeSeparator();
    }
    
    public IChoice makeChoice(String itemList[],
            String defaultIndex, final String valueID, String tip,
            final boolean saveState, final IValueSender sender) {
        final IChoice choice = mToolBarBuilder.makeComboBox(null,false);
        
        // Assign a unique name for the benefit of the GUI tester...
        choice.setName(mView.getDisplayKind() + ".combo." + valueID);
        
        for (int i = 0; i < itemList.length; i++) {
            choice.addItem(itemList[i]);
        }
        choice.setSelectionIndex(Integer.parseInt(defaultIndex));
        choice.setToolTipText(tip);
        choice.addObserver(new IChoice.IObserver() {

            @Override
            public void selectionChanged(IChoice c) {
                sender.sendValueUpdate(valueID, c.getText(), saveState);

            }
        });
        getValueMap().put(valueID,choice);
        return choice;
    }
    
    public ILabel makeLabel(String text, String id){
        ILabel label = mToolBarBuilder.makeLabel(text);
     
        if (id != null){
            // Assign a unique name for the benefit of the GUI tester...
            label.setName(mView.getDisplayKind() + ".label." + id);
            getValueMap().put(id,label);
        }
        return label;
    }
    
    public IComponent makeTextField(String label,
            final String valueID, String tip, int width,
            final boolean saveState, boolean CR_needed, final IValueSender sender) {
        final IChoice textField = mToolBarBuilder.makeComboBox(label,true);
        if (width > 0)
            textField.setColumns(width);
        // Assign a unique name for the benefit of the GUI tester...
        textField.setName(mView.getDisplayKind() + ".text." + valueID);
        
        textField.setToolTipText(tip);
        if (CR_needed) {
            
            textField.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent event) {
                    sender.sendValueUpdate(valueID, textField.getText(),
                            saveState);

                }
            });
        } else {
            
            textField.addTextObserver(new IChoice.ITextObserver() {

                @Override
                public void textChanged (IChoice choice, String text) {
                    sender.sendValueUpdate(valueID, text,
                            saveState);
                    
                }
            });

        }
        getValueMap().put(valueID,textField);
        return textField;
    }
    
    protected IToolBarBuilder getToolBarBuilder(){
        return mToolBarBuilder;
    }
    
    protected IComponentFactory getWidgetFactory(){
        return mWidgetFactory;
    }
//    public IButton makeButton(String labelKey,
//            final String value_id, String tip, final IValueSender sender) {
//        mToolBar = null;
//        IButton b = mFactory.makeButton(getControl());
//        b.setName(value_id); // so button can be found by name.
//        b.setText(LabelsAndIcons.getButtonLabel(labelKey));
//        b.setToolTipText(tip);
//        IImage image = LabelsAndIcons.getButtonIcon(labelKey,mFactory);
//        if (image != null)
//            b.setImage(image);
//        b.addActionListener(new ActionListener() {
//
//            public void actionPerformed(ActionEvent event) {
//                sender.sendValueUpdate(value_id, "1");
//            }
//        });
//        return b;
//    }

}
