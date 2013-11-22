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
*** File: psp_ticm.c
***
*** Comments:      
***   This file contains the function to compare two tick structures
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"


/*FUNCTION*-------------------------------------------------------------------
 * 
 * Function Name    : _psp_cmp_ticks
 * Returned Value   : none
 * Comments         : Compares two tick structures together.
 * Returns  1 if a >  b
 * Returns -1 if a <  b
 * Returns  0 if a == b
 *
 *END*----------------------------------------------------------------------*/

_mqx_int _psp_cmp_ticks
   (
       /* [IN] The two structures to compare - both must be normalized */
       PSP_TICK_STRUCT_PTR a_ptr,
       PSP_TICK_STRUCT_PTR b_ptr

   )
{ /* Body */

   return
   ( ((PSP_TICK_STRUCT_PTR)(a_ptr)->TICKS[1] > (PSP_TICK_STRUCT_PTR)(b_ptr)->TICKS[1]) ?  1 : \
     ((PSP_TICK_STRUCT_PTR)(a_ptr)->TICKS[1] < (PSP_TICK_STRUCT_PTR)(b_ptr)->TICKS[1]) ? -1 : \
     ((PSP_TICK_STRUCT_PTR)(a_ptr)->TICKS[0] > (PSP_TICK_STRUCT_PTR)(b_ptr)->TICKS[0]) ?  1 : \
     ((PSP_TICK_STRUCT_PTR)(a_ptr)->TICKS[0] < (PSP_TICK_STRUCT_PTR)(b_ptr)->TICKS[0]) ? -1 : \
     0 );

} /* Endbody */

/* EOF */
