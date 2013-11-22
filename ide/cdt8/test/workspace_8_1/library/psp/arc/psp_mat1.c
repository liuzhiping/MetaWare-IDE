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
*** File: psp_mat1.c
***
*** Comments:      
***   This file contains the math functions. 
***
***
**************************************************************************
*END**********************************************************************/

#include "mqx_inc.h"


/*FUNCTION*-----------------------------------------------------------------
* 
* Function Name    : _psp_mul_128_by_32
* Returned Value   : uint_32 - overflow
* Comments         :
*    This function multiplies a 128 bit quantity by a 32 bit quantity and 
* stores the result in a 128 bit quantity
*
*END*----------------------------------------------------------------------*/

uint_32 _psp_mul_128_by_32
   (
      /* 
      ** [IN] Pointer to a 4 long word array in which the value to be multiplied
      ** is stored 
      */
      PSP_128_BIT_UNION_PTR   m_ptr,

      /* [IN] The multiplier */
      uint_32                 mul,

      /* [OUT] Pointer to a 4 long word array where the result will be stored */
      PSP_128_BIT_UNION_PTR   r_ptr

   )
{ /* Body */
   PSP_128_BIT_UNION tmp;
   uint_64           w,r;
   uint_32           w0;
   _mqx_uint         i;

#if PSP_ENDIAN == MQX_LITTLE_ENDIAN
   tmp.LLW[0] = 0;
   r = 0;
   if (!mul || (!m_ptr->LLW[0] && !m_ptr->LLW[1])) {
      tmp.LLW[1] = 0;
   } else if (mul == 1) {
      *r_ptr = *m_ptr;
      return r;
   } else {
      for ( i = 0; i < 3; i++ ) {
         w  = (uint_64)mul * (uint_64)m_ptr->LW[i];
         w0 = (uint_32)w;
         tmp.LW[i] += w0;
         tmp.LW[i+1] = (w >> 32) + (tmp.LW[i] < w0);
      } /* Endfor */

      w = (uint_64)mul * (uint_64)m_ptr->LW[3];
      w0 = (uint_32)w;
      tmp.LW[3] += w0;
      r = (w >> 32) + (tmp.LW[3] < w0);
   } /* Endif */
   
   *r_ptr = tmp;
   return r;
#else
   tmp.LLW[1] = 0;
   r = 0;
   if (!mul || (!m_ptr->LLW[0] && !m_ptr->LLW[1])) {
      tmp.LLW[0] = 0;
   } else if (mul == 1) {
      *r_ptr = *m_ptr;
      return r;
   } else {
      for ( i = 3; i > 0; i-- ) {
         w  = (uint_64)mul * (uint_64)m_ptr->LW[i];
         w0 = (uint_32)w;
         tmp.LW[i] += w0;
         tmp.LW[i-1] = (w >> 32) + (tmp.LW[i] < w0);
      } /* Endfor */

      w = (uint_64)mul * (uint_64)m_ptr->LW[0];
      w0 = (uint_32)w;
      tmp.LW[0] += w0;
      r = (w >> 32) + (tmp.LW[0] < w0);
   } /* Endif */
   
   *r_ptr = tmp;
   return r;
#endif

} /* Endbody */

/* EOF */
