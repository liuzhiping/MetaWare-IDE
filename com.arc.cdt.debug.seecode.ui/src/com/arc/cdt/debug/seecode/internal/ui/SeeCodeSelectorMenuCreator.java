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

import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget3;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.cdt.debug.seecode.ui.UISeeCodePlugin;
import com.arc.cdt.debug.seecode.ui.views.MemorySearchView;
import com.arc.cdt.debug.seecode.ui.views.SeeCodeCommandView;
import com.arc.seecode.display.DisplayMenuGenerator;
import com.arc.seecode.engine.EngineDisconnectedException;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;


/**
 * The creator for the SeeCode display menu.
 * <P>
 * The menu items are obtained by calling {@link EngineInterface#getDisplaySelectors()}.
 * The engine can have the menu refreshed by invoking the appropriate callback
 * method.
 * @author David Pickens
 */
class SeeCodeSelectorMenuCreator implements IMenuCreator {

    private EngineInterface mEngine;
    private Menu mMenu = null;

    SeeCodeSelectorMenuCreator(EngineInterface engine){
        mEngine = engine;
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IMenuCreator#dispose()
     */
    @Override
    public void dispose() {
        if (mMenu != null)
            mMenu.dispose();
        
    }


    @Override
    public Menu getMenu(Control parent) {
        if (mMenu != null) {
            mMenu.dispose(); // may have changed.
        }
        mMenu = new Menu(parent);
        fillMenu(mMenu);
        return mMenu;
    }
    
    @Override
    public Menu getMenu(Menu parent) {
        if (mMenu != null) {
            mMenu.dispose(); // may have changed.
        }
        mMenu = new Menu(parent);
        fillMenu(mMenu);
        return mMenu;
    }
    
    @SuppressWarnings("unused")
    private void fillMenu(Menu menu) {
        if (mEngine == null) // shouldn't happen
            return;
        String[] selectors;
        try {
            selectors = mEngine.getDisplaySelectors();
        } catch (EngineDisconnectedException e){
            //Engine has terminated.
            // We shouldn't be populating a menu under
            // these conditions, but things happen
            return;            
        } catch (EngineException e) {
            SeeCodePlugin.log(e);
            return;
        }
        DisplayMenuGenerator gen = new DisplayMenuGenerator(selectors);
        gen.generate(new DisplayMenuGenerator.ICallback() {

            @Override
            public Object createSubmenu(String name, Object parent) {
                MenuItem item = new MenuItem((Menu) parent, SWT.CASCADE);
                item.setText(name);                
                Menu submenu = new Menu(item);
                item.setMenu(submenu);
                return submenu;
            }

            @Override
            public void createMenuItem(final String kind, String label,
                    Object parent) {
                MenuItem item = new MenuItem((Menu) parent, SWT.PUSH);
                item.setText(label);
                item.addSelectionListener(new SelectionListener() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                            //Tell engine to generate display of
                            // given type.
                       createDisplay(kind);
                    }

                    @Override
                    public void widgetDefaultSelected(SelectionEvent e) {
                        widgetSelected(e);
                    }
                });
            }

            @Override
            public void error(String msg, Exception e) {
                SeeCodePlugin.log(msg, e);
            }
        }, menu);
        new MenuItem(menu,SWT.SEPARATOR);
        appendCommand(menu);
        appendFile2Mem(menu);
        appendMemorySearch(menu);
        new MenuItem(menu,SWT.SEPARATOR);
        appendLoad(menu,"Download File...","download","Path to executable to download");
        appendLoad(menu,"Load Symbols...","symbols","Executable from which symbols are to be extracted");
        new MenuItem(menu,SWT.SEPARATOR);
        appendRefresh(menu);
        new MenuItem(menu,SWT.SEPARATOR);
        appendAboutBox(menu);
    }
    
    private void appendLoad(Menu menu,String title, final String cmd, final String description){
    	MenuItem load = new MenuItem(menu,SWT.PUSH);
    	load.setText(title);
    	load.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
		        Display display = PlatformUI.getWorkbench().getDisplay();
	            Shell parent = display.getActiveShell();
	            FileDialog f = new FileDialog(parent,SWT.OPEN);
	            f.setText(description);
	            f.setFilterPath(defaultDirectory);
	            String fn = f.open();
	            if (fn != null){
	            	try {
	            	    if (fn.indexOf(' ') > 0){ //cr100758
	            	        fn = "\"" + fn + "\"";
	            	    }
						mEngine.invokeCommand(cmd + " " +fn);
					} catch (EngineException e1) {
						UISeeCodePlugin.showError("Write error","Couldn't write file " + fn, e1);
					}
	            }
				
			}});
    }
    
    private String defaultDirectory = ".";
    private void appendRefresh(Menu menu){
        MenuItem refresh = new MenuItem(menu,SWT.PUSH);
        refresh.setText("Refresh displays");
        refresh.addSelectionListener(new SelectionListener(){

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    ICDITarget t = SeeCodePlugin.getEngineTarget(mEngine);
                    if (t instanceof ICDITarget3){
                        ((ICDITarget3)t).refreshViews();
                    }
                    else mEngine.refreshDisplays(); // Shouldn't get here.
                } catch (EngineException e1) {
                    SeeCodePlugin.log(e1);
                }       
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
                
            }});
        refresh.setImage(UISeeCodePlugin.getDefault().getImage("icons/refresh.gif"));
    }
    
    private void appendFile2Mem(Menu menu){
        
        //Now add selection for file-to-memory operations.
       
        MenuItem file2mem = new MenuItem(menu,SWT.PUSH);
        file2mem.setText("File-memory-fill operation...");
        file2mem.addSelectionListener(new SelectionListener(){

            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    mEngine.invokeCommand("file2mem -gui");
                } catch (EngineException e1) {
                    SeeCodePlugin.log(e1);
                }            
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
                
            }});
    }
    
    private void appendMemorySearch(final Menu menu){
        MenuItem item = new MenuItem(menu,SWT.PUSH);
        item.setText("Search memory");
        item.addSelectionListener(new SelectionListener(){
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    MemorySearchView view = (MemorySearchView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .showView(MemorySearchView.VIEW_ID, null,
                            IWorkbenchPage.VIEW_ACTIVATE);
                    view.setEngineSource(mEngine);
                } catch (PartInitException e1) {
                    SeeCodePlugin.log(e1);
                }              
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);   
            }});
    }
    
    private void appendAboutBox (final Menu menu) {
        if (mEngine.isSplashPathSupported()) {
            MenuItem item = new MenuItem(menu, SWT.PUSH);
            item.setText("About MetaWare Debugger");
            item.addSelectionListener(new SelectionListener() {

                @Override
                public void widgetSelected (SelectionEvent e) {
                    doAboutBox();
                }

                @Override
                public void widgetDefaultSelected (SelectionEvent e) {
                    widgetSelected(e);
                }
            });
        }
    }
    
    private void doAboutBox(){
        final Display display = PlatformUI.getWorkbench().getDisplay();
        Shell parent = display.getActiveShell();
        final Shell popup = new Shell(parent,SWT.NO_TRIM|SWT.APPLICATION_MODAL);
        popup.addKeyListener(new KeyListener(){
            @Override
            public void keyPressed (KeyEvent e) {  }
            @Override
            public void keyReleased (KeyEvent e) {
                popup.dispose();               
            }});

        final Canvas canvas = new Canvas(popup,0);
        canvas.addMouseListener(new MouseListener(){
            @Override
            public void mouseDoubleClick (MouseEvent e) { }
            @Override
            public void mouseDown (MouseEvent e) { }
            @Override
            public void mouseUp (MouseEvent e) {
                popup.dispose();             
            }});
        canvas.setLayout(null);
        canvas.setLocation(0,0);
        popup.setLayout(null);
        Image image = null;
        boolean sizeSet = false;
        try {
            String path = mEngine.getSplashPath();
            if (path != null) {
                image = new Image(popup.getDisplay(),path);
                canvas.setSize(image.getImageData().width,image.getImageData().height);
                sizeSet = true;
            }
        }
        catch (SWTException x) {
            // shouldn't get here.
        }
        catch (EngineException e) {
            //Engine failed to pass back splash image path.
        }
        if (!sizeSet){
            canvas.setSize(600,400);
        }
        final boolean isBlank = !sizeSet;
        
        final boolean synopsys = canvas.getSize().x > 700; // set to true if we are showing new Synopsys screen

        final Image image_ = image;
        canvas.addPaintListener(new PaintListener(){
            private static final int RIGHT_MARGIN = 7;
            private static final int GAP = 17;

            @Override
            public void paintControl (PaintEvent e) {
                GC g = e.gc;
                if (image_ != null){
                    g.drawImage(image_,0,0);
                }
                int y = synopsys?290:160; // trial and error
                int valueWidth = 120; // by trial and error
                int dw = canvas.getSize().x;
                String[] info;
                try {
                    info = mEngine.getEngineVersionStrings();
                }
                catch (EngineException e2) {
                    info = new String[]{null,null,null};
                }
                Font smallFont = new Font(display,"Arial",10,SWT.NORMAL);
                Font savedFont = g.getFont();
                Color savedColor = g.getForeground();
                g.setFont(smallFont);
                g.setForeground(display.getSystemColor(synopsys||isBlank?SWT.COLOR_BLACK:SWT.COLOR_WHITE));
                if (info[1] != null) valueWidth = g.stringExtent(info[1]).x;
                if (info[0] != null){
                    String label = "Debugger Product Version:";
                    int x = synopsys?dw/2:dw - valueWidth - RIGHT_MARGIN;
                    e.gc.drawString(info[0],x,y,true);
                    x -= GAP + e.gc.stringExtent(label).x;
                    g.drawString(label,x,y,true);
                }
                y += 22;
                g.setFont(smallFont);
                if (info[2] != null){
                    String label = "Engine Version:";
                    int x = synopsys?dw/2:dw - valueWidth - RIGHT_MARGIN;               
                    g.drawString(info[2], x, y,true);
                    x -= GAP + g.stringExtent(label).x;
                    g.drawString(label,x,y,true);
                }
                
                y += 22;
                if (info[1] != null) {
                    String label = "Engine Build Date:";
                    int x = synopsys?dw/2:dw - valueWidth - RIGHT_MARGIN;
                    g.drawString(info[1], x, y,true);
                    x -= GAP + g.stringExtent(label).x;
                    g.drawString(label,x,y,true);
                }
                y += 22;
                int days;
                try {
                    days = mEngine.getLicenseExpirationDays();
                }
                catch (EngineException e1) {
                   days = -1;
                }
                String label = "Debugger License Expiration:";
                int x = synopsys?dw/2:dw - valueWidth - RIGHT_MARGIN;
                String v;
                if (days < 0) v = "Not Scheduled";
                else
                if (days == 0) v = "today";
                else if (days == 1) v = "tomorrow";
                else v = "" + days + " days";
                g.drawString(v, x, y,true);
                x -= GAP + g.stringExtent(label).x;
                g.drawString(label,x,y,true);
                smallFont.dispose();
                g.setFont(savedFont);
                g.setForeground(savedColor);
                
            }});
        popup.pack();
        Point loc = parent.getLocation();
        Point size = parent.getSize();
        popup.setLocation(loc.x + size.x/2 - popup.getSize().x/2,
                          loc.y + size.y/2 - popup.getSize().y/2);
        popup.setVisible(true);
        while (!popup.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
    }
    
    private void appendCommand(final Menu menu){
        MenuItem item = new MenuItem(menu,SWT.PUSH);
        item.setText("Command-line input");
        item.addSelectionListener(new SelectionListener(){
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    SeeCodeCommandView view = (SeeCodeCommandView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .showView(SeeCodeCommandView.VIEW_ID, null,
                            IWorkbenchPage.VIEW_ACTIVATE);
                    view.setEngineSource(mEngine);
                } catch (PartInitException e1) {
                    SeeCodePlugin.log(e1);
                }              
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);   
            }});
    }
    
    private IViewPart createDisplay(String kind) {
        return UISeeCodePlugin.getDefault().createDisplay(getEngine(),kind);
    }
    
    public void refresh(){
        if (mMenu != null && !mMenu.isDisposed()) {
            MenuItem items[] = mMenu.getItems();
            for (int i = 0; i < items.length; i++) {
                items[i].dispose();
            }
            fillMenu(mMenu);
        }
    }
    
    public EngineInterface getEngine(){
        return mEngine;
    }

}
