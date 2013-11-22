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
*** File: lwe_test.c
***
*** Comments:      
***   This file contains the function for testing the light weight events.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "lwevent.h"
#include "lwe_prv.h"

#if MQX_USE_LWEVENTS
/*FUNCTION*------------------------------------------------------------
* 
* Function Name    : _lwevent_test
* Returned Value   :  _mqx_uint MQX_OK or a MQX error code
* Comments         :
*    This function tests the event component for validity and consistency.
* 
*END*------------------------------------------------------------------*/

_mqx_uint _lwevent_test
   (
      /* [OUT] the event in error */
      pointer _PTR_ event_error_ptr,

      /* [OUT] the td on a light weight event in error */
      pointer _PTR_ td_error_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR   kernel_data;
   LWEVENT_STRUCT_PTR       event_ptr;
   _mqx_uint                result;
   _mqx_uint                queue_size;

   _GET_KERNEL_DATA(kernel_data);                                         

   _KLOGE2(KLOG_lwevent_test, event_error_ptr);

   *td_error_ptr    = NULL;
   *event_error_ptr = NULL;

#if MQX_CHECK_ERRORS
   if (kernel_data->IN_ISR) {
      _KLOGX2(KLOG_lwevent_test, MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
      return(MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
   }/* Endif */
#endif

   /* 
   ** It is not considered an error if the lwevent component has not been
   ** created yet
   */
   if (kernel_data->LWEVENTS.NEXT == NULL) {
      return(MQX_OK);
   } /* Endif */


   result = _queue_test((QUEUE_STRUCT_PTR)&kernel_data->LWEVENTS, 
      event_error_ptr);
   if (result != MQX_OK) {
      _KLOGX3(KLOG_lwevent_test, result, *event_error_ptr);
      return(result);
   } /* Endif */

   /* Start CR 2332 */
   _int_disable(); 
   /* End CR 2332 */
   
   event_ptr = (LWEVENT_STRUCT_PTR)((pointer)kernel_data->LWEVENTS.NEXT);
   queue_size = _QUEUE_GET_SIZE(&kernel_data->LWEVENTS);
   while (queue_size--) {
      if (event_ptr->VALID != LWEVENT_VALID) {
         result = MQX_LWEVENT_INVALID;
         break;
      } /* Endif */
      result = _queue_test(&event_ptr->WAITING_TASKS, td_error_ptr);
      if (result != MQX_OK) {
         break;
      } /* Endif */
      event_ptr = (LWEVENT_STRUCT_PTR)(pointer)event_ptr->LINK.NEXT;
   } /* Endwhile */

   _int_enable();

   if (result != MQX_OK) {
      *event_error_ptr = (pointer)event_ptr;
   } /* Endif */
   _KLOGX4(KLOG_lwevent_test, result, *event_error_ptr, *td_error_ptr);
   return(result);

} /* Endbody */
#endif /* MQX_USE_LWEVENTS */

/* EOF */
