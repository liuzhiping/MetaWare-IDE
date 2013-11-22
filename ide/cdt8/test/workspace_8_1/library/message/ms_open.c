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
*** File: ms_open.c
***
*** Comments:      
***   This file contains the functions for creating message queues.
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
* Function Name   : _msgq_open
* Returned Value  : _queue_id - indicating successful queue creation,
*   a NULL indicates error
* Comments        : This function is called by the application,
*   to open a specific queue.  If Queue is non-zero, then it must not be
*   currently open.  IF Queue is zero, then a free queue entry is
*   found and the queue_id returned to the requester.
*
*END*------------------------------------------------------------------*/

_queue_id _msgq_open
   (
      /* [IN] the queue being opened */
      _queue_number queue,

      /* [IN] the maximum number of entries allowed in this queue */
      uint_16       max_queue_size
   )
{ /* Body */
   _queue_id result;
   _KLOGM(KERNEL_DATA_STRUCT_PTR kernel_data;)

   _KLOGM(_GET_KERNEL_DATA(kernel_data);)
   _KLOGE3(KLOG_msgq_open, queue, max_queue_size);

   result =  _msgq_open_internal(queue, max_queue_size, MSG_QUEUE,
      (void (_CODE_PTR_)(pointer))NULL, NULL);

   _KLOGX2(KLOG_msgq_open, result);

   return result;

} /* Endbody */
#endif /* MQX_USE_MESSAGES */

/* EOF */
