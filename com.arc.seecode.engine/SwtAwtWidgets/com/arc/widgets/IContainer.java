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


import java.awt.Rectangle;


/**
 * A GUI component that serves as a container for other components. A component is added to a container by the the
 * component factory.
 */
public interface IContainer extends IComponent {

    /**
     * Return the number of components in this container.
     */
    // int getComponentCount();
    /**
     * Return the i'th component within this contain.
     */
    // IComponent getComponent(int i);
    /**
     * Return the style of container.
     * @see IComponentFactory#makeContainer(IContainer,int)
     */
    int getContainerStyle ();

    /**
     * Set the number of pixels between horizontal components in this container.
     */
    void setHorizontalSpacing (int pixels);

    /**
     * Set the number of pixels between vertical components in this container.
     */
    void setVerticalSpacing (int pixels);

    /**
     * Return children of this container.
     * @return children of this container.
     */
    IComponent[] getChildren ();

    /**
     * Once all children have been added. Do layout.
     */
    void layout ();

    /**
     * Return the rectangle where were suppose to embed stuff.
     * @return the rectangle where were suppose to embed stuff.
     */
    Rectangle getClientArea ();
}
