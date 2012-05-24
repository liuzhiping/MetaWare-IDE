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
package com.metaware.guihili;

import com.arc.widgets.IComponent;

/**
 * A callback for associating a help message to a component so
 * as to display a help frame when F1 is pressed.
 * @author David Pickens
 */
public interface IHelpHandler {
    void associateHelpMessage(IComponent c, String msg);

}
