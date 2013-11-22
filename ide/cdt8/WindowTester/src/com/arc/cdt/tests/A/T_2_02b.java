package com.arc.cdt.tests.A;
import com.arc.cdt.testutil.UIArcTestCaseSWT;

public class T_2_02b extends UIArcTestCaseSWT {
    
    public static final String CATEGORY = PROJECT_MANAGEMENT;
    public static final String DESCRIPTION = "Test drag-and-drop of a source file from one project to another";

//	private static final String PROJECT_NAME1 = "PQueens_Copy"; // Name chosen so as to be immediately before Queens
//	private static final String PROJECT_NAME2 = "Queens_AC.*";
//	private static final String SOURCE = "queens.c";

    /**
	 * Main test method.
	 */
	public void testT_2_02b(){
	     // WindowTester's drag-and-drop tester is broken
//		IUIContext ui = getUI();
//		switchToCPerspective();  // in case left in bogus state
//		EclipseUtil.closeAllEditors();
//		deleteProject(PROJECT_NAME1);
//		createNewProject(PROJECT_NAME1);
//		ui.click(this.computeTreeItemLocator(PROJECT_NAME2));
//		ui.click(1,this.computeTreeItemLocator(PROJECT_NAME2+"/" + SOURCE +".*"));
//		ui.click(this.computeTreeItemLocator(PROJECT_NAME1));
//		ui.click(1,this.computeTreeItemLocator(PROJECT_NAME2+"/" + SOURCE +".*"));
//		
//	    //EclipseUtil.dragTo(ui,this.computeTreeItemLocator(PROJECT_NAME2+"/" + SOURCE +".*"),new TreeItemLocator(PROJECT_NAME1, new ViewLocator("org.eclipse.ui.navigator.ProjectExplorer")),WT.CTRL);
//	    ui.dragTo(new TreeItemLocator(PROJECT_NAME1, new ViewLocator("org.eclipse.ui.navigator.ProjectExplorer")),WT.CTRL);
//		buildProject(PROJECT_NAME1);
//		runProject(PROJECT_NAME1);
//		EclipseUtil.waitForLaunchTermination(ui, 10000);
//		compareApplicationConsole("T_2_02b.1");
//		EclipseUtil.fixProjectView(ui);
	}

}