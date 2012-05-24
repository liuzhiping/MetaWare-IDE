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

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.arc.cdt.debug.seecode.internal.ui.FileFieldPanel.IValidator;
import com.arc.cdt.debug.seecode.ui.Utilities;
import com.arc.mw.util.StringUtil;


/**
 * @author pickensd
 */
class CMPDPrelaunchBlock {

    private Composite panel;

    private Button fPrelaunchCheckBox;

    private FileFieldPanel fApplicationText;
    private FileFieldPanel fWorkingDirText;

    
    /**
     * A listener to update for text changes and widget selection
     */
    private class WidgetListener extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            Object source = e.getSource();
            if (source == fPrelaunchCheckBox) {
                handlePrelaunchCheckBoxSelected(fPrelaunchCheckBox.getSelection());
            }
        }
    }

    private WidgetListener fListener = new WidgetListener();

    private Combo fDelayCombo;

    private Runnable fRefresh;
    private String fWdError = null;
    private String fAppError = null;

    /**
     * 
     * @param refresh callback to be invoked when anything changes.
     */
    public CMPDPrelaunchBlock(Runnable refresh){
        fRefresh = refresh;
    }

    public void createControl (Composite parent) {
        Font font = parent.getFont();
        Group group = new Group(parent, SWT.NONE);
        // WorkbenchHelp.setHelp(group,
        // IJavaDebugHelpContextIds.WORKING_DIRECTORY_BLOCK);
        GridLayout exeLayout = new GridLayout(2,false);
        exeLayout.makeColumnsEqualWidth = false;
        group.setLayout(exeLayout);
        group.setFont(font);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        group.setLayoutData(gd);
        panel = group;

        group.setText("Application to invoke prior to launch"); //$NON-NLS-1$
        fPrelaunchCheckBox = new Button(group, SWT.CHECK);
        fPrelaunchCheckBox.setText("Prelaunch an application"); //$NON-NLS-1$
        gd = new GridData(GridData.FILL, GridData.BEGINNING, true, false);
        gd.horizontalSpan = 3;
        fPrelaunchCheckBox.setLayoutData(gd);
        fPrelaunchCheckBox.setFont(font);
        fPrelaunchCheckBox.addSelectionListener(fListener);
        
        Label label = new Label(group,SWT.NONE);
        label.setText("        Command to invoke prior to debugger launch: ");
        label.setLayoutData(new GridData());

        
        fApplicationText = new FileFieldPanel(null,new String[0],"Enter path to executable");
        fApplicationText.setValidator(new IValidator(){

            @Override
            public boolean validate (String text) {
                return validateApp(text);
            }});
        fApplicationText.createControl(group);

        fApplicationText.addModifyListener(new ModifyListener(){

            @Override
            public void modifyText (ModifyEvent e) {
                 validateApp(fApplicationText.getFile());
                 updateButtons();
            }});

        gd = new GridData(GridData.FILL_HORIZONTAL);
        fApplicationText.getControl().setLayoutData(gd);
        fApplicationText.getControl().setFont(font);
        fApplicationText.setQuoteIfSpaces(true);

        label = new Label(group,SWT.NONE);
        label.setText("        Working directory from which to launch: ");
        label.setLayoutData(new GridData());
        fWorkingDirText = new FileFieldPanel(null,new String[0],"Enter directory from which to launch",true);
        fWorkingDirText.createControl(group);
        fWorkingDirText.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fWorkingDirText.addModifyListener(new ModifyListener(){

            @Override
            public void modifyText (ModifyEvent e) {
                 validateWorkingDirectory(fWorkingDirText.getFile());
                 updateButtons();
            }});
        fWorkingDirText.setValidator(new IValidator(){

            @Override
            public boolean validate (String text) {
                  return validateWorkingDirectory(text);
            }});
        
        label = new Label(group,SWT.NONE);
        label.setText("        Delay before launching debugger (in seconds): ");
        label.setLayoutData(new GridData());
     
        
        fDelayCombo = new Combo(group,SWT.READ_ONLY);
        fDelayCombo.setLayoutData(new GridData());
        fDelayCombo.setItems(new String[]{"0","2","5","8","10","12","15","20"});
        fDelayCombo.select(0);
        fDelayCombo.addSelectionListener(new SelectionListener(){

            @Override
            public void widgetSelected (SelectionEvent e) {
                updateButtons();               
            }

            @Override
            public void widgetDefaultSelected (SelectionEvent e) {
                              
            }});
        handlePrelaunchCheckBoxSelected(this.fPrelaunchCheckBox.getSelection());
    }

    public Control getControl () {
        return panel;
    }

    public String getErrorMessage () {
        return fAppError != null?fAppError:fWdError;
    }
    
    public void initializeFrom(ILaunchConfiguration config){
        File wd = Utilities.getWorkingDirectory(config);
        fApplicationText.setWorkingDirectory(wd.getPath());
        fWorkingDirText.setWorkingDirectory(wd.getPath());
    }

    public void setApplication (String app) {
        if (app == null) app = "";
        fApplicationText.setFile(app);
        fPrelaunchCheckBox.setSelection(app.trim().length() > 0);
        handlePrelaunchCheckBoxSelected(fPrelaunchCheckBox.getSelection());
    }
    
    public void setWorkingDir(String wd) {
        if (wd == null) wd = "";
        fWorkingDirText.setFile(wd);
    }

    public void setDelay (int seconds) {
        String items[] = fDelayCombo.getItems();
        for (int i = 0; i < items.length; i++){
            int s = Integer.parseInt(items[i]);
            if (seconds <= s) {
                fDelayCombo.select(i);
                return;
            }
        }
        fDelayCombo.select(items.length-1);
    }

    public String getApplication () {
        return fApplicationText.getFile();
    }
    
    public String getWorkingDir(){
        return fWorkingDirText.getFile();
    }

    public int getDelay () {
        try {
            return Integer.parseInt(fDelayCombo.getText());
        }
        catch (NumberFormatException e) {
           return 0;
        }
    }

    public boolean hasPrelaunch () {
        return fPrelaunchCheckBox.getSelection() && fApplicationText.getFile() != null && fApplicationText.getFile().trim().length() > 0;
    }
    
    public void setPrelaunch(boolean v){
        fPrelaunchCheckBox.setSelection(v);
        handlePrelaunchCheckBoxSelected(v);
    }
    
