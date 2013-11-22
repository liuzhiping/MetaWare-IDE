/*HEADER***************************************************************
***********************************************************************
***
*** Copyright (c) 1989-2005 ARC International.
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
***
*** File: ti_test.c
***
*** Comments:
***   This file contains the function that tests the timer component.
***
***
************************************************************************
*END*******************************************************************/

#include "mqx_inc.h"
#include "timer.h"
#include "tim_prv.h"

#if MQX_USE_TIMER
/*FUNCTION*-----------------------------------------------------------
*
* Function Name   : _timer_test
* Return Value    : _mqx_uint - MQX_OK or error code
* Comments        : This function tests the timer component for validity
*   and consistency.
*
*END*------------------------------------------------------------------*/

_mqx_uint _timer_test
   (
      /* [OUT] the timer element in error */
      pointer _PTR_ timer_error_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR     kernel_data;
   TIMER_COMPONENT_STRUCT_PTR timer_component_ptr;
   QUEUE_STRUCT_PTR           queue_ptr;
   TIMER_ENTRY_STRUCT_PTR     element_ptr;
   _mqx_uint                  result;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE2(KLOG_timer_test, timer_error_ptr);

   *timer_error_ptr = NULL;
  
   timer_component_ptr = kernel_data->KERNEL_COMPONENTS[KERNEL_TIMER];
   if (timer_component_ptr == NULL) {
      _KLOGX2(KLOG_timer_test, MQX_OK);
      return(MQX_OK);
   } /* Endif */

   /* Gain exclusive access to the timer queues */
   _lwsem_wait(&timer_component_ptr->TIMER_ENTRIES_LWSEM);

   result = _queue_test(&timer_component_ptr->ELAPSED_TIMER_ENTRIES,
      timer_error_ptr);
   if (result != MQX_OK) {
      _lwsem_post(&timer_component_ptr->TIMER_ENTRIES_LWSEM);
      _KLOGX3(KLOG_timer_test, result, *timer_error_ptr);
      return(result);
   } /* Endif */
   
   result = _queue_test(&timer_component_ptr->KERNEL_TIMER_ENTRIES,
      timer_error_ptr);
   if (result != MQX_OK) {
      _lwsem_post(&timer_component_ptr->TIMER_ENTRIES_LWSEM);
      _KLOGX3(KLOG_timer_test, result, *timer_error_ptr);
      return(result);
   } /* Endif */

   queue_ptr   = (pointer)&timer_component_ptr->ELAPSED_TIMER_ENTRIES;
   element_ptr = (pointer)queue_ptr->NEXT;
   while (element_ptr != (pointer)queue_ptr) {
#if MQX_CHECK_VALIDITY
      if (element_ptr->VALID != TIMER_VALID) {
         *timer_error_ptr = element_ptr;
         _lwsem_post(&timer_component_ptr->TIMER_ENTRIES_LWSEM);
         _KLOGX3(KLOG_timer_test, MQX_INVALID_COMPONENT_HANDLE,
            *timer_error_ptr);
         return(MQX_INVALID_COMPONENT_HANDLE);
      } /* Endif */
#endif
      element_ptr = (pointer)element_ptr->QUEUE_ELEMENT.NEXT;
   } /* Endwhile */

   queue_ptr   = (pointer)&timer_component_ptr->KERNEL_TIMER_ENTRIES;
   element_ptr = (pointer)queue_ptr->NEXT;
   while (element_ptr != (pointer)queue_ptr) {
#if MQX_CHECK_VALIDITY
      if (element_ptr->VALID != TIMER_VALID) {
         *timer_error_ptr = element_ptr;
         _lwsem_post(&timer_component_ptr->TIMER_ENTRIES_LWSEM);
         _KLOGX3(KLOG_timer_test, MQX_INVALID_COMPONENT_HANDLE,
            *timer_error_ptr);
         return(MQX_INVALID_COMPONENT_HANDLE);
      } /* Endif */
#endif
      element_ptr = (pointer)element_ptr->QUEUE_ELEMENT.NEXT;
   } /* Endwhile */

   _lwsem_post(&timer_component_ptr->TIMER_ENTRIES_LWSEM);
   _KLOGX2(KLOG_timer_test, MQX_OK);
   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_TIMER */

/* EOF */