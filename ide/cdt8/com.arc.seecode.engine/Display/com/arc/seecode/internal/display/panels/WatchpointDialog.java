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

import com.arc.seecode.display.IContext;
import com.arc.seecode.engine.IEngineAPI;
import com.arc.widgets.IButton;
import com.arc.widgets.IChoice;
import com.arc.widgets.IComponent;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;
import com.arc.widgets.ILabel;
import com.arc.widgets.ITextField;
import com.arc.widgets.IToolItem;
import com.arc.widgets.IWindow;


/**
 * Watchpoint set dialog. Translated from Standalone GUI.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
class WatchpointDialog {
    private IChoice fWatchpointChoice;
    private IButton fAddress;
    private IButton fRegister;
    private IButton fExpression;
    private IChoice fSize;
    private IChoice fThreadChoice;
    private ITextField fCondition;
    private int fMode = IEngineAPI.WP_WRITE;
    private IButton fSetButton;
    private IWindow fDialog;
    private IContext fContext;
    private IComponentFactory fFactory;
    private ITextField fValueField;
    private ITextField fMaskField;

    WatchpointDialog(IComponentFactory factory, IContext threadInfo){      
        fContext = threadInfo;
        fFactory = factory;      
    }
    
    private void makeSetWatchpointPanel(IComponentFactory fact, IContainer parent){
        IContainer p = fact.makeContainer(parent,IComponentFactory.FLOW_STYLE);
        p.setGridSpan(1,2);
        p.setHorizontalWeight(1.0);
        fact.makeLabel(p,"Set watchpoint on: ").getComponent();
        fWatchpointChoice = fact.makeComboBox(p,true);
        fWatchpointChoice.setColumns(24);
        fWatchpointChoice.setToolTipText("Enter or select watchpoint");
        fWatchpointChoice.addItem("0xXXXX");
        fWatchpointChoice.addTextObserver(new IChoice.ITextObserver(){

            @Override
            public void textChanged (IChoice choice, String text) {
                fSetButton.setEnabled(text != null && text.trim().length() > 0);  
                if (text != null && text.endsWith("\n")){
                    try {
                        setAction();
                        dismiss();
                    } catch (IllegalArgumentException x){
                        //Do nothing; bad attribute will have prompted user
                    }
                }
            }});
    }
    
    public void show(){
        fDialog = fFactory.makeDialog(fContext.getShell(), true);
        fDialog.addWindowObserver(new IWindow.IObserver(){

            @Override
            public void windowActivated (IWindow w) {  }

            @Override
            public void windowClosed (IWindow w) {}

            @Override
            public void windowClosing (IWindow w) {
                dismiss();    
            }

            @Override
            public void windowDeactivated (IWindow w) {}

            @Override
            public void windowDeiconified (IWindow w) {}

            @Override
            public void windowIconified (IWindow w) {}});
        fDialog.setTitle("Set Watchpoint");
        IContainer mainPanel = fFactory.makeGridContainer(fDialog.getContents(), 2);
        
        makeSetWatchpointPanel(fFactory,mainPanel);
        makeLocationPanel(fFactory,mainPanel);
        makeTypePanel(fFactory,mainPanel);
        makeSizePanel(fFactory,mainPanel);
        makeBreakOnThreadPanel(fFactory,mainPanel,fContext);
        makeConditionPanel(fFactory,mainPanel);   
        makeButtonsPanel(fFactory, mainPanel);
        fContext.setHelpID(fDialog.getContents(),"watch_dialog");
        fDialog.pack();
        fDialog.open();
    }
    
    /**
     * Returns the "Type" panel.
     * 
     */
    protected void makeTypePanel(IComponentFactory fact, IContainer parent) {
        IContainer panel = fact.makeGridContainer(parent,1);
        panel.setBorderTitle("Type");
        final IButton write = fact.makeRadioButton(panel);
        final IButton read = fact.makeRadioButton(panel);
        final IButton rw = fact.makeRadioButton(panel);
        write.setText("Write");
        read.setText("Read");
        rw.setText("Read/Write");
        IButton.IObserver observer = new IButton.IObserver(){

            @Override
            public void itemChanged (IToolItem item) {
                if (item.isSelected()){
                    if (item == write){
                        fMode = IEngineAPI.WP_WRITE;
                        read.setSelected(false);
                        rw.setSelected(false);
                    }
                    else if (item == read){
                        fMode = IEngineAPI.WP_READ;
                        write.setSelected(false);
                        rw.setSelected(false);
                    }
                    else {
                        fMode = IEngineAPI.WP_WRITE|IEngineAPI.WP_WRITE;
                        write.setSelected(false);
                        read.setSelected(false);
                    }
                }
                
            }};
       write.addObserver(observer);
       read.addObserver(observer);
       rw.addObserver(observer);
       write.setSelected(true);
    }
    
    protected void makeLocationPanel(IComponentFactory fact, IContainer parent) {
        IContainer locationPanel = fact.makeGridContainer(parent,1);
        locationPanel.setBorderTitle("Location");
        fAddress = fact.makeRadioButton(locationPanel);
        fAddress.setText("Address");
        fAddress.setHorizontalWeight(1.0);
        fAddress.setHorizontalAlignment(IComponent.FILL);
       
        fAddress.addObserver(new IButton.IObserver() {

            @Override
            public void itemChanged (IToolItem item) {
                if (item.isSelected()){
                    if (fSize != null)
                        fSize.setEnabled(true);
                    String curItem = fWatchpointChoice.getText();
                    if (curItem.equals("RR") || curItem.equals("expn"))
                        fWatchpointChoice.setSelection("0xXXXX");
                    fRegister.setSelected(false);
                    fExpression.setSelected(false);
                }
                
            }
        });

        fRegister = fact.makeRadioButton(locationPanel);
        fRegister.setText("Register");
        fRegister.setHorizontalWeight(1.0);
        fRegister.setHorizontalAlignment(IComponent.FILL);
        fRegister.addObserver(new IButton.IObserver() {

            @Override
            public void itemChanged (IToolItem item) {
                if (item.isSelected()){
                    if (fSize != null)
                        fSize.setEnabled(false);
                    String curItem = fWatchpointChoice.getText();
                    if (curItem.equals("0xXXXX") || curItem.equals("expn"))
                        fWatchpointChoice.setSelection("RR");
                    fAddress.setSelected(false);
                    fExpression.setSelected(false);
                }
               
            }
        });
       
        fExpression = fact.makeRadioButton(locationPanel);
        fExpression.setText("Expression");
        fExpression.setHorizontalWeight(1.0);
        fExpression.setHorizontalAlignment(IComponent.FILL);
        fExpression.addObserver(new IButton.IObserver() {

            @Override
            public void itemChanged (IToolItem item) {
                if (item.isSelected()) {
                    fSize.setEnabled(false);
                    String curItem = fWatchpointChoice.getText();
                    fAddress.setSelected(false);
                    fRegister.setSelected(false);
                    if (curItem.equals("0xXXXX") || curItem.equals("RR"))
                        fWatchpointChoice.setSelection("expn");
                }
            }
        });
        fAddress.setSelected(true);
    }
    
    protected void makeSizePanel(IComponentFactory fact, IContainer parent) {
        IContainer p = fact.makeContainer(parent,IComponentFactory.FLOW_STYLE);
        p.setBorderTitle("Size");
        p.setHorizontalAlignment(IComponent.FILL);

        fSize = fact.makeComboBox(p, true);
     // Set to a good-looking size...
        //fSize.setPreferredSize(80, 22);
        fSize.setColumns(8);
        fSize.addItem("1");
        fSize.addItem("2");
        fSize.addItem("4");
        fSize.addItem("8");
        fSize.setSelectionIndex(0);
        
    }
    
    protected void makeBreakOnThreadPanel(IComponentFactory fact, IContainer parent,
        IContext threadInfo) {
        IContainer p = fact.makeContainer(parent,IComponentFactory.FLOW_STYLE);
        p.setBorderTitle("Break on Thread");

        IContext.IThread[] threads = threadInfo != null?threadInfo.getThreads():new IContext.IThread[0];
        
        fThreadChoice = fact.makeComboBox(p,false);
        fThreadChoice.addItem("Not Thread Specific");
        if (threads.length > 1) {
            for (IContext.IThread thread: threads){
                fThreadChoice.addItem(thread.getName());
            }
            fThreadChoice.setEnabled(true);
            fThreadChoice.setSelectionIndex(0);
        } else {
            fThreadChoice.setEnabled(false);
        }
    }
    
    /**
     * Returns the "Watch on Condition" panel
     */
    protected void makeConditionPanel(IComponentFactory fact, IContainer parent) {
        IContainer p = fact.makeGridContainer(parent,4);
        p.setGridSpan(1,2);
        p.setBorderTitle("Watch on Condition");
        ILabel conditionLabel = fact.makeLabel(p,"Condition: ");
        conditionLabel.setHorizontalAlignment(IComponent.BEGINNING);
        conditionLabel.getComponent(); //hack to get it to materialize

        
        fCondition = fact.makeTextField(p);
        fCondition.setHorizontalAlignment(IComponent.FILL); 
        fCondition.setHorizontalWeight(1.0);
        fCondition.setGridSpan(1,3);
        fCondition.getComponent(); // hack
        
        if (fContext.supportsWatchpointMask()){
            fact.makeLabel(p,"Value:").getComponent();
            fValueField = fact.makeTextField(p);
            fValueField.setToolTipText("Value to test for after applying mask");
            fValueField.setHorizontalAlignment(IComponent.FILL);
            fValueField.setColumns(12);
            fValueField.getComponent();
            
            fact.makeLabel(p,"  Mask: ").getComponent();
            fMaskField = fact.makeTextField(p);
            fMaskField.setHorizontalAlignment(IComponent.FILL);
            fMaskField.setColumns(12);
            fMaskField.setToolTipText("Mask to apply before comparing with value");
            fMaskField.setText("");
            fMaskField.getComponent();
        }
    }
    
    /**
     * Returns a panel containing the Ok, Cancel, and Help buttons.
     */
    protected void makeButtonsPanel(IComponentFactory factory, IContainer parent) {
        IContainer p = factory.makeGridContainer(parent,1);
        p.setGridSpan(1,2);
        IContainer buttonsPanel = factory.makeContainer(p,IComponentFactory.FLOW_STYLE);
        buttonsPanel.setHorizontalAlignment(IComponent.END);

        fSetButton = factory.makeButton(buttonsPanel);
        fSetButton.setText(" Set ");
        fSetButton.addObserver(new IButton.IObserver(){

            @Override
            public void itemChanged (IToolItem item) {
                setAction();
                item.setEnabled(false);             
            }});
        fSetButton.setEnabled(false);
        fDialog.setDefaultButton(fSetButton);
        
        IButton closeButton = factory.makeButton(buttonsPanel);
        closeButton.setText("Close");
        closeButton.addObserver(new IButton.IObserver(){

            @Override
            public void itemChanged (IToolItem item) {
                dismiss();               
            }});
    }
    
    private void dismiss(){
        fDialog.dispose();       
    }
    
    private void setWatchpoint() throws IllegalStateException{
        String expr = fWatchpointChoice.getText();
        String cond = fCondition.getText();
        String watchpointSize = fSize.getText();
        long mask = 0;
        long value = 0;
        int threadID = 0;
        int size = 0;
        
        if (cond != null && cond.trim().length() == 0) cond = null;
        
        boolean address = fAddress.isSelected();
        boolean register = fRegister.isSelected();

        // deal with the size here
        if (address && watchpointSize != null
                && watchpointSize.trim().length() > 0) {
            size = (int)extractValue(watchpointSize.trim(),"size");
        }

        // deal with the thread-specific watchpoints here
        if (fThreadChoice != null) {
            int ix = fThreadChoice.getSelectionIndex();
            if (ix > 0) {
                threadID = fContext.getThreads()[ix-1].getIndex();
            }
        }

       
        if (fValueField != null && fValueField.isEnabled() &&
                fValueField.getText() != null &&
                fValueField.getText().trim().length() != 0){
            value = extractValue(fValueField.getText().trim(),"value");
            
        }
        if (fMaskField != null &&  fMaskField.isEnabled() &&
                fMaskField.getText() != null &&
                fMaskField.getText().trim().length() != 0){
            mask = extractValue(fMaskField.getText().trim(),"mask");
            if (mask == 0){
                error("Mask must be non-zero, if specified");
                throw new IllegalStateException("Mask must be non-zero");
            }
        }
        if (value != 0) {
            if (mask == 0){
                error("Mask must be non-zero if value is set");
                throw new IllegalStateException("Mask must be non-zero if value is set");
            }
        }

        if (!register)
           this.fContext.setWatchpoint(expr, size, cond, threadID, fMode, mask, value);
        else
            this.fContext.setWatchpointReg(expr, cond, threadID, fMode, mask, value);
    }
    
    private long extractValue(String value, String what) throws IllegalArgumentException{
        try {
            if (value.startsWith("0x") || value.startsWith("0X")){
                return Long.parseLong(value.substring(2),16);
            }
            return Long.parseLong(value);
        }
        catch (NumberFormatException e) {
            error("Invalid value for " + what + ": " + value);
            throw new IllegalArgumentException(e);
        }
    }
    
    private void error(String msg){
        fContext.displayError(msg,"Watchpoint Set Failure");
    }
    
    /**
     * Gets called when the user clicks the "Apply" button.
     * @throws IllegalStateException if a bogus value is specified.
     */
    private void setAction() throws IllegalStateException {
        setWatchpoint();  // may throw IllegalStateException
        // remember the last entered item
        fWatchpointChoice.addItem(fWatchpointChoice.getText());
        removeChoiceItem("0xXXXX");
        removeChoiceItem("RR");
        removeChoiceItem("expn");
    }
    
    private void removeChoiceItem(String s){
        for (int i = 0; i < fWatchpointChoice.getItemCount(); i++) {
            if (s.equals(fWatchpointChoice.getItemAt(i))){
                fWatchpointChoice.removeItemAt(i);
                break;
            }
        }
    }
}
