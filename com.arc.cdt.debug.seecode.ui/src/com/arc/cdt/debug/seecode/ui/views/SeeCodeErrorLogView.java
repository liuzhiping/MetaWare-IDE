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


import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.arc.cdt.debug.seecode.core.ISeeCodeConstants;
import com.arc.cdt.debug.seecode.core.SeeCodePlugin;
import com.arc.cdt.debug.seecode.ui.UISeeCodePlugin;
import com.arc.cdt.debug.seecode.ui.Utilities;
import com.arc.seecode.engine.EngineInterface;


/**
 * @author dpickens
 */
public class SeeCodeErrorLogView extends ViewPart implements IPropertyChangeListener {

    public static final String VIEW_ID = "com.arc.cdt.seecode.errorlog";

    private boolean fIsVisible = false;

    private StackLayout mCardLayout;

    private Composite mBlank;

    private Composite mCards;

    private EngineInterface fEngine;

    private Map<EngineInterface, Text> fMap = new WeakHashMap<EngineInterface, Text>();
    
    // This must be static so that we can keep the state of things going even when the view is dismissed.
    // Is there a better way to do this?
    private static Map<EngineInterface,StringBuilder> fContent = new HashMap<EngineInterface,StringBuilder>();
    
    private static ILaunchesListener fLaunchListener = null;

    private IDebugContextListener mDebugContextListener = new IDebugContextListener() {

        @Override
        public void debugContextChanged (DebugContextEvent event) {
            if (fIsVisible)
                setSelection(event.getContext());

        }
    };

    private IPartListener2 fPartListener = new IPartListener2() {

        @Override
        public void partActivated (IWorkbenchPartReference partRef) { }

        @Override
        public void partBroughtToTop (IWorkbenchPartReference partRef) {}

        @Override
        public void partClosed (IWorkbenchPartReference partRef) {
        }

        @Override
        public void partDeactivated (IWorkbenchPartReference partRef) {
        }

        @Override
        public void partHidden (IWorkbenchPartReference partRef) {
            if (partRef.getPart(false) == SeeCodeErrorLogView.this) {
                // System.out.println("HIDDEN");
                fIsVisible = false;
            }
        }

        @Override
        public void partInputChanged (IWorkbenchPartReference partRef) {
        }

        @Override
        public void partOpened (IWorkbenchPartReference partRef) {
        }

        @Override
        public void partVisible (IWorkbenchPartReference partRef) {
            if (partRef.getPart(false) == SeeCodeErrorLogView.this) {
                fIsVisible = true;
                // System.out.println("SHOWN");
                resetSelection();
            }
        }
    };

    public void write (final EngineInterface engine, final String string) {
        Display display = PlatformUI.getWorkbench().getDisplay();
        if (display.getThread() == Thread.currentThread()) {
            try {
                if (!fIsVisible)
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(VIEW_ID);
            }
            catch (PartInitException e) {
                UISeeCodePlugin.log(e);
            }
            StringBuilder buf = fContent.get(engine);
            if (buf == null) {
                buf = new StringBuilder();
                fContent.put(engine,buf);
            }
            int maxSize = SeeCodePlugin.getDefault().getPreferences().getInt(ISeeCodeConstants.PREF_MAX_ERROR_LOG_SIZE, ISeeCodeConstants.DEF_MAX_ERROR_LOG_SIZE);
            if (buf.length() + string.length() > maxSize){
                if (string.length() >= maxSize)
                    buf.delete(0,buf.length());
                else
                    buf.delete(0,maxSize-string.length());
            }
            buf.append(string);
            if (fEngine != engine) {
                setEngine(engine);
            }
            else {
                Text t = fMap.get(engine);
                if (t != null) {
                    t.setText(buf.toString());
                }
            }          
        }
        else {
            display.syncExec(new Runnable() {
                @Override
                public void run () {
                    write(engine,string);               
                }
            });
        }
    }

    @Override
    public void createPartControl (Composite parent) {
        mCards = new Composite(parent, 0);
        mCardLayout = new StackLayout();
        mCards.setLayout(mCardLayout);
        mBlank = new Composite(mCards, 0);
        mCardLayout.topControl = mBlank;
        // listen to selection in debug view
        IWorkbenchWindow w = this.getViewSite().getWorkbenchWindow();
        DebugUITools.getDebugContextManager().getContextService(w).addDebugContextListener(mDebugContextListener);
        getSite().getPage().addPartListener(fPartListener);
        
        setFont();
        setColor();
        JFaceResources.getFontRegistry().addListener( this );
        JFaceResources.getColorRegistry().addListener( this );
        fIsVisible = true;
        resetSelection();
        
        if (fLaunchListener == null) {
        	// Remove engine's error log cache when reference is removed from Launch View.
        	fLaunchListener = new ILaunchesListener(){

                @Override
                public void launchesRemoved (ILaunch[] launches) {
                    for (ILaunch launch: launches){
                    	IDebugTarget targets[] = launch.getDebugTargets();
                    	for (IDebugTarget target: targets) {
                    		ICDISession session = (ICDISession)target.getAdapter(ICDISession.class);
                    		for (ICDITarget cdiTarget: session.getTargets()) {
                    			if (cdiTarget instanceof IAdaptable) {
                    			    EngineInterface engine = (EngineInterface)((IAdaptable)cdiTarget).getAdapter(EngineInterface.class);
                    			    if (engine != null) {
                    			    	fContent.remove(engine);
                    			    }
                    			}
                    		}
                    	}
                    }                   
                }

                @Override
                public void launchesAdded (ILaunch[] launches) {   
                }

                @Override
                public void launchesChanged (ILaunch[] launches) {
                  
                }};
            DebugPlugin.getDefault().getLaunchManager().addLaunchListener(fLaunchListener);
        }
    }
    
    private void setFont(){
        Font font = UISeeCodePlugin.getSeeCodeFont();
        for (Text t: fMap.values()){
            t.setFont(font);
        }
    }
    
    private void setColor(){
        Color background = (Color)UISeeCodePlugin.getColorPreferences().getErrorLogBackgroundColor().getObject();
        Color foreground = (Color)UISeeCodePlugin.getColorPreferences().getErrorLogForegroundColor().getObject();

        for (Text t: fMap.values()){
            t.setBackground(background);
            t.setForeground(foreground);
        }
    }

    protected void setEngineSource (final IStructuredSelection selection) {
        if (fIsVisible && !selection.isEmpty()) {
            // At this point, we are NOT in the UI thread.
            final EngineInterface engine = Utilities.computeEngineFromSelection(selection);
            if (engine != null) {
                setEngine(engine);
            }
        }
    }

    private void resetSelection () {
        setSelection(getSite().getPage().getSelection(IDebugUIConstants.ID_DEBUG_VIEW));
    }

    protected void clearDisplay () {
        mCardLayout.topControl = mBlank;
        if (!mCards.isDisposed()) {
            mCards.layout();
            mCards.redraw();
        }
    }

    public void setEngine (final EngineInterface engine) {
        Display d = PlatformUI.getWorkbench().getDisplay();
        if (d.getThread() != Thread.currentThread()) {
            d.syncExec(new Runnable(){

                @Override
                public void run () {setEngine(engine);}
                });
            return;
        }
        if (fEngine != engine && engine != null) {
            Text t = fMap.get(engine);
            if (t == null) {
                ICDITarget target = SeeCodePlugin.getEngineTarget(engine);
                if (target != null) {
                    t = new Text(mCards, SWT.WRAP | SWT.READ_ONLY);              
                    fMap.put(engine, t);
                    setColor();
                    setFont();
                }
                else
                    return;
            }
            StringBuilder b = fContent.get(engine);
            t.setText(b != null?b.toString():"");
            fEngine = engine;
            mCardLayout.topControl = t;
            mCards.layout();
            mCards.redraw();
        }
    }

    protected void setSelection (ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            setEngineSource((IStructuredSelection) selection);
        }
    }

    @Override
    public void setFocus () {
        // TODO Auto-generated method stub

    }

    @Override
    public void propertyChange (PropertyChangeEvent event) {
        String prop = event.getProperty();
        if (prop.equals(UISeeCodePlugin.SEECODE_FONT)){
            setFont();
        }
        else if (UISeeCodePlugin.isKnownColorProperty(prop)){
            setColor();
        }
        
    }
}
