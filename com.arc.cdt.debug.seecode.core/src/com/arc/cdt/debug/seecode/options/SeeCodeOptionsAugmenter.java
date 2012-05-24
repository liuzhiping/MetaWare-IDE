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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;

import com.arc.mw.util.Cast;

/**
 * Concrete implementation of the interface for augmenting the SeeCode swahili
 * arguments (i.e. those passed to "scac") with compiler options (e.g.,
 * "-arc700", "-Xnorm", etc.)
 * <P>
 * The mapping is based on an XML file.
 * 
 * @author David Pickens
 */
class SeeCodeOptionsAugmenter implements ISeeCodeOptionsAugmenter {

    static class OptionEnum {
        /**
         * 
         * @param option
         *            the enumerated tool option that is being referenced.
         * @param enumID
         *            the enumID ID that this object wraps.
         * @param seecodeArg
         *            the seecode argument to be specified if this enumID value is
         *            set.
         * @param propertyValue
         *            the value to be assigned to a parent property value if
         *            this enumID is set.
         * @param booleanProperty
         *            if not null, a boolean property that is to be set to true
         *            if this enumID is set; false otherwise.
         * @param namePropertyName
         *            if not null, the name of a property that is to be set to
         *            the name of the enumID id.
         */
        OptionEnum(IOption option, String enumID, String seecodeArg,
                String propertyValue, String booleanProperty,
                String namePropertyName, boolean isDefault) {
            this.isDefault = isDefault;
            this.enumID = enumID;
            this.option = option;
            this.seecodeArg = seecodeArg;
            this.propertyValue = propertyValue;
            this.booleanProperty = booleanProperty;
            this.nameProperty = namePropertyName;
        }

        String enumID;

        IOption option;

        String seecodeArg;

        String propertyValue;

        String booleanProperty;

        String nameProperty;
        
        boolean isDefault;
    }

    class SeeCodeOption {
    	/**
    	 * 
    	 * @param optionName
    	 * @param seecodeArg
    	 * @param negated
    	 * @param propertyName
    	 * @param alternate
    	 * @param trueValue if not null, it is the value for the property if it
    	 * happens to be a boolean true and is enabled. Default is "true".
    	 * @param falseValue if not null, it is the value for the property if it
    	 * happens to be boolean false and is enabled. Default is "false".
    	 */
        SeeCodeOption(String optionName, String seecodeArg, boolean negated,
                String propertyName,
                String alternate,
                String trueValue,
                String falseValue) {
            this.optionName = optionName;
            this.seecodeArg = seecodeArg;
            this.negated = negated;
            this.propertyName = propertyName;
            this.alternatePropertyName = alternate;
            this.trueValue = trueValue;
            this.falseValue = falseValue;
        }

        String seecodeArg;
        
        String alternatePropertyName;
        
        // We can't reference IOption object directly because
        // it may change from Option to OptionReference when
        // a value is assigned to it (e.g. predicate change).
        IOption getOption(){
            return lookupOption(optionName);
        }

        private String optionName;
        String propertyName;

        boolean negated;
        String trueValue;
        String falseValue;
    }
    
    class DefaultSpec{
        public DefaultSpec(String p, String v, String o, String ov){
            property = p;
            value = v;
            option = o;
            optionValue = ov;
        }
        String property;
        String value;
        String option;
        String optionValue;
    }
    
    class SetIf {
        public SetIf(String property, String value, Condition cond){
            this.property = property;
            this.value = value;
            this.condition = cond;
        }
        String property;
        String value;
        Condition condition;
    }

    /**
     * Map whose key is a SeeCode argument (e.g. "-Xbs"); the value is a
     * corresponding compiler tool option (e.g., "barrel_shifter").
     */
    private Map<String,SeeCodeOption> mFromSeeCodeArgMap = new HashMap<String,SeeCodeOption>();

    /**
     * Map whose key is a SeeCode argument (e.g. "-a7"); the value is a
     * {@link OptionEnum}instance: the option is enumerated (e.g."target") and
     * the enumID is a corresponding Enum (e.g. "ARC 700").
     */
    private Map<String,OptionEnum> mFromSeeCodeEnumMap = new HashMap<String,OptionEnum>();

