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
package com.metaware.guihili.builder.legacy;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.xml.sax.SAXException;

import com.arc.widgets.IButton;
import com.arc.widgets.IComponent;
import com.arc.widgets.IContainer;
import com.arc.xml.Element;
import com.metaware.guihili.Gui;
import com.metaware.guihili.ITextWrapper;
import com.metaware.guihili.builder.ContainerBuilder;

/**
 * Handle radiobutton_group node.
 * 
 * <pre>
 * 
 *  
 *   &lt;radiobutton_group property=&quot;...&quot; default=&quot;...&quot; arg_action=&quot;...&quot;&gt;
 *       ...radiobutton definitions...
 *   &lt;/radiobutton_group&gt;
 *   
 *  
 * </pre>
 * 
 * The property value is set to the <i>name </i> of the button that selected. We
 * do this by an action listener on each button.
 * <P>
 * We also have a property-change listener so that we can alter the selected
 * buttons programatically when the property value is changed by some other
 * means.
 */
public class RadioButtonGroupBuilder extends ContainerBuilder {
    public RadioButtonGroupBuilder(Gui gui) {
        super(gui);
    }

    @Override
    public void startNewInstance(Element element) throws SAXException {
        mButtons = new HashMap<String,IButton>();
        super.startNewInstance(element);
    }

    public void setProperty(String name) {
        mProperty = name;
    }

    public void setDefault(String name) {
        mDefault = name;
    }

    @Override
    public void addChild(Object child, Element element) throws SAXException {
        //
        // If a radiobutton (which is should always be), then
        // add to group
        if (child instanceof IButton) {
            final IButton c = (IButton) child;
            if (c.getName() != null) {
                if (c.getName().equals(mDefault))
                    c.setSelected(true);
                mButtons.put(c.getName(), c);
                if (mProperty != null) {
                    c.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent event) {
                            if (c.isSelected())
                                try {
                                    _gui.setProperty(mProperty, c.getName());
                                } catch (PropertyVetoException x) {
                                }
                        }
                    });
                    if (c.isSelected())
                        try {
                            _gui.setProperty(mProperty, c.getName());
                        } catch (PropertyVetoException e) {
                        }
                }
            }
        }
        super.addChild(child, element);
    }

    @Override
    public Object returnObject() throws SAXException {
        if (mProperty != null) {
            // If the property changes, make sure the corresponding button is
            // enabled.
            _gui.addPropertyChangeListener(mProperty,
                    new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent event) {
                            String oldValue = (String) event.getOldValue();
                            if (oldValue != null) {
                                IButton b = mButtons.get(oldValue);
                                if (b != null)
                                    b.setSelected(false);
                            }
                            IButton b =  mButtons.get(_gui.getProperty(mProperty));
                            if (b != null)
                                b.setSelected(true);
                        }
                    });
            //
            // Do initial setting.
            //
            _gui.addPropertyChangeListener(Gui.READING_XML,
                    new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent event) {
                            if (event.getNewValue().equals("false")) {
                                Object v = _gui.getProperty(mProperty);
                                if (v != null) {
                                    IButton b =  mButtons.get(v);
                                    if (b != null)
                                        b.setSelected(true);
                                }
                            }
                        }
                    });
            if (mDefault != null)
                try {
                    _gui.setProperty(mProperty, mDefault);
                } catch (PropertyVetoException e) {
                }
        }
        return super.returnObject();
    }
    
    @Override
    protected ITextWrapper getActionValueWrapper(final IComponent c){
        return new ITextWrapper() {
            @Override
            public String getText() {
                return c.isEnabled() ? (String) _gui
                        .getProperty(mProperty) : null;
            }

            @Override
            public void setText(String s) {
            }
        };
    }

    private String mProperty;

    private String mDefault;

    private HashMap<String,IButton> mButtons;

    // when button is selected; changes property value
    /**
     * @see com.arc.xml.AbstractBuilder#beginChildren(Element)
     */
    @Override
    protected void beginChildren(Element element) throws SAXException {
        super.beginChildren(element);
        sRadioButtonGroupSet.add(_gui.getParent());
    }

    /**
     * Return true if given container is a radiobutton group. We need to know
     * this so that we can change the old legacy checkbox to a radiobutton when
     * appropriate.
     */
    public static boolean isRadioButtonGroup(IContainer container) {
        return sRadioButtonGroupSet.contains(container);
    }

    private static Set<IContainer> sRadioButtonGroupSet = new HashSet<IContainer>();
}
