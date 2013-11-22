package com.arc.cdt.toolchain.internal.ui.bcf.properties.metaware;

import com.arc.cdt.toolchain.ui.bcf.ProcessorFamily;

/**
 * NOTE: these constants must match those in the plugin.xml files of
 * com.arc.cdt.toolchain and com.arc.cdt.toolchain.arc
 * 
 * Since thus plugin is used by the ARC GNU version of the IDE, we must not have
 * dependencies on MetaWare plugins.
 * 
 * @author pickensd
 * 
 */
public class MWConstants {
    /**
     * NOTE: this string must match ISeeCodeLaunchConfigurationConstants.ATTR_GUIHILI_PROPERTIES from 
     * the com.arc.cdt.debug.seecode.core plugin.
     */
    public static final String ATTR_LAUNCH_GUIHILI = "com.arc.cdt.debug.seecode.core.GUIHILI_PROPS";
    public static final String ATTR_LAUNCH_SWAHILI_ARGS = "com.arc.cdt.debug.seecode.core.SWAHILI_ARGS";

    public static final String COMPILER_VERSION_ID = "arc.compiler.options.target.version";
    public static final String LINKER_VERSION_ID = "arc.linker.options.target.version";
    public static final String ASM_VERSION_ID = "arc.compiler.options.target.asm.version";

    public static final String VERSION_A5_ID = "arc.compiler.options.target.arc5";
    public static final String VERSION_ARC600_ID = "arc.compiler.options.target.arc600";
    public static final String VERSION_ARC601_ID = "arc.compiler.options.target.arc601";
    public static final String VERSION_ARC700_ID = "arc.compiler.options.target.arc700";
    public static final String VERSION_AV2EM_ID = "arc.compiler.options.target.arcv2em";
    public static final String VERSION_AV2HS_ID = "arc.compiler.options.target.arcv2hs";
    
    public static final String COMPILER_BCF_ID = "arc.compiler.options.bcf";
    public static final String ASM_BCF_ID = "arc.asm.options.bcf";
    public static final String LINKER_BCF_ID = "arc.linker.options.bcf";

    // The folloiwng option is always invisible in the Tool Settings page. It is where we store the
    // .cfg file path, if any
    public static final String COMPILER_CFG_ID = "arc.compiler.options.cfg";


    public static final String RF16_COMPILER_ID = "arc.compiler.options.rf16";
    public static final String RF16_ASM_ID = "arc.asm.options.rf16";
    public static final String RF16_LINKER_ID = "arc.linker.options.rf16";

    public static final String LPC_WIDTH_ID = "arc.compiler.options.lpc_width";
    public static final String LPC_WIDTH_VALUE_PREFIX = "com.arc.cdt.toolchain.arc.lpcwidth.";

    public static final String PC_WIDTH_LINKER_ID = "arc.linker.options.pc_width" ;
    public static final String PC_WIDTH_COMPILER_ID = "arc.compiler.options.pc_width" ;
    public static final String PC_WIDTH_VALUE_PREFIX = "com.arc.cdt.toolchain.arc.pcwidth.";

    public static final String ENDIAN_LINKER_ID = "arc.linker.options.endian" ;
    public static final String ENDIAN_COMPILER_ID = "arc.compiler.options.endian";
    public static final String ENDIAN_ASM_ID = "arc.assembler.options.endian" ;
    public static final String ENDIAN_VALUE_PREFIX = "arc.compiler.option.endian.";

    public static final String XATOMIC_COMPILER_ID = "arc.compiler.options.atomic";
    public static final String XATOMIC_ASM_ID = "arc.asm.options.atomic";
    public static final String XATOMIC_LINKER_ID = "arc.linker.options.atomic";

    public static final String XLL64_COMPILER_ID = "arc.compiler.options.ll64";
    public static final String XLL64_ASM_ID = "arc.asm.options.ll64";
    public static final String XLL64_LINKER_ID = "arc.linker.options.ll64";

    public static final String XCD_COMPILER_ID = "arc.compiler.options.cd";
    public static final String XCD_ASM_ID = "arc.asm.options.cd";
    public static final String XCD_LINKER_ID = "arc.linker.options.cd";

