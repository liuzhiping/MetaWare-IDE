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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;

import com.arc.cdt.debug.seecode.ui.UISeeCodePlugin;
import com.arc.cdt.debug.seecode.ui.Utilities;
import com.arc.mw.util.StringUtil;

/**
 * File dialog panel consisting of a combobox (to show history), a clear button,
 * and a Browse-to button.
 * 
 * @author David Pickens Dec 6, 2010
 */
public class FileFieldPanel  {

    // When using file browse repeatedly, always start from last
    // directory that was sought. We make it static so that we
    // can share this with all instances of this class.
    private static String sLastDirectory = null;
    
    private boolean fIsDirectory = false;
    
    private String fWorkingDirectory = "."; //Directory to use if "." is seen.

    private String fPanelLabel;

    private String[] fInitialHistoryList;

    private String fTip;

    private Label fLabel;
    private Button fBrowse;
    private Combo fCombo;
    private transient boolean fTextTyped = false;
    
    private boolean fQuoteIfSpaces = false;
    
    private transient Dialog fFileDialog = null;  //FileDialog or DirectoryDialog
    private Composite fPanel;

    private Button fClear;
    
    static public interface IValidator {
        /**
         * If "text" is valid, then return true;
         * otherwise, set an error state and return false.
         * @param text the file being set.
         * @return true if valid.
         */
        public boolean validate(String text);
    }
    
    private IValidator fValidator = null;
    
    public FileFieldPanel(
            String panelLabel,
            String[] initialHistoryList,
            String tip) {
        this(panelLabel,initialHistoryList,tip,false);
    }

    /**
     * @param panelLabel
     *                the label to appear at the start of the field, or null.
     * @param initialValues
     *                the initial values of the combobox field.
     * @param tip
     *                the tool tip.
     * @param isDirectory if true, the field is to denote a directory.
     */
    FileFieldPanel(
        String panelLabel,
        String[] initialHistoryList,
        String tip,
        boolean isDirectory) {

        fPanelLabel = panelLabel;
        fInitialHistoryList = initialHistoryList;
        fTip = tip;
        fIsDirectory = isDirectory;
    }
    
    public void setValidator(IValidator v){
        fValidator = v;
    }
    
    public void createControl(Composite parent) {
        
        Composite panel = new Composite(parent,0);
        fPanel = panel;
        GridLayout layout = new GridLayout(4,false);
        panel.setLayout(layout);

      
        if (fPanelLabel != null) {
            fLabel = new Label(panel,0);
            fLabel.setText(fPanelLabel);
        }

        fCombo = new Combo(panel,SWT.DROP_DOWN);
        fCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fCombo.addKeyListener(new KeyListener(){

            @Override
            public void keyPressed(KeyEvent e) {               
            }

            @Override
            public void keyReleased(KeyEvent e) {
                fTextTyped = true;                   
            }});
        fCombo.addFocusListener(new FocusListener() {
            private Button saveDefaultButton;

            @Override
            public void focusGained(FocusEvent event) {
                // We want to process return key; don't dismiss shell!
                saveDefaultButton = fCombo.getShell().getDefaultButton();
                fCombo.getShell().setDefaultButton(null);
                fTextTyped = false;
            }

            @Override
            public void focusLost (FocusEvent event) {
                fCombo.getShell().setDefaultButton(saveDefaultButton);
                String text = fCombo.getText();
                if (fTextTyped && text.length() > 0) {
                    text = text.trim();
                    fTextTyped = false;
                    if (validate(text)) {
                        String[] items = fCombo.getItems();
                        for (int i = 0; i < items.length; i++) {
                            if (items[i].equals(text)) {
                                fCombo.remove(i);
                                break;
                            }
                        }
                        fCombo.add(text, 0);
                        fCombo.select(0);
                    }
                }
            }
        });
        setHistoryList(fInitialHistoryList);
        
        fClear = new Button(panel,SWT.PUSH);
        fClear.setImage(UISeeCodePlugin.getDefault().getImage("icons/clear.gif"));
        fClear.addSelectionListener(new SelectionListener(){

            @Override
            public void widgetSelected (SelectionEvent e) {
                fCombo.setItems(new String[]{});
                fCombo.clearSelection();               
            }

            @Override
            public void widgetDefaultSelected (SelectionEvent e) {              
            }});
        
        
        fBrowse = new Button(panel,SWT.PUSH);
        fBrowse.setText("Browse");
        fBrowse.setToolTipText(fIsDirectory?"Look for directory":"Look for file");
        fBrowse.addSelectionListener(new SelectionListener(){

            @Override
            public void widgetSelected (SelectionEvent e) {
                doBrowseAction();        
            }

            @Override
            public void widgetDefaultSelected (SelectionEvent e) {
               
                
            }});   
    }
    
