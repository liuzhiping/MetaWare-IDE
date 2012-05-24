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

import java.io.File;

import com.arc.seecode.display.icons.LabelsAndIcons;
import com.arc.widgets.IChoice;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IFileChooser;
import com.arc.widgets.IToolItem;
import com.arc.widgets.IToolItem.IObserver;

/**
 * @author David Pickens
 */
public class SourcePanel extends SourceBasedPanel {
    private IFileChooser mFileChooser = null;
    private IChoice mChoice;
    /**
     *  
     */
    public SourcePanel() {
        super();
        //Double click caused break point to be set
        setDoubleClickAction("double_click");
    }
    

    @Override
    protected void addStaticComponents() {
        super.addStaticComponents();
        makeSourceChoice();
        makeBreakpointButton();
        makePC();
        
        setSourceWindowMenuItems();
    }
    
    private void makeBreakpointButton(){
        makeButton("Break","breakpoint","Set breakpoint at selection");
    }
    
    private void makePC(){
        makeButton("PC","show_pc","Show source at PC");
    }

    private void makeSourceChoice() {
        mChoice = makeChoiceWidget("Source: ",
                "Source:", "Set file name or function name",
                "source");
      
        IToolItem b = getToolBarBuilder().makeButton();
        b.addObserver(new IObserver() {

            @Override
            public void itemChanged(IToolItem item) {
                doFileDialog();
                
            }
        });
        b.setToolTipText("Look for file(s)");
        LabelsAndIcons.setButtonAttributes("Browse", b, getWidgetFactory(),getSCDir());
    }

    protected void doSourceChoice(String value) {
        mSender.sendValueUpdate("source", value);
    }

    private void doFileDialog() {
        if (mFileChooser == null) {
            mFileChooser = getWidgetFactory().makeFileChooser(getControl(),
                    IComponentFactory.FILE_OPEN);
        }
        File f = mFileChooser.open();
        if (f != null){
            mChoice.setSelection(f.toString());
            doSourceChoice(f.toString());
        }
    }
}
