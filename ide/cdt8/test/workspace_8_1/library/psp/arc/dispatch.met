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
;** File: dispatch.met
;**
;** Comments:
;**   This assembler file contains functions for task scheduling
;**
;*************************************************************************
;END**********************************************************************


         .include psp_cnfg.met
         .include types.met
         .include psp_dsp52prv.met
         .include psp_prv.met

         .text

;FUNCTION*-------------------------------------------------------------------
;
; Function Name    : _task_block
; Comments         :
; This function is called by a task to save its context and remove
; itself from its ready queue. The next runnable task in the ready queues
; is made active and dispatched.  The state of the task is marked as being
; blocked.
;
;END*------------------------------------------------------------------------

        .global    _task_block

        .align  MQX_CODE_ALIGNMENT
_task_block:

.if MQX_SMALL_CODE_SIZE
        mov     %r5,_task_block_save_reg_return
        bra_s   _sched_save_nonscratch_registers
_task_block_save_reg_return:
.else
        SAVE_NONSCRATCH_REGISTERS       ; Save the context of the active task
.endif

        GET_KERNEL_DATA                 ; Get address of kernel data
        ldw_s   %r1,[%r2,KD_DISABLE_SR]
        ld_s    %r3,[%r2,KD_ACTIVE_PTR] ; get active td
        flag    %r1                     ; DISABLE INTERRUPTS
        ld_s    %r1,[%r3,TD_STATE]
        st      %sp,[%r3,TD_STACK_PTR]  ; save stack pointer
        bset_s  %r1,%r1,0
        st_s    %r1,[%r3,TD_STATE]      ; set the blocked bit

        ld      %r1,[%r3,TD_FLAGS]      ; get flags   
        and     %r1,%r1,~SCRATCH_REGISTERS_SAVED ; clear saving of scratch registers
        st      %r1,[%r3,TD_FLAGS]		; update flags

        KLOG    _klog_block_internal, no_klog_block

        ld_s    %r0,[%r3,TD_TD_PREV]
        ld_s    %r3,[%r3,TD_TD_NEXT]

;       Remove the active task from the ready queue.
        ld_s    %r1,[%r2,KD_CURRENT_READY_Q]  ; get current ready q
        st_s    %r3,[%r0,RQ_HEAD_READY_Q]
        bra.d   find_nonempty_queue    ; Search for the next task in the ready q
        st_s    %r0,[%r3,TD_TD_PREV]

   .type _task_block, @function
   .size _task_block, . - _task_block



;FUNCTION*-------------------------------------------------------------------
;
; Function Name    : _sched_start_internal
; Comments         :
;  This function is called from _mqx in order to start the task
;  scheduler running.
;
;END*------------------------------------------------------------------------

        .global    _sched_start_internal

        .align  MQX_CODE_ALIGNMENT
_sched_start_internal:
        GET_KERNEL_DATA
        ldw_s    %r3,[%r2,KD_CONFIG2]
        add      %r1,%r2,KD_CONFIG2
        or       %r3,%r3,PSP_CNFG2
        stw_s    %r3,[%r1]
        bra      _sched_run_internal

   .type _sched_start_internal, @function
   .size _sched_start_internal, . - _sched_start_internal

