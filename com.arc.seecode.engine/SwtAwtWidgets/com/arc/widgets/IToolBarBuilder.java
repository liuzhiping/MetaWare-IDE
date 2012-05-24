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
 * We use this interface to construct the toolbar of the SeeCode displays.
 * Under Eclipse, we use the interface to construct toolbar items in the View's
 * toolbar area.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface IToolBarBuilder {
    
    /**
     * Create a combobox.
     * @param label if not <code>null</code>, a label that is to immediately
     * preceed the combobox.
     * @param editable if true, the combobox will be editable.
     * @return a combobox.
     */
    IChoice makeComboBox(String label, boolean editable);
    
    /**
     * Create an image that is typically a rectangle with a color.
     * @param width the width in pixels.
     * @param height the height in pixels.
     * @param depth the number of colors per pixel.
     * @return an image
     */
    IImageWidget makeImage(int width, int height, int depth);
    
    /**
     * Create a push button.
     * @return a new push button.
     */
    IToolItem makeButton();
    
    /**
     * 
     * Make a label
     * @param label text of the label
     * @return a newly created label
     */
    ILabel makeLabel(String label);
    
    /**
     * Make a push-in/push-out button.
     * @return a toggle button.
     */
    IToolItem makeToggleButton();
    
    /**
     * Make a horizontal separator for the toolbar.
     */
    void makeSeparator();
    
    /**
     * Indicate that a new row is to appear.
     * The Eclipse implementation ignores this because its toolbars automatically
     * fold.
     */
    void makeRowSeparator();
}
