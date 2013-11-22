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
;** File: vectors.met
;**
;** Comments:
;**   This assembler file contains a startup interrupt vector table.
;**   A new vector table will be created in RAM during MQX BSP
;**   initialization.  Each entry in this temporary vector table
;**   will loop forever to help find the cause of the problem during
;**   startup.  You should be able to halt in the debugger and examine
;**   the register state should an error occur.
;**
;*************************************************************************
;END**********************************************************************


        ; Start CR 2347
        .include psp_cnfg.met
        ; End CR 2347

        .file   "vectors.s"
        .text
        .section .vectors, text

.macro INT_VECTOR, FNAME, TARG
        .align  8
        .global FNAME
FNAME:
        .cfa_bf FNAME
        j       TARG
        .cfa_ef
        .type FNAME, @function
        .size FNAME, . - FNAME
.endm

; Start CR 2396
.if MQX_USE_PMU
        INT_VECTOR _int_vector_0, _restart
.else
        INT_VECTOR _int_vector_0, _start
.endif
; End CR 2396 
        INT_VECTOR _int_vector_1, _int_vector_1
        INT_VECTOR _int_vector_2, _int_vector_2
        INT_VECTOR _int_vector_3, _int_vector_3
        INT_VECTOR _int_vector_4, _int_vector_4
        INT_VECTOR _int_vector_5, _int_vector_5
        INT_VECTOR _int_vector_6, _int_vector_6
        INT_VECTOR _int_vector_7, _int_vector_7
        INT_VECTOR _int_vector_8, _int_vector_8
        INT_VECTOR _int_vector_9, _int_vector_9
        INT_VECTOR _int_vector_10, _int_vector_10
        INT_VECTOR _int_vector_11, _int_vector_11
        INT_VECTOR _int_vector_12, _int_vector_12
        INT_VECTOR _int_vector_13, _int_vector_13
        INT_VECTOR _int_vector_14, _int_vector_14
        INT_VECTOR _int_vector_15, _int_vector_15

; Start CR 2347
; .ifdef PSP_EXTENDED_INTS_EXIST
.if PSP_EXTENDED_INTS_EXIST
; End CR 2347
        INT_VECTOR _int_vector_16, _int_vector_16
        INT_VECTOR _int_vector_17, _int_vector_17
        INT_VECTOR _int_vector_18, _int_vector_18
        INT_VECTOR _int_vector_19, _int_vector_19
        INT_VECTOR _int_vector_20, _int_vector_20
        INT_VECTOR _int_vector_21, _int_vector_21
        INT_VECTOR _int_vector_22, _int_vector_22
        INT_VECTOR _int_vector_23, _int_vector_23
        INT_VECTOR _int_vector_24, _int_vector_24
        INT_VECTOR _int_vector_25, _int_vector_25
        INT_VECTOR _int_vector_26, _int_vector_26
        INT_VECTOR _int_vector_27, _int_vector_27
        INT_VECTOR _int_vector_28, _int_vector_28
        INT_VECTOR _int_vector_29, _int_vector_29
        INT_VECTOR _int_vector_30, _int_vector_30
        INT_VECTOR _int_vector_31, _int_vector_31

; Start CR 2347
; .ifdef PSP_ARC700_INTS_EXIST
.if PSP_ARC700_INTS_EXIST 
; End CR 2347

; Start CR 2312
;        .align  0x100
; End CR 2312
        INT_VECTOR _int_vector_32, _int_vector_32
        INT_VECTOR _int_vector_33, _int_vector_33
        INT_VECTOR _int_vector_34, _int_vector_34
        INT_VECTOR _int_vector_35, _int_vector_35
        INT_VECTOR _int_vector_36, _int_vector_36
        INT_VECTOR _int_vector_37, _int_vector_37
        INT_VECTOR _int_vector_38, _int_vector_38
.endif
.endif

.end

; EOF
