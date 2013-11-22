/*HEADER*******************************************************************
***************************************************************************
*** 
*** Copyright (c) 1989-2005 ARC International.
***
*** All rights reserved
***
*** This software embodies materials and concepts which are confidential
*** to ARC International and is made available
*** solely pursuant to the terms of a written license agreement with
*** ARC International
***
*** File: ipc_rout.c
***
*** Comments: 
***   This file contains the function for routing the MQX messages.
***
***
***************************************************************************
*END**********************************************************************/

#include "mqx_inc.h"
#include "message.h"
#include "msg_prv.h"
#include "ipc.h"
#include "ipc_prv.h"
#include "ipcrtprv.h"

#if MQX_USE_IPC
/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _ipc_msg_route_add
* Returned Value   : _mqx_uint result
* Comments         :
*   This function is called to add a route to the message routing table
*
*END*----------------------------------------------------------------------*/

_mqx_uint _ipc_msg_route_add
   (
      /* [IN] The minimum processor number in the range */
      _processor_number min_proc_number,

      /* [IN] The maximum processor number in the range */
      _processor_number max_proc_number,

      /* [IN] The queue to use if the processor number is in the above range */
      _queue_number     queue
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR               kernel_data;
   IPC_MSG_ROUTING_COMPONENT_STRUCT_PTR component_ptr;
   IPC_MSG_ROUTING_STRUCT_PTR           routing_ptr;
#if MQX_CHECK_ERRORS
   _processor_number                    i;
#endif

   _GET_KERNEL_DATA(kernel_data);
   component_ptr = (IPC_MSG_ROUTING_COMPONENT_STRUCT_PTR)
      kernel_data->KERNEL_COMPONENTS[KERNEL_IPC_MSG_ROUTING];
#if MQX_CHECK_ERRORS
   if (component_ptr == NULL) {
      return(MQX_COMPONENT_DOES_NOT_EXIST);
   } /* Endif */
   if ((min_proc_number < 1) || (max_proc_number > MQX_MAX_PROCESSOR_NUMBER) ||
       (min_proc_number > max_proc_number))
   {
      return(MQX_INVALID_PROCESSOR_NUMBER);
   } /* Endif */
   if ((queue == 0) || (queue >= MAX_QNUMBERS)) {
      return(MSGQ_INVALID_QUEUE_ID);
   } /* Endif */
   for (i = min_proc_number; i <= max_proc_number; ++i) {
      if (_ipc_msg_processor_route_exists(i)) {
         return(IPC_ROUTE_EXISTS);
      } /* Endif */
   } /* Endfor */
#endif
   routing_ptr = (IPC_MSG_ROUTING_STRUCT_PTR)_mem_alloc_system(
      sizeof(IPC_MSG_ROUTING_STRUCT));
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if (routing_ptr == NULL) {
      return(MQX_OUT_OF_MEMORY);
   } /* Endif */
#endif
    routing_ptr->MIN_PROC_NUMBER = min_proc_number;
    routing_ptr->MAX_PROC_NUMBER = max_proc_number;
    routing_ptr->QUEUE           = queue;

    _QUEUE_ENQUEUE(&component_ptr->ROUTING_LIST, &routing_ptr->LINK);
   
   return (MQX_OK);

} /* Endbody */

   
/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _ipc_msg_route_remove
* Returned Value   : _mqx_uint result
* Comments         :
*   This function is called to remove a routing from the message routing table
*
*END*----------------------------------------------------------------------*/

