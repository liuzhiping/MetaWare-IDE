

package com.arc.cdt.tests.B;


import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.swt.locator.CTabItemLocator;
import com.windowtester.runtime.swt.locator.FilteredTreeItemLocator;


public class T_CR97980 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that we can launch a project-relative" +
    		" executable that is not a build artifact.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;

    private static final String LAUNCH = "Test1 cr97980";

    /**
     * Main test method.
     */
    public void testT_CR97980() throws Exception {
        switchToCPerspective();
        this.bringUpDebugLaunchDialog(new IUIRunnable(){

			@Override
			public void run(IUIContext ui) throws WidgetSearchException {
				ui.click(new FilteredTreeItemLocator("C\\/C++ Application/"+LAUNCH));
				ui.click(new CTabItemLocator("Debugger"));
				EclipseUtil.setActiveShellSize(ui, 1100, 1000);
				ui.click(EclipseUtil.computeTreeItemLocator(ui,"Target Selection"));
				// If get this far, then the executable was recognized.						
			}});
         
    }

}