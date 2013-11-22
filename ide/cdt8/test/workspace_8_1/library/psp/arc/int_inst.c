/*HEADER******************************************************************
**************************************************************************
***
*** Copyright (c) 1989-2004 ARC International.
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
*** File: int_inst.c
***
*** Comments:
***   This file contains the functions for initializaing the handling of
***   interrupts.
***
*** $Header:int_inst.c, 8, 1/31/2006 10:50:40 AM, Behdad Besharat$
***
*** $NoKeywords$
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"


/* Start CR 2396 */
#if MQX_USE_PMU
extern void _restart(void);
#else
extern void _start(void);
#endif
/* End CR 2396 */
extern void _int_kernel_vector_1(void);
extern void _int_kernel_vector_2(void);
extern void _int_kernel_vector_3(void);
extern void _int_kernel_vector_4(void);
extern void _int_kernel_vector_5(void);
extern void _int_kernel_vector_6(void);
extern void _int_kernel_vector_7(void);
extern void _int_kernel_vector_8(void);
extern void _int_kernel_vector_9(void);
extern void _int_kernel_vector_10(void);
extern void _int_kernel_vector_11(void);
extern void _int_kernel_vector_12(void);
extern void _int_kernel_vector_13(void);
extern void _int_kernel_vector_14(void);
extern void _int_kernel_vector_15(void);
#if PSP_EXTENDED_INTS_EXIST
extern void _int_kernel_vector_16(void);
extern void _int_kernel_vector_17(void);
extern void _int_kernel_vector_18(void);
extern void _int_kernel_vector_19(void);
extern void _int_kernel_vector_20(void);
extern void _int_kernel_vector_21(void);
extern void _int_kernel_vector_22(void);
extern void _int_kernel_vector_23(void);
extern void _int_kernel_vector_24(void);
extern void _int_kernel_vector_25(void);
extern void _int_kernel_vector_26(void);
extern void _int_kernel_vector_27(void);
extern void _int_kernel_vector_28(void);
extern void _int_kernel_vector_29(void);
extern void _int_kernel_vector_30(void);
extern void _int_kernel_vector_31(void);
#if PSP_ARC700_INTS_EXIST
extern void _int_kernel_vector_32(void);
extern void _int_kernel_vector_33(void);
extern void _int_kernel_vector_34(void);
extern void _int_kernel_vector_35(void);
extern void _int_kernel_vector_36(void);
extern void _int_kernel_vector_37(void);
extern void _int_kernel_vector_38(void);
#endif
#endif

static void (_CODE_PTR_ int_handlers[])(void) = {
/* Start CR 2396 */
#if MQX_USE_PMU
   _restart,
#else
   _start,
#endif
/* End CR 2396 */
   _int_kernel_vector_1,
   _int_kernel_vector_2,
   _int_kernel_vector_3,
   _int_kernel_vector_4,
   _int_kernel_vector_5,
   _int_kernel_vector_6,
   _int_kernel_vector_7,
   _int_kernel_vector_8,
   _int_kernel_vector_9,
   _int_kernel_vector_10,
   _int_kernel_vector_11,
   _int_kernel_vector_12,
   _int_kernel_vector_13,
   _int_kernel_vector_14,
   _int_kernel_vector_15,
#if PSP_EXTENDED_INTS_EXIST
   _int_kernel_vector_16,
   _int_kernel_vector_17,
   _int_kernel_vector_18,
   _int_kernel_vector_19,
   _int_kernel_vector_20,
   _int_kernel_vector_21,
   _int_kernel_vector_22,
   _int_kernel_vector_23,
   _int_kernel_vector_24,
   _int_kernel_vector_25,
   _int_kernel_vector_26,
   _int_kernel_vector_27,
   _int_kernel_vector_28,
   _int_kernel_vector_29,
   _int_kernel_vector_30,
   _int_kernel_vector_31,
#if PSP_ARC700_INTS_EXIST
   _int_kernel_vector_32,
   _int_kernel_vector_33,
   _int_kernel_vector_34,
   _int_kernel_vector_35,
   _int_kernel_vector_36,
   _int_kernel_vector_37,
   _int_kernel_vector_38,
#endif
#endif
};

/*FUNCTION*-------------------------------------------------------------------
*
* Function Name    : _psp_int_install
* Returned Value   : void
* Comments         :
*    This function initializes the hardware interrupt table
*
*END*----------------------------------------------------------------------*/

void _psp_int_install
   (
      void
   )
{ /* Body */
   uint_16_ptr base, loc_ptr;
   _mqx_uint   i;

   /* Initialize the hardware interrupt vector table */
   base = (uint_16_ptr)_int_get_vector_table();
   loc_ptr = base;
   for (i = 0; i < PSP_MAXIMUM_INTERRUPT_VECTORS; i++) {
      uint_32 handler = (uint_32) int_handlers[i];
      *loc_ptr++ = 0x2020; /* Hi word of JL */
      *loc_ptr++ = 0x0F80; /* Lo word of JL */
      *loc_ptr++ = handler >> 16;
      *loc_ptr++ = (uint_16)handler;
   } /* Endfor */

   /* Flush the interrupt table instructions */
   _DCACHE_FLUSH_MLINES(base, (uchar_ptr)loc_ptr - (uchar_ptr)base);

   _ICACHE_INVALIDATE();

} /* Endbody */

/* EOF */
