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
*** File: ms_pool.c
***
*** Comments:
***   This file contains the functions for the creating a
*** message pool.
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
* Function Name   :  _msgpool_create
* Returned Value  :  _pool_id of the named pool, or MSGPOOL_NULL_POOL_ID on error
* Comments        :
*
*END*------------------------------------------------------------------*/

_pool_id   _msgpool_create
   (
      /*  [IN]  size of the messages being created  */
      uint_16  message_size,

      /*  [IN]  initial number of messages in this pool  */
      uint_16  num_messages,

      /*  [IN]  number of messages to grow pool by if empty */
      uint_16  grow_number,

      /*  [IN]  maximum number of messages allowed in pool */
      uint_16  grow_limit
   )
{ /* Body */
   _KLOGM(KERNEL_DATA_STRUCT_PTR kernel_data;)
   _pool_id result;

   _KLOGM(_GET_KERNEL_DATA(kernel_data);)
   _KLOGE5(KLOG_msgpool_create, message_size, num_messages, grow_number, grow_limit);

   result = _msgpool_create_internal(message_size, num_messages, grow_number,
      grow_limit, MSG_POOL);

   _KLOGX2(KLOG_msgpool_create, result);
   return(result);
   
} /* Endbody */
#endif /* MQX_USE_MESSAGES */

/* EOF */
