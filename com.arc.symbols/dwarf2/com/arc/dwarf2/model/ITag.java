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


/**
 * Denotes a Dwarf2 "tag" description. The root of the tag hierarchy of a compilation unit is
 * retreived from {@link IUnit#getTag()}.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface ITag {
    /**
     * Return the tag ID (e.g., DW_TAG_compile_unit, DW_TAG_typedef, etc.).
     * @return the tag ID.
     */
    public int getID();
    
    /**
     * Return an immutable list of the child tags. If there are no child tags, then
     * this method will return a zero-length list.
     * @return an immutable list of the child tags.
     */
    public List<ITag> getChildren();
    
    /**
     * Return an immutable list of the associated attributes. If there are no attributes,
     * then a zero-length list is returned.
     * @return an immutable list of the associated attributes
     */
    public List<IAttribute> getAttributes();
    
    /**
     * Return the attribute corresponding to an attribute ID.
     * @param atID the ID of the attribute to be extracted.
     * @return the corresponding attribute, or <code>null</code>.
     */
    public IAttribute getAttribute(int atID);
    
    /**
     * Return the offset within the .debug_section at which this tag resides. (For the benefit
     * of dwarf displayers).
     * @return the offset within the .debug_section at which this tag resides.
     */
    public int getOffset();
    
    /**
     * Does this tag have children?
     * @return true if this tag has children.
     */
    public boolean hasChildren();
    
    /**
     * Return the abbrev entry code (used for displaying dwarf).
     * @return the abbrev entry code.
     */
    public int getAcode();
    
    /**
     * Return the offset within the .debug_abbrev table the corresponds to this
     * entry. Provide for display purposes only.
     * @return the offset within the .debug_abbrev table the corresponds to this
     * entry.
     */
    public int getAbbrevOffset();
    
//    /**
//     * Return sibling if applicable; otherwise <code>null</code>.
//     * @return sibling if applicable; otherwise <code>null</code>.
//     */
//    public ITag getSibling();
    
 

}
