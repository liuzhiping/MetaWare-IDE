package com.arc.cdt.toolchain.ui.bcf;

public enum BCFKey {
    Family("processor.family"), 
    Core("processor.core_version"), 
    RF16("processor.rf16"), 
    RGF_NUM_BANKS("processor.rgf_num_banks"), 
    RGF_BANKED_REGS("processor.rgf_banked_regs"),
    LPC_SIZE("processor.lpc_size"), 
    PC_SIZE("processor.pc_size"), 
    ENDIAN("processor.endian"), 
    ADDR_SIZE("processor.addr_size"),
    XATOMIC("processor.Xatomic"),
    XLL64("processor.Xll64"),
    XCD("processor.Xcode_density"),
    XDIV_REM("processor.Xdiv_rem"),
    XSWAP("processor.Xswap"),
    XNORM("processor.Xnorm"),
    XMPY("processor.Xmpy"),
    XMPY16("processor.Xmpy16"),
    XQMPYH("processor.Xqmpyh"),
    XMPY_OPTION("processor.mpy_option"),
    XMPY_CYCLES("processor.Xmpy_cycles"),
    XSA("processor.Xshift_assist"),
    XBS("processor.Xbarrel_shifter"),
    XFPUD_DIV("processor.Xfpud_div"),
    XFPUD("processor.Xfpud"),
    XFPUS("processor.Xfpus"),
    XFPUS_DIV("processor.Xfpus_div"),
    XFPU_MAC("processor.Xfpu_mac"),
    XTIMER0("processor.Xtimer0"),
    XTIMER1("processor.Xtimer1"),
    XRTC("processor.Xrtc"),
    MPU("processor.mpu"),
    MPU_REGIONS("processor.mpu.regions"),
    INTERRUPTS("processor.interrupts"),
    INTERRUPT_PRIORITIES("processor.interrupt_priorities"),
    EXT_INTERRUPTS("processor.ext_interrupts"),
    FIRQ("processor.firq"),
    INTERRUPT_BASE("processor.interrupt_base"),
    DCACHE_SIZE("processor.dcache.size"),
    DCACHE_LINE_SIZE("processor.dcache.line_size"),
    DCACHE_WAYS("processor.dcache.ways"),
    ICACHE_SIZE("processor.icache.size"),
    ICACHE_LINE_SIZE("processor.icache.line_size"),
    ICACHE_WAYS("processor.icache.ways"),
    DCCM_SIZE("processor.dccm_size"),
    DCCM_BASE("processor.dccm_base"),
    ICCM_SIZE("processor.iccm_size"),
    ICCM_BASE("processor.iccm_base"),
    ICCM1_SIZE("processor.iccm1_size"),
    ICCM1_BASE("processor.iccm1_base");

    private final String key;

    BCFKey(String s) {
        key = s;
    }

    @Override
    public String toString() {
        return key;
    }

}