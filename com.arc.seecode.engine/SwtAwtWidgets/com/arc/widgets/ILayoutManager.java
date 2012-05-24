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
package com.arc.widgets;

import java.awt.Dimension;


/**
 * A class that is responsible for laying out
 * a container.
 * @author David Pickens
 */
public interface ILayoutManager {
    public final int DEFAULT_SIZE = -1;
    /**
     * 
     * @param wHint a hint as to what the width should be,
     * or {@link #DEFAULT_SIZE} if there is no hint.
     * @param hHint wHint a hint as to what the height should be,
     * or {@link #DEFAULT_SIZE} if there is no hint.
     * @return the preferred size of the container.
     */
    public Dimension computeSize(IContainer c, int wHint, int hHint);

    /**
     * Layout the components in the container by
     * calling {@link IComponent#setBounds} on each child.
     * Also, lays out child containers transitively.
     * @param c
     */
    public void layout(IContainer c);
}
