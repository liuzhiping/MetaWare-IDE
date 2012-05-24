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

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import com.arc.cdt.debug.seecode.ui.UISeeCodePlugin;

/**
 * Creates a panel consisting of a text field and a "browse" button for selecting
 * a C/C++ project.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
@SuppressWarnings("restriction")
class ProjectSelectorFactory {
    private static final String PROJECT_TOOLTIP = "Enter the project name (may be empty).";
    
    public interface IProjectChangeListener{
        /**
         * Called to indicate a project change. The argument may be <code>null</code>,
         * which means that the user has cleared the project name field.
         * @param project the specifid project, or <code>null</code>.
         */
        void onProjectChange(IProject project);
        
        /**
         * Called when the user types in a bogus project name. Implementer is expected
         * to enable/disable associated widgets, or set a status field somewhere.
         * @param projectName the name the user entered which does not correspond to
         * a valid, open C/C++ project. Will be non-null and not empty.
         */
        void onBogusProjectName(String projectName);
    }
    
    public interface IProjectSelector {
        Control getControl();
        IProject getProject();
        void setProject(IProject project);
    }
    
    /**
     * Create a panel that will select a project.
     * @param parent parent of panel.
     * @param initProject the initial project name or <code>null</code>.
     * @param tooltip a tooltip to be used, or <code>null</code> if a default is to be used.
     * @param listener callback for the client to extract the project.
     * @param horizontalSpan the horizontal grid span that the widgets are to consume.
     * @param name name to apply to widgets for benefit of GUI tester.
     * @return callback for accessing the content of the panel.
     */
    public static IProjectSelector createProjectSelectionPanel(Composite parent, String initProject,
                    String tooltip,
                    final IProjectChangeListener listener,
                    int horizontalSpan,
                    String name){
        Composite panel = parent;
        // If not enough, then just create a subpanel.
        if (horizontalSpan < 3) {
            panel = new Composite(parent, 0);
            GridLayout gridLayout = new GridLayout(3, false);
            gridLayout.marginWidth = 0; // so as to align with other panels vertically
            panel.setLayout(gridLayout);
            GridData gd = new GridData(GridData.FILL_BOTH);
            gd.horizontalSpan = horizontalSpan;
            panel.setLayoutData(gd);
        }
             
        
        Label label = new Label(panel,SWT.LEFT);
        label.setText("Select Project: ");
        if (tooltip == null) tooltip = PROJECT_TOOLTIP;
        label.setToolTipText(tooltip);
        
        final Text field = new Text(panel,SWT.BORDER);
        field.setData("name",name + ".field"); // for GUI tester
        field.setToolTipText(tooltip);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = 200;
        if (panel == parent){
            gd.horizontalSpan = horizontalSpan-2;
        }
        field.setLayoutData(gd); 
        field.setToolTipText(tooltip);
        final IProject project[] = new IProject[1];
        if (initProject != null) {
            field.setText(initProject);
            project[0] = setProjectSelection(initProject,listener,null);
        }
       
        //if (initContent != null) fProjectField.setText(initContent.projectName);
        field.addModifyListener(new ModifyListener(){

            @Override
            public void modifyText (ModifyEvent e) {
                project[0] = setProjectSelection(field.getText(),listener,project[0]);            
            }});
        
        field.addFocusListener(new FocusListener(){

            @Override
            public void focusGained (FocusEvent e) {           
            }

            @Override
            public void focusLost (FocusEvent e) {
                if (field.getText() != null && field.getText().trim().length() > 0){
                    IProject p = lookupProject(field.getText());
                    if (p == null){
                        UISeeCodePlugin.showError("Project selection error","\"" + field.getText() + "\" is not a valid, open C/C++ project");
                        field.setText("");
                        field.selectAll();
                        field.setFocus();
                    }
                    else if (p != project[0]){
                        project[0] = p;
                        listener.onProjectChange(p);
                    }
                }
                
            }});
        
        final Button projectButton = new Button(panel,SWT.PUSH);
        projectButton.setText("Browse...");
        projectButton.setData("name",name + ".browse");  // for GUI tester.
        projectButton.setToolTipText(tooltip);

        projectButton.addSelectionListener(new SelectionListener(){

            @Override
            public void widgetDefaultSelected (SelectionEvent e) {}

            @Override
            public void widgetSelected (SelectionEvent e) {
                ICProject choice = chooseCProject(projectButton.getShell(),lookupProject(field.getText()));
                if (choice != null){
                    field.setText(choice.getProject().getName());
                    if (project[0] != choice.getProject()){
                        project[0] = choice.getProject();
                        listener.onProjectChange(choice.getProject());
                    }
                }
                // If browse-to-project dialog canceled, don't change anything
//                else {
//                    field.setText("");
//                    listener.onProjectChange(null);
//                }
                
            }});
        final Composite panelCopy = panel;
        return new IProjectSelector(){

            @Override
            public Control getControl () {
                return panelCopy;
            }

            @Override
            public IProject getProject () {
                return project[0];
            }

            @Override
            public void setProject (IProject p) {
                project[0] = p;
                field.setText(p != null?p.getName():"");
                
            }};
    }
    
    private static IProject setProjectSelection (String projectName, IProjectChangeListener listener, IProject oldProject) {
        if (projectName != null && projectName.trim().length() > 0) {
            projectName = projectName.trim();
            IProject project = lookupProject(projectName);
            if (project != oldProject) {
                if (project != null)
                    listener.onProjectChange(project);
                else
                    listener.onBogusProjectName(projectName);
            }
            return project;
        }
        else {
            listener.onProjectChange(null);
            return null;
        }
    }
    
    public static IProject lookupProject(String projectName){
        ICProject p;
        try {
            p = CoreModel.getDefault().getCModel().getCProject(projectName);
        }
        catch (IllegalArgumentException e) {
            return null;
        }
        if (p.exists()) return p.getProject();
        return null;
    }
    
    private static ICProject chooseCProject(Shell shell, IProject defaultProject) {
        try {
            ICProject[] projects = getCProjects();

            ILabelProvider labelProvider = new CElementLabelProvider();
            ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, labelProvider);
            dialog.setTitle(LaunchMessages.getString("CMainTab.Project_Selection")); //$NON-NLS-1$
            dialog.setMessage(LaunchMessages.getString("CMainTab.Choose_project_to_constrain_search_for_program")); //$NON-NLS-1$
            dialog.setElements(projects);

            if (defaultProject != null) {
                ICProject cp = getCProject(defaultProject);
                if (cp != null)
                    dialog.setInitialSelections(new Object[]{cp});
            }
            if (dialog.open() == Window.OK) {
                return (ICProject)dialog.getFirstResult();
            }
        } catch (CModelException e) {
            LaunchUIPlugin.errorDialog("Launch UI internal error", e); //$NON-NLS-1$            
        }
        return null;
    }
    
    /**
     * Return an array a ICProject whose platform match that of the runtime env.
     */
    private static ICProject[] getCProjects() throws CModelException {
        return CoreModel.getDefault().getCModel().getCProjects();
    }
    
    private static ICProject getCProject(IProject p) throws CModelException{
        if (p == null) return null;
        ICProject[] cprojects = getCProjects();
        for (ICProject cp: cprojects){
            if (cp.getProject() == p) return cp;
        }
        return null;
    }

}
