package com.arc.cdt.tests.B;

import java.io.File;

import junit.framework.Assert;

import org.eclipse.core.resources.ResourcesPlugin;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.FileUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;

public class T_CR91878 extends UIArcTestCaseSWT {

	private static final String SOURCE = "auto_bal.c";
	private static final String PROJECT = "BAL";

	  public static final String DESCRIPTION = "Confirm that auto-bracket completion works.";
	    public static final String CATEGORY = GENERIC_CDT_TESTS;
	/**
	 * Main test method.
	 */
	public void testT_CR91878() throws Exception {
	    switchToCPerspective(); //in case previous test left in wrong perspective
		deleteProject(PROJECT);
		createNewProject(PROJECT);
		createSourceFile(PROJECT,SOURCE,
				"[\n{\n(\n\"\n");
		String workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		String test_workspace = workspace+"/" +PROJECT;
		
		Assert.assertTrue(FileUtil.compareFiles(new File(workspace,"auto_bal.c.ref"),
				new File(test_workspace+"/" + SOURCE),true));
		EclipseUtil.closeAllEditors();
		deleteProject(PROJECT);
	}

}