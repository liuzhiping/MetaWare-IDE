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


import java.math.BigInteger;
import java.util.Arrays;


/**
 * A simple structure for describing the value of a register. It contains the ID of the register and its length. It also
 * contains fields to identify if the register is treated as a word, double-word, or a sequence of bytes, etc.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 * <P> tag "new" means that this class doesn't need to exist if invoking older debugger engines.
 * @new
 */
public class RegisterContent {

    private int id;

    private long value; // value if a scalar that is 8-bytes or less in size.

    private int length; // lenght of the register in bytes.
    
    private static final String INVALID_CONTENT = "--------";

    /**
     * The content of the register or <code>null</code> if "value" is to be used. May be an instance of
     * byte/short/int/long array, or a String if the value is "special";
     */
    private Object content; // null if "value" is to be used.

    public RegisterContent() {
        setSpecial(-1, "???");
    }

    public RegisterContent(int regID, long value, int length) {
        set(regID, value, length);
    }

    public RegisterContent(int regID, byte[] content) {
        set(regID, content);
    }

    public RegisterContent(int regID, short[] content) {
        set(regID, content);
    }

    public RegisterContent(int regID, int[] content) {
        set(regID, content);
    }

    public RegisterContent(int regID, long[] content) {
        set(regID, content);
    }

    public RegisterContent(int regID, String specialValue) {
        setSpecial(regID, specialValue);
    }
    
    /**
     * Create a content to denote an invalid register.
     * @nojni
     */
    public static RegisterContent newInvalid(int regID, int len){
        RegisterContent r = new RegisterContent();
        r.setInvalid(regID,len);
        return r;
    }

    /**
     * Return a clone.
     * @return a clone of this object.
     * @nojni
     */
    @Override
    public RegisterContent clone () {
        RegisterContent newOne = new RegisterContent();
        newOne.id = id;
        newOne.length = length;
        newOne.value = value;
        newOne.content = content;
        return newOne;
    }

    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @nojni
     */
    @Override
    public String toString () {
        return "Reg " + getRegister() + "=" + toString(Format.HEXADECIMAL);
    }

    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @return hash code
     * @nojni
     */
    @Override
    public int hashCode () {
        return (id << 8) ^ (int) value;
    }

    private static boolean compareEqual (Object a, Object b) {
        if (a == null)
            return b == null;
        if (a.equals(b))
            return true;
        if (a instanceof int[]) {
            if (b instanceof int[]) {
                return Arrays.equals((int[]) a, (int[]) b);
            }
            return false;
        }
        if (a instanceof byte[]) {
            if (b instanceof byte[]) {
                return Arrays.equals((byte[]) a, (byte[]) b);
            }
            return false;
        }
        if (a instanceof short[]) {
            if (b instanceof short[]) {
                return Arrays.equals((short[]) a, (short[]) b);
            }
            return false;
        }
        if (a instanceof long[]) {
            if (b instanceof long[]) {
                return Arrays.equals((long[]) a, (long[]) b);
            }
            return false;
        }
        return false;
    }

    /**
     * {@inheritDoc} Implementation of overridden (or abstract) method.
     * @param obj
     * @return true if this object is equal to the argument.
     * @nojni
     */
    @Override
    public boolean equals (Object obj) {
        if (!(obj instanceof RegisterContent))
            return false;
        RegisterContent r = (RegisterContent) obj;
        if (r.getRegister() != getRegister())
            return false;
        if (r.getValue() != getValue())
            return false;
        return compareEqual(r.getValueAsObject(), getValueAsObject());
    }

    public void set (int regID, long value, int length) {
        if (length > 8 || length <= 0)
            throw new IllegalArgumentException("Bad length");
        this.id = regID;
        this.value = value;
        this.length = length;
        this.content = null;
    }
    
    public void setInvalid(int regID, int length){
        this.id = regID;
        this.value = 0xDeadBeef;
        this.content = INVALID_CONTENT;
        this.length = length;
    }

    public void set (int regID, byte[] content) {
        if (content == null || content.length == 0)
            throw new IllegalArgumentException("Bad byte content");
        length = content.length;
        value = content[0] & 0xFF;
        this.content = content;
    }

    public void set (int regID, short[] content) {
        if (content == null || content.length == 0)
            throw new IllegalArgumentException("Bad short content");
        this.id = regID;
        length = content.length * 2;
        this.content = content;
        value = content[0] & 0xFFFF;
    }

    public void set (int regID, long[] content) {
        if (content == null || content.length == 0)
            throw new IllegalArgumentException("Bad long content");
        this.id = regID;
        length = content.length * 8;
        this.content = content;
        value = content[0];
    }

