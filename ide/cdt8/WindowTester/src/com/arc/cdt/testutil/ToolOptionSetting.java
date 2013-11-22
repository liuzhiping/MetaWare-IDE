/*
 * ToolOptionSetting
 * $Revision$
 * $Date$
 *
 * CONFIDENTIAL AND PROPRIETARY INFORMATION.
 * Copyright 2008 ARC International (Unpublished).
 * All Rights Reserved.
 * This document, material and/or software contains confidential and
 * proprietary information of ARC International and is protected by copyright,
 * trade secret and other state, federal, and international laws, and may be
 * embodied in patents issued or pending. Its receipt or possession does not
 * convey any rights to use, reproduce, disclose its contents, or to
 * manufacture, or sell anything it may describe.  Reverse engineering is
 * prohibited, and reproduction, disclosure or use without specific written
 * authorization of ARC International is strictly forbidden.  ARC and the ARC
 * logotype are trademarks of ARC International.
 */
package com.arc.cdt.testutil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;

import com.arc.cdt.testutil.EclipseUtil.IMatch;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.locator.IWidgetReference;
import com.windowtester.runtime.locator.WidgetReference;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
import com.windowtester.runtime.swt.locator.ButtonLocator;
import com.windowtester.runtime.swt.locator.ComboItemLocator;
import com.windowtester.runtime.swt.locator.LabeledTextLocator;
import com.windowtester.runtime.swt.locator.ListItemLocator;
import com.windowtester.runtime.swt.locator.NamedWidgetLocator;
import com.windowtester.runtime.swt.locator.TabItemLocator;

