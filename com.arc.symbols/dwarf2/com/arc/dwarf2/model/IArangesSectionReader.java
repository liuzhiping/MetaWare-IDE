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
package com.arc.dwarf2.model;

import java.util.Iterator;

/**
 * An interface to read the content of the ".debug_aranges" section.
 * @author davidp
 *
 */
public interface IArangesSectionReader {
    /**
     * Provide iterator to yield each address ranges entry.
     * @return iterator for traversing address ranges.
     */
    Iterator<IArange> each();
    
    /**
     * Return the address ranges for a particular compilation unit.
     * @param offset the offset of the compilation unit in the .debug_info section.
     * @return the corresponding address ranges, or <code>null</code>.
     */
    IArange getCompilationUnit(int offset);
}
