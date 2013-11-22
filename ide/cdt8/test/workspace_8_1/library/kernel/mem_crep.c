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
*** File: mem_crep.c
***
*** Comments :
***   This file contains the function that initializes a memory pool.
***
**************************************************************************
*END*********************************************************************/

#define __MEMORY_MANAGER_COMPILE__
#include "mqx_inc.h"
#include "mem_prv.h"

/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _mem_create_pool
* Returned Value   : A handle to the memory pool or 
*                    NULL_MEM_POOL_ID on error.
* Comments         :
*   This function initializes a memory storage pool. Will set task error
* code if error occurs
* 
*END*---------------------------------------------------------*/

_mem_pool_id _mem_create_pool
   (
      /* [IN] the start of the memory pool */
      pointer    start,

      /* [IN] the size of the memory pool */
      _mem_size  size
   )
{ /* Body */
   _KLOGM(KERNEL_DATA_STRUCT_PTR  kernel_data;)
   MEMPOOL_STRUCT_PTR             mem_pool_ptr;
   uchar_ptr                      end;
   _mqx_uint                      error;

   _KLOGM(_GET_KERNEL_DATA(kernel_data);)
   _KLOGE3(KLOG_mem_create_pool, start, size);

#if MQX_CHECK_ERRORS
   if (size < MQX_MIN_MEMORY_POOL_SIZE) {
      error = MQX_MEM_POOL_TOO_SMALL;
      _task_set_error(error);
      _KLOGX4(KLOG_mem_create_pool, start, size, error);
      return NULL;
   } /* Endif */
#endif
   
   mem_pool_ptr = (MEMPOOL_STRUCT_PTR)_ALIGN_ADDR_TO_HIGHER_MEM(start);
   _mem_zero((pointer)mem_pool_ptr, (_mem_size)sizeof(MEMPOOL_STRUCT));

   end   = (uchar_ptr)start + size;
   start = (pointer)((uchar_ptr)mem_pool_ptr + sizeof(MEMPOOL_STRUCT));
   error = _mem_create_pool_internal(start, (pointer)end, mem_pool_ptr);

#if (MQX_CHECK_ERRORS)
   if (error != MQX_OK ) {
      _task_set_error(error);
      _KLOGX4(KLOG_mem_create_pool, start, size, error);
      return NULL;
   } /* Endif */
#endif
  
   return ((_mem_pool_id)mem_pool_ptr);

} /* Endbody */

/* EOF */
