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
package com.arc.cdt.importer.internal.ui;

import java.io.File;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.arc.cdt.importer.ImporterPlugin;
import com.arc.cdt.importer.core.ICodewrightProject;
import com.arc.cdt.importer.core.ICodewrightProjectSpace;


/**
 * The first Wizard page for the CodeWright project importer wizard.
 * It selects the project-space file to import.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
class SelectProjectSpaceWizardPage extends WizardPage {

    private static final String PAGE_NAME = "SelectProject";
    private File mPspFile = null; // selected file
    private FileDialog mFileDialog = null;
    private Text mPathField = null;
    private ICodewrightProject mLoneProject = null;
    private static final String FILE_DIALOG_ID = "selectProjectFileFilter";
    
    SelectProjectSpaceWizardPage() {
        super(PAGE_NAME);
        this.setTitle("Project-space file");
        this.setDescription("Select the project-space file to import");
    }

  
    public void createControl (Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setFont(parent.getFont());
        
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        layout.numColumns = 3;
        Label l = new Label(composite,SWT.HORIZONTAL);
        GridData gridData = new GridData();
        gridData.horizontalSpan = 3;
        l.setLayoutData(gridData);
        l.setText("Select project-space file (.psp):");  
        
        mPathField = new Text(composite,SWT.LEFT|SWT.SINGLE|SWT.BORDER);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 2;
        mPathField.setLayoutData(gridData);
        mPathField.addModifyListener(new ModifyListener(){

            public void modifyText (ModifyEvent e) {
                setPspFileAndValidate(mPathField.getText());
                
            }});
        
        Button browseButton = new Button(composite,SWT.PUSH);
        browseButton.setText("Browse...");
        gridData = new GridData();
        browseButton.setLayoutData(gridData);
        browseButton.addSelectionListener(new SelectionListener(){

            public void widgetSelected (SelectionEvent e) {
                showFileDialog();                
            }

            public void widgetDefaultSelected (SelectionEvent e) {
                widgetSelected(e);
                
            }});
        setControl(composite);
        setPageComplete(false);
    }
    
    private void setPspFileAndValidate (String text) {
        if (text.trim().length() == 0) {
            mPspFile = null;
            this.setErrorMessage("File not specified");
        }
        else {
            File f = new File(text);
            if (f.exists() && f.isFile()) {
                mPspFile = f;
                this.setErrorMessage(null);
            }
            else {
                mPspFile = null;
                this.setErrorMessage(f.isDirectory()?"Not a file":"Non-existent file");
            }
        }
        getAccessModel().setPspFile(mPspFile);
        mLoneProject = null;
        if (mPspFile != null){
            try {
                ICodewrightProjectSpace psp = getAccessModel().getProjectSpace();
                ICodewrightProject projects[] = psp.getProjects();
                if (projects.length == 0){
                    this.setErrorMessage("No projects in project space");
                    setPageComplete(false);
                }
                else {
                    if (projects.length == 1 && AccessModel.getProjectType(projects[0]) != null){
                        mLoneProject = projects[0];
                    }
                    setPageComplete(true);
                }
            }
            catch (Exception e) {
                this.setErrorMessage(e.getMessage());
            }
        }
        else
            setPageComplete(false);
    }
    
    private void showFileDialog(){
        if (mFileDialog == null){
            mFileDialog = new FileDialog(getShell(),SWT.OPEN);
            mFileDialog.setFilterExtensions(new String[]{"*.psp"});
            IPreferenceStore prefs = ImporterPlugin.getDefault().getPreferenceStore();
            String dir = prefs.getString(FILE_DIALOG_ID);
            if (dir != null) mFileDialog.setFilterPath(dir);
        }
        String fileName = mFileDialog.open();
        if (fileName != null){
            mPathField.setText(fileName);
            setPspFileAndValidate(fileName);
        }
        String filterPath = mFileDialog.getFilterPath();
        if (filterPath != null)
            ImporterPlugin.getDefault().getPreferenceStore().putValue(FILE_DIALOG_ID,filterPath);
    }

    @Override
    public IWizardPage getNextPage () {
        if (mLoneProject != null){
            getAccessModel().setSelectedProjects(new ICodewrightProject[]{mLoneProject});
            return getWizard().getPage(NewProjectCreatorPage.PAGE_NAME);
        }
        else return super.getNextPage();
    }
    
    private AccessModel getAccessModel(){
        return ((CodeWrightImportWizard)getWizard()).getAccessModel();
    }
}
