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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.ui.CLaunchConfigurationTab;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import com.arc.cdt.debug.seecode.core.ISeeCodeLaunchConfigurationConstants;
import com.arc.cdt.debug.seecode.core.launch.CMPDInfoFromVDKConfigReader;
import com.arc.cdt.debug.seecode.core.launch.ICMPDInfo;
import com.arc.cdt.debug.seecode.internal.ui.PathSelectorFactory.IPathChangeListener;
import com.arc.cdt.debug.seecode.internal.ui.PathSelectorFactory.IPathSelector;
import com.arc.cdt.debug.seecode.internal.ui.PathSelectorFactory.ISelectionContext;
import com.arc.cdt.debug.seecode.internal.ui.ProjectSelectorFactory.IProjectChangeListener;
import com.arc.cdt.debug.seecode.internal.ui.ProjectSelectorFactory.IProjectSelector;
import com.arc.cdt.debug.seecode.ui.UISeeCodePlugin;
import com.arc.cdt.debug.seecode.ui.views.IContextHelpIds;
import com.arc.mw.util.StringUtil;
import com.arc.seecode.engine.ProcessIdList;

/**
 * This class creates the debugger configuration page on the launcher page that
 * is associated with a MetaWare CMPD invocation.
 * <P>
 * The logic of basically translated from the standalone GUI's CMPD configuration dialog.
 * 
 * @author David Pickens
 */
public class CMPDDebuggerTab extends CLaunchConfigurationTab {
    private static final String VDK_CONFIG_NAME_PATTERN = "(dbg|vdk)config.*.xml";
    private static final Pattern VDK_CONFIG_PATTERN = Pattern.compile(VDK_CONFIG_NAME_PATTERN);
    private static final String ADD_PROCESS = "Add process";
    private static final String[] COLUMNS = {  "Process Set Name", "IDs", "Project", "Program Command",
    // "Export Mem",
    // "Import Mem",
    // "Load Initially",
    // "Import Options"
    };
    private static final String[] COLUMN_TIPS = { "Name of CMPD process", "ID number or collection of numbers as a range (e.g., 3:5) or comma separated list (e.g., 1,3).",
                                "Associated project, if any", "Target executable and arguments (possibly quoted)" };
    private static final int[] COLUMN_WIDTHS = { 120, 80, 120, 300 };
    private static final int[] COLUMN_STYLES = { SWT.LEFT, SWT.RIGHT, SWT.CENTER, SWT.LEFT };
    
    private static boolean VDK_SUPPORT = false; // change to true when VDK support is restored
    private Table fTable;
    private Button fEditButton;
    private Button fDeleteButton;
    private EditCmpdProcessDialog fEditDialog;
    private String fProjectName = null;
    private IProject fProject = null;
    private String fVDKRelativeConfigPath = null;
    private String fVDKAbsoluteConfigPath = null;
    private IProjectSelector fProjectSelector;
    private boolean fWasCustomized = false; // true if user customizes things.
    private ILaunchConfiguration fLaunchConfig = null;
   
    public CMPDDebuggerTab() {
        // @todo Auto-generated constructor stub
    }
    
    @Override
    public Image getImage(){
        return UISeeCodePlugin.getDefault().getDebuggerIcon();
    }

    @Override
    public void createControl (Composite parent) {
        // There has to be an easier way to get this panel to scroll. The following works
        // but seems like overkill. If anyone knows a better way, by all means simplify this
        // code.
        ScrolledComposite scroller = new ScrolledComposite(parent,SWT.V_SCROLL|SWT.H_SCROLL){
            @Override
            public Point computeSize(int hhint,int vhint, boolean changed){
                if (changed) {
                    Control content = getContent();
                    //We assume the trim has negligible demands on the content size.
                    //But we want to take into account wrapping labels when the width is constrained.
                    Point size = content.computeSize(hhint,vhint,true);
                    setMinSize(size);                    
                }
                return super.computeSize(hhint,vhint,changed);
            }
        };
        Composite cmpdPanel = new Composite(scroller,0);
        cmpdPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
        scroller.setExpandHorizontal(true);
        scroller.setExpandVertical(true);
        scroller.setContent(cmpdPanel);
        cmpdPanel.setLayout(new GridLayout(1,false));
        setControl(scroller);
        
        if (VDK_SUPPORT)
            createVDKSelectionPanel(cmpdPanel);

        createConfigPanel(cmpdPanel);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(cmpdPanel, IContextHelpIds.PREFIX + "cmpd_config");
        
    }
    
