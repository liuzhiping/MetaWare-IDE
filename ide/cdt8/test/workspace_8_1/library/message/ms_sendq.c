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
*** File: ms_sendq.c
***
*** Comments:      
***   This file contains the function for sending a message to a queue.
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
* Function Name   :  _msgq_send_queue
* Returned Value  :  boolean, indicating validity of queue_id
* Comments        :  Put a message onto a directed queue
*
*END*------------------------------------------------------------------*/

boolean  _msgq_send_queue
   (
      /* [IN]  pointer to the  message being sent by application */
      pointer   msg_ptr,

      /* [IN] the queue upon which to put the message */
      _queue_id qid
   )
{ /* Body */
   boolean result;
   _KLOGM(KERNEL_DATA_STRUCT_PTR kernel_data;)

   _KLOGM(_GET_KERNEL_DATA(kernel_data);)

   _KLOGE4(KLOG_msgq_send_queue, msg_ptr, qid, ((MESSAGE_HEADER_STRUCT_PTR)msg_ptr)->SOURCE_QID);

   result = _msgq_send_internal((MESSAGE_HEADER_STRUCT_PTR)msg_ptr, FALSE, qid);

   _KLOGX2(KLOG_msgq_send_queue, result);
   return(result);
   
} /* Endbody */
#endif /* MQX_USE_MESSAGES */

/* EOF */