//    private String[] getPathAndArgs() {
//        String s = fApplicationText.getFile().trim();
//        return StringUtil.stringToArray(s);
//    }

    private void updateButtons () {
        if (fRefresh != null) fRefresh.run();
    }
    
    protected void handlePrelaunchCheckBoxSelected(boolean v){
        fApplicationText.setEnabled(v);
        fWorkingDirText.setEnabled(v);
        this.fDelayCombo.setEnabled(v);
        updateButtons();
    }
    
    public void setAppHistory(String list[]) {
        fApplicationText.setHistoryList(list);
    }
    
    public void setWdHistory(String list[]) {
        fWorkingDirText.setHistoryList(list);
    }
    
    public String[] getAppHistory() {
        return fApplicationText.getHistoryList();
    }
    
    public String[] getWdHistory() {
        return fWorkingDirText.getHistoryList();
    }

    private boolean validateWorkingDirectory (String f) {
        fWdError = null;
        if (f != null){
            f = f.trim();
            if (f.length() > 0){
                if (!new File(f).isDirectory()){
                    fWdError = "Not a valid directory: " + f;
                    return false;
                }
            }
        }
        return true;
    }
    
    private static boolean isWindows(){
        String os = System.getProperty("os.name"); //$NON-NLS-1$
        return (os != null && os.startsWith("Win")); //$NON-NLS-1$       
    }

    private boolean validateApp (String text) {
        String cmd[] = StringUtil.stringToArray(text);
        fAppError = null;
        if (cmd.length == 0) {
            fAppError = "Need to specify command to launch";
            return false;
        }
        String s = cmd[0];
        File f = new File(s);
        // Assume relative paths with use search path.
        // NOTE: windows considers "\foo" as relative! 
        if (!f.isAbsolute() && !f.getPath().startsWith(File.separator)) return true;
        if (! f.exists() ) {
            if (isWindows()){
                for (String ext: new String[]{".exe",".com",".sh",".bat"}) {
                    if (new File(s + ext).exists()){
                        return true;
                    }
                }
            }
            fAppError = "File \"" + f + "\" does not exist.";
            return false;
        }
        return true;
    }
}
