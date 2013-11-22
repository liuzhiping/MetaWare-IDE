/**
 * 
 */
package com.arc.cdt.toolchain.ui.bcf;

import java.io.File;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.arc.cdt.toolchain.internal.ui.bcf.BcfUtils;
import com.arc.cdt.toolchain.internal.ui.bcf.properties.metaware.MetaWareOptionUpdater;

/**
 * Creates the panel for choosing a BCF property file.
 * 
 * @author thuymain
 * 
 */
public class TcfFileBrowser extends FileFieldEditor{
	
	private Composite mControl;
	private Shell mTopParent;
	private ILaunchConfiguration  mLauchConfiguration;
	private IGuihiliRefresh mGuihiliRefresh;
	private Button mSynchronizeBtn;
	public TcfFileBrowser(Composite parent, Composite topParent, IGuihiliRefresh guiRefresh) {

		super("", "Import TCF File", parent);
		mControl = parent;
		mTopParent = topParent.getShell();
		mGuihiliRefresh = guiRefresh;
		
		StringBuilder buf = new StringBuilder();
		for (String s: BcfUtils.EXTENSIONS){
			if (buf.length() > 0) buf.append(';');
			buf.append(s);
		}
		this.setFileExtensions(new String[]{buf.toString(), "*.*"});
		
	}


	public void setLaunchConfiguration (ILaunchConfiguration config){
		mLauchConfiguration = config;
		try{
		  String bcfFilePath = mLauchConfiguration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_NOPROJECT_IMPORT_BCF_FILE, (String)null);
		  // calling parent setString value as to prevent update config at this time
		  super.setStringValue(bcfFilePath);
		}catch ( CoreException ex){
			BcfUtils.displayBCFError(ex, mControl);
		}
		
	}


	public void updateLaunchConfiguration() {
		
		//add busy cursor
		Cursor busyCursor = new Cursor(mTopParent.getDisplay(), SWT.CURSOR_WAIT);
        Cursor savedCursor = mControl.getCursor();
        mTopParent.setCursor(busyCursor);
        
		String bcfFilePath = this.getStringValue();

		if (bcfFilePath != null && bcfFilePath.length() > 0) {

			//save to Launch Config 

			File file = new File(bcfFilePath);
			String bcfPath = file.getAbsolutePath();
			if (bcfPath != null) {
				SettingsFileContent settings;
				try {
					settings = SettingsFileContent.read(file,null);
					MetaWareOptionUpdater updater = new MetaWareOptionUpdater (settings);
					ILaunchConfigurationWorkingCopy working = mLauchConfiguration.getWorkingCopy();
					working.setAttribute(ICDTLaunchConfigurationConstants.ATTR_NOPROJECT_IMPORT_BCF_FILE, bcfFilePath);
					//SettingsFileContents
					if (updater.updateLaunchConfiguration(working) && updater.updateCompileOptionForLaunchConfiguration(working)){
        				working.doSave();
						mGuihiliRefresh.refreshGuihiliGUI();

					}


				} catch (Exception ex) {
					BcfUtils.displayBCFError(ex, mControl);
				}
			}
		}
		mTopParent.setCursor(savedCursor);

	}

	public void setEnabled( boolean value){
		super.setEnabled(value, mControl);
		getTextControl(mControl).setEnabled(value);
		getLabelControl(mControl).setEnabled(value);
		mSynchronizeBtn.setEnabled(value);
	}

	public void setVisible (boolean value){
		mControl.setVisible(value);
	}

	@Override
	public void setStringValue(String value) {

		if (value != null) {
			if (!value.equals(getStringValue())) {
				super.setStringValue(value);
				updateLaunchConfiguration();
			}
		} else if (getStringValue() != null && getStringValue().length() > 0) {
			super.setStringValue(value);
			updateLaunchConfiguration();
		}

	}
	
	public int getNumberOfControls() {
		return 4;
	}

	protected void doFillIntoGrid(Composite parent, int numColumns) {
		super.doFillIntoGrid(parent, numColumns-1);

		Button btn = getChangeControl(parent);
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;

		btn.setLayoutData(gd);

		mSynchronizeBtn = new Button(parent, SWT.PUSH);
		mSynchronizeBtn.addSelectionListener(new SelectionListener(){

            @Override
            public void widgetDefaultSelected (SelectionEvent e) {           
            }

            @Override
            public void widgetSelected (SelectionEvent e) {
            	updateLaunchConfiguration();
                
            }});
		gd = new GridData();

		gd.horizontalAlignment = GridData.FILL_HORIZONTAL;
		mSynchronizeBtn.setText("Synchronize");
		mSynchronizeBtn.setLayoutData(gd);
	}

	
}


