;HEADER*******************************************************************
;*************************************************************************
;**
;** Copyright (c) 1989-2007 ARC International.
;** All rights reserved
;**
;** This software embodies materials and concepts which are
;** confidential to ARC International and is made
;** available solely pursuant to the terms of a written license
;** agreement with ARC International
;**
;** File: restart.met
;**
;** Comments:
;**   This assembler file contains functions for restart (wakeup) the 
;**   system from sleep. This file has been added to address CR 2396.
;**
;*************************************************************************
;END**********************************************************************


    .include psp_cnfg.met
    .include types.met
    .include psp_prv.met

.if MQX_USE_PMU    
	.file   "restart.s"

    .text

;FUNCTION*-------------------------------------------------------------------
;
; Function Name    : _restart
; Comments         :
; This function intercepts the reset and determines the source of the wake-up.
; The PMU status register identifies the source.
;
;END*------------------------------------------------------------------------

    .global    _restart

    .align  MQX_CODE_ALIGNMENT

_restart:
    lr    %r0,[PSP_PMU_STATUS] ; get PMU status - cause of wakeup

    mov_s %r1,0x00     ; reset cause was external reset
    cmp_s %r0,%r1
    jeq   _start

    mov   sp, _estack  ; initialize stack pointer
    
.if MQX_KERNEL_LOGGING
    jl    _klog_pmu_powerdown_end
.endif

    jl    _pmu_wakeup

   .type _restart, @function
   .size _restart, . - _restart

.endif ; MQX_USE_PMU