    public void set (int regID, int[] content) {
        if (content == null || content.length == 0)
            throw new IllegalArgumentException("Bad word content");
        this.id = regID;
        length = content.length * 4;
        this.content = content;
        value = content[0] & 0xFFFFFFFFL;
    }
    
    /**
     * Return whether or not the register contents are invalid.
     * To avoid a boolean "valid" field, we denote an invalid contents by setting
     * content to "INVALID_CONTENT" and value to 0xdeadbeef.
     * @return false if the register content is invalid.
     */
    public boolean isValid(){
        return content != INVALID_CONTENT || value != 0xDEADBEEF;
    }

    /**
     * Set value given its ascii representation in a particular format.
     * @param regID the register ID.
     * @param value the value of the register.
     * @param length the size of the register in bytes.
     * @param format the format that the value presumably conforms to.
     * @exception IllegalArgumentException if invalid length or unrecognized format.
     * @exception NumberFormatException if format of the string is invalid.
     * @nojni
     */
    public void set (int regID, String value, int length, Format format) throws IllegalArgumentException,
        NumberFormatException {
        // For flexibility, allow C-style hex in any format.
        if (length == 0 || length > 8)
            throw new IllegalArgumentException("Size is inappropriate: " + length);
        if (value.startsWith("0x") || value.startsWith("0X")) {
            value = value.substring(2);
            set(regID, Long.parseLong(value, 16), length);
            return;
        }
        switch (format) {
            case HEXADECIMAL:
                set(regID, Long.parseLong(value, 16), length);
                break;
            case SIGNED_DECIMAL:
            case UNSIGNED_DECIMAL:
                set(regID, Long.parseLong(value), length);
                break;
            case FLOAT:
            case DOUBLE:
            case EXTENDED:
                if (length <= 4) {
                    set(regID, Float.floatToRawIntBits(Float.parseFloat(value)), length);
                }
                else {
                    set(regID, Double.doubleToRawLongBits(Double.parseDouble(value)), length);
                }
                break;
            case OCTAL:
                set(regID, Long.parseLong(value.replace("_", ""), 8), length);
                break;
            case BINARY:
                set(regID, Long.parseLong(value.replace("_", ""), 2), length);
                break;
            case FRACTIONAL_31: {
                double d = Double.parseDouble(value);
                if (Math.abs(d) >= 1.0)
                    throw new IllegalArgumentException(
                        "Absolute value must be between 0 (inclusive) and 1.0(exclusive)");
                if (length <= 2) {
                    d = d * (1 << 15);
                }
                else if (length <= 4) {
                    d = d * (1 << 31);
                }
                else {
                    d = d * (1L << 63);
                }
                set(regID, (long) d, length);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown format");

        }
    }

    /**
     * Set to special value
     * @param id register id
     * @param value what to be displayed for special value.
     */
    public void setSpecial (int id, String value) {
        this.id = id;
        length = 0;
        content = value;
    }

    /**
     * Return the register number for which this value applies.
     * @return the register number for which this value applies.
     */
    public int getRegister () {
        return id;
    }

    /**
     * Return length of the register in bytes.
     * @return the length of the register in bytes.
     * @nojni
     */
    public int getLength () {
        return length;
    }

    public boolean isSpecial () {
        return content instanceof String && isValid();
    }

    /**
     * @return special value if this is special, otherwise null.
     * @nojni
     */
    public String getSpecialValue () {
        if (content instanceof String)
            return (String) content;
        return null;
    }

    /**
     * @return the unit size of the register.
     * @nojni
     */
    public int getUnitSize () {
        if (content == null)
            return length;
        if (content instanceof byte[])
            return 1;
        if (content instanceof short[])
            return 2;
        if (content instanceof int[])
            return 4;
        if (content instanceof long[])
            return 8;
        return 1; // not 0; we could get divide-by-zero error from caller.
    }

    /**
     * Return whether or not this is a scalar value.
     * @return whether or not this is a scalar value.
     * @nojni
     */
    public boolean isScalar () {
        return content == null || getUnitSize() == getLength();
    }

    /**
     * Return whether or not this is an aggregate value.
     * @return whether or not this is an aggregate value.
     * @nojni
     */
    public boolean isAggregate () {
        return content != null && !isSpecial() && getLength() > getUnitSize();
    }

    /**
     * Return the value of the register. If the register is an aggregate, it returns the first element value.
     * @return the value of the register or the value of the first element if its an aggregate.
     * @nojni
     */
    public long getValue () {
        return value;
    }

    /**
     * Return the value of the register as a general object.
     * @return The value of the register as a general object.
     * @nojni
     */

    public Object getValueAsObject () {
        if (content != null)
            return content;
        if (length == 4)
            return new Integer((int) value);
        if (length == 8)
            return new Long(value);
        if (length == 1)
            return new Byte((byte) value);
        if (length == 2)
            return new Short((short) value);
        return "??BAD_LENGTH???";
    }

    private static final BigInteger TWO_TO_64_POWER = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE).multiply(
        BigInteger.valueOf(2));

