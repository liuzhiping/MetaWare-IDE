/*
 * LabelContribution
 * $Revision$
 * $Date$
 *
 * CONFIDENTIAL AND PROPRIETARY INFORMATION.
 * Copyright 2007 ARC International (Unpublished).
 * All Rights Reserved.
 * This document, material and/or software contains confidential and
 * proprietary information of ARC International and is protected by copyright,
 * trade secret and other state, federal, and international laws, and may be
 * embodied in patents issued or pending. Its receipt or possession does not
 * convey any rights to use, reproduce, disclose its contents, or to
 * manufacture, or sell anything it may describe.  Reverse engineering is
 * prohibited, and reproduction, disclosure or use without specific written
 * authorization of ARC International is strictly forbidden.  ARC and the ARC
 * logotype are trademarks of ARC International.
 */
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
