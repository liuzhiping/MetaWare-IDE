/*
 * CDTUtil
 * $Revision$
 * $Date$
 *
 * CONFIDENTIAL AND PROPRIETARY INFORMATION.
 * Copyright 2008 ARC International (Unpublished).
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
package com.arc.cdt.testutil;

import junit.framework.Assert;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IOption;

import com.windowtester.runtime.WidgetSearchException;

/**
 * Utilities related to CDT data structure processing.
 * @todo davidp needs to add a class description.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */
public class CDTUtil {
    /**
     * Given a list of options, return one that correspond to a command-line
     * switch (e.g. "-Hansi").
     * @param options the list of options to search as returned from
     * @{link IToolOptionSetting#getCompilerOptions},etc.
     * @param switchName the name of the switch (e.g. "-Hansi").
     * @return the corresponding option or <code>null></code>.
     */
    public static IOption getOptionForSwitch(IOption[] options, String switchName){
        for (IOption option: options){
            if (switchName.equals(option.getCommand()) ||
                switchName.equals(option.getCommandFalse())){
                return option;
            }
            try {
                boolean endsInEQ = switchName.endsWith("=");
                if (option.getValueType() == IOption.ENUMERATED){
                    for (String name: option.getApplicableValues()){
                        String id = option.getEnumeratedId(name);
                        if (id != null){
                            if (switchName.equals(option.getEnumCommand(id)) || 
                                endsInEQ && option.getEnumCommand(id) != null && option.getEnumCommand(id).startsWith(switchName)){
                                return option;
                            }
                        }
                    }
                }
            }
            catch (BuildException e) {
                // shouldn't happen
            }
        }
        return null;       
    }
    
    private static boolean matchesSuperClass(IOption opt, String id){
        if (id.equals(opt.getId())) return true;
        IOption base = opt.getSuperClass();
        if (base != null) return matchesSuperClass(base,id);
        return false;
    }
    
    /**
     * Given a list of options, return one that has the given ID or is a subclass of such.
     * @param options the list of options to search as returned from
     * @{link IToolOptionSetting#getCompilerOptions},etc.
     * @param id the name of the option or its super class (e.g. "-Hansi").
     * @return the corresponding option or <code>null></code>.
     */
    public static IOption getOptionForID(IOption[] options, String id){
        for (IOption option: options){
            if (matchesSuperClass(option,id))
                return option;
        }
        return null;       
    }
    
    /**
     * Given a list of options, find one that it named as indicated.
     * @param options the list of options.
     * @param label the name we're looking for.
     * @return the option with the given name or <code>null</code>.
     */
    public static IOption getOptionWithLabel(IOption options[], String label){
        for (IOption option: options){
            if (label.equals(option.getName())){
                return option;
            }
        }
        label = label.trim();
        for (IOption option: options){
            if (label.equals(option.getName().trim())){
                return option;
            }
        }
        return null;
    }
    
    public static String getEnumNameForSwitch(IOption option, String switchName){
        String[] enumNames = option.getApplicableValues();
        for (String enumName: enumNames){
            try {
                String enumId = option.getEnumeratedId(enumName);
                if (enumId != null && switchName.equals(option.getEnumCommand(enumId))){
                    return enumName;
                }
            }
            catch (BuildException e) {
                Assert.fail("BuildException while searching for " + switchName + " within " + option.getName());
            }
        }
        return null;
    }
    
    public static void setCompilerSwitch(String switchName, IToolOptionSetting setting, boolean value) throws WidgetSearchException{
        IOption option = getOptionForSwitch(setting.getCompilerOptions(),switchName);
        if (option == null) {
            throw new WidgetSearchException("No compiler switch named \"" + switchName + "\" found");
        }
        try {
            if (option.getValueType() == IOption.ENUMERATED){
                String enumName = getEnumNameForSwitch(option,switchName);
                Assert.assertTrue("Enumeration for " + switchName,enumName != null);
                if (value)
                    setting.setOptionValue(option, enumName);
                else {
                    // Hmm. An enumeration value is set to false. Example: the user is attempting to
                    // turn off "-g", but -g is part of the "Debugger Support" enumeration.
                    // We'll look for an alternative that has no command.
                    enumName = getEnumNameForSwitch(option,"");
                    if (enumName == null){
                        //Choose the one that is the default.
                        String id = (String)option.getDefaultValue();
                        Assert.assertTrue("Default value for " + option.getName() + " not known",id != null);
                        enumName = option.getEnumName(id);                      
                    }
                    setting.setOptionValue(option,enumName);
                }
            }
            else {
                setting.setOptionValue(option,value);
            }
        }
        catch (BuildException e) {
            Assert.fail("BuildException while setting compiler switch " + switchName );
        }
    }
    
    /**
     * Set option with given name to the given value.
     * @todo davidp needs to add a method comment.
     * @param setting setting manager.
     * @param label name of option to set.
     * @param value value to be assigned the option.
     * @throws WidgetSearchException 
     */
    public static void setCompilerOption(IToolOptionSetting setting, String label, Object value) throws WidgetSearchException{
        IOption option = getOptionWithLabel(setting.getCompilerOptions(),label);
        if (option == null) {
            throw new WidgetSearchException("No compiler option named \"" + label + "\" found");
        }
        setting.setOptionValue(option,value);
    }

}
