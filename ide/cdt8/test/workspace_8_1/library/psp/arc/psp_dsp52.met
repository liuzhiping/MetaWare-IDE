;HEADER*******************************************************************
;*************************************************************************
;** 
;** Copyright (c) 1989-2003 ARC International.
;** All rights reserved                                          
;**                                                              
;** This software embodies materials and concepts which are      
;** confidential to ARC International and is made
;** available solely pursuant to the terms of a written license   
;** agreement with ARC International             
;**
;** File: psp_dsp52.met
;**
;** Comments:      
;**   This assembler file contains functions for saving and restore DSP
;** task contexts
;**                                                               
;*************************************************************************
;END**********************************************************************


         .include psp_cnfg.met
         .include types.met
         .include psp_prv.met
         .include psp_dsp52prv.met

         .extern _sched_save_dsp_ret

         .text

;FUNCTION*-------------------------------------------------------------------
; 
; Function Name    : _sched_save_dsp
; Comments         :
; This function is called during a context switch to save DSP registers for
; one task and to restore the DSP registers of another task.
; 
;     r2 - contains pointer to kernel data
;     r3 - contains active_ptr
;END*------------------------------------------------------------------------

        .global    _sched_save_dsp

        .align  MQX_CODE_ALIGNMENT
_sched_save_dsp:
;       See if a dsp task is currently active
        ld       %r0,[%r2,KD_DSP_ACTIVE_PTR]
        or.f     0,%r0,%r0
        beq.d    restore_dsp_context
        ld       %r10,[%r0,TD_DSP_CONTEXT_PTR]

;       See if the dsp task is in fact the task being scheduled
;       So, no need to save and restore pointers
        sub.f    0,%r3,%r0
        beq      _sched_save_dsp_ret

        ld       %r11,[%r0,TD_FLAGS]

;       Save the dsp registers

        ; Next do the FBF extension to the XMAC
        .ifdef AUX_FBF_STORE_16_LOC
        lr      %r4,[PSP_AUX_FBF_STORE_16]
        st      %r4,[%r10, AUX_FBF_STORE_16_LOC]
	.endif

;       Next do the CRC extension, if it exists
    	.ifdef AUX_CRC_POLY_LOC
	lr      %r4,[PSP_AUX_CRC_POLY]
	st      %r4,[%r10, AUX_CRC_POLY_LOC]
    	.endif
    	.ifdef AUX_CRC_MODE_LOC
        lr      %r4,[PSP_AUX_CRC_MODE]
        st      %r4,[%r10, AUX_CRC_MODE_LOC]
    	.endif

