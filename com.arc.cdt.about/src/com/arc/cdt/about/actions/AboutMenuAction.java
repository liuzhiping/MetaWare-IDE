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
package com.arc.cdt.about.actions;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import com.arc.cdt.about.AboutPlugin;
import com.arc.mw.util.ver.FileInfoExtractor;
import com.arc.mw.util.ver.VersionInfo;


public class AboutMenuAction implements IWorkbenchWindowActionDelegate {

    
    private static int BOTTOM_MARGIN = 40;

    private IWorkbenchWindow window;

    // NOTE: these tables are a sequence of pairs. First of each pair is the
    // name of hte component, second is the executable name.
    // The executable name may be ambiguous among toolsets; in such a case,
    // we list two executables. The first is to be sought and the second is assumed
    // to be in the same directory as the first, and contains the version and license info.

    private static String win32ExesArc[] = new String[] {
            "Compiler",
            "hcac1.exe",
            "Linker",
            "ldac.exe",
            "Assembler",
            "asac.exe",
            "Debugger",
            "scac.exe,crout.dll" };

    private static String win32ExesVC[] = new String[] {
            "Compiler",
            "hcvc1.exe",
            "Linker",
            "ldvc.exe",
            "Assembler",
            "asvc.exe",
            "Debugger",
            "scvc.exe,crout.dll" };

    private static String linuxExesArc[] = new String[] {
            "Compiler",
            "hcac,hc@l3861com",
            "Linker",
            "ldac",
            "Assembler",
            "asac",
            "Debugger",
            "scac,libcrout.so" };

    private static String linuxExesVC[] = new String[] {
            "Compiler",
            "hcvc,hc@l3861com",
            "Linker",
            "ldvc",
            "Assembler",
            "asvc",
            "Debugger",
            "scvc,libcrout.so" };

    private static SortedMap<String, String[]> TOOLSETS = new TreeMap<String, String[]>();

