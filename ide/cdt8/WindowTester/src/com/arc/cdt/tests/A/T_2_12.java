package com.arc.cdt.tests.A;

import junit.framework.Assert;

import org.eclipse.swt.widgets.Text;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.locator.IWidgetReference;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
import com.windowtester.runtime.swt.locator.TableItemLocator;

/**
 * Checks that discovery profile is honored when computing symbols and such.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class T_2_12 extends UIArcTestCaseSWT {
    public static final String DESCRIPTION = "Confirm that \"discovery\" of predefined paths and include directories works.";
    public static final String CATEGORY = PROJECT_MANAGEMENT;
    
    private static final String PROJECT_NAME = "Queens_AC";
	/**
	 * Main test method.
	 */
	public void testT_2_12() throws Exception {
		this.registerPerspectiveConfirmationHandler();
	    this.switchToCPerspective();
	    this.setDefaultBuildProperties(PROJECT_NAME);
	    this.buildProject(PROJECT_NAME);
	    this.bringUpPropertiesDialogDiscoverOptions(PROJECT_NAME, new IUIRunnable(){

            @Override
            public void run (IUIContext ui) throws WidgetSearchException {
                ui.click(new ButtonLocator("Restore &Defaults"));               
            }});
		this.comparePathsAndSymbols(PROJECT_NAME,"T_2_12.1");
		
		this.bringUpPropertiesDialogDiscoverOptions(PROJECT_NAME, new IUIRunnable(){

            @Override
            public void run (IUIContext ui) throws Exception {
                ui.assertThat(new ButtonLocator("Automate discovery of paths and symbols").isSelected());
                ui.assertThat(new ButtonLocator("Report path detection problems").isSelected());
                IWidgetLocator tableItem = ui.find(new TableItemLocator("MetaWare C Input"));
                IWidgetReference table = EclipseUtil.getParent((IWidgetReference)tableItem);
                compareWidget("T_2_12.table",table);
                IWidgetLocator textFields[] = ui.findAll(new SWTWidgetLocator(Text.class));
                Assert.assertTrue(textFields.length == 3);
                IWidgetLocator cmdField = null;
                IWidgetLocator argsField = null;
                for (IWidgetLocator loc: textFields){
                    String text = EclipseUtil.getText(loc);
                    if (text == null || text.length() == 0){
                        argsField = loc;
                    }
                    else if (text.indexOf("type") < 0){
                        cmdField = loc;
                    }
                    else if (text.equals("hcac"))
                        cmdField = loc;
                }
                Assert.assertTrue(argsField != null);
                String cmdText = cmdField != null?EclipseUtil.getText(cmdField):"???";
                Assert.assertTrue(cmdText,"hcac".equals(cmdText));
                EclipseUtil.clearTextField(argsField);
                if (argsField != null)
                    ui.ensureThat(((SWTWidgetLocator)argsField).hasFocus());
                ui.enterText("-DFOOBAR=1 -I/MyIncludes");
                
            }});
            
		this.buildProject(PROJECT_NAME);
	    this.comparePathsAndSymbols(PROJECT_NAME,"T_2_12.2");
	    this.bringUpPropertiesDialogDiscoverOptions(PROJECT_NAME, new IUIRunnable(){

            @Override
            public void run (IUIContext ui) throws WidgetSearchException {
                ui.click(new ButtonLocator("Restore &Defaults"));               
            }});
		
	    this.buildProject(PROJECT_NAME);
        this.comparePathsAndSymbols(PROJECT_NAME,"T_2_12.3");
	}
}