

package com.arc.cdt.tests.B;


import java.io.File;

import junit.framework.Assert;

import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

import com.arc.cdt.testutil.EclipseUtil;
import com.arc.cdt.testutil.IUIRunnable;
import com.arc.cdt.testutil.UIArcTestCaseSWT;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.locator.IWidgetReference;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.ComboItemLocator;
import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
import com.windowtester.runtime.swt.locator.TabItemLocator;
import com.windowtester.runtime.swt.locator.TableItemLocator;


public class T_CR97672 extends UIArcTestCaseSWT {

    public static final String DESCRIPTION = "Confirm that we can change a project's type";

    public static final String CATEGORY = PROJECT_MANAGEMENT;

    private static final String PROJECT_NAME = "TEMP";

    /**
     * Main test method.
     */
    public void testT_CR97672() throws Exception {
        switchToCPerspective();
        this.deleteProject(PROJECT_NAME);
        this.createNewProject(PROJECT_NAME);
        this.createSourceFile(PROJECT_NAME,"main.c","void main() {\n\tprintf(\"hello\");\n");
        this.buildProject(PROJECT_NAME);
        File f = EclipseUtil.getProjectRelativeReference(PROJECT_NAME, "Debug/TEMP.elf");
        Assert.assertTrue("exe exists after build",f.exists());
        this.bringUpPropertiesDialog(PROJECT_NAME, new IUIRunnable(){

            @Override
            public void run (IUIContext ui) throws WidgetSearchException {
                IWidgetLocator w = EclipseUtil.computeTreeItemLocator(ui,"C\\/C++ Build/Tool Chain Editor");
                ui.click(w);
                w = ui.find(new SWTWidgetLocator(Text.class, new SWTWidgetLocator(Group.class, "Used tools")));
                compareWidget("T_CR97672.1",(IWidgetReference)w,"Used tools list");
                
                ui.click(new ButtonLocator("Select Tools..."));
        		ui.wait(new ShellShowingCondition("Select tools"));
        		ui.click(new ButtonLocator("Allow all changes"));
        		ui.click(new TableItemLocator("MetaWare Linker for ARCompact"));
        		ui.click(new ButtonLocator("<-- Remove tool"));
        		ui.click(new TableItemLocator("MetaWare Archiver for ARCompact"));
        		ui.click(new ButtonLocator("Add tool -->"));
        		ui.click(new ButtonLocator("OK"));
        		ui.wait(new ShellDisposedCondition("Select tools"));
                
                ui.click(EclipseUtil.computeTreeItemLocator(ui,"C\\/C++ Build/Settings"));
                ui.click(new TabItemLocator("Build Artifact"));
                ui.click(new ComboItemLocator("Static Library", new SWTWidgetLocator(Combo.class, 0, new SWTWidgetLocator(
                    Composite.class))));
                ui.click(new TabItemLocator("Tool Settings"));
                compareWidget("T_CR97672.2",(IWidgetReference)ui.find(new SWTWidgetLocator(Tree.class,
                            new SWTWidgetLocator(SashForm.class))));
                
            }});
         this.buildProject(PROJECT_NAME);
         //Assert.assertTrue("exe no longer exists",!EclipseUtil.getProjectRelativeReference(PROJECT_NAME,"Debug/TEMP.elf").exists());
         Assert.assertTrue("archive exists",EclipseUtil.getProjectRelativeReference(PROJECT_NAME,"Debug/TEMP.a").exists());
         this.deleteProject(PROJECT_NAME);
         
    }

}