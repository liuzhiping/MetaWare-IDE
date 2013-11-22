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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.arc.dwarf2.model.IAttribute;
import com.arc.dwarf2.model.IDwarf2ErrorReporter;
import com.arc.dwarf2.model.ITag;

/**
 * An instance of this class exists for each compile unit. It is responsible
 * for constructing the Tag objects lazily. It takes into account sibling
 * links to avoid unnecessarily scanning child tags before they are needed.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
class TagBuilder {
    
    private Map<Integer, Abbrev> fAbbrevMap;
    private Extractor fInfo;
    private Extractor fStr;
    private IDwarf2ErrorReporter fReporter;
    private Map<Integer, Tag> fTagMap;

    /**
     * 
     * @todo davidp needs to add a constructor comment.
     * @param abbrevMap maps the "acode" to the Abbrev table entry.
     * @param info the ".debug_info" data.
     * @param str the ".debug_str" data if it exists; otherwise <code>null</code>.
     * @param reporter where errors are sent.
     */
    public TagBuilder(Map<Integer,Abbrev>abbrevMap, Extractor info, Extractor str, IDwarf2ErrorReporter reporter){
        fAbbrevMap = abbrevMap;
        fInfo = info;
        fStr = str;
        fReporter = reporter;
        fTagMap = new HashMap<Integer,Tag>();
        
    }
    
    /**
     * Return the tag that is defined at the given offset in the ".debug_info" section. Once a tag
     * is read in, it is cached in a hash map. 
     * @param offset the offset within the .debug_info's compile_unit table.
     * @return the tag object associated with the given offset, or <code>null</code> if the
     * offset is positioned at the end of the table, or at a null byte.
     */
    public Tag getTagAt(int offset){
        Tag tag = fTagMap.get(offset);
        if (tag == null) {
            try {
                tag = readTagAt(offset);
            }
            catch (IOException e) {
                fReporter.error(fInfo.getName(),fInfo.getOffset(),e.getMessage());
            }
            if (tag != null)
                fTagMap.put(offset,tag);
        }
        return tag;       
    }
    
    /**
     * Read a list of tags starting at the given offset. The list assumed to end in a zero byte. 
     * @param offset the offset of where the children start.
     * @return list of child tags.
     */
    public List<ITag> getChildTags(int offset) {
        ArrayList<ITag>list = new ArrayList<ITag>();
        while (true){
            Tag t = getTagAt(offset);
            if (t == null) break;
            list.add(t);
            offset = t.getOffsetOfEnd();
        }
        return list;
    }
    
    /**
     * Read in the tag at the given offset in the .debug_info table, or null if
     * the byte at that offset is 0.
     * @param offset
     * @return the tag constructed from .debug_info table, or null if the byte at the offset is 0.
     * @throws IOException
     */
    private Tag readTagAt(int offset) throws IOException{
        fInfo.setOffsetFromStart(offset);
        if (fInfo.getOffset() == fInfo.getLength())
            return null; // at the end
        int acode = fInfo.readULEB();
        if (acode == 0) return null;
        Abbrev abbrev = fAbbrevMap.get(acode);
        if (abbrev == null){
           
            throw new IOException("Bad acode reference: 0x" + 
                Integer.toHexString(acode));
        }
        List<IAttribute> attributes = new ArrayList<IAttribute>();
        int[] attributeAndForm = abbrev.getAttributesAndForms();
        try {
            for (int i = 0; i < attributeAndForm.length; i += 2) {
                attributes.add(new Attribute(attributeAndForm[i], attributeAndForm[i + 1], fInfo, fStr, fReporter));
            }
        }
        catch (IOException e) {
            fReporter.error(fInfo.getName(),fInfo.getOffset(),"Read failure : " + e.getMessage());
        }            
        return new Tag(this,abbrev,Collections.unmodifiableList(attributes), offset+fInfo.getStart(),
            fInfo.getOffsetFromStart());       
    }

}
