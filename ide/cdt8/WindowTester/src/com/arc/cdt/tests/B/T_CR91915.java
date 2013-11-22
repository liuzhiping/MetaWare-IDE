package com.arc.cdt.tests.B;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Table;

import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.locator.IWidgetReference;
import com.windowtester.runtime.swt.locator.LabeledLocator;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;

public class T_CR91915 extends UIArcTestCaseSWT {
    
    public static final String DESCRIPTION = "Confirm that there are no bogus entries in the discovery profile.";
    public static final String CATEGORY = PROJECT_MANAGEMENT;

	/**
	 * Main test method.
	 */
	public void testT_CR91915() throws Exception {
		registerPerspectiveConfirmationHandler();
	    switchToCPerspective(); //in case previous test left in wrong perspective
		this.bringUpPropertiesDialog("Test1", new IUIRunnable(){

            @Override
            public void run (IUIContext ui) throws WidgetSearchException {
                ui.click(computeTreeItemLocator("C\\/C++ Build/Discovery [oO]ptions"));
                IWidgetReference w = (IWidgetReference)ui.find(new LabeledLocator(Combo.class,"Discovery profile:"));
                compareWidget("T_CR91915.1",w);
                w = (IWidgetReference)ui.find(new SWTWidgetLocator(Table.class));
                compareWidget("T_CR91915.2",w);
                
            }});
	}

}