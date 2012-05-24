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


import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import com.metaware.guihili.Gui;
import com.metaware.guihili.builder.Builder;


/**
 * The Guihili "card_trigger_button" Attributes are:
 * <ul>
 * <li>card -- name of tabbed panel to display
 * <li>target -- name of target JTabbedPanel
 * <li>label -- to appear on tab
 * </ul>
 * All it does it place the component at "card" into the tabbed pane at "target". But it is a forward reference!
 * Therefore, we save it in a GUI property that is derived from the target name and access it when we see the "cards"
 * element.
 */
public class CardTriggerButtonBuilder extends Builder {

    public CardTriggerButtonBuilder(Gui gui) {
        super(gui);
    }

    public void setLabel (String label) {
        mLabel = label;
    }

    public void setTarget (String name) {
        mTarget = name;
    }

    public void setCard (String name) {
        mCard = name;
    }

    public void setDoc (String name) {
        mDoc = name;
    }

    public void setId (String name) {
    }

    public void setTip (String name) {
        mTip = name;
    }

    String getCard () {
        return mCard;
    }

    String getTip () {
        return mTip;
    }

    String getLabel () {
        return mLabel;
    }

    String getDoc () {
        return mDoc;
    }

    /**
     * Property name in which we store the list of cards associated with the given target name.
     */
    static String propName (String targetName) {
        return "CARDS:" + targetName;
    }

    /**
     * We have a tab panel name ("card") and a tabbed pane("target") and a table title ("label")
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object returnObject () throws SAXException {
        List<CardTriggerButtonBuilder> tabs = (List<CardTriggerButtonBuilder>) _gui.getGlobalEnvironment().getSymbolValue(propName(mTarget));
        if (tabs == null) {
            tabs = new ArrayList<CardTriggerButtonBuilder>();
            _gui.getGlobalEnvironment().putSymbolValue(propName(mTarget), tabs);
        }
        tabs.add(this);
        return null;
    }

    private String mLabel;

    private String mTarget;

    private String mCard;

    private String mTip;

    private String mDoc;
}
