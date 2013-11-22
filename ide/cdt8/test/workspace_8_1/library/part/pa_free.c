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
*** File: pa_free.c
***
*** Comments :
***  This file contains the function for freeing a partition block.
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
* Function Name    : _partition_free
* Returned Value   : MQX_OK or an error code
* Comments         :
*   This function frees the given block of memory.
*
*END*---------------------------------------------------------*/

_mqx_uint _partition_free
   (
      /* [IN] the address of the memory block to free */
      pointer mem_ptr
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR              kernel_data;
   PARTPOOL_STRUCT_PTR                 partpool_ptr;
   INTERNAL_PARTITION_BLOCK_STRUCT_PTR block_ptr;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE2(KLOG_partition_free, mem_ptr);

   block_ptr = (INTERNAL_PARTITION_BLOCK_STRUCT_PTR)
      ((uchar_ptr)mem_ptr - sizeof(INTERNAL_PARTITION_BLOCK_STRUCT));

#if MQX_CHECK_VALIDITY
   if (! VALID_PARTITION_CHECKSUM(block_ptr)) {
      _KLOGX2(KLOG_partition_free, PARTITION_BLOCK_INVALID_CHECKSUM);
      return(PARTITION_BLOCK_INVALID_CHECKSUM);
   } /* Endif */
#endif

#if MQX_CHECK_ERRORS
   if (block_ptr->TASK_ID != SYSTEM_TD_PTR(kernel_data)->TASK_ID) {
      /* Let system blocks be freed by anyone */
      if (block_ptr->TASK_ID != kernel_data->ACTIVE_PTR->TASK_ID) {
         _KLOGX2(KLOG_partition_free, MQX_NOT_RESOURCE_OWNER);
         return(MQX_NOT_RESOURCE_OWNER);
      } /* Endif */
   } /* Endif */
#endif

   partpool_ptr = block_ptr->LINK.PARTITION_PTR;
   _INT_DISABLE();

#if MQX_CHECK_VALIDITY
   if (partpool_ptr->POOL.VALID != PARTITION_VALID) {
      _int_enable();
      _KLOGX2(KLOG_partition_free, PARTITION_INVALID);
      return(PARTITION_INVALID);
   } /* Endif */
#endif

   block_ptr->TASK_ID = 0;
   block_ptr->LINK.NEXT_BLOCK_PTR = partpool_ptr->FREE_LIST_PTR;
   partpool_ptr->FREE_LIST_PTR    = block_ptr;

   ++partpool_ptr->AVAILABLE;
   CALC_PARTITION_CHECKSUM(block_ptr);

   _INT_ENABLE();
   _KLOGX2(KLOG_partition_free, MQX_OK);
   return(MQX_OK);

} /* Endbody */
#endif /* MQX_USE_PARTITIONS */

/* EOF */
