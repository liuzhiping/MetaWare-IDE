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

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;

import com.arc.cdt.toolchain.AbstractOptionEnablementManager;
import com.arc.cdt.toolchain.ApplicabilityCalculator;
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

public class ArcOptionEnablementManager extends AbstractOptionEnablementManager {

    private static final String COMPILER_VERSION = "arc.compiler.options.target.version";
    private static final String VERSION_AV2EM = "arc.compiler.options.target.arcv2em";
    private static final String VERSION_AV2HS = "arc.compiler.options.target.arcv2hs";
    private static final String VERSION_ARC700 = "arc.compiler.options.target.arc700";
    private static final String VERSION_ARC600 = "arc.compiler.options.target.arc600";
    private static final String VERSION_ARC601 = "arc.compiler.options.target.arc601";
    private static final String VERSION_ARC5 = "arc.compiler.options.target.arc5";
    
    private static final String[] ALL_VERSIONS = {
        VERSION_ARC5,
        VERSION_ARC601,
        VERSION_ARC600,
        VERSION_ARC700,
        VERSION_AV2HS,
        VERSION_AV2EM
    };


    /**
     * The list of targets that are CLANG/LLVM based. Others are assumed to be metaware.
     */
    private static final String CLANG_BASED_TARGETS[] = {VERSION_AV2EM, VERSION_AV2HS};

    /**
     * 
     */
    private static final String ARC_ASM_OPTIONS_DIV_REM = "arc.asm.options.div_rem";
    /**
     * 
     */
    private static final String ARC_LINKER_OPTIONS_DIV_REM = "arc.linker.options.div_rem";
    
    /**
     * The linker mapfile option for EM4 case.
     */
    private static final String EM4_MAP_OPTION_ID = "arcem4.linker.option.map";

    //ARCV2HS

    private static final String ARC_ASM_OPTIONS_LL64 = "arc.asm.options.ll64";
    private static final String ARC_LINKER_OPTIONS_LL64 = "arc.linker.options.ll64";
    private static final String ARC_COMPILER_OPTIONS_LL64 = "arc.compiler.options.ll64";

    private static final String ARC_ASM_OPTIONS_UNALIGNED = "arc.asm.options.unaligned";
    private static final String ARC_LINKER_OPTIONS_UNALIGNED = "arc.linker.options.unaligned";
    private static final String ARC_COMPILER_OPTIONS_UNALIGNED = "arc.compiler.options.unaligned";

    private static final String ARC_ASM_OPTIONS_QMPYH = "arc.asm.options.qmpyh";
    private static final String ARC_LINKER_OPTIONS_QMPYH = "arc.linker.options.qmpyh";
    private static final String ARC_COMPILER_OPTIONS_QMPYH = "arc.compiler.options.qmpyh";

    private static final String ARC_ASM_OPTIONS_MACD = "arc.asm.options.macd";
    private static final String ARC_LINKER_OPTIONS_MACD = "arc.linker.options.macd";
    private static final String ARC_COMPILER_OPTIONS_MACD = "arc.compiler.options.macd";

    private static final String ARC_ASM_OPTIONS_MAC = "arc.asm.options.mac";
    private static final String ARC_LINKER_OPTIONS_MAC = "arc.linker.options.mac";
    private static final String ARC_COMPILER_OPTIONS_MAC = "arc.compiler.options.mac";

    private static final String XLIB = "arc.compiler.options.xlib";
    private static final String MUL32x16 = "arc.compiler.options.mul32x16";
    private static final String XMACD16 = "arc.compiler.options.xmacd16";
    private static final String XMAC24 = "arc.compiler.options.xmac24";
    private static final String MULT32 = "arc.compiler.options.mult32";
    private static final String MULT32_CYCLES = "arc.compiler.options.mult32_cycles";
    
    /**
     * 
     */
    private static final String NO_STLLIB = "linker.option.lib.std";
    /**
     * 
     */
    private static final String STLLIB_WITHOUT_EXCEPTIONS = "linker.option.lib.stlnoexcept";
    
    private static final String STLLIB_CCAC_WITHOUT_EXCEPTIONS = "linker.option.lib.ccac.stlnoexcept";

    /**
     * 
     */
    private static final String STLLIB_WITH_EXCEPTIONS = "linker.option.lib.stlexcept";
    private static final String STLLIB_CCAC_WITH_EXCEPTIONS = "linker.option.lib.ccac.stlexcept";

    /**
     * 
     */
    private static final String COMPILER_OPTIONS_STL = "arc.compiler.options.stl";
    private static final String COMPILER_OPTIONS_MWCPP = "arc.compiler.options.mwcpp";
    /**
     * 
     */
    private static final String LINKER_OPTION_CPPLIB = "arc.linker.options.cpplib";
    private static final String LINKER_CCAC_OPTION_CPPLIB = "arc.linker.options.ccac.cpplib";


    /**
     * 
     */
    private static final String ARC_COMPILER_OPTIONS_DIV_REM = "arc.compiler.options.div_rem";

    private static final String ARC_ASM_OPTIONS_MPY = "arc.asm.options.mpy";
    private static final String ARC_LINKER_OPTIONS_MPY = "arc.linker.options.mpy";
    private static final String ARC_COMPILER_OPTIONS_MPY = "arc.compiler.options.mpy";

    private static final String ARC_ASM_OPTIONS_MPY16 = "arc.asm.options.mpy16";
    private static final String ARC_LINKER_OPTIONS_MPY16 = "arc.linker.options.mpy16";
    private static final String ARC_COMPILER_OPTIONS_MPY16 = "arc.compiler.options.mpy16";
    private static final String ARC_COMPILER_OPTIONS_ASM_ARCV2HS = "arc.compiler.options.asm.arcv2hscore";
    private static final String ARC_COMPILER_OPTIONS_ARCV2HS = "arc.compiler.options.arcv2hscore";

    //SPFP and DPFP
    private static final String ARC_ASM_OPTIONS_SPFP = "arc.asm.options.spfp";
    private static final String ARC_ASM_OPTIONS_SPFP_FAST = "arc.asm.options.spfpfast";
    private static final String ARC_ASM_OPTIONS_DPFP = "arc.asm.options.dpfp";
    private static final String ARC_ASM_OPTIONS_DPFP_FAST = "arc.asm.options.dpfpfast";

    // FPU

    private static final String ARC_COMPILER_OPTIONS_FPUS = "arc.compiler.options.fpus";
    private static final String ARC_COMPILER_OPTIONS_FPUS_DIV = "arc.compiler.options.fpus_div";
    private static final String ARC_COMPILER_OPTIONS_FPUD = "arc.compiler.options.fpud";
    private static final String ARC_COMPILER_OPTIONS_FPUD_DIV = "arc.compiler.options.fpud_div";
    private static final String ARC_COMPILER_OPTIONS_FPU_MAC= "arc.compiler.options.fpu_mac";


    private static final String ARC_ASM_OPTIONS_FPUS = "arc.asm.options.fpus";
    private static final String ARC_ASM_OPTIONS_FPUS_DIV = "arc.asm.options.fpus_div";
    private static final String ARC_ASM_OPTIONS_FPUD = "arc.asm.options.fpud";
    private static final String ARC_ASM_OPTIONS_FPUD_DIV = "arc.asm.options.fpud_div";
    private static final String ARC_ASM_OPTIONS_FPU_MAC= "arc.asm.options.fpu_mac";


    private static final String ARC_LINKER_OPTIONS_FPUS = "arc.linker.options.fpus";
    private static final String ARC_LINKER_OPTIONS_FPUS_DIV = "arc.linker.options.fpus_div";
    private static final String ARC_LINKER_OPTIONS_FPUD = "arc.linker.options.fpud";
    private static final String ARC_LINKER_OPTIONS_FPUD_DIV = "arc.linker.options.fpud_div";
    private static final String ARC_LINKER_OPTIONS_FPU_MAC= "arc.linker.options.fpu_mac";

    private static final String[] MPY16_OPTS = {ARC_COMPILER_OPTIONS_MPY16,ARC_LINKER_OPTIONS_MPY16,ARC_ASM_OPTIONS_MPY16};