    /**
     * Map whose key is a (guihili) property (e.g., "ARC_barrel_shifter") andthe
     * value is the corresponding compiler tool option.
     */
    private Map<String,SeeCodeOption> mFromPropertyMap = new HashMap<String,SeeCodeOption>();

    /**
     * Relavent compiler options that have a corresponding SeeCode option.
     */
    private List<SeeCodeOption> mOptions = new ArrayList<SeeCodeOption>(50);

    /**
     * Given an option enumID ID, return the corresponding {@link OptionEnum}
     * instance.
     */
    private Map<String,OptionEnum> mEnumIdToOptionEnumMap = new HashMap<String,OptionEnum>();

    /**
     * Properties set by &lt;property&gt; node.
     */
    private Map<String,String> mSetPropertyMap = new HashMap<String,String>();
    
    private List<DefaultSpec> mDefaultList = new ArrayList<DefaultSpec>();
    private List<SetIf> mSetIfList = new ArrayList<SetIf>();
    
    /**
     * Properties that are only set as initial defaults, but permit user
     * to change subsequently.
     */
    private Set<String> mSetAsDefaultOnly = new HashSet<String>();
    
    private IConfiguration mConfig;

    /**
     * <pre>
     * 
     *  
     *   
     *    
     *         options
     *             option name=&quot;...&quot; property=&quot;...&quot; [seecode=&quot;...&quot;] [negate=&quot;true&quot;]
     *                 [enumID name=&quot;...&quot; seecode=&quot;...&quot; propertyValue=&quot;...&quot;]
     *                 ...
     *             ...
     *                 
     *     
     *  
     * </pre>
     * 
     * @param root
     *            the XML root element that this object represents.
     * @param config
     *            the build configuration from which to extract options.
     * @throws ConfigurationException
     *             if unknown compiler options referenced, etc.
     * @throws DocumentException
     *             if bad XML.
     */
     SeeCodeOptionsAugmenter(Element root, IConfiguration config)
            throws ConfigurationException, DocumentException {
        if (!root.getName().equalsIgnoreCase("options")) {
            throw new DocumentException("Unrecognized root node "
                    + root.getName());
        }
        mConfig = config;
        
        List<Element> elements = Cast.toType(root.elements());
        for (Element e: elements){
            if (e.getName().equalsIgnoreCase("option")) {
                handleOptionElement(e);
            } else if (e.getName().equalsIgnoreCase("property")) {
                handlePropertyElement(e);
            } else if (e.getName().equalsIgnoreCase("default")){
                handleDefaultElement(e);
            } else if (e.getName().equalsIgnoreCase("setif")){
                handleSetIf(e);
            } else
                throw new DocumentException("Unknown element: " + e.getName());
        }
    }
     
    private void handleSetIf(Element e) {
        // <setIf property=...  value=...> conditions </setIf>
        String property = e.attributeValue("property");
        String value = e.attributeValue("value");
        if (property == null || property.length() == 0) throw new IllegalStateException("Missing property in setIf");
        if (value == null) throw new IllegalStateException("Value is null in setIf");
        @SuppressWarnings("unchecked")
        List<Element> kids = e.elements();
        if (kids.size() != 1){
            throw new IllegalStateException("setIf must have one and only one child element");
        }
        Condition cond = Condition.parse(kids.get(0));
        mSetIfList.add(new SetIf(property,value,cond));
    }