    public static final String XDIVREM_COMPILER_ID = "arc.compiler.options.div_rem";
    public static final String XDIVREM_ASM_ID = "arc.asm.options.div_rem";
    public static final String XDIVREM_LINKER_ID = "arc.linker.options.div_rem";
    public static final String XDIVREM_VALUE_PREFIX = "arc.options.divrem.";

    public static final String XMPY_CYCLES_COMPILER_ID = "arc.compiler.options.mpycycles";
    public static final String XMPY_CYCLES_VALUE_PREFIX = "arc.options.mpycycles.";

    public static final String XSWAP_COMPILER_ID = "arc.compiler.options.swap";
    public static final String XSWAP_ASM_ID = "arc.asm.options.swap";
    public static final String XSWAP_LINKER_ID = "arc.linker.options.swap";

    public static final String XNORM_COMPILER_ID = "arc.compiler.options.norm";
    public static final String XNORM_ASM_ID = "arc.asm.options.norm";
    public static final String XNORM_LINKER_ID = "arc.linker.options.norm";

    public static final String XMPY_COMPILER_ID = "arc.compiler.options.mpy";
    public static final String XMPY_ASM_ID = "arc.asm.options.mpy";
    public static final String XMPY_LINKER_ID = "arc.linker.options.mpy";

    public static final String XMPY16_COMPILER_ID = "arc.compiler.options.mpy16";
    public static final String XMPY16_ASM_ID = "arc.asm.options.mpy16";
    public static final String XMPY16_LINKER_ID = "arc.linker.options.mpy16";

    public static final String XQMPYH_COMPILER_ID = "arc.compiler.options.qmpyh";
    public static final String XQMPYH_ASM_ID = "arc.asm.options.qmpyh";
    public static final String XQMPYH_LINKER_ID = "arc.linker.options.qmpyh";

    public static final String XMACD_COMPILER_ID = "arc.compiler.options.macd";
    public static final String XMACD_ASM_ID = "arc.asm.options.macd";
    public static final String XMACD_LINKER_ID = "arc.linker.options.macd";

    public static final String XMAC_COMPILER_ID = "arc.compiler.options.mac";
    public static final String XMAC_ASM_ID = "arc.asm.options.mac";
    public static final String XMAC_LINKER_ID = "arc.linker.options.mac";     

    public static final String XSA_COMPILER_ID = "arc.compiler.options.sa";
    public static final String XSA_ASM_ID = "arc.asm.options.sa";
    public static final String XSA_LINKER_ID = "arc.linker.options.sa";

    public static final String XBS_COMPILER_ID = "arc.compiler.options.bs";
    public static final String XBS_ASM_ID = "arc.asm.options.bs";
    public static final String XBS_LINKER_ID = "arc.linker.options.bs";

    public static final String XFPUD_DIV_COMPILER_ID = "arc.compiler.options.fpud_div";
    public static final String XFPUD_DIV_ASM_ID = "arc.asm.options.fpud_div";
    public static final String XFPUD_DIV_LINKER_ID = "arc.linker.options.fpud_div";

    public static final String XFPUD_COMPILER_ID = "arc.compiler.options.fpud";
    public static final String XFPUD_ASM_ID = "arc.asm.options.fpud";
    public static final String XFPUD_LINKER_ID = "arc.linker.options.fpud";

    public static final String XFPUS_COMPILER_ID = "arc.compiler.options.fpus";
    public static final String XFPUS_ASM_ID = "arc.asm.options.fpus";
    public static final String XFPUS_LINKER_ID = "arc.linker.options.fpus";

    public static final String XFPUS_DIV_COMPILER_ID = "arc.compiler.options.fpus_div";
    public static final String XFPUS_DIV_ASM_ID = "arc.asm.options.fpus_div";
    public static final String XFPUS_DIV_LINKER_ID = "arc.linker.options.fpus_div";

