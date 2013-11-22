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

/**
 * Represents the content of a single entry of the ".debug_abbrev" table.
 * @todo davidp needs to add a class description.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
class Abbrev {
    private int fTagID;
    private boolean fHasChildren;
    private int fAoff;
    private List<Integer> fAttr = new ArrayList<Integer>();
    private List<Integer> fForm = new ArrayList<Integer>();
    private int fAcode;

    /**
     * 
     * @param tagID the tag ID (e.g., DW_tag_compile_uint)
     * @param acode the number of this entry.
     * @param aoff the offset in the .debug_abbrev table where this entry is defined.
     * @param hasChildren true if there are children.
     */
    Abbrev(int tagID, int acode, int aoff, boolean hasChildren){
        fTagID = tagID;
        fHasChildren = hasChildren;
        fAoff = aoff;
        fAcode = acode;
    }
    
    /**
     * 
     * Add an attribute.
     * @param attributeID the attribute ID.
     * @param formID the format of the attribute.
     */
    public void add(int attributeID, int formID){
        fAttr.add(attributeID);
        fForm.add(formID);       
    }
    
    /**
     * Return array of attribute/format pairs.
     * @return array of attribute/format pairs.
     */
    public int[] getAttributesAndForms(){
        int result[] = new int[fAttr.size()*2];
        for (int i = 0; i < fAttr.size(); i++){
            result[i*2] = fAttr.get(i);
            result[i*2+1] = fForm.get(i);
        }
        return result;
    }
    
    public int getAOffset() { return fAoff; }
    
    public int getACode() { return fAcode; }
    
    public boolean hasChildren(){
        return fHasChildren;
    }
    
    public int getTagID(){
        return fTagID;
    }

}
