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
package com.arc.seecode.internal.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.arc.seecode.engine.AssemblyRecord;
import com.arc.seecode.engine.Variable;
import com.arc.seecode.engine.type.defaults.TypeFactory;

/**
 * Test each of the serialization methods.
 * 
 * @author David Pickens
 */
public class Test {

    public static void main(String[] args) {
        testInt();
        testBoolean();
        testString();
        testType();
        testVariable();
        testValue();
        testLocation();
        testAssemblyRecord();
        testVoid();
        testIntArray();
        testByteArray();
        testLocationArray();
        testVariableArray();
        testAssemblyRecordArray();
    }

    /**
     *  
     */
    private static void testAssemblyRecordArray() {
        boolean failed = false;
        try {
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(ba);
            AssemblyRecord sample = new AssemblyRecord();
            sample.setOpcode("add");
            sample.setOperands("r1,r2,r3");
            sample.setHex("ab cd ef");
            sample.setAddress(0xDeadBeef);
            ISerializer s = AssemblyRecordSerializer.getInstance();
            s.serialize(sample, out);

            ByteArrayInputStream is = new ByteArrayInputStream(ba.toByteArray());
            DataInputStream in = new DataInputStream(is);
            AssemblyRecord result = (AssemblyRecord) s.deserialize(in);
            if (!result.equals(sample)){
                    failure("AssemblyRecordSerialize");
                    failed = true;
            }
        } catch (Exception e) {
            failure("AssemblyRecordSerialize test: Exception:" + e);
            failed = true;
        }
        if (!failed) {
            System.out.println("testAssemblyRecord Passed");
        }

    }

    /**
     *  
     */
    private static void testVariableArray() {
        boolean failed = false;
        try {
//            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            //DataOutputStream out = new DataOutputStream(ba);
            Variable sample = new Variable();
            sample.setName("Variable");
            sample.setActualName("ActualName");
            sample.setValue("0xDeadBeef");
            sample.setType(new TypeFactory().createInteger("int",4,false));
            sample.setRegister(14);
            sample.setAddress(0xDeadBeef);
//            ISerializer s = null;//VariableSerializer.getInstance();
            //s.serialize(sample, out);

//            ByteArrayInputStream is = new ByteArrayInputStream(ba.toByteArray());
//            DataInputStream in = new DataInputStream(is);
//            Variable result = (Variable) s.deserialize(in);
//            if (!result.equals(sample)){
//                    failure("VariableSerialize");
//                    failed = true;
//            }
        } catch (Exception e) {
            failure("VariableSerialize test: Exception:" + e);
            failed = true;
        }
        if (!failed) {
            System.out.println("testVariable Passed");
        }
    }

    /**
     *  
     */
    private static void testLocationArray() {
        // TODO Auto-generated method stub

    }

    /**
     *  
     */
    private static void testByteArray() {
        boolean failed = false;
        try {
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(ba);
            byte samples[] = new byte[] { 1, 3, 127, 123, 121, 0, -3, -57};
            ISerializer s = ByteArraySerializer.getInstance();
            s.serialize(samples, out);

            ByteArrayInputStream is = new ByteArrayInputStream(ba.toByteArray());
            DataInputStream in = new DataInputStream(is);
            byte result[] = (byte[]) s.deserialize(in);
            for (int i = 0; i < samples.length; i++) {
                int v = result[i];
                if (v != samples[i]) {
                    failure("ByteArraySerialize, test " + i);
                    failed = true;
                }
            }
        } catch (Exception e) {
            failure("ByteArraySerialize test: Exception:" + e);
            failed = true;
        }
        if (!failed) {
            System.out.println("testByteArray Passed");
        }

    }