.ifdef __Xxy
	; There are so many X/Y registers that we don't want to
	; save and restore them unless they are actually used
        and.f   0,%r11,DSP_USES_XY
        beq     skip_xy_save
        .ifdef AUX_XY_CFG_LOC
        lr      %r4,[PSP_AUX_XY_CFG]
        st      %r4,[%r10, AUX_XY_CFG_LOC]
	.endif
        .ifdef AUX_XY_BURSTSYS_LOC
        lr      %r4,[PSP_AUX_XY_BURSTSYS]
        st      %r4,[%r10, AUX_XY_BURSTSYS_LOC]
	.endif
        .ifdef AUX_XY_BURSTXYM_LOC
        lr      %r4,[PSP_AUX_XY_BURSTXYM]
        st      %r4,[%r10, AUX_XY_BURSTXYM_LOC]
	.endif
        .ifdef AUX_XY_BURSTSZ_LOC
        lr      %r4,[PSP_AUX_XY_BURSTSZ]
        st      %r4,[%r10, AUX_XY_BURSTSZ_LOC]
	.endif
        .ifdef AUX_XY_BURSTVAL_LOC
        lr      %r4,[PSP_AUX_XY_BURSTVAL]
        st      %r4,[%r10, AUX_XY_BURSTVAL_LOC]
	.endif
        .ifdef AUX_XY_LSBASEX_LOC
        lr      %r4,[PSP_AUX_XY_LSBASEX]
        st      %r4,[%r10, AUX_XY_LSBASEX_LOC]
	.endif
        .ifdef AUX_XY_LSBASEY_LOC
        lr      %r4,[PSP_AUX_XY_LSBASEY]
        st      %r4,[%r10, AUX_XY_LSBASEY_LOC]
	.endif
        .ifdef AUX_XY_AX0_LOC
        lr      %r4,[PSP_AUX_XY_AX0]
        st      %r4,[%r10, AUX_XY_AX0_LOC]
	.endif
        .ifdef AUX_XY_AY0_LOC
        lr      %r4,[PSP_AUX_XY_AY0]
        st      %r4,[%r10, AUX_XY_AY0_LOC]
	.endif
        .ifdef AUX_XY_MX00_LOC
        lr      %r4,[PSP_AUX_XY_MX00]
        st      %r4,[%r10, AUX_XY_MX00_LOC]
	.endif
        .ifdef AUX_XY_MX01_LOC
        lr      %r4,[PSP_AUX_XY_MX01]
        st      %r4,[%r10, AUX_XY_MX01_LOC]
	.endif
        .ifdef AUX_XY_MY00_LOC
        lr      %r4,[PSP_AUX_XY_MY00]
        st      %r4,[%r10, AUX_XY_MY00_LOC]
	.endif
        .ifdef AUX_XY_MY01_LOC
        lr      %r4,[PSP_AUX_XY_MY01]
        st      %r4,[%r10, AUX_XY_MY01_LOC]
	.endif
        .ifdef AUX_XY_AX1_LOC
        lr      %r4,[PSP_AUX_XY_AX1]
        st      %r4,[%r10, AUX_XY_AX1_LOC]
	.endif
        .ifdef AUX_XY_AY1_LOC
        lr      %r4,[PSP_AUX_XY_AY1]
        st      %r4,[%r10, AUX_XY_AY1_LOC]
	.endif
        .ifdef AUX_XY_MX10_LOC
        lr      %r4,[PSP_AUX_XY_MX10]
        st      %r4,[%r10, AUX_XY_MX10_LOC]
	.endif
        .ifdef AUX_XY_MX11_LOC
        lr      %r4,[PSP_AUX_XY_MX11]
        st      %r4,[%r10, AUX_XY_MX11_LOC]
	.endif
        .ifdef AUX_XY_MY10_LOC
        lr      %r4,[PSP_AUX_XY_MY10]
        st      %r4,[%r10, AUX_XY_MY10_LOC]
	.endif
        .ifdef AUX_XY_MY11_LOC
        lr      %r4,[PSP_AUX_XY_MY11]
        st      %r4,[%r10, AUX_XY_MY11_LOC]
	.endif
        .ifdef AUX_XY_AX2_LOC
        lr      %r4,[PSP_AUX_XY_AX2]
        st      %r4,[%r10, AUX_XY_AX2_LOC]
	.endif
        .ifdef AUX_XY_AY2_LOC
        lr      %r4,[PSP_AUX_XY_AY2]
        st      %r4,[%r10, AUX_XY_AY2_LOC]
	.endif
        .ifdef AUX_XY_MX20_LOC
        lr      %r4,[PSP_AUX_XY_MX20]
        st      %r4,[%r10, AUX_XY_MX20_LOC]
	.endif
        .ifdef AUX_XY_MX21_LOC
        lr      %r4,[PSP_AUX_XY_MX21]
        st      %r4,[%r10, AUX_XY_MX21_LOC]
	.endif
        .ifdef AUX_XY_MY20_LOC
        lr      %r4,[PSP_AUX_XY_MY20]
        st      %r4,[%r10, AUX_XY_MY20_LOC]
	.endif
        .ifdef AUX_XY_MY21_LOC
        lr      %r4,[PSP_AUX_XY_MY21]
        st      %r4,[%r10, AUX_XY_MY21_LOC]
	.endif
        .ifdef AUX_XY_AX3_LOC
        lr      %r4,[PSP_AUX_XY_AX3]
        st      %r4,[%r10, AUX_XY_AX3_LOC]
	.endif
        .ifdef AUX_XY_AY3_LOC
        lr      %r4,[PSP_AUX_XY_AY3]
        st      %r4,[%r10, AUX_XY_AY3_LOC]
	.endif
        .ifdef AUX_XY_MX30_LOC
        lr      %r4,[PSP_AUX_XY_MX30]
        st      %r4,[%r10, AUX_XY_MX30_LOC]
	.endif
        .ifdef AUX_XY_MX31_LOC
        lr      %r4,[PSP_AUX_XY_MX31]
        st      %r4,[%r10, AUX_XY_MX31_LOC]
	.endif
        .ifdef AUX_XY_MY30_LOC
        lr      %r4,[PSP_AUX_XY_MY30]
        st      %r4,[%r10, AUX_XY_MY30_LOC]
	.endif
        .ifdef AUX_XY_MY31_LOC
        lr      %r4,[PSP_AUX_XY_MY31]
        st      %r4,[%r10, AUX_XY_MY31_LOC]
	.endif
