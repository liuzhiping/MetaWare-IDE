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
package com.arc.cdt.debug.seecode.options;

import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;


/**
 * @author pickensd
 * A conditional expression, as is referenced by the "setIf" node.
 */
abstract class Condition {
    public abstract boolean isTrue(Map<String,Object> map, IConfiguration config);
    
    static Condition parse(Element e){
        String name = e.getName().toLowerCase();
        if (name.equals("equals") || name.equals("notequals")) {
            String propName = e.attributeValue("property");
            String option = e.attributeValue("option");
            String value = e.attributeValue("value");
            if (value == null) throw new IllegalStateException("Value in SetIf condition is missing");
            if (propName != null){
                if (option != null) throw new IllegalStateException("Can't specify properyt and option in same SetIf condition");
                return PropertyEquals(propName,value,name.charAt(0) == 'n');
            }
            if (option == null) throw new IllegalStateException("Missing property or option in setIf \"equals\" condition");
            return OptionEquals(option,value,name.charAt(0) == 'n');
        }
        if (name.equals("and") || name.equals("or")){
            @SuppressWarnings("unchecked")
            List<Element> kids = e.elements();
            if (kids.size() < 2) throw new IllegalStateException("And/Or node in SetIf condition must have 2 or more kids");
            Condition cond = parse(kids.get(0));
            for (int i = 1; i < kids.size(); i++) {
                Condition cond2 = parse(kids.get(i));
                if (name.charAt(0) == 'a') cond = And(cond,cond2); else cond = Or(cond,cond2);
            }
            return cond;
        }
        if (name.equals("not")){
            @SuppressWarnings("unchecked")
            List<Element> kids = e.elements();
            if (kids.size() != 1) throw new IllegalStateException("Not node in SetIf condition must have 2 or more kids");
            Condition cond = parse(kids.get(0));
            return Not(cond);
        }
        throw new IllegalStateException("Unknown SetIf condition node: " + name);
    }
    
    static Condition PropertyEquals(String property, String value, boolean negate){
        return new PropertyEquals(property,value,negate);
    }
    static Condition OptionEquals(String optionName, String value, boolean negate){
        return new OptionEquals(optionName,value,negate);
    }
    static Condition And(Condition opd1, Condition opd2){
        return new And(opd1,opd2);
    }
    static Condition Or(Condition opd1, Condition opd2){
        return new Or(opd1,opd2);
    }
    static Condition Not(Condition opd){
        return new Not(opd);
    }
    
    private static class PropertyEquals extends Condition {
        private String property;
        private String value;
        private boolean negate;
        PropertyEquals(String property, String value, boolean negate){
            this.property = property;
            this.value = value;
            this.negate = negate;
        }
        @Override
        public boolean isTrue(Map<String,Object> map, IConfiguration config){
            Object v = map.get(property);
            if (v == null)
                return (value != null) == negate;
            if (value == null) return negate;
            if (value.equals(v.toString())) return !negate;
            return false;
        }
    }
    
    private static boolean computeBoolean(String v){
        if (v == null) return false;
        if (v.equals("0") || v.equalsIgnoreCase("false")) return false;
        return true;
    }

    private static class OptionEquals extends Condition {
        private String optionId;
        private String value;
        private boolean negate;
        OptionEquals(String option, String value, boolean negate){
            this.optionId = option;
            this.value = value;
            this.negate = negate;
        }
        @Override
        public boolean isTrue(Map<String,Object> map, IConfiguration config){
            IOption option = OptionLookup.lookupOption(optionId,config);
            if (option == null) throw new IllegalStateException("Unknown option: " + optionId);
            try {
                switch(option.getValueType()){
                    case IOption.BOOLEAN:
                        return option.getBooleanValue() == (computeBoolean(value) ^ negate);
                    case IOption.STRING: {
                        String v = option.getStringValue();
                        return value.equals(v) != negate;
                    }
                    case IOption.ENUMERATED: {
                        String e = option.getSelectedEnum();
                        return value.equals(e) != negate;
                    }
                }
            }
            catch (BuildException e) {
                return false;
            }
            return false;
        }  
    }
    
    private static class And extends Condition {
        private Condition opd1,opd2;
        And(Condition opd1, Condition opd2){
            if (opd1 == null || opd2 == null) throw new IllegalArgumentException("Null argument");
            this.opd1 = opd1;
            this.opd2 = opd2;
        }
        @Override
        public boolean isTrue(Map<String,Object> map, IConfiguration config){
            if (!opd1.isTrue(map,config)) return false;
            return opd2.isTrue(map,config);
        }
    }
    
    private static class Or extends Condition {
        private Condition opd1,opd2;
        Or(Condition opd1, Condition opd2){
            if (opd1 == null || opd2 == null) throw new IllegalArgumentException("Null argument");
            this.opd1 = opd1;
            this.opd2 = opd2;
        }
        @Override
        public boolean isTrue(Map<String,Object> map, IConfiguration config){
            if (opd1.isTrue(map,config)) return true;
            return opd2.isTrue(map,config);
        }
    }
    
    private static class Not extends Condition {
        private Condition opd;
        Not(Condition opd){
            if (opd == null) throw new IllegalArgumentException("Null argument");
            this.opd = opd;
        }
        @Override
        public boolean isTrue(Map<String,Object> map, IConfiguration config){
            return !opd.isTrue(map,config);
        }
    }
}
