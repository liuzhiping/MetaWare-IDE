

package com.arc.cdt.tests.B;


import junit.framework.Assert;

import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.Path;

import com.arc.cdt.testutil.CDTUtil;
import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IToolOptionSetting;
import com.arc.cdt.testutil.IUIToolOptionSetter;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;


public class T_CR95543 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that linker-generated map files are removed during \"clean\" operation.";
    public static final String CATEGORY = BUILD_MANAGEMENT;

    private static final String PROJECT = "Queens_AC";

    /**
     * Main test method.
     */
    public void testT_CR95543 () throws Exception {

        switchToCPerspective();
        
        this.setBuilder(PROJECT,false);
        
        this.bringUpBuildSettings(PROJECT, new IUIToolOptionSetter(){

            @Override
            public void run (IUIContext ui, IToolOptionSetting settings) throws WidgetSearchException {
                IOption mapOption = CDTUtil.getOptionForID(settings.getLinkerOptions(), "com.arc.cdt.toolchain.linker.option.map");
                Assert.assertTrue(mapOption != null);
                settings.setOptionValue(mapOption,Boolean.TRUE);                
            }});

        this.buildProject(PROJECT);
        
        IWorkspace ws = EclipseUtil.getWorkspace();
        IFile mapFile = ws.getRoot().getFile(new Path(PROJECT + "/Debug/Queens_AC.map"));
        Assert.assertTrue(mapFile.exists());
       
        this.cleanProject(PROJECT);

        Assert.assertTrue(!mapFile.exists());
        
        // Now check internal builder...
        
        this.setBuilder(PROJECT,true);
        this.buildProject(PROJECT);
        ws.getRoot().getProject(PROJECT).refreshLocal(IResource.DEPTH_INFINITE,null);
        Assert.assertTrue(mapFile.exists());
        this.cleanProject(PROJECT);
        ws.getRoot().getProject(PROJECT).refreshLocal(IResource.DEPTH_INFINITE,null);
        Assert.assertTrue(!mapFile.exists());
        
        
        this.setBuilder(PROJECT,false);
        this.setDefaultBuildProperties(PROJECT);

    }

}