

package com.arc.cdt.tests.B;


import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;
import junit.framework.Assert;

import org.eclipse.cdt.managedbuilder.core.IOption;

import com.arc.cdt.testutil.CDTUtil;
import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IToolOptionSetting;
import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.IUIToolOptionSetter;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.locator.IWidgetReference;
import com.windowtester.runtime.swt.locator.CTabItemLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;


public class T_CR100286 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that the new -Xmpy16 and -Xmult32_cycles option are supported in the "+
            "compiler and debugger, and that it is implicitly set in the debug launch if present as " +
            "a compiler option. Also confirm that it is enabled/disabled when appropriate.";
    public static final String CATEGORY = BUILD_MANAGEMENT;
    private static final String PROJECT = "CR100286_Project";
    private static final String LAUNCH = "CR100286_LAUNCH";

    /**
     * Main test method.
     */
    public void testT_CR100286() throws Exception {
       
        this.switchToCPerspective();
        EclipseUtil.deleteLaunch(LAUNCH);
    	this.deleteProject(PROJECT);
    	this.createNewProject(PROJECT);
    	this.createSourceFile(PROJECT, "main.c", "void main(){}");
    	this.buildProject(PROJECT);
    	
    	this.bringUpBuildSettings(PROJECT,new IUIToolOptionSetter(){

            @Override
            public void run (IUIContext ui, IToolOptionSetting settings) throws WidgetSearchException {
                IOption mpy16Option = CDTUtil.getOptionForSwitch(settings.getCompilerOptions(), "-Xmpy16");
                Assert.assertTrue("-Xmpy16 exists",mpy16Option != null);
                IOption mult32Option = CDTUtil.getOptionForSwitch(settings.getCompilerOptions(), "-Xmult32");
                IOption mult32CyclesOption = CDTUtil.getOptionForSwitch(settings.getCompilerOptions(), "-Xmult32_cycles=");
                Assert.assertTrue("-Xmult32",mult32Option != null);
                Assert.assertTrue("-Xmult32_cycles=",mult32CyclesOption != null);
                IWidgetReference ref = settings.findWidgetLocatorForOption(mult32CyclesOption);
                Assert.assertTrue("-Xmpy16 widget exists",ref != null);
                Assert.assertTrue("Initially disabled",!settings.isEnabled(mult32CyclesOption));
                
                settings.setOptionValue(mult32Option,Boolean.TRUE);
                Assert.assertTrue("Now enabled",EclipseUtil.isEnabled(ref));
                
                settings.setOptionValue(mpy16Option,Boolean.TRUE);
                settings.setOptionValue(mult32CyclesOption, "2");
            }});
    	
    	this.bringUpDebugLaunchDialog(new IUIRunnable(){

            @Override
            public void run (IUIContext ui) throws WidgetSearchException {
                createNewLaunchFromLaunchDialog(ui,LAUNCH,PROJECT);
                ui.click(new CTabItemLocator("Debugger"));
                String text = EclipseUtil.getText(ui.find(new NamedWidgetLocator("debugger_options")));
                Assert.assertTrue("-Xmpy16 present",text.indexOf("-Xmpy16") >= 0);
                Assert.assertTrue("-Xmult32_cycles present",text.indexOf("-Xmult32_cycles=2") >= 0);

                
            }});
    	
    	this.bringUpBuildSettings(PROJECT,new IUIToolOptionSetter(){

            @Override
            public void run (IUIContext ui, IToolOptionSetting settings) throws WidgetSearchException {
                IOption mpy16Option = CDTUtil.getOptionForSwitch(settings.getCompilerOptions(), "-Xmpy16");
                IOption mult32Option = CDTUtil.getOptionForSwitch(settings.getCompilerOptions(), "-Xmult32");
                settings.setOptionValue(mult32Option,false);
                ui.wait(milliseconds(100));
                IOption mult32CyclesOption = CDTUtil.getOptionForSwitch(settings.getCompilerOptions(), "-Xmult32_cycles=");
                Assert.assertTrue(!settings.isEnabled(mult32CyclesOption));
                settings.setOptionValue(mpy16Option,false);
            }});
    	
    	this.bringUpDebugLaunchDialog(new IUIRunnable(){

            @Override
            public void run (IUIContext ui) throws WidgetSearchException {
                selectLaunchFromLaunchDialog(ui,LAUNCH);
                ui.click(new CTabItemLocator("Debugger"));
                String text = EclipseUtil.getText(ui.find(new NamedWidgetLocator("debugger_options")));
                Assert.assertTrue("-Xmpy16 not present",text.indexOf("-Xmpy16") < 0);
                Assert.assertTrue("-Xmult32_cycles not present",text.indexOf("-Xmult32") < 0);               
            }});
    	EclipseUtil.deleteLaunch(LAUNCH);
    	this.deleteProject(PROJECT);
    }

}