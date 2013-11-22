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
*** File: ms_id.c
***
*** Comments:      
***   This file contains the function for creating a queue id.
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
* Function Name   : _msgq_get_id
* Returned Value  : _queue_id - the constructed QUEUE ID
* Comments        : This is an interface primitive is called by an 
*   application to get a queue id of a queue number.
*
*END*------------------------------------------------------------------*/

_queue_id _msgq_get_id
   (
      /* [IN] the processor on which the queue exists */
      _processor_number  processor_number,

      /* [IN] the queue number */
      _queue_number      queue_number
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;

   _GET_KERNEL_DATA(kernel_data);

#if MQX_CHECK_ERRORS
   if (processor_number > MQX_MAX_PROCESSOR_NUMBER) {
      return(MSGQ_NULL_QUEUE_ID);
   }/* Endif */
#endif   
   if ( processor_number == 0 ) {
      processor_number = (_processor_number)kernel_data->PROCESSOR_NUMBER;
   } /* Endif */
   return BUILD_QID(processor_number, queue_number);

} /* Endbody */
#endif /* MQX_USE_MESSAGES */

/* EOF */