;FUNCTION*-------------------------------------------------------------------
;
; Function Name    : _sched_check_scheduler_internal
; Comments         :
;    This function is called to check whether scheduling is necessary
; It falls through into the next function
;
; Function Name    : _sched_execute_scheduler_internal
; Comments         :
;    This function is called by a task to save its context.
; This is usually done when the task has already been removed from the
; ready queue, and is on some other queue.
; However it can also be called so that the current tasks context is saved
; so that the scheduler can run (in case of a higher priority task being
; available.
;
; It falls through to the next function
;
; Function Name    : _sched_internal
; Comments         :
;   This function is the actual task scheduler... IT IS NOT CALLABLE from C
; rather, other assembler functions in this file jump to it.
;
;END*------------------------------------------------------------------------

        .global    _sched_check_scheduler_internal
        .global    _sched_execute_scheduler_internal
        .global    _sched_context_switch_internal
        .global    _sched_run_internal

.if MQX_DSP_REGISTERS_EXIST
        .global    _sched_save_dsp_ret
.endif

        .align  MQX_CODE_ALIGNMENT
_sched_run_internal:
        GET_KERNEL_DATA                      ; Get address of kernel data
        ld_s    %r1,[%r2,KD_CURRENT_READY_Q] ; get current ready q
        bra     find_nonempty_queue

.if MQX_SMALL_CODE_SIZE
        .align  MQX_CODE_ALIGNMENT
_sched_save_nonscratch_registers:
        SAVE_NONSCRATCH_REGISTERS       ; Save the context of the active task.
        j       [%r5]
.endif

        .align  MQX_CODE_ALIGNMENT
_sched_check_scheduler_internal:
        GET_KERNEL_DATA                  ; Get address of kernel data
        ldw_s   %r0,[%r2,KD_IN_ISR]
        ld_s    %r1,[%r2,KD_CURRENT_READY_Q]
        tst_s   %r0,%r0
        ld_s    %r3,[%r2,KD_ACTIVE_PTR]
        jne.d   [%blink]                 ; We are in an ISR, so return
        ld_s    %r1,[%r1,RQ_HEAD_READY_Q]
        cmp_s   %r1,%r3
        jeq     [%blink]                 ; Current task is still the active task

   .type _sched_check_scheduler_internal, @function
   .size _sched_check_scheduler_internal, . - _sched_check_scheduler_internal

        .align  MQX_CODE_ALIGNMENT
_sched_execute_scheduler_internal:
.if MQX_SMALL_CODE_SIZE
        mov     %r5,_sched_execute_scheduler_save_reg_return
        bra_s   _sched_save_nonscratch_registers
_sched_execute_scheduler_save_reg_return:
.else
        SAVE_NONSCRATCH_REGISTERS       ; Save the context of the active task.
.endif

        GET_KERNEL_DATA                 ; Get address of kernel data
        ldw     %r4,[%r2,KD_DISABLE_SR]
        ld_s    %r3,[%r2,KD_ACTIVE_PTR] ; get active td
        flag    %r4                     ; DISABLE INTERRUPTS
        ld_s    %r1,[%r2,KD_CURRENT_READY_Q] ; get current ready q
        st      %sp,[%r3,TD_STACK_PTR]  ; save stack pointer

        KLOG    _klog_execute_scheduler_internal, no_klog_execute_scheduler

;-------------------------------------------------------------
;
;  MAIN TASK SCHEDULER CODE
;  Arrive here from other assembler functions with a2 already set
         .global find_nonempty_queue

        .align  MQX_CODE_ALIGNMENT
find_nonempty_queue:
        ld_s    %r3,[%r1]               ; address of first td
find_nonempty_queue2:
        sub.f   0,%r3,%r1               ; ready_q structure itself?
        ld_s    %r0,[%r1,RQ_NEXT_Q]     ; try next queue
        bne.d   activate
        tst_s   %r0,%r0
        mov_s   %r1,%r0
        bne.d   find_nonempty_queue2
        ld_s    %r3,[%r1]               ; address of first td

       ; No task available to run
no_one_to_run:
;       Set up system task running and wait for an interrupt
        add     %r3,%r2,KD_SYSTEM_TD
        st_s    %r3,[%r2,KD_ACTIVE_PTR]
        stw     0,[%r2,KD_ACTIVE_SR]
        ld      %sp,[%r2,KD_INTERRUPT_STACK_PTR]
        .align 8
.if MQX_USE_PMU == 0
        flag    0x06                    ; enable all interrupts
.endif

; Start CR 2396
.if MQX_USE_PMU
        ld      %r9,[%r2,KD_PMU_STRUCT_PTR] 
        ldb     %r9,[%r9,EP_PMU_STRUCT_PMU_MODE]  ; check the PMU mode
        cmp     %r9,1                   ; PMU mode 1 (powerdown core and cache)
        jeq     _set_pmu_mode_1
        cmp     %r9,2                   ; PMU mode 2 (powerdown core, cache and CCM)
        jeq     _set_pmu_mode_2
        sleep   3                       ; we go to sleep and enable interrupt
        ld_s    %r1,[%r2,KD_READY_Q_LIST] ; get ready just in case
        bra_s   find_nonempty_queue

_set_pmu_mode_1:
.if MQX_KERNEL_LOGGING
        jl      _klog_pmu_powerdown_start
.endif
        DCACHE_FLASH _dcache_flush      ; flush data cache
        sleep   7                       ; enter PMU mode 1 and wait for interrupt
		                      
_set_pmu_mode_2:
.if MQX_KERNEL_LOGGING
        jl      _klog_pmu_powerdown_start
.endif
        DCACHE_FLASH _dcache_flush      ; flush data cache
        sleep   11                      ; enter PMU mode 2 and wait for interrupt
.else        
waiting:
        sleep                           ; nothing to do, wait for interrupt
        bra_s   waiting
        ld_s    %r1,[%r2,KD_READY_Q_LIST] ; get ready just in case
        bra_s   find_nonempty_queue
.endif
; End CR 2396

activate:

.if MQX_FP_REGISTERS_EXIST
;       Check to see if it needs the floating point co-processor
        ld       %r0,[%r3,TD_FLAGS]
        and.f    0,%r0,FP_TASK
        bne      do_floating_point
ret_do_floating_point:
.endif

.if MQX_DSP_REGISTERS_EXIST
;       Check to see if DSP registers need to be saved and restored
        ld       %r0,[%r3,TD_FLAGS]
        and.f    0,%r0,DSP_TASK
        bne      _sched_save_dsp
_sched_save_dsp_ret:
.endif

        ldw      %r0,[%r3,TD_TASK_SR]
        ld       %sp,[%r3,TD_STACK_PTR]  ; restore stack pointer
        st_s     %r1,[%r2,KD_CURRENT_READY_Q]
        st_s     %r3,[%r2,KD_ACTIVE_PTR]
        stw_s    %r0,[%r2,KD_ACTIVE_SR]

        KLOG     _klog_context_switch_internal, no_klog_context_switch


; Start CR 2396
.if MQX_USE_PMU
        ld       %r11,[%r2,KD_CURRENT_KERNEL_DVFS_MODE]
        cmp      %r11,1 ; current kernel mode is set to 100%, set it to zero
        and.eq.f %r11,%r11,0
        ld       %r10,[%r3,TD_TASK_DVFS_MODE]
        cmp      %r10,0 ; task specific mode is not set
        jeq      _ep_dvfs_adjust_time1
        cmp      %r10,1 ; task specific mode is set to 100%. Default is 100%, set it to zero
        and.eq.f %r10,%r10,0
        cmp      %r10,%r11   ; check if we need to change the DVFS mode (task based)
        jeq      _ep_dvfs_adjust_time1
        bne      _ep_dvfs_adjust_time2


_ep_dvfs_adjust_time1:
        ld       %r9,[%r2,KD_PMU_STRUCT_PTR]
        ldb      %r10,[%r9,EP_PMU_STRUCT_GLOBAL_DVFS_MODE] ; get global mode
        cmp      %r10,1 ; global mode is 100%, set it to zero
        and.eq.f %r10,%r10,0
        cmp      %r10,%r11   ; check if we need to change the DVFS mode (global based)
        jeq      _ep_no_time_adjust
        bne      _ep_dvfs_adjust_time2

_ep_dvfs_adjust_time2:
        jl       _ep_dvfs_adjust_time

        ; Macro RESTORE_ALL_REGISTERS expects kernel data address stored in r2 
        ; and active task descriptor address stored in r3. Calling function 
        ; _ep_dvfs_adjust_time trashes r2 and r3. We need to restored them.
        GET_KERNEL_DATA                  ; Get address of kernel data
        ld_s     %r3,[%r2,KD_ACTIVE_PTR] ; Get address of active task descriptor

_ep_no_time_adjust:
.endif
; End CR 2396

        RESTORE_ALL_REGISTERS            ; restore task context

_sched_context_switch_internal:
        add      %sp,%sp,CONTEXT_SIZE
        j.f      [%ilink2]

;-------------------------------------------------------------

.if MQX_FP_REGISTERS_EXIST
do_floating_point:
        bra    ret_do_floating_point
.endif

;-------------------------------------------------------------

   .type _sched_execute_scheduler_internal, @function
   .size _sched_execute_scheduler_internal, . - _sched_execute_scheduler_internal

;ISR*-----------------------------------------------------------------------
;
; Function Name    : _int_kernel_isr()
; Comments         :
;   This is the assembler level interrupt isr that intercepts all
; interrupts. (When installed on a particular vector).
;
;   Enough registers are saved so that a 'C' isr can be called.
;   If the current stack is not the interrupt stack, then the stack is
;   switched over.
;
;   An interrupt 'context' is created on the stack, thus allowing for proper
;   operation of MQX 'C' functions that access the error code and _int_enable
;   and _int_disable
;
;   Then interrupt handlers are checked for.  If they have not been installed,
;   or the current ISR number is out the range of installed handlers,
;   the DEFAULT_ISR is called.
;
;   If they have been installed then if a user 'C' hander has not been installed
;   for this vector, the DEFAULT_ISR is called.
;
;   After returning from the call to the 'C' isr the following is checked:
;   If this is a nested ISR, then we do an interrupt return.
;   If the current task is still the highest priority running task, we
;   do an interrupt return.
;   Otherwise we must save the full context for the current task, and
;   enter the scheduler.
;
;END*------------------------------------------------------------------------

        .align  MQX_CODE_ALIGNMENT
_isr_save_extra_registers:
        SAVE_REST_ISR_REGISTERS
        j    [%r5]

        .global    _int_kernel_isr
        .global    _int_kernel_isr_return_internal

     .macro HANDLE_INT, FNAME, NUM
        .align  MQX_CODE_ALIGNMENT
        .global FNAME
FNAME:
        .cfa_bf FNAME
;             push   %r1
;             mov_s  %r1,\&NUM
;             bra_s  _int_kernel_isr
              flag   0
.if (CONTEXT_SIZE < 0x100)
              st     %r1,[%sp,R1_LOC-CONTEXT_SIZE]
              mov_s  %r1,\&NUM
              bra    _int_kernel_isr
.else
              sub    %sp, %sp, CONTEXT_SIZE
              st     %r1,[%sp,R1_LOC]
              mov_s  %r1,\&NUM
              bra    _int_kernel_isr
.endif
        .cfa_ef
        .type FNAME, @function
        .size FNAME, . - FNAME
     .endm

        HANDLE_INT _int_kernel_vector_1, 1
        HANDLE_INT _int_kernel_vector_2, 2
        HANDLE_INT _int_kernel_vector_3, 3
        HANDLE_INT _int_kernel_vector_4, 4
        HANDLE_INT _int_kernel_vector_5, 5
        HANDLE_INT _int_kernel_vector_6, 6
        HANDLE_INT _int_kernel_vector_7, 7
        HANDLE_INT _int_kernel_vector_8, 8
        HANDLE_INT _int_kernel_vector_9, 9
        HANDLE_INT _int_kernel_vector_10, 10
        HANDLE_INT _int_kernel_vector_11, 11
        HANDLE_INT _int_kernel_vector_12, 12
        HANDLE_INT _int_kernel_vector_13, 13
        HANDLE_INT _int_kernel_vector_14, 14
        HANDLE_INT _int_kernel_vector_15, 15

     .if PSP_EXTENDED_INTS_EXIST
        HANDLE_INT _int_kernel_vector_16, 16
        HANDLE_INT _int_kernel_vector_17, 17
        HANDLE_INT _int_kernel_vector_18, 18
        HANDLE_INT _int_kernel_vector_19, 19
        HANDLE_INT _int_kernel_vector_20, 20
        HANDLE_INT _int_kernel_vector_21, 21
        HANDLE_INT _int_kernel_vector_22, 22
        HANDLE_INT _int_kernel_vector_23, 23
        HANDLE_INT _int_kernel_vector_24, 24
        HANDLE_INT _int_kernel_vector_25, 25
        HANDLE_INT _int_kernel_vector_26, 26
        HANDLE_INT _int_kernel_vector_27, 27
        HANDLE_INT _int_kernel_vector_28, 28
        HANDLE_INT _int_kernel_vector_29, 29
        HANDLE_INT _int_kernel_vector_30, 30
        HANDLE_INT _int_kernel_vector_31, 31
     .if PSP_ARC700_INTS_EXIST
; Start CR 2312
;		.align  0x100
; End CR 2312
        HANDLE_INT _int_kernel_vector_32, 32
        HANDLE_INT _int_kernel_vector_33, 33
        HANDLE_INT _int_kernel_vector_34, 34
        HANDLE_INT _int_kernel_vector_35, 35
        HANDLE_INT _int_kernel_vector_36, 36
        HANDLE_INT _int_kernel_vector_37, 37
        HANDLE_INT _int_kernel_vector_38, 38
	 .endif
	 .endif


        .align  MQX_CODE_ALIGNMENT

_int_kernel_isr:
        SAVE_ISR_REGISTERS                   ; save the registers

        ; clear the IRQ hint register in case interrupt generate via this aux.
        ; register
        lr      %r0,[0x201]
        cmp_s   %r0,%r1
        bne_s   _skip_clear
        sr      0,[0x201]
_skip_clear:
        ; save other registers if exception_isr is installed
        ld_s    %r0,[%r2,KD_FLAGS]
        ldw_s   %r1,[%r2,KD_IN_ISR]
        btst_s  %r0,0
        mov     %r5,_isr_no_save_extra
        bne     _isr_save_extra_registers
_isr_no_save_extra:
        tst_s   %r1,%r1
        ld_s    %r3,[%r2,KD_ACTIVE_PTR]      ; Get TD pointer
        bne.d   _isr_no_stack_swap
        add_s   %r1,%r1,1

        st      %sp,[%r3,TD_STACK_PTR]       ; and save the stack pointer
        ld      %sp,[%r2,KD_INTERRUPT_STACK_PTR]

_isr_no_stack_swap:
        stw_s   %r1,[%r2,KD_IN_ISR]          ; Indicate that ISR running

        ld      %r3,[%r2,KD_INTERRUPT_CONTEXT_PTR]
        sub_s   %sp,%sp,IC_STRUCT_SIZE       ; create interrupt context

        ; Initialize the interrupt "context"
        st      0,[%sp,IC_ERROR_CODE]        ; Clear the error code
        stw     %r12,[%sp,IC_ENABLE_SR]      ; save SR as ENABLE sr
        stw     %r4,[%sp,IC_EXCEPTION_NUMBER] ; save interrupt # in context
        st_s    %r3,[%sp,IC_PREV_CONTEXT]    ; set isr cntxt

        KLOG    _klog_isr_start_internal, no_klog_isr_start

        add     %r3,%r2,KD_INTERRUPT_CONTEXT_PTR
        ld      %r0,[%r3,KD_LAST_USER_ISR_VECTOR-KD_INTERRUPT_CONTEXT_PTR]
        st      %sp,[%r3,KD_INTERRUPT_CONTEXT_PTR-KD_INTERRUPT_CONTEXT_PTR]

        flag    %r12                         ; Lower to running sr level

   ;       Find the 'C' isr to run:
        tst_s   %r0,%r0
        ld      %r1,[%r3,KD_FIRST_USER_ISR_VECTOR-KD_INTERRUPT_CONTEXT_PTR]
        mov_s   %r2,%r4
        beq.d   _isr_run_default             ; int table not installed
        cmp_s   %r0,%r2
        blt.d   _isr_run_default             ; r4 too big
        sub.f   %r2,%r2,%r1
        blt.d   _isr_run_default             ; r4 too small

;       we have the interrupt # relative to start of interrupt table
;       Each table entry is 12 bytes in size, so to get to the correct
        ld      %r1,[%r3,KD_INTERRUPT_TABLE_PTR-KD_INTERRUPT_CONTEXT_PTR]
        add1_s  %r2,%r2,%r2                  ; add1 does r2 + r2 << 1
                                             ; so r2 now 3*r2
        add2    %r2,%r1,%r2                  ; r2 now points to ISR info
                                             ; add2 does r1 + r2 << 2
                                             ; ie we now have r1 + 12*r2
        ld_s    %r1,[%r2,0]                  ; get ISR handler
        ld_s    %r0,[%r2,8]                  ; get ISR data

_isr_execute:
        jl_s.d  [%r1]                        ; transfer to 'C' isr
        sub_s   %sp,%sp,16

_int_kernel_isr_returns:
        add_s   %sp,%sp,16

;; The idea is to allow another interrupt as soon as
;; possible.  The current interrupt must be cleared before enabling
;; another one.
;;
;; 
;; NOTE: The MQX_GUERRILLA_INTERRUPTS_EXIST only works with complete
;; knowlege of the interrupt characteristics of you environment.
;; Otherwise an interrupt stack overflow could occur if too many
;; interrupts are nested.

.if MQX_GUERRILLA_INTERRUPTS_EXIST
        ; When MQX_GUERRILLA_INTERRUPTS_EXIST is enabled the following will
        ; create a small window for a nested interrupt to preempt this one.
        mov_s   %r0,6
        flag    %r0                          ; enable L1 & L2 interrupts
.endif

_int_kernel_isr_return_internal:

        GET_KERNEL_DATA                      ; get kernel data addres
        ldw_s   %r0,[%r2,KD_DISABLE_SR]      ; set level to kernel disable
        flag    %r0

.if MQX_KERNEL_LOGGING
        ldw     %r4,[%sp,IC_EXCEPTION_NUMBER]
        KLOG    _klog_isr_end_internal, no_klog_isr_end
.endif

        add     %r3,%r2,KD_INTERRUPT_CONTEXT_PTR
        ld_s    %r1,[%sp,IC_PREV_CONTEXT]
        ldw_s   %r0,[%r2,KD_IN_ISR]
        st_s    %r1,[%r3,KD_INTERRUPT_CONTEXT_PTR-KD_INTERRUPT_CONTEXT_PTR]
        ld_s    %r3,[%r2,KD_ACTIVE_PTR]      ; Get TD pointer

        sub.f   %r0,%r0,1
        add_s   %sp,%sp,IC_STRUCT_SIZE       ; remove the interrupt context
        stw_s   %r0,[%r2,KD_IN_ISR]          ; out of 1 ISR
        bne.d   _isr_nested_interrupt
        ld_s    %r0,[%r2,KD_FLAGS]

;       Completed all nested interrupts
        ld_s    %r1,[%r2,KD_CURRENT_READY_Q]
        ld      %r4,[%r3,TD_FLAGS]
        ld      %sp,[%r3,TD_STACK_PTR]       ; Return to old stack

;       Check to see if reschedule necessary
        and.f   0,%r4,PREEMPTION_DISABLED
        ld_s    %r1,[%r1]
        bne.d   _isr_nested_interrupt        ; task has preemption disabled

;        If a different TD at head of current readyq, then we need to run
;        the scheduler
        sub.f   0,%r3,%r1
        bne.d   _isr_context_switch          ; diffent td at head of readyq
_isr_nested_interrupt:
        ; save other registers if exception_isr is installed
        btst_s   %r0,0
        bne_s    _isr_restore_extra

_isr_completion:
        RESTORE_ISR_REGISTERS
        add      %sp,%sp,CONTEXT_SIZE
        j.f      [%ilink2]

        .align  MQX_CODE_ALIGNMENT
_isr_restore_extra:                          ; restore extra registers
        RESTORE_REST_ISR_REGISTERS
        bra_s    _isr_completion

        .align  MQX_CODE_ALIGNMENT
_isr_run_default:
        ldw     %r0,[%sp,IC_EXCEPTION_NUMBER] ; get interrupt # in context
        bra.d   _isr_execute
        ld      %r1,[%r3,KD_DEFAULT_ISR-KD_INTERRUPT_CONTEXT_PTR]

        .align  MQX_CODE_ALIGNMENT
_isr_context_switch:
        ; indicate all registers saved.
        or      %r4,%r4,SCRATCH_REGISTERS_SAVED
        st      %r4,[%r3,TD_FLAGS]
        ; No need to save other registers if exception_isr is installed
        bne.d   find_nonempty_queue
        ld_s    %r1,[%r2,KD_CURRENT_READY_Q]
        mov     %r5, find_nonempty_queue ; Will return to find_nonempty_queue
        bra     _isr_save_extra_registers

   .type _int_kernel_isr, @function
   .size _int_kernel_isr, . - _int_kernel_isr

   .end

; EOF
