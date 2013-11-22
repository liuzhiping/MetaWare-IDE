/*HEADER***************************************************************
***********************************************************************
***
*** Copyright (c) 1989-2005 ARC International.
*** All rights reserved
***
*** This software embodies materials and concepts which are
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license
*** agreement with ARC International
***
***
*** File: ms_free.c
***
*** Comments:
***   This file contains the functions for freeing a message
***
***
************************************************************************
*END*******************************************************************/

#include "mqx_inc.h"
#include "message.h"
#include "msg_prv.h"

#if MQX_USE_MESSAGES
/*FUNCTION*------------------------------------------------------------
*
* Function Name   : _msg_free
* Returned Value  : None
* Comments        : mark the specified message as 'free'
*
*END*------------------------------------------------------------------*/

void  _msg_free
   (
      /*  [IN]   pointer to a message struct which is to be released  */
      pointer msg_ptr
   )
{ /* Body */
            KERNEL_DATA_STRUCT_PTR      kernel_data;
   register INTERNAL_MESSAGE_STRUCT_PTR imsg_ptr;
   register MSGPOOL_STRUCT_PTR          msgpool_ptr;

   _GET_KERNEL_DATA(kernel_data);
   _KLOGE2(KLOG_msg_free, msg_ptr);

   imsg_ptr = GET_INTERNAL_MESSAGE_PTR(msg_ptr);
#if MQX_CHECK_VALIDITY
   if ( imsg_ptr->VALID != MSG_VALID ) {
      _KLOGX2(KLOG_msg_free, MQX_INVALID_POINTER);
      _task_set_error(MQX_INVALID_POINTER);
      return;
   } /* Endif */
#endif

#if MQX_CHECK_ERRORS
   if (imsg_ptr->FREE){
      _KLOGX2(KLOG_msg_free, MQX_NOT_RESOURCE_OWNER);
      _task_set_error(MQX_NOT_RESOURCE_OWNER);
      return;
   } /* Endif */
   if (imsg_ptr->QUEUED){
      _KLOGX2(KLOG_msg_free, MSGQ_MESSAGE_IS_QUEUED);
      _task_set_error(MSGQ_MESSAGE_IS_QUEUED);
      return;
   } /* Endif */
#endif

   msgpool_ptr = imsg_ptr->MSGPOOL_PTR;
   imsg_ptr->FREE   = TRUE;
   imsg_ptr->QUEUED = FALSE;

   _INT_DISABLE();
   /* Link onto the free list */
   imsg_ptr->NEXT = msgpool_ptr->MSG_FREE_LIST_PTR;
   msgpool_ptr->MSG_FREE_LIST_PTR   = imsg_ptr;
   ++msgpool_ptr->SIZE;
   _INT_ENABLE();

   _KLOGX2(KLOG_msg_free, MQX_OK);

} /* Endbody */
#endif /* MQX_USE_MESSAGES */

/* EOF */