    private static final String[] MACD_OPTS = {ARC_COMPILER_OPTIONS_MACD,ARC_LINKER_OPTIONS_MACD,ARC_ASM_OPTIONS_MACD};
    private static final String[] MAC_OPTS =  {ARC_COMPILER_OPTIONS_MAC,ARC_LINKER_OPTIONS_MAC,ARC_ASM_OPTIONS_MAC};
    private static final String[] MPY_OPTS =  {ARC_COMPILER_OPTIONS_MPY,ARC_LINKER_OPTIONS_MPY,ARC_ASM_OPTIONS_MPY};

    // COMPACT LIB
    private static final String ARC_COMPILER_OPTIONS_HCL       = "arc.compiler.options.hcl" ;
    private static final String ARC_LINKER_OPTIONS_HCL         = "arc.linker.options.hcl" ;
    private static final String ARC_LINKER_OPTIONS_CRTINITCOPY = "arc.linker.options.crtinitcopy";
    private static final String ARC_LINKER_OPTIONS_CRTINITBSS = "arc.linker.options.crtinitbss";
    private static final String ARC_LINKER_OPTIONS_CRTINITCACHE = "arc.linker.options.crtinvcache";
    private static final String ARC_LINKER_OPTIONS_CRTINITTIMER = "arc.linker.options.crtinittimer";
    private static final String ARC_LINKER_OPTIONS_CRTARGV = "arc.linker.options.crtargv";
    private static final String ARC_LINKER_OPTIONS_CRTPFMIN = "arc.linker.options.crtpfmin";

    private static String [] ARC_COMPACT_LIB_CRT_SETTINGS = {ARC_LINKER_OPTIONS_CRTINITCOPY,ARC_LINKER_OPTIONS_CRTINITBSS,ARC_LINKER_OPTIONS_CRTINITCACHE,ARC_LINKER_OPTIONS_CRTINITTIMER,

        ARC_LINKER_OPTIONS_CRTARGV, ARC_LINKER_OPTIONS_CRTPFMIN};



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

     private static String MPY_CYCLES_COMPILER_OPT = "arc.compiler.options.mpycycles";
     
     /**
      * Stuff to be disabled for the ccac compiler
      */
     private static String CCAC_DISABLED[] = {

         COMPILER_OPTIONS_STL, // -Hstl
     };
     
     /**
      * Disabled for metaware compiler (mcc):
      */
     private static String MCC_DISABLED[] = {
         COMPILER_OPTIONS_MWCPP, 
     };

     /**
      * The names of options that are disabled for ARC5/Core 1
      */
     private static String ARC5_CORE1_DISABLED[] = {
         ARC_COMPILER_OPTIONS_MPY,
         MPY_CYCLES_COMPILER_OPT,
         "arc.linker.options.mpy",
         // "arc.compiler.options.mul32x16",
         "arc.asm.options.mpy", /*"arc.asm.options.bs",*/
         "arc.linker.options.spfp", 
         "arc.linker.options.dpfp",
         "arc.linker.options.spfpfast", 
         "arc.linker.options.dpfpfast",
         "arc.compiler.options.arc7core", /* "arc.compiler.options.bs", */
         "arc.compiler.options.arc6core", // for core 1
         "arc.compiler.options.arcv2emcore",
         ARC_COMPILER_OPTIONS_ARCV2HS,
         "arc.compiler.options.asm.arc7core",
         "arc.compiler.options.asm.arc6core",
         "arc.compiler.options.asm.arcv2emcore",
         ARC_COMPILER_OPTIONS_ASM_ARCV2HS,
         "arc.compiler.options.spfp",
         "arc.compiler.options.bitscan",
         "arc.compiler.options.dpfp",
         "arc.compiler.options.spfpfast",
         "arc.compiler.options.dpfpfast",

         ARC_ASM_OPTIONS_SPFP,
         ARC_ASM_OPTIONS_SPFP_FAST,
         ARC_ASM_OPTIONS_DPFP,
         ARC_ASM_OPTIONS_DPFP_FAST,

         "arc.asm.options.rf16",
         "arc.compiler.options.rf16",
         "arc.linker.options.rf16",

         "arc.compiler.options.sa",
         "arc.linker.options.sa",
         "arc.asm.options.sa",
         ARC_COMPILER_OPTIONS_DIV_REM,
         ARC_LINKER_OPTIONS_DIV_REM,
         ARC_ASM_OPTIONS_DIV_REM,
         "arc.asm.options.bitscan",
         "arc.linker.options.bitscan",
         "arc.compiler.options.cd",
         "arc.linker.options.cd",
         "arc.asm.options.cd",
         "arc.compiler.options.atomic",
         "arc.linker.options.atomic",
         "arc.asm.options.atomic",
         //ARCV2HS
         ARC_ASM_OPTIONS_LL64, 
         ARC_LINKER_OPTIONS_LL64,  
         ARC_COMPILER_OPTIONS_LL64,     
         ARC_ASM_OPTIONS_UNALIGNED, 
         ARC_LINKER_OPTIONS_UNALIGNED,  
         ARC_COMPILER_OPTIONS_UNALIGNED,     
         ARC_ASM_OPTIONS_QMPYH,  
         ARC_LINKER_OPTIONS_QMPYH,
         ARC_COMPILER_OPTIONS_QMPYH,     
         ARC_ASM_OPTIONS_MACD,
         ARC_LINKER_OPTIONS_MACD, 
         ARC_COMPILER_OPTIONS_MACD,     
         ARC_ASM_OPTIONS_MAC,  
         ARC_LINKER_OPTIONS_MAC,  
         ARC_COMPILER_OPTIONS_MAC, 

         //ARCV2HS FDU
         ARC_COMPILER_OPTIONS_FPUS,
         ARC_COMPILER_OPTIONS_FPUS_DIV,
         ARC_COMPILER_OPTIONS_FPUD,
         ARC_COMPILER_OPTIONS_FPUD_DIV,
         ARC_COMPILER_OPTIONS_FPU_MAC,
         ARC_ASM_OPTIONS_FPUS,
         ARC_ASM_OPTIONS_FPUS_DIV,
         ARC_ASM_OPTIONS_FPUD,
         ARC_ASM_OPTIONS_FPUD_DIV,
         ARC_ASM_OPTIONS_FPU_MAC,    
         ARC_LINKER_OPTIONS_FPUS,
         ARC_LINKER_OPTIONS_FPUS_DIV ,
         ARC_LINKER_OPTIONS_FPUD,
         ARC_LINKER_OPTIONS_FPUD_DIV ,
         ARC_LINKER_OPTIONS_FPU_MAC,

         ARC700_MX,
         ARC700_VRAPTOR,

         //COMPACT LIB FOR EM
         ARC_COMPILER_OPTIONS_HCL,ARC_LINKER_OPTIONS_HCL, ARC_LINKER_OPTIONS_CRTINITCOPY,ARC_LINKER_OPTIONS_CRTINITBSS,ARC_LINKER_OPTIONS_CRTINITCACHE,ARC_LINKER_OPTIONS_CRTINITTIMER,ARC_LINKER_OPTIONS_CRTARGV, ARC_LINKER_OPTIONS_CRTPFMIN,





     };

     private static Set<String> DISABLED_FOR_ARC5_CORE1 = new HashSet<String>(Arrays.asList(ARC5_CORE1_DISABLED));

