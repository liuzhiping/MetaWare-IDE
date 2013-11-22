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
package org.eclipse.cdt.managedbuilder.ui.properties;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.utils.ui.controls.FileListControl;
import org.eclipse.cdt.utils.ui.controls.IFileListChangeListener;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;


public class StringOrFileListWidget extends Composite {
    
    interface IValue {
        public void setValue(String[] values);
        public String[] getValue();
    }
    
    interface IObserver {
    	void valueChanged(StringOrFileListWidget x);
    }
    
    private List<IObserver> fObservers = null;

    private StackLayout stackLayout;
    private static final int NONE_TYPE = 0;
    private static final int STRING_TYPE = 1;
    private static final int STRING_LIST_TYPE = 2;
    private static final int FILE_TYPE = 3;
    private static final int FILE_LIST_TYPE = 4;
    private Composite panels[] = new Composite[5];
    private IValue value[] = new IValue[5];
    
    private int fSelectionIndex;
    private Combo fTypeSelector;
    private Composite stack;

    public StringOrFileListWidget(Composite parent) {
        super(parent, SWT.NONE);
        setLayout(new FormLayout());

        Label typeLabel = new Label(this, SWT.LEFT);
        typeLabel.setFont(parent.getFont());
        typeLabel.setText("Type: ");
        FormData fd = new FormData();
        fd.top = new FormAttachment(0, 100, 10);
        fd.left = new FormAttachment(0, 100, 0);
        typeLabel.setLayoutData(fd);

        fTypeSelector = new Combo(this, SWT.READ_ONLY | SWT.DROP_DOWN);
        fTypeSelector.setData("name","StringOrFileList.combo"); // For GUI tester //$NON-NLS-1$ //$NON-NLS-2$
        fd = new FormData();
        fd.left = new FormAttachment(typeLabel, 15);
        fd.top = new FormAttachment(0, 100, 10);
        fTypeSelector.setItems(new String[] { "None", "String", "String list", "File", "File list", });
        fTypeSelector.setLayoutData(fd);

        
        stackLayout = new StackLayout();
        stack = new Composite(this,SWT.NONE);
        stack.setFont(parent.getFont());
        stack.setLayout(stackLayout);
        fd = new FormData();
        fd.left = new FormAttachment(0,100,10);
        fd.right = new FormAttachment(100,100,-10);
        fd.top = new FormAttachment(fTypeSelector,15,SWT.BOTTOM);
        fd.bottom = new FormAttachment(100,100,-10);
        stack.setLayoutData(fd);
        
        createNonePanel(stack,NONE_TYPE);
        createStringPanel(stack,STRING_TYPE);
        createStringListPanel(stack,STRING_LIST_TYPE);
        createFilePanel(stack,FILE_TYPE);
        createFileListPanel(stack,FILE_LIST_TYPE);
        
        
        fTypeSelector.select(NONE_TYPE);

        fTypeSelector.addListener(SWT.Selection, new Listener() {

            public void handleEvent (Event e) {
                setTypeSelection(fTypeSelector.getSelectionIndex());
            }
        });
        setTypeSelection(NONE_TYPE);

    }
    
    private void createNonePanel(Composite parent, int selectionIndex){
        Composite blank = new Composite(parent,SWT.NONE);
        panels[selectionIndex] = blank;
    }
    
    private void setTypeSelection(int selectionIndex){
        stackLayout.topControl = panels[selectionIndex];
        fSelectionIndex = selectionIndex;
        fTypeSelector.select(selectionIndex);
        stack.layout(true);
        fireValueChanged();  // may have converted from list to single item.
    }
    
    /**
     * Return the value being set. It will be a string or string list or <code>null</code>.
     * @return the value that was set.
     */
    public String[] getValue(){
        return value[fSelectionIndex] != null?value[fSelectionIndex].getValue(): new String[0];
    }
    
    public void addObserver(IObserver observer){
    	if (fObservers == null) fObservers = new ArrayList<IObserver>();
    	fObservers.add(observer);
    }
    
    public void removeObserver(IObserver observer){
    	if (fObservers != null) fObservers.remove(observer);
    }
    
	private void fireValueChanged() {
		if (fObservers != null) {
			for (IObserver observer : fObservers) {
				observer.valueChanged(this);
			}
		}
	}
	
	static boolean looksLikeFile(String s){
		return new File(s).exists();
	}
	
	static boolean looksLikeFiles(String[] values){
		for (String s: values) {
			if (!looksLikeFile(s)) return false;
		}
		return true;
	}
    public void setValue(String[] values){
        if (values == null || values.length == 0) {
            setTypeSelection(0); // "NONE"
        }
        else if (values.length == 1 && fSelectionIndex > 0 ||
            values.length > 1 && (fSelectionIndex == FILE_LIST_TYPE || fSelectionIndex == STRING_LIST_TYPE)) {
            this.value[fSelectionIndex].setValue(values);
        }
        else {
            for (int i = 1; i < value.length; i++){
                this.value[i].setValue(values);
            }
            if (values.length == 1) setTypeSelection(looksLikeFiles(values)?FILE_TYPE:STRING_TYPE); // single file
            else setTypeSelection(looksLikeFiles(values)?FILE_LIST_TYPE:STRING_LIST_TYPE); // file list.           
        }
        fireValueChanged();
    }
    
