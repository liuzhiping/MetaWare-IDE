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
package com.arc.seecode.engine;

/**
 * Manifest constant for formatting expression values.
 * <P>
 * <b>Note:</b> these correspond to manifest constants in the engine, as defined in
 * <code>language/format.h</code>.
 * @author David Pickens
 */
public enum Format {
    NATURAL("Natural"),        // Use "natural" representation
    SIGNED_DECIMAL("Signed Decimal"), 
    UNSIGNED_DECIMAL("Unsigned Decimal"),
    HEXADECIMAL("Hex"),
    OCTAL("Octal"),
    CHAR("Char"),       // single ASCII character?
    ENUM("Enum"),       // ???
    FLOAT("Floating Point"),      // single precision floating point
    DOUBLE("Double Float"),     // double precision floating point
    EXTENDED("Extended Float"),    // extended precision floating point
    POINTER("Pointer"),    // ???
    POINTER_ASCII("Pointer-to-ASCII"),  // ???
    INCLUDE_HEX("Natural+Hex"),    // Mix natural + hex.
    CHANGE_DISPLAY("Change"), // format as it should appear in a change dialog
    ASCII("ASCII"),          // array of ascii characters
    UCHAR("Unsigned Char"),          // unsigned character
    SCHAR("Signed Char"),         // signed character
    FRACTIONAL_15("Fraction-16"),   // 16-bit fractional
    FRACTIONAL_31("Fraction"),  // 32-bit fractional
    FRACTIONAL_9_31("Fraction-40"),    // 40-bit fractional
    BINARY("Binary");
    
    Format(String name){
        _name = name;
    }
    
    
    private String _name;
    @Override
    public String toString(){
        return _name;
    }
    
    public static Format lookup(String name){
        for (Format f: values()){
            if (f.toString().equals(name))
                return f;
        }
        return null;
    }
}
