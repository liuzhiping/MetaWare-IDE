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
 * A representation of an image.
 * @author David Pickens
 */
public interface IImage {
    /**
     * @return underlying implementation object.
     */
    Object getObject();
    
    /**
     * @return the width of the image in pixels.
     */
    int getWidth();
    
    /**
     * @return the height of the image in pixels.
     */
    int getHeight();
    

    /**
     * Dispose of this image.
     *
     */
    void dispose();
}
