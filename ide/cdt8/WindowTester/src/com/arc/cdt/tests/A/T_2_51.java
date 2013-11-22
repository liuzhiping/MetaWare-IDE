package com.arc.cdt.tests.A;
import junit.framework.Assert;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.TabItemLocator;

public class T_2_51 extends UIArcTestCaseSWT {
    
    public static final String DESCRIPTION = "Confirm that appropriate error parsers are selected.";
    public static final String CATEGORY = PROJECT_MANAGEMENT;
	private static final String PROJECT = "LEP";

	/**
	 * Main test method.
	 */
	public void testT_2_51() throws Exception {
	    switchToCPerspective(); //in case previous test left in wrong perspective
	    deleteProject(PROJECT);
		this.createNewProject(PROJECT);
		
		this.bringUpPropertiesDialog(PROJECT, new IUIRunnable(){

			@Override
			public void run(IUIContext ui) throws WidgetSearchException {
				ui.click(computeTreeItemLocator("C\\/C++ Build/Settings"));
				ui.click(new TabItemLocator("Error [Pp]arsers"));
				// verify state of check boxes
				String[] trueItems = { "MetaWare C/C++ Error Parser",
						"CDT GNU Make Error Parser 6.0 (Deprecated)",
						"CDT GNU Make Error Parser 7.0",
						"MetaWare Linker Error Parser",
						"ARC Assembler Error parser",
				};
			    String[] falseItems = {
						"CDT Visual C Error Parser",
						"CDT GNU C/C++ Error Parser",
						"CDT GNU Assembler Error Parser",
						"CDT GNU Linker Error Parser"};
			    for (String label: trueItems){
			    	Assert.assertTrue(label,EclipseUtil.isTableItemSelected(ui,label));
			    }
			    for (String label: falseItems){
			    	Assert.assertFalse(label,EclipseUtil.isTableItemSelected(ui,label));
			    }
				//
				ui.click(new ButtonLocator("Cancel"));
				
			}});
		
	    deleteProject(PROJECT);
	}

}