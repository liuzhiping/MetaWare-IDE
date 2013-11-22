package com.arc.cdt.tests.A;
import com.arc.cdt.testutil.UIArcTestCaseSWT;

public class T_2_03b extends UIArcTestCaseSWT {
    
    public static final String CATEGORY = BUILD_MANAGEMENT;
    public static final String DESCRIPTION = "Make change to queens.c to produce error, and make sure it is properly captured.";
    private static final String PROJECT_NAME = "no_exist_func";
    static final String CONTENT =
        "int main() {\n" +
        "no_exist_func();\n" +
        "return 0;";

	/**
	 * Main test method.
	 */
	public void testT_2_03b() throws Exception {
		//registerPerspectiveConfirmationHandler();
	    switchToCPerspective(); //in case previous test left in wrong perspective
		deleteProject(PROJECT_NAME);
		createNewProject(PROJECT_NAME);
		createSourceFile(PROJECT_NAME,"no_exist_func.c",CONTENT);
		buildProject(PROJECT_NAME);
		showView(PROBLEM_VIEW_ID);
		setProblemsViewFilter();
		compareView("T_2_03b.1",PROBLEM_VIEW_ID);
		deleteProject(PROJECT_NAME);
	}

}