    /**
     * Process the "option" element.
     * 
     * @param e
     *            the "option" element.
     * @throws DocumentException
     * @throws ConfigurationException
     */
    private void handleOptionElement(Element e)
            throws DocumentException, ConfigurationException {
        List<Attribute> attrs = Cast.toType(e.attributes());
        String optionName = null;
        String propertyName = null;
        String namePropertyName = null;
        String seecodeArg = null;
        String negate = null;
        String alternatePropertyName = null;
        String trueValue = null;
        String falseValue = null;
        boolean setAsDefaultOnly = false;
        List<Element> enums = Cast.toType(e.elements());
        for (Attribute a: attrs){
            String aname = a.getName().toLowerCase();
            String value = a.getValue();
            if (aname.equals("name"))
                optionName = value;
            else if (aname.equals("property"))
                propertyName = value;
            else if (aname.equals("seecode"))
                seecodeArg = value;
            else if (aname.equals("negate"))
                negate = value;
            else if (aname.equalsIgnoreCase("nameProperty"))
                namePropertyName = value;
            else if (aname.equalsIgnoreCase("defaultOnly")){
                setAsDefaultOnly = !(value.equals("0") || value.equalsIgnoreCase("false"));
            }
            else if (aname.equalsIgnoreCase("alternate")){
                alternatePropertyName = value;
            }
            else if (aname.equalsIgnoreCase("trueValue")){
            	trueValue = value;
            }
            else if (aname.equalsIgnoreCase("falseValue")){
            	falseValue = value;
            }
            else
                throw new DocumentException("Unknown attribute name: " + aname);
        }
        if (optionName == null) {
            throw new DocumentException("\"name\" missing in \"option\" node");
        }
        IOption option = lookupOption(optionName);
        if (option == null) {
            throw new ConfigurationException("Unrecognized option name: "
                    + optionName);
        }
        if (seecodeArg == null && enums.size() == 0) {
            seecodeArg = option.getCommand();
            //                    if (seecodeArg==null)
            //                        throw new DocumentException("Option \"" + optionName +
            // "\" needs 'seecode' attribute");
        }
        if (enums.size() == 0 && namePropertyName != null) {
            throw new DocumentException(
                    "nameProperty only applies to enumID options");
        }
        boolean first = true;
        for (Element enumElement: enums){
            if (!enumElement.getName().equalsIgnoreCase("enum")) {
                throw new DocumentException("Unrecognized element \""
                        + enumElement.getName() + "\" under option \"" + optionName
                        + "\"");
            }
            String name = null;
            String scArg = null;
            String propertyValue = null;
            String booleanProperty = null;
            List<Attribute> eattrs = Cast.toType(enumElement.attributes());
            boolean isDefault = first; // assume first is default
            first = false;
            for (Attribute a: eattrs){
                String aname = a.getName();
                String value = a.getValue();
                if (aname.equals("name")) {
                    name = value;
                } else if (aname.equals("seecode"))
                    scArg = value;
                else if (aname.equalsIgnoreCase("propertyvalue"))
                    propertyValue = value;
                else if (aname.equalsIgnoreCase("property"))
                    booleanProperty = value;
                else
                    throw new DocumentException("Unknown enumID attribute: "
                            + aname);
            }
            try {
                if (name == null || option.getEnumCommand(name) == null) {
                    throw new DocumentException("Unrecognized enumID id: " + name);
                }
                if (scArg == null && propertyValue == null) {
                    scArg = option.getEnumCommand(name);
                    if (scArg == null)
                        throw new DocumentException("Enum \"" + name
                                + "\" needs 'seecode' attribute");
                }
            } catch (BuildException e1) {
                throw new ConfigurationException(e1.getMessage(),e1);
            } 
            OptionEnum oe = new OptionEnum(option, name, scArg, propertyValue,
                    booleanProperty, namePropertyName,isDefault);
            mEnumIdToOptionEnumMap.put(name, oe);
            mFromSeeCodeEnumMap.put(scArg, oe);
        }

        SeeCodeOption sco = new SeeCodeOption(optionName, seecodeArg, "true"
                .equalsIgnoreCase(negate)
                || "1".equals(negate),propertyName,alternatePropertyName,trueValue,falseValue);
        mOptions.add(sco);
        if (seecodeArg != null) {
            mFromSeeCodeArgMap.put(seecodeArg, sco);
        }
        if (propertyName != null) {
            mFromPropertyMap.put(propertyName, sco);
            if (setAsDefaultOnly)
                this.mSetAsDefaultOnly.add(propertyName);
        }
    }

