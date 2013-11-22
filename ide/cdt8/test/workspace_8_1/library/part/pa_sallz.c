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
*** File: pa_sallz.c
***
*** Comments :
***  This file contains the function for allocating a zero filled partition
*** block, owned by the system.
***
***
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "part.h"
#include "part_prv.h"

#if MQX_USE_PARTITIONS
/*FUNCTION*-----------------------------------------------------
* 
* Function Name    : _partition_alloc_system_zero
* Returned Value   : pointer
* Comments         :
*    Allocates zero filled block of memory owned by the system
*
*END*--------------------------------------------------------*/

pointer _partition_alloc_system_zero
   (
      /* [IN] the partition from which to obtain the memory block */
      _partition_id partition
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   PARTPOOL_STRUCT_PTR    partpool_ptr = (PARTPOOL_STRUCT_PTR)partition;
   pointer                result;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE2(KLOG_partition_alloc_system_zero, partition);

   result = _partition_alloc_internal(partpool_ptr, SYSTEM_TD_PTR(kernel_data));
#if MQX_CHECK_MEMORY_ALLOCATION_ERRORS
   if ( result == NULL ) {
      _KLOGX2(KLOG_partition_alloc_system_zero, result);
      return (result);
   } /* Endif */
#endif

   _mem_zero(result, (_mem_size)(partpool_ptr->BLOCK_SIZE -
      sizeof(INTERNAL_PARTITION_BLOCK_STRUCT))); 

   _KLOGX2(KLOG_partition_alloc_system_zero, result);
   return (result);

} /* Endbody */
#endif /* MQX_USE_PARTITIONS */

/* EOF */
