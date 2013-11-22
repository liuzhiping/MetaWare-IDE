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
*** File: ms_dpool.c
***
*** Comments:
***   This file contains the function for deleting a message pool.
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
* Function Name   :  _msgpool_destroy
* Returned Value  :  _mqx_uint MQX_OK, or a MQX error code.
* Comments        :
*
*END*------------------------------------------------------------------*/

_mqx_uint _msgpool_destroy
   (
      /*  [IN]  the pool to delete */
      _pool_id pool_id
   )
{ /* Body */
#if MQX_KERNEL_LOGGING || MQX_CHECK_ERRORS
   KERNEL_DATA_STRUCT_PTR    kernel_data;
#endif
#if MQX_CHECK_ERRORS
   MSG_COMPONENT_STRUCT_PTR  msg_component_ptr;
#endif
   MSGPOOL_STRUCT_PTR        msgpool_ptr;
   MSGPOOL_BLOCK_STRUCT_PTR  msgpool_block_ptr;
   MSGPOOL_BLOCK_STRUCT_PTR  next_block_ptr;

#if MQX_KERNEL_LOGGING || MQX_CHECK_ERRORS
   _GET_KERNEL_DATA(kernel_data);
#endif

   _KLOGE2(KLOG_msgpool_destroy, pool_id);

#if MQX_CHECK_ERRORS
   msg_component_ptr = _GET_MSG_COMPONENT_STRUCT_PTR(kernel_data);
   if (msg_component_ptr == NULL) {
      _KLOGX2(KLOG_msgpool_destroy, MQX_COMPONENT_DOES_NOT_EXIST);
      return MQX_COMPONENT_DOES_NOT_EXIST;
   } /* Endif */
#endif

   msgpool_ptr = (MSGPOOL_STRUCT_PTR)pool_id;
#if MQX_CHECK_VALIDITY
   if ( msgpool_ptr->VALID != MSG_VALID ) {
      _KLOGX2(KLOG_msgpool_destroy, MSGPOOL_INVALID_POOL_ID);
      return MSGPOOL_INVALID_POOL_ID;
   } /* Endif */
#endif

   _int_disable();
   if (msgpool_ptr->SIZE == msgpool_ptr->MAX) {
      /* All messages currently returned, lets delete them */
      msgpool_ptr->SIZE        = 0;
      msgpool_ptr->GROW_NUMBER = 0;
      _int_enable();

      msgpool_block_ptr = msgpool_ptr->MSGPOOL_BLOCK_PTR;
      while (msgpool_block_ptr != NULL) {
         next_block_ptr = msgpool_block_ptr->NEXT_BLOCK_PTR;
         _mem_free((pointer)msgpool_block_ptr);
         msgpool_block_ptr = next_block_ptr;
      } /* Endwhile */

      msgpool_ptr->MSGPOOL_BLOCK_PTR = NULL;
      msgpool_ptr->VALID             = 0;
      msgpool_ptr->MSGPOOL_TYPE      = 0;
      _KLOGX2(KLOG_msgpool_destroy, MQX_OK);
      return MQX_OK;
   } else {
      _int_enable();
      _KLOGX2(KLOG_msgpool_destroy, MSGPOOL_ALL_MESSAGES_NOT_FREE);
      return MSGPOOL_ALL_MESSAGES_NOT_FREE;
   } /* Endif */

} /* Endbody */
#endif /* MQX_USE_MESSAGES */

/* EOF */
