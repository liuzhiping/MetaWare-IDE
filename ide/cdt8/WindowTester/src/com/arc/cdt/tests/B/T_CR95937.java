

package com.arc.cdt.tests.B;


import junit.framework.Assert;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.Path;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.UIArcTestCaseSWT;


public class T_CR95937 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that a \"Build\" from IDE startup does not do an implicit clean.";
    public static final String CATEGORY = BUILD_MANAGEMENT;

    private static final String PROJECT = "Queens_AC";

    /**
     * Main test method.
     */
    public void testT_CR95937 () throws Exception {
        this.switchToCPerspective();
            
        IWorkspace ws = EclipseUtil.getWorkspace();
        IFile exeFile = ws.getRoot().getFile(new Path(PROJECT + "/Debug/" + PROJECT +".elf"));
        Assert.assertTrue(exeFile.exists());
        
        long timeStamp = exeFile.getLocalTimeStamp();
       
        this.buildProject(PROJECT);

        Assert.assertTrue("Time stamp on exe doesn't match",timeStamp == exeFile.getLocalTimeStamp());
       
    }

}