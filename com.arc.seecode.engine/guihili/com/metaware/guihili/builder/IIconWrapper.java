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


import com.arc.widgets.IImage;


/**
 * A wrapper for being able retrieve and set an icon property.
 */
public interface IIconWrapper {

    public void setIcon (IImage icon);

    public IImage getIcon ();
}
