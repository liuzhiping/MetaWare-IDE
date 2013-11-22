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
*** File: _mem_tesa.c
***
*** Comments :
***    This file contains the function that all memory pools, include the
*** system pool, validity.
*** It checks for incorrect checksums, and incorrect memory pointers.
*** This function can run concurrently with any other memory functions.
***
***
**************************************************************************
*END*********************************************************************/

#define __MEMORY_MANAGER_COMPILE__
#include "mqx_inc.h"
#include "mem_prv.h"

/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : __mem_test_all
* Returned Value   : _mqx_uint MQX_OK or a MQX error coded.
*    If system pool is in error, pool_error_ptr will be set to
*    null but error code will be set appropriately.
*    
* Comments         : This function checks the all memory pool 
*   for any errors. The task error code is set if an error is
*   encountered
*
*END*--------------------------------------------------------*/

_mqx_uint _mem_test_all
   (
      /* [OUT] - the pool in error */
      _mem_pool_id _PTR_ pool_error_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   MEMPOOL_STRUCT_PTR     pool_ptr;
   _mqx_uint              result;
   

   _GET_KERNEL_DATA(kernel_data);

   /* Use a semaphore to protect the list of pools */
   _lwsem_wait((LWSEM_STRUCT_PTR)&kernel_data->MEM_COMP.SEM);

   /* Make sure that the queue of memory pools is ok */
   result = _queue_test((QUEUE_STRUCT_PTR)&kernel_data->MEM_COMP.POOLS, 
      (pointer _PTR_)pool_error_ptr);

   _lwsem_post((LWSEM_STRUCT_PTR)&kernel_data->MEM_COMP.SEM);

   if (result != MQX_OK) {
      return(result);
   } /* Endif */
 
   /* Now test application pools */
   _lwsem_wait((LWSEM_STRUCT_PTR)&kernel_data->MEM_COMP.SEM);
   pool_ptr = (MEMPOOL_STRUCT_PTR)((pointer)kernel_data->MEM_COMP.POOLS.NEXT);
   while (pool_ptr != (MEMPOOL_STRUCT_PTR)
      ((pointer)&kernel_data->MEM_COMP.POOLS))
   {
      result = _mem_test_pool(pool_ptr);
      if (result != MQX_OK ) {
         break;
      } /* Endif */
      pool_ptr = (MEMPOOL_STRUCT_PTR)((pointer)pool_ptr->LINK.NEXT);
   } /* Endwhile */

   _lwsem_post((LWSEM_STRUCT_PTR)&kernel_data->MEM_COMP.SEM);

   *pool_error_ptr = (_mem_pool_id)pool_ptr;
   return(result);
      
} /* Endbody */

/* EOF */
