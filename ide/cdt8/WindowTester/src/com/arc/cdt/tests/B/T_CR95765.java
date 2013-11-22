

package com.arc.cdt.tests.B;


import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.locator.IWidgetReference;


public class T_CR95765 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that warnings in linker command file is captured.";
    public static final String CATEGORY = BUILD_MANAGEMENT;

    private static final String PROJECT = "linkwarn";

    /**
     * Main test method.
     */
    public void testT_CR95765 () throws Exception {

        switchToCPerspective();
        
        this.cleanProject(PROJECT);
       
        this.buildProject(PROJECT);
        
        showView(PROBLEM_VIEW_ID);
        setProblemsViewFilter();
        
        IWidgetLocator item = EclipseUtil.computeTreeItemLocator(getUI(), "Warnings (1 item)");
        EclipseUtil.expandTreeItem(getUI(),(IWidgetReference)item);
        compareView("T_CR95765.1",PROBLEM_VIEW_ID);
       
    }

}