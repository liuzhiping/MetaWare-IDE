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
package com.arc.cdt.debug.seecode.internal.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class HelpDialog {

    /**
     * Show help dialog. Must be called from main UI thread.
     * Any key or mouse event will dismiss the dialog.
     * @param control
     * @param msg
     */
    public static void show(Control control, String msg){
        Shell parent = control.getShell();
        final Shell shell = new Shell(parent, SWT.NO_TRIM | SWT.APPLICATION_MODAL);
        shell.setLayout(new GridLayout(1,false));
        
        Text text = new Text(shell,SWT.BORDER|SWT.READ_ONLY|SWT.WRAP);
        text.setText(msg);
        text.setLayoutData(new GridData(GridData.FILL_BOTH));
        text.setBackground(text.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        
        Point size = text.computeSize(400,SWT.DEFAULT);
        final Display display = parent.getDisplay();
        Point location = display.map(control,null,0,0);
        shell.setLocation(location);
        shell.setSize(size.x+10,size.y+10);
        
        KeyListener keyListener = new KeyListener() {

            @Override
            public void keyPressed (KeyEvent e) {
                e.doit = false;   
                shell.dispose();
            }

            @Override
            public void keyReleased (KeyEvent e) {
                e.doit = false;            
                
            }};
        MouseListener mouseListener = new MouseListener() {

            @Override
            public void mouseDoubleClick (MouseEvent e) {
                
            }

            @Override
            public void mouseDown (MouseEvent e) {
                shell.dispose();
                
            }

            @Override
            public void mouseUp (MouseEvent e) {
                // @todo Auto-generated method stub
                
            }};
        MouseTrackListener trackListener = new MouseTrackListener(){

            @Override
            public void mouseEnter (MouseEvent e) {
                // @todo Auto-generated method stub
                
            }

            @Override
            public void mouseExit (MouseEvent e) {
                shell.dispose();
                
            }

            @Override
            public void mouseHover (MouseEvent e) {
                // @todo Auto-generated method stub
                
            }};
            
        text.addKeyListener(keyListener);
        text.addMouseListener(mouseListener);
        //NOTE: formerly we placed the mouse tracker on "shell", but that doesn't work
        // under Linux. Shell received "mouseExit" events when entering the nested text widget!
        text.addMouseTrackListener(trackListener);
              
        shell.open();
        
        Event mouseEvent = new Event();
        mouseEvent.x = location.x + size.x/2;
        mouseEvent.y = location.y + size.y/2;
        mouseEvent.type = SWT.MouseMove;
        display.post(mouseEvent);
        
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
    }
}
