package com.arc.cdt.tests.B;

import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import junit.framework.Assert;

import org.eclipse.cdt.managedbuilder.core.IOption;

import com.arc.cdt.testutil.CDTUtil;
import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IToolOptionSetting;
import com.arc.cdt.testutil.IUIToolOptionSetter;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.swt.locator.ButtonLocator;

public class T_CR22360 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that automatic overlay stuff is supported in the build properties.";
    public static final String CATEGORY = BUILD_MANAGEMENT;
	/**
	 * Main test method.
	 */
	public void testT_CR22360() throws Exception {
	    switchToCPerspective(); //in case previous test left in wrong perspective
		this.bringUpBuildSettings("Test1", new IUIToolOptionSetter() {

            @Override
            public void run (IUIContext ui, IToolOptionSetting settings) throws WidgetSearchException {
                settings.restoreDefault();
                ui.click(new ButtonLocator("Apply"));
                ui.wait(milliseconds(500));
                EclipseUtil.checkForProgressBarDisposal(ui, "User Operation is Waiting");
                
                CDTUtil.setCompilerSwitch("-arc600",settings,true);
                CDTUtil.setCompilerSwitch("-Haom", settings, true);
                CDTUtil.setCompilerSwitch("-Hon=aom_rtos_aware",settings,true);
                IOption rf16 = CDTUtil.getOptionForSwitch(settings.getCompilerOptions(), "-rf16");
                Assert.assertTrue(!settings.isEnabled(rf16));
                compareBuildSettings("T_CR22360.1",settings);
                CDTUtil.setCompilerSwitch("-Haom", settings, false);
                compareBuildSettings("T_CR22360.2",settings);
                CDTUtil.setCompilerSwitch("-rf16",settings,true);
                compareBuildSettings("T_CR22360.3",settings);
                CDTUtil.setCompilerSwitch("-arc700",settings,true);
                compareBuildSettings("T_CR22360.4",settings);
                IOption aomaware = CDTUtil.getOptionForSwitch(settings.getCompilerOptions(), "-Hon=aom_rtos_aware");
                Assert.assertTrue("-Hon=aom_rtos_aware disabled",!settings.isEnabled(aomaware));
              
                settings.restoreDefault();
                
            }
        });
	}

}