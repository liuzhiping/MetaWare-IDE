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

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.arc.cdt.debug.seecode.core.ISeeCodeConstants;
import com.arc.cdt.debug.seecode.core.SeeCodePlugin;


public class PromptForEngineSelectionDialog extends Dialog {

    private Shell fShell;
    private String fEnginePath;
    private int fToolsetBuildID;
    private int fBundledBuildID;
    private boolean fResult;
    private int fStrategy = ISeeCodeConstants.ENGINE_VERSION_PROMPT;

    public PromptForEngineSelectionDialog(Shell parent,int bundledBuildID, int toolsetBuildID, String toolset) {
        super(parent);
        setText("Choose Debugger Engine");  
        fBundledBuildID = bundledBuildID;
        fToolsetBuildID = toolsetBuildID;
        fEnginePath = toolset;
        fResult = true;
    }
    
    /**
     * Open the dialog and return true if the toolset engine is to be used.
     * @return true if the toolset engine is to be used.
     */
    @SuppressWarnings("deprecation")
	public boolean open(){
        Shell parent = getParent();
        fShell = new Shell(parent, SWT.DIALOG_TRIM | getStyle());
        fShell.setText(getText());
        fShell.setLayout(new GridLayout(1, true));
        
        fStrategy = SeeCodePlugin.getDefault().getPluginPreferences().getInt(ISeeCodeConstants.PREF_ENGINE_VERSION_MANAGEMENT);
        
        Text desc = new Text(fShell,SWT.READ_ONLY|SWT.MULTI);
        desc.setText("The debugger engine bundled with the IDE differs from the one in the\n" +
            "installed toolkit at " + new File(fEnginePath).getParent() + ".\n" +
            "The bundled version has build ID " + fBundledBuildID + ";\n" +
            "The toolkit version has build ID " + fToolsetBuildID + ".\n\n" +
            "Specify which debugger engine to use: ");
        
        final Button b1 = new Button(fShell,SWT.RADIO);
        final Button b2 = new Button(fShell,SWT.RADIO);
        b1.setText("Use the toolkit version");
        b2.setText("Use the bundled version");
        b1.setSelection(true);
        b2.setSelection(false);
        SelectionListener listener = new SelectionListener(){

            @Override
            public void widgetDefaultSelected (SelectionEvent e) {
                // @todo Auto-generated method stub
                
            }

            @Override
            public void widgetSelected (SelectionEvent e) {
                if (e.getSource() == b1){
                    b2.setSelection(!b1.getSelection());
                }
                else {
                    b1.setSelection(!b2.getSelection());
                }
                fResult = b1.getSelection();
                
            }};
         b1.addSelectionListener(listener);
         b2.addSelectionListener(listener);
         
         Label padding = new Label(fShell,0);
         padding.setText("\n\n");
         
         Group group = new Group(fShell,0);
         group.setLayout(new GridLayout(1,false));
         group.setText("What to do if this condition occurs again:");
         createRadio(group,"Continue prompting",ISeeCodeConstants.ENGINE_VERSION_PROMPT);
         createRadio(group,"Always choose latest engine",ISeeCodeConstants.ENGINE_VERSION_USE_LATEST);
         createRadio(group,"Always use bundled engine",ISeeCodeConstants.ENGINE_VERSION_USE_BUNDLED);
         createRadio(group,"Always use toolkit engine",ISeeCodeConstants.ENGINE_VERSION_USE_TOOLSET);
         
         Label label = new Label(group,0);
         label.setText("\n\nNote: this setting can also be specified from the Preferences dialog\n"+
             "under C/C++ --> Debugger --> MetaWare Debugger");
         
         
         Button ok = new Button(fShell,SWT.PUSH);
         ok.setText("OK");
         ok.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
         ok.addSelectionListener(new SelectionListener(){

            @Override
            public void widgetDefaultSelected (SelectionEvent e) {
                widgetSelected(e);
                
            }

            @Override
            public void widgetSelected (SelectionEvent e) {
                saveStrategy();
                fShell.dispose();
                
            }});
         fShell.pack();
         centerShell(parent, fShell);

         fShell.open();

         Display display = parent.getDisplay();
         while (!fShell.isDisposed()) {
             if (!display.readAndDispatch())
                 display.sleep();
         }
        
         return fResult;       
    }
    
    @SuppressWarnings("deprecation")
	private void saveStrategy(){
        SeeCodePlugin.getDefault().getPluginPreferences().setValue(ISeeCodeConstants.PREF_ENGINE_VERSION_MANAGEMENT,fStrategy);
    }
    
    private void createRadio(Composite parent, String label, final int value){
        final Button b = new Button(parent,SWT.RADIO);
        b.setText(label);
        b.addSelectionListener(new SelectionListener(){

            @Override
            public void widgetDefaultSelected (SelectionEvent e) {
                // @todo Auto-generated method stub
                
            }

            @Override
            public void widgetSelected (SelectionEvent e) {
                if (b.getSelection()){
                    fStrategy = value;
                }
                
            }});
        b.setSelection(value == fStrategy);
    }
    
    private void centerShell (Shell parent, Shell shell) {
        Rectangle parentRect = parent.getBounds();
        Point size = fShell.getSize();
        shell.setLocation(parentRect.x + (parentRect.width - size.x) / 2, parentRect.y +
            (parentRect.height - size.y) /
            2);
    }

}
