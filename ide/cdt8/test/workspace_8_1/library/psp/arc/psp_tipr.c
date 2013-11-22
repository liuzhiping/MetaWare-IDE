/*HEADER*******************************************************************
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
*** File: psp_tipr.c
***
*** Comments:      
***   This file contains the function for printing the ticks portion
***   of the PSP_TICK_STRUCT
***
**************************************************************************
*END**********************************************************************/

#include "mqx_inc.h"
#include "fio.h"


/*FUNCTION*-----------------------------------------------------------------
* 
* Function Name    : _psp_print_ticks
* Returned Value   : none
* Comments         :
*    Prints ticks in hex notation
*
*END*----------------------------------------------------------------------*/

void _psp_print_ticks
   (
      PSP_TICK_STRUCT_PTR tick_ptr
   )
{ /* Body */
   PSP_64_BIT_UNION  tmp;
   int_32            i;

   printf("0x");
   tmp.LLW = tick_ptr->TICKS[0];
#if PSP_ENDIAN == MQX_LITTLE_ENDIAN
   for (i = 7; i >= 0; i--) {
      printf("%X", tmp.B[i]);
   } /* Endfor */
#else
   for (i = 0; i <= 7; i++) {
      printf("%X", tmp.B[i]);
   } /* Endfor */
#endif
   printf(":%04lX", tick_ptr->HW_TICKS[0]);
   
} /* Endbody */

/* EOF */
