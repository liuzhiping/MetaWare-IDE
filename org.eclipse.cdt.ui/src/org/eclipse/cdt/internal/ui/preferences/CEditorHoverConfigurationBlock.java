/*******************************************************************************
 * Copyright (c) 2002, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Anton Leherbauer (Wind River Systems)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.cdt.internal.ui.preferences.OverlayPreferenceStore.OverlayKey;
import org.eclipse.cdt.internal.ui.text.c.hover.CEditorTextHoverDescriptor;
import org.eclipse.cdt.internal.ui.util.SWTUtil;
import org.eclipse.cdt.internal.ui.util.TableLayoutComposite;

/**
 * CEditorHoverConfigurationBlock
 */
public class CEditorHoverConfigurationBlock implements IPreferenceConfigurationBlock {
	static final String DELIMITER= PreferencesMessages.CEditorHoverConfigurationBlock_delimiter; 

	private static final int ENABLED_PROP= 0;
	private static final int MODIFIER_PROP= 1;

	// Data structure to hold the values which are edited by the user
	private static class HoverConfig {
		
		String fModifierString;
		boolean fIsEnabled;
		int fStateMask;

		private HoverConfig(String modifier, int stateMask, boolean enabled) {
			fModifierString= modifier;
			fIsEnabled= enabled;
			fStateMask= stateMask;
		}
	}
	
	
	private class CEditorTextHoverDescriptorLabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		
		public String getColumnText(Object element, int columnIndex) {
			switch (columnIndex) {
			case ENABLED_PROP:
				return ((CEditorTextHoverDescriptor)element).getLabel();

			case MODIFIER_PROP:
				TableItem item= (TableItem)fHoverTableViewer.testFindItem(element);
				int index= fHoverTable.indexOf(item);
				return fHoverConfigs[index].fModifierString;

			default:
				break;
			}
			
			return null;
		}

		public void addListener(ILabelProviderListener listener) {
		}
		
