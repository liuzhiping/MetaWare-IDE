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
import com.arc.widgets.ILayoutManager;


class ProfilingColumnLayout implements ILayoutManager {
    static final int COLUMN_WIDTH = 7;  // in characters
    
    static final int MAX_COLUMNS = 32;
    
    private int columnWidth[] = new int[MAX_COLUMNS];
    
    void setColumnWidth(int column,int width){
        columnWidth[column] = width;
    }

    @Override
    public Dimension computeSize (IContainer c, int wHint, int hHint) {
        IComponent kids[] = c.getChildren();
        int width = 0;
        int height = 0;
        int headerHeight = 0;
        for (int i = 0; i < kids.length; i++) {
            String layoutData = (String) kids[i].getLayoutData();
            if (layoutData != null) {
                switch (layoutData.charAt(0)) {
                    case 'C': {
                        int column = Integer.parseInt(layoutData.substring(1));
                        width += columnWidth[column];
                        break;
                    }
                    case 'P':{
                        Dimension size = kids[i].computeSize(IComponent.DEFAULT,
                                IComponent.DEFAULT);
                        height += size.height;
                        width += size.width;
                        break;
                    }
                }

            }
        }
        return new Dimension(width,height+headerHeight);
    }

    @Override
    public void layout (IContainer c) {
        IComponent kids[] = c.getChildren();
        IComponent columns[] = new IComponent[MAX_COLUMNS];
        IComponent primaryCanvas = null;
        int headerHeight = 0;
        for (int i = 0; i < kids.length; i++) {
            String layoutData = (String) kids[i].getLayoutData();
            if (layoutData != null) {
                switch (layoutData.charAt(0)) {
                    case 'C': {
                        int column = Integer.parseInt(layoutData.substring(1));
                        columns[column] = kids[i];
                        break;
                    }
                    case 'P':{
                        primaryCanvas = kids[i];
                        break;
                    }
                }

            }
        }
        Dimension size = c.getSize();
        int x=0;
        for (int i = 0; i < columns.length; i++){
            if (columns[i] != null){
                int y = 0;
                int width = columnWidth[i];
                y += headerHeight;              
                if (columns[i] != null){
                    columns[i].setBounds(x,y,width,size.height-y);
                }
                x += width;
            }           
        }
        if (primaryCanvas != null){
            primaryCanvas.setBounds(x,headerHeight,size.width-x,size.height-headerHeight);
        }

    }
    
    void setColumnCanvas(IComponent c, int column) {
        c.setLayoutData("C" + column);
    }

    void setPrimaryCanvas(IComponent c) {
        c.setLayoutData("P");
    }

}
