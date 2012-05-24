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
package com.arc.cdt.toolchain.arc;


import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.arc.cdt.toolchain.AbstractOptionEnablementManager;
import com.arc.cdt.toolchain.IOptionEnablementManager;
import com.arc.cdt.toolchain.OptionEnablementManager;


/**
 * Handles ARCompact options. Determines which is enabled or disabled when things are set. Also ties similar compiler,
 * assembler, and linker options together.
 * @author davidp
 * @currentOwner <a href="mailto:davidp@arc.com">davidp</a>
 * @version $Revision$
 * @lastModified $Date$
 * @lastModifiedBy $Author$
 * @reviewed 0 $Revision:1$
 */

public class ArcOptionEnablementManager extends OptionEnablementManager {

    /**
     * 
     */
    private static final String COMPILER_OPTIONS_EXCEPT = "arc.compiler.options.c++.except";

    private static final String ARC700_VRAPTOR = "arc.compiler.options.vraptor";

    private static final String ARC700_MX = "arc.compiler.options.mx";

    private static final String TIMER1 = "arc.compiler.options.timers.timer1";

    private static final String TIMER0 = "arc.compiler.options.timers.timer0";

    private static String ARC5_CORE = "arc.compiler.options.arc5core";
    
    private static String XY_MEMORY_ID = "arc.compiler.options.xy";
    
    private static String MPY_COMPILER_OPT = "arc.compiler.options.mpy";

    /**
     * The names of options that are disabled for ARC5/Core 1
     */
    private static String ARC5_CORE1_DISABLED[] = {
            MPY_COMPILER_OPT,
            // "arc.compiler.options.mul32x16",
            "arc.compiler.options.spfp",
            "arc.compiler.options.dpfp",
            "arc.compiler.options.spfpfast",
            "arc.compiler.options.dpfpfast",
            "arc.asm.options.mpy",
            "arc.linker.options.spfp",
            "arc.linker.options.dpfp",
            "arc.linker.options.spfpfast",
            "arc.linker.options.dpfpfast",
            "arc.linker.options.mpy",
            "arc.compiler.options.arc7core",
            "arc.compiler.options.asm.arc7core",
            
            "arc.compiler.options.sa",
            "arc.linker.options.sa",
            "arc.asm.options.sa",
            
            "arc.compiler.options.divrem",
            "arc.linker.options.divrem",
            "arc.asm.options.divrem",
            "arc.compiler.options.cd",
            "arc.linker.options.cd",
            "arc.asm.options.cd",
            "arc.compiler.options.atomic",
            "arc.linker.options.atomic",
            "arc.asm.options.atomic",
            
            "arc.asm.options.rf16",
            "arc.compiler.options.rf16",
            "arc.linker.options.rf16",
            

    };

    private static Set<String> DISABLED_FOR_ARC5_CORE1 = new HashSet<String>(Arrays.asList(ARC5_CORE1_DISABLED));

    /**
     * The names of options that are disabled for ARC5, Core's 2 and greater
     */
    private static String ARC5_DISABLED[] = { 
        MPY_COMPILER_OPT,
             "arc.linker.options.mpy",
    // "arc.compiler.options.mul32x16",
            "arc.asm.options.mpy", "arc.asm.options.bs", // disabled except for core 1
            "arc.linker.options.spfp", 
            "arc.linker.options.dpfp",
            "arc.linker.options.spfpfast", 
            "arc.linker.options.dpfpfast",
            "arc.compiler.options.arc7core", "arc.compiler.options.bs", // disabled except
            "arc.compiler.options.arc6core",                                                                                        // for core 1
            "arc.compiler.options.asm.arc7core",
            "arc.compiler.options.asm.arc6core",
            "arc.compiler.options.spfp",
            "arc.compiler.options.bitscan",
            "arc.compiler.options.dpfp",
            "arc.compiler.options.spfpfast",
            "arc.compiler.options.dpfpfast",
            
            "arc.asm.options.rf16",
            "arc.compiler.options.rf16",
            "arc.linker.options.rf16",
            
            "arc.compiler.options.sa",
            "arc.linker.options.sa",
            "arc.asm.options.sa",
            "arc.compiler.options.divrem",
            "arc.linker.options.divrem",
            "arc.asm.options.divrem",
            "arc.asm.options.bitscan",
            "arc.linker.options.bitscan",
            "arc.compiler.options.cd",
            "arc.linker.options.cd",
            "arc.asm.options.cd",
            "arc.compiler.options.atomic",
            "arc.linker.options.atomic",
            "arc.asm.options.atomic",

    };

    private static Set<String> DISABLED_FOR_ARC5 = new HashSet<String>(Arrays.asList(ARC5_DISABLED));