    private Control createVDKSelectionPanel(Composite parent){
        Group group = new Group(parent,SWT.SHADOW_ETCHED_IN);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        group.setLayout(new GridLayout(4,false));
        
        fProjectSelector = createProjectSelectPanel(group,4);
        
        fileSelector = createVDKConfigSelect(group,4);
        
        extractInfoButton = new Button(group,SWT.PUSH);
        extractInfoButton.setText("Reset CMPD Processes");
        extractInfoButton.setToolTipText("Configure CMPD processes from the specified VDK configuration file.");
        extractInfoButton.addSelectionListener(new SelectionListener(){

            @Override
            public void widgetDefaultSelected (SelectionEvent e) {           
            }

            @Override
            public void widgetSelected (SelectionEvent e) {
                resetFromVDKConfigFile();
                
            }});
        boolean valid = this.fVDKAbsoluteConfigPath != null && !new File(fVDKAbsoluteConfigPath).exists();
        extractInfoButton.setData("name","reset_cmpd_processes"); // for GUI tester
        extractInfoButton.setEnabled(valid);
        
        this.configFileNotExistLabel = new Label(group,SWT.CENTER);
        configFileNotExistLabel.setText("VDK Configuration file does not exist.");
        configFileNotExistLabel.setVisible(!valid);
        configFileNotExistLabel.setData("name","config_file_not_exist"); // for GUI tester
        configFileNotExistLabel.setForeground(configFileNotExistLabel.getDisplay().getSystemColor(SWT.COLOR_RED));
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        configFileNotExistLabel.setLayoutData(gd);
        return group;
    }
    
    
    private void resetFromVDKConfigFile(){
        try {
            ICMPDInfo info = CMPDInfoFromVDKConfigReader.extractCMPDInfo(new File(fVDKAbsoluteConfigPath), fProject);
            initializeFrom(info);
            fWasCustomized = false;
            
        }
        catch (Exception e) {
            UISeeCodePlugin.showError("VDK read error", "The specified file is not a VDK configuration file, or it is corrupt.",e);
        }
    }

    
    private IProjectSelector createProjectSelectPanel(Composite parent, int horizontalSpan){
        IProjectSelector projectSelector = ProjectSelectorFactory.createProjectSelectionPanel(parent, fProjectName, 
            "Specify project from which to look for VDK configuration file",new IProjectChangeListener(){

            @Override
            public void onBogusProjectName (String projectName) {
                CMPDDebuggerTab.this.setErrorMessage("Unknown project \"" + projectName +"\"");  
                if (fileSelector != null)
                    fileSelector.setProjectSelectEnabled(false);
            }

            @Override
            public void onProjectChange (IProject project) {
                CMPDDebuggerTab.this.setErrorMessage(null);
                setProject(project,true);
            }},horizontalSpan,"vdk_project");
        if (fProject != null) projectSelector.setProject(fProject);
        return projectSelector;
    }
    
