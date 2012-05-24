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

import java.util.List;

import org.xml.sax.SAXException;

import com.arc.mw.util.Cast;
import com.arc.xml.Element;
import com.metaware.guihili.Gui;

/**
 * Process a "checkbox_group" node.
 * <P>
 * 
 * <pre>
 * 
 *  &lt;checkbox_group property=&quot;...&quot; default=&quot;...&quot; arg_action=&quot;...&quot;&gt;
 *      ...radiobutton definitions...
 *  &lt;/checkbox_group&gt;
 *  
 * </pre>
 * 
 * <b>Note: </b> This is tag that is supported only for legacy code. We convert
 * all embedded checkboxes to radiobuttons.
 * <P>
 * 
 * The property value is set to the <i>name </i> of the button that selected. We
 * do this by an action listener on each button.
 * <P>
 * We also have a property-change listener so that we can alter the selected
 * buttons programatically when the property value is changed by some other
 * means.
 */
public class CheckBoxGroupBuilder extends RadioButtonGroupBuilder {

    public CheckBoxGroupBuilder(Gui gui) {
        super(gui);
    }

    /**
     * We override this to correct the names of the kids. We convert checkboxes
     * into radiobuttons
     */
    @Override
    public void startNewInstance(Element element) throws SAXException {
        List<Element> kids = Cast.toType(element.elements());
        for (Element kid: kids){
            String tag = kid.getName().toLowerCase();
            if (tag.equals("checkbox"))
                kid.setName("radiobutton");
            else if (tag.equals("enable_trigger_checkbox"))
                    kid.setName("enable_trigger_radiobutton");
        }
        super.startNewInstance(element);
    }

}
