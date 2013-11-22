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
;** File: psputilc.met
;**
;** Comments:      
;**   This assembler file contains utility functions
;**                                                               
;*************************************************************************
;END**********************************************************************

        .include psp_cnfg.met
        .include types.met
        .include psp_prv.met

         .text

;FUNCTION*-------------------------------------------------------------------
; 
; Function Name    : _psp_save_fp_context_internal
; Returned Value   : none
; Comments         : 
;   This function saves the floating point context for the
; current floating point task
;
; THIS FUNCTION MUST BE CALLED DISABLED
;
;END*----------------------------------------------------------------------

        .global   _psp_save_fp_context_internal
        .align  4
   
_psp_save_fp_context_internal:
        j_s   [%blink]
 
   .type _psp_save_fp_context_internal, @function
   .size _psp_save_fp_context_internal, . - _psp_save_fp_context_internal


;FUNCTION*-------------------------------------------------------------------
; 
; Function Name    : _mem_test_and_set
; Returned Value   : previous value of location
; Comments         : 
;   This function tests a byte location, and if 0, sets it to 0x80.
;   It returns the previous value of the byte.
;
;END*----------------------------------------------------------------------

        .global   _mem_test_and_set
        .align  4
   
_mem_test_and_set:
   lr      %r3,[STATUS32]
   flag    0
   ldb_s   %r2,[%r0]
   tst_s   %r2,%r2
   bne.d   bit_was_set
   mov     %r1,0x80
   stb_s   %r1,[%r0]
   flag    %r3
   j_s.d   [%blink]
   mov_s   %r0,0

bit_was_set:
   flag    %r3
   j_s.d   [%blink]
   mov_s   %r0,0x80

   .type _mem_test_and_set, @function
   .size _mem_test_and_set, . - _mem_test_and_set


;FUNCTION*-------------------------------------------------------------------
; 
; Function Name    : _psp_get_periph_b
; Returned Value   : byte read bypassing data cache
; Comments         : 
;
;END*----------------------------------------------------------------------

        .global   _psp_get_periph_b
        .align  4
   
_psp_get_periph_b:
   ldb.di  %r0,[%r0]
   j       [%blink]

   .type _psp_get_periph_b, @function
   .size _psp_get_periph_b, . - _psp_get_periph_b


;FUNCTION*-------------------------------------------------------------------
; 
; Function Name    : _psp_set_periph_b
; Returned Value   : byte write bypassing data cache
; Comments         : 
;
;END*----------------------------------------------------------------------

        .global   _psp_set_periph_b
        .align  4
   
_psp_set_periph_b:
   j.d     [%blink]
   stb.di  %r1,[%r0]

   .type _psp_set_periph_b, @function
   .size _psp_set_periph_b, . - _psp_set_periph_b


;FUNCTION*-------------------------------------------------------------------
; 
; Function Name    : _psp_get_periph_w
; Returned Value   : 16bit read bypassing data cache
; Comments         : 
;
;END*----------------------------------------------------------------------

        .global   _psp_get_periph_w
        .align  4
   
_psp_get_periph_w:
   ldw.di  %r0,[%r0]
   j       [%blink]

   .type _psp_get_periph_w, @function
   .size _psp_get_periph_w, . - _psp_get_periph_w


;FUNCTION*-------------------------------------------------------------------
; 
; Function Name    : _psp_set_periph_w
; Returned Value   : 16bit write bypassing data cache
; Comments         : 
;
;END*----------------------------------------------------------------------

        .global   _psp_set_periph_w
        .align  4
   
_psp_set_periph_w:
   j.d     [%blink]
   stw.di  %r1,[%r0]

   .type _psp_set_periph_w, @function
   .size _psp_set_periph_w, . - _psp_set_periph_w


;FUNCTION*-------------------------------------------------------------------
; 
; Function Name    : _psp_get_periph_l
; Returned Value   : 32bit read bypassing data cache
; Comments         : 
;
;END*----------------------------------------------------------------------

        .global   _psp_get_periph_l
        .align  4
   
_psp_get_periph_l:
   ld.di   %r0,[%r0]
   j       [%blink]

   .type _psp_get_periph_l, @function
   .size _psp_get_periph_l, . - _psp_get_periph_l


;FUNCTION*-------------------------------------------------------------------
; 
; Function Name    : _psp_set_periph_l
; Returned Value   : 16bit write bypassing data cache
; Comments         : 
;
;END*----------------------------------------------------------------------

        .global   _psp_set_periph_l
        .align  4
   
_psp_set_periph_l:
   j.d     [%blink]
   st.di   %r1,[%r0]

   .type _psp_set_periph_l, @function
   .size _psp_set_periph_l, . - _psp_set_periph_l

   .end

; EOF
