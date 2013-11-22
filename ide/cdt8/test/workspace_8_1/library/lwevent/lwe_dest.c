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
*** File: lwe_dest.c
***
*** Comments:      
***   This file contains the function for destroying a light weight event.
***                                                               
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "lwevent.h"
#include "lwe_prv.h"

#if MQX_USE_LWEVENTS
/*FUNCTION*------------------------------------------------------------
* 
* Function Name    : _lwevent_destroy
* Returned Value   : 
*   Returns MQX_OK upon success, a Task Error code or an error code:
* Comments         :
*    Used by a task to destroy an instance of a light weight event
*
* 
*END*------------------------------------------------------------------*/

_mqx_uint _lwevent_destroy
   (
      /* [IN] the location of the event */
      LWEVENT_STRUCT_PTR event_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
#if MQX_COMPONENT_DESTRUCTION
   TD_STRUCT_PTR          td_ptr;
#endif

   _GET_KERNEL_DATA(kernel_data);                                         

   _KLOGE2(KLOG_lwevent_destroy, event_ptr);
   
#if MQX_COMPONENT_DESTRUCTION

#if MQX_CHECK_ERRORS
   if (kernel_data->IN_ISR) {
      _KLOGX2(KLOG_lwevent_destroy, MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
      return(MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
   } /* Endif */
#endif

   _int_disable();
#if MQX_CHECK_VALIDITY
   if (event_ptr->VALID != LWEVENT_VALID) {
      _int_enable();
      _KLOGX2(KLOG_lwevent_destroy, MQX_LWEVENT_INVALID);
      return(MQX_LWEVENT_INVALID);
   } /* Endif */
#endif

   /* Effectively stop all access to the event */
   event_ptr->VALID = 0;
   while (_QUEUE_GET_SIZE(&event_ptr->WAITING_TASKS)) {
      _QUEUE_DEQUEUE(&event_ptr->WAITING_TASKS, td_ptr);
      _BACKUP_POINTER(td_ptr, TD_STRUCT, AUX_QUEUE);
      _TIME_DEQUEUE(td_ptr, kernel_data);
      _TASK_READY(td_ptr, kernel_data);
   } /* Endwhile */   
 
   /* remove event from kernel LWEVENTS queue */
   _QUEUE_REMOVE(&kernel_data->LWEVENTS, event_ptr);

   _int_enable();

   /* May need to let higher priority task run */
   _CHECK_RUN_SCHEDULER();
#endif

   _KLOGX2(KLOG_lwevent_destroy, MQX_OK);
   return(MQX_OK);
   
} /* Endbody */   
#endif /* MQX_USE_LWEVENTS */

/* EOF */
