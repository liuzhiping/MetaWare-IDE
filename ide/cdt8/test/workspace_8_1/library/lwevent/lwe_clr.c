/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2005 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: lwe_clr.c
***
*** Comments:      
***   This file contains the function for clearing an event.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "lwevent.h"
#include "lwe_prv.h"

#if MQX_USE_LWEVENTS
/*FUNCTION*------------------------------------------------------------
* 
* Function Name    : _lwevent_clear
* Returned Value   : 
*   Returns MQX_OK upon success, or an error code
* Comments         :
*    Used by a task to clear the specified event bits in an event.
*
* 
*END*------------------------------------------------------------------*/

_mqx_uint _lwevent_clear
   (
      /* [IN] - The address of the light weight event */
      LWEVENT_STRUCT_PTR  event_ptr,

      /* [IN] - bit mask, each bit of which represents an event. */
      _mqx_uint           bit_mask
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR      kernel_data;

   _GET_KERNEL_DATA(kernel_data);
   _KLOGE3(KLOG_lwevent_clear, event_ptr, bit_mask);

   _INT_DISABLE();
#if MQX_CHECK_VALIDITY
   if (event_ptr->VALID != LWEVENT_VALID){
      _int_enable();
      _KLOGX2(KLOG_lwevent_clear, MQX_LWEVENT_INVALID);
      return(MQX_LWEVENT_INVALID); 
   } /* Endif */
#endif

   event_ptr->VALUE &= ~bit_mask;
   _INT_ENABLE();

   _KLOGX2(KLOG_lwevent_clear, MQX_OK);
   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_LWEVENTS */

/* EOF */
