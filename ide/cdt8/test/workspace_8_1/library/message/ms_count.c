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
*** File: ms_count.c
***
*** Comments:      
***   This file contains the function for returning the number of messages
***   waiting on a queue.
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
* Function Name   : _msgq_get_count
* Returned Value  : _mqx_uint, number of messages pending in this queue
* Comments        : 
*
*END*------------------------------------------------------------------*/

_mqx_uint  _msgq_get_count
   (
      /* [IN] the id of the queue which is being checked for waiting msgs  */
      _queue_id queue_id
   )
{ /* Body */
            KERNEL_DATA_STRUCT_PTR      kernel_data;
            MSG_COMPONENT_STRUCT_PTR    msg_component_ptr;
   register MSGQ_STRUCT_PTR             msgq_ptr;
            INTERNAL_MESSAGE_STRUCT_PTR imsg_ptr;
   register _mqx_uint                    pending;
   register uint_16                     queue;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE2(KLOG_msgq_get_count, queue_id);

   msg_component_ptr = _GET_MSG_COMPONENT_STRUCT_PTR(kernel_data);
#if MQX_CHECK_ERRORS
   if (msg_component_ptr == NULL) {
      _task_set_error(MQX_COMPONENT_DOES_NOT_EXIST);
      _KLOGX3(KLOG_msgq_get_count, MQX_COMPONENT_DOES_NOT_EXIST, 0);
      return(0);
   } /* Endif */
#endif
   
   if (queue_id == MSGQ_ANY_QUEUE) {
      _KLOGX3(KLOG_msgq_get_count, MQX_OK, kernel_data->ACTIVE_PTR->MESSAGES_AVAILABLE);
      return(kernel_data->ACTIVE_PTR->MESSAGES_AVAILABLE );
   } /* Endif */
 
   pending = 0;
   queue   = QUEUE_FROM_QID(queue_id);
   if ( (PROC_NUMBER_FROM_QID(queue_id) == kernel_data->PROCESSOR_NUMBER) &&
      VALID_QUEUE(queue) )
   {
      msgq_ptr = &msg_component_ptr->MSGQS_PTR[queue];
      if ( msgq_ptr->QUEUE == (queue) ) {
         pending = msgq_ptr->NO_OF_ENTRIES;

         /* Check for short-cutted message ie not on q but could have been */
         if (msgq_ptr->TD_PTR != NULL) {
            imsg_ptr = (INTERNAL_MESSAGE_STRUCT_PTR)msgq_ptr->TD_PTR->MESSAGE;
            if ((imsg_ptr != NULL) && 
               (QUEUE_FROM_QID(imsg_ptr->MESSAGE.TARGET_QID) == queue))
            {
               ++pending;
            } /* Endif */
         } /* Endif */
      } else {
         _task_set_error(MSGQ_QUEUE_IS_NOT_OPEN);
      } /* Endif */
   } else {
      _task_set_error(MSGQ_INVALID_QUEUE_ID);
   } /* Endif */

   _KLOGX2(KLOG_msgq_get_count, pending);
   return(pending);

} /* Endbody */
#endif /* MQX_USE_MESSAGES */

/* EOF */
