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

import org.xml.sax.SAXException;

import com.arc.xml.Element;
import com.arc.xml.IBinding;
import com.arc.xml.IBuilder;
import com.metaware.guihili.Gui;
import com.metaware.guihili.builder.Builder;

/**
 * The Guihili "gui_proc".
 * 
 * <pre>
 * 
 *  &lt;gui_proc name=&quot;name&quot;&gt;
 *       &lt;component...&gt; 
 *  &lt;gui_proc&gt;
 *  
 * </pre>
 * 
 * We merely save the Element represending the body by calling
 * {@link Gui#addGuiProc(String,Element) Gui.addGuiProc(name,body)}.
 */
public class GuiProcBuilder extends Builder {
    public GuiProcBuilder(Gui gui) {
        super(gui);
    }

    public void setName(String name) {
        mName = name;
    }

    @Override
    public Object build(Element element, IBinding binding, IBuilder parent)
            throws SAXException {
        doAttributes(element, binding);
        if (mName != null)
            _gui.addGuiProc(mName, element);
        cleanup();
        return null;
    }

    @Override
    public Object returnObject() throws SAXException {
        return null;
    }

    private String mName;
}
