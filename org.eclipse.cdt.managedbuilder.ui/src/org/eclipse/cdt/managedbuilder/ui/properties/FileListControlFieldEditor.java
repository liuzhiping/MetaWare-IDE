/*******************************************************************************
 * Copyright (c) 2004, 2010 BitMethods Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * BitMethods Inc - Initial API and implementation
 * ARM Ltd. - basic tooltip support
 * Miwako Tokugawa (Intel Corporation) - Fixed-location tooltip support
 * Synopsys
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;


import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.internal.macros.OptionContextData;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.utils.ui.controls.FileListControl;
import org.eclipse.cdt.utils.ui.controls.IFileListChangeListener;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.PlatformUI;

/**
 * Field editor that uses FileListControl for user input.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class FileListControlFieldEditor extends FieldEditor {

	// file list control
	private FileListControl list;
	private int browseType;
	private Composite topLayout;
	private static final String DEFAULT_SEPARATOR = ";"; //$NON-NLS-1$

	//values
//	private String[] values = null;

	/**
	 * Creates a file list control field editor.
	 * @param name the name of the preference this field editor works on
	 * @param labelText the label text of the field editor
	 * @param parent the parent of the field editor's control
	 * @param type the browseType of the file list control
	 */
	public FileListControlFieldEditor(
		String name,
		String labelText,
		Composite parent,
		int type) {
		super(name, labelText, parent);
		browseType = type;
		// Set the browse strategy for the list editor
		list.setType(type);
	}

	/**
	 * Creates a file list control field editor.
	 * @param name the name of the preference this field editor works on
	 * @param labelText the label text of the field editor
	 * @param tooltip the tooltip text of the field editor
	 * @param contextId 
	 * @param parent the parent of the field editor's control
	 * @param type the browseType of the file list control
	 */
	public FileListControlFieldEditor(
		String name,
		String labelText,
		String tooltip,
		String contextId,
		Composite parent,
		int type) {
		this(name, labelText, parent, type);
		// can't use setToolTip(tooltip) as label not created yet 
		getLabelControl(parent).setToolTipText(tooltip);
		if (!contextId.equals("")) PlatformUI.getWorkbench().getHelpSystem().setHelp(list.getListControl(), contextId);	 //$NON-NLS-1$
	}
	
	/**
	 * Set the configuration description we want to prompt for CDT Build variable.
	 * @param config the configuration description from which CDT build variables are extracted.
	 * <P>
	 * CUSTOMIZATION
	 */
	public void setConfigurationDescription(ICConfigurationDescription config){
	    list.setConfigurationDescription(config);
	}

	/**
	 * Sets the field editor's tool tip text to the argument, which
	 * may be null indicating that no tool tip text should be shown.
	 *
	 * @param tooltip the new tool tip text (or null)
	 *
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the field editor has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the field editor</li>
	 * </ul>
	 */
	public void setToolTip(String tooltip) {
		// Currently just the label has the tooltip
		getLabelControl().setToolTipText(tooltip);
	}
	
	/**
	 * Returns the field editor's tool tip text, or null if it has
	 * not been set.
	 *
	 * @return the field editor's tool tip text
	 *
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the field editor has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the field editor</li>
	 * </ul>
	 */
	public String getToolTipText() {
		return getLabelControl().getToolTipText();
	}

	/**
	 * Creates a file list control field editor.
	 * @param name the name of the preference this field editor works on
	 * @param labelText the label text of the field editor
	 * @param parent the parent of the field editor's control
	 * @param value the field editor's value
	 * @param type the browseType of the file list control
	 */
	public FileListControlFieldEditor(
		String name,
		String labelText,
		Composite parent,
		String value,
		int type) {
		this(name, labelText, parent, type);
		browseType = type;
//		this.values = parseString(value);
	}

	/**
	 * Sets the filter-path for the underlying Browse dialog. Only applies when browseType is 'file' or 'dir'.
	 * @param filterPath
	 * 
	 * @since 7.0
	 */
	public void setFilterPath(String filterPath) {
		list.setFilterPath(filterPath);
	}
	
	/**
	 * Sets the filter-extensions for the underlying Browse dialog. Only applies when browseType is 'file'.
	 * @param filterExtensions
	 * 
	 * @since 7.0
	 */
	public void setFilterExtensions(String[] filterExtensions) {
		list.setFilterExtensions(filterExtensions);
	}
	
	/**
	 * Fills this field editor's basic controls into the given parent.
	 */
	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		topLayout = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.makeColumnsEqualWidth = false;
		topLayout.setLayout(layout);
		GridData gddata = new GridData(GridData.FILL_BOTH);
		gddata.horizontalSpan = 2;
		topLayout.setLayoutData(gddata);
		// file list control
		list = new FileListControl(topLayout, getLabelText(), getType(), false);
		list.addChangeListener(new IFileListChangeListener(){

			public void fileListChanged(FileListControl fileList, String oldValue[], String newValue[]) {
				handleFileListChange(fileList,oldValue,newValue);
			}
			
		});
		topLayout.setLayout(layout);
	}
	
	private void handleFileListChange(FileListControl fileList, String oldValue[], String newValue[]){
//		values = fileList.getItems();
		fireValueChanged(
				VALUE,
				createList(oldValue),
				createList(newValue));
	}

	/**
	 * Returns the browseType of this field editor's file list control
	 * @return
	 */
	private int getType() {
		return browseType;
	}

	/**
	 * @return the file list control 
	 */
	protected List getListControl() {
		return list.getListControl();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doLoad()
	 */
	@Override
	protected void doLoad() {
		if (list != null) {
			IPreferenceStore store = getPreferenceStore(); 
			if (store != null) {
				String s = store.getString(getPreferenceName());
				String[] array = parseString(s);
				list.setList(array);
				list.setSelection(0);
				// Set the resource the editor works for
				if (store instanceof ToolSettingsPrefStore) {
					IConfiguration config = ((ToolSettingsPrefStore)store).getSelectedConfig();
					if (config != null) {
						IResource project = config.getOwner();
						if (project != null) {
							/* Enable workspace support for list and set project */
							list.setWorkspaceSupport(true);
							if (store instanceof ToolSettingsPrefStore){
								ToolSettingsPrefStore btsStore = ((ToolSettingsPrefStore)store);
								Object[] option = btsStore.getOption(getPreferenceName());
								if(option != null){
									list.setContext(
											btsStore.obtainMacroProvider().getMacroContextInfo(
													IBuildMacroProvider.CONTEXT_OPTION,
													new OptionContextData((IOption)option[1],
															(IHoldsOptions)option[0])));
								}
							}
						}
					}
				}
			}
		}
		list.selectionChanged();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
	 */
	@Override
	protected void doLoadDefault() {
		if (list != null) {
			list.removeAll();
			String s =
				getPreferenceStore().getDefaultString(getPreferenceName());
			String[] array = parseString(s);
			list.setList(array);
			list.selectionChanged();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doStore()
	 */
	@Override
	protected void doStore() {
		String s = createList(list.getItems());
		if (s != null)
			getPreferenceStore().setValue(getPreferenceName(), s);
	}
	
	public String[] getStringListValue(){
		return list.getItems();
	}

	/**
	* Returns the number of basic controls this field editor consists of.
	*
	* @return the number of controls
	*/
	@Override
	public int getNumberOfControls() {
		return 1;
	}

	/**
	 * Answers a <code>String</code> containing the strings passed in the 
	 * argument separated by the DEFAULT_SEPERATOR
	 * 
	 * @param items An array of strings
	 * @return 
	 */
	protected String createList(String[] items) {
		StringBuffer path = new StringBuffer(""); //$NON-NLS-1$

		for (int i = 0; i < items.length; i++) {
			path.append(items[i]);
			if (i < (items.length - 1)) {
				path.append(DEFAULT_SEPARATOR);
			}
		}
		return path.toString();
	}

	/**
	 * Parse the string with the separator and returns the string array. 
	 * @param stringList
	 * @return
	 */
	private String[] parseString(String stringList) {
		StringTokenizer tokenizer =
			new StringTokenizer(stringList, DEFAULT_SEPARATOR);
		ArrayList<String> list = new ArrayList<String>();
		while (tokenizer.hasMoreElements()) {
			list.add((String)tokenizer.nextElement());
		}
		return list.toArray(new String[list.size()]);
	}

	/**
	 * Set style
	 */
	public void setStyle() {
		((GridLayout)topLayout.getLayout()).marginWidth = 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
	 */
	@Override
	protected void adjustForNumColumns(int numColumns) {
		((GridData)topLayout.getLayoutData()).horizontalSpan = numColumns;
	}
	
    @Override
	public Label getLabelControl(Composite parent) {
    	return list.getLabelControl();
    }
    
    @Override
	public void setEnabled(boolean enabled, Composite parent) {
    	list.setEnabled(enabled);
    }

}
