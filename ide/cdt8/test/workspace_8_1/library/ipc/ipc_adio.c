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
*** File: ipc_adio.c
***
*** Comments:      
***   This file contains the functions for the ipc task.
***
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "message.h"
#include "ipc.h"
#include "ipc_prv.h"

#if MQX_USE_IPC
/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _ipc_add_io_ipc_handler
* Returned Value   : _mqx_uint an mqx task error code
* Comments         :
*   this function adds an IPC io component handler.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _ipc_add_io_ipc_handler
   (
      /* [IN] The handler for the component */
      _mqx_uint (_CODE_PTR_ handler)(pointer),

      /* [IN] The component number */
      _mqx_uint             component
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR     kernel_data;
   IPC_COMPONENT_STRUCT_PTR   ipc_component_ptr;

   _GET_KERNEL_DATA(kernel_data);

   ipc_component_ptr = kernel_data->IPC_COMPONENT_PTR;

#if MQX_CHECK_ERRORS
   if (!ipc_component_ptr) {
      return MQX_IPC_SERVICE_NOT_AVAILABLE;
   } /* Endif */
#endif

   ipc_component_ptr->IPC_IO_COMPONENT_HANDLER[component] =
      (_mqx_uint (_CODE_PTR_)(IPC_MESSAGE_STRUCT_PTR))handler;
      
   return MQX_OK;

} /* Endif */
#endif /* MQX_USE_IPC */

/* EOF */
