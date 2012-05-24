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
import java.io.IOException;

import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.cdt.utils.elf.parser.ElfParser;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;

import com.arc.mw.util.StringUtil;

/**
 * A factory method for creating a panel for selecting a file that can be associated
 * with a project or be outside of the workspace altogether.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
@SuppressWarnings("restriction")
class PathSelectorFactory {
    
    public interface IPathChangeListener {
        public void onPathChange(String path);
    }
    
    public interface ISelectionContext {
        /**
         * Return the default directory from which to search.
         * @return the default director
         */
        public String getDefaultSearchLocation();
        /**
         * Return the list of things to select from when doing a search-from-project
         * operation.
         * @return the associated list of things to choose from.
         */
        public Object[] getSearchList();
        
        /**
         * Return the associated project.
         * @return the associated project, or <code>null</code> if there isn't one.
         */
        public IProject getProject();
        
        /**
         * @return file dialog filter extensions to pass to <code>FileDialog</code> object.
         */
        public String[] getFilterExtensions();
        
        /**
         * @return file dialog filter names to pass to <code>FileDialog</code> object.
         */
        public String[] getFilterNames();
    }
    
    public interface IPathSelector{
        /**
         * Called by client to get the path selection panel.
         * @return the path selection panel.
         */
        Control getControl();
        /**
         * Called by client to enable/disable the "search project" button.
         * @param v enable state for the "search project" button.
         */
        void setProjectSelectEnabled(boolean v);
        
        /**
         * Called by client to set the focus on the path selection field.
         */
        void setFocus();
        
        /**
         * Get the selected path.
         */
        String getPath();
        
        /**
         * Set a path that is somehow derived externally.
         * @param the path to be set; {@link #getPath()} will subsequently return it.
         */
        void setPath(String path);
        
        /**
         * Set new associated project.
         * @param the project.
         */
        void setProject(IProject project);
    }
    
    private static String getCommandFromCommandLine(String commandLine){
        if (commandLine == null) return null;
        String command[] = StringUtil.stringToArray(commandLine);
        return command.length > 0?command[0]:null;
    }
    
    /**
     * Given a command to invoke and its associated project, compute the target CPU.
     * @param command the command.
     * @param project the associated project if known; otherwise <code>null</code>.
     * @return the target CPU (such as, "ac", "mips", "arm", etc.)
     */
    public static String getTargetCPU (String command, IProject project) {
        String programName = getCommandFromCommandLine(command);
        String programCPU = null;
        if (programName != null) {
            IPath exePath = new Path(programName);
            if (project != null && !exePath.isAbsolute()) {
                exePath = project.getFile(programName).getLocation();
            }
            if (!exePath.toFile().exists()) {
                return null;
            }
            ElfParser parser = new ElfParser();
            IBinaryParser.IBinaryFile b;
            try {
                b = parser.getBinary(exePath);
            }
            catch (IOException e) {
                b = null;
            }
            if (b instanceof IBinaryParser.IBinaryExecutable)
                programCPU = ((IBinaryParser.IBinaryExecutable) b).getCPU();
        }
        if ("st100".equals(programCPU)) {
            programCPU = "st";
        }
        // We treat vc1, vc2, and vc3 as the same. The debugger
        // will distinguish.
        if ("vc3".equals(programCPU))
            programCPU = "vc";
        return programCPU;
    }
    
    /**
     * 
     * @param parent parent container.
     * @param label Label to prefix the selector panel.
     * @param tooltip tool tip.
     * @param initContent initial setting for the file.
     * @param fileDescription description of file for label.
     * @param listener callback for when things change.
     * @param selectionContext callback to retrieve context information.
     * @param horizontalSpan the number of grid columns that are available for this panel.
     * @param name the name prefix to be assigned to widgets for benefit of GUI tester.
     * @return an interface through which client can communicate with the panel.
     */
    public static IPathSelector createPathSelectPanel(Composite parent,
        String label,
        String tooltip,
        String initContent,
        final String fileDescription,
        final IPathChangeListener listener,
        final ISelectionContext selectionContext,
        int horizontalSpan,
        String name){
        Composite panel = parent;
        if (horizontalSpan < 4) {
            panel = new Composite(parent,0);
            GridLayout gridLayout = new GridLayout(4,false);
            gridLayout.marginWidth = 0; // to make things align with other things in parent
            panel.setLayout(gridLayout);
            panel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.horizontalSpan = horizontalSpan;
            panel.setLayoutData(gd);
        }
        
        final IProject[] projectPtr = new IProject[1];
        projectPtr[0] = selectionContext.getProject();
        
        Label pathLabel = new Label(panel,SWT.LEFT);
        pathLabel.setText(label);
        pathLabel.setToolTipText(tooltip);
        
        final Text field = new Text(panel,SWT.BORDER);
        
        field.setData("name",name + ".field");  // for GUI tester.
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = 400;
        if (panel == parent) {
            gd.horizontalSpan = horizontalSpan-3;
        }
        field.setLayoutData(gd); 
        field.setToolTipText(tooltip);
        if (initContent != null) field.setText(initContent);
        field.addModifyListener(new ModifyListener(){

            @Override
            public void modifyText (ModifyEvent e) {    
                String text = field.getText();
                if (projectPtr[0] != null) {
                    String command[] = StringUtil.stringToArray(text);
                    if (command.length > 0){
                        if (!new File(command[0]).isAbsolute()){
                            command[0] = new File(projectPtr[0].getLocation().toOSString(),command[0]).toString();
                            text = StringUtil.arrayToArgString(command);
                        }
                    }
                }
                listener.onPathChange(text);              
            }});
        

        
        
        final Button searchProjectButton = new Button(panel,SWT.PUSH);
        searchProjectButton.setData("name", name + ".search_project");
        searchProjectButton.setText("Search Project...");
        searchProjectButton.setToolTipText("Search the project for the file");
        searchProjectButton.addSelectionListener(new SelectionListener(){
            @Override
            public void widgetDefaultSelected (SelectionEvent e) {                  
            }

            @Override
            public void widgetSelected (SelectionEvent e) {
                String command = searchProjectForFile(searchProjectButton.getShell(),selectionContext,fileDescription);
                if (command != null) {
                    command = canonicalizePath(command,selectionContext.getProject());
                    field.setText(command);
                    listener.onPathChange(command);  
                }              
            }

           });
        
        Button browseButton = new Button(panel,SWT.PUSH);
        browseButton.setText("Browse...");
        browseButton.setData("name",name + ".browse");
        browseButton.setToolTipText("Browse file system for the file");
       
        browseButton.addSelectionListener(new SelectionListener(){
            @Override
            public void widgetDefaultSelected (SelectionEvent e) {                  
            }

            @Override
            public void widgetSelected (SelectionEvent e) {
                String command = doBrowseForFile(field.getShell(),formAbsolutePath(getCommandFromCommandLine(field.getText()),projectPtr[0]),selectionContext);
                if (command != null) {
                    command = canonicalizePath(command,projectPtr[0]);
                    field.setText(command);
                    listener.onPathChange(command);  
                }             
            }});
        final Composite panelCopy = panel;
        return new IPathSelector(){

            @Override
            public Control getControl () {
                return panelCopy;
            }

            @Override
            public String getPath () {
                return field.getText();
            }

            @Override
            public void setFocus () {
                field.selectAll();
                field.setFocus();            
            }

            @Override
            public void setProjectSelectEnabled (boolean v) {
                searchProjectButton.setEnabled(v);               
            }
            
            @Override
            public void setPath(String path){
                if (path == null) path = "";
                field.setText(path);
            }

            @Override
            public void setProject (IProject project) {
                if (projectPtr[0] != project){
                    String path = field.getText();
                    if (path != null && path.length() > 0){
                        String command[] = StringUtil.stringToArray(path);
                        if (command.length > 0){
                            if (!new File(command[0]).isAbsolute() && projectPtr[0] != null){
                                command[0] = formAbsolutePath(command[0],projectPtr[0]);                             
                            }
                            if (project != null){
                                command[0] = canonicalizePath(command[0],project);
                            }
                            path = StringUtil.arrayToArgString(command);
                        }
                        projectPtr[0] = project;
                        field.setText(path);
                        listener.onPathChange(path);  
                    }
                }
                
            }
        };
    }
    
    private static String formAbsolutePath(String path, IProject project){
        if (path == null || new File(path).isAbsolute() || project == null) return path;
        IPath pathPath = new Path(path);
        IPath projectPath = project.getLocation();
        return projectPath.append(pathPath).toString();
        
    }
    
    private static String canonicalizePath (String command, IProject project) {
        // if command is absolute and we have an associated project, attempt to
        // make it relative to the project.
        if (project != null && new File(command).isAbsolute()){
            IPath projectPath = project.getLocation();
            IPath cmdPath = new Path(command);
            if (projectPath.isPrefixOf(cmdPath)){
                command = cmdPath.setDevice(null).removeFirstSegments(projectPath.segmentCount()).toString();
            }
        }
        if (command.indexOf(' ') > 0){
            command = "\"" + command.replaceAll("\\\\","\\\\\\\\") + "\"";
        }
        return command;
    }
    
    /**
     * Show dialog to browse to exe file. Return the exe file path, or null if it was canceled.
     * @return the exe file path or null if it was canceled.
     */
    private static String searchProjectForFile (Shell shell, ISelectionContext context, String description) {
        ILabelProvider programLabelProvider = new CElementLabelProvider() {

            @Override
            public String getText (Object element) {
                if (element instanceof IBinary) {
                    IBinary bin = (IBinary) element;
                    StringBuffer name = new StringBuffer();
                    name.append(bin.getPath().lastSegment());
                    return name.toString();
                }
                return super.getText(element);
            }

            @Override
            public Image getImage (Object element) {
                if (!(element instanceof ICElement)) {
                    return super.getImage(element);
                }
                ICElement celement = (ICElement) element;

                if (celement.getElementType() == ICElement.C_BINARY) {
                    IBinary belement = (IBinary) celement;
                    if (belement.isExecutable()) {
                        return DebugUITools.getImage(IDebugUIConstants.IMG_ACT_RUN);
                    }
                }

                return super.getImage(element);
            }
        };

        ILabelProvider qualifierLabelProvider = new CElementLabelProvider() {

            @Override
            public String getText (Object element) {
                if (element instanceof IBinary) {
                    IBinary bin = (IBinary) element;
                    StringBuffer name = new StringBuffer();
                    name.append(bin.getCPU() + (bin.isLittleEndian() ? "le" : "be")); //$NON-NLS-1$ //$NON-NLS-2$
                    name.append(" - "); //$NON-NLS-1$
                    name.append(bin.getPath().toString());
                    return name.toString();
                }
                if (element instanceof IFile){
                    IFile file = (IFile)element;
                    return file.getProjectRelativePath().toString();
                }
                return super.getText(element);
            }
        };

        TwoPaneElementSelector dialog = new TwoPaneElementSelector(shell, programLabelProvider,
            qualifierLabelProvider);
        dialog.setElements(context.getSearchList());
        dialog.setMessage(description/*LaunchMessages.getString("CMainTab.Choose_program_to_run")*/); 
        dialog.setTitle("File Selection"); //$NON-NLS-1$
        dialog.setUpperListLabel("Discovered file list"/*LaunchMessages.getString("Launch.common.BinariesColon*/); //$NON-NLS-1$
        dialog.setLowerListLabel(LaunchMessages.getString("Launch.common.QualifierColon")); //$NON-NLS-1$
        dialog.setMultipleSelection(false);
        // dialog.set
        if (dialog.open() == Window.OK) {
            Object result = dialog.getFirstResult();
            if (result instanceof IBinary) {
                IBinary binary = (IBinary) result;
                result = binary.getResource().getLocation();
            }
            else if (result instanceof IFile) {
                result = ((IFile) result).getLocation();
            }
            //IProject p = context.getProject();
            if (result instanceof IPath) {
//                if (p != null) {
//                    IPath projectPath = p.getLocation();
//                    if (projectPath.isPrefixOf((IPath) result)) {
//                        result = ((IPath) result).removeFirstSegments(projectPath.segmentCount());
//                    }
//                }
                result = ((IPath)result).toOSString();
            }
            return result.toString();
        }
        return null;
    }
    
    private static String doBrowseForFile (Shell shell, String oldValue, ISelectionContext context) {
        FileDialog fileDialog = new FileDialog(shell, SWT.NONE);
        if (oldValue != null) {
            fileDialog.setFilterPath(new File(oldValue).getParent());
            fileDialog.setFilterExtensions(context.getFilterExtensions());
            fileDialog.setFilterNames(context.getFilterNames());
            fileDialog.setFileName(new File(oldValue).getName());
        }
        return fileDialog.open();
    }
}
