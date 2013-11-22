

package com.arc.cdt.tests.B;


import junit.framework.Assert;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.CTabItemLocator;
import com.windowtester.runtime.swt.locator.ComboItemLocator;
import com.windowtester.runtime.swt.locator.FilteredTreeItemLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;
import com.windowtester.runtime.swt.locator.eclipse.WorkbenchLocator;


public class T_CR100010 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that chipinit and DLL specifications are properly stored in Launch Configuration.";

    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    
    
    private static final String LAUNCH = "QUEENS WITH CHIPINIT";
    private static final String PROJECT = "Queens_AC";

    /*
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp () throws Exception {
        super.setUp();
        IUIContext ui = getUI();
        ui.ensureThat(new WorkbenchLocator().hasFocus());
    }

    /**
     * Main test method.
     */
    public void testT_CR100010 () throws Exception {

        EclipseUtil.deleteLaunch(LAUNCH);
        this.bringUpDebugLaunchDialog(new IUIRunnable(){

            @Override
            public void run (IUIContext ui) throws WidgetSearchException {
                createNewLaunchFromLaunchDialog(ui,LAUNCH,PROJECT);
                ui.click(new FilteredTreeItemLocator("C\\/C++ Application/"+LAUNCH));
                ui.click(new CTabItemLocator("Debugger"));
                ui.click(EclipseUtil.computeTreeItemLocator(ui,"Target Selection"));            
                ui.click(new ComboItemLocator("Hardware", new NamedWidgetLocator("ARC_target")));
                ui.click(new NamedWidgetLocator("ARC_use_ARC_DLL"));
                ui.click(new NamedWidgetLocator("DLL_name"));
                ui.enterText("foo.dll");
                ui.click(EclipseUtil.computeTreeItemLocator(ui,"Initialization"));
                ui.click(new NamedWidgetLocator("AC_chipinit"));
                ui.click(new NamedWidgetLocator("AC_Chipinit_filename"));
                ui.enterText("chipinit");
                ui.click(new ButtonLocator("Apply"));
                ui.click(new ButtonLocator("Close"));
            }});
        
        this.bringUpDebugLaunchDialog(new IUIRunnable(){

            @Override
            public void run (IUIContext ui) throws WidgetSearchException {
                ui.click(new FilteredTreeItemLocator("C\\/C++ Application/"+LAUNCH));
                ui.click(new CTabItemLocator("Debugger"));
                String text = EclipseUtil.getText(ui.find(new NamedWidgetLocator("debugger_options")));
                Assert.assertTrue(text.indexOf("-chipinit=chipinit") >= 0);
                Assert.assertTrue(text.indexOf("-DLL=foo.dll") >= 0);
                ui.click(new ButtonLocator("Close"));
            }});
        EclipseUtil.deleteLaunch(LAUNCH);
    }

}