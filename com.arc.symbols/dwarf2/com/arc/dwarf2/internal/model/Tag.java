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
package com.arc.dwarf2.internal.model;

import java.util.ArrayList;
import java.util.List;

import com.arc.dwarf2.DwarfConstants;
import com.arc.dwarf2.model.IAttribute;
import com.arc.dwarf2.model.ITag;


class Tag implements ITag {

    private List<IAttribute> fAttributes = null;
    private Abbrev fAbbrev;
    private List<ITag> fChildren = null;
    private TagBuilder fBuilder;
    private int fChildOffset;
    private int fThisOffset;

    /**
     * 
     * @param builder a builder for constructing child tags, if necessary.
     * @param abbrev the entry in the ".debug_abbrev" table that corresponds to this tag.
     * @param attributes immutable list of attributes.
     * @param thisOffset offset within .debug_info" that this tag resides.
     * @param childOffset  offset wwhere children begin, if any.
     */
    Tag(TagBuilder builder, Abbrev abbrev, List<IAttribute>attributes, int thisOffset, int childOffset){
        fBuilder = builder;
        fAbbrev = abbrev;
        fAttributes = attributes;
        fChildOffset = childOffset;
        fThisOffset = thisOffset;
    }

    @Override
    public List<IAttribute> getAttributes () {
        return fAttributes;
    }
    
    private static ArrayList<ITag> NO_CHILDREN = new ArrayList<ITag>(0);

    @Override
    public List<ITag> getChildren () {
        if (fChildren == null) {
            if (fAbbrev.hasChildren()){
                fChildren = fBuilder.getChildTags(fChildOffset);
            }
            else
                fChildren = NO_CHILDREN;
        }
        return fChildren;
    }

    @Override
    public int getID () {
        return fAbbrev.getTagID();
    }
    
    /**
     * Return an attribute with the give ID, or <code>null</code> if there
     * is no such attribute.
     * @param id the attribute ID being sought.
     * @return an attribute with the give ID, or <code>null</code> if there
     * is no such attribute.
     */
    @Override
    public IAttribute getAttribute(int id){
        for (IAttribute at: fAttributes){
            if (at.getID() == id) return at;
        }
        return null;
    }
    /**
     * Return the offset of the byte immediately following this tag in the .debug_info section.
     * If there is a "sibling" attribute, then use that. Otherwise, its the byte beyond the last
     * child.
     * @return the offset of the byte immediately following this tag in the .debug_info section.
     */
    int getOffsetOfEnd(){
        if (fAbbrev.hasChildren()){
            IAttribute attr = getAttribute(DwarfConstants.DW_AT_sibling);
            if (attr != null) return (int)attr.getIntValue();
            List<ITag> children = getChildren();
            if (children.size() == 0) return fChildOffset+1;
            return ((Tag)children.get(children.size()-1)).getOffsetOfEnd() + 1;
        }
        else return fChildOffset;
        
    }
    
    public ITag getSibling() {
        return fBuilder.getTagAt(getOffsetOfEnd());
    }

    @Override
    public int getOffset () {
        return fThisOffset;
    }

    @Override
    public int getAbbrevOffset () {
        return fAbbrev.getAOffset();
    }

    @Override
    public int getAcode () {
        return fAbbrev.getACode();
    }

    @Override
    public boolean hasChildren () {
        return fAbbrev.hasChildren();
    }

}
