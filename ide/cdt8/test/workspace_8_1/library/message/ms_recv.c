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
*** File: ms_recv.c
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
* Function Name   : _msgq_receive
* Returned Value  : pointer to a message structure, or NULL if timeout 
*                   occurred.
* Comments        : Dequeue the first item from
*   the specified queue.  
*   ONLY tasks can call this function
*
*END*------------------------------------------------------------------*/

pointer _msgq_receive
   (
      /* [IN]  id of the queue from which a message is to be received */
     _queue_id        queue_id,

      /* [IN]  indication of the number of millisecs which can expire before
      **       this request times out
      */
      uint_32         timeout
   )
{ /* Body */
   /* Start CR 330 */
   /* TIME_STRUCT                 time; */
   /* End CR 330 */
   MQX_TICK_STRUCT             ticks;
#if MQX_KERNEL_LOGGING
   KERNEL_DATA_STRUCT_PTR      kernel_data;
#endif
   MESSAGE_HEADER_STRUCT_PTR   message_ptr;
   _mqx_uint                   error;


#if MQX_KERNEL_LOGGING
   _GET_KERNEL_DATA(kernel_data);
   _KLOGE3(KLOG_msgq_receive, queue_id, timeout);
#endif

   if (timeout) {
      /* Convert milliseconds to ticks */
      /* Start CR 330 */
      /* time.MILLISECONDS = timeout % 1000; */
      /* time.SECONDS      = timeout / 1000; */
      /*                                     */
      /* PSP_TIME_TO_TICKS(&time, &ticks);   */
      PSP_MILLISECONDS_TO_TICKS_QUICK(timeout, &ticks);
      /* End CR 330 */

      message_ptr = _msgq_receive_internal(queue_id, &ticks, 
         MSG_TIMEOUT_RELATIVE, &error);
   } else {
      message_ptr = _msgq_receive_internal(queue_id, &ticks, 
         MSG_TIMEOUT_NONE, &error);
   } /* Endif */

#if MQX_KERNEL_LOGGING
   if ( (error ==  MQX_OK) && (message_ptr == NULL) ) {
      _KLOGX3(KLOG_msgq_receive, message_ptr, MSGQ_MESSAGE_NOT_AVAILABLE);
   } else if (error == MQX_OK) {
      _KLOGX5(KLOG_msgq_receive, message_ptr, message_ptr->TARGET_QID,
         message_ptr->SOURCE_QID, *(_mqx_uint_ptr)((uchar_ptr)message_ptr+
         sizeof(MESSAGE_HEADER_STRUCT)));
   } else {
      _KLOGX3(KLOG_msgq_receive, message_ptr, error);
   } /* Endif */
#endif

   return (pointer)message_ptr;

} /* Endbody */
#endif /* MQX_USE_MESSAGES */

/* EOF */
