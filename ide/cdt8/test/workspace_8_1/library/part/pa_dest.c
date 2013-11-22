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
*** File: pa_dest.c
***
*** Comments :
***  This file contains the function for creating a partition.
***
***
**************************************************************************
*END*********************************************************************/

#include "mqx_inc.h"
#include "part.h"
#include "part_prv.h"

#if MQX_USE_PARTITIONS
/*FUNCTION*--------------------------------------------------------------
* 
* Function Name    : _partition_destroy
* Returned Value   : MQX_OK or an error code
* Comments         :
*    Destroys a partition allocated in kernel memory
*
*END*-----------------------------------------------------------------*/

_mqx_uint _partition_destroy
   ( 
      /* [IN] the partition to destory */
      _partition_id partition
   )
{ /* Body */
   /* Start CR 532 */
   /* _KLOGM(KERNEL_DATA_STRUCT_PTR kernel_data;) */
   KERNEL_DATA_STRUCT_PTR kernel_data;
   /* End   CR 532 */
   PARTPOOL_STRUCT_PTR           partpool_ptr;
   PARTPOOL_BLOCK_STRUCT_PTR     current_ptr;
   PARTPOOL_BLOCK_STRUCT_PTR     next_ptr;
   _mqx_uint                     result;
   /* Start CR 532 */
   PARTITION_COMPONENT_STRUCT_PTR part_component_ptr;

   /* _KLOGM(_GET_KERNEL_DATA(kernel_data);) */
   _GET_KERNEL_DATA(kernel_data);
   /* End   CR 532 */

   _KLOGE2(KLOG_partition_destroy, partition);

   /* Start CR 532 */
   part_component_ptr = (PARTITION_COMPONENT_STRUCT_PTR)
      kernel_data->KERNEL_COMPONENTS[KERNEL_PARTITIONS];
   /* End   CR 532 */

   partpool_ptr = (PARTPOOL_STRUCT_PTR)partition;
   
#if MQX_CHECK_ERRORS
   /* Start CR 532 */
   if (part_component_ptr == NULL){
      _KLOGX2(KLOG_partition_destroy, MQX_COMPONENT_DOES_NOT_EXIST);
      return MQX_COMPONENT_DOES_NOT_EXIST;
   } /* Endif */
   /* End   CR 532 */

   if (partpool_ptr->POOL.VALID != PARTITION_VALID) {
      _KLOGX2(KLOG_partition_destroy, MQX_INVALID_PARAMETER);
      return MQX_INVALID_PARAMETER;
   } /* Endif */

   if (partpool_ptr->PARTITION_TYPE != PARTITION_DYNAMIC) {
      _KLOGX2(KLOG_partition_destroy, PARTITION_INVALID_TYPE);
      return PARTITION_INVALID_TYPE;
   } /* Endif */
#endif

   if (partpool_ptr->TOTAL_BLOCKS != partpool_ptr->AVAILABLE) {
      _KLOGX2(KLOG_partition_destroy, PARTITION_ALL_BLOCKS_NOT_FREE);
      return PARTITION_ALL_BLOCKS_NOT_FREE;
   } /* Endif */

   /* Start CR 532 */
   /* Remove the partition from the list of partitions maintained by MQX */
   _int_disable();
   _QUEUE_REMOVE(&part_component_ptr->PARTITIONS, partpool_ptr);
   _int_enable();
   /* End  CR 532 */

   partpool_ptr->POOL.VALID = 0;

   /* Free any extensions */
   current_ptr = partpool_ptr->POOL.NEXT_POOL_PTR;
   while(current_ptr) {
      next_ptr = current_ptr->NEXT_POOL_PTR;
      _mem_free(current_ptr);
      current_ptr = next_ptr;
   } /* Endwhile */  

   result = _mem_free(partpool_ptr);

   _KLOGX2(KLOG_partition_destroy, result);

   return result;

} /* Endbody */
#endif /* MQX_USE_PARTITIONS */

/* EOF */