     /**
      * The names of options that are disabled for ARC5, Core's 2 and greater
      */
     private static String ARC5_DISABLED[] = { 
         ARC_COMPILER_OPTIONS_MPY,
         MPY_CYCLES_COMPILER_OPT,
         ARC700_MX,
         ARC700_VRAPTOR,
         "arc.linker.options.mpy",
         // "arc.compiler.options.mul32x16",
         "arc.asm.options.mpy", "arc.asm.options.bs", // disabled except for core 1
         "arc.linker.options.spfp", 
         "arc.linker.options.dpfp",
         "arc.linker.options.spfpfast", 
         "arc.linker.options.dpfpfast",
         "arc.compiler.options.arc7core", "arc.compiler.options.bs", // disabled except
         "arc.compiler.options.arc6core", // for core 1
         "arc.compiler.options.arcv2emcore",
         ARC_COMPILER_OPTIONS_ARCV2HS,
         "arc.compiler.options.asm.arc7core",
         "arc.compiler.options.asm.arc6core",
         "arc.compiler.options.asm.arcv2emcore",
         ARC_COMPILER_OPTIONS_ASM_ARCV2HS,
         "arc.compiler.options.spfp",
         "arc.compiler.options.bitscan",
         "arc.compiler.options.dpfp",
         "arc.compiler.options.spfpfast",
         "arc.compiler.options.dpfpfast",
         ARC_ASM_OPTIONS_SPFP,
         ARC_ASM_OPTIONS_SPFP_FAST,
         ARC_ASM_OPTIONS_DPFP,
         ARC_ASM_OPTIONS_DPFP_FAST,

         "arc.asm.options.rf16",
         "arc.compiler.options.rf16",
         "arc.linker.options.rf16",

         "arc.compiler.options.sa",
         "arc.linker.options.sa",
         "arc.asm.options.sa",
         ARC_COMPILER_OPTIONS_DIV_REM,
         ARC_LINKER_OPTIONS_DIV_REM,
         ARC_ASM_OPTIONS_DIV_REM,
         "arc.asm.options.bitscan",
         "arc.linker.options.bitscan",
         "arc.compiler.options.cd",
         "arc.linker.options.cd",
         "arc.asm.options.cd",
         "arc.compiler.options.atomic",
         "arc.linker.options.atomic",
         "arc.asm.options.atomic",
         //ARCV2HS
         ARC_ASM_OPTIONS_LL64, 
         ARC_LINKER_OPTIONS_LL64,  
         ARC_COMPILER_OPTIONS_LL64,     
         ARC_ASM_OPTIONS_UNALIGNED, 
         ARC_LINKER_OPTIONS_UNALIGNED,  
         ARC_COMPILER_OPTIONS_UNALIGNED,     
         ARC_ASM_OPTIONS_QMPYH,  
         ARC_LINKER_OPTIONS_QMPYH,
         ARC_COMPILER_OPTIONS_QMPYH,     
         ARC_ASM_OPTIONS_MACD,
         ARC_LINKER_OPTIONS_MACD, 
         ARC_COMPILER_OPTIONS_MACD,     
         ARC_ASM_OPTIONS_MAC,  
         ARC_LINKER_OPTIONS_MAC,  
         ARC_COMPILER_OPTIONS_MAC, 
         //ARCV2HS FPU
         ARC_COMPILER_OPTIONS_FPUS,
         ARC_COMPILER_OPTIONS_FPUS_DIV,
         ARC_COMPILER_OPTIONS_FPUD,
         ARC_COMPILER_OPTIONS_FPUD_DIV,
         ARC_COMPILER_OPTIONS_FPU_MAC,
         ARC_ASM_OPTIONS_FPUS,
         ARC_ASM_OPTIONS_FPUS_DIV,
         ARC_ASM_OPTIONS_FPUD,
         ARC_ASM_OPTIONS_FPUD_DIV,
         ARC_ASM_OPTIONS_FPU_MAC,    
         ARC_LINKER_OPTIONS_FPUS,
         ARC_LINKER_OPTIONS_FPUS_DIV ,
         ARC_LINKER_OPTIONS_FPUD,
         ARC_LINKER_OPTIONS_FPUD_DIV ,
         ARC_LINKER_OPTIONS_FPU_MAC,
         
         COMPILER_OPTIONS_MWCPP, // Remove when A5 becomes clang based.


         //COMPACT LIB FOR EM
         ARC_COMPILER_OPTIONS_HCL,ARC_LINKER_OPTIONS_HCL, ARC_LINKER_OPTIONS_CRTINITCOPY,ARC_LINKER_OPTIONS_CRTINITBSS,ARC_LINKER_OPTIONS_CRTINITCACHE,ARC_LINKER_OPTIONS_CRTINITTIMER,ARC_LINKER_OPTIONS_CRTARGV, ARC_LINKER_OPTIONS_CRTPFMIN,




     };

     private static Set<String> DISABLED_FOR_ARC5 = new HashSet<String>(Arrays.asList(ARC5_DISABLED));

     /**
      * The names of options that are disabled for ARC600
      */
     private static String ARC6_DISABLED[] = {
         "arc.compiler.options.arc5core",
         "arc.compiler.options.arcv2emcore",
         ARC_COMPILER_OPTIONS_ARCV2HS,
         "arc.compiler.options.minmax",
         "arc.linker.options.minmax",
         "arc.compiler.options.asm.arc5core",
         "arc.compiler.options.asm.arcv2emcore",
         ARC_COMPILER_OPTIONS_ASM_ARCV2HS,
         ARC_COMPILER_OPTIONS_MPY,
         MPY_CYCLES_COMPILER_OPT,
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
         ARC_COMPILER_OPTIONS_DIV_REM,
         ARC_LINKER_OPTIONS_DIV_REM,
         ARC_ASM_OPTIONS_DIV_REM,
         "arc.compiler.options.cd",
         "arc.linker.options.cd",
         "arc.asm.options.cd",
         "arc.compiler.options.atomic",
         "arc.linker.options.atomic",
         "arc.asm.options.atomic",
         //ARCV2HS
         ARC_ASM_OPTIONS_LL64, 
         ARC_LINKER_OPTIONS_LL64,  
         ARC_COMPILER_OPTIONS_LL64,     
         ARC_ASM_OPTIONS_UNALIGNED, 
         ARC_LINKER_OPTIONS_UNALIGNED,  
         ARC_COMPILER_OPTIONS_UNALIGNED,     
         ARC_ASM_OPTIONS_QMPYH,  
         ARC_LINKER_OPTIONS_QMPYH,
         ARC_COMPILER_OPTIONS_QMPYH,     
         ARC_ASM_OPTIONS_MACD,
         ARC_LINKER_OPTIONS_MACD, 
         ARC_COMPILER_OPTIONS_MACD,     
         ARC_ASM_OPTIONS_MAC,  
         ARC_LINKER_OPTIONS_MAC,  
         ARC_COMPILER_OPTIONS_MAC, 

         //ARCV2HS FDU
         ARC_COMPILER_OPTIONS_FPUS,
         ARC_COMPILER_OPTIONS_FPUS_DIV,
         ARC_COMPILER_OPTIONS_FPUD,
         ARC_COMPILER_OPTIONS_FPUD_DIV,
         ARC_COMPILER_OPTIONS_FPU_MAC,
         ARC_ASM_OPTIONS_FPUS,
         ARC_ASM_OPTIONS_FPUS_DIV,
         ARC_ASM_OPTIONS_FPUD,
         ARC_ASM_OPTIONS_FPUD_DIV,
         ARC_ASM_OPTIONS_FPU_MAC,    
         ARC_LINKER_OPTIONS_FPUS,
         ARC_LINKER_OPTIONS_FPUS_DIV ,
         ARC_LINKER_OPTIONS_FPUD,
         ARC_LINKER_OPTIONS_FPUD_DIV ,
         ARC_LINKER_OPTIONS_FPU_MAC,
         ARC700_MX,
         ARC700_VRAPTOR,
         //COMPACT LIB FOR EM
         ARC_COMPILER_OPTIONS_HCL,ARC_LINKER_OPTIONS_HCL, ARC_LINKER_OPTIONS_CRTINITCOPY,ARC_LINKER_OPTIONS_CRTINITBSS,ARC_LINKER_OPTIONS_CRTINITCACHE,ARC_LINKER_OPTIONS_CRTINITTIMER,ARC_LINKER_OPTIONS_CRTARGV, ARC_LINKER_OPTIONS_CRTPFMIN,


     };

