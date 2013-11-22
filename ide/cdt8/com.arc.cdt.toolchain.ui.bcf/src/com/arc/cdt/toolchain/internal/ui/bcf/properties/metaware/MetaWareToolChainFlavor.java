package com.arc.cdt.toolchain.internal.ui.bcf.properties.metaware;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.xml.sax.SAXException;

import com.arc.cdt.toolchain.internal.ui.bcf.AbstractToolChainFlavor;
import com.arc.cdt.toolchain.internal.ui.bcf.BcfUtils;
import com.arc.cdt.toolchain.ui.bcf.Activator;
import com.arc.cdt.toolchain.ui.bcf.BadPropertyException;
import com.arc.cdt.toolchain.ui.bcf.IOptionUpdater;
import com.arc.cdt.toolchain.ui.bcf.ProcessorFamily;
import com.arc.cdt.toolchain.ui.bcf.SettingsFileContent;

public class MetaWareToolChainFlavor extends AbstractToolChainFlavor {

    public MetaWareToolChainFlavor() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public IOptionUpdater createOptionUpdator(SettingsFileContent profile) throws BadPropertyException, IOException, SAXException {
        return new MetaWareOptionUpdater(profile);
    }

    @Override
    public void setProcessorFamily(IConfiguration config) {
        IToolChain tc = config.getToolChain();
        String id = tc.getBaseId();
        assert (id != null);
        String ver = MWConstants.getVersionIdFromToolChainId(id);
        if (ver != null) {
            try {
                BcfUtils.setOption(config, MWConstants.COMPILER_VERSION_ID, ver);
                BcfUtils.setOption(config, MWConstants.ASM_VERSION_ID, ver);
                BcfUtils.setOption(config, MWConstants.LINKER_VERSION_ID, ver);
            } catch (BuildException e) {
                Activator.log(e.getMessage(), e);
            }
        }
    }

    @Override
    protected ITool getCompileTool(IToolChain tc) {
        for (ITool tool: tc.getTools()){
            if (tool.getBaseId().indexOf("ompil") > 0)
                return tool;
        }
        throw new IllegalStateException("Cannot identify compile tool for " + tc.getName());
    }

    @Override
    protected boolean isBCFOption(IOption opt) {
         return BCFSet.contains(opt.getBaseId());
    }
    
    private static final String[] BCF_OPTIONS = {
       MWConstants.COMPILER_VERSION_ID,
       MWConstants.LINKER_VERSION_ID,
       MWConstants.ASM_VERSION_ID,

       MWConstants.RF16_COMPILER_ID,
       MWConstants.RF16_ASM_ID,
       MWConstants.RF16_LINKER_ID,

       MWConstants.LPC_WIDTH_ID,
       MWConstants.LPC_WIDTH_VALUE_PREFIX,

       MWConstants.PC_WIDTH_LINKER_ID,
       MWConstants.PC_WIDTH_COMPILER_ID,
       MWConstants.PC_WIDTH_VALUE_PREFIX,

       MWConstants.ENDIAN_LINKER_ID,
       MWConstants.ENDIAN_COMPILER_ID,
       MWConstants.ENDIAN_ASM_ID,
       MWConstants.ENDIAN_VALUE_PREFIX,

       MWConstants.XATOMIC_COMPILER_ID,
       MWConstants.XATOMIC_ASM_ID,
       MWConstants.XATOMIC_LINKER_ID,

       MWConstants.XLL64_COMPILER_ID,
       MWConstants.XLL64_ASM_ID,
       MWConstants.XLL64_LINKER_ID,

       MWConstants.XCD_COMPILER_ID,
       MWConstants.XCD_ASM_ID,
       MWConstants.XCD_LINKER_ID,

       MWConstants.XDIVREM_COMPILER_ID,
       MWConstants.XDIVREM_ASM_ID,
       MWConstants.XDIVREM_LINKER_ID,
       MWConstants.XDIVREM_VALUE_PREFIX,

       MWConstants.XMPY_CYCLES_COMPILER_ID,
       MWConstants.XMPY_CYCLES_VALUE_PREFIX,

       MWConstants.XSWAP_COMPILER_ID,
       MWConstants.XSWAP_ASM_ID,
       MWConstants.XSWAP_LINKER_ID,

       MWConstants.XNORM_COMPILER_ID,
       MWConstants.XNORM_ASM_ID,
       MWConstants.XNORM_LINKER_ID,

       MWConstants.XMPY_COMPILER_ID,
       MWConstants.XMPY_ASM_ID,
       MWConstants.XMPY_LINKER_ID,

       MWConstants.XMPY16_COMPILER_ID,
       MWConstants.XMPY16_ASM_ID,
       MWConstants.XMPY16_LINKER_ID,

       MWConstants.XQMPYH_COMPILER_ID,
       MWConstants.XQMPYH_ASM_ID,
       MWConstants.XQMPYH_LINKER_ID,

       MWConstants.XMACD_COMPILER_ID,
       MWConstants.XMACD_ASM_ID,
       MWConstants.XMACD_LINKER_ID,

       MWConstants.XMAC_COMPILER_ID,
       MWConstants.XMAC_ASM_ID,
       MWConstants.XMAC_LINKER_ID,     

       MWConstants.XSA_COMPILER_ID,
       MWConstants.XSA_ASM_ID,
       MWConstants.XSA_LINKER_ID,

       MWConstants.XBS_COMPILER_ID,
       MWConstants.XBS_ASM_ID,
       MWConstants.XBS_LINKER_ID,

       MWConstants.XFPUD_DIV_COMPILER_ID,
       MWConstants.XFPUD_DIV_ASM_ID,
       MWConstants.XFPUD_DIV_LINKER_ID,

       MWConstants.XFPUD_COMPILER_ID,
       MWConstants.XFPUD_ASM_ID,
       MWConstants.XFPUD_LINKER_ID,

       MWConstants.XFPUS_COMPILER_ID,
       MWConstants.XFPUS_ASM_ID,
       MWConstants.XFPUS_LINKER_ID,

       MWConstants.XFPUS_DIV_COMPILER_ID,
       MWConstants.XFPUS_DIV_ASM_ID,
       MWConstants.XFPUS_DIV_LINKER_ID,

       MWConstants.XFPU_MAC_COMPILER_ID,
       MWConstants.XFPU_MAC_ASM_ID,
       MWConstants.XFPU_MAC_LINKER_ID,

       MWConstants.XTIMER0_COMPILER_ID,
       MWConstants.XTIMER0_ASM_ID,

       MWConstants.XTIMER1_COMPILER_ID,
       MWConstants.XTIMER1_ASM_ID,

       MWConstants.XTIMER_LINKER_ID,
       MWConstants.XTIMER0_LINKER_VALUE,
       MWConstants.XTIMER1_LINKER_VALUE,

       MWConstants.COMPILER_ADDITIONAL_OPTIONS,
       MWConstants.LINKER_ADDITIONAL_OPTIONS,
       MWConstants.ASM_ADDITIONAL_OPTIONS,

    };
    
    
    private static Set<String> BCFSet = new HashSet<String>();
    static {
        for (String s: BCF_OPTIONS){
            BCFSet.add(s);
        }
    }
    @Override
    protected boolean isExeToolChain(IToolChain tc) {
        String id = tc.getBaseId();
        return id.indexOf("exe") >= 0;
    }

