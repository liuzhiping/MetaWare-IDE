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


/**
 * A GUI component that serves as a container for "cards".
 */
public interface ICardContainer extends IContainer {

    /**
     * Set the name of a child.
     * @param child assign name to existing child.
     */
    void setCardName (Object child, String name);

    /**
     * Show given child.
     * @param child child component to display.
     * @exception IllegalArgumentException Thrown if child isn't in the container.
     */
    void showCard (Object child);

    /**
     * Show child that has a given name.
     * @param name name of child component to display.
     * @exception IllegalArgumentException thrown if child isn't in the container or is <code>null</code>.
     */
    void showCard (String name);
}
