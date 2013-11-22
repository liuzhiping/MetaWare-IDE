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


import java.io.IOException;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.envvar.Messages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.arc.cdt.debug.seecode.internal.ui.CMPDDebuggerTab.IGuihiliCallbackModify;
import com.arc.cdt.debug.seecode.internal.ui.PathSelectorFactory.IPathSelector;
import com.arc.cdt.debug.seecode.ui.UISeeCodePlugin;
import com.arc.cdt.debug.seecode.ui.views.IContextHelpIds;
import com.arc.mw.util.StringUtil;
import com.arc.seecode.engine.ProcessIdList;


@SuppressWarnings({ "deprecation", "restriction" })
class EditCmpdProcessDialog extends Dialog {

    private static final String PROCESS_NAME_TOOLTIP = "Enter the name by which this process will be referenced.";

    private static final String INSTANCE_TOOLTIP = "Enter IDs of processes being defined, comma-separated, or as range (e.g., 1,3,5:8,9).";

    private static final String PATH_TOOLTIP = "Enter the path of the executable and its arguments.";

    private CMPDRecordContent result = null;

    private String fPath = null;

    private Text fIDField;

    private Text fProcessNameField;

    private Button fOKButton;

    private Button fSearchProjectButton;
   
    private boolean fSwahiliSet = false;

    private String fBogusProjectName = null;

    private IProject fSelectedProject = null;

    private IPathSelector fPathSelector;
    
    private List<ProcessIdList> fExisting = null;
    
    private boolean fIsNoProject;


    public EditCmpdProcessDialog(Shell parent, boolean isNoProject) {
        super(parent, SWT.APPLICATION_MODAL);
        setText("Add Process");
        fIsNoProject = isNoProject;
    }

    private void setSwahiliSet (boolean v) {
        fSwahiliSet = v;
    }

    /**
     * Return the ICProject corresponding to the project name in the project name text field, or null if the text does
     * not match a project name.
     */
    protected ICProject getCProject () {
        if (fSelectedProject == null)
            return null;
        ICProject p = CoreModel.getDefault().getCModel().getCProject(fSelectedProject.getName());
        if (p.exists())
            return p;
        return null;
    }

    private void updateButtons () {
        boolean validPath = validatePath(fPath,false);
        boolean okEnabled = fPath != null && fPath.trim().length() > 0;
        boolean configEnabled = validPath;
        String idError = fIDField != null?validateIDs(fIDField,fExisting):null;
        if (bogusEntryLabel != null) {
            if (!validPath) {
                bogusEntryLabel.setText("Not a valid executable path");
                bogusEntryLabel.setVisible(true);
            }
            else if (fBogusProjectName != null && fBogusProjectName.trim().length() > 0) {
                bogusEntryLabel.setText("Not a valid project name");
                bogusEntryLabel.setVisible(true);
                okEnabled = false;
            }
            else if (!validateProcessName(fProcessNameField,false)){
                bogusEntryLabel.setText(fProcessNameField.getText() == null || fProcessNameField.getText().trim().length()==0?
                    "Process name required":"Process name must be alphanumeric");
                bogusEntryLabel.setVisible(true);
                okEnabled = false;
            }
            else if (idError != null){
                bogusEntryLabel.setText(idError);
                bogusEntryLabel.setVisible(true);
                fIDField.setFocus();
                okEnabled = false;
            }
            else {
                bogusEntryLabel.setVisible(false);
            }
        }
        if (fConfigButton != null)
            fConfigButton.setEnabled(configEnabled);
        if (fOKButton != null)
            fOKButton.setEnabled(okEnabled);
    }

    private String getTargetCPU () {
        ICProject cp = getCProject();
        IProject project = cp != null ? cp.getProject() : null;
        return PathSelectorFactory.getTargetCPU(fPath, project);
    }

    private transient Button fOKConfigButton;

    private Button fConfigButton;

