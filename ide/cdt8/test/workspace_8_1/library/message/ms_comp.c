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
*** File: ms_comp.c
***
*** Comments:
***   This file contains the function for creating the message component.
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
* Function Name   : _msg_create_component
* Returned Value  : MQX error code
* Comments        :
*
*END*------------------------------------------------------------------*/

_mqx_uint  _msg_create_component
   (
      void
   )
{ /* Body */
            KERNEL_DATA_STRUCT_PTR   kernel_data;
   register MSG_COMPONENT_STRUCT_PTR msg_component_ptr;
            pointer                  pools_ptr;
            pointer                  msgqs_ptr;
            _mqx_uint                error;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE1(KLOG_msg_create_component);

   error = _lwsem_wait((LWSEM_STRUCT_PTR)&kernel_data->COMPONENT_CREATE_LWSEM);
#if MQX_CHECK_ERRORS
   if (error != MQX_OK) {
      _KLOGX2(KLOG_msg_create_component, error);
      return(error);
   } /* Endif */
#endif

   if (kernel_data->KERNEL_COMPONENTS[KERNEL_MESSAGES] != NULL) {
      _lwsem_post((LWSEM_STRUCT_PTR)&kernel_data->COMPONENT_CREATE_LWSEM);
      _KLOGX2(KLOG_msg_create_component, MQX_OK);
      return(MQX_OK);
   } /* Endif */

   msg_component_ptr = (MSG_COMPONENT_STRUCT_PTR)
      _mem_alloc_system_zero((_mem_size)sizeof(MSG_COMPONENT_STRUCT));
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if (msg_component_ptr == NULL) {
      _lwsem_post((LWSEM_STRUCT_PTR)&kernel_data->COMPONENT_CREATE_LWSEM);
      _KLOGX2(KLOG_msg_create_component, MQX_OUT_OF_MEMORY);
      return(MQX_OUT_OF_MEMORY);
   } /* Endif */
#endif

   if (kernel_data->INIT.MAX_MSGPOOLS == 0) {
      kernel_data->INIT.MAX_MSGPOOLS = 1;
   } /* Endif */
   pools_ptr = _mem_alloc_system_zero((_mem_size)(kernel_data->INIT.MAX_MSGPOOLS * 
      sizeof(MSGPOOL_STRUCT)));
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if (pools_ptr == NULL) {
      _lwsem_post((LWSEM_STRUCT_PTR)&kernel_data->COMPONENT_CREATE_LWSEM);
      _KLOGX2(KLOG_msg_create_component, MSGPOOL_POOL_NOT_CREATED);
      return MSGPOOL_POOL_NOT_CREATED;
   }/* Endif */
#endif

   if (kernel_data->INIT.MAX_MSGQS >= MAX_UINT_16) {
      kernel_data->INIT.MAX_MSGQS = MAX_UINT_16 - 1;
   } else if (kernel_data->INIT.MAX_MSGQS < 1) {
      kernel_data->INIT.MAX_MSGQS = 1;
   } /* Endif */
   msgqs_ptr = _mem_alloc_system_zero( (_mem_size)((kernel_data->INIT.MAX_MSGQS + 1) *
      sizeof(MSGQ_STRUCT)));
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if (msgqs_ptr == NULL) {
      _lwsem_post((LWSEM_STRUCT_PTR)&kernel_data->COMPONENT_CREATE_LWSEM);
      _mem_free(pools_ptr);
      _KLOGX2(KLOG_msg_create_component, MSGQ_TOO_MANY_QUEUES);
      return MSGQ_TOO_MANY_QUEUES;
   } /* Endif */
#endif

   if (msg_component_ptr->MSGPOOLS_PTR == NULL) {
      msg_component_ptr->MAX_MSGPOOLS_EVER    = 0;
      msg_component_ptr->SMALLEST_MSGPOOL_PTR = NULL;
      msg_component_ptr->LARGEST_MSGPOOL_PTR  = NULL;
      msg_component_ptr->MAX_MSGPOOLS = kernel_data->INIT.MAX_MSGPOOLS;
      msg_component_ptr->MAX_MSGQS    = kernel_data->INIT.MAX_MSGQS;
      msg_component_ptr->MSGPOOLS_PTR = (MSGPOOL_STRUCT_PTR)pools_ptr;
      pools_ptr = NULL;
      msg_component_ptr->MSGQS_PTR    = (MSGQ_STRUCT_PTR)msgqs_ptr;
      msgqs_ptr = NULL;
   }/* Endif */

   msg_component_ptr->VALID = MESSAGE_VALID;

   kernel_data->KERNEL_COMPONENTS[KERNEL_MESSAGES] = msg_component_ptr;

#if MQX_TASK_DESTRUCTION
   kernel_data->COMPONENT_CLEANUP[KERNEL_MESSAGES] = _msg_cleanup;
#endif

   _lwsem_post((LWSEM_STRUCT_PTR)&kernel_data->COMPONENT_CREATE_LWSEM);

   if (pools_ptr) {
      _mem_free(pools_ptr);
   }/* Endif */
   if (msgqs_ptr) {
      _mem_free(msgqs_ptr);
   }/* Endif */

   _KLOGX2(KLOG_msg_create_component, MQX_OK);
   return MQX_OK;

} /* Endbody */
#endif /* MQX_USE_MESSAGES */

/* EOF */
