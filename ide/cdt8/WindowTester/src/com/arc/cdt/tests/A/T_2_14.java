package com.arc.cdt.tests.A;
import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import com.arc.cdt.testutil.CDTUtil;
import com.arc.cdt.testutil.IToolOptionSetting;
import com.arc.cdt.testutil.IUIToolOptionSetter;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;

public class T_2_14 extends UIArcTestCaseSWT {
    
    public static final String DESCRIPTION = "Confirm that extension specifications are propagated from build settings to debugger configuration.";
    public static final String CATEGORY = DEBUGGER_INTEGRATION;
    
    private static final String PROJECT_NAME = "Queens_AC";
	/**
	 * Main test method.
	 */
	public void testT_2_14() throws Exception {
	    switchToCPerspective(); //in case previous test left in wrong perspective
	    this.bringUpBuildSettings(PROJECT_NAME,new IUIToolOptionSetter(){

            @Override
            public void run (IUIContext ui, IToolOptionSetting setting) throws WidgetSearchException {
                setting.restoreDefault();
                CDTUtil.setCompilerSwitch("-Xlib",setting,false);
                compareBuildSettings("T_2_14.1",setting);
            }});
		buildProject(PROJECT_NAME);
		this.runLaunch(PROJECT_NAME+".elf");
		getUI().wait(milliseconds(4000));
		compareApplicationConsole("T_2_14.2");
		this.bringUpBuildSettings(PROJECT_NAME,new IUIToolOptionSetter(){

	            @Override
                public void run (IUIContext ui, IToolOptionSetting setting) throws WidgetSearchException {
	                CDTUtil.setCompilerSwitch("-arc700",setting,true);
	                CDTUtil.setCompilerSwitch("-Xlib",setting,true);
	                compareBuildSettings("T_2_14.3",setting);               
	            }});
		
		buildProject(PROJECT_NAME);
		this.runLaunch(PROJECT_NAME+".elf");
        getUI().wait(milliseconds(4000));
        compareApplicationConsole("T_2_14.4");
        //Set it back to default 
        setDefaultBuildProperties(PROJECT_NAME);
        
	}
}