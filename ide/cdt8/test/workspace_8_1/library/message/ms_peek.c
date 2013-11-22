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
*** File: ms_peek.c
***
*** Comments:      
***   This file contains the function for returning the address of a message
*** on the top of the message queue, without actually receiving the message.
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
* Function Name   : _msgq_peek
* Returned Value  : pointer to a message structure
* Comments        : 
*   return a pointer to the message at the head of the message queue,
* but DO NOT dequeue it
*
*END*------------------------------------------------------------------*/

pointer _msgq_peek
   (
      /* [IN]  the queue to look at */
      _queue_id queue_id
   )
{ /* Body */
            KERNEL_DATA_STRUCT_PTR      kernel_data;
            MSG_COMPONENT_STRUCT_PTR    msg_component_ptr;
   register MESSAGE_HEADER_STRUCT_PTR   message_ptr;
   register MSGQ_STRUCT_PTR             msgq_ptr;
   register INTERNAL_MESSAGE_STRUCT_PTR imsg_ptr;
            _queue_number               queue;

   _GET_KERNEL_DATA(kernel_data);
 
   _KLOGE2(KLOG_msgq_peek, queue_id);

    msg_component_ptr = _GET_MSG_COMPONENT_STRUCT_PTR(kernel_data);
#if MQX_CHECK_ERRORS
   if (msg_component_ptr == NULL) {
      _task_set_error(MQX_COMPONENT_DOES_NOT_EXIST);
      _KLOGX3(KLOG_msgq_peek, NULL, MQX_COMPONENT_DOES_NOT_EXIST);
      return(NULL);
   } /* Endif */
#endif

   message_ptr = NULL;
   queue       = QUEUE_FROM_QID(queue_id);

#if MQX_CHECK_ERRORS
   if ((PROC_NUMBER_FROM_QID(queue_id) != kernel_data->PROCESSOR_NUMBER) ||
      (! VALID_QUEUE(queue)) )
   {
      _task_set_error(MSGQ_INVALID_QUEUE_ID);
      _KLOGX3(KLOG_msgq_peek, NULL, MSGQ_INVALID_QUEUE_ID);
      return (pointer)message_ptr;
   } /* Endif */
#endif

   msgq_ptr = &msg_component_ptr->MSGQS_PTR[queue];
   if (msgq_ptr->QUEUE != (queue)) {
      _task_set_error(MSGQ_QUEUE_IS_NOT_OPEN);
      _KLOGX3(KLOG_msgq_poll, NULL, MSGQ_QUEUE_IS_NOT_OPEN);
      return((pointer)message_ptr);
   } /* Endif */
      
#if MQX_CHECK_ERRORS
   if (msgq_ptr->TD_PTR != NULL) {
      if (msgq_ptr->TD_PTR != kernel_data->ACTIVE_PTR) {
         _task_set_error(MSGQ_NOT_QUEUE_OWNER);
         _KLOGX3(KLOG_msgq_poll, NULL, MSGQ_NOT_QUEUE_OWNER);
         return((pointer)message_ptr);
      } /* Endif */
   } /* Endif */
#endif

   /* check the specified queue for an entry */
   _INT_DISABLE();
   if ( msgq_ptr->NO_OF_ENTRIES == 0 ) {
      _INT_ENABLE();
      _task_set_error(MSGQ_MESSAGE_NOT_AVAILABLE);
      _KLOGX3(KLOG_msgq_peek, NULL, MSGQ_MESSAGE_NOT_AVAILABLE);
   } else {
      /* Return the top entry, but keep it on the queue */
      imsg_ptr    = msgq_ptr->FIRST_MSG_PTR;
      message_ptr = (MESSAGE_HEADER_STRUCT_PTR)&imsg_ptr->MESSAGE;

      _INT_ENABLE();
      _KLOGX5(KLOG_msgq_peek, message_ptr, message_ptr->TARGET_QID, message_ptr->SOURCE_QID, *(_mqx_uint_ptr)((uchar_ptr)message_ptr+sizeof(MESSAGE_HEADER_STRUCT)));
   } /* Endif */
   return (pointer)message_ptr;

} /* Endbody */
#endif /* MQX_USE_MESSAGES */

/* EOF */
