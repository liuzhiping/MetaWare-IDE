/*
 * ICDIJumpToLine
 * $Revision$
 * $Date$
 *
 * CONFIDENTIAL AND PROPRIETARY INFORMATION.
 * Copyright 2007 ARC International (Unpublished).
 * All Rights Reserved.
 * This document, material and/or software contains confidential and
 * proprietary information of ARC International and is protected by copyright,
 * trade secret and other state, federal, and international laws, and may be
 * embodied in patents issued or pending. Its receipt or possession does not
 * convey any rights to use, reproduce, disclose its contents, or to
 * manufacture, or sell anything it may describe.  Reverse engineering is
 * prohibited, and reproduction, disclosure or use without specific written
 * authorization of ARC International is strictly forbidden.  ARC and the ARC
 * logotype are trademarks of ARC International.
 */
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
