/*
 * UIArcTestCaseSWT
 * $Revision$
 * $Date$
 *
 * CONFIDENTIAL AND PROPRIETARY INFORMATION.
 * Copyright 2008 ARC International (Unpublished).
 * All Rights Reserved.
 * This document, material and/or software contains confidential and
 * proprietary information of ARC International and is protected by copyright,
 * trade secret and other state, federal, and international laws, and may be
 * embodied in patents issued or pending. Its receipt or possession does not
 * convey any rights to use, reproduce, disclose its contents, or to
 * manufacture, or sell anything it may describe.  Reverse engineering is
 * prohibited, and reproduction, disclosure or use without specific written
 * authorization of ARC International is strictly forbidden.  ARC and the ARC
 * logotype are trademarks of ARC International.
 */
package com.arc.cdt.testutil;

import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;
import junit.framework.TestResult;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.TextConsole;

import com.arc.cdt.tests.plugin.TestsPlugin;
import com.arc.tests.database.DatabaseStuff;
import com.arc.widgets.internal.swt.TextColumn;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.WaitTimedOutException;
import com.windowtester.runtime.WidgetNotFoundException;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.condition.ICondition;
import com.windowtester.runtime.condition.IConditionMonitor;
import com.windowtester.runtime.condition.IHandler;
import com.windowtester.runtime.condition.IsEnabledCondition;
import com.windowtester.runtime.locator.ILocator;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.locator.IWidgetReference;
import com.windowtester.runtime.locator.XYLocator;
import com.windowtester.runtime.swt.UITestCaseSWT;
import com.windowtester.runtime.swt.condition.eclipse.PerspectiveActiveCondition;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.CTabItemLocator;
import com.windowtester.runtime.swt.locator.ComboItemLocator;
import com.windowtester.runtime.swt.locator.LabeledLocator;
import com.windowtester.runtime.swt.locator.MenuItemLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
import com.windowtester.runtime.swt.locator.TabItemLocator;
import com.windowtester.runtime.swt.locator.TableItemLocator;
import com.windowtester.runtime.swt.locator.TreeItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ContributedToolItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.PullDownMenuItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;

