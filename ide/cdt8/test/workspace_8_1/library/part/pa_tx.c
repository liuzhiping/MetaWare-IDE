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
*** File: pa_tx.c
***
*** Comments :
***  This file contains the function for transfering ownership of a partition
*** memory block.
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
* Function Name    : _partition_transfer
* Returned Value   : _mqx_uint
*   MQX_OK is returned if no errors otherwise an MQX error code
*   is returned
* Comments         :
*    transfers ownership of a partition
*
*END*---------------------------------------------------------*/

_mqx_uint _partition_transfer
   (
      /* [IN] the block to transfer */
      pointer  mem_ptr,

      /* [IN] the new owner task */
      _task_id new_owner_id
   )
{ /* Body */
   KERNEL_DATA_STRUCT_PTR              kernel_data;
   TD_STRUCT_PTR                       new_td_ptr;
   INTERNAL_PARTITION_BLOCK_STRUCT_PTR block_ptr;

   _GET_KERNEL_DATA(kernel_data);

   _KLOGE3(KLOG_partition_transfer, mem_ptr, new_owner_id);

   block_ptr = (INTERNAL_PARTITION_BLOCK_STRUCT_PTR)((uchar_ptr)mem_ptr -
      sizeof(INTERNAL_PARTITION_BLOCK_STRUCT));

#if MQX_CHECK_VALIDITY
   if (! VALID_PARTITION_CHECKSUM(block_ptr)) {
      _KLOGX2(KLOG_partition_transfer, PARTITION_BLOCK_INVALID_CHECKSUM);
      return(PARTITION_BLOCK_INVALID_CHECKSUM);
   } /* Endif */
#endif

   new_td_ptr = (TD_STRUCT_PTR)_task_get_td(new_owner_id);
#if MQX_CHECK_ERRORS
   if (new_td_ptr == NULL) {
      _KLOGX2(KLOG_partition_transfer, PARTITION_INVALID_TASK_ID);
      return(PARTITION_INVALID_TASK_ID);
   } /* Endif */
#endif

   _INT_DISABLE();
   block_ptr->TASK_ID = new_owner_id;
   CALC_PARTITION_CHECKSUM(block_ptr);
   _INT_ENABLE();
  
   _KLOGX2(KLOG_partition_transfer, MQX_OK);
   return(MQX_OK);
   
} /* Endbody */
#endif /* MQX_USE_PARTITIONS */

/* EOF */
