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
package com.metaware.guihili.builder;

import org.xml.sax.SAXException;

import com.arc.widgets.IComponent;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;
import com.arc.widgets.ILabel;
import com.arc.xml.Element;
import com.metaware.guihili.Gui;

/**
 * Base class for those components that have a label prefix.
 * This is actually part of legacy guihili in which textfields and comboboxes
 * can be prefixed with a label.
 * <P>
 * Note that the component can't be constructed until {@link #beginChildren(Element)}.
 * <P>
 * @author David Pickens
 */
public abstract class LabeledComponentBuilder extends ComponentBuilder {

    public LabeledComponentBuilder(Gui gui) {
        super(gui);
    }

    public void setLabel(String label) {
        mLabel = label;
    }
    /**
     * Construct the component for this builder.
     * If a label is given, it will be appropriately prefixed.
     */
    abstract protected IComponent makeComponent();

    /**
     * @see com.arc.xml.AbstractBuilder#beginChildren(Element)
     */
    @Override
    protected void beginChildren(Element element) throws SAXException {
        super.beginChildren(element);
        IContainer panel = null;
        if (mLabel != null) {
            panel =
                _gui.getComponentFactory().makeContainer(
                    _gui.getParent(),
                    IComponentFactory.ROW_STYLE);
            panel.setMargins(0, 0, 0, 0);
            if (isExpandable()) {
                panel.setHorizontalAlignment(IComponent.FILL);
                panel.setHorizontalWeight(1.0);
            }
            ILabel label = _gui.getComponentFactory().makeLabel(panel, mLabel);
            label.getComponent();
            IContainer saveParent = _gui.getParent();
            _gui.setParent(panel);
            IComponent ic = makeComponent();
            if (isExpandable()){
                ic.setHorizontalAlignment(IComponent.FILL);
                ic.setHorizontalWeight(1.0);
            }
            _gui.setParent(saveParent);
            setComponent(panel);
            if (!mFinished)
                finishComponent(panel);
        } else {
            IComponent ic = makeComponent();
            setComponent(ic);
            if (!mFinished)
                finishComponent(ic);
        }
        String name = getName();
        if (name != null){
            getActiveComponent().setName(name); // for gui tester
            _gui.setComponent(name,getActiveComponent());
        }
    }
    
    protected abstract IComponent getActiveComponent();

    @Override
    protected void setComponentName (IComponent component) {
        // Don't assign name to container. We want to assign name to active component within
        // the container. See beginChildren above.
    }
    
    private String mLabel;

}
