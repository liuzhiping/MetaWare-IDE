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
package com.arc.dwarf2.internal.symbols;

import java.util.List;
import java.util.Map;

import com.arc.dwarf2.Dwarf2.AttributeFormat;
import com.arc.dwarf2.model.IAttribute;
import com.arc.dwarf2.model.ILineTableReader;
import com.arc.dwarf2.model.ITag;
import com.arc.dwarf2.model.IUnit;
import com.arc.symbols.types.IType;


class Utility {
    public static String getStringAttribute(ITag tag, int atID){
        IAttribute attr = tag.getAttribute(atID);
        if (attr != null) return attr.getStringValue();
        return null;
    }
    
    public static Boolean getBooleanAttribute(ITag tag, int atID){
        IAttribute attr = tag.getAttribute(atID);
        if (attr != null) return attr.getIntValue() != 0;
        return false;
    }
    
    public static ILineTableReader.FileRef getFileRefAttribute(ITag tag, int atID, IUnit unit)
        throws IllegalStateException
        {
        IAttribute attr = tag.getAttribute(atID);
        if (attr != null && unit.getLineTableReader() != null) {
            long fileIndex = attr.getIntValue();
            if (fileIndex > 0) {
                List<ILineTableReader.FileRef> files = unit.getLineTableReader().getFiles();
                if (fileIndex <= files.size()) {
                    return files.get((int)fileIndex-1);
                }
                throw new IllegalStateException("Bad file reference: " + fileIndex);
            }
        }
        return null;
    }
    
    public static IType getTypeAttribute(ITag tag, int atID, Map<ITag,IType>typeMap, IUnit unit){
        IAttribute attr = tag.getAttribute(atID);
        if (attr != null) {
            if (attr.getFormat() == AttributeFormat.REF_FORM){
                long off = attr.getIntValue();
                ITag typeTag = unit.getTagRef((int)off);
                IType type = typeMap.get(typeTag);
                if (type == null){
                    type = Type.makeType(typeTag,typeMap,unit);
                    typeMap.put(typeTag,type);
                }
                return type;
            }
            else throw new IllegalArgumentException("Format for type tag at " + tag.getOffset() + " within unit of offset " +
                unit.getInfoOffset() + " has unexpected format: " + attr.getFormID());
   
        }
        return null;
    }
    
    
}