     /**
      * The names of options that are disabled for ARC601
      */
     private static String ARC601_DISABLED[] = {
         "arc.compiler.options.arc5core",
         "arc.compiler.options.minmax",
         "arc.linker.options.minmax",
         "arc.compiler.options.asm.arc5core",
         ARC_COMPILER_OPTIONS_MPY,
         MPY_CYCLES_COMPILER_OPT,
         "arc.linker.options.mpy",
         // "arc.compiler.options.mul32x16",
         "arc.asm.options.minmax",
         "arc.asm.options.arc5core",
         "arc.compiler.options.arc7core",
         "arc.compiler.options.arc6core", 
         "arc.compiler.options.arcv2emcore",
         ARC_COMPILER_OPTIONS_ARCV2HS,
         "arc.compiler.options.asm.arc6core",
         "arc.compiler.options.asm.arc7core",           
         "arc.compiler.options.asm.arcv2emcore",
         ARC_COMPILER_OPTIONS_ASM_ARCV2HS,
         "arc.asm.options.mpy", 
         //"arc.compiler.options.spfp",
         //"arc.compiler.options.dpfp",
         // "arc.compiler.options.spfpfast",
         // "arc.compiler.options.dpfpfast",
         // "arc.linker.options.spfp", 
         // "arc.linker.options.dpfp",
         //  "arc.linker.options.spfpfast", 
         //"arc.linker.options.dpfpfast",

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
         ARC_COMPILER_OPTIONS_DIV_REM,
         ARC_LINKER_OPTIONS_DIV_REM,
         ARC_ASM_OPTIONS_DIV_REM,
         "arc.compiler.options.cd",
         "arc.linker.options.cd",
         "arc.asm.options.cd",
         "arc.compiler.options.atomic",
         "arc.linker.options.atomic",
         "arc.asm.options.atomic",
         "arc.compiler.options.bitscan",
         "arc.asm.options.bitscan",
         "arc.linker.options.bitscan",
         //ARCV2HS
         ARC_ASM_OPTIONS_LL64, 
         ARC_LINKER_OPTIONS_LL64,  
         ARC_COMPILER_OPTIONS_LL64,     
         ARC_ASM_OPTIONS_UNALIGNED, 
         ARC_LINKER_OPTIONS_UNALIGNED,  
         ARC_COMPILER_OPTIONS_UNALIGNED,     
         ARC_ASM_OPTIONS_QMPYH,  
         ARC_LINKER_OPTIONS_QMPYH,
         ARC_COMPILER_OPTIONS_QMPYH,     
         ARC_ASM_OPTIONS_MACD,
         ARC_LINKER_OPTIONS_MACD, 
         ARC_COMPILER_OPTIONS_MACD,     
         ARC_ASM_OPTIONS_MAC,  
         ARC_LINKER_OPTIONS_MAC,  
         ARC_COMPILER_OPTIONS_MAC, 

         //ARCV2HS FDU
         ARC_COMPILER_OPTIONS_FPUS,
         ARC_COMPILER_OPTIONS_FPUS_DIV,
         ARC_COMPILER_OPTIONS_FPUD,
         ARC_COMPILER_OPTIONS_FPUD_DIV,
         ARC_COMPILER_OPTIONS_FPU_MAC,
         ARC_ASM_OPTIONS_FPUS,
         ARC_ASM_OPTIONS_FPUS_DIV,
         ARC_ASM_OPTIONS_FPUD,
         ARC_ASM_OPTIONS_FPUD_DIV,
         ARC_ASM_OPTIONS_FPU_MAC,    
         ARC_LINKER_OPTIONS_FPUS,
         ARC_LINKER_OPTIONS_FPUS_DIV ,
         ARC_LINKER_OPTIONS_FPUD,
         ARC_LINKER_OPTIONS_FPUD_DIV ,
         ARC_LINKER_OPTIONS_FPU_MAC,
         ARC700_MX,
         ARC700_VRAPTOR,
         //COMPACT LIB FOR EM
         ARC_COMPILER_OPTIONS_HCL,ARC_LINKER_OPTIONS_HCL, ARC_LINKER_OPTIONS_CRTINITCOPY,ARC_LINKER_OPTIONS_CRTINITBSS,ARC_LINKER_OPTIONS_CRTINITCACHE,ARC_LINKER_OPTIONS_CRTINITTIMER,ARC_LINKER_OPTIONS_CRTARGV, ARC_LINKER_OPTIONS_CRTPFMIN,


     };

     /**
      * The names of options that are disabled for ARCv2EM
      */
     private static String ARCV2EM_DISABLED[] = {
         ARC_COMPILER_OPTIONS_ARCV2HS,
         ARC_COMPILER_OPTIONS_ASM_ARCV2HS,
         "arc.compiler.options.arc5core",
         "arc.compiler.options.asm.arc5core",

         // "arc.compiler.options.mul32x16",
         "arc.asm.options.arc5core",
         "arc.compiler.options.arc7core",
         "arc.compiler.options.arc6core", 
         "arc.compiler.options.asm.arc6core",
         "arc.compiler.options.asm.arc7core",           

         /*// FPX is supported for av2!!
            "arc.compiler.options.spfp",
            "arc.compiler.options.dpfp",
            "arc.compiler.options.spfpfast",
            "arc.compiler.options.dpfpfast",
            "arc.linker.options.spfp", 
            "arc.linker.options.dpfp",
            "arc.linker.options.spfpfast", 
            "arc.linker.options.dpfpfast",
          */            
         "arc.compiler.options.xmac24",
         "arc.compiler.options.xmacd16",
         "arc.compiler.options.dmulpf",
         "arc.compiler.options.mul32x16",
         "arc.compiler.options.ea",
         "arc.compiler.options.crc",
         "arc.compiler.options.dvbf",
         MULT32,
         MULT32_CYCLES,

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

         "arc.linker.options.mult32",
         "arc.asm.options.mult32",

         XY_MEMORY_ID,
         "arc.asm.options.xy",
         "arc.linker.options.xy",

         "arc.compiler.options.norm",
         "arc.asm.options.norm",
         "arc.linker.options.norm",

         ARC_ASM_OPTIONS_LL64, 
         ARC_LINKER_OPTIONS_LL64,  
         ARC_COMPILER_OPTIONS_LL64,     
         ARC_ASM_OPTIONS_UNALIGNED, 
         ARC_LINKER_OPTIONS_UNALIGNED,  
         ARC_COMPILER_OPTIONS_UNALIGNED,     
         ARC_ASM_OPTIONS_QMPYH,  
         ARC_LINKER_OPTIONS_QMPYH,
         ARC_COMPILER_OPTIONS_QMPYH,     
         ARC_ASM_OPTIONS_MACD,
         ARC_LINKER_OPTIONS_MACD, 
         ARC_COMPILER_OPTIONS_MACD,     
         ARC_ASM_OPTIONS_MAC,  
         ARC_LINKER_OPTIONS_MAC,  
         ARC_COMPILER_OPTIONS_MAC, 

         //ARCV2HS FDU
         ARC_COMPILER_OPTIONS_FPUS,
         ARC_COMPILER_OPTIONS_FPUS_DIV,
         ARC_COMPILER_OPTIONS_FPUD,
         ARC_COMPILER_OPTIONS_FPUD_DIV,
         ARC_COMPILER_OPTIONS_FPU_MAC,
         ARC_ASM_OPTIONS_FPUS,
         ARC_ASM_OPTIONS_FPUS_DIV,
         ARC_ASM_OPTIONS_FPUD,
         ARC_ASM_OPTIONS_FPUD_DIV,
         ARC_ASM_OPTIONS_FPU_MAC,    
         ARC_LINKER_OPTIONS_FPUS,
         ARC_LINKER_OPTIONS_FPUS_DIV ,
         ARC_LINKER_OPTIONS_FPUD,
         ARC_LINKER_OPTIONS_FPUD_DIV ,
         ARC_LINKER_OPTIONS_FPU_MAC,
         ARC700_MX,
         ARC700_VRAPTOR,
     };


     /**
      * The names of options that are disabled for ARCv2HS
      */
     private static String ARCV2HS_DISABLED[] = {
         "arc.compiler.options.arcv2emcore",
         "arc.compiler.options.asm.arcv2emcore",
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

         ARC_ASM_OPTIONS_SPFP,
         ARC_ASM_OPTIONS_SPFP_FAST,
         ARC_ASM_OPTIONS_DPFP,
         ARC_ASM_OPTIONS_DPFP_FAST,

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
         MPY_CYCLES_COMPILER_OPT,

         MULT32, MULT32_CYCLES,
         XY_MEMORY_ID,
         "arc.asm.options.xy",
         "arc.linker.options.xy",

         "arc.compiler.options.norm",
         "arc.asm.options.norm",
         "arc.linker.options.norm",
         // HS not supported for EM
         "arc.compiler.options.bs",
         "arc.linker.options.bs",
         "arc.asm.options.bs",

         "arc.compiler.options.sa",
         "arc.linker.options.sa",
         "arc.asm.options.sa",

         "arc.compiler.options.cd",
         "arc.linker.options.cd",
         "arc.asm.options.cd",

         "arc.compiler.options.bitscan",

         "arc.linker.options.bitscan",
         "arc.asm.options.bitscan",

         "arc.compiler.options.swap",
         "arc.linker.options.swap",
         "arc.asm.options.swap",

         "arc.compiler.options.atomic",
         "arc.linker.options.atomic",
         "arc.asm.options.atomic",

         "arc.linker.options.mult32",
         "arc.asm.options.mult32",

         //pc lpc
         "arc.compiler.options.pc_width",
         "arc.linker.options.pc_width",
         "arc.compiler.options.lpc_width",
         ARC700_MX,
         ARC700_VRAPTOR,

         //COMPACT LIB FOR EM
         ARC_COMPILER_OPTIONS_HCL,ARC_LINKER_OPTIONS_HCL, ARC_LINKER_OPTIONS_CRTINITCOPY,ARC_LINKER_OPTIONS_CRTINITBSS,ARC_LINKER_OPTIONS_CRTINITCACHE,ARC_LINKER_OPTIONS_CRTINITTIMER,ARC_LINKER_OPTIONS_CRTARGV, ARC_LINKER_OPTIONS_CRTPFMIN,
     };

