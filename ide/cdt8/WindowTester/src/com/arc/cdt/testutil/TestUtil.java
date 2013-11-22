/*
 * TestUtil
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.PlatformUI;

import com.arc.tests.database.DatabaseStuff;
import com.arc.tests.database.ITestDataBase;
import com.windowtester.runtime.swt.UITestCaseSWT;


public class TestUtil {

    /**
     * Extract tests that are in a package, and add them to the
     * test suite.
     * @param suite the test suite to fill in with tests.
     * @param package1 the package from which tests are to be extracted.
     */
    @SuppressWarnings("unchecked")
    public static void extractTests (TestSuite suite, Package package1) {
        EclipseUtil.setCanonicalSize(); // make sure things are correct size
        EclipseUtil.keepWindowActive(); // Don't let it be hidden
        EclipseUtil.closeAllEditors();
        
        Class< ? extends TestCase> classes[];
        try {
            classes = (Class< ? extends TestCase>[]) PackageUtil.getClasses(package1.getName(),UITestCaseSWT.class,false);
            Arrays.sort(classes, new Comparator<Class<?>>(){
    
                @Override
                public int compare(Class<?> o1, Class<?> o2) {
                    return o1.getName().compareTo(o2.getName());
                }});
        }
        catch (IOException e) {
            classes = (Class< ? extends TestCase>[]) new Class<?>[0];
            e.printStackTrace();
        }
        clearTestsFromDatabase(classes);
        for (Class< ? extends TestCase> c: classes){
            suite.addTestSuite(c);
        }
    }

    private static void clearTestsFromDatabase (Class< ? >[] classes) {
        // Remove previous contents of these tests from database so that we know if they have run.
        ITestDataBase db = DatabaseStuff.getDatabase();
        if (db != null){
            for (Class<?>c: classes){
                db.removeTest(c.getSimpleName());
            }
        }
    }
    
    public static void extractSubsetOfTests(final TestSuite suite, String packageNames, boolean transitively){
    	extractSubsetOfTests(suite,new String[]{packageNames},transitively);
    }
    
    /**
     * Pop up a dialog that lists all of the tests in a package. Add the tests that
     * the user selects to a test suite.
     * @param suite the test suite to fill in with tests.
     * @param packageName the name of the package from which tests are to be extracted.
     */
    @SuppressWarnings("unchecked")
    public static void extractSubsetOfTests(final TestSuite suite, String packageNames[], boolean transitively){
        EclipseUtil.setCanonicalSize(); // make sure things are correct size
        EclipseUtil.keepWindowActive(); // Don't let it be hidden
        EclipseUtil.closeAllEditors();

        final List<Class<? extends TestCase>> classList = new ArrayList<Class<? extends TestCase>>();
        try {
			for (String packageName : packageNames) {
				Class<? extends TestCase>[] classes = (Class< ? extends TestCase>[]) PackageUtil.getClasses(packageName,
						UITestCaseSWT.class, transitively);
				Arrays.sort(classes, new Comparator<Class<?>>() {

					@Override
                    public int compare(Class<?> o1, Class<?> o2) {
						return o1.getName().compareTo(o2.getName());
					}
				});
				classList.addAll(Arrays.asList(classes));
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

            @Override
            public void run () {
                final Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay(), SWT.APPLICATION_MODAL|SWT.DIALOG_TRIM|SWT.RESIZE);
                shell.setLayout(new GridLayout(1,false));
                final Table list = new Table(shell, SWT.CHECK|SWT.V_SCROLL|SWT.H_SCROLL);
                final ITestDataBase db = DatabaseStuff.getDatabase();
                list.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
                for (Class< ? > c : classList) {
                    TableItem item = new TableItem(list,0);
                    item.setText(c.getName());
                    String name = c.getSimpleName();
                    item.setChecked(!db.isKnown(name) || !db.isPassed(name));
                }
                Button ok = new Button(shell, SWT.PUSH);
                ok.setText("OK");
                ok.addSelectionListener(new SelectionListener() {

                    @Override
                    public void widgetDefaultSelected (SelectionEvent e) {
                        TableItem[] items = list.getItems();
                        
                        for (int i = 0; i < items.length; i++) {
                            if (items[i].getChecked()) {
                                if (db != null) db.removeTest(classList.get(i).getSimpleName());
                                suite.addTestSuite(classList.get(i));
                            }
                        }
                        shell.dispose();

                    }

                    @Override
                    public void widgetSelected (SelectionEvent e) {
                        widgetDefaultSelected(e);

                    }
                });
                shell.pack();
                shell.setSize(shell.getSize().x,800);
                shell.setVisible(true);
                while (!shell.isDisposed()) {
                    if (!shell.getDisplay().readAndDispatch())
                        shell.getDisplay().sleep();
                }
            }
        });
        
    }

}
