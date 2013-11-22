package com.arc.cdt.tests.B;

import java.io.File;

import junit.framework.Assert;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;

public class T_CR22194 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that clean operation doesn't affect dependent projects.";
    public static final String CATEGORY = BUILD_MANAGEMENT;
    
    private static final String PARENT_PROJECT = "ParentProject";
    private static final String DEPENDENT_PROJECT = "DependentProject";
	/**
	 * Main test method.
	 */
	public void testT_CR22194() throws Exception {
	    switchToCPerspective(); //in case previous test left in wrong perspective
		this.cleanProject(PARENT_PROJECT);
		this.cleanProject(DEPENDENT_PROJECT);
		
		//Confirm that there is no exe and no library in either project.
		File exe = EclipseUtil.getProjectRelativeReference(PARENT_PROJECT, "Debug/" + PARENT_PROJECT + ".elf");
		File lib = EclipseUtil.getProjectRelativeReference(DEPENDENT_PROJECT, "Debug/" + DEPENDENT_PROJECT + ".a");
		Assert.assertTrue(exe != null && !exe.exists());
		Assert.assertTrue(lib != null && !lib.exists());
		
		if (exe == null || lib == null) return; // keep compiler happy
				
		this.buildProject(PARENT_PROJECT);
		// Confirm that they now exist.
		
		Assert.assertTrue(exe.exists());
		Assert.assertTrue(lib.exists());
		
		this.cleanProject(PARENT_PROJECT);
		// Now, only the parent should be cleaned. Not dependent.
		Assert.assertTrue(!exe.exists());
		Assert.assertTrue(lib.exists());
	}

}