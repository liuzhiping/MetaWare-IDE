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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.metaware.guihili.Gui;
import com.metaware.guihili.ITextWrapper;

/**
 * An instance of this class is used to track a text widget whose
 * text depends on the setting of a property.
 *
  * @author J. David Pickens
 */
public class TextPropertyChange implements PropertyChangeListener {
	public TextPropertyChange(Gui gui, ITextWrapper component) {
		if (gui == null || component == null)
			throw new IllegalArgumentException("Argument is null!");
//		_gui = gui;
		_component = component;
	}
	@Override
    public void propertyChange(PropertyChangeEvent event) {
//		String name = event.getPropertyName();
		String value = event.getNewValue() != null?event.getNewValue().toString():null;
		/*
		 * We must be careful here! By setting the value of the text
		 * field, we will fire a PropertyChange event that could reinvoke
		 * this method recursively. Thus, only change things if they
		 * need to be.
		 */
		if (_component.getText() == null
			|| !_component.getText().equals(value)) {
			_component.setText(value);
		}
	}
//	private Gui _gui;
	private ITextWrapper _component;
}
