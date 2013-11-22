package com.arc.cdt.tests.A;
import com.arc.cdt.testutil.UIArcTestCaseSWT;

public class T_2_53 extends UIArcTestCaseSWT {
    public static final String DESCRIPTION = "Test if \".asm\" files are recognized as assembly files.";
    public static final String CATEGORY = PROJECT_MANAGEMENT;
	/**
	 * Main test method.
	 */
	public void testT_2_53() throws Exception {
	    switchToCPerspective(); //in case previous test left in wrong perspective
		buildProject("Assemble_err_asm");
		this.setProblemsViewFilter();
		showView(PROBLEM_VIEW_ID);
		compareView("T_2_53.1",PROBLEM_VIEW_ID);
	}

}