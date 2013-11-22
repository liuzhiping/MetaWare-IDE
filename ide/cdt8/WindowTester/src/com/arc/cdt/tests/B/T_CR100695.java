

package com.arc.cdt.tests.B;


import junit.framework.Assert;

import org.eclipse.cdt.managedbuilder.core.IOption;

import com.arc.cdt.testutil.CDTUtil;
import com.arc.cdt.testutil.IToolOptionSetting;
import com.arc.cdt.testutil.IUIToolOptionSetter;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;


public class T_CR100695 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that the linker options -Cfunction and -Ccrossfunc are supported.";
    public static final String CATEGORY = BUILD_MANAGEMENT;

    private static final String PROJECT = "Queens_AC";
    
    protected void assertLinkerOptionEnabled(IToolOptionSetting settings, String switchName, boolean state) throws WidgetSearchException{
        IOption option = CDTUtil.getOptionForSwitch(settings.getLinkerOptions(), switchName);
        assertTrue("Option for " + switchName,option != null);
        assertTrue("Enablement state for " + switchName,settings.isEnabled(option) == state);
    }
    
    protected void checkBooleanBuildLinkerSetting(IToolOptionSetting settings, String switchName) throws WidgetSearchException{
        IOption option = CDTUtil.getOptionForSwitch(settings.getLinkerOptions(), switchName);
        assertTrue("Option for " + switchName,option != null);
        settings.setOptionValue(option, Boolean.TRUE);
    }

    /**
     * Main test method.
     */
    public void testT_CR100695 () throws Exception {

        switchToCPerspective();
        
        this.setBuilder(PROJECT,false);
        
        this.bringUpBuildSettings(PROJECT, new IUIToolOptionSetter(){

            @Override
            public void run (IUIContext ui, IToolOptionSetting settings) throws WidgetSearchException {
                IOption mapOption = CDTUtil.getOptionForID(settings.getLinkerOptions(), "com.arc.cdt.toolchain.linker.option.map");
                Assert.assertTrue(mapOption != null);
                settings.setOptionValue(mapOption,Boolean.FALSE);   
                assertLinkerOptionEnabled(settings,"-Hldopt=-Cfunctions",false);
                assertLinkerOptionEnabled(settings,"-Hldopt=-Ccrossfunc",false);
                
                settings.setOptionValue(mapOption,Boolean.TRUE);
                assertLinkerOptionEnabled(settings,"-Hldopt=-Cfunctions",true);
                assertLinkerOptionEnabled(settings,"-Hldopt=-Ccrossfunc",true);
                checkBooleanBuildLinkerSetting(settings,"-Hldopt=-Cfunctions");
                checkBooleanBuildLinkerSetting(settings,"-Hldopt=-Ccrossfunc");
            }});


    }

}