    private IPathSelector createVDKConfigSelect(Composite parent,int horizontalSpan){
        IPathSelector selector = PathSelectorFactory.createPathSelectPanel(parent,
            "VDK Configuration File: ",
            "Specify path to VDK configuration file from which to extract CMPD information",
            fVDKRelativeConfigPath,
            "Choose VDK configuration file",
            new IPathChangeListener(){

                @Override
                public void onPathChange (String path) {
                    String segments[] = StringUtil.stringToArray(path);
                    String vdkPath = segments.length > 0?canonicalizePath(segments[0]):null;
                    if (vdkPath != null && new File(vdkPath).exists()){
                        if (!vdkPath.equals(fVDKAbsoluteConfigPath)){
                            fVDKAbsoluteConfigPath = vdkPath;
                            fVDKRelativeConfigPath = relativizePath(vdkPath,fProject);
                            setDirty(true);
                            updateLaunchConfigurationDialog();                           
                        }
                        extractInfoButton.setEnabled(true);
                        configFileNotExistLabel.setVisible(false);
                        if (!fWasCustomized)
                            resetFromVDKConfigFile();
                    }
                    else {
                        extractInfoButton.setEnabled(false);  
                        configFileNotExistLabel.setVisible(true);
                    }
                }},
            new ISelectionContext(){

                @Override
                public Object[] getSearchList () {
                    if (fProject != null){                       
                        IFile[] paths = extractConfigFiles(fProject);
                        if (paths != null) return paths;
                    }
                    return new Object[0];                  
                }
                
                @Override
                public IProject getProject(){
                    return fProject;
                }

                @Override
                public String getDefaultSearchLocation () {
                    if (fProject != null){
                        return fProject.getRawLocation().toOSString();
                    }
                    return Platform.getLocation().toOSString();
                }

                @Override
                public String[] getFilterExtensions () {
                    return new String[]{"*config*.xml"};
                }

                @Override
                public String[] getFilterNames () {
                    return new String[]{"VDK Configuration File"};
                }},
                horizontalSpan,"vdkconfig"
            );
        selector.setProjectSelectEnabled(fProject != null);
        return selector;
    }
       
    
    private Control createConfigPanel(Composite parent){
        Composite tablePanel = new Composite(parent,0);

        tablePanel.setLayoutData(new GridData(GridData.FILL_BOTH));
        tablePanel.setLayout(new GridLayout(2,false));
               
        suspendAtStartButton = new Button(tablePanel,SWT.CHECK);
        suspendAtStartButton.setText("Start session with processes suspended");
        suspendAtStartButton.setData("name","cmpd_suspend_at_startup"); // for GUI teseter.
        suspendAtStartButton.setSelection(this.fSuspendedAtStart);
        suspendAtStartButton.addSelectionListener(new SelectionListener(){

            @Override
            public void widgetDefaultSelected (SelectionEvent e) {
                // @todo Auto-generated method stub
                
            }

            @Override
            public void widgetSelected (SelectionEvent e) {
                setSuspendAtStartup(suspendAtStartButton.getSelection());               
            }});
        
        Label instructionLabel = new Label(tablePanel,0);
        instructionLabel.setText( 
            "Click " + ADD_PROCESS + " to add a process to the process table. "+
                "To modify a process, select a row and click Edit.");
        GridData gd = new GridData(GridData.FILL_HORIZONTAL|GridData.BEGINNING);
        gd.horizontalSpan = 2;
        instructionLabel.setLayoutData(gd);
        
        Composite buttonPanel = new Composite(tablePanel,0);
        gd = new GridData(GridData.BEGINNING);
        buttonPanel.setLayout(new GridLayout(3,true));
        buttonPanel.setLayoutData(gd);
        
        Button addButton = new Button(buttonPanel,SWT.PUSH);
        addButton.setData("name","cmpd_add"); // for GUI tester
        addButton.addSelectionListener(new SelectionListener(){
            @Override
            public void widgetDefaultSelected (SelectionEvent e) {        
            }

            @Override
            public void widgetSelected (SelectionEvent e) {
                showAddProcessDialog();             
            }});
        addButton.setText(ADD_PROCESS);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        addButton.setLayoutData(gd);
        
        fEditButton = new Button(buttonPanel,SWT.PUSH);
        fEditButton.setData("name","cmpd_edit"); // for GUI tester
        fEditButton.setEnabled(false);
        fEditButton.addSelectionListener(new SelectionListener(){
            @Override
            public void widgetDefaultSelected (SelectionEvent e) {        
            }

            @Override
            public void widgetSelected (SelectionEvent e) {
                showEditProcessDialog();             
            }});
        fEditButton.setText("Edit");
        gd = new GridData(GridData.FILL_HORIZONTAL);
        fEditButton.setLayoutData(gd);
        
        fDeleteButton = new Button(buttonPanel,SWT.PUSH);
        fDeleteButton.setData("name","cmpd_delete"); // for GUI tester
        fDeleteButton.addSelectionListener(new SelectionListener(){
            @Override
            public void widgetDefaultSelected (SelectionEvent e) {        
            }

            @Override
            public void widgetSelected (SelectionEvent e) {
                deleteSelectedProcess();             
            }});
        fDeleteButton.setEnabled(false);
        fDeleteButton.setText("Delete");
        gd = new GridData(GridData.FILL_HORIZONTAL);
        fDeleteButton.setLayoutData(gd);
        
        fTable = new Table(tablePanel,SWT.BORDER| SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        fTable.setData("name","cmpd_table"); // for GUI tester.
        fTable.setLinesVisible(true);
        fTable.setHeaderVisible(true);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        fTable.setLayoutData(gd);
        fTable.addSelectionListener(new SelectionListener(){

            @Override
            public void widgetDefaultSelected (SelectionEvent e) {
                // @todo Auto-generated method stub
                
            }

            @Override
            public void widgetSelected (SelectionEvent e) {
                boolean selected = fTable.getSelectionIndex() >= 0;
                fEditButton.setEnabled(selected);
                fDeleteButton.setEnabled(selected);
                
            }});
        int c = 0;
        for (String name: COLUMNS) {
            TableColumn column = new TableColumn(fTable,COLUMN_STYLES[c]);
            column.setToolTipText(COLUMN_TIPS[c]);
            column.setWidth(COLUMN_WIDTHS[c++]);
            column.setText(name);

        }
        fTable.setItemCount(0);
        return tablePanel;
    }
    
    
    private void error(Shell shell, String msg){
        MessageBox msgBox = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
        msgBox.setMessage(msg);
        msgBox.setText("Input error");
        msgBox.open();
    }
    
    private void setSuspendAtStartup(boolean v){
        if (this.fSuspendedAtStart != v){
            this.fSuspendedAtStart = v;
            if (this.suspendAtStartButton != null)
                this.suspendAtStartButton.setSelection(v);
            this.setDirty(true);
            this.updateLaunchConfigurationDialog();
        }
    }
    
    private EditCmpdProcessDialog getEditDialog(){
        if (fEditDialog == null){
            fEditDialog = new EditCmpdProcessDialog(getShell());
        }
        return fEditDialog;
    }
    

    private static final int LOC_PROCESS_NAME = 0;
    private static final int LOC_IDS = 1;
    private static final int LOC_PROJECT_NAME = 2;
    private static final int LOC_COMMAND = 3;
    
    private void showAddProcessDialog(){
        ArrayList<ProcessIdList> list = new ArrayList<ProcessIdList>();
        for (TableItem item: fTable.getItems())  {
            list.add(ProcessIdList.create(item.getText(LOC_IDS)));
        }
        CMPDRecordContent results = getEditDialog().open(null,getConfigurator(fTable.getItemCount()),list);
        if (results != null){
            fWasCustomized = true;
            TableItem item = new TableItem(fTable,0);
            updateTableRow(results, item);
        }
    }

    /**
     * @param content
     * @param item
     */
    private void updateTableRow (CMPDRecordContent content, TableItem item) {
        item.setText(LOC_PROCESS_NAME,content.processName);
        item.setText(LOC_IDS,content.IDs);
        item.setText(LOC_PROJECT_NAME,content.projectName != null?content.projectName:"");
        item.setText(LOC_COMMAND,content.command);
        IGuihiliCallbackModify g = getConfigurator(fTable.getItemCount()-1);
        g.setProject(lookupProject(content.projectName));
        g.setTargetCPU(content.targetCPU);
        fTable.getParent().layout(true);
        this.setDirty(true);
        this.updateLaunchConfigurationDialog();
    }
    
    interface IGuihiliCallbackModify extends IGuihiliCallback {
        public void setProject(IProject project);
        public void setTargetCPU(String s);
    }
    
    private List<IGuihiliCallbackModify> fConfigurators = new ArrayList<IGuihiliCallbackModify>();
    private String fLaunchName;

    private IPathSelector fileSelector;
    private Button extractInfoButton;
    private Label configFileNotExistLabel;
    private List<String> fLaunchArgs;
    private boolean fSuspendedAtStart;
    
    private static IProject lookupProject(String name){
        IProject project = null;
        if (name != null && name.trim().length() > 0) {
            ICProject cp = CoreModel.getDefault().getCModel().getCProject(name);
            if (cp.exists()) project = cp.getProject();
        }
        return project;
    }
    
    private IGuihiliCallbackModify getConfigurator (int index) {
        
        if (index == fConfigurators.size()) {          
            IGuihiliCallbackModify guihiliCallbackModify = new IGuihiliCallbackModify(){
                private Properties fProps = null;
                private List<String>fArgs = null;
                private IProject _fProject;
                private String fCPU;
                @Override
                public String getLaunchName () {
                    return fLaunchName;
                }

                @Override
                public Properties getProperties () {
                    return fProps;
                }

                @Override
                public List<String> getSwahiliArguments () {
                    return fArgs;
                }
                

                @Override
                public void setProperties (Properties props) {
                    fProps = props;                   
                }

                @Override
                public void setSwahiliArguments (List<String> args) {
                    fArgs = args;                  
                }

                @Override
                public IProject getProject () {
                    return _fProject;
                }

                @Override
                public void setProject (IProject p) {
                    _fProject = p;                   
                }
                
                @Override
                public void setTargetCPU(String s){ fCPU = s; }

                @Override
                public String getTargetCPU () {
                    return fCPU;
                }

                @Override
                public String[] getEnvironment () {
                    if (fLaunchConfig != null){
                        try {
                            return DebugPlugin.getDefault().getLaunchManager().getEnvironment(fLaunchConfig);
                        }
                        catch (CoreException e) {
                            //Ignore
                        }
                    }
                    return null;
                }
                
                @Override
                public int getProcessCount(){
                    return CMPDDebuggerTab.this.getProcessCount();
                }

                @Override
                public File getWorkingDirectory () {
                    if (_fProject != null){
                        return new File(_fProject.getLocation().toOSString());
                    }
                    return new File("."); //shouldn't get here.
                }};
            fConfigurators.add(guihiliCallbackModify);
        }
        else if (index > fConfigurators.size()) {
            throw new IllegalArgumentException("Out-of-order configurators");
        }
        IGuihiliCallbackModify c =  fConfigurators.get(index);
        return c;
    }
    
    private void removeConfigurator(int index){
        fConfigurators.remove(index);
    }
    
    private void showEditProcessDialog(){
        TableItem item[] = fTable.getSelection();
        if (item.length == 0){
            error(fTable.getShell(),"Please select a table row");
            return;
        }
        CMPDRecordContent content = new CMPDRecordContent();
        content.processName = item[0].getText(LOC_PROCESS_NAME);
        content.projectName = item[0].getText(LOC_PROJECT_NAME);
        content.IDs = item[0].getText(LOC_IDS);
        content.command = item[0].getText(LOC_COMMAND);
        
        ArrayList<ProcessIdList> list = new ArrayList<ProcessIdList>();
        for (TableItem it: fTable.getItems())  {
            if (content.IDs == null || !content.IDs.equals(it.getText(LOC_IDS)))
                list.add(ProcessIdList.create(it.getText(LOC_IDS)));
        }
        
        CMPDRecordContent results = getEditDialog().open(content,getConfigurator(fTable.getSelectionIndex()),list);
        if (results != null) {
            fWasCustomized = true;
            updateTableRow(results,item[0]);  
            this.getConfigurator(fTable.getItemCount()-1).setProject(lookupProject(content.projectName));
            this.getConfigurator(fTable.getItemCount()-1).setTargetCPU(content.targetCPU);
        }
    }
    
    private void deleteSelectedProcess(){
        int selection = fTable.getSelectionIndex();
        if (selection >= 0){
            fTable.remove(selection);
            removeConfigurator(selection);
            if (fTable.getItemCount() > selection){
                fTable.setSelection(selection);
            }
            else if (selection > 0 && fTable.getItemCount() > 0){
                fTable.setSelection(selection-1);
            }
            else if (fTable.getItemCount() > 0) {
                fTable.setSelection(0);
            } 
            else {
                this.fEditButton.setEnabled(false);
                this.fDeleteButton.setEnabled(false);
            }
            this.setDirty(true);
            this.updateLaunchConfigurationDialog();
        }
    }
    
    @Override
    public String getName () {
        return "CMPD Debugger Configuration";
    }
    
    private void setCommandOptions(List<String> args){
         for (ILaunchConfigurationTab tab: this.getLaunchConfigurationDialog().getTabs()) {
             if (tab instanceof ICommandOptionsSetter){
                 ((ICommandOptionsSetter)tab).setCommandOptions(args);
             }           
         }
    }
    
    /**
     * These were derived going thru the guihili files and looking at each "arg_action"
     * to figure out the corresponding property corresponding to an argument.
     * <P>
     * Special characters in the property name means this:
     * <dl>
     * <dt> ! as a prefix
     * <dd> property is a boolean that is set to 0 if the argument is present.
     * <dt> , as a prefix
     * <dd> argument values can be specified multiple times and are to be appended
     * to the property value, separated by commas. Toggles use this.
     * <dt># as a suffix.
     * <dd>The argument may be specified multiple times, and the property name is
     * derived by appending the instance number starting at 1(e.g., 1,2,3,4).
     * <dt>= embedded
     * <dd>means that the property name on the left of the = character is to be assigned
     * the value of the string on the right of it.
     * </dl>
     */
    private static String[] argToProp = {
        "-notrace", "!trace_history",
        "-source_path=", "Source_path",
        "-dir_xlation=", "Directory_translation",
        "-toggle=include_local_symbols=", "Local_symbols",
        "-off=download", "dont_download",
        "-on=verify_download", "verify_download",
        "-off=cache_target_memory", "cache_target_mem",
        "-on=read_ro_from_exe", "read_ro_from_exe",
        "-on=prefer_soft_bp", "prefer_sw_bp",
        "-off=prefer_soft_bp", "!prefer_sw_bp",
        "-on=program_zeros_bss", "program_zeros_bss",
        "-initiallog=", "log_filename",
        "-profile", "profiling_window",
        "-noprofile", "!profiling_window",
        "-semint=", "SIDLL#",
        
        
        "-sim", "ARC_target=ARC_simulator", //TOFIX: this only works for ARC!
        "-hard", "ARC_target=ARC_hardware", //TOFIX: this only works for ARC!
        "-xiss", "ARC_target=XISS",
        
        "-a7", "which_arc=ARC7",
        "-arc700", "which_arc=ARC7",
        "-on=exceptions", "A7_exceptions", //TOFIX: arc only
        "-mmu", "A7_mmu",                  //TOFIX: arc only
        "-Xsimd", "A7_simd",
       "-off=recursive_delay_slot", "!A7_recur",
        
        "-a6", "which_arc=ARC6",
        "-arc600", "which_arc=ARC6",
        "-mpu", "A6_mpu",
        
        "-core1", "ARC5_Core_Version=1", //TOFIX: a5 only
        "-core2", "ARC5_Core_Version=2", //TOFIX: a5 only
        "-core3", "ARC5_Core_Version=3", //TOFIX: a5 only
        "-arcver=", "ARC_Core_Version",
        
        "-on=icnts", "ARC_instr_cnt",
        "-on=killeds" ,"ARC_killed_cnt",
        "-DLL=", "ARC_DLL_filename+ARC_use_ARC_DLL=1",       //TOFIX: arc only
        "-DLL", "ARC_DLL_filename=",   //TOFIX: arc only
        "-port=", "ARC_parallel_port_address", //TOFIX: arc only
        "-timeout=", "ARC_Timeout",   //TOFIX: arc only
        "-jtag", "parallel_jtag=1",
        "-on=fujitsu_fast_serial_hostif" , "ARC_fujitsu_if=1",
        "-intpar" , "ARC_fujitsu_if=0",
        "-simextp=", "ARC_ExtDLL#",
        "-on=store_cache_data", "ARC_cache_rams",
        "-Xbs", "ARC_barrel_shifter=1",
        "-Xbarrel_shifter", "ARC_barrel_shifter=1",
        "-Xnobs", "ARC_barrel_shifter=0",
        "-Xmult32", "ARC_mult32=1",
        "-Xnorm", "ARC_norm=1",
        "-Xswap", "ARC_swap=1",
        "-Xmin_max", "ARC_min_max=1",
        "-Xtimer0", "ARC_timer0=1",
        "-Xtimer1", "ARC_timer1=1",
        "-Xmpy", "ARC_mpy=1",
        "-Xxmac_16", "ARC_xmac_support=xmac_16",
        "-Xxmac_24", "ARC_xmac_support=xmac_24",
        "-Xxmac_d16", "ARC_xmac_support=xmac_d16",
        "-Xmul_mac", "ARC_xmac_support=mul_mac",
        "-Xea", "ARC_ea=1",
        "-Xdvbf", "ARC_dvbf=1",
        "-Xcrc", "ARC_crc=1",
        "-Xmul32_16", "ARC_mul32_16=1",      
        
        //MORE TO DO...
              
        "-on=", ",Program_toggles_on",
        "-off=", ",Program_toggles_off",
        
    };
    private Button suspendAtStartButton = null;
    
    private boolean addPropFor (Properties props, String arg) {
        // Special case "icache=x,x,x,x" and "dcache=x,x,x,x"
        if (arg.startsWith("-icache=") || arg.startsWith("-dcache=")) {
            doCacheProperties(props, arg.charAt(1) == 'i', arg.substring(8).split(","));
            return true;
        }
        else if (arg.startsWith("-simextp=termsim,")) {
            doTermsimProperties(props, arg.substring(17).split(","));
            return true;
        }

        for (int i = 0; i < argToProp.length; i += 2) {
            String a = argToProp[i];
            String p = argToProp[i + 1];
            String propName = p;
            String extras = null;
            if (propName.indexOf('+') > 0){
                extras = propName.substring(propName.indexOf('+')+1);
                p = propName.substring(0,propName.indexOf('+'));
                propName = p;
            }
            boolean negate = false;
            boolean append = false;
            if (p.startsWith("!")) {
                negate = true;
                p = p.substring(1);
                propName = p;
            }
            else if (p.startsWith(",")) {
                append = true;
                p = p.substring(1);
                propName = p;
            }

            if (arg.startsWith(a) && (arg.equals(a) || a.endsWith("="))) {
                String value = null;
                if (p.indexOf('=') > 0) {
                    value = p.substring(p.indexOf('=') + 1);
                    propName = p.substring(0, p.indexOf('='));
                }
                else if (arg.equals(a)) {
                    value = negate ? "0" : "1";
                }
                else  {
                    value = arg.substring(a.length());
                }
                if (propName.endsWith("#")) {
                    propName = propName.substring(0, propName.length() - 1);
                    for (int n = 1; n <= 10; n++) {
                        if (props.getProperty(propName + n) == null) {
                            propName = propName + n;
                            break;
                        }
                    }
                }
                if (append) {
                    String oldValue = props.getProperty(propName);
                    if (oldValue != null)
                        value = oldValue + "," + value;
                }
                props.setProperty(propName, value);
                if (extras != null){
                    for (String e: extras.split("\\+")){
                        int index = e.indexOf('=');
                        if (index > 0){
                            props.setProperty(e.substring(0,index), e.substring(index+1));
                        } 
                        else throw new IllegalStateException("Arg table messed up for argToProp[" + 
                            (i+1) + "]=" + argToProp[i+1]);
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    /**
     * Given the icache/dcache property string, translate it back into the
     * guihili properties that generated it.
     * @param prop guihili properties to update.
     * @param icache if true, instruction cache, otherwise data cache.
     * @param args four parameters: size, ways, line_size, and repeat algorithm.
     */
    private void doCacheProperties(Properties props, boolean icache, String[]args){
        String tag = "AC_"+(icache?"i":"d")+"cache";
        props.setProperty(tag,"1");
        tag += "_";
        String[] keys = {"size", "line_size","ways","repalg"};
        for (int i = 0; i < args.length && i < keys.length; i++){
            props.setProperty(tag + keys[i],i != 3?args[i]:args[i].equals("o")?"Round_robin":"Random");
        }    
    }
    
    /**
     * Given the arguments to the -simextp=termsim specification, set the guihili
     * properties appropriately.
     * @param props properties to be updated.
     * @param args term_key=value
     */
    private void doTermsimProperties(Properties props, String args[]){
        props.setProperty("commtermsim","term");
        for (String s: args){
            String keyValue[] = s.split("=");
            if (keyValue.length == 2){
                //prop name and attribute name differ for tcp port:
                if (keyValue[0].equals("term_tcpport")) keyValue[0] = "term_tcp_port";
                props.setProperty(keyValue[0],keyValue[1]);
            }
        }
    }
    
    private Properties createGuihiliPropertiesFromArguments(String[] args){
        Properties props = new Properties();
        List<String>unrecognized = new ArrayList<String>();
        for (String arg: args){
            if (!addPropFor(props,arg)){
                unrecognized.add(arg);
            }
        }
        if (unrecognized.size() > 0){
            props.setProperty("cmd_line_option",StringUtil.listToArgString(unrecognized));
        }
        return props;
    }
    
    private void initializeFrom(ICMPDInfo cmpdInfo){
        ICMPDInfo.IProcess processes[] = cmpdInfo.getProcesses();
        fTable.setItemCount(processes.length);
        int rowCount = 0;
        for (ICMPDInfo.IProcess p: processes){
            TableItem item = fTable.getItem(rowCount);
            item.setText(LOC_PROJECT_NAME,p.getProject()!=null?p.getProject().getName():"");
            item.setText(LOC_PROCESS_NAME,p.getProcessName());
            item.setText(LOC_IDS,""+p.getIDList().getEncoding());
            item.setText(LOC_COMMAND,StringUtil.arrayToArgString(p.getCommand()));
            IGuihiliCallbackModify c = getConfigurator(rowCount);
            Properties props = createGuihiliPropertiesFromArguments(p.getSwahiliArgs());
            Map<String,String> extra = p.getGuihiliProperties();
            if (extra != null){
                for (Map.Entry<String,String> entry: extra.entrySet()){
                    props.put(entry.getKey(),entry.getValue());
                }
            }
            c.setProperties(props);
            c.setProject(p.getProject());
            c.setTargetCPU(PathSelectorFactory.getTargetCPU(p.getCommand()[0], p.getProject()));
            c.setSwahiliArguments(Arrays.asList(p.getSwahiliArgs()));
            rowCount++;
        }
        fLaunchArgs = Arrays.asList(cmpdInfo.getLaunchArgs());
        String cmds[] = cmpdInfo.getStartupCommands();
        if (cmds.length > 0){
             fLaunchArgs = new ArrayList<String>(fLaunchArgs);
             for (String c: cmds){
                 fLaunchArgs.add("-cmd=" + c);
             }
        }
        setCommandOptions(fLaunchArgs);
        if (processes.length > 0)
            fTable.select(0);
        else {
            this.fEditButton.setEnabled(false);
            this.fDeleteButton.setEnabled(false);
        }
        fTable.getParent().layout(true);
        setDirty(true);
        this.updateLaunchConfigurationDialog();
    }
    
    private String canonicalizePath(String path){
        if (path == null) return null;
        if (new File(path).isAbsolute() || fProject == null){
            return path;
        }
        IPath configPath = new Path(path);
        IPath projectPath = fProject.getLocation();
        return projectPath.append(configPath).toOSString();
    }
    
    private String relativizePath(String path, IProject project){
        if (project == null || !new File(path).isAbsolute()) return path;
        IPath pathPath = new Path(path);
        IPath projectPath = project.getLocation();
        if (projectPath.isPrefixOf(pathPath)){
            return pathPath.setDevice(null).removeFirstSegments(projectPath.segmentCount()).toString();
        }
        return path;       
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initializeFrom (ILaunchConfiguration configuration) {
        try {
            fLaunchName = configuration.getName();
            fLaunchConfig = configuration;
            fProjectName = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
            this.fVDKRelativeConfigPath = configuration.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_VDK_CONFIG_FILE,(String)null);
           
            this.setSuspendAtStartup(!configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_RESUME_AT_START, true));
            if (fProjectName != null){
                setProject(ProjectSelectorFactory.lookupProject(fProjectName),false);
            }
            fVDKAbsoluteConfigPath = canonicalizePath(fVDKRelativeConfigPath);
            if (fileSelector != null && fVDKRelativeConfigPath != null) {
                fileSelector.setPath(fVDKRelativeConfigPath);
            }
            int cmpdCount = configuration.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_COUNT, 0);
            fTable.setItemCount(cmpdCount);
            int pid = 1;
            for (int i = 0; i < cmpdCount; i++){
                String projectName = configuration.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_PROJECT_NAME + i,"???");
                String processName = configuration.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_PROCESS_NAME + i,"???");
                String processList = configuration.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_PROCESS_ID_LIST + i,(String)null);
                if (processList == null) {
                    int processInstances = configuration.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_PROCESS_INSTANCE_COUNT + i,0);
                    processList = processInstances > 1? ("" + pid + ":" + (pid+processInstances-1)):""+pid;
                    pid += processInstances;
                }
                String processPath = configuration.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_PROCESS_PATH + i,"???");
                String cpu = configuration.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_TARGET_CPU + i,"ac");
                TableItem item = fTable.getItem(i);
                item.setText(LOC_PROJECT_NAME,projectName);
                item.setText(LOC_PROCESS_NAME,processName);
                item.setText(LOC_IDS,processList);
                item.setText(LOC_COMMAND,processPath);     
                IGuihiliCallbackModify c = getConfigurator(i);
                Map<Object,Object>map = configuration.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_GUIHILI_PROPERTIES + "_" +i, (Map<Object,Object>)null);
                if (map != null){
                    Properties props = new Properties();
                    props.putAll(map);
                    c.setProperties(props);
                }
                else c.setProperties(null);
                c.setProject(lookupProject(projectName));
                c.setTargetCPU(cpu);
                c.setSwahiliArguments(configuration.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_SWAHILI_ARGS+"_"+i, (List<String>)null));
                fWasCustomized = true;
            }
            if (cmpdCount > 0)
                fTable.select(0);
            else {
                this.fEditButton.setEnabled(false);
                this.fDeleteButton.setEnabled(false);
            }
            fTable.getParent().layout(true);
            fLaunchArgs = configuration.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_SWAHILI_ARGS,new ArrayList<String>(0));
        }
        catch (CoreException e) {
            setErrorMessage(e.getMessage());
        }
        if (!fWasCustomized){
            if (fVDKAbsoluteConfigPath != null && new File(fVDKAbsoluteConfigPath).exists()) {
                this.resetFromVDKConfigFile();
            }
        }

    }
    
    static class Range {
        Range(int f, int l) { first = f; last = l; }
        int first;
        int last;
    }
    
    static class RangeList {
        Range[] ranges;
        RangeList(Range[] ranges) { this.ranges = ranges; }
        int getProcessCount(){
            int cnt = 0;
            for (Range r: ranges){
                cnt += r.last-r.first+1;
            }
            return cnt;
        }
    }
    
    private static RangeList computeRanges(String ids) throws NumberFormatException{
        String r[] = ids.split(",");
        Range ranges[] = new Range[r.length];
        int i = 0;
        for (String idr: r){
            if (idr.indexOf(':') > 0) {
                String i2[] = idr.split(":");
                if (i2.length != 2)
                    throw new NumberFormatException("Invalid range: " + idr);
                ranges[i] = new Range(Integer.parseInt(i2[0]), Integer.parseInt(i2[1]));
                if (ranges[i].first < 0 || ranges[i].first > ranges[i].last){
                    throw new NumberFormatException("Invalid range: " + idr);
                }
            }
            else {
                int j = Integer.parseInt(idr);
                ranges[i] = new Range(j,j);
            }
        }
        return new RangeList(ranges);
    }
    
    private int getProcessCount(){
        int count = 0;
        for (int i = 0; i < fTable.getItemCount(); i++){
            count += computeRanges(fTable.getItem(i).getText(LOC_IDS)).getProcessCount();
        }
        return count;
    }

    @Override
    public void performApply (ILaunchConfigurationWorkingCopy configuration) {
        SeeCodeConfigPage.setSpecialMetaWareAttributes(configuration);
        fLaunchConfig = configuration;
        for (int i = 0; i < fTable.getItemCount(); i++){
            TableItem item = fTable.getItem(i);
            String projectName = item.getText(LOC_PROJECT_NAME);
            configuration.setAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_PROJECT_NAME + i,projectName);
            configuration.setAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_PROCESS_NAME + i,item.getText(LOC_PROCESS_NAME));
            configuration.setAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_PROCESS_ID_LIST + i,item.getText(LOC_IDS));
            configuration.setAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_PROCESS_PATH + i,item.getText(LOC_COMMAND));
            
            IGuihiliCallback c = getConfigurator(i);
            Properties props = c.getProperties();
            configuration.setAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_TARGET_CPU + i,c.getTargetCPU());
            configuration.setAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_GUIHILI_PROPERTIES + "_" +i, props);
            configuration.setAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_SWAHILI_ARGS+"_"+i, c.getSwahiliArguments());
        }
        configuration.setAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_COUNT, fTable.getItemCount());
        configuration.setAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_SWAHILI_ARGS,fLaunchArgs);
        configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_RESUME_AT_START,!this.fSuspendedAtStart);
        if (this.fVDKRelativeConfigPath != null)
            configuration.setAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_VDK_CONFIG_FILE,fVDKRelativeConfigPath);
        if (fProject != null) {
            configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, fProject.getName());
            ICProjectDescription projDes = CCorePlugin.getDefault().getProjectDescription(fProject);
            if (projDes != null)
            {
                String buildConfigID = projDes.getActiveConfiguration().getId();
                configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_ID, buildConfigID);          
            }
        }


    }
    


    @Override
    public void setDefaults (ILaunchConfigurationWorkingCopy configuration) {
        SeeCodeConfigPage.setSpecialMetaWareAttributes(configuration);
        ICElement cElement = getContext(configuration, getPlatform(configuration));
        if (cElement != null) {
            initializeCProject(cElement, configuration);
        }
        configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_RESUME_AT_START,true);
        configuration.setAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_COUNT,0);
