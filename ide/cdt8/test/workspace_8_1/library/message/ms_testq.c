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
*** File: ms_testq.c
***
*** Comments:      
***   This file contains the function for testing all message queues
*** in the system
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
* Function Name   : _msgq_test
* Returned Value  : _mqx_uint MQX_OK or a MQX error code
* Comments        : This function tests all message queues in the system
* for validity and consistency
*
*END*------------------------------------------------------------------*/

_mqx_uint _msgq_test
   (  
      /* [OUT] the queue in error */
      pointer _PTR_ queue_error_ptr,
      
      /* [OUT] the message in error */
      pointer _PTR_ msg_error_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR      kernel_data;
   MSG_COMPONENT_STRUCT_PTR    msg_component_ptr;
   MSGQ_STRUCT_PTR             msgq_ptr;
   INTERNAL_MESSAGE_STRUCT_PTR imsg_ptr;
   _mqx_uint                    i,j;

   _GET_KERNEL_DATA(kernel_data);
   _KLOGE3(KLOG_msgq_test, queue_error_ptr, msg_error_ptr);
   msg_component_ptr = _GET_MSG_COMPONENT_STRUCT_PTR(kernel_data);
#if MQX_CHECK_ERRORS
   if (msg_component_ptr == NULL) {
      _KLOGX2(KLOG_msgq_test, MQX_COMPONENT_DOES_NOT_EXIST);
      return(MQX_COMPONENT_DOES_NOT_EXIST);
   } /* Endif */
#endif
   
   /* test the message queues */
   msgq_ptr = &msg_component_ptr->MSGQS_PTR[MSGQ_FIRST_USER_QUEUE];
   for (i = 1; i <= msg_component_ptr->MAX_MSGQS; i++) {
      _int_disable();
      if (msgq_ptr->QUEUE == i) {
         /* Queue is open */
         imsg_ptr = msgq_ptr->FIRST_MSG_PTR;
         for (j = 0; j < msgq_ptr->NO_OF_ENTRIES; ++j) {
            if ((imsg_ptr->VALID != MSG_VALID) && (!imsg_ptr->QUEUED)){
               _int_enable();
               *queue_error_ptr = msgq_ptr;
               *msg_error_ptr   = imsg_ptr;
               _KLOGX4(KLOG_msgq_test, MSGQ_INVALID_MESSAGE, msgq_ptr,
                  imsg_ptr);
               return(MSGQ_INVALID_MESSAGE);
            } /* Endif */
            imsg_ptr = imsg_ptr->NEXT;
         } /* Endfor */ 
      } /* Endif */
      _int_enable();
     ++msgq_ptr;
   } /* Endfor */

   _KLOGX2(KLOG_msgq_test, MQX_OK);
   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_MESSAGES */

/* EOF */
