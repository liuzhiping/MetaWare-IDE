

package com.arc.cdt.tests.B;


import com.arc.cdt.testutil.UIArcTestCaseSWT;


public class T_CR95525 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that bundled \"Unix\" tools work if user doesn't have Cygwin installed.";

    public static final String CATEGORY = BUILD_MANAGEMENT;

    private static final String PROJECT = "No Cygwin";

    /**
     * Main test method.
     */
    public void testT_CR95525 () throws Exception {

        if (!isWindows())
            return; // Windows Only

        switchToCPerspective();

        this.buildProject(PROJECT);

        this.compareBuildConsole("T_CR95525.1");

        this.cleanProject(PROJECT);

        this.compareBuildConsole("T_CR95525.2");

    }

}