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
*** File: int_lvl.c
***
*** Comments:      
***   This file contains the functions for manipulating the user
*** context on the stack.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"

/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _psp_set_int_level
* Returned Value   : none
* Comments         :
*   This function sets the interrupt level for a interrupt
*
*END*----------------------------------------------------------------------*/

void _psp_set_int_level
   (
      /* [IN] the interrupt vector number */
      uint_32 vector_number,

      /* [IN] the level to set the vector to: 1 or 2 */
      uint_32 level  
   )
{ /* Body */
   uint_32 val;

   if (vector_number < 32) {
      val = _psp_get_aux(PSP_AUX_IRQ_LEV) & ~(1 << vector_number);
      if (level == 2) {
         val |= (1 << vector_number);
      } /* Endif */
      _psp_set_aux(PSP_AUX_IRQ_LEV, val);
   } /* Endif */
      
} /* Endbody */

/* EOF */
