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
*** File: ms_sendu.c
***
*** Comments:      
***   This file contains the function for sending an urgent message.
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
* Function Name   :  _msgq_send_urgent
* Returned Value  :  boolean, indicating validity of queue_id
* Comments        :  
*    This function sends an urgent message that is put at the head
* of the message queue.
*
*END*------------------------------------------------------------------*/

boolean  _msgq_send_urgent
   (
      /* [IN]  pointer to the  message being sent by application */
      pointer input_msg_ptr
   )
{ /* Body */
   boolean result;
   MESSAGE_HEADER_STRUCT_PTR msg_ptr = (MESSAGE_HEADER_STRUCT_PTR)
      input_msg_ptr;
   _KLOGM(KERNEL_DATA_STRUCT_PTR kernel_data;)

   _KLOGM(_GET_KERNEL_DATA(kernel_data);)
   _KLOGE4(KLOG_msgq_send_urgent, msg_ptr, ((MESSAGE_HEADER_STRUCT_PTR)msg_ptr)->TARGET_QID, ((MESSAGE_HEADER_STRUCT_PTR)msg_ptr)->SOURCE_QID);

   msg_ptr->CONTROL |= MSG_HDR_URGENT;

   result = _msgq_send_internal(msg_ptr, FALSE, msg_ptr->TARGET_QID);

   _KLOGX2(KLOG_msgq_send_urgent, result);
   return(result);
   
} /* Endbody */
#endif /* MQX_USE_MESSAGES */

/* EOF */
