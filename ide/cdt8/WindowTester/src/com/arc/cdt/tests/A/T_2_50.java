package com.arc.cdt.tests.A;
import com.arc.cdt.testutil.UIArcTestCaseSWT;

public class T_2_50 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Test assembler errors show up in Problems View.";
    public static final String CATEGORY = BUILD_MANAGEMENT;
	/**
	 * Main test method.
	 */
	public void testT_2_50() throws Exception {
		registerPerspectiveConfirmationHandler();
	    switchToCPerspective(); //in case previous test left in wrong perspective
		buildProject("Assembler_err_s");
		this.setProblemsViewFilter();
		compareView("T_2_50.1",PROBLEM_VIEW_ID);
	}

}