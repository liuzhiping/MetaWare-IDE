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
*** File: ms_poola.c
***
*** Comments:
***   This file contains the function which returns the number of messages
*** available in a message pool.
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
* Function Name   :  _msg_available
* Returned Value  :  _mqx_uint the number of message messages available
* Comments        :
*   returns the number of available message messages in the pool.
*   If the input POOL_ID is MSGPOOL_NULL_POOL_ID then the total number of
*   general pool messages are returned.
*
*END*------------------------------------------------------------------*/

_mqx_uint _msg_available
   (
      /* [IN]  the pool from which to get the size */
      _pool_id      pool
   )
{ /* Body */
            KERNEL_DATA_STRUCT_PTR   kernel_data;
            MSG_COMPONENT_STRUCT_PTR msg_component_ptr;
   register MSGPOOL_STRUCT_PTR       msgpool_ptr;
            _mqx_uint                 i;
            _mqx_uint                 count;

   _GET_KERNEL_DATA(kernel_data);

   msg_component_ptr = _GET_MSG_COMPONENT_STRUCT_PTR(kernel_data);
#if MQX_CHECK_ERRORS
   if (msg_component_ptr == NULL){
      return(0);
   } /* Endif */
#endif

   msgpool_ptr = msg_component_ptr->MSGPOOLS_PTR;
#if MQX_CHECK_ERRORS
   if (msgpool_ptr == NULL) {
      return(0);
   }/* Endif */
#endif

   if (pool == MSGPOOL_NULL_POOL_ID) {

      count     = 0;
      _INT_DISABLE();
      i = msg_component_ptr->MAX_MSGPOOLS_EVER + 1;
      while ( --i ) {
         if ( 
#if MQX_CHECK_VALIDITY
            (msgpool_ptr->VALID == MSG_VALID) &&
#endif
            (msgpool_ptr->MSGPOOL_TYPE == SYSTEM_MSG_POOL))
         {
            count += msgpool_ptr->SIZE;
         } /* Endif */
         ++msgpool_ptr;
      } /* Endwhile */
      _INT_ENABLE();
      return count;

   } else {

      msgpool_ptr   = (MSGPOOL_STRUCT_PTR)pool;
      if ( 
#if MQX_CHECK_VALIDITY
         (msgpool_ptr->VALID != MSG_VALID) || 
#endif
         (msgpool_ptr->MSGPOOL_TYPE != MSG_POOL) )
      {  
         _task_set_error(MSGPOOL_INVALID_POOL_ID);
         return (0);
      } /* Endif */
      return (_mqx_uint)msgpool_ptr->SIZE;

   } /* Endif */
   
} /* Endbody */
#endif /* MQX_USE_MESSAGES */

/* EOF */
