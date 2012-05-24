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
 * Representation of a font.
 * @author David Pickens
 */
public interface IFont {

    /**
     * Return underlying representation.
     * @return underlying representation.
     */
    Object getObject ();

    /**
     * Return the name of the font family.
     * @return the name of the font family.
     */
    String getName ();

    /**
     * Return the size of the font in points.
     * @return the size of the font in points.
     */
    int getSize ();

    /**
     * @return whether or not this font is in italics.
     */
    boolean isItalic ();

    /**
     * @return whether or not htis font is bold.
     */
    boolean isBold ();

    /**
     * Return with of string in pixels required with this font.
     * @param x string.
     * @param c an associated component inwhich the string is to be rendered.
     * @return the width in pixels of the string being rendered with this font.
     */
    int getPixelWidth (String x, IComponent c);

    /**
     * Return the distance from the base of one line to the base of the next consecutive line, using this font.
     * @param c the associated component.
     * @return height of this font.
     */
    int getPixelHeight (IComponent c);

}
