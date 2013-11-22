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
*** File: ev_test.c
***
*** Comments:      
***   This file contains the function for testing the event component.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "name.h"
#include "name_prv.h"
#include "event.h"
#include "evnt_prv.h"
#include "mqx_str.h"

#if MQX_USE_EVENTS
/*FUNCTION*------------------------------------------------------------
* 
* Function Name    : _event_test
* Returned Value   :  _mqx_uint MQX_OK or a MQX error code
* Comments         :
*    This function tests the event component for validity and consistency.
* 
*END*------------------------------------------------------------------*/

_mqx_uint _event_test
   (
      /* [OUT] the event in error */
      pointer _PTR_ event_error_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR     kernel_data;
   EVENT_COMPONENT_STRUCT_PTR event_component_ptr;
   EVENT_STRUCT_PTR           event_ptr;
   pointer                    table_ptr;
   _mqx_uint                   result;
   _mqx_uint                   i;
   _mqx_max_type                  data;

   _GET_KERNEL_DATA(kernel_data);                                         

   _KLOGE2(KLOG_event_test, event_error_ptr);

   *event_error_ptr = NULL;

   event_component_ptr = (EVENT_COMPONENT_STRUCT_PTR)
      kernel_data->KERNEL_COMPONENTS[KERNEL_EVENTS];
   if (event_component_ptr == NULL) {
      _KLOGX2(KLOG_event_test, MQX_OK);
      return(MQX_OK);
   } /* Endif */

   if (event_component_ptr->VALID != EVENT_VALID){
      _KLOGX2(KLOG_event_test, MQX_INVALID_COMPONENT_BASE);
      return(MQX_INVALID_COMPONENT_BASE);
   } /* Endif */

   _int_disable();
   /* Test the event name table */
   result = _name_test_internal((NAME_COMPONENT_STRUCT_PTR)
      event_component_ptr->NAME_TABLE_HANDLE, (pointer _PTR_)&table_ptr, 
      (pointer _PTR_)event_error_ptr);
   _int_enable();
   if (result != MQX_OK) {
      _KLOGX3(KLOG_event_test, MQX_INVALID_COMPONENT_BASE, *event_error_ptr);
      return(MQX_INVALID_COMPONENT_BASE);
   } /* Endif */


   i = 0;
   while (TRUE) {
      _int_disable();
      result =  _name_find_internal_by_index(
         event_component_ptr->NAME_TABLE_HANDLE, i++, &data);
      if (result != MQX_OK) {
         result = MQX_OK;
         break;
      } /* Endif */

      event_ptr = (EVENT_STRUCT_PTR)data;
      if (event_ptr->VALID != EVENT_VALID) {
         result = EVENT_INVALID_EVENT;
         break;
      } /* Endif */
      result = _queue_test(&event_ptr->WAITING_TASKS, event_error_ptr);
      if (result != MQX_OK) {
         break;
      } /* Endif */
      _int_enable();

   } /* Endwhile */

   _int_enable();

   _KLOGX3(KLOG_event_test, result, *event_error_ptr);
   return(result);

} /* Endbody */
#endif /* MQX_USE_EVENTS */

/* EOF */
