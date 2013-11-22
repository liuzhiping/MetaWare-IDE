package com.arc.cdt.tests.A;
import com.arc.cdt.testutil.UIArcTestCaseSWT;

public class T_3_3 extends UIArcTestCaseSWT {
    public static final String DESCRIPTION = "Confirm that an application that has a single warning upon build has it registered in the Problems view";
    public static final String CATEGORY = BUILD_MANAGEMENT;
	/**
	 * Main test method.
	 */
	public void testT_3_3() throws Exception {
	    switchToCPerspective(); //in case previous test left in wrong perspective
		cleanProject("Test1");
		buildProject("Test1");
		showView(PROBLEM_VIEW_ID);
		this.setProblemsViewFilter();
		compareView("T_3_3.1",PROBLEM_VIEW_ID);
	}

}