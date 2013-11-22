/*
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


import static com.windowtester.runtime.condition.TimeElapsedCondition.milliseconds;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.tm.internal.terminal.textcanvas.ITextCanvasModel;
import org.eclipse.tm.terminal.model.ITerminalTextDataReadOnly;
import org.eclipse.tm.terminal.model.LineSegment;
import org.eclipse.tm.terminal.model.Style;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import com.arc.widgets.internal.swt.TextColumn;
import com.windowtester.runtime.IUIContext;
import com.windowtester.runtime.WT;
import com.windowtester.runtime.WaitTimedOutException;
import com.windowtester.runtime.WidgetSearchException;
import com.windowtester.runtime.locator.ILocator;
import com.windowtester.runtime.locator.IWidgetLocator;
import com.windowtester.runtime.locator.IWidgetReference;
import com.windowtester.runtime.locator.WidgetReference;
import com.windowtester.runtime.locator.XYLocator;
import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
import com.windowtester.runtime.swt.locator.ComboItemLocator;
import com.windowtester.runtime.swt.locator.ListItemLocator;
import com.windowtester.runtime.swt.locator.eclipse.ContributedToolItemLocator;



/**
 * Utility methods to do Eclipse-specific things.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class EclipseUtil {
    
    static final String PROJECT_VIEW_ID = "org.eclipse.ui.navigator.ProjectExplorer";
    static final String DEBUG_VIEW_ID = "org.eclipse.debug.ui.DebugView";
    protected static final String LAUNCH_VIEW_ID = "org.eclipse.debug.ui.DebugView"; //$NON-NLS-1$

    public static void dumpControl(Control c){
    	dumpControl(System.out,c);
    }
    public static void dumpControl (final PrintStream out,final Control c) {
        if (c == null){
            out.println("NULL");
        }
        else
        if (c.isDisposed()){
            out.println("Disposed instance of " + c.getClass().getName());
        }
        else
        if (Thread.currentThread() != c.getDisplay().getThread()) {
            c.getDisplay().syncExec(new Runnable() {

                @Override
                public void run () {
                    dumpControl(out,c);
                }
            });
        }
        else
            dumpControl(out, 0, c);
    }
    
    /**
     * Return the name of the active shell.
     * We query this to determine if a recently popped dialog fails to have focus.
     * It happens.
     * @param ui
     * @return name of active shell.
     */
    public static String getActiveShellTitle(IUIContext ui){
        final Shell shell = (Shell)ui.getActiveWindow();
        if (shell != null){
            final String result[] = new String[1];
            shell.getDisplay().syncExec(new Runnable(){

                @Override
                public void run () {
                    result[0] = shell.getText();
                    
                }});
            return result[0];
        }
        return null;       
    }

    /**
     * Wait for a shell with a particular title to materialize. Take into account
     * an Eclipse 3.6 feature in which sometimes they materialize without being "active",
     * therefore the "ShellShowingCondition" doesn't always suffice.
     * @param ui
     * @param title
     * @return
     * @throws WidgetSearchException 
     */
    public static boolean waitForShellShowing(final IUIContext ui, final String title) throws WidgetSearchException{
        Display d = PlatformUI.getWorkbench().getDisplay();
        if (d.getThread() != Thread.currentThread()) {
            final boolean result[] = new boolean[1];
            result[0] = false;
            d.syncExec(new Runnable() {

                @Override
                public void run () {
                    try {
                        result[0] = waitForShellShowing(ui,title);
                    }
                    catch (WidgetSearchException e) {
                        //Shouldn't happen
                        e.printStackTrace();
                    }
                }
            });
            if (!result[0]) throw new WidgetSearchException("Dialog with title \"" + title + "\" did not materialize");
            return result[0];
        }
        
       //Assertion: we're in the main UI thread
       long endTime = System.currentTimeMillis() + 15000; //15 seconds
       while (System.currentTimeMillis() < endTime){
           for (Shell shell: d.getShells()){
               if (shell.getText().equals(title)){
                   shell.setActive();
                   return true;
               }
           }
           ui.wait(milliseconds(1000));
       }
       return false;
    }
    
    public static void setActiveShell(final Shell shell){
        if (shell == null) return;
        Display d = PlatformUI.getWorkbench().getDisplay();
        if (d.getThread() != Thread.currentThread()) {
            d.syncExec(new Runnable() {

                @Override
                public void run () {
                    shell.setActive();
                }
            });
        }
        else shell.setActive();
    }
    /**
     * We have cases where WT fails to find a visible dialog with a particular title.
     * WT's ShellLocator doesn't work. Here is our version.
     * @param title
     * @return locator for shell or null.
     */
    public static IWidgetReference findShell (final String title) {
        Display d = PlatformUI.getWorkbench().getDisplay();
        if (d.getThread() != Thread.currentThread()) {
            final IWidgetReference result[] = new IWidgetReference[1];
            d.syncExec(new Runnable() {

                @Override
                public void run () {
                    result[0] = findShell(title);
                }
            });
            return result[0];
        }
        else {
            Shell shells[] = d.getShells();
            for (Shell s : shells) {
                if (title.equals(s.getText())) {
                    return new WidgetReference<Shell>(s);
                }
            }
        }
        return null;
    }
    
    /**
     * Find a combo box with the given item, and select it in the combo box.
     * @param ui
     * @param item combo box item.
     * @throws WidgetSearchException 
     */
    public static void setComboBox(IUIContext ui, final String item) throws WidgetSearchException{
        IWidgetReference ref = findWidgetLocator(new IMatch(){

            @Override
            public boolean matches (Widget widget) {
                if (widget instanceof Combo){
                    String items[] = ((Combo)widget).getItems();
                    for (int i = 0; i < items.length; i++){
                        if (items[i].equals(item)){
                            //((Combo)widget).select(i);
                            return true;
                        }
                    }
                }
                return false;
            }},(Shell)ui.getActiveWindow(),"Combo with item " + item);
        ui.click(new ComboItemLocator(item,ref));
    }

    private static void dumpControl (PrintStream out, int indent, Control control) {
        indentDot(out, indent);
        out.print(control.getClass().getName());
        if (!control.isEnabled()){
            out.print(" enabled=false");
        }
        if (control.getMenu() != null){
            out.print(" popupMenu=true");
        }
        if (!control.isVisible()){
            out.print(" visible=false");
        }
        if (control.getData("name") != null) {
            out.print(" name=");
            out.print(control.getData("name"));
        }
        if (control.getData("id") != null) {
            out.print(" id=");
            out.print(control.getData("id"));
        }
        if (control instanceof Scrollable){
            Scrollable s = (Scrollable)control;
            ScrollBar vbar = s.getVerticalBar();
            if (vbar != null){
                out.print(" verticalScroll="+vbar.getSelection());
            }
            ScrollBar hbar = s.getHorizontalBar();
            if (hbar != null){
                out.print(" horizontalScroll="+hbar.getSelection());
            }
            if (control instanceof Shell){
            	out.print(" title=\"" + ((Shell)control).getText() + "\"");
            }
        }
        if (control instanceof Label) {
            out.print(" text=");
            out.print(((Label) control).getText());
        }
        else if (control instanceof Button) {
            out.print(" text=");
            out.print(((Button) control).getText());
        }
        else if (control instanceof CLabel) {
            out.print(" text=");
            out.print(((CLabel) control).getText());
        }
        else if (control instanceof Tree) {
            Tree t = (Tree) control;
            out.println(" tree");
            for (TreeItem item : t.getItems()) {
                dumpItem(out, indent + 2, item);
            }
            return;
        }
        else if (control instanceof Group) {
            Group group = (Group) control;
            out.print(" border=");
            out.print(group.getText());
        }
        else if (control instanceof TabFolder) {
            TabFolder tabFolder = (TabFolder) control;
            TabItem items[] = tabFolder.getItems();
            for (TabItem item : items) {
                for (int i = 0; i < indent + 2; i++) {
                    out.print(". ");
                }
                out.print("TAB=");
                out.println(item.getText());
                dumpControl(out, indent + 4, item.getControl());
            }
            return;

        }
        else if (control instanceof CTabFolder) {
            CTabFolder tabFolder = (CTabFolder) control;
            CTabItem items[] = tabFolder.getItems();
            for (CTabItem item : items) {
                for (int i = 0; i < indent + 2; i++) {
                    out.print(". ");
                }
                out.print("TAB=");
                out.println(item.getText());
            }
        }
        else if (control instanceof Text){
            out.print(" text=");
            out.println(((Text)control).getText());
        }
        else if (control instanceof Table){
            Table table = (Table)control;
			for (TableItem item : table.getItems()) {
				out.println();
				for (int i = 0; i < indent + 2; i++) {
					out.print(". ");
				}
				out.print("item:");
				if (table.getColumnCount() > 0) {
					for (int i = 0; i < table.getColumnCount(); i++) {
						out.print("|");
						out.print(item.getText(i));
					}
					out.println("|");
				} else {
					out.println(item.getText());
				}
			}
        }
        else if (control instanceof ToolBar){
            ToolBar tb = (ToolBar)control;
            ToolItem items[] = tb.getItems();
            for (ToolItem item: items){
                out.println();
                for (int i = 0; i < indent+2; i++) {
                    out.print(". ");
                }
                out.print("item ");
                out.print(item.getClass().getName());
                if (item.getData("name") != null){
                    out.print(" name=" + item.getData("name"));
                }
                if (item.getData("id") != null) {
                	out.print(" id=" + item.getData("id"));
                }
                String ctrlID = ContributedToolItemLocator.getAssociatedContributionID(item);
                if (ctrlID != null)
                    out.print(" actionID=" + ctrlID);
                if (item.getText() != null && item.getText().length() > 0){
                    out.print(" text=" + item.getText());
                }
                if (item.getToolTipText() != null && item.getToolTipText().length() > 0){
                    out.print(" tip=" + item.getToolTipText());
                }
                if (!item.isEnabled()) out.print(" enabled=false");
            }
        }
        out.print(" bounds=" + control.getBounds());
        out.println();
        
        if (control instanceof Composite) {
			if (control instanceof Decorations) {
				Menu menu = ((Decorations) control).getMenuBar();
				if (menu != null) {
					indentDot(out, indent + 2);
					out.println("menu (item count=" + menu.getItemCount()
							+ ") [bounds=" + getBounds(menu) + "]:");
					dumpMenu(out, indent + 4, menu);
				}
			}
            Control kids[] = ((Composite) control).getChildren();
            for (Control kid : kids) {
                dumpControl(out, indent + 2, kid);
            }
        }
    }
	private static void indentDot(PrintStream out, int indent) {
		for (int i = 0; i < indent; i++) {
            out.print(". ");
        }
	}
    
    private static void dumpMenu(PrintStream out, int indent, Menu menu){
    	for (MenuItem item: menu.getItems()){
    	    indentDot(out,indent);
    	    out.println("MenuItem: " + item.getText() + "  (bounds:" + getBounds(item) + ")");
    	    Menu subMenu = item.getMenu();
    	    if (subMenu != null){
    	    	dumpMenu(out,indent+2,subMenu);
    	    }
    	}
    }

    private static void dumpItem (PrintStream out, int indent, TreeItem t) {
        if (t == null) return;
        indentDot(out, indent);
        int columnCount = t.getParent().getColumnCount();
        if (columnCount == 0) {
            out.println(t.getText());
        }
        else {
            for (int i = 0; i < columnCount;i++){
                out.print(t.getText(i));
                if ( i+1 < columnCount) out.print(", ");
            }
            out.println();
        }
        TreeItem kids[] = t.getItems();
        for (TreeItem kid : kids) {
            dumpItem(out, indent + 2, kid);
        }
    }

    public static List<TreeItem> findTreeItems (final Control control, final String text) {
        final List<TreeItem> list = new ArrayList<TreeItem>();
        if (Thread.currentThread() != control.getDisplay().getThread()) {
            control.getDisplay().syncExec(new Runnable() {

                @Override
                public void run () {
                    findTreeItem(control, text, list);
                    if (list.size() >= 1) {
                        if (list.size() == 1) {
                            System.out.println("Found single instance of " + text);
                        }
                        else {
                            System.out.println("FOUND MULTIPLE INSTANCES OF " + text + "!!!");
                        }
                        for (TreeItem item : list) {
                            dumpItem(System.out, 2, item);
                        }
                    }
                    else
                        System.out.println("COULD NOT FIND " + text);
                }
            });
        }
        else {
            findTreeItem(control, text, list);
        }

        return list;
    }

    private static void findTreeItem (Control control, String text, List<TreeItem> list) {
        if (control instanceof Tree) {
            for (TreeItem item : ((Tree) control).getItems()) {
                findTreeItem(item, text, list);
            }
        }
        if (control instanceof Composite) {
            for (Control kid : ((Composite) control).getChildren()) {
                findTreeItem(kid, text, list);
            }
        }
    }

    private static void findTreeItem (TreeItem item, String text, List<TreeItem> list) {
        if (Pattern.matches(text, item.getText())) {
            list.add(item);
        }
        for (TreeItem i : item.getItems()) {
            findTreeItem(i, text, list);
        }
    }
    
    /**
     * Given a reference to a Text or Combo widget, clear it.
     * @param loc reference to widget.
     */
    public static void clearTextField(IWidgetLocator loc){
        if (loc instanceof IWidgetReference){
            final Widget widget = (Widget)((IWidgetReference)loc).getWidget();
            if (widget instanceof Text || widget instanceof Combo){
                widget.getDisplay().syncExec(new Runnable(){

                    @Override
                    public void run () {
                        if (widget instanceof Text){
                            ((Text)widget).setText("");
                        }
                        else {
                            ((Combo)widget).setText("");
                        }
                        
                    }});
                
            }
        }
    }
    
    private static void computeAllTreeItems(TreeItem items[], List<TreeItem> list){
        //ASSERTION: we're in the UI thread
        for (TreeItem item: items){
            list.add(item);
            computeAllTreeItems(item.getItems(),list);
        }
        
    }
    
    private static List<TreeItem> computeAllTreeItems(Tree tree){
        //ASSERTION: we're in the UI thread
        List<TreeItem> list = new ArrayList<TreeItem>();
        computeAllTreeItems(tree.getItems(),list);
        return list;
    }
    
    private static final int NO_SELECTION = 1;
    private static final int BAD_INDEX = 2;
    /**
     * Given a tree with an item selected, change the selection by moving
     * up or down by some amount.
     * @param ui
     * @param treeLoc
     * @param amount if > 0, move up, else move down.
     * @throws WidgetSearchException 
     */
    public static void moveTreeSelection(IUIContext ui, IWidgetLocator treeLoc, final int amount) throws WidgetSearchException{
        final Tree tree = (Tree)((IWidgetReference)treeLoc).getWidget();
        final IWidgetReference[] result = new WidgetReference[1];
        final int[] errorCondition = new int[1];
        tree.getDisplay().syncExec(new Runnable(){

            @Override
            public void run () {
                TreeItem selectedItems[]  = tree.getSelection();
                if (selectedItems.length != 1) {
                    errorCondition[0] = NO_SELECTION;
                    return;
                }
                TreeItem selection = selectedItems[0];
                List<TreeItem> allItems = computeAllTreeItems(tree);
                int where = allItems.indexOf(selection);
                if (where >= 0){
                    where -= amount;
                }
                if (where < 0 || where >= allItems.size()){
                    errorCondition[0] = BAD_INDEX;
                    return;
                }
                result[0] = new WidgetReference<TreeItem>(allItems.get(where));
                
            }});
        switch(errorCondition[0]){
            case NO_SELECTION:
                throw new WidgetSearchException("Tree doesn't have a selection");
            case BAD_INDEX:
                throw new WidgetSearchException("Can't bump tree selection");
        }
        ui.click(result[0]);      
    }
    
    /**
     * Write the state of a widget.
     * @param outStream output stream into which to write the state information.
     * @param ref reference to the view widget.
     * @param name node name if not null.
     */
    public static void writeWidgetState(final OutputStream outStream, IWidgetReference ref, String name){
        writeWidgetState(outStream,(Widget)ref.getWidget(),name);      
    }
    
    /**
     * Write the state of a widget.
     * @param outStream output stream into which to write the state information.
     * @param widget the widget.
     * @param name name of widget if not null.
     */
    public static void writeWidgetState(final OutputStream outStream, final Widget widget, final String name){
        widget.getDisplay().syncExec(new Runnable(){

            @Override
            public void run () {
                PrintStream out = new PrintStream(outStream);
                widget.getDisplay().update();
                if (name == null)
                    out.println("<view>");
                else
                    out.println("<view name=\"" + name +"\">");
                writeWidgetState(out,widget,4);
                out.println("</view>");
                
            }});             
    }
    
    protected static void indent(PrintStream out, int amount){
        for (int i = 0; i < amount; i++){
            out.print(' ');
        }
    }
    
    private static boolean allColumnsAreEmpty(TableItem item, int columns){
        for (int i = 0; i < columns; i++){
            if (item.getText(i).length() > 0) return false;
        }
        return true;
    }
    
    protected static void writeMenuState(PrintStream out, Menu menu, int indent){
        indent(out,indent);
        out.println("<menu>");
        for (MenuItem item: menu.getItems()){
            indent(out,indent+4);
            out.print("<item text=\"");
            out.print(item.getText());
            out.print("\"");
            switch(item.getStyle()){
                case SWT.CASCADE:{
                    Menu submenu = item.getMenu();
                    if (submenu != null){
                        out.println(">");
                        writeMenuState(out,submenu,indent+8);
                        indent(out,indent+4);
                        out.println("</item>");
                    }
                    break;
                }
                case SWT.CHECK:
                case SWT.RADIO:
                    if (item.getSelection()){
                        out.print(" selection=\"1\"");
                    }
                    out.println("/>");
                    break;
                default:
                    out.println("/>");
            }
        }
        indent(out,indent);
        out.println("</menu>");
    }
    
    protected static void writeWidgetState(PrintStream out, Widget c, int indent){
        String disabled="";
        if ((c instanceof Control) && !((Control)c).isEnabled()) disabled = " enabled=\"false\"";
        if (c instanceof Combo){
            indent(out,indent);
            Combo combo = (Combo)c;         
            if ((combo.getStyle() & SWT.READ_ONLY) == 0 || !combo.isEnabled())
                out.println("<combo text=\"" + xlate(combo.getText()) +"\"" + disabled +"/>");
            else {
                out.println("<combo text=\"" + xlate(combo.getText()) + "\"" + disabled +">");
                for (String item: combo.getItems()){
                    indent(out,indent+4);
                    out.println("<item>" + item + "</item>");
                }
                indent(out,indent);
                out.println("</combo>");
            }
        }
        else if (c instanceof CCombo){
            indent(out,indent);
            out.println("<ccombo text=\"" + xlate(((CCombo)c).getText()) + "\"" + disabled + "/>");         
        }
        else if (c instanceof Text){
            indent(out,indent);
            out.println("<text text=\"" + xlate(((Text)c).getText()) + "\"" + disabled +"/>");              
        }
        else if (c instanceof Label){
            indent(out,indent);
            out.println("<label text=\"" + ((Label)c).getText() + "\"" + disabled +"/>");
        }
        else if (c instanceof CLabel){
            indent(out,indent);
            out.println("<label text=\"" + ((CLabel)c).getText() + "\"" + disabled +"/>");
        }
        else if (c instanceof Button){
            Button b = (Button)c;
            if ((b.getStyle() & SWT.CHECK) != 0) {
                indent(out,indent);
                out.print("<checkbox text=\"" + b.getText() + "\"" + disabled);
                if (b.isEnabled()){
                    out.print(" selected=\"" + (b.getSelection()?"1\"":"0\""));
                }
                out.println("/>");
            }
        }
        else if (c instanceof ToolBar){
            // do nothing
        }
        else if (c instanceof TextColumn){
            ((TextColumn)c).recordState(out);
        }
        else if (c instanceof Menu){
            writeMenuState(out,(Menu)c,indent);
        }
        else if (c instanceof Tree){
            indent(out,indent);
            Tree tree = (Tree)c;
            int columnCount = tree.getColumnCount();
            if (columnCount == 0)
                out.println("<tree" + disabled +">");
            else {
                out.println("<tree columns=\"" + columnCount + "\"" + disabled+">");
            }          
            writeTreeItems(out,((Tree)c).getItems(),indent+4,
                Arrays.asList(((Tree)c).getSelection()),((Tree)c).getColumns());
            indent(out,indent);
            out.println("</tree>");
        }
        else if (c instanceof Table){
            indent(out,indent);           
            Table table = (Table)c;
            TableItem items[] = table.getItems();
            int rows = items.length;
            int columns = table.getColumnCount();
            int firstRow = 0;
            if (columns == 0 && items.length > 0) columns = 1; //quirk of SWT single column has 0 columns
            //If the table is virtual, then we don't want to dump un-instantiated rows. The only way
            // I know of to test for this, if all columns in the row are empty.
            if ((table.getStyle() & SWT.VIRTUAL) != 0) {
                firstRow = table.getTopIndex();
                for (int i = firstRow; i < items.length; i++){
                    if (allColumnsAreEmpty(items[i],columns)){
                        rows = i;
                        break;                     
                    }
                }
            }         
            out.println("<table columns=\"" + columns + "\"" + disabled +">");
            //NOTE: register view highlights changes in red.
            // note any odd-columned cell that has a different background than
            // the even-columned cell.
            c.getDisplay().update(); // make sure all paint requests are completed.
            Color normalBackground = items.length > 0 && columns > 0?
                items[0].getBackground(0):null;
            for (int row = firstRow; row < rows; row++){
                TableItem item = items[row];
                indent(out,indent+4);
                out.println("<row>");
                for (int i = 0; i < columns; i++) {
                    indent(out,indent+8);
                    out.print("<cell");
                    if (!item.getBackground(i).equals(normalBackground))
                        out.print(" highlighted=\"true\"");
                    Color foreground = item.getForeground(i);
                    // Show red foreground
                    if (foreground != null && foreground.getRed() > 200 && foreground.getBlue() < 10 && foreground.getGreen() < 10){
                        out.print(" red=\"true\"");
                    }
                    out.print(">");
                    out.print(xlate(item.getText(i)));                   
                    out.println("</cell>");
                }               
                indent(out,indent+4);
                out.println("</row>");
            }
            indent(out,indent);
            out.println("</table>");
        }
        else if (c instanceof StyledText){
            indent(out,indent);
            out.println("<styledText" + disabled +">");
            StyledText text = (StyledText)c;
            if (text.isEnabled()) {
                StyleRange ranges[] = text.getStyleRanges();
                String textString = text.getText();
                int startOffset = 0;
                for (StyleRange range : ranges) {
                    indent(out, indent + 4);
                    if (startOffset < range.start) {
                        out.println("<text>" + textString.substring(startOffset, range.start) + "</text>");
                        indent(out, indent + 4);
                    }
                    startOffset = range.start + range.length;
                    out.print("<text");
                    if (range.foreground != null) {
                        out.print(" foreground=\"" +
                            getColorName(range.foreground) + "\"");
                    }
                    out.print(" fontStyle=\"" +
                            getFontStyleName(range.fontStyle) +
                            "\">");
                    out.print(textString.substring(range.start, range.start + range.length));
                    out.println("</text>");
                }
                if (startOffset < textString.length()) {
                    indent(out, indent + 4);
                    out.print("<text>");
                    out.print(textString.substring(startOffset));
                    out.println("</text>");
                }
            }
            indent(out,indent);
            out.println("</styledText>");
        }
        else if (c instanceof IAdaptable && ((IAdaptable)c).getAdapter(ITextCanvasModel.class) != null) { // Terminal display
            ITextCanvasModel model = (ITextCanvasModel)((IAdaptable)c).getAdapter(ITextCanvasModel.class);
            indent(out,indent);
            out.println("<terminalText>");
            ITerminalTextDataReadOnly data = model.getTerminalText();
            int rows = data.getHeight();
            int cols = data.getWidth();
            for (int lineNo = 0; lineNo < rows; lineNo++){
                indent(out,indent+4);
                out.print("<line>");
                LineSegment segments[] = data.getLineSegments(lineNo, 0, cols);
                for (LineSegment seg : segments) {
                    Style style = seg.getStyle();
                    String text = seg.getText();
                    out.print("<text");
                    if (style != null) {
                        if (style.getBackground() != null) {
                            out.print(" background=\"" + style.getBackground().getName() + "\"");
                        }
                        if (style.getForground() != null) {
                            out.print(" foreground=\"" + style.getForground().getName() + "\"");
                        }
                        if (style.isBold())
                            out.print(" bold=\"1\"");
                        if (style.isUnderline())
                            out.print(" underline=\"1\"");
                    }
                    out.print(">");
                    out.print(text.replaceAll("\\00"," ")); // Blanks are nulls
                    out.print("</text>");
                }
                out.println("</line>");
            }
            indent(out,indent);
            out.println("</terminalText>");
        }
        else if (c instanceof org.eclipse.swt.widgets.List) {
            org.eclipse.swt.widgets.List list = (org.eclipse.swt.widgets.List)c;
            indent(out,indent);
            out.println("<list>");
            for (String item: list.getItems()){
                indent(out,indent+4);
                out.println("<item>" + item + "</item>");
            }
            indent(out,indent);
            out.println("</list>");
        }
        else if (c instanceof Composite){
            Control kids[] = ((Composite)c).getChildren();
            for (Control kid: kids){
                if (kid.isVisible())
                    writeWidgetState(out,kid,indent);
            }
        }      
    }
    
    private static String getColorName(Color color){
        RGB rgb = color.getRGB();
        if (rgb.blue == 0 && rgb.red == 0 && rgb.green == 0){
            return "black";
        }
        if (rgb.blue == 255 && rgb.red == 0 && rgb.green == 0){
            return "blue";
        }
        if (rgb.blue == 0 && rgb.red == 255 && rgb.green == 0){
            return "red";
        }
        if (rgb.blue == 0 && rgb.red == 0 && rgb.green == 255){
            return "green";
        }
        return String.format("#%02x%02x%02x",rgb.red,rgb.green,rgb.blue);     
    }
    
    private static String getFontStyleName(int style){
        switch(style){
            case SWT.ITALIC: return "italic";
            case SWT.BOLD: return "bold";
            case SWT.ITALIC|SWT.BOLD: return "bold/italic";
            default: return "plain";
        }
    }
    
    protected static void writeTreeItems(PrintStream out, TreeItem items[], int indent,
        Collection<TreeItem>selection, TreeColumn columns[]){
        for (TreeItem item: items){
            indent(out,indent);
            out.print("<item");
            if (columns.length == 0) {
                out.print(" text=\"");
                String s = item.getText();
                if (s == null) s = "";
                out.print(xlate(s.trim()));
                out.print("\"");
            }
            if (selection.contains(item)){
                out.print(" selected=\"true\"");
            }
            if (item.getChecked()){
                out.print(" checked=\"true\"");
            }
            TreeItem subItems[] = item.getItems();
            if (columns.length > 0){
                out.println(">");
                for (int i = 0; i < columns.length; i++){
                    //Hidden fields are denoted by columns of zero width. Problems view is
                    // a case in point. It has a hidden 5'th column for the time stamp, which we
                    // do not want to show.
                    if (columns[i].getWidth() > 0) {
                        indent(out,indent+4);
                        out.print("<cell>");
                        out.print(item.getText(i));
                        out.println("</cell>");
                    }
                }               
            }
            else
            if (subItems.length == 0) out.println("/>");
            else {
                out.println(">");
            }
            if (subItems.length > 0){
                writeTreeItems(out,subItems,indent+4,selection,columns);
                indent(out,indent);
                out.println("</item>");
            }
        }
    }
    
    /**
     * WindowTester's ViewLocator doesn't seem to work when trying to get the
     * root component so that we can traverse it. So, we do it here.
     * @param viewID the view ID.
     * @return the top level control of the view.
     * @throws WidgetSearchException 
     */
    public static IWidgetReference findView (final String viewID) throws WidgetSearchException {
        return findView(viewID,null);
    }
    
    /**
     * WindowTester's ViewLocator doesn't seem to work when trying to get the
     * root component so that we can traverse it. So, we do it here.
     * @param viewID the view ID.
     * @param secondary the secondary ID, or null.
     * @return the top level control of the view.
     * @throws WidgetSearchException 
     */
    public static IWidgetReference findView (final String viewID, final String secondary) throws WidgetSearchException {
        final IWidgetReference result[] = new IWidgetReference[1];
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

            @Override
            public void run () {
                IViewPart view = findOpenView(viewID,secondary);
                result[0] = getViewReference(view);
                if (result[0] == null) {
                	IViewReference viewRefs[] = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
                    for (IViewReference viewRef: viewRefs){
                    	System.out.print("viewRef " + viewRef.getId());
                    	if (viewRef.getSecondaryId() != null) {
                    		System.out.print(" secondary=" + viewRef.getSecondaryId());
                    	}
                        System.out.println();
                    }     
                }
            }
        });

        if (result[0] == null) {    
            throw new WidgetSearchException("Couldn't find view " + viewID);
        }
        return result[0];
    }
    
    /**
     * Given a control, return any associated popup menu, if any.
     * @param controlRef the reference to the control.
     * @return the associated popup menu, or <code>null</code>.
     */
    public static Menu getPopupMenu (final IWidgetReference controlRef) {

        if (controlRef.getWidget() instanceof Control) {
            final Menu menu[] = new Menu[1];
            PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
                @Override
                public void run () {
                    Control control = (Control)controlRef.getWidget();
                    menu[0] = getPopupMenu(control);
                }
            });
            return menu[0];
        }
       
        return null;
    }
    
    protected static Menu getPopupMenu (Control control) {
        if (!control.isDisposed()) {
            if (control.getMenu() != null)
                return control.getMenu();
            if (control instanceof Composite) {
                Composite container = (Composite) control;
                for (Control c : container.getChildren()) {
                    Menu m = getPopupMenu(c);
                    if (m != null)
                        return m;
                }
            }
        }
        return null;
    }
    
    @SuppressWarnings("restriction")
    public static IWidgetReference getViewReference (IViewPart view) {
    	if (view == null) return null;
        // IDebugView is the only one for which we know how to get the
        // control.
        if (view instanceof IDebugView) {
            Viewer viewer = ((IDebugView) view).getViewer();
            if (viewer != null && viewer.getControl() != null) {
                return new WidgetReference<Widget>(viewer.getControl());
            }
        }
        else if (view.getSite() instanceof org.eclipse.ui.internal.PartSite){
            return new WidgetReference<Widget>(((org.eclipse.ui.internal.PartSite)view.getSite()).getPane().getControl());                   
        }
        return null;
    }
    
    /**
     * Find a reference to the toolbar of a view.
     * @param viewID the view ID.
     * @return the reference to the toolbar for the view, assuming that it is showing.
     * @throws WidgetSearchException 
     */
    public static IWidgetReference findViewToolBar(final String viewID) throws WidgetSearchException {
        final IWidgetReference ref = findView(viewID);
        final IWidgetReference result[] = new IWidgetReference[1];
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

            @Override
            public void run () {
                Composite c = ((Control)ref.getWidget()).getParent();
                Control control = (Control)ref.getWidget();
                if (control instanceof Composite){
                    c = (Composite)control;
                }
//                System.out.println("Looking at:");
//                dumpControl(c);
                ToolBar toolbar = findToolBar(c,control == c?null:control);
                if (toolbar == null) {
                    Control prev = c;
                    c = c.getParent();
                    if (c != null) {
//                        System.out.println("Looking at parent:");
//                        dumpControl(c);
                        toolbar = findToolBar(c,prev);
                    }
                }
                if (toolbar == null && c != null) {
                    Control prev = c;
                    c = c.getParent();
                    if (c != null) {
//                        System.out.println("Looking at parent of parent:");
//                        dumpControl(c);
                        toolbar = findToolBar(c,prev);
                    }
                }
                if (toolbar != null){
                    result[0] = new WidgetReference<Widget>(toolbar);
                    //dumpControl(toolbar);
                }
            }
        });

        if (result[0] == null)
            throw new WidgetSearchException("Couldn't find toolbar for view " + viewID);
        return result[0];
    }
    
    /**
     * Search a container for a toolbar where there may be more than one toolbar.
     * We want the one immediately preceeding a particular contorl.
     * @param container container to search for toolbar.
     * @param preceedingThis if not null, return the toolbar immediately preceeding this child.
     * @return a toolbar.
     */
    private static ToolBar findToolBar(Composite container, Control preceedingThis){
        ToolBar pendingToolBar = null;
        for (Control kid: container.getChildren()){
            if (kid == preceedingThis && pendingToolBar != null){
                return pendingToolBar;
            }
            if (kid instanceof ToolBar && kid.isVisible()){
                pendingToolBar = (ToolBar)kid;
                if (preceedingThis == null)
                    return pendingToolBar;
            }
        }
        for (Control kid: container.getChildren()){
            if (kid instanceof Composite && kid.isVisible()){
                ToolBar t = findToolBar((Composite)kid,preceedingThis != null?container:null);
                if (t != null) return t;
            }
        }
        return null;
    }
    
    /**
     * Return first toolbar found in container.
     * @param container
     * @return the first toolbar found in container.
     * @throws WidgetSearchException
     */
    public static IWidgetReference findToolBarRef(IWidgetReference container) throws WidgetSearchException{
        final Composite parent = (Composite)container.getWidget();
        final ToolBar result[] = new ToolBar[1];
        parent.getDisplay().syncExec(new Runnable(){

            @Override
            public void run () {
                result[0] = findToolBar(parent,null);
                
            }});
        if (result[0] != null){
            return new WidgetReference<Widget>(result[0]);
        }
        dumpControl(parent);
        throw new WidgetSearchException("Cannot locate toolbar in container");
    }
    
    /**
     * Return a reference to the n'th tool item in a tool bar.
     * @param toolbar the tool bar.
     * @param n the index of the tool item to return.
     * @return a reference to the nt'th tool item in a tool bar.
     * @throws WidgetSearchException 
     */
    public static IWidgetReference getToolItem(final IWidgetReference toolbar, final int n) throws WidgetSearchException{
        final ToolItem result[] = new ToolItem[1];
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

            @Override
            public void run () {
                result[0] = ((ToolBar)toolbar.getWidget()).getItem(n);
            }
        });
        if (result[0] == null) throw new WidgetSearchException("Can't locate item #" + n + " in toolbar");
        return new WidgetReference<Widget>(result[0]);
    }
    /**
     * If a view is open, then make it visible.
     * @param viewID ID of the view.
     * @return true if the view is open and has been made visible; false if the view is not open.
     */
    public static IViewPart makeViewVisible(final String viewID, final String secondaryID) {
        final IViewPart result[] = new IViewPart[1];
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable(){

            @Override
            public void run () {
                IViewPart view = findOpenView(viewID,secondaryID);
                IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                if (view != null) {
                    try {
                        activePage.showView(viewID,secondaryID,IWorkbenchPage.VIEW_ACTIVATE);
                        result[0] = view;
                    }
                    catch (PartInitException e) {
                            // ignore
                    }                   
                }              
            }});
        return result[0];
    }
    
    public static void makeViewVisible(IViewPart view){
       makeViewVisible(view.getViewSite().getId(),view.getViewSite().getSecondaryId());
    }
    
    /**
     * Return all instances of an open view with the given ID. (E.g, Terminal view can
     * have more than one instance.)
     * @param viewID the view ID.
     * @return all instances of open views with the give view ID.
     */
    public static IViewPart[] findOpenViews(final String viewID){
        Display display = PlatformUI.getWorkbench().getDisplay();
        final IViewPart result[][] = new IViewPart[1][];
        if (display.getThread() == Thread.currentThread()){           
            IViewReference viewRefs[] = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
            List<IViewPart>list = new ArrayList<IViewPart>();
            for (IViewReference viewRef: viewRefs){
                if (viewID.equals(viewRef.getId())){
                    IViewPart v = viewRef.getView(false);
                    if (v != null) list.add(v);
                }
            }
            // Sort so that our tests are deterministic.
            Collections.sort(list,new Comparator<IViewPart>(){

                @Override
                public int compare (IViewPart o1, IViewPart o2) {
                    if (o1.getViewSite().getSecondaryId() == null) return -1;
                    if (o2.getViewSite().getSecondaryId() == null) return 1;
                    return o1.getViewSite().getSecondaryId().compareTo(o2.getViewSite().getSecondaryId());
                }});
            return list.toArray(new IViewPart[list.size()]);
        }
        else {
            display.syncExec(new Runnable(){

                @Override
                public void run () {
                    result[0] = findOpenViews(viewID);
                    
                }});
            return result[0];
        } 
    }
    
    /**
     * Return the View if it is open. Otherwise, return null.
     * @param viewID the view to be returned.
     * @return the corresponding view part, or <code>null</code>.
     */
    public static IViewPart findOpenView(final String viewID, final String secondaryID){
        Display display = PlatformUI.getWorkbench().getDisplay();
        if (display.getThread() == Thread.currentThread()){
            IViewReference viewRefs[] = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
            for (IViewReference viewRef: viewRefs){
                if (viewID.equals(viewRef.getId()) && (secondaryID == null ||
                    secondaryID.equals(viewRef.getSecondaryId()))){
                    return viewRef.getView(false);
                }
            }     
            return null;
        }
        else {
            final IViewPart result[] = new IViewPart[1];
            display.syncExec(new Runnable(){

                @Override
                public void run () {
                    result[0] = findOpenView(viewID,secondaryID);
                    
                }});
            return result[0];
        }
    }
    
    /**
     * Delete a project if it exists.
     * @param projectName the name of the project to delete.
     * @return true if the project existed and was deleted; false if the
     * project does not exist.
     * @throws WidgetSearchException if an error occurred while deleting
     * an existing project.
     */
    public static boolean deleteProject(String projectName) throws WidgetSearchException{
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = root.getProject(projectName);
        if (project != null && project.exists()){
            try {
                project.delete(true,true,null);
            }
            catch (CoreException e) {
                throw new WidgetSearchException(e);
            }
            return true;
        }
        return false;      
    }
    
    /**
     * Delete a launch configuration with the give name.
     * @param launchName
     * @throws WidgetSearchException
     */
    public static void deleteLaunch(String launchName) throws WidgetSearchException {
        try {
            for (ILaunchConfiguration config:DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations()){
                if (launchName.equals(config.getName())){
                    config.delete();
                    break;
                }
            }
        }
        catch (CoreException e) {
            throw new WidgetSearchException(e);
        }
    }
    
    /**
     * Return a reference to a file within a project.
     * @param projectName name of the project.
     * @param path project-relative path.
     * @return the File reference.
     */
    public static File getProjectRelativeReference(String projectName, String path){
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject project = root.getProject(projectName);
        IPath dir = project.getLocation();
        return new File(dir.toFile(),path);
    }
    
    public static String getText(IWidgetLocator loc) throws WidgetSearchException{
        if (loc instanceof IWidgetReference){
            return getText((Widget)((IWidgetReference)loc).getWidget());
        }
        throw new WidgetSearchException("Can't locate text field");
    }
    
    private static String getTextFromWidget(Widget widget){
        if (widget instanceof Text){
            return ((Text)widget).getText();
        }
        else if (widget instanceof Combo){
            return ((Combo)widget).getText();
        }
        else if (widget instanceof TreeItem){
            return getFullTreeItemName((TreeItem)widget);
        }
        else if (widget instanceof TableItem){
            return ((TableItem)widget).getText();
        }
        else if (widget instanceof Shell){
            return ((Shell)widget).getText();
        }
        else if (widget instanceof Label){
            return ((Label)widget).getText();
        }
        else if (widget instanceof StyledText){
            return  ((StyledText)widget).getText();
        }
        else if (widget instanceof TextColumn){
            StringBuilder buf = new StringBuilder();
            int cnt = ((TextColumn)widget).getLineCount();
            for (int i = 0; i < cnt; i++){
                if (i > 0) buf.append('\n');
                buf.append( ((TextColumn)widget).getLine(i));
            }
            return buf.toString();
        }
        else if (widget instanceof Composite){
            Control kids[] = ((Composite)widget).getChildren();
            // Go backwards so as to likely see a "Text" before seeing a "Label".
            for (int i = kids.length-1; i >= 0; i--){
                String s = getTextFromWidget(kids[i]);
                if (s != null) return s;
            }          
        }
        return null;
    }
    
    public static String getText(final Widget widget) throws WidgetSearchException{
        final String result[] = new String[1];
        widget.getDisplay().syncExec(new Runnable(){

            @Override
            public void run () {
                result[0] = getTextFromWidget(widget);
                
            }});
        if (result[0] == null){
            dumpControl((Control)widget);
            throw new WidgetSearchException("Widget " + widget.getClass().getName() + " is not text");
        }
        return result[0];    
    }
    
    /**
     * Given the reference to a tab item, return its panel.
     * @param tab reference to tab item.
     * @return reference to associated panel.
     */
    public static IWidgetReference getTabPanel(IWidgetReference tab){
        final TabItem tabItem = (TabItem)tab.getWidget();
        final IWidgetReference result[] = new IWidgetReference[1];
        tabItem.getDisplay().syncExec(new Runnable(){
            @Override
            public void run () {
                result[0] = new WidgetReference<Widget>(tabItem.getControl());
                //dumpControl(tabItem.getControl()); //TOBEREMOVED
                
            }});
        return result[0];
    }
    
    /**
     * Given the reference to a tab item, return its panel.
     * @param tab reference to tab item.
     * @return reference to associated panel.
     */
    public static IWidgetReference getCTabPanel(IWidgetReference tab){
        final CTabItem tabItem = (CTabItem)tab.getWidget();
        final IWidgetReference result[] = new IWidgetReference[1];
        tabItem.getDisplay().syncExec(new Runnable(){
            @Override
            public void run () {
                if (tabItem.getControl() != null) {
                    result[0] = new WidgetReference<Widget>(tabItem.getControl());
                    //dumpControl(tabItem.getControl()); //TOBEREMOVED
                }
                
            }});
        return result[0];
    }
    
    static public interface IMatch{
        public boolean matches(Widget widget);
    }
    
    /**
     * WindowTester can't find a button within a Composite that we've referenced by means of
     * a WidgetReference object. So, we do the job ourselves.
     * @param label label on the button.
     * @param parent parent control (typically a Composite).
     * @return A widget reference to the button
     * @throws WidgetSearchException if can't be found.
     */
    public static IWidgetReference findButtonLocator(final String label, Control parent) throws WidgetSearchException {
          return findWidgetLocator(new IMatch(){

            @Override
            public boolean matches (Widget widget) {
                return widget instanceof Button &&
                    label.equals(((Button)widget).getText());
            }},parent, "Button with label \"" + label + "\"");
    }
    
    /**
     * WindowTester can't find a button within a Composite that we've referenced by means of
     * a WidgetReference object. So, we do the job ourselves.
     * @param label label on the button.
     * @param parent parent control (typically a Composite).
     * @return A widget reference to the button
     * @throws WidgetSearchException if can't be found.
     */
    public static IWidgetReference findComboLocator(final String comboItem, Control parent) throws WidgetSearchException {
          return findWidgetLocator(new IMatch(){

            @Override
            public boolean matches (Widget widget) {
                if (widget instanceof Combo) {
                	for (String item: ((Combo)widget).getItems()){
                		if (item.equals(comboItem))
                			return true;
                	}
                }
                return false;
            }},parent, "Combo with item \"" + comboItem + "\"");
    }
    
    /**
     * Find a label with the given text.
     * @param text text on the label.
     * @param parent parent control (typically a Composite).
     * @return A widget reference to the button
     * @throws WidgetSearchException if can't be found.
     */
    public static IWidgetReference findLabelLocator(final String text, Control parent) throws WidgetSearchException {
          return findWidgetLocator(new IMatch(){

            @Override
            public boolean matches (Widget widget) {
                return widget instanceof Label &&
                    text.equals(((Label)widget).getText());
            }},parent, "Label with text \"" + text + "\"");
    }
    
    /**
     * Find a text field with the given content.
     * @param text text on the label.
     * @param parent parent control (typically a Composite).
     * @return A widget reference to the button
     * @throws WidgetSearchException if can't be found.
     */
    public static IWidgetReference findTextLocator(final String text, Control parent) throws WidgetSearchException {
          return findWidgetLocator(new IMatch(){

            @Override
            public boolean matches (Widget widget) {
                return widget instanceof Text &&
                    text.equals(((Text)widget).getText());
            }},parent, "Text field with text \"" + text + "\"");
    }
    
    
    /**
     * Locate a widget based on its known tool tip content.
     * @param tooltip the tool tip of the widget being sought.
     * @param parent parent control (typically a Composite).
     * @return the Control or ToolItem with the give tool tip.
     * @throws WidgetSearchException if can't be found, or there is more than one.
     */
    public static IWidgetReference findWidgetLocatorFromToolTip(final String tooltip, Control parent) throws WidgetSearchException {
        return findWidgetLocator(new IMatch(){

          @Override
        public boolean matches (Widget widget) {
              if (widget instanceof Control)
                  return  tooltip.equals(((Control)widget).getToolTipText());
              if (widget instanceof ToolItem){
                  return tooltip.equals(((ToolItem)widget).getToolTipText());
              }
              return false;
          }},parent, "Widget with tooltip \"" + tooltip + "\"");
  }
    
    /**
     * WindowTester can't find a widget within a Composite that we've referenced by means of
     * a WidgetReference object. So, we do the job ourselves.
     * @param parent parent control (typically a Composite).
     * @return A widget reference to the button
     * @throws WidgetSearchException if can't be found.
     */
    public static IWidgetReference findTableLocator(Control parent) throws WidgetSearchException {
          return findWidgetLocator(new IMatch(){

            @Override
            public boolean matches (Widget widget) {
                return widget instanceof Table && ((Table)widget).isVisible();
            }},parent, "Table widget");
    }
    
    /**
     * Find a Tree widget.
     * @param parent parent control (typically a Composite).
     * @return A widget reference to the button
     * @throws WidgetSearchException if can't be found.
     */
    public static IWidgetReference findTreeLocator(Control parent) throws WidgetSearchException {
          return findWidgetLocator(new IMatch(){

            @Override
            public boolean matches (Widget widget) {
                return widget instanceof Tree && ((Tree)widget).isVisible();
            }},parent, "Tree widget");
    }
    
    /**
     * Given a reference to a MetaWare custom display ("SquareCanvas"), select a line.
     * @param ref
     * @param line
     * @throws WidgetSearchException 
     */
    public static ILocator findSquareCanvasLocator(IUIContext ui, IWidgetReference ref, int line) throws WidgetSearchException{
        IWidgetReference sqRef =  findWidgetLocator(new IMatch(){

            @Override
            public boolean matches (Widget widget) {
                return widget instanceof TextColumn && ((Control)widget).isVisible();
            }},(Control)ref.getWidget(), "Square widget");
        TextColumn tc = (TextColumn)sqRef.getWidget();
        int y = tc.getLinePosition(line);
        return new XYLocator(sqRef,5,y);
    }
    
    /**
     * Locate a List widget.
     * @param parent parent control (typically a Composite).
     * @return A widget reference to the button
     * @throws WidgetSearchException if can't be found.
     */
    public static IWidgetReference findListLocator(Control parent) throws WidgetSearchException {
          return findWidgetLocator(new IMatch(){

            @Override
            public boolean matches (Widget widget) {
                return widget instanceof org.eclipse.swt.widgets.List && ((Control)widget).isVisible();
            }},parent, "List widget");
    }
    
    /**
     * Look for a widget that matches a criteria.
     * @param matcher call back to determine what we're looking or.
     * @param parent parent composite.
     * @param what name for error messages if we can't find it.
     * @return reference to the widget that matches.
     * @throws WidgetSearchException if that isn't exactly one match.
     */
    public static IWidgetReference findWidgetLocator(final IMatch matcher, final Control parent, String what) throws WidgetSearchException{
        final List<Widget> list = new ArrayList<Widget>();
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

            @Override
            public void run () {
                findWidgets(list, matcher, parent);
            }
        });
        if (list.size() == 0){
            dumpControl(parent);
            throw new WidgetSearchException("Can't find " + what);
        }
        if (list.size() > 1) {
            dumpControl(parent);
            throw new WidgetSearchException(what + " is ambiguous; " + list.size() + " matches");
        }
        return new WidgetReference<Widget>(list.get(0));
    }
    
    /**
     * Look for a widget that matches a criteria.
     * @param matcher
     * @param parent
     * @return list of widgets that match.
     * @throws WidgetSearchException if that isn't exactly one match.
     */
    public static IWidgetReference[] findAllWidgetLocators(final IMatch matcher, final Control parent) throws WidgetSearchException{
        final List<Widget> list = new ArrayList<Widget>();
        if (parent == null) throw new IllegalArgumentException("parent is null");
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

            @Override
            public void run () {
                findWidgets(list, matcher, parent);
            }
        });
        IWidgetReference result[] = new WidgetReference[list.size()];
        for (int i = 0; i < result.length; i++){
            result[i] = new WidgetReference<Widget>(list.get(i));
        }
        return result;
    }
    
    protected static void findWidgets(List<Widget> list, IMatch matcher, Widget parent){
        if (parent.isDisposed() || parent instanceof Control && !((Control)parent).isVisible()) return;
        if (matcher.matches(parent)) {
            list.add(parent);
        }
        else
        if (parent instanceof Composite){
            if (parent instanceof Tree) {
                TreeItem items[] = ((Tree)parent).getItems();
                for (TreeItem item: items){
                    if (matcher.matches(item)){
                        list.add(item);
                    }
                }
            }
            for (Control c: ((Composite)parent).getChildren()){
                findWidgets(list,matcher,c);
            }
			if (parent instanceof Decorations) {
				Menu menu = ((Decorations) parent).getMenuBar();
				if (menu != null) {
					findWidgets(list, matcher, menu);
				}
			}
        }
        if (parent instanceof ToolBar){
            for (ToolItem item: ((ToolBar)parent).getItems()){
                if (matcher.matches(item)){
                    list.add(item);
                }
            }
        }
        if (parent instanceof Menu){
        	Menu menu = (Menu)parent;
        	for (MenuItem item: menu.getItems()){
        		findWidgets(list,matcher,item);
        	}
        }
        else if (parent instanceof MenuItem){
        	Menu menu = ((MenuItem)parent).getMenu();
        	if (menu != null)
        	   findWidgets(list,matcher,menu);
        }
    }
    
    public static IWidgetReference[] findAllWithName(IUIContext ui, final String name) throws WidgetSearchException{
        return findAllWidgetLocators(new IMatch(){

            @Override
            public boolean matches (Widget widget) {
                return name.equals(widget.getData("name"));
            }},(Control)ui.getActiveWindow());
    }
    
    /**
     * Find a widget with a particular name. WT's version gets messed up
     * if disposed widgets exists with the name.
     * @param ui
     * @param name
     * @return the widget.
     * @throws WidgetSearchException
     */
    public static IWidgetReference findWidgetWithName(IUIContext ui, String name) throws WidgetSearchException{
    	IWidgetReference refs[] = findAllWithName(ui,name);
    	if (refs.length == 0){
    	    dumpControl((Control)ui.getActiveWindow());
    		throw new WidgetSearchException("Can't find widget named \"" + name + "\"");
    	}
    	else if (refs.length > 1){
    		System.out.println("More than one widget named \"" + name + "\"");
    		for (IWidgetReference r: refs){
    			Widget w = (Widget)r.getWidget();
    			if (w instanceof Control) dumpControl((Control)w);
    			else {
    				System.out.println("   Instance of " + w.getClass().getName());
    			}
    		}
    		throw new WidgetSearchException("More than one widget named \"" + name + "\"" );
    	}
    	return refs[0];
    }
    /**
     * Determine if a (checkbox) button reference is selected.
     * @param ref the button reference.
     * @return true if selected.
     * @throws WidgetSearchException
     */
    public static boolean isSelected(IWidgetReference ref) throws WidgetSearchException{
        if (ref.getWidget() instanceof Button){
            final boolean result[] = new boolean[1];
            final Button b = (Button)ref.getWidget();
            b.getDisplay().syncExec(new Runnable(){

                @Override
                public void run () {
                    result[0] = b.getSelection();
                    
                }});
            return result[0];
        }
        else
            throw new WidgetSearchException("Not a button: " + ref.getWidget().getClass().getName());
    }
    
    /**
     * Determine if a widget is visible;
     * @param ref the widget.
     * @return true if visible.
     * @throws WidgetSearchException
     */
    public static boolean isVisible(IWidgetReference ref) throws WidgetSearchException{
        if (ref.getWidget() instanceof Control){
            final boolean result[] = new boolean[1];
            final Control b = (Control)ref.getWidget();
            b.getDisplay().syncExec(new Runnable(){

                @Override
                public void run () {
                    result[0] = b.isVisible();
                    
                }});
            return result[0];
        }
        else
            throw new WidgetSearchException("Not a control: " + ref.getWidget().getClass().getName());
    }
    
    
    /**
     * Determine if a widget is enabled;
     * @param ref the widget.
     * @return true if visible.
     * @throws WidgetSearchException
     */
    public static boolean isEnabled(IWidgetReference ref) throws WidgetSearchException{
        if (ref.getWidget() instanceof Control){
            final boolean result[] = new boolean[1];
            final Control b = (Control)ref.getWidget();
            b.getDisplay().syncExec(new Runnable(){

                @Override
                public void run () {
                    result[0] = b.isEnabled();
                    
                }});
            return result[0];
        }
        else if (ref.getWidget() instanceof ToolItem) {
        	 final boolean result[] = new boolean[1];
             final ToolItem b = (ToolItem)ref.getWidget();
             b.getDisplay().syncExec(new Runnable(){

                 @Override
                public void run () {
                     result[0] = b.isEnabled();
                     
                 }});
             return result[0];
        }
        else
            throw new WidgetSearchException("Not a control: " + ref.getWidget().getClass().getName());
    }
    
    /**
     * Set the size of the the current active window, and center it on the screen.
     * @param width the width
     * @param height the height.
     */
    public static void setShellSize(final int width, final int height){
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable(){

            @Override
            public void run () {
                Rectangle screen = PlatformUI.getWorkbench().getDisplay().getClientArea();
                Rectangle requiredBounds = new Rectangle((screen.width-width)/2,
                    (screen.height-height)/2,width,height);
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().setBounds(requiredBounds);      
             
            }});
    }
    
    private final static int CANONICAL_WIDTH = 1200;
    private final static int CANONICAL_HEIGHT = 800;
    
    /**
     * Set the main Eclipse window to a canonical size so that
     * size-dependent views will have a deterministic state.
     */
    public static void setCanonicalSize(){
       EclipseUtil.setShellSize(CANONICAL_WIDTH,CANONICAL_HEIGHT);
    }
    
    
    /**
     * Set the size of the the current active window, and center it on the screen.
     * @param width the width
     * @param height the height.
     */
    public static void setShellSize(final IWidgetLocator shell, final int width, final int height){
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable(){

            @Override
            public void run () {
                Rectangle screen = PlatformUI.getWorkbench().getDisplay().getClientArea();
                Rectangle requiredBounds = new Rectangle((screen.width-width)/2,
                    (screen.height-height)/2,width,height);
                ((Shell)((IWidgetReference)shell).getWidget()).setBounds(requiredBounds);      
             
            }});
    }
    
    /**
     * Set the size of the the current active dialog, and center it on the screen.
     * @param width the width
     * @param height the height.
     */
    public static void setActiveShellSize (IUIContext ui, final int width, final int height) {
        final Shell shell = (Shell) ui.getActiveWindow();
        if (shell != null) {
            PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

                @Override
                public void run () {
                    Rectangle screen = PlatformUI.getWorkbench().getDisplay().getClientArea();
                    //NOTE: we adjust the display to be in the upper part of the screen because
                    // Redhat Enterprise 3 return the full screen size for getClientArea and
                    // we don't want the lower portion to be covered by toolbar.
                    Rectangle requiredBounds = new Rectangle((screen.width - width) / 2, (screen.height - height) / 4,
                        width, height);
                    shell.setBounds(requiredBounds);

                }
            });
        }
    }
    
    public static void setMaximizedForActiveShell(IUIContext ui, final boolean v){
    	final Shell shell = (Shell) ui.getActiveWindow();
    	if (shell != null) {
    		shell.getDisplay().syncExec(new Runnable(){

				@Override
				public void run() {
					shell.setMaximized(v);
					
				}});
    	}
    }
    
    public static void addShellSizeListener(IUIContext ui, final ControlListener listener){
        final Shell shell = (Shell)ui.getActiveWindow();
        if (shell != null) {
            shell.getDisplay().syncExec(new Runnable(){

                @Override
                public void run () {
                    shell.addControlListener(listener);
                    
                }});
        }
    }
    
    /**
     * Wait for paint requests to complete so that things are not transient.
     */
    public static void waitForPaintRequestsToComplete(){
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
            @Override
            public void run(){
                PlatformUI.getWorkbench().getDisplay().update();
            }
        });
    }
    
    /**
     * Given a widget reference, return a reference to its parent container.
     * @param ref widget reference.
     * @return a reference to the parent container.
     * @throws WidgetSearchException
     */
    public static IWidgetReference getParent(IWidgetReference ref) throws WidgetSearchException{
        final Widget widget = (Widget)ref.getWidget();
        final Widget result[] = new Widget[1];
        widget.getDisplay().syncExec(new Runnable(){

            @Override
            public void run () {
                if (widget instanceof Control){
                    result[0] =  ((Control)widget).getParent();
                }
                else if (widget instanceof TableItem){
                    result[0] = ((TableItem)widget).getParent();
                }
                else if (widget instanceof TabItem){
                    result[0] = ((TabItem)widget).getParent();
                }
                else if (widget instanceof CTabItem){
                    result[0] = ((CTabItem)widget).getParent();
                }
                else if (widget instanceof ToolItem){
                    result[0] = ((ToolItem)widget).getParent();
                }
                
            }});
        if (result[0] == null) throw new WidgetSearchException("Cannot find parent of " + widget);
        return new WidgetReference<Widget>(result[0]);
    }
    
    protected static String[] computeItems(String path){
        String segments[] = path.replaceAll("\\\\/","%%%").split("/");
        for (int i = 0; i < segments.length; i++){
            segments[i] = segments[i].replaceAll("%%%","/");
        }
        return segments;
    }
    
    private static String getFullTreeItemName(TreeItem item){
        int cnt = item.getParent().getColumnCount();
        if (cnt <= 1) return item.getText();
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < cnt; i++){
            if (i > 0) b.append("|");
            b.append(item.getText(i));
        }
        return b.toString();
    }
    
    /**
     * Given a list of tree items, and path segments, return the tree item(s) that
     * match. Should be just one.
     * @param items the tree items to search.
     * @param segments path segments (regular expressions) to match.
     * @return the items that match.
     */
    private static List<TreeItem> findMatches(TreeItem items[], List<String>segments) {
        List<TreeItem> list = new ArrayList<TreeItem>(items.length);
        String segment = segments.get(0);
        for (TreeItem item: items){
            if (segment.equals(item.getText()) || item.getText().matches(segment) || segment.equals(getFullTreeItemName(item)) ||
                getFullTreeItemName(item).matches(segment)){
                if (segments.size() == 1){
                    list.add(item);
                }
                else {
                	item.getParent().showItem(item);
                    // Hmm. some trees won't let us see the kids unless expanded. Virtual trees?
                    if (!item.getExpanded()) {
                    	item.setExpanded(true);
                    	// item.setExpanded(true) alone doesn't work for virtual trees because apparently
                        // no expand event is sent. Fire it explicitly.
                    	Event e = new Event();
                    	e.item = item;
                    	e.type = SWT.Expand;
                    	item.getParent().notifyListeners(SWT.Expand,e);
                    }
                    TreeItem[] subtrees = item.getItems();
                    //System.out.printf("Subtrees of %s is cnt=%d\n",item.getText(),subtrees.length);
                    List<TreeItem>matches = findMatches(subtrees,segments.subList(1,segments.size()));
                    list.addAll(matches);
                }
            }
        }
        return list;
    }
    
    protected static boolean doesTreeItemContainIcon(final TreeItem item){
        if (item.getDisplay().getThread() == Thread.currentThread()){
            return item.getImage() != null;
        }
        else {
            final boolean result[] = new boolean[1];
            item.getDisplay().syncExec(new Runnable(){

                @Override
                public void run () {
                    result[0] = item.getImage() != null;
                    
                }});
            return result[0];
        }
        
    }
    
    /**
     * Click the treeitem so as to expand or collapse it.
     * @param ui context
     * @param treeItem reference to the tree item.
     * @throws WidgetSearchException
     */
    public static void clickTreeItem(IUIContext ui, IWidgetReference treeItem) throws WidgetSearchException{
        int x;
        if (doesTreeItemContainIcon((TreeItem)treeItem.getWidget())){
            x = -23;
        }
        else
            x = -5;
        ui.click(new XYLocator(treeItem,x,5));
    }
    
    /**
     * Expand a tree item if it isn't already.
     * @param ui
     * @param treeItem
     * @throws WidgetSearchException 
     */
    public static void expandTreeItem(IUIContext ui, IWidgetReference treeItem) throws WidgetSearchException{
        final TreeItem item = (TreeItem)treeItem.getWidget();

        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable(){

            @Override
            public void run () {
                if (!item.getExpanded()) {
                	item.getParent().showItem(item);
                	item.setExpanded(true);
                	Event event = new Event();
                	event.item = item;
                	event.type = SWT.FocusIn;        	
                	item.getParent().notifyListeners(SWT.FocusIn,event);
                	event = new Event();
                	event.item = item;
                	event.type = SWT.Expand;
                	try {
                	    item.getParent().notifyListeners(SWT.Expand,event);
                	}catch (ClassCastException x){
                		x.printStackTrace();
                		// Sometimes occurs when casting to "MarkerCategory". Don't know why.
                	}
                }
            }
        });
        
    }
    
    public static TreeItem computeTreeItem(Control container, final String path) throws WidgetSearchException {
    	final IWidgetReference[] trees = findAllWidgetLocators(new IMatch(){
			@Override
			public boolean matches(Widget widget) {
				return widget instanceof Tree;
			}},container);
        if (trees.length == 0){
        	dumpControl(container);
            throw new WidgetSearchException("Could not find any trees for searching \"" + path + "\"");
        }
        
        final String itemName[] = computeItems(path);
        final List<TreeItem> matches = new ArrayList<TreeItem>();
        final String actualItemName[] = new String[1];
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable(){

            @Override
            public void run () {
                List<String> segments = Arrays.asList(itemName);
                for (IWidgetReference ref: trees) {
                    Tree tree = (Tree)ref.getWidget();
                    // Yes, it can somehow get disposed while
                    // traversing it!
                    if (!tree.isDisposed()) {
                        List<TreeItem>result = findMatches(tree.getItems(),segments);
                        matches.addAll(result);
                    }
                }
                if (matches.size() == 1) {
                	actualItemName[0] = matches.get(0).getText();
                    matches.get(0).getParent().showItem(matches.get(0));
                }
                
            }});
        if (matches.size() == 0){
        	dumpControl(container);
        	 PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable(){

                 @Override
                 public void run () {
        	        System.out.println("Trees items: ");
        	        for (IWidgetReference tree: trees){
        	            for (TreeItem item: ((Tree)tree.getWidget()).getItems()){
        	                System.out.println("...." + getFullTreeItemName(item));
        	            }
        	        }
                 }});
            throw new WidgetSearchException("Could not find tree with path \"" + path +"\"");
        }
        else if (matches.size() > 1) {
            final StringBuilder buf = new StringBuilder();
            PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable(){
                
                String computeFullPath(TreeItem item){
                    if (item.getParentItem() != null) {
                        return computeFullPath(item.getParentItem()) + "-->" + item.getText();
                    }
                    return item.getText();
                }
                @Override
                public void run () {
                    buf.append("Tree path \"");
                    buf.append(path);
                    buf.append("\" is ambiguous:\n");
                    for (TreeItem m: matches){
                        buf.append("    ");
                        buf.append(computeFullPath(m));
                        buf.append("\n");
                    }
                    
                }});
            throw new WidgetSearchException(buf.toString());
        }
        else {
            matches.get(0).getDisplay().syncExec(new Runnable(){

                @Override
                public void run () {
                	//<KLOODGE>
                	// The SWT "virtual" trees can have items re-assigned underneath as a side-effect
                	// of calling "Tree.showItem(item)".
                	TreeItem item = matches.get(0);
                	if (!item.getText().equals(actualItemName[0])){
                		TreeItem items[] = item.getParentItem() != null?item.getParentItem().getItems():item.getParent().getItems();
                		for (TreeItem it: items){
                			if (actualItemName[0].equals(it.getText())){
                				item = it;
                				matches.set(0,item);
                				break;
                			}
                		}
                	}
                	//</KLOODGE>
                    item.getParent().showItem(matches.get(0));
                    
                }});
            return matches.get(0);
        }
    }
    
    public static IWidgetLocator computeTreeItemLocator (final IUIContext ui, final String path) throws WidgetSearchException {
        //final IWidgetLocator[] trees = ui.findAll(new SWTWidgetLocator(Tree.class));
    	// ui.findAll misses stuff under Linux. So we use our own.
    	Control parent = (Control)ui.getActiveWindow();
    	if (parent == null) {
    		parent = EclipseUtil.activateEclipseWindow(); 		
    	}
    	
        return computeTreeItemLocator(ui,path,parent);
    }
    
    public static IWidgetLocator computeTreeItemLocator (final IUIContext ui, final String path, Control parent) throws WidgetSearchException {
      
        if (parent == null) {
            parent = EclipseUtil.activateEclipseWindow();       
        }
        
        return new WidgetReference<Widget>(computeTreeItem(parent,path));
    }
    
    public static IToolOptionSetting makeToolOptionsSettings(IUIContext ui){
        return new ToolOptionSetting(ui);
    }
    
    public static boolean isLinux(){
    	return "Linux".equals(System.getProperty("os.name"));
    }
    
    public static void walkTree(final Tree tree, final ITreeItemVisitor visitor){
        tree.getDisplay().syncExec(new Runnable(){

            @Override
            public void run () {
                visitTreeItems(tree.getItems(),visitor);
                }               
            });
    }
    
    private static void visitTreeItems(TreeItem items[], ITreeItemVisitor visitor){
        //ASSERTION: we're in the UI thread.
        for (TreeItem item: items){
            if (visitor.visit(item)){
                item.getParent().showItem(item); // appears to be broken   
                item.setExpanded(true);
                visitTreeItems(item.getItems(),visitor);
                //NOTE: item.getParent().showItem(item) doesn't work
                // if scrollpane is a parent of the tree (instead of the tree
                // itself). Thus, we must close other trees so as not to ever
                // require a scroll.
                item.setExpanded(false);
            }
        }
    }
    
    /**
     * Given a single-column table widget, return its content as a list.
     * 
     */
    public static String[] getSingleColumnTableContent(final Table table){
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
        return list[0];
    }
    
    public static void writeBuildSettingState(final OutputStream outStream, final IToolOptionSetting setting) throws WidgetSearchException {
    	if (PlatformUI.getWorkbench().getDisplay().getThread() != Thread.currentThread()){
    		// Force to UI thread so as to avoid having to do so on a 
    		// widget-by-widget basis which results in very slow performance.
    		final WidgetSearchException x[] = new WidgetSearchException[1];
    		x[0] = null;
    		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable(){
				@Override
                public void run() {
					try {
						writeBuildSettingState(outStream,setting);
					} catch (WidgetSearchException e) {
						x[0] = e;
					}
					
				}});
    		if (x[0] != null) throw x[0];
    		return;
    	}
        PrintStream out = new PrintStream(outStream);
        out.println("<toolSettings>");
        out.println("    <compiler name=\"" + setting.getCompilerName() + "\" options=\"" +
            setting.getCompilerCommandLine() + "\">");
        writeOptionStates(out,setting,setting.getCompilerOptions(),8);
        out.println("    </compiler>");
        if (setting.hasLinker()){
            out.println("    <linker name=\"" + setting.getLinkerName() + "\" options=\"" +
                setting.getLinkerCommandLine() + "\">");
            writeOptionStates(out,setting,setting.getLinkerOptions(),8);
            out.println("    </linker>");
        }
        else {
            out.println("    <archiver name=\"" + setting.getArchiverName() + "\" options=\"" + 
                setting.getArchiverCommandLine() + "\">");
            out.println("    </archiver>");
        }
        
        out.println("    <assembler name=\"" + setting.getAssemblerName() + "\" options=\"" +
            setting.getLinkerCommandLine() + "\">");
        writeOptionStates(out,setting, setting.getAssemblerOptions(),8);
        out.println("    </assembler>");
        out.println("</toolSettings>");       
    }
    
    public static void writeOptionStates (PrintStream out, IToolOptionSetting setting, IOption[] options, int indent)
        throws WidgetSearchException {
        for (IOption option : options) {
            indent(out, indent);
            out.print("<option name=\"" + option.getName() + "\"");
            if (option.getCommand() != null && option.getCommand().length() > 0)
                out.print(" command=\"" + option.getCommand() + "\"");
            else if (option.getCommandFalse() != null && option.getCommandFalse().length() > 0)
                out.print(" commandFalse=\"" + option.getCommandFalse() + "\"");
            if (!setting.isEnabled(option)) {
                out.println(" enabled=\"false\"/>");
            }
            else {
                Object value = setting.getValue(option);
                if (value instanceof String[]) {
                    out.println(">");
                    for (String v : (String[]) value) {
                        indent(out, indent + 4);
                        out.println("<value>" + v + "</value>");
                    }
                    indent(out, indent);
                    out.println("</option>");
                }
                else {
                    out.println(" value=\"" + value + "\"/>");
                }
            }
        }
    }
    
    public static IWorkspace getWorkspace() {
        return ResourcesPlugin.getWorkspace();
    }
    
    private static Object buildLock = new Object();
    private static IResourceChangeListener buildListener = null;
    private static String pendingProjectBuild = null;
    private static boolean prebuildSeen = false;
    private static ITextTranslator fTextTranslater = new ITextTranslator(){

        @Override
        public String translate (String s) {
            return s;
        }};
    
    /**
     * Indicate that a project is about to be built. Set up listeners so that we can
     * synchronize with the build completion.
     * @param projectName name of project.
     */
    public static void prepareToBuild(String projectName){
        if (buildListener == null) {
            prebuildSeen = false;
            buildListener = new IResourceChangeListener(){

                @Override
                public void resourceChanged (IResourceChangeEvent event) {
                    if (event.getType() == IResourceChangeEvent.PRE_BUILD){
                        synchronized(buildLock){
                            prebuildSeen = true;
                            buildLock.notifyAll();
                            pendingProjectBuild = ((IProject)event.getSource()).getName();
                            // System.out.println(">>>PRE-BUILD for " + pendingProjectBuild);
                        }
                    }
                    else if (event.getType() == IResourceChangeEvent.POST_BUILD){
                        synchronized(buildLock){
                            pendingProjectBuild = null;
                            buildLock.notifyAll();
                            
                            //System.out.println(">>>POST-BUILD for " + ((IProject)event.getSource()).getName());
                        }
                    }
                    
                }};
            IWorkspace workspace = getWorkspace();
            workspace.addResourceChangeListener(buildListener,IResourceChangeEvent.PRE_BUILD|IResourceChangeEvent.POST_BUILD);
        }
    }
    
    /**
     * Called after a build has started to delay until the build is complete.
     * <P>
     * NOTE: {@link #prepareToBuild} must have been previously called prior to
     * starting the build.
     * @param projectName the name of the project being built.
     * @param timeout timeout value in milli-seconds.
     * @return true if build completed; false if timeout occurred.
     */
    public static boolean waitUntilBuildCompleted(String projectName, int timeout) {
        synchronized(buildLock){
            if (pendingProjectBuild == null && !prebuildSeen){
                //System.out.println(">>>Wait for build to start");
                try {
                 // Build not yet instantiated! Wait for it.
                    buildLock.wait(10000);
                }
                catch (InterruptedException e) {
                   
                }  
            }
            if (pendingProjectBuild != null){
                try {
                    //System.out.println(">>>Waiting for build of " + pendingProjectBuild + " to complete");
                    buildLock.wait(timeout);
                }
                catch (InterruptedException e) {
                }
            }
            //else System.out.println(">>>No build pending");
            return pendingProjectBuild == null;
        }       
    }
    
    /**
     * Return content of a list.
     * @param list
     * @return the content of a list widget.
     */
    public static String[] getListContent(final org.eclipse.swt.widgets.List list){
        final String stringList[][] = new String[1][];
        list.getDisplay().syncExec(new Runnable(){

            @Override
            public void run () {
                stringList[0] = list.getItems();
                
            }});
        return stringList[0];
    }
    
    /**
     * Do whatever it necessary to guarantee that the Eclipse window is the active one.
     */
    public static Shell activateEclipseWindow(){
        Display display = PlatformUI.getWorkbench().getDisplay();
        final Shell result[] = new Shell[1];
        display.syncExec(new Runnable(){

            @Override
            public void run () {
                Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                if (shell == null) {
                     shell = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell();
                }
                shell.setMinimized(false); // In case a bogus mouse stroke minimized it.
                shell.moveAbove(null); // in case it is completely covered?
                shell.forceActive();
                result[0] = shell;
                
            }});
        return result[0];
    }
    
