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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.launch.internal.ui.WorkingDirectoryBlock;
import org.eclipse.cdt.launch.ui.CLaunchConfigurationTab;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import com.arc.cdt.debug.seecode.core.ISeeCodeLaunchConfigurationConstants;
import com.arc.cdt.debug.seecode.ui.Utilities;
import com.arc.cdt.debug.seecode.ui.views.IContextHelpIds;
import com.arc.mw.util.StringUtil;


@SuppressWarnings("restriction")
public class CMPDAdditionalSettingsTab extends CLaunchConfigurationTab implements ICommandOptionsSetter{
    
    private WorkingDirectoryBlock fWorkingDirectoryBlock = new WorkingDirectoryBlock();
    private CMPDPrelaunchBlock fPrelaunchBlock = new CMPDPrelaunchBlock(new Runnable(){

        @Override
        public void run () {
            CMPDAdditionalSettingsTab.this.updateLaunchConfigurationDialog();
            
        }});
    private Text fCommandOptions;
    private boolean fCommandTextModified = false;


    public CMPDAdditionalSettingsTab() {
        // @todo Auto-generated constructor stub
    }

    @Override
    public void createControl (Composite parent) {
        // There has to be an easier way to get this panel to scroll. The following works
        // but seems like overkill. If anyone knows a better way, by all means simplify this
        // code.
        ScrolledComposite scroller = new ScrolledComposite(parent,SWT.V_SCROLL|SWT.H_SCROLL){
            @Override
            public Point computeSize(int hhint,int vhint, boolean changed){
                if (changed) {
                    Control content = getContent();
                    //We assume the trim has negligible demands on the content size.
                    //But we want to take into account wrapping labels when the width is constrained.
                    Point size = content.computeSize(hhint,vhint,true);
                    setMinSize(size);                    
                }
                return super.computeSize(hhint,vhint,changed);
            }
        };
        Composite cmpdPanel = new Composite(scroller,0);
        cmpdPanel.setLayoutData(new GridData(GridData.FILL_BOTH));
        scroller.setExpandHorizontal(true);
        scroller.setExpandVertical(true);
        scroller.setContent(cmpdPanel);
        cmpdPanel.setLayout(new GridLayout(1,false));
        setControl(scroller);
              
        this.createCommandPanel(cmpdPanel);
        
        fWorkingDirectoryBlock.createControl(cmpdPanel);
        GridData gdw = new GridData(GridData.FILL_HORIZONTAL|GridData.BEGINNING);
        fWorkingDirectoryBlock.getControl().setLayoutData(gdw);
        
        fPrelaunchBlock.createControl(cmpdPanel);
        gdw = new GridData(GridData.FILL_HORIZONTAL|GridData.BEGINNING);
        fPrelaunchBlock.getControl().setLayoutData(gdw);
        
        PlatformUI.getWorkbench().getHelpSystem().setHelp(cmpdPanel, IContextHelpIds.PREFIX + "cmpd_config");
        
    }

    @Override
    public String getName () {
        return "Additional Settings";
    }

