

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
import com.windowtester.runtime.condition.IsEnabledCondition;
import com.windowtester.runtime.condition.IsSelectedCondition;
import com.windowtester.runtime.locator.IWidgetReference;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.CTabItemLocator;


public class T_CR96324 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that the new -Xdmulpf option is supported in the "+
            "compiler and debugger, and that it is implicitly set in the debug launch if present as " +
            "a compiler option. Also confirm that it is enabled/disabled when appropriate.";
    public static final String CATEGORY = BUILD_MANAGEMENT;
    private static final String PROJECT = "CR96324_Project";
    private static final String LAUNCH = "CR96324_LAUNCH";

    /**
     * Main test method.
     */
    public void testT_CR96324() throws Exception {
       
        this.switchToCPerspective();
        EclipseUtil.deleteLaunch(LAUNCH);
    	this.deleteProject(PROJECT);
    	this.createNewProject(PROJECT);
    	this.createSourceFile(PROJECT, "main.c", "void main(){}");
    	this.buildProject(PROJECT);
    	
    	this.bringUpBuildSettings(PROJECT,new IUIToolOptionSetter(){

            @Override
            public void run (IUIContext ui, IToolOptionSetting settings) throws WidgetSearchException {
                IOption dmulpfOption = CDTUtil.getOptionForSwitch(settings.getCompilerOptions(), "-Xdmulpf");
                Assert.assertTrue("-Xdmulpf exists",dmulpfOption != null);
                IOption dspOption = CDTUtil.getOptionForSwitch(settings.getCompilerOptions(), "-Xxy");
                Assert.assertTrue("XY option",dspOption != null);
                IWidgetReference ref = settings.findWidgetLocatorForOption(dmulpfOption);
                Assert.assertTrue("dmulpf widget exists",ref != null);
                pause(ui,400); // Appears to be a delay to disable a widget?
                Assert.assertTrue("Initially disabled",!settings.isEnabled(dmulpfOption));
                
                settings.setOptionValue(dspOption,Boolean.TRUE);
                Assert.assertTrue("Now enabled",EclipseUtil.isEnabled(ref));
                
                settings.setOptionValue(dmulpfOption,Boolean.TRUE);              
            }});
    	
    	this.bringUpDebugLaunchDialog(new IUIRunnable(){

            @Override
            public void run (IUIContext ui) throws WidgetSearchException {
                createNewLaunchFromLaunchDialog(ui,LAUNCH,PROJECT);
                ui.click(EclipseUtil.computeTreeItemLocator(ui,"Simulator Extensions/DSP Memory"));
                ButtonLocator xyButton = new ButtonLocator("XY memory");
                ButtonLocator noDSP = new ButtonLocator("No DSP memory support");
                ui.assertThat(new IsSelectedCondition(xyButton,true));
                ui.assertThat(new IsEnabledCondition(noDSP,false));
                
                ui.click(EclipseUtil.computeTreeItemLocator(ui,"Simulator Extensions/DSP Instructions"));
                ButtonLocator dmulpfButton = new ButtonLocator("dual 32x16.*");
                ui.assertThat(new IsSelectedCondition(dmulpfButton,true));
                ui.assertThat(new IsEnabledCondition(dmulpfButton,false));
                
            }});
    	
    	this.bringUpBuildSettings(PROJECT,new IUIToolOptionSetter(){

            @Override
            public void run (IUIContext ui, IToolOptionSetting settings) throws WidgetSearchException {
                IOption dmulpfOption = CDTUtil.getOptionForSwitch(settings.getCompilerOptions(), "-Xdmulpf");
                IOption dspOption = CDTUtil.getOptionForSwitch(settings.getCompilerOptions(), "-Xxy");
                settings.setOptionValue(dspOption,"None");
                ui.wait(milliseconds(100));
                Assert.assertTrue(!settings.isEnabled(dmulpfOption));
            }});
    	
    	this.bringUpDebugLaunchDialog(new IUIRunnable(){

            @Override
            public void run (IUIContext ui) throws WidgetSearchException {
                selectLaunchFromLaunchDialog(ui,LAUNCH);
                ui.click(new CTabItemLocator("Debugger"));
                ui.click(EclipseUtil.computeTreeItemLocator(ui,"Simulator Extensions/DSP Memory"));
                ButtonLocator xyButton = new ButtonLocator("XY memory");
               // ui.assertThat(new IsSelectedCondition(xyButton,false));
                ui.assertThat(new IsEnabledCondition(xyButton,true));
                
                ui.click(EclipseUtil.computeTreeItemLocator(ui,"Simulator Extensions/DSP Instructions"));
                ButtonLocator dmulpfButton = new ButtonLocator("dual 32x16.*");
                //ui.assertThat(new IsSelectedCondition(dmulpfButton,false));
                ui.assertThat(new IsEnabledCondition(dmulpfButton,true));
                
                ui.click(EclipseUtil.computeTreeItemLocator(ui,"Simulator Extensions/DSP Memory"));
                ui.click(new ButtonLocator("No DSP memory support"));
                ui.assertThat(new IsSelectedCondition(xyButton,false));
                
                ui.click(EclipseUtil.computeTreeItemLocator(ui,"Simulator Extensions/DSP Instructions"));
                ui.assertThat(new IsEnabledCondition(dmulpfButton,false));
                
            }});
    	EclipseUtil.deleteLaunch(LAUNCH);
    	this.deleteProject(PROJECT);
    }

}