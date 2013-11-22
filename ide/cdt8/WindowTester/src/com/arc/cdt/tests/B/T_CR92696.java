

package com.arc.cdt.tests.B;


import junit.framework.Assert;

import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.arc.cdt.testutil.CDTUtil;
import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IToolOptionSetting;
import com.arc.cdt.testutil.IUIToolOptionSetter;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.locator.IWidgetReference;
import com.windowtester.runtime.locator.WidgetReference;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
import com.windowtester.runtime.swt.locator.TableItemLocator;


public class T_CR92696 extends UIArcTestCaseSWT {
    public static final String DESCRIPTION = "Test that \"Build Variable...\" button in the Include stuff works.";
    public static final String CATEGORY = BUILD_MANAGEMENT;
    private static final String PROJECT = "Queens_AC";

    /**
     * Test the "Build Variable..." button in the Include stuff.
     */
    public void testT_cr92696 () throws Exception {
        switchToCPerspective();
        this.bringUpBuildSettings(PROJECT, new IUIToolOptionSetter(){

            @Override
            public void run (IUIContext ui, IToolOptionSetting settings) throws WidgetSearchException {
                IOption incOpt = CDTUtil.getOptionWithLabel(settings.getCompilerOptions(),"Include Directories (one per line)");
                Assert.assertTrue("Can't locate -I option", incOpt != null);
                IWidgetReference incs = settings.findWidgetLocatorForOption(incOpt);
                ui.click(computeTreeItemLocator("MetaWare ARC C\\/C++ Compiler/Include Directories"));
                IWidgetReference toolbar = EclipseUtil.findToolBarRef(EclipseUtil.getParent(incs));               
                ui.click(EclipseUtil.getToolItem(toolbar,0));
                ui.wait(new ShellShowingCondition("Add directory path"));
                Shell addDialog = (Shell)ui.getActiveWindow();
                ui.click(new ButtonLocator("Build Variable.*"));
                ui.wait(new ShellShowingCondition("Select build variable"));
                ui.click(new TableItemLocator("ProjDirPath"));
                ui.click(new ButtonLocator("OK"));
                ui.wait(new ShellDisposedCondition("Select build variable"));
                EclipseUtil.setFocus(new WidgetReference<Object>(addDialog));
                ui.click(new SWTWidgetLocator(Text.class));
                ui.enterText("/FOOBAR");
                ui.click(new ButtonLocator("OK"));
                ui.wait(new ShellDisposedCondition("Add directory path"));               
            }});
        
        this.buildProject(PROJECT);
        String console = getBuildConsoleContent();
        if (console.indexOf("../FOOBAR") < 0) {
            this.writeStringToFile("T_cr92696.txt",console);
            Assert.fail("Include directory doesn't appear on compiler command line");
        }
        this.setDefaultBuildProperties(PROJECT);
       
    }

}