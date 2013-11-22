

package com.arc.cdt.tests.A;


import junit.framework.Assert;

import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;
import com.windowtester.runtime.swt.locator.TabItemLocator;


public class T_2_04 extends UIArcTestCaseSWT {
    
    public static final String CATEGORY = BUILD_MANAGEMENT;
    public static final String DESCRIPTION = "Confirm that pre and post build commands work.";

    private static final String PROJECT_NAME = "Queens_AC";

    /**
     * Test if pre and post steps work for external and internal builder.
     */
    private void doTest () throws Exception {
        final NamedWidgetLocator preCmd = new NamedWidgetLocator("preCmd");
        final NamedWidgetLocator postCmd = new NamedWidgetLocator("postCmd");
        bringUpPropertiesDialog(PROJECT_NAME, new IUIRunnable() {

            @Override
            public void run (IUIContext uiContext) throws WidgetSearchException {
                uiContext.click(computeTreeItemLocator("C\\/C++ Build/Settings"));
                uiContext.click(new TabItemLocator("Build [Ss]teps"));

                uiContext.click(preCmd);
                uiContext.keyClick(WT.CTRL,'A');
                uiContext.enterText("echo starting");

                uiContext.click(postCmd);
                uiContext.keyClick(WT.CTRL,'A');
                uiContext.enterText("echo ending");

            }
        });
        cleanProject(PROJECT_NAME);
        buildProject(PROJECT_NAME);

        String console = getConsoleContent();
        int i1 = console.indexOf("\nstarting");
        int i2 = console.indexOf("\nending");
        if (i1 <= 0 || i2 <= 0 || i1 >= i2){
        	this.writeStringToFile("T_2_4.txt", console);
        }
        Assert.assertTrue("starting found", i1 > 0);
        Assert.assertTrue("ending found", i2 > 0);
        Assert.assertTrue(i1 < i2);
        char nl = console.charAt(i1+9);
        Assert.assertTrue("starting ending",nl == '\n' || nl == '\r');
        nl = console.charAt(i2+7);
        Assert.assertTrue("ending ending",nl == '\n' || nl == '\r');

        bringUpPropertiesDialog(PROJECT_NAME, new IUIRunnable() {

            @Override
            public void run (IUIContext uiContext) throws WidgetSearchException {
                uiContext.click(new TabItemLocator("Build [sS]teps"));
                uiContext.click(preCmd);
                uiContext.keyClick(WT.CTRL,'A');
                uiContext.keyClick((char) 127); // delete
                uiContext.click(postCmd);
                uiContext.keyClick(WT.CTRL,'A');
                uiContext.keyClick((char) 127); // delete

            }
        });

        cleanProject(PROJECT_NAME);
        buildProject(PROJECT_NAME);
        console = getConsoleContent();
        i1 = console.indexOf("starting");
        i2 = console.indexOf("ending");
        Assert.assertTrue("starting found", i1 == -1);
        Assert.assertTrue("ending found", i2 == -1);
    }

    private void setBuilder (boolean internal) throws WidgetSearchException {
        setBuilder(PROJECT_NAME, internal);
    }

    public void testT_2_04 () throws Exception {
        this.switchToCPerspective();
        this.setDefaultBuildProperties(PROJECT_NAME);
        setBuilder(false);
        doTest();

        setBuilder(true);
        doTest();

        setBuilder(false);
    }

}