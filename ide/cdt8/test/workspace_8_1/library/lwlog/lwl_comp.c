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
*** File: lwl_comp.c
***
*** Comments:      
***   This file contains the function for creating the lw log component
***                                                               
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "log.h"
#include "lwlog.h"
#include "lwlogprv.h"

#if MQX_USE_LWLOGS
/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _lwlog_create_component
* Returned Value   : _mqx_uint MQX_OK, MQX_OUT_OF_MEMORY
* Comments         :
*   This function creates a kernel component providing a lightweight log 
* service for all user tasks.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _lwlog_create_component
   (
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR     kernel_data;
   LWLOG_COMPONENT_STRUCT_PTR log_component_ptr;

   _GET_KERNEL_DATA(kernel_data);

#if MQX_CHECK_ERRORS
   if (kernel_data->IN_ISR) {
      return MQX_CANNOT_CALL_FUNCTION_FROM_ISR;
   } /* Endif */
#endif

   _lwsem_wait((LWSEM_STRUCT_PTR)&kernel_data->COMPONENT_CREATE_LWSEM);

#if MQX_CHECK_ERRORS
   if (kernel_data->KERNEL_COMPONENTS[KERNEL_LWLOG] != NULL) {
      _lwsem_post((LWSEM_STRUCT_PTR)&kernel_data->COMPONENT_CREATE_LWSEM);
      return(MQX_OK);
   } /* Endif */
#endif

   log_component_ptr = _mem_alloc_system_zero(
      (_mem_size)sizeof(LWLOG_COMPONENT_STRUCT));
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if (log_component_ptr == NULL) {
      _lwsem_post((LWSEM_STRUCT_PTR)&kernel_data->COMPONENT_CREATE_LWSEM);
      return(MQX_OUT_OF_MEMORY);
   } /* Endif */
#endif

   kernel_data->KERNEL_COMPONENTS[KERNEL_LWLOG] = log_component_ptr;
   log_component_ptr->VALID = LWLOG_VALID;

   _lwsem_post((LWSEM_STRUCT_PTR)&kernel_data->COMPONENT_CREATE_LWSEM);

   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_LWLOGS */

/* EOF */
