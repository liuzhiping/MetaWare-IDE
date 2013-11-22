package com.arc.cdt.toolchain.internal.ui.bcf.properties.metaware;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.xml.sax.SAXException;

import com.arc.cdt.toolchain.internal.ui.bcf.BcfUtils;
import com.arc.cdt.toolchain.internal.ui.bcf.properties.AbstractOptionUpdater;
import com.arc.cdt.toolchain.ui.bcf.Activator;
import com.arc.cdt.toolchain.ui.bcf.BCFKey;
import com.arc.cdt.toolchain.ui.bcf.BadPropertyException;
import com.arc.cdt.toolchain.ui.bcf.ProcessorFamily;
import com.arc.cdt.toolchain.ui.bcf.SettingsFileContent;

/**
 * Responsible for applying the properties read from the CAT file to the option
 * values of each configuration.
 * 
 * @author pickensd
 * 
 */
public class MetaWareOptionUpdater extends AbstractOptionUpdater {

    public MetaWareOptionUpdater(SettingsFileContent profile) throws BadPropertyException, IOException, SAXException {
        super(profile);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected IProjectType getProjectTypeFor(SettingsFileContent content, boolean lib) {
        //<HACK> if target is EM, but the BCF files has "em4" in its name, assume EM4
        String id;
        if (family == ProcessorFamily.ARCEM && content.getBcfFile() != null && content.getBcfFile().getPath().indexOf("em4") >= 0){
            id = lib?MWConstants.EM4_LIB_PROJECT_TYPE_ID: MWConstants.EM4_EXE_PROJECT_TYPE_ID;
        }
        else
            id = MWConstants.computeProjectTypeID(family, lib);
        IProjectType pt = ManagedBuildManager.getProjectType(id);
        if (pt == null) {
            throw new IllegalStateException("Cannot compute project type for " + family);
        }
        return pt;
    }

    private static String computeVersionValueID(ProcessorFamily version) {
        switch (version) {
        case A4:
            return "";
        case A5:
            return MWConstants.VERSION_A5_ID;
        case ARC600:
            return MWConstants.VERSION_ARC600_ID;
        case ARC700:
            return MWConstants.VERSION_ARC700_ID;
        case ARCEM:
            return MWConstants.VERSION_AV2EM_ID;
        case ARCHS:
            return MWConstants.VERSION_AV2HS_ID;
        default:
            return "???";
        }
    }

    @Override
    protected Map<String, Object> computeOptionValues(Properties bcfProperties) {
        Map<String, Object> optionMap = new HashMap<String, Object>();
        if (this.family != ProcessorFamily.A4) { // A4 has its own project type
            String versionID = computeVersionValueID(this.family);
            optionMap.put(MWConstants.COMPILER_VERSION_ID, versionID);
            optionMap.put(MWConstants.LINKER_VERSION_ID, versionID);
            optionMap.put(MWConstants.ASM_VERSION_ID, versionID);
        }
        Set<String> unprocessed = new HashSet<String>();
        for (Object key: bcfProperties.keySet()){
            unprocessed.add((String)key);
        }
        for (BCFKey key : BCFKey.values()) {
            String value = bcfProperties.getProperty(key.toString());
            unprocessed.remove(key.toString());
            try {
                if (value != null) {
                    switch (key) {
                    case Family:
                        break; // already handled
                    case Core:
                        if (family != ProcessorFamily.ARC601) {
                            String id = MWConstants.computeCorePropertyID(family);
                            //CR9000672960 we should store coreValue the same format with plugin.xml
                            
                            String coreValue = id + ".core"+value;
                            optionMap.put(id, coreValue);
                        }
                        break;
                    case RF16:
                        optionMap.put(MWConstants.RF16_COMPILER_ID, value);
                        optionMap.put(MWConstants.RF16_ASM_ID, value);
                        optionMap.put(MWConstants.RF16_LINKER_ID, value);
                        break;
                    case RGF_NUM_BANKS:
                    case RGF_BANKED_REGS:
                    case ADDR_SIZE:
                    case    XRTC:
                    case MPU:
                    case MPU_REGIONS:
                    case INTERRUPTS:
                    case INTERRUPT_PRIORITIES:
                    case EXT_INTERRUPTS:
                    case FIRQ:
                    case INTERRUPT_BASE:
                    case DCACHE_SIZE:
                    case DCACHE_LINE_SIZE:
                    case DCACHE_WAYS:
                    case ICACHE_SIZE:
                    case ICACHE_LINE_SIZE:
                    case ICACHE_WAYS:
                    case DCCM_SIZE:
                    case ICCM_SIZE:
                    case ICCM1_SIZE:
                    case ICCM_BASE:
                    case DCCM_BASE:
                    case ICCM1_BASE:
                        break; // not applicable to compiler, linker, or assembler
                    case LPC_SIZE:
                        optionMap.put(MWConstants.LPC_WIDTH_ID,
                                MWConstants.computeLPC_WIDTH_ID(value));
                        break;
                    case PC_SIZE:
                        optionMap.put(MWConstants.PC_WIDTH_COMPILER_ID,
                                MWConstants.computePC_WIDTH_ID(value));
                        optionMap.put(MWConstants.PC_WIDTH_LINKER_ID,
                                MWConstants.computePC_WIDTH_ID(value));
                        break;
                    case ENDIAN: {
                        String v = MWConstants.ENDIAN_VALUE_PREFIX + value;
                        if (!"little".equals(value) && !"big".equals(value))
                            throw new IllegalArgumentException(
                                    "Invalid CAT property value for endian: " + value);
                        optionMap.put(MWConstants.ENDIAN_ASM_ID, v);
                        optionMap.put(MWConstants.ENDIAN_COMPILER_ID, v);
                        optionMap.put(MWConstants.ENDIAN_LINKER_ID, v);
                        break;
                    }
                    case XATOMIC:
                        optionMap.put(MWConstants.XATOMIC_COMPILER_ID, value);
                        optionMap.put(MWConstants.XATOMIC_ASM_ID, value);
                        optionMap.put(MWConstants.XATOMIC_LINKER_ID, value);
                        break;
                    case XLL64:
                        optionMap.put(MWConstants.XLL64_COMPILER_ID, value);
                        optionMap.put(MWConstants.XLL64_ASM_ID, value);
                        optionMap.put(MWConstants.XLL64_LINKER_ID, value);
                        break;
                    case XCD:
                        optionMap.put(MWConstants.XCD_COMPILER_ID, value);
                        optionMap.put(MWConstants.XCD_ASM_ID, value);
                        optionMap.put(MWConstants.XCD_LINKER_ID, value);
                        break;
                    case XDIV_REM:
                        if ("radix2".equals(value) || "radix4".equals(value)){
                            String v = MWConstants.XDIVREM_VALUE_PREFIX + value;
                            optionMap.put(MWConstants.XDIVREM_COMPILER_ID, v);
                            optionMap.put(MWConstants.XDIVREM_ASM_ID, v);
                            optionMap.put(MWConstants.XDIVREM_LINKER_ID, v);
                        }
                        break;
                    case XSWAP:
                        optionMap.put(MWConstants.XSWAP_COMPILER_ID, value);
                        optionMap.put(MWConstants.XSWAP_ASM_ID, value);
                        optionMap.put(MWConstants.XSWAP_LINKER_ID, value);
                        break;
                    case XNORM:
                        optionMap.put(MWConstants.XNORM_COMPILER_ID, value);
                        optionMap.put(MWConstants.XNORM_ASM_ID, value);
                        optionMap.put(MWConstants.XNORM_LINKER_ID, value);
                        break;
                    case XMPY:
                        optionMap.put(MWConstants.XMPY_COMPILER_ID, value);
                        optionMap.put(MWConstants.XMPY_ASM_ID, value);
                        optionMap.put(MWConstants.XMPY_LINKER_ID, value);
                        break;
                    case XMPY16:
                        optionMap.put(MWConstants.XMPY16_COMPILER_ID, value);
                        optionMap.put(MWConstants.XMPY16_ASM_ID, value);
                        optionMap.put(MWConstants.XMPY16_LINKER_ID, value);
                        break;
                    case XQMPYH:
                        optionMap.put(MWConstants.XQMPYH_COMPILER_ID, value);
                        optionMap.put(MWConstants.XQMPYH_ASM_ID, value);
                        optionMap.put(MWConstants.XQMPYH_LINKER_ID, value);
                        break;
                    case XMPY_OPTION:
                        if (value.length() == 1){
                            switch(value.charAt(0)){
                            case '9':
                                optionMap.put(MWConstants.XQMPYH_COMPILER_ID, "1");
                                optionMap.put(MWConstants.XQMPYH_ASM_ID, "1");
                                optionMap.put(MWConstants.XQMPYH_LINKER_ID, "1");
                                //$FALL-THROUGH$
                            case '8':
                                optionMap.put(MWConstants.XMACD_COMPILER_ID, "1");
                                optionMap.put(MWConstants.XMACD_ASM_ID, "1");
                                optionMap.put(MWConstants.XMACD_LINKER_ID, "1");
                                //$FALL-THROUGH$
                            case '7':
                                optionMap.put(MWConstants.XMAC_COMPILER_ID, "1");
                                optionMap.put(MWConstants.XMAC_ASM_ID, "1");
                                optionMap.put(MWConstants.XMAC_LINKER_ID, "1");
                                //$FALL-THROUGH$
                            case '6':
                            case '5':
                            case '4':
                            case '3':
                            case '2':
                                optionMap.put(MWConstants.XMPY_COMPILER_ID, "1");
                                optionMap.put(MWConstants.XMPY_ASM_ID, "1");
                                optionMap.put(MWConstants.XMPY_LINKER_ID, "1");
                                //$FALL-THROUGH$
                            case '1':
                                optionMap.put(MWConstants.XMPY16_COMPILER_ID, "1");
                                optionMap.put(MWConstants.XMPY16_ASM_ID, "1");
                                optionMap.put(MWConstants.XMPY16_LINKER_ID, "1");
                                break;
                            case '0': break;
                            default: 
                                throw new IllegalArgumentException("Invalid mpy_option value: " + value);

                            }
                        }
                        else throw new IllegalArgumentException("Invalid mpy_option value: " + value);
                        break;
                    case XMPY_CYCLES:
                        if (value.length() == 1 && value.charAt(0) >= '1' && value.charAt(0) <= '5') {
                            optionMap.put(MWConstants.XMPY_CYCLES_COMPILER_ID, MWConstants.XMPY_CYCLES_VALUE_PREFIX+value);
                        }
                        break;
                    case  XSA:
                        optionMap.put(MWConstants.XSA_COMPILER_ID, value);
                        optionMap.put(MWConstants.XSA_ASM_ID, value);
                        optionMap.put(MWConstants.XSA_LINKER_ID, value);
                        break;
                    case  XBS:
                        optionMap.put(MWConstants.XBS_COMPILER_ID, value);
                        optionMap.put(MWConstants.XBS_ASM_ID, value);
                        optionMap.put(MWConstants.XBS_LINKER_ID, value);
                        break;
                    case  XFPUD_DIV:
                        optionMap.put(MWConstants.XFPUD_DIV_COMPILER_ID, value);
                        optionMap.put(MWConstants.XFPUD_DIV_ASM_ID, value);
                        optionMap.put(MWConstants.XFPUD_DIV_LINKER_ID, value);
                        break;
                    case  XFPUD:
                        optionMap.put(MWConstants.XFPUD_COMPILER_ID, value);
                        optionMap.put(MWConstants.XFPUD_ASM_ID, value);
                        optionMap.put(MWConstants.XFPUD_LINKER_ID, value);
                        break;
                    case  XFPUS:
                        optionMap.put(MWConstants.XFPUS_COMPILER_ID, value);
                        optionMap.put(MWConstants.XFPUS_ASM_ID, value);
                        optionMap.put(MWConstants.XFPUS_LINKER_ID, value);
                        break;
                    case  XFPUS_DIV:
                        optionMap.put(MWConstants.XFPUS_DIV_COMPILER_ID, value);
                        optionMap.put(MWConstants.XFPUS_DIV_ASM_ID, value);
                        optionMap.put(MWConstants.XFPUS_DIV_LINKER_ID, value);
                        break;
                    case  XFPU_MAC:
                        optionMap.put(MWConstants.XFPU_MAC_COMPILER_ID, value);
                        optionMap.put(MWConstants.XFPU_MAC_ASM_ID, value);
                        optionMap.put(MWConstants.XFPU_MAC_LINKER_ID, value);
                        break;
                    case XTIMER0:
                        optionMap.put(MWConstants.XTIMER0_COMPILER_ID, value);
                        optionMap.put(MWConstants.XTIMER0_ASM_ID, value);
                        optionMap.put(MWConstants.XTIMER_LINKER_ID, MWConstants.XTIMER0_LINKER_VALUE);
                        break;
                    case XTIMER1:
                        optionMap.put(MWConstants.XTIMER1_COMPILER_ID, value);
                        optionMap.put(MWConstants.XTIMER1_ASM_ID, value);
                        optionMap.put(MWConstants.XTIMER_LINKER_ID, MWConstants.XTIMER1_LINKER_VALUE);
                        break;
                    }
                }
            } catch (Exception e) {
                Activator.log(e.getMessage(), e);
            }
        }
        if (unprocessed.size() > 0){
            StringBuilder buf = new StringBuilder();
            for (String prop: unprocessed){
                buf.append(' ');
                buf.append(prop);
            }
            Activator.log("Unrecognized BCF properties: " + buf.toString(), null);
        }
        return optionMap;
    }

    /*
     * The function below is used for non project case
     * (non-Javadoc)
     * @see com.arc.cdt.toolchain.internal.ui.bcf.properties.AbstractOptionUpdater#updateCompileOptionForLaunchConfiguration(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
     */
    @Override
    public boolean updateCompileOptionForLaunchConfiguration(ILaunchConfigurationWorkingCopy launch) {
    	
    	 try {
             return updateCompileOptionForLaunchConfiguration(launch,settings.getProperties());
         
         } catch (Exception ex) {
        	 Activator.log(ex.getMessage(), ex);
             
         }
    	
    	 return false;
    }

    
    private void handleXMPY_OPTION (DebuggerState state, String value){
    	String select = "1";
    	String noSelect = "0";
    	if (value.length() == 1){
    		switch(value.charAt(0)){
    		case '9':
    			state.doValue("ARC_qmpyh", select, noSelect, "-Xqmpyh");
    			//$FALL-THROUGH$
    		case '8':
    			state.doValue("ARC_macd", select ,noSelect, "-Xmacd");
    			//$FALL-THROUGH$
    		case '7':
    			state.doValue("ARC_mac", select ,noSelect, "-Xmac");
    			//$FALL-THROUGH$

    		case '6':
    		case '5':
    		case '4':
    		case '3':
    		case '2':

    			state.doValue("ARC_mpy", select, noSelect, "-Xmpy");
    			//$FALL-THROUGH$
     		case '1':
    					
		        state.doValue("ARC_mpy16", select, noSelect, "-Xmpy16");
			    
    		case '0': break;
    		
    		default: 
    			throw new IllegalArgumentException("Invalid mpy_option value: " + value);

    		}
    	}
    }
    private boolean updateCompileOptionForLaunchConfiguration(ILaunchConfigurationWorkingCopy launch, Properties bcfProperties) throws CoreException, BadPropertyException {
    
    	@SuppressWarnings("unchecked")
        Map<String,String> guihiliProperties = launch.getAttribute(MWConstants.ATTR_LAUNCH_GUIHILI, (Map<String,String>)new HashMap<String,String>(0));
        @SuppressWarnings("unchecked")
        List<String> args = launch.getAttribute(MWConstants.ATTR_LAUNCH_SWAHILI_ARGS, new ArrayList<String>());

        DebuggerState state = new DebuggerState(guihiliProperties, args);
       
        ProcessorFamily processorDefault = ProcessorFamily.ARC600;
        ProcessorFamily processor = processorDefault;
        String processPropName = "which_arc";
        for (BCFKey key : BCFKey.values()) {
            String value = bcfProperties.getProperty(key.toString());
            
            String thisarg= "";
            if (value != null) {
                switch (key) {
                case Family:
                	if(value.equals("4"))
                		processor = ProcessorFamily.ARCEM;
                	
                	else if(value.equals("5"))     
                		processor = ProcessorFamily.ARCHS;
                	else{
                		BcfUtils.displayBCFError("TCF file is not ARC HS or ARC EM", Display.getDefault().getActiveShell());
                		return false;
                	}
                	
                	state.doValue(processPropName, processor.getGuihiliName(), processorDefault.getGuihiliName(), processor.getDebuggerArg());
                	break;
                case Core:
                	String processorName = state.getProperties().get(processPropName);
                	if(processorName != null && processorName.equals(ProcessorFamily.ARCEM.getGuihiliName()))
                		state.doValue("v2em_core_version", value, "1", "-core"+value);
                	else if (processorName != null && processorName.equals(ProcessorFamily.ARCHS.getGuihiliName()))
                		state.doValue("v2hs_core_version", value, "0", "-core"+value);
                	break;
                case RF16:
                    
                	state.doValue("rf16", value, "-rf16", "-rf16");
                    break;
                
                case LPC_SIZE:
                	state.doValue("ARC_lpc_width", value, "32", "-lpc_width=");
                	break;
                case PC_SIZE:
                	state.doValue("ARC_pc_width", value, "32", "-pc_width=");
                        // Set implicitly from build configuration
                    break;
                case ENDIAN: 
                	String endian = "BE";
                	String thisargs = "-connect_only_be -off=download";
                			
                	if(value.equals("little")){
                		endian = "LE";
                		thisarg = "-connect_only -off=download";
                	}
                		
                	state.doValue("connect_only", endian, "BE", thisarg);
                    break; // endianess read from ELF header
                case XATOMIC:
                	state.doValue("ARC_atomic", value, "0", "-Xatomic");
                    break;
                    
                case XLL64:
                	state.doValue("ARC_ll64", value, "0", "-Xll64");
                	break;
                case XCD:
                	state.doValue("ARC_code_density", value, "0", "-Xcode_density");
                	break;
                case XDIV_REM:
                	state.doValue("ARC_div_rem", value, "0", "-Xdiv_rem");
                	break;
                case XSWAP:
                	state.doValue("ARC_swap", value, "0", "-Xswap");
                	break;
                	
                case XNORM:  
                	state.doValue("ARC_norm", value, "0", "-Xnorm");
                	break;
                case XMPY: 
                	if(processor == ProcessorFamily.ARCEM)
                		state.doValue("ARC_mpy", value, "0", "-Xmpy");
                    else if (processor == ProcessorFamily.ARCHS)
                	    state.doValue("ARC_mpy", value, "0", "-Xmpy -Xmpy16");
                	break;
                case XMPY16:
                	state.doValue("ARC_mpy16", value, "0", "-Xmpy16");
                	break;
                case XQMPYH:  
                	state.doValue("ARC_qmpyh", value, "0", "-Xqmpyh -Xmacd -Xmac -Xmpy -Xmpy16");
                	break;
                case XMPY_OPTION:     
                	handleXMPY_OPTION (state, value);
                case XMPY_CYCLES: 
                	//state.doValue("ARC_mult32_cycles", value, "Default", "-Xmult32_cycles=");
                	state.doValue("ARC_mpy_cycles", value, "Default", "-Xmpy_cycles=");
                	break;
                	     
                case XSA:  
                	state.doValue("ARC_shift_assist", value, "Default", "-Xshift_assist");
                	break;
                	
                case  XBS:   
                    state.doValue("ARC_barrel_shifter", value, "0", "-Xbarrel_shifter");
            	    break;
            	
                case  XFPUD_DIV:  
                	state.doValue("ARC_fpud_div", value, "0", "-Xfpud_div");
            	    break;
                case  XFPUD: 
                	state.doValue("ARC_fpud", value, "0", "-Xfpud");
            	    break;
                case  XFPUS:       
                	state.doValue("ARC_fpus", value, "0", "-Xfpus");
            	    break;
                case  XFPUS_DIV:
                	state.doValue("ARC_fpus_div", value, "0", "-Xfpus_div");
            	    break;
                case  XFPU_MAC:
                	state.doValue("ARC_fpu_mac", value, "0", "-Xfpu_mac");
                	break;
                case XTIMER0:
                	state.doValue("ARC_timer0", value, "0", "-Xtimer0");
                	break;
                case XTIMER1:
                	state.doValue("ARC_timer1", value, "0", "-Xtimer1");
                	break;
                }
            }
        }
        
        if (state.isDirty()){
        	launch.setAttribute(MWConstants.ATTR_LAUNCH_GUIHILI, state.getProperties());
            launch.setAttribute(MWConstants.ATTR_LAUNCH_SWAHILI_ARGS, state.getArgs());
            return true;
        }
        return false;
        
    }
    
        
    @Override
    protected boolean updateLaunchConfiguration(ILaunchConfigurationWorkingCopy launch,
            Properties bcfProperties) throws CoreException {
        @SuppressWarnings("unchecked")
        Map<String,String> guihiliProperties = launch.getAttribute(MWConstants.ATTR_LAUNCH_GUIHILI, (Map<String,String>)new HashMap<String,String>(0));
        @SuppressWarnings("unchecked")
        List<String> args = launch.getAttribute(MWConstants.ATTR_LAUNCH_SWAHILI_ARGS, new ArrayList<String>());

        DebuggerState state = new DebuggerState(guihiliProperties, args);
        
        // Set defaults:
        state.setProperty("ARC_default_interrupts","0");
        state.setProperty("ARC_default_ext_interrupts", "0");
        state.setProperty("AC_icache","0");
        state.setProperty("AC_dcache","0");
        
        String dcacheSize = null;
        String dcacheLineSize = "16";
        String dcacheWays = "1";
        String dcacheRep = "a";
        String icacheSize = null;
        String icacheLineSize = "8";
        String icacheWays = "1";
        String icacheRep = "a";
        state.setProperty("dcache_repalg","Random");
        state.setProperty("icache_repalg","Random");


        for (BCFKey key : BCFKey.values()) {
            String value = bcfProperties.getProperty(key.toString());
            if (value != null) {
                switch (key) {
                case Family:
                case Core:
                case RF16:
                    // Inherited from build configuration during launch         
                    break;
                case RGF_NUM_BANKS:
                    state.doValue("v2em_regis_bank",value,"1","-rgf_num_banks=");
                    break;
                case RGF_BANKED_REGS:
                    state.doValue("v2em_core_register_per_bank",value,"0","-rgf_banked_regs=");
                    break;
                case ADDR_SIZE:
                    state.doValue("ARC_addr_size",value,"32","-addr_size=");
                    break;
                case XRTC:
                    state.doBoolean("realtime_counter", value, "-Xrtc");
                    break;
                case MPU:
                    state.doBoolean("A6_mpu", value, "-mpu");
                    break;
                case MPU_REGIONS:
                    state.doValue("ARC_mpu_region", value, "Default", "-mpu_regions=");
                    break;
                case INTERRUPTS:
                    if (family.ordinal() >= ProcessorFamily.ARCEM.ordinal()){
                        state.setProperty("ARC_default_interrupts","1");
                        state.doValue("ARC_interrupt_num",value,"16","-interrupts=");
                    }
                    else
                        state.doValue("ARC_interrupt_vector_count", value,"(default)","-interrupts=");
                    break;
                case INTERRUPT_PRIORITIES:
                    state.doValue("ARC_interrupt_priorities", value,"1","-interrupt_priorities=");
                    break;
                case EXT_INTERRUPTS:
                    state.doValue("ARC_ext_interrupt_num",value,"0", "-ext_interrupts=");
                    state.setProperty("ARC_default_ext_interrupts", "1");
                    break;
                case FIRQ:
                    state.doBoolean("ARC_fast_interrupt",value,"-firq");
                    break;
                case INTERRUPT_BASE:
                    state.doValue("ARC_interrupt_baseaddr",value,"0","-interrupt_base=");
                    break;
                case DCACHE_SIZE:
                    state.setProperty("AC_dcache_size", value);
                    state.setProperty("AC_dcache", "1");
                    dcacheSize = value;
                    break;
                case DCACHE_LINE_SIZE:
                    state.setProperty("AC_dcache_line_size", value);
                    dcacheLineSize = value;
                    break;
                case DCACHE_WAYS:
                    state.setProperty("AC_dcache_ways", value);
                    dcacheWays = value;
                    break;
                case ICACHE_SIZE:
                    state.setProperty("AC_icache_size", value);
                    state.setProperty("AC_icache", "1");
                    icacheSize = value;
                    break;
                case ICACHE_LINE_SIZE:
                    state.setProperty("AC_icache_line_size", value);
                    icacheLineSize = value;
                    break;
                case ICACHE_WAYS:
                    state.setProperty("AC_icache_ways", value);
                    icacheWays = value;
                    break;
                case DCCM_SIZE:
                    state.doValue("dccm_size",value,"none","-dccm_size=");
                    break;
                case DCCM_BASE:
                    state.doValue("dccm_base",value,"0","-dccm_base=");
                    break;
                case ICCM_SIZE:
                    state.doValue("iccm0_size",value,"none","-iccm0_size=");
                    break;
                case ICCM_BASE:
                    state.doValue("iccm0_base",value,"0","-iccm0_base=");
                    break;
                case ICCM1_SIZE:
                    state.doValue("iccm1_size",value,"none","-iccm_size=");
                    break;                     
                case ICCM1_BASE:
                    state.doValue("iccm1_base",value,"0","-iccm1_base=");
                    break;
                case LPC_SIZE:
                case PC_SIZE:
                        // Set implicitly from build configuration
                    break;
                case ENDIAN: 
                    break; // endianess read from ELF header
                case XATOMIC:
                case XLL64:
                case XCD:
                case XDIV_REM:
                case XSWAP:
                case XNORM:       
                case XMPY:        
                case XMPY16:                
                case XQMPYH:                      
                case XMPY_OPTION:                      
                case XMPY_CYCLES:                    
                case  XSA:                      
                case  XBS:                 
                case  XFPUD_DIV:              
                case  XFPUD:         
                case  XFPUS:            
                case  XFPUS_DIV:
                case  XFPU_MAC:
                case XTIMER0:
                case XTIMER1:
                   break; // implicitly set form build configurations
                }
            }
        }
        if (dcacheSize != null){
            state.setArg("-dcache=", dcacheSize + "," + dcacheLineSize + "," + dcacheWays + "," + dcacheRep );
        }
        if (icacheSize != null){
            state.setArg("-icache=", icacheSize + "," + icacheLineSize + "," + icacheWays + "," + icacheRep );
        }
        if (state.isDirty()){
            launch.setAttribute(MWConstants.ATTR_LAUNCH_GUIHILI, state.getProperties());
            launch.setAttribute(MWConstants.ATTR_LAUNCH_SWAHILI_ARGS, state.getArgs());
            return true;
        }
        return false;
    }

    @Override
    protected String getCompilerAdditionalOptionsID() {
         return MWConstants.COMPILER_ADDITIONAL_OPTIONS;
    }

    @Override
    protected String getAsmAdditionalOptionsID() {
        return MWConstants.ASM_ADDITIONAL_OPTIONS;
    }

    @Override
    protected String getLinkerAdditionalOptionsID() {
        return MWConstants.LINKER_ADDITIONAL_OPTIONS;
    }

    @Override
    protected List<String> canonicalizeLinkerArgs(List<String> args) {
        List<String> result = new ArrayList<String>();
        boolean mapFile = false;
        for (String arg: args){
            if (arg.startsWith("-C")){
                mapFile = true;
                result.add("-Hldopt=" + arg);
            }
            else if (arg.startsWith("-X")){
                result.add("-Hldopt=" + arg);
            }
            else {
                if (arg.startsWith("-Hldopt=-C"))
                    mapFile = true;
                result.add(arg);
            }
            
        }
        if (mapFile){
            result.add("-Hldopt=-Coutput=${BuildArtifactFileBaseName}.map");
        }
        return result;
    }

    @Override
    protected String getLinkerCommandFileID() {
        return MWConstants.LINKER_COMMAND_FILE_LIST_ID;
    }

}