    private void doConfigure (Shell parent, final IGuihiliCallback config) {
        Cursor busyCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_WAIT);
        Cursor savedCursor = parent.getCursor();
        parent.setCursor(busyCursor);
        final Shell shell;
        try {
            shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
            shell.setLayout(new GridLayout(1, false));
            shell.setText("Configure process");
            shell.setImage(getImage());
            final Label errorStatus = new Label(shell,0);
            errorStatus.setForeground(errorStatus.getDisplay().getSystemColor(SWT.COLOR_RED));
            errorStatus.setLayoutData( new GridData(GridData.FILL_HORIZONTAL) );
            final GuihiliPage gui = new GuihiliPage(new Runnable() {

                @Override
                public void run () {
                    if (fOKConfigButton != null) // Could be called before OK button made!
                        fOKConfigButton.setEnabled(true);

                }
            }, new GuihiliPage.IErrorSetter() {

                @Override
                public void setErrorMessage (String msg) {
                    if (msg == null) msg = "";
                    msg = msg.trim();
                    errorStatus.setText(msg);
                    errorStatus.redraw();
                    if (fOKConfigButton != null && !fOKConfigButton.isDisposed()) {
                        fOKConfigButton.setEnabled(msg.length() == 0);
                        fOKConfigButton.redraw();
                    }
                }
            });
            gui.createControl(shell);
            gui.initializeFrom(config);
            Composite okCancelPanel = new Composite(shell, 0);
            okCancelPanel.setLayout(new GridLayout(2, false));
            okCancelPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            Composite padding = new Composite(okCancelPanel, 0);
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.grabExcessHorizontalSpace = true;
            padding.setLayoutData(gd);

            Composite okCancel = new Composite(okCancelPanel, 0);

            okCancel.setLayout(new GridLayout(2, true));
            fOKConfigButton = new Button(okCancel, SWT.PUSH);
            fOKConfigButton.setText("OK");
            fOKConfigButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            if (errorStatus.getText() != null && errorStatus.getText().length() > 0){
                fOKConfigButton.setEnabled(false);
            }
            fOKConfigButton.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetDefaultSelected (SelectionEvent e) {
                    widgetSelected(e);
                }

                @Override
                public void widgetSelected (SelectionEvent e) {
                    gui.performApply(config);
                    gui.performOK();
                    setSwahiliSet(true);
                    shell.dispose();

                }
            });
            Button cancelButton = new Button(okCancel, SWT.PUSH);
            cancelButton.setText("Cancel");
            cancelButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            cancelButton.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetDefaultSelected (SelectionEvent e) {
                }

                @Override
                public void widgetSelected (SelectionEvent e) {
                    shell.dispose();

                }
            });
            shell.setSize(800, 600); // pack() doesn't work correctly
            centerShell(parent, shell);
            shell.open();
        }
        finally {
            parent.setCursor(savedCursor);
        }

        Display display = parent.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
    }

    /**
     * Iterate through and suck up all of the executable files that we can find.
     */
    private IBinary[] getBinaryFiles (final ICProject cproject) {
        final Display display;
        if (cproject == null || !cproject.exists()) {
            return null;
        }
        if (fShell == null) {
            display = LaunchUIPlugin.getShell().getDisplay();
        }
        else {
            display = fShell.getDisplay();
        }
        final Object[] ret = new Object[1];
        BusyIndicator.showWhile(display, new Runnable() {

            @Override
            public void run () {
                try {
                    ret[0] = cproject.getBinaryContainer().getBinaries();
                }
                catch (CModelException e) {
                    LaunchUIPlugin.errorDialog("Launch UI internal error", e); //$NON-NLS-1$
                }
            }
        });

        return (IBinary[]) ret[0];
    }

    private boolean validateProcessName (Text field, boolean complain) {
        String name = field.getText();
        if (name == null || !name.matches("[A-Za-z]\\w*")) {
            if (complain) {
                error("Process name must be alphanumeric");
                field.selectAll();
                field.setFocus();
            }
            return false;
        }
        return true;
    }

    private Image getImage () {
        return UISeeCodePlugin.getDefault().getDebuggerIcon();
    }
    
    private int computeNextFreeProcessID(List<ProcessIdList> existing) {
        int next = 1;
        for (ProcessIdList list: existing) {
            for (ProcessIdList.Range range: list.getRanges()) {
                if (next <= range.getLast()) next = range.getLast()+1;
            }
        }
        return next;
    }
    

    public CMPDRecordContent open (CMPDRecordContent initContent, final IGuihiliCallbackModify config, final List<ProcessIdList> existing) {
        if (initContent == null)
            setText("Add Process");
        else
            setText("Edit Process");
        fExisting = existing;
        setSwahiliSet(initContent != null);
        Shell parent = getParent();
        fPath = initContent != null ? initContent.command : "";
        fSelectedProject = null;
        bogusEntryLabel = null;
        fConfigButton = null;
        fOKButton = null;
        fIDField = null;
        if (initContent != null && initContent.projectName != null) {
            fSelectedProject = ProjectSelectorFactory.lookupProject(initContent.projectName);
        }
        result = null;
        fShell = new Shell(parent, SWT.DIALOG_TRIM | getStyle());
        fShell.setText(getText());
        fShell.setLayout(new GridLayout(1, false));
        fShell.setImage(getImage());
        PlatformUI.getWorkbench().getHelpSystem().setHelp(fShell,IContextHelpIds.PREFIX + "cmpd_edit");

        Composite backPanel = new Composite(fShell, 0);
        backPanel.setLayout(new GridLayout(1, false));
        backPanel.setLayoutData(new GridData(GridData.FILL_BOTH));

        Group processPanel = new Group(backPanel, SWT.SHADOW_ETCHED_IN);
        processPanel.setLayout(new GridLayout(5, false));

        Label processNameLabel = new Label(processPanel, SWT.LEFT);
        processNameLabel.setText("Process Set Name: ");
        processNameLabel.setToolTipText(PROCESS_NAME_TOOLTIP);

        fProcessNameField = new Text(processPanel, SWT.BORDER);
        fProcessNameField.setText("");
        fProcessNameField.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText (ModifyEvent e) {
                updateButtons();

            }
        });
        fProcessNameField.setData("name","process_name"); // for GUI tester

        GridData gd = new GridData();
        gd.widthHint = 260;
        gd.horizontalSpan = 4;
        if (initContent != null)
            fProcessNameField.setText(initContent.processName);
        fProcessNameField.setLayoutData(gd);
        fProcessNameField.setToolTipText(PROCESS_NAME_TOOLTIP);

        Label instancesLabel = new Label(processPanel, SWT.LEFT);
        instancesLabel.setText("Process IDs: ");
        instancesLabel.setToolTipText(INSTANCE_TOOLTIP);

        fIDField = new Text(processPanel, SWT.BORDER);
        if (initContent != null)
            fIDField.setText("" + initContent.IDs);
        else if (existing != null)
            fIDField.setText(Integer.toString(computeNextFreeProcessID(existing)));
        else fIDField.setText("1");
        fIDField.setToolTipText(INSTANCE_TOOLTIP);
        gd = new GridData();
        gd.horizontalSpan = 4;
        gd.widthHint = 50;
        fIDField.setLayoutData(gd);
        fIDField.setData("name","process_ids");
        fIDField.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText (ModifyEvent e) {
                updateButtons();

            }
        });

        fPathSelector = null; // remove previous instance.
        
        if(!fIsNoProject){
           ProjectSelectorFactory.createProjectSelectionPanel(processPanel, initContent != null ? initContent.projectName
            : null, null, new ProjectSelectorFactory.IProjectChangeListener() {

            @Override
            public void onProjectChange (IProject project) {
                if (fSearchProjectButton != null)
                    fSearchProjectButton.setEnabled(project != null);
                fSelectedProject = project;
                fBogusProjectName = null;
                if (fPathSelector != null) {
                    fPathSelector.setProject(project);
                }
                updateButtons();
            }

            @Override
            public void onBogusProjectName (String projectName) {
                fBogusProjectName = projectName;
                if (fPathSelector != null) {
                    fPathSelector.setProject(null);
                }
                updateButtons();

            }
           }, 5, "cmpdproject");

        }
        
        fPathSelector = PathSelectorFactory.createPathSelectPanel(
            processPanel,
            "Path and arguments: ",
            PATH_TOOLTIP,
            initContent != null ? initContent.command : null,
            "Choose program to execute",
            new PathSelectorFactory.IPathChangeListener() {

                @Override
                public void onPathChange (String path) {
                    fPath = path;
                    updateButtons();
                }
            },
            new PathSelectorFactory.ISelectionContext() {

                @Override
                public Object[] getSearchList () {
                    return getBinaryFiles(getCProject());
                }

                @Override
                public IProject getProject () {
                    return fSelectedProject;
                }

                @Override
                public String[] getFilterExtensions () {
                    return null;
                }

                @Override
                public String[] getFilterNames () {
                    return null;
                }

                @Override
                public String getDefaultSearchLocation () {
                    if (fPath != null)
                        return fPath;
                    if (fSelectedProject != null)
                        return fSelectedProject.getRawLocation().toOSString();
                    return Platform.getLocation().toOSString();
                }
            },
            5,
            "pathargs");
        
        fPathSelector.setProjectSelectEnabled(!fIsNoProject);
        fConfigButton = new Button(processPanel, SWT.PUSH);
        fConfigButton.setText("Configure...");
        fConfigButton.setData("name", "cmpd_config"); // for GUI tester

        fConfigButton.setToolTipText("Configure the debugger on behalf of this process");
        fConfigButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected (SelectionEvent e) {
            }

            @Override
            public void widgetSelected (SelectionEvent e) {
                config.setProject(fSelectedProject);
                config.setTargetCPU(getTargetCPU());
                doConfigure(fShell, config);
            }
        });

        Composite okCancelPanel = new Composite(backPanel, 0);
        okCancelPanel.setLayout(new GridLayout(2, false));
        okCancelPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        bogusEntryLabel = new Label(okCancelPanel, 0);
        bogusEntryLabel.setText("Path is not a recognizable executable");
        bogusEntryLabel.setData("name", "cmpd_error_label"); // for GUI tester
        bogusEntryLabel.setForeground(bogusEntryLabel.getDisplay().getSystemColor(SWT.COLOR_RED));
        bogusEntryLabel.setVisible(false);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.grabExcessHorizontalSpace = true;
        bogusEntryLabel.setLayoutData(gd);

        Composite okCancel = new Composite(okCancelPanel, 0);

        okCancel.setLayout(new GridLayout(2, true));
        fOKButton = new Button(okCancel, SWT.PUSH);
        fOKButton.setText("OK");
        fOKButton.setData("name", "cmpd_OK"); // for GUI tester
        fOKButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fOKButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected (SelectionEvent e) {
                if (validateProcessName(fProcessNameField, true) && validatePath(fPath, true)) {
                    String err = validateIDs(fIDField, existing);
                    if (err != null) {
                        error(err);
                    }
                    else {
                        result = new CMPDRecordContent();
                        result.processName = fProcessNameField.getText();
                        result.projectName = fSelectedProject != null ? fSelectedProject.getName() : null;
                        result.IDs = fIDField.getText();
                        result.command = fPath;
                        result.targetCPU = getTargetCPU();
                        if (!fSwahiliSet) {
                            setGuihiliDefaults(config);
                        }
                        fShell.dispose();
                    }
                }
            }

            @Override
            public void widgetSelected (SelectionEvent e) {
                this.widgetDefaultSelected(e);

            }
        });
        updateButtons();

        Button cancelButton = new Button(okCancel, SWT.PUSH);
        cancelButton.setText("Cancel");
        cancelButton.setData("name", "cmpd_cancel"); // for GUI tester.
        cancelButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        cancelButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetDefaultSelected (SelectionEvent e) {
            }

            @Override
            public void widgetSelected (SelectionEvent e) {
                result = null;
                fShell.dispose();

            }
        });

        fShell.pack();
        centerShell(parent, fShell);

        fShell.open();

        Display display = parent.getDisplay();
        while (!fShell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        fOKButton = null; // in case we open it again and have a forward-referencing listener
        return result;
    }

    private void centerShell (Shell parent, Shell shell) {
        Rectangle parentRect = parent.getBounds();
        Point size = fShell.getSize();
        shell.setLocation(parentRect.x + (parentRect.width - size.x) / 2, parentRect.y +
            (parentRect.height - size.y) /
            2);
    }

    private void setGuihiliDefaults (IGuihiliCallbackModify config) {
        config.setProject(fSelectedProject);
        config.setTargetCPU(getTargetCPU());

        final GuihiliPage gui = new GuihiliPage(new Runnable() {

            @Override
            public void run () {
                if (fOKConfigButton != null)
                    fOKConfigButton.setEnabled(true);

            }
        }, new GuihiliPage.IErrorSetter() {

            @Override
            public void setErrorMessage (String msg) {
                //Shouldn't happen because its restoring previously set values.
                //if (msg != null)
                //    error(msg);
            }
        });
        gui.initializeFrom(config);
        gui.performApply(config);
    }

    private transient Shell fShell;

    private Label bogusEntryLabel;

    // Validation process IDs and return error message if not ok.
    private String validateIDs (Text field, List<ProcessIdList>existing) {
        String encoding = field.getText();
        if (encoding != null) {
            try {
                ProcessIdList list = ProcessIdList.create(encoding);
                if (existing != null){
                    for (ProcessIdList l: existing){
                        if (l.doesOverlap(list)){
                            return "Process ID list \"" + encoding + "\" overlaps another one (\"" + l.getEncoding()+  "\")";
                        }
                    }
                }
            }
            catch (NumberFormatException e) {
                return "Invalid process ID list: " + encoding + " (" + e.getMessage() + ")";
            }
            return null;
        }
        return "Need to enter process ID(s)";
    }

    private void error (String msg) {
        MessageBox msgBox = new MessageBox(fShell, SWT.OK | SWT.ICON_ERROR);
        msgBox.setMessage(msg);
        msgBox.setText("Input error");
        msgBox.open();
    }

    private static String getCommandFromCommandLine (String commandLine) {
        if (commandLine == null)
            return null;
        String command[] = StringUtil.stringToArray(commandLine);
        return command.length > 0 ? command[0] : null;
    }

    private boolean validatePath (String path, boolean complain) {
        String command = getCommandFromCommandLine(path);
        if (command != null) {
            IPath exePath = new Path(command);
            ICProject cp = getCProject();
            IProject project = cp != null ? cp.getProject() : null;
            if (project != null && !exePath.isAbsolute()) {
                if (!project.getFile(command).exists()) {
                    if (complain) {
                        error(Messages.getString("CMainTab.Program_does_not_exist")); //$NON-NLS-1$
                        fPathSelector.setFocus();
                    }
                    return false;
                }
                exePath = project.getFile(command).getLocation();
            }
            else {
                if (!exePath.toFile().exists()) {
                    if (complain) {
                        error(Messages.getString("CMainTab.Program_does_not_exist")); //$NON-NLS-1$
                        fPathSelector.setFocus();
                    }
                    return false;
                }
            }
            try {
                if (!isBinary(project, exePath)) {
                    if (complain) {
                        error(Messages.getString("CMainTab.Program_is_not_a_recongnized_executable")); //$NON-NLS-1$
                        fPathSelector.setFocus();
                    }
                    return false;
                }
            }
            catch (CoreException e) {
                if (complain) {
                    LaunchUIPlugin.log(e);
                    error(e.getLocalizedMessage());
                    fPathSelector.setFocus();
                }
                return false;
            }
            return true;
        }
        else if (complain)
            error("Command path not specified");
        if (complain) {
            fPathSelector.setFocus();
        }
        return false;
    }

    /**
     * @param project
     * @param exePath
     * @return
     * @throws CoreException
     */
    private boolean isBinary (IProject project, IPath exePath) throws CoreException {
        if (project != null) {
            ICExtensionReference[] parserRef = CCorePlugin.getDefault().getBinaryParserExtensions(project);
            for (int i = 0; i < parserRef.length; i++) {
                try {
                    IBinaryParser parser = (IBinaryParser) parserRef[i].createExtension();
                    IBinaryObject exe = (IBinaryObject) parser.getBinary(exePath);
                    if (exe != null) {
                        return true;
                    }
                }
                catch (ClassCastException e) {
                }
                catch (IOException e) {
                }
            }
        }
        IBinaryParser parser = CCorePlugin.getDefault().getDefaultBinaryParser();
        try {
            IBinaryObject exe = (IBinaryObject) parser.getBinary(exePath);
            return exe != null;
        }
        catch (ClassCastException e) {
        }
        catch (IOException e) {
        }
        return false;
    }
}