     // default value on ARCV2HS
     private static String ARCV2HS_DEFAULT[] = {

         // HS default value to be true
         "arc.compiler.options.bs",
         "arc.linker.options.bs",
         "arc.asm.options.bs",

         "arc.compiler.options.sa",
         "arc.linker.options.sa",
         "arc.asm.options.sa",

         "arc.compiler.options.cd",
         "arc.linker.options.cd",
         "arc.asm.options.cd",

         "arc.compiler.options.bitscan",

         "arc.linker.options.bitscan",
         "arc.asm.options.bitscan",

         "arc.compiler.options.swap",
         "arc.linker.options.swap",
         "arc.asm.options.swap",

         "arc.compiler.options.atomic",
         "arc.linker.options.atomic",
         "arc.asm.options.atomic",

     };
     private static Set<String> DISABLED_FOR_ARC600 = new HashSet<String>(Arrays.asList(ARC6_DISABLED));
     private static Set<String> DISABLED_FOR_ARC601 = new HashSet<String>(Arrays.asList(ARC601_DISABLED));
     private static Set<String> DISABLED_FOR_ARCV2EM = new HashSet<String>(Arrays.asList(ARCV2EM_DISABLED));
     private static Set<String> DISABLED_FOR_ARCV2HS = new HashSet<String>(Arrays.asList(ARCV2HS_DISABLED));
     private static Set<String> DEFAULT_FOR_ARCV2HS = new HashSet<String>(Arrays.asList(ARCV2HS_DEFAULT));

     public static boolean isApplicableToToolChain(IOption opt, IToolChain toolChain){
         String baseID = toolChain.getBaseId();
         if (baseID.indexOf("av2em") > 0){
             String id = opt.getBaseId();
             if (id.endsWith(".version")) return false;
             return !DISABLED_FOR_ARCV2EM.contains(id);
         }
         if (baseID.indexOf("av2hs") > 0){
             String id = opt.getBaseId();
             if (id.endsWith(".version")) return false;
             return !DISABLED_FOR_ARCV2HS.contains(id);
         } 
         if (baseID.indexOf(".arc5") > 0){
             String id = opt.getBaseId();
             if (id.endsWith(".version")) return false;
             return !DISABLED_FOR_ARC5.contains(id);
         } 
         if (baseID.indexOf(".arc600") > 0){
             String id = opt.getBaseId();
             if (id.endsWith(".version")) return false;
             return !DISABLED_FOR_ARC600.contains(id);
         } 
         if (baseID.indexOf(".arc700") > 0){
             String id = opt.getBaseId();
             if (id.endsWith(".version")) return false;
             return !DISABLED_FOR_ARC700.contains(id);
         } 
         if (baseID.indexOf(".arc601") > 0){
             String id = opt.getBaseId();
             if (id.endsWith(".version")) return false;
             return !DISABLED_FOR_ARC601.contains(id);
         } 
         return true;
     }

     /**
      * The names of options that are disabled for ARC700
      */
     private static String ARC7_DISABLED[] = {
         "arc.compiler.options.arc5core",
         "arc.compiler.options.arc6core",
         "arc.compiler.options.arcv2emcore",
         ARC_COMPILER_OPTIONS_ARCV2HS,
         "arc.compiler.options.minmax",
         "arc.compiler.options.asm.arc5core",
         "arc.compiler.options.asm.arc6core",
         "arc.compiler.options.asm.arcv2emcore",
         ARC_COMPILER_OPTIONS_ASM_ARCV2HS,
         MULT32, MULT32_CYCLES,
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
         ARC_COMPILER_OPTIONS_DIV_REM,
         ARC_LINKER_OPTIONS_DIV_REM,
         ARC_ASM_OPTIONS_DIV_REM,
         "arc.compiler.options.cd",
         "arc.linker.options.cd",
         "arc.asm.options.cd",
         "arc.compiler.options.atomic",
         "arc.linker.options.atomic",
         "arc.asm.options.atomic",
         //CR 9000576154
         ARC_ASM_OPTIONS_MPY16,
         ARC_LINKER_OPTIONS_MPY16,
         ARC_COMPILER_OPTIONS_MPY16,
         //ARCV2HS
         ARC_ASM_OPTIONS_LL64, 
         ARC_LINKER_OPTIONS_LL64,  
         ARC_COMPILER_OPTIONS_LL64,     
         ARC_ASM_OPTIONS_UNALIGNED, 
         ARC_LINKER_OPTIONS_UNALIGNED,  
         ARC_COMPILER_OPTIONS_UNALIGNED,     
         ARC_ASM_OPTIONS_QMPYH,  
         ARC_LINKER_OPTIONS_QMPYH,
         ARC_COMPILER_OPTIONS_QMPYH,     
         ARC_ASM_OPTIONS_MACD,
         ARC_LINKER_OPTIONS_MACD, 
         ARC_COMPILER_OPTIONS_MACD,     
         ARC_ASM_OPTIONS_MAC,  
         ARC_LINKER_OPTIONS_MAC,  
         ARC_COMPILER_OPTIONS_MAC, 

         //ARCV2HS FDU
         ARC_COMPILER_OPTIONS_FPUS,
         ARC_COMPILER_OPTIONS_FPUS_DIV,
         ARC_COMPILER_OPTIONS_FPUD,
         ARC_COMPILER_OPTIONS_FPUD_DIV,
         ARC_COMPILER_OPTIONS_FPU_MAC,
         ARC_ASM_OPTIONS_FPUS,
         ARC_ASM_OPTIONS_FPUS_DIV,
         ARC_ASM_OPTIONS_FPUD,
         ARC_ASM_OPTIONS_FPUD_DIV,
         ARC_ASM_OPTIONS_FPU_MAC,    
         ARC_LINKER_OPTIONS_FPUS,
         ARC_LINKER_OPTIONS_FPUS_DIV ,
         ARC_LINKER_OPTIONS_FPUD,
         ARC_LINKER_OPTIONS_FPUD_DIV ,
         ARC_LINKER_OPTIONS_FPU_MAC,

         //COMPACT LIB FOR EM
         ARC_COMPILER_OPTIONS_HCL,ARC_LINKER_OPTIONS_HCL, ARC_LINKER_OPTIONS_CRTINITCOPY,ARC_LINKER_OPTIONS_CRTINITBSS,ARC_LINKER_OPTIONS_CRTINITCACHE,ARC_LINKER_OPTIONS_CRTINITTIMER,ARC_LINKER_OPTIONS_CRTARGV, ARC_LINKER_OPTIONS_CRTPFMIN,



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
     
     private static Set<String> DISABLED_FOR_ARC700 = new HashSet<String>(Arrays.asList(ARC7_DISABLED));
     
     private static Set<String> getDisabledSetForVersion(String v){
         if (v.equals(VERSION_AV2HS))
             return DISABLED_FOR_ARCV2HS;
         if (v.equals(VERSION_AV2EM))
             return DISABLED_FOR_ARCV2EM;
         if (v.equals(VERSION_ARC700))
             return DISABLED_FOR_ARC700;
         if (v.equals(VERSION_ARC600))
             return DISABLED_FOR_ARC600;
         if (v.equals(VERSION_ARC601))
             return DISABLED_FOR_ARC601;
         if (v.equals(VERSION_ARC5))
             return DISABLED_FOR_ARC5;
         throw new IllegalArgumentException("Unknown version : " + v);
     }
     /**
      * Disable non-ccac stuff in ccac-based targets.
      * Disable non-mcc stuff in mcc-based targets
      */
     static {
         HashSet<String> clangSet = new HashSet<String>(Arrays.asList(CLANG_BASED_TARGETS));
         for (String v: ALL_VERSIONS) {
             Set<String> disabledSet = getDisabledSetForVersion(v);
             if (clangSet.contains(v)){
                 for (String s: CCAC_DISABLED)
                     disabledSet.add(s);
             }
             else
             for (String s: MCC_DISABLED){
                 disabledSet.add(s);
             }
         }
     }
     
     private static Set<String> ALL_TARGET_DEPENDENT = new HashSet<String>();
     
     static {
         ALL_TARGET_DEPENDENT.addAll(DISABLED_FOR_ARC5);
         ALL_TARGET_DEPENDENT.addAll(DISABLED_FOR_ARC600);
         ALL_TARGET_DEPENDENT.addAll(DISABLED_FOR_ARC601);
         ALL_TARGET_DEPENDENT.addAll(DISABLED_FOR_ARC700);
         ALL_TARGET_DEPENDENT.addAll(DISABLED_FOR_ARCV2EM);
         ALL_TARGET_DEPENDENT.addAll(DISABLED_FOR_ARCV2HS);
     }

     private static String ARC5_XLIB_SETTINGS[] = {
         "arc.compiler.options.norm",
         "arc.compiler.options.swap",
         MULT32,
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
         MULT32,
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
         ARC_COMPILER_OPTIONS_MPY16,
         ARC_COMPILER_OPTIONS_DIV_REM,
         "arc.compiler.options.cd",

         "arc.linker.options.bitscan",
         "arc.linker.options.swap",
         "arc.linker.options.mpy",
         ARC_LINKER_OPTIONS_MPY16,
         ARC_LINKER_OPTIONS_DIV_REM,
         "arc.linker.options.cd",

         "arc.asm.options.bitscan",
         "arc.asm.options.swap",
         "arc.asm.options.mpy",
         ARC_ASM_OPTIONS_MPY16,
         ARC_ASM_OPTIONS_DIV_REM,
         "arc.asm.options.cd",

         "arc.compiler.options.bs",
         "arc.linker.options.bs",
         "arc.asm.options.bs",
     };


     private static String ARCV2HS_XLIB_SETTINGS[] = {

         "arc.compiler.options.mpy",
         ARC_COMPILER_OPTIONS_DIV_REM,
         ARC_COMPILER_OPTIONS_LL64,

         "arc.linker.options.mpy",
         ARC_LINKER_OPTIONS_DIV_REM,
         ARC_LINKER_OPTIONS_LL64,

         "arc.asm.options.mpy",
         ARC_ASM_OPTIONS_DIV_REM,
         ARC_ASM_OPTIONS_LL64,


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
                     ArcOptionEnablementManager.this.setEnabled(LINKER_CCAC_OPTION_CPPLIB, b);
                 }
                 else if (COMPILER_OPTIONS_EXCEPT.equals(optionId)){
                     // If -Hstl, then set the appropriate library
                     boolean b = mgr.getValue(optionId).equals(Boolean.TRUE);
                     String lib = (String)ArcOptionEnablementManager.this.getValue(LINKER_OPTION_CPPLIB);
                     if (lib != null && lib.indexOf("stl") >= 0 && (b != (lib.indexOf("stlexcept") > 0))){
                         ArcOptionEnablementManager.this.setOptionValue(LINKER_OPTION_CPPLIB, b?STLLIB_WITH_EXCEPTIONS:STLLIB_WITHOUT_EXCEPTIONS);
                     }
                     lib = (String)ArcOptionEnablementManager.this.getValue(LINKER_CCAC_OPTION_CPPLIB);
                     if (lib != null && lib.indexOf("stl") >= 0 && (b != (lib.indexOf("stlexcept") > 0))){                        
                         ArcOptionEnablementManager.this.setOptionValue(LINKER_CCAC_OPTION_CPPLIB, b?STLLIB_CCAC_WITH_EXCEPTIONS:STLLIB_CCAC_WITHOUT_EXCEPTIONS);                       
                     }
                 }
             }