    /**
     * Process the "property" element.
     * @param e
     *            the "property" element.
     * @throws DocumentException
     */
    private void handlePropertyElement(Element e)
            throws DocumentException {
        List<Attribute> attrs = Cast.toType(e.attributes());
        String propertyName = null;
        String propertyValue = null;
        for (Attribute a: attrs) {
            String aname = a.getName().toLowerCase();
            String value = a.getValue();
            if (aname.equals("name")) {
                propertyName = value;
            } else if (aname.equals("value")) {
                propertyValue = value;
            } else
                throw new DocumentException(
                        "Unknown attribute under 'property': " + aname);
        }
        if (propertyName == null || propertyValue == null)
            throw new DocumentException(
                    "'name' or 'value' attributes missing for element 'property'");
        mSetPropertyMap.put(propertyName, propertyValue);
    }
    
    private void handleDefaultElement(Element e) throws DocumentException{
        @SuppressWarnings("unchecked")
        List<Attribute> attrs = e.attributes();
        String property = null;
        String defaultValue = null;
        String option = null;
        String optionValue = null;
        for (Attribute a: attrs) {
            String aname = a.getName().toLowerCase();
            String value = a.getValue();
            if (aname.equals("property")){
                property = value;
            }
            else if (aname.equals("value")){
                defaultValue = value;
            }
            else if (aname.equals("option")){
                option = value;
            }
            else if (aname.equals("optionvalue")){
                optionValue = value;
            }
        }
        if (property == null || defaultValue == null || option == null || optionValue == null){
            throw new DocumentException("Attributes on Default element missing");
        }
        mDefaultList.add(new DefaultSpec(property,defaultValue,option,optionValue));
    }