    /**
     * The names of options that are disabled for ARC600
     */
    private static String ARC6_DISABLED[] = {
            "arc.compiler.options.arc5core",
            "arc.compiler.options.minmax",
            "arc.linker.options.minmax",
            "arc.compiler.options.asm.arc5core",
            MPY_COMPILER_OPT,
            "arc.linker.options.mpy",
            // "arc.compiler.options.mul32x16",
            "arc.asm.options.minmax",
            "arc.asm.options.arc5core",
            "arc.asm.options.bs",
            "arc.compiler.options.arc7core",
            "arc.compiler.options.asm.arc7core",
            "arc.compiler.options.bs",
            "arc.linker.options.bs",
            "arc.compiler.options.bitscan",
            "arc.asm.options.bitscan",
            "arc.linker.options.bitscan",
            "arc.asm.options.mpy", 
            
            "arc.compiler.options.sa",
            "arc.linker.options.sa",
            "arc.asm.options.sa",
            "arc.compiler.options.divrem",
            "arc.linker.options.divrem",
            "arc.asm.options.divrem",
            "arc.compiler.options.cd",
            "arc.linker.options.cd",
            "arc.asm.options.cd",
            "arc.compiler.options.atomic",
            "arc.linker.options.atomic",
            "arc.asm.options.atomic",};
    
    /**
     * The names of options that are disabled for ARC601
     */
    private static String ARC601_DISABLED[] = {
            "arc.compiler.options.arc5core",
            "arc.compiler.options.minmax",
            "arc.linker.options.minmax",
            "arc.compiler.options.asm.arc5core",
            MPY_COMPILER_OPT,
            "arc.linker.options.mpy",
            // "arc.compiler.options.mul32x16",
            "arc.asm.options.minmax",
            "arc.asm.options.arc5core",
            "arc.compiler.options.arc7core",
            "arc.compiler.options.arc6core", 
            "arc.compiler.options.asm.arc6core",
            "arc.compiler.options.asm.arc7core",           
            "arc.asm.options.mpy", 
            "arc.compiler.options.spfp",
            "arc.compiler.options.dpfp",
            "arc.compiler.options.spfpfast",
            "arc.compiler.options.dpfpfast",
            "arc.linker.options.spfp", 
            "arc.linker.options.dpfp",
            "arc.linker.options.spfpfast", 
            "arc.linker.options.dpfpfast",
            
            "arc.compiler.options.xmac24",
            "arc.compiler.options.xmacd16",
            "arc.compiler.options.dmulpf",
            "arc.compiler.options.mul32x16",
            "arc.compiler.options.ea",
            
            "arc.asm.options.xmac24",
            "arc.asm.options.xmacd16",
            "arc.asm.options.dmulpf",
            "arc.asm.options.mul32x16",
            "arc.asm.options.ea",
            
            "arc.linker.options.xmac24",
            "arc.linker.options.xmacd16",
            "arc.linker.options.dmulpf",
            "arc.linker.options.mul32x16",
            "arc.linker.options.ea",
            
            XY_MEMORY_ID,
            "arc.asm.options.xy",
            "arc.linker.options.xy",
            
            "arc.compiler.options.sa",
            "arc.linker.options.sa",
            "arc.asm.options.sa",
            "arc.compiler.options.divrem",
            "arc.linker.options.divrem",
            "arc.asm.options.divrem",
            "arc.compiler.options.cd",
            "arc.linker.options.cd",
            "arc.asm.options.cd",
            "arc.compiler.options.atomic",
            "arc.linker.options.atomic",
            "arc.asm.options.atomic",
            "arc.compiler.options.bitscan",
            "arc.asm.options.bitscan",
            "arc.linker.options.bitscan",
            };
    
    /**
     * The names of options that are disabled for ARCv2EM
     */
    private static String ARCV2_DISABLED[] = {
            "arc.compiler.options.arc5core",
            "arc.compiler.options.asm.arc5core",
         
            // "arc.compiler.options.mul32x16",
            "arc.asm.options.arc5core",
            "arc.compiler.options.arc7core",
            "arc.compiler.options.arc6core", 
            "arc.compiler.options.asm.arc6core",
            "arc.compiler.options.asm.arc7core",           

            "arc.compiler.options.spfp",
            "arc.compiler.options.dpfp",
            "arc.compiler.options.spfpfast",
            "arc.compiler.options.dpfpfast",
            "arc.linker.options.spfp", 
            "arc.linker.options.dpfp",
            "arc.linker.options.spfpfast", 
            "arc.linker.options.dpfpfast",
            
            "arc.compiler.options.xmac24",
            "arc.compiler.options.xmacd16",
            "arc.compiler.options.dmulpf",
            "arc.compiler.options.mul32x16",
            "arc.compiler.options.ea",
            "arc.compiler.options.crc",
            "arc.compiler.options.dvbf",
         
            "arc.asm.options.xmac24",
            "arc.asm.options.xmacd16",
            "arc.asm.options.dmulpf",
            "arc.asm.options.mul32x16",
            "arc.asm.options.ea",
            "arc.asm.options.crc",
            "arc.asm.options.dvbf",
            
            "arc.linker.options.xmac24",
            "arc.linker.options.xmacd16",
            "arc.linker.options.dmulpf",
            "arc.linker.options.mul32x16",
            "arc.linker.options.ea",
            "arc.linker.options.crc",
            "arc.linker.options.dvbf",
            
            "arc.compiler.options.mult32",
            "arc.linker.options.mult32",
            "arc.asm.options.mult32",
            
            XY_MEMORY_ID,
            "arc.asm.options.xy",
            "arc.linker.options.xy",
            
            "arc.compiler.options.norm",
            "arc.asm.options.norm",
            "arc.linker.options.norm",
    };