skip_xy_save:
.endif	; .ifdef __Xxy

	.ifdef AUX_VBFDW_MODE_LOC
        lr      %r4,[PSP_AUX_VBFDW_MODE]
        st      %r4,[%r10, AUX_VBFDW_MODE_LOC]
	.endif
	.ifdef AUX_VBFDW_BM0_LOC
        lr      %r4,[PSP_AUX_VBFDW_BM0]
        st      %r4,[%r10, AUX_VBFDW_BM0_LOC]
	.endif
	.ifdef AUX_VBFDW_BM1_LOC
        lr      %r4,[PSP_AUX_VBFDW_BM1]
        st      %r4,[%r10, AUX_VBFDW_BM1_LOC]
	.endif
	.ifdef AUX_VBFDW_ACCU_LOC
        lr      %r4,[PSP_AUX_VBFDW_ACCU]
        st      %r4,[%r10, AUX_VBFDW_ACCU_LOC]
	.endif
	.ifdef AUX_VBFDW_OFST_LOC
        lr      %r4,[PSP_AUX_VBFDW_OFST]
        st      %r4,[%r10, AUX_VBFDW_OFST_LOC]
	.endif
	.ifdef AUX_VBFDW_INTSTAT_LOC
        lr      %r4,[PSP_AUX_VBFDW_INTSTAT]
        st      %r4,[%r10, AUX_VBFDW_INTSTAT_LOC]
	.endif

        or       %r11,%r11,DSP_CONTEXT_SAVED
        st       %r11,[%r0,TD_FLAGS]        


restore_dsp_context:

;       Restore the context of the current DSP task if necessary
        ld       %r12,[%r3,TD_FLAGS]
        st       %r3,[%r2,KD_DSP_ACTIVE_PTR]
        and.f    0,%r12,DSP_CONTEXT_SAVED
        beq      no_dsp_registers_to_restore
        ld       %r10,[%r3,TD_DSP_CONTEXT_PTR]

;       Now restore the dsp context

        ; Next do the FBF extension to the XMAC
        .ifdef AUX_FBF_STORE_16_LOC
        ld      %r4,[%r10, AUX_FBF_STORE_16_LOC]
        sr      %r4,[PSP_AUX_FBF_STORE_16]
	.endif

;       Next do the CRC extension, if it exists
    	.ifdef AUX_CRC_POLY_LOC
	ld      %r4,[%r10, AUX_CRC_POLY_LOC]
	sr      %r4,[PSP_AUX_CRC_POLY]
    	.endif
    	.ifdef AUX_CRC_MODE_LOC
        ld      %r4,[%r10, AUX_CRC_MODE_LOC]
        sr      %r4,[PSP_AUX_CRC_MODE]
    	.endif

