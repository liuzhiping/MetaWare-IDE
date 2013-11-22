package com.arc.cdt.tests.A;
import com.arc.cdt.testutil.UIArcTestCaseSWT;

public class T_2_48 extends UIArcTestCaseSWT {
    
    public static final String DESCRIPTION = "Confirm that linking errors show up in Problem's view.";
    public static final String CATEGORY = BUILD_MANAGEMENT;
    private static String SOURCE_CONTENT = 
        "main() {\n" +
        "int x;\n" +
        "x=y;\n" + 
        "nonfunc1(\n" +
        ";\n"+
        "nonfunc2(0\n" +
        ";\n";

	/**
	 * Main test method.
	 */
	public void testT_2_48() throws Exception {
		String project = "unresolved_symbols";
		registerPerspectiveConfirmationHandler();
		switchToCPerspective(); //in case previous test left in wrong perspective
	    deleteProject(project);
	    this.createNewProject(project);
	    this.createSourceFile(project,"unresolved_sym.c", SOURCE_CONTENT);
		buildProject("unresolved_symbols");
		showView(PROBLEM_VIEW_ID);
		this.setProblemsViewFilter();
		compareView("T_2_48.1",PROBLEM_VIEW_ID);
	    deleteProject(project);
	}

}