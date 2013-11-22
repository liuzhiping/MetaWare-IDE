/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2007 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: ipc_evnt.c
***
*** Comments:      
***   This file contains the functions for the handling of multi-processor
*** events.
***
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "message.h"
#include "msg_prv.h"
#include "name.h"
#include "name_prv.h"
#include "event.h"
#include "evnt_prv.h"
#include "ipc.h"
#include "ipc_prv.h"

#if MQX_USE_IPC
/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _event_ipc_handler
* Returned Value   : _mqx_uint an mqx task error code
* Comments         :
*   this function handles multi-processor event requests.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _event_ipc_handler
   (
      /* [IN] The incoming message */
      pointer imsg_ptr
   )
{ /* Body */
   IPC_MESSAGE_STRUCT_PTR ipc_msg_ptr = imsg_ptr;
   pointer                event_ptr;
   _mqx_uint              error_code;
   _mqx_uint              result = 0;  /* CR 2366 */

   switch (IPC_GET_TYPE(ipc_msg_ptr->MESSAGE_TYPE)) {
      case IPC_EVENT_OPEN:
         error_code = _event_open((char _PTR_)&ipc_msg_ptr->PARAMETERS[0],
            &event_ptr);
         result = (_mqx_uint)event_ptr;
         break;
      case IPC_EVENT_SET:
         event_ptr = (pointer)ipc_msg_ptr->PARAMETERS[0];
         error_code = _event_set(event_ptr, ipc_msg_ptr->PARAMETERS[1]);
         break;
      default:
         error_code = MQX_IPC_INVALID_MESSAGE;
         break;
   } /* Endswitch */

   _ipc_send_internal(FALSE,
      PROC_NUMBER_FROM_QID(ipc_msg_ptr->HEADER.SOURCE_QID), 
      KERNEL_MESSAGES, IPC_ACTIVATE, 
      3, result, ipc_msg_ptr->REQUESTOR_ID,
      error_code);
   _msg_free(ipc_msg_ptr);

   return MQX_OK;

} /* Endif */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _event_install_ipc_handler
* Returned Value   : none.
* Comments         :
*   this function installs the handler for IPC events.
*
*END*----------------------------------------------------------------------*/

void _event_install_ipc_handler
   (
      void
   )
{ /* Body */

   _ipc_add_ipc_handler( _event_ipc_handler, KERNEL_EVENTS);

} /* Endbody */
#endif /* MQX_USE_IPC */

/* EOF */
