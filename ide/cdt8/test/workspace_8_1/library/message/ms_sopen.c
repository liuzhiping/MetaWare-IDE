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
*** File: ms_sopen.c
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
* Function Name   : _msgq_open_system
* Returned Value  : _queue_id - indicating successful queue creation,
*   a 0 indicates error
* Comments        : This funciton is called by the application,
*   to open a message queue that is available for use by tasks and ISRs
*
*END*------------------------------------------------------------------*/

_queue_id _msgq_open_system
   (
      /* [IN] the queue being opened */
      _queue_number queue,

      /* [IN] the maximum number of entries allowed in this queue */
      uint_16       max_queue_size,

      /* [IN] the function to be called when an entry is put on the queue */
      void (_CODE_PTR_ notification_function)(pointer),

      /* [IN] information to pass to the notification function */
      pointer       notification_data
   )
{ /* Body */
   _queue_id result;
   _KLOGM(KERNEL_DATA_STRUCT_PTR kernel_data;)

   _KLOGM(_GET_KERNEL_DATA(kernel_data);)
   _KLOGE5(KLOG_msgq_open_system, queue, max_queue_size, notification_function, notification_data);
      
   result = _msgq_open_internal(queue, max_queue_size, SYSTEM_MSG_QUEUE,
      notification_function, notification_data);

   _KLOGX2(KLOG_msgq_open_system, result);

   return(result);

} /* Endbody */
#endif /* MQX_USE_MESSAGES */

/* EOF */
