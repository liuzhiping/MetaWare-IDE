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
*** File: ms_broad.c
***
*** Comments:      
***   This file contains the function for broadcasting messages.
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
* Function Name   :  _msgq_send_broadcast
* Returned Value  :  _mqx_uint, the number of messages sent.
* Comments        :  
*    send the message to multiple queues
*
*END*------------------------------------------------------------------*/

_mqx_uint  _msgq_send_broadcast
   (
      /* [IN] pointer to the  message being sent by application */
      pointer input_msg_ptr,
      
      /* [IN] an array of _queue_ids, MSGQ_NULL_QUEUE_ID terminated
      ** to which the message is to be sent.  A successfull
      ** message send will result in the queue id being overwritten
      ** with a MSGQ_NULL_QUEUE_ID
      */
      _queue_id _PTR_ qid_ptr,

      /* [IN] a pool id from which to obtain the messages,
      ** note that if a MSGPOOL_NULL_POOL_ID, then the message will be
      ** obtained from the system pools
      */
      _pool_id pool
   )
{ /* Body */
   _KLOGM(KERNEL_DATA_STRUCT_PTR kernel_data;)
   MESSAGE_HEADER_STRUCT_PTR     msg_ptr = (MESSAGE_HEADER_STRUCT_PTR)
      input_msg_ptr;
   MESSAGE_HEADER_STRUCT_PTR     sending_msg_ptr;
#if MQX_CHECK_ERRORS
   INTERNAL_MESSAGE_STRUCT_PTR   imsg_ptr;
#endif
   _mqx_uint                      size;
   _mqx_uint                      count;
   _mqx_uint                      error;

   _KLOGM(_GET_KERNEL_DATA(kernel_data);)
   _KLOGE4(KLOG_msgq_send_broadcast, msg_ptr, qid_ptr, pool);

   count = 0;

#if MQX_CHECK_ERRORS
   if (msg_ptr == NULL ) {
      _task_set_error(MSGQ_INVALID_MESSAGE);
      _KLOGX2(KLOG_msgq_send_broadcast, count);
      return(count);
   }/* Endif */
   if (qid_ptr == NULL ) {
      _task_set_error(MQX_INVALID_PARAMETER);
      _KLOGX2(KLOG_msgq_send_broadcast, count);
      return(count);
   } /* Endif */
#endif

#if MQX_CHECK_ERRORS
   imsg_ptr = GET_INTERNAL_MESSAGE_PTR(msg_ptr);
   if (imsg_ptr->VALID != MSG_VALID){
      /* An invalid message was input by the application. */
      _task_set_error(MSGQ_INVALID_MESSAGE);
      return(count);
   } /* Endif */
#endif

   if (*qid_ptr == MSGQ_NULL_QUEUE_ID ) {
      /* Nothing to do */
      _KLOGX2(KLOG_msgq_send_broadcast, count);
      return(count);
   } /* Endif */

   size = msg_ptr->SIZE;

   while (*(qid_ptr + 1) != MSGQ_NULL_QUEUE_ID) {
      if (pool == MSGPOOL_NULL_POOL_ID) {
         sending_msg_ptr = (MESSAGE_HEADER_STRUCT_PTR)
            _msg_alloc_system((_msg_size)size);
      } else {
         sending_msg_ptr = (MESSAGE_HEADER_STRUCT_PTR)_msg_alloc(pool);
      } /* Endif */
      if (sending_msg_ptr != NULL) {
         _mem_copy(msg_ptr, sending_msg_ptr, (_mem_size)size);
         sending_msg_ptr->TARGET_QID = *qid_ptr;
         error = _task_get_error();
         if (_msgq_send_internal(sending_msg_ptr, FALSE, *qid_ptr++) ) {
            count++;
         } /* Endif */
         _task_set_error(error);
      } /* Endif */
   } /* Endwhile */

   msg_ptr->TARGET_QID = *qid_ptr;
   error = _task_get_error();
   if (_msgq_send_internal(msg_ptr, FALSE, *qid_ptr)) {
      count++;
   } /* Endif */
   _task_set_error(error);
   _KLOGX2(KLOG_msgq_send_broadcast, count);
   return(count);
   
} /* Endbody */
#endif /* MQX_USE_MESSAGES */

/* EOF */
