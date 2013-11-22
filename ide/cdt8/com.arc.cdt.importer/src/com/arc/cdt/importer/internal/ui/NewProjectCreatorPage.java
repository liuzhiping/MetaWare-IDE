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

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.arc.cdt.importer.ImporterPlugin;
import com.arc.cdt.importer.core.ICodewrightProject;
import com.arc.cdt.importer.core.ICodewrightProjectSpace;


/**
 * The wizard page that decides how the new project will be created.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp </a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class NewProjectCreatorPage extends WizardPage {

    static final String PAGE_NAME = "NewProject";

    private File mOriginalDir = null;

    private Button mInPlaceCheckBox;

    private Button mCreateInWorkspaceCheckBox;

    private Button mCreateInNewPlaceCheckBox;

    private Group copyOrLinkGroup;

    private Button mCopySourceCheckBox;

    private Button mLinkSourceCheckBox;

    private Text mNewProjectLocation;

    private Text mOriginalLocation;

    private static final int IN_WORKSPACE = 0;

    private static final int IN_PLACE = 1;

    private static final int IN_FILE_SYSTEM = 2;

    private int mHowCreated = IN_WORKSPACE;

    private Button mBrowse;

    private DirectoryDialog mDirectoryDialog = null;

    private Text mNameField;

    private ICodewrightProjectSpace mPsp = null;
    
    private String mBadProjectNameError = null;
    private String mBadTargetLocationError = null;

    private static final String LOCATION_DIR_FILTER_ID = "locationDirFilter";

    /**
     */
    public NewProjectCreatorPage() {
        super(PAGE_NAME, "Project selection from original project space", null);
        this.setDescription("Choose location of new project");
    }

    public void createControl (Composite parent) {
        SelectionListener selectionListener = new SelectionListener() {

            public void widgetSelected (SelectionEvent e) {
                handleButtonSelection(e);
            }

            public void widgetDefaultSelected (SelectionEvent e) {
                widgetSelected(e);
            }
        };
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new FormLayout());
        FormData fd = new FormData();
        fd.top = new FormAttachment(0, 5);
        fd.left = new FormAttachment(0, 5);

        Label label = new Label(composite, SWT.LEFT);
        label.setText("Project name:");
        label.setLayoutData(fd);

        mNameField = new Text(composite, SWT.BORDER);
        fd = new FormData();
        fd.left = new FormAttachment(label, 5);
        fd.right = new FormAttachment(100, -5);
        fd.top = new FormAttachment(0, 5);
        mNameField.setLayoutData(fd);
        mNameField.addModifyListener(new ModifyListener() {

            public void modifyText (ModifyEvent e) {
                validateProjectName();

            }
        });
        mNameField.addVerifyListener(new VerifyListener() {
            private IPath test = new Path("/");
            public void verifyText (VerifyEvent e) {
                if (e.character == '\b') // always allow backspace
                    return;
                String current = mNameField.getText();
                StringBuilder sb = new StringBuilder(current.length()+10);
                sb.append(current.substring(0,e.start));
                sb.append(e.text);
                sb.append(current.substring(e.end));
                if (!test.isValidSegment(sb.toString()))
                    e.doit = false;
            }
        });

        mInPlaceCheckBox = new Button(composite, SWT.RADIO);
        mInPlaceCheckBox.setText("Create new project in original folder");
        mInPlaceCheckBox.setToolTipText("Creates the new project in the same folder "
                + "as the original; no source files to be moved");
        mInPlaceCheckBox.addSelectionListener(selectionListener);
        mInPlaceCheckBox.setSelection(false);
        fd = new FormData();
        fd.top = new FormAttachment(label, 20);
        fd.left = new FormAttachment(0, 5);
        mInPlaceCheckBox.setLayoutData(fd);

        fd = new FormData();
        fd.top = new FormAttachment(mInPlaceCheckBox, 5);
        fd.left = new FormAttachment(0, 20);
        fd.right = new FormAttachment(100, -5);
        mOriginalLocation = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
        mOriginalLocation.setLayoutData(fd);

        mCreateInWorkspaceCheckBox = new Button(composite, SWT.RADIO);
        mCreateInWorkspaceCheckBox.setText("Create new project in workspace");
        mCreateInWorkspaceCheckBox.setToolTipText("The new project will be created in the "
                + "workspace; original source files will be copied or linked");
        mCreateInWorkspaceCheckBox.addSelectionListener(selectionListener);
        mCreateInWorkspaceCheckBox.setSelection(true);
        fd = new FormData();
        fd.top = new FormAttachment(mOriginalLocation, 7);
        fd.left = new FormAttachment(0, 5);
        mCreateInWorkspaceCheckBox.setLayoutData(fd);

        mCreateInNewPlaceCheckBox = new Button(composite, SWT.RADIO);
        mCreateInNewPlaceCheckBox.setText("Create new project in specified location");
        mCreateInNewPlaceCheckBox.setToolTipText("The new project will be created at an "
                + "arbitrary location in the file system");
        mCreateInNewPlaceCheckBox.addSelectionListener(selectionListener);
        mCreateInNewPlaceCheckBox.setSelection(false);
        fd = new FormData();
        fd.top = new FormAttachment(mCreateInWorkspaceCheckBox, 7);
        fd.left = new FormAttachment(0, 5);
        mCreateInNewPlaceCheckBox.setLayoutData(fd);

        mNewProjectLocation = new Text(composite, SWT.BORDER);
        mNewProjectLocation.addModifyListener(new ModifyListener() {

            public void modifyText (ModifyEvent e) {
                validateNewProjectLocation();

            }
        });
        mBrowse = new Button(composite, SWT.PUSH);
        fd = new FormData();
        fd.top = new FormAttachment(mCreateInNewPlaceCheckBox, 5);
        fd.left = new FormAttachment(0, 20);
        fd.right = new FormAttachment(mBrowse, -5);
        mNewProjectLocation.setLayoutData(fd);

        mBrowse.setText("Browse...");
        mBrowse.setToolTipText("Browse for the project directory");
        mBrowse.addSelectionListener(new SelectionListener() {

            public void widgetSelected (SelectionEvent e) {
                browseToNewProjectLocation();
            }

            public void widgetDefaultSelected (SelectionEvent e) {
                widgetSelected(e);

            }
        });
        fd = new FormData();
        fd.top = new FormAttachment(mCreateInNewPlaceCheckBox, 5);
        fd.right = new FormAttachment(100, -4);
        mBrowse.setLayoutData(fd);

        copyOrLinkGroup = new Group(composite, SWT.NONE);
        copyOrLinkGroup.setText("Source code access");
        copyOrLinkGroup.setLayout(new GridLayout(1, false));
        fd = new FormData();
        fd.top = new FormAttachment(mNewProjectLocation, 20);
        fd.left = new FormAttachment(0, 20);
        copyOrLinkGroup.setLayoutData(fd);

        mCopySourceCheckBox = new Button(copyOrLinkGroup, SWT.RADIO);
        mCopySourceCheckBox.setText("Copy source files into new project location");
        mCopySourceCheckBox.setToolTipText("The source files from the original project "
                + "will be copied into the new project");
        mCopySourceCheckBox.addSelectionListener(selectionListener);
        mCopySourceCheckBox.setSelection(true);

        mLinkSourceCheckBox = new Button(copyOrLinkGroup, SWT.RADIO);
        mLinkSourceCheckBox.setText("Access original source files by link");
        mLinkSourceCheckBox.setToolTipText("Original source files will be accessed "
                + "in the new location by means of links. Generated makefiles will have "
                + "absolute file references.");
        mLinkSourceCheckBox.addSelectionListener(selectionListener);
        mLinkSourceCheckBox.setSelection(false);
        setControl(composite);

        refresh();

    }

    private void browseToNewProjectLocation () {
        if (mDirectoryDialog == null) {
            mDirectoryDialog = new DirectoryDialog(getShell(), SWT.SAVE);
            mDirectoryDialog.setText("Select location of converted project");
            IPreferenceStore pref = ImporterPlugin.getDefault().getPreferenceStore();
            String dir = pref.getString(LOCATION_DIR_FILTER_ID);
            if (dir != null) {
                mDirectoryDialog.setFilterPath(dir);
            }
        }
        String fn = mDirectoryDialog.open();
        if (fn != null) {
            mNewProjectLocation.setText(fn);
            this.validateNewProjectLocation();
        }
        String dir = mDirectoryDialog.getFilterPath();
        if (dir != null){
            ImporterPlugin.getDefault().getPreferenceStore().putValue(LOCATION_DIR_FILTER_ID,dir);
        }
    }

    private void handleButtonSelection (SelectionEvent e) {
        Button source = (Button) e.getSource();
        if (source.getSelection()) {
            if (source == this.mInPlaceCheckBox)
                mHowCreated = IN_PLACE;
            else if (source == this.mCreateInWorkspaceCheckBox)
                mHowCreated = IN_WORKSPACE;
            else if (source == this.mCreateInNewPlaceCheckBox)
                mHowCreated = IN_FILE_SYSTEM;
            else if (source == this.mLinkSourceCheckBox) {
                this.mCopySourceCheckBox.setSelection(false);
            }
            else if (source == this.mCopySourceCheckBox) {
                this.mLinkSourceCheckBox.setSelection(false);
            }
        }
        refresh();
    }

    private void setSelectedProjects (ICodewrightProject[] projects) {
        if (projects == null || projects.length == 0) {
            // We shouldn't get here.
            setPageComplete(false);
            getControl().setEnabled(false);
        }
        else {
            getControl().setEnabled(true);
            File dir = projects[0].getProjectSpace().getLocation();
            setOriginalDir(dir);
        }
    }

    private void setOriginalDir (File dir) {
        if (dir == null && mOriginalDir != null || dir != null && !dir.equals(mOriginalDir)) {
            mOriginalDir = dir;
            mOriginalLocation.setText(dir != null?dir.getPath():"");

        }
    }

    private void refresh () {
        AccessModel access = getAccessModel();
        setSelectedProjects(access.getSelectedProjects());
        mBadTargetLocationError = null;

        switch (mHowCreated) {
        case IN_PLACE:
            mOriginalLocation.setEnabled(true);
            copyOrLinkGroup.setEnabled(false);
            mCopySourceCheckBox.setEnabled(false);
            mLinkSourceCheckBox.setEnabled(false);
            mNewProjectLocation.setEnabled(false);
            mBrowse.setEnabled(false);
            access.setCreateInPlace(true);
            access.setCreateInWorkspace(false);
            break;
        case IN_WORKSPACE:
            mOriginalLocation.setEnabled(false);
            copyOrLinkGroup.setEnabled(true);
            mCopySourceCheckBox.setEnabled(true);
            mLinkSourceCheckBox.setEnabled(true);
            mNewProjectLocation.setEnabled(false);
            mBrowse.setEnabled(false);
            access.setCreateInWorkspace(true);
            access.setCreateInPlace(false);
            break;
        case IN_FILE_SYSTEM:
            mOriginalLocation.setEnabled(false);
            copyOrLinkGroup.setEnabled(true);
            mCopySourceCheckBox.setEnabled(true);
            mLinkSourceCheckBox.setEnabled(true);
            mNewProjectLocation.setEnabled(true);
            mBrowse.setEnabled(true);
            access.setCreateInWorkspace(false);
            access.setCreateInPlace(false);
            validateNewProjectLocation();
            break;
        }
        access.setLinkToSourceFiles(mLinkSourceCheckBox.getSelection());
        validateProjectName();
        setErrorStatus();
    }

    /**
     * @todo davidp needs to add a method comment.
     */
    private void validateProjectName () {
        String name = mNameField.getText();
        if (name == null || name.trim().length() == 0) {
            mBadProjectNameError = "Missing project name";
        }
        else if (ResourcesPlugin.getWorkspace().getRoot().findMember(name) != null){
            mBadProjectNameError = "Project already exists in workspace";
        }
        else { 
            mBadProjectNameError = null;
            getAccessModel().setNewProjectName(name);
        }
        setErrorStatus();
    }

    private void validateNewProjectLocation () {
        String text = mNewProjectLocation.getText();
        this.mBadTargetLocationError = null;
        if (text != null && text.trim().length() > 0) {
            File f = new File(text);
            if (f.exists() && !f.isDirectory()) {
                this.mBadTargetLocationError = "Project location is not a directory";
            }
            else if (f.exists() && f.listFiles().length != 0) {
                mBadTargetLocationError = "Directory is not empty";
            }
            else {
                getAccessModel().setNewProjectLocation(f);
            }
        }
        else {
            this.mBadTargetLocationError = "Project location is not specified";
        }
        setErrorStatus();

    }

    @Override
    public void setVisible (boolean visible) {
        super.setVisible(visible);
        if (visible) {
            try {
                AccessModel access = getAccessModel();
                // If project space source changed, then reset project name.
                if (access.getProjectSpace() != null && !access.getProjectSpace().equals(mPsp)) {
                    ICodewrightProject[] projects = access.getSelectedProjects();
                    String name;
                    //If single project, use its name; otherwise use
                    // project-space name.
                    if (projects.length == 1)
                        name = projects[0].getName();
                    else
                        name = access.getProjectSpace().getName();
                    mNameField.setText(name);
                }
            }
            catch (Exception e) {
                mNameField.setText("");
            }
            refresh();
        }
    }
    
    
    private void setErrorStatus(){
        if (this.mBadProjectNameError != null){
            setErrorMessage(mBadProjectNameError);
            setPageComplete(false);
        }
        else if (this.mBadTargetLocationError != null){
            setErrorMessage(mBadTargetLocationError);
            setPageComplete(false);
        }
        else {
            setErrorMessage(null);
            setPageComplete(true);
        }
    }
    
    private AccessModel getAccessModel(){
        return ((CodeWrightImportWizard)getWizard()).getAccessModel();
    }
}
