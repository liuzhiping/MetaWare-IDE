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
;** File: profisrc.met
;**
;** Comments:      
;**   This assembler file contains the function for intercepting the
;** timer isr for the Profiler.
;**                                                               
;*************************************************************************
;END**********************************************************************


        .include psp_cnfg.met
        .include types.met
        .include psp_prv.met

        .extern _mwprofile_tick
        .extern _mwprof_countdown_ticks

        .extern _psp_prof_countdown_ticks
        .extern _psp_prof_counter
        .extern _psp_prof_enabled
        .extern _psp_prof_vector_number
        .extern _psp_prof_timer_tcontrol
        .extern _psp_prof_timer_tcount

        .extern _int_kernel_isr

;ISR*-----------------------------------------------------------------------
;
; ISR Name : _psp_prof_isr1()
; Comments :
;   This is the assembler level interrupt isr that intercepts all 
; timer interrupts for use with the profiler
; _psp_prof_isr1 uses ilink1, _psp_prof_isr2 uses ilink2
;
;---------------------------------------------------------------------------

        .text
        .global _psp_prof_isr1
_psp_prof_isr1:
        flag   0
        sub_s  %sp,%sp,16
        st_s   %r0,[%sp,0]
        st_s   %r1,[%sp,4]
        st_s   %r2,[%sp,8]
        st     %blink,[%sp,12]

        ld     %r0,[_psp_prof_enabled]
        sub.f  0,%r0,0
        beq_s  profiling_not_enabled
        
        and    %r0,%ilink1,0x00FFFFFF
.ifdef __Xbarrel_shifter
        asl    %r0,%r0,2
.else
        lsl    %r0,%r0                
        lsl    %r0,%r0                
.endif
        
        bl.d   _mwprofile_tick
        sub_s  %sp,%sp,16
        add_s  %sp,%sp,16
        
profiling_not_enabled:
; Reset the timer
        ld     %r2,[_psp_prof_timer_tcontrol]
        ld     %r1,[_mwprof_countdown_ticks]
        sr     3,[%r2]
; Add fudge for regular profiling, but not for Kernel profiling
        add    %r1,%r1,16       
        ld     %r0,[_psp_prof_timer_wrap]
        ld     %r2,[_psp_prof_timer_tcount]
        sub_s  %r0,%r0,%r1
        sr     %r0,[%r2]

        ld     %r2,[_psp_prof_countdown_ticks]
        ld     %r1,[_mwprof_countdown_ticks]
        ld     %r0,[_psp_prof_counter]
        add_s  %r0,%r0,%r1
        sub.f  0,%r0,%r2
        st     %r0,[_psp_prof_counter]
        blo    _psp_prof_isr_exit

; Start processing for handing off to MQX
        xor    %r0,%r0,%r0
        st     %r0,[_psp_prof_counter]
        ld     %blink,[%sp,12]
        ld_s   %r2,[%sp,8]
        ld_s   %r1,[%sp,4]
        ld_s   %r0,[%sp,0]
; Start CR 755
;        add    %sp,%sp,16
;        sub    %sp,%sp,CONTEXT_SIZE
;        st     %r4,[%sp,R4_LOC-CONTEXT_SIZE]
;        ld     %r4,[_psp_prof_vector_number]

        add    %sp,%sp,16
        st     %r1,[%sp,R1_LOC-CONTEXT_SIZE]
        ld     %r1,[_psp_prof_vector_number]
; End   CR 755
        j      _int_kernel_isr

_psp_prof_isr_exit:
        ld     %blink,[%sp,12]
        ld_s   %r2,[%sp,8]
        ld_s   %r1,[%sp,4]
        ld_s   %r0,[%sp,0]
        add_s  %sp,%sp,16
        j.f    [%ilink1]
                   
        .type _psp_prof_isr1, @function
        .size _psp_prof_isr1, . - _psp_prof_isr1

        .end

; EOF