//        try {
//            fProjectName = configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
//            if (fProjectName != null){
//                fProject = ProjectSelectorFactory.lookupProject(fProjectName);
//                setProject(fProject);
//            }
//            else fProject = null;
//        }
//        catch (CoreException e) {
//            fProjectName = null;
//        }
    }
    
    private void setProject (IProject project, boolean resetConfigFile) {
        if (project != fProject){
            fProject = project;
            setDirty(true);
            this.updateLaunchConfigurationDialog();
            if (fProjectSelector != null){
                fProjectSelector.setProject(project);
            }
            if (fileSelector != null){
                fileSelector.setProject(project);
            }
        }
        fProjectName = project != null ? project.getName() : null;
        if (fileSelector != null) {
            fileSelector.setProjectSelectEnabled(project != null);
        }

        if (project != null && resetConfigFile) {
            IFile[] path = extractConfigFiles(project);
            if (path.length > 0 && path.length > 0 && path[0].exists()) {
                String old = fVDKAbsoluteConfigPath;
                fVDKAbsoluteConfigPath = path[0].getRawLocation().toOSString();
                if (!fVDKAbsoluteConfigPath.equals(old)){
                    fVDKRelativeConfigPath = relativizePath(fVDKAbsoluteConfigPath,fProject);
                    if (fileSelector != null) {
                        fileSelector.setPath(fVDKRelativeConfigPath);
                    }
                    setDirty(true);
                }
                if (!fWasCustomized){
                     if (fTable != null)
                         this.resetFromVDKConfigFile();
                }
            }
        }
        else if (project == null && fVDKAbsoluteConfigPath != null){
            // No project, use same path in relative mode.
            fVDKRelativeConfigPath = fVDKAbsoluteConfigPath;
            if (fileSelector != null){             
                fileSelector.setPath(fVDKAbsoluteConfigPath);
            }
        }
        else if (project == null && fVDKRelativeConfigPath != null) {
            // No project specified; make config path absolute.
            fVDKAbsoluteConfigPath = fVDKRelativeConfigPath;
            if (fileSelector != null){             
                fileSelector.setPath(fVDKAbsoluteConfigPath);
            }
        }
        else if (project != null && fVDKAbsoluteConfigPath != null) {
            fVDKRelativeConfigPath = relativizePath(fVDKAbsoluteConfigPath,project);
            if (fileSelector != null)
                fileSelector.setPath(fVDKRelativeConfigPath);
        }
    }

    private IFile[] extractConfigFiles (IProject project) {
        List<IFile> files = new ArrayList<IFile>();
        extractConfigFiles(project,files);
        return files.toArray(new IFile[files.size()]);
    }
    
    private void extractConfigFiles(IContainer folder, final List<IFile>list){
        try {
            folder.accept(new IResourceVisitor(){

                @Override
                public boolean visit (IResource resource) throws CoreException {
                    if (resource instanceof IFile){
                        if (VDK_CONFIG_PATTERN.matcher(resource.getName()).matches()){
                            list.add((IFile)resource);
                        }
                        return false;
                    }
                    return true;
                }});
        }
        catch (CoreException e) {
            UISeeCodePlugin.log(e);
        }     
    }

}
