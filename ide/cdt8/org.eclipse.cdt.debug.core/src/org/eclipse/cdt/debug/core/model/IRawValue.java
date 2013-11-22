/*
 * IRawValue
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
package org.eclipse.cdt.debug.core.model;


/**
 * A new interface so that we can impose formatting on a value as we
 * see fit.
 * <P>
 * CUSTOMIZATION
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public interface IRawValue {
    /**
     * Return whether or not the value is valid. The value of an invalid register would be
     * invalid.
     * @return whether or not this value is valid.
     * 
     */
    boolean isValid();
    
    /**
     * If this value is an array, return the size of each element in bytes.
     * @return the size of each element if this is an aggregate, or the the size of the value
     * if it is a scalar.
     */
    int getUnitSize();
    
    /**
     * Return the length in bytes of this value.
     * @return the length of this value in bytes.
     */
    int getLength();
    
    /**
     * Return the value as an integer if it is a scalar. Returns the first element value if 
     * an array.
     * @return the value as an integer if it is a scalar, or the first element value if 
     * an array.
     */
    long getValue();
    
    /**
     * Return whether or not this value is a scalar. If not, it is assumed to be an array or
     * scalars.
     * @return whether or not this value is a scalar or an array of scalars.
     */
    boolean isScalar();
    
    /**
     * Returns the value as either an instance of Number, or instance of byte[], short[], int[],
     * or long[], based on the unit size.
     * @return the value as an object.
     */
    Object getValueObject();
    
    /**
     * A "special" value is one that is to displayed as a string and is not subject for formatting.
     * @todo davidp needs to add a method comment.
     * @return
     */
    boolean isSpecial();
    
    /**
     * Return the value that is to be displayed if this value is "special".
     * @return the value to be displayed if this value is special; undefined if the value is
     * not special.
     */
    String getSpecialValue();
}
