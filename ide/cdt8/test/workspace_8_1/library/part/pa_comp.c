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
*** File: pa_comp.c
***
*** Comments :
***  This file contains the function for creating and installing the
*** partition component.
***
***
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "part.h"
#include "part_prv.h"

#if MQX_USE_PARTITIONS
/*FUNCTION*-------------------------------------------------------------------
* 
* Function Name    : _partition_create_component
* Returned Value   : _mqx_uint MQX_OK, MQX_OUT_OF_MEMORY
* Comments         :
*   This function creates the partition kernel component.
*
*END*----------------------------------------------------------------------*/

_mqx_uint _partition_create_component
   ( 
      void
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR          kernel_data;
   PARTITION_COMPONENT_STRUCT_PTR  part_component_ptr;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE1(KLOG_partition_create_component);

#if MQX_CHECK_ERRORS
   if (kernel_data->IN_ISR) {
      _KLOGX2(KLOG_partition_create_component, MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
      return(MQX_CANNOT_CALL_FUNCTION_FROM_ISR);
   } /* Endif */
#endif

   _lwsem_wait((LWSEM_STRUCT_PTR)&kernel_data->COMPONENT_CREATE_LWSEM);

#if MQX_CHECK_ERRORS
   if (kernel_data->KERNEL_COMPONENTS[KERNEL_PARTITIONS] != NULL) {
      _lwsem_post((LWSEM_STRUCT_PTR)&kernel_data->COMPONENT_CREATE_LWSEM);
      _KLOGX2(KLOG_partition_create_component, MQX_OK);
      return(MQX_OK);
   } /* Endif */
#endif

   part_component_ptr = _mem_alloc_system_zero(
      (_mem_size)sizeof(PARTITION_COMPONENT_STRUCT));
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if (part_component_ptr == NULL) {
      _lwsem_post((LWSEM_STRUCT_PTR)&kernel_data->COMPONENT_CREATE_LWSEM);
      _KLOGX2(KLOG_partition_create_component, MQX_OUT_OF_MEMORY);
      return(MQX_OUT_OF_MEMORY);
   } /* Endif */
#endif

   kernel_data->KERNEL_COMPONENTS[KERNEL_PARTITIONS] = part_component_ptr;
/* START CR 308 */
   part_component_ptr->VALID = PARTITION_VALID;
/* END CR 308 */

   _QUEUE_INIT(&part_component_ptr->PARTITIONS, 0);

#if MQX_TASK_DESTRUCTION
   kernel_data->COMPONENT_CLEANUP[KERNEL_PARTITIONS] = _partition_cleanup;
#endif

   _lwsem_post((LWSEM_STRUCT_PTR)&kernel_data->COMPONENT_CREATE_LWSEM);

   _KLOGX2(KLOG_partition_create_component, MQX_OK);

   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_PARTITIONS */

/* EOF */
