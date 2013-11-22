

package com.arc.cdt.tests.B;


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
import com.windowtester.runtime.swt.locator.CTabItemLocator;
import com.windowtester.runtime.swt.locator.ComboItemLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;


public class T_CR102177 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that the new ARCv2 options are supported in the "+
            "compiler and debugger, and that they are implicitly set in the debug launch if present as " +
            "a compiler option. Also confirm that it is enabled/disabled when appropriate.";
    public static final String CATEGORY = BUILD_MANAGEMENT;
    private static final String PROJECT = "CR102177_Project";
    private static final String LAUNCH = "CR102177_LAUNCH";
    
    private static final String boolArgsAv2Only[] = {"-Xcd", "-Xsa", "-Xdiv_rem","-Xatomic"};
    private static final String av2Xlib[] = {"-Xbs", "-Xnorm", "-Xswap", "-Xmpy", "-Xdiv_rem", "-Xcd"};

    /**
     * Main test method.
     */
    public void testT_CR102177() throws Exception {
       
        this.switchToCPerspective();
        EclipseUtil.deleteLaunch(LAUNCH);
    	this.deleteProject(PROJECT);
    	this.createNewProject(PROJECT);
    	this.createSourceFile(PROJECT, "main.c", "void main(){}");
    	this.buildProject(PROJECT);
    	
    	this.bringUpBuildSettings(PROJECT,new IUIToolOptionSetter(){

            @Override
            public void run (IUIContext ui, IToolOptionSetting settings) throws WidgetSearchException {
                ui.click(computeTreeItemLocator(
                "MetaWare ARC C\\/C++ Compiler/Processor\\\\/Extensions"));
                ui.click(new ComboItemLocator("ARCv2EM", new NamedWidgetLocator("arc.compiler.options.target.version")));
                
                IOption xlib = CDTUtil.getOptionForSwitch(settings.getCompilerOptions(),"-Xlib");
                Assert.assertTrue("-Xlib not found", xlib != null);
                settings.setOptionValue(xlib,Boolean.TRUE);
                for (String arg: av2Xlib) {
                    IOption op = CDTUtil.getOptionForSwitch(settings.getCompilerOptions(), arg);
                    Assert.assertTrue(arg + " exists for ARCv2EM",op != null);
                    Assert.assertTrue(arg + " disabled as expected",!settings.isEnabled(op));
                }
                settings.setOptionValue(xlib,Boolean.FALSE);
                for (String arg: boolArgsAv2Only) {
                    IOption op = CDTUtil.getOptionForSwitch(settings.getCompilerOptions(), arg);
                    Assert.assertTrue(arg + " exists for ARCv2EM",op != null);
                    Assert.assertTrue(arg + " enabled as expected",settings.isEnabled(op));
                    settings.setOptionValue(op,Boolean.TRUE);
                }
                IOption pcOption = CDTUtil.getOptionForSwitch(settings.getCompilerOptions(), "-Hpc_width=");
                Assert.assertTrue("-pc_width exists",pcOption != null);
                Assert.assertTrue("-pc_width enabled as expected",settings.isEnabled(pcOption));
                settings.setOptionValue(pcOption,"24");
                
                IOption lpcOption = CDTUtil.getOptionForSwitch(settings.getCompilerOptions(), "-Hlpc_width=");
                Assert.assertTrue("-lpc_width exists",lpcOption != null);
                Assert.assertTrue("-lpc_width enabled as expected",settings.isEnabled(lpcOption));
                settings.setOptionValue(lpcOption,"16");
                       
            }});
    	
    	this.bringUpDebugLaunchDialog(new IUIRunnable(){

            @Override
            public void run (IUIContext ui) throws WidgetSearchException {
                createNewLaunchFromLaunchDialog(ui,LAUNCH,PROJECT);
                ui.click(new CTabItemLocator("Debugger"));
                String text = EclipseUtil.getText(ui.find(new NamedWidgetLocator("debugger_options")));
                T_CR102177.this.writeStringToFile("T_CR102177.txt", text);
                Assert.assertTrue(text.indexOf("-av2em") >= 0);
                Assert.assertTrue(text.indexOf("-pc_width=24") >= 0);
                Assert.assertTrue(text.indexOf("-lpc_width=16") >= 0);
                for (String arg:boolArgsAv2Only){
                    Assert.assertTrue(arg + " present",text.indexOf(arg) >= 0);
                }
            }});
    	
    	this.bringUpBuildSettings(PROJECT,new IUIToolOptionSetter(){

            @Override
            public void run (IUIContext ui, IToolOptionSetting settings) throws WidgetSearchException {
                ui.click(computeTreeItemLocator(
                "MetaWare ARC C\\/C++ Compiler/Processor\\\\/Extensions"));
                ui.click(new ComboItemLocator("ARC 700", new NamedWidgetLocator("arc.compiler.options.target.version")));
                for (String arg: boolArgsAv2Only) {
                    IOption op = CDTUtil.getOptionForSwitch(settings.getCompilerOptions(), arg);
                    Assert.assertTrue(arg + " exists for ARCv2EM",op != null);
                    Assert.assertTrue(arg + " disabled as expected",!settings.isEnabled(op));
                }
            }});
    	
    	
    	EclipseUtil.deleteLaunch(LAUNCH);
    	this.deleteProject(PROJECT);
    }
}