/**
 * Common base class for all our tests.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class UIArcTestCaseSWT extends UITestCaseSWT {
    
    /**
     * The name of the drop down menu item to open the Debug Configuration dialog.
     */
    protected static final String DEBUG_CONFIG_SELECTION_NAME = isEclipse3_4()?"Debug Configurations...":"Open Debug Dialog...";
    /**
     * The title used for the Run configuration dialog.
     */
    protected static final String RUN_CONFIG_DIALOG_TITLE = isEclipse3_4()?"Run Configurations":"Run";
    /**
     * The title used for the Launch configuration dialog.
     */
    protected static final String DEBUG_CONFIG_DIALOG_TITLE = isEclipse3_4() || isEclipse3_6()?"Debug Configurations":"Debug";
    
    /**
     * The name of the drop down menu item to open the Run Configuration dialog.
     */
    protected static final String RUN_CONFIG_SELECTION_NAME = isEclipse3_4()?"Run Configurations...":"Open Run Dialog...";
    // Categories:
    protected static final String DEBUGGER_INTEGRATION = "Debugger Integration";
    protected static final String INSTALLATION = "Installation";
    protected static final String PROJECT_MANAGEMENT = "Project Management";
    protected static final String BUILD_MANAGEMENT = "Build Management";
    protected static final String HELP_SYSTEM = "Help System";
    protected static final String GENERIC_CDT_TESTS = "Generic CDT Tests";
    protected static final String MANUAL = "Tests to be performed manually";
   
    /**
     * Default timeout in milliesconds for a build to complete.
     */
    protected static final int BUILD_TIMEOUT = 30000;
    
    protected static final String DISASM = "disasm"; // SeeCode disassembly ID
    protected static final String GLOBAL_VARS = "globals";  // SeeCode global variables display ID
    protected static final String FUNCS = "funcs";  // SeeCode ID for Functions display
    
    private static final String CONFIRM_PERSPECTIVE_SWITCH = "Confirm Perspective Switch";
    
    /**
     * Subdirectory where baseline snapshot state files are stored.
     */
    protected static final String BASELINE_DIRECTORY = "baselineSnapshots";
    /**
     * Subdirectory where test output is located.
     */
    protected static final String TEST_DIRECTORY = "testSnapshots";
    /**
     * View IDs that we need to be able to reference.
     */
    protected static final String DISASM_VIEW_ID = "com.arc.cdt.debug.seecode.ui.views.disasm";
    protected static final String DEBUG_VIEW_ID = EclipseUtil.DEBUG_VIEW_ID;
    protected static final String REGISTER_VIEW_ID = "org.eclipse.cdt.debug.ui.RegisterView";
    protected static final String COMMAND_VIEW_ID = "com.arc.cdt.debug.seecode.ui.command";
    protected static final String PROBLEM_VIEW_ID = "org.eclipse.ui.views.ProblemView";
    protected static final String PROJECT_VIEW_ID = "org.eclipse.ui.navigator.ProjectExplorer";
    protected static final String CONSOLE_VIEW_ID = "org.eclipse.ui.console.ConsoleView";
    protected static final String MEMORY_VIEW_ID = "org.eclipse.debug.ui.MemoryView";
    protected static final String SEECODE_VIEW_ID = "com.arc.cdt.debug.seecode.ui.views.SeeCodeCustomView";
    protected static final String OUTLINE_VIEW_ID = "org.eclipse.ui.views.ContentOutline";
    protected static final String TERMINAL_VIEW_ID = "org.eclipse.tm.arc.terminal.view.TerminalView";

    /**
     * Debug view identifier (value <code>"org.eclipse.debug.ui.DebugView"</code>).
     */
    protected static final String LAUNCH_VIEW_ID = "org.eclipse.debug.ui.DebugView"; //$NON-NLS-1$
    
    /**
     * Breakpoint view identifier (value <code>"org.eclipse.debug.ui.BreakpointView"</code>).
     */
    protected static final String BREAKPOINT_VIEW_ID= "org.eclipse.debug.ui.BreakpointView"; //$NON-NLS-1$
    
    /**
     * Variable view identifier (value <code>"org.eclipse.debug.ui.VariableView"</code>).
     */
    protected static final String VARIABLE_VIEW_ID = "org.eclipse.debug.ui.VariableView"; //$NON-NLS-1$
    
    /**
     * Expression view identifier (value <code>"org.eclipse.debug.ui.ExpressionView"</code>).
     * @since 2.0
     */
    protected static final String EXPRESSION_VIEW_ID = "org.eclipse.debug.ui.ExpressionView"; //$NON-NLS-1$
        
   

    /**
     * Map between a view ID and the corresponding label in the Open View dialog
     * table.
     */
    private static Map<String,String> viewIdToLabelMap = new HashMap<String,String>();
    static {
        viewIdToLabelMap.put(DISASM_VIEW_ID, "Debug/Disassembly (MetaWare)");
        viewIdToLabelMap.put(REGISTER_VIEW_ID,"Debug/Registers (Tabular)");
        viewIdToLabelMap.put(COMMAND_VIEW_ID,"Debug/Debugger Commands");
        viewIdToLabelMap.put(CONSOLE_VIEW_ID, "General/Console");
        viewIdToLabelMap.put(MEMORY_VIEW_ID, "Debug/Memory");
        viewIdToLabelMap.put(EXPRESSION_VIEW_ID, "Debug/Expressions");
        // add more as we need them...
    }
    
    public UIArcTestCaseSWT() {
        super();
    }
    

    /**
     * Return the path of the workspace
     * @return the path of the workspace
     */
    protected File getWorkspace(){
        return new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString());
    }
    /**
     * Return the content of the Console display that happens to be showing.
     * @return the contents of the Console display as a string.
     * @throws WidgetSearchException if the console view couldn't be located, or if we couldn't extract its contents.
     */
    protected  String getConsoleContent () throws WidgetSearchException {
        IUIContext ui = getUI();
        showView(CONSOLE_VIEW_ID);
        IWidgetLocator editorLocator = ui.find(new SWTWidgetLocator(StyledText.class, new ViewLocator(
            CONSOLE_VIEW_ID)));
        if (editorLocator != null) {
            final StyledText editor = (StyledText) ((IWidgetReference) editorLocator).getWidget();
            if (editor != null) {
                final String result[] = new String[1];
                editor.getDisplay().syncExec(new Runnable() {

                    @Override
                    public void run () {
                        result[0] = editor.getText();
                    }
                });
                return result[0];
            }
        }
        throw new WidgetSearchException("Cannot locate console");
    }
    
    /**
     * Return the contents of the build console, by first making it show, if it isn't already.
     * @return the contents of the build console.
     * @throws WidgetSearchException 
     */
    protected String getBuildConsoleContent() throws WidgetSearchException{
        IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
        for (int i = 0; i < consoles.length; i++) {
            if (consoles[i].getName().indexOf("C-Build") >= 0) {
            	if (consoles[i] instanceof TextConsole){
            		return ((TextConsole)consoles[i]).getDocument().get();
            	}
                ConsolePlugin.getDefault().getConsoleManager().showConsoleView(consoles[i]);
                getUI().wait(milliseconds(500)); // not sure this necessary, but...
                return getConsoleContent();
            }
        }
        throw new WidgetSearchException("Can't find build console");       
    }
    
    /**
     * Return the contents of the console that displays stdout of the application being debugged,
     *  by first making it show, if it isn't already.
     * @return the contents of the application's stdout console.
     * @throws WidgetSearchException 
     */
    protected String getApplicationConsoleContent() throws WidgetSearchException{
		IUIContext ui = getUI();
		ui.click(new MenuItemLocator("Window/Show View/Console"));
        IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
        for (int i = 0; i < consoles.length; i++) {
            if (consoles[i].getName().indexOf("Application]") >= 0 &&
                consoles[i].getName().indexOf("Debugger Engine") < 0) {
            	if (consoles[i] instanceof TextConsole){
            		return ((TextConsole)consoles[i]).getDocument().get();
            	}
                ConsolePlugin.getDefault().getConsoleManager().showConsoleView(consoles[i]);
                return getConsoleContent();
            }
        }
        for (IConsole console: consoles){
        	System.out.println("Console: " + console.getName());
        }
        throw new WidgetSearchException("Can't find application console");       
    }
    
    /**
     * Return the contents of the console that displays debugger output.
     * @return the contents of the debugger's console.
     * @throws WidgetSearchException 
     */
    protected String getDebuggerConsoleContent() throws WidgetSearchException{
//        IUIContext ui = getUI();
//        ui.click(new XYLocator(new WidgetReference<Object>(ui.getActiveWindow()),400,40)); // Hack to get around Windows Bug
//        ui.click(new MenuItemLocator("Window/Show View/Console"));
        IConsole[] consoles = ConsolePlugin.getDefault().getConsoleManager().getConsoles();
        for (int i = 0; i < consoles.length; i++) {
            if (consoles[i].getName().indexOf("Application]") > 0 &&
                consoles[i].getName().indexOf("Debugger Engine") >= 0) {
            	if (consoles[i] instanceof TextConsole){
            		return ((TextConsole)consoles[i]).getDocument().get();
            	}
                ConsolePlugin.getDefault().getConsoleManager().showConsoleView(consoles[i]);
                return getConsoleContent();
            }
        }
        throw new WidgetSearchException("Can't find application console");       
    }

    /**
     * A project name may be suffixed with subversion stuff (e.g. "[Tools/Teja5...]"). Compute a perl pattern that will
     * take that into account.
     * <P>
     * We might also be clicking something within a project, so that
     * "projectName" could be a path. Take that into account.
     * @param projectName name of project.
     * @return a regular-expression pattern that will match the project if subversions stuff exists.
     */
    protected static  String computeProjectNameString (String projectName) {
    	String segments[] = projectName.split("/");
    	segments[0] += "(\\s*\\[.*\\])?";
    	if (segments.length == 1) {
    		return segments[0];
    	}
    	StringBuilder b = new StringBuilder();
    	b.append(segments[0]);
    	for (int i = 1; i < segments.length; i++){
    		b.append('/');
    		b.append(segments[i]);
    		b.append("(\\s\\d+.*)?"); //source revision
    	}
    	return b.toString();
    }
    
    /**
     * Due to a bug in WindowTester, we cannot locate a tree item if
     * there is more than one tree showing. We must explicit disambiguate
     * the multiple trees.
     * @param itemName the name of the tree item.
     * @return a locator that will find the tree item, even if there is more
     * than one tree showing.
     * @throws WidgetSearchException 
     */
    protected  IWidgetLocator computeTreeItemLocator(String path) throws WidgetSearchException{
        return EclipseUtil.computeTreeItemLocator(getUI(),path);
    }

    /**
     * Given a project, sets its builder to "internal" or "external".
     * @param projectName the name of the project.
     * @param internal if true, set to internal builder, otherwise set to external builder.
     * @throws WidgetSearchException
     */
    protected  void setBuilder (String projectName, boolean internal) throws WidgetSearchException {
        IUIContext ui = getUI();
        ui.contextClick(new TreeItemLocator(computeProjectNameString(projectName), new ViewLocator(
            PROJECT_VIEW_ID)), "Properties");
        ui.wait(new ShellShowingCondition("Properties for " + projectName));
        // <BUG> WindowTester bug: following fails if we simply click
        // on "C/C++ Build" if "Settings" happens to be showing. It cannot
        // find the tree because it cannot disambiguate two trees that are visible.
        // Thus, we force find all trees, and choose the right one as a parent.
        
        IWidgetLocator treeItemLocator = this.computeTreeItemLocator("C\\/C\\+\\+ Build");
        // </BUG>
        //  IWidgetLocator treeItemLocator = new TreeItemLocator("C\\/C\\+\\+ Build");
        ui.click(treeItemLocator);       
        ui.click(new ComboItemLocator(internal ? "Internal builder" : "External builder", new SWTWidgetLocator(
            Combo.class, new SWTWidgetLocator(Group.class, "Builder"))));
        ui.click(new ButtonLocator("&Apply"));
        ui.click(new ButtonLocator("OK"));
        ui.wait(new ShellDisposedCondition("Properties for " + projectName));
    }
    
    protected static boolean isLinux(){
    	return EclipseUtil.isLinux();
    }
    
    protected static boolean isWindows(){
    	return System.getProperty("osgi.os").startsWith("win");
    }
    
    protected static final String NEWLINE = isWindows()?"\r\n":"\n";


    /**
     * Bring up the properties dialog on behalf of a project, execute some code,
     * then dispose of the properties dialog.
     * @param projectName the name of the project.
     * @param runner code to run while properties dialog is up.
     * @throws WidgetSearchException
     */
    protected  void bringUpPropertiesDialog (String projectName,
        IUIRunnable runner) throws Exception {
        IUIContext ui = getUI();
        this.rightClickProjectMenu(projectName, "Properties");
        int slash = projectName.lastIndexOf('/');
        String resource = projectName;
        if (slash > 0) {
        	resource = projectName.substring(slash+1);
        }
        
        ui.wait(new ShellShowingCondition("Properties for " + resource));
        EclipseUtil.setActiveShellSize(ui, 1100, 900); // so as to be a decent size
        
        runner.run(ui);
        ShellDisposedCondition shellDisposedCondition = new ShellDisposedCondition("Properties for " + resource);
        if (!shellDisposedCondition.test()){
            // Some pages have no "Apply" button (e.g., "File Types").
            IWidgetLocator all[] = ui.findAll(new ButtonLocator("&Apply"));
            if (all.length > 0) {
                ui.click(all[0]);
                ui.wait(new ShellDisposedCondition("User Operation.*"));
            }
            all = ui.findAll(new ButtonLocator("OK"));
            if (all.length > 0) // May have already been dismissed
                ui.click(all[0]);
            ui.wait(shellDisposedCondition);
        }
        
       
		
    }
    
    /**
     * Bring up the Properties dialog and set the focus on the Paths and Symbols page.
     * @param projectName the name of the project.
     * @param runner code to run while properties dialog is up.
     * @throws WidgetSearchException 
     */
    protected void bringUpPropertiesDialogPathsAndSymbols(String projectName, final IUIRunnable runner) throws Exception{
        bringUpPropertiesDialog(projectName,new IUIRunnable(){

            @Override
            public void run (IUIContext ui) throws Exception {
                ui.click(EclipseUtil.computeTreeItemLocator(ui,"C\\/C++ General/Paths and [Ss]ymbols"));
                runner.run(ui);
                
            }});
    }
    
    /**
     * Bring up the Properties dialog set to the Settings page.
     * @param projectName the name of the project.
     * @param runner code to run while properties dialog is up.
     * @throws WidgetSearchException 
     */
    protected void bringUpBuildSettings(String projectName, final IUIToolOptionSetter runner) throws Exception{
        bringUpPropertiesDialog(projectName,new IUIRunnable(){

            @Override
            public void run (IUIContext ui) throws Exception {
                ui.click(EclipseUtil.computeTreeItemLocator(ui,"C\\/C++ Build/Settings"));
                IToolOptionSetting toolSetting = EclipseUtil.makeToolOptionsSettings(ui);
                
                runner.run(ui,toolSetting);
                
            }});
    }
    
    /**
     * Bring up the Properties dialog and set the focus on the Discover Options page.
     * @param projectName the name of the project.
     * @param runner code to run while properties dialog is up.
     * @throws WidgetSearchException 
     */
    protected void bringUpPropertiesDialogDiscoverOptions(String projectName, final IUIRunnable runner) throws Exception{
        bringUpPropertiesDialog(projectName,new IUIRunnable(){

            @Override
            public void run (IUIContext ui) throws Exception {
                ui.click(computeTreeItemLocator("C\\/C++ Build/Discovery [oO]ptions"));
                runner.run(ui);
                
            }});
    }
    
    /**
     * Bring up Preprocessor settings dialog and enter values.
     * @param projectName the name of the project.
     * @param runner code to run while preprocessor dialog is up.
     */
    protected void enterPreprocessorValues(String projectName, final String[]values) throws Exception {
        this.bringUpBuildSettings(projectName, new IUIToolOptionSetter(){
            @Override
            public void run(IUIContext ui, IToolOptionSetting setter) throws WidgetSearchException{
                CDTUtil.setCompilerOption(setter,"Preprocessor Defines (one per line)",values);
        }});
    }
    
    /**
     * Bring up Preprocessor settings dialog and enter values.
     * @param projectName the name of the project.
     * @param runner code to run while preprocessor dialog is up.
     */
    protected void enterPreprocessorValue(String projectName, String value) throws Exception {
        enterPreprocessorValues(projectName,new String[]{value});
    }
    
    /**
     * Remove the first preprocessor entry.
     * @param projectName associated project.
     * @throws WidgetSearchException
     */
    protected void removePreprocessorValue(String projectName) throws Exception
    {
        enterPreprocessorValues(projectName,new String[0]);
    }
    
    /**
     * Invoke "Build" on behalf of a project.
     * @param project the name of the project.
     * @param timeout the timeout value for when the build completes.
     * @throws WidgetSearchException 
     */
    protected boolean buildProject(String project,int timeout) throws WidgetSearchException{
        // NOTE must be in C/C++ perspective
        EclipseUtil.prepareToBuild(project);
        rightClickProjectMenu(project,"Build Project");
        boolean result = EclipseUtil.waitUntilBuildCompleted(project,timeout);
        EclipseUtil.checkForProgressBarDisposal(getUI(),"Build Project");
        return result;
    }
    
    protected boolean buildProject(String project) throws WidgetSearchException {
        return buildProject(project,BUILD_TIMEOUT);
    }

    protected void rightClickProjectMenu (String project, String menuItem) throws WidgetSearchException {
     // NOTE must be in C/C++ perspective
    	
        IUIContext ui = getUI();
        String name = computeProjectNameString(project);
        IWidgetLocator treeItemLocator = new TreeItemLocator(name, new ViewLocator(PROJECT_VIEW_ID));
        treeItemLocator = ui.find(treeItemLocator); 
        //<LINUX hack> Under Linux GTK, right-click menu won't materialize
        //  unless the target tree is selected multiple times.
        if (isLinux()){
        	EclipseUtil.activateEclipseWindow();  // Linux often loses active window!
        	// Linux right-click stuff doesn't work unless
        	// we insert delays -- at least under VNC.
            ui.click(treeItemLocator);
            EclipseUtil.fixProjectView(ui);
            ui.wait(milliseconds(250));
            ui.click(1,treeItemLocator,WT.BUTTON3);
            ui.wait(milliseconds(250));
        }
        //</LINUX>
        
        try {
            ui.contextClick(treeItemLocator, menuItem);
        }
        catch(RuntimeException x){
        	// A raw RuntimeException is thrown if the context menu doesn't pop up.
        	// Try clicking it again.
        	EclipseUtil.fixProjectView(ui);
        	try {
        		ui.contextClick(treeItemLocator,menuItem);
        	}
        	catch (RuntimeException xx){
        		if (menuItem.equals("Properties")){
        			ui.keyClick(WT.ALT,WT.CR);
        		}
        		else throw xx;
        	}
        }
    }
    
    /**
     * Invoke "Clean" on behalf of a project.
     * @param project the name of the project.
     * @param timeout timeout to wait for completion.
     * @return true if completed; false if timeout occurred.
     * @throws WidgetSearchException 
     */
    protected boolean cleanProject(String project, int timeout) throws WidgetSearchException{
        //EclipseUtil.prepareToBuild(project);  // Clean doesn't fire build events!
        rightClickProjectMenu(project,"Clean Project");
        //return EclipseUtil.waitUntilBuildCompleted(project,timeout);
        EclipseUtil.waitForEclipseFrameToBeActive(getUI());
        return true;
    }
    
    /**
     * Invoke "Clean" on behalf of a project.
     * @param project the name of the project.
     * @return true if completed; false if timeout occurred.
     * @throws WidgetSearchException 
     */
    protected boolean cleanProject(String project) throws WidgetSearchException{
        return cleanProject(project,BUILD_TIMEOUT);
    }
    
    /**
     * Write string to a file in the directory above the workspace.
     * Used to say, write state of things that are about to fail for some reason
     * so that they can be investigated.
     * @param filename name of file (e.g., "T_2_6.txt").
     * @param string the thing to write to the file.
     * @throws AssertionFailureError if file cannot be written.
     */
    protected void writeStringToFile (String filename, String string)  {
        try {
            //Write bad console to file so that we can investigate failure.
            File path = getWorkspace().getParentFile();
            File f_out = new File(path, filename);
            FileWriter w_out = new FileWriter(f_out);
            w_out.write(string);
            w_out.close();
        }
        catch (IOException e) {
            Assert.assertTrue(false);
        }
    }
    
   // private static Set<IUIContext> contextsInWhichDebugPerspectiveMonitorIsSet = new HashSet<IUIContext>();
    private static Set<IUIContext> contextsInWhichSVNPromptIsSet = new HashSet<IUIContext>();
    private static Set<IUIContext> contextsInWhichCleanProjectIsSet = new HashSet<IUIContext>();
    
    /**
     * Arrange things so that if the "Confirm Perspective Switch" dialog ever pops up, select "Yes".
     *  // NOTE: this doesn't work! when handler is invoked
        // the Confirm Perspective Switch dialog isn't yet the "active" one.
     */
    protected void dealWithDebugPerspectiveConfirmation(boolean expectPerspectiveChange)
			throws WidgetSearchException {
        IUIContext ui = getUI();
		ICondition perspective = PerspectiveActiveCondition.forName("Debug");
		ICondition errorInWorkspaceDialog = new ShellShowingCondition("Errors in Workspace");
		ICondition launchDialog = new ShellShowingCondition("Launching.*");
		boolean errorSeen = false;
		if (expectPerspectiveChange && !perspective.test()) {

			try {
				ICondition confirmDialog = new ShellShowingCondition(
						CONFIRM_PERSPECTIVE_SWITCH);				
				long limit = System.currentTimeMillis() + 60000;
				while (!confirmDialog.test() && !perspective.test()) {
				    // Some projects don't build, but that's okay.
				    if (errorInWorkspaceDialog.test()){
				        errorSeen = true;
				        ui.click(new ButtonLocator("Yes"));
				    }
					if (System.currentTimeMillis() > limit && !launchDialog.test()) {
						throw new WidgetSearchException(
								"Failed to switch perspective");
					}
					ui.wait(milliseconds(500));
				}
				if (confirmDialog.test()) {

					// <BUG> But in eclipse: the Conform Perspective Switch
					// dialog doesn't have focus, so is not "active shell".
					// force it.
					IWidgetLocator shellLocator = EclipseUtil
							.findShell(CONFIRM_PERSPECTIVE_SWITCH);
					if (shellLocator != null) {
						//ui.ensureThat(shellLocator.hasFocus());
					    EclipseUtil.setFocus((IWidgetReference)shellLocator);
					}
					// </BUG>
					ui.click(new ButtonLocator("&Yes"));
				}
			} catch (WaitTimedOutException t) {
				// Ignore, we don't care if this popup doesn't occur.
			} catch (WidgetSearchException t) {
				t.printStackTrace();
				throw t;
			}
		}
		ICondition licenseFailureCondition = getLicenseFailureDialogCondition(true);
		if (!errorSeen) {
		    long limit = System.currentTimeMillis() + 10000;
		    while (System.currentTimeMillis() < limit) {
		        if (EclipseUtil.isLaunchActive())
		            break;
		         if (licenseFailureCondition.test())
		        	 break;
		        if (errorInWorkspaceDialog.test()){
		        	IWidgetLocator loc[] = ui.findAll(new ButtonLocator("Yes"));
		        	if (loc.length > 0) {
		        		// Another dialog may be hiding the error dialog.
		        		// So we can only click if it "Yes" button is visible.
		                ui.click(loc[0]);
		                errorSeen = true;
		                break;
		        	}
		        }
		    }
		}
		
		while (launchDialog.test() && !licenseFailureCondition.test()){
		    if (!errorSeen && errorInWorkspaceDialog.test()){
		        ui.click(new ButtonLocator("Yes"));
		        errorSeen = true;
		    }
		    ui.wait(milliseconds(300));
		}
	}


	protected ICondition getLicenseFailureDialogCondition(boolean showing) {
		String title = "MetaWare Debugger Licensing Failure";
		return showing?new ShellShowingCondition(title):new ShellDisposedCondition(title);
	}
    /**
	 * Arrange things so that if the "Confirm Perspective Switch" dialog ever
	 * pops up, select "Yes". // NOTE: this doesn't work! when handler is
	 * invoked // the Confirm Perspective Switch dialog isn't yet the "active"
	 * one.
	 */
    protected void registerPerspectiveConfirmationHandler () {
    	//Condition monitors cause too much overhead. We must explicitly
    	// check for the condition.
//        if (contextsInWhichDebugPerspectiveMonitorIsSet.add(getUI())) {
//            IConditionMonitor monitor = (IConditionMonitor) getUI().getAdapter(IConditionMonitor.class);
//            monitor.add(new ShellShowingCondition(CONFIRM_PERSPECTIVE_SWITCH), new IHandler() {
//
//                public void handle (IUIContext ui) throws WidgetSearchException {
//                    try {
//                        // <BUG> But in eclipse: the Conform Perspective Switch
//                        // dialog doesn't have focus, so is not "active shell".
//                        // force it.
//                        IWidgetLocator shellLocator = EclipseUtil.findShell(CONFIRM_PERSPECTIVE_SWITCH);
//                        if (shellLocator != null) {
//                            ui.setFocus(shellLocator);
//                        }
//                        // </BUG>
//                        ui.click(new ButtonLocator("&Yes"));
//                    }
//                    catch (WidgetSearchException t) {
//                        t.printStackTrace();
//                        throw t;
//                    }
//                }
//            });
//        }
    }
    
    /**
     * If the "Auto Add file" subversion confirmation pops up, select "No"
     * @throws WidgetSearchException
     */
    protected void dealWithSVNConfirmation() throws WidgetSearchException{
        try {
            getUI().wait(new ShellShowingCondition("Auto-add.*"),3000,500);
            getUI().click(new ButtonLocator("No"));
        } catch(WaitTimedOutException x){
            // Don't care if condition does not occur.
        }
    }

    
    /**
     * Arrange things so that if the "Auto-add to SVN" dialog ever pops up, select "No".
     * 
     */
    protected void registerSVNConfirmationHandler () {
        if (contextsInWhichSVNPromptIsSet.add(getUI())) {
            IConditionMonitor monitor = (IConditionMonitor) getUI().getAdapter(IConditionMonitor.class);
            monitor.add(new ShellShowingCondition("Auto-add.*"), new IHandler() {

                @Override
                public void handle (IUIContext ui) throws WidgetSearchException {
                    try {
                        ui.click(new ButtonLocator("(\\&)?No"));
                    }
                    catch (WidgetSearchException t) {
                        t.printStackTrace();
                        throw t;
                    }
                }
            });
        }
    }
    
    /**
     * Arrange things so that if the "Auto-add to SVN" dialog ever pops up, select "No".
     * 
     */
    protected void registerMiscellaneousHandlers () {
        if (contextsInWhichCleanProjectIsSet.add(getUI())) {
            IConditionMonitor monitor = (IConditionMonitor) getUI().getAdapter(IConditionMonitor.class);
          
            // We don't care if it doesn't build. The exe is already there...
            monitor.add(new ShellShowingCondition("Errors in Workspace"), new IHandler() {

                @Override
                public void handle (IUIContext ui1) throws WidgetSearchException {
                    ui1.click(new ButtonLocator("Yes"));
                }
            });
            monitor.add(new ShellShowingCondition("(Clean|Build) Project"), new IHandler() {

                @Override
                public void handle (IUIContext ui) throws WidgetSearchException {
                    ui.keyClick(WT.CR);
                }
            });
            // We get "Problem Occurred" message if the editor is viewing a file that was deleted
            // in previous run.
            monitor.add(new ShellShowingCondition("Problem Occurred"), new IHandler() {

                @Override
                public void handle (IUIContext ui) throws WidgetSearchException {
                    ui.keyClick(WT.CR);
                }
            });
            
            // We get "Wizard Closing" message if the editor is viewing a file that was deleted
            // in previous run.
            monitor.add(new ShellShowingCondition("Wizard Closing"), new IHandler() {

                @Override
                public void handle (IUIContext ui) throws WidgetSearchException {
                    ui.keyClick(WT.CR);
                }
            });

        }
    }
    
    protected void waitForDebugPerspective () {     
        getUI().wait(PerspectiveActiveCondition.forName("Debug"));
        resetPerspective(); // in case it we left in a non-canonical state.
    }
    
    /**
     * Invoke debugger by mean of on-the-fly launch configuration on
     * behalf of a project.
     * @param projectName name of project.
     * @param waitForDebugPerspective if true, we wait for perspective to change.
     * @throws WidgetSearchException 
     */
    protected void invokeDebuggerFor (String projectName, boolean waitForDebugPerspective) throws WidgetSearchException {
        // We have more than one Queens_AC launch; use the right one
        if (projectName.equals("Queens_AC")) {
            launchDebugger("Queens_AC.elf", waitForDebugPerspective);
        }
        else {
            getUI().contextClick(
                new TreeItemLocator(computeProjectNameString(projectName), new ViewLocator(
                    PROJECT_VIEW_ID)),
                "Debug As/1 Local C\\/C++ Application");
            dealWithDebugPerspectiveConfirmation(waitForDebugPerspective);
            if (waitForDebugPerspective) {
                waitForDebugPerspective();
            }
        }
    }
    
    /**
     * Run the application with an on-the-fly launch configuration on
     * behalf of a project.
     * @param projectName name of project.
     * @param waitForDebugPerspective if true, we wait for perspective to change.
     * @throws WidgetSearchException 
     */
    protected void runProject(String projectName) throws WidgetSearchException{
        getUI().contextClick(new TreeItemLocator(computeProjectNameString(projectName), new ViewLocator(
        PROJECT_VIEW_ID)),
        "Run As/1 Local C\\/C++ Application");
        EclipseUtil.waitForLaunchStart(getUI(),5000);
    }
   
    /**
     * Do whatever is necessary to terminate the debugger, assuming the
     * Launch View is visible, and the appropriate session is highlighted.
     * @throws WidgetSearchException
     */
    protected void terminateDebugger() throws WidgetSearchException{
        // In case it is CMPD, we must first select the session before terminating.
        String launchName = EclipseUtil.getLaunchName();
        if (launchName != null){
            getUI().click(computeTreeItemLocator(launchName +".*Application.*"));
        }
        ContributedToolItemLocator terminateItem = new ContributedToolItemLocator(
        "org.eclipse.debug.ui.commands.Terminate");
		getUI().click(terminateItem);
		ICondition disabled = new IsEnabledCondition(terminateItem,false);
		try {
	    	getUI().wait(disabled,5000,250);
		}catch(WaitTimedOutException x){
			// oh well
		}
        
    }
    
    /**
     * Do the necessary action to switch to the C/C++ perspective.
     * @throws WidgetSearchException
     */
    protected void switchToCPerspective() throws WidgetSearchException {
        if (!PerspectiveActiveCondition.forName("C/C++").test()) {
            changePerspective("C/C++ (default)");
            getUI().wait(milliseconds(1000));
        }
        resetPerspective(); // in case it was left in a crummy state.
        EclipseUtil.fixProjectView(getUI()); // In case it is not scrolled to the left
    }
    
    /**
     * Do the necessary action to switch to the debug perspective.
     * @throws WidgetSearchException
     */
    protected void switchToDebugPerspective() throws WidgetSearchException {
        if (!PerspectiveActiveCondition.forName("Debug").test()) {
            changePerspective("Debug");
            getUI().wait(milliseconds(1000));
        }
        resetPerspective();
    }
    protected void resetPerspective () {
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable(){
            @Override
            public void run () {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().resetPerspective();            
            }});
    }
    
    /**
     * Change to a particular perspective.
     * @param name name of the perspective (e.g., "Debug", "C/C++", etc.)
     * @throws WidgetSearchException
     */
    protected void changePerspective(String name) throws WidgetSearchException{
        IUIContext ui = getUI();
        EclipseUtil.waitForEclipseFrameToBeActive(ui);
        ui.click(new MenuItemLocator("Window/Open Perspective/Other..."));
        //EclipseUtil.clickMenuItem(ui,"Window/Open Perspective/Other...");
        ui.wait(new ShellShowingCondition("Open Perspective"));
        ui.click(new TableItemLocator(name));
        ui.click(new ButtonLocator("OK"));
        ui.wait(new ShellDisposedCondition("Open Perspective"));
        int i = name.indexOf(" (default)");
        if (i > 0)
            name = name.substring(0,i);       
        ui.wait(PerspectiveActiveCondition.forName(name));
    }
    
    /**
     * Click an arbitrary debugger button on the Launch view.
     * @param cmd the suffix name of the button (e.g., "Resume").
     * @throws WidgetSearchException
     */
    protected void clickDebuggerButton (String cmd) throws WidgetSearchException {
        getUI().click(getDebugButtonLocator(cmd));
        this.pause(getUI(),500); // If steps happen too fast, they get skipped
    }

    protected ContributedToolItemLocator getStepIntoLocator(){
    	return getDebugButtonLocator("StepInto");
    }
    
    protected ContributedToolItemLocator getStepOverLocator(){
    	return getDebugButtonLocator("StepOver");
    }

	protected ContributedToolItemLocator getDebugButtonLocator(String cmd) {
		return new ContributedToolItemLocator("org.eclipse.debug.ui.commands." + cmd);
	}
	
	protected ContributedToolItemLocator getRestartLocator(){
		return getDebugButtonLocator("Restart");
	}
    
    protected void clickResumeButton() throws WidgetSearchException{
        clickDebuggerButton("Resume");
    }
    
    protected void clickStopButton() throws WidgetSearchException{
        //clickDebuggerButton("Suspend");
        getUI().click(new ContributedToolItemLocator("org.eclipse.debug.ui.debugview.toolbar.suspend"));
    }
    
    protected void clickStepIntoButton() throws WidgetSearchException{
        clickDebuggerButton("StepInto");
    }
    
    protected void clickStepOverButton() throws WidgetSearchException{
        clickDebuggerButton("StepOver");
    }
    
    protected void clickTerminateButton() throws WidgetSearchException{
        clickDebuggerButton("Terminate");
    }
    
    protected void clickRestartButton() throws WidgetSearchException{
    	getUI().click(getRestartLocator());
    }
    
    protected String computeViewLabelOf(String viewID) throws WidgetSearchException{
        String label = viewIdToLabelMap.get(viewID);
        if (label == null) throw new WidgetSearchException("Can't open view " + viewID);
        return label;
    }
    
    /**
     * Show a view as it would appear in the <i>Show View</i> dialog.
     * For example "Debug/Registers"
     * @param viewID the view ID of the view to be shown.
     * @throws WidgetSearchException
     */
    protected void showView(String viewID) throws WidgetSearchException{
        if (EclipseUtil.makeViewVisible(viewID,null) == null) {
            IUIContext ui = getUI();
            //ui.click(new MenuItemLocator("\\&Window/Show \\&View/\\&Other....*"));
            //Sometimes menu selection gets lost under Windows
            // How do we fix it??
            
            //MenuItemLocator often fails on Windows. It miscomputes the location of the topmost
            // menu as 0,0. 
            EclipseUtil.waitForEclipseFrameToBeActive(ui);
           
            ui.click(new MenuItemLocator("Window/Show View/Other....*"));
            ui.wait(new ShellShowingCondition("Show View"));
            if (!"Show View".equals(EclipseUtil.getActiveShellTitle(ui))){
                // Under XP, we occasional have the problem of a popped-up modal dialog
                // not having the focus. Fix it here.
                IWidgetLocator sh = EclipseUtil.findShell("Show View");
                if (sh != null){
                    //ui.ensureThat(sh.hasFocus());
                    EclipseUtil.setFocus((IWidgetReference)sh);
                }
            }
            ui.click(computeTreeItemLocator(computeViewLabelOf(viewID)));
            ui.click(new ButtonLocator("OK"));
            ui.wait(milliseconds(2000)); // Linux seems to delay materializing the view.
        }
    }
    
    protected void makeSeeCodeDisplayVisible(String display){
        EclipseUtil.makeViewVisible(SEECODE_VIEW_ID,display);
    }
    
    protected void showSeeCodeView(String name) throws WidgetSearchException {
        IUIContext ui = getUI();
        EclipseUtil.waitForEclipseFrameToBeActive(ui);
        ui.click(new MenuItemLocator("Debugger/"+name));
    }
    
    /**
     * Given a snapshot name, return the corresponding file name in which
     * state information is to be written.
     * @param snapshotName name of snapshot.
     * @return corresponding file name.
     */
    protected String computeSnapshotFileName(String snapshotName){
        return snapshotName + ".xml";
    }
    
    protected String getSnapshotTestDirectory(){
        String nameSegments[] = this.getClass().getName().split("\\.");
        return TEST_DIRECTORY + "/" + nameSegments[nameSegments.length-2];     
    }
    
    
    
    protected String getSnapshotBaselineDirectory(){
        String nameSegments[] = this.getClass().getName().split("\\.");
        return BASELINE_DIRECTORY + "/" + nameSegments[nameSegments.length-2];     
    }
    
    
    protected String computeSnapshotTestPath(String snapshotName){
        String dir = getSnapshotTestDirectory();
        if (!new File(dir).isDirectory()){
            new File(dir).mkdirs();
        }
        return  dir + "/" + computeSnapshotFileName(snapshotName);
    }
    
    protected String computeSnapshotBaselinePath(String snapshotName){
        String dir = getSnapshotBaselineDirectory();
        if (!new File(dir).isDirectory()){
            new File(dir).mkdirs();
        }
        return  dir + "/" + computeSnapshotFileName(snapshotName);
    }
    
    /**
     * Open test output stream into which state information can be written.
     * @param snapshotName name of the snapshot.
     * @return the test output stream.
     * @throws IOException
     */
    protected OutputStream openSnapshotTestOutput(String snapshotName) throws IOException{
        String path = computeSnapshotTestPath(snapshotName);
        return TestsPlugin.getDefault().getOutputStream(path);
    }
    
    /**
     * Compare if test snapshot that was just generated matches the
     * baseline. If the baseline doesn't yet exist, then copy the test
     * snapshot to the baseline.
     * @param snapshotName the name of the snapshot.
     * @throws IOException 
     */
    protected void compareSnapshot(String snapshotName) throws IOException{
        String testPath = computeSnapshotTestPath(snapshotName);
        String baselinePath = computeSnapshotBaselinePath(snapshotName);
        InputStream testIn = TestsPlugin.getDefault().getInputStream(testPath);
        Assert.assertTrue("Couldn't open test stream!!",testIn != null);
        InputStream baseIn = TestsPlugin.getDefault().getInputStream(baselinePath);
//        if (fReplaceAllPattern != null) {
//            String testContent = FileUtil.getContent(testIn).replaceAll(fReplaceAllPatten, fReplaceWith);
//        }
        if (baseIn != null && testIn != null) {
            boolean v = FileUtil.compareStreams(testIn,baseIn,true);
            // There may be some benign differences from, say, Linux
            // and windows. Check if there is an alternate
            // baseline to compare with.
            if (!v){
            	int i = 1;
            	while (true){
            	    String alternative = computeSnapshotBaselinePath(snapshotName+"_alt"+i++);
            	    InputStream newBaseIn = TestsPlugin.getDefault().getInputStream(alternative);
            	    if (newBaseIn != null){
            	    	testIn.close();
            	    	baseIn.close();
            	    	baseIn = newBaseIn;
            	    	testIn = TestsPlugin.getDefault().getInputStream(testPath);
            	    	v = FileUtil.compareStreams(testIn,baseIn,true);
            	    	if (v) break;
            	    }
            	    else break;
            	}           	
            }
           Assert.assertTrue(snapshotName,v);
           baseIn.close();
        }
        else {
            // baseline doesn't yet exist. Make the current test the base line.
            OutputStream outputStream = TestsPlugin.getDefault().getOutputStream(baselinePath);
            FileUtil.copyStream(testIn,outputStream);
            outputStream.close();
        }
        if (testIn != null)
            testIn.close();       
    }
    
    
    /**
     * Take a snapshot of a seecode display and compare with baseline.
     * If there is no baseline, then make the snapshot the baseline.
     * <P>
     * If the baseline exists and this snapshot doesn't match then write it
     * in a directory named "failures" before firing assertion failure.
     * 
     * @param snapshotName name of snapshot from which filename will be derived.
     * @param view the SeeCode view name, for example "disasm".
     * @throws WidgetSearchException 
     * @throws IOException 
     */
    protected void compareSeecodeDisplay(String snapshotName,String view) throws WidgetSearchException, IOException{
        IUIContext ui = getUI();
        NamedWidgetLocator namedWidgetLocator = new NamedWidgetLocator(view);
        IWidgetLocator locs[] = EclipseUtil.findAllWithName(ui,view);
        if (locs.length == 0){
            // Console may have obscure it.
            this.makeSeeCodeDisplayVisible(view);
            locs = EclipseUtil.findAllWithName(ui,view);
            if (locs.length == 0) {
                ui.wait(milliseconds(1000));
                this.makeSeeCodeDisplayVisible(view);
                locs = EclipseUtil.findAllWithName(ui,view);
                if (locs.length == 0) {
                    ui.wait(milliseconds(1000));
                    this.makeSeeCodeDisplayVisible(view);
                    locs = EclipseUtil.findAllWithName(ui,view);
                }
            }
        }
        IWidgetLocator loc = locs.length == 1?locs[0]:ui.find(namedWidgetLocator);
        OutputStream outStream = openSnapshotTestOutput(snapshotName);
        Assert.assertTrue(loc instanceof IWidgetReference);
        TextColumn canvas = (TextColumn)((IWidgetReference)loc).getWidget(); 
        ui.wait(milliseconds(500)); // make sure display has time to update.
        EclipseUtil.waitForPaintRequestsToComplete();
        canvas.recordState(outStream);
        outStream.close();
        compareSnapshot(snapshotName);
    }
    
    /**
     * Compare the contents of a view.
     * <P>
     * NOTE: SeeCode views are handled by {@link #compareSeeCodeDisplay}. This one
     * is used for conventially-defined views.
     * @param snapshotName name of snapshot from which filename will be derived.
     * @param view ID of visible view.
     * @throws WidgetSearchException 
     * @throws IOException 
     */
    protected void compareView(String snapshotName, String view) throws WidgetSearchException, IOException{
    	EclipseUtil.makeViewVisible(view,null);
        IWidgetReference loc = EclipseUtil.findView(view);
        compareWidget(snapshotName,loc,view);
    }
    
    protected void compareSeeCodeView(String snapshotName, String kind) throws WidgetSearchException{
        IWidgetReference loc = EclipseUtil.findView(SEECODE_VIEW_ID,kind);
        compareWidget(snapshotName,loc,SEECODE_VIEW_ID + ":" + kind);
    }
    
    protected void compareBuildSettings(String snapshot,IToolOptionSetting setting) throws WidgetSearchException {
        try {
            OutputStream outStream = openSnapshotTestOutput(snapshot);
            EclipseUtil.writeBuildSettingState(outStream,setting);
            outStream.close();
            compareSnapshot(snapshot);
        }
        catch (IOException e) {
            Assert.assertTrue("I/O Error for snapshot " + snapshot + ": " +
                e.getMessage(),false);
        }
        
    }
    
    /**
     * Compare the state of the Console display.
     * @param snapshot name of the snapshot.
     * @throws WidgetSearchException
     */
    protected void compareConsole(String snapshot) throws WidgetSearchException {   
        String console = getConsoleContent();
        this.writeAndCompareSnapshot(snapshot,console);
    }
    
    /**
     * Compare the state of the application Console display.
     * @param snapshot name of the snapshot.
     * @throws WidgetSearchException
     */
    protected void compareApplicationConsole(String snapshot) throws WidgetSearchException {   
        String console = getApplicationConsoleContent();
        this.writeAndCompareSnapshot(snapshot,console);
    }
    
    /**
     * Compare the state of the build Console display.
     * @param snapshot name of the snapshot.
     * @throws WidgetSearchException
     */
    protected void compareBuildConsole(String snapshot) throws WidgetSearchException {   
        String console = getBuildConsoleContent();
        this.writeAndCompareSnapshot(snapshot,console);
    }

    /**
     * Compare the contents of a widget, typically a table or tree.
     * <P>
     * @param snapshotName name of snapshot from which filename will be derived.
     * @param ref the reference to the widget.
     * @param name name of widget, or null.
     * @throws WidgetSearchException 
     * @throws IOException 
     */
    protected void compareWidget(String snapshotName, IWidgetReference ref, String name) {
        try {
            OutputStream outStream = openSnapshotTestOutput(snapshotName);
            EclipseUtil.writeWidgetState(outStream,ref,name);
            outStream.close();
            compareSnapshot(snapshotName);
        }
        catch (IOException e) {
            Assert.assertTrue("I/O Error for snapshot " + snapshotName + ": " +
                e.getMessage(),false);
        }
    }
    
    /**
     * Compare the contents of a widget, typically a table or tree.
     * <P>
     * @param snapshotName name of snapshot from which filename will be derived.
     * @param ref the reference to the widget.
     * @param name name of widget, or null.
     * @throws WidgetSearchException 
     * @throws IOException 
     */
    protected void compareWidget(String snapshotName, Widget widget, String name) {
        try {
            OutputStream outStream = openSnapshotTestOutput(snapshotName);
            EclipseUtil.writeWidgetState(outStream,widget,name);
            outStream.close();
            compareSnapshot(snapshotName);
        }
        catch (IOException e) {
            Assert.assertTrue("I/O Error for snapshot " + snapshotName + ": " +
                e.getMessage(),false);
        }
    }
    
    /**
     * Compare the contents of a widget, typically a table or tree.
     * <P>
     * @param snapshotName name of snapshot from which filename will be derived.
     * @param ref the reference to the widget.
     * @throws WidgetSearchException 
     * @throws IOException 
     */
    protected void compareWidget(String snapshotName, IWidgetReference ref) {
        compareWidget(snapshotName,ref,null);
    }
    
    protected void writeAndCompareSnapshot(String snapshotName, String content){
        try {
            OutputStream outStream = openSnapshotTestOutput(snapshotName);
            PrintStream out = new PrintStream(outStream);
            out.println("<content>");
            out.println(content);
            out.println("</content>");
            out.close();
            compareSnapshot(snapshotName);
        }
        catch (IOException e) {
            Assert.assertTrue("I/O Error for snapshot " + snapshotName + ": " +
                e.getMessage(),false);
        }
    }
    
    /**
     * Enter a command into the Debugger's command-line field. The view that
     * contains the field is assumed to be visible.
     * @param command
     * @throws WidgetSearchException 
     */
    protected void enterDebugCommandLine(String command) throws WidgetSearchException{
        EclipseUtil.confirmDebuggerSelected(getUI());
        enterTextWithinNamedWidget("SeeCodeCommandText",command,true);
        getUI().wait(milliseconds(500)); // don't issue commands to frequently; engine needs time to respond
    }
    
    /**
     * Enter text into a Text field or Combo box.
     * @param widgetName the name of the  text field or combo box.
     * @param text the string to insert into the text field or combobox.
     * @throws WidgetSearchException
     */
    protected void enterTextWithinNamedWidget(String widgetName, String text, boolean appendCR) throws WidgetSearchException{
        IUIContext ui = getUI();
        IWidgetLocator textField = EclipseUtil.findWidgetWithName(ui,widgetName);
        EclipseUtil.clearTextField(textField);
        //ui.ensureThat(textField.hasFocus());
        ui.click(textField);
        EclipseUtil.setFocus((IWidgetReference)textField);
        pause(ui,500); // if we don't delay, sometimes things go badly.
        ui.keyClick(WT.CTRL,'A');
        pause(ui,500); // if we don't delay, sometimes things go badly.
        ui.enterText(text);
        if (appendCR)
            ui.keyClick(WT.CR);       
    }
    
    /**
     * Enter text and get around bugs in "ui.enterText(s)".
     * @param s text to enter on the key board.
     */
    protected void enterText(String s){
        IUIContext ui = getUI();
        for (int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            if (c != '\n')
                ui.keyClick(c);
            else
                ui.keyClick(WT.CR);
        }
    }
    
    /**
     * Move the selection within the Launch View up or down by a particular amount.
     * <P>
     * The Launch view is assumed to be visible.
     * @param amount if negative, move down accordingly; otherwise move up.
     * @throws WidgetSearchException 
     */
    protected void moveLaunchViewSelection(int amount) throws WidgetSearchException{
        IUIContext ui = getUI();
        IWidgetLocator tree = ui.find(new SWTWidgetLocator(Tree.class,new ViewLocator("org.eclipse.debug.ui.DebugView")));
        EclipseUtil.moveTreeSelection(ui,tree,amount);               
    }
    
    /**
     * Delete the given project if it exists.
     * @param projectName name of the project to delete.
     * @return true if the project existed and has been deleted; false if the
     * project did not exist.
     * @throws WidgetSearchException if an error occurred while attempting to
     * delete an existing project.
     */
    protected boolean deleteProject(String projectName) throws WidgetSearchException{
        return  EclipseUtil.deleteProject(projectName);
    }
    
    /**
     * Compare the Paths and Symbols of a project with a baseline. Assert failure
     * if they don't match.
     * @param projectName the name of the project.
     * @param snapshotName the name of the snapshot.
     * @throws WidgetSearchException 
     */
    protected void comparePathsAndSymbols(String projectName, final String snapshotName) throws Exception{
        this.bringUpPropertiesDialogPathsAndSymbols(projectName, new IUIRunnable(){

            @Override
            public void run (IUIContext ui) throws WidgetSearchException {
                TabItemLocator tabItemLocator = new TabItemLocator("Includes");
                ui.click(tabItemLocator);
                IWidgetReference panel = EclipseUtil.getTabPanel((IWidgetReference)ui.find(tabItemLocator));
                IWidgetReference button = EclipseUtil.findButtonLocator("Show built-in values", (Control)panel.getWidget());
                if (!EclipseUtil.isSelected(button)) {
                    ui.click(button);
                }
                ui.click(new TreeItemLocator("C Source File", new SWTWidgetLocator(Tree.class, new SWTWidgetLocator(
                    SashForm.class))));
                IWidgetReference table = EclipseUtil.findTableLocator((Control)panel.getWidget());
                compareWidget(snapshotName+".1",table);
                TabItemLocator tabItemLocator2 = new TabItemLocator("Symbols");
                ui.click(tabItemLocator2);
                IWidgetReference panel2 = EclipseUtil.getTabPanel((IWidgetReference)ui.find(tabItemLocator2));
                IWidgetReference button2 = EclipseUtil.findButtonLocator("Show built-in values", (Control)panel2.getWidget());
                if (!EclipseUtil.isSelected(button2)) {
                    ui.click(button2);
                }
                ui.click(new TreeItemLocator("C Source File", new SWTWidgetLocator(Tree.class, new SWTWidgetLocator(
                    SashForm.class))));
                table = EclipseUtil.findTableLocator((Control)panel2.getWidget());
                compareWidget(snapshotName+".2",table);
                
            }}); 
    }
    
    /**
     * Locate a Control or ToolItem that has the given tool tip.
     * @param tooltip the tool tip string.
     * @throws WidgetSearchException  if no widget found, or more than one found.
     */
    protected IWidgetReference findWidgetWithToolTip(String tooltip) throws WidgetSearchException{
    	return EclipseUtil.findWidgetLocatorFromToolTip(tooltip,(Shell)getUI().getActiveWindow());
    }
    
    /**
     * Set the main Eclipse window to a canonical size so that
     * size-dependent views will have a deterministic state.
     */
    protected void setCanonicalSize(){
       EclipseUtil.setCanonicalSize();
    }
    
    /**
     * Set the build properties to their default value on behalf of a project.
     * @param projectName the project name.
     * @throws WidgetSearchException
     */
    protected void setDefaultBuildProperties(String projectName) throws Exception{
        this.bringUpBuildSettings(projectName,new IUIToolOptionSetter(){
            @Override
            public void run (IUIContext ui, IToolOptionSetting setting) throws WidgetSearchException {
                setting.restoreDefault();      
            }});
    }
    
    /**
     * Turn on or turn off the animation buttons on the Debug View toolbar.
     * <P>
     * Assumes that Debug View is showing.
     * @param v if true, turn on; otherwise turn off.
     * @throws WidgetSearchException
     */
    protected void setAnimationItems(boolean v) throws WidgetSearchException{
        EclipseUtil.checkPulldownMenuItem(getUI(),DEBUG_VIEW_ID,"Show Animation Items",false);
    }
    
    /**
     * Record the state of the current shell (often a dialog); fails if it doesn't match
     * the base line.
     * @param snapshotName name of the snapshot.
     */
    protected void compareShellContent(String snapshotName){
        try {
            OutputStream outStream = openSnapshotTestOutput(snapshotName);
            PrintStream out = new PrintStream(outStream);
            EclipseUtil.writeWidgetState(out, (Shell)getUI().getActiveWindow(),null);
            compareSnapshot(snapshotName);
        }
        catch (IOException e) {
        	e.printStackTrace();
            Assert.assertTrue("I/O Error for snapshot " + snapshotName + ": " +
                e.getMessage(),false);
        }
    }
    
    static boolean isEclipse3_4(){
    	return System.getProperty("osgi.framework.version").compareTo("3.4") >= 0;
    }
    
    static boolean isEclipse3_5(){
    	return System.getProperty("osgi.framework.version").compareTo("3.5") >= 0;
    }
    
    static boolean isEclipse3_6(){
        return System.getProperty("osgi.framework.version").compareTo("3.6") >= 0;
    }
    
    static int getCdtVersion() {
    	return isEclipse3_5()?6:5;
    }
    /**
     * Select launch from Launch Configuration dialog.
     */
    protected void selectLaunchFromLaunchDialog(IUIContext ui, String launchName) throws WidgetSearchException {
    	// CDT 5 was "C/C++ Local Application"
    	// CDT 6 is "C/C++ Application"
    	// Can't remember what the "M*" was for.
        ui.click(computeTreeItemLocator("C\\/C\\+\\+ [LMA].*/"+launchName));
    }
    
    /**
     * Launch the debugger, given the Launch configuration name.
     * @param launchName the user-friendly name of the launch configuration.
     * @param waitForDebugPerspective wait until perspectives switches to Debug.
     * @param bypassPopupDialogs if true, wait for transient popup dialogs to clear (e.g. "Launch in progress").
     * @throws WidgetSearchException 
     */
    protected void launchDebugger (String launchName, boolean waitForDebugPerspective, boolean bypassPopupDialogs) throws WidgetSearchException {
        IUIContext ui = getUI();
        EclipseUtil.waitForEclipseFrameToBeActive(ui);
        String selectName = DEBUG_CONFIG_SELECTION_NAME;
        ui.click(new PullDownMenuItemLocator(selectName, new ContributedToolItemLocator(
            "org.eclipse.debug.internal.ui.actions.DebugDropDownAction")));
        EclipseUtil.waitForShellShowing(ui, DEBUG_CONFIG_DIALOG_TITLE);
        ui.wait(new ShellShowingCondition(DEBUG_CONFIG_DIALOG_TITLE));
        selectLaunchFromLaunchDialog(ui,launchName);
        ui.click(new ButtonLocator("&Debug"));
        ui.wait(new ShellDisposedCondition(DEBUG_CONFIG_DIALOG_TITLE));
        dealWithDebugPerspectiveConfirmation(waitForDebugPerspective);
        if (waitForDebugPerspective){
            waitForDebugPerspective();
        }
        if (bypassPopupDialogs)
            EclipseUtil.waitForEclipseFrameToBeActive(ui);
    }
    
    /**
     * Launch the debugger, given the Launch configuration name.
     * @param launchName the user-friendly name of the launch configuration.
     * @throws WidgetSearchException 
     */
    protected void launchDebugger (String launchName, boolean waitForDebugPerspective) throws WidgetSearchException {
    	launchDebugger(launchName,waitForDebugPerspective,true);
    }
    
    /**
     * Run an application, given the Launch configuration name.
     * @param launchName the user-friendly name of the launch configuration.
     * @throws WidgetSearchException 
     */
    protected void runLaunch (String launchName) throws WidgetSearchException {
        IUIContext ui = getUI();
        ui.click(new PullDownMenuItemLocator(RUN_CONFIG_SELECTION_NAME, new ContributedToolItemLocator(
            "org.eclipse.debug.internal.ui.actions.RunDropDownAction")));
        ui.wait(new ShellShowingCondition(RUN_CONFIG_DIALOG_TITLE));
        ui.click(new TreeItemLocator("C\\/C\\+\\+.* Application/" + launchName));
        ui.click(new ButtonLocator("&Run"));
        ui.wait(new ShellDisposedCondition(RUN_CONFIG_DIALOG_TITLE));
        EclipseUtil.waitForLaunchStart(getUI(),5000);
    }
    
    /**
     * Create a new project.
     * @param projectName name of new project.
     * @param library if true, generate static library project. Otherwise, exe project.
     * @throws WidgetSearchException
     */
    protected void createNewProject (String projectName, boolean library) throws WidgetSearchException {
        IUIContext ui = getUI();
        ui.click(new MenuItemLocator("File/New/Project..."));
        ui.wait(new ShellShowingCondition("New Project"));
        ui.click(computeTreeItemLocator(getCdtVersion()>=6?"C\\/C++/C Project":"C/C Project"));
        ui.click(new ButtonLocator("&Next >"));
        ui.enterText(projectName);
        if (library){
            ui.click(new TreeItemLocator("Static Library", new LabeledLocator(Tree.class, "Toolchains:")));
            ui.click(new TreeItemLocator("Static Library/Empty Project", new LabeledLocator(Tree.class, "Toolchains:")));
        }
        ui.click(new ButtonLocator("&Finish"));
        dealWithSVNConfirmation();
        EclipseUtil.waitForEclipseFrameToBeActive(ui);
    }
    
    /**
     * Create a new exe project.
     * @param projectName name of new project.
     * @throws WidgetSearchException
     */
    protected void createNewProject (String projectName) throws WidgetSearchException {
        createNewProject(projectName,false);
    }
    
    protected static final String ARROW_DOWN = "" + WT.ESC + "D";
    protected static final String ARROW_RIGHT = "" + WT.ESC + "R";
    /**
     * Create a source file in a project.
     * @param projectName the name of the project.
     * @param sourceName the name of the source file (e.g., "main.c").
     * @param content the content of the file.
     * @param isSource if true, then this is a source file; editor automatically activated.
     * @throws WidgetSearchException 
     */
    protected void createTextFile (String projectName, String sourceName, String content, boolean isSource)
        throws WidgetSearchException {
        IUIContext ui = getUI();
        this.rightClickProjectMenu(projectName, "New/Source File");
        ui.wait(new ShellShowingCondition("New Source File"));
        ui.enterText(sourceName);
        
        //Sometimes, due to disk delay, the "Finish" button will delay in being
        // enabled.
        ButtonLocator finishButton = new ButtonLocator("&Finish");
        IsEnabledCondition cond = new IsEnabledCondition(finishButton);
        ui.wait(cond,15000,250);
        
        ui.click(finishButton);
        ui.wait(new ShellDisposedCondition("New Source File"));
        dealWithSVNConfirmation();
        // NOTE editor automatically has focus for Source file.
        if (!isSource) {
            ui.click(2, computeTreeItemLocator(computeProjectNameString(projectName) + "/" + sourceName));
        }
        ui.keyClick(WT.CTRL,'A'); // erase any comment stuff
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '\n') {
            	if (buf.length() > 0){
            		ui.enterText(buf.toString());
            		buf.setLength(0);
            	}
                ui.keyClick(WT.CR);
            }
            else if (c == WT.ESC) {
            	if (buf.length() > 0){
            		ui.enterText(buf.toString());
            		buf.setLength(0);
            	}
                if (i + 1 < content.length()) {
                    switch (content.charAt( ++i)) {
                        case 'D':
                            ui.keyClick(WT.ARROW_DOWN);
                            break;
                        case 'U':
                            ui.keyClick(WT.ARROW_UP);
                            break;
                        case 'L':
                            ui.keyClick(WT.ARROW_LEFT);
                            break;
                        case 'R':
                            ui.keyClick(WT.ARROW_RIGHT);
                            break;
                        default:
                            ui.keyClick('?');
                            ui.keyClick(content.charAt(i));
                            break;
                    }
                }
            }
            else
                buf.append(c);
        }
        if (buf.length() > 0){
    		ui.enterText(buf.toString());
    		buf.setLength(0);
    	}
        ui.keyClick(WT.CTRL, 'S'); // save it.
        ui.wait(milliseconds(3000)); // delay so as to finish saving the file.
    }   
    
    /**
     * Create a source file in a project.
     * @param projectName the name of the project.
     * @param sourceName the name of the source file (e.g., "main.c").
     * @param content the content of the file.
     * @throws WidgetSearchException 
     */
    protected void createSourceFile (String projectName, String sourceName, String content)
        throws WidgetSearchException {
        createTextFile(projectName,sourceName,content,true);
    }
    
    /**
     * Create a folder.
     * @param projectName the container for the folder; may be path
     * separated by slashes.
     * @param folderName name of the folder.
     * @throws WidgetSearchException
     */
    protected void createSourceFolder(String projectName, String folderName) throws WidgetSearchException{
    	this.rightClickProjectMenu(projectName, "New/Source Folder");
    	IUIContext ui = getUI();
		ui.wait(new ShellShowingCondition("New Source Folder"));
		ui.enterText(folderName);
		ui.click(new ButtonLocator("&Finish"));
		ui.wait(new ShellDisposedCondition("New Source Folder"));
    }
    
    protected boolean isEclipse3_3(){
        return EclipseUtil.isEclipse3_3();
    }
    
    /**
     * Set the Problems View filter to only show for selected project.
     * @throws WidgetSearchException 
     */
    protected void setProblemsViewFilter() throws WidgetSearchException {
        showView(PROBLEM_VIEW_ID);
        IWidgetReference viewRef = EclipseUtil.findView(PROBLEM_VIEW_ID);
        IUIContext ui = getUI();
        pause(ui,1000);  // Sometimes there is a delay in populating
        TreeItemLocator item = new TreeItemLocator("Errors .*",viewRef);
        IWidgetLocator items[] = ui.findAll(item);
        if (items.length > 0) {
            EclipseUtil.expandTreeItem(ui,(IWidgetReference)items[0]);
        }
        else EclipseUtil.dumpControl((Control)viewRef.getWidget());
        item = new TreeItemLocator("Warnings .*",viewRef);
        items = ui.findAll(item);
        if (items.length > 0) {
            EclipseUtil.expandTreeItem(ui,(IWidgetReference)items[0]);
        }
        
       
        String dialogName;
        if (isEclipse3_3()) {
            IWidgetReference ref = EclipseUtil.findViewToolBar(PROBLEM_VIEW_ID);
            ui.click(EclipseUtil.getToolItem(ref,0));
            dialogName = "Filters";
        }
        else {
            ui.click(new PullDownMenuItemLocator("Configure Contents...", new ViewLocator(
            "org.eclipse.ui.views.ProblemView")));
            dialogName = "Configure Contents";
        }
        ui.wait(new ShellShowingCondition(dialogName));
        ui.click(new ButtonLocator("On any element in same pro&ject"));
        ui.click(new ButtonLocator("OK"));
        ui.wait(new ShellDisposedCondition(dialogName));      
    }
    
    /**
     * Wait until the debugger goes into a suspended state, or is terminated altogether.
     * @param timeout in milliseconds.
     * @return true if debugger stopped; false if timeout occurred.
     */
    protected boolean waitUntilDebuggerStops(int timeout){
        boolean v =  EclipseUtil.waitUntilDebuggerStops(getUI(),timeout);
        EclipseUtil.waitForEclipseFrameToBeActive(getUI());
        return v;
    }

    /**
     * Wait until the application or debugger launch terminates.
     * @param timeout in milliseconds.
     * @return true launch terminated before timeout occurred.
     */
    protected boolean waitForLaunchTermination(int timeout){
        return EclipseUtil.waitForLaunchTermination(getUI(), timeout);
    }
    
    /**
     * Return whether or not breakpoints are to be cleared at startup.
     * Clients may override this if they require that breakpoints from a previous test
     * be persistent.
     * @return whether or not breakpoints are to be cleared at startup.
     */
    protected boolean clearBreakpointsAtStartup(){
        return true;
    }
    
    /**
     * Click the "remove all" button of the Breakpoint view.
     * @throws WidgetSearchException 
     */
    protected void clickRemoveAllBreakpoints() throws WidgetSearchException{
        IUIContext ui = getUI();
        ui.click(new ContributedToolItemLocator("org.eclipse.debug.ui.breakpointsView.toolbar.removeAll"));
        ui.wait(new ShellShowingCondition("Remove All Breakpoints"));
        ui.click(new ButtonLocator("&Yes"));
        ui.wait(new ShellDisposedCondition("Remove All Breakpoints"));
    }

    @Override
    protected void setUp () throws Exception {
        super.setUp();
        System.out.println("Running test " + this.getClass().getName());
        EclipseUtil.activateEclipseWindow(); // in case previous test went wild and lost focus
        this.setCanonicalSize();
        EclipseUtil.terminateAllLaunches(); // in case any were left running from previous test
        if (clearBreakpointsAtStartup())
            EclipseUtil.clearBreakpoints();      // Remove any inherited breakpoints
        //Don't use Condition monitors! They impose way too much overhead,
        // especially under Linux.
//        this.registerPerspectiveConfirmationHandler();
//        this.registerMiscellaneousHandlers();
//        this.registerSVNConfirmationHandler();
        String workspace = new File(Platform.getInstanceLocation().getURL().toURI()).toString();
        final String workspacePattern = workspace.replaceAll("\\\\","/");
        final String HEADERFILES = "(C:)?/.*MetaWare/arc/";
        EclipseUtil.setTranslator(new ITextTranslator(){

            @Override
            public String translate (String s) {
                String t = s.replaceAll("\\\\","/").replaceAll(workspacePattern,"\\$\\{WORKSPACE\\}");
                t = t.replaceAll(HEADERFILES,"/ARC/MetaWare/arc/");
                // System.out.println("\"" + s + "\" --> \"" + t + "\"");
                return t;
            }});       
    }


    @Override
    public void run (TestResult result) {
        // Note: if this test is one of a collection, then the following operation is redundant, but
        // it tests for such redundancy.
        DatabaseStuff.addListeners(result);
        super.run(result);
    }


    @Override
    protected void runTest () throws Throwable {
        // TODO Auto-generated method stub
        String dumpName = getClass().getName() + ".txt";
        new File(dumpName).delete();
        try {
            super.runTest();
        }
        catch (WidgetNotFoundException x) {
            PrintStream out;
            try {
                out = new PrintStream(new FileOutputStream(dumpName));
                System.out.println(">>>Dump state in file " + dumpName);
            }
            catch (Exception e) {
                out = System.out;
            }
            final PrintStream out_ = out;
            PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

                @Override
                public void run () {
                    try {
                        EclipseUtil.dumpControl(out_, PlatformUI.getWorkbench().getDisplay().getActiveShell());
                    }
                    catch (Exception ex) {
                        System.out.println("Couldn't dump state");
                        ex.printStackTrace();
                    }
                }
            });

            throw x;
        }
    }


	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		// In case the JVM crashes, we want to make sure the database is flushed
		// ever-so-often
		DatabaseStuff.doPeriodicFlush();
	}
	
	/**
	 * Assuming the Launch Configuration dialog is showing, create a new launch.
	 * When the method returns, the Debugger tab will be showing.
	 * @param ui
	 * @param launchName the name of the launch
	 * @param project the associated project
	 * @throws WidgetSearchException
	 */
	protected void createNewLaunchFromLaunchDialog(IUIContext ui, String launchName, String project) throws WidgetSearchException{
	    IWidgetLocator loc = EclipseUtil.computeTreeItemLocator(ui,getCdtVersion()>= 6?"C\\/C++ Application":"C\\/C\\+\\+ .*Application");
	    ui.contextClick(loc, "New");
	    //EclipseUtil.dumpControl((Control)ui.getActiveWindow()); //TOBEREMOVED
	    ui.click(new LabeledLocator(Text.class,"&Name:"));
	    ui.keyClick(WT.CTRL,'A');
	    ui.enterText(launchName);
	    
	    Shell launchDialog = (Shell)ui.getActiveWindow();
	    
	    ui.click(new LabeledLocator(Text.class, "&Project:"));
	    ui.keyClick(WT.CTRL,'A');
	    ui.enterText(project);
//        ui.click(new LabeledLocator(Button.class, "&Project:"));
//        ui.wait(new ShellShowingCondition("Project Selection"));
//        ui.click(new TableItemLocator(project));
//        ui.click(new ButtonLocator("OK"));
//        ui.wait(new ShellDisposedCondition("Project Selection"));
	    IWidgetReference ref = EclipseUtil.findButtonLocator("Searc&h Project...", (Control)ui.getActiveWindow());
	    //ui.click(new ButtonLocator("Searc&h Project..."));  // flakey
	    ui.click(ref);
	    ui.wait(new ShellShowingCondition("Program Selection"));
        ui.click(new TableItemLocator(project + ".elf"));
        ui.click(new ButtonLocator("OK"));
        ui.wait(new ShellDisposedCondition("Program Selection"));
        //sometimes the parent dialog fails to have focus
        EclipseUtil.setActiveShell(launchDialog);
        ui.click(new CTabItemLocator("Debugger"));
        ui.click(new ButtonLocator("Apply"));
	}
	
	protected void pause(IUIContext ui, int millisec){
	    ui.wait(milliseconds(millisec));
	}


    protected void bringUpDebugLaunchDialog (IUIRunnable runnable) throws Exception {
        IUIContext ui = getUI();
        ui.click(new PullDownMenuItemLocator(DEBUG_CONFIG_SELECTION_NAME, new ContributedToolItemLocator(
            "org.eclipse.debug.internal.ui.actions.DebugDropDownAction")));
        String dialogTitle = DEBUG_CONFIG_DIALOG_TITLE;
        ShellShowingCondition shellShowingCondition = new ShellShowingCondition(dialogTitle);
        //NOTE: as of Eclipse 3.6, the Launch Config dialog can pop up but not be "active", so
        // the "ShellShowingCondition" doesn't ever get activated. Hack around this.
        EclipseUtil.waitForShellShowing(ui,DEBUG_CONFIG_DIALOG_TITLE);
      
        EclipseUtil.setActiveShellSize(ui, 1100, 800); // so as to be a decent size
        runnable.run(ui);
        if (shellShowingCondition.test()) {
            // On XP we may have lost focus...
            IWidgetLocator sh = EclipseUtil.findShell(dialogTitle);
            if (sh != null){
                //ui.ensureThat(sh.hasFocus());
                EclipseUtil.setFocus((IWidgetReference)sh);
            }
            ui.click(new ButtonLocator("Close"));
            try {
                ui.wait(new ShellShowingCondition(isEclipse3_5()?"Save Changes":"Save changes?"),5000);
                ui.click(new ButtonLocator("No"));
                ui.wait(new ShellDisposedCondition("Save changes?"));
            } catch(WaitTimedOutException x){
                //OK
            }        
        }
        ui.wait(new ShellDisposedCondition("Debug"));
    }
    
    protected void toggleInstructionStepMode() throws WidgetSearchException{
    	ContributedToolItemLocator contributedToolItemLocator = new ContributedToolItemLocator(
				"org.eclipse.cdt.debug.internal.ui.actions.ToggleInstructionStepModeActionDelegate");
//    	if (false) {
//    		//WindowTester is broken here. This always times out no matter what.
//    		IsEnabledCondition cond = new IsEnabledCondition(contributedToolItemLocator);
//            getUI().wait(cond,15000,250);
//    	}
		getUI().click(contributedToolItemLocator);
    }
    
    /**
     * Given a register name, returns its value as shown in the Registers view.
     * @param regName the name of the register, e.g., "r10".
     * @return the value of the register
     */
    protected String getValueFromRegisterView(String regName) throws WidgetSearchException{
        this.showView(REGISTER_VIEW_ID);
        IWidgetReference ref = EclipseUtil.findView(REGISTER_VIEW_ID);
        String[][] tableItems = EclipseUtil.getTableRows(getUI(),ref);
        for (String[] row: tableItems){
            for (int i = 0; i < row.length; i++){
                if (regName.equals(row[i])){
                    return row[i+1];
                }
            }
        }
        throw new WidgetSearchException("Cannot find register \"" + regName + "\"");
    }
    
    /**
     * Given a "source" or "disasm" display, make sure all profiling is cleared.
     * @param seeCodeDisplayKind either "source" or "disasm".
     */
    protected void clearProfilingColumns(String seeCodeDisplayKind) throws WidgetSearchException{
    	  IWidgetReference srcView = EclipseUtil.findView(SEECODE_VIEW_ID, seeCodeDisplayKind);
          Point size = EclipseUtil.getSize(srcView);
          ILocator contextLoc = new XYLocator(srcView,size.x*5/6,size.y/2);
          
          IUIContext ui = getUI();
          
          ui.contextClick(contextLoc, "Profiling"); // Force creation of menu

          final Menu popupMenu = EclipseUtil.getPopupMenu(srcView);
          if (popupMenu == null) {
              EclipseUtil.dumpControl((Control)srcView.getWidget());
              Assert.assertTrue(false);
              return;
          }


          final List<String> selectedItems = new ArrayList<String>();
          popupMenu.getDisplay().syncExec(new Runnable() {

              @Override
              public void run () {
                  for (MenuItem item : popupMenu.getItems()) {
                      if ("Profiling".equals(item.getText())) {
                          for (MenuItem profItem : item.getMenu().getItems()) {
                              if (profItem.getSelection()) {
                                  selectedItems.add(profItem.getText());
                              }
                          }
                          break;
                      }
                  }

              }
          });
          ui.keyClick(WT.ESC);
          ui.wait(milliseconds(200));
          ui.keyClick(WT.ESC);
          for (String item : selectedItems) {
              ui.contextClick(contextLoc, "Profiling/" + item);
          }
    }
    
    /**
     * Set or unset a checkbox or radiobutton.
     * @param name the name of the check box or radio button.
     * @param selection to check or uncheck.
     * @throws WidgetSearchException 
     */
    protected void setCheckBox(String name, boolean selection) throws WidgetSearchException{
        IUIContext ui = getUI();
        NamedWidgetLocator c = new NamedWidgetLocator(name);
        IWidgetLocator w = ui.find(c);
        if (EclipseUtil.isSelected((IWidgetReference)w) != selection) {
           ui.click(w);
           assertTrue("Checkbox selection changed",EclipseUtil.isSelected((IWidgetReference)w) == selection);
        }
    }
}
