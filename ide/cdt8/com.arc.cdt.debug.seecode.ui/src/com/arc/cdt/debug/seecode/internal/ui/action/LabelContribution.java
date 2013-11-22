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
package com.arc.cdt.debug.seecode.internal.ui.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;


class LabelContribution extends ControlContribution {

   
    private List<Label> fWidgets = new ArrayList<Label>(2); // One per DebugView
    private String fToolTip;
    private Font fFont = null;
    private String fText = null;
    private boolean fEnabled = true;

    public LabelContribution(String id, String tooltip) {
        super(id);
        fToolTip = tooltip;
        fFont = new Font(PlatformUI.getWorkbench().getDisplay(),"Ariel",8,SWT.NORMAL);
    }

    @Override
    protected Control createControl (Composite parent) {
        final Label label = new Label(parent,SWT.CENTER);
        label.setToolTipText(fToolTip);
        
        label.setFont(fFont);
        fWidgets.add(label);
        if (fText != null) label.setText(fText);
        if (!fEnabled) label.setEnabled(false);
        label.addDisposeListener(new DisposeListener(){

            @Override
            public void widgetDisposed (DisposeEvent e) {
                fWidgets.remove(label);
                
            }});
        
        return label;
    }
    
    @Override
    public void dispose(){
        super.dispose();
        fFont.dispose();
    }

    public void setText (final String s) {
        Display display = PlatformUI.getWorkbench().getDisplay();
        if (Thread.currentThread() == display.getThread()) {
            fText = s;
            for (Label label: fWidgets){
                label.setText(s);
            }
        }
        else {
            display.asyncExec(new Runnable() {

                @Override
                public void run () {
                    setText(s);

                }
            });
        }
    }
    
    public void setEnabled(final boolean e){
        Display display = PlatformUI.getWorkbench().getDisplay();
        if (Thread.currentThread() == display.getThread()) {
            fEnabled = e;
            for (Label label: fWidgets){
                label.setEnabled(e);
            }
        }
        else {
            display.asyncExec(new Runnable() {

                @Override
                public void run () {
                    setEnabled(e);
                }
            });
        }
    }
    
    @Override
    public int computeWidth(Control c){
        return 40;        
    }
    
}
