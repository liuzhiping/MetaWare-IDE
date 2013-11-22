/*******************************************************************************
 * Copyright (c) 2005-2012 Synopsys, Incorporated
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Synopsys, Inc - Initial implementation 
 *******************************************************************************/
package com.arc.cdt.toolchain.ui;

import java.io.File;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


public class PDFAction implements IWorkbenchWindowActionDelegate {
    
    private static String CONTENTS_PDF = "docs/pdf/contents.pdf";
    private static String[] PDF_PATHs = { "../../arc/" + CONTENTS_PDF,
                                   "../../videocore/" + CONTENTS_PDF
    };

    private IWorkbenchWindow fWindow;


    public void dispose () {
        // @todo Auto-generated method stub

    }

    public void init (IWorkbenchWindow window) {
        fWindow = window;

    }
    
    private static File computePDFLocation(){
        String install = Platform.getInstallLocation().getURL().getFile();
        for (String s: PDF_PATHs){
            File f = new File(install + "/" + s);
            if (f.exists()) {
                return f;
           }           
        }
        // Hmm. IDE is apparently not running from its default location. Search the path.
        for (String s: System.getenv("PATH").split(File.pathSeparator)){
            if (s.endsWith("bin")){
                File bin = new File(s);
                if (bin.isDirectory()){
                    File f = new File(bin.getParentFile(),CONTENTS_PDF);
                    if (f.isFile()){
                        return f;
                    }                                     
                }
            }          
        }
        return null;
    }

    /**
     * Opens a system editor on the given file resource.
     *
     * @param action the originating action.
     */
 
    public void run (IAction action) {
        final File pdf = computePDFLocation();
        if (pdf == null) {
            MessageBox box = new MessageBox(fWindow.getShell(), SWT.OK | SWT.ICON_ERROR);
            box.setMessage("Could not locate the MetaWare toolset PDF files");
            box.setText("PDF Launch failure");
            box.open();
            return;
        }

        final boolean result[] = new boolean[1];
        result[0] = false;
        BusyIndicator.showWhile(fWindow.getShell().getDisplay(), new Runnable() {

            public void run () {
                result[0] = Program.launch(pdf.getAbsolutePath());
            }
        });

        if (!result[0]) {
            MessageBox box = new MessageBox(fWindow.getShell(), SWT.OK | SWT.ICON_ERROR);
            box.setMessage("Could not open PDF file " + pdf.getName());
            box.setText("PDF Launch failure");
            box.open();
        }
    }


    public void selectionChanged (IAction action, ISelection selection) {
        // @todo Auto-generated method stub

    }

}