    private static Set<String> DISABLED_FOR_ARC6 = new HashSet<String>(Arrays.asList(ARC6_DISABLED));
    private static Set<String> DISABLED_FOR_ARC601 = new HashSet<String>(Arrays.asList(ARC601_DISABLED));
    private static Set<String> DISABLED_FOR_ARCV2 = new HashSet<String>(Arrays.asList(ARCV2_DISABLED));


    /**
     * The names of options that are disabled for ARC700
     */
    private static String ARC7_DISABLED[] = {
            "arc.compiler.options.arc5core",
            "arc.compiler.options.arc6core",
            "arc.compiler.options.minmax",
            "arc.compiler.options.asm.arc5core",
            "arc.compiler.options.asm.arc6core",
            "arc.compiler.options.mult32",
            "arc.compiler.options.norm",
            "arc.compiler.options.bitscan",
            "arc.compiler.options.swap",
            "arc.compiler.options.ea",
            "arc.compiler.options.dvbf",
            "arc.compiler.options.xmac24",
            "arc.compiler.options.bs",
            
            "arc.linker.options.minmax",
            "arc.linker.options.asm.arc5core",
            "arc.linker.options.mult32",
            "arc.linker.options.norm",
            "arc.linker.options.bitscan",
            "arc.linker.options.swap",
            "arc.linker.options.ea",
            "arc.linker.options.dvbf",
            "arc.linker.options.xmac24",
            "arc.linker.options.bs",
            
            "arc.asm.options.arc5core",
            "arc.asm.options.arc6core",
            "arc.asm.options.minmax",
            "arc.asm.options.mult32",
            "arc.asm.options.norm",
            "arc.asm.options.bitscan",
            "arc.asm.options.swap",
            "arc.asm.options.ea",
            "arc.asm.options.dvbf",
            "arc.asm.options.xmac24",
           
            "arc.asm.options.bs",
            
            "arc.asm.options.rf16",
            "arc.compiler.options.rf16",
            "arc.linker.options.rf16",
            
            "arc.asm.options.timers.timer0",
            "arc.asm.options.timers.timer1",
            TIMER0,
            TIMER1,
            "arc.compiler.options.sa",
            "arc.linker.options.sa",
            "arc.asm.options.sa",
            "arc.compiler.options.divrem",
            "arc.linker.options.divrem",
            "arc.asm.options.divrem",
            "arc.compiler.options.cd",
            "arc.linker.options.cd",
            "arc.asm.options.cd",
            "arc.compiler.options.atomic",
            "arc.linker.options.atomic",
            "arc.asm.options.atomic",
            };
    
    private static String[] IMPLICITLY_SET_FOR_ARC700 = {
            "arc.compiler.options.minmax",
            "arc.compiler.options.norm",
            "arc.compiler.options.swap",
            "arc.compiler.options.ea",
            //"arc.compiler.options.dvbf",
            "arc.compiler.options.bs",
            
            "arc.linker.options.minmax",
            "arc.linker.options.norm",
            "arc.linker.options.swap",
            "arc.linker.options.ea",
            //"arc.linker.options.dvbf",
            "arc.linker.options.bs",
            
            "arc.asm.options.minmax",
            "arc.asm.options.norm",
            "arc.asm.options.swap",
            "arc.asm.options.ea",
            //"arc.asm.options.dvbf",       
            "arc.asm.options.bs",
            

            "arc.asm.options.timers.timer0",
            "arc.asm.options.timers.timer1",
            TIMER0,
            TIMER1
            
    };

    private static Set<String> DISABLED_FOR_ARC7 = new HashSet<String>(Arrays.asList(ARC7_DISABLED));

    private static Set<String> ALL_TARGET_DEPENDENT = new HashSet<String>();
    static {
        ALL_TARGET_DEPENDENT.addAll(DISABLED_FOR_ARC5);
        ALL_TARGET_DEPENDENT.addAll(DISABLED_FOR_ARC6);
        ALL_TARGET_DEPENDENT.addAll(DISABLED_FOR_ARC601);
        ALL_TARGET_DEPENDENT.addAll(DISABLED_FOR_ARC7);
    }
    
