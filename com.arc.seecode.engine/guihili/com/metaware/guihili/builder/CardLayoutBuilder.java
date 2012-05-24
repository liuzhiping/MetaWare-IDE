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

import java.awt.CardLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;

import org.xml.sax.SAXException;

import com.arc.widgets.ICardContainer;
import com.arc.widgets.IComponent;
import com.arc.widgets.IContainer;
import com.arc.widgets.IScrollPane;
import com.arc.xml.Element;
import com.arc.xml.IBinding;
import com.metaware.guihili.Gui;

/**
 * Construct a card layout Attributes are:
 * <dl>
 * <dt> property
 * <dd> name of property that controls which card is showing. Its value is the
 * name of a card.
 * <dt> vgap
 * <dd> number of pixels of space at top and bottom of card.
 * <dt> ygap
 * <dd> number of pixels of space at left and right of card.
 * </dl>
 * The only valid children are "card" nodes. See {@link CardLayout}.
 * <P>
 * 
 * @author David Pickens
 */
public class CardLayoutBuilder extends ContainerBuilder {
    public CardLayoutBuilder(Gui gui) {
        super(gui);
    }

    @Override
    public void startNewInstance(Element element) {
    }

    @Override
    protected IContainer makeContainer() {
        mCards = _gui.getComponentFactory().makeCardContainer(_gui.getParent());
        setComponent(mCards);
        return mCards;
    }

    @Override
    protected void addChild(Object child, Element element) throws SAXException {
        IComponent c = (IComponent) child;
        // We contrive each "card" to be in its own ScrollPane. So, reference
        // the parent container.
        if (c.getParent() instanceof IScrollPane){
            child = c = c.getParent();
        }
        String name = element.attributeValue("name");
        // If property has no value, then assign one.
        if (mProperty != null && _gui.getProperty(mProperty) == null) {
            try {
                _gui.setProperty(mProperty, name);
            } catch (PropertyVetoException x) {
            }
        }
        super.addChild(child, element);
        mCards.setCardName(c.getComponent(), name);
    }

    public void setProperty(String name) {
        mProperty = name;
    }

    public void setVgap(int gap) {
        mVGap = gap;
    }

    public void setHgap(int gap) {
        mHGap = gap;
    }

    @Override
    public Object returnObject() throws SAXException {
        mCards.setMargins(mVGap, mHGap, mVGap, mHGap);
        super.returnObject(); // sets things
        if (mProperty != null) {
            _gui.addPropertyChangeListener(mProperty,
                    new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent event) {
                            mCards.showCard((String) event.getNewValue());
                        }

                    });
            if (_gui.getProperty(mProperty) != null)
                mCards.showCard((String) _gui.getProperty(mProperty));
        }
        return mCards;
    }
    
    /**
     * Override so that we can put each "card" in a scroller.
     */
    @Override
    protected Object doChild (Element child, IBinding parentBinding) throws SAXException {
        IContainer parent = _gui.getParent();
        IScrollPane scroller = _gui.getComponentFactory().makeScrollPane(parent,false);
        _gui.setParent(scroller);
        try {
            super.doChild(child, parentBinding);
            return scroller;
        } finally{
            _gui.setParent(parent);
        }
    }
    
    public void setScrollable(boolean v) { }

    private String mProperty;

    private ICardContainer mCards;

    private int mVGap;
    //private boolean mScrollable = true;
    private int mHGap;
}