    protected boolean validate(String text){
        if (fValidator != null) return fValidator.validate(text);
        return true;
    }
    
    public void setWorkingDirectory(String s){
        fWorkingDirectory = s;
    }

    public void setEnabled(boolean value) {
        fPanel.setEnabled(value);
        fCombo.setEnabled(value);
        fBrowse.setEnabled(value);
        fClear.setEnabled(value);
    }
    
    public Control getControl(){
        return fPanel;
    }
    
    public void setQuoteIfSpaces(boolean v){
        fQuoteIfSpaces = v;
    }
    
    public void addModifyListener(ModifyListener listener){
        fCombo.addModifyListener(listener);
    }

    public void setFocus() {
        // Don't know why JComboBox.requestFocus doesn't appear to work.
        // mFileName.getEditor().getEditorComponent().requestFocus();
        fCombo.setFocus();
    }

    String getFile() {
        return fCombo.getText();
    }

    public void setFile(String s) {
        List<String> list = new ArrayList<String>(fCombo.getItemCount()+1);
        if (s == null) s = "";
        s = s.trim();
        if (s.equals(".")) s = fWorkingDirectory;
        if (fCombo.getItemCount() > 0) {
            list.addAll(Arrays.asList(fCombo.getItems()));
            // Don't put empty entries in history list.
            int empty = list.indexOf("");
            if (empty >= 0) {
                list.remove(empty);
                fCombo.setItems(list.toArray(new String[list.size()]));
            }
        }
        int index = list.indexOf(s);
        if (index > 0){
            fCombo.remove(index);
        }
        if (index != 0) {
            fCombo.add(s,0);
        }
        fCombo.select(0);
    }

    public void addToHistoryList(String s) {
        String[] items = fCombo.getItems();
        int index = Arrays.asList(items).indexOf(s);
        if (index < 0) {
            fCombo.add(s);
        }
    }

    public void setHistoryList(String list[]) {
        if (list == null){
            list = new String[]{};
        }
        String item = fCombo.getText();
        fCombo.setItems(list);
        fCombo.setText(item);
    }

    public String[] getHistoryList() {
        return fCombo.getItems();
    }

    /**
     * Invoked when "browse" button pressed.
     */
    private void doBrowseAction() {
        if (fFileDialog == null) {
            fFileDialog = fIsDirectory?new DirectoryDialog(fCombo.getShell()):new FileDialog(fCombo.getShell());
            fFileDialog.setText(fTip != null?fTip:(fIsDirectory?"Select directory":"Select file"));
        }
        String curFile = getFile();
        String current[] = null;
        if (curFile  != null && curFile.trim().length() > 0){
            if (fQuoteIfSpaces){
                current = StringUtil.stringToArray(curFile);
                curFile = current.length > 0 ?current[0]:"";
            }
            curFile = curFile.trim();
            if (curFile.equals("."))
                curFile = fWorkingDirectory;
            File f = new File(curFile);
            sLastDirectory = f.isDirectory()?f.getPath():f.getParent();
            curFile = f.getName();
        }
        if (sLastDirectory == null) {
            sLastDirectory = Utilities.getWorkspacePath();
        }
        if (sLastDirectory != null){
            if (fFileDialog instanceof FileDialog){
                ((FileDialog)fFileDialog).setFilterPath(sLastDirectory);
                if (curFile != null)
                    ((FileDialog)fFileDialog).setFileName(curFile);
            }
            else {
                ((DirectoryDialog)fFileDialog).setFilterPath(sLastDirectory);
            }
        }
            
        String s = fFileDialog instanceof FileDialog?((FileDialog)fFileDialog).open():
                                                     ((DirectoryDialog)fFileDialog).open();
        if (s != null && s.trim().length() > 0) {
            sLastDirectory = new File(s).getParent();
            if (fQuoteIfSpaces) {
                if (current == null || current.length == 0) current = new String[1];
                current[0] = s;
                s = StringUtil.arrayToArgString(current);
            }
            setFile(s);
        }
        fCombo.setFocus();
    }

    /**
     * For the sake of the GUI tester, make sure the text field has a name
     * when this panel is assigned one.
     */
    public void setName(String name) {
        fCombo.setData("name",name + ".textField");
    }
   

}
