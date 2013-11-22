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
*** File: ev_creai.c
***
*** Comments:      
***   This file contains the functions for creating an event.
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

#if MQX_USE_EVENTS
/*FUNCTION*------------------------------------------------------------
* 
* Function Name    : _event_create_internal
* Returned Value   : 
*   Returns MQX_OK upon success, a Task Error code or an error code:
* Comments         :
*    Used internally to create an instance of an event.
*
* 
*END*------------------------------------------------------------------*/

_mqx_uint _event_create_internal
   (
      /* [IN] the string name for the event */
      char _PTR_              name_ptr,

      /* [OUT] where the event pointer is */
      EVENT_STRUCT_PTR _PTR_  event_ptr_ptr
   )
{ /* Body */
            KERNEL_DATA_STRUCT_PTR     kernel_data;
   register EVENT_COMPONENT_STRUCT_PTR event_component_ptr;
   register EVENT_STRUCT_PTR           event_ptr;
            _mqx_uint                   result;
   
   _GET_KERNEL_DATA(kernel_data);                                         

   _KLOGE2(KLOG_event_create, name_ptr);

#if MQX_CHECK_ERRORS
   if (kernel_data->IN_ISR) {
      _KLOGX2(KLOG_event_create, MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
      return(MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
   } /* Endif */
#endif

   event_component_ptr = (EVENT_COMPONENT_STRUCT_PTR)
      kernel_data->KERNEL_COMPONENTS[KERNEL_EVENTS];
   if (event_component_ptr == NULL) {
      result = _event_create_component(EVENT_DEFAULT_INITIAL_NUMBER,
         EVENT_DEFAULT_GROW_NUMBER, EVENT_DEFAULT_MAXIMUM_NUMBER);
      event_component_ptr = (EVENT_COMPONENT_STRUCT_PTR)
         kernel_data->KERNEL_COMPONENTS[KERNEL_EVENTS];
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
      if (event_component_ptr == NULL){
         _KLOGX2(KLOG_event_create, result);
        return(result);
      } /* Endif */
#endif
   } /* Endif */

#if MQX_CHECK_VALIDITY
   if (event_component_ptr->VALID != EVENT_VALID){
      _KLOGX2(KLOG_event_create, MQX_INVALID_COMPONENT_BASE);
      return(MQX_INVALID_COMPONENT_BASE);
   } /* Endif */
#endif

   event_ptr = (EVENT_STRUCT_PTR)_mem_alloc_system_zero(
      (_mem_size)sizeof(EVENT_STRUCT));
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if (event_ptr == NULL) {
      _KLOGX2(KLOG_event_create, MQX_OUT_OF_MEMORY);
      return(MQX_OUT_OF_MEMORY);
   } /* Endif */
#endif

   _QUEUE_INIT(&event_ptr->WAITING_TASKS, EVENT_MAX_WAITING_TASKS);

   strncpy(event_ptr->NAME, name_ptr, (_mqx_uint)NAME_MAX_NAME_SIZE-1);
   event_ptr->NAME[NAME_MAX_NAME_SIZE-1] = '\0';
   result = _name_add_internal(event_component_ptr->NAME_TABLE_HANDLE, 
      event_ptr->NAME, (_mqx_uint)event_ptr);
#if MQX_CHECK_ERRORS
   if (result != MQX_OK) {
      _mem_free(event_ptr);
      if (result == NAME_EXISTS) {
         _KLOGX2(KLOG_event_create, EVENT_EXISTS);
         return(EVENT_EXISTS);
      } else if (result == NAME_TABLE_FULL) {
         _KLOGX2(KLOG_event_create, EVENT_TABLE_FULL);
         return(EVENT_TABLE_FULL);
      } /* Endif */
      _KLOGX2(KLOG_event_create, result);
      return(result);
   } /* Endif */
#endif
   event_ptr->VALID = EVENT_VALID;

   *event_ptr_ptr = event_ptr;

   _KLOGX2(KLOG_event_create, result);
   return(result);

} /* Endbody */
#endif /* MQX_USE_EVENTS */

/* EOF */
