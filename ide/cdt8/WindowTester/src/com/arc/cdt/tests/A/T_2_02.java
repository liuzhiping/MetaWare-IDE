

package com.arc.cdt.tests.A;


import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;


public class T_2_02 extends UIArcTestCaseSWT {

    public static final String CATEGORY = PROJECT_MANAGEMENT;
    public static final String DESCRIPTION = "Test copy-and-paste of a source file from one project to another";

    private static final String PROJECT_NAME1 = "Queens_AB_Copy";

    private static final String PROJECT_NAME2 = "Queens_AC.*";

    private static final String SOURCE = "queens.c";

    /**
     * Main test method.
     */
    public void testT_2_02 () throws Exception {
        IUIContext ui = getUI();
        switchToCPerspective(); // in case left in bogus state
        EclipseUtil.closeAllEditors();
        deleteProject(PROJECT_NAME1);
        createNewProject(PROJECT_NAME1);
        ui.click(this.computeTreeItemLocator(PROJECT_NAME2));
        ui.click(1, this.computeTreeItemLocator(PROJECT_NAME2 + "/" + SOURCE + ".*"));
        ui.wait(milliseconds(400)); // Do it again in case it was on the bottom and scrolled up.
        ui.click(1, this.computeTreeItemLocator(PROJECT_NAME2 + "/" + SOURCE + ".*"));
        
        ui.keyClick(WT.CTRL, 'C');
        ui.wait(milliseconds(1000));

        ui.click(this.computeTreeItemLocator(PROJECT_NAME1));
        this.rightClickProjectMenu(PROJECT_NAME1, "Paste");
        buildProject(PROJECT_NAME1);
        runProject(PROJECT_NAME1);
        EclipseUtil.waitForLaunchTermination(ui, 10000);
        compareApplicationConsole("T_2_02.1");
        EclipseUtil.fixProjectView(ui);
        deleteProject(PROJECT_NAME1);
    }

}