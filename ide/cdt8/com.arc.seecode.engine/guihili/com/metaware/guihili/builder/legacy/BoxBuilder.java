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
import com.metaware.guihili.builder.ContainerBuilder;

/**
 * Guihili "box" node: Its like a container but with a border.
 */
public class BoxBuilder extends ContainerBuilder {
	public BoxBuilder(Gui gui) {
		super(gui);
		try {
			setBorder("etched");
		} catch (Exception x) {
		}
	}
}
