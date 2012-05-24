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
package com.arc.widgets.internal.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;

import com.arc.widgets.IComponent;
import com.arc.widgets.IContainer;


/**
 * @author David Pickens
 */
abstract class AbstractContainer extends SwingComponent implements IContainer {

    AbstractContainer(IContainer parent, IComponentMapper mapper){
        super(parent,mapper);
    }

    /*override*/
    @Override
    public IComponent[] getChildren() {
        Container c = (Container)getComponent();
        Component kids[] = c.getComponents();
        IComponentMapper mapper = getComponentMapper();
        ArrayList<IComponent> list = new ArrayList<IComponent>(kids.length);
        for (int i = 0; i < kids.length; i++){
            IComponent ic = mapper.findWrapperFor(kids[i]);
            if (ic != null) list.add(ic);
        }
        return list.toArray(new IComponent[list.size()]);
    }

    @Override
    public void layout() {
        Container c = (Container)getComponent();
        c.doLayout();
        
    }
    
    @Override
    public Rectangle getClientArea(){
        Container c = (Container)getComponent();
        Rectangle size = c.getBounds();
        Insets insets = c.getInsets();
        size.x = insets.left;
        size.y = insets.top;
        size.width -= insets.left + insets.right;
        size.height -= insets.top + insets.bottom;
        return size;
    }

}
