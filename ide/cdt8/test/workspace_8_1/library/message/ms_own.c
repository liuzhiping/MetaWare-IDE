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
*** File: ms_own.c
***
*** Comments:      
***   This file contains the function for returning the task id
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "message.h"
#include "msg_prv.h"

#if MQX_USE_MESSAGES
/*FUNCTION*------------------------------------------------------------
*
* Function Name   : _msgq_get_owner
* Returned Value  : task id  which owns the queue
*                   MQX_NULL_TASK_ID if the message queue have an error.
*
*END*------------------------------------------------------------------*/

_task_id _msgq_get_owner
   (
      /* [IN] queue from which need to match the task id */
      _queue_id queue_id
   )
{ /* Body */
            KERNEL_DATA_STRUCT_PTR      kernel_data;
            MSG_COMPONENT_STRUCT_PTR    msg_component_ptr;
   register MSGQ_STRUCT_PTR             msgq_ptr;
            _queue_number               queue;
            _task_id                    task_id;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE2(KLOG_msgq_get_owner, queue_id);

    msg_component_ptr = _GET_MSG_COMPONENT_STRUCT_PTR(kernel_data);
#if MQX_CHECK_ERRORS
   if (msg_component_ptr == NULL){
      _task_set_error(MQX_COMPONENT_DOES_NOT_EXIST);
      _KLOGX3(KLOG_msgq_get_owner, MQX_NULL_TASK_ID, MQX_COMPONENT_DOES_NOT_EXIST);
      return(MQX_NULL_TASK_ID);
   } /* Endif */
#endif

   queue = QUEUE_FROM_QID(queue_id);

#if MQX_CHECK_ERRORS
   if ((PROC_NUMBER_FROM_QID(queue_id) != kernel_data->PROCESSOR_NUMBER) ||
      (! VALID_QUEUE(queue)) )
   {
      _task_set_error(MQX_INVALID_PROCESSOR_NUMBER);
      _KLOGX3(KLOG_msgq_get_owner, MQX_NULL_TASK_ID, MQX_INVALID_PROCESSOR_NUMBER);
      return (MQX_NULL_TASK_ID);
   } /* Endif */
 #endif

   msgq_ptr = &msg_component_ptr->MSGQS_PTR[queue];
   if (msgq_ptr->QUEUE != (queue)) {
      _task_set_error(MSGQ_QUEUE_IS_NOT_OPEN);
      _KLOGX3(KLOG_msgq_get_owner, MQX_NULL_TASK_ID, MSGQ_QUEUE_IS_NOT_OPEN);
      return (MQX_NULL_TASK_ID);      
   } /* Endif */
      
   task_id = msgq_ptr->TD_PTR->TASK_ID;
   _KLOGX3(KLOG_msgq_get_owner, task_id, MQX_OK);
   return (task_id);

} /* Endbody */
#endif /* MQX_USE_MESSAGES */

/* EOF */
