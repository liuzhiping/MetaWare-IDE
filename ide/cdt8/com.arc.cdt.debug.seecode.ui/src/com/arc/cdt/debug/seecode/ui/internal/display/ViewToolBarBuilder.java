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
package com.arc.cdt.debug.seecode.ui.internal.display;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;

import com.arc.cdt.debug.seecode.ui.UISeeCodePlugin;
import com.arc.cdt.debug.seecode.ui.display.IViewToolBarBuilder;
import com.arc.widgets.IChoice;
import com.arc.widgets.IImageWidget;
import com.arc.widgets.ILabel;
import com.arc.widgets.IToolItem;

public class ViewToolBarBuilder implements IViewToolBarBuilder {

    private List<IToolBarItem> mItems = new ArrayList<IToolBarItem>();
    
    static boolean isWindows(){
        return "win32".equals(Platform.getWS());
    }
    
    @Override
    public void populateView(final IViewPart viewPart) {
        IViewSite vs = viewPart.getViewSite();
        final IToolBarManager tm = vs.getActionBars().getToolBarManager();

        tm.removeAll();
        for (IToolBarItem it : mItems) {
            it.addToToolBar(tm);
        }
        //<WIN32 BUG>: if the toolbar doesn't have any buttons (e.g., only
        // comboboxes), Windows version of SWT (as of 3.1M5) doesn't 
        // compute the height correctly.
        // For such cases, we must add a dummy button with an invisible 1-pixel
        // wide icon. It won't be seen in the toolbar unless the mouse happens
        // to go over it. If anyone has a better solution, please make appropriate
        // corrections. This problem has already cost me a day! (D.P.)
        if (isWindows()){
            boolean hasButtons = false;
            for (IToolBarItem it: mItems){
                if (it instanceof DeferredButton){
                    hasButtons = true;
                    break;
                }
            }
            if (!hasButtons){
                DeferredButton item = new DeferredButton(IAction.AS_PUSH_BUTTON);
                item.addToToolBar(tm);  
                final String DUMMY_ICON = "IconToFixWindowsSWTBug";
                    
                ImageRegistry imageRegistry = UISeeCodePlugin.getDefault().getImageRegistry();
                ImageDescriptor id = imageRegistry.getDescriptor(DUMMY_ICON);
                if (id == null){
                    final ImageData data = new ImageData(1,15,1,new PaletteData(new RGB[]{new RGB(0,0,0),new RGB(255,255,255)}));
                    data.transparentPixel = 0;
                    id = new ImageDescriptor(){

                        @Override
                        public ImageData getImageData() {
                            return data;
                        }};
                     imageRegistry.put(DUMMY_ICON,id);
                }
                item.setImage(id);
            }
        }
        //</WIN32 BUG>
        
        
        tm.update(true);
    }
    
    private void add(IToolBarItem item){
        mItems.add(item);
    }

    @Override
    public IChoice makeComboBox(String labelText, boolean editable) {
        ILabel label = null;
        if (labelText != null && labelText.length() > 0) {
            DeferredLabel dl = new DeferredLabel(labelText);
            add(dl);
            label = dl;
        }
        DeferredCombo c = new DeferredCombo(label,editable);
        add(c);
        return c;
    }

    @Override
    public IToolItem makeButton() {
        DeferredButton b = new DeferredButton(IAction.AS_PUSH_BUTTON);
        add(b);
        return b;
    }

    @Override
    public ILabel makeLabel(String label) {
        
        DeferredLabel deferredLabel = new DeferredLabel(label);
        add(deferredLabel);
        return deferredLabel;
    }

    @Override
    public IToolItem makeToggleButton() {
        DeferredButton b = new DeferredButton(IAction.AS_CHECK_BOX);
        add(b);
        return b;
    }

    @Override
    public void makeSeparator() {
        add(new DeferredSeparator());

    }

    @Override
    public void makeRowSeparator() {
        // not necessary; toolbar already folds, well, at least under Windows

    }

    @Override
    public IImageWidget makeImage (int width, int height, int depth) {
        DeferredImage deferredImage = new DeferredImage(width,height,depth);
        add(deferredImage);
        return deferredImage;
    }

}