    /**
     * Lookup a tool option and return it if it exists. Returns
     * <code>null</code. otherwise.
     * @param name id of the option to look up.
     * @parma config the configuration from which to extract it.
     * @return the corresponding option or <code>null</code>.
     */
    private  IOption lookupOption(String name) {
        return OptionLookup.lookupOption(name,mConfig);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.cdt.debug.seecode.options.ISeeCodeOptionAugmenter#augmentProperties(java.util.Map)
     */
    @Override
    public void augmentProperties(Map<String,Object> propertyMap, PropState state) {
        //Must copy the keySet to avoid "ConcurrentModificationException"
        for (Map.Entry<String,SeeCodeOption> entry: mFromPropertyMap.entrySet()){
            SeeCodeOption sco = entry.getValue();
            String prop = entry.getKey();
            IOption option = sco.getOption();
            if (option != null && (state == PropState.DEFAULTS || !isDefaultOnly(prop))){
                setPropertyFromOption(propertyMap, prop, option,
                    sco.alternatePropertyName, sco.trueValue,sco.falseValue);
            }
        }
        for(String prop: new ArrayList<String>(propertyMap.keySet())) {
            SeeCodeOption sco =  mFromPropertyMap.get(prop);
            if (sco == null) {
                String value =  mSetPropertyMap.get(prop);
                if (value != null)
                    propertyMap.put(prop, value);
            }
        }
        // Hardcode changes to properties due to other conditions occuring.
        // (e.g.: ARC can't target ARCSIM if not ARCv2.
        if (state != PropState.UPDATING)
            for (SetIf spec: mSetIfList){
                if (spec.condition.isTrue(propertyMap, mConfig)) {
                    propertyMap.put(spec.property,spec.value);
                }
            }
    }

    private boolean isEnabled (IOption option) {
        return (option.getApplicabilityCalculator() == null
            || option.getApplicabilityCalculator().isOptionEnabled(mConfig, option.getOptionHolder(), option));
    }

    /**
     * 
     * @param prop
     * @return whether or not we inherit property value from build options
     * only as the initial default.
     */
    private boolean isDefaultOnly (String prop) {
        return mSetAsDefaultOnly.contains(prop);
    }

    /**
     * @param propertyMap the debugger's property map that we're augmenting from build properties.
     * @param prop the debugger's property being set.
     * @param option the corresponding build option.
     * @param alternative another property to be set, typically controlling
     * @param value the value to set the property to if it is boolean (default: "true")
     * gray-out in guihili
     */
    private void setPropertyFromOption(Map<String,Object> propertyMap, String prop,
            IOption option, String alternative, String valueTrue,String valueFalse) {
        try {
            switch (option.getValueType()) {
            case IOption.BOOLEAN: {
                boolean v = option.getBooleanValue() && isEnabled(option);
                // For now, don't turn something off if it is
                // controlled by an alternative name. If it isn't on,
                // then it may be turned on or off independently from
                // Launch config.
                // See ac_features.opt file to see why this is so.
                if (v || alternative == null) {
                    Object current = propertyMap.get(prop);
                    if (valueTrue != null){
                    	propertyMap.put(prop,v?valueTrue:valueFalse);
                    }
                    else
                    if (current != null) {
                        if (current instanceof Boolean){
                            propertyMap.put(prop,Boolean.valueOf(v));
                        }
                        else
                        if (current instanceof Integer){
                            propertyMap.put(prop,new Integer(v?1:0));
                        }
                        else
                        if (current.equals("1") || current.equals("0"))
                            propertyMap.put(prop, v ? "1" : "0");
                        else
                            propertyMap.put(prop, v ? "true" : "false");
    
                    } else
                        propertyMap.put(prop, v ? "1" : "0");
                }
                if (alternative != null) {
                	propertyMap.put(alternative,v? (valueTrue!=null?valueTrue:"1"):(valueFalse!=null?valueFalse:"0"));
                }
                break;
            }
            case IOption.STRING:
                propertyMap.put(prop, option.getStringValue());
                break;
            case IOption.ENUMERATED: {
                String enumID = option.getSelectedEnum();
                OptionEnum oe = mEnumIdToOptionEnumMap.get(enumID);
                //should always be non-null; we would have caught
                // it during construction otherwise.
                //
                // If we have an alternative name, then we only
                // set the property if its non-default. Otherwise, the
                // launch config can set it independently.
                if (oe != null && (alternative == null || !oe.isDefault)) {
                    if (prop != null && oe.propertyValue != null) {
                        propertyMap.put(prop, oe.propertyValue);
                    }
                    if (oe.booleanProperty != null) {
                        clearEnumeratedBooleanProperties(option, propertyMap);
                        propertyMap.put(oe.booleanProperty, "1");
                    }
                    // Can't enter new property, or our iterator will mess up
                    if (oe.nameProperty != null
                            && propertyMap.get(oe.nameProperty) != null) {
                        propertyMap.put(oe.nameProperty, option
                                .getEnumName(enumID));
                    }
                }
                if (oe != null && alternative != null && oe.propertyValue != null){
                    propertyMap.put(alternative,oe.propertyValue);
                }
            }
                break;
            default:
                //Shouldn't get here except if
                // user specifies an esoteric option by hand.
                break;
            }
        } catch (BuildException e) {
            e.printStackTrace();
        }
    }

    private void clearEnumeratedBooleanProperties(IOption enumeratedOption,
            Map<String,Object> propertyMap) {
        String[] enums = enumeratedOption.getApplicableValues();
        if (enums != null) {
            for (int i = 0; i < enums.length; i++) {
                OptionEnum oe =  mEnumIdToOptionEnumMap
                        .get(enums[i]);
                if (oe.booleanProperty != null)
                    propertyMap.put(oe.booleanProperty, "0");

            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.arc.cdt.debug.seecode.options.ISeeCodeOptionAugmenter#augmentArguments(java.lang.String[],
     *      org.eclipse.cdt.managedbuilder.core.IConfiguration)
     */
    @Override
    public String[] augmentArguments(String[] arguments) {
        arguments = filterOutInterestingArguments(arguments);
        return addArgumentsFromBuildConfiguration(arguments);
    }

    private String[] addArgumentsFromBuildConfiguration (String args[]) {
        ArrayList<String> list = new ArrayList<String>(args.length * 2 + 10);
        list.addAll(Arrays.asList(args));
        for (SeeCodeOption o : mOptions) {
            if (o.propertyName == null || !isDefaultOnly(o.propertyName))
                try {
                    IOption opt = o.getOption();
                    if (isEnabled(opt)) {
                        switch (opt.getValueType()) {
                        case IOption.BOOLEAN:
                            if (opt.getBooleanValue() != o.negated && o.seecodeArg != null) {
                                list.add(o.seecodeArg);
                            }
                            break;
                        case IOption.STRING: {
                            String v = opt.getStringValue();
                            if (v != null) {
                                if (o.seecodeArg.endsWith("="))
                                    list.add(o.seecodeArg + v);
                                else {
                                    list.add(o.seecodeArg);
                                    list.add(v);
                                }
                            }
                            break;
                        }
                        case IOption.ENUMERATED: {
                            String e = opt.getSelectedEnum();
                            OptionEnum oe = mEnumIdToOptionEnumMap.get(e);
                            if (oe != null) {
                                if (oe.seecodeArg != null && oe.seecodeArg.length() > 0)
                                    list.add(oe.seecodeArg);
                            }
                            else {
                                String a = opt.getEnumCommand(e);
                                if (a != null && a.length() > 0) list.add(a);
                            }
                            break;
                        }
                        }
                    }
                }
                catch (BuildException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }
        return list.toArray(new String[list.size()]);
    }
    
    private static Boolean getBooleanValueOf(Object v){
        if (v == null) return false;
        if (v instanceof Number){
            return ((Number)v).intValue() != 0?Boolean.TRUE:Boolean.FALSE;
        }
        if (v instanceof Boolean) return (Boolean)v;
        return false;
    }
    
    private boolean isDefaultSetting(SeeCodeOption sco){
        IOption option = sco.getOption();
        if (option != null){
            Object value = option.getValue();
            if (value != null && value.equals(option.getDefaultValue())) return true;
            if (value instanceof Boolean && value.equals(getBooleanValueOf(option.getDefaultValue()))) //cr98730
                return true;
            return false;
        }
        return true;
    }

    /**
     * Given a SeeCode argument list (i.e., one that will be passed to Swahili
     * processor "scac"), filter out all options that have a corresponding
     * compiler option (e.g. "-a7", "-Xbs"). The caller will presumably add them
     * back later in such a way as to reflect reality.
     * 
     * @param arguments
     *            tentative SeeCode argument list.
     * @return the argument list with compiler-related options filtered out.
     */
    private String[] filterOutInterestingArguments(String[] arguments) {
        ArrayList<String> list = new ArrayList<String>(arguments.length * 2);
        for (int i = 0; i < arguments.length; i++) {
            String a = arguments[i];
            if (a.startsWith("-")) {
                SeeCodeOption sco = mFromSeeCodeArgMap.get(a);
                // If this seecode argument corresponds to
                // a compiler option (e.g., "-a7") then remove
                // it. We'll add it back later.
                if (sco == null) {
                    // See if corresponds to an Enum ID
                    OptionEnum oi =  mFromSeeCodeEnumMap.get(a);
                    if (oi == null)
                        list.add(a);
                }
                else if (sco.propertyName != null && isDefaultOnly(sco.propertyName)
                    || sco.alternatePropertyName != null && isDefaultSetting(sco)){
                    list.add(a);
                }
            } else 
                list.add(a);
        }
        return list.toArray(new String[list.size()]);
    }

    @Override
    public void augmentDefaults (Map<String, Object> propertyMap) {
        for (DefaultSpec spec: mDefaultList){
            IOption option = lookupOption(spec.option);
            if (option == null) throw new IllegalStateException("Unknown option: " + spec.option);
            Object value = option.getValue();
            if (value != null) value = value.toString();
            if (spec.optionValue.startsWith("!")){
                if (!spec.optionValue.substring(1).equals(value)){
                    propertyMap.put(spec.property,spec.value);
                }
            }
            else
            if (spec.optionValue.equals(value)){
                propertyMap.put(spec.property,spec.value);
            }
        }
    }

}
