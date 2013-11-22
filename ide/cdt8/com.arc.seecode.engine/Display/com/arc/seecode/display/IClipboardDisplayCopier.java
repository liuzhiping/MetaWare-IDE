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
package com.arc.seecode.display;


/**
 * A callback for coping the content of a display to the clipboard.
 * @author dpickens
 *
 */
public interface IClipboardDisplayCopier {
    /**
     * Do whatever is necessary to copy the entire content of the associated
     * display to the clipboard.
     * @return true if operation is successful; false if the implementation
     * does not support this operation, which case the caller is expected
     * to do it himself on whatever is visible in the display.
     */
    boolean copyAllToClipboard(ISeeCodeTextViewer d);
}