             public void onOptionEnablementChanged (IOptionEnablementManager mgr, String optionID) {
                 // TODO Auto-generated method stub               
             }});


     }
     
     private static final String CFG_OPTION_ID = "arc.compiler.options.cfg";
     private static final String BCF_OPTION_ID = "arc.compiler.options.bcf";


     @Override
     public void initialize(IBuildObject config) {
         // TODO Auto-generated method stub
         super.initialize(config);
         IToolChain tc = getToolChain();
         if (tc != null) {
             String id = tc.getBaseId();
             Object oldVersion = this.getValue(COMPILER_VERSION);
             String version = null;
             // If the project type is specific to a particular ARC processor,
             // Force the version.
             if (id.indexOf("av2em") > 0)
                 version = VERSION_AV2EM;
             else if (id.indexOf("av2hs") > 0)
                 version = VERSION_AV2HS;
             else if (id.indexOf("arc600") > 0)
                 version = VERSION_ARC600;
             else if (id.indexOf("arc700") > 0)
                 version = VERSION_ARC700;
             else if (id.indexOf("arc601") > 0)
                 version = VERSION_ARC601;
             else if (id.indexOf(".arc5") > 0)
                 version = VERSION_ARC5;
             if (version != null) {
                 this.setOptionValue(COMPILER_VERSION, version);
                 if (isClangBased(version)) {
                     ApplicabilityCalculator.setClangBased(true);
                     if (oldVersion == null ||!isClangBased(oldVersion.toString())){
                         // If switched from mcc to ccac, then correct defaults.
                         // -Hcppmw is now disabled.
                         setOptionValue(COMPILER_OPTIONS_MWCPP,Boolean.FALSE);
                     }
                 }
                 else {
                     ApplicabilityCalculator.setClangBased(false);
                     if (oldVersion == null ||!isClangBased(oldVersion.toString())){
                         // If switched from ccac to mcc, then correct defaults.
                         // -Hstl is now disabled.
                         setOptionValue(COMPILER_OPTIONS_STL,Boolean.FALSE);
                     }
                 }
             }
             if (id.indexOf("em4") > 0){
                 ApplicabilityCalculator.setMapOptionID(EM4_MAP_OPTION_ID);
             }
             else ApplicabilityCalculator.setMapOptionID(OptionEnablementManager.MAP_OPTION_ID);
         }
         // The .cfg path is stored here. We don't want it visible or enabled.
         this.setEnabled(CFG_OPTION_ID, false);
         this.setEnabled(BCF_OPTION_ID, false);

     }

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
     
     private static boolean isClangBased(String version){
         for (String s: CLANG_BASED_TARGETS){
             if (s.equals(version)) return true;
         }
         return false;
     }
     
    // private static final String ARC_GENERIC_EXE_PROJECT_TYPE = "com.arc.cdt.toolchain.arc.exeProject";
    // private static final String ARC_GENERIC_LIB_PROJECT_TYPE = "com.arc.cdt.toolchain.arc.libProject";
     private static final String ARC_GENERIC_EXE_TC_PREFIX = "com.arc.cdt.toolchain.arc.exe";
     private static final String ARC_GENERIC_LIB_TC_PREFIX = "com.arc.cdt.toolchain.arc.lib";

     
     private static boolean isArcGeneric(IToolChain tc){
         //IProjectType pt = tc.getParent().getProjectType();  -- could be NULL
         //return  pt.getBaseId().equals(ARC_GENERIC_EXE_PROJECT_TYPE) ||
         //    pt.getBaseId().equals(ARC_GENERIC_LIB_PROJECT_TYPE);
         
         String id = tc.getBaseId();
         return id.startsWith(ARC_GENERIC_EXE_TC_PREFIX) || id.startsWith(ARC_GENERIC_LIB_TC_PREFIX);
     }

     class Observer implements IOptionEnablementManager.IObserver {



         private static final String LINKER_OPTION_LIB_CCAC_STLNOEXCEPT = "linker.option.lib.ccac.stlnoexcept";
        private static final String LINKER_OPTION_LIB_CCAC_MWCPP = "linker.option.lib.ccac.mwcpp";
        private String lastVersion = null;

         /**
          * Called when an option value changes. Enable or disable any options that are dependent on this one.
          * @param mgr
          * @param optionId
          */
         public void onOptionValueChanged (IOptionEnablementManager mgr, String optionId) {
             String targetValue = "";
             Set<String> disabledSet = null;
             Set<String> defaultSet = null;
             boolean enableBS = false;
             boolean checkDSP = false;
             boolean checkFP = false;

             //TN: look like this is duplicated - so removing it
             /***if (optionId.equals(ARC5_CORE)) {
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
            }**/
             
             
             if (optionId.endsWith(".target.version")) {

                 String value = (String) mgr.getValue(optionId);
                 targetValue = value;
                 // If clang based, then so indicate...
                 ApplicabilityCalculator.setClangBased(isClangBased(value));
                 
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
                     toMCC();
                 }
                 if (value.endsWith("arc5")) {
                     disabledSet = DISABLED_FOR_ARC5;
                     if (mgr.getValue(ARC5_CORE) != null && mgr.getValue(ARC5_CORE).toString().endsWith("core1")) {
                         disabledSet = DISABLED_FOR_ARC5_CORE1;
                         enableBS = false;
                     }
                     toMCC();
                 }
                 else if (value.endsWith("arc600")) {
                     disabledSet = DISABLED_FOR_ARC600;
                     checkFP = true;
                     toMCC();
                 }
                 else if (value.endsWith("arc601")) {
                     disabledSet = DISABLED_FOR_ARC601;
                     checkFP = false;
                     enableBS = true;
                     toMCC();
                 }
                 else if (value.endsWith("arc700")) {
                     checkFP = true;
                     setEnabled(ARC700_MX,true);
                     setEnabled(XLIB,true); // -xlib unconditionally enabled.
                     setDSPEnabled(MUL32x16,true);  // DSP options unconditionally enabled under ARC700
                     setDSPEnabled(XMACD16,true);
                     setOptionValue(XMAC24,false);
                     setDSPEnabled(XMAC24,false);
                     disabledSet = DISABLED_FOR_ARC700;
                     for (String option: IMPLICITLY_SET_FOR_ARC700) {
                         setOptionValue(option,Boolean.TRUE);
                     }
                     // "mult32" is not under ARC700. Turn it off to avoid confusing user. (cr92726)	
                     setOptionValue(MULT32,false);
                     toMCC();
                 }
                 else if (value.endsWith("arcv2em")){
                     //CR:900064842 arcv2em support FPX
                     checkFP = true;
                     checkDSP = false;
                     disabledSet = DISABLED_FOR_ARCV2EM;
                     toCCAC();
                     // TN move this after disabledSet finished their work to reserve hierachical disable options
                     //checkMPY(value);
                 }
                 else if (value.endsWith("arcv2hs")){
                     checkFP = false;
                     checkDSP = false;
                     disabledSet = DISABLED_FOR_ARCV2HS;
                     defaultSet =  DEFAULT_FOR_ARCV2HS;
                     toCCAC();
                     // TN move this after disabledSet finished their work to reserve hierachical disable options
                     //checkMPY(value);
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
                         
             else if ( isEnabled(optionId)) {
            	 
                 /* TN – optionId needs to be in enable state before we act anything on it  as the disabledSet will be processed before this*/
            	 if (optionId.indexOf("fpu") >0) { 
                	 // Ignore processing fpu_mac option
                	 if (!optionId.endsWith("fpu_mac")){
                		 //fpus, fpus_div, fpud, fpud_div
                		 if(!handleFPUMAC()){ //clear fpu_mac if none of other FPU is enabled
                			Object fpu_mac_value = getValue(ARC_COMPILER_OPTIONS_FPU_MAC);
                			if(fpu_mac_value != null && fpu_mac_value.equals(Boolean.TRUE))
                		        set (ARC_COMPILER_OPTIONS_FPU_MAC, Boolean.FALSE);
                	
                		 }
                	 }
            	 }
                 if (optionId.endsWith("isDashG")) {
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
                     setEnabled(ARC_ASM_OPTIONS_SPFP,!fastFP);

                 }
                 else if (optionId.endsWith("dpfpfast")){
                     boolean fastFP = mgr.getValue(optionId).equals(Boolean.TRUE);
                     setEnabled("arc.linker.options.dpfp",!fastFP);
                     setEnabled("arc.compiler.options.dpfp",!fastFP);
                     setEnabled(ARC_ASM_OPTIONS_DPFP,!fastFP);
                 }
                 else if (optionId.endsWith(".spfp")){
                     boolean fastFP = mgr.getValue(optionId).equals(Boolean.TRUE);
                     setEnabled("arc.linker.options.spfpfast",!fastFP);
                     setEnabled("arc.compiler.options.spfpfast",!fastFP);
                     setEnabled( ARC_ASM_OPTIONS_SPFP_FAST,!fastFP);
                 }
                 else if (optionId.endsWith(".stl")){
                     boolean v = mgr.getValue(optionId).equals(Boolean.TRUE);
                     if (v) {
                         boolean except = Boolean.TRUE.equals(com.arc.cdt.toolchain.ApplicabilityCalculator.getOptionEnablementManager().getValue(COMPILER_OPTIONS_EXCEPT));
                         setOptionValue(LINKER_OPTION_CPPLIB,
                                 except?STLLIB_WITH_EXCEPTIONS:STLLIB_WITHOUT_EXCEPTIONS);
                         setOptionValue(COMPILER_OPTIONS_MWCPP,false);
                         setOptionValue(LINKER_CCAC_OPTION_CPPLIB,except?"linker.option.lib.ccac.stlexcept":LINKER_OPTION_LIB_CCAC_STLNOEXCEPT);
                     }
                     else {
                         setOptionValue(COMPILER_OPTIONS_MWCPP,true);
                         setOptionValue(LINKER_OPTION_CPPLIB,NO_STLLIB);
                         setOptionValue(LINKER_CCAC_OPTION_CPPLIB,LINKER_OPTION_LIB_CCAC_MWCPP);
                     }
                 }
                 else if (optionId.equals(COMPILER_OPTIONS_MWCPP)){
                     boolean v = mgr.getValue(optionId).equals(Boolean.TRUE);
                     setOptionValue(COMPILER_OPTIONS_STL,!v);
                     if (v)
                         setOptionValue(LINKER_CCAC_OPTION_CPPLIB,LINKER_OPTION_LIB_CCAC_MWCPP);
                     else if (LINKER_OPTION_LIB_CCAC_MWCPP.equals(mgr.getValue(LINKER_CCAC_OPTION_CPPLIB))){
                         setOptionValue(LINKER_CCAC_OPTION_CPPLIB,LINKER_OPTION_LIB_CCAC_STLNOEXCEPT);
                     }
                 }
                 else if (optionId.equals(LINKER_OPTION_CPPLIB) || optionId.equals(LINKER_CCAC_OPTION_CPPLIB)){
                     // If stl library chosen, then add -Hstl to the compiler.
                     String lib = (String)ArcOptionEnablementManager.this.getValue(optionId);
                     if (lib != null) {
                         setOptionValue(COMPILER_OPTIONS_STL,lib.indexOf("stl") >= 0?Boolean.TRUE:Boolean.FALSE);  
                         setOptionValue(COMPILER_OPTIONS_MWCPP,lib.indexOf("stl") < 0?Boolean.TRUE:Boolean.FALSE);  

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
                     setEnabled(ARC_ASM_OPTIONS_DPFP_FAST,!fastFP);
                 }
                 else if (optionId.endsWith(".aom")){
                     boolean aom = mgr.getValue(optionId).equals(Boolean.TRUE);
                     setOverlayOptions(mgr,aom);
                 }
                 else if (optionId.endsWith(".mult32_cycles") || optionId.equals(MULT32)) {
                     setEnabled(MULT32_CYCLES,mgr.getValue(MULT32).equals(Boolean.TRUE) && mgr.isEnabled(MULT32));
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
                 else if (optionId.endsWith(".qmpyh") || optionId.endsWith(".macd") || optionId.endsWith(".mac") || optionId.endsWith(".mpy")){
                     String version = getVersion(mgr);
                     checkMPY(version);
                     handleMPYCycles(version);
                 }
                 else if (optionId.endsWith(".hcl")){
                     Boolean isHclSelectedObj = (Boolean)getValue(ARC_COMPILER_OPTIONS_HCL) ;
                     boolean isHclSelected = (isHclSelectedObj != null)? isHclSelectedObj.booleanValue():false;
                     for(String optCrtId:ARC_COMPACT_LIB_CRT_SETTINGS){
                         setEnabled(optCrtId,isHclSelected);
                     }
                 }

                 boolean xy = Boolean.TRUE.equals(mgr.getValue(XY_MEMORY_ID));
                 setEnabled(DMULPF,xy);

             } // TN - just move a block of code inside if enable condition



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

             // TN: As so far xlib is always enable for all Targe, we don't need to check if the optionId is enabled.
             if (optionId.endsWith(".xlib")) {
                 setXlibProps(optionId, version);               
             }

             // TN: this calls only when target changing 
             if (disabledSet != null) {
                 // Turn on any option not in the set.
                 for (String id : ALL_TARGET_DEPENDENT) {
                     if (!disabledSet.contains(id) && !xlibDisabled.contains(id)
                             && !dspDisabled.contains(id)) {
                         setEnabled(id, true);
                     }
                 }
                 if (!disabledSet.contains(MULT32)) {
                     setEnabled(MULT32_CYCLES,
                             Boolean.TRUE.equals(mgr.getValue(MULT32)));
                 }

                 // TN:now set true for default value- Need to set this before
                 // setDisabled.

                 if (defaultSet != null) {
                     for (String id : defaultSet) {
                         setOptionValue(id, Boolean.TRUE);
                     }
                 }
                 // Now disable all options in the set.

                 for (String id : disabledSet) {
                     setEnabled(id, false);
                 }

                 // TN: move checkMPY after disabledSet running
                 if (disabledSet == DISABLED_FOR_ARCV2EM
                         || disabledSet == DISABLED_FOR_ARCV2HS)
                     checkMPY(targetValue);
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
                 setEnabled(ARC_ASM_OPTIONS_SPFP,!fastFP);
                 if (!fastFP) {
                     fastFP = Boolean.TRUE.equals(mgr.getValue("arc.compiler.options.spfp"));
                     setEnabled("arc.linker.options.spfpfast",!fastFP);
                     setEnabled("arc.compiler.options.spfpfast",!fastFP);
                     setEnabled(ARC_ASM_OPTIONS_SPFP_FAST,!fastFP);

                 }
                 fastFP = Boolean.TRUE.equals(mgr.getValue("arc.compiler.options.dpfpfast"));
                 setEnabled("arc.linker.options.dpfp",!fastFP);
                 setEnabled("arc.compiler.options.dpfp",!fastFP);
                 setEnabled(ARC_ASM_OPTIONS_DPFP,!fastFP);
                 if (!fastFP){
                     fastFP = Boolean.TRUE.equals(mgr.getValue("arc.compiler.options.dpfp"));
                     setEnabled("arc.linker.options.dpfpfast",!fastFP);
                     setEnabled("arc.compiler.options.dpfpfast",!fastFP);
                     setEnabled(ARC_ASM_OPTIONS_DPFP_FAST,!fastFP);
                 }
             }
         }


        private boolean changeCommand(String from, String to) {
            // Programmatically set the "command" to "ccac" if it is set to "mcc"
             IToolChain tc = getToolChain();
             boolean result = false;
             if (tc != null && isArcGeneric(tc)){
                 IConfiguration config = tc.getParent();
                 for (ITool tool: tc.getTools()){
                     if (from.equals(config.getToolCommand(tool))){
                         config.setToolCommand(tool, to);
                         result = true;
                     }
                 }
             }
             return result;
        }
        
        private void toCCAC(){
            if (changeCommand("mcc","ccac")){
                setOptionValue(COMPILER_OPTIONS_MWCPP,false);
                setOptionValue(COMPILER_OPTIONS_STL,true);
            }
        }
        private void toMCC(){
            if (changeCommand("ccac","mcc")){
                setOptionValue(COMPILER_OPTIONS_MWCPP,true);
                setOptionValue(COMPILER_OPTIONS_STL,false);
            }
        }

         
         private boolean handleFPUMAC(){
        	 Object fpus_value     = getValue(ARC_COMPILER_OPTIONS_FPUS);
    		 Object fpus_div_value = getValue(ARC_COMPILER_OPTIONS_FPUS_DIV );
    		 Object fpud_value     = getValue(ARC_COMPILER_OPTIONS_FPUD);
    	     Object fpud_div_value = getValue(ARC_COMPILER_OPTIONS_FPUD_DIV);
    	    
    		 if( (fpus_value != null  && fpus_value.equals(Boolean.TRUE)) ||
    			 (fpus_div_value != null && fpus_div_value.equals(Boolean.TRUE)) || 
    			 (fpud_value != null  &&  fpud_value.equals(Boolean.TRUE)) || 
    			 (fpud_div_value != null && fpud_div_value.equals(Boolean.TRUE)) ){
    		 
    			 setEnabled("arc.compiler.options.fpu_mac", Boolean.TRUE);
    			 return true;
    		 
    		 }else{
    			 setEnabled("arc.compiler.options.fpu_mac", Boolean.FALSE);
    			 return false;
    			 
    		 }
    	 
         }

         private boolean isXlibSelected (){

             Boolean cValue = (Boolean)getValue("arc.compiler.options.xlib") ;
             Boolean lValue = (Boolean)getValue("arc.linker.options.xlib") ;
             Boolean aValue = (Boolean)getValue("arc.asm.options.xlib") ;
             if( cValue != null && cValue.booleanValue())
                 return true;
             if( lValue != null && lValue.booleanValue())
                 return true;
             if( aValue != null && aValue.booleanValue())
                 return true;
             return false;
         }

         // ARCV2HS same as MPY as ARCV2EM
         private void checkMPY(String version){

             if (version != null && version.indexOf("arcv2") >= 0 ){

                 if( version.indexOf("hs") >0) {// arcv2hs or acv2hs
                     boolean allowSetEnable = !isXlibSelected(); // if Xlib selected - not allow to set Enable
                     handleHirachicalOptsSelection(ARC_COMPILER_OPTIONS_QMPYH, MACD_OPTS, true);
                     handleHirachicalOptsSelection(ARC_COMPILER_OPTIONS_MACD, MAC_OPTS, true);
                     handleHirachicalOptsSelection(ARC_COMPILER_OPTIONS_MAC, MPY_OPTS, allowSetEnable);
                     handleHirachicalOptsSelection (ARC_COMPILER_OPTIONS_MPY, MPY16_OPTS, true);


                 }
                 else // arcv2em
                     //Disable -mpy16 if -mpy is specified
                     handleHirachicalOptsSelection (ARC_COMPILER_OPTIONS_MPY, MPY16_OPTS, true);


             }
         }

         private boolean handleHirachicalOptsSelection (String srcOpt, String [] destOpts, boolean allowSetEnable){
             Boolean v = (Boolean)getValue(srcOpt);
             if (v != null) {
                 for (String s: destOpts) {
                     if (v) 
                         setOptionValue(s,true);
                     if(allowSetEnable)
                         setEnabled(s,!v);
                 }
                 return v; // could be true or false - true mean srcOpt is selected
             }

             return false;
         }
         // enable mycycles if MPY is selected
         private void handleMPYCycles(String version){

             Boolean v = (Boolean)getValue(ARC_COMPILER_OPTIONS_MPY);
             if(v == null)
                 return;
             boolean mpyCyclesSupported = version != null && (version.endsWith("arc700") || version.indexOf("arcv2") >= 0);
             boolean isEnabled = v & mpyCyclesSupported;
             // not allow MPY CYCLES for HS
             if(version != null && version.indexOf("arcv2hs") <0 )
                 setEnabled(MPY_CYCLES_COMPILER_OPT, isEnabled);

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
                 else if (version.endsWith("arcv2hs")){
                     xlibOptions = ARCV2HS_XLIB_SETTINGS;
                 }
             }

             if (xlibOptions != null){
                 //TN: This optionId must be .xlib
                 Boolean V = (Boolean)getValue(optionId);

                 if (V != null)  {
                     boolean isXlibSelected  = V.booleanValue();
                     boolean mpySupported = version != null && (version.endsWith("arc700") || version.indexOf("arcv2") >= 0);
                     for (String s: xlibOptions){
                         if (isXlibSelected) {
                             // "div_rem" is the only setting that isn't boolean.
                             if (!s.endsWith("div_rem"))
                                 setOptionValue(s,isXlibSelected && (mpySupported || !s.endsWith(".mpy")));
                             else if (version != null && version.indexOf("arcv2") >= 0) {
                                 if(version.indexOf("arcv2hs") >= 0)
                                     setOptionValue(s,"arc.options.divrem.radix4");
                                 else
                                     setOptionValue(s,"arc.options.divrem.radix2");
                             }
                             else setOptionValue(s,"arc.options.divrem.none");
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
