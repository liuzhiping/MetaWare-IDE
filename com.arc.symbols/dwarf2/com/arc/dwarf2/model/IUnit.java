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


/**
 * A description of a Dwarf2 compilation unit.
 * Instances of this interface are returned from the {@link IDwarf2Reader#getUnits()} method.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface IUnit {
    /**
     * Return the version of this compilation unit.
     * @return the version of this compilation unit.
     */
    public int getVersion();
    
    /**
     * Return the address size (e.g., 4).
     * @return the address size.
     */
    public int getAddressSize();
    
    /**
     * Return the byte-length of the data in this compilation unit. Provided in case it
     * needs to be displayed for some reason.
     * @return the byte-length of the data in this compilation unit.
     */
    public int getLength();
    
    /**
     * Return the offset in the ".debug_abbrev" section of the Abbrev table.
     * Provided in case it needs to be displayed.
     * @return the offset in the ".debug_abbrev" section of where the associated abbrev table starts.
     */
    public int getAbbrevOffset();
    
    /**
     * Return the offset within the ".debug_info" section where this unit resides. Provided
     * for display purposes.
     * @return the offset within the ".debug_info" section where this unit resides.
     */
    public int getInfoOffset();
    
    /**
     * Return the "compile_unit" tag for this compilation unit.
     * @return the "compile_unit" tag for this compilation unit.
     */
    public ITag getTag();
    
    /**
     * Return the associated line table for this compilation unit.
     * @return the associated line table for this compilation unit.
     */
    public ILineTableReader getLineTableReader();
    
    /**
     * Given a relative offset to a tag in this compilation, return the tag.
     * @param relativeOffset relative offset of tag.
     * @return the corresponding tag.
     * @throws IllegalArgumentException if the offset doesn't appear to reference a tag boundary.
     */
    public ITag getTagRef(int relativeOffset);

}
