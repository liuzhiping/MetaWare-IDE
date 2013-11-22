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
package org.eclipse.cdt.debug.internal.ui.views.registers;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;


/**
 * Creates label in the toolbar to prefix a combo box.
 * @author David Pickens, ARC International
 */
class LabelContribution extends ControlContribution {
    private String fText;

    private String fToolTip;

    protected LabelContribution(String id, String text, String tooltip) {
        super(id);
        fText = text;
        fToolTip = tooltip;
    }

    @Override
    protected Control createControl (Composite parent) {
        Label label = new Label(parent, SWT.LEFT);
        label.setText(fText);
        label.setEnabled(isEnabled());
        label.setToolTipText(fToolTip);
        return label;
    }
}