    private static String ARC5_XLIB_SETTINGS[] = {
        "arc.compiler.options.norm",
        "arc.compiler.options.swap",
        "arc.compiler.options.mult32",
        "arc.linker.options.norm",
        "arc.linker.options.swap",
        "arc.linker.options.mult32",
        "arc.asm.options.norm",
        "arc.asm.options.swap",
        "arc.asm.options.mult32",
        "arc.compiler.options.mpy",
        "arc.linker.options.mpy",
        "arc.asm.options.mpy",
    };
    
    private static String ARC601_XLIB_SETTINGS[] = {
        "arc.compiler.options.norm",
        "arc.compiler.options.swap",
        "arc.compiler.options.mult32",
        "arc.linker.options.norm",
        "arc.linker.options.swap",
        "arc.linker.options.mult32",
        "arc.asm.options.norm",
        "arc.asm.options.swap",
        "arc.asm.options.mult32",
        "arc.compiler.options.mpy",
        "arc.linker.options.mpy",
        "arc.asm.options.mpy",
        "arc.compiler.options.bs",
        "arc.linker.options.bs",
        "arc.asm.options.bs",
    };
    
    private static String ARCV2EM_XLIB_SETTINGS[] = {
        "arc.compiler.options.bitscan",
        "arc.compiler.options.swap",
        "arc.compiler.options.mpy",
        "arc.compiler.options.mpy16",
        "arc.compiler.options.divrem",
        "arc.compiler.options.cd",

        "arc.linker.options.bitscan",
        "arc.linker.options.swap",
        "arc.linker.options.mpy",
        "arc.linker.options.mpy16",
        "arc.linker.options.divrem",
        "arc.linker.options.cd",

        "arc.asm.options.bitscan",
        "arc.asm.options.swap",
        "arc.asm.options.mpy",
        "arc.asm.options.mpy16",
        "arc.asm.options.divrem",
        "arc.asm.options.cd",

        "arc.compiler.options.bs",
        "arc.linker.options.bs",
        "arc.asm.options.bs",
    };
    
    private static String ARC6_XLIB_SETTINGS[] = ARC5_XLIB_SETTINGS;
    
    private static String ARC7_XLIB_SETTINGS[] = {
        "arc.compiler.options.mpy",
        "arc.linker.options.mpy",
        "arc.asm.options.mpy",
    };
    
    /**
     * Options disabled because "-Xlib" specified.
     */
    private Set<String> xlibDisabled = new HashSet<String>();
    
    /**
     * Options disabled due to mutual-exclusiveness of DSP options.
     */
    private Set<String> dspDisabled = new HashSet<String>();

    public ArcOptionEnablementManager() {
        addObserver(new Observer());
        
        AbstractOptionEnablementManager generalOptionManager = com.arc.cdt.toolchain.ApplicabilityCalculator.getOptionEnablementManager();
        generalOptionManager.addObserver(new IObserver(){

            public void onOptionValueChanged (IOptionEnablementManager mgr, String optionId) {
                // If nolib enabled (by being turned off, then disable stlport library
                if ("arc.linker.options.nolib".equals(optionId)){
                    boolean b = mgr.getValue("arc.linker.options.nolib").equals(Boolean.TRUE);
                    ArcOptionEnablementManager.this.setEnabled(LINKER_OPTION_CPPLIB, b);
                }
                else if (COMPILER_OPTIONS_EXCEPT.equals(optionId)){
                    // If -Hstl, then set the appropriate library
                    boolean b = mgr.getValue(optionId).equals(Boolean.TRUE);
                    String lib = (String)ArcOptionEnablementManager.this.getValue(LINKER_OPTION_CPPLIB);
                    if (lib.indexOf("stl") >= 0 && (b != (lib.indexOf("stlexcept") > 0))){
                        ArcOptionEnablementManager.this.setOptionValue(LINKER_OPTION_CPPLIB, b?STLLIB_WITH_EXCEPTIONS:STLLIB_WITHOUT_EXCEPTIONS);                       
                    }
                }
            }

            public void onOptionEnablementChanged (IOptionEnablementManager mgr, String optionID) {
                // TODO Auto-generated method stub               
            }});
    }
    