//    /**
//     * Set horizontal scroll value of a widget.
//     * @param scroller
//     * @param value
//     */
//    private static void setHorizontalScroll(final Scrollable scroller, final int value){
//        scroller.getDisplay().syncExec(new Runnable(){
//
//            public void run () {
//                ScrollBar scrollBar = scroller.getHorizontalBar();
//                if (scrollBar != null){
//                    scrollBar.setSelection(value);
//                }
//                
//            }});
//      
//    }
    
    /**
     * Do whatever is necessary to make sure the Project View is fully scrolled to the left,
     * in case a previously faulty test messed it up.
     * @param ui
     */
    public static void fixProjectView(IUIContext ui){
       // System.out.println(">>>FixProjectView");
        try {
            IWidgetReference ref = EclipseUtil.findView(PROJECT_VIEW_ID);
            ref = EclipseUtil.findWidgetLocator(new IMatch(){

                @Override
                public boolean matches (Widget widget) {
                    return widget instanceof Tree;
                }}, (Control)ref.getWidget(),"Project tree");
           // System.out.println(">>> project view found");
            EclipseUtil.resetHorizontalScroller(ui,(Scrollable)ref.getWidget());
            //System.out.println(">>>Horizontal scroll reset?");
        }
        catch (WidgetSearchException e) {
            e.printStackTrace();
            //failed
        }    
    }
    
    public static IWorkbenchWindow getActiveWorkbenchWindow() {
        final IWorkbenchWindow w[] = new IWorkbenchWindow[1];
        // Can only be read from UI thread.
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable(){

            @Override
            public void run () {
                w[0] = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                
            }});
        return w[0];
    }
    
    /**
     * Confirm that something in the Launch view is selected so that SeeCode displays will be active.
     * @return true if successfully confirmed.
     */
    public static boolean confirmDebuggerSelected(IUIContext ui){
        IWorkbenchWindow w = getActiveWorkbenchWindow();
        if (w == null) return false; // shouldn't happen, or does it?
        IDebugContextService dservice = DebugUITools.getDebugContextManager().getContextService(w);
        ISelection selection = dservice.getActiveContext();
        if (selection == null || !(selection instanceof IStructuredSelection) ||
            ((IStructuredSelection)selection).getFirstElement() == null)
        {
            // No debugger selection! See if we can fix it.
            try {
                IWidgetReference loc = findView(LAUNCH_VIEW_ID);
                ui.click(findWidgetLocator(new IMatch(){

                    @Override
                    public boolean matches (Widget widget) {
                        return (widget instanceof TreeItem) &&
                        ((TreeItem)widget).getParentItem() != null &&
                        ((TreeItem)widget).getParentItem().getText().indexOf("MetaWare") >= 0;
                    }},(Control)loc.getWidget(),"debugger launch"));
                return true;
            }
            catch (WidgetSearchException e) {
                return false;
            }
        }
        return true;       
    }
    
    public static boolean isDisposed(IWidgetReference ref){
        return ((Widget)ref.getWidget()).isDisposed();      
    }

    /**
     * Return whether or not a launch is active.
     */
    public static boolean isLaunchActive(){
        ILaunch launches[] = DebugPlugin.getDefault().getLaunchManager().getLaunches();
        for (ILaunch launch: launches){
            if (!launch.isTerminated()){
                return true;
            }
        }
        return false;
    }
    /**
     * Remove terminated launches from the manager so that
     * {@link #waitUntilDebuggerStops(IUIContext, int)} can
     * assume what it sees are new launches.
     */
    public static void removeTerminatedLaunches(){
    	ILaunch launches[] = DebugPlugin.getDefault().getLaunchManager().getLaunches();
    	for (ILaunch launch: launches){
    		if (launch.isTerminated()){
    			DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);
    		}
    	}
    }
    
    /**
     * Wait until the debugger hits a breakpoint, or otherwise stops.
     * @param ui
     * @param timeout timeout value in milliseconds.
     * @return true if debugger stopped within the timeout; false if timeout occurred while debugger
     * was still in run state.
     */
    public static boolean waitUntilDebuggerStops (IUIContext ui, int timeout) {
        long endTime = System.currentTimeMillis() + timeout;

        while (System.currentTimeMillis() < endTime) {
            ILaunch launches[] = DebugPlugin.getDefault().getLaunchManager().getLaunches();
            if (launches.length != 0) {
                if (launches[0].isTerminated())
                    return true;
                IDebugTarget targets[] = launches[0].getDebugTargets();
                if (targets.length > 0) {
                    boolean allSuspended = true;
                    for (IDebugTarget t : targets) {
                        if (!t.isTerminated() && !t.isSuspended()) {
                            allSuspended = false;
                            break;
                        }
                    }
                    if (allSuspended) {
                        int cnt = targets.length;
                        // may be CMPD and new processes are being added.
                        ui.wait(milliseconds(500));
                        if (launches[0].getDebugTargets().length == cnt)
                            return true;
                    }
                }
            }
            ui.wait(milliseconds(250));
        }
        return false;
    }
    
    /**
     * Return the launch name of the currently active launch.
     * @return the name of the current debugger launch, or <code>null</code>.
     */
    public static String getLaunchName(){
        ILaunch launches[] = DebugPlugin.getDefault().getLaunchManager().getLaunches();
        // If multiple, return the one that isn't terminated.
        for (ILaunch l: launches){
        	if (!l.isTerminated()) return l.getLaunchConfiguration().getName();
        }
        if (launches.length > 0) return launches[0].getLaunchConfiguration().getName();
        return null;
    }
    
    /**
     * Wait until the debugger terminates.
     * @param ui
     * @param timeout timeout value in milliseconds.
     * @return true if debugger terminates before timeout occurs.
     */
    public static boolean waitForLaunchTermination(IUIContext ui, int timeout){
        long endTime = System.currentTimeMillis() + timeout;
        
        while (System.currentTimeMillis() < endTime) {
            ILaunch launches[] = DebugPlugin.getDefault().getLaunchManager().getLaunches();
            if (launches.length == 0) return true;
            if (launches[0].isTerminated()) return true;
            ui.wait(milliseconds(250));
        }  
        return false;
    }
    
    /**
     * Wait for a launch to start.
     * @param ui
     * @param timeout timeout value in milliseconds.
     * @return true if launch occurs before timeout occurs.
     */
    public static boolean waitForLaunchStart(IUIContext ui, int timeout){
        long endTime = System.currentTimeMillis() + timeout;
        
        while (System.currentTimeMillis() < endTime) {
            ILaunch launches[] = DebugPlugin.getDefault().getLaunchManager().getLaunches();
            if (launches.length > 0) return true;
            ui.wait(milliseconds(250));
        }  
        return false;
    }
    
    /**
     * Do whatever is necessary to make sure that Eclipse window stays on top.
     */
    public static void keepWindowActive(){
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable(){

            @Override
            public void run () {
                final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                shell.addShellListener(new ShellListener(){

                    @Override
                    public void shellActivated (ShellEvent e) {
                       
                        
                    }

                    @Override
                    public void shellClosed (ShellEvent e) {
                       
                        
                    }

                    @Override
                    public void shellDeactivated (ShellEvent e) {
                        if (PlatformUI.getWorkbench().getDisplay().getActiveShell() == null)
                            shell.forceActive(); // undo the damage!            
                    }

                    @Override
                    public void shellDeiconified (ShellEvent e) {                                         
                    }

                    @Override
                    public void shellIconified (ShellEvent e) {
                      
                    }});
                
            }});      
    }
    
    /**
     * Terminate any launch that may have been left running
     * after a previous test failed.
     */
    public static void terminateAllLaunches(){
        ILaunch launches[] = DebugPlugin.getDefault().getLaunchManager().getLaunches();
        for (ILaunch launch: launches){
            try {
                launch.terminate();
            }
            catch (DebugException e) {
                System.err.println("Problem in terminating a launch:");
                e.printStackTrace(System.err);
            }
        }
    }
    
    /**
     * Clear all breakpoints (called when starting a debugger test and want
     * to make sure we haven't inherited breakpoints from previous test.)
     */
    public static void clearBreakpoints(){
        for (IBreakpoint bp: DebugPlugin.getDefault().getBreakpointManager().getBreakpoints()){
            try {
                bp.delete();
            }
            catch (CoreException e) {
                System.err.println("Problem in deleting breakpoint:");
                e.printStackTrace(System.err);
            }
        }    
    }
    
    /**
     * Clear the contents of the Expression view.
     */
    public static void clearExpressionView(){
    	IExpressionManager emgr = DebugPlugin.getDefault().getExpressionManager();
    	emgr.removeExpressions(emgr.getExpressions());
    }
    
    /**
     * Programmatically close all open editors.
     */
    public static void closeAllEditors () {
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

            @Override
            public void run () {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().closeAllEditors(false);
            }
        });
    }
    
    
    /**
     * Open editor given a workspace-relative file path.
     * @param path
     * @throws WidgetSearchException
     */
    public static void openEditor (final String path) throws WidgetSearchException {

        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        final IFile f = root.getFile(new Path(path));
        if (!f.exists()) {
            throw new WidgetSearchException("File \"" + path + "\" does not exist");
        }
        IContentType type = Platform.getContentTypeManager().findContentTypeFor(f.getLocation().toOSString());
        if (type == null) {
            throw new WidgetSearchException("Can't identify editor for " + path);
        }
        final IWorkbench workbench = PlatformUI.getWorkbench();
        IEditorRegistry edReg = workbench.getEditorRegistry();
        final IEditorDescriptor edDesc = edReg.getDefaultEditor(f.getLocation().toOSString(), type);
        if (edDesc == null) {
            throw new WidgetSearchException("Can't identify editor for " + path);
        }
        final Exception[] exception = new Exception[1];
        workbench.getDisplay().syncExec(new Runnable(){

            @Override
            public void run () {
                try {
                    workbench.getActiveWorkbenchWindow().getActivePage().openEditor(new FileEditorInput(f), edDesc.getId());
                }
                catch (PartInitException e) {
                    exception[0] = e;
                }
                
            }});
        if (exception[0] != null) throw new WidgetSearchException(exception[0]);
       
    }
    
    /**
     * Given a reference to a Table, or a composite that contains a table, return the
     * rows of the table as a 2-dimensional array.
     * @param ui
     * @param parent the table or a composite containing the table.
     * @return the content of the table.
     * @throws WidgetSearchException
     */
    public static String[][]getTableRows(IUIContext ui, IWidgetReference parent) throws WidgetSearchException {
        IWidgetReference tableRef = findTableLocator((Control)parent.getWidget());
        final Table table = (Table)tableRef.getWidget();
        final String[][][] result = new String[1][][];
        table.getDisplay().syncExec(new Runnable(){

            @Override
            public void run () {
                TableItem items[] = table.getItems();
                result[0] = new String[items.length][];
                int columns = Math.max(table.getColumnCount(),1);
                for (int i = 0; i < items.length; i++){
                    result[0][i] = new String[columns];
                    for (int j = 0; j < columns; j++){
                        result[0][i][j] = items[i].getText(j);
                    }
                }
                
            }});
        return result[0];
        
    }
    
    public static String getDebugViewSelectionString() throws WidgetSearchException {
        IWidgetReference viewRef = findView(DEBUG_VIEW_ID);
        final IWidgetReference treeRef = findTreeLocator((Control)viewRef.getWidget());
        final String result[] = new String[1];
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable(){

            @Override
            public void run () {
                Tree t = (Tree)treeRef.getWidget();
                TreeItem[] items = t.getSelection();
                if (items.length > 0){
                    result[0] = items[0].getText();
                }
                
            }});
        if (result[0] == null) throw new WidgetSearchException("Can't find Debug View selection");
        return result[0];
    }
    
    /**
     * Look at the selected Debug view item and return its PC value as a string (e.g., 0x8120c).
     */
    public static String getDebugViewPC() throws WidgetSearchException{
        String v = getDebugViewSelectionString();
        int i = v.indexOf("0x");
        if (i >= 0){
            return v.substring(i);
        }
        throw new WidgetSearchException("Can't find hex address in \"" + v + "\"");
//        final WidgetSearchException exception[] = new WidgetSearchException[1];
//        final String[] result = new String[0];
//        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
//
//            public void run () {
//                IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
//                if (w == null){
//                    exception[0] = new WidgetSearchException("Can't find active window");
//                    return;
//                }
//                ISelection selection = DebugUITools.getDebugContextManager().getContextService(w).getActiveContext();
//                if (selection instanceof IStructuredSelection) {
//                    Object element = ((IStructuredSelection) selection).getFirstElement();
//                    if (element instanceof ICStackFrame) {
//                        ICStackFrame stackframe = (ICStackFrame) element;
//                        IAddress addr = stackframe.getAddress();
//                        if (addr != null)
//                            result[0] = addr.toHexAddressString();
//                        return;
//                    }
//                }
//                exception[0] = new WidgetSearchException("Can't find Debug View selection");
//            }
//        });
//        if (exception[0] != null) throw exception[0];
//        return result[0];
    }
    
    /**
     * Escape characters of a string so that it can be used for a literal perl pattern
     * with no meta characters getting in the way.
     * @param s
     * @return
     */
    public static String escapeString(String s){
        StringBuilder buf = new StringBuilder(s.length()*2);
        int len = s.length();
        for (int i = 0; i < len; i++){
            char c = s.charAt(i);
            switch(c){
                case '(':
                case ')':
                case '[':
                case ']':
                case '\\':
                    buf.append('\\');
                    break;
            }
            buf.append(c);
        }
    return buf.toString();  
    }
    
    
    /**
     * Check or uncheck a pulldown menu item.
     * @param viewID the View ID.
     * @param item name of Item.
     * @param check if true, check it; or else uncheck it.
     * @throws WidgetSearchException 
     */
    public static void checkPulldownMenuItem(IUIContext ui, String viewID, String item, boolean check) throws WidgetSearchException{
        /* Doesn't work yet.
         IWidgetLocator r = ui.find(new PullDownMenuItemLocator(item,
            new ViewLocator(viewID)));
         MenuItem menuItem = (MenuItem)((IWidgetReference)r).getWidget();
         if (check != menuItem.getSelection()){
             ui.click(r);
         }
         */
    }
    
    public static void scrollVertical(final Scrollable scroller){
    	if (scroller.getDisplay().getThread() != Thread.currentThread()){
    		scroller.getDisplay().syncExec(new Runnable(){

				@Override
				public void run() {
					scrollVertical(scroller);
					
				}});
    	}
    	else {
    		ScrollBar vscroll = scroller.getVerticalBar();
    		if (vscroll != null){
    			scrollUntilStop(scroller,vscroll,true);
    		}
    	}
    }
    
    public static void scrollRight(final IUIContext ui,
			final IWidgetReference widget) {
		Scrollable scroller = (Scrollable) widget.getWidget();
		if (scroller.getDisplay().getThread() != Thread.currentThread()) {
			scroller.getDisplay().syncExec(new Runnable() {

				@Override
                public void run() {
					scrollRight(ui, widget);
				}
			});
		} else {
			// dumpControl(scroller);
			ScrollBar hscroll = scroller.getHorizontalBar();
			if (hscroll != null) {
				//hscroll.setSelection(hscroll.getMaximum());  // Doesn't work
				scrollUntilStop(scroller, hscroll,false);
				
			}
		}
	}
	private static void scrollUntilStop(Scrollable scroller, ScrollBar hscroll, boolean vertical) {
		int old = hscroll.getSelection();
		Rectangle loc = scroller.getBounds();
		if (vertical){
			loc.x = loc.width-10;
			loc.y = loc.y + loc.height-20;
		}
		else {
		    loc.x = loc.x + loc.width-20; 
	    	loc.y = loc.y + loc.height-10;
		}
		loc.height = 20;
		loc = scroller.getDisplay().map(scroller.getParent(),null,loc);
		
		// System.out.println(">>Horizontal scroller is " + old);

		while (true) {
			click(scroller.getDisplay(),loc.x,loc.y, 3);
			int selection = hscroll.getSelection();
			if (selection <= old) {
				break;
			}
			// System.out.println(">>Horizontal scroller adjusted to
			// " + old);
			old = selection;
		}
	}
    
    public static void resetHorizontalScroller (final IUIContext ui, final Scrollable scroller) {
        if (scroller.getDisplay().getThread() != Thread.currentThread()) {
            scroller.getDisplay().syncExec(new Runnable() {

                @Override
                public void run () {
                    resetHorizontalScroller(ui, scroller);
                }
            });
        }
        else {
            // dumpControl(scroller);
            ScrollBar hscroll = scroller.getHorizontalBar();

            // hscroll.setSelection(0); // Doesn't work

            if (hscroll != null) {
                // hscroll.setSelection(hscroll.getMaximum()); // Doesn't work
                int old = hscroll.getSelection();
                Rectangle loc = scroller.getBounds();
                loc.x = loc.x + 20;
                loc.y = loc.y + loc.height - 10;
                loc.height = 20;
                loc = scroller.getDisplay().map(scroller.getParent(), null, loc);

                // System.out.println(">>Horizontal scroller is " + old);

                while (true) {
                    click(scroller.getDisplay(), loc.x, loc.y, 3);
                    int selection = hscroll.getSelection();
                    if (selection >= old) {
                        break;
                    }
                    // System.out.println(">>Horizontal scroller adjusted to
                    // " + old);
                    old = selection;
                }

            }
        }
    }
    
    public static String xlate(String s){
        return fTextTranslater.translate(s);
    }
    /**
     * Set a translator so that we can transform strings like "C:/workspace/blah" to something
     * more portable across environments.
     * @param xfrm
     */
    public static void setTranslator(ITextTranslator xfrm){
        fTextTranslater = xfrm;
    }
    
    public static void setFocus(final IWidgetReference ref){
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run () {
                Widget w = (Widget)ref.getWidget();
                setFocus(w);
            }
        });
    }
    
    private static boolean setFocus(Widget w){
        if (w instanceof Composite){
            Control[] kids = ((Composite)w).getChildren();
            if (kids.length == 1) return setFocus(kids[0]);
            else {
                for (Control kid: kids){
                    if (setFocus(kid)) return true;
                }
            }
        }
        else if (w instanceof Text || w instanceof StyledText || w instanceof Combo && !((((Combo)w).getStyle() & SWT.READ_ONLY) == 0)){
            ((Control)w).setFocus();
            return true;
        }

        return false;
    }

    /**
     * Make a view visible.
     * @param v the view to make visible.
     */
    public static void showView (final IViewPart v) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run () {
                try {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().
                    showView(v.getViewSite().getId(),v.getViewSite().getSecondaryId(),
                        IWorkbenchPage.VIEW_VISIBLE);
                }
                catch (PartInitException e) {                  
                }
            }
        });       
    }
    
    public static void checkForProgressBarDisposal(IUIContext ui,
			final String title) {
		final Shell shell = (Shell) ui.getActiveWindow();
		final boolean result[] = new boolean[1];
		Display display = shell != null && !shell.isDisposed()? shell.getDisplay() : null;
		if (display != null && shell != null) {
			result[0] = false;
			display.syncExec(new Runnable() {
				@Override
                public void run() {
					result[0] = !shell.isDisposed()
							&& title.equals(shell.getText());
				}
			});

			if (result[0]) {
				ui.wait(new ShellDisposedCondition(title));
			}
		}
	}
    
    /**
     * In case that there is a progress dialog showing, wait
     * for it to be dismissed so that the Eclipse frame is
     * the active one.
     * @param ui
     */
    public static void waitForEclipseFrameToBeActive (final IUIContext ui) {
        long limit = System.currentTimeMillis() + 20000;
        while (System.currentTimeMillis() < limit) {
            final String title[] = new String[1];
            title[0] = null;
            final Display display = PlatformUI.getWorkbench().getDisplay();
            display.syncExec(new Runnable() {

                @Override
                public void run () {
                    try {

                        Shell shell = display.getActiveShell();
                        title[0] = shell != PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() ? shell
                            .getText() : null;
                    }
                    catch (SWTException e) {
                        // Disposed exceptions happen sometimes
                    }
                }
            });

            if (title[0] != null) {
                try {
                    ui.wait(new ShellDisposedCondition(title[0]), 5000);
                }
                catch (WaitTimedOutException e) {
                    // Must have already disappeared
                }
            }
            else
                break;
        }
    }
    
    /**
     * Remove launch configuration with the given name. Does nothing if there is no
     * such launch configuration.
     * @param launchName the name of the launch configuration to remove.
     */
    public static void removeLaunchConfiguration(String launchName){
        try {
            for (ILaunchConfiguration config: DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations()){
                if (launchName.equals(config.getName())){
                    config.delete();
                    return;
                }
            }
        }
        catch (CoreException e) {
            // @todo Auto-generated catch block
            e.printStackTrace();
        }     
    }
    
    public static void clickListItem(IUIContext ui, int clickCount, final String itemName) throws WidgetSearchException{
        IWidgetReference tableRef = findListLocator((Control)ui.getActiveWindow());
        final org.eclipse.swt.widgets.List list = (org.eclipse.swt.widgets.List)tableRef.getWidget();
        final String[] result = new String[1];
        list.getDisplay().syncExec(new Runnable(){

            @Override
            public void run () {
            	String items[] = list.getItems();
                for (int i = 0; i < items.length; i++){
                	String item = items[i];
                    if (itemName.equals(item) || item.matches(itemName)){
                        list.select(i);
                        list.showSelection();
                        result[0] = item;
                        break;
                    }
                }
                
            }});
        if (result[0] != null)
           ui.click(clickCount,new ListItemLocator(result[0],tableRef));
        else throw new WidgetSearchException("Couldn't find table item " + itemName);
    }
    
    public static boolean isTableItemSelected(IUIContext ui, String itemName) throws WidgetSearchException {
    	IWidgetReference ref = findTableItem(ui,itemName);
    	if (ref == null) throw new WidgetSearchException("Can't find table item " + itemName);
    	final TableItem item = (TableItem)ref.getWidget();
    	final boolean result[] = new boolean[1];
    	item.getDisplay().syncExec(new Runnable(){

			@Override
			public void run() {
				result[0] = item.getChecked();
				
			}});
    	return result[0];
    }
    
    public static IWidgetReference findTableItem (IUIContext ui, final String itemName) throws WidgetSearchException {
        IWidgetReference tableRefs[] = findAllWidgetLocators(new IMatch() {

            @Override
            public boolean matches (Widget widget) {
                return widget instanceof Table && ((Table) widget).isVisible();
            }
        }, (Control) ui.getActiveWindow());
        if (tableRefs.length == 0) {
            throw new WidgetSearchException("Can't find a table");
        }
        final IWidgetReference[] result = new IWidgetReference[1];
        for (IWidgetReference tableRef : tableRefs) {
            final Table table = (Table) tableRef.getWidget();

            table.getDisplay().syncExec(new Runnable() {

                @Override
                public void run () {
                    int actualCount = table.getColumnCount();
					int cnt = Math.max(1,actualCount);
					// In case table is "virtual" scroll it to the bottom to materialize all
					// items.
					scrollVertical(table);
                    for (TableItem item : table.getItems()) {
                        for (int i = 0; i < cnt; i++) {
                        	String txt = actualCount == 0? item.getText():item.getText(i);
                            if (itemName.equals(txt) || txt.matches(itemName)) {
                                table.showItem(item);
                                result[0] = new WidgetReference<Widget>(item);
                                break;
                            }
                        }
                        if (result[0] != null)
                            break;
                    }
                }
            });
        }
        return result[0];
    }
    
    public static void clickTableItem(IUIContext ui, int clickCount, final String itemName) throws WidgetSearchException{
        IWidgetReference ref = findTableItem(ui,itemName);
        if (ref != null)
           ui.click(clickCount,ref);
        else {
        	System.out.println("Couldn't find table item \"" + itemName + "\" to click");
        	dumpControl((Control) ui.getActiveWindow());
        	throw new WidgetSearchException("Couldn't find table item " + itemName);        
        }
    }
    
    /**
     * Return the size of a widget.
     * @param ref the reference to the widget.
     * @return the size.
     */
    public static Point getSize(IWidgetReference ref){
        return getSize((Widget)ref.getWidget());
    }
    
    
    /**
     * Return the size of a widget.
     * @param widget the widget.
     * @return the size.
     */
    public static Point getSize(final Widget widget){
        if (widget.isDisposed()) return new Point(0,0);
        if (widget.getDisplay().getThread() == Thread.currentThread()){
            if (widget instanceof Control){
                return ((Control)widget).getSize();
            }
            return new Point(0,0);
        }
        else {
            final Point point[] = new Point[1];
            widget.getDisplay().syncExec(new Runnable(){

                @Override
                public void run () {
                    point[0] = getSize(widget);
                    
                }});
            if (point[0] == null) return new Point(0,0);
            return point[0];
        }
    }
    
    /**
     * Return the content of the clipboard as text.
     * @return the content of the clipboard as text.
     */
    public static String getClipboardText(){
        final String[] result = new String[1];
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable(){

            @Override
            public void run () {
                Clipboard clipboard = new Clipboard(PlatformUI.getWorkbench().getDisplay());
                TextTransfer textTransfer = TextTransfer.getInstance();
                result[0] = (String)clipboard.getContents(textTransfer);              
            }});
        return result[0];
    }
    
    private static Rectangle getScreenBounds(Widget widget) throws WidgetSearchException{
        Display display = widget.getDisplay();
        if (widget instanceof Control){
            if (widget instanceof Shell){
                return ((Shell)widget).getBounds();
            }
            Control c = (Control)widget;
            Rectangle bounds = c.getBounds();
            Point p = c.toDisplay(0,0);
            bounds.x = p.x;
            bounds.y = p.y;
            return bounds;
        }
        else
        if (widget instanceof ToolItem){
            ToolItem item = (ToolItem)widget;
            Composite parent = item.getParent();
            return display.map(parent, null, item.getBounds());
        }
        else
        if (widget instanceof TreeItem){
            TreeItem item = (TreeItem)widget;
            Composite parent = item.getParent();
            return display.map(parent, null, item.getBounds());  
        }
        else
            throw new WidgetSearchException("Can't figure out bounds for instance of " +widget.getClass().getName());
    }
    
    private static Point getScreenLocation(Widget widget) throws WidgetSearchException{
        Rectangle bounds = getScreenBounds(widget);
        return new Point(bounds.x + 3, bounds.y + 3);
    }
    
    /**
     * Do a drag-and-drop operation, in leu of WindowTester's IUIContext.dragTo not working.
     * @param ui
     * @param target
     * @param modifiers
     * @throws WidgetSearchException 
     */
    public static void dragTo (
        final IUIContext ui,
        final IWidgetLocator source,
        final IWidgetLocator target,
        final int modifiers) throws WidgetSearchException {
        final Display display = PlatformUI.getWorkbench().getDisplay();


        if (display.getThread() != Thread.currentThread()) {
            final WidgetSearchException x[] = new WidgetSearchException[1];
            display.syncExec(new Runnable() {

                @Override
                public void run () {
                    try {
                        dragTo(ui, source, target, modifiers);
                    }
                    catch (WidgetSearchException e) {
                        x[0] = e;
                    }

                }
            });
            if (x[0] != null)
                throw x[0];
        }
        else {
            
            ui.click(source);
          
            Event keyEvent = null;
            if ((modifiers & WT.CTRL) != 0){
                keyEvent = new Event();
                keyEvent.type = SWT.KeyDown;
                keyEvent.keyCode = SWT.CTRL;
                if (!display.post(keyEvent)){
                    throw new WidgetSearchException("Couldn't set CTRL key");
                }            
            }
            Event mouseEvent = new Event();
            mouseEvent.type = SWT.MouseDown;
            mouseEvent.keyCode = SWT.CTRL; // probably not necessary
            mouseEvent.button = 1;
            if (!display.post(mouseEvent)){
                throw new WidgetSearchException("Couldn't press mouse");
            }
            Point targetPoint = getScreenLocation((Widget)((IWidgetReference)ui.find(target)).getWidget());
            Point sourcePoint = getScreenLocation((Widget)((IWidgetReference)ui.find(source)).getWidget());
            dragTo(display,sourcePoint,targetPoint,modifiers);
        }
    }
    
    private static void dragTo(Display display,Point sourcePoint, Point targetPoint, int modifiers) throws WidgetSearchException {
        
       
        Event mouseEvent = new Event();
        
        mouseEvent.x = sourcePoint.x;
        mouseEvent.y = sourcePoint.y;
        mouseEvent.type = SWT.MouseMove;
        if (!display.post(mouseEvent)){
            throw new WidgetSearchException("Couldn't move mouse");
        }
        System.out.println("Mouse should now be on source");
        try {
            Thread.sleep(3000);
        }
        catch (InterruptedException e1) {
            // @todo Auto-generated catch block
            e1.printStackTrace();
        }
        Event keyEvent = null;
        if ((modifiers & WT.CTRL) != 0){
            keyEvent = new Event();
            keyEvent.type = SWT.KeyDown;         
            keyEvent.keyCode = SWT.CTRL;
            if (!display.post(keyEvent)){
                throw new WidgetSearchException("Couldn't set CTRL key");
            }     
            display.wake();
            System.out.println("CTRL Key pressed!");
        }
        mouseEvent.type = SWT.MouseDown;
        mouseEvent.keyCode = SWT.CTRL; // probably not necessary
        mouseEvent.button = 1;
        if (!display.post(mouseEvent)){
            throw new WidgetSearchException("Couldn't press mouse");
        }
 
        int steps = 10;
        for (int i = 1; i <= steps; i++){
            mouseEvent.x = sourcePoint.x + (targetPoint.x-sourcePoint.x)*i/steps;
            mouseEvent.y = sourcePoint.y + (targetPoint.y-sourcePoint.y)*i/steps;
            mouseEvent.type = SWT.MouseMove;
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                // @todo Auto-generated catch block
                e.printStackTrace();
            }
            if (!display.post(mouseEvent)){
               throw new WidgetSearchException("Couldn't move mouse");
            }
        }
        System.out.println("Mouse should now be at target.");
        try {
            Thread.sleep(3000);
        }
        catch (InterruptedException e) {
            // @todo Auto-generated catch block
            e.printStackTrace();
        }
       
        mouseEvent.type = SWT.MouseUp;
        if (!display.post(mouseEvent)){
            throw new WidgetSearchException("Couldn't release mouse");
        }
        if (keyEvent != null){
            keyEvent.type = SWT.KeyUp;
            if (!display.post(keyEvent)){
                throw new WidgetSearchException("Couldn't release CTRL key");
            }
        }      
    }
    
    /**
     * Return true if we're running Eclipse 3.3 or earlier. Return false if we're running something
     * later (e.g., 3.4).
     */
    public static boolean isEclipse3_3(){
        String s = System.getProperty("osgi.framework.version");
        if (s == null) return true; // shouldn't happen
        return s.compareTo("3.4") < 0;
    }
    
    /**
     * Select a tree item.
     * @param ref reference to tree item.
     * @throws WidgetSearchException 
     */
    public static void selectTreeItem (final IUIContext ui, IWidgetReference ref) throws WidgetSearchException {
        final TreeItem item = (TreeItem) ref.getWidget();
        item.getDisplay().syncExec(new Runnable() {

            @Override
            public void run () {
                item.getParent().showItem(item);
            }
        });
        ui.click(ref);
    }
    
    /**
     * Return the breakpoints in the Breakpoint view.
     * @return the breakpoints in the Breakpoint view.
     */
    public static IBreakpoint[] getBreakpoints() {
        return DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();       
    }
    
    /**
     * Click tree item from UI thread.
     * @param item
     */
	public static void clickItem(final TreeItem item) {
		Rectangle originalLoc = item.getBounds();
		final Rectangle loc = item.getDisplay().map(item.getParent(), null, originalLoc);
		click(item.getDisplay(),loc.x+loc.width/4, loc.y+loc.height/2,1);
	}
	
	public static void click(final Display display, final int x, final int y, final int count) {
		final boolean b[] = new boolean[1];
		
		Thread t = new Thread("MouseActions") {
			@Override
			public void run() {
				Event e = new Event();
				e.button = 1;
				e.x = x;
				e.y = y;
				e.type = SWT.MouseMove;
				display.post(e);
				pause(20);
				for (int i = 0; i < count; i++) {
				    e.type = SWT.MouseDown;
				    display.post(e);
				    pause(20);
				    e.type = SWT.MouseUp;
				    display.post(e);
				    pause(20);
				}
				pause(200);
				display.syncExec(new Runnable(){

					@Override
					public void run() {
					    b[0] = true;
						
					}});
			}
		};
		t.start();
		while (!b[0]) {
			while (display.readAndDispatch());
			if (!b[0]) display.sleep();
		}
		
	}
    
    private static void pause(int millis){
    	try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
	private static Rectangle getBounds (Object object) {
		Rectangle result = new Rectangle (0, 0, 0, 0);
		try {
			Method method = object.getClass().getDeclaredMethod ("getBounds", (Class[]) null);
			method.setAccessible(true);
			result = (Rectangle) method.invoke (object, (Object[]) null);
		} catch (Exception th) {
			th.printStackTrace();
		}
		return result;
	}
	
	private static Rectangle getBounds(MenuItem menuItem) {
	    Rectangle menuRect = getBounds (menuItem.getParent ());
		Rectangle itemRect = getBounds ((Object)menuItem);
		if ((menuItem.getParent ().getStyle() & SWT.RIGHT_TO_LEFT) != 0) {
			itemRect.x = menuRect.x + menuRect.width - itemRect.width - itemRect.x;
		} else {
			itemRect.x += menuRect.x;
		}
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=38436#c143
		itemRect.y += menuRect.y;
		return itemRect;
	}

	private static Rectangle getBounds (Menu menu) {
	    
	    Decorations parent = menu.getParent();
	    parent.setVisible(true);
	    parent.setEnabled(true);
	    parent.setFocus();
	    menu.setEnabled(true);
        menu.setVisible(true);
		Rectangle r = getBounds ((Object)menu);
//		
//		if (r.width == 0 || r.height == 0){
//		    System.out.println(">>>Diffulty compute bounds for menu " + menu);
//		    System.out.println(">>>Enabled= " + menu.isEnabled());
//		    System.out.println(">>>Visible=" + menu.isVisible());
//		    System.out.println(">>>Style is 0x" + Integer.toHexString(menu.getStyle()));
//		    System.out.println(">>>Parent is " + parent + "; isVisible=" + parent.isVisible() + "; focus=" + parent.isFocusControl());
//		    System.out.println(">>>Parent bounds is " + parent.getBounds());
//		}
		return r;
	}
	
	public static void clickMenuItem(final IUIContext ui, final String path) throws WidgetSearchException {
		Control activeFrame = (Control) ui.getActiveWindow();
		if (activeFrame.getDisplay().getThread() != Thread.currentThread()){
			final WidgetSearchException x[] = new WidgetSearchException[1];
			activeFrame.getDisplay().syncExec(new Runnable(){

				@Override
				public void run() {
					try {
						clickMenuItem(ui,path);
					} catch (WidgetSearchException e) {
						x[0] = e;
					}					
				}});
			if (x[0] != null)
				throw x[0];
			return;
		}
		String itemNames[] = computeItems(path);
		if (itemNames == null || itemNames.length == 0){
			throw new WidgetSearchException(path);
		}
		MenuItem menuItem = null;
		for (int i = 0; i < itemNames.length; i++){
			if (i == 0) {
			    menuItem = findMenuItem((Control)ui.getActiveWindow(),itemNames[0]);
			    Rectangle loc = getBounds(menuItem);
	            if (loc.width == 0) {
	                System.out.println(">>>BOUNDS OF MENUITEM " + itemNames[0] + " is " + loc);
	                dumpControl(System.out,0,menuItem.getParent().getParent());
	                ui.click(new WidgetReference<Widget>(menuItem.getParent()));
	            }
			    ui.click(new WidgetReference<Widget>(menuItem));
			}
			else if (menuItem != null) {  // <- test to keep compiler happy
				Menu menu = menuItem.getMenu();
				if (menu == null){
					throw new WidgetSearchException("In " + path + ": item "+itemNames[i-1] + " is not a menu");
				}
				menuItem = findMenuItem(menu,itemNames[i]);
				Rectangle loc = getBounds(menuItem);
				//System.out.println(">>>loc="+loc);
				ui.click(new XYLocator(loc.x+loc.width/2,loc.y+loc.height/2));	
			}			
		}		
	}
	
	private static MenuItem findMenuItem(Menu menu, String name) throws WidgetSearchException {
		for (MenuItem item: menu.getItems()){
			if (name.equals(stripAmpersands(item.getText()))){
				return item;
			}
		}
		for (MenuItem item: menu.getItems()){
			System.out.println("MenuItem " + item.getText() + " (" + stripAmpersands(item.getText()) + ")");
		}
		throw new WidgetSearchException("Sub menu " + name  + " not found");
	}
	
	private static String stripAmpersands(String s){
		return s.replaceAll("&", "");
	}
	
	private static MenuItem findMenuItem(Control c, final String name) throws WidgetSearchException {
		IWidgetReference refs[] = findAllWidgetLocators(new IMatch(){

			@Override
			public boolean matches(Widget widget) {
				return widget instanceof MenuItem && stripAmpersands(((MenuItem)widget).getText()).equals(name);
			}}, c);
		if (refs == null || refs.length == 0){
			dumpControl(c);
			throw new WidgetSearchException("Cannot find menu " + name);
		}
		if (refs.length > 1) {
			throw new WidgetSearchException("Menu item name is ambiguous: " + name);
		}
		return (MenuItem)refs[0].getWidget();
	}
	
	public static IEditorPart getActiveEditor(){
	    final IEditorPart result[] = new IEditorPart[1];
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable(){

            @Override
            public void run () {
                IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                result[0] = activePage.getActiveEditor();                  
                }              
            });
        return result[0];
	}
	
	public static ITextEditor getActiveTextEditor() {
	    IEditorPart part = getActiveEditor();
	    if (part instanceof ITextEditor){
	        return (ITextEditor)part;
	    }
	    return null;
	}

}
