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
*** File: mem_zsys.c
***
*** Comments:      
***   This file contains the functions for returning a memory
*** block owned by the system.  The block is also zeroed out.
***                                                               
***
**************************************************************************
*END*********************************************************************/

#define __MEMORY_MANAGER_COMPILE__
#include "mqx_inc.h"
#include "mem_prv.h"

/*FUNCTION*------------------------------------------------------------
*
* Function Name   : _mem_alloc_system_zero
* Returned Value  : None
* Comments        : allocates memory from the system, zeroed
*
*END*------------------------------------------------------------------*/

pointer _mem_alloc_system_zero
   (
      /* [IN] the size of the memory block */
      _mem_size size
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR  kernel_data;
   pointer                 result;
   _mqx_uint               error;

   _GET_KERNEL_DATA(kernel_data);
   _KLOGE2(KLOG_mem_alloc_system_zero, size);

   _INT_DISABLE();
   result = _mem_alloc_internal(size, SYSTEM_TD_PTR(kernel_data),
      (MEMPOOL_STRUCT_PTR)&kernel_data->KD_POOL, &error);

   /* update the memory allocation pointer in case a lower priority
   ** task was preempted inside _mem_alloc_internal
   */
   kernel_data->KD_POOL.POOL_ALLOC_CURRENT_BLOCK = 
      kernel_data->KD_POOL.POOL_FREE_LIST_PTR;

   _INT_ENABLE();
   
#if MQX_CHECK_ERRORS
   if (error != MQX_OK) {
      _task_set_error(error);
   } /* Endif */
#endif

   if (result != NULL) {
      _mem_zero(result, size);
   } /* Endif */

   _KLOGX3(KLOG_mem_alloc_system_zero, result, 
      kernel_data->KD_POOL.POOL_BLOCK_IN_ERROR);
   return(result);

} /* Endbody */

/* EOF */