    public static final String XFPU_MAC_COMPILER_ID = "arc.compiler.options.fpu_mac";
    public static final String XFPU_MAC_ASM_ID = "arc.asm.options.fpu_mac";
    public static final String XFPU_MAC_LINKER_ID = "arc.linker.options.fpu_mac";

    public static final String XTIMER0_COMPILER_ID = "arc.compiler.options.timers.timer0";
    public static final String XTIMER0_ASM_ID = "arc.asm.options.timers.timer0";

    public static final String XTIMER1_COMPILER_ID = "arc.compiler.options.timers.timer1";
    public static final String XTIMER1_ASM_ID = "arc.asm.options.timers.timer1";

    public static final String XTIMER_LINKER_ID = "arc.arc.linker.options.xtimer";
    public static final String XTIMER0_LINKER_VALUE = "arc.linker.options.timer.timer0";
    public static final String XTIMER1_LINKER_VALUE = "arc.linker.options.timer.timer1";

    public static final String COMPILER_ADDITIONAL_OPTIONS = "arc.compiler.options.misc.additional";
    public static final String LINKER_ADDITIONAL_OPTIONS = "arc.link.options.ldflags";
    public static final String ASM_ADDITIONAL_OPTIONS = "com.arc.cdt.toolchain.option.asm.extra";
    
    public static final String LINKER_COMMAND_FILE_LIST_ID = "com.arc.cdt.toolchain.option.linker.svr3";
    
    public static final String EM4_EXE_PROJECT_TYPE_ID = "com.arc.cdt.toolchain.arc.av2em4.exeProject";
    public static final String EM4_LIB_PROJECT_TYPE_ID = "com.arc.cdt.toolchain.arc.av2em4.libProject";




    public static String computeProjectTypeID(ProcessorFamily processor,
            boolean lib) {
        String id;
        if (!lib)
            id = "com.arc.cdt.toolchain.arc." + processor.getProjectType()
            + ".exeProject";
        else
            id = "com.arc.cdt.toolchain.arc." + processor.getProjectType()
            + ".libProject";
        return id;
    }

    public static String computeCorePropertyID(ProcessorFamily processor) {
        switch (processor) {
        case A4:
            return "arc4.compiler.options.arc4core";
        case A5:
            return "arc.compiler.options.arc5core";
        case ARC600:
            return "arc.compiler.options.arc6core";
        case ARC700:
            return "arc.compiler.options.arc7core";
        case ARCEM:
            return "arc.compiler.options.arcv2emcore";
        case ARCHS:
            return "arc.compiler.options.arcv2hscore";
        default:
            throw new IllegalArgumentException("No core for " + processor);
        }
    }

    public static String computeLPC_WIDTH_ID(String value)
            throws IllegalArgumentException {
        int width = Integer.parseInt(value);
        switch (width) {
        case 8:
        case 12:
        case 16:
        case 20:
        case 24:
        case 28:
        case 32:
            break;
        default:
            throw new IllegalArgumentException("Invalid LPC width: " + value);
        }
        return LPC_WIDTH_VALUE_PREFIX + value;
    }

    public static String computePC_WIDTH_ID(String value)
            throws IllegalArgumentException {
        int width = Integer.parseInt(value);
        switch (width) {
        case 16:
        case 20:
        case 24:
        case 28:
        case 32:
            break;
        default:
            throw new IllegalArgumentException("Invalid PC width: " + value);
        }
        return PC_WIDTH_VALUE_PREFIX + value;
    }

    /**
     * Given a toolchain ID, return the processor version ID that corresponds to it.
     */
    public static String getVersionIdFromToolChainId(String tc){
        if (tc.indexOf("hs.") > 0) return VERSION_AV2HS_ID;
        if (tc.indexOf("em.") > 0) return VERSION_AV2EM_ID;
        if (tc.indexOf("arc600") > 0) return VERSION_ARC600_ID;
        if (tc.indexOf("arc700") > 0) return VERSION_ARC700_ID;
        if (tc.indexOf("arc601") > 0) return VERSION_ARC601_ID;
        if (tc.indexOf("arc5") > 0) return VERSION_A5_ID;
        return null;
    }

    private MWConstants() {
    }

}