.ifdef __Xxy
	; There are so many X/Y registers that we don't want to
	; save and restore them unless they are actually used
        and.f   0,%r12,DSP_USES_XY
        beq     skip_xy_restore
	.ifdef AUX_XY_CFG_LOC
        ld      %r4,[%r10, AUX_XY_CFG_LOC]
        sr      %r4,[PSP_AUX_XY_CFG]
	.endif
	.ifdef AUX_XY_BURSTSYS_LOC
        ld      %r4,[%r10, AUX_XY_BURSTSYS_LOC]
        sr      %r4,[PSP_AUX_XY_BURSTSYS]
	.endif
	.ifdef AUX_XY_BURSTXYM_LOC
        ld      %r4,[%r10, AUX_XY_BURSTXYM_LOC]
        sr      %r4,[PSP_AUX_XY_BURSTXYM]
	.endif
	.ifdef AUX_XY_BURSTSZ_LOC
        ld      %r4,[%r10, AUX_XY_BURSTSZ_LOC]
        sr      %r4,[PSP_AUX_XY_BURSTSZ]
	.endif
	.ifdef AUX_XY_BURSTVAL_LOC
        ld      %r4,[%r10, AUX_XY_BURSTVAL_LOC]
        sr      %r4,[PSP_AUX_XY_BURSTVAL]
	.endif
	.ifdef AUX_XY_LSBASEX_LOC
        ld      %r4,[%r10, AUX_XY_LSBASEX_LOC]
        sr      %r4,[PSP_AUX_XY_LSBASEX]
	.endif
	.ifdef AUX_XY_LSBASEY_LOC
        ld      %r4,[%r10, AUX_XY_LSBASEY_LOC]
        sr      %r4,[PSP_AUX_XY_LSBASEY]
	.endif
	.ifdef AUX_XY_AX0_LOC
        ld      %r4,[%r10, AUX_XY_AX0_LOC]
        sr      %r4,[PSP_AUX_XY_AX0]
	.endif
	.ifdef AUX_XY_AY0_LOC
        ld      %r4,[%r10, AUX_XY_AY0_LOC]
        sr      %r4,[PSP_AUX_XY_AY0]
	.endif
	.ifdef AUX_XY_MX00_LOC
        ld      %r4,[%r10, AUX_XY_MX00_LOC]
        sr      %r4,[PSP_AUX_XY_MX00]
	.endif
	.ifdef AUX_XY_MX01_LOC
        ld      %r4,[%r10, AUX_XY_MX01_LOC]
        sr      %r4,[PSP_AUX_XY_MX01]
	.endif
	.ifdef AUX_XY_MY00_LOC
        ld      %r4,[%r10, AUX_XY_MY00_LOC]
        sr      %r4,[PSP_AUX_XY_MY00]
	.endif
	.ifdef AUX_XY_MY01_LOC
        ld      %r4,[%r10, AUX_XY_MY01_LOC]
        sr      %r4,[PSP_AUX_XY_MY01]
	.endif
	.ifdef AUX_XY_AX1_LOC
        ld      %r4,[%r10, AUX_XY_AX1_LOC]
        sr      %r4,[PSP_AUX_XY_AX1]
	.endif
	.ifdef AUX_XY_AY1_LOC
        ld      %r4,[%r10, AUX_XY_AY1_LOC]
        sr      %r4,[PSP_AUX_XY_AY1]
	.endif
	.ifdef AUX_XY_MX10_LOC
        ld      %r4,[%r10, AUX_XY_MX10_LOC]
        sr      %r4,[PSP_AUX_XY_MX10]
	.endif
	.ifdef AUX_XY_MX11_LOC
        ld      %r4,[%r10, AUX_XY_MX11_LOC]
        sr      %r4,[PSP_AUX_XY_MX11]
	.endif
	.ifdef AUX_XY_MY10_LOC
        ld      %r4,[%r10, AUX_XY_MY10_LOC]
        sr      %r4,[PSP_AUX_XY_MY10]
	.endif
	.ifdef AUX_XY_MY11_LOC
        ld      %r4,[%r10, AUX_XY_MY11_LOC]
        sr      %r4,[PSP_AUX_XY_MY11]
	.endif
	.ifdef AUX_XY_AX2_LOC
        ld      %r4,[%r10, AUX_XY_AX2_LOC]
        sr      %r4,[PSP_AUX_XY_AX2]
	.endif
	.ifdef AUX_XY_AY2_LOC
        ld      %r4,[%r10, AUX_XY_AY2_LOC]
        sr      %r4,[PSP_AUX_XY_AY2]
	.endif
	.ifdef AUX_XY_MX20_LOC
        ld      %r4,[%r10, AUX_XY_MX20_LOC]
        sr      %r4,[PSP_AUX_XY_MX20]
	.endif
	.ifdef AUX_XY_MX21_LOC
        ld      %r4,[%r10, AUX_XY_MX21_LOC]
        sr      %r4,[PSP_AUX_XY_MX21]
	.endif
	.ifdef AUX_XY_MY20_LOC
        ld      %r4,[%r10, AUX_XY_MY20_LOC]
        sr      %r4,[PSP_AUX_XY_MY20]
	.endif
	.ifdef AUX_XY_MY21_LOC
        ld      %r4,[%r10, AUX_XY_MY21_LOC]
        sr      %r4,[PSP_AUX_XY_MY21]
	.endif
	.ifdef AUX_XY_AX3_LOC
        ld      %r4,[%r10, AUX_XY_AX3_LOC]
        sr      %r4,[PSP_AUX_XY_AX3]
	.endif
	.ifdef AUX_XY_AY3_LOC
        ld      %r4,[%r10, AUX_XY_AY3_LOC]
        sr      %r4,[PSP_AUX_XY_AY3]
	.endif
	.ifdef AUX_XY_MX30_LOC
        ld      %r4,[%r10, AUX_XY_MX30_LOC]
        sr      %r4,[PSP_AUX_XY_MX30]
	.endif
	.ifdef AUX_XY_MX31_LOC
        ld      %r4,[%r10, AUX_XY_MX31_LOC]
        sr      %r4,[PSP_AUX_XY_MX31]
	.endif
	.ifdef AUX_XY_MY30_LOC
        ld      %r4,[%r10, AUX_XY_MY30_LOC]
        sr      %r4,[PSP_AUX_XY_MY30]
	.endif
	.ifdef AUX_XY_MY31_LOC
        ld      %r4,[%r10, AUX_XY_MY31_LOC]
        sr      %r4,[PSP_AUX_XY_MY31]
	.endif
