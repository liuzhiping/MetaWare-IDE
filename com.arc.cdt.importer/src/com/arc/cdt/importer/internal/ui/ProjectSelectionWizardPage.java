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

import java.io.IOException;

import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.arc.cdt.importer.ImporterPlugin;
import com.arc.cdt.importer.core.ICodewrightProject;
import com.arc.cdt.importer.core.ICodewrightProjectSpace;
import com.arc.cdt.importer.core.PSPException;


/**
 * This wizard displays the projects extracted from the
 * CodeWright project-space file and offers the user the option to
 * select a subset if more then one are specified.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
class ProjectSelectionWizardPage extends WizardPage {

    private static final String PAGE_NAME = "ProjectSelect";
    private static final String PROJECT_IMAGE_KEY = "MetaDeveloper.project.image";
    private ICodewrightProjectSpace mProjectSpace;
    private CheckboxTableViewer mViewer;
    private static final Object[] EMPTY = new Object[0];
    private String mProjectSpaceError = null;
    private String mProjectError = null;


    /**
     */
    public ProjectSelectionWizardPage() {
        super(PAGE_NAME,"Project selection from Project space",null);
        
        Image image = ImporterPlugin.getDefault().getImageRegistry().get(PROJECT_IMAGE_KEY);
        if (image == null){
            ImageDescriptor desc = ImporterPlugin.getDefault().getImageDescriptor("icons/arc.gif");
            if (desc != null) 
                ImporterPlugin.getDefault().getImageRegistry().put(PROJECT_IMAGE_KEY,desc);           
        }
        
        this.setTitle("Project selection");
        this.setDescription("Select specific projects to be imported");
    }
    

    public void createControl (Composite parent) {
        Composite composite = new Composite(parent,SWT.NULL);
        composite.setLayout(new GridLayout(2,false));
        mViewer = CheckboxTableViewer.newCheckList(composite,SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.verticalSpan = 3;
        mViewer.getControl().setLayoutData(gridData);
        
        mViewer.setContentProvider(new IStructuredContentProvider(){

            public Object[] getElements (Object inputElement) {
                if (inputElement instanceof ICodewrightProjectSpace){
                    try {
                        return ((ICodewrightProjectSpace)inputElement).getProjects();
                    }
                    catch (Exception e) {
                        return new Exception[]{e};
                    }
                }
                else
                    return EMPTY;
            }

            public void dispose () {
            }

            public void inputChanged (Viewer viewer, Object oldInput, Object newInput) {
            }

        });
        
        mViewer.setLabelProvider(new LabelProvider(){
            @Override
            public String getText(Object element){
                if (element instanceof ICodewrightProject){
                    return ((ICodewrightProject)element).getName();
                }
                else if (element instanceof Exception){
                    return ((Exception)element).getMessage();
                }
                else return "";
            }
            @Override
            public Image getImage(Object element){
                if (element instanceof ICodewrightProject){
                    return ImporterPlugin.getDefault().getImageRegistry().get(PROJECT_IMAGE_KEY);
                }
                return null;
            }

            });
        
        mViewer.addCheckStateListener(new ICheckStateListener(){

            public void checkStateChanged (CheckStateChangedEvent event) {
                validate();        
            }});
        
        Button setAllButton = new Button(composite,SWT.PUSH);
        setAllButton.setText("Select All");
        setAllButton.setLayoutData(new GridData());
        setAllButton.addSelectionListener(new SelectionListener(){

            public void widgetSelected (SelectionEvent e) {
                mViewer.setAllChecked(true);
                validate();
            }

            public void widgetDefaultSelected (SelectionEvent e) {
                widgetSelected(e);
                
            }});
        
        Button clearAllButton = new Button(composite,SWT.PUSH);
        clearAllButton.setText("Clear All");
        clearAllButton.setLayoutData(new GridData());
        clearAllButton.addSelectionListener(new SelectionListener(){

            public void widgetSelected (SelectionEvent e) {
                mViewer.setAllChecked(false);
                validate();
            }

            public void widgetDefaultSelected (SelectionEvent e) {
                widgetSelected(e);
                
            }});
        
        setControl(composite);

    }
    
    private void validate(){
        Object[] elements = mViewer.getCheckedElements();
        ICodewrightProject projects[] = new ICodewrightProject[elements.length];
        System.arraycopy(elements,0,projects,0,projects.length);
        getAccessModel().setSelectedProjects(projects);
        mProjectError = null;
        if (projects.length == 0){
            mProjectError = "No project selected";
        }
        else {
            IProjectType type = null;
            ICodewrightProject last = null;
            for (ICodewrightProject p: projects){
                IProjectType t = AccessModel.getProjectType(p);
                if (t == null){
                    mProjectError = "Project " + p.getName() + " is of unknown type " + p.getTarget();
                }
                else if (type == null){
                    type = t;
                    last = p;                  
                }
                else if (type != t){
                    mProjectError = "Project " + p.getName() + " is of type " + 
                    p.getTarget() + 
                    (p.isLibrary()?"(library)":"(exe)")+", but project " +
                    (last != null? last.getName() + " is of type " + last.getTarget()+
                    (last.isLibrary()?"(library)":"(exe)"):"???");
                }
            }
        }
        setErrorState();
    }
    
    private void setProjectSpace (ICodewrightProjectSpace projectSpace) {
        mProjectSpaceError = null;
        if (mProjectSpace != projectSpace) {
            mProjectSpace = projectSpace;
            if (projectSpace != null) {
                ICodewrightProject projects[] = null;
                try {
                    projects = projectSpace.getProjects();
                    if (projects == null || projects.length == 0) {
                        mProjectSpaceError = "No projects in project space";
                    }
                }
                catch (IOException e) {
                    mProjectSpaceError = "Error occurred reading file: " + e.getMessage();
                    mProjectSpace = null;
                }
                catch (PSPException e) {
                    mProjectSpaceError = e.getMessage();
                    mProjectSpace = null;
                }
                
            }
            mViewer.setInput(mProjectSpace);
            mViewer.setAllChecked(true);
            validate();
            setErrorState();
        }    
    }
    
    private void setErrorState(){
        if (mProjectSpaceError != null) {
            setErrorMessage(mProjectSpaceError);
            setPageComplete(false);
        }
        else if (mProjectError != null){
            setErrorMessage(mProjectError);
            setPageComplete(false);
        }
        else {
            setErrorMessage(null);
            setPageComplete(mViewer.getCheckedElements().length > 0);
        }
    }
    
    
    @Override
    public void setVisible (boolean visible) {
        super.setVisible(visible);
        if (visible){
            try {
                setProjectSpace(getAccessModel().getProjectSpace());
            }
            catch (Exception e) {
                mProjectSpaceError = e.getMessage();
                setProjectSpace(null);
                setErrorState();
            }
        }
    }
    
    private AccessModel getAccessModel(){
        return ((CodeWrightImportWizard)getWizard()).getAccessModel();
    }
}
