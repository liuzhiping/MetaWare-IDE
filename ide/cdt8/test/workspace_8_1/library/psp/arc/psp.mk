#
# Define the following variables:
#
#     srcs          list of source files without extensions
#     srcdirs       list of directories containing source files
#
#     dups          list of files to be copied to the output directory
#     dupdirs       list of directories containing the 'dups' files
#

dupdirs += psp$(PS)$(MQX_PSP_SRC_DIR)
dups    += psp.h psptypes.h psp_rev.h
dups    += psp_cnfg.$(MQX_AEXT)
dups	+= arc_cnfg.h
dups    += arc.h $(MQX_PSP).h psp_time.h psp_math.h
dups    += ac_bvci.h ac_dsp31.h ac_tlb.h

srcdirs += psp$(PS)$(MQX_PSP_SRC_DIR)
srcs    += \
   dispatch ipsum    \
   prof_isr psp_util \
   arc      psp_prof \
   pspcach1 pspcach2 pspcach3 pspcach4 \
   pspcach5 pspcach6 pspcach7 pspcach8 \
   pspcach9 pspcacha \
   int_gkis int_inst int_kisr int_lvl  \
   int_sr   int_unx  int_vtab int_xcpt \
   mem_copy mem_zero \
   sc_irdyq stack_bu stack_de stack_st \
   pspiinit psp_mat1 psp_mat2 psp_mat3 \
   psp_tkti \
   psp_dati psp_hoti psp_miti psp_seti \
   psp_msti psp_usti psp_nsti psp_psti \
   psp_tips psp_tins psp_tius psp_msti \
   psp_tise psp_timn psp_tihr psp_tida \
   psp_tiad psp_tipr psp_tiol psp_tims \
   psp_tisu psp_tixd psp_tinm \
   psp_mstiq psp_gelms psp_ctx \
   int_ivunx int_vunx

# Start CR 818
ifneq ($(findstring $(MQX_CPU_SUPPORT),arcta52 arca6 arca7 ),)
srcs     +=  \
   psp_dsp52 
endif
# End   CR 818

dispatch.$(MQX_OEXT): pspcont.s psp_prv.$(MQX_COMPILER)
prof_isr.$(MQX_OEXT): pspcont.s
psp_dsp52.$(MQX_OEXT): pspcont.s
psp_dsp52prv.$(MQX_OEXT): pspcont.s
psp_prv.$(MQX_OEXT): pspcont.s

## pspcont.s is automatically generated from psp_ctx.c by the
## MetaWare compiler.  We do this so the compiler will calculate
## structure member offsets for assembly code so that the assembly
## will always be in sync with C/C++.
pspcont.s: psp_ctx.$(MQX_OEXT)
pspcont.$(MQX_OEXT): pspcont.s

# EOF
