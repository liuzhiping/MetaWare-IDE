package com.arc.cdt.tests.A;
import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.eclipse.core.resources.ResourcesPlugin;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.FileUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;

public class T_4_2 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Test that auto-indention works in the CDT source editor";
    public static final String CATEGORY = GENERIC_CDT_TESTS;
    private static final String PROJECT_NAME = "CDT_Edit";
    private static final String SOURCE_NAME = "cdtedit.c";
	/**
	 * Main test method.
	 */
	public void testT_4_2() throws Exception {
	    switchToCPerspective(); //in case previous test left in wrong perspective
		// File compare variables
		deleteProject(PROJECT_NAME);
		String workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		String test_workspace = workspace+"/"+PROJECT_NAME;
		// WT
		this.createNewProject(PROJECT_NAME);
		this.createSourceFile(PROJECT_NAME, SOURCE_NAME, 
		    "int func(void) {\n" +
		    "for(;1;) {\n" + 
		    "break;" + ARROW_DOWN + "\n" +
		    "if (true) {\n" + 
		    ";" + ARROW_DOWN +
		    " else {\n" +
		    ";");
		 
		try {
		    Assert.assertTrue(FileUtil.compareFiles(new File(test_workspace, SOURCE_NAME), new File(workspace, "cdtedit.c.ref"),true));
		    EclipseUtil.closeAllEditors();
		    deleteProject(PROJECT_NAME); // delete only if compare; otherwise we need to see why they don't
		}
		catch (IOException e) {
			Assert.fail("File compare failed\n");
		}		
	}

}