_mqx_uint _ipc_msg_route_remove
   (
      /* [IN] The minimum processor number in the range */
      _processor_number min_proc_number,

      /* [IN] The maximum processor number in the range */
      _processor_number max_proc_number,

      /* [IN] The queue to use if the processor number is in the above range */
      _queue_number     queue
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR               kernel_data;
   IPC_MSG_ROUTING_COMPONENT_STRUCT_PTR component_ptr;
   IPC_MSG_ROUTING_STRUCT_PTR           routing_ptr;
   IPC_MSG_ROUTING_STRUCT_PTR           next_ptr;
   uint_16                              i;

   _GET_KERNEL_DATA(kernel_data);
   component_ptr = (IPC_MSG_ROUTING_COMPONENT_STRUCT_PTR)((pointer)
      kernel_data->KERNEL_COMPONENTS[KERNEL_IPC_MSG_ROUTING]);
#if MQX_CHECK_ERRORS
   if (component_ptr == NULL) {
      return(MQX_COMPONENT_DOES_NOT_EXIST);
   } /* Endif */
   if ((min_proc_number < 1) || (max_proc_number > MQX_MAX_PROCESSOR_NUMBER) ||
       (min_proc_number > max_proc_number))
   {
      return(MQX_INVALID_PROCESSOR_NUMBER);
   } /* ENdif */
#endif   

   routing_ptr = (IPC_MSG_ROUTING_STRUCT_PTR)
      ((pointer)component_ptr->ROUTING_LIST.NEXT);

   /* Scan list for specified processors */
   for (i = 0; i < component_ptr->ROUTING_LIST.SIZE; i++) {
       next_ptr = (IPC_MSG_ROUTING_STRUCT_PTR)((pointer)routing_ptr->LINK.NEXT);
       if ((routing_ptr->MIN_PROC_NUMBER == min_proc_number) &&
          (routing_ptr->MAX_PROC_NUMBER == max_proc_number))
       {
#if MQX_CHECK_ERRORS
          if (routing_ptr->QUEUE == queue) {
#endif
             _QUEUE_REMOVE(&component_ptr->ROUTING_LIST, &routing_ptr->LINK);
             _mem_free(routing_ptr);
#if MQX_CHECK_ERRORS
          } /* Endif */
#endif
       } /* Endif */
       routing_ptr = next_ptr;
   } /* Endfor */

   return (MQX_OK);

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _ipc_msg_processor_route_exists
* Returned Value   : pointer - Returns pointer to route. NULL otherwise.
* Comments         : This function checks to see if a route exists to the 
*   specified processor
*
*END*----------------------------------------------------------------------*/

pointer _ipc_msg_processor_route_exists
   (
      /* [IN] The processor number to check */
      _processor_number proc_number

   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR                kernel_data;
   IPC_MSG_ROUTING_COMPONENT_STRUCT_PTR  component_ptr;
   IPC_MSG_ROUTING_STRUCT_PTR            routing_ptr;
   uint_16                               i;

   _GET_KERNEL_DATA(kernel_data);
   component_ptr = (IPC_MSG_ROUTING_COMPONENT_STRUCT_PTR)
      kernel_data->KERNEL_COMPONENTS[KERNEL_IPC_MSG_ROUTING];
#if MQX_CHECK_ERRORS
   if (component_ptr == NULL) {
      return(NULL);
   } /* Endif */
#endif

   routing_ptr = (IPC_MSG_ROUTING_STRUCT_PTR)((pointer)
      component_ptr->ROUTING_LIST.NEXT);
   for (i = 0; i < component_ptr->ROUTING_LIST.SIZE; i++) {
      if ((proc_number >= routing_ptr->MIN_PROC_NUMBER) &&
         (proc_number <= routing_ptr->MAX_PROC_NUMBER))
      {
         return((pointer)routing_ptr);
      } /* Endif */
      routing_ptr = (IPC_MSG_ROUTING_STRUCT_PTR)((pointer)
         routing_ptr->LINK.NEXT);
   } /* Endfor */

   /* Route not found */
   return(NULL);

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _ipc_msg_route_init_internal
* Returned Value   : _mqx_uint result
* Comments         :
*   This function is called from the _ipc_init to initialize the message
* router.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _ipc_msg_route_init_internal
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR               kernel_data;
   IPC_MSG_ROUTING_COMPONENT_STRUCT_PTR component_ptr;
   IPC_ROUTING_STRUCT_PTR               route_ptr;
   _mqx_uint                            result;

   _GET_KERNEL_DATA(kernel_data);

   component_ptr = (IPC_MSG_ROUTING_COMPONENT_STRUCT_PTR)
      _mem_alloc_system_zero((_mem_size)
      sizeof(IPC_MSG_ROUTING_COMPONENT_STRUCT));
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if (component_ptr == NULL) {
      return(MQX_OUT_OF_MEMORY);
   } /* Endif */
#endif

   _QUEUE_INIT(&component_ptr->ROUTING_LIST, 0);

   /* Install the message routing handlers */
   kernel_data->KERNEL_COMPONENTS[KERNEL_IPC_MSG_ROUTING] = (pointer)component_ptr;

   route_ptr = _ipc_routing_table;
   while (route_ptr->QUEUE != 0) {
      result = _ipc_msg_route_add(route_ptr->MIN_PROC_NUMBER,
         route_ptr->MAX_PROC_NUMBER, route_ptr->QUEUE);
#if MQX_CHECK_ERRORS
      if (result != MQX_OK) {
         return(result);
      } /* Endif */
#endif      
      route_ptr++;
   } /* Endwhile */
   
   component_ptr->MSG_ROUTER = _ipc_msg_route_internal;

   return (MQX_OK);

} /* Endbody */


/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _ipc_msg_route_internal
* Returned Value   : _mqx_uint result
* Comments         :
*   This function is called from _msg_send to route an inter-processor message.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _ipc_msg_route_internal
   (
      /* [IN] the processor to send the message to */
      _processor_number pnum,
      
      /* [IN] the message to be sent */
      pointer           message,
      
      /* [IN] is this a blocking message */
      boolean           blocking
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR               kernel_data;
   IPC_MSG_ROUTING_COMPONENT_STRUCT_PTR component_ptr;
   IPC_MSG_ROUTING_STRUCT_PTR           route_ptr;
   boolean                              result;
   _queue_number                        queue;

   _GET_KERNEL_DATA(kernel_data);
   component_ptr = (IPC_MSG_ROUTING_COMPONENT_STRUCT_PTR)
      kernel_data->KERNEL_COMPONENTS[KERNEL_IPC_MSG_ROUTING];
   
   route_ptr = (IPC_MSG_ROUTING_STRUCT_PTR)_ipc_msg_processor_route_exists(pnum);
   if (!route_ptr) {
      _task_set_error(MSGQ_INVALID_QUEUE_ID);
      return(FALSE);
   } /* Endif */
   queue = route_ptr->QUEUE;

   result = _msgq_send_queue(message,
      BUILD_QID(kernel_data->PROCESSOR_NUMBER, queue));
   if (result && blocking && (!kernel_data->IN_ISR) ) {
      _int_disable();
      kernel_data->ACTIVE_PTR->STATE = SEND_BLOCKED;
      _task_block();
      _int_enable();
   } /* Endif */

   return(result);
   
} /* Endbody */
#endif /* MQX_USE_IPC */

/* EOF */