    @SuppressWarnings("unchecked")
    @Override
    public void initializeFrom (ILaunchConfiguration configuration) {
        try {
            fWorkingDirectoryBlock.initializeFrom(configuration);
            List<String> args = configuration.getAttribute(
                ISeeCodeLaunchConfigurationConstants.ATTR_SWAHILI_ARGS,
                new ArrayList<String>(0));
            boolean prelaunch = configuration.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_PRELAUNCH,false);
            // Strip off prelaunch artifacts
            if (prelaunch) {
                int index = args.size() - 1;
                for (; index >= 0; index--) {
                    String s = args.get(index);
                    if (!s.startsWith("-prelaunch=") &&
                        !s.startsWith("-prelaunchdelay=") &&
                        !s.startsWith("-prelaunchwd=")) {
                        break;
                    }
                }
                args = args.subList(0, index + 1);
            }
            fPrelaunchBlock.initializeFrom(configuration);
            
            List<String> prelaunchAppHistory = configuration.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_PRELAUNCH_APP_HISTORY, (List<String>)null);
            List<String> prelaunchWdHistory = configuration.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_PRELAUNCH_WD_HISTORY, (List<String>)null);
            String app = configuration.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_PRELAUNCH_APP,(String)null);
            String wd = configuration.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_PRELAUNCH_WD,Utilities.getWorkspacePath());
            int delay = configuration.getAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_PRELAUNCH_DELAY,0);
            if (prelaunchAppHistory != null){
                fPrelaunchBlock.setAppHistory(prelaunchAppHistory.toArray(new String[prelaunchAppHistory.size()]));
            }
            else fPrelaunchBlock.setAppHistory(null);
            if (prelaunchWdHistory != null){
                fPrelaunchBlock.setWdHistory(prelaunchWdHistory.toArray(new String[prelaunchWdHistory.size()]));
            }
            else fPrelaunchBlock.setWdHistory(null);
            fPrelaunchBlock.setApplication(app);
            fPrelaunchBlock.setWorkingDir(wd);
            fPrelaunchBlock.setDelay(delay);
            fPrelaunchBlock.setPrelaunch(prelaunch);
           
            fCommandOptions.setText(StringUtil.listToArgString(args));
        }
        catch (CoreException e) {
            setErrorMessage(e.getMessage());
        }
    }

    @Override
    public void performApply (ILaunchConfigurationWorkingCopy configuration) {
        fWorkingDirectoryBlock.performApply(configuration);
        List<String> args = StringUtil.stringToList(fCommandOptions.getText());

        String appHist[] = fPrelaunchBlock.getAppHistory();
        String wdHist[] = fPrelaunchBlock.getWdHistory();
        String app = fPrelaunchBlock.getApplication();
        String wd = fPrelaunchBlock.getWorkingDir();
        int delay = fPrelaunchBlock.getDelay();
        if (fPrelaunchBlock.hasPrelaunch()) {
            List<String> newArgs = new ArrayList<String>(args.size() + 2);
            newArgs.addAll(args);
            newArgs.add("-prelaunch=" + app);
            if (wd != null && wd.trim().length() > 0) {
                newArgs.add("-prelaunchwd=" + wd);
            }
            if (delay > 0) {
                newArgs.add("-prelaunchdelay=" + delay + "000 Delay for pre-launched application to start...");
            }
            args = newArgs;
        }
        
        configuration.setAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_PRELAUNCH,fPrelaunchBlock.hasPrelaunch());
        configuration.setAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_PRELAUNCH_APP,app);
        configuration.setAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_PRELAUNCH_WD,wd);
        configuration.setAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_PRELAUNCH_DELAY,delay);

        if (appHist != null) {
            List<String> list = Arrays.asList(appHist);
            if (list.size() > 10)
                list = list.subList(0, 10);
            configuration.setAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_PRELAUNCH_APP_HISTORY, list);
        }
        if (wdHist != null) {
            List<String> list = Arrays.asList(wdHist);
            if (list.size() > 10)
                list = list.subList(0, 10);
            configuration.setAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_CMPD_PRELAUNCH_WD_HISTORY, list);
        }

        configuration.setAttribute(ISeeCodeLaunchConfigurationConstants.ATTR_SWAHILI_ARGS, args);
    }
    
    @Override
    public boolean canSave() {
        return getErrorMessage() == null;
    }
    
    @Override
    public String getErrorMessage() {
        String m = super.getErrorMessage();
        if (m == null) {
            m = fWorkingDirectoryBlock.getErrorMessage();
        }
        if (m == null && fPrelaunchBlock.hasPrelaunch()) {
            m = fPrelaunchBlock.getErrorMessage();
        }
        return m;
    }
    
    @Override
    public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
        super.setLaunchConfigurationDialog(dialog);
        fWorkingDirectoryBlock.setLaunchConfigurationDialog(dialog);
    }

    @Override
    public String getMessage() {
        String m = super.getMessage();
        if (m == null) {
            m = fWorkingDirectoryBlock.getMessage();
        }
       
        return m;
    }
    
    @Override
    public boolean isValid(ILaunchConfiguration config) {
        if (getErrorMessage() != null) return false;
        boolean v = super.isValid(config);
        if (v) {
           v = fWorkingDirectoryBlock.isValid(config);
        }
        return v;
    }

    @Override
    public void setDefaults (ILaunchConfigurationWorkingCopy configuration) {
        // @todo Auto-generated method stub

    }
    
    class MyCommandLayout extends Layout {

        @Override
        protected Point computeSize (Composite composite, int wHint, int hHint, boolean flushCache) {
            int height = 0;
            int width = 0;
            int defaultWidth;
            if (wHint != SWT.DEFAULT) {
                defaultWidth = wHint;
            }
            else {
                defaultWidth = fWorkingDirectoryBlock.getControl().computeSize(wHint,SWT.DEFAULT).x;
            }
            Control kids[] = composite.getChildren();
            for (int i = 0; i < kids.length-1; i++) {
                Control kid = kids[i];
                Point preferredSize = kid.computeSize(wHint,hHint,flushCache);
                height = Math.max(preferredSize.y,height);
                width += preferredSize.x;
            }
            if (kids.length > 0){
                Point size = kids[kids.length-1].computeSize(Math.max(defaultWidth-width,300), hHint);
                width += size.x;
                height = Math.max(height,size.y);
            }
            return new Point(width,height);
        }

        @Override
        protected void layout (Composite composite, boolean flushCache) {
            Control kids[] = composite.getChildren();
            Rectangle area = composite.getClientArea();
            int x = area.x;
            for (int i = 0; i < kids.length-1; i++){
                int w = kids[i].computeSize(SWT.DEFAULT, area.height).x;
                kids[i].setBounds(x,area.y,w,area.height);
                x += w;
                if (x > area.x+area.width) break;
            }
            if (x < area.x+area.width){
                int width = area.x+area.width - x;
                kids[kids.length-1].setBounds(x,area.y,width,area.height);
            }
        }      
    }
    
    @Override
    public void setCommandOptions(List<String> args){
        fCommandOptions.setText(StringUtil.listToArgString(args));
    }
    
    private void fireTextChange(){
        if (fCommandTextModified){
            fCommandTextModified = false;
            setDirty(true);
            this.updateLaunchConfigurationDialog();
        }
    }
    
    private Control createCommandPanel(Composite parent){
        Composite textPanel = new Composite(parent,0);
        textPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        textPanel.setLayout(new MyCommandLayout());
        Label label = new Label(textPanel,0);
        label.setText("Additional command-line options: ");
        fCommandOptions = new Text(textPanel,SWT.WRAP|SWT.V_SCROLL|SWT.BORDER);
        fCommandOptions.setData("name","cmpd_command_options"); // for GUI tester
        fCommandOptions.addModifyListener(new ModifyListener(){
            long timeOfLastFire = 0;
            @Override
            public void modifyText (ModifyEvent e) {
                fCommandTextModified = true;
                // Don't update too frequently or else typing will slow down.
                if (e.time > timeOfLastFire + 3000){
                    timeOfLastFire = e.time;
                    fireTextChange();
                }
                
            }});
        fCommandOptions.addFocusListener(new FocusListener(){

            @Override
            public void focusGained (FocusEvent e) {        
            }

            @Override
            public void focusLost (FocusEvent e) {
                 fireTextChange();                
            }});
        fCommandOptions.addMouseTrackListener(new MouseTrackListener(){

            @Override
            public void mouseEnter (MouseEvent e) {
                // @todo Auto-generated method stub
                
            }

            @Override
            public void mouseExit (MouseEvent e) {
                fireTextChange();               
            }

            @Override
            public void mouseHover (MouseEvent e) {
                // @todo Auto-generated method stub               
            }

           

          });
        return textPanel;
    }

}
