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
*** File: mem_extd.c
***
*** Comments :
***   This file contains the function that will add an additional block
*** of physical memory to the memory pool.
***
**************************************************************************
*END*********************************************************************/

#define __MEMORY_MANAGER_COMPILE__
#include "mqx_inc.h"
#include "mem_prv.h"

/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _mem_extend
* Returned Value   : _mqx_uint MQX_OK or a MQX error code.
* Comments         :
*   This function adds the specified memory area for use
* by the memory manager.
*
*END*--------------------------------------------------------*/

_mqx_uint _mem_extend
   (
      /* [IN] the address of the start of the memory pool addition */
      pointer    start_of_pool,
                
      /* [IN] the size of the memory pool addition */
      _mem_size  size
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   _mqx_uint              result;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE3(KLOG_mem_extend, start_of_pool, size);

   result = _mem_extend_pool_internal(start_of_pool, size, 
      (MEMPOOL_STRUCT_PTR)&kernel_data->KD_POOL);

   _KLOGX2(KLOG_mem_extend, result);

   return(result);

} /* Endbody */

/* EOF */
