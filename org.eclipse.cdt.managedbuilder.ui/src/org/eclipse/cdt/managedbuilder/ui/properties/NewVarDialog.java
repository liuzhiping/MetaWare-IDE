/*******************************************************************************
 * Copyright (c) 2005, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import com.ibm.icu.text.Collator;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICMultiItemsHolder;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.ui.newui.UIMessages;
import org.eclipse.cdt.utils.cdtvariables.CdtVariableResolver;
import org.eclipse.cdt.utils.ui.controls.FileListControl;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * the dialog used to create or edit the build macro
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class NewVarDialog extends Dialog {
	// String constants
	private static final String PREFIX = "NewBuildMacroDialog";	//$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$
	private static final String VALUE = LABEL + ".value";	//$NON-NLS-1$

	private static final String TYPE = LABEL + ".type";	//$NON-NLS-1$
	private static final String TYPE_TEXT = TYPE + ".text";	//$NON-NLS-1$
	private static final String TYPE_TEXT_LIST = TYPE + ".text.list";	//$NON-NLS-1$
	private static final String TYPE_PATH_FILE = TYPE + ".path.file";	//$NON-NLS-1$
	private static final String TYPE_PATH_FILE_LIST = TYPE + ".path.file.list";	//$NON-NLS-1$
	private static final String TYPE_PATH_DIR = TYPE + ".path.dir";	//$NON-NLS-1$
	private static final String TYPE_PATH_DIR_LIST = TYPE + ".path.dir.list";	//$NON-NLS-1$
	private static final String TYPE_PATH_ANY = TYPE + ".path.any";	//$NON-NLS-1$
	private static final String TYPE_PATH_ANY_LIST = TYPE + ".path.any.list";	//$NON-NLS-1$
	
	private static final String BROWSE = LABEL + ".browse";	//$NON-NLS-1$
	private static final String LIST_TITLE = LABEL + ".list.title";	//$NON-NLS-1$
	private static final String TITLE_NEW = LABEL + ".title.new"; 	//$NON-NLS-1$
	private static final String TITLE_EDIT = LABEL + ".title.edit"; 	//$NON-NLS-1$
	private static final String EMPTY_STRING = new String();

	// The title of the dialog.
	private String fTitle;
	// hold the macro being edited(in the case of the "edit" mode)
	private ICdtVariable fEditedMacro;
	//the resulting macro. Can be accessed only when the dialog is closed
	private ICdtVariable fResultingMacro;
	
	private boolean fTotalSizeCalculated;

	private String fTypedName;
	private int fTypedType = -1;
	public boolean isForAllCfgs = false;

	// Widgets
	private Composite fContainer;
	private CCombo fMacroNameEdit;
	private Label fMacroValueLabel;
	private Text fMacroValueEdit;
	private Button fBrowseButton;
	private Combo fTypeSelector;
	private Composite fListEditorContainier;
	
	private FileListControl fListEditor;
	private ICConfigurationDescription cfgd;
	private ICdtVariable[] vars; 

	public NewVarDialog(Shell parentShell, ICdtVariable editedMacro, ICConfigurationDescription _cfgd, ICdtVariable[] _vars) {
		super(parentShell);
		cfgd = _cfgd;
		if(editedMacro != null)
			fTitle = UIMessages.getString(TITLE_EDIT);
		else
			fTitle = UIMessages.getString(TITLE_NEW);
		fEditedMacro = editedMacro;
		vars = _vars;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (fTitle != null)
			shell.setText(fTitle);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = new Composite(parent, SWT.NULL);
		comp.setFont(parent.getFont());
		comp.setLayout(new GridLayout(3, false));
		GridData gd = new GridData(GridData.FILL_BOTH);
		comp.setLayoutData(gd);

		fContainer = comp;

		Label nameLabel = new Label(comp, SWT.LEFT);
		nameLabel.setFont(comp.getFont());
		nameLabel.setText(Messages.getString("NewVarDialog.0")); //$NON-NLS-1$
		nameLabel.setLayoutData(new GridData());
		
		fMacroNameEdit = new CCombo(comp, SWT.BORDER);
		fMacroNameEdit.setItems(getMacroNames());
		fMacroNameEdit.setFont(comp.getFont());
		gd = new GridData();	// don't use GridData.FILL_HORIZONTAL; see bugzilla 229793
		gd.horizontalSpan = 2;
		gd.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH + 50;
		fMacroNameEdit.setLayoutData(gd);
		fMacroNameEdit.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleMacroNameModified();
			}
		});
		fMacroNameEdit.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e){
				handleMacroNameSelection();
			}
		});

		if (fEditedMacro == null && cfgd != null && !(cfgd instanceof ICMultiItemsHolder)) {
			Button c_all = new Button(comp, SWT.CHECK);
			c_all.setText(Messages.getString("NewVarDialog.1")); //$NON-NLS-1$
			gd = new GridData(GridData.BEGINNING);
			gd.horizontalSpan = 3;
			c_all.setLayoutData(gd);
			c_all.addSelectionListener(new SelectionAdapter(){
				@Override
				public void widgetSelected(SelectionEvent e){
					isForAllCfgs = ((Button)e.widget).getSelection();
				}
			});
		}
		
		Label typeLabel = new Label(comp, SWT.LEFT);
		typeLabel.setFont(comp.getFont());
		typeLabel.setText(UIMessages.getString(TYPE));
		gd = new GridData();
		typeLabel.setLayoutData(gd);
		
		fTypeSelector = new Combo(comp, SWT.READ_ONLY | SWT.DROP_DOWN | SWT.BORDER);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalSpan = 2;
//		gd.widthHint = 100;
		fTypeSelector.setLayoutData(gd);
		fTypeSelector.setItems(new String[]{
				UIMessages.getString(TYPE_TEXT),
				UIMessages.getString(TYPE_TEXT_LIST),
				UIMessages.getString(TYPE_PATH_FILE),
				UIMessages.getString(TYPE_PATH_FILE_LIST),
				UIMessages.getString(TYPE_PATH_DIR),
				UIMessages.getString(TYPE_PATH_DIR_LIST),
				UIMessages.getString(TYPE_PATH_ANY),
				UIMessages.getString(TYPE_PATH_ANY_LIST)
		});
		setSelectedType(IBuildMacro.VALUE_TEXT);

		fTypeSelector.addListener(SWT.Selection, new Listener () {
			public void handleEvent(Event e) {
				handleTypeModified();
			}
		});

		fMacroValueLabel = new Label(comp, SWT.LEFT);
		fMacroValueLabel.setFont(comp.getFont());
		fMacroValueLabel.setText(UIMessages.getString(VALUE));
		gd = new GridData();
		gd.horizontalSpan = 1;		
		fMacroValueLabel.setLayoutData(gd);
		
		fMacroValueEdit = new Text(comp, SWT.SINGLE | SWT.BORDER);
		fMacroValueEdit.setFont(comp.getFont());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH + 100;
		gd.horizontalSpan = 1;
		fMacroValueEdit.setLayoutData(gd);
		fMacroValueEdit.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleMacroValueModified();
			}
		});

		fBrowseButton = new Button(comp,SWT.PUSH);
		fBrowseButton.setFont(comp.getFont());
		fBrowseButton.setText(UIMessages.getString(BROWSE));
		fBrowseButton.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e){
				handleBrowseButtonPressed();
			}
		});
		
		gd = new GridData();
		gd.widthHint = IDialogConstants.BUTTON_WIDTH;
		gd.horizontalSpan = 1;
		fBrowseButton.setLayoutData(gd);
		
		fListEditorContainier = new Composite(comp,0);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 3;
		fListEditorContainier.setLayoutData(gd);
		fListEditorContainier.setLayout(new GridLayout());

		fListEditor = new FileListControl(fListEditorContainier, UIMessages.getString(LIST_TITLE), IOption.BROWSE_NONE);
		/* Enable workspace support for list editor */
		fListEditor.setWorkspaceSupport(true);

		// TODO: getContextInfo
		// fListEditor.setContext(fMacrosBlock.getContextInfo());

		if(fEditedMacro != null){
			loadVar(fEditedMacro);
			fMacroNameEdit.setEnabled(false);
		}
		return comp;
	}
	
	/*
	 * get the names to be displayed in the var Name combo.
	 */
	private String[] getMacroNames(){
		String names[] = null;
		if(vars == null || vars.length == 0)
			names = new String[0];
		else{
			names = new String[vars.length];
			for(int i = 0; i < vars.length; i++)
				names[i] = vars[i].getName();
			final Collator collator = Collator.getInstance();
			Arrays.sort(names, new Comparator<String>() {
	            public int compare(final String a, final String b) {
					final String strA = a.toUpperCase();
					final String strB = b.toUpperCase();
					return collator.compare(strA,strB);
	            }
	        });
		}
		return names;
	}

	private void handleBrowseButtonPressed(){
		switch(getBrowseType(getSelectedType())){
		case IOption.BROWSE_FILE:
			FileDialog fileDlg = new FileDialog(fBrowseButton.getShell());
			String file = fileDlg.open();
			if(file != null)
				fMacroValueEdit.setText(file);
			break;
		case IOption.BROWSE_DIR:
			DirectoryDialog dirDlg = new DirectoryDialog(fBrowseButton.getShell());
			String dir = dirDlg.open();
			if(dir != null)
				fMacroValueEdit.setText(dir);
			break;
		}
	}
	
	private int getBrowseType(int type){
		int browseType = IOption.BROWSE_NONE;
		switch(type){
		case IBuildMacro.VALUE_PATH_FILE:
		case IBuildMacro.VALUE_PATH_FILE_LIST:
			browseType = IOption.BROWSE_FILE;
			break;
		case IBuildMacro.VALUE_PATH_DIR:
		case IBuildMacro.VALUE_PATH_DIR_LIST:
			browseType = IOption.BROWSE_DIR;
			break;
		case IBuildMacro.VALUE_PATH_ANY:
		case IBuildMacro.VALUE_PATH_ANY_LIST:
			break;
		case IBuildMacro.VALUE_TEXT:
		case IBuildMacro.VALUE_TEXT_LIST:
		default:
		}
		return browseType;

	}

	/*
	 * called when the variable name is selected, loads all the dialog fields with the variable settings
	 */
	private void handleMacroNameSelection(){
		int index = fMacroNameEdit.getSelectionIndex();
		if (index != -1) 
			loadVarSettings(fMacroNameEdit.getItem(index));
	}
	
	private void loadVarSettings(String name) {
		ICdtVariable v = null;
		for (int i=0; i<vars.length; i++) {
			if (vars[i].getName().equals(name)) { 
				v = vars[i]; 
				break;
			}
		}
		if(v != null)
			loadVar(v);
		else
			loadVar(name,IBuildMacro.VALUE_TEXT,EMPTY_STRING);
	}
	
	private void loadVar(String name, int type, String value[]){
		setSelectedType(type);
		setSelectedMacroName(notNull(name));
		fListEditor.setList(value);
		updateWidgetState();
	}
	
	private void loadVar(String name,	int type, String value){
		setSelectedType(type);
		setSelectedMacroName(notNull(name));
		fMacroValueEdit.setText(notNull(value));
		updateWidgetState();
	}
	
	/*
	 * loads all the dialog fields with the variable settings
	 */
	private void loadVar(ICdtVariable var){
		try{
			if(CdtVariableResolver.isStringListVariable(var.getValueType()))
				loadVar(var.getName(),var.getValueType(),var.getStringListValue());
			else
				loadVar(var.getName(),var.getValueType(),var.getStringValue());
		}catch(CdtVariableException e){}
	}

	/*
	 * returns an empty string in the case the string passed is null.
	 * otherwise returns the string passed
	 */
	private String notNull(String str){
		return str == null ? EMPTY_STRING : str;
	}
	
	/*
	 * returns the name typed in the dialog var name edit triming spaces  
	 */
	private String getSelectedVarName(){
		return fMacroNameEdit.getText().trim();
	}
	
	/*
	 * sets the variable name to the dialog "variable name" edit control
	 */
	private void setSelectedMacroName(String name){
		if(!macroNamesEqual(fMacroNameEdit.getText(),name)){
			fTypedName = name;
			fMacroNameEdit.setText(notNull(name).trim());
		}
	}
	
	private boolean macroNamesEqual(String name1, String name2){
		name1 = name1.trim();
		name2 = name2.trim();
		return name1.equalsIgnoreCase(name2);
	}

	/*
	 * returns the selected type
	 */
	private int getSelectedType(){
		switch(fTypeSelector.getSelectionIndex()){
			case 1:
				return IBuildMacro.VALUE_TEXT_LIST;
			case 2:
				return IBuildMacro.VALUE_PATH_FILE;
			case 3:
				return IBuildMacro.VALUE_PATH_FILE_LIST;
			case 4:
				return IBuildMacro.VALUE_PATH_DIR;
			case 5:
				return IBuildMacro.VALUE_PATH_DIR_LIST;
			case 6:
				return IBuildMacro.VALUE_PATH_ANY;
			case 7:
				return IBuildMacro.VALUE_PATH_ANY_LIST;
			case 0:
			default:
				return IBuildMacro.VALUE_TEXT;
		}
	}
	
	/*
	 * sets the selected type
	 */
	private void setSelectedType(int type){
		switch(type){
		case IBuildMacro.VALUE_TEXT_LIST:
			fTypeSelector.select(1);
			break;
		case IBuildMacro.VALUE_PATH_FILE:
			fTypeSelector.select(2);
			break;
		case IBuildMacro.VALUE_PATH_FILE_LIST:
			fTypeSelector.select(3);
			break;
		case IBuildMacro.VALUE_PATH_DIR:
			fTypeSelector.select(4);
			break;
		case IBuildMacro.VALUE_PATH_DIR_LIST:
			fTypeSelector.select(5);
			break;
		case IBuildMacro.VALUE_PATH_ANY:
			fTypeSelector.select(6);
			break;
		case IBuildMacro.VALUE_PATH_ANY_LIST:
			fTypeSelector.select(7);
			break;
		case IBuildMacro.VALUE_TEXT:
		default:
			fTypeSelector.select(0);
			break;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed(){
		String name = getSelectedVarName();
		if(name != null || !EMPTY_STRING.equals(name)){
			int type = getSelectedType();
			if(CdtVariableResolver.isStringListVariable(type))
				fResultingMacro = new BuildMacro(name,type,getSelectedStringListValue());
			else
				fResultingMacro = new BuildMacro(name,type,getSelectedStringValue());
		}

		super.okPressed();
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#open()
	 */
	@Override
	public int open(){
		fResultingMacro = null;
		return super.open();
	}
	
	/*
	 * returns the macro value that should be stored in the resulting variable
	 */
	private String getSelectedStringValue(){
		return fMacroValueEdit.getText();
	}

	private String[] getSelectedStringListValue(){
		return fListEditor.getItems();
	}

	/*
	 * this method should be called after the dialog is closed
	 * to obtain the created variable.
	 * if the variable was not created, e.g. because a user has pressed 
	 * the cancel button this method returns null
	 */
	public ICdtVariable getDefinedMacro(){
		return fResultingMacro;
	}
	
	/*
	 * called when the variable name is modified
	 */
	private void handleMacroNameModified(){
		String name = getSelectedVarName();
		if(fTypedName == null || !fTypedName.equals(name)){
			loadVarSettings(name);
		}
	}
	
	private void handleMacroValueModified(){}
	
	/*
	 * called when the operation is modified
	 */
	private void handleTypeModified(){
		int type = getSelectedType();
		if(fTypedType != -1 && fTypedType == type)
			return;
	
		fTypedType = type;

		adjustLayout(type);
	}
	
	private void adjustLayout(int type){
		GridData listGd = (GridData)fListEditorContainier.getLayoutData();
		GridData labelGd = (GridData)fMacroValueLabel.getLayoutData();
		GridData editGd = (GridData)fMacroValueEdit.getLayoutData();
		GridData browseGd = (GridData)fBrowseButton.getLayoutData();

		if(CdtVariableResolver.isStringListVariable(type)){
			listGd.exclude = false;
			labelGd.exclude = true;
			editGd.exclude = true;
			browseGd.exclude = true;
			fListEditorContainier.setVisible(true);
			fListEditor.setType(getBrowseType(type));
			fMacroValueLabel.setVisible(false);
			fMacroValueEdit.setVisible(false);
			fBrowseButton.setVisible(false);
		} else 
		{
			listGd.exclude = true;
			labelGd.exclude = false;
			editGd.exclude = false;
			int editSpan;
			fListEditorContainier.setVisible(false);
			fMacroValueLabel.setVisible(true);
			fMacroValueEdit.setVisible(true);
			if(getBrowseType(type) != IOption.BROWSE_NONE){
				browseGd.exclude = false;
				editSpan = 1;
				fBrowseButton.setVisible(true);
			} else {
				browseGd.exclude = true;
				editSpan = 2;
				fBrowseButton.setVisible(false);
			}
			editGd.horizontalSpan = editSpan;
		}
		fContainer.layout(true,true);
	}

	
	/*
	 * updates the state of the dialog controls
	 */
	private void updateWidgetState(){
		if(!fTotalSizeCalculated)
			return;
		handleTypeModified();

		Button b = getButton(IDialogConstants.OK_ID);
		if (b != null) {
			String name = getSelectedVarName();
			b.setEnabled(!EMPTY_STRING.equals(name));
		}
	}
	
	@Override
	protected Point getInitialSize() {
		Point size = super.getInitialSize();
		fTotalSizeCalculated = true;
		updateWidgetState();
		return size;
	}
}