    static {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.indexOf("windows") >= 0) {
            TOOLSETS.put("ARC Toolset", win32ExesArc);
            TOOLSETS.put("VideoCore Toolset", win32ExesVC);
        }
        else {
            TOOLSETS.put("ARC Toolset", linuxExesArc);
            TOOLSETS.put("VideoCore Toolset", linuxExesVC);
        }
    }

    private Shell aboutBox;

    static class Line {

        Line(int x, int y, String content, Font font) {
            this.x = x;
            this.y = y;
            this.content = content;
            this.font = font;
        }

        public int x;

        public int y;

        public String content;

        public Font font;

        @Override
        public String toString () {
            return "" + x + "," + y + ": " + content;
        }
    }

    private List<Line> lines = new ArrayList<Line>();

    private Font normalFont;

    private Font titleFont;
    
    private Font smallFont;

    private int canvasHeight;

    private Image doubleBuffer = null;

    @Override
    public void dispose () {
        // TODO Auto-generated method stub

    }

    @Override
    public void init (IWorkbenchWindow w) {
        this.window = w;

    }

    private Image createImage (String name) {
        InputStream stream = AboutPlugin.class.getResourceAsStream(name);
        Image image = null;
        try {
            image = new Image(null, stream);
            stream.close();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return image;
    }

    @Override
    public void run (final IAction action) {
        aboutBox = null;
        titleFont = new Font(window.getWorkbench().getDisplay(), "Arial", 14, SWT.BOLD | SWT.ITALIC);
        normalFont = new Font(window.getWorkbench().getDisplay(), "Arial", 10, SWT.NORMAL);
        smallFont = new Font(window.getWorkbench().getDisplay(), "Arial", 9, SWT.NORMAL);
        lines.clear();
        // To read version info from exes may take too long to tie up UI thread.
        // We spawn a separate thread to do it.
        Thread aboutThread = new Thread(new Runnable() {

            @Override
            public void run () {
                try {
                    if (!readAndDisplayVersionInfo()) {
                        action.setEnabled(false);
                    }
                }
                catch (RuntimeException exc) {
                    AboutPlugin.log("Error in reading version & license info",exc);
                }
                catch (Error err){
                    AboutPlugin.log("Error in reading version & license info",err);
                    throw err;
                }

            }
        }, "MetaWare About Thread");
        aboutThread.setDaemon(true);
        aboutThread.start();
        showAboutBox();    
    }

    // NOTE: this is called from a special thread that we created in run() above.
    // Returns true if at least one toolset found in search path.
    // Also note: it is designed so that the UI thread is not blocked while extracting
    // licensing and version information. Things are displayed dynamically as they are extracted.
    private boolean readAndDisplayVersionInfo () {
        int x = 20;
        int startY = 80;
        int y = startY;
        
        for (Map.Entry<String,String[]> entry: TOOLSETS.entrySet()){
            boolean titleWritten = false;
           
            String exes[] = entry.getValue();
            for (int i = 0; i < exes.length; i+=2){
                String componentName = exes[i];
                String exe = exes[i+1];
                File directoryOf = null;
                int comma = exe.indexOf(',');
                if (comma > 0){
                    directoryOf = findDirectoryOf(exe.substring(0,comma));
                    exe = exe.substring(comma+1);
                }
                else{
                    directoryOf = findDirectoryOf(exe);
                }
                if (directoryOf != null){
                    File file = new File(directoryOf,exe);
                    FileInfoExtractor info = new FileInfoExtractor(file);
                    try {
                        if (!titleWritten) {
                            // "ARC Toolset" is now redundant with the name at the top of the screen.
                            // Omit it
                            if (!entry.getKey().equals("ARC Toolset")){
                                y+=25;
                                writeLine(x,y,entry.getKey(),titleFont);
                                y+=5;
                            }
                            titleWritten = true;
                        }
  
                        VersionInfo vinfo = info.extractVersionInfo();
                        if (vinfo != null) {
                            y += 25;
                            writeLine(x+30, y, vinfo.toString(componentName),normalFont);
                        }
                        else {
                            y += 25;
                            writeLine(x+30, y, "Version information missing from " + componentName + ": ",normalFont);
                            y += 20;
                            writeLine(x + 55, y, file.toString(),normalFont);
                        }
                    }
                    catch (IOException exc) {
                        y += 25;
                        writeLine(x+30, y, "Can't read " + componentName + ": ",normalFont);
                        y += 15;
                        writeLine(x + 50, y, file.toString() + ": " + exc.getMessage(),normalFont);
                    }
                }
            }
        }

        if (startY == y) {
            writeLine(x+20, 200, "No toolset components on search path",titleFont);
            return false;
        }
        return true;
    }

    @Override
    public void selectionChanged (IAction action, ISelection selection) {
        // TODO Auto-generated method stub

    }

    private void writeLine (final int x, final int y, final String msg, final Font font) {
        final Display display = PlatformUI.getWorkbench().getDisplay();
        if (display.getThread() != Thread.currentThread()) {
            display.asyncExec(new Runnable() {

                @Override
                public void run () {
                    writeLine(x, y, msg, font);
                }
            });
            return;
        }
        
        lines.add(new Line(x, y, msg, font));

        // At this point, the About box is showing, or has been disposed by the user.
        // If the latter case, we do nothing.
        if (!aboutBox.isDisposed()) {
            if (canvasHeight < y+BOTTOM_MARGIN){
                canvasHeight = y+BOTTOM_MARGIN;
                canvas.setSize(canvas.getSize().x,canvasHeight);
                aboutBox.pack(true);
            }
            Point size = aboutBox.getSize();
            aboutBox.redraw(0, 0, size.x, size.y, true);
        }
    }

    private void showAboutBox () {
        final Display display = PlatformUI.getWorkbench().getDisplay();
        canvasHeight = 200;
        try {
            Shell parentShell = display.getActiveShell();
            aboutBox = new Shell(parentShell, SWT.NO_TRIM | SWT.APPLICATION_MODAL);
            aboutBox.addKeyListener(new KeyListener() {

                @Override
                public void keyPressed (KeyEvent e) {
                }

                @Override
                public void keyReleased (KeyEvent e) {
                    aboutBox.dispose();
                }
            });

            canvas = new Canvas(aboutBox, SWT.NO_BACKGROUND|SWT.NO_REDRAW_RESIZE);
            canvas.addMouseListener(new MouseListener() {

                @Override
                public void mouseDoubleClick (MouseEvent e) {
                }

                @Override
                public void mouseDown (MouseEvent e) {
                }

                @Override
                public void mouseUp (MouseEvent e) {
                    aboutBox.dispose();
                }
            });
            backgroundImage = createImage("about.jpg");
            canvas.addPaintListener(new PaintListener() {

                @Override
                public void paintControl (PaintEvent e) {

                    paintContent(e.gc, display.getSystemColor(SWT.COLOR_WHITE));
                }
            });
            canvas.setLayout(null);
            canvas.setLocation(0, 0);
            aboutBox.setLayout(null);

            if (backgroundImage != null) {
                //canvasHeight = backgroundImage.getImageData().height;
                canvas.setSize(backgroundImage.getImageData().width, canvasHeight);
                doubleBuffer  = new Image(canvas.getDisplay(),backgroundImage.getBounds().width,backgroundImage.getBounds().height);
            }
            else {
                canvas.setSize(600, canvasHeight);
                doubleBuffer = new Image(canvas.getDisplay(),600,600);
            }
            aboutBox.pack();
            Point loc = parentShell.getLocation();
            Point size = parentShell.getSize();
            aboutBox.setLocation(loc.x + size.x / 2 - aboutBox.getSize().x / 2, loc.y +
                size.y /
                2 -
                400 / // Use max size instead of current size
                2);
            aboutBox.setVisible(true);

            while (!aboutBox.isDisposed()) {
                if (!display.readAndDispatch())
                    display.sleep();
            }
        }
        finally {
            titleFont.dispose();
            normalFont.dispose();
            smallFont.dispose();
            if (backgroundImage != null)
                backgroundImage.dispose();
            if (doubleBuffer != null)
                doubleBuffer.dispose();
            titleFont = null;
            normalFont = null;
            smallFont = null;
            doubleBuffer = null;
            backgroundImage = null;
        }
    }

    private void paintContent (GC gc, Color foreground) {
        // To avoid flicker, draw to image first
        GC gcShadow = new GC(doubleBuffer);
        try {
            int width = canvas.getSize().x;
            int height = canvas.getSize().y;
            gcShadow.drawImage(backgroundImage, 0, 0);
            gcShadow.setForeground(foreground);
            for (Line line : lines) {
                gcShadow.setFont(line.font);
                gcShadow.drawText(line.content, line.x, line.y, true);
            }

            gc.drawImage(doubleBuffer, 0, 0, width, height, 0, 0, width, height);
        }
        finally {
            gcShadow.dispose();
        }
    }


    private static File findDirectoryOf (String fileName) {
        String path = System.getenv("PATH");

        String dirs[] = path.split(File.pathSeparator);
        
        File dir = findDirectoryOf(dirs,fileName);
        if (dir == null) {
            // Path not set up; look at installation directory
            String installURI = System.getProperty("osgi.install.area");
            if (installURI != null) {
                try {
                    File ideDir = new File(new URI(installURI).toURL().getFile()).getParentFile();
                    List<String> paths = new ArrayList<String>();
                    for (String s : new String[] { "arc", "videocore", "VideoCore", "ARC" }) {
                        File f = new File(ideDir, s);
                        if (f.exists()) {
                            paths.add(f.toString());
                        }
                    }
                    if (paths.size() > 0){
                        dir = findDirectoryOf(paths.toArray(new String[paths.size()]),fileName);
                    }
                }
                catch (Exception e) {
                  // Do nothing if we can't figure out install location
                }
            }
        }
        return dir;
    }
                    
       
    private static File findDirectoryOf(String dirs[], String fileName){
        for (int i = 0; i < dirs.length; i++) {
            String filepath = dirs[i] + File.separator + fileName;
            File f = new File(filepath);
            if (f.exists()) {
                return f.getParentFile();
            }
        }
        return null;
    }

    private Canvas canvas;

    private Image backgroundImage;

}