		public void dispose() {
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}
	}
	
	
	private class CEditorTextHoverDescriptorContentProvider implements IStructuredContentProvider {
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// Do nothing since the viewer listens to resource deltas
		}
		
		public void dispose() {
		}
				
		public Object[] getElements(Object element) {
			return (Object[])element;
		}
	}

	
	OverlayPreferenceStore fStore;
	HoverConfig[] fHoverConfigs;
	Text fModifierEditor;
	Table fHoverTable;
	TableViewer fHoverTableViewer;
	private TableColumn fNameColumn;
	private TableColumn fModifierColumn;
	private Text fDescription;
	//private Button fShowHoverAffordanceCheckbox;
	private Button fShowEditorAnnotationCheckbox;
	
	private PreferencePage fMainPreferencePage;

	private StatusInfo fStatus;
	
	public CEditorHoverConfigurationBlock(PreferencePage mainPreferencePage, OverlayPreferenceStore store) {
		Assert.isNotNull(mainPreferencePage);
		Assert.isNotNull(store);
		fMainPreferencePage= mainPreferencePage;
		fStore= store;
		fStore.addKeys(createOverlayStoreKeys());
	}


	private OverlayPreferenceStore.OverlayKey[] createOverlayStoreKeys() {
		
		ArrayList<OverlayKey> overlayKeys= new ArrayList<OverlayKey>();
	
		//overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_ANNOTATION_ROLL_OVER));
		//overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_EVALUATE_TEMPORARY_PROBLEMS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, PreferenceConstants.EDITOR_TEXT_HOVER_MODIFIERS));
		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, PreferenceConstants.EDITOR_TEXT_HOVER_MODIFIER_MASKS));
		
		OverlayPreferenceStore.OverlayKey[] keys= new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
		overlayKeys.toArray(keys);
		return keys;
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.preferences.IPreferenceConfigurationBlock#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl(Composite parent) {

		Composite hoverComposite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		hoverComposite.setLayout(layout);
		GridData gd= new GridData(GridData.FILL_BOTH);
		hoverComposite.setLayoutData(gd);

		//String rollOverLabel= PreferencesMessages.getString("CEditorHoverConfigurationBlock.annotationRollover"); //$NON-NLS-1$
		//addCheckBox(hoverComposite, rollOverLabel, PreferenceConstants.EDITOR_ANNOTATION_ROLL_OVER, 0); //$NON-NLS-1$

		// Affordance checkbox
		//fShowHoverAffordanceCheckbox= new Button(hoverComposite, SWT.CHECK);
		//fShowHoverAffordanceCheckbox.setText(PreferencesMessages.getString("CEditorHoverConfigurationBlock.showAffordance")); //$NON-NLS-1$
		//fShowHoverAffordanceCheckbox.setLayoutData(gd);
		
		// Disable/enable editor problem annotaion checkbox
		fShowEditorAnnotationCheckbox = new Button(hoverComposite, SWT.CHECK);
		fShowEditorAnnotationCheckbox.setText(PreferencesMessages.CEditorPreferencePage_behaviourPage_EnableEditorProblemAnnotation); 
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= 0;
		gd.horizontalSpan= 2;
		fShowEditorAnnotationCheckbox.setLayoutData(gd);

		addFiller(hoverComposite);

		Label label= new Label(hoverComposite, SWT.NONE);
		label.setText(PreferencesMessages.CEditorHoverConfigurationBlock_hoverPreferences); 
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment= GridData.BEGINNING;
		gd.horizontalSpan= 2;
		label.setLayoutData(gd);

		TableLayoutComposite layouter= new TableLayoutComposite(hoverComposite, SWT.NONE);
		addColumnLayoutData(layouter);
		
		// Hover table
		fHoverTable= new Table(layouter, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.CHECK);
		fHoverTable.setHeaderVisible(true);
		fHoverTable.setLinesVisible(true);
		
		gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint= SWTUtil.getTableHeightHint(fHoverTable, 10);
		gd.horizontalSpan= 2;
		gd.widthHint= new PixelConverter(parent).convertWidthInCharsToPixels(30);
		layouter.setLayoutData(gd);

		fHoverTable.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				handleHoverListSelection();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		TableLayout tableLayout= new TableLayout();
		fHoverTable.setLayout(tableLayout);

		fNameColumn= new TableColumn(fHoverTable, SWT.NONE);
		fNameColumn.setText(PreferencesMessages.CEditorHoverConfigurationBlock_nameColumnTitle); 
		fNameColumn.setResizable(true);
		
		fModifierColumn= new TableColumn(fHoverTable, SWT.NONE);
		fModifierColumn.setText(PreferencesMessages.CEditorHoverConfigurationBlock_modifierColumnTitle); 
		fModifierColumn.setResizable(true);

		fHoverTableViewer= new CheckboxTableViewer(fHoverTable);
		fHoverTableViewer.setUseHashlookup(true);
		fHoverTableViewer.setContentProvider(new CEditorTextHoverDescriptorContentProvider());
		fHoverTableViewer.setLabelProvider(new CEditorTextHoverDescriptorLabelProvider());
		
		((CheckboxTableViewer)fHoverTableViewer).addCheckStateListener(new ICheckStateListener() {
			/*
			 * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
			 */
			public void checkStateChanged(CheckStateChangedEvent event) {
				String id= ((CEditorTextHoverDescriptor)event.getElement()).getId();
				if (id == null)
					return;
				CEditorTextHoverDescriptor[] descriptors= getContributedHovers();
				HoverConfig hoverConfig = null;
				int i= 0, length= fHoverConfigs.length;
				while (i < length) {
					if (id.equals(descriptors[i].getId())) {
						hoverConfig = fHoverConfigs[i];
						hoverConfig.fIsEnabled= event.getChecked();
						fModifierEditor.setEnabled(event.getChecked());
						break;
					}
					i++;
				}
				updateStatus(hoverConfig);
			}
		});
		
		// Text field for modifier string
		label= new Label(hoverComposite, SWT.LEFT);
		label.setText(PreferencesMessages.CEditorHoverConfigurationBlock_keyModifier); 
		fModifierEditor= new Text(hoverComposite, SWT.BORDER);
		gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		fModifierEditor.setLayoutData(gd);

		fModifierEditor.addKeyListener(new KeyListener() {
			private boolean isModifierCandidate;
			public void keyPressed(KeyEvent e) {
				isModifierCandidate= e.keyCode > 0 && e.character == 0 && e.stateMask == 0;
			}
		
			public void keyReleased(KeyEvent e) {
				if (isModifierCandidate && e.stateMask > 0 && e.stateMask == e.stateMask && e.character == 0) {// && e.time -time < 1000) {
					String text= fModifierEditor.getText();
					Point selection= fModifierEditor.getSelection();
					int i= selection.x - 1;
					while (i > -1 && Character.isWhitespace(text.charAt(i))) {
						i--;
					}
					boolean needsPrefixDelimiter= i > -1 && !String.valueOf(text.charAt(i)).equals(DELIMITER);

					i= selection.y;
					while (i < text.length() && Character.isWhitespace(text.charAt(i))) {
						i++;
					}
					boolean needsPostfixDelimiter= i < text.length() && !String.valueOf(text.charAt(i)).equals(DELIMITER);

					String insertString;

					if (needsPrefixDelimiter && needsPostfixDelimiter)
						insertString= NLS.bind(PreferencesMessages.CEditorHoverConfigurationBlock_insertDelimiterAndModifierAndDelimiter, new String[] {Action.findModifierString(e.stateMask)}); 
					else if (needsPrefixDelimiter)
						insertString= NLS.bind(PreferencesMessages.CEditorHoverConfigurationBlock_insertDelimiterAndModifier, new String[] {Action.findModifierString(e.stateMask)}); 
					else if (needsPostfixDelimiter)
						insertString= NLS.bind(PreferencesMessages.CEditorHoverConfigurationBlock_insertModifierAndDelimiter, new String[] {Action.findModifierString(e.stateMask)}); 
					else
						insertString= Action.findModifierString(e.stateMask);

					if (insertString != null)
						fModifierEditor.insert(insertString);
				}
			}
		});

		fModifierEditor.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleModifierModified();
			}
		});

		// Description
		Label descriptionLabel= new Label(hoverComposite, SWT.LEFT);
		descriptionLabel.setText(PreferencesMessages.CEditorHoverConfigurationBlock_description); 
		gd= new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		gd.horizontalSpan= 2;
		descriptionLabel.setLayoutData(gd);
		fDescription= new Text(hoverComposite, SWT.LEFT | SWT.WRAP | SWT.MULTI | SWT.READ_ONLY | SWT.BORDER);
		gd= new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan= 2;
		fDescription.setLayoutData(gd);
		
		initialize();

		Dialog.applyDialogFont(hoverComposite);
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(hoverComposite, ICHelpContextIds.C_EDITOR_HOVERS_PAGE);

		return hoverComposite;
	}
	
	private void addColumnLayoutData(TableLayoutComposite layouter) {
		layouter.addColumnData(new ColumnWeightData(40, true));
		layouter.addColumnData(new ColumnWeightData(60, true));
	}

	CEditorTextHoverDescriptor[] getContributedHovers() {
		return CUIPlugin.getDefault().getCEditorTextHoverDescriptors();
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.preferences.IPreferenceConfigurationBlock#initialize()
	 */
	public void initialize() {
		CEditorTextHoverDescriptor[] hoverDescs= getContributedHovers();
		fHoverConfigs= new HoverConfig[hoverDescs.length];
		for (int i= 0; i < hoverDescs.length; i++)
			fHoverConfigs[i]= new HoverConfig(hoverDescs[i].getModifierString(), hoverDescs[i].getStateMask(), hoverDescs[i].isEnabled());

		fHoverTableViewer.setInput(hoverDescs);
		
		initializeFields();
	}

	void initializeFields() {
		//fShowHoverAffordanceCheckbox.setSelection(fStore.getBoolean(PreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE));
		
		fShowEditorAnnotationCheckbox.setSelection(fStore.getBoolean(PreferenceConstants.EDITOR_EVALUATE_TEMPORARY_PROBLEMS));
		fModifierEditor.setEnabled(false);
		
		CEditorTextHoverDescriptor[] hoverDescs= getContributedHovers();
		for (int i= 0; i < hoverDescs.length; i++)
			fHoverTable.getItem(i).setChecked(hoverDescs[i].isEnabled());
		fHoverTableViewer.refresh();
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.preferences.IPreferenceConfigurationBlock#performOk()
	 */
	public void performOk() {
		StringBuffer buf= new StringBuffer();
		StringBuffer maskBuf= new StringBuffer();
		for (int i= 0; i < fHoverConfigs.length; i++) {
			buf.append(getContributedHovers()[i].getId());
			buf.append(CEditorTextHoverDescriptor.VALUE_SEPARATOR);
			if (!fHoverConfigs[i].fIsEnabled)
				buf.append(CEditorTextHoverDescriptor.DISABLED_TAG);
			String modifier= fHoverConfigs[i].fModifierString;
			if (modifier == null || modifier.length() == 0)
				modifier= CEditorTextHoverDescriptor.NO_MODIFIER;
			buf.append(modifier);
			buf.append(CEditorTextHoverDescriptor.VALUE_SEPARATOR);
			
			maskBuf.append(getContributedHovers()[i].getId());
			maskBuf.append(CEditorTextHoverDescriptor.VALUE_SEPARATOR);
			maskBuf.append(fHoverConfigs[i].fStateMask);
			maskBuf.append(CEditorTextHoverDescriptor.VALUE_SEPARATOR);
		}
		fStore.setValue(PreferenceConstants.EDITOR_TEXT_HOVER_MODIFIERS, buf.toString());
		fStore.setValue(PreferenceConstants.EDITOR_TEXT_HOVER_MODIFIER_MASKS, maskBuf.toString());
		
		//fStore.setValue(PreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE, fShowHoverAffordanceCheckbox.getSelection());
		fStore.setValue(PreferenceConstants.EDITOR_EVALUATE_TEMPORARY_PROBLEMS, fShowEditorAnnotationCheckbox.getSelection());
		CUIPlugin.getDefault().resetCEditorTextHoverDescriptors();
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.preferences.IPreferenceConfigurationBlock#performDefaults()
	 */
	public void performDefaults() {
		fStatus= new StatusInfo();
		restoreFromPreferences();
		initializeFields();
		updateStatus(null);
	}

	private void restoreFromPreferences() {

		//fShowHoverAffordanceCheckbox.setSelection(fStore.getBoolean(PreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE));

		String compiledTextHoverModifiers= fStore.getString(PreferenceConstants.EDITOR_TEXT_HOVER_MODIFIERS);
		
		StringTokenizer tokenizer= new StringTokenizer(compiledTextHoverModifiers, CEditorTextHoverDescriptor.VALUE_SEPARATOR);
		HashMap<String, String> idToModifier= new HashMap<String, String>(tokenizer.countTokens() / 2);

		while (tokenizer.hasMoreTokens()) {
			String id= tokenizer.nextToken();
			if (tokenizer.hasMoreTokens())
				idToModifier.put(id, tokenizer.nextToken());
		}

		String compiledTextHoverModifierMasks= CUIPlugin.getDefault().getPreferenceStore().getString(PreferenceConstants.EDITOR_TEXT_HOVER_MODIFIER_MASKS);

		tokenizer= new StringTokenizer(compiledTextHoverModifierMasks, CEditorTextHoverDescriptor.VALUE_SEPARATOR);
		HashMap<String, String> idToModifierMask= new HashMap<String, String>(tokenizer.countTokens() / 2);

		while (tokenizer.hasMoreTokens()) {
			String id= tokenizer.nextToken();
			if (tokenizer.hasMoreTokens())
				idToModifierMask.put(id, tokenizer.nextToken());
		}

		for (int i= 0; i < fHoverConfigs.length; i++) {
			String modifierString= idToModifier.get(getContributedHovers()[i].getId());
			boolean enabled= true;
			if (modifierString == null)
				modifierString= CEditorTextHoverDescriptor.DISABLED_TAG;
			
			if (modifierString.startsWith(CEditorTextHoverDescriptor.DISABLED_TAG)) {
				enabled= false;
				modifierString= modifierString.substring(1);
			}

			if (modifierString.equals(CEditorTextHoverDescriptor.NO_MODIFIER))
				modifierString= ""; //$NON-NLS-1$

			fHoverConfigs[i].fModifierString= modifierString;
			fHoverConfigs[i].fIsEnabled= enabled;
			fHoverConfigs[i].fStateMask= CEditorTextHoverDescriptor.computeStateMask(modifierString);

			if (fHoverConfigs[i].fStateMask == -1) {
				try {
					fHoverConfigs[i].fStateMask= Integer.parseInt(idToModifierMask.get(getContributedHovers()[i].getId()));
				} catch (NumberFormatException ex) {
					fHoverConfigs[i].fStateMask= -1;
				}
			}
		}
	}

	void handleModifierModified() {
		int i= fHoverTable.getSelectionIndex();
		String modifiers= fModifierEditor.getText();
		fHoverConfigs[i].fModifierString= modifiers;
		fHoverConfigs[i].fStateMask= CEditorTextHoverDescriptor.computeStateMask(modifiers);
		if (fHoverConfigs[i].fIsEnabled && fHoverConfigs[i].fStateMask == -1)
			fStatus= new StatusInfo(IStatus.ERROR, NLS.bind(PreferencesMessages.CEditorHoverConfigurationBlock_modifierIsNotValid, fHoverConfigs[i].fModifierString)); 
		else
			fStatus= new StatusInfo();
		
		// update table
		fHoverTableViewer.refresh(getContributedHovers()[i]);
		
		updateStatus(fHoverConfigs[i]);
	}

	void handleHoverListSelection() {	
		int i= fHoverTable.getSelectionIndex();
		
		if (i < 0) {
			if (fHoverTable.getSelectionCount() == 0)
				fModifierEditor.setEnabled(false);
			return;
		}
		
		boolean enabled= fHoverConfigs[i].fIsEnabled;
		fModifierEditor.setEnabled(enabled);
		fModifierEditor.setText(fHoverConfigs[i].fModifierString);
		String description= getContributedHovers()[i].getDescription();
		if (description == null)
			description= ""; //$NON-NLS-1$
		fDescription.setText(description);
	}

	IStatus getStatus() {
		if (fStatus == null)
			fStatus= new StatusInfo();
		return fStatus;
	}

	void updateStatus(HoverConfig hoverConfig) {
		if (hoverConfig != null && hoverConfig.fIsEnabled && hoverConfig.fStateMask == -1)
			fStatus= new StatusInfo(IStatus.ERROR, NLS.bind(PreferencesMessages.CEditorHoverConfigurationBlock_modifierIsNotValid, hoverConfig.fModifierString)); 
		else
			fStatus= new StatusInfo();

		int i= 0;
		HashMap<Integer, String> stateMasks= new HashMap<Integer, String>(fHoverConfigs.length);
		while (fStatus.isOK() && i < fHoverConfigs.length) {
			if (fHoverConfigs[i].fIsEnabled) {
				String label= getContributedHovers()[i].getLabel();
				Integer stateMask= new Integer(fHoverConfigs[i].fStateMask);
				if (fHoverConfigs[i].fStateMask == -1)
					fStatus= new StatusInfo(IStatus.ERROR, NLS.bind(PreferencesMessages.CEditorHoverConfigurationBlock_modifierIsNotValidForHover, new String[] {fHoverConfigs[i].fModifierString, label})); 
				else if (stateMasks.containsKey(stateMask))
					fStatus= new StatusInfo(IStatus.ERROR, NLS.bind(PreferencesMessages.CEditorHoverConfigurationBlock_duplicateModifier, new String[] {label, stateMasks.get(stateMask)})); 
				else
					stateMasks.put(stateMask, label);
			}
			i++;
		}

		fMainPreferencePage.setValid(fStatus.isOK());
		StatusUtil.applyToStatusLine(fMainPreferencePage, fStatus);
	}
	
	
	private void addFiller(Composite composite) {
		PixelConverter pixelConverter= new PixelConverter(composite);
		Label filler= new Label(composite, SWT.LEFT );
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		gd.heightHint= pixelConverter.convertHeightInCharsToPixels(1) / 2;
		filler.setLayoutData(gd);
	}
	
	/*
	 * @see DialogPage#dispose()
	 */
	public void dispose() {
		// nothing to dispose
	}

}
