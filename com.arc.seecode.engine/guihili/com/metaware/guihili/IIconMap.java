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

import com.arc.widgets.IImage;

/**
 * An interface for associating button icons with a label.
 * If a button has just a label but no icon, then this map
 * will be consulted to see if there is a corresponding icon.
 *
 * @author David Pickens
 * @version May 22, 2002
 */
public interface IIconMap {
    /**
     * Return icon associated with a button label, if any.
     * @param label the button label.
     * @return the associated icon, or null if there is no associated icon.
     */
    public IImage getIcon(String label);
    }