/**
 * Implementing class for programmatically settings the toolset settings.
 * The Settings properties dialog must be showing for this class to operate.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
class ToolOptionSetting implements IToolOptionSetting {
    
    IUIContext ui;
    private List<PathAndOption> optionList = null;
    
    ToolOptionSetting(IUIContext ui){
        this.ui = ui;      
    }
    
    static class PathAndOption {
        PathAndOption(String s, IOption o, Control widget){
            treeItemPath = s;
            option = o;
            control = widget;
        }
        @Override
        public String toString(){
            return treeItemPath + "[" + option.getBaseId() + "]";
        }
        String treeItemPath;
        IOption option;
        Control control;
    }
    
    private static String computeItemPath(TreeItem item){
        TreeItem parent = item.getParentItem();
        String name = item.getText().replaceAll("/","\\\\/");
        if (parent != null){
            return computeItemPath(parent) + "/" + name;
        }
        return name;
    }
    
    private void computeList() throws WidgetSearchException {
        ui.click(EclipseUtil.computeTreeItemLocator(ui,"C\\/C++ Build/Settings"));
        IWidgetLocator tab = ui.find(new TabItemLocator("Tool [Ss]ettings"));
        ui.click(tab);
        final IWidgetReference panel = EclipseUtil.getTabPanel((IWidgetReference)tab);
        IWidgetReference tree = EclipseUtil.findWidgetLocator(new IMatch(){

            @Override
            public boolean matches (Widget widget) {
                return widget instanceof Tree;
            }}, (Control)panel.getWidget(), "Settings tree");
        optionList = new ArrayList<PathAndOption>();
        final Set<String>seenOptions = new HashSet<String>();
        EclipseUtil.walkTree((Tree)tree.getWidget(),new ITreeItemVisitor(){

            @Override
            public boolean visit (TreeItem item) {
                try {
                    //ui.click(new WidgetReference(item)); // Not permissable from UI thread on Linux
                	item.getParent().showItem(item);
	                EclipseUtil.clickItem(item);
        			
                    IWidgetReference optionsWidgets[] = EclipseUtil.findAllWidgetLocators(new IMatch(){

                        @Override
                        public boolean matches (Widget widget) {
                            return widget.getData("option") != null;
                        }}, (Control)panel.getWidget());
                    String itemPath = computeItemPath(item);
                    for (IWidgetReference ref: optionsWidgets){
                        IOption opt = (IOption)((Widget)ref.getWidget()).getData("option");
                        if (seenOptions.add(opt.getBaseId()))
                            optionList.add(new PathAndOption(itemPath,opt,(Control)ref.getWidget()));
                    }
                }
                catch (WidgetSearchException e) {
                    Assert.fail(e.getMessage());
                }
                return true;
            }});
        
//        for (Object p: optionList){ 
//            System.out.println("Found: " + p);
//        }
//        System.out.println("Done");
    }
    
    protected  PathAndOption[] getOptions() throws WidgetSearchException{
        if (optionList == null){
            computeList();
        }
        return optionList.toArray(new PathAndOption[optionList.size()]);
    }

    @Override
    public IOption[] getArchiverOptions () {
        // @todo Auto-generated method stub
        return new IOption[0];
    }

    @Override
    public IOption[] getAssemblerOptions () throws WidgetSearchException {
        return computeOptions(".*Assembler.*");
    }

    @Override
    public IOption[] getCompilerOptions () throws WidgetSearchException {
        return computeOptions(".*ompiler.*");
    }

    @Override
    public IOption[] getLinkerOptions () throws WidgetSearchException {
        return computeOptions(".*inker.*");
    }
    
    private IOption[] computeOptions(String string) throws WidgetSearchException{
        List<IOption> list = new ArrayList<IOption>();
        for (PathAndOption p: getOptions()){
            String item = p.treeItemPath.replaceAll("\\\\/","%%%").split("/")[0]; //deal with escaped slashes
            item = item.replaceAll("%%%","/");
            if (item.matches(string)){
                list.add(p.option);
            }        
        }
        return list.toArray(new IOption[list.size()]);
    }

    @Override
    public Object getValue (final IOption option) throws WidgetSearchException {
        final Control control = findWidgetFor(option);
        if (control != null) {
        	if (control.getDisplay().getThread() == Thread.currentThread()){
        		return getValue(control);
        	}
            final Object[] result = new Object[1];
            control.getDisplay().syncExec(new Runnable() {

                @Override
                public void run () {
                    result[0] = getValue(control);
                }
            });
            if (result[0] != null)
               return result[0];
        }
        throw new WidgetSearchException("Cannot find widget for option " + option.getBaseId());
    }

    @Override
    public boolean isEnabled (final IOption option) throws WidgetSearchException {
        final Control control = findWidgetFor(option);
        if (control != null){
        	if (control.getDisplay().getThread() == Thread.currentThread()){
        	    String treeItemPath = findItemPath(option);
                Assert.assertTrue("Tree Item Path to " + option.getName(),treeItemPath != null);
                EclipseUtil.clickItem(EclipseUtil.computeTreeItem((Control)ui.getActiveWindow(), treeItemPath));
        		return control.getEnabled();
        	}
            final boolean result[] = new boolean[1];
            final WidgetSearchException x[] = new WidgetSearchException[1];
            control.getDisplay().syncExec(new Runnable(){

                @Override
                public void run () {
                    try {
                        result[0] = isEnabled(option);
                    }
                    catch (WidgetSearchException e) {
                        x[0] = e;
                    }
                    
                }});
            if (x[0] != null) throw x[0];
            return result[0];
        }
        throw new WidgetSearchException("Cannot find widget for option " + option.getBaseId());
    }
    
    private Control findWidgetFor(IOption opt) throws WidgetSearchException{
        for (PathAndOption p: getOptions()){
            if (p.option == opt) return p.control;
        }
        return null;
    }
    
    private String findItemPath(IOption opt) throws WidgetSearchException{
        for (PathAndOption p: getOptions()){
            if (p.option == opt) return p.treeItemPath;
        }
        return null;
    }
    
/*    private static boolean compareStringList(String[] list1, String[] list2){
        if (list1 == null) return list2 == null;
        if (list2 == null) return false;
        if (list1.length != list2.length) return false;
        for (int i = 0; i < list1.length; i++){
            if (!list1[i].equals(list2[i])) return false;
        }
        return true;
    }*/

    @Override
    public void setOptionValue (IOption option, final Object value) throws WidgetSearchException {
        final Control control = findWidgetFor(option);

        if (control != null) {
            String treeItemPath = findItemPath(option);
            Assert.assertTrue("Tree Item Path to " + option.getName(),treeItemPath != null);
            ui.click(EclipseUtil.computeTreeItemLocator(ui, treeItemPath));
            if (!value.equals(getValue(option))) {
                IWidgetReference ref = new WidgetReference<Control>(control);
                if (value instanceof Boolean){
                    ui.click(ref);
                }
                else if (value instanceof String){
                    //ui.ensureThat(ref.hasFocus());
                    EclipseUtil.setFocus(ref);
                    try {
                        if (option.getValueType() == IOption.ENUMERATED){
                            String enumName = (String)value;
                            ui.click(new ComboItemLocator(enumName, new NamedWidgetLocator(option.getBaseId())));
//                            String[] enums = option.getApplicableValues();
//                            if (!Arrays.asList(enums).contains(enumName)) {
//                                for (String e: enums){
//                                    if (option.getEnumeratedId(e).equals(value)){
//                                        enumName = e;
//                                        break;
//                                    }
//                                }
//                            }
//                            ui.click(new ComboItemLocator(enumName,ref));
                        }
                        else {                       
                            ui.enterText((String)value);
                        }
                    }
                    catch (BuildException e) {
                        Assert.fail("Build exception while setting " + option.getName() + " to " + value);
                    }
                }
                else if (value instanceof String[]){
                    IWidgetReference toolbar = EclipseUtil.findToolBarRef(EclipseUtil.getParent(new WidgetReference<Widget>(control)));
                    if (control instanceof Table)
                       clearTable(option,(Table)control,EclipseUtil.getToolItem(toolbar,1));
                    else if (control instanceof org.eclipse.swt.widgets.List){
                        clearList(option,(org.eclipse.swt.widgets.List)control,EclipseUtil.getToolItem(toolbar,1));
                    }
                    
                    for (String v: (String[])value) {
                        enterTableValue(option,v, EclipseUtil.getToolItem(toolbar,0));
                    }
                }
                else {
                    Assert.fail("Don't know how to set " + option.getName() + " to " + value);
                }
            }
        }
        else
            throw new WidgetSearchException("Cannot find widget for option " + option.getBaseId());
    }
    
    private void clearTable(IOption option,final Table table, IWidgetReference clearButton) throws WidgetSearchException{
        final String[][] list = new String[1][];
        table.getDisplay().syncExec(new Runnable(){

            @Override
            public void run () {
                TableItem items[] = table.getItems();
                list[0] = new String[items.length];
                int i = 0;
                for (TableItem item: items){
                    list[0][i++] = item.getText();
                }
                
            }});
        for (String item: list[0]) {       
            ui.click(new ListItemLocator(item, new NamedWidgetLocator(option.getBaseId())));
            ui.click(clearButton);
        } 
        Assert.assertTrue("Table " + option.getName(),EclipseUtil.getSingleColumnTableContent(table).length==0);
    }
    
    private void clearList(IOption option,final org.eclipse.swt.widgets.List list, IWidgetReference clearButton) throws WidgetSearchException{
     
        for (String item: EclipseUtil.getListContent(list)) {       
            ui.click(new ListItemLocator(item, new NamedWidgetLocator(option.getBaseId())));
            ui.click(clearButton);
        } 
        Assert.assertTrue("List " + option.getName(),EclipseUtil.getListContent(list).length==0);
    }
    
    private void enterTableValue(IOption option, String value, IWidgetReference addItem) throws WidgetSearchException{
        ui.click(addItem);
        ui.wait(new ShellShowingCondition("Enter Value"));
        ui.enterText(value);
        ui.click(new ButtonLocator("OK"));
        ui.wait(new ShellDisposedCondition("Enter Value"));
        
    }
    
    @Override
    public void restoreDefault() throws WidgetSearchException {
        ui.click(new ButtonLocator("Restore &Defaults"));
    }
    
    private void clickItem(String path) throws WidgetSearchException{
    	// Linux can't handle calls to IUIContext.click() from within UI thread.
    	if (PlatformUI.getWorkbench().getDisplay().getThread() == Thread.currentThread()){
    		TreeItem item = EclipseUtil.computeTreeItem((Control)ui.getActiveWindow(),path);
    		EclipseUtil.clickItem(item);
    	}
    	else {
    		ui.click(EclipseUtil.computeTreeItemLocator(ui,path));
    	}
    }

    @Override
    public String getAssemblerCommandLine () throws WidgetSearchException {
        clickItem(".*Assembler");
        return extractAllOptions();
    }

    @Override
    public String getCompilerCommandLine () throws WidgetSearchException {
        clickItem(".*Compiler");
        return extractAllOptions();
        
    }

    private String extractAllOptions () throws WidgetSearchException {
        IWidgetLocator[] locs = ui.findAll(new LabeledTextLocator("All options:"));
        if (locs == null || locs.length != 1) {
        	EclipseUtil.dumpControl((Control)ui.getActiveWindow());
        	throw new WidgetSearchException("Can't find \"All options:\"");
        }
        return EclipseUtil.getText(locs[0]);
    }

    @Override
    public String getLinkerCommandLine () throws WidgetSearchException {
        clickItem(".*Linker");
        return extractAllOptions();
    }
    
    @Override
    public String getArchiverCommandLine () throws WidgetSearchException {
        clickItem(".*Archiver");
        return extractAllOptions();
    }
    
    @Override
    public String getCompilerName() throws WidgetSearchException {
        IWidgetLocator loc = ui.find(EclipseUtil.computeTreeItemLocator(ui,".*Compiler"));
        return EclipseUtil.getText(loc);
    }
    
    @Override
    public String getLinkerName() throws WidgetSearchException {
        IWidgetLocator loc = ui.find(EclipseUtil.computeTreeItemLocator(ui,".*Linker"));
        return EclipseUtil.getText(loc);
    }
    
    @Override
    public String getAssemblerName() throws WidgetSearchException {
        IWidgetLocator loc = ui.find(EclipseUtil.computeTreeItemLocator(ui,".*Assembler"));
        return EclipseUtil.getText(loc);
    }
    
    @Override
    public String getArchiverName() throws WidgetSearchException {
        IWidgetLocator loc = ui.find(EclipseUtil.computeTreeItemLocator(ui,".*Archiver"));
        return EclipseUtil.getText(loc);
    }
    
    @Override
    public boolean hasLinker() throws WidgetSearchException {
        IWidgetLocator loc[] = ui.findAll(EclipseUtil.computeTreeItemLocator(ui,".*Linker"));
        return loc != null && loc.length > 0;
    }

    @Override
    public IWidgetReference findWidgetLocatorForOption (IOption option) throws WidgetSearchException {
        Control c= findWidgetFor(option);
        if ( c == null) throw new WidgetSearchException("Can't find widget for " + option.getName());
        return new WidgetReference<Control>(c);
    }

	private Object getValue(final Control control) {
		Object result = null;
		if (control instanceof Button){
		    result = Boolean.valueOf(((Button)control).getSelection());
		}
		else if (control instanceof Text){
		    result = ((Text)control).getText();
		}
		else if (control instanceof Combo){
		    result = ((Combo)control).getText();
		}
		else if (control instanceof org.eclipse.swt.widgets.List) {
		    result = ((org.eclipse.swt.widgets.List)control).getItems();                    
		}
		return result;
	}

  

}
