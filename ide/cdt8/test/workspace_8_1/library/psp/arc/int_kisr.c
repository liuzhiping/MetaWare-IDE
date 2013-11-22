/*HEADER******************************************************************
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
*** File: int_kisr.c
***
*** Comments:      
***   This file contains the function for installing a kernel level isr.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _int_install_kernel_isr
* Returned Value   : pointer
* Comments         :
*    This function installs a kernel level isr handler.
*
*END*----------------------------------------------------------------------*/

void (_CODE_PTR_ _int_install_kernel_isr
   (  
      /* [IN] the vector where the ISR is to be installed */
      uint_32  vector,

      /* [IN] the function to install into the vector table */
      void     (_CODE_PTR_ isr_ptr)(void)

   ))(void)
{ /* Body */
   _KLOGM(KERNEL_DATA_STRUCT_PTR kernel_data;)
   void                   (_CODE_PTR_ old_isr_ptr)(void);

/* Start CR 2365 */
#if MQX_KERNEL_LOGGING || MQX_CHECK_ERRORS
   uint_32                result_code;
#endif
/* End CR 2365 */

   uint_16_ptr            vbr_ptr;
   uint_32                isr;
   uint_32                old_isr;

   _KLOGM(_GET_KERNEL_DATA(kernel_data);)
   _KLOGE3(KLOG_int_install_kernel_isr, vector, isr_ptr);

#if MQX_CHECK_ERRORS
   result_code = MQX_OK;
   old_isr_ptr = NULL;

   if ( vector >= PSP_MAXIMUM_INTERRUPT_VECTORS ) {
      result_code = MQX_INVALID_VECTORED_INTERRUPT;
   } else {
#endif
      vbr_ptr = (uint_16_ptr)_int_get_vector_table();
      vector  = (vector * 4) + 2;
      old_isr = vbr_ptr[vector];
      old_isr = (old_isr << 16) | vbr_ptr[vector + 1];
      isr = (uint_32)isr_ptr;
      vbr_ptr[vector]     = isr >> 16;
      vbr_ptr[vector + 1] = (uint_16)isr;
      old_isr_ptr = (void (_CODE_PTR_)(void))old_isr;

      _DCACHE_FLUSH_MLINES(vbr_ptr + vector, 32);
      _ICACHE_INVALIDATE();

#if MQX_CHECK_ERRORS
   } /* Endif */

   /* Set result code and return result. */
   _task_set_error(result_code);
#endif

   _KLOGX3(KLOG_int_install_kernel_isr, old_isr_ptr, result_code);

   return (old_isr_ptr);

} /* Endbody */

/* EOF */
