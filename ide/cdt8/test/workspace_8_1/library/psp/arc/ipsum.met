;HEADER*******************************************************************
;*************************************************************************
;**
;** Copyright (c) 1989-2003 ARC Internationalorporated
;** All rights reserved
;**
;** This software embodies materials and concepts which are
;** confidential to ARC International and is made
;** available solely pursuant to the terms of a written license
;** agreement with ARC International
;**
;** File: ipsumc.met
;**
;** Comments:  This file contains the implementation for a one's
;**            complement checksum.
;**            The function can handle ANY alignment for the data area.
;**
;*************************************************************************
;END**********************************************************************

         .include psp_cnfg.met
         .include types.met
         .include psp_prv.met

         .text

;; 
.ifdef __Xbarrel_shifter

.macro asl_it, dest_reg,src_reg,bits
   asl_s \&dest_reg,\&src_reg,\&bits
.endm

.macro lsr_it, dest_reg,src_reg,bits
   lsr_s \&dest_reg,\&src_reg,\&bits
.endm

.else

.macro asl_it, dest_reg,src_reg,bits
   asl_s \&dest_reg,\&src_reg
.rep \&bits-1
   asl_s \&dest_reg,\&dest_reg
.endr
.endm

.macro lsr_it, dest_reg,src_reg,bits
   lsr_s \&dest_reg,\&src_reg
.rep \&bits-1
   lsr_s \&dest_reg,\&dest_reg
.endr
.endm

.endif


;FUNCTION*--------------------------------------------------------------
;
; Function Name    : _mem_sum_ip(a, len, src)
; Returned Value   : one's complement checksum
; Comments         :
;       This function calculates a 16 bit checksum over the specified data.
;       This is optimized for Little Endian memory accesses.
;
;       Note:  This function returns 0 iff all summands are 0.
;
;END*-------------------------------------------------------------------

        .global _mem_sum_ip
        .align  MQX_CODE_ALIGNMENT

_mem_sum_ip:
                                ; r0 is the initial sum
                                ; r1 is the number of bytes to checksum
                                ; r2 is the source address

        sub_s   %sp,%sp,4
        st_s    %r12,[%sp]      ; save r12 to use as temporary (optim for A5)
        
        sub.f   0,%r1,36        ; Do we have 36 or less bytes?
        ble.d   do_small
        xor     %r4,%r4,%r4     ; Clear flag

        btst_s  %r2,0           ; Check if address is byte aligned
        beq_s   even_address

        ldb_s   %r3,[%r2]       ; read odd byte
        add_s   %r2,%r2,1
        sub_s   %r1,%r1,1
        mov     %r4,1           ; set byte swap flag
        asl_it  %r3,%r3,8
        bra.d   odd_address
        add_s   %r0,%r0,%r3
        
even_address:  
        and     %r12,%r0,0xff    ; Swap bytes in %r0
        asl_it  %r12,%r12,8
        lsr_it  %r0,%r0,8
        or      %r0,%r0,%r12

odd_address:
        btst_s  %r2,1           ; Align address to 4 bytes
        beq_s   done_alignment  ; YES, address is aligned to a long word

        ldw_s   %r3,[%r2]       ; read odd word
        add_s   %r2,%r2,2
        sub_s   %r1,%r1,2
        add_s   %r0,%r0,%r3
        
done_alignment:
        mov     %r12,%r1
        lsr_it  %r12,%r12,5     ; 32 bytes summed per loop
        sub_s   %r2,%r2,4       ; Optimize addressing for Arc
        mov_s   LP_COUNT,%r12
        lp      do_longs_end
do_longs:
        ld.a    %r3,[%r2,4]
        ld.a    %r5,[%r2,4]
        adc.f   %r0,%r0,%r3
        ld.a    %r6,[%r2,4]
        adc.f   %r0,%r0,%r5
        ld.a    %r7,[%r2,4]
        adc.f   %r0,%r0,%r6
        ld.a    %r8,[%r2,4]
        adc.f   %r0,%r0,%r7
        ld.a    %r9,[%r2,4]
        adc.f   %r0,%r0,%r8
        ld.a    %r10,[%r2,4]
        adc.f   %r0,%r0,%r9
        ld.a    %r11,[%r2,4]
        adc.f   %r0,%r0,%r10
        adc.f   %r0,%r0,%r11
do_longs_end:
        add_s   %r2,%r2,4         ; get address back correctly

; fold the sum from 32 bits to 16 to avoid carry bit problems
        mov_s   %r3,%r0           ; Fold 32 bit sum into 16
        lsr_it  %r3,%r3,16
        and     %r0,%r0,0xffff
        adc.f   %r0,%r0,%r3
        and.f   0,%r0,0xffff0000
        beq_s   no_extra
        add_s   %r0,%r0,1
        and     %r0,%r0,0xffff
no_extra:        

; Check for original odd address if odd can continue as is
        and.f   0,%r4,%r4
        bne.d   align_odd_after
        and.f   %r1,%r1,0x1f     ; must process remaining bytes/words

        and     %r12,%r0,0xff     ; swap bytes in %r0
        asl_it  %r12,%r12,8
        lsr_it  %r0,%r0,8
        or      %r0,%r0,%r12
        bra_s   do_small

align_odd_after:                 ; since started odd must unalign
        beq_s   ip_sum_done
        ldb_s   %r3,[%r2]       ; read odd byte
        add_s   %r2,%r2,1
        sub_s   %r1,%r1,1
        add_s   %r0,%r0,%r3

; Checksum a small packet
do_small:
        btst_s  %r1,0           ; odd number left to do
        beq_s   do_small_even

        sub_s   %r1,%r1,1
        ldb_s   %r3,[%r2,%r1]   ; read byte
        asl_it  %r3,%r3,8
        add_s   %r0,%r0,%r3

do_small_even:        
        tst_s   %r1,%r1
        beq_s   ip_sum_done
        
do_small_loop:
        lsr_s   %r1,%r1
        mov_s   LP_COUNT,%r1
        sub_s   %r2,%r2,1
        lp      do_small_loop_end
; loop start        
        ldb.a   %r3,[%r2,1]      ; read byte
        ldb.a   %r12,[%r2,1]     ; read byte
        asl_it  %r3,%r3,8
        or      %r3,%r3,%r12
        add_s   %r0,%r0,%r3
do_small_loop_end:

ip_sum_done:
        mov_s   %r3,%r0           ; Fold 32 bit sum into 16 last time
        lsr_it  %r3,%r3,16
        and     %r0,%r0,0xffff
        add_s   %r0,%r0,%r3
        and.f   0,%r0,0xffff0000
        beq_s   ip_sum_return
        add_s   %r0,%r0,1
        and     %r0,%r0,0xffff
ip_sum_return:        
        ld_s    %r12,[%sp]      ; restore r12
        add_s   %sp,%sp,4
        j_s     [%blink]


   .type _mem_sum_ip, @function
   .size _mem_sum_ip, . - _mem_sum_ip
   .end

; EOF
