

package com.arc.cdt.tests.B;


import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;

import java.io.File;

import junit.framework.Assert;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.condition.ICondition;
import com.windowtester.runtime.condition.IsEnabledCondition;
import com.windowtester.runtime.condition.IsVisibleCondition;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.locator.IWidgetReference;
import com.windowtester.runtime.locator.WidgetReference;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.CTabItemLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;
import com.windowtester.runtime.swt.locator.TableItemLocator;


public class T_CR94025 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Test that CMPD launch config dialog displays file paths relative to associated project, and"
        + " diagnoses errors, etc.";

    public static final String CATEGORY = DEBUGGER_INTEGRATION;

    private static final String LAUNCH_NAME = "Graphics pipeline 2";

    static boolean VDK_SUPPORT = false;
    /**
     * Main test method.
     */
    public void testCR94025 () throws Exception {
        this.bringUpDebugLaunchDialog(new IUIRunnable() {

            @Override
            public void run (IUIContext ui) throws WidgetSearchException {
                ICondition isInvisible = new IsVisibleCondition(new NamedWidgetLocator("config_file_not_exist"),false);
                ui.click(computeTreeItemLocator("C\\/C\\+\\+ [LM].*/"+LAUNCH_NAME));
                ui.click(new CTabItemLocator("CMPD Debugger Configuration"));

				if (VDK_SUPPORT) {
					ui
							.click(new NamedWidgetLocator(
									"vdkconfig.search_project"));
					ui.wait(new ShellShowingCondition("File Selection"));
					compareWidget("T_CR94025.1", new WidgetReference<Object>(ui
							.getActiveWindow()));
					ui.click(new ButtonLocator("Cancel"));
					ui.wait(new ShellDisposedCondition("File Selection"));
					ui.click(new NamedWidgetLocator("vdk_project.field"));
					ui.keyClick(WT.CTRL, 'A');
					ui.wait(milliseconds(200));
					ui.keyClick('\b');
					ui.wait(milliseconds(300));
					ui.keyClick('\b');

					String vdkpath = EclipseUtil.getText(ui
							.find(new NamedWidgetLocator("vdkconfig.field")));
					Assert.assertTrue(
							"VDK config path must be absolute at this point: "
									+ vdkpath, vdkpath != null
									&& vdkpath.trim().length() > 0
									&& new File(vdkpath).isAbsolute());
					ui.assertThat(
							"\"file does not exist\" label must be invisible",
							isInvisible);

					ui.enterText(".cproject");
					ui.click(new NamedWidgetLocator("reset_cmpd_processes"));
					ui
							.wait(new ShellShowingCondition(
									"Project selection error"));
					compareWidget("T_CR94025.2", new WidgetReference<Object>(ui
							.getActiveWindow()));
					ui.click(new ButtonLocator("OK"));
					ui.wait(new ShellDisposedCondition(
							"Project selection error"));

					ui.click(new NamedWidgetLocator("vdk_project.browse"));
					ui.wait(new ShellShowingCondition("Project Selection"));
					ui.click(new TableItemLocator("graphics_pipeline"));
					ui.click(new ButtonLocator("OK"));
					ui.wait(new ShellDisposedCondition("Project Selection"));
					vdkpath = EclipseUtil.getText(ui
							.find(new NamedWidgetLocator("vdkconfig.field")));
					Assert.assertTrue(
							"VDK config path must be relative at this point",
							vdkpath != null && vdkpath.trim().length() > 0
									&& !new File(vdkpath).isAbsolute());
					ui.click(new NamedWidgetLocator("vdkconfig.field"));
					ui.keyClick(WT.END);
					ui.keyClick('X');
					ICondition isDisabled = new IsEnabledCondition(
							new NamedWidgetLocator("reset_cmpd_processes"),
							false);
					ICondition isEnabled = new IsEnabledCondition(
							new NamedWidgetLocator("reset_cmpd_processes"),
							true);
					ui.assertThat(
							"Reset CMPD Processes button must be disabled",
							isDisabled);
					ICondition isVisible = new IsVisibleCondition(
							new NamedWidgetLocator("config_file_not_exist"),
							true);
					ui.assertThat(
							"\"file does not exist\" label must be visible",
							isVisible);
					ui.keyClick('\b');
					ui.assertThat(
							"\"file does not exist\" label must be invisible",
							isInvisible);
					ui.assertThat(
							"Reset CMPD Processes button must be enabled",
							isEnabled);

					ui.click(new NamedWidgetLocator("vdkconfig.field"));
					ui.keyClick(WT.CTRL, 'A');
					ui.enterText(".cproject");
					ui.assertThat(
							"\"file does not exist\" label must be invisible",
							isInvisible);
					ui.click(new NamedWidgetLocator("reset_cmpd_processes"));
					ui.wait(new ShellShowingCondition("VDK read error"));
					ui.click(new ButtonLocator("&Details >>"));
					compareWidget("T_CR94025.3", new WidgetReference<Object>(ui
							.getActiveWindow()));
					ui.click(new ButtonLocator("OK"));
					ui.wait(new ShellDisposedCondition("VDK read error"));

					// Under XP, we occasional have the problem of a popped-up
					// modal dialog
					// not having the focus. Fix it here.
					IWidgetLocator sh = EclipseUtil.findShell(DEBUG_CONFIG_DIALOG_TITLE);
					if (sh != null) {
						//ui.ensureThat(sh.hasFocus());
					    EclipseUtil.setFocus((IWidgetReference)sh);
					}

					ui.click(EclipseUtil.findWidgetWithName(ui,
							"vdkconfig.field"));
					ui.keyClick(WT.CTRL, 'A');
					ui.enterText(vdkpath);
					ui.assertThat(
							"\"file does not exist\" label must be invisible",
							isInvisible);
					ui.assertThat(
							"Reset CMPD Processes button must be enabled",
							isEnabled);
				}
                
                ui.click(new TableItemLocator("grmain"));
                ui.click(new NamedWidgetLocator("cmpd_edit"));
                ui.wait(new ShellShowingCondition("Edit Process"));
                ui.click(new NamedWidgetLocator("pathargs.search_project"));
                ui.wait(new ShellShowingCondition("File Selection"));
                compareWidget("T_CR94025.4",new WidgetReference<Object>(ui.getActiveWindow()));
                ui.click(new ButtonLocator("Cancel"));
                ui.wait(new ShellDisposedCondition("File Selection"));
                
                String cmd = EclipseUtil.getText(ui.find(new NamedWidgetLocator("pathargs.field")));
                Assert.assertTrue("CMPD exe path must be relative at this point",cmd != null && cmd.trim().length() > 0 && !new File(cmd).isAbsolute());
                
                String processName = EclipseUtil.getText(ui.find(new NamedWidgetLocator("process_name")));
                checkErrorLabel(null);
                
                ui.click(new NamedWidgetLocator("process_name"));
                ui.keyClick('%');
                checkErrorLabel("Process name must be alphanumeric");
                ui.keyClick(WT.CTRL,'A');
                ui.keyClick('\b');
                checkErrorLabel("Process name required");
                ui.enterText(processName);
                checkErrorLabel(null);
                
                ui.click(new NamedWidgetLocator("process_ids"));
                ui.keyClick(WT.CTRL,'A');
                ui.keyClick('\b');
                checkErrorLabel("(Need to enter process ID\\(s\\))|(Invalid process ID list.*)");
                ui.enterText("0");
                checkErrorLabel("Invalid process ID list: 0 \\(Process ID must be positive integer constant\\)\\.?");
                ui.keyClick('\b');
                ui.enterText("1000");
                checkErrorLabel("Invalid process ID list: 1000 \\(Process ID must not exceed \\d\\d\\d\\)");
                ui.enterText("\b\b\b\b4");
                checkErrorLabel("Process ID list \"4\" overlaps another one (\"4\")");
                ui.enterText("\b9:8");
                checkErrorLabel(".*Invalid range:.*");
                ui.enterText("\b\b");
                checkErrorLabel(null);
                
                ui.click(new NamedWidgetLocator("cmpdproject.field"));
                ui.keyClick('X');
                checkErrorLabel("Not a valid project name");
                cmd = EclipseUtil.getText(ui.find(new NamedWidgetLocator("pathargs.field")));
                Assert.assertTrue("CMPD exe path must be absolute at this point",cmd != null && cmd.trim().length() > 0 && new File(cmd).isAbsolute());
                ui.keyClick('\b');
                checkErrorLabel(null);
                
                ui.click(new NamedWidgetLocator("pathargs.field"));
                ui.keyClick('X');
                checkErrorLabel("Not a valid executable path");
                ui.keyClick('\b');
                checkErrorLabel(null);          
                
                ui.click(new ButtonLocator("Cancel"));
                ui.wait(new ShellDisposedCondition("Edit Process"));
            }
        });
    }
    
    private void checkErrorLabel(String text) throws WidgetSearchException{
        IUIContext ui = getUI();
        NamedWidgetLocator label = new NamedWidgetLocator("cmpd_error_label");
        ICondition isVisible = new IsVisibleCondition(label,true);
        ICondition isInvisible = new IsVisibleCondition(label,false);
        if (text == null) {
            ui.assertThat("error label must not be visible",isInvisible);            
        }
        else {
            ui.assertThat("Error label must be visible",isVisible);
            String actual = EclipseUtil.getText(ui.find(label));
            Assert.assertTrue("Label: '" + text  + "' vs '" + actual + "'",text.equals(actual) || 
                actual.matches(text));
        }
    }

}