    /**
     * Convert integer to string presentation for given radix. If field length is specified, zero-pad.
     * @param v the integer to convert.
     * @param radix the radix.
     * @param fieldLength field length to zero pad to, or 0.
     * @return string representation.
     */
    private static String convert (long v, int radix, int fieldLength) {
        String s;
        if (v >= 0) {
            s = Long.toString(v, radix);
        }
        else {
            // To get unsigned, we add 2**64 which is (2**63)*2
            BigInteger b = BigInteger.valueOf(v);
            b = b.add(TWO_TO_64_POWER);
            s = b.toString(radix);
        }

        if (fieldLength > s.length()) {
            StringBuilder buf = new StringBuilder(fieldLength);
            for (int i = s.length(); i < fieldLength; i++) {
                buf.append('0');
            }
            buf.append(s);
            s = buf.toString();
        }
        return s;
    }

    /**
     * Convert an integer into a string representation according to a format.
     * <P>
     * NOTE: we will undoubtedly move this a public place at some point.
     * @param v the value to be converted.
     * @param format the format.
     * @return string representation
     * @nojni
     */
    private static String convertToString (long v, Format format, int length) {
        if (length <= 4) v = v & 0xFFFFFFFFL;
        switch (format) {
            default:
            case HEXADECIMAL: {
                return convert(v, 16, length * 2);
            }
            case OCTAL: {
                return convert(v, 8, (length*8+2)/3);
            }
            case BINARY: {
                return convert(v, 2, length * 8);
            }
            case SIGNED_DECIMAL: {
                if (length == 1)
                    v = (byte) v;
                else if (length == 2)
                    v = (short) v;
                else if (length == 4)
                    v = (int) v;
                return Long.toString(v);
            }
            case UNSIGNED_DECIMAL: {
                if (length == 1)
                    v = v & 0xFF;
                else if (length == 2)
                    v = v & 0xFFFF;
                else if (length == 4)
                    v = v & 0xFFFFFFFFL;
                if (v >= 0)
                    return Long.toString(v);
                long u = v / -10;
                return Long.toString(u) + Long.toString(v % -10);
            }
            case FLOAT:
            case DOUBLE:
            case EXTENDED: {
                if (length == 4)
                    return String.format("%.6e",Float.intBitsToFloat((int) v));
                if (length == 8)
                    return Double.toString(Double.longBitsToDouble(v));
                return convert(v, 16, length * 2); // can't handle extended floating point.
            }

            case FRACTIONAL_15: {
                float f = (float) (short) v / (float) (1 << 15);
                return Float.toString(f);
            }
            case FRACTIONAL_31: {
                double f = (double) (int) v / (double) (1L << 31);
                return String.format("%.8f", f);
            }
            case FRACTIONAL_9_31: {
                double f = (double) v / (double)(1L << 31);
                return Double.toString(f);
            }

        }
    }

    /**
     * Convert to string representation for display.
     * @param format format to use.
     * @return the string representation for display.
     * @nojni
     */
    public String toString (Format format) {
        if (!isValid()){
            if (format == Format.HEXADECIMAL){
                return "----------------".substring(0,Math.min(16,getLength()*2));
            }
            else return "------";
        }
        if (isSpecial())
            return getSpecialValue();
        if (isScalar()) {
            return convertToString(getValue(), format, getLength());
        }
        StringBuilder buf = new StringBuilder();
        switch (getUnitSize()) {
            case 1: {
                byte[] bytes = (byte[]) getValueAsObject();
                for (byte b : bytes) {
                    if (buf.length() > 0)
                        buf.append(' ');
                    buf.append(convertToString(b * 0xFF, format, 1));
                }
                break;
            }
            case 2: {
                short[] shorts = (short[]) getValueAsObject();
                for (short s : shorts) {
                    if (buf.length() > 0)
                        buf.append(' ');
                    buf.append(convertToString(s & 0xFFFF, format, 2));
                }
                break;
            }
            case 4: {
                int[] ints = (int[]) getValueAsObject();
                for (int i : ints) {
                    if (buf.length() > 0)
                        buf.append(' ');
                    buf.append(convertToString(i & 0xFFFFFFFF, format, 4));
                }
                break;
            }
            case 8: {
                long[] longs = (long[]) getValueAsObject();
                for (long l : longs) {
                    if (buf.length() > 0)
                        buf.append(' ');
                    buf.append(convertToString(l, format, 8));
                }
                break;
            }
            default:
                buf.append("BADUNITSIZE:" + getUnitSize());
                break;
        }
        return buf.toString();
    }
}
