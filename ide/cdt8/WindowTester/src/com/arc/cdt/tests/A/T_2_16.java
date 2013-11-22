package com.arc.cdt.tests.A;

import junit.framework.Assert;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IOption;

import com.arc.cdt.testutil.CDTUtil;
import com.arc.cdt.testutil.IToolOptionSetting;
import com.arc.cdt.testutil.IUIToolOptionSetter;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;

public class T_2_16 extends UIArcTestCaseSWT {
	
    public static final String CATEGORY = BUILD_MANAGEMENT;
	public static final String DESCRIPTION = "Confirm that applicable settings are propagated from the compiler to the linker and assembler";
	
	private static String PROJECT = "Queens_AC";

	/**
	 * Main test method.
	 */
	public void testT_2_16() throws Exception {
		final WidgetSearchException exception[] = new WidgetSearchException[1];
		this.switchToCPerspective();
		this.bringUpBuildSettings(PROJECT, new IUIToolOptionSetter(){

			@Override
			public void run(IUIContext ui, IToolOptionSetting settings) {
				try {
					CDTUtil.setCompilerSwitch("-a5",settings,true);
				} catch (WidgetSearchException e) {
					exception[0] = e;
				}
				
			}});
		if (exception[0] != null) throw exception[0];
		
		this.bringUpBuildSettings(PROJECT, new IUIToolOptionSetter(){

			@Override
			public void run(IUIContext ui, IToolOptionSetting settings)
					throws WidgetSearchException {
				IOption opt = CDTUtil.getOptionForSwitch(settings.getLinkerOptions(), "-a5");
				Assert.assertTrue("-a5 linker switch",opt != null);
				if (opt == null) return; // keeps compiler happy
				try {
					String selection = opt.getSelectedEnum();
					String switchName = opt.getEnumCommand(selection);
					Assert.assertTrue("-a5 linker option set","-a5".equals(switchName));
				} catch (BuildException e) {
					Assert.fail(e.getMessage());
				}
				opt = CDTUtil.getOptionForSwitch(settings.getAssemblerOptions(),"-a5");
				try {
					String selection = opt.getSelectedEnum();
					String switchName = opt.getEnumCommand(selection);
					Assert.assertTrue("-a5 assembler option set","-a5".equals(switchName));
				} catch (BuildException e) {
					Assert.fail(e.getMessage());
				}
				settings.restoreDefault();
				
			}});
	}

}