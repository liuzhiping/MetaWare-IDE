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
package org.eclipse.cdt.debug.core.cdi.model;

import org.eclipse.core.resources.IFile;

/**
 * 
 * Interface that a CDI thread can implement so as to query if a jump-to-line operation
 * is supportable.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */

public interface ICDIJumpToLine {
    /**
     * Indicate whether or not the target can jump to the given source line.
     * @param file the source file.
     * @param line the source line.
     * @return whether or not the target can jump to the given source line.
     */
    boolean canJumpToLine(IFile file, int line);
    /**
     * Indicate whether or not the target can jump to the given source line.
     * @param file the source file.
     * @param line the source line.
     * @return whether or not the target can jump to the given source line.
     */
    boolean canJumpToLine(String file, int line);
}
