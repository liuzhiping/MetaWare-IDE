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
package com.arc.cdt.debug.seecode.ui.views;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget3;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.cdt.debug.seecode.ui.UISeeCodePlugin;
import com.arc.cdt.debug.seecode.ui.Utilities;
import com.arc.mw.util.StringUtil;
import com.arc.seecode.cmpd.ICMPDController;
import com.arc.seecode.command.CommandFactory;
import com.arc.seecode.command.ICommandExecutor;
import com.arc.seecode.command.ICommandProcessor;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;
import com.arc.seecode.engine.StackFrameRef;


/**
 * The command view consists of an editable combobox by which users
 * can send commands to the SeeCode engine.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class SeeCodeCommandView extends AbstractEngineBasedView {
    
    public static final String VIEW_ID = "com.arc.cdt.debug.seecode.ui.command";

    private static final String ITEM_COUNT = "item.count";
    private String[] mItems = new String[0];
    private StackFrameRef mStackFrameRef;
    private Composite mainPanel;

    private ICDISession fSelectedSession = null;
    
    private List<ICommandProcessor>fCommandProcs = new ArrayList<ICommandProcessor>();
    
    private Map<EngineInterface,ICommandProcessor>fProcMap = new HashMap<EngineInterface,ICommandProcessor>();

    public SeeCodeCommandView() {
        super();
    }
   
    @Override
    protected String computeViewTitle (EngineInterface engine) {
        return "Debugger Commands";
    }
  
    @Override
    protected void initEngineView (EngineInterface engine, Composite control) {
        control.setLayout(new GridLayout(2,false));
        ICommandProcessor cmdProc = getCommandProcessor(engine);
        if (cmdProc != null){
            Combo combo = createComboBox(control,cmdProc);
            combo.setData("name","SeeCodeCommandText"); // for GUI tester
            combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            createClearButton(control, combo);
        }
        control.pack();
        wireInHelp(control);
    }
    
    @Override
    protected String getHelpID(){
        return IContextHelpIds.COMMAND;
    }

    /**
     * Create the button that clears the combobox.
     * @param parent the parent container.
     */
    private void createClearButton (Composite parent, final Combo combo) {
        assert combo != null;
        Button clear = new Button(parent,SWT.PUSH);
        final Image image = UISeeCodePlugin.getDefault().getImage("icons/clear.gif");
        if (image != null){
            clear.setImage(image);      
        }
        else clear.setText("clear");
        clear.setData("name","SeeCodeCommandClear"); // for GUI tester
        clear.addSelectionListener(new SelectionListener(){

            @Override
            public void widgetSelected (SelectionEvent e) {
                combo.setItems(new String[0]);
                mItems = new String[0];
            }

            @Override
            public void widgetDefaultSelected (SelectionEvent e) {
                widgetSelected(e);
                
            }});
        clear.setToolTipText("Clear command history");
    }
    
    private void createDismissButton (Composite parent) {
        //NOTE: we would like to use a button, but SWT doesn't offer a borderless button
        // except as a toolbar item. Thus, we fake a borderless button by using a label
        // with mouse listeners.
        final Label button = new Label(parent,0);
        final Image image = UISeeCodePlugin.getDefault().getImage("icons/x.gif");
        final Image hoverImage = UISeeCodePlugin.getDefault().getImage("icons/x_over.gif");
        final Image pressedImage = UISeeCodePlugin.getDefault().getImage("icons/x_pressed.gif");
        if (image != null){
            button.setImage(image);       
        }
        else button.setText("dismiss");
        button.setData("name","SeeCodeCommandDismiss"); // for GUI tester
        final boolean mouseOnButton[] = new boolean[1];
        mouseOnButton[0] = false;
        if (hoverImage != null) {
            button.addMouseTrackListener(new MouseTrackListener() {

                @Override
                public void mouseEnter (MouseEvent e) {
                    button.setImage(hoverImage);
                    mouseOnButton[0] = true;
                }

                @Override
                public void mouseExit (MouseEvent e) {
                    button.setImage(image);
                    mouseOnButton[0] = false;

                }

                @Override
                public void mouseHover (MouseEvent e) {

                }
            });
        }
        button.addMouseListener(new MouseListener(){

            @Override
            public void mouseDoubleClick (MouseEvent e) {
                // @todo Auto-generated method stub
                
            }

            @Override
            public void mouseDown (MouseEvent e) {
                button.setImage(pressedImage);
                
            }

            @Override
            public void mouseUp (MouseEvent e) {
                if (mouseOnButton[0]) {
                    button.setImage(hoverImage);
                    getViewSite().getPage().hideView(SeeCodeCommandView.this);
                }
                
            }});
       
        button.setToolTipText("Close this view.");
    }

    /**
     * Create the combobox from which SeeCode command will be sent
     * to the engine.
     * @param parent the parent container.
     */
    private Combo createComboBox (Composite parent, final ICommandProcessor cmdProc) {
        final Combo combo = new Combo(parent,SWT.DROP_DOWN);
        combo.addSelectionListener(new SelectionListener(){

            @Override
            public void widgetSelected (SelectionEvent e) {
                
            }

            @Override
            public void widgetDefaultSelected (SelectionEvent e) {
                String cmd = combo.getText();
                if (cmd.trim().length() > 0){
                    addItemToCombo(combo,cmd);
                    sendCommand(cmd/*, cmdProc*/);
                    combo.setSelection(new Point(0,cmd.length()));
                }   
            }});
        combo.setItems(mItems);
        FormData fd = new FormData();
        fd.top = new FormAttachment(0,3);
        fd.left = new FormAttachment(0,3);
        fd.right = new FormAttachment(100,-30);
        combo.setLayoutData(fd);
        return combo;
    }
    
    private void addItemToCombo(Combo combo, String item){
        String[] items = combo.getItems();
        int index = Arrays.asList(items).indexOf(item);
        if (index >= 0) {
            combo.remove(index);
        }
        combo.add(item,0);
        mItems = combo.getItems();
        combo.select(0);       
    }

    /**
     * Create a command processor on behalf of an engine instance.
     * @param engine the engine instance.
     * @return the newly-created command processor or <code>null</code> if
     * something went wrong.
     */
    private ICommandProcessor getCommandProcessor (EngineInterface engine) {
        ICommandProcessor cp = fProcMap.get(engine);
        if (cp == null) {
            try {
                cp = CommandFactory.createCommandProcessor(engine, engine.getOutputStream(), engine.getErrorStream());
                // Since we want "stop" to stop animation if it is running, we must override it
                cp.addCommandExecutor("stop", new ICommandExecutor() {

                    @Override
                    public void execute (String arguments) throws Exception {
                        ICDIThread t = getThread();
                        if (t != null) {
                            t.suspend();
                        }
                        else if (getEngine() != null) {
                            getEngine().stop(0); // shouldn't get here
                        }
                    }

                    @Override
                    public boolean repeat () throws Exception {
                        return false;
                    }
                });
                
                cp.addCommandExecutor("restart", new ICommandExecutor(){

                    @Override
                    public void execute (String arguments) throws Exception {
                        ICDITarget target = getTarget();
                        if (arguments == null || arguments.length() == 0){
                            //Invoke "restart" from ICDebugTarget so that temp bkpt on main is set.
                            ILaunch launch = getLaunch();
                            if (launch != null){
                                IDebugTarget targs[] = launch.getDebugTargets();
                                for (IDebugTarget t: targs){
                                    if (t.getAdapter(ICDITarget.class) == target) {
                                        ((ICDebugTarget)t).restart();
                                        return;
                                    }
                                }
                            }
                            // Shouldn't get here.
                            target.restart();
                        }
                        else {
                            // Restart with new arguments won't resume beyond _start.
                            String args[] = StringUtil.stringToArray(arguments);
                            ((ICDITarget3)target).restart(args);
                        }                  
                    }

                    @Override
                    public boolean repeat () throws Exception {
                        return false;
                    }});
                fProcMap.put(engine, cp);
            }
            catch (EngineException e1) {
                SeeCodePlugin.log(e1);
            }
        }
        return cp;
    }
    
    private void sendCommand (String command) {
        command = command.trim();
        if (command.length() > 0) {
            try {

                // <CMPD HACK>
                // If the user selected the launch, instead of a particular target,
                // and there is multiple targets, then just blindly pass the command
                // to the debugger engine as a CMPD command.
                // This could probably be done better if we extracted out the correct
                // interface from "EngineInterface" and have Session implement the same
                // interface. But we don't know what to do with target-specific commands
                // (e.g. "getMemory()") when there is not specific target selected.
                if (fSelectedSession != null && fSelectedSession instanceof IAdaptable) {
                    ICMPDController cmpd = (ICMPDController) ((IAdaptable) fSelectedSession)
                        .getAdapter(ICMPDController.class);
                    if (cmpd != null) {
                        cmpd.invokeCommand(command);
                        return;
                    }
                }
                //</CMPD HACK>

                // For CMPD, there could be more than one process highlighted, but not all.
                for (ICommandProcessor proc: fCommandProcs) {
                    proc.setStackFrame(mStackFrameRef);
                    proc.processCommand(command);
                }
                getErrorStream().flush();
                getOutputStream().flush();
            }
            catch (RuntimeException e){
                throw e;
            }
            catch (Exception e) {
                try {
                    getErrorStream().write(("[SEECODE] " + e.getMessage()).getBytes());
                    getErrorStream().write('\n');
                }
                catch (IOException e1) {
                    SeeCodePlugin.log(e1);
                }
            }
        }
    }
    
    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param engine
     * @param sf
     */
    @Override
    protected void setStackFrame (EngineInterface engine, StackFrameRef sf) {
        super.setStackFrame(engine, sf);
        mStackFrameRef  = sf;       
    }

    private OutputStream getErrorStream(){
        return getEngine().getErrorStream();
    }
    
    private OutputStream getOutputStream(){
        return getEngine().getOutputStream();
    }
    
    @Override
    public void init (IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);
        if (memento != null) {
            Integer itemCnt = memento.getInteger(ITEM_COUNT);
            if (itemCnt != null) {
                int cnt = itemCnt.intValue();
                String items[] = new String[cnt];
                for (int i = 0; i < cnt; i++) {
                    items[i] = memento.getString("item." + i);
                }
                mItems = items;
            }
        }
    }

    @Override
    public void saveState (IMemento memento) {
        super.saveState(memento);
        if (mItems != null) {
            memento.putInteger(ITEM_COUNT, mItems.length);
            for (int i = 0; i < mItems.length; i++) {
                memento.putString("item." + i, mItems[i]);
            }
        }
    }

    @Override
    protected void unwireEngine (EngineInterface engine) {
        fProcMap.remove(engine);
        super.unwireEngine(engine);
    }

    @Override
    public void createPartControl (Composite parent) {
        // We went the "dismiss" button to be always visible in the view. So don't put
        //it in the engine card stack.
        mainPanel = new Composite(parent,0);
        mainPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout gridLayout = new GridLayout(2,false);
        gridLayout.horizontalSpacing = 0;
        mainPanel.setLayout(gridLayout);      
        super.createPartControl(mainPanel);
        mainPanel.getChildren()[0].setLayoutData(new GridData(GridData.FILL_BOTH));
        createDismissButton(mainPanel);
    }

    @Override
    protected void setEngineSource (IStructuredSelection selection) {
        super.setEngineSource(selection);
        fSelectedSession = null;
        //If we're actually referencing the Launch (i.e. no particular target), and
        // we have a multi-process session, then send commands to the CMPD controller
        // so that they can be broadcast to every process, subject to focus qualifications.
        if (!selection.isEmpty()) {
            // At this point, we are NOT in the UI thread.
            Object element = selection.getFirstElement();
            if (element instanceof ILaunch){
                fSelectedSession  = computeSessionFrom(element);
            }
            else {
                fCommandProcs.clear();
                for (Object select: selection.toList()){
                    EngineInterface engine = Utilities.computeEngineFromSelection(select);
                    if (engine != null) {
                        ICommandProcessor cp = getCommandProcessor(engine);
                        if (!fCommandProcs.contains(cp)) 
                            fCommandProcs.add(cp);
                    }                   
                }
            }
        }
    }
}