    private static String XLIB = "arc.compiler.options.xlib";
    private static String MUL32x16 = "arc.compiler.options.mul32x16";
    private static String XMACD16 = "arc.compiler.options.xmacd16";
    private static String XMAC24 = "arc.compiler.options.xmac24";
    private static String MULT32 = "arc.compiler.options.mult32";
    /**
     * 
     */
    private static final String NO_STLLIB = "linker.option.lib.std";
    /**
     * 
     */
    private static final String STLLIB_WITHOUT_EXCEPTIONS = "linker.option.lib.stlnoexcept";
    /**
     * 
     */
    private static final String STLLIB_WITH_EXCEPTIONS = "linker.option.lib.stlexcept";
    /**
     * 
     */
    private static final String COMPILER_OPTIONS_STL = "arc.compiler.options.stl";
    /**
     * 
     */
    private static final String LINKER_OPTION_CPPLIB = "arc.linker.options.cpplib";
    
    private static String[] EXCLUSIVE = {XLIB,MUL32x16,XMACD16,XMAC24,/*MULT32*/};
    
    private static String DMULPF = "arc.compiler.options.dmulpf";
    
    private static String DSP = "arc.compiler.options.dspmem";
    private static String XY = "arc.compiler.options.dspmem.xy";
    
    private static Map<String,String[]> ASM_OPTION = new HashMap<String,String[]>();
    static {
        ASM_OPTION.put(MUL32x16,new String[]{"arc.asm.options.mul32x16","arc.linker.options.mul32x16"});
        ASM_OPTION.put(XMACD16,new String[]{"arc.asm.options.xmacd16","arc.linker.options.xmacd16"});
        ASM_OPTION.put(XMAC24, new String[]{"arc.asm.options.xmac24","arc.linker.options.xmac24"});
    }
    private static List<String> EXCLUSIVE_LIST = Arrays.asList(EXCLUSIVE);
    
    private static String WIDTH_OPTIONS[] = {"arc.compiler.options.pc_width",  "arc.compiler.options.lpc_width", "arc.linker.options.pc_width"};

    class Observer implements IOptionEnablementManager.IObserver {

	

        private String lastVersion = null;

