/*HEADER******************************************************************
**************************************************************************
*** 
*** Copyright (c) 1989-2004 ARC International.
*** All rights reserved                                          
***                                                              
*** This software embodies materials and concepts which are      
*** confidential to ARC International and is made
*** available solely pursuant to the terms of a written license   
*** agreement with ARC International             
***
*** File: mem_gsyp.c
***
*** Comments:      
***   This file contains the function for allocating memory from the
*** specified pool that belongs to the system.
***                                                               
**************************************************************************
*END*********************************************************************/

#define __MEMORY_MANAGER_COMPILE__
#include "mqx_inc.h"
#include "mem_prv.h"

/*FUNCTION*------------------------------------------------------------
*
* Function Name   : _mem_alloc_system_from
* Returned Value  : None
* Comments        : allocates memory that is available system wide.
*
*END*------------------------------------------------------------------*/

pointer _mem_alloc_system_from
   (
      /* [IN] the pool to allocate from */
      _mem_pool_id   pool_id,

      /* [IN] the size of the memory block */
      _mem_size      size

   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR  kernel_data;
   MEMPOOL_STRUCT_PTR      mem_pool_ptr = (MEMPOOL_STRUCT_PTR)pool_id;
   _mqx_uint               error;
   pointer                 result;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE2(KLOG_mem_alloc_system_from, size);

   _INT_DISABLE();
   result = _mem_alloc_internal(size, SYSTEM_TD_PTR(kernel_data), mem_pool_ptr,
      &error );

   /* update the memory allocation pointer in case a lower priority
   ** task was preempted inside _mem_alloc_internal
   */
   mem_pool_ptr->POOL_ALLOC_CURRENT_BLOCK = mem_pool_ptr->POOL_FREE_LIST_PTR;

   _INT_ENABLE();
   
#if MQX_CHECK_ERRORS
   if (error != MQX_OK) {
      _task_set_error(error);
   } /* Endif */
#endif

   _KLOGX3(KLOG_mem_alloc_system_from, result, mem_pool_ptr->POOL_BLOCK_IN_ERROR);

   return(result);

} /* Endbody */

/* EOF */
