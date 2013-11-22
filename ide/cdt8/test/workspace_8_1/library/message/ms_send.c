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
*** File: ms_send.c
***
*** Comments:      
***   This file contains the functions for sending messages.
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
* Function Name   :  _msgq_send
* Returned Value  :  boolean, indicating validity of queue_id
* Comments        :  Verify the input queue_id and try to send the
*                    message directly. 
*
*   Messages can be sent by tasks and ISRs.
*
*END*------------------------------------------------------------------*/

boolean  _msgq_send
   (
      /* [IN]  pointer to the  message being sent by application */
      pointer input_msg_ptr
   )
{ /* Body */
   _KLOGM(KERNEL_DATA_STRUCT_PTR kernel_data;)
   MESSAGE_HEADER_STRUCT_PTR msg_ptr = (MESSAGE_HEADER_STRUCT_PTR)
      input_msg_ptr;
   boolean result;

   _KLOGM(_GET_KERNEL_DATA(kernel_data);)

   _KLOGE4(KLOG_msgq_send, msg_ptr, ((MESSAGE_HEADER_STRUCT_PTR)msg_ptr)->TARGET_QID, ((MESSAGE_HEADER_STRUCT_PTR)msg_ptr)->SOURCE_QID);

   result = _msgq_send_internal(msg_ptr, FALSE, msg_ptr->TARGET_QID);

   _KLOGX2(KLOG_msgq_send, result);

   return(result);
   
} /* Endbody */
#endif /* MQX_USE_MESSAGES */

/* EOF */
