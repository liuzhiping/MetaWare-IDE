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
package com.arc.seecode.internal.display;

import java.awt.Dimension;

import com.arc.widgets.IComponent;
import com.arc.widgets.IContainer;
import com.arc.widgets.ILabel;
import com.arc.widgets.ILayoutManager;

/**
 * Layout manager for SeeCode textview
 * 
 * @author David Pickens
 */
class ViewLayout implements ILayoutManager {

    static final int COLUMN_WIDTH = ProfilingColumnLayout.COLUMN_WIDTH;  // in characters
    
    static final int MAX_COLUMNS = ProfilingColumnLayout.MAX_COLUMNS;
    
    private ProfilingColumnLayout pLayout;
    
    // We pass the profiling layout so that we can set its column widths.
    ViewLayout(ProfilingColumnLayout pLayout){
        this.pLayout = pLayout;
    }

    void setHeaderLabel(IComponent c, int column) {
        c.setLayoutData("H" + column);
    }

    void setScroller(IComponent c) {
        c.setLayoutData("P");
    }
    
    void setPrimaryHeader(IComponent c){
        c.setLayoutData("I");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.ILayoutManager#computeSize(com.arc.widgets.IContainer,
     *      int, int)
     */
    @Override
    public Dimension computeSize(IContainer c, int wHint, int hHint) {
        IComponent kids[] = c.getChildren();
        int width = 0;
        int height = 0;
        int headerHeight = 0;
        int columnWidth = computeColumnWidth(c);
        IComponent scroller = null;
        for (int i = 0; i < kids.length; i++) {
            String layoutData = (String) kids[i].getLayoutData();
            if (layoutData != null) {
                switch (layoutData.charAt(0)) {
                    case 'I': {// main header
                        Dimension s = kids[i].computeSize(IComponent.DEFAULT,IComponent.DEFAULT);
                        headerHeight = Math.max(headerHeight,s.height);
                        break;
                    }
                    case 'H': {
                        Dimension s = kids[i].computeSize(IComponent.DEFAULT,IComponent.DEFAULT);
                        headerHeight = Math.max(headerHeight,s.height);                 
                        if (s.width > columnWidth){
                            //Shorten the header label
                            if (kids[i] instanceof ILabel){
                                ILabel lab = (ILabel)kids[i];
                                String text = lab.getText();
                                String truncatedText = text.substring(0,text.length()-2);
                                while (true) {
                                    lab.setText(truncatedText + "...");
                                    s = lab.computeSize(IComponent.DEFAULT,IComponent.DEFAULT);
                                    if (s.width > columnWidth){
                                        truncatedText = truncatedText.substring(0,truncatedText.length()-1);
                                    }
                                    else break;
                                }
                            }
                        }
                        int w = Math.max(columnWidth,s.width);
                        int column = Integer.parseInt(layoutData.substring(1));
                        pLayout.setColumnWidth(column, w);
                        width += w;
                        break;
                    }
                    case 'P':{
                        scroller = kids[i];                    
                        break;
                    }
                }

            }
        }
        if (scroller != null){
            Dimension size = scroller.computeSize(IComponent.DEFAULT,
                IComponent.DEFAULT);
            height += size.height;
            width += size.width;
        }
        return new Dimension(width,height+headerHeight);
    }

    private static int computeColumnWidth (IComponent c) {
        return c.getFont().getPixelWidth("XXXXXXXXXX".substring(0,COLUMN_WIDTH),c);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.widgets.ILayoutManager#layout(com.arc.widgets.IContainer)
     */
    @Override
    public void layout(IContainer c) {
        IComponent kids[] = c.getChildren();
        IComponent headers[] = new IComponent[MAX_COLUMNS];
        IComponent columns[] = new IComponent[MAX_COLUMNS];
        IComponent scroller = null;
        IComponent primaryHeader = null;
        int headerHeight = 0;
        int columnWidths[] = new int[MAX_COLUMNS];
        for (int i = 0; i < kids.length; i++) {
            String layoutData = (String) kids[i].getLayoutData();
            if (layoutData != null) {
                switch (layoutData.charAt(0)) {
                    case 'I': { // main header
                        primaryHeader = kids[i];
                        break;
                    }
                    case 'H': {
                        int column = Integer.parseInt(layoutData.substring(1));
                        headers[column] = kids[i];
                        int width = computeColumnWidth(c);
                        Dimension hsize = kids[i].computeSize(IComponent.DEFAULT,IComponent.DEFAULT);
                        headerHeight = Math.max(hsize.height,headerHeight);
                        columnWidths[column] = Math.max(width,hsize.width);
                        break;
                    }
                    case 'P':{
                        scroller = kids[i];
                        break;
                    }
                }

            }
        }
        Dimension size = c.getSize();
        int x=0;
        int columnPixelWidth = computeColumnWidth(c);
        for (int i = 0; i < columns.length; i++){
            if (headers[i] != null){
                int y = 0;
                int width = Math.max(columnPixelWidth,columnWidths[i]);
                pLayout.setColumnWidth(i, width);
                // We add "+1" to the headers so that their boundaries line up with the vertical line
                // that separates each column in the scroller.
                headers[i].setBounds(x+1,y,width,headerHeight);
                //System.out.println(">>>" + ((ILabel)headers[i]).getText() + " " + (x+1) + "," + y + "," + width + "," + headerHeight);
                x += width;
            }           
        }
        if (primaryHeader != null){
            primaryHeader.setBounds(x,0,size.width-x,headerHeight);
        }
        if (scroller != null){
            scroller.setBounds(0,headerHeight,size.width,size.height-headerHeight);
        }
    }
}
