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
package com.arc.widgets.internal.swt;

import java.awt.Rectangle;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.arc.widgets.IComponent;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;


/**
 * @author David Pickens
 */
abstract class AbstractContainer extends Component implements IContainer {

    /**
     * @param parent
     * @param mapper
     */
    AbstractContainer(IContainer parent, IComponentMapper mapper) {
        super(parent, mapper);
        //Containers default to FILL
        setHorizontalAlignment(FILL);
        // unless vertical column...
        if (parent != null && parent.getContainerStyle() != IComponentFactory.COLUMN_STYLE)
            setVerticalAlignment(FILL);
    }

    /*override*/
    @Override
    public IComponent[] getChildren() {
        Control c[] = ((Composite)getComponent()).getChildren();
        ArrayList<IComponent> list = new ArrayList<IComponent>(c.length);
        IComponentMapper mapper = getComponentMapper();
        for (int i = 0; i < c.length; i++){
            IComponent ic = mapper.findWrapperFor(c[i]);
            if (ic != null) list.add(ic);            
        }
        return list.toArray(new IComponent[list.size()]);
    }
    
    @Override
    public void layout(){
        Composite c = (Composite)getComponent();
        c.layout();
    }
    
    public Composite getComposite(){
        return (Composite)getComponent();
    }

    @Override
    public Rectangle getClientArea () {
        Composite c = (Composite)getComponent();
        org.eclipse.swt.graphics.Rectangle r = c.getClientArea();
        return new Rectangle(r.x,r.y,r.width,r.height);
    }


}
