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
*** File: ms_recf.c
***
*** Comments:      
***   This file contains the function for reading a message from a message 
***   queue.
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
* Function Name   : _msgq_receive_for
* Returned Value  : pointer to a message structure, or NULL if timeout 
*                   occurred.
* Comments        : Dequeue the first item from
*   the specified queue.  
*   ONLY tasks can call this function
*
*END*------------------------------------------------------------------*/

pointer _msgq_receive_for
   (
      /* [IN]  id of the queue from which a message is to be received */
     _queue_id              queue_id,

      /* 
      ** [IN]  the number of ticks which can expire before
      **       this request times out
      */
      MQX_TICK_STRUCT_PTR   tick_ptr
   )
{ /* Body */
#if MQX_KERNEL_LOGGING
   KERNEL_DATA_STRUCT_PTR      kernel_data;
#endif
   MESSAGE_HEADER_STRUCT_PTR   message_ptr;
   _mqx_uint                    error;


#if MQX_KERNEL_LOGGING
   _GET_KERNEL_DATA(kernel_data);
   _KLOGE3(KLOG_msgq_receive_for, queue_id, tick_ptr);
#endif

   message_ptr = _msgq_receive_internal(queue_id, tick_ptr, 
      MSG_TIMEOUT_RELATIVE, &error);

#if MQX_KERNEL_LOGGING
   if ( (error ==  MQX_OK) && (message_ptr == NULL) ) {
      _KLOGX3(KLOG_msgq_receive_for, message_ptr, MSGQ_MESSAGE_NOT_AVAILABLE);
   } else if (error == MQX_OK) {
      _KLOGX5(KLOG_msgq_receive_for, message_ptr, message_ptr->TARGET_QID,
         message_ptr->SOURCE_QID, *(_mqx_uint_ptr)((uchar_ptr)message_ptr+
         sizeof(MESSAGE_HEADER_STRUCT)));
   } else {
      _KLOGX3(KLOG_msgq_receive_for, message_ptr, error);
   } /* Endif */
#endif

   return (pointer)message_ptr;

} /* Endbody */
#endif /* MQX_USE_MESSAGES */

/* EOF */