    @Override
    protected ITool getAssemblerTool(IToolChain tc) {
        for (ITool tool: tc.getTools()){
            if (tool.getBaseId().indexOf("ssembler") > 0 || tool.getBaseId().indexOf("asm") > 0)
                return tool;
        }
        throw new IllegalStateException("Cannot identify assembler tool for " + tc.getName());
    }

    @Override
    protected ITool getLinkerTool(IToolChain tc) {
        for (ITool tool: tc.getTools()){
            if (tool.getBaseId().indexOf("inker") > 0)
                return tool;
        }
        throw new IllegalStateException("Cannot identify linker tool for " + tc.getName());
    }

    @Override
    protected String getBcfPathOptionId() {
        return MWConstants.COMPILER_BCF_ID;
    }

    @Override
    protected String getCfgPathOptionId() {
        return MWConstants.COMPILER_CFG_ID;
    }

    @Override
    protected String getCompilerVersionOptionID() {
        return MWConstants.COMPILER_VERSION_ID;
    }

    @Override
    protected File getBcfRootDirectory() {
        String path = System.getenv("METAWARE_ROOT");
        if (path != null) { 
            File f = new File(path,"arc/tcf");
            if (f.isDirectory()) return f;
        }
        path = System.getenv("PATH");
        for (String dir: path.split(File.pathSeparator)){
            if (new File(dir + "/mcc").exists()){
                File f = new File(dir);
                if (f.getParentFile() != null) {
                    f = new File(f.getParentFile(),"tcf");
                    if (f.isDirectory())
                        return f;
                }
            }
        }
        return null;
    }
    
//    private static File computeInstallLocation() {
//        URL url = Platform.getInstallLocation().getURL();
//        return url == null ? new File(".") : new File(url.getPath()).getParentFile();
//    }

    @Override
    public ProcessorFamily getProcessorFamily(IToolChain tc) {
        String id = tc.getBaseId();
        if (id.indexOf(".a4") > 0 || id.indexOf(".arc4") > 0) return ProcessorFamily.A4;
        if (id.indexOf(".a5") > 0 || id.indexOf("arc5") > 0) return ProcessorFamily.A5;
        if (id.indexOf("601") > 0) return ProcessorFamily.ARC601;
        if (id.indexOf(".a6") > 0 || id.indexOf(".arc600") > 0) return ProcessorFamily.ARC600;
        if (id.indexOf(".a7") > 0 || id.indexOf(".arc700") > 0) return ProcessorFamily.ARC700;
        if (id.indexOf("em") > 0) return ProcessorFamily.ARCEM;
        if (id.indexOf("hs") > 0) return ProcessorFamily.ARCHS;
        return null;
    }

    @Override
    public boolean isGenericARC(IToolChain tc) {
        return getProcessorFamily(tc) == null;
    }

}