        /**
         * Called when an option value changes. Enable or disable any options that are dependent on this one.
         * @param mgr
         * @param optionId
         */
        public void onOptionValueChanged (IOptionEnablementManager mgr, String optionId) {
            Set<String> disabledSet = null;
            boolean enableBS = false;
            boolean checkDSP = false;
            boolean checkFP = false;
            if (optionId.equals(ARC5_CORE)) {
                String value = (String) mgr.getValue(optionId);
                
                String version = getVersion(mgr);
                if (version != null && version.endsWith("arc5")) {
                	if (value.endsWith("core1")){
                        disabledSet = DISABLED_FOR_ARC5_CORE1;
                	}
                	else{
                        enableBS = true;
                		disabledSet = DISABLED_FOR_ARC5;
                	}
                }
            }
            else
            if (optionId.endsWith(".target.version")) {
                String value = (String) mgr.getValue(optionId);
                // If we're changing from ARC700, then clear things that was set explicitly.
                if (!value.equals(lastVersion) && lastVersion != null && lastVersion.endsWith("arc700")){
                    for (String option: IMPLICITLY_SET_FOR_ARC700){
                        setOptionValue(option,Boolean.FALSE);
                    }
                }
                lastVersion = value;
                enableBS = true;
                if (!value.endsWith("arc700")){
                    setEnabled(ARC700_MX,false);
                    setEnabled(ARC700_VRAPTOR,false);
                    if (!value.endsWith("arc601") && !value.endsWith("arcv2em"))
                        checkDSP = true;
                }
                if (value.endsWith("arc5")) {
                    disabledSet = DISABLED_FOR_ARC5;
                    if (mgr.getValue(ARC5_CORE) != null && mgr.getValue(ARC5_CORE).toString().endsWith("core1")) {
                        disabledSet = DISABLED_FOR_ARC5_CORE1;
                        enableBS = false;
                    }
                }
                else if (value.endsWith("arc600")) {
                    disabledSet = DISABLED_FOR_ARC6;
                    checkFP = true;
                }
                else if (value.endsWith("arc601")) {
                    disabledSet = DISABLED_FOR_ARC601;
                    checkFP = false;
                    enableBS = true;
                }
                else if (value.endsWith("arc700")) {
                    checkFP = true;
                    setEnabled(ARC700_MX,true);
                    setEnabled(XLIB,true); // -xlib unconditionally enabled.
                    setDSPEnabled(MUL32x16,true);  // DSP options unconditionally enabled under ARC700
                    setDSPEnabled(XMACD16,true);
                    setOptionValue(XMAC24,false);
                    setDSPEnabled(XMAC24,false);
                    disabledSet = DISABLED_FOR_ARC7;
                    for (String option: IMPLICITLY_SET_FOR_ARC700) {
                        setOptionValue(option,Boolean.TRUE);
                    }
                    // "mult32" is not under ARC700. Turn it off to avoid confusing user. (cr92726)
                    setOptionValue("arc.compiler.options.mult32",false);
                }
                else if (value.endsWith("arcv2em")){
                    checkFP = false;
                    checkDSP = false;
                    disabledSet = DISABLED_FOR_ARCV2;
                    checkMPY(value);
                }
                // if RF16 just went enabled, then check for AOM enablement.
                setRF16Options(mgr,Boolean.TRUE.equals(mgr.getValue("arc.compiler.options.rf16")));
                setXlibProps(XLIB,value);
                if (value.endsWith("arc601")){
                    // Just set "ARC 601". Turn off -Xbs since its default is false.
                    for (String option: ARC601_XLIB_SETTINGS) {
                        if (option.endsWith(".bs"))
                            setOptionValue(option,Boolean.FALSE);
                    }
                }
            }
            else if (optionId.endsWith("isDashG")) {
                boolean dashG = mgr.getValue(optionId).equals(Boolean.TRUE);
                setEnabled("arc.arc.compiler.options.dwarf1", dashG);
            }
            else if (optionId.endsWith(".profiling")) {
                boolean profiling = mgr.getValue(optionId).equals(Boolean.TRUE);
                setEnabled("arc.arc.linker.options.xtimer", profiling);
            }
            else if (optionId.endsWith("spfpfast")){
                boolean fastFP = mgr.getValue(optionId).equals(Boolean.TRUE);
                setEnabled("arc.linker.options.spfp",!fastFP);
                setEnabled("arc.compiler.options.spfp",!fastFP);
            }
            else if (optionId.endsWith("dpfpfast")){
                boolean fastFP = mgr.getValue(optionId).equals(Boolean.TRUE);
                setEnabled("arc.linker.options.dpfp",!fastFP);
                setEnabled("arc.compiler.options.dpfp",!fastFP);
            }
            else if (optionId.endsWith(".spfp")){
                boolean fastFP = mgr.getValue(optionId).equals(Boolean.TRUE);
                setEnabled("arc.linker.options.spfpfast",!fastFP);
                setEnabled("arc.compiler.options.spfpfast",!fastFP);
            }
            else if (optionId.endsWith(".stl")){
                boolean v = mgr.getValue(optionId).equals(Boolean.TRUE);
                if (v) {
                    boolean except = Boolean.TRUE.equals(com.arc.cdt.toolchain.ApplicabilityCalculator.getOptionEnablementManager().getValue(COMPILER_OPTIONS_EXCEPT));
                    setOptionValue(LINKER_OPTION_CPPLIB,
                        except?STLLIB_WITH_EXCEPTIONS:STLLIB_WITHOUT_EXCEPTIONS);
                }
                else {
                    setOptionValue(LINKER_OPTION_CPPLIB,NO_STLLIB);
                }
            }
            else if (optionId.equals(LINKER_OPTION_CPPLIB)){
                // If stl library chosen, then add -Hstl to the compiler.
                String lib = (String)ArcOptionEnablementManager.this.getValue(optionId);
                if (lib != null) {
                     setOptionValue(COMPILER_OPTIONS_STL,lib.indexOf("stl") >= 0?Boolean.TRUE:Boolean.FALSE);  
                     if (lib.indexOf("stlexcept") >= 0){
                         com.arc.cdt.toolchain.ApplicabilityCalculator.getOptionEnablementManager().setOptionValue(COMPILER_OPTIONS_EXCEPT, true);
                     }
                     else if (lib.indexOf("stlnoexcept") >= 0){
                         com.arc.cdt.toolchain.ApplicabilityCalculator.getOptionEnablementManager().setOptionValue(COMPILER_OPTIONS_EXCEPT, false);
                     }
                }
            }
            else if (optionId.endsWith(".dpfp")){
                boolean fastFP = mgr.getValue(optionId).equals(Boolean.TRUE);
                setEnabled("arc.linker.options.dpfpfast",!fastFP);
                setEnabled("arc.compiler.options.dpfpfast",!fastFP);
            }
            else if (optionId.endsWith(".aom")){
            	boolean aom = mgr.getValue(optionId).equals(Boolean.TRUE);
            	setOverlayOptions(mgr,aom);
            }
            else if (optionId.endsWith(".mult32_cycles") || optionId.equals(MULT32)) {
                setEnabled("arc.compiler.options.mult32_cycles",mgr.getValue(MULT32).equals(Boolean.TRUE));
            }
            else if (optionId.endsWith("arc.compiler.options.rf16")) {
                boolean aom = mgr.getValue(optionId).equals(Boolean.TRUE);
                setRF16Options(mgr,aom);
            }
            else if (optionId.endsWith(".pc_width")) {
                String value = (String) mgr.getValue(optionId);
                setOptionValue("arc.linker.options.pc_width",value);
                setOptionValue("arc.compiler.options.pc_width",value);
            }
            else if (optionId.endsWith(".optimization.level") || optionId.endsWith(".arc.compilerDebugOptLevel")){
            	String optLevel = mgr.getValue(optionId).toString();
            	setEnabled("arc.compiler.options.Os1","arc.optimization.level.Os".equals(optLevel));
            }
            else if (optionId.equals(DSP)){
            	// Formerly -Xxy was an enumeration of DSP Memory
            	// Since Scratch RAM has been remove, it is now just a check box.
            	// But in case we have an older project, we still recognize it.
            	String dspMem = mgr.getValue(optionId).toString();
            	if (dspMem != null && dspMem.endsWith(".xy") && !Boolean.TRUE.equals(mgr.getValue(XY_MEMORY_ID))){
            		mgr.set(XY_MEMORY_ID,Boolean.TRUE);
            	}
            }
            else if (optionId.equals(XY_MEMORY_ID)){
            	if (Boolean.TRUE.equals(mgr.getValue(optionId))){
            		mgr.set(DSP,XY); // Legacy
            	}
            }
            else if (optionId.endsWith(".mpy")){
                checkMPY(getVersion(mgr));
            }

            boolean xy = Boolean.TRUE.equals(mgr.getValue(XY_MEMORY_ID));
            setEnabled(DMULPF,xy);
            
            if (enableBS && disabledSet != null){
                // Even though we disabled barrel-shift, check it so that it
                // reflects the fact that it is enabled by default.
                for (String id: disabledSet){
                    if (id.endsWith(".bs"))
                        setOptionValue(id,Boolean.TRUE);
                }
            }
            

            
//            else if (optionId.indexOf("arc.linker.options") >= 0 ||
//                     optionId.indexOf("arc.compiler.options") >= 0) {
//                // Synchronize compiler and linker options
//                String other;
//                if (optionId.indexOf("arc.linker.options") >= 0){
//                    other = optionId.replaceFirst(".linker",".compiler");
//                }
//                else {
//                    other = optionId.replaceFirst(".compiler",".linker");            
//                }
//                mgr.set(other,mgr.getValue(optionId));              
//            }
            String version = getVersion(mgr);
            //Check SIMD. Make them work like radio buttons.
            if (version != null && version.endsWith("arc700")) {
                setEnabled(ARC700_VRAPTOR,Boolean.TRUE.equals(mgr.getValue(ARC700_MX)));              
            }

            if (optionId.endsWith(".xlib")) {
                setXlibProps(optionId, version);               
            }
           
            if (disabledSet != null) {
                // Turn on any option not in the set.
                for (String id : ALL_TARGET_DEPENDENT) {
                    if (!disabledSet.contains(id) && !xlibDisabled.contains(id) && !dspDisabled.contains(id)) {
                        setEnabled(id, true);
                    }

                }
                // Now disable all options in the set.
                for (String id : disabledSet) {
                    setEnabled(id, false);
                }
            }
            if (version != null && (version.endsWith("arc600") || version.endsWith("arc601") || version.endsWith("v2em"))){
                for (String s: WIDTH_OPTIONS){
                    setEnabled(s,true);
                }
            }
            else {
                for (String s: WIDTH_OPTIONS){
                    setOptionValue(s,"32");
                    setEnabled(s,false);
                }
            }
            if (checkDSP || version == null || EXCLUSIVE_LIST.contains(optionId) && !version.endsWith("arc700")){
                handleExclusiveDspStuff(mgr);
            }
            if (checkFP){
                boolean fastFP = Boolean.TRUE.equals(mgr.getValue("arc.compiler.options.spfpfast"));
                setEnabled("arc.linker.options.spfp",!fastFP);
                setEnabled("arc.compiler.options.spfp",!fastFP);
                if (!fastFP) {
                    fastFP = Boolean.TRUE.equals(mgr.getValue("arc.compiler.options.spfp"));
                    setEnabled("arc.linker.options.spfpfast",!fastFP);
                    setEnabled("arc.compiler.options.spfpfast",!fastFP);
                }
                fastFP = Boolean.TRUE.equals(mgr.getValue("arc.compiler.options.dpfpfast"));
                setEnabled("arc.linker.options.dpfp",!fastFP);
                setEnabled("arc.compiler.options.dpfp",!fastFP);
                if (!fastFP){
                    fastFP = Boolean.TRUE.equals(mgr.getValue("arc.compiler.options.dpfp"));
                    setEnabled("arc.linker.options.dpfpfast",!fastFP);
                    setEnabled("arc.compiler.options.dpfpfast",!fastFP);
                }
            }
        }
        
