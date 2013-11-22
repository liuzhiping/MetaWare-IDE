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
*** File: kl_cntrl.c
***
*** Comments:      
***   This file contains the function for controlling Kernel Logging.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "klog.h"


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _klog_control
* Returned Value   : none
* Comments         :
*   This function modifies the log control for the kernel log
*
*END*----------------------------------------------------------------------*/

void _klog_control
   (
      /* [IN] what bits to modify */
      uint_32 bit_mask,

      /* [IN] TRUE if set the bits, else clear the bits */
      boolean set_bits
   )
{ /* Body */

/* Start CR 2404 */
#if MQX_KERNEL_LOGGING
/* End CR 2404 */

   KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);
   _int_disable();
   if (set_bits) {
      kernel_data->LOG_CONTROL |= bit_mask;
   } else {
      kernel_data->LOG_CONTROL &= ~bit_mask;
   } /* Endif */
   _int_enable();

/* Start CR 2404 */
#endif /* MQX_KERNEL_LOGGING */
/* End CR 2404 */

} /* Endbody */


/* EOF */
