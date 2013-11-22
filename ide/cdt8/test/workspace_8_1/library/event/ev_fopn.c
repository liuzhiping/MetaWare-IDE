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
*** File: ev_fopn.c
***
*** Comments:      
***   This file contains the function for opening an event quickly.
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
* Function Name    : _event_open_fast
* Returned Value   : 
*   Returns MQX_OK upon success, a Task Error code or an error code:
* Comments         :
*    Used by a task to gain access to an event instance via an index
* number (rather than a name)
*
* 
*END*------------------------------------------------------------------*/
 
_mqx_uint _event_open_fast
   (
      /* [IN] the event index to gain access to */
      _mqx_uint       event_index,

      /* [OUT] - the address where the event handle is to be 
      ** written
      */
      pointer _PTR_ returned_event_ptr
   )
{ /* Body */
            KERNEL_DATA_STRUCT_PTR      kernel_data;
   register EVENT_COMPONENT_STRUCT_PTR  event_component_ptr;
            EVENT_STRUCT_PTR            event_ptr;
   register EVENT_CONNECTION_STRUCT_PTR event_connection_ptr;
            _mqx_uint                    result;
            _mqx_max_type                   tmp;

   _GET_KERNEL_DATA(kernel_data);                                         

   _KLOGE3(KLOG_event_open_fast, event_index, returned_event_ptr);
   
   *returned_event_ptr = NULL;
   
   event_component_ptr = (EVENT_COMPONENT_STRUCT_PTR)
      kernel_data->KERNEL_COMPONENTS[KERNEL_EVENTS];
#if MQX_CHECK_ERRORS
   if (event_component_ptr == NULL)  {
      _KLOGX2(KLOG_event_open_fast, MQX_COMPONENT_DOES_NOT_EXIST);
      return(MQX_COMPONENT_DOES_NOT_EXIST);
   } /* Endif */
#endif
#if MQX_CHECK_VALIDITY
   if (event_component_ptr->VALID != EVENT_VALID){
      _KLOGX2(KLOG_event_open_fast, MQX_INVALID_COMPONENT_BASE);
      return(MQX_INVALID_COMPONENT_BASE);
   } /* Endif */
#endif

   result = _name_find_internal_by_index(event_component_ptr->NAME_TABLE_HANDLE, 
      event_index, &tmp);
#if MQX_CHECK_ERRORS
   if (result != MQX_OK) {
      if (result == NAME_NOT_FOUND) {
         _KLOGX2(KLOG_event_open_fast, EVENT_NOT_FOUND);
         return(EVENT_NOT_FOUND);
      } /* Endif */
      _KLOGX2(KLOG_event_open_fast, result);
      return(result);
   } /* Endif */
#endif

   event_ptr = (EVENT_STRUCT_PTR)tmp;

#if MQX_CHECK_VALIDITY
   if (event_ptr->VALID != EVENT_VALID) {
      /* Event not valid */
      _KLOGX2(KLOG_event_open_fast, EVENT_INVALID_EVENT);
      return(EVENT_INVALID_EVENT);
   } /* Endif */
#endif

   event_connection_ptr = (EVENT_CONNECTION_STRUCT_PTR)
      _mem_alloc_zero((_mem_size)sizeof(EVENT_CONNECTION_STRUCT));
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if (event_connection_ptr == NULL) {
      _KLOGX2(KLOG_event_open_fast, MQX_OUT_OF_MEMORY);
      return(MQX_OUT_OF_MEMORY);
   } /* Endif */
#endif
   
   event_connection_ptr->EVENT_PTR = event_ptr;
   event_connection_ptr->VALID     = EVENT_VALID;
   event_connection_ptr->TD_PTR    = kernel_data->ACTIVE_PTR;

   *returned_event_ptr = (pointer)event_connection_ptr;

   _KLOGX3(KLOG_event_open_fast, MQX_OK, event_connection_ptr);
   return(MQX_OK);
   
} /* Endbody */   
#endif /* MQX_USE_EVENTS */

/* EOF */
