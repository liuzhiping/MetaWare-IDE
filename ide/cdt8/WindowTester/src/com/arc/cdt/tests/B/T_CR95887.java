

package com.arc.cdt.tests.B;


import junit.framework.Assert;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.Path;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;


public class T_CR95887 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm nested source folders can have build properties distinct from parent folder.";
    public static final String CATEGORY = BUILD_MANAGEMENT;

    private static final String PROJECT = "SubFolder";

    /**
     * Main test method.
     */
    public void testT_CR95543 () throws Exception {

        switchToCPerspective();
        
        this.setBuilder(PROJECT,false);
        
        this.cleanProject(PROJECT);
      
        
        IWorkspace ws = EclipseUtil.getWorkspace();
        IFile exeFile = ws.getRoot().getFile(new Path(PROJECT + "/Debug/" + PROJECT +".elf"));
        Assert.assertTrue(!exeFile.exists());
       
        this.buildProject(PROJECT);

        Assert.assertTrue(exeFile.exists());
        
        // Now check internal builder...
        
        this.setBuilder(PROJECT,true);
        this.cleanProject(PROJECT);
       
        Assert.assertTrue(!exeFile.exists());
        this.buildProject(PROJECT);
      
        Assert.assertTrue(exeFile.exists());
        
        
        this.setBuilder(PROJECT,false);
       
    }

}