    /**
     *  
     */
    private static void testIntArray() {
        boolean failed = false;
        try {
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(ba);
            int samples[] = new int[] { 1, 0xdeafbeef, 0xcafebabe, -1, 0,
                    12345678};
            ISerializer s = IntArraySerializer.getInstance();
            s.serialize(samples, out);

            ByteArrayInputStream is = new ByteArrayInputStream(ba.toByteArray());
            DataInputStream in = new DataInputStream(is);
            int result[] = (int[]) s.deserialize(in);
            for (int i = 0; i < samples.length; i++) {
                int v = result[i];
                if (v != samples[i]) {
                    failure("IntArraySerialize, test " + i);
                    failed = true;
                }
            }
        } catch (Exception e) {
            failure("IntArraySerialize test: Exception:" + e);
            failed = true;
        }
        if (!failed) {
            System.out.println("testIntArray Passed");
        }

    }

    /**
     *  
     */
    private static void testVoid() {
        // TODO Auto-generated method stub

    }

    /**
     *  
     */
    private static void testAssemblyRecord() {
        // TODO Auto-generated method stub

    }

    /**
     *  
     */
    private static void testLocation() {
        // TODO Auto-generated method stub

    }

    /**
     *  
     */
    private static void testValue() {
        // TODO Auto-generated method stub

    }

    /**
     *  
     */
    private static void testVariable() {
        // TODO Auto-generated method stub

    }

    /**
     *  
     */
    private static void testType() {
        // TODO Auto-generated method stub

    }

    /**
     *  
     */
    private static void testString() {
        boolean failed = false;
        try {
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(ba);
            String samples[] = new String[] { "One", "Two", "Three", "Four",
                    null, "Six"};
            ISerializer s = StringSerializer.getInstance();
            for (int i = 0; i < samples.length; i++) {
                s.serialize(samples[i], out);
            }
            ByteArrayInputStream is = new ByteArrayInputStream(ba.toByteArray());
            DataInputStream in = new DataInputStream(is);
            for (int i = 0; i < samples.length; i++) {
                String v = (String) s.deserialize(in);
                if (v == null && samples[i] != null || v != null
                        && !v.equals(samples[i])) {
                    failure("StringSerialize, test " + i + "; Expected \""
                            + samples[i] + "\" got: \"" + v + "\"");

                    failed = true;
                }
            }
        } catch (Exception e) {
            failure("StringSerialize test: Exception:" + e);
            failed = true;
        }
        if (!failed) {
            System.out.println("testString Passed");
        }
    }

    /**
     *  
     */
    private static void testBoolean() {
        boolean failed = false;
        try {
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(ba);
            boolean samples[] = new boolean[] { true, false, false, true, true,
                    false};
            ISerializer s = BooleanSerializer.getInstance();
            for (int i = 0; i < samples.length; i++) {
                s.serialize(new Boolean(samples[i]), out);
            }
            ByteArrayInputStream is = new ByteArrayInputStream(ba.toByteArray());
            DataInputStream in = new DataInputStream(is);
            for (int i = 0; i < samples.length; i++) {
                boolean v = ((Boolean) s.deserialize(in)).booleanValue();
                if (v != samples[i]) {
                    failure("BooleanSerialize, test " + i);
                    failed = true;
                }
            }
        } catch (Exception e) {
            failure("BooleanSerialize test: Exception:" + e);
            failed = true;
        }
        if (!failed) {
            System.out.println("testBoolean Passed");
        }
    }

    static void failure(String msg) {
        System.err.println("FAILURE: " + msg);

    }

    /**
     *  
     */
    private static void testInt() {
        boolean failed = false;
        try {
            ByteArrayOutputStream ba = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(ba);
            int samples[] = new int[] { 1, 0xdeafbeef, 0xcafebabe, -1, 0,
                    12345678};
            ISerializer s = IntSerializer.getInstance();
            for (int i = 0; i < samples.length; i++) {
                s.serialize(new Integer(samples[i]), out);
            }
            ByteArrayInputStream is = new ByteArrayInputStream(ba.toByteArray());
            DataInputStream in = new DataInputStream(is);
            for (int i = 0; i < samples.length; i++) {
                int v = ((Integer) s.deserialize(in)).intValue();
                if (v != samples[i]) {
                    failure("IntSerialize, test " + i);
                    failed = true;
                }
            }
        } catch (Exception e) {
            failure("IntSerialize test: Exception:" + e);
            failed = true;
        }
        if (!failed) {
            System.out.println("testInt Passed");
        }

    }
}
