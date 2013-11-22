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
 * An object that denotes an assembly instruction as produced
 * by the engine's disassembler.
 * <P>
 * Instances of this class are created from C++ by means of JNI.
 * Thus, any changes need to be reflected back into the C++ code
 * that accesses it.
 * <P>
 * Note that this class extends {@link Location} as it needs 
 * location information. This decision is to reduce the complication
 * of allocating a Location object from C++.
 * 
 * @author David Pickens
 */
public class AssemblyRecord {
    private String mHex;
    private String mOpcode;
    private String mOperands;
    private String mComment;
    private long mAddress;
    /**
     * Return the code address that this record corresponds to.
     * @return the associated code address.
     */
    public long getAddress(){
        return mAddress;
    }
    
    /**
     * Return the hexadecimal representation of the instruction,
     * formatted with spaces as the user would like to see it.
     * @return the hexadecimal representation of the instruction.
     */
    public String getHex(){
        return mHex;
    }
    
    /**
     * Return the opcode portion of the instruction.
     * @return the assembly instruction.
     */
    public String getOpcode(){
        return mOpcode;
    }
    
    /**
     * Return the operands of the instruction.
     * @return the operands of the instruction.
     */
    public String getOperands(){
        return mOperands;
    }
    
    /**
     * Return a comment to appear after the instruction, if any.
     * @return a comment to appear after the instruction.
     */
    public String getComment(){
        return mComment;
    }
    
    /**
     * Set the comment to appear after the instruction, if any.
     * @param comment the comment to appear after the instruction.
     */
    public void setComment(String comment){
        mComment = comment;
    }
    
    /**
     * Set the hexadecimal encoding of the instruction as
     * it will appear preceeding the instruction.
     * @param hex hexadecimal encoding.
     */
    public void setHex(String hex){
        mHex = hex;
    }
    
    public void setOperands(String operands){
        mOperands = operands;
    }
    
    /**
     * Set the text of the assembly instruction, including operands.
     * @param opcode the opcode name.
     */
    public void setOpcode(String opcode){
        mOpcode = opcode;
    }
    
    public void setAddress(long a){
        mAddress = a;
    }
    
    private static boolean cmp(String a1, String a2){
        if (a1 == null) return a2==null;
        return a1.equals(a2);
    }
    
    /**
     * For sake of deserializing test.
     * @param r
     * @return whether or not this object is equal to the argument.
     */
    public boolean equals(AssemblyRecord r){
        return r.getAddress() == getAddress() &&
        	cmp(r.getHex(),getHex()) &&
        	cmp(r.getComment(),getComment()) &&
        	cmp(r.getOpcode(),getOpcode()) &&
        	cmp(r.getOperands(),getOperands());
        
    }
}