        private void checkMPY(String version){
            if (version != null && version.indexOf("arcv2") >= 0){
                //Disable -mpy16 if -mpy is specified
                Boolean v = (Boolean)getValue(MPY_COMPILER_OPT);
                if (v != null)
                    for (String s: getOptionIds()) {
                        if (s.endsWith(".mpy16")){
                            if (v) setOptionValue(s,true);
                            setEnabled(s,!v);
                        }
                }
            }
        }

        private void setXlibProps (String optionId, String version) {
            String xlibOptions[] = null;
            if (version != null){
                if (version.endsWith("arc5")){
                    xlibOptions = ARC5_XLIB_SETTINGS;
                }
                else if (version.endsWith("arc600")){
                    xlibOptions = ARC6_XLIB_SETTINGS;
                }
                else if (version.endsWith("arc700")){
                    xlibOptions = ARC7_XLIB_SETTINGS;
                }
                else if (version.endsWith("arc601")){
                    xlibOptions = ARC601_XLIB_SETTINGS;
                }
                else if (version.endsWith("arcv2em")){
                    xlibOptions = ARCV2EM_XLIB_SETTINGS;
                }
            }
            if (xlibOptions != null){
                Boolean V = (Boolean)getValue(optionId);
                if (V != null)  {
                    boolean v = V.booleanValue();
                    boolean mpySupported = version != null && (version.endsWith("arc700") || version.indexOf("arcv2") >= 0);
                    for (String s: xlibOptions){
                        if (v) {
                            setOptionValue(s,v && (mpySupported || !s.endsWith(".mpy")));
                            setEnabled(s,false);
                            xlibDisabled.add(s);
                        }
                        else {
                            xlibDisabled.remove(s);
                            if (mpySupported || !s.endsWith(".mpy"))
                                setEnabled(s,true);
                        }
                    }
                }
            }
            if (version != null) checkMPY(version);
        }
        
