package com.arc.cdt.tests.B;

import org.eclipse.swt.widgets.Shell;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.swt.locator.ButtonLocator;

public class T_CR91950 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm support for ARC 700 SIMD instructions.";
    public static final String CATEGORY = PROJECT_MANAGEMENT;
	private static final String PROJECT = "SIMD";

	/**
	 * Main test method.
	 */
	public void testT_CR91950() throws Exception {
		registerPerspectiveConfirmationHandler();
	    switchToCPerspective(); //in case previous test left in wrong perspective
	    deleteProject(PROJECT);
		createNewProject(PROJECT);
		
		this.bringUpPropertiesDialog(PROJECT, new IUIRunnable(){

			@Override
			public void run(IUIContext ui) throws WidgetSearchException {
				
				Shell thisDisplay = (Shell)ui.getActiveWindow();
				ui.click(computeTreeItemLocator("C\\/C++ Build/Settings"));
				/*
				ui.click(new TreeItemLocator(
					"MetaWare ARC C\\/C++ Compiler/Processor\\\\/Extensions",
					new SWTWidgetLocator(Tree.class, new SWTWidgetLocator(
						SashForm.class))));
				*/
				ui.click(computeTreeItemLocator(
					"MetaWare ARC C\\/C++ Compiler/Processor\\\\/Extensions"));
				EclipseUtil.setComboBox(ui,"ARC 700");
				//ui.click(new ComboItemLocator("ARC 700", EclipseUtil.findComboLocator("ARC 700", (Control)ui.getActiveWindow())));
				/*
				ui.click(new TreeItemLocator(
					"MetaWare ARC C\\/C++ Compiler/ARC 700 SIMD Support",
					new SWTWidgetLocator(Tree.class, new SWTWidgetLocator(
						SashForm.class))));
				*/
				EclipseUtil.setActiveShell(thisDisplay); // Get around bug that occurs from time to time
				ui.click(computeTreeItemLocator(
						"MetaWare ARC C\\/C++ Compiler/ARC 700 SIMD Support"));
				ui.click(new ButtonLocator("Enable MX Support (-Xsimd) [ARC700 only]"));
				
			}});

	    deleteProject(PROJECT);
	}

}