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

import com.metaware.guihili.Gui;

/**
 * Construct an editable combobox with a button on the right
 * for clearing it.
 * Attributes are:
 * <dl>
 * <dt>property : name
 * <dd>name of property to be updated by the combo box.
 * <dt>columns: int
 * <dd> width of the combobox
 * <dt>expandible: boolean
 * <dd> boolean to determine if this component stretches in its layout
 *  <dt>list: list
 *  <dd> list of initial values
 *  <dt>label: string
 *  <dd> Label to preceed the combobox
 *  <dt>arg_action: expression
 *   <dd> value to be passed to "setarg" Lisp function when selected.
 *  <dt>default: string
 *  <dd> initial setting.
 * </dl>
 * 
 * For hysterical reasons, the VALUE symbol is set in the environment
 * to the value of the combobox.
 */
public class ComboBoxBuilder extends ChoiceBuilder {
    public ComboBoxBuilder(Gui gui) {
        super(gui);
    }

    @Override
    protected boolean isEditable() {
        return true;
    }

}