    /**
     * Creates a panel that consists of a Text widget that sets a single string.
     * @param parent the stack composite.
     * @param selectionIndex the selection index that activates this panel.
     */
    private void createStringPanel(Composite parent, final int selectionIndex){
        Composite panel = new Composite(parent,SWT.NONE);
        panel.setLayout(new FormLayout());
        panels[selectionIndex] = panel;
        Label label = new Label(panel,SWT.LEFT);
        label.setFont(parent.getFont());
        label.setText("Value: ");
        FormData fd = new FormData();
        fd.left = new FormAttachment(0,100,10);
        fd.top = new FormAttachment(0,100,10);
        label.setLayoutData(fd);
        
        final Text text = new Text(panel,SWT.SINGLE | SWT.BORDER);
        text.setFont(parent.getFont());
        text.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent e) {
				fireValueChanged();
				
			}});
        fd = new FormData();
        fd.left = new FormAttachment(label,0);
        fd.right = new FormAttachment(100,100,-10);
        fd.top = new FormAttachment(0,100,10);
        text.setLayoutData(fd);
        
        value[selectionIndex] = new IValue(){

            public String[] getValue () {
                String s = text.getText();
                if (s != null && s.trim().length() > 0)
                    return new String[]{s.trim()};
                return new String[0];
            }

            public void setValue (String[] values) {
                text.setText(values.length > 0?values[0]:"");               
            }};
    }
    
    /**
     * Creates a panel that consists of a string-list widget.
     * @param parent the stack composite.
     * @param selectionIndex the selection index that activates this panel.
     */
    private void createStringListPanel(Composite parent, final int selectionIndex){
        makeListComposite(parent,selectionIndex,false);
    }
    
    /**
     * Creates a panel that consists of a file-list widget.
     * @param parent the stack composite.
     * @param selectionIndex the selection index that activates this panel.
     */
    private void createFileListPanel(Composite parent, final int selectionIndex){
        makeListComposite(parent,selectionIndex,true);
    }
    
    private void makeListComposite(Composite parent, final int selectionIndex, boolean browseFiles){
        
        Composite comp = new Composite(parent,0);
        comp.setLayout(new GridLayout(1,false));
        final FileListControl f = new FileListControl(comp, "Define dependents list", browseFiles?IOption.BROWSE_FILE:IOption.BROWSE_NONE);
        /* Enable workspace support for list editor */
        f.setWorkspaceSupport(true);
        panels[selectionIndex] = comp;
        f.addChangeListener(new IFileListChangeListener(){

			public void fileListChanged(FileListControl fileList, String[] oldValue, String[] newValue) {
				fireValueChanged();
				
			}});
        value[selectionIndex] = new IValue(){

            public String[] getValue () {
                return f.getItems();
            }

            public void setValue (String[] values) {
                if (values == null) values = new String[0];
                f.setList(values);
                
            }};
    }
    
    private void createFilePanel(Composite parent, final int selectionIndex){
        Composite comp = new Composite(parent,0);
        panels[selectionIndex] = comp;
        comp.setLayout(new GridLayout(3, false));
        Label label = new Label(comp, SWT.LEFT);
        label.setFont(parent.getFont());
        label.setText("Value: ");
        GridData gd = new GridData();
        gd.horizontalSpan = 1;      
        label.setLayoutData(gd);
        
        final Text valueField = new Text(comp, SWT.SINGLE | SWT.BORDER);
        valueField.setFont(parent.getFont());
        gd = new GridData(GridData.FILL_HORIZONTAL);
        valueField.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent e) {
				fireValueChanged();
				
			}});
        gd.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH + 100;
        gd.horizontalSpan = 1;
        valueField.setLayoutData(gd);
        value[selectionIndex] = new IValue(){

            public String[] getValue () {
                String s = valueField.getText();
                if (s != null && s.trim().length() > 0)
                    return new String[]{s.trim()};
                return new String[0];
            }

            public void setValue (String[] values) {
                valueField.setText(values.length > 0?values[0]:"");               
            }};

        final Button browse = new Button(comp,SWT.PUSH);
        browse.setFont(parent.getFont());
        browse.setText("Browse");
        browse.addSelectionListener(new SelectionAdapter(){
            @Override
            public void widgetSelected(SelectionEvent e){
                FileDialog fileDlg = new FileDialog(browse.getShell());
                String file = fileDlg.open();
                if(file != null) {
                    valueField.setText(file);
                    fireValueChanged();
                }
            }
        });      
    }

}
