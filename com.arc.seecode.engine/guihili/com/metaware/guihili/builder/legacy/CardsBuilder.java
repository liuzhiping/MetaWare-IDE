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
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import com.arc.widgets.IButton;
import com.arc.widgets.ICardContainer;
import com.arc.widgets.IComponent;
import com.arc.widgets.IComponentFactory;
import com.arc.widgets.IContainer;
import com.arc.widgets.ITabItem;
import com.arc.widgets.ITabbedPane;
import com.arc.xml.Element;
import com.metaware.guihili.Gui;
import com.metaware.guihili.builder.ComponentBuilder;

/**
 * The Old Guihili "cards" node.
 *
 * This is a poorly defined construct. If there are "card_trigger_button"s 
 * embedded, then it is a tabbed panel. Otherwise, its a panel using a
 * card layout and each child is a card.
 * 
 * In the latter case, the cards are selected by "enable_trigger_button"s
 * that are located at arbitrary positions!
 *
 * We have much better constructs than these: tabbedPane, and cardLayout. Use
 * these instead.
 * <p>
 * For the tabbed panel case, we take the name of this panel. 
 * We then find all  "card_trigger_button" instances that have this panel as 
 * its "target".  We then find the "card" and make it a tab.
 * <P>
 * Otherwise, we create a JPanel with a CardLayout and add each child as
 * a card. Each child must have a name. As the "enable_trigger_button"
 * select this card, we show it.
 */
public class CardsBuilder extends ComponentBuilder {
    public CardsBuilder(Gui gui) {
        super(gui);
    }

    @Override
    public void startNewInstance(Element e) {
        mElement = e; // for error diagnostics later on
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void beginChildren(Element element) throws SAXException {
        String name = getName();
        if (name == null)
            error(element, "cards needs an id specification!");
        //
        // HACK CITY:
        // If there are "card_trigger_buttons" then we assume this to be a tabbed
        // pane that is constructed in a very convoluted manner from old Guihili.
        // If there are none, then we look for "enable_trigger_button" that
        // references this card. In such a case, we have a "card layout" -- not
        // a tabbed pane. Oh, what a kloodge.
        //
        // The preferred approach it to use either "tabbedPane" or "cardLayout"
        //
        List<Object> tabs =
            (List<Object>) _gui.getEnvironment().getSymbolValue(
                CardTriggerButtonBuilder.propName(name));
        IContainer c;
        if (tabs != null) {
            c =
                _gui.getComponentFactory().makeTabbedPane(
                    _gui.getParent(),
                    IComponentFactory.TABS_ON_TOP);
            //mIsTabPane = true;
        }
        else
            c = _gui.getComponentFactory().makeCardContainer(_gui.getParent());
        setComponent(c);
        mSaveParent = _gui.getParent();
        _gui.setParent(c);
	super.beginChildren(element);
    }

    @Override
    protected void cleanup() {
        _gui.setParent(mSaveParent);
    }

    @Override
    protected void addChild(Object object, Element element)
        throws SAXException {
        if (object instanceof IComponent) {
            IComponent c = (IComponent) object;
            if (c.getName() == null)
                error(element, "Child of cards node must be named");
            else
                mChildren.add(c);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object returnObject() throws SAXException {
        String name = getName();
        if (name == null) {
            error(mElement, "cards needs an id specification!");
            return null;
        }
        //
        // HACK CITY:
        // If there are "card_trigger_buttons" then we assume this to be a tabbed
        // pane that is constructed in a very convoluted manner from old Guihili.
        // If there are none, then we look for "enable_trigger_button" that
        // references this card. In such a case, we have a "card layout" -- not
        // a tabbed pane. Oh, what a kloodge.
        //
        // The preferred approach it to use either "tabbedPane" or "cardLayout"
        //
        List<ITabbedPane> tabs =
            (List<ITabbedPane>) _gui.getEnvironment().getSymbolValue(
                CardTriggerButtonBuilder.propName(name));
        if (tabs != null) {
            ITabbedPane pane = (ITabbedPane) getComponent();
            for (int i = 0; i < tabs.size(); i++) {
                CardTriggerButtonBuilder ct =
                    (CardTriggerButtonBuilder) tabs.get(i);
                IComponent card = _gui.getComponent(ct.getCard());
                if (card == null)
                    error(
                        mElement,
                        "Can't find card \""
                            + ct.getCard()
                            + "\" in \""
                            + name
                            + '"');
                ITabItem tab = pane.addTab(card, ct.getLabel());
                if (ct.getTip() != null) {
                    tab.setToolTipText(ct.getTip());
                }
            }
        }
        else {
            final ICardContainer cards = (ICardContainer) getComponent();
            // Arrange to display cards in response to their respective
            // enable_trigger_button selection.
            final int cnt = mChildren.size();
            if (cnt == 0)
                error(mElement, "No named children for cards node");
            for (int i = 0; i < cnt; i++) {
                IComponent c = mChildren.get(i);
                cards.setCardName(c.getComponent(), c.getName());
            }

            _gui.invokeAfterReading(new Runnable() {
                @Override
                public void run() {
                    int count = mChildren.size();
                    try {
                        for (int i = 0; i < count; i++) {
                            final IComponent c = mChildren.get(i);
                            List<IButton> triggers = _gui.getEnableTriggers(c);
                            if (triggers == null)
                                error(
                                    mElement,
                                    "No enable_trigger_button for card "
                                        + c.getName());
                            else {
                                for (int j = 0; j < triggers.size(); j++) {
                                    final IButton trigger = triggers.get(j);
                                    trigger
                                        .addActionListener(
                                            new ActionListener() {
                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            if (trigger.isSelected())
                                                cards.showCard(c.getName());
                                        }
                                    });
                                    if (trigger.isSelected())
                                    	cards.showCard(c.getName());
                                }
                            }
                        }
                    }
                    catch (Exception x) {
                        _gui.handleException(x);
                    }
                }
            }, true);
        }
        return super.returnObject();
    }
    private Element mElement;
    private List<IComponent> mChildren = new ArrayList<IComponent>();
    private IContainer mSaveParent;
}
