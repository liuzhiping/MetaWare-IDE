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

import java.util.List;

import com.arc.dwarf2.Dwarf2ReaderFactory;


/**
 * The primary interface for the Dwarf2 reader. The instance of this interface
 * is created by calling a method in {@link Dwarf2ReaderFactory}.
 * <P>
 * <b>Note:</b> the underlying implementation may read the Dwarf2 information lazily. Thus, errors that
 * are passed to the associated {@link IDwarf2ErrorReporter} interface may occur as a side-effect of
 * calling any of the methods in this interface.
 */
public interface IDwarf2Reader {
    
    /**
     * Return the compilation units as an immutable collection.
     * <P>
     * Note: the underlying implementation may actually read the unit data
     * lazily on demand. This means that errors may be reported at arbitrary times.
     * @return the compilation units as an immutable collection.
     */
    public List<IUnit> getUnits();
    
    /**
     * Return the ".debug_info" section name corresponding to the (possibly) composite offset.
     * If we're not reading a composite section, then this method returns ".debug_info".
     * <P>
     * Intended for getting information for display purposes only.
     * @param infoOffset the composite offset.
     * @return the name of the debug info section at the given composite offset.
     */
    
    public String getInfoName(int infoOffset);
    
    /**
     * If we're reading from a composite of info sections, return the offset relative to the
     * given composite offset. If a we're simply reading a single ".debug_info" section, then
     * this method returns the value of the argument.
     * <P>
     * This information is for display purposes only.
     * @param infoOffset the composite offset.
     * @return the offset relative to the base of the .debug_info$Foo that the offset references.
     */
    public int getInfoOffset(int infoOffset);
    
}