skip_xy_restore:
.endif ; .ifdef __Xxy

	.ifdef AUX_VBFDW_MODE_LOC
        ld      %r4,[%r10, AUX_VBFDW_MODE_LOC]
        sr      %r4,[PSP_AUX_VBFDW_MODE]
	.endif
	.ifdef AUX_VBFDW_BM0_LOC
        ld      %r4,[%r10, AUX_VBFDW_BM0_LOC]
        sr      %r4,[PSP_AUX_VBFDW_BM0]
	.endif
	.ifdef AUX_VBFDW_BM1_LOC
        ld      %r4,[%r10, AUX_VBFDW_BM1_LOC]
        sr      %r4,[PSP_AUX_VBFDW_BM1]
	.endif
	.ifdef AUX_VBFDW_ACCU_LOC
        ld      %r4,[%r10, AUX_VBFDW_ACCU_LOC]
        sr      %r4,[PSP_AUX_VBFDW_ACCU]
	.endif
	.ifdef AUX_VBFDW_OFST_LOC
        ld      %r4,[%r10, AUX_VBFDW_OFST_LOC]
        sr      %r4,[PSP_AUX_VBFDW_OFST]
	.endif
	.ifdef AUX_VBFDW_INTSTAT_LOC
        ld      %r4,[%r10, AUX_VBFDW_INTSTAT_LOC]
        sr      %r4,[PSP_AUX_VBFDW_INTSTAT]
	.endif

no_dsp_registers_to_restore:
        bic      %r12,%r12,DSP_CONTEXT_SAVED
        bra.d    _sched_save_dsp_ret
        st       %r12,[%r3,TD_FLAGS]

	.type _sched_save_dsp,@function
	.size _sched_save_dsp,.-_sched_save_dsp