        private  String[] AOM_STUFF = {
            "arc.linker.options.aom_nolib",
            "arc.compiler.options.aom_overlay_only",
            "arc.compiler.options.aomrtosaware",
            "arc.linker.options.aom_overlay_only",
            "arc.compiler.options.aom_padding",
            "arc.linker.options.aom_padding",
        };
        
        private boolean supportsRF16(IOptionEnablementManager mgr){
            String version = getVersion(mgr);
            return version != null && (version.indexOf('6') >= 0 || version.indexOf("arcv2") >= 0);
        }

		private void setOverlayOptions(IOptionEnablementManager mgr,boolean aom) {
            if (Boolean.TRUE.equals(mgr.getValue("arc.compiler.options.rf16")))
                aom = false;
            
            for (String s: AOM_STUFF) {
			    setEnabled(s,aom);
            }
            
            boolean enabled = !aom && supportsRF16(mgr);
			// No AOM support for -rf16
            for (String s: new String[]{"arc.asm.option.rf16", "arc.linker.options.rf16","arc.compiler.options.rf16"}) {
                setEnabled(s,enabled);
                if (!enabled) setOptionValue(s,false);
            }
		}
		
		private void setRF16Options(IOptionEnablementManager mgr,boolean v){
		    if (!supportsRF16(mgr)) v = false;
		    // No aom support when -rf16 specified.
		    setEnabled("arc.linker.options.aom",!v);
		    setEnabled("arc.compiler.options.aom",!v);
		    if (v) {
		        for (String s: AOM_STUFF) {
	                setEnabled(s,false);
	            }
		    }
		    else {
		        setOverlayOptions(mgr,Boolean.TRUE.equals(mgr.getValue("arc.compiler.options.aom")));
		    }
		}

        /**
         * Handle DSP settings that are mutually exclusive.
         * @param mgr
         */
        private void handleExclusiveDspStuff (IOptionEnablementManager mgr) {
            String oneIsSet = null;
            for (String s: EXCLUSIVE){
                if (Boolean.TRUE.equals(mgr.getValue(s))){
                    oneIsSet = s;
                    break;
                }
            }
            for (String s: EXCLUSIVE){
                if (oneIsSet != null && s != oneIsSet && !(oneIsSet == MULT32 && s == XLIB) &&
                    !(oneIsSet == MUL32x16 && s == XMACD16 || oneIsSet == XMACD16 && s == MUL32x16)){
                    setDSPEnabled(s,false);
                }
                else {
                    setDSPEnabled(s,true);
                }
            }
        }

        /**
         * Set the DSP options enablement property appropriately.
         * @param s the name of a DSP property that has changed.
         * @param value value of the DSP property.
         */
        private void setDSPEnabled (String s, boolean value) {
            setEnabled(s,value);
            String[] asmOpt = ASM_OPTION.get(s);
            if (asmOpt != null)
                for (String id: asmOpt){
                    setEnabled(id,value);
                }
        }

        /**
         * Return the target processor version.
         * @param mgr
         * @return the target process version.
         */
        private String getVersion (IOptionEnablementManager mgr) {
            return (String) mgr.getValue("arc.compiler.options.target.version");
        }

        public void onOptionEnablementChanged (IOptionEnablementManager mgr, String optionID) {
            // Nothing to do.

        }
    }
}
