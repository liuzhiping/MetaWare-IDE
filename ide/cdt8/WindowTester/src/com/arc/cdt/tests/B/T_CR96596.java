package com.arc.cdt.tests.B;

import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.condition.IsEnabledCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.ComboItemLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;

public class T_CR96596 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm support for ARC 600 core1 through core4 specification.";
    public static final String CATEGORY = PROJECT_MANAGEMENT;
	private static final String PROJECT = "Queens_AC";

	/**
	 * Main test method.
	 */
	public void testT_CR96596() throws Exception {
	    switchToCPerspective(); //in case previous test left in wrong perspective
		
		this.bringUpPropertiesDialog(PROJECT, new IUIRunnable(){

			@Override
			public void run(IUIContext ui) throws WidgetSearchException {
				
				ui.click(computeTreeItemLocator("C\\/C++ Build/Settings"));
				
				ui.click(computeTreeItemLocator(
					"MetaWare ARC C\\/C++ Compiler/Processor\\\\/Extensions"));
				ui.click(new ComboItemLocator("ARC 700", new NamedWidgetLocator("arc.compiler.options.target.version")));
				NamedWidgetLocator arc6core = new NamedWidgetLocator("arc.compiler.options.arc6core");
				NamedWidgetLocator arc5core = new NamedWidgetLocator("arc.compiler.options.arc5core");
				ui.assertThat(new IsEnabledCondition(arc6core,false));
				ui.assertThat(new IsEnabledCondition(arc5core,false));
				ui.click(new ComboItemLocator("ARC 600", new NamedWidgetLocator("arc.compiler.options.target.version")));
				ui.assertThat(new IsEnabledCondition(arc6core,true));
                ui.assertThat(new IsEnabledCondition(arc5core,false));
                ui.click(new ComboItemLocator("Core 4",arc6core));
                ui.click(new ButtonLocator("Cancel"));
				
			}});
	}

}