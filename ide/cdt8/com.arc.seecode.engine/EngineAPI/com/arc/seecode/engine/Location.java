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
 * An address location as associated with a stackframe of a 
 * stack trace, etc.
 * <P>
 * Instances of this class are created from the C++-based engine
 * via JNI calls. Any changes must be reflected back into the C++
 * code.
 * 
 * @author David Pickens
 */
public class Location {
    private String mFunction;
    private String mSource;
    private int mLine;
    private long mAddress;
    private int mFunctionOffset;
    private int mLineOffset;
    private boolean mAmbiguous; // could reference more than one address
    private boolean mValid = true;
   
    // The following two instance variables only exists if interface is >= 5
    private int mModule = 0;  
    private long mLogicalAddress = 0;
    
    /**
     * Return the function associated with this location,
     * or <code>null</code> if no associated function can be located.
     * @return associated function or <code>null</code>.
     * 
     * @pre $none
     * @post $none
     */
    public String getFunction() {
        return mFunction;
    }
    
    /**
     * Clone a copy of this object so that, presumably, it can subsequently modified.
     * @nojni
     */
    @Override
	public Location clone(){
    	Location loc = new Location();
    	loc.mAddress = mAddress;
    	loc.mFunction = mFunction;
    	loc.mFunctionOffset = mFunctionOffset;
    	loc.mLine = mLine;
    	loc.mLineOffset = mLineOffset;
    	loc.mLogicalAddress = mLogicalAddress;
    	loc.mModule = mModule;
    	loc.mSource = mSource;
    	loc.mValid = mValid;
    	return loc;  	
    }
    
    /**
     * @nojni
     * @param s1
     * @param s2
     * @return cmpare if two objects are equal taking into account that one or both may be <code>null</code>.
     */
    
    static boolean compareEqual(Object s1, Object s2){
        if (s1 == null) return s2 == null;
        return s1.equals(s2);
    }
    /**
     * @nojni
     */
    public final boolean equals (Location loc) {
        return mAddress == loc.mAddress
                && mLine == loc.mLine
                && mFunctionOffset == loc.mFunctionOffset
                && mLineOffset == loc.mLineOffset
                && compareEqual(mFunction, loc.mFunction)
                && compareEqual(mSource, loc.mSource);
    }
    
    /**
     * @nojni
     */
    @Override
    public boolean equals(Object obj){
        return obj instanceof Location && equals((Location)obj);
    }
    
    /**
     * @nojni
     */
    @Override
    public int hashCode(){
    	return (mLine << 16) ^ (int)mAddress ^ (mSource!=null?mSource.hashCode():0) ^
    			(mFunction != null?mFunction.hashCode():0);
    }
   
    /**
     * Return the byte-offset from the function, if there is
     * an associated function.
     * @return byte-offset from the function.
     * 
     * @pre getFunction() != null
     * @post $none
     */ 
    public int getFunctionOffset(){
        return mFunctionOffset;
    }
    
    /**
     * Return whether or not this location is valid.
     * If false, then the method that construction this object
     * encountered an error of some sort.
     * @return whether or not this location is valid.
     */
    public boolean isValid(){ return mValid;}
    
    /**
     * Return whether or not there is potentially more then one machine address this
     * location could reference. For example, a source line reference could have been
     * inlined in multiple places. The resolved address is to one of the locations.
     * @return whether or not more than one machine address could resolved to this location.
     * @nojni
     */
    public boolean isAmbiguous() { return mAmbiguous; }
    
    /**
     * Return the source file path, or <code>null</code> if
     * the source file path can't be determined.
     * @return source file path, or <code>null</code>.
     * 
     * @pre $none
     * @post $none
     */
    public String getSource(){
        return mSource;        
    }
    
    /**
     * Return the source line, if applicable.
     * @return the corresponding source line, or 0.
     */
    public int getSourceLine(){
        return mLine;      
    }
    
    /**
     * Return the number of bytes passed the nearest source line.
     * @return the number of bytes passed the nearest source line.
     */
    public int getSourceLineOffset(){
        return mLineOffset;
    }
    
    /**
     * Return the associated physical machine address
     * @return the associated physical machine address.
     * 
     * @pre $none
     * @post $none
     */
    public long getAddress(){
        return mAddress;
    }
    
    /**
     * If there is an associated logical address, return the index of the"module" that is associated
     * with the logical address.
     * @return the module associated with the logical address, or 0 if there is no associated module.
     * "new" tells out auto-wrapper generator to not require that this method exist. This permits newer debuggers to
     * work with older IDE's.
     * @new
     */
    public int getModule(){
        return mModule;
    }
    
    /**
     * Return the associated logical address if known. Otherwise, returns 0.
     * <B>NOTE:</b> a logical address is assumed to exist if {@link #getModule()} return an index > 0.
     * @return the associated logical address if there is an associated module.
     * "new" tells out auto-wrapper generator to not require that this method exist. This permits newer debuggers to
     * work with older IDE's.
     * @new
     */
    public long getLogicalAddress(){
        return mLogicalAddress;
    }
    
    /**
     * Set the associated machine address for the case where there is no logical address.
     * @param a the associated machine address.
     */
    public void setAddress(long a){
        mAddress = a;
        mModule = 0;
        mLogicalAddress = 0;
    }
    
    /**
     * Set the associated function name.
     * @param name the associated function name.
     * @param byteOffset the byte offset into the function.
     */
    public void setFunction(String name, int byteOffset){
        mFunction = name;
        mFunctionOffset = byteOffset;
    }
    
    /**
     * Set the source line number and a byte-offset from there.
     * @param line the source line number.
     */
    public void setSourceLine(int line, int byteOffset){
        mLine = line;
        mLineOffset = byteOffset;
    }
    
    /**
     * Set the source file path.
     * @param name the source file path.
     */
    public void setSource(String name){
        mSource = name;
    }
    
    public void setValid(boolean v){
        mValid = v;       
    }
    
    /**
     * 
     * @param module
     * @param logAddress
     * @param phyAddress
     * "new" tells out auto-wrapper generator to not require that this method exist. This permits newer debuggers to
     * work with older IDE's.
     * @new
     */
    public void setLogicalAddress(int module, long logAddress, long phyAddress){
        if (module <= 0) throw new IllegalArgumentException("Invalid module index");
        mModule = module;
        mLogicalAddress = logAddress;
        mAddress = phyAddress;
    }
    
    /**
     * Indicate whether or not this location could resolve to more than one machine address.
     * <P>
     * Valid for interface version 22 or later.
     * @new
     */
    public void setAmbiguous(boolean v){
        mAmbiguous = v;
    }
    /**
     * @nojni
     */
    @Override
    public String toString(){
        StringBuffer buf = new StringBuffer();

        if (mFunction != null){
            buf.append(mFunction);
            if (mFunctionOffset != 0){
                buf.append('+');
                buf.append(mFunctionOffset);
            }
        }
        buf.append("[=0x");
        buf.append(Long.toHexString(mAddress));
        buf.append("]");
        if (mSource != null){
            buf.append("@\"");
            buf.append(mSource);
            buf.append("\",line ");
            buf.append(mLine);
            if (mLineOffset != 0){
                buf.append("+0x");
                buf.append(Integer.toHexString(mLineOffset));
            }
        }
        return buf.toString();
    }

}
