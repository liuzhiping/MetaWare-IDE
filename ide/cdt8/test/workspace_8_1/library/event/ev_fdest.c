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
*** File: ev_fdest.c
***
*** Comments:      
***   This file contains the function for destroying an event quickly.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include <string.h>
#include "mqx_inc.h"
#include "name.h"
#include "name_prv.h"
#include "event.h"
#include "evnt_prv.h"
#include "mqx_str.h"

#if MQX_USE_EVENTS
/*FUNCTION*------------------------------------------------------------
* 
* Function Name    : _event_destroy_fast
* Returned Value   : 
*   Returns MQX_OK upon success, a Task Error code or an error code:
* Comments         :
*    Used by a task to destroy an instance of an event using an index
*
* 
*END*------------------------------------------------------------------*/

_mqx_uint _event_destroy_fast
   (
      /* [IN] the index of the event to destroy */
      _mqx_uint event_index
   )
{ /* Body */
            KERNEL_DATA_STRUCT_PTR      kernel_data;
   register EVENT_COMPONENT_STRUCT_PTR  event_component_ptr;
            EVENT_STRUCT_PTR            event_ptr;
            EVENT_CONNECTION_STRUCT_PTR event_connection_ptr;
            QUEUE_STRUCT_PTR            queue_ptr;
            TD_STRUCT_PTR               td_ptr;
            _mqx_uint                    result;
            _mqx_uint                    i;
            _mqx_max_type                   tmp;
#if MQX_CHECK_ERRORS
            char                        name[9];
#endif

   _GET_KERNEL_DATA(kernel_data);                                         

   _KLOGE2(KLOG_event_destroy_fast, event_index);
   
   event_component_ptr = (EVENT_COMPONENT_STRUCT_PTR)
      kernel_data->KERNEL_COMPONENTS[KERNEL_EVENTS];

#if MQX_CHECK_ERRORS
   if (kernel_data->IN_ISR) {
      _KLOGX2(KLOG_event_destroy_fast, MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
      return(MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
   } /* Endif */
   if (event_component_ptr == NULL) {
      _KLOGX2(KLOG_event_destroy_fast, MQX_COMPONENT_DOES_NOT_EXIST);
      return(MQX_COMPONENT_DOES_NOT_EXIST);
   } /* Endif */
#endif
#if MQX_CHECK_VALIDITY
   if (event_component_ptr->VALID != EVENT_VALID) {
      _KLOGX2(KLOG_event_destroy_fast, MQX_INVALID_COMPONENT_BASE);
      return(MQX_INVALID_COMPONENT_BASE);
   } /* Endif */
#endif

   result = _name_find_internal_by_index(event_component_ptr->NAME_TABLE_HANDLE, 
      event_index, &tmp);
   /* Start CR 349 */
   event_ptr = (EVENT_STRUCT_PTR)tmp;
   /* End CR 349 */
#if MQX_CHECK_ERRORS
   if (result != MQX_OK) {
      if (result == NAME_NOT_FOUND) {
         _KLOGX2(KLOG_event_destroy_fast, EVENT_NOT_FOUND);
         return(EVENT_NOT_FOUND);
      } /* Endif */
      _KLOGX2(KLOG_event_destroy_fast, result);
      return(result);
   } /* Endif */

   /* Start CR 349 */
   /* event_ptr = (EVENT_STRUCT_PTR)tmp; */
   /* End CR 349 */
   _str_mqx_uint_to_hex_string(event_index, name);
   if (strncmp(name, event_ptr->NAME, 8)) {
      /* Name does not match */
      _KLOGX2(KLOG_event_destroy_fast, EVENT_NOT_FOUND);
      return(EVENT_NOT_FOUND);
   } /* Endif */
#endif
   
   queue_ptr = &event_ptr->WAITING_TASKS;

   _int_disable();
   if (event_ptr->VALID != EVENT_VALID) {
      _int_enable();
      _KLOGX2(KLOG_event_destroy_fast, EVENT_INVALID_EVENT);
      return(EVENT_INVALID_EVENT); 
   } /* Endif */

   /* Effectively stop all access to the event */
   event_ptr->VALID = 0;

    /* Ready all waiting tasks */
   i = _QUEUE_GET_SIZE(queue_ptr) + 1;
   while (--i) {
      event_connection_ptr = (EVENT_CONNECTION_STRUCT_PTR)((pointer)queue_ptr->NEXT);
      td_ptr = event_connection_ptr->TD_PTR;
      if ((td_ptr->STATE & STATE_MASK)== EVENT_BLOCKED) {
         _TIME_DEQUEUE(td_ptr, kernel_data);
         _task_set_error_td_internal(td_ptr,EVENT_DELETED);
         _TASK_READY(td_ptr,kernel_data);
      } /* Endif */
   } /* Endwhile */

   _int_enable();

   result = _name_delete_internal_by_index(event_component_ptr->NAME_TABLE_HANDLE,
      event_index);
#if MQX_CHECK_ERRORS
   if (result == NAME_NOT_FOUND) {
      result = EVENT_NOT_FOUND;
   } /* Endif */
#endif

   _mem_free(event_ptr);

   /* May need to let higher priority task run */
   _CHECK_RUN_SCHEDULER();

   _KLOGX2(KLOG_event_destroy_fast, result);
   return(result);
   
} /* Endbody */   
#endif /* MQX_USE_EVENTS */

/* EOF */
