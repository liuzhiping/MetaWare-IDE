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
package com.arc.seecode.engine.display;

import com.arc.seecode.display.DisplayMenuGenerator;
import com.arc.seecode.display.ISeeCodeTextViewer;
import com.arc.seecode.display.ISeeCodeTextViewerCallback;
import com.arc.seecode.display.MenuDescriptor;
import com.arc.seecode.engine.EngineDisconnectedException;
import com.arc.seecode.engine.EngineException;
import com.arc.seecode.engine.EngineInterface;

/**
 * An implementation of the SeeCode display callback that
 * should be good enough to work for communicating with
 * the SeeCode engine.
 * @author David Pickens
 */
public class SeeCodeTextViewerCallback implements ISeeCodeTextViewerCallback {
    private EngineInterface mEngine;
    private AbstractCustomDisplayCallback mCustomDisplayCallback;
    private IDisplayCreator mDisplayCreator;
    /**
     * 
     * @param engine the SeeCode engine object.
     * @param c the object that the engine invokes to
     * control displays.
     * @param displayCreator a callback for creating displays. It is expected to directly
     * or indirectly call {@link EngineInterface#createDisplay}.
     */
    public SeeCodeTextViewerCallback(EngineInterface engine, AbstractCustomDisplayCallback c,
            IDisplayCreator displayCreator){
        if (engine == null || c == null || displayCreator == null)
            throw new IllegalArgumentException("Arguments must not be null");
        mEngine = engine;
        mCustomDisplayCallback = c;
        mDisplayCreator = displayCreator;
    }
    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ISeeCodeTextViewerCallback#sendValueUpdate(com.arc.seecode.display.ISeeCodeTextViewer,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public void sendValueUpdate(ISeeCodeTextViewer d, String propertyName,
            String value) {
        try {
            mEngine.sendValueUpdate(d!=null?d.getDisplayID():0,propertyName,value);
        } catch (EngineDisconnectedException e){
            // Ignore spurious GUI events after engine has terminated.
        } catch (EngineException e) {
            internalError(d,e.getMessage(),e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ISeeCodeTextViewerCallback#sendValueUpdate(com.arc.seecode.display.ISeeCodeTextViewer,
     *      java.lang.String, java.lang.String, int)
     */
    @Override
    public boolean sendValueUpdate(ISeeCodeTextViewer d, String propertyName,
            String value, int timeout) {
        try {
            return mEngine.sendValueUpdate2(d!=null?d.getDisplayID():0,propertyName,value,timeout);
        } catch (EngineDisconnectedException e){
            // Ignore spurious GUI events after engine has terminated.
        } catch (EngineException e) {
            internalError(d,e.getMessage(),e);
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ISeeCodeTextViewerCallback#onClose(com.arc.seecode.display.ISeeCodeTextViewer)
     */
    @Override
    public void onClose(ISeeCodeTextViewer d) {
        try {
            if (!mEngine.isShutdown()){
                mEngine.closeDisplay(d.getDisplayID());
            }
            else {
                // Engine shutdown. This close must have
                // been from dispose events on the parent
                // widgets.
                d.dispose();
            }
        } catch (EngineDisconnectedException e) {
            //When the debugger is shutting down,
            // the displays will attempt to close
            // themselves. But the engine may
            // be terminated before they have a
            // chance. So, ignore this exception.
        } catch (EngineException e) {
            internalError(d,e.getMessage(),e);
            d.dispose(); // make sure it goes away anyway.
        } catch (RuntimeException e){
            d.dispose();  //make it go away anyway
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.seecode.display.ISeeCodeTextViewerCallback#createDisplayMenuFor(com.arc.seecode.display.ISeeCodeTextViewer)
     */
    @Override
    public MenuDescriptor createDisplayMenuFor(final ISeeCodeTextViewer d) {
        try {
            String selectors[] = mEngine.getDisplaySelectors();
            DisplayMenuGenerator gen = new DisplayMenuGenerator(selectors);
            MenuDescriptor menu = new MenuDescriptor();
            gen.generate(new DisplayMenuGenerator.ICallback(){
                @Override
                public Object createSubmenu(String name, Object parent){
                    MenuDescriptor submenu = new MenuDescriptor();
                    MenuDescriptor parentMenu = (MenuDescriptor)parent;
                    parentMenu.addSubMenu(name,name,submenu);
                    return submenu;
                }
                @Override
                public void createMenuItem(final String kind, String label, Object parent){
                    ((MenuDescriptor)parent).addMenuItem(kind,label,
                            new MenuDescriptor.IActionObserver(){
                        @Override
                        public void actionPerformed(String name){
                                mDisplayCreator.createDisplay(kind,d);
                            }
                        });
                }
                @Override
                public void error(String message,Exception e){
                    internalError(d,message,e);
                }
                },menu);
            return menu;

        } catch (EngineException e) {
            internalError(d,e.getMessage(),e);
            return null;
        }
    }
    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewerCallback#internalError(com.arc.seecode.display.ISeeCodeTextViewer, java.lang.String, java.lang.Throwable)
     */
    @Override
    public void internalError(ISeeCodeTextViewer viewer, String message, Throwable t) {
        mCustomDisplayCallback.internalError(viewer,message,t);
        
    }
    /* (non-Javadoc)
     * @see com.arc.seecode.display.ISeeCodeTextViewerCallback#notifyError(com.arc.seecode.display.ISeeCodeTextViewer, java.lang.String, java.lang.String)
     */
    @Override
    public void notifyError(ISeeCodeTextViewer viewer, String message, String title) {
        mCustomDisplayCallback.notifyError(viewer,message,title);
        
    }

    @Override
    public boolean copyAllToClipboard (ISeeCodeTextViewer d) {
        try {
            if (mEngine.canEngineHandleCopyToClipboard()) {
                mEngine.copyAllToClipboard(d.getDisplayID());
                return true;
            }
        }
        catch (EngineException e) {
            internalError(d, e.getMessage(), e);
        }
        return false;
    }
 

}
