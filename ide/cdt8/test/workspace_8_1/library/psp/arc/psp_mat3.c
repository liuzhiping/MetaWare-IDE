/*HEADER*******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2007 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: psp_math.c
***
*** Comments:      
***   This file contains the math functions. 
***
**************************************************************************
*END**********************************************************************/

#include "mqx_inc.h"


/*FUNCTION*-----------------------------------------------------------------
* 
* Function Name    : _psp_add_element_to_array
* Returned Value   : uint_32 - 1 if overflow, 0 otherwise
* Comments         :
*    This function adds a single element to an array. r = a[] + val
*
*END*----------------------------------------------------------------------*/


/* START CR 2364 */
uint_32 _psp_add_element_to_array
   (
      /* [IN] Pointer to the array to be added to */
      PSP_128_BIT_UNION_PTR s1_ptr,

      /* [IN] The value to add the array */
      uint_32     val,

      /* [IN] The size of the array to add in long words */
      uint_32     size,

      /* [OUT] Pointer to where the result is to be stored */
      PSP_128_BIT_UNION_PTR res_ptr

   )
{ /* Body */
   register uint_32 x, y, z, cy;
   register int_32  j;

#if PSP_ENDIAN == MQX_LITTLE_ENDIAN
   x  = s1_ptr->LW[0];
   y  = x + val;
   cy = (y < val);
   res_ptr->LW[0] = y;
   for ( j = 1; j < size; j++) {
      z = s1_ptr->LW[j];
      x = z + cy;
      res_ptr->LW[j] = x;
      cy = (x < z);
   } /* Endfor */
#else
   x  = s1_ptr->LW[size-1];
   y  = x + val;
   cy = (y < val);
   res_ptr->LW[size-1] = y;
   for ( j = (size-2); j >= 0; j--) { 
      z = s1_ptr->LW[j];
      x = z + cy;
      res_ptr->LW[j] = x;
      cy = (x < z);
   } /* Endfor */
#endif
   return cy;
/* END CR 2364 */

} /* Endbody */